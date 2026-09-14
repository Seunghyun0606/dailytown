#!/usr/bin/env python3
"""Plan the next Daily Town field-test evidence collection without inventing product thresholds."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

from aggregate_exports import BatchAggregationError, EVIDENCE_TO_METRIC, aggregate_files
from validate_export import METRIC_KEYS


PLAN_POLICY_NOT_CONFIGURED = "POLICY_NOT_CONFIGURED"
PLAN_STRUCTURAL_EVIDENCE_REQUIRED = "STRUCTURAL_EVIDENCE_REQUIRED"
PLAN_ADDITIONAL_PROTOCOL_EVIDENCE_REQUIRED = "ADDITIONAL_PROTOCOL_EVIDENCE_REQUIRED"
PLAN_NO_ADDITIONAL_PROTOCOL_EVIDENCE = "NO_ADDITIONAL_PROTOCOL_EVIDENCE"


def _issue_keys(result: dict[str, Any]) -> set[str]:
    return {issue["key"] for issue in result["protocolAssessment"]["issues"]}


def _shared_evidence_lower_bounds(result: dict[str, Any]) -> tuple[int, int]:
    """Return the smallest structural session lower bound that could create one shared metric."""
    candidates: list[tuple[int, int]] = []
    for key in METRIC_KEYS:
        new_has = result["newArea"]["metrics"][key]["evidenceCount"] > 0
        repeat_has = result["repeatArea"]["metrics"][key]["evidenceCount"] > 0
        if new_has and repeat_has:
            return 0, 0
        if new_has:
            candidates.append((0, 1))
        elif repeat_has:
            candidates.append((1, 0))
        else:
            candidates.append((1, 1))
    return min(candidates, key=lambda pair: (sum(pair), pair[0], pair[1])) if candidates else (1, 1)


def _cohort_plan(
    result: dict[str, Any],
    result_key: str,
    profile: str,
    policy: dict[str, Any],
    structural_lower_bound: int,
) -> dict[str, Any]:
    cohort = result[result_key]
    minimum = policy["minimumSessionsPerCohort"]
    required_count = minimum if minimum is not None else 1
    session_deficit = max(0, minimum - cohort["sessionCount"]) if minimum is not None else 0

    evidence_plan: dict[str, dict[str, Any]] = {}
    evidence_deficits: list[int] = []
    for evidence in sorted(policy["requiredEvidence"]):
        applicable = not (profile == "NEW_AREA" and evidence == "REPEAT_AREA_FATIGUE")
        metric_key = EVIDENCE_TO_METRIC[evidence]
        current = cohort["metrics"][metric_key]["evidenceCount"]
        deficit = max(0, required_count - current) if applicable else 0
        evidence_plan[evidence] = {
            "metric": metric_key,
            "applicable": applicable,
            "currentEvidence": current,
            "targetEvidence": required_count if applicable else None,
            "deficit": deficit,
        }
        if applicable:
            evidence_deficits.append(deficit)

    lower_bound = max([structural_lower_bound, session_deficit, *evidence_deficits], default=0)
    return {
        "profile": profile,
        "currentSessions": cohort["sessionCount"],
        "minimumSessionTarget": minimum,
        "sessionDeficit": session_deficit,
        "minimumAdditionalSessionsLowerBound": lower_bound,
        "requiredEvidence": evidence_plan,
    }


def build_collection_plan(result: dict[str, Any]) -> dict[str, Any]:
    """Build an evidence-collection plan from an already validated offline aggregate."""
    protocol = result["protocolAssessment"]
    policy = result["comparisonPolicy"]
    issues = _issue_keys(result)

    if protocol["status"] == "PRODUCT_REVIEW_READY":
        plan_status = PLAN_NO_ADDITIONAL_PROTOCOL_EVIDENCE
    elif protocol["configured"]:
        plan_status = PLAN_ADDITIONAL_PROTOCOL_EVIDENCE_REQUIRED
    elif protocol["status"] == "DATA_INSUFFICIENT":
        plan_status = PLAN_STRUCTURAL_EVIDENCE_REQUIRED
    else:
        plan_status = PLAN_POLICY_NOT_CONFIGURED

    new_structural = 1 if "newAreaSessions" in issues else 0
    repeat_structural = 1 if "repeatAreaSessions" in issues else 0
    if "sharedEvidence" in issues:
        shared_new, shared_repeat = _shared_evidence_lower_bounds(result)
        new_structural = max(new_structural, shared_new)
        repeat_structural = max(repeat_structural, shared_repeat)

    new_plan = _cohort_plan(result, "newArea", "NEW_AREA", policy, new_structural)
    repeat_plan = _cohort_plan(result, "repeatArea", "REPEAT_AREA", policy, repeat_structural)

    actions: list[dict[str, Any]] = []
    blockers: list[dict[str, str]] = []

    if not protocol["configured"]:
        actions.append(
            {
                "key": "approveComparisonPolicy",
                "type": "HUMAN_DECISION",
                "detail": "comparison policy is not configured; no product-readiness collection target is invented",
            }
        )

    if "trackingPresetConsistency" in issues:
        presets = sorted(set(result["newArea"]["trackingPresets"]) | set(result["repeatArea"]["trackingPresets"]))
        blockers.append(
            {
                "key": "trackingPresetConsistency",
                "detail": (
                    "current aggregate mixes tracking presets "
                    + ("+".join(presets) if presets else "(none)")
                    + "; adding sessions cannot remove existing mismatch, so review separate same-preset data or collect a clean batch"
                ),
            }
        )

    if "sharedEvidence" in issues:
        actions.append(
            {
                "key": "establishSharedEvidence",
                "type": "STRUCTURAL_COLLECTION",
                "detail": "collect at least one metric with evidence in both NEW_AREA and REPEAT_AREA",
            }
        )

    for label, cohort_plan in (("newArea", new_plan), ("repeatArea", repeat_plan)):
        lower_bound = cohort_plan["minimumAdditionalSessionsLowerBound"]
        if lower_bound > 0:
            actions.append(
                {
                    "key": f"{label}AdditionalSessions",
                    "type": "COLLECT_SESSIONS",
                    "minimumLowerBound": lower_bound,
                    "detail": "lower bound only; one session may satisfy several evidence deficits simultaneously",
                }
            )
        for evidence, evidence_plan in cohort_plan["requiredEvidence"].items():
            if evidence_plan["applicable"] and evidence_plan["deficit"] > 0:
                actions.append(
                    {
                        "key": f"{label}Evidence.{evidence}",
                        "type": "CAPTURE_REQUIRED_EVIDENCE",
                        "deficit": evidence_plan["deficit"],
                        "detail": f"capture {evidence} evidence in {label}",
                    }
                )

    if plan_status == PLAN_NO_ADDITIONAL_PROTOCOL_EVIDENCE:
        actions.append(
            {
                "key": "humanProductReview",
                "type": "HUMAN_REVIEW",
                "detail": "approved evidence protocol is satisfied; product quality/release verdict still requires human review",
            }
        )

    return {
        "planType": "OFFLINE_PROTOCOL_COLLECTION_PLAN",
        "planStatus": plan_status,
        "protocolStatus": protocol["status"],
        "productVerdict": "NOT_COMPUTED",
        "lowerBoundSemantics": "minimum_only_not_guaranteed",
        "comparisonPolicy": policy,
        "newArea": new_plan,
        "repeatArea": repeat_plan,
        "blockers": blockers,
        "actions": actions,
    }


def render_text(plan: dict[str, Any]) -> str:
    lines = [
        "Daily Town field-test collection plan",
        f"planStatus={plan['planStatus']}",
        f"protocolStatus={plan['protocolStatus']}",
        "productVerdict=NOT_COMPUTED",
        "lowerBoundSemantics=minimum_only_not_guaranteed",
    ]
    for key in ("newArea", "repeatArea"):
        cohort = plan[key]
        lines.append(f"{key}.currentSessions={cohort['currentSessions']}")
        target = cohort["minimumSessionTarget"]
        lines.append(f"{key}.minimumSessionTarget={'unset' if target is None else target}")
        lines.append(f"{key}.sessionDeficit={cohort['sessionDeficit']}")
        lines.append(f"{key}.minimumAdditionalSessionsLowerBound={cohort['minimumAdditionalSessionsLowerBound']}")
        for evidence, item in cohort["requiredEvidence"].items():
            if not item["applicable"]:
                lines.append(f"{key}.evidence.{evidence}=not_applicable")
            else:
                lines.append(
                    f"{key}.evidence.{evidence}={item['currentEvidence']}/{item['targetEvidence']} deficit={item['deficit']}"
                )
    for blocker in plan["blockers"]:
        lines.append(f"blocker.{blocker['key']}={blocker['detail']}")
    for action in plan["actions"]:
        suffix = ""
        if "minimumLowerBound" in action:
            suffix += f" minimumLowerBound={action['minimumLowerBound']}"
        if "deficit" in action:
            suffix += f" deficit={action['deficit']}"
        lines.append(f"action.{action['key']}={action['type']}{suffix} detail={action['detail']}")
    lines.append("note=planner uses only approved comparison policy and structural evidence rules; it never invents product thresholds")
    return "\n".join(lines)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Plan the next Daily Town field-test evidence collection locally.")
    parser.add_argument("files", nargs="+", type=Path, help="field-test export JSON file(s)")
    parser.add_argument(
        "--confirm-non-overlapping",
        action="store_true",
        help="required for 2+ exports; confirms the operator collected non-overlapping batches",
    )
    parser.add_argument("--json", action="store_true", help="print the collection plan as JSON")
    args = parser.parse_args(argv)

    try:
        result = aggregate_files(args.files, confirm_non_overlapping=args.confirm_non_overlapping)
        plan = build_collection_plan(result)
    except BatchAggregationError as error:
        print(f"INVALID BATCH: {error}", file=sys.stderr)
        return 1

    if args.json:
        print(json.dumps(plan, ensure_ascii=False, sort_keys=True, separators=(",", ":")))
    else:
        print(render_text(plan))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
