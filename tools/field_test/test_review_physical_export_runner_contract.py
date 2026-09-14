from pathlib import Path
import unittest


class ReviewPhysicalExportRunnerContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.script = Path("tools/field_test/review_physical_export.sh").read_text(encoding="utf-8")

    def test_resolves_a_working_python_39_plus_runtime(self) -> None:
        self.assertIn("resolve_python()", self.script)
        self.assertIn("for candidate in python3 python", self.script)
        self.assertIn("py -3 -c", self.script)
        self.assertIn("Python 3.9+ is required", self.script)

    def test_all_review_tools_use_the_resolved_python_command(self) -> None:
        for tool in ("validate_export.py", "review_report.py", "collection_plan.py"):
            self.assertIn(f'"${{PYTHON_CMD[@]}}" "$SCRIPT_DIR/{tool}"', self.script)

    def test_review_execution_does_not_hardcode_python3(self) -> None:
        executable_lines = [
            line.strip()
            for line in self.script.splitlines()
            if line.strip() and not line.lstrip().startswith("#")
        ]
        hardcoded = [
            line
            for line in executable_lines
            if line.startswith("python3 ") and " -c " not in line
        ]
        self.assertEqual([], hardcoded)


if __name__ == "__main__":
    unittest.main()
