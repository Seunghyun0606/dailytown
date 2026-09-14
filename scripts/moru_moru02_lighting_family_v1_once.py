from __future__ import annotations

import hashlib
import json
import math
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
BRANCH = "design/production-consolidated-v2"
CANONICAL = ROOT / "design/reference/baseline-v2/moru_candidate3_canonical_baseline_v2.png"
EXPECTED_CANONICAL_SHA = "541b53464bbc589d86323faa3a7dbc8790cf9ab9573e0dd66812986c4a430500"
USAGE_ROOT = ROOT / "design/reference/moru-v2/native-usage-context-v1"
USAGE_CANDIDATE = USAGE_ROOT / "candidate"
USAGE_METRICS = USAGE_ROOT / "qa/moru_native_usage_acceptance_metrics_v1.json"
OUT_ROOT = ROOT / "design/reference/moru-v2/native-lighting-family-v1"
TMP_ROOT = ROOT / ".moru-lighting-v1-tmp"
MANIFEST_PATH = ROOT / "design/export-spec/moru-production-manifest.v2.json"

USAGES = {
    "map_avatar": {
        "file": "companion_moru_v2_map_avatar_neutral_LIGHT_base.png",
        "sha": "098df5b9b4b8e9cfe215c7fc8e9f1e9129b5673b7dc168893c5ea185f849d531",
        "dims": (512, 512),
        "review_dp": [48, 56, 64],
    },
    "hud_portrait": {
        "file": "companion_moru_v2_hud_portrait_neutral_LIGHT_base.png",
        "sha": "c67bac8dd8eb7e8cd13b8434b2cf92ce2ab6b82c657291109230a2a672af79ab",
        "dims": (768, 768),
        "review_dp": [56, 64, 72],
    },
    "encounter_halfbody": {
        "file": "companion_moru_v2_encounter_halfbody_neutral_LIGHT_base.png",
        "sha": "00a643aa14661a9e8b511854b33c7f7f19c04230b781c786ab592e0504dd46e5",
        "dims": (1024, 1280),
        "review_dp": [],
    },
    "companion_portrait": {
        "file": "companion_moru_v2_companion_portrait_neutral_LIGHT_base.png",
        "sha": "b57d5732a1f65a0b1cc5f108e267c422bd0fcfdbd144c403c23627de558e6b14",
        "dims": (1024, 1280),
        "review_dp": [],
    },
    "journal_crop": {
        "file": "companion_moru_v2_journal_crop_neutral_LIGHT_base.png",
        "sha": "cbcae8d5a79bc5c68368fe0f36f57fbe385d3c35f5c069ce87a2224ba85540b4",
        "dims": (768, 768),
        "review_dp": [56, 64, 72],
    },
    "result_large": {
        "file": "companion_moru_v2_result_large_neutral_LIGHT_base.png",
        "sha": "b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692",
        "dims": (1280, 1600),
        "review_dp": [],
    },
}

LIGHTING_CROPS = {
    "LIGHT": (27, 950, 198, 1142),
    "WARM_DUSK": (202, 950, 379, 1142),
    "DARK": (382, 950, 555, 1142),
}

DENSITY = 2.625


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def srgb_to_linear(x: np.ndarray) -> np.ndarray:
    x = x / 255.0
    return np.where(x <= 0.04045, x / 12.92, ((x + 0.055) / 1.055) ** 2.4)


def linear_to_srgb(x: np.ndarray) -> np.ndarray:
    x = np.clip(x, 0.0, 1.0)
    return np.where(x <= 0.0031308, x * 12.92, 1.055 * (x ** (1 / 2.4)) - 0.055)


def rgb_to_lab(rgb: np.ndarray) -> np.ndarray:
    lin = srgb_to_linear(rgb.astype(np.float64))
    m = np.array([
        [0.4124564, 0.3575761, 0.1804375],
        [0.2126729, 0.7151522, 0.0721750],
        [0.0193339, 0.1191920, 0.9503041],
    ])
    xyz = lin @ m.T
    xyz /= np.array([0.95047, 1.0, 1.08883])
    eps = 216 / 24389
    k = 24389 / 27
    f = np.where(xyz > eps, np.cbrt(xyz), (k * xyz + 16) / 116)
    L = 116 * f[..., 1] - 16
    a = 500 * (f[..., 0] - f[..., 1])
    b = 200 * (f[..., 1] - f[..., 2])
    return np.stack([L, a, b], axis=-1)


def lab_to_rgb(lab: np.ndarray) -> np.ndarray:
    fy = (lab[..., 0] + 16) / 116
    fx = fy + lab[..., 1] / 500
    fz = fy - lab[..., 2] / 200
    eps = 216 / 24389
    k = 24389 / 27
    def invf(t):
        t3 = t ** 3
        return np.where(t3 > eps, t3, (116 * t - 16) / k)
    xyz = np.stack([invf(fx), invf(fy), invf(fz)], axis=-1)
    xyz *= np.array([0.95047, 1.0, 1.08883])
    mi = np.array([
        [ 3.2404542, -1.5371385, -0.4985314],
        [-0.9692660,  1.8760108,  0.0415560],
        [ 0.0556434, -0.2040259,  1.0572252],
    ])
    lin = xyz @ mi.T
    srgb = linear_to_srgb(lin)
    return np.clip(np.round(srgb * 255.0), 0, 255).astype(np.uint8)


def relative_luminance(rgb: np.ndarray) -> np.ndarray:
    lin = srgb_to_linear(rgb.astype(np.float64))
    return 0.2126 * lin[..., 0] + 0.7152 * lin[..., 1] + 0.0722 * lin[..., 2]


def crop_foreground_stats(im: Image.Image, box: tuple[int, int, int, int]) -> dict:
    crop = np.asarray(im.crop(box).convert("RGB"), dtype=np.uint8)
    h, w = crop.shape[:2]
    border = np.concatenate([
        crop[:6].reshape(-1, 3), crop[-6:].reshape(-1, 3),
        crop[:, :6].reshape(-1, 3), crop[:, -6:].reshape(-1, 3)
    ], axis=0)
    bg = np.median(border, axis=0)
    dist = np.linalg.norm(crop.astype(np.float64) - bg[None, None, :], axis=2)
    sat = crop.max(axis=2).astype(np.float64) - crop.min(axis=2).astype(np.float64)
    yy = np.arange(h)[:, None]
    central = (yy < int(h * 0.90))
    mask = central & ((dist > 24.0) | (sat > 28.0))
    # Fallback: keep the most non-background pixels if the board crop is extremely pale.
    if int(mask.sum()) < 400:
        threshold = np.percentile(dist, 70)
        mask = central & (dist >= threshold)
    pixels = crop[mask]
    lab = rgb_to_lab(pixels.reshape(-1, 1, 3)).reshape(-1, 3)
    return {
        "background_rgb": [float(x) for x in bg],
        "foreground_pixels": int(len(pixels)),
        "lab_mean": [float(x) for x in lab.mean(axis=0)],
        "lab_std": [float(x) for x in lab.std(axis=0)],
        "mean_luminance": float(relative_luminance(pixels.reshape(-1, 1, 3)).mean()),
    }


def transfer_params(ref: dict, target: dict, lighting: str) -> dict:
    mu0 = np.array(ref["lab_mean"], dtype=np.float64)
    sd0 = np.maximum(np.array(ref["lab_std"], dtype=np.float64), 1e-6)
    mu1 = np.array(target["lab_mean"], dtype=np.float64)
    sd1 = np.maximum(np.array(target["lab_std"], dtype=np.float64), 1e-6)
    ratio = np.clip(sd1 / sd0, [0.78, 0.75, 0.75], [1.22, 1.28, 1.28])
    delta = mu1 - mu0
    if lighting == "WARM_DUSK":
        delta[0] = np.clip(delta[0], -10.0, 6.0)
        delta[1] = np.clip(delta[1], 0.5, 12.0)
        delta[2] = np.clip(delta[2], 1.0, 18.0)
        blend = 0.88
    else:
        delta[0] = np.clip(delta[0], -28.0, -8.0)
        delta[1] = np.clip(delta[1], -3.0, 10.0)
        delta[2] = np.clip(delta[2], -3.0, 14.0)
        blend = 0.92
    return {
        "mu_light": mu0.tolist(),
        "scale": ratio.tolist(),
        "delta": delta.tolist(),
        "blend": blend,
    }


def apply_transfer(rgba: np.ndarray, p: dict, lighting: str) -> np.ndarray:
    rgb = rgba[..., :3]
    alpha = rgba[..., 3].copy()
    lab = rgb_to_lab(rgb)
    mu0 = np.array(p["mu_light"])
    scale = np.array(p["scale"])
    delta = np.array(p["delta"])
    mapped = (lab - mu0) * scale + mu0 + delta
    blend = float(p["blend"])
    out_lab = lab * (1.0 - blend) + mapped * blend
    # DARK keeps a deterministic local readability lift in face-scale lightness only,
    # but geometry/alpha remain bit-identical. The lift is intentionally tiny and global
    # enough to avoid painting unsupported detail.
    if lighting == "DARK":
        out_lab[..., 0] = np.maximum(out_lab[..., 0], np.clip(lab[..., 0] * 0.46, 13.0, 42.0))
    out_rgb = lab_to_rgb(out_lab)
    return np.dstack([out_rgb, alpha]).astype(np.uint8)


def bbox_int(box):
    return tuple(int(round(v)) for v in box)


def feature_stats(arr: np.ndarray, box) -> dict:
    x0, y0, x1, y1 = bbox_int(box)
    x0 = max(0, min(arr.shape[1], x0)); x1 = max(0, min(arr.shape[1], x1))
    y0 = max(0, min(arr.shape[0], y0)); y1 = max(0, min(arr.shape[0], y1))
    sub = arr[y0:y1, x0:x1]
    if sub.size == 0:
        return {"pixels": 0, "mean_luminance": 0.0, "luminance_std": 0.0}
    mask = sub[..., 3] > 32
    if not mask.any():
        return {"pixels": 0, "mean_luminance": 0.0, "luminance_std": 0.0}
    lum = relative_luminance(sub[..., :3])[mask]
    return {
        "pixels": int(mask.sum()),
        "mean_luminance": float(lum.mean()),
        "luminance_std": float(lum.std()),
    }


def alpha_bbox(alpha: np.ndarray):
    ys, xs = np.where(alpha > 0)
    return [int(xs.min()), int(ys.min()), int(xs.max() + 1), int(ys.max() + 1)]


def fit_preview(im: Image.Image, box_w: int, box_h: int) -> Image.Image:
    out = Image.new("RGBA", (box_w, box_h), (0, 0, 0, 0))
    copy = im.copy()
    copy.thumbnail((box_w, box_h), Image.Resampling.LANCZOS)
    out.alpha_composite(copy, ((box_w-copy.width)//2, (box_h-copy.height)//2))
    return out


def draw_checker(size, cell=24):
    w, h = size
    a = np.full((h, w, 3), 236, dtype=np.uint8)
    for y in range(0, h, cell):
        for x in range(0, w, cell):
            if ((x//cell)+(y//cell)) % 2:
                a[y:y+cell, x:x+cell] = 214
    return Image.fromarray(a, "RGB").convert("RGBA")


def make_qa_board(images: dict, out_path: Path):
    usages = list(USAGES.keys())
    lightings = ["LIGHT", "WARM_DUSK", "DARK"]
    cell_w, cell_h = 330, 420
    margin = 36
    header = 80
    board = Image.new("RGB", (margin*2 + cell_w*3, header + margin + cell_h*len(usages)), (244, 237, 222))
    d = ImageDraw.Draw(board)
    d.text((margin, 22), "Moru native lighting family v1 — LIGHT / WARM_DUSK / DARK", fill=(42, 38, 31))
    for j, lighting in enumerate(lightings):
        d.text((margin + j*cell_w + 105, 54), lighting, fill=(42, 38, 31))
    for i, usage in enumerate(usages):
        y0 = header + margin + i*cell_h
        d.text((6, y0+8), usage, fill=(42, 38, 31))
        for j, lighting in enumerate(lightings):
            x0 = margin + j*cell_w
            bg = draw_checker((cell_w-12, cell_h-48)) if j == 0 else Image.new("RGBA", (cell_w-12, cell_h-48), (55, 60, 54, 255) if j == 2 else (238, 218, 185, 255))
            pv = fit_preview(images[(usage, lighting)], cell_w-36, cell_h-72)
            bg.alpha_composite(pv, ((bg.width-pv.width)//2, (bg.height-pv.height)//2))
            board.paste(bg.convert("RGB"), (x0+6, y0+30))
    board.save(out_path, format="PNG", optimize=False)


def main() -> int:
    if TMP_ROOT.exists():
        shutil.rmtree(TMP_ROOT)
    TMP_ROOT.mkdir(parents=True)
    if sha256(CANONICAL) != EXPECTED_CANONICAL_SHA:
        raise SystemExit("CANONICAL_HASH_MISMATCH")
    canonical = Image.open(CANONICAL).convert("RGBA")
    if canonical.size != (1122, 1402):
        raise SystemExit("CANONICAL_DIMENSION_MISMATCH")

    ref_stats = {k: crop_foreground_stats(canonical, v) for k, v in LIGHTING_CROPS.items()}
    params = {
        "WARM_DUSK": transfer_params(ref_stats["LIGHT"], ref_stats["WARM_DUSK"], "WARM_DUSK"),
        "DARK": transfer_params(ref_stats["LIGHT"], ref_stats["DARK"], "DARK"),
    }
    metrics = json.loads(USAGE_METRICS.read_text())
    assets_metrics = metrics["assets"]

    generated_dir = TMP_ROOT / "candidate"
    qa_dir = TMP_ROOT / "qa"
    generated_dir.mkdir(parents=True)
    qa_dir.mkdir(parents=True)
    all_images = {}
    report = {
        "version": "moru-native-lighting-family-v1-2026-09-14",
        "verdict": "PENDING",
        "classification": "NATIVE_LIGHTING_QA_CANDIDATE_NOT_RUNTIME_ACTIVE",
        "source_canonical_sha256": EXPECTED_CANONICAL_SHA,
        "source_usage_verdict": metrics.get("verdict"),
        "reference_lighting_stats": ref_stats,
        "transfer": params,
        "assets": {},
        "checks": {},
        "generation_used": False,
        "geometry_edit_used": False,
        "alpha_edit_used": False,
        "expression_fan_out": False,
        "affinity_fan_out": False,
        "runtime_activation": False,
    }

    global_checks = []
    for usage, spec in USAGES.items():
        src_path = USAGE_CANDIDATE / spec["file"]
        if sha256(src_path) != spec["sha"]:
            raise SystemExit(f"USAGE_HASH_MISMATCH:{usage}")
        src = Image.open(src_path).convert("RGBA")
        if src.size != spec["dims"]:
            raise SystemExit(f"USAGE_DIMENSION_MISMATCH:{usage}")
        src_arr = np.asarray(src, dtype=np.uint8)
        all_images[(usage, "LIGHT")] = src
        feature_boxes = assets_metrics[usage]["feature_boxes"]
        rec = {"LIGHT": {"sha256": spec["sha"], "dimensions": list(spec["dims"]), "mode": "RGBA", "alpha_bbox_xyxy": alpha_bbox(src_arr[...,3])}}
        src_face = feature_stats(src_arr, feature_boxes["face"])
        src_sprout = feature_stats(src_arr, feature_boxes["sprout"])
        for lighting in ["WARM_DUSK", "DARK"]:
            out_arr = apply_transfer(src_arr, params[lighting], lighting)
            if not np.array_equal(out_arr[..., 3], src_arr[..., 3]):
                raise SystemExit(f"ALPHA_DRIFT:{usage}:{lighting}")
            out = Image.fromarray(out_arr, "RGBA")
            out_name = f"companion_moru_v2_{usage}_neutral_{lighting}_base.png"
            out_path = generated_dir / out_name
            out.save(out_path, format="PNG", optimize=False)
            all_images[(usage, lighting)] = out
            face = feature_stats(out_arr, feature_boxes["face"])
            sprout = feature_stats(out_arr, feature_boxes["sprout"])
            visible = out_arr[..., 3] > 32
            light_mean = float(relative_luminance(src_arr[..., :3])[visible].mean())
            out_mean = float(relative_luminance(out_arr[..., :3])[visible].mean())
            lab_src = rgb_to_lab(src_arr[..., :3][visible].reshape(-1,1,3)).reshape(-1,3).mean(axis=0)
            lab_out = rgb_to_lab(out_arr[..., :3][visible].reshape(-1,1,3)).reshape(-1,3).mean(axis=0)
            delta = lab_out - lab_src
            if lighting == "WARM_DUSK":
                checks = {
                    "alpha_bit_identical": True,
                    "same_alpha_bbox": alpha_bbox(out_arr[...,3]) == alpha_bbox(src_arr[...,3]),
                    "warmer_a_or_b": bool(delta[1] >= 0.35 or delta[2] >= 0.7),
                    "face_readable": bool(face["mean_luminance"] >= max(0.18, src_face["mean_luminance"] * 0.62)),
                    "face_texture_preserved": bool(face["luminance_std"] >= src_face["luminance_std"] * 0.48),
                    "sprout_readable": bool(sprout["luminance_std"] >= src_sprout["luminance_std"] * 0.42),
                }
            else:
                checks = {
                    "alpha_bit_identical": True,
                    "same_alpha_bbox": alpha_bbox(out_arr[...,3]) == alpha_bbox(src_arr[...,3]),
                    "meaningfully_darker": bool(out_mean <= light_mean * 0.84),
                    "face_readable": bool(face["mean_luminance"] >= 0.105),
                    "face_texture_preserved": bool(face["luminance_std"] >= 0.022),
                    "sprout_readable": bool(sprout["luminance_std"] >= 0.016),
                }
            passed = all(checks.values())
            global_checks.append(passed)
            rec[lighting] = {
                "path": f"candidate/{out_name}",
                "sha256": sha256(out_path),
                "dimensions": list(out.size),
                "mode": "RGBA",
                "alpha_minmax": [int(out_arr[...,3].min()), int(out_arr[...,3].max())],
                "alpha_bbox_xyxy": alpha_bbox(out_arr[...,3]),
                "visible_mean_luminance": out_mean,
                "mean_lab_delta_from_LIGHT": [float(x) for x in delta],
                "face": face,
                "sprout": sprout,
                "checks": checks,
                "verdict": "PASS" if passed else "FAIL",
            }
        report["assets"][usage] = rec

    qa_board = qa_dir / "moru_native_lighting_family_qa_v1.png"
    make_qa_board(all_images, qa_board)
    report["qa_artifacts"] = {
        qa_board.name: {"sha256": sha256(qa_board), "bytes": qa_board.stat().st_size}
    }
    report["checks"] = {
        "accepted_usage_hashes_match": True,
        "canonical_lighting_reference_read": True,
        "all_alpha_geometry_unchanged": True,
        "all_12_new_assets_pass": bool(all(global_checks) and len(global_checks) == 12),
    }

    pass_all = all(report["checks"].values())
    report["verdict"] = "PASS_MORU_02_LIGHTING_FAMILY_V1" if pass_all else "FAIL_MORU_02_LIGHTING_FAMILY_V1"
    metrics_path = qa_dir / "moru_native_lighting_acceptance_metrics_v1.json"
    metrics_path.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n")

    if OUT_ROOT.exists():
        shutil.rmtree(OUT_ROOT)
    if pass_all:
        shutil.copytree(TMP_ROOT, OUT_ROOT)
        readme = OUT_ROOT / "README.md"
        readme.write_text(
            "# Moru native lighting family v1\n\n"
            "Status: **PASS_MORU_02_LIGHTING_FAMILY_V1 / NOT RUNTIME ACTIVE**\n\n"
            "Deterministic color/light derivation only from the accepted `neutral / LIGHT / base / static` usage-context family. "
            "The exact Candidate 3 canonical lighting row calibrated the transfer. Alpha and geometry are bit-identical to LIGHT sources. "
            "No expression or affinity fan-out is included here.\n"
        )
        manifest = json.loads(MANIFEST_PATH.read_text())
        manifest["status"]["transparent_master_export"] = "neutral_usage_context_and_lighting_family_pass_expression_affinity_pending"
        manifest["status"]["mobile_qa"] = "neutral_usage_and_lighting_qa_pass_expression_affinity_pending"
        manifest["native_lighting_family_v1"] = {
            "status": "PASS_MORU_02_LIGHTING_FAMILY_V1",
            "path": "design/reference/moru-v2/native-lighting-family-v1/",
            "source": "accepted neutral/LIGHT/base usage family",
            "lightings": ["LIGHT", "WARM_DUSK", "DARK"],
            "expression": "neutral",
            "affinity": "base",
            "alpha_geometry": "bit_identical_to_LIGHT",
            "runtime_activation": False,
            "next": "author/validate six expression family and four affinity stages; then cross-product/fallback QA before MORU-03",
        }
        manifest["native_usage_context_v1"]["next"] = "lighting family passed; expression and affinity authoring/consistency QA remain before MORU-03"
        MANIFEST_PATH.write_text(json.dumps(manifest, indent=2, ensure_ascii=False) + "\n")
    else:
        OUT_ROOT.mkdir(parents=True)
        fail_qa = OUT_ROOT / "qa"
        fail_qa.mkdir(parents=True)
        shutil.copy2(metrics_path, fail_qa / "FAILURE_MORU_02_LIGHTING_FAMILY_V1.json")
    shutil.rmtree(TMP_ROOT, ignore_errors=True)
    print(json.dumps(report, indent=2, ensure_ascii=False))
    return 0 if pass_all else 2


if __name__ == "__main__":
    raise SystemExit(main())
