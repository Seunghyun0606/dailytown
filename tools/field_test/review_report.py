#!/usr/bin/env python3
"""Render a human-reviewable Daily Town field-test batch report to stdout."""

from __future__ import annotations

import argparse
import csv
import io
import sys
from pathlib import Path
from typing import Any

from aggregate_exports import BatchAggregationError, aggregate_files
from collection_plan import build_collection_plan
from validate_export import METRIC_KEYS


def _display(value: Any) -> str:
    if value is None:
        return "null"
    if isinstance(value, bool):
        return str(value).lower()
    return str(value)


def _evidence_display(item: dict[str, Any]) -> str:
    if not item["applicable"]:
        return "n/a"
    return f"{item['currentEvidence']}/{item['targetEvidence']}"


def render_markdown(result: dict[str, Any]) -> str:
    protocol = result["protocolAssessment"]
    plan = build_collection_plan(result)
    lines = [
        "# Daily Town field-test batch review",
        "",
        "> Derived offline review only. Product verdict is **NOT_COMPUTED**.",
        "",
        "## Summary",
        "",
        "| Item | Value |",
        "| --- | --- |",
        f"| Source files | {result['sourceFileCount']} |",
        f"| Sessions | {result['sessionCount']} |",
        f"| App versions | {', '.join(result['appVersions'])} |",
        f"| Batch protocol | {protocol['status']} |",
        f"| Protocol configured | {_display(protocol['configured'])} |",
        f"| Collection plan | {plan['planStatus']} |",
        "| Partial overlap detectable | false |",
        "",
    ]
    if protocol["issues"]:
        lines.extend(["## Protocol issues", ""])
        for issue in protocol["issues"]:
            lines.append(f"- `{issue['key']}`: {issue['detail']}")
        lines.append("")

    lines.extend([
        "## Next collection plan",
        "",
        "> Additional-session values are lower bounds only; one session can satisfy several evidence deficits at once.",
        "",
        "| Item | NEW_AREA | REPEAT_AREA |",
        "| --- | ---: | ---: |",
        f"| Current sessions | {plan['newArea']['currentSessions']} | {plan['repeatArea']['currentSessions']} |",
        f"| Minimum session target | {_display(plan['newArea']['minimumSessionTarget'])} | {_display(plan['repeatArea']['minimumSessionTarget'])} |",
        f"| Session deficit | {plan['newArea']['sessionDeficit']} | {plan['repeatArea']['sessionDeficit']} |",
        f"| Minimum additional sessions (lower bound) | {plan['newArea']['minimumAdditionalSessionsLowerBound']} | {plan['repeatArea']['minimumAdditionalSessionsLowerBound']} |",
        "",
    ])
    evidence_keys = sorted(set(plan["newArea"]["requiredEvidence"]) | set(plan["repeatArea"]["requiredEvidence"]))
    if evidence_keys:
        lines.extend([
            "| Required evidence | NEW current/target | NEW deficit | REPEAT current/target | REPEAT deficit |",
            "| --- | ---: | ---: | ---: | ---: |",
        ])
        for evidence in evidence_keys:
            new_item = plan["newArea"]["requiredEvidence"][evidence]
            repeat_item = plan["repeatArea"]["requiredEvidence"][evidence]
            lines.append(
                f"| `{evidence}` | {_evidence_display(new_item)} | {new_item['deficit']} | "
                f"{_evidence_display(repeat_item)} | {repeat_item['deficit']} |"
            )
        lines.append("")

    if plan["blockers"]:
        lines.extend(["### Collection blockers", ""])
        for blocker in plan["blockers"]:
            lines.append(f"- `{blocker['key']}`: {blocker['detail']}")
        lines.append("")

    if plan["actions"]:
        lines.extend(["### Next actions", ""])
        for action in plan["actions"]:
            qualifiers = []
            if "minimumLowerBound" in action:
                qualifiers.append(f"minimum lower bound {action['minimumLowerBound']}")
            if "deficit" in action:
                qualifiers.append(f"deficit {action['deficit']}")
            suffix = f" ({', '.join(qualifiers)})" if qualifiers else ""
            lines.append(f"- `{action['type']}` `{action['key']}`{suffix}: {action['detail']}")
        lines.append("")

    lines.extend([
        "## Cohort metrics",
        "",
        "| Metric | NEW avg | NEW evidence | REPEAT avg | REPEAT evidence | Repeat − New |",
        "| --- | ---: | ---: | ---: | ---: | ---: |",
    ])
    for key in METRIC_KEYS:
        new_metric = result["newArea"]["metrics"][key]
        repeat_metric = result["repeatArea"]["metrics"][key]
        lines.append(
            f"| `{key}` | {_display(new_metric['average'])} | "
            f"{new_metric['evidenceCount']}/{new_metric['sessionCount']} | "
            f"{_display(repeat_metric['average'])} | "
            f"{repeat_metric['evidenceCount']}/{repeat_metric['sessionCount']} | "
            f"{_display(result['repeatMinusNew'][key])} |"
        )

    lines.extend(["", "## Review counts", "", "| State | NEW_AREA | REPEAT_AREA |", "| --- | ---: | ---: |"])
    states = sorted(
        set(result["newArea"]["runReview"])
        | set(result["repeatArea"]["runReview"])
        | {"REFERENCE_ONLY", "REVIEWABLE", "NEEDS_ATTENTION", "UNSET"}
    )
    for state in states:
        lines.append(
            f"| Run `{state}` | {result['newArea']['runReview'].get(state, 0)} | "
            f"{result['repeatArea']['runReview'].get(state, 0)} |"
        )
    for state in ("PASS", "FAIL", "NOT_EVALUATED"):
        lines.append(
            f"| Acceptance `{state}` | {result['newArea']['acceptance'].get(state, 0)} | "
            f"{result['repeatArea']['acceptance'].get(state, 0)} |"
        )

    lines.extend([
        "",
        "## Interpretation boundary",
        "",
        "- `PRODUCT_REVIEW_READY` means the human-approved comparison protocol has enough evidence.",
        "- It does **not** mean the product experience is good or ready to release.",
        "- Collection-plan session counts are lower bounds, not guarantees that the next sessions will capture every required evidence item.",
        "- If matching tracking preset is required and the current aggregate mixes presets, adding sessions cannot remove that existing mismatch.",
        "- Missing evidence remains `null`; evidence counts should be reviewed alongside averages.",
        "- Different snapshots can partially overlap because durable session IDs/timestamps are intentionally absent.",
        "- Product verdict remains `NOT_COMPUTED`.",
    ])
    return "\n".join(lines)


def render_csv(result: dict[str, Any]) -> str:
    output = io.StringIO()
    writer = csv.writer(output, lineterminator="\n")
    writer.writerow(
        [
            "rowType",
            "key",
            "newArea",
            "repeatArea",
            "repeatMinusNew",
            "newEvidence",
            "repeatEvidence",
            "detail",
        ]
    )
    protocol = result["protocolAssessment"]
    plan = build_collection_plan(result)
    writer.writerow(["SUMMARY", "sourceFiles", result["sourceFileCount"], "", "", "", "", ""])
    writer.writerow(["SUMMARY", "sessions", result["sessionCount"], "", "", "", "", ""])
    writer.writerow(["SUMMARY", "appVersions", ",".join(result["appVersions"]), "", "", "", "", ""])
    writer.writerow(["SUMMARY", "protocolStatus", protocol["status"], "", "", "", "", ""])
    writer.writerow(["SUMMARY", "collectionPlanStatus", plan["planStatus"], "", "", "", "", ""])
    writer.writerow(["SUMMARY", "productVerdict", "NOT_COMPUTED", "", "", "", "", ""])
    for issue in protocol["issues"]:
        writer.writerow(["PROTOCOL_ISSUE", issue["key"], "", "", "", "", "", issue["detail"]])

    writer.writerow(
        [
            "COLLECTION_PLAN",
            "minimumAdditionalSessionsLowerBound",
            plan["newArea"]["minimumAdditionalSessionsLowerBound"],
            plan["repeatArea"]["minimumAdditionalSessionsLowerBound"],
            "",
            "",
            "",
            "minimum only; one session may satisfy several evidence deficits",
        ]
    )
    evidence_keys = sorted(set(plan["newArea"]["requiredEvidence"]) | set(plan["repeatArea"]["requiredEvidence"]))
    for evidence in evidence_keys:
        new_item = plan["newArea"]["requiredEvidence"][evidence]
        repeat_item = plan["repeatArea"]["requiredEvidence"][evidence]
        writer.writerow(
            [
                "COLLECTION_EVIDENCE",
                evidence,
                _evidence_display(new_item),
                _evidence_display(repeat_item),
                "",
                new_item["deficit"],
                repeat_item["deficit"],
                new_item["metric"],
            ]
        )
    for blocker in plan["blockers"]:
        writer.writerow(["COLLECTION_BLOCKER", blocker["key"], "", "", "", "", "", blocker["detail"]])
    for action in plan["actions"]:
        writer.writerow(["COLLECTION_ACTION", action["key"], action["type"], "", "", "", "", action["detail"]])

    for key in METRIC_KEYS:
        new_metric = result["newArea"]["metrics"][key]
        repeat_metric = result["repeatArea"]["metrics"][key]
        writer.writerow(
            [
                "METRIC",
                key,
                "" if new_metric["average"] is None else new_metric["average"],
                "" if repeat_metric["average"] is None else repeat_metric["average"],
                "" if result["repeatMinusNew"][key] is None else result["repeatMinusNew"][key],
                f"{new_metric['evidenceCount']}/{new_metric['sessionCount']}",
                f"{repeat_metric['evidenceCount']}/{repeat_metric['sessionCount']}",
                "",
            ]
        )

    for state in sorted(set(result["newArea"]["runReview"]) | set(result["repeatArea"]["runReview"])):
        writer.writerow(
            [
                "RUN_REVIEW",
                state,
                result["newArea"]["runReview"].get(state, 0),
                result["repeatArea"]["runReview"].get(state, 0),
                "",
                "",
                "",
                "",
            ]
        )
    for state in ("PASS", "FAIL", "NOT_EVALUATED"):
        writer.writerow(
            [
                "ACCEPTANCE",
                state,
                result["newArea"]["acceptance"].get(state, 0),
                result["repeatArea"]["acceptance"].get(state, 0),
                "",
                "",
                "",
                "",
            ]
        )
    return output.getvalue()


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Render an offline Daily Town field-test review report.")
    parser.add_argument("files", nargs="+", type=Path, help="field-test export JSON file(s)")
    parser.add_argument(
        "--confirm-non-overlapping",
        action="store_true",
        help="required for 2+ exports; confirms the operator collected non-overlapping batches",
    )
    parser.add_argument("--format", choices=("markdown", "csv"), default="markdown")
    args = parser.parse_args(argv)

    try:
        result = aggregate_files(args.files, confirm_non_overlapping=args.confirm_non_overlapping)
    except BatchAggregationError as error:
        print(f"INVALID BATCH: {error}", file=sys.stderr)
        return 1

    print(render_markdown(result) if args.format == "markdown" else render_csv(result), end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
