import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import package_marker_physical_evidence as pack

FP = "a" * 64


def make_session(root: Path, *, emulator=False, fingerprint=FP, missing_capture=False, traversal=False):
    runner = root / "device-output"
    session_path = runner / "visual/naver-diagnostics/session.json"
    session_path.parent.mkdir(parents=True)
    captures = []
    for i in range(28):
        kind = "baseline" if i < 18 else "ev1_checkpoint"
        group = "baseline" if kind == "baseline" else "ev1_checkpoint"
        storage = f"visual/naver-matrix/{group}/capture-{i:02d}.dense_urban.normal"
        if traversal and i == 0:
            storage = "visual/naver-matrix/../../escape"
        captures.append({
            "kind": kind,
            "id": f"capture-{i:02d}",
            "technicalCaptureCompleted": True,
            "storageName": storage,
        })
        if not (missing_capture and i == 27) and not (traversal and i == 0):
            p = runner / f"{storage}.png"
            p.parent.mkdir(parents=True, exist_ok=True)
            p.write_bytes(f"fake-png-{i}".encode())
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
            "markerCandidateFingerprintSha256": fingerprint,
        },
        "environment": {
            "runnerHint": "physical-connected-device",
            "emulator": emulator,
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
    path = root / "approval.json"
    path.write_text(json.dumps({
        "schema_version": 1,
        "marker_batch_id": "marker-split-export-v1",
        "marker_candidate_fingerprint_sha256": "REPLACE",
        "physical_session_sha256": pack.EXPECTED_SESSION_SHA_PLACEHOLDER,
        "decision": "PENDING",
        "reviewer": "",
        "reviewed_at": "",
        "checks": {
            "marker_readability": "PENDING",
            "selected_state_anchor": "PENDING",
            "route_hud_companion_readability": "PENDING",
            "provider_road_place_comprehension": "PENDING",
            "naver_attribution_legal_ui": "PENDING",
        },
    }), encoding="utf-8")
    return path


class PackageMarkerPhysicalEvidenceTest(unittest.TestCase):
    def run_package(self, root: Path, session: Path):
        template = make_template(root)
        with patch.object(pack, "verify_marker_batch", return_value=FP):
            return pack.package_evidence(
                session_path=session,
                output_dir=root / "bundle",
                marker_batch=root / "unused-batch.json",
                approval_template=template,
                create_zip=True,
            )

    def test_packages_28_captures_and_pending_bound_approval(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            bundle, archive = self.run_package(root, make_session(root))
            self.assertTrue(archive.is_file())
            manifest = json.loads((bundle / "bundle-manifest.v1.json").read_text())
            self.assertEqual(manifest["matrix_capture_count"], 28)
            self.assertEqual(manifest["marker_candidate_fingerprint_sha256"], FP)
            self.assertFalse(manifest["emulator"])
            self.assertFalse(manifest["promotion_performed"])
            approval = json.loads((bundle / "marker-promotion-approval.v1.json").read_text())
            self.assertEqual(approval["marker_candidate_fingerprint_sha256"], FP)
            self.assertEqual(approval["physical_session_sha256"], manifest["physical_session_sha256"])
            self.assertEqual(
                approval["physical_session_sha256"],
                pack.sha256_file(bundle / "session.json"),
            )
            self.assertEqual(approval["decision"], "PENDING")
            self.assertEqual(len(list((bundle / "captures").rglob("*.png"))), 28)

    def test_rejects_emulator_evidence(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            with self.assertRaises(pack.ReadinessError):
                self.run_package(root, make_session(root, emulator=True))

    def test_rejects_missing_capture_file(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            with self.assertRaises(pack.PackagingError):
                self.run_package(root, make_session(root, missing_capture=True))

    def test_rejects_path_traversal_storage_name(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            with self.assertRaises(pack.PackagingError):
                self.run_package(root, make_session(root, traversal=True))

    def test_rejects_stale_marker_fingerprint(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            with self.assertRaises(pack.ReadinessError):
                self.run_package(root, make_session(root, fingerprint="b" * 64))

    def test_rejects_approval_template_without_session_placeholder(self):
        with tempfile.TemporaryDirectory() as td:
            root = Path(td)
            session = make_session(root)
            template = make_template(root)
            data = json.loads(template.read_text())
            data["physical_session_sha256"] = "stale-session-hash"
            template.write_text(json.dumps(data), encoding="utf-8")
            with patch.object(pack, "verify_marker_batch", return_value=FP):
                with self.assertRaisesRegex(pack.PackagingError, "physical-session placeholder"):
                    pack.package_evidence(
                        session_path=session,
                        output_dir=root / "bundle",
                        marker_batch=root / "unused-batch.json",
                        approval_template=template,
                        create_zip=False,
                    )


if __name__ == "__main__":
    unittest.main()
