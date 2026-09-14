#!/usr/bin/env python3
"""Run the Daily Town offline field-test review pipeline as one stdout-only command."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

from aggregate_exports import BatchAggregationError, aggregate_files
from collection_plan import build_collection_plan
from review_report import render_csv, render_markdown


REVIEW_PACKET_SCHEMA = "dailytown.field_test_review_packet"
REVIEW_PACKET_SCHEMA_VERSION = 1


def build_review_packet(result: dict[str, Any]) -> dict[str, Any]:
    """Combine validated aggregate evidence and the next-collection plan without adding product judgment."""
    plan = build_collection_plan(result)
    protocol = result["protocolAssessment"]
    return {
        "schema": REVIEW_PACKET_SCHEMA,
        "schemaVersion": REVIEW_PACKET_SCHEMA_VERSION,
        "reviewType": "OFFLINE_DERIVED_FIELD_TEST_REVIEW",
        "productVerdict": "NOT_COMPUTED",
        "evidenceReady": protocol["status"] == "PRODUCT_REVIEW_READY",
        "protocolAssessment": protocol,
        "collectionPlan": plan,
        "aggregate": result,
        "privacy": {
            "derivedOnly": True,
            "rawLocation": False,
            "routeGeometry": False,
            "eventIdentifiers": False,
            "persistentSessionLinkage": False,
            "deviceLinkage": False,
            "credentials": False,
            "automaticPersistence": False,
            "automaticUpload": False,
        },
    }


def render_summary(packet: dict[str, Any]) -> str:
    aggregate = packet["aggregate"]
    protocol = packet["protocolAssessment"]
    plan = packet["collectionPlan"]
    lines = [
        "Daily Town field-test review packet",
        f"schema={packet['schema']}",
        f"schemaVersion={packet['schemaVersion']}",
        f"sourceFiles={aggregate['sourceFileCount']}",
        f"sessions={aggregate['sessionCount']}",
        f"protocolStatus={protocol['status']}",
        f"evidenceReady={str(packet['evidenceReady']).lower()}",
        f"collectionPlanStatus={plan['planStatus']}",
        f"newArea.minimumAdditionalSessionsLowerBound={plan['newArea']['minimumAdditionalSessionsLowerBound']}",
        f"repeatArea.minimumAdditionalSessionsLowerBound={plan['repeatArea']['minimumAdditionalSessionsLowerBound']}",
        f"blockerCount={len(plan['blockers'])}",
        "productVerdict=NOT_COMPUTED",
        "privacy=derived_only_stdout_only_no_automatic_persistence_or_upload",
    ]
    for blocker in plan["blockers"]:
        lines.append(f"blocker.{blocker['key']}={blocker['detail']}")
    lines.append(
        "note=evidenceReady only means the approved comparison protocol is satisfied; product quality and release decisions remain human-owned"
    )
    return "\n".join(lines)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description="Validate, aggregate, plan, and render a Daily Town field-test review in one offline command."
    )
    parser.add_argument("files", nargs="+", type=Path, help="field-test export JSON file(s)")
    parser.add_argument(
        "--confirm-non-overlapping",
        action="store_true",
        help="required for 2+ exports; confirms the operator collected non-overlapping batches",
    )
    parser.add_argument(
        "--format",
        choices=("summary", "json", "markdown", "csv"),
        default="summary",
        help="stdout output format",
    )
    parser.add_argument(
        "--require-evidence-ready",
        action="store_true",
        help="return exit code 2 unless the approved comparison protocol reaches PRODUCT_REVIEW_READY",
    )
    args = parser.parse_args(argv)

    try:
        result = aggregate_files(args.files, confirm_non_overlapping=args.confirm_non_overlapping)
    except BatchAggregationError as error:
        print(f"INVALID BATCH: {error}", file=sys.stderr)
        return 1

    packet = build_review_packet(result)
    if args.format == "json":
        print(json.dumps(packet, ensure_ascii=False, sort_keys=True, separators=(",", ":")))
    elif args.format == "markdown":
        print(render_markdown(result))
    elif args.format == "csv":
        print(render_csv(result), end="")
    else:
        print(render_summary(packet))

    if args.require_evidence_ready and not packet["evidenceReady"]:
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
