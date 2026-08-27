#!/usr/bin/env python3
"""Fail-closed integrity verifier for Daily Town physical marker evidence bundles."""

from __future__ import annotations

import argparse
import json
from pathlib import Path, PurePosixPath
import shutil
import stat
import sys
import tempfile
from typing import Any
import zipfile

from package_marker_physical_evidence import EXPECTED_CAPTURE_COUNT, sha256_file
from verify_marker_promotion_readiness import (
    DEFAULT_MARKER_BATCH,
    EXPECTED_ASSET_COUNT,
    EXPECTED_BATCH_ID,
    REQUIRED_HUMAN_CHECKS,
    ROOT,
    load_json,
    verify_marker_batch,
    verify_naver_session,
)

EXPECTED_BUNDLE_TYPE = "dailytown_marker_physical_evidence"
EXPECTED_ROOT_FILES = {
    "session.json",
    "bundle-manifest.v1.json",
    "marker-promotion-approval.v1.json",
    "REVIEW.md",
}
ALLOWED_CHECK_STATES = {"PENDING", "PASS", "FAIL"}
ALLOWED_DECISIONS = {"PENDING", "APPROVED"}
MAX_ZIP_ENTRY_BYTES = 64 * 1024 * 1024
MAX_ZIP_TOTAL_BYTES = 1024 * 1024 * 1024


class BundleIntegrityError(AssertionError):
    pass


def _safe_relative_path(value: Any, *, field: str, prefix: str | None = None) -> PurePosixPath:
    if not isinstance(value, str) or not value or "\\" in value:
        raise BundleIntegrityError(f"{field}: invalid relative POSIX path: {value!r}")
    path = PurePosixPath(value)
    if path.is_absolute() or any(part in ("", ".", "..") for part in path.parts):
        raise BundleIntegrityError(f"{field}: unsafe relative path: {value!r}")
    if prefix is not None and (not path.parts or path.parts[0] != prefix):
        raise BundleIntegrityError(f"{field}: path must stay under {prefix}/: {value!r}")
    return path


def _expected_capture_rows(session: dict[str, Any]) -> dict[str, dict[str, Any]]:
    captures = session.get("matrixCaptures")
    if not isinstance(captures, list) or len(captures) != EXPECTED_CAPTURE_COUNT:
        raise BundleIntegrityError(f"session must reference exactly {EXPECTED_CAPTURE_COUNT} captures")

    rows: dict[str, dict[str, Any]] = {}
    ids: set[str] = set()
    for capture in captures:
        if not isinstance(capture, dict):
            raise BundleIntegrityError("session capture entry must be an object")
        kind = capture.get("kind")
        if kind not in {"baseline", "ev1_checkpoint"}:
            raise BundleIntegrityError(f"session capture has unexpected kind: {kind!r}")
        capture_id = capture.get("id")
        if not isinstance(capture_id, str) or not capture_id:
            raise BundleIntegrityError("session capture id is required")
        if capture_id in ids:
            raise BundleIntegrityError(f"duplicate session capture id: {capture_id}")
        ids.add(capture_id)

        storage = _safe_relative_path(capture.get("storageName"), field="session storageName", prefix="visual")
        if len(storage.parts) < 4 or storage.parts[1] != "naver-matrix":
            raise BundleIntegrityError(f"session storageName is outside visual/naver-matrix: {storage}")
        expected_group = "baseline" if kind == "baseline" else "ev1_checkpoint"
        if storage.parts[2] != expected_group:
            raise BundleIntegrityError(f"session capture kind/storageName mismatch: {storage}")
        if storage.suffix == ".png":
            raise BundleIntegrityError(f"session storageName must omit .png: {storage}")

        rel = PurePosixPath("captures", *storage.parts[2:-1], f"{storage.parts[-1]}.png")
        key = rel.as_posix()
        if key in rows:
            raise BundleIntegrityError(f"duplicate expected capture path: {key}")
        rows[key] = {"kind": kind, "id": capture_id}
    return rows


def _verify_approval(
    path: Path,
    *,
    expected_fingerprint: str,
    expected_session_sha256: str,
) -> dict[str, Any]:
    approval = load_json(path)
    if approval.get("schema_version") != 1:
        raise BundleIntegrityError("approval schema_version must be 1")
    if approval.get("marker_batch_id") != EXPECTED_BATCH_ID:
        raise BundleIntegrityError("approval marker_batch_id mismatch")
    if approval.get("marker_candidate_fingerprint_sha256") != expected_fingerprint:
        raise BundleIntegrityError("approval marker fingerprint mismatch")
    if approval.get("physical_session_sha256") != expected_session_sha256:
        raise BundleIntegrityError("approval physical session SHA-256 mismatch")

    decision = approval.get("decision")
    if decision not in ALLOWED_DECISIONS:
        raise BundleIntegrityError(f"approval decision is invalid: {decision!r}")
    checks = approval.get("checks")
    if not isinstance(checks, dict) or set(checks) != REQUIRED_HUMAN_CHECKS:
        raise BundleIntegrityError("approval human-check set mismatch")
    invalid_states = sorted(
        name for name, value in checks.items() if value not in ALLOWED_CHECK_STATES
    )
    if invalid_states:
        raise BundleIntegrityError(f"approval checks have invalid states: {invalid_states}")

    if decision == "APPROVED":
        failed = sorted(name for name in REQUIRED_HUMAN_CHECKS if checks.get(name) != "PASS")
        if failed:
            raise BundleIntegrityError(f"APPROVED bundle has non-PASS checks: {failed}")
        if not str(approval.get("reviewer", "")).strip() or not str(approval.get("reviewed_at", "")).strip():
            raise BundleIntegrityError("APPROVED bundle requires reviewer and reviewed_at")
    return approval


def verify_bundle_directory(
    bundle_dir: Path,
    *,
    marker_batch: Path = DEFAULT_MARKER_BATCH,
) -> dict[str, Any]:
    bundle_dir = bundle_dir.resolve()
    if not bundle_dir.is_dir():
        raise BundleIntegrityError(f"bundle directory does not exist: {bundle_dir}")

    fingerprint = verify_marker_batch(ROOT, marker_batch)
    manifest_path = bundle_dir / "bundle-manifest.v1.json"
    session_path = bundle_dir / "session.json"
    approval_path = bundle_dir / "marker-promotion-approval.v1.json"
    review_path = bundle_dir / "REVIEW.md"

    manifest = load_json(manifest_path)
    if manifest.get("schema_version") != 1 or manifest.get("bundle_type") != EXPECTED_BUNDLE_TYPE:
        raise BundleIntegrityError("bundle manifest schema/type mismatch")
    if manifest.get("marker_batch_id") != EXPECTED_BATCH_ID:
        raise BundleIntegrityError("bundle manifest marker_batch_id mismatch")
    if manifest.get("marker_candidate_asset_count") != EXPECTED_ASSET_COUNT:
        raise BundleIntegrityError("bundle manifest marker asset count mismatch")
    if manifest.get("marker_candidate_fingerprint_sha256") != fingerprint:
        raise BundleIntegrityError("bundle manifest marker fingerprint mismatch")
    if manifest.get("runner_hint") != "physical-connected-device" or manifest.get("emulator") is not False:
        raise BundleIntegrityError("bundle manifest is not physical-connected-device evidence")
    if manifest.get("matrix_capture_count") != EXPECTED_CAPTURE_COUNT:
        raise BundleIntegrityError("bundle manifest capture count mismatch")
    if manifest.get("human_approval_state") != "PENDING":
        raise BundleIntegrityError("bundle manifest packaging approval state must remain PENDING")
    if manifest.get("promotion_performed") is not False:
        raise BundleIntegrityError("bundle must not claim promotion was performed")

    if not review_path.is_file():
        raise BundleIntegrityError("bundle REVIEW.md is missing")
    expected_session_sha = sha256_file(session_path)
    if manifest.get("physical_session_sha256") != expected_session_sha:
        raise BundleIntegrityError("bundle manifest physical session SHA-256 mismatch")
    expected_review_sha = manifest.get("review_sha256")
    if not isinstance(expected_review_sha, str) or expected_review_sha != sha256_file(review_path):
        raise BundleIntegrityError("bundle REVIEW.md SHA-256 mismatch")

    verify_naver_session(
        session_path,
        expected_marker_fingerprint=fingerprint,
        expected_emulator=False,
        expected_runner_hint="physical-connected-device",
    )
    session = load_json(session_path)
    environment = session.get("environment") or {}
    if manifest.get("android_release") != environment.get("androidRelease"):
        raise BundleIntegrityError("bundle manifest Android release does not match session")
    if manifest.get("device_model") != environment.get("model"):
        raise BundleIntegrityError("bundle manifest device model does not match session")

    expected_rows = _expected_capture_rows(session)
    captures = manifest.get("captures")
    if not isinstance(captures, list) or len(captures) != EXPECTED_CAPTURE_COUNT:
        raise BundleIntegrityError("bundle manifest must contain exactly 28 capture records")

    manifest_paths: set[str] = set()
    for record in captures:
        if not isinstance(record, dict):
            raise BundleIntegrityError("bundle manifest capture record must be an object")
        rel = _safe_relative_path(record.get("path"), field="manifest capture path", prefix="captures")
        key = rel.as_posix()
        if key in manifest_paths:
            raise BundleIntegrityError(f"duplicate manifest capture path: {key}")
        manifest_paths.add(key)
        expected = expected_rows.get(key)
        if expected is None:
            raise BundleIntegrityError(f"manifest capture is not referenced by session: {key}")
        if record.get("kind") != expected["kind"] or record.get("id") != expected["id"]:
            raise BundleIntegrityError(f"manifest/session capture identity mismatch: {key}")
        capture_path = bundle_dir.joinpath(*rel.parts)
        if not capture_path.is_file():
            raise BundleIntegrityError(f"bundle capture file is missing: {key}")
        if record.get("sha256") != sha256_file(capture_path):
            raise BundleIntegrityError(f"bundle capture SHA-256 mismatch: {key}")

    if manifest_paths != set(expected_rows):
        missing = sorted(set(expected_rows) - manifest_paths)
        raise BundleIntegrityError(f"bundle manifest is missing session captures: {missing}")

    actual_files = {
        path.relative_to(bundle_dir).as_posix()
        for path in bundle_dir.rglob("*")
        if path.is_file()
    }
    expected_files = EXPECTED_ROOT_FILES | manifest_paths
    if actual_files != expected_files:
        missing = sorted(expected_files - actual_files)
        extra = sorted(actual_files - expected_files)
        raise BundleIntegrityError(f"bundle file set mismatch missing={missing} extra={extra}")

    approval = _verify_approval(
        approval_path,
        expected_fingerprint=fingerprint,
        expected_session_sha256=expected_session_sha,
    )
    return {
        "marker_fingerprint": fingerprint,
        "physical_session_sha256": expected_session_sha,
        "capture_count": EXPECTED_CAPTURE_COUNT,
        "approval_decision": approval.get("decision"),
    }


def _validate_zip_entries(archive: zipfile.ZipFile) -> str:
    seen: set[str] = set()
    roots: set[str] = set()
    total_size = 0
    for info in archive.infolist():
        name = info.filename
        if "\\" in name or name.startswith("/"):
            raise BundleIntegrityError(f"ZIP contains unsafe entry: {name!r}")
        path = PurePosixPath(name.rstrip("/"))
        if not path.parts or any(part in ("", ".", "..") for part in path.parts):
            raise BundleIntegrityError(f"ZIP contains unsafe entry: {name!r}")
        normalized = path.as_posix()
        if normalized in seen:
            raise BundleIntegrityError(f"ZIP contains duplicate entry: {normalized}")
        seen.add(normalized)
        roots.add(path.parts[0])

        mode = info.external_attr >> 16
        if stat.S_ISLNK(mode):
            raise BundleIntegrityError(f"ZIP contains symlink entry: {normalized}")
        if not info.is_dir():
            if info.file_size > MAX_ZIP_ENTRY_BYTES:
                raise BundleIntegrityError(f"ZIP entry is too large: {normalized}")
            total_size += info.file_size
            if total_size > MAX_ZIP_TOTAL_BYTES:
                raise BundleIntegrityError("ZIP uncompressed size exceeds integrity limit")
    if len(roots) != 1:
        raise BundleIntegrityError(f"ZIP must contain exactly one top-level bundle directory, got {sorted(roots)}")
    return next(iter(roots))


def verify_bundle_zip(
    zip_path: Path,
    *,
    marker_batch: Path = DEFAULT_MARKER_BATCH,
) -> dict[str, Any]:
    zip_path = zip_path.resolve()
    if not zip_path.is_file():
        raise BundleIntegrityError(f"bundle ZIP does not exist: {zip_path}")
    try:
        with zipfile.ZipFile(zip_path, "r") as archive:
            root_name = _validate_zip_entries(archive)
            with tempfile.TemporaryDirectory(prefix="dailytown-marker-bundle-") as td:
                temp_root = Path(td)
                for info in archive.infolist():
                    path = PurePosixPath(info.filename.rstrip("/"))
                    target = temp_root.joinpath(*path.parts)
                    if info.is_dir():
                        target.mkdir(parents=True, exist_ok=True)
                        continue
                    target.parent.mkdir(parents=True, exist_ok=True)
                    with archive.open(info, "r") as source, target.open("wb") as destination:
                        shutil.copyfileobj(source, destination)
                return verify_bundle_directory(temp_root / root_name, marker_batch=marker_batch)
    except zipfile.BadZipFile as exc:
        raise BundleIntegrityError(f"invalid bundle ZIP: {zip_path}: {exc}") from exc


def verify_bundle(bundle: Path, *, marker_batch: Path = DEFAULT_MARKER_BATCH) -> dict[str, Any]:
    if bundle.is_dir():
        return verify_bundle_directory(bundle, marker_batch=marker_batch)
    if bundle.is_file() and bundle.suffix.lower() == ".zip":
        return verify_bundle_zip(bundle, marker_batch=marker_batch)
    raise BundleIntegrityError(f"bundle must be a directory or .zip file: {bundle}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Verify a Daily Town physical marker evidence bundle directory or ZIP without promoting assets."
    )
    parser.add_argument("--bundle", type=Path, required=True)
    parser.add_argument("--marker-batch", type=Path, default=DEFAULT_MARKER_BATCH)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    result = verify_bundle(args.bundle, marker_batch=args.marker_batch)
    print(
        "physical marker evidence bundle integrity PASS: "
        f"captures={result['capture_count']}, approval={result['approval_decision']}, "
        f"fingerprint={result['marker_fingerprint']}, session_sha256={result['physical_session_sha256']}"
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"physical marker evidence bundle integrity FAIL: {exc}", file=sys.stderr)
        raise SystemExit(1)
