from pathlib import Path
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[2]
RUNNER = ROOT / "tools/android/run_naver_physical_evidence.sh"


class PhysicalEvidenceRunnerContractTest(unittest.TestCase):
    def test_runner_shell_syntax_is_valid(self):
        subprocess.run(["bash", "-n", str(RUNNER)], check=True)

    def test_runner_verifies_directory_and_zip_after_packaging(self):
        text = RUNNER.read_text(encoding="utf-8")
        package_at = text.index("package_marker_physical_evidence.py")
        verifier_calls = [
            index
            for index in range(len(text))
            if text.startswith("verify_marker_physical_evidence_bundle.py", index)
        ]
        self.assertEqual(len(verifier_calls), 2)
        self.assertTrue(all(index > package_at for index in verifier_calls))
        self.assertIn('--bundle "$BUNDLE_DIR"', text)
        self.assertIn('--bundle "$BUNDLE_ZIP"', text)
        self.assertIn("packaged and integrity-verified", text)


if __name__ == "__main__":
    unittest.main()
