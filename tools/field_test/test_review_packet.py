#!/usr/bin/env python3

import json
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from aggregate_exports import aggregate_documents
from review_packet import (
    REVIEW_PACKET_SCHEMA,
    REVIEW_PACKET_SCHEMA_VERSION,
    build_review_packet,
    render_summary,
)
from test_validate_export import valid_document


class FieldTestReviewPacketTest(unittest.TestCase):
    def test_packet_is_versioned_and_keeps_product_verdict_uncomputed(self):
        packet = build_review_packet(aggregate_documents([valid_document()]))
        self.assertEqual(REVIEW_PACKET_SCHEMA, packet["schema"])
        self.assertEqual(REVIEW_PACKET_SCHEMA_VERSION, packet["schemaVersion"])
        self.assertEqual("NOT_COMPUTED", packet["productVerdict"])
        self.assertEqual("NOT_COMPUTED", packet["aggregate"]["productVerdict"])
        self.assertEqual("NOT_COMPUTED", packet["collectionPlan"]["productVerdict"])

    def test_evidence_ready_follows_protocol_readiness_only(self):
        document = valid_document()
        document["policies"]["comparison"] = {
            "minimumSessionsPerCohort": 1,
            "requireMatchingTrackingPreset": True,
            "requiredEvidence": ["SESSION_DISTANCE"],
        }
        packet = build_review_packet(aggregate_documents([document]))
        self.assertTrue(packet["evidenceReady"])
        self.assertEqual("PRODUCT_REVIEW_READY", packet["protocolAssessment"]["status"])
        self.assertEqual("NOT_COMPUTED", packet["productVerdict"])

    def test_unconfigured_policy_is_not_evidence_ready(self):
        packet = build_review_packet(aggregate_documents([valid_document()]))
        self.assertFalse(packet["evidenceReady"])
        self.assertEqual("COMPARABLE", packet["protocolAssessment"]["status"])
        self.assertEqual("POLICY_NOT_CONFIGURED", packet["collectionPlan"]["planStatus"])

    def test_summary_exposes_collection_lower_bounds_and_interpretation_boundary(self):
        packet = build_review_packet(aggregate_documents([valid_document()]))
        summary = render_summary(packet)
        self.assertIn("newArea.minimumAdditionalSessionsLowerBound=0", summary)
        self.assertIn("repeatArea.minimumAdditionalSessionsLowerBound=0", summary)
        self.assertIn("productVerdict=NOT_COMPUTED", summary)
        self.assertIn("product quality and release decisions remain human-owned", summary)

    def test_packet_privacy_markers_keep_storage_and_upload_disabled(self):
        packet = build_review_packet(aggregate_documents([valid_document()]))
        self.assertFalse(packet["privacy"]["rawLocation"])
        self.assertFalse(packet["privacy"]["routeGeometry"])
        self.assertFalse(packet["privacy"]["eventIdentifiers"])
        self.assertFalse(packet["privacy"]["persistentSessionLinkage"])
        self.assertFalse(packet["privacy"]["deviceLinkage"])
        self.assertFalse(packet["privacy"]["credentials"])
        self.assertFalse(packet["privacy"]["automaticPersistence"])
        self.assertFalse(packet["privacy"]["automaticUpload"])

    def test_packet_does_not_embed_raw_source_documents_or_sensitive_keys(self):
        rendered = json.dumps(build_review_packet(aggregate_documents([valid_document()])), sort_keys=True)
        for forbidden in (
            '"latitude"',
            '"longitude"',
            '"poiId"',
            '"encounterId"',
            '"templateId"',
            '"sessionToken"',
            '"deviceId"',
            '"generatedAt"',
            "NAVER_MAP_NCP_KEY_ID",
        ):
            self.assertNotIn(forbidden, rendered)


if __name__ == "__main__":
    unittest.main()
