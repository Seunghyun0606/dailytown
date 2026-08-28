#!/usr/bin/env python3
"""Validate Daily Town field-test structured export v1 without network access."""

from __future__ import annotations

import argparse
import json
import math
import sys
from collections import Counter
from pathlib import Path
from typing import Any

SCHEMA_NAME = "dailytown.field_test_export"
SCHEMA_VERSION = 1
PACKAGE_ID = "com.dailytown.app"
MAX_SESSIONS = 20

METRIC_KEYS = (
    "sessionDurationSeconds",
    "sessionDistanceMeters",
    "gpsRejectionRatePercent",
    "distanceErrorPercent",
    "batteryDrainPercentPerHour",
    "discoveredEncountersPerSession",
    "encounterResolutionRatePercent",
    "revisitSharePercent",
    "repeatAreaFatigueProxyPercent",
)

EXPECTED_PRIVACY_FLAGS = {
    "rawGps": False,
    "routeGeometry": False,
    "placeLabels": False,
    "eventIdentifiers": False,
    "sessionLinkage": False,
    "deviceLinkage": False,
    "credentials": False,
    "appPersistence": False,
}

FORBIDDEN_KEYS = {
    "latitude",
    "longitude",
    "coordinates",
    "routepoints",
    "routepath",
    "placename",
    "routelabel",
    "poiid",
    "encounterid",
    "templateid",
    "rawevents",
    "sessiontoken",
    "sessionid",
    "deviceid",
    "generatedat",
    "timestamp",
    "credential",
    "navermapncpkeyid",
}

FORBIDDEN_STRING_MARKERS = {"NAVER_MAP_NCP_KEY_ID"}

ALLOWED_AREA_PROFILES = {"NEW_AREA", "REPEAT_AREA"}
ALLOWED_RUN_REVIEW = {"REFERENCE_ONLY", "REVIEWABLE", "NEEDS_ATTENTION"}
ALLOWED_PROTOCOL_STATUS = {"DATA_INSUFFICIENT", "COMPARABLE", "PRODUCT_REVIEW_READY"}
ALLOWED_ACCEPTANCE = {"PASS", "FAIL", "NOT_EVALUATED"}


class ExportValidationError(ValueError):
    """Raised when a structured field-test export violates schema/safety invariants."""


def _require(condition: bool, message: str) -> None:
    if not condition:
        raise ExportValidationError(message)


def _normalized_key(value: str) -> str:
    return "".join(char for char in value.lower() if char.isalnum())


def _scan_forbidden_content(value: Any, path: str = "$") -> None:
    if isinstance(value, dict):
        for key, child in value.items():
            key_text = str(key)
            normalized = _normalized_key(key_text)
            if path != "$.privacy" and normalized in FORBIDDEN_KEYS:
                raise ExportValidationError(f"forbidden key at {path}.{key_text}")
            _scan_forbidden_content(child, f"{path}.{key_text}")
    elif isinstance(value, list):
        for index, child in enumerate(value):
            _scan_forbidden_content(child, f"{path}[{index}]")
    elif isinstance(value, str):
        for marker in FORBIDDEN_STRING_MARKERS:
            if marker in value:
                raise ExportValidationError(f"forbidden credential marker at {path}")


def _kotlin_round_average(values: list[int]) -> int | None:
    """Match Kotlin Double.average().roundToInt() for non-negative field-test metrics."""
    if not values:
        return None
    return int(math.floor((sum(values) / len(values)) + 0.5))


def _validate_metric(metric: Any, cohort_count: int, path: str) -> None:
    _require(isinstance(metric, dict), f"{path} must be an object")
    _require(set(metric) == {"average", "evidenceCount", "sessionCount"}, f"{path} fields must match schema v1")
    evidence_count = metric["evidenceCount"]
    session_count = metric["sessionCount"]
    average = metric["average"]
    _require(isinstance(evidence_count, int) and not isinstance(evidence_count, bool) and evidence_count >= 0, f"{path}.evidenceCount must be >= 0")
    _require(session_count == cohort_count, f"{path}.sessionCount must equal cohort sessionCount")
    _require(evidence_count <= session_count, f"{path}.evidenceCount cannot exceed sessionCount")
    if evidence_count == 0:
        _require(average is None, f"{path}.average must be null when evidenceCount is 0")
    else:
        _require(isinstance(average, int) and not isinstance(average, bool), f"{path}.average must be an integer when evidence exists")


def _expected_cohort_from_sessions(sessions: list[dict[str, Any]], profile: str) -> dict[str, Any]:
    cohort_sessions = [session for session in sessions if session["areaProfile"] == profile]
    metrics: dict[str, dict[str, Any]] = {}
    for key in METRIC_KEYS:
        evidence = [session["metrics"][key] for session in cohort_sessions if session["metrics"][key] is not None]
        metrics[key] = {
            "average": _kotlin_round_average(evidence),
            "evidenceCount": len(evidence),
            "sessionCount": len(cohort_sessions),
        }
    return {
        "areaProfile": profile,
        "sessionCount": len(cohort_sessions),
        "trackingPresets": sorted({session["trackingPreset"] for session in cohort_sessions if session["trackingPreset"]}),
        "metrics": metrics,
        "acceptance": {
            "pass": sum(session["acceptanceOverall"] == "PASS" for session in cohort_sessions),
            "fail": sum(session["acceptanceOverall"] == "FAIL" for session in cohort_sessions),
            "notEvaluated": sum(session["acceptanceOverall"] == "NOT_EVALUATED" for session in cohort_sessions),
        },
    }


def _validate_cohort(cohort: Any, expected: dict[str, Any], path: str) -> None:
    _require(isinstance(cohort, dict), f"{path} must be an object")
    _require(cohort.get("areaProfile") == expected["areaProfile"], f"{path}.areaProfile mismatch")
    _require(cohort.get("sessionCount") == expected["sessionCount"], f"{path}.sessionCount mismatch")
    presets = cohort.get("trackingPresets")
    _require(isinstance(presets, list), f"{path}.trackingPresets must be an array")
    _require(presets == expected["trackingPresets"], f"{path}.trackingPresets must match session rows")

    metrics = cohort.get("metrics")
    _require(isinstance(metrics, dict), f"{path}.metrics must be an object")
    _require(tuple(metrics.keys()) == METRIC_KEYS, f"{path}.metrics keys/order must match schema v1")
    for key in METRIC_KEYS:
        metric = metrics[key]
        _validate_metric(metric, expected["sessionCount"], f"{path}.metrics.{key}")
        _require(metric == expected["metrics"][key], f"{path}.metrics.{key} does not match session rows")

    acceptance = cohort.get("acceptance")
    _require(isinstance(acceptance, dict), f"{path}.acceptance must be an object")
    _require(set(acceptance) == {"pass", "fail", "notEvaluated"}, f"{path}.acceptance fields must match schema v1")
    for key in ("pass", "fail", "notEvaluated"):
        count = acceptance.get(key)
        _require(isinstance(count, int) and not isinstance(count, bool) and count >= 0, f"{path}.acceptance.{key} must be >= 0")
    _require(acceptance == expected["acceptance"], f"{path}.acceptance counts must match session rows")


def _validate_policy_shape(policies: Any) -> None:
    _require(isinstance(policies, dict), "policies must be an object")
    _require(set(policies) == {"acceptance", "comparison"}, "policies fields must match schema v1")
    acceptance = policies["acceptance"]
    comparison = policies["comparison"]
    _require(isinstance(acceptance, dict), "policies.acceptance must be an object")
    _require(isinstance(comparison, dict), "policies.comparison must be an object")
    _require(
        set(acceptance)
        == {
            "minimumSessionDurationSeconds",
            "maximumGpsRejectionRatePercent",
            "requiredMapHealth",
            "maximumDistanceErrorPercent",
            "maximumBatteryDrainPercentPerHour",
            "minimumDiscoveredEncountersPerSession",
            "minimumEncounterResolutionRatePercent",
            "maximumRepeatAreaFatiguePercent",
        },
        "policies.acceptance fields must match schema v1",
    )
    _require(
        set(comparison) == {"minimumSessionsPerCohort", "requireMatchingTrackingPreset", "requiredEvidence"},
        "policies.comparison fields must match schema v1",
    )
    _require(isinstance(comparison["requiredEvidence"], list), "policies.comparison.requiredEvidence must be an array")


def validate_export(document: Any) -> dict[str, Any]:
    _require(isinstance(document, dict), "root must be a JSON object")
    _require(document.get("schema") == SCHEMA_NAME, f"schema must be {SCHEMA_NAME}")
    _require(document.get("schemaVersion") == SCHEMA_VERSION, f"schemaVersion must be {SCHEMA_VERSION}")

    app = document.get("app")
    _require(isinstance(app, dict), "app must be an object")
    _require(app.get("packageId") == PACKAGE_ID, f"app.packageId must be {PACKAGE_ID}")
    _require(isinstance(app.get("version"), str) and app["version"].strip(), "app.version must be non-empty")

    privacy = document.get("privacy")
    _require(privacy == EXPECTED_PRIVACY_FLAGS, "privacy flags must exactly match the v1 safe boundary")

    _validate_policy_shape(document.get("policies"))

    protocol = document.get("protocol")
    _require(isinstance(protocol, dict), "protocol must be an object")
    _require(isinstance(protocol.get("configured"), bool), "protocol.configured must be boolean")
    _require(protocol.get("status") in ALLOWED_PROTOCOL_STATUS, "unsupported protocol.status")
    _require(isinstance(protocol.get("issues"), list), "protocol.issues must be an array")

    sessions = document.get("sessions")
    _require(isinstance(sessions, list), "sessions must be an array")
    _require(1 <= len(sessions) <= MAX_SESSIONS, f"sessions must contain 1..{MAX_SESSIONS} rows")

    area_counts: Counter[str] = Counter()
    review_counts: Counter[str] = Counter()
    expected_ordinals = list(range(1, len(sessions) + 1))
    actual_ordinals: list[int] = []

    for index, session in enumerate(sessions):
        path = f"$.sessions[{index}]"
        _require(isinstance(session, dict), f"{path} must be an object")
        _require(
            set(session)
            == {"ordinal", "areaProfile", "trackingPreset", "runReviewStatus", "mapHealthStatus", "metrics", "acceptanceOverall"},
            f"{path} fields must match schema v1",
        )
        ordinal = session.get("ordinal")
        _require(isinstance(ordinal, int) and not isinstance(ordinal, bool), f"{path}.ordinal must be integer")
        actual_ordinals.append(ordinal)

        profile = session.get("areaProfile")
        _require(profile in ALLOWED_AREA_PROFILES, f"{path}.areaProfile is invalid")
        area_counts[profile] += 1

        review = session.get("runReviewStatus")
        _require(review is None or review in ALLOWED_RUN_REVIEW, f"{path}.runReviewStatus is invalid")
        review_counts[review or "UNSET"] += 1

        _require(isinstance(session.get("trackingPreset"), str) and session["trackingPreset"], f"{path}.trackingPreset required")
        map_health = session.get("mapHealthStatus")
        _require(map_health is None or isinstance(map_health, str), f"{path}.mapHealthStatus must be string or null")
        metrics = session.get("metrics")
        _require(isinstance(metrics, dict), f"{path}.metrics must be an object")
        _require(tuple(metrics.keys()) == METRIC_KEYS, f"{path}.metrics keys/order must match schema v1")
        for key in METRIC_KEYS:
            value = metrics[key]
            _require(value is None or (isinstance(value, int) and not isinstance(value, bool)), f"{path}.metrics.{key} must be integer or null")
        _require(session.get("acceptanceOverall") in ALLOWED_ACCEPTANCE, f"{path}.acceptanceOverall is invalid")

    _require(actual_ordinals == expected_ordinals, "session ordinals must be contiguous and start at 1")

    comparison = document.get("comparison")
    _require(isinstance(comparison, dict), "comparison must be an object")
    _require(set(comparison) == {"newArea", "repeatArea", "deltas"}, "comparison fields must match schema v1")

    expected_new = _expected_cohort_from_sessions(sessions, "NEW_AREA")
    expected_repeat = _expected_cohort_from_sessions(sessions, "REPEAT_AREA")
    _validate_cohort(comparison.get("newArea"), expected_new, "$.comparison.newArea")
    _validate_cohort(comparison.get("repeatArea"), expected_repeat, "$.comparison.repeatArea")

    deltas = comparison.get("deltas")
    _require(isinstance(deltas, list), "$.comparison.deltas must be an array")
    _require(len(deltas) == len(METRIC_KEYS), "$.comparison.deltas must contain all v1 metrics")
    for index, key in enumerate(METRIC_KEYS):
        path = f"$.comparison.deltas[{index}]"
        delta = deltas[index]
        _require(isinstance(delta, dict), f"{path} must be an object")
        _require(set(delta) == {"key", "repeatMinusNew"}, f"{path} fields must match schema v1")
        _require(delta.get("key") == key, f"{path}.key must be {key}")
        new_average = expected_new["metrics"][key]["average"]
        repeat_average = expected_repeat["metrics"][key]["average"]
        expected_delta = repeat_average - new_average if new_average is not None and repeat_average is not None else None
        _require(delta.get("repeatMinusNew") == expected_delta, f"{path}.repeatMinusNew does not match cohort averages")

    _scan_forbidden_content(document)

    return {
        "sessions": len(sessions),
        "newArea": area_counts["NEW_AREA"],
        "repeatArea": area_counts["REPEAT_AREA"],
        "protocolStatus": protocol["status"],
        "runReview": dict(sorted(review_counts.items())),
    }


def render_summary(result: dict[str, Any]) -> str:
    reviews = ",".join(f"{key}:{value}" for key, value in result["runReview"].items())
    return (
        f"VALID {SCHEMA_NAME} v{SCHEMA_VERSION} "
        f"sessions={result['sessions']} new={result['newArea']} repeat={result['repeatArea']} "
        f"protocol={result['protocolStatus']} review={reviews}"
    )


def load_document(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, UnicodeError, json.JSONDecodeError) as error:
        raise ExportValidationError(f"{path}: cannot read valid UTF-8 JSON: {error}") from error


def validate_file(path: Path) -> str:
    result = validate_export(load_document(path))
    return render_summary(result)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Validate Daily Town field-test export v1 locally.")
    parser.add_argument("files", nargs="+", type=Path, help="JSON export file(s) to validate")
    args = parser.parse_args(argv)

    failed = False
    for path in args.files:
        try:
            print(f"{path}: {validate_file(path)}")
        except ExportValidationError as error:
            failed = True
            print(f"{path}: INVALID: {error}", file=sys.stderr)
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
