import copy
import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

MODULE_PATH = Path(__file__).with_name('verify_marker_activation_contract.py')
spec = importlib.util.spec_from_file_location('marker_activation_contract', MODULE_PATH)
marker_activation = importlib.util.module_from_spec(spec)
assert spec.loader is not None
spec.loader.exec_module(marker_activation)


class MarkerActivationContractTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.batch_path = self.root / 'design/production/marker-split-export-v1.json'
        self.app_build = self.root / 'app/build.gradle.kts'
        self.registry = self.root / 'app/src/main/java/com/dailytown/app/ui/visual/ProductionMarkerAssetRegistry.kt'

        assets = []
        for family in ('DAY', 'DARK'):
            for index, semantic in enumerate(sorted(marker_activation.EXPECTED_SEMANTICS)):
                rel = Path(f'design/production/markers/v1/{family.lower()}/{index}.svg')
                path = self.root / rel
                path.parent.mkdir(parents=True, exist_ok=True)
                content = f'<svg><title>{family}:{semantic}</title></svg>\n'.encode()
                path.write_bytes(content)
                assets.append({
                    'family': family,
                    'semantic_key': semantic,
                    'path': rel.as_posix(),
                    'sha256': hashlib.sha256(content).hexdigest(),
                    'approval_state': marker_activation.EXPECTED_CANDIDATE_ASSET_STATE,
                })

        self.batch = {
            'schema_version': 1,
            'batch_id': marker_activation.EXPECTED_BATCH_ID,
            'status': marker_activation.EXPECTED_CANDIDATE_STATUS,
            'asset_count': marker_activation.EXPECTED_ASSET_COUNT,
            'assets': assets,
        }
        self.write_json(self.batch_path, self.batch)
        self.write_candidate_sources()

    def tearDown(self):
        self.temp.cleanup()

    @staticmethod
    def write_json(path, value):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(value), encoding='utf-8')

    def write_candidate_sources(self):
        self.app_build.parent.mkdir(parents=True, exist_ok=True)
        self.app_build.write_text(
            'sourceSets {\n'
            '    getByName("main").assets.directories.add("../design/production/companion")\n'
            '    getByName("androidTest").assets.directories.add("../design/production")\n'
            '}\n',
            encoding='utf-8',
        )
        self.registry.parent.mkdir(parents=True, exist_ok=True)
        self.registry.write_text(
            'object ProductionMarkerAssetRegistry {\n'
            '    private val index = MarkerProductionAssetIndex(emptyList())\n'
            '    const val PROMOTED_MARKER_COUNT = 0\n'
            '}\n',
            encoding='utf-8',
        )

    def write_production_sources(self, *, drop_last=False):
        self.app_build.write_text(
            'sourceSets {\n'
            '    getByName("main").assets.directories.add("../design/production/companion")\n'
            '    getByName("main").assets.directories.add("../design/production/markers/v1")\n'
            '}\n',
            encoding='utf-8',
        )
        records = []
        source_assets = self.batch['assets'][:-1] if drop_last else self.batch['assets']
        for asset in source_assets:
            runtime_path = Path(asset['path']).relative_to('design/production/markers/v1').as_posix()
            records.append(
                f'        marker(MarkerFamily.{asset["family"]}, "{asset["semantic_key"]}", "{runtime_path}"),'
            )
        self.registry.write_text(
            'object ProductionMarkerAssetRegistry {\n'
            '    private val index = MarkerProductionAssetIndex(\n'
            '        listOf(\n'
            + '\n'.join(records)
            + '\n        ),\n'
            '    )\n'
            '    const val PROMOTED_MARKER_COUNT = 24\n'
            '}\n',
            encoding='utf-8',
        )

    def promote_batch_metadata(self):
        promoted = copy.deepcopy(self.batch)
        promoted['status'] = marker_activation.EXPECTED_PRODUCTION_STATUS
        for asset in promoted['assets']:
            asset['approval_state'] = marker_activation.EXPECTED_PRODUCTION_ASSET_STATE
        self.batch = promoted
        self.write_json(self.batch_path, promoted)

    def verify_candidate(self):
        return marker_activation.verify_candidate_state(
            root=self.root,
            batch_path=self.batch_path,
            app_build=self.app_build,
            marker_registry=self.registry,
        )

    def verify_production(self):
        return marker_activation.verify_production_state(
            root=self.root,
            batch_path=self.batch_path,
            app_build=self.app_build,
            marker_registry=self.registry,
        )

    def test_actual_repository_remains_candidate_only(self):
        fingerprint = marker_activation.verify_candidate_state(
            root=marker_activation.ROOT,
            batch_path=marker_activation.DEFAULT_MARKER_BATCH,
            app_build=marker_activation.DEFAULT_APP_BUILD,
            marker_registry=marker_activation.DEFAULT_MARKER_REGISTRY,
        )
        self.assertRegex(fingerprint, r'^[0-9a-f]{64}$')

    def test_candidate_state_passes_and_plan_is_exact_family_aware_24(self):
        fingerprint = self.verify_candidate()
        plan = marker_activation.activation_plan(self.root, self.batch_path)
        self.assertEqual(fingerprint, plan['marker_candidate_fingerprint_sha256'])
        self.assertEqual(24, plan['promoted_marker_count'])
        self.assertEqual('../design/production/markers/v1', plan['main_asset_source_root'])
        self.assertEqual(24, len(plan['records']))
        pairs = {(item['family'], item['semantic_key']) for item in plan['records']}
        self.assertEqual(24, len(pairs))
        self.assertEqual({'DAY', 'DARK'}, {item['family'] for item in plan['records']})
        self.assertTrue(all(item['runtime_asset_path'].startswith(item['family'].lower() + '/') for item in plan['records']))

    def test_candidate_state_rejects_main_apk_marker_source_exposure(self):
        self.app_build.write_text(
            'sourceSets {\n'
            '    getByName("main").assets.directories.add("../design/production/markers/v1")\n'
            '}\n',
            encoding='utf-8',
        )
        with self.assertRaisesRegex(marker_activation.ActivationContractError, 'must not be exposed'):
            self.verify_candidate()

    def test_candidate_state_rejects_nonzero_registry(self):
        self.registry.write_text(
            'object ProductionMarkerAssetRegistry {\n'
            '    private val index = MarkerProductionAssetIndex(emptyList())\n'
            '    const val PROMOTED_MARKER_COUNT = 24\n'
            '}\n',
            encoding='utf-8',
        )
        with self.assertRaisesRegex(marker_activation.ActivationContractError, 'PROMOTED_MARKER_COUNT = 0'):
            self.verify_candidate()

    def test_production_state_passes_only_with_exact_24_records(self):
        self.promote_batch_metadata()
        self.write_production_sources()
        fingerprint = self.verify_production()
        self.assertRegex(fingerprint, r'^[0-9a-f]{64}$')

    def test_production_state_rejects_missing_family_aware_record(self):
        self.promote_batch_metadata()
        self.write_production_sources(drop_last=True)
        with self.assertRaisesRegex(marker_activation.ActivationContractError, 'exactly 24'):
            self.verify_production()

    def test_production_state_rejects_candidate_metadata(self):
        self.write_production_sources()
        with self.assertRaisesRegex(marker_activation.ActivationContractError, 'batch status'):
            self.verify_production()

    def test_changed_marker_bytes_are_rejected_in_both_states(self):
        first = self.batch['assets'][0]
        (self.root / first['path']).write_text('changed', encoding='utf-8')
        with self.assertRaisesRegex(marker_activation.ActivationContractError, 'checksum mismatch'):
            self.verify_candidate()


if __name__ == '__main__':
    unittest.main()
