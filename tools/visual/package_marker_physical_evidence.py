#!/usr/bin/env python3
"""Package one clean physical-device NAVER marker evidence run for human review."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import shutil
import sys
import tempfile
import zipfile
from typing import Any

from verify_marker_promotion_readiness import (
    DEFAULT_MARKER_BATCH,
    EXPECTED_ASSET_COUNT,
    EXPECTED_BATCH_ID,
    REQUIRED_HUMAN_CHECKS,
    ROOT,
    ReadinessError,
    load_json,
    verify_marker_batch,
    verify_naver_session,
)

DEFAULT_APPROVAL_TEMPLATE = ROOT / "design/export-spec/marker-promotion-approval.template.v1.json"
EXPECTED_CAPTURE_COUNT = 28
EXPECTED_SESSION_SHA_PLACEHOLDER = "REPLACE_WITH_PHYSICAL_SESSION_SHA256_FROM_BUNDLE"


class PackagingError(AssertionError):
    pass


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def capture_source_path(session_path: Path, storage_name: str) -> Path:
    rel = Path(storage_name)
    if rel.is_absolute() or ".." in rel.parts:
        raise PackagingError(f"unsafe capture storageName: {storage_name!r}")
    if len(rel.parts) < 3 or rel.parts[0:2] != ("visual", "naver-matrix"):
        raise PackagingError(f"unexpected capture storageName: {storage_name!r}")
    if storage_name.endswith(".png"):
        raise PackagingError(f"capture storageName must not include .png: {storage_name!r}")
    output_root = session_path.parent.parent.parent
    return output_root / f"{storage_name}.png"


def load_capture_sources(session_path: Path, session: dict[str, Any]) -> list[tuple[dict[str, Any], Path]]:
    captures = session.get("matrixCaptures")
    if not isinstance(captures, list) or len(captures) != EXPECTED_CAPTURE_COUNT:
        raise PackagingError(f"expected exactly {EXPECTED_CAPTURE_COUNT} matrix captures")

    seen: set[str] = set()
    result: list[tuple[dict[str, Any], Path]] = []
    for capture in captures:
        if not isinstance(capture, dict):
            raise PackagingError("matrix capture entry must be an object")
        storage_name = capture.get("storageName")
        if not isinstance(storage_name, str) or not storage_name:
            raise PackagingError("matrix capture storageName is required")
        if storage_name in seen:
            raise PackagingError(f"duplicate capture storageName: {storage_name}")
        seen.add(storage_name)
        source = capture_source_path(session_path, storage_name)
        expected_group = "baseline" if capture.get("kind") == "baseline" else "ev1_checkpoint"
        if Path(storage_name).parts[2] != expected_group:
            raise PackagingError(f"capture kind/storageName mismatch: {storage_name}")
        if not source.is_file():
            raise PackagingError(f"missing matrix capture file: {source}")
        result.append((capture, source))
    return result


def bind_pending_approval(
    template_path: Path,
    fingerprint: str,
    physical_session_sha256: str,
) -> dict[str, Any]:
    approval = load_json(template_path)
    if approval.get("schema_version") != 1 or approval.get("marker_batch_id") != EXPECTED_BATCH_ID:
        raise PackagingError("approval template schema/batch mismatch")
    if approval.get("decision") != "PENDING":
        raise PackagingError("approval template must remain PENDING")
    if approval.get("physical_session_sha256") != EXPECTED_SESSION_SHA_PLACEHOLDER:
        raise PackagingError("approval template physical-session placeholder mismatch")
    checks = approval.get("checks")
    if not isinstance(checks, dict) or set(checks) != REQUIRED_HUMAN_CHECKS:
        raise PackagingError("approval template human-check set mismatch")
    if any(value != "PENDING" for value in checks.values()):
        raise PackagingError("approval template checks must all remain PENDING")
    approval["marker_candidate_fingerprint_sha256"] = fingerprint
    approval["physical_session_sha256"] = physical_session_sha256
    return approval


def write_json(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def write_review_instructions(path: Path, fingerprint: str, physical_session_sha256: str) -> None:
    path.write_text(
        "# Daily Town marker physical review bundle\n\n"
        "This bundle is evidence only. It does not promote marker assets.\n\n"
        f"- Marker batch: `{EXPECTED_BATCH_ID}`\n"
        f"- Marker assets: `{EXPECTED_ASSET_COUNT}` candidates\n"
        f"- Fingerprint: `{fingerprint}`\n"
        f"- Physical session SHA-256: `{physical_session_sha256}`\n"
        f"- Matrix captures: `{EXPECTED_CAPTURE_COUNT}`\n\n"
        "Review every capture on the physical-device run and update "
        "`marker-promotion-approval.v1.json` only after checking marker readability, "
        "selected-state anchor, route/HUD/companion readability, provider road/place "
        "comprehension, and NAVER attribution/legal UI. Keep any non-PASS item as "
        "PENDING/FAIL; do not force approval. Do not copy approval between physical "
        "sessions: the approval is bound to this session SHA-256.\n\n"
        "After human approval, run `tools/visual/verify_marker_promotion_readiness.py` "
        "with a passing emulator session, this bundle's `session.json`, and the completed "
        "approval JSON. Readiness PASS still does not mutate production assets.\n",
        encoding="utf-8",
    )


def package_evidence(
    *,
    session_path: Path,
    output_dir: Path,
    marker_batch: Path = DEFAULT_MARKER_BATCH,
    approval_template: Path = DEFAULT_APPROVAL_TEMPLATE,
    replace: bool = False,
    create_zip: bool = True,
) -> tuple[Path, Path | None]:
    session_path = session_path.resolve()
    output_dir = output_dir.resolve()
    if output_dir.exists() and not replace:
        raise PackagingError(f"output directory already exists: {output_dir}")

    fingerprint = verify_marker_batch(ROOT, marker_batch)
    verify_naver_session(
        session_path,
        expected_marker_fingerprint=fingerprint,
        expected_emulator=False,
        expected_runner_hint="physical-connected-device",
    )
    session = load_json(session_path)
    capture_sources = load_capture_sources(session_path, session)
    physical_session_sha256 = sha256_file(session_path)
    approval = bind_pending_approval(
        approval_template,
        fingerprint,
        physical_session_sha256,
    )

    zip_path: Path | None = output_dir.parent / f"{output_dir.name}.zip" if create_zip else None
    if zip_path is not None and zip_path.exists() and not replace:
        raise PackagingError(f"output ZIP already exists: {zip_path}")

    output_dir.parent.mkdir(parents=True, exist_ok=True)
    staging = Path(tempfile.mkdtemp(prefix=f".{output_dir.name}.staging-", dir=output_dir.parent))
    try:
        shutil.copy2(session_path, staging / "session.json")
        capture_manifest: list[dict[str, Any]] = []
        for capture, source in capture_sources:
            storage_name = str(capture["storageName"])
            capture_parts = Path(storage_name).parts[2:]
            rel = Path(*capture_parts[:-1]) / f"{capture_parts[-1]}.png"
            target = staging / "captures" / rel
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, target)
            capture_manifest.append(
                {
                    "kind": capture.get("kind"),
                    "id": capture.get("id"),
                    "path": target.relative_to(staging).as_posix(),
                    "sha256": sha256_file(target),
                }
            )

        write_json(staging / "marker-promotion-approval.v1.json", approval)
        review_path = staging / "REVIEW.md"
        write_review_instructions(review_path, fingerprint, physical_session_sha256)
        manifest = {
            "schema_version": 1,
            "bundle_type": "dailytown_marker_physical_evidence",
            "marker_batch_id": EXPECTED_BATCH_ID,
            "marker_candidate_asset_count": EXPECTED_ASSET_COUNT,
            "marker_candidate_fingerprint_sha256": fingerprint,
            "physical_session_sha256": physical_session_sha256,
            "review_sha256": sha256_file(review_path),
            "runner_hint": (session.get("environment") or {}).get("runnerHint"),
            "emulator": (session.get("environment") or {}).get("emulator"),
            "android_release": (session.get("environment") or {}).get("androidRelease"),
            "device_model": (session.get("environment") or {}).get("model"),
            "matrix_capture_count": len(capture_manifest),
            "captures": capture_manifest,
            "human_approval_state": "PENDING",
            "promotion_performed": False,
        }
        write_json(staging / "bundle-manifest.v1.json", manifest)

        if output_dir.exists():
            shutil.rmtree(output_dir)
        staging.replace(output_dir)

        if zip_path is not None:
            if zip_path.exists():
                zip_path.unlink()
            temp_zip = zip_path.with_name(f".{zip_path.name}.tmp")
            with zipfile.ZipFile(temp_zip, "w", compression=zipfile.ZIP_DEFLATED) as archive:
                for source in sorted(output_dir.rglob("*")):
                    if source.is_file():
                        archive.write(source, arcname=f"{output_dir.name}/{source.relative_to(output_dir).as_posix()}")
            temp_zip.replace(zip_path)
        return output_dir, zip_path
    except Exception:
        if staging.exists():
            shutil.rmtree(staging, ignore_errors=True)
        raise


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Package one clean physical NAVER marker evidence run.")
    parser.add_argument("--session", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--marker-batch", type=Path, default=DEFAULT_MARKER_BATCH)
    parser.add_argument("--approval-template", type=Path, default=DEFAULT_APPROVAL_TEMPLATE)
    parser.add_argument("--replace", action="store_true")
    parser.add_argument("--no-zip", action="store_true")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    bundle, archive = package_evidence(
        session_path=args.session,
        output_dir=args.output_dir,
        marker_batch=args.marker_batch,
        approval_template=args.approval_template,
        replace=args.replace,
        create_zip=not args.no_zip,
    )
    print(f"physical marker evidence bundle PASS: {bundle}")
    if archive is not None:
        print(f"physical marker evidence archive: {archive}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (PackagingError, ReadinessError, OSError, ValueError) as exc:
        print(f"physical marker evidence bundle FAIL: {exc}", file=sys.stderr)
        raise SystemExit(1)
