import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import zipfile

import package_marker_physical_evidence as pack
import verify_marker_physical_evidence_bundle as verify

FP = "a" * 64


def make_session(root: Path):
    runner = root / "device-output"
    session_path = runner / "visual/naver-diagnostics/session.json"
    session_path.parent.mkdir(parents=True)
    captures = []
    for i in range(28):
        kind = "baseline" if i < 18 else "ev1_checkpoint"
        group = kind
        storage = f"visual/naver-matrix/{group}/capture-{i:02d}.dense_urban.normal"
        captures.append({
            "kind": kind,
            "id": f"capture-{i:02d}",
            "technicalCaptureCompleted": True,
            "storageName": storage,
        })
        path = runner / f"{storage}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(f"fake-png-{i}".encode())
    data = {
        "schemaVersion": 3,
        "outcome": "PASS",
        "failureCategory": None,
        "packageName": "com.dailytown.app",
        "naverCredentialConfigured": True,
        "naverClient": {
            "mode": "NCP_KEY_ID",
            "expectedRegisteredAndroidPackage": "com.dailytown.app",
            "packageMatchesExpected": True,
        },
        "visualContract": {
            "markerBatchId": "marker-split-export-v1",
            "markerCandidateAssetCount": 24,
            "markerCandidateFingerprintSha256": FP,
        },
        "environment": {
            "runnerHint": "physical-connected-device",
            "emulator": False,
            "androidRelease": "16",
            "model": "Physical Phone",
        },
        "networkFinal": {"internet": True, "validated": True},
        "matrixCaptures": captures,
        "baseMapAttempts": [{"passed": True}],
    }
    session_path.write_text(json.dumps(data), encoding="utf-8")
    return session_path


def make_template(root: Path):
    path = root / "approval-template.json"
    path.write_text(json.dumps({
        "schema_version": 1,
        "marker_batch_id": "marker-split-export-v1",
        "marker_candidate_fingerprint_sha256": "REPLACE",
        "physical_session_sha256": pack.EXPECTED_SESSION_SHA_PLACEHOLDER,
        "decision": "PENDING",
        "reviewer": "",
        "reviewed_at": "",
        "checks": {name: "PENDING" for name in verify.REQUIRED_HUMAN_CHECKS},
    }), encoding="utf-8")
    return path


def make_bundle(root: Path):
    with patch.object(pack, "verify_marker_batch", return_value=FP):
        return pack.package_evidence(
            session_path=make_session(root),
            output_dir=root / "bundle",
            marker_batch=root / "unused-marker-batch.json",
            approval_template=make_template(root),
            create_zip=True,
        )


class MarkerPhysicalEvidenceBundleIntegrityTest(unittest.TestCase):
    def verify_dir(self, bundle: Path):
        with patch.object(verify, "verify_marker_batch", return_value=FP):
            return verify.verify_bundle_directory(bundle, marker_batch=bundle / "unused.json")

    def verify_zip(self, archive: Path):
        with patch.object(verify, "verify_marker_batch", return_value=FP):
            return verify.verify_bundle_zip(archive, marker_batch=archive.parent / "unused.json")

    def test_directory_and_zip_pass_without_mutating_approval(self):
        with tempfile.TemporaryDirectory() as td:
            bundle, archive = make_bundle(Path(td))
            directory_result = self.verify_dir(bundle)
            zip_result = self.verify_zip(archive)
            self.assertEqual(directory_result["capture_count"], 28)
            self.assertEqual(directory_result["approval_decision"], "PENDING")
            self.assertEqual(directory_result, zip_result)

    def test_human_approved_bundle_passes_integrity_when_checks_are_complete(self):
        with tempfile.TemporaryDirectory() as td:
            bundle, _ = make_bundle(Path(td))
            approval_path = bundle / "marker-promotion-approval.v1.json"
            approval = json.loads(approval_path.read_text())
            approval["decision"] = "APPROVED"
            approval["reviewer"] = "human-reviewer"
            approval["reviewed_at"] = "2026-08-28T08:00:00+09:00"
            approval["checks"] = {name: "PASS" for name in verify.REQUIRED_HUMAN_CHECKS}
            approval_path.write_text(json.dumps(approval), encoding="utf-8")
            result = self.verify_dir(bundle)
            self.assertEqual(result["approval_decision"], "APPROVED")

    def test_rejects_tampered_session(self):
        with tempfile.TemporaryDirectory() as td:
            bundle, _ = make_bundle(Path(td))
            with (bundle / "session.json").open("a", encoding="utf-8") as stream:
                stream.write("\n")
            with self.assertRaisesRegex(verify.BundleIntegrityError, "session SHA-256"):
                self.verify_dir(bundle)

    def test_rejects_tampered_capture(self):
        with tempfile.TemporaryDirectory() as td:
            bundle, _ = make_bundle(Path(td))
            capture = next((bundle / "captures").rglob("*.png"))
            capture.write_bytes(b"tampered")
            with self.assertRaisesRegex(verify.BundleIntegrityError, "capture SHA-256"):
                self.verify_dir(bundle)

    def test_rejects_tampered_review_instructions(self):
        with tempfile.TemporaryDirectory() as td:
            bundle, _ = make_bundle(Path(td))
            (bundle / "REVIEW.md").write_text("tampered", encoding="utf-8")
            with self.assertRaisesRegex(verify.BundleIntegrityError, "REVIEW.md SHA-256"):
                self.verify_dir(bundle)

    def test_rejects_extra_file(self):
        with tempfile.TemporaryDirectory() as td:
            bundle, _ = make_bundle(Path(td))
            (bundle / "unexpected.txt").write_text("unexpected", encoding="utf-8")
            with self.assertRaisesRegex(verify.BundleIntegrityError, "file set mismatch"):
                self.verify_dir(bundle)

    def test_rejects_approval_from_other_session(self):
        with tempfile.TemporaryDirectory() as td:
            bundle, _ = make_bundle(Path(td))
            approval_path = bundle / "marker-promotion-approval.v1.json"
            approval = json.loads(approval_path.read_text())
            approval["physical_session_sha256"] = "b" * 64
            approval_path.write_text(json.dumps(approval), encoding="utf-8")
            with self.assertRaisesRegex(verify.BundleIntegrityError, "approval physical session"):
                self.verify_dir(bundle)

    def test_rejects_approved_bundle_with_pending_check(self):
        with tempfile.TemporaryDirectory() as td:
            bundle, _ = make_bundle(Path(td))
            approval_path = bundle / "marker-promotion-approval.v1.json"
            approval = json.loads(approval_path.read_text())
            approval["decision"] = "APPROVED"
            approval["reviewer"] = "human-reviewer"
            approval["reviewed_at"] = "2026-08-28T08:00:00+09:00"
            approval["checks"] = {name: "PASS" for name in verify.REQUIRED_HUMAN_CHECKS}
            approval["checks"]["marker_readability"] = "PENDING"
            approval_path.write_text(json.dumps(approval), encoding="utf-8")
            with self.assertRaisesRegex(verify.BundleIntegrityError, "non-PASS checks"):
                self.verify_dir(bundle)

    def test_rejects_manifest_capture_path_traversal(self):
        with tempfile.TemporaryDirectory() as td:
            bundle, _ = make_bundle(Path(td))
            manifest_path = bundle / "bundle-manifest.v1.json"
            manifest = json.loads(manifest_path.read_text())
            manifest["captures"][0]["path"] = "captures/../escape.png"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            with self.assertRaisesRegex(verify.BundleIntegrityError, "unsafe relative path"):
                self.verify_dir(bundle)

    def test_rejects_zip_path_traversal_before_extraction(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            _, archive = make_bundle(root)
            malicious = root / "malicious.zip"
            with zipfile.ZipFile(archive, "r") as source, zipfile.ZipFile(malicious, "w") as target:
                for info in source.infolist():
                    target.writestr(info, source.read(info.filename))
                target.writestr("bundle/../escape.txt", "escape")
            with self.assertRaisesRegex(verify.BundleIntegrityError, "unsafe entry"):
                self.verify_zip(malicious)

    def test_rejects_zip_symlink_before_extraction(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            _, archive = make_bundle(root)
            malicious = root / "symlink.zip"
            with zipfile.ZipFile(archive, "r") as source, zipfile.ZipFile(malicious, "w") as target:
                for info in source.infolist():
                    target.writestr(info, source.read(info.filename))
                link = zipfile.ZipInfo("bundle/captures/link")
                link.create_system = 3
                link.external_attr = (0o120777 << 16)
                target.writestr(link, "session.json")
            with self.assertRaisesRegex(verify.BundleIntegrityError, "symlink"):
                self.verify_zip(malicious)


if __name__ == "__main__":
    unittest.main()
