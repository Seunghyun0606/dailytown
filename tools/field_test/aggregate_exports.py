#!/usr/bin/env python3
"""Aggregate already-exported Daily Town field-test batches locally without persistence."""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from collections import Counter
from pathlib import Path
from typing import Any

from validate_export import METRIC_KEYS, ExportValidationError, _kotlin_round_average, load_document, validate_export


EVIDENCE_TO_METRIC = {
    "SESSION_DURATION": "sessionDurationSeconds",
    "SESSION_DISTANCE": "sessionDistanceMeters",
    "GPS_REJECTION_RATE": "gpsRejectionRatePercent",
    "DISTANCE_ERROR": "distanceErrorPercent",
    "BATTERY_DRAIN": "batteryDrainPercentPerHour",
    "DISCOVERED_ENCOUNTERS": "discoveredEncountersPerSession",
    "ENCOUNTER_RESOLUTION": "encounterResolutionRatePercent",
    "REVISIT_SHARE": "revisitSharePercent",
    "REPEAT_AREA_FATIGUE": "repeatAreaFatigueProxyPercent",
}


class BatchAggregationError(ValueError):
    """Raised when exports cannot be safely combined for one local review batch."""


def _canonical_digest(document: Any) -> str:
    payload = json.dumps(document, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(payload).hexdigest()


def _aggregate_cohort(sessions: list[dict[str, Any]], profile: str) -> dict[str, Any]:
    selected = [session for session in sessions if session["areaProfile"] == profile]
    metrics = {}
    for key in METRIC_KEYS:
        evidence = [session["metrics"][key] for session in selected if session["metrics"][key] is not None]
        metrics[key] = {
            "average": _kotlin_round_average(evidence),
            "evidenceCount": len(evidence),
            "sessionCount": len(selected),
        }
    return {
        "sessionCount": len(selected),
        "trackingPresets": sorted({session["trackingPreset"] for session in selected}),
        "metrics": metrics,
        "acceptance": dict(sorted(Counter(session["acceptanceOverall"] for session in selected).items())),
        "runReview": dict(sorted(Counter((session["runReviewStatus"] or "UNSET") for session in selected).items())),
    }


def _protocol_is_configured(criteria: dict[str, Any]) -> bool:
    return (
        criteria["minimumSessionsPerCohort"] is not None
        or criteria["requireMatchingTrackingPreset"] is True
        or bool(criteria["requiredEvidence"])
    )


def _protocol_issue(key: str, detail: str) -> dict[str, str]:
    return {"key": key, "detail": detail}


def recompute_protocol_assessment(
    policy: dict[str, Any],
    new_area: dict[str, Any],
    repeat_area: dict[str, Any],
    deltas: dict[str, int | None],
) -> dict[str, Any]:
    """Mirror FieldTestProtocolEvaluator for one operator-confirmed offline batch."""
    criteria = policy["comparison"]
    configured = _protocol_is_configured(criteria)

    structural_issues = []
    if new_area["sessionCount"] == 0:
        structural_issues.append(_protocol_issue("newAreaSessions", "missing"))
    if repeat_area["sessionCount"] == 0:
        structural_issues.append(_protocol_issue("repeatAreaSessions", "missing"))
    if (
        new_area["sessionCount"] > 0
        and repeat_area["sessionCount"] > 0
        and all(value is None for value in deltas.values())
    ):
        structural_issues.append(_protocol_issue("sharedEvidence", "missing"))

    if structural_issues:
        return {
            "configured": configured,
            "status": "DATA_INSUFFICIENT",
            "issues": structural_issues,
        }

    if not configured:
        return {
            "configured": False,
            "status": "COMPARABLE",
            "issues": [],
        }

    issues = []
    minimum = criteria["minimumSessionsPerCohort"]
    if minimum is not None:
        if new_area["sessionCount"] < minimum:
            issues.append(_protocol_issue("newAreaMinimumSessions", f"{new_area['sessionCount']}/{minimum}"))
        if repeat_area["sessionCount"] < minimum:
            issues.append(_protocol_issue("repeatAreaMinimumSessions", f"{repeat_area['sessionCount']}/{minimum}"))

    if criteria["requireMatchingTrackingPreset"] is True:
        presets = sorted(set(new_area["trackingPresets"]) | set(repeat_area["trackingPresets"]))
        if len(presets) != 1:
            issues.append(_protocol_issue("trackingPresetConsistency", f"mismatch:{'+'.join(presets)}"))

    required_count = minimum if minimum is not None else 1
    for evidence in sorted(criteria["requiredEvidence"]):
        metric_key = EVIDENCE_TO_METRIC.get(evidence)
        if metric_key is None:
            raise BatchAggregationError(f"unsupported comparison evidence in export policy: {evidence}")
        if evidence != "REPEAT_AREA_FATIGUE":
            new_count = new_area["metrics"][metric_key]["evidenceCount"]
            if new_count < required_count:
                issues.append(_protocol_issue(f"newAreaEvidence.{evidence}", f"{new_count}/{required_count}"))
        repeat_count = repeat_area["metrics"][metric_key]["evidenceCount"]
        if repeat_count < required_count:
            issues.append(_protocol_issue(f"repeatAreaEvidence.{evidence}", f"{repeat_count}/{required_count}"))

    return {
        "configured": True,
        "status": "PRODUCT_REVIEW_READY" if not issues else "COMPARABLE",
        "issues": issues,
    }


def aggregate_documents(documents: list[dict[str, Any]], confirm_non_overlapping: bool = False) -> dict[str, Any]:
    if not documents:
        raise BatchAggregationError("at least one export is required")
    if len(documents) > 1 and not confirm_non_overlapping:
        raise BatchAggregationError(
            "multiple exports require explicit non-overlap confirmation; distinct snapshots can contain the same sessions"
        )

    digests: set[str] = set()
    policy = None
    sessions: list[dict[str, Any]] = []
    versions: set[str] = set()
    source_protocol = Counter()

    for index, document in enumerate(documents, start=1):
        try:
            validate_export(document)
        except ExportValidationError as error:
            raise BatchAggregationError(f"export {index} is invalid: {error}") from error

        digest = _canonical_digest(document)
        if digest in digests:
            raise BatchAggregationError("exact duplicate export detected; refusing to double-count sessions")
        digests.add(digest)

        if policy is None:
            policy = document["policies"]
        elif document["policies"] != policy:
            raise BatchAggregationError("policy mismatch between exports; review policy cohorts separately")

        sessions.extend(document["sessions"])
        versions.add(document["app"]["version"])
        source_protocol[document["protocol"]["status"]] += 1

    new_area = _aggregate_cohort(sessions, "NEW_AREA")
    repeat_area = _aggregate_cohort(sessions, "REPEAT_AREA")
    deltas = {}
    for key in METRIC_KEYS:
        new_average = new_area["metrics"][key]["average"]
        repeat_average = repeat_area["metrics"][key]["average"]
        deltas[key] = repeat_average - new_average if new_average is not None and repeat_average is not None else None

    protocol_assessment = recompute_protocol_assessment(policy, new_area, repeat_area, deltas)

    return {
        "reviewType": "OFFLINE_DERIVED_BATCH_SUMMARY",
        "productVerdict": "NOT_COMPUTED",
        "privacy": "derived_only_no_raw_location_no_identifiers",
        "sourceFileCount": len(documents),
        "sessionCount": len(sessions),
        "appVersions": sorted(versions),
        "sourceProtocolStatus": dict(sorted(source_protocol.items())),
        "protocolAssessment": protocol_assessment,
        "overlap": {
            "exactDuplicatesRejected": True,
            "partialOverlapDetectable": False,
            "nonOverlapConfirmedByOperator": len(documents) == 1 or confirm_non_overlapping,
        },
        "newArea": new_area,
        "repeatArea": repeat_area,
        "repeatMinusNew": deltas,
    }


def render_text(result: dict[str, Any]) -> str:
    protocol = result["protocolAssessment"]
    lines = [
        "Daily Town offline field-test batch review",
        "productVerdict=NOT_COMPUTED",
        f"sourceFiles={result['sourceFileCount']}",
        f"sessions={result['sessionCount']}",
        f"appVersions={','.join(result['appVersions'])}",
        "partialOverlapDetectable=false",
        f"batchProtocolConfigured={str(protocol['configured']).lower()}",
        f"batchProtocolStatus={protocol['status']}",
    ]
    for issue in protocol["issues"]:
        lines.append(f"batchProtocolIssue.{issue['key']}={issue['detail']}")
    for label, key in (("newArea", "newArea"), ("repeatArea", "repeatArea")):
        cohort = result[key]
        lines.append(f"{label}.sessions={cohort['sessionCount']}")
        lines.append(f"{label}.trackingPresets={','.join(cohort['trackingPresets']) or '-'}")
        for metric_key in METRIC_KEYS:
            metric = cohort["metrics"][metric_key]
            average = "null" if metric["average"] is None else str(metric["average"])
            lines.append(f"{label}.{metric_key}.average={average} evidence={metric['evidenceCount']}/{metric['sessionCount']}")
    lines.append("note=protocol readiness mirrors approved comparison policy; product quality verdict is never computed")
    return "\n".join(lines)


def aggregate_files(paths: list[Path], confirm_non_overlapping: bool = False) -> dict[str, Any]:
    documents = []
    for path in paths:
        try:
            documents.append(load_document(path))
        except ExportValidationError as error:
            raise BatchAggregationError(str(error)) from error
    return aggregate_documents(documents, confirm_non_overlapping=confirm_non_overlapping)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Aggregate validated Daily Town field-test exports locally.")
    parser.add_argument("files", nargs="+", type=Path, help="field-test export JSON file(s)")
    parser.add_argument(
        "--confirm-non-overlapping",
        action="store_true",
        help="required for 2+ exports; confirms the operator collected non-overlapping batches",
    )
    parser.add_argument("--json", action="store_true", help="print derived aggregate JSON to stdout instead of text")
    args = parser.parse_args(argv)

    try:
        result = aggregate_files(args.files, confirm_non_overlapping=args.confirm_non_overlapping)
    except BatchAggregationError as error:
        print(f"INVALID BATCH: {error}", file=sys.stderr)
        return 1

    if args.json:
        print(json.dumps(result, ensure_ascii=False, sort_keys=True, separators=(",", ":")))
    else:
        print(render_text(result))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
