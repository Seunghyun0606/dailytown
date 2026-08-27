#!/usr/bin/env python3
"""Fail-closed structural verifier for Daily Town marker production activation.

This tool never mutates repository files. Before human/physical approval it verifies that marker
assets remain candidate-only and absent from the main APK. After readiness passes, the same tool
can verify that the explicit activation delta is exactly the approved 24 family-aware records.
"""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re
import sys
from typing import Any

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_MARKER_BATCH = ROOT / "design/production/marker-split-export-v1.json"
DEFAULT_APP_BUILD = ROOT / "app/build.gradle.kts"
DEFAULT_MARKER_REGISTRY = ROOT / "app/src/main/java/com/dailytown/app/ui/visual/ProductionMarkerAssetRegistry.kt"

EXPECTED_BATCH_ID = "marker-split-export-v1"
EXPECTED_ASSET_COUNT = 24
EXPECTED_SOURCE_ROOT = "../design/production/markers/v1"
EXPECTED_CANDIDATE_STATUS = "design_qa_complete_production_export_candidate"
EXPECTED_CANDIDATE_ASSET_STATE = "production_export_candidate"
EXPECTED_PRODUCTION_STATUS = "production_export"
EXPECTED_PRODUCTION_ASSET_STATE = "production_export"
EXPECTED_SEMANTICS = {
    "marker.encounter.hinted",
    "marker.encounter.discoverable",
    "marker.encounter.active",
    "marker.encounter.solved",
    "marker.encounter.revisit",
    "marker.clue",
    "marker.poi.park",
    "marker.poi.culture",
    "marker.poi.landmark",
    "marker.poi.daily_life",
    "marker.poi.nature",
    "marker.poi.other",
}

_COUNT_RE = re.compile(r"const\s+val\s+PROMOTED_MARKER_COUNT\s*=\s*(\d+)")
_RECORD_RE = re.compile(
    r'marker\(\s*MarkerFamily\.(DAY|DARK)\s*,\s*"([^"]+)"\s*,\s*"([^"]+)"\s*\)',
    re.MULTILINE,
)
_EMPTY_INDEX_RE = re.compile(r"MarkerProductionAssetIndex\(\s*emptyList\(\)\s*\)", re.MULTILINE)
_MAIN_SOURCE_RE = re.compile(
    r'getByName\("main"\)\.assets\.directories\.add\(\s*"\.\./design/production/markers/v1"\s*\)'
)


class ActivationContractError(AssertionError):
    pass


def load_json(path: Path) -> dict[str, Any]:
    if not path.is_file():
        raise ActivationContractError(f"missing required file: {path}")
    try:
        with path.open(encoding="utf-8") as stream:
            value = json.load(stream)
    except (OSError, json.JSONDecodeError) as exc:
        raise ActivationContractError(f"invalid JSON: {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise ActivationContractError(f"expected JSON object: {path}")
    return value


def read_text(path: Path) -> str:
    if not path.is_file():
        raise ActivationContractError(f"missing required file: {path}")
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        raise ActivationContractError(f"cannot read required file: {path}: {exc}") from exc


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def marker_fingerprint(assets: list[dict[str, Any]]) -> str:
    rows = [
        f"{asset.get('family','')}|{asset.get('semantic_key','')}|{asset.get('sha256','')}"
        for asset in assets
    ]
    return hashlib.sha256("\n".join(sorted(rows)).encode("utf-8")).hexdigest()


def _safe_relative_marker_path(raw: Any) -> Path:
    rel = Path(str(raw or ""))
    expected_prefix = Path("design/production/markers/v1")
    if rel.is_absolute() or ".." in rel.parts or rel.suffix != ".svg":
        raise ActivationContractError(f"unsafe marker path: {rel}")
    try:
        rel.relative_to(expected_prefix)
    except ValueError as exc:
        raise ActivationContractError(f"marker path escapes approved production root: {rel}") from exc
    return rel


def validate_batch(root: Path, batch_path: Path, *, expected_state: str) -> tuple[list[dict[str, Any]], str]:
    batch = load_json(batch_path)
    if batch.get("batch_id") != EXPECTED_BATCH_ID:
        raise ActivationContractError(f"unexpected marker batch id: {batch.get('batch_id')!r}")
    if batch.get("asset_count") != EXPECTED_ASSET_COUNT:
        raise ActivationContractError(f"marker asset_count must be {EXPECTED_ASSET_COUNT}")

    expected_status, expected_asset_state = {
        "candidate": (EXPECTED_CANDIDATE_STATUS, EXPECTED_CANDIDATE_ASSET_STATE),
        "production": (EXPECTED_PRODUCTION_STATUS, EXPECTED_PRODUCTION_ASSET_STATE),
    }[expected_state]
    if batch.get("status") != expected_status:
        raise ActivationContractError(
            f"marker batch status must be {expected_status!r} for {expected_state}, got {batch.get('status')!r}"
        )

    assets = batch.get("assets")
    if not isinstance(assets, list) or len(assets) != EXPECTED_ASSET_COUNT:
        raise ActivationContractError(f"marker batch must contain exactly {EXPECTED_ASSET_COUNT} assets")

    seen_pairs: set[tuple[str, str]] = set()
    semantics_by_family: dict[str, set[str]] = {"DAY": set(), "DARK": set()}
    normalized: list[dict[str, Any]] = []
    for asset in assets:
        family = asset.get("family")
        semantic = asset.get("semantic_key")
        sha256 = asset.get("sha256")
        if family not in semantics_by_family or not isinstance(semantic, str) or not semantic.startswith("marker."):
            raise ActivationContractError(f"invalid marker family/semantic entry: {asset!r}")
        if not isinstance(sha256, str) or not re.fullmatch(r"[0-9a-f]{64}", sha256):
            raise ActivationContractError(f"invalid marker sha256 for {family}/{semantic}")
        pair = (family, semantic)
        if pair in seen_pairs:
            raise ActivationContractError(f"duplicate marker family/semantic pair: {pair}")
        seen_pairs.add(pair)
        semantics_by_family[family].add(semantic)
        if asset.get("approval_state") != expected_asset_state:
            raise ActivationContractError(
                f"{pair}: approval_state must be {expected_asset_state!r} for {expected_state}"
            )

        rel = _safe_relative_marker_path(asset.get("path"))
        runtime_path = rel.relative_to(Path("design/production/markers/v1"))
        if not runtime_path.parts or runtime_path.parts[0] != family.lower():
            raise ActivationContractError(f"{pair}: path does not match marker family: {rel}")
        absolute = root / rel
        if not absolute.is_file():
            raise ActivationContractError(f"{pair}: missing marker asset: {rel}")
        if sha256_file(absolute) != sha256:
            raise ActivationContractError(f"{pair}: marker asset checksum mismatch: {rel}")

        normalized.append(
            {
                "family": family,
                "semantic_key": semantic,
                "runtime_asset_path": runtime_path.as_posix(),
                "source_path": rel.as_posix(),
                "sha256": sha256,
            }
        )

    for family, semantics in semantics_by_family.items():
        if semantics != EXPECTED_SEMANTICS:
            missing = sorted(EXPECTED_SEMANTICS - semantics)
            extra = sorted(semantics - EXPECTED_SEMANTICS)
            raise ActivationContractError(f"{family}: semantic set mismatch missing={missing} extra={extra}")

    return sorted(normalized, key=lambda item: (item["family"], item["semantic_key"])), marker_fingerprint(assets)


def parse_promoted_count(registry_source: str) -> int:
    match = _COUNT_RE.search(registry_source)
    if not match:
        raise ActivationContractError("ProductionMarkerAssetRegistry must declare PROMOTED_MARKER_COUNT")
    return int(match.group(1))


def parse_registry_records(registry_source: str) -> list[dict[str, str]]:
    return sorted(
        [
            {"family": family, "semantic_key": semantic, "runtime_asset_path": path}
            for family, semantic, path in _RECORD_RE.findall(registry_source)
        ],
        key=lambda item: (item["family"], item["semantic_key"]),
    )


def activation_plan(root: Path, batch_path: Path) -> dict[str, Any]:
    records, fingerprint = validate_batch(root, batch_path, expected_state="candidate")
    return {
        "schema_version": 1,
        "marker_batch_id": EXPECTED_BATCH_ID,
        "marker_candidate_fingerprint_sha256": fingerprint,
        "promoted_marker_count": EXPECTED_ASSET_COUNT,
        "main_asset_source_root": EXPECTED_SOURCE_ROOT,
        "target_batch_status": EXPECTED_PRODUCTION_STATUS,
        "target_asset_approval_state": EXPECTED_PRODUCTION_ASSET_STATE,
        "records": records,
    }


def verify_candidate_state(*, root: Path, batch_path: Path, app_build: Path, marker_registry: Path) -> str:
    _, fingerprint = validate_batch(root, batch_path, expected_state="candidate")
    build_source = read_text(app_build)
    registry_source = read_text(marker_registry)

    if _MAIN_SOURCE_RE.search(build_source) or EXPECTED_SOURCE_ROOT in build_source:
        raise ActivationContractError("candidate marker source root must not be exposed through the main APK source set")
    if parse_promoted_count(registry_source) != 0:
        raise ActivationContractError("candidate marker registry must keep PROMOTED_MARKER_COUNT = 0")
    if not _EMPTY_INDEX_RE.search(registry_source):
        raise ActivationContractError("candidate marker registry must use an empty production index")
    if parse_registry_records(registry_source):
        raise ActivationContractError("candidate marker registry must not contain production marker records")
    return fingerprint


def verify_production_state(*, root: Path, batch_path: Path, app_build: Path, marker_registry: Path) -> str:
    expected_records, fingerprint = validate_batch(root, batch_path, expected_state="production")
    build_source = read_text(app_build)
    registry_source = read_text(marker_registry)

    if not _MAIN_SOURCE_RE.search(build_source):
        raise ActivationContractError(
            f"production marker activation must add main APK asset source {EXPECTED_SOURCE_ROOT!r}"
        )
    if parse_promoted_count(registry_source) != EXPECTED_ASSET_COUNT:
        raise ActivationContractError(
            f"production marker registry must set PROMOTED_MARKER_COUNT = {EXPECTED_ASSET_COUNT}"
        )
    actual_records = parse_registry_records(registry_source)
    if len(actual_records) != EXPECTED_ASSET_COUNT:
        raise ActivationContractError(
            f"production marker registry must contain exactly {EXPECTED_ASSET_COUNT} explicit family-aware records"
        )

    expected_runtime = [
        {
            "family": item["family"],
            "semantic_key": item["semantic_key"],
            "runtime_asset_path": item["runtime_asset_path"],
        }
        for item in expected_records
    ]
    if actual_records != expected_runtime:
        expected_pairs = {(r["family"], r["semantic_key"], r["runtime_asset_path"]) for r in expected_runtime}
        actual_pairs = {(r["family"], r["semantic_key"], r["runtime_asset_path"]) for r in actual_records}
        missing = sorted(expected_pairs - actual_pairs)
        extra = sorted(actual_pairs - expected_pairs)
        raise ActivationContractError(f"production marker registry mismatch missing={missing} extra={extra}")
    return fingerprint


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Verify Daily Town marker activation structure without mutating it.")
    parser.add_argument("--state", choices=("candidate", "production"), default="candidate")
    parser.add_argument("--marker-batch", type=Path, default=DEFAULT_MARKER_BATCH)
    parser.add_argument("--app-build", type=Path, default=DEFAULT_APP_BUILD)
    parser.add_argument("--marker-registry", type=Path, default=DEFAULT_MARKER_REGISTRY)
    parser.add_argument(
        "--print-plan",
        action="store_true",
        help="Print the deterministic candidate-to-production activation plan after candidate verification.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    verifier = verify_candidate_state if args.state == "candidate" else verify_production_state
    fingerprint = verifier(
        root=ROOT,
        batch_path=args.marker_batch,
        app_build=args.app_build,
        marker_registry=args.marker_registry,
    )
    print(
        f"marker activation contract PASS: state={args.state}, "
        f"count={'0' if args.state == 'candidate' else EXPECTED_ASSET_COUNT}, fingerprint={fingerprint}"
    )
    if args.print_plan:
        if args.state != "candidate":
            raise ActivationContractError("--print-plan is only valid while marker assets are candidate-only")
        print(json.dumps(activation_plan(ROOT, args.marker_batch), ensure_ascii=False, indent=2, sort_keys=True))
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"marker activation contract FAIL: {exc}", file=sys.stderr)
        raise SystemExit(1)
