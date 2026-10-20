#!/usr/bin/env python3

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from validate_export import ExportValidationError, METRIC_KEYS, render_summary, validate_export


def metric(average, evidence_count, session_count):
    return {"average": average, "evidenceCount": evidence_count, "sessionCount": session_count}


def session(ordinal, profile, distance=10, battery=None, acceptance="NOT_EVALUATED"):
    values = {
        "sessionDurationSeconds": 10,
        "sessionDistanceMeters": distance,
        "gpsRejectionRatePercent": 0,
        "distanceErrorPercent": None,
        "batteryDrainPercentPerHour": battery,
        "discoveredEncountersPerSession": 1,
        "encounterResolutionRatePercent": 100,
        "revisitSharePercent": None,
        "repeatAreaFatigueProxyPercent": None,
    }
    return {
        "ordinal": ordinal,
        "areaProfile": profile,
        "trackingPreset": "BALANCED",
        "runReviewStatus": "REFERENCE_ONLY",
        "mapHealthStatus": "READY",
        "metrics": values,
        "acceptanceOverall": acceptance,
    }


def kotlin_round(values):
    if not values:
        return None
    return int((sum(values) / len(values)) + 0.5)


def cohort(profile, sessions):
    selected = [item for item in sessions if item["areaProfile"] == profile]
    metrics = {}
    for key in METRIC_KEYS:
        evidence = [item["metrics"][key] for item in selected if item["metrics"][key] is not None]
        metrics[key] = metric(kotlin_round(evidence), len(evidence), len(selected))
    return {
        "areaProfile": profile,
        "sessionCount": len(selected),
        "trackingPresets": sorted({item["trackingPreset"] for item in selected}),
        "metrics": metrics,
        "acceptance": {
            "pass": sum(item["acceptanceOverall"] == "PASS" for item in selected),
            "fail": sum(item["acceptanceOverall"] == "FAIL" for item in selected),
            "notEvaluated": sum(item["acceptanceOverall"] == "NOT_EVALUATED" for item in selected),
        },
    }


def comparison(sessions):
    new = cohort("NEW_AREA", sessions)
    repeat = cohort("REPEAT_AREA", sessions)
    deltas = []
    for key in METRIC_KEYS:
        new_average = new["metrics"][key]["average"]
        repeat_average = repeat["metrics"][key]["average"]
        value = repeat_average - new_average if new_average is not None and repeat_average is not None else None
        deltas.append({"key": key, "repeatMinusNew": value})
    return {"newArea": new, "repeatArea": repeat, "deltas": deltas}


def policies():
    return {
        "acceptance": {
            "minimumSessionDurationSeconds": None,
            "maximumGpsRejectionRatePercent": None,
            "requiredMapHealth": None,
            "maximumDistanceErrorPercent": None,
            "maximumBatteryDrainPercentPerHour": None,
            "minimumDiscoveredEncountersPerSession": None,
            "minimumEncounterResolutionRatePercent": None,
            "maximumRepeatAreaFatiguePercent": None,
        },
        "comparison": {
            "minimumSessionsPerCohort": None,
            "requireMatchingTrackingPreset": None,
            "requiredEvidence": [],
        },
    }


def valid_document(sessions=None):
    sessions = sessions or [session(1, "NEW_AREA"), session(2, "REPEAT_AREA")]
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
        "policies": policies(),
        "protocol": {"configured": False, "status": "COMPARABLE", "issues": []},
        "sessions": sessions,
        "comparison": comparison(sessions),
    }


class FieldTestExportValidatorTest(unittest.TestCase):
    def test_valid_export(self):
        result = validate_export(valid_document())
        self.assertEqual(2, result["sessions"])
        self.assertIn("REFERENCE_ONLY:2", render_summary(result))

    def test_wrong_schema_version_is_rejected(self):
        document = valid_document(); document["schemaVersion"] = 2
        with self.assertRaisesRegex(ExportValidationError, "schemaVersion"): validate_export(document)

    def test_privacy_flag_change_is_rejected(self):
        document = valid_document(); document["privacy"]["rawGps"] = True
        with self.assertRaisesRegex(ExportValidationError, "privacy flags"): validate_export(document)

    def test_forbidden_location_key_is_rejected_anywhere_outside_privacy(self):
        document = valid_document(); document["sessions"][0]["metrics"]["latitude"] = 37
        with self.assertRaisesRegex(ExportValidationError, "metrics keys/order|forbidden key"): validate_export(document)

    def test_credential_marker_in_string_is_rejected(self):
        document = valid_document(); document["protocol"]["issues"].append({"key":"x","detail":"NAVER_MAP_NCP_KEY_ID"})
        with self.assertRaisesRegex(ExportValidationError, "credential marker"): validate_export(document)

    def test_session_ordinals_must_be_contiguous(self):
        document = valid_document(); document["sessions"][1]["ordinal"] = 3
        with self.assertRaisesRegex(ExportValidationError, "ordinals"): validate_export(document)

    def test_comparison_count_must_match_session_rows(self):
        document = valid_document(); document["comparison"]["newArea"]["sessionCount"] = 2
        with self.assertRaisesRegex(ExportValidationError, "sessionCount mismatch"): validate_export(document)

    def test_missing_evidence_average_must_remain_null(self):
        document = valid_document(); value = document["comparison"]["newArea"]["metrics"]["batteryDrainPercentPerHour"]
        value["average"] = 0
        with self.assertRaisesRegex(ExportValidationError, "must be null|does not match"): validate_export(document)

    def test_more_than_twenty_sessions_is_rejected(self):
        sessions = [session(i + 1, "NEW_AREA") for i in range(21)]
        document = valid_document(sessions)
        with self.assertRaisesRegex(ExportValidationError, "1..20"): validate_export(document)

    def test_cohort_average_must_match_session_rows(self):
        sessions = [session(1,"NEW_AREA",distance=10), session(2,"NEW_AREA",distance=11), session(3,"REPEAT_AREA")]
        document = valid_document(sessions)
        self.assertEqual(11, document["comparison"]["newArea"]["metrics"]["sessionDistanceMeters"]["average"])
        document["comparison"]["newArea"]["metrics"]["sessionDistanceMeters"]["average"] = 10
        with self.assertRaisesRegex(ExportValidationError, "does not match session rows"): validate_export(document)

    def test_tracking_presets_must_match_session_rows(self):
        document = valid_document(); document["comparison"]["newArea"]["trackingPresets"] = ["HIGH_ACCURACY"]
        with self.assertRaisesRegex(ExportValidationError, "trackingPresets"): validate_export(document)

    def test_acceptance_counts_must_match_session_rows(self):
        document = valid_document(); document["comparison"]["newArea"]["acceptance"] = {"pass":1,"fail":0,"notEvaluated":0}
        with self.assertRaisesRegex(ExportValidationError, "acceptance counts"): validate_export(document)

    def test_delta_must_match_recomputed_cohort_averages(self):
        document = valid_document(); document["comparison"]["deltas"][1]["repeatMinusNew"] = 99
        with self.assertRaisesRegex(ExportValidationError, "repeatMinusNew"): validate_export(document)


if __name__ == "__main__": unittest.main()
