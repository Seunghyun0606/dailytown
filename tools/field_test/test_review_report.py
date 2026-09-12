#!/usr/bin/env python3

import csv
import io
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from aggregate_exports import aggregate_documents
from review_report import render_csv, render_markdown
from test_validate_export import session, valid_document


class FieldTestReviewReportTest(unittest.TestCase):
    def test_markdown_keeps_product_verdict_separate_from_protocol_readiness(self):
        document = valid_document()
        result = aggregate_documents([document])
        rendered = render_markdown(result)
        self.assertIn("Product verdict is **NOT_COMPUTED**", rendered)
        self.assertIn("| Batch protocol | COMPARABLE |", rendered)

    def test_markdown_includes_metric_evidence_and_delta(self):
        document = valid_document(
            [session(1, "NEW_AREA", distance=10), session(2, "REPEAT_AREA", distance=20)]
        )
        rendered = render_markdown(aggregate_documents([document]))
        self.assertIn("`sessionDistanceMeters`", rendered)
        self.assertIn("| 10 | 1/1 | 20 | 1/1 | 10 |", rendered)

    def test_markdown_includes_protocol_issues(self):
        document = valid_document([session(1, "NEW_AREA")])
        result = aggregate_documents([document])
        rendered = render_markdown(result)
        self.assertIn("`repeatAreaSessions`: missing", rendered)
        self.assertIn("DATA_INSUFFICIENT", rendered)

    def test_csv_is_machine_readable_and_preserves_missing_evidence(self):
        document = valid_document(
            [session(1, "NEW_AREA", battery=None), session(2, "REPEAT_AREA", battery=None)]
        )
        rows = list(csv.DictReader(io.StringIO(render_csv(aggregate_documents([document])))))
        battery = next(row for row in rows if row["rowType"] == "METRIC" and row["key"] == "batteryDrainPercentPerHour")
        self.assertEqual("", battery["newArea"])
        self.assertEqual("0/1", battery["newEvidence"])
        self.assertEqual("", battery["repeatMinusNew"])

    def test_csv_contains_no_product_pass_fail_verdict(self):
        rows = list(csv.DictReader(io.StringIO(render_csv(aggregate_documents([valid_document()])))))
        verdict = next(row for row in rows if row["rowType"] == "SUMMARY" and row["key"] == "productVerdict")
        self.assertEqual("NOT_COMPUTED", verdict["newArea"])

    def test_markdown_includes_collection_lower_bound_from_approved_policy(self):
        document = valid_document()
        document["policies"]["comparison"] = {
            "minimumSessionsPerCohort": 3,
            "requireMatchingTrackingPreset": None,
            "requiredEvidence": ["SESSION_DISTANCE"],
        }
        rendered = render_markdown(aggregate_documents([document]))
        self.assertIn("| Collection plan | ADDITIONAL_PROTOCOL_EVIDENCE_REQUIRED |", rendered)
        self.assertIn("| Minimum additional sessions (lower bound) | 2 | 2 |", rendered)
        self.assertIn("`SESSION_DISTANCE`", rendered)

    def test_csv_includes_collection_evidence_deficit_rows(self):
        sessions = [session(1, "NEW_AREA", battery=10), session(2, "REPEAT_AREA", battery=None)]
        document = valid_document(sessions)
        document["policies"]["comparison"] = {
            "minimumSessionsPerCohort": 1,
            "requireMatchingTrackingPreset": None,
            "requiredEvidence": ["BATTERY_DRAIN"],
        }
        rows = list(csv.DictReader(io.StringIO(render_csv(aggregate_documents([document])))))
        plan_status = next(row for row in rows if row["rowType"] == "SUMMARY" and row["key"] == "collectionPlanStatus")
        evidence = next(row for row in rows if row["rowType"] == "COLLECTION_EVIDENCE" and row["key"] == "BATTERY_DRAIN")
        self.assertEqual("ADDITIONAL_PROTOCOL_EVIDENCE_REQUIRED", plan_status["newArea"])
        self.assertEqual("1/1", evidence["newArea"])
        self.assertEqual("0/1", evidence["repeatArea"])
        self.assertEqual("0", evidence["newEvidence"])
        self.assertEqual("1", evidence["repeatEvidence"])


if __name__ == "__main__":
    unittest.main()
