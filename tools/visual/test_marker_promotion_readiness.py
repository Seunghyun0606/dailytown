import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

MODULE_PATH = Path(__file__).with_name('verify_marker_promotion_readiness.py')
spec = importlib.util.spec_from_file_location('marker_readiness', MODULE_PATH)
marker_readiness = importlib.util.module_from_spec(spec)
assert spec.loader is not None
spec.loader.exec_module(marker_readiness)


class MarkerPromotionReadinessTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.batch_path = self.root / 'design/production/marker-split-export-v1.json'
        self.batch_path.parent.mkdir(parents=True)
        assets = []
        for family in ('DAY', 'DARK'):
            for index, semantic in enumerate(sorted(marker_readiness.EXPECTED_SEMANTICS)):
                rel = Path(f'design/production/markers/v1/{family.lower()}/{index}.svg')
                path = self.root / rel
                path.parent.mkdir(parents=True, exist_ok=True)
                content = f'<svg><title>{family}:{semantic}</title></svg>\n'.encode()
                path.write_bytes(content)
                assets.append({
                    'family': family,
                    'semantic_key': semantic,
                    'path': str(rel),
                    'sha256': hashlib.sha256(content).hexdigest(),
                    'approval_state': 'production_export_candidate',
                })
        self.batch = {
            'schema_version': 1,
            'batch_id': marker_readiness.EXPECTED_BATCH_ID,
            'status': 'design_qa_complete_production_export_candidate',
            'asset_count': 24,
            'common_geo_anchor': marker_readiness.EXPECTED_ANCHOR,
            'assets': assets,
        }
        self.write_json(self.batch_path, self.batch)
        self.fingerprint = marker_readiness.marker_fingerprint(assets)
        self.emulator_path = self.root / 'emulator.json'
        self.physical_path = self.root / 'physical.json'
        self.approval_path = self.root / 'approval.json'
        self.write_json(self.emulator_path, self.session(True, 'pixel2Api30Atd'))
        self.write_json(self.physical_path, self.session(False, 'physical-connected-device'))
        self.write_json(self.approval_path, self.approval('APPROVED'))

    def tearDown(self):
        self.temp.cleanup()

    @staticmethod
    def write_json(path, value):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(value), encoding='utf-8')

    def session(self, emulator, runner_hint):
        captures = [
            {'kind': 'baseline', 'technicalCaptureCompleted': True}
            for _ in range(18)
        ] + [
            {'kind': 'ev1_checkpoint', 'technicalCaptureCompleted': True}
            for _ in range(10)
        ]
        return {
            'schemaVersion': 3,
            'outcome': 'PASS',
            'failureCategory': None,
            'packageName': 'com.dailytown.app',
            'naverCredentialConfigured': True,
            'naverClient': {
                'mode': 'NCP_KEY_ID',
                'expectedRegisteredAndroidPackage': 'com.dailytown.app',
                'packageMatchesExpected': True,
            },
            'environment': {'emulator': emulator, 'runnerHint': runner_hint},
            'networkFinal': {'internet': True, 'validated': True},
            'visualContract': {
                'markerBatchId': marker_readiness.EXPECTED_BATCH_ID,
                'markerCandidateAssetCount': 24,
                'markerCandidateFingerprintSha256': self.fingerprint,
            },
            'baseMapAttempts': [{'passed': True}],
            'matrixCaptures': captures,
        }

    def approval(self, decision):
        return {
            'schema_version': 1,
            'marker_batch_id': marker_readiness.EXPECTED_BATCH_ID,
            'marker_candidate_fingerprint_sha256': self.fingerprint,
            'decision': decision,
            'reviewer': 'human-reviewer',
            'reviewed_at': '2026-08-27T15:00:00+09:00',
            'checks': {name: 'PASS' for name in marker_readiness.REQUIRED_HUMAN_CHECKS},
        }

    def verify(self):
        return marker_readiness.verify_readiness(
            root=self.root,
            marker_batch=self.batch_path,
            emulator_session=self.emulator_path,
            physical_session=self.physical_path,
            human_approval=self.approval_path,
        )

    def test_full_bound_evidence_passes(self):
        self.assertEqual(self.fingerprint, self.verify())

    def test_physical_evidence_cannot_be_emulator(self):
        self.write_json(self.physical_path, self.session(True, 'physical-connected-device'))
        with self.assertRaisesRegex(marker_readiness.ReadinessError, 'physical device'):
            self.verify()

    def test_stale_marker_fingerprint_is_rejected(self):
        session = self.session(True, 'pixel2Api30Atd')
        session['visualContract']['markerCandidateFingerprintSha256'] = '0' * 64
        self.write_json(self.emulator_path, session)
        with self.assertRaisesRegex(marker_readiness.ReadinessError, 'fingerprint'):
            self.verify()

    def test_pending_human_decision_is_rejected(self):
        self.write_json(self.approval_path, self.approval('PENDING'))
        with self.assertRaisesRegex(marker_readiness.ReadinessError, 'not APPROVED'):
            self.verify()

    def test_changed_candidate_bytes_are_rejected(self):
        first = self.batch['assets'][0]
        (self.root / first['path']).write_text('changed', encoding='utf-8')
        with self.assertRaisesRegex(marker_readiness.ReadinessError, 'checksum mismatch'):
            self.verify()


if __name__ == '__main__':
    unittest.main()
