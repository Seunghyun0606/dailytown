#!/usr/bin/env python3
"""Value-blind visual candidate gate: checks manifests, SHA-256, semantic metadata and marker anchors."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BATCHES = [
    ROOT / "design/production/production-promotion-batch-01.v1.json",
    ROOT / "design/production/production-promotion-batch-01-luca-derivatives.v1.json",
    ROOT / "design/production/marker-split-export-v1.json",
    ROOT / "design/production/a3-split-export-v1.json",
]
EXPECTED_COUNTS = [23, 9, 24, 9]


def digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            value.update(block)
    return value.hexdigest()


def load(path: Path) -> dict:
    with path.open(encoding="utf-8") as stream:
        return json.load(stream)


def verify_svg(path: Path, semantic_key: str) -> None:
    text = path.read_text(encoding="utf-8")
    ET.fromstring(text)
    if semantic_key not in text:
        raise AssertionError(f"{path}: semantic key metadata/title missing: {semantic_key}")
    if "design/source/" in str(path.relative_to(ROOT)):
        raise AssertionError(f"{path}: source master entered production candidate path")


def verify_marker_contract(manifest: dict) -> None:
    anchor = manifest["common_geo_anchor"]
    if anchor != {"x": 0.5, "y": 0.96875, "viewbox_tip": [0, 76]}:
        raise AssertionError(f"unexpected marker anchor: {anchor}")
    families: dict[str, set[str]] = {"DAY": set(), "DARK": set()}
    structural_by_family: dict[str, dict[str, str]] = {"DAY": {}, "DARK": {}}
    for asset in manifest["assets"]:
        family = asset["family"]
        semantic = asset["semantic_key"]
        families[family].add(semantic)
        text = (ROOT / asset["path"]).read_text(encoding="utf-8")
        if "anchor_x=0.5;anchor_y=0.96875" not in text:
            raise AssertionError(f"{asset['path']}: marker anchor metadata mismatch")
        # Ignore paint values and compare geometry/icon structure. This guards against hue-only state encoding.
        structural = re.sub(r'\s(?:fill|stroke)="[^"]*"', "", text)
        structural = re.sub(r'<title>.*?</title>|<desc>.*?</desc>|<metadata>.*?</metadata>', "", structural, flags=re.S)
        structural_by_family[family][semantic] = structural
    if families["DAY"] != families["DARK"]:
        raise AssertionError("DAY/DARK semantic marker sets differ")
    encounter_keys = [key for key in families["DAY"] if key.startswith("marker.encounter.")]
    for family in ("DAY", "DARK"):
        values = [structural_by_family[family][key] for key in encounter_keys]
        if len(values) != len(set(values)):
            raise AssertionError(f"{family}: encounter states collapse to color-only duplicate geometry")


def main() -> int:
    total = 0
    loaded = []
    for manifest_path, expected in zip(BATCHES, EXPECTED_COUNTS):
        manifest = load(manifest_path)
        assets = manifest.get("assets", [])
        if len(assets) != expected:
            raise AssertionError(f"{manifest_path}: expected {expected} assets, got {len(assets)}")
        for asset in assets:
            rel = Path(asset["path"])
            if rel.is_absolute() or not str(rel).startswith("design/production/"):
                raise AssertionError(f"{manifest_path}: non-production candidate path: {rel}")
            path = ROOT / rel
            if not path.is_file():
                raise AssertionError(f"missing candidate: {rel}")
            actual = digest(path)
            if actual != asset["sha256"]:
                raise AssertionError(f"checksum mismatch: {rel}")
            if path.suffix.lower() == ".svg":
                verify_svg(path, asset["semantic_key"])
        total += len(assets)
        loaded.append(manifest)
    verify_marker_contract(loaded[2])
    print(f"visual candidate verification PASS: {total} assets (32 companion, 24 marker, 9 A-3)")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"visual candidate verification FAIL: {exc}", file=sys.stderr)
        raise
