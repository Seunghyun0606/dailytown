#!/usr/bin/env python3

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from aggregate_exports import aggregate_documents
from collection_plan import (
    PLAN_ADDITIONAL_PROTOCOL_EVIDENCE_REQUIRED,
    PLAN_NO_ADDITIONAL_PROTOCOL_EVIDENCE,
    PLAN_POLICY_NOT_CONFIGURED,
    PLAN_STRUCTURAL_EVIDENCE_REQUIRED,
    build_collection_plan,
    render_text,
)
from test_validate_export import session, valid_document


def with_policy(document, minimum=None, matching=None, evidence=None):
    document["policies"]["comparison"] = {
        "minimumSessionsPerCohort": minimum,
        "requireMatchingTrackingPreset": matching,
        "requiredEvidence": evidence or [],
    }
    return document


class FieldTestCollectionPlannerTest(unittest.TestCase):
    def test_comparable_without_policy_requires_human_policy_decision_not_invented_target(self):
        plan = build_collection_plan(aggregate_documents([valid_document()]))
        self.assertEqual(PLAN_POLICY_NOT_CONFIGURED, plan["planStatus"])
        self.assertEqual(0, plan["newArea"]["minimumAdditionalSessionsLowerBound"])
        self.assertTrue(any(action["key"] == "approveComparisonPolicy" for action in plan["actions"]))
        self.assertEqual("NOT_COMPUTED", plan["productVerdict"])

    def test_missing_cohort_has_one_session_structural_lower_bound_without_policy(self):
        document = valid_document([session(1, "NEW_AREA")])
        plan = build_collection_plan(aggregate_documents([document]))
        self.assertEqual(PLAN_STRUCTURAL_EVIDENCE_REQUIRED, plan["planStatus"])
        self.assertEqual(0, plan["newArea"]["minimumAdditionalSessionsLowerBound"])
        self.assertEqual(1, plan["repeatArea"]["minimumAdditionalSessionsLowerBound"])

    def test_minimum_session_policy_reports_exact_cohort_deficits_and_lower_bounds(self):
        document = with_policy(valid_document(), minimum=3)
        plan = build_collection_plan(aggregate_documents([document]))
        self.assertEqual(PLAN_ADDITIONAL_PROTOCOL_EVIDENCE_REQUIRED, plan["planStatus"])
        self.assertEqual(2, plan["newArea"]["sessionDeficit"])
        self.assertEqual(2, plan["repeatArea"]["sessionDeficit"])
        self.assertEqual(2, plan["newArea"]["minimumAdditionalSessionsLowerBound"])
        self.assertEqual(2, plan["repeatArea"]["minimumAdditionalSessionsLowerBound"])

    def test_required_evidence_deficit_can_raise_lower_bound_above_session_deficit(self):
        sessions = [
            session(1, "NEW_AREA", battery=10),
            session(2, "NEW_AREA", battery=None),
            session(3, "REPEAT_AREA", battery=None),
            session(4, "REPEAT_AREA", battery=None),
        ]
        document = with_policy(valid_document(sessions), minimum=2, evidence=["BATTERY_DRAIN"])
        plan = build_collection_plan(aggregate_documents([document]))
        self.assertEqual(0, plan["newArea"]["sessionDeficit"])
        self.assertEqual(1, plan["newArea"]["requiredEvidence"]["BATTERY_DRAIN"]["deficit"])
        self.assertEqual(2, plan["repeatArea"]["requiredEvidence"]["BATTERY_DRAIN"]["deficit"])
        self.assertEqual(1, plan["newArea"]["minimumAdditionalSessionsLowerBound"])
        self.assertEqual(2, plan["repeatArea"]["minimumAdditionalSessionsLowerBound"])

    def test_repeat_area_fatigue_target_never_creates_new_area_deficit(self):
        sessions = [session(1, "NEW_AREA"), session(2, "REPEAT_AREA"), session(3, "REPEAT_AREA")]
        sessions[1]["metrics"]["repeatAreaFatigueProxyPercent"] = 40
        document = with_policy(valid_document(sessions), minimum=2, evidence=["REPEAT_AREA_FATIGUE"])
        plan = build_collection_plan(aggregate_documents([document]))
        new_item = plan["newArea"]["requiredEvidence"]["REPEAT_AREA_FATIGUE"]
        repeat_item = plan["repeatArea"]["requiredEvidence"]["REPEAT_AREA_FATIGUE"]
        self.assertFalse(new_item["applicable"])
        self.assertIsNone(new_item["targetEvidence"])
        self.assertEqual(0, new_item["deficit"])
        self.assertEqual(1, repeat_item["deficit"])

    def test_matching_preset_mismatch_is_blocker_not_fake_numeric_fix(self):
        sessions = [session(1, "NEW_AREA"), session(2, "REPEAT_AREA")]
        sessions[1]["trackingPreset"] = "HIGH_ACCURACY"
        document = with_policy(valid_document(sessions), minimum=1, matching=True)
        plan = build_collection_plan(aggregate_documents([document]))
        self.assertEqual(PLAN_ADDITIONAL_PROTOCOL_EVIDENCE_REQUIRED, plan["planStatus"])
        self.assertEqual(0, plan["newArea"]["minimumAdditionalSessionsLowerBound"])
        self.assertEqual(0, plan["repeatArea"]["minimumAdditionalSessionsLowerBound"])
        blocker = next(item for item in plan["blockers"] if item["key"] == "trackingPresetConsistency")
        self.assertIn("adding sessions cannot remove existing mismatch", blocker["detail"])

    def test_product_review_ready_has_no_additional_protocol_evidence_but_still_human_review(self):
        document = with_policy(
            valid_document(),
            minimum=1,
            matching=True,
            evidence=["SESSION_DISTANCE"],
        )
        plan = build_collection_plan(aggregate_documents([document]))
        self.assertEqual(PLAN_NO_ADDITIONAL_PROTOCOL_EVIDENCE, plan["planStatus"])
        self.assertEqual("PRODUCT_REVIEW_READY", plan["protocolStatus"])
        self.assertEqual("NOT_COMPUTED", plan["productVerdict"])
        self.assertTrue(any(action["key"] == "humanProductReview" for action in plan["actions"]))

    def test_shared_evidence_gap_yields_structural_collection_lower_bound(self):
        sessions = [session(1, "NEW_AREA"), session(2, "REPEAT_AREA")]
        for item in sessions:
            for key in item["metrics"]:
                item["metrics"][key] = None
        document = valid_document(sessions)
        plan = build_collection_plan(aggregate_documents([document]))
        self.assertEqual(PLAN_STRUCTURAL_EVIDENCE_REQUIRED, plan["planStatus"])
        self.assertEqual(1, plan["newArea"]["minimumAdditionalSessionsLowerBound"])
        self.assertEqual(1, plan["repeatArea"]["minimumAdditionalSessionsLowerBound"])
        self.assertTrue(any(action["key"] == "establishSharedEvidence" for action in plan["actions"]))

    def test_text_output_states_lower_bound_and_no_product_verdict(self):
        document = with_policy(valid_document(), minimum=3, evidence=["SESSION_DISTANCE"])
        rendered = render_text(build_collection_plan(aggregate_documents([document])))
        self.assertIn("lowerBoundSemantics=minimum_only_not_guaranteed", rendered)
        self.assertIn("newArea.minimumAdditionalSessionsLowerBound=2", rendered)
        self.assertIn("productVerdict=NOT_COMPUTED", rendered)
        self.assertIn("never invents product thresholds", rendered)


if __name__ == "__main__":
    unittest.main()
