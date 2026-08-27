#!/usr/bin/env python3
"""Fail-closed readiness gate for promoting Daily Town marker candidates."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import sys
from typing import Any

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_MARKER_BATCH = ROOT / "design/production/marker-split-export-v1.json"
EXPECTED_BATCH_ID = "marker-split-export-v1"
EXPECTED_ASSET_COUNT = 24
EXPECTED_PACKAGE = "com.dailytown.app"
EXPECTED_ANCHOR = {"x": 0.5, "y": 0.96875, "viewbox_tip": [0, 76]}
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
REQUIRED_HUMAN_CHECKS = {
    "marker_readability",
    "selected_state_anchor",
    "route_hud_companion_readability",
    "provider_road_place_comprehension",
    "naver_attribution_legal_ui",
}


class ReadinessError(AssertionError):
    pass


def load_json(path: Path) -> dict[str, Any]:
    if not path.is_file():
        raise ReadinessError(f"missing required evidence: {path}")
    try:
        with path.open(encoding="utf-8") as stream:
            value = json.load(stream)
    except (OSError, json.JSONDecodeError) as exc:
        raise ReadinessError(f"invalid JSON evidence: {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise ReadinessError(f"expected JSON object: {path}")
    return value


def sha256_file(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            value.update(block)
    return value.hexdigest()


def marker_fingerprint(assets: list[dict[str, Any]]) -> str:
    rows = [
        f"{asset.get('family','')}|{asset.get('semantic_key','')}|{asset.get('sha256','')}"
        for asset in assets
    ]
    payload = "\n".join(sorted(rows)).encode("utf-8")
    return hashlib.sha256(payload).hexdigest()


def verify_marker_batch(root: Path, batch_path: Path) -> str:
    batch = load_json(batch_path)
    if batch.get("batch_id") != EXPECTED_BATCH_ID:
        raise ReadinessError(f"unexpected marker batch id: {batch.get('batch_id')!r}")
    if batch.get("status") != "design_qa_complete_production_export_candidate":
        raise ReadinessError(f"marker batch is not candidate-only: {batch.get('status')!r}")
    if batch.get("asset_count") != EXPECTED_ASSET_COUNT:
        raise ReadinessError(f"marker asset_count must be {EXPECTED_ASSET_COUNT}")
    if batch.get("common_geo_anchor") != EXPECTED_ANCHOR:
        raise ReadinessError(f"marker common_geo_anchor changed: {batch.get('common_geo_anchor')!r}")

    assets = batch.get("assets")
    if not isinstance(assets, list) or len(assets) != EXPECTED_ASSET_COUNT:
        raise ReadinessError(f"marker batch must contain exactly {EXPECTED_ASSET_COUNT} assets")

    semantics_by_family: dict[str, set[str]] = {"DAY": set(), "DARK": set()}
    seen_pairs: set[tuple[str, str]] = set()
    for asset in assets:
        family = asset.get("family")
        semantic = asset.get("semantic_key")
        if family not in semantics_by_family or not isinstance(semantic, str):
            raise ReadinessError(f"invalid marker family/semantic entry: {asset!r}")
        pair = (family, semantic)
        if pair in seen_pairs:
            raise ReadinessError(f"duplicate marker semantic pair: {pair}")
        seen_pairs.add(pair)
        semantics_by_family[family].add(semantic)

        if asset.get("approval_state") != "production_export_candidate":
            raise ReadinessError(f"{pair}: marker asset is not candidate-only")
        rel = Path(str(asset.get("path", "")))
        if rel.is_absolute() or not str(rel).startswith("design/production/markers/v1/"):
            raise ReadinessError(f"{pair}: invalid marker candidate path: {rel}")
        path = root / rel
        if not path.is_file():
            raise ReadinessError(f"{pair}: missing marker candidate: {rel}")
        if sha256_file(path) != asset.get("sha256"):
            raise ReadinessError(f"{pair}: marker candidate checksum mismatch: {rel}")

    for family, semantics in semantics_by_family.items():
        if semantics != EXPECTED_SEMANTICS:
            missing = sorted(EXPECTED_SEMANTICS - semantics)
            extra = sorted(semantics - EXPECTED_SEMANTICS)
            raise ReadinessError(f"{family}: semantic set mismatch missing={missing} extra={extra}")

    return marker_fingerprint(assets)


def verify_naver_session(
    path: Path,
    *,
    expected_marker_fingerprint: str,
    expected_emulator: bool,
    expected_runner_hint: str | None,
) -> None:
    data = load_json(path)
    if data.get("schemaVersion") != 3:
        raise ReadinessError(f"{path}: unsupported NAVER diagnostic schemaVersion")
    if data.get("outcome") != "PASS" or data.get("failureCategory") is not None:
        raise ReadinessError(f"{path}: NAVER QA outcome is not clean PASS")
    if data.get("packageName") != EXPECTED_PACKAGE or data.get("naverCredentialConfigured") is not True:
        raise ReadinessError(f"{path}: NAVER package/credential wiring proof failed")

    client = data.get("naverClient") or {}
    if (
        client.get("mode") != "NCP_KEY_ID"
        or client.get("expectedRegisteredAndroidPackage") != EXPECTED_PACKAGE
        or client.get("packageMatchesExpected") is not True
    ):
        raise ReadinessError(f"{path}: NAVER client/package contract mismatch")

    environment = data.get("environment") or {}
    if environment.get("emulator") is not expected_emulator:
        target = "emulator" if expected_emulator else "physical device"
        raise ReadinessError(f"{path}: evidence is not from expected {target}")
    if expected_runner_hint is not None and environment.get("runnerHint") != expected_runner_hint:
        raise ReadinessError(
            f"{path}: runnerHint must be {expected_runner_hint!r}, got {environment.get('runnerHint')!r}"
        )

    network = data.get("networkFinal") or {}
    if network.get("internet") is not True or network.get("validated") is not True:
        raise ReadinessError(f"{path}: final network is not INTERNET+VALIDATED")

    contract = data.get("visualContract") or {}
    if contract.get("markerBatchId") != EXPECTED_BATCH_ID:
        raise ReadinessError(f"{path}: marker batch id does not match current gate")
    if contract.get("markerCandidateAssetCount") != EXPECTED_ASSET_COUNT:
        raise ReadinessError(f"{path}: marker candidate count is not {EXPECTED_ASSET_COUNT}")
    if contract.get("markerCandidateFingerprintSha256") != expected_marker_fingerprint:
        raise ReadinessError(f"{path}: marker candidate fingerprint does not match current batch")

    captures = data.get("matrixCaptures")
    if not isinstance(captures, list) or len(captures) != 28:
        raise ReadinessError(f"{path}: expected exactly 28 NAVER matrix captures")
    baseline = [item for item in captures if item.get("kind") == "baseline"]
    ev1 = [item for item in captures if item.get("kind") == "ev1_checkpoint"]
    if len(baseline) != 18 or len(ev1) != 10:
        raise ReadinessError(f"{path}: expected baseline=18 and ev1=10")
    if any(item.get("technicalCaptureCompleted") is not True for item in captures):
        raise ReadinessError(f"{path}: matrix contains incomplete technical capture")

    attempts = data.get("baseMapAttempts")
    if not isinstance(attempts, list) or not attempts or not any(item.get("passed") is True for item in attempts):
        raise ReadinessError(f"{path}: no passing marker-free NAVER base-map evidence")


def verify_human_approval(path: Path, expected_marker_fingerprint: str) -> None:
    approval = load_json(path)
    if approval.get("schema_version") != 1:
        raise ReadinessError(f"{path}: unsupported human approval schema")
    if approval.get("marker_batch_id") != EXPECTED_BATCH_ID:
        raise ReadinessError(f"{path}: human approval references wrong marker batch")
    if approval.get("marker_candidate_fingerprint_sha256") != expected_marker_fingerprint:
        raise ReadinessError(f"{path}: human approval fingerprint does not match current marker batch")
    if approval.get("decision") != "APPROVED":
        raise ReadinessError(f"{path}: human decision is not APPROVED")
    if not str(approval.get("reviewer", "")).strip() or not str(approval.get("reviewed_at", "")).strip():
        raise ReadinessError(f"{path}: reviewer and reviewed_at are required")

    checks = approval.get("checks")
    if not isinstance(checks, dict):
        raise ReadinessError(f"{path}: human checks object is required")
    missing = sorted(REQUIRED_HUMAN_CHECKS - checks.keys())
    if missing:
        raise ReadinessError(f"{path}: missing human checks: {missing}")
    failed = sorted(name for name in REQUIRED_HUMAN_CHECKS if checks.get(name) != "PASS")
    if failed:
        raise ReadinessError(f"{path}: human checks not PASS: {failed}")


def verify_readiness(
    *,
    root: Path,
    marker_batch: Path,
    emulator_session: Path,
    physical_session: Path,
    human_approval: Path,
) -> str:
    fingerprint = verify_marker_batch(root, marker_batch)
    verify_naver_session(
        emulator_session,
        expected_marker_fingerprint=fingerprint,
        expected_emulator=True,
        expected_runner_hint="pixel2Api30Atd",
    )
    verify_naver_session(
        physical_session,
        expected_marker_fingerprint=fingerprint,
        expected_emulator=False,
        expected_runner_hint="physical-connected-device",
    )
    verify_human_approval(human_approval, fingerprint)
    return fingerprint


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Verify all evidence required before marker candidates may be promoted."
    )
    parser.add_argument("--marker-batch", type=Path, default=DEFAULT_MARKER_BATCH)
    parser.add_argument("--emulator-session", type=Path, required=True)
    parser.add_argument("--physical-session", type=Path, required=True)
    parser.add_argument("--human-approval", type=Path, required=True)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    fingerprint = verify_readiness(
        root=ROOT,
        marker_batch=args.marker_batch,
        emulator_session=args.emulator_session,
        physical_session=args.physical_session,
        human_approval=args.human_approval,
    )
    print(
        "marker promotion readiness PASS: "
        f"{EXPECTED_ASSET_COUNT} assets, emulator 18+10, physical 18+10, "
        f"human APPROVED, fingerprint={fingerprint}"
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"marker promotion readiness FAIL: {exc}", file=sys.stderr)
        raise SystemExit(1)
