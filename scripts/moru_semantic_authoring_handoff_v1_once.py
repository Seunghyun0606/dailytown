from __future__ import annotations

import hashlib
import json
import shutil
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
CANONICAL = ROOT / "design/reference/baseline-v2/moru_candidate3_canonical_baseline_v2.png"
CANONICAL_SHA = "541b53464bbc589d86323faa3a7dbc8790cf9ab9573e0dd66812986c4a430500"
NEUTRAL_PNG = ROOT / "design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png"
NEUTRAL_SHA = "b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692"
NEUTRAL_ORA = ROOT / "design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.ora"
ORA_SHA = "2b64df529c308039fda608dbbde7514e7384b89e93ff76f384f98beb8e122505"
LIGHTING_METRICS = ROOT / "design/reference/moru-v2/native-lighting-family-v1/qa/moru_native_lighting_acceptance_metrics_v1.json"
MANIFEST = ROOT / "design/export-spec/moru-production-manifest.v2.json"
OUT = ROOT / "design/reference/moru-v2/native-semantic-authoring-handoff-v1"
TMP = ROOT / ".moru-semantic-handoff-v1-tmp"

EXPRESSION_REGIONS = {
    "neutral": (28, 690, 190, 905),
    "happy": (195, 690, 365, 905),
    "curious": (365, 690, 535, 905),
    "surprised": (535, 690, 705, 905),
    "clue_found": (705, 690, 885, 905),
    "resolved": (885, 690, 1085, 905),
}
AFFINITY_REGIONS = {
    "base": (563, 952, 684, 1128),
    "familiar": (688, 952, 809, 1128),
    "trusted": (812, 952, 940, 1128),
    "best_friend": (944, 952, 1088, 1128),
}


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def parse_ora_layers(path: Path) -> list[dict]:
    with zipfile.ZipFile(path) as z:
        raw = z.read("stack.xml")
        root = ET.fromstring(raw)
        layers = []
        def walk(node, depth=0):
            for child in node:
                tag = child.tag.split("}")[-1]
                if tag == "layer":
                    layers.append({
                        "name": child.attrib.get("name", ""),
                        "src": child.attrib.get("src", ""),
                        "visibility": child.attrib.get("visibility", "visible"),
                        "opacity": child.attrib.get("opacity", "1.0"),
                        "composite_op": child.attrib.get("composite-op", "svg:src-over"),
                        "depth": depth,
                    })
                elif tag == "stack":
                    walk(child, depth + 1)
        walk(root, 0)
        return layers


def save_crops(board: Image.Image, regions: dict, out_dir: Path, prefix: str) -> dict:
    out_dir.mkdir(parents=True, exist_ok=True)
    result = {}
    for key, box in regions.items():
        crop = board.crop(box)
        name = f"{prefix}_{key}_exact_reference_crop.png"
        path = out_dir / name
        crop.save(path, format="PNG", optimize=False)
        result[key] = {
            "path": path.relative_to(TMP).as_posix(),
            "source_box_xyxy": list(box),
            "dimensions": list(crop.size),
            "mode": crop.mode,
            "sha256": sha256(path),
            "role": "PIXEL_EXACT_REFERENCE_CROP_NOT_PRODUCTION_MASTER",
        }
    return result


def make_board(expressions: dict, affinities: dict, out_path: Path):
    canvas = Image.new("RGB", (1600, 980), (247, 240, 225))
    d = ImageDraw.Draw(canvas)
    d.text((40, 24), "Moru semantic authoring handoff v1 — exact canonical reference crops", fill=(50, 43, 34))
    d.text((40, 62), "Expression references (guide only; do not upscale into production masters)", fill=(70, 60, 48))
    x, y = 40, 100
    for key in EXPRESSION_REGIONS:
        im = Image.open(TMP / expressions[key]["path"]).convert("RGB")
        im.thumbnail((220, 250), Image.Resampling.LANCZOS)
        canvas.paste(im, (x, y))
        d.text((x, y + im.height + 8), key, fill=(50, 43, 34))
        x += 250
    d.text((40, 430), "Affinity references (keepsake/patch/charm progression only; anatomy/costume locked)", fill=(70, 60, 48))
    x, y = 70, 470
    for key in AFFINITY_REGIONS:
        im = Image.open(TMP / affinities[key]["path"]).convert("RGB")
        scale = min(260 / im.width, 300 / im.height)
        im = im.resize((round(im.width * scale), round(im.height * scale)), Image.Resampling.NEAREST)
        canvas.paste(im, (x, y))
        d.text((x, y + im.height + 10), key, fill=(50, 43, 34))
        x += 370
    d.text((40, 900), "Production authority remains accepted neutral PNG/ORA + semantic contract. Crops are visual guides only.", fill=(70, 60, 48))
    canvas.save(out_path, format="PNG", optimize=False)


def main() -> int:
    for path, expected in [(CANONICAL, CANONICAL_SHA), (NEUTRAL_PNG, NEUTRAL_SHA), (NEUTRAL_ORA, ORA_SHA)]:
        if sha256(path) != expected:
            raise SystemExit(f"HASH_MISMATCH:{path}")
    lighting = json.loads(LIGHTING_METRICS.read_text())
    if lighting.get("verdict") != "PASS_MORU_02_LIGHTING_FAMILY_V1":
        raise SystemExit("LIGHTING_GATE_NOT_PASS")

    shutil.rmtree(TMP, ignore_errors=True)
    TMP.mkdir(parents=True)
    source_dir = TMP / "source"
    qa_dir = TMP / "qa"
    source_dir.mkdir(); qa_dir.mkdir()
    board = Image.open(CANONICAL).convert("RGBA")
    expressions = save_crops(board, EXPRESSION_REGIONS, source_dir / "expression", "expression")
    affinities = save_crops(board, AFFINITY_REGIONS, source_dir / "affinity", "affinity")
    layers = parse_ora_layers(NEUTRAL_ORA)
    qa_board = qa_dir / "moru_semantic_authoring_reference_board_v1.png"
    make_board(expressions, affinities, qa_board)

    contract = {
        "version": "moru-native-semantic-authoring-handoff-v1-2026-09-14",
        "status": "HANDOFF_READY_EXPRESSION_AFFINITY_NOT_PRODUCED",
        "runtime_activation": False,
        "authorities": {
            "canonical_board": {
                "path": CANONICAL.relative_to(ROOT).as_posix(),
                "sha256": CANONICAL_SHA,
                "dimensions": [1122, 1402],
            },
            "accepted_neutral_png": {
                "path": NEUTRAL_PNG.relative_to(ROOT).as_posix(),
                "sha256": NEUTRAL_SHA,
                "dimensions": [1280, 1600],
                "mode": "RGBA",
            },
            "accepted_neutral_ora": {
                "path": NEUTRAL_ORA.relative_to(ROOT).as_posix(),
                "sha256": ORA_SHA,
                "layers": layers,
            },
            "accepted_lighting_family": {
                "path": "design/reference/moru-v2/native-lighting-family-v1/",
                "status": "PASS_MORU_02_LIGHTING_FAMILY_V1",
            },
        },
        "reference_crops": {
            "expressions": expressions,
            "affinity": affinities,
        },
        "authoring_strategy": {
            "principle": "Separate human/image-edit authoring sources from deterministic export fan-out. Do not hand-author the full Cartesian product.",
            "expression_source_masters": {
                "target": "1280x1600 RGBA result_large-equivalent master, LIGHT/base",
                "count": 6,
                "neutral": "reuse accepted neutral master; do not remake",
                "new_required": ["happy", "curious", "surprised", "clue_found", "resolved"],
                "allowed_changes": [
                    "eyes/mouth expression",
                    "small head tilt/lean where reference supports it",
                    "limited forearm/hand gesture within canonical body envelope",
                    "clue interaction only for clue_found when directly supported by the canonical expression reference"
                ],
                "locked": [
                    "sprout count and asymmetric silhouette",
                    "hood shape/material",
                    "scarf diagonal and ochre family",
                    "cream jacket/botanical layering",
                    "satchel/compass identity",
                    "boots/body proportions/species",
                    "soft raster material language"
                ],
            },
            "affinity_sources": {
                "base": "no additional overlay",
                "familiar": "small tag/stitching/shared-travel accent overlay",
                "trusted": "route charm/compass/richer satchel keepsake overlay",
                "best_friend": "BF-B restrained keepsake/patch cluster overlay",
                "preferred_form": "transparent additive overlay anchored to jacket/scarf/satchel/compass region; do not repaint anatomy",
                "pose_exception": "if a pose moves an anchor enough to misalign the overlay, author a pose-specific overlay instead of warping the whole character"
            },
            "lighting": "Apply the already accepted deterministic LIGHT/WARM_DUSK/DARK transform after expression + affinity composition; alpha/geometry remain controlled by the accepted semantic master.",
            "usage_fan_out": "Only after a semantic master passes identity/semantic QA, derive map/HUD/encounter/result/Companion/journal framing from that master using the accepted usage-context rules.",
        },
        "acceptance": {
            "expression": {
                "hood_iou_min": 0.95,
                "sprout_iou_min": 0.94,
                "subject_silhouette_iou_min": 0.90,
                "face_anchor_deviation_max_head_width": 0.025,
                "palette_deltaE76_median_max_LIGHT": 8,
                "forbidden": [
                    "new costume",
                    "new species/anatomy",
                    "sprout reinterpretation",
                    "large body-envelope change",
                    "sharp/vector/sticker rendering",
                    "unsupported generated micro-detail"
                ]
            },
            "affinity": {
                "anatomy_invariant": True,
                "base_character_silhouette_iou_min": 0.985,
                "overlay_scope": "jacket/scarf/satchel/compass keepsake regions only",
                "forbidden": [
                    "crown/royal evolution",
                    "body growth/species change",
                    "major costume replacement",
                    "large aura/effect changing silhouette",
                    "keepsakes unsupported by canonical affinity reference"
                ]
            },
            "cross_family": [
                "all six expressions read consistently at HUD/Companion/result contexts",
                "LIGHT/WARM_DUSK/DARK preserve face/eye readability",
                "affinity progression remains restrained AF-1 + AF-3/BF-B",
                "transparent edge/halo checks remain PASS",
                "neutral/LIGHT/base/static fallback remains exact",
                "runtime activation stays false until MORU-03"
            ]
        },
        "minimum_authoring_work": {
            "new_expression_masters": 5,
            "affinity_overlay_stages": 3,
            "lighting_manual_variants": 0,
            "usage_context_manual_redraws": 0,
            "note": "Final exports may expand to required semantic tuples, but the authoring source set should remain compact and derivable."
        },
        "qa_artifacts": {
            qa_board.name: {"sha256": sha256(qa_board), "bytes": qa_board.stat().st_size}
        }
    }
    (TMP / "semantic-authoring-contract.v1.json").write_text(json.dumps(contract, indent=2, ensure_ascii=False) + "\n")
    (TMP / "HANDOFF.md").write_text(
        "# Moru semantic authoring handoff v1\n\n"
        "Status: **HANDOFF_READY / EXPRESSION + AFFINITY ART NOT YET PRODUCED**\n\n"
        "This package does not create a new Moru design. The accepted neutral PNG/ORA remains the identity authority, and the accepted lighting family remains reusable.\n\n"
        "## Authoring order\n\n"
        "1. Keep the accepted neutral master immutable.\n"
        "2. Author only five additional 1280×1600 LIGHT/base expression masters: happy, curious, surprised, clue_found, resolved. Neutral is reused.\n"
        "3. Run identity + expression QA before any fan-out.\n"
        "4. Author familiar/trusted/best_friend as restrained transparent keepsake overlays where possible; do not repaint anatomy.\n"
        "5. Composite expression + affinity, then apply the already-PASS lighting transform.\n"
        "6. Derive the six usage contexts from each accepted semantic master.\n"
        "7. Run cross-family/mobile/fallback QA; only then prepare MORU-03.\n\n"
        "## Important\n\n"
        "The exact canonical crops under `source/` are visual guides only. They are intentionally too small to be production masters and must not be upscaled and mislabeled as native assets.\n"
    )

    shutil.rmtree(OUT, ignore_errors=True)
    shutil.copytree(TMP, OUT)
    shutil.rmtree(TMP, ignore_errors=True)

    manifest = json.loads(MANIFEST.read_text())
    manifest["semantic_authoring_handoff_v1"] = {
        "status": "HANDOFF_READY_EXPRESSION_AFFINITY_NOT_PRODUCED",
        "path": "design/reference/moru-v2/native-semantic-authoring-handoff-v1/",
        "new_expression_masters_required": 5,
        "affinity_overlay_stages_required": 3,
        "lighting_reuse": "PASS_MORU_02_LIGHTING_FAMILY_V1",
        "runtime_activation": False,
    }
    manifest["native_lighting_family_v1"]["next"] = "consume native-semantic-authoring-handoff-v1 to author five expression masters + three restrained affinity overlay stages, then run consistency/fallback QA before MORU-03"
    MANIFEST.write_text(json.dumps(manifest, indent=2, ensure_ascii=False) + "\n")
    print(json.dumps({"status": contract["status"], "expression_crops": len(expressions), "affinity_crops": len(affinities), "ora_layers": len(layers), "qa_sha256": contract["qa_artifacts"][qa_board.name]["sha256"]}, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
