#!/usr/bin/env python3

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from validate_export import (
    ExportValidationError,
    render_summary,
    validate_export,
)


def metric(average, evidence_count, session_count):
    return {
        "average": average,
        "evidenceCount": evidence_count,
        "sessionCount": session_count,
    }


def cohort(profile, session_count):
    has_evidence = session_count > 0
    value = 10 if has_evidence else None
    evidence_count = session_count if has_evidence else 0
    return {
        "areaProfile": profile,
        "sessionCount": session_count,
        "trackingPresets": ["BALANCED"] if has_evidence else [],
        "metrics": {
            "sessionDurationSeconds": metric(value, evidence_count, session_count),
            "sessionDistanceMeters": metric(value, evidence_count, session_count),
        },
        "acceptance": {
            "pass": 0,
            "fail": 0,
            "notEvaluated": session_count,
        },
    }


def session(ordinal, profile):
    return {
        "ordinal": ordinal,
        "areaProfile": profile,
        "trackingPreset": "BALANCED",
        "runReviewStatus": "REFERENCE_ONLY",
        "mapHealthStatus": "READY",
        "metrics": {
            "sessionDurationSeconds": 10,
            "sessionDistanceMeters": 10,
            "gpsRejectionRatePercent": 0,
            "distanceErrorPercent": None,
            "batteryDrainPercentPerHour": None,
            "discoveredEncountersPerSession": 1,
            "encounterResolutionRatePercent": 100,
            "revisitSharePercent": None,
            "repeatAreaFatigueProxyPercent": None,
        },
        "acceptanceOverall": "NOT_EVALUATED",
    }


def valid_document():
    sessions = [session(1, "NEW_AREA"), session(2, "REPEAT_AREA")]
    return {
        "schema": "dailytown.field_test_export",
        "schemaVersion": 1,
        "app": {"version": "0.7.0", "packageId": "com.dailytown.app"},
        "privacy": {
            "rawGps": False,
            "routeGeometry": False,
            "placeLabels": False,
            "eventIdentifiers": False,
            "sessionLinkage": False,
            "deviceLinkage": False,
            "credentials": False,
            "appPersistence": False,
        },
        "policies": {
            "acceptance": {},
            "comparison": {"requiredEvidence": []},
        },
        "protocol": {
            "configured": False,
            "status": "COMPARABLE",
            "issues": [],
        },
        "sessions": sessions,
        "comparison": {
            "newArea": cohort("NEW_AREA", 1),
            "repeatArea": cohort("REPEAT_AREA", 1),
            "deltas": [
                {"key": "sessionDistanceMeters", "repeatMinusNew": 0},
            ],
        },
    }


class FieldTestExportValidatorTest(unittest.TestCase):
    def test_valid_export(self):
        result = validate_export(valid_document())
        self.assertEqual(2, result["sessions"])
        self.assertEqual(1, result["newArea"])
        self.assertEqual(1, result["repeatArea"])
        self.assertIn("REFERENCE_ONLY:2", render_summary(result))

    def test_wrong_schema_version_is_rejected(self):
        document = valid_document()
        document["schemaVersion"] = 2
        with self.assertRaisesRegex(ExportValidationError, "schemaVersion"):
            validate_export(document)

    def test_privacy_flag_change_is_rejected(self):
        document = valid_document()
        document["privacy"]["rawGps"] = True
        with self.assertRaisesRegex(ExportValidationError, "privacy flags"):
            validate_export(document)

    def test_forbidden_location_key_is_rejected_anywhere_outside_privacy(self):
        document = valid_document()
        document["sessions"][0]["metrics"]["latitude"] = 37.5
        with self.assertRaisesRegex(ExportValidationError, "forbidden key"):
            validate_export(document)

    def test_credential_marker_in_string_is_rejected(self):
        document = valid_document()
        document["protocol"]["issues"].append(
            {"key": "example", "detail": "NAVER_MAP_NCP_KEY_ID should never be here"}
        )
        with self.assertRaisesRegex(ExportValidationError, "credential marker"):
            validate_export(document)

    def test_session_ordinals_must_be_contiguous(self):
        document = valid_document()
        document["sessions"][1]["ordinal"] = 3
        with self.assertRaisesRegex(ExportValidationError, "ordinals"):
            validate_export(document)

    def test_comparison_count_must_match_session_rows(self):
        document = valid_document()
        document["comparison"]["newArea"]["sessionCount"] = 2
        with self.assertRaisesRegex(ExportValidationError, "sessionCount mismatch"):
            validate_export(document)

    def test_evidence_count_cannot_exceed_cohort_count(self):
        document = valid_document()
        document["comparison"]["newArea"]["metrics"]["sessionDistanceMeters"]["evidenceCount"] = 2
        with self.assertRaisesRegex(ExportValidationError, "cannot exceed"):
            validate_export(document)

    def test_missing_evidence_average_must_remain_null(self):
        document = valid_document()
        metric_value = document["comparison"]["newArea"]["metrics"]["sessionDistanceMeters"]
        metric_value["evidenceCount"] = 0
        metric_value["average"] = 0
        with self.assertRaisesRegex(ExportValidationError, "must be null"):
            validate_export(document)

    def test_more_than_twenty_sessions_is_rejected(self):
        document = valid_document()
        document["sessions"] = [session(i + 1, "NEW_AREA") for i in range(21)]
        document["comparison"]["newArea"] = cohort("NEW_AREA", 21)
        document["comparison"]["repeatArea"] = cohort("REPEAT_AREA", 0)
        with self.assertRaisesRegex(ExportValidationError, "1..20"):
            validate_export(document)


if __name__ == "__main__":
    unittest.main()
