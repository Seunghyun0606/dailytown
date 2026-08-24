#!/usr/bin/env python3

import copy
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from aggregate_exports import BatchAggregationError, aggregate_documents, render_text
from test_validate_export import session, valid_document


def document_with_sessions(sessions, version="0.7.0"):
    document = valid_document(sessions)
    document["app"]["version"] = version
    return document


class FieldTestBatchAggregatorTest(unittest.TestCase):
    def test_single_export_aggregates_without_confirmation(self):
        result = aggregate_documents([valid_document()])
        self.assertEqual(2, result["sessionCount"])
        self.assertEqual("NOT_COMPUTED", result["productVerdict"])
        self.assertIn("productVerdict=NOT_COMPUTED", render_text(result))

    def test_multiple_exports_require_non_overlap_confirmation(self):
        first = document_with_sessions([session(1,"NEW_AREA")])
        second = document_with_sessions([session(1,"REPEAT_AREA")])
        with self.assertRaisesRegex(BatchAggregationError, "non-overlap confirmation"):
            aggregate_documents([first, second])

    def test_exact_duplicate_export_is_rejected_even_with_confirmation(self):
        document = valid_document()
        with self.assertRaisesRegex(BatchAggregationError, "duplicate export"):
            aggregate_documents([document, copy.deepcopy(document)], confirm_non_overlapping=True)

    def test_policy_mismatch_is_rejected(self):
        first = document_with_sessions([session(1,"NEW_AREA")])
        second = document_with_sessions([session(1,"REPEAT_AREA")])
        second["policies"]["acceptance"]["minimumSessionDurationSeconds"] = 60
        with self.assertRaisesRegex(BatchAggregationError, "policy mismatch"):
            aggregate_documents([first, second], confirm_non_overlapping=True)

    def test_aggregate_recomputes_metrics_from_session_rows(self):
        first = document_with_sessions([session(1,"NEW_AREA",distance=10), session(2,"REPEAT_AREA",distance=20)])
        second = document_with_sessions([session(1,"NEW_AREA",distance=11), session(2,"REPEAT_AREA",distance=40)], version="0.7.1")
        result = aggregate_documents([first, second], confirm_non_overlapping=True)
        self.assertEqual(11, result["newArea"]["metrics"]["sessionDistanceMeters"]["average"])
        self.assertEqual(30, result["repeatArea"]["metrics"]["sessionDistanceMeters"]["average"])
        self.assertEqual(19, result["repeatMinusNew"]["sessionDistanceMeters"])
        self.assertEqual(["0.7.0", "0.7.1"], result["appVersions"])

    def test_missing_evidence_remains_null_across_files(self):
        first = document_with_sessions([session(1,"NEW_AREA",battery=None)])
        second = document_with_sessions([session(1,"REPEAT_AREA",battery=None)])
        result = aggregate_documents([first, second], confirm_non_overlapping=True)
        self.assertIsNone(result["newArea"]["metrics"]["batteryDrainPercentPerHour"]["average"])
        self.assertEqual(0, result["newArea"]["metrics"]["batteryDrainPercentPerHour"]["evidenceCount"])

    def test_invalid_source_export_is_rejected_before_aggregation(self):
        document = valid_document()
        document["comparison"]["newArea"]["metrics"]["sessionDistanceMeters"]["average"] = 999
        with self.assertRaisesRegex(BatchAggregationError, "invalid"):
            aggregate_documents([document])

    def test_partial_overlap_limitation_is_explicit(self):
        result = aggregate_documents([valid_document()])
        self.assertFalse(result["overlap"]["partialOverlapDetectable"])
        self.assertIn("partialOverlapDetectable=false", render_text(result))


if __name__ == "__main__": unittest.main()
