#!/usr/bin/env python3
"""Package curated mechanic scenario art (game_analysis image pipeline) into runtime assets.

Input : design/pipeline/mechanic_selection.v1.json + design/pipeline/output/{raw,processed}
Output:
  design/reference/discovery/<mechanic>/  master PNGs + provenance manifest (reference, persisted)
  app/src/main/assets/scenario/<mechanic>/ exact runtime bytes (same usages as Old Ginkgo)
  app/src/main/java/com/dailytown/app/ui/visual/GeneratedMechanicScenarioAssets.kt  sha256-bound registry

Usages mirror the approved Old Ginkgo runtime set: two CLUE_ART items, the place scene as
DISCOVERY_CARD 1600x900 / RECORDS_HEADER 1600x600 / MEMORY_THUMBNAIL 640x480, and the shared-memory
keepsake as exact crops of the same scene (RECORDS_CARD 1024x768, COMPANION_RECENT_MEMORY 1280x720).
"""
from __future__ import annotations

import argparse
import hashlib
import io
import json
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
PIPELINE = ROOT / "design/pipeline"
SELECTION = PIPELINE / "mechanic_selection.v1.json"
REFERENCE = ROOT / "design/reference/discovery"
ASSETS = ROOT / "app/src/main/assets/scenario"
KOTLIN = ROOT / "app/src/main/java/com/dailytown/app/ui/visual/GeneratedMechanicScenarioAssets.kt"


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def crop_to_ratio(img: Image.Image, ratio: float, focus_y: float = 0.5) -> Image.Image:
    w, h = img.size
    if w / h > ratio:
        nw = round(h * ratio)
        left = (w - nw) // 2
        return img.crop((left, 0, left + nw, h))
    nh = round(w / ratio)
    top = int(max(0, min(h - nh, (h - nh) * focus_y)))
    return img.crop((0, top, w, top + nh))


def webp(img: Image.Image, size: tuple[int, int], quality: int = 84) -> bytes:
    buf = io.BytesIO()
    img.convert("RGB").resize(size, Image.Resampling.LANCZOS).save(buf, "WEBP", quality=quality, method=6)
    return buf.getvalue()


def alpha_webp(img: Image.Image, size: int, quality: int = 90) -> bytes:
    """Transparent item art as lossy WebP with alpha (~5x smaller than PNG, decoded natively by Android)."""
    buf = io.BytesIO()
    img.convert("RGBA").resize((size, size), Image.Resampling.LANCZOS).save(
        buf, "WEBP", quality=quality, alpha_quality=100, method=6, exact=False,
    )
    return buf.getvalue()


def provenance(category: str, asset_id: str, variant: str) -> dict:
    raw = json.loads((PIPELINE / "output/raw" / category / asset_id / f"{variant}.json").read_text(encoding="utf-8"))
    params = raw["params"]
    return {
        "variant": variant,
        "generator": raw.get("generator"),
        "seed": params.get("seed"),
        "attempt": raw.get("attempt"),
        "prompt": params.get("prompt"),
        "negative": params.get("negative"),
        "checkpoint": params.get("checkpoint"),
        "model_sha256": raw.get("model_sha256"),
        "workflow_sha256": params.get("workflow_sha256"),
    }


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--selection", type=Path, default=SELECTION)
    args = ap.parse_args(argv)
    selection = json.loads(args.selection.read_text(encoding="utf-8"))
    records: list[tuple[str, str, str, str, int, int, bool]] = []
    for entry in selection["mechanics"]:
        mech = entry["mechanic"]
        ref_dir = REFERENCE / mech
        out_dir = ASSETS / mech
        ref_dir.mkdir(parents=True, exist_ok=True)
        out_dir.mkdir(parents=True, exist_ok=True)
        for stale in out_dir.iterdir():
            stale.unlink()

        scene_variant = entry["scene"]
        scene_src = PIPELINE / "output/processed/scene" / f"{scene_variant}.png"
        scene = Image.open(scene_src).convert("RGB")
        (ref_dir / "place-main.master.png").write_bytes(scene_src.read_bytes())
        clue_src = PIPELINE / "output/processed/mclue" / f"{entry['clue']}.png"
        keep_src = PIPELINE / "output/processed/mclue" / f"{entry['keepsake_item']}.png"
        (ref_dir / "clue-main.master.png").write_bytes(clue_src.read_bytes())
        (ref_dir / "clue-keepsake.master.png").write_bytes(keep_src.read_bytes())

        focus = float(entry.get("keepsake_focus_y", 0.6))
        outputs = {
            ("place", "DISCOVERY_CARD", "place-main-discovery-card.v1.webp", 1600, 900, False):
                webp(crop_to_ratio(scene, 16 / 9), (1600, 900)),
            ("place", "RECORDS_HEADER", "place-main-records-header.v1.webp", 1600, 600, False):
                webp(crop_to_ratio(scene, 16 / 6, 0.45), (1600, 600)),
            ("place", "MEMORY_THUMBNAIL", "place-main-memory-thumbnail.v1.webp", 640, 480, False):
                webp(crop_to_ratio(scene, 4 / 3), (640, 480)),
            ("memory", "RECORDS_CARD", "memory-keepsake-records-card.v1.webp", 1024, 768, False):
                webp(crop_to_ratio(scene, 4 / 3, focus), (1024, 768)),
            ("memory", "COMPANION_RECENT_MEMORY", "memory-keepsake-companion-recent.v1.webp", 1280, 720, False):
                webp(crop_to_ratio(scene, 16 / 9, focus), (1280, 720)),
            ("clue_main", "CLUE_ART", "clue-main.v1.webp", 768, 768, True): alpha_webp(Image.open(clue_src), 768),
            ("clue_keepsake", "CLUE_ART", "clue-keepsake.v1.webp", 512, 512, True): alpha_webp(Image.open(keep_src), 512),
        }
        manifest_assets = []
        for (kind, usage, name, w, h, alpha), data in outputs.items():
            (out_dir / name).write_bytes(data)
            key = {
                "place": f"place.{mech}.main",
                "memory": f"memory.{mech}.keepsake",
                "clue_main": f"clue.{mech}.main",
                "clue_keepsake": f"clue.{mech}.keepsake",
            }[kind]
            records.append((key, usage, f"scenario/{mech}/{name}", sha(data), w, h, alpha))
            manifest_assets.append({"semantic_key": key, "usage": usage, "runtime_path": f"scenario/{mech}/{name}", "sha256": sha(data)})
        manifest = {
            "schema_version": 1,
            "mechanic": mech,
            "status": "runtime_bound_owner_approved_2026_10_20",
            "style_baseline": "Old Ginkgo production v3 (approved raster baseline); generated, not a new style family",
            "provenance": {
                "scene": provenance("scene", f"scene_{mech}", scene_variant),
                "clue": provenance("mclue", f"clue_{mech}", entry["clue"]),
                "keepsake_item": provenance("mclue", f"keepsake_{mech}", entry["keepsake_item"]),
            },
            "curation_note": entry.get("note", ""),
            "runtime_assets": manifest_assets,
        }
        (ref_dir / "mechanic-scenario-manifest.v1.json").write_text(
            json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n",
        )

    lines = [
        "package com.dailytown.app.ui.visual",
        "",
        "import com.dailytown.app.visual.ScenarioAssetUsage",
        "import com.dailytown.app.visual.SemanticAssetKey",
        "",
        "// GENERATED by tools/visual/package_mechanic_scenarios.py — do not edit by hand.",
        "internal object GeneratedMechanicScenarioAssets {",
        "    val records: List<ProductionScenarioAssetRegistry.Record> = listOf(",
    ]
    for key, usage, path, digest, w, h, alpha in records:
        lines.append(
            f'        ProductionScenarioAssetRegistry.Record(SemanticAssetKey("{key}"), ScenarioAssetUsage.{usage}, '
            f'"{path}", "{digest}", {w}, {h}, {"true" if alpha else "false"}),'
        )
    lines += ["    )", "}", ""]
    KOTLIN.write_text("\n".join(lines), encoding="utf-8", newline="\n")
    print(f"packaged {len(selection['mechanics'])} mechanics, {len(records)} runtime assets")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
