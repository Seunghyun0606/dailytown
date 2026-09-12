from pathlib import Path
import unittest


BASELINE_PATH = Path(__file__).with_name("mvp_baseline.env")

EXPECTED = {
    "FIELD_TEST_MIN_SESSION_SECONDS": "600",
    "FIELD_TEST_MAX_GPS_REJECTION_PERCENT": "15",
    "FIELD_TEST_REQUIRE_MAP_READY": "true",
    "FIELD_TEST_MAX_DISTANCE_ERROR_PERCENT": "15",
    "FIELD_TEST_MAX_BATTERY_DRAIN_PERCENT_PER_HOUR": "12",
    "FIELD_TEST_MIN_ENCOUNTERS_PER_SESSION": "2",
    "FIELD_TEST_MIN_ENCOUNTER_RESOLUTION_PERCENT": "50",
    "FIELD_TEST_MAX_REPEAT_AREA_FATIGUE_PERCENT": "40",
    "FIELD_TEST_COMPARISON_MIN_SESSIONS_PER_COHORT": "2",
    "FIELD_TEST_COMPARISON_REQUIRE_MATCHING_PRESET": "true",
    "FIELD_TEST_COMPARISON_REQUIRED_EVIDENCE": (
        "SESSION_DURATION,SESSION_DISTANCE,GPS_REJECTION_RATE,DISTANCE_ERROR,"
        "BATTERY_DRAIN,DISCOVERED_ENCOUNTERS,ENCOUNTER_RESOLUTION,REVISIT_SHARE,"
        "REPEAT_AREA_FATIGUE"
    ),
}


def parse_exports(text: str) -> dict[str, str]:
    parsed: dict[str, str] = {}
    for raw_line in text.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if not line.startswith("export ") or "=" not in line:
            raise AssertionError(f"unsupported baseline line: {raw_line}")
        key, value = line[len("export "):].split("=", 1)
        if key in parsed:
            raise AssertionError(f"duplicate baseline key: {key}")
        parsed[key] = value
    return parsed


class MvpBaselineContractTest(unittest.TestCase):
    def test_baseline_matches_approved_pilot_contract(self) -> None:
        parsed = parse_exports(BASELINE_PATH.read_text(encoding="utf-8"))
        self.assertEqual(EXPECTED, parsed)

    def test_baseline_contains_no_credentials(self) -> None:
        text = BASELINE_PATH.read_text(encoding="utf-8")
        self.assertNotIn("NAVER_MAP_NCP_KEY_ID=", text)
        self.assertNotIn("NCP_KEY", text)

    def test_percent_thresholds_are_valid_percentages(self) -> None:
        parsed = parse_exports(BASELINE_PATH.read_text(encoding="utf-8"))
        percent_keys = (
            "FIELD_TEST_MAX_GPS_REJECTION_PERCENT",
            "FIELD_TEST_MAX_DISTANCE_ERROR_PERCENT",
            "FIELD_TEST_MIN_ENCOUNTER_RESOLUTION_PERCENT",
            "FIELD_TEST_MAX_REPEAT_AREA_FATIGUE_PERCENT",
        )
        for key in percent_keys:
            self.assertIn(int(parsed[key]), range(0, 101), key)


if __name__ == "__main__":
    unittest.main()
