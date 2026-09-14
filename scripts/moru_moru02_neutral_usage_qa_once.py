from __future__ import annotations

import hashlib
import json
import math
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont
from scipy.ndimage import distance_transform_edt

ROOT = Path.cwd()
MASTER = ROOT / "design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png"
OUT_ROOT = ROOT / "design/reference/moru-v2/native-usage-context-v1"
TMP_ROOT = Path("/tmp/moru-native-usage-context-v1")
MANIFEST_PATH = ROOT / "design/export-spec/moru-production-manifest.v2.json"
EXPECTED_MASTER_SHA = "b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692"
DENSITY = 2.625  # API 30 / prior DailyTown visual-QA reference density


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def alpha_bbox(img: Image.Image, threshold: int = 8) -> tuple[int, int, int, int]:
    a = np.asarray(img.getchannel("A"))
    ys, xs = np.nonzero(a > threshold)
    if len(xs) == 0:
        raise RuntimeError("empty alpha support")
    return int(xs.min()), int(ys.min()), int(xs.max()) + 1, int(ys.max()) + 1


def clamp(v: int, lo: int, hi: int) -> int:
    return max(lo, min(hi, v))


def make_crop(subject: tuple[int, int, int, int], xf0: float, yf0: float, xf1: float, yf1: float, pad: float = 0.02) -> tuple[int, int, int, int]:
    x0, y0, x1, y1 = subject
    sw, sh = x1 - x0, y1 - y0
    px, py = int(round(sw * pad)), int(round(sh * pad))
    return (
        clamp(int(round(x0 + sw * xf0)) - px, 0, 1280),
        clamp(int(round(y0 + sh * yf0)) - py, 0, 1600),
        clamp(int(round(x0 + sw * xf1)) + px, 0, 1280),
        clamp(int(round(y0 + sh * yf1)) + py, 0, 1600),
    )


def bleed_transparent_rgb(img: Image.Image, radius: int = 16) -> Image.Image:
    arr = np.array(img, dtype=np.uint8)
    alpha = arr[..., 3]
    visible = alpha > 0
    transparent = ~visible
    if not visible.any() or not transparent.any():
        return img
    dist, indices = distance_transform_edt(transparent, return_indices=True)
    ring = transparent & (dist <= radius)
    yy, xx = indices
    nearest = arr[yy, xx, :3]
    arr[..., :3][ring] = nearest[ring]
    return Image.fromarray(arr, "RGBA")


def fit_crop(source: Image.Image, crop_box: tuple[int, int, int, int], target: tuple[int, int], fill_w: float, fill_h: float, anchor: str) -> tuple[Image.Image, dict]:
    crop = source.crop(crop_box)
    cw, ch = crop.size
    tw, th = target
    scale = min((tw * fill_w) / cw, (th * fill_h) / ch)
    rw, rh = max(1, int(round(cw * scale))), max(1, int(round(ch * scale)))
    resized = crop.resize((rw, rh), Image.Resampling.LANCZOS)
    resized = bleed_transparent_rgb(resized, radius=max(8, int(round(scale * 4))))
    canvas = Image.new("RGBA", target, (0, 0, 0, 0))
    if anchor == "bottom_center":
        ox = (tw - rw) // 2
        oy = th - rh - max(8, int(round(th * 0.02)))
    elif anchor == "top_center":
        ox = (tw - rw) // 2
        oy = max(8, int(round(th * 0.02)))
    else:
        ox = (tw - rw) // 2
        oy = (th - rh) // 2
    canvas.alpha_composite(resized, (ox, oy))
    return canvas, {
        "source_crop_xyxy": list(crop_box),
        "source_crop_dimensions": [cw, ch],
        "scale": scale,
        "resized_dimensions": [rw, rh],
        "paste_offset": [ox, oy],
        "anchor": anchor,
    }


def mapped_feature_box(subject: tuple[int, int, int, int], feature_norm: tuple[float, float, float, float], transform: dict) -> tuple[float, float, float, float]:
    sx0, sy0, sx1, sy1 = subject
    sw, sh = sx1 - sx0, sy1 - sy0
    fx0 = sx0 + feature_norm[0] * sw
    fy0 = sy0 + feature_norm[1] * sh
    fx1 = sx0 + feature_norm[2] * sw
    fy1 = sy0 + feature_norm[3] * sh
    cx0, cy0, _, _ = transform["source_crop_xyxy"]
    scale = transform["scale"]
    ox, oy = transform["paste_offset"]
    return (
        ox + (fx0 - cx0) * scale,
        oy + (fy0 - cy0) * scale,
        ox + (fx1 - cx0) * scale,
        oy + (fy1 - cy0) * scale,
    )


def visible_intersection(box: tuple[float, float, float, float], target: tuple[int, int]) -> tuple[float, float, float, float]:
    x0, y0, x1, y1 = box
    return max(0.0, x0), max(0.0, y0), min(float(target[0]), x1), min(float(target[1]), y1)


def box_height(box: tuple[float, float, float, float]) -> float:
    return max(0.0, box[3] - box[1])


def edge_bleed_error(img: Image.Image, radius: int = 8) -> float:
    arr = np.asarray(img, dtype=np.int16)
    alpha = arr[..., 3]
    visible = alpha > 0
    transparent = ~visible
    if not transparent.any():
        return 0.0
    dist, indices = distance_transform_edt(transparent, return_indices=True)
    ring = transparent & (dist <= radius)
    if not ring.any():
        return 0.0
    yy, xx = indices
    nearest = arr[yy, xx, :3]
    delta = np.abs(arr[..., :3] - nearest).mean(axis=2)
    return float(delta[ring].mean())


def composite_cell(asset: Image.Image, logical_dp: int, bg: str) -> Image.Image:
    physical = max(1, int(round(logical_dp * DENSITY)))
    thumb = asset.resize((physical, physical), Image.Resampling.LANCZOS)
    cell = Image.new("RGBA", (240, 240), (246, 237, 220, 255))
    d = ImageDraw.Draw(cell)
    if bg == "dark":
        d.rectangle((0, 0, 239, 239), fill=(43, 42, 37, 255))
    elif bg == "map":
        d.rectangle((0, 0, 239, 239), fill=(205, 211, 185, 255))
        for x in range(-80, 300, 52):
            d.line((x, 0, x + 120, 240), fill=(235, 226, 191, 255), width=10)
        for y in range(25, 240, 60):
            d.line((0, y, 240, y - 22), fill=(144, 164, 129, 255), width=4)
    ox = (240 - physical) // 2
    oy = (240 - physical) // 2
    cell.alpha_composite(thumb, (ox, oy))
    d.rectangle((0, 211, 239, 239), fill=(255, 255, 255, 218))
    d.text((8, 216), f"{logical_dp}dp @ {DENSITY:g}x / {bg}", fill=(25, 25, 25, 255))
    return cell


def save_mobile_board(assets: dict[str, Image.Image], path: Path) -> None:
    tests = [
        ("map_avatar", [48, 56, 64]),
        ("hud_portrait", [56, 64, 72]),
        ("journal_crop", [56, 64, 72]),
    ]
    backgrounds = ["cream", "dark", "map"]
    cols = 9
    rows = len(tests)
    board = Image.new("RGBA", (cols * 240, rows * 270), (248, 244, 234, 255))
    draw = ImageDraw.Draw(board)
    for r, (name, sizes) in enumerate(tests):
        draw.text((8, r * 270 + 4), name, fill=(30, 30, 30, 255))
        c = 0
        for dp in sizes:
            for bg in backgrounds:
                cell = composite_cell(assets[name], dp, bg)
                board.alpha_composite(cell, (c * 240, r * 270 + 28))
                c += 1
    board.convert("RGB").save(path, quality=95)


def save_usage_board(assets: dict[str, Image.Image], path: Path) -> None:
    slots = [
        ("map_avatar", (360, 360)),
        ("hud_portrait", (440, 440)),
        ("encounter_halfbody", (360, 450)),
        ("result_large", (360, 450)),
        ("companion_portrait", (360, 450)),
        ("journal_crop", (440, 440)),
    ]
    board = Image.new("RGBA", (1500, 1040), (246, 237, 220, 255))
    draw = ImageDraw.Draw(board)
    positions = [(30, 55), (520, 55), (1010, 55), (30, 550), (520, 550), (1010, 550)]
    for (name, box), pos in zip(slots, positions):
        asset = assets[name].copy()
        asset.thumbnail(box, Image.Resampling.LANCZOS)
        x, y = pos
        draw.rounded_rectangle((x - 10, y - 28, x + box[0] + 10, y + box[1] + 12), radius=18, fill=(255, 252, 245, 255), outline=(205, 194, 173, 255), width=2)
        draw.text((x, y - 22), name, fill=(40, 40, 40, 255))
        board.alpha_composite(asset, (x + (box[0] - asset.width) // 2, y + (box[1] - asset.height) // 2))
    board.convert("RGB").save(path, quality=95)


def main() -> None:
    if sha256(MASTER) != EXPECTED_MASTER_SHA:
        raise SystemExit("ACCEPTED_MASTER_HASH_MISMATCH")
    source = Image.open(MASTER).convert("RGBA")
    if source.size != (1280, 1600):
        raise SystemExit(f"ACCEPTED_MASTER_DIMENSION_MISMATCH {source.size}")
    subject = alpha_bbox(source)
    sx0, sy0, sx1, sy1 = subject
    sw, sh = sx1 - sx0, sy1 - sy0

    # Feature boxes normalized to the accepted subject silhouette, inherited from the committed handoff contract.
    features = {
        "sprout": (0.266, 0.000, 0.861, 0.170),
        "face": (0.274, 0.238, 0.745, 0.406),
        "scarf": (0.328, 0.406, 0.712, 0.518),
        "satchel": (0.000, 0.500, 0.405, 0.731),
        "boots_base": (0.150, 0.830, 0.860, 1.000),
    }

    specs = {
        "map_avatar": {"target": (512, 512), "crop": (0.00, 0.00, 1.00, 1.00), "fill": (0.90, 0.94), "anchor": "bottom_center"},
        "hud_portrait": {"target": (768, 768), "crop": (0.00, 0.00, 1.00, 0.60), "fill": (0.92, 0.92), "anchor": "top_center"},
        "encounter_halfbody": {"target": (1024, 1280), "crop": (0.00, 0.00, 1.00, 0.78), "fill": (0.90, 0.94), "anchor": "top_center"},
        "result_large": {"target": (1280, 1600), "crop": (0.00, 0.00, 1.00, 1.00), "fill": (1.00, 1.00), "anchor": "identity_copy"},
        "companion_portrait": {"target": (1024, 1280), "crop": (0.02, 0.00, 0.98, 0.70), "fill": (0.92, 0.94), "anchor": "top_center"},
        "journal_crop": {"target": (768, 768), "crop": (0.08, 0.00, 0.92, 0.56), "fill": (0.94, 0.92), "anchor": "top_center"},
    }

    if TMP_ROOT.exists():
        shutil.rmtree(TMP_ROOT)
    candidate_dir = TMP_ROOT / "candidate"
    qa_dir = TMP_ROOT / "qa"
    candidate_dir.mkdir(parents=True)
    qa_dir.mkdir(parents=True)

    assets: dict[str, Image.Image] = {}
    records: dict[str, dict] = {}
    crop_signatures = set()

    for name, spec in specs.items():
        target = tuple(spec["target"])
        if name == "result_large":
            asset = source.copy()
            transform = {
                "source_crop_xyxy": [0, 0, 1280, 1600],
                "source_crop_dimensions": [1280, 1600],
                "scale": 1.0,
                "resized_dimensions": [1280, 1600],
                "paste_offset": [0, 0],
                "anchor": "identity_copy",
            }
        else:
            crop_box = make_crop(subject, *spec["crop"], pad=0.02)
            asset, transform = fit_crop(source, crop_box, target, spec["fill"][0], spec["fill"][1], spec["anchor"])
        assets[name] = asset
        crop_signatures.add(tuple(transform["source_crop_xyxy"]))
        filename = f"companion_moru_v2_{name}_neutral_LIGHT_base.png"
        out = candidate_dir / filename
        asset.save(out, optimize=True)
        bbox = alpha_bbox(asset)
        a = np.asarray(asset.getchannel("A"))
        edge_error = edge_bleed_error(asset)
        feature_boxes = {k: visible_intersection(mapped_feature_box(subject, v, transform), target) for k, v in features.items()}
        records[name] = {
            "path": f"candidate/{filename}",
            "dimensions": list(target),
            "mode": "RGBA",
            "sha256": sha256(out),
            "alpha_minmax": [int(a.min()), int(a.max())],
            "alpha_bbox_xyxy": list(bbox),
            "content_coverage": {
                "width": (bbox[2] - bbox[0]) / target[0],
                "height": (bbox[3] - bbox[1]) / target[1],
            },
            "edge_bleed_mean_abs_rgb_error": edge_error,
            "transform": transform,
            "feature_boxes": {k: [round(x, 3) for x in v] for k, v in feature_boxes.items()},
        }

    # Logical dp review metrics. Prior reference candidates failed because full-canvas content occupied only ~0.51-0.53 of the canvas.
    mobile_checks = {}
    for name, sizes in {"map_avatar": [48, 56, 64], "hud_portrait": [56, 64, 72], "journal_crop": [56, 64, 72]}.items():
        rec = records[name]
        target_w = rec["dimensions"][0]
        coverage_h = rec["content_coverage"]["height"]
        per_size = []
        for dp in sizes:
            factor = dp / target_w
            face_h = box_height(tuple(rec["feature_boxes"]["face"])) * factor
            sprout_h = box_height(tuple(rec["feature_boxes"]["sprout"])) * factor
            scarf_h = box_height(tuple(rec["feature_boxes"]["scarf"])) * factor
            per_size.append({
                "dp": dp,
                "physical_px_at_2_625x": int(round(dp * DENSITY)),
                "visible_content_height_dp": round(coverage_h * dp, 3),
                "face_height_dp": round(face_h, 3),
                "sprout_height_dp": round(sprout_h, 3),
                "scarf_height_dp": round(scarf_h, 3),
            })
        mobile_checks[name] = per_size

    checks = {
        "accepted_master_hash": sha256(MASTER) == EXPECTED_MASTER_SHA,
        "six_contexts_generated": len(records) == 6,
        "all_rgba_genuine_alpha": all(r["alpha_minmax"] == [0, 255] for r in records.values()),
        "all_dimensions_exact": all(records[n]["dimensions"] == list(specs[n]["target"]) for n in specs),
        "context_framing_not_identical": len(crop_signatures) >= 5,
        "transparent_side_edge_bleed": all(r["edge_bleed_mean_abs_rgb_error"] <= 1.0 for r in records.values()),
        "map_mobile_content": mobile_checks["map_avatar"][0]["visible_content_height_dp"] >= 42.0,
        "map_sprout": mobile_checks["map_avatar"][0]["sprout_height_dp"] >= 6.0,
        "map_face_negative_space_proxy": mobile_checks["map_avatar"][0]["face_height_dp"] >= 6.0,
        "hud_mobile_content": mobile_checks["hud_portrait"][0]["visible_content_height_dp"] >= 48.0,
        "hud_face": mobile_checks["hud_portrait"][0]["face_height_dp"] >= 12.0,
        "hud_sprout": mobile_checks["hud_portrait"][0]["sprout_height_dp"] >= 10.0,
        "hud_scarf": mobile_checks["hud_portrait"][0]["scarf_height_dp"] >= 7.0,
        "journal_mobile_content": mobile_checks["journal_crop"][0]["visible_content_height_dp"] >= 46.0,
        "journal_face": mobile_checks["journal_crop"][0]["face_height_dp"] >= 12.0,
        "journal_scarf": mobile_checks["journal_crop"][0]["scarf_height_dp"] >= 7.0,
        "encounter_includes_satchel": box_height(tuple(records["encounter_halfbody"]["feature_boxes"]["satchel"])) > 0,
        "result_full_sprout": box_height(tuple(records["result_large"]["feature_boxes"]["sprout"])) > 0,
        "result_full_boots": box_height(tuple(records["result_large"]["feature_boxes"]["boots_base"])) > 0,
        "companion_face": box_height(tuple(records["companion_portrait"]["feature_boxes"]["face"])) > 0,
        "result_is_exact_accepted_master": records["result_large"]["sha256"] == EXPECTED_MASTER_SHA,
    }

    save_mobile_board(assets, qa_dir / "moru_native_usage_mobile_context_qa_v1.png")
    save_usage_board(assets, qa_dir / "moru_native_usage_framing_qa_v1.png")

    passed = all(checks.values())
    verdict = "PASS_MORU_02_NEUTRAL_USAGE_V1" if passed else "FAIL_MORU_02_NEUTRAL_USAGE_V1"
    result = {
        "version": "moru-native-usage-context-v1-2026-09-14",
        "verdict": verdict,
        "classification": "NATIVE_USAGE_QA_CANDIDATE_NOT_RUNTIME_ACTIVE",
        "source": {
            "path": str(MASTER.relative_to(ROOT)),
            "sha256": EXPECTED_MASTER_SHA,
            "dimensions": [1280, 1600],
            "mode": "RGBA",
            "subject_alpha_bbox_xyxy": list(subject),
        },
        "semantic_state": {"expression": "neutral", "lighting": "LIGHT", "affinity": "base", "motion": "static"},
        "density_reference": DENSITY,
        "assets": records,
        "mobile_checks": mobile_checks,
        "checks": checks,
        "qa_artifacts": {},
        "generation_used": False,
        "new_semantic_microdetail": False,
        "expression_fan_out": False,
        "lighting_fan_out": False,
        "affinity_fan_out": False,
        "runtime_activation": False,
    }
    for qname in ["moru_native_usage_mobile_context_qa_v1.png", "moru_native_usage_framing_qa_v1.png"]:
        qp = qa_dir / qname
        result["qa_artifacts"][qname] = {"sha256": sha256(qp), "bytes": qp.stat().st_size}

    (qa_dir / "moru_native_usage_acceptance_metrics_v1.json").write_text(json.dumps(result, indent=2) + "\n")

    readme = f"""# Moru native usage-context v1\n\nStatus: **{verdict}**\n\nSource authority: accepted `native-master-v1` PNG `{EXPECTED_MASTER_SHA}`.\n\nThis family contains only `neutral / LIGHT / base / static` deterministic usage-context derivatives. No expression, lighting or affinity fan-out and no runtime activation are included.\n\nThe six usage contexts follow `docs/design/MORU_PRODUCTION_EXPORT_V2.md`. Map/HUD/journal are additionally reviewed at their required dp sizes using the established 2.625x Android QA density reference and cream/dark/map-heavy surfaces.\n\nIf this gate passes, the next work remains expression/lighting/affinity family production plus the rest of MORU-02; MORU-03/runtime activation stays blocked.\n"""
    (TMP_ROOT / "README.md").write_text(readme)

    print(json.dumps(result, indent=2))
    Path("/tmp/moru02-result.json").write_text(json.dumps(result, indent=2) + "\n")

    if passed:
        if OUT_ROOT.exists():
            shutil.rmtree(OUT_ROOT)
        shutil.copytree(TMP_ROOT, OUT_ROOT)
        prod = json.loads(MANIFEST_PATH.read_text())
        prod["status"]["transparent_master_export"] = "neutral_usage_context_family_pass_semantic_family_pending"
        prod["status"]["mobile_qa"] = "neutral_usage_context_mobile_qa_pass_expression_lighting_affinity_pending"
        prod["native_master_handoff"]["status"] = "HANDOFF_CONSUMED_NATIVE_NEUTRAL_V1_PASS"
        prod["native_neutral_master_v1"]["fan_out"] = "neutral_usage_context_v1_pass"
        prod["human_gates"]["identity_lock"] = "native_neutral_v1_pass"
        prod["native_usage_context_v1"] = {
            "status": verdict,
            "path": "design/reference/moru-v2/native-usage-context-v1/",
            "semantic_state": result["semantic_state"],
            "assets": {name: {"dimensions": rec["dimensions"], "sha256": rec["sha256"]} for name, rec in records.items()},
            "qa": result["qa_artifacts"],
            "runtime_activation": False,
            "next": "produce/validate expressions, LIGHT/WARM_DUSK/DARK and affinity stages before MORU-03",
        }
        MANIFEST_PATH.write_text(json.dumps(prod, indent=2) + "\n")
    else:
        failure = ROOT / "design/reference/moru-v2/native-master-v1/qa/FAILURE_MORU_02_NEUTRAL_USAGE_V1.md"
        failed = [k for k, v in checks.items() if not v]
        failure.write_text("# Moru neutral usage-context QA failure\n\nVerdict: **FAIL_MORU_02_NEUTRAL_USAGE_V1**\n\nFailed checks:\n" + "\n".join(f"- `{x}`" for x in failed) + "\n\nNo usage-context candidate binaries were persisted.\n")
        raise SystemExit(2)


if __name__ == "__main__":
    main()
