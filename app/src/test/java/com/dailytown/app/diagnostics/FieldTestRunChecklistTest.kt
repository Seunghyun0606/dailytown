package com.dailytown.app.diagnostics

import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.map.MapHealthStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldTestRunChecklistTest {
    private val evaluator = FieldTestRunChecklistEvaluator()

    @Test
    fun noRunPolicyStaysReferenceOnlyWithoutInventingReadiness() {
        val checklist = evaluator.evaluate(
            diagnostic = diagnostic(),
            areaProfile = FieldTestAreaProfile.NEW_AREA,
            acceptanceCriteria = FieldTestAcceptanceCriteria(),
            protocolCriteria = FieldTestProtocolCriteria(),
        )

        assertEquals(FieldTestRunReviewStatus.REFERENCE_ONLY, checklist.status)
        assertFalse(checklist.policyConfigured)
        assertTrue(checklist.missingRequiredEvidence.isEmpty())
    }

    @Test
    fun configuredEvidenceAndPassingAcceptanceBecomeReviewable() {
        val criteria = FieldTestAcceptanceCriteria(
            maximumGpsRejectionRatePercent = 10,
            maximumDistanceErrorPercent = 20,
            maximumBatteryDrainPercentPerHour = 10,
            minimumDiscoveredEncountersPerSession = 1,
        )
        val completed = diagnostic().withSessionPlan(
            plan = FieldTestSessionPlan(
                areaProfile = FieldTestAreaProfile.REPEAT_AREA,
                trackingPreset = LocationTrackingPreset.BALANCED,
                referenceDistanceMeters = 520,
            ),
            acceptanceCriteria = criteria,
        )
        val checklist = evaluator.evaluate(
            diagnostic = completed,
            areaProfile = FieldTestAreaProfile.REPEAT_AREA,
            acceptanceCriteria = criteria,
            protocolCriteria = FieldTestProtocolCriteria(
                requiredEvidence = setOf(FieldTestProtocolEvidence.ENCOUNTER_RESOLUTION),
            ),
        )

        assertEquals(FieldTestRunReviewStatus.REVIEWABLE, checklist.status)
        assertTrue(checklist.policyConfigured)
        assertTrue(checklist.missingRequiredEvidence.isEmpty())
        assertEquals("PASS", completed.acceptanceOverall)
    }

    @Test
    fun missingRequiredDistanceEvidenceNeedsAttention() {
        val criteria = FieldTestAcceptanceCriteria(maximumDistanceErrorPercent = 20)
        val completed = diagnostic().withSessionPlan(
            plan = FieldTestSessionPlan(
                areaProfile = FieldTestAreaProfile.NEW_AREA,
                trackingPreset = LocationTrackingPreset.BALANCED,
                referenceDistanceMeters = null,
            ),
            acceptanceCriteria = criteria,
        )
        val checklist = evaluator.evaluate(
            diagnostic = completed,
            areaProfile = FieldTestAreaProfile.NEW_AREA,
            acceptanceCriteria = criteria,
            protocolCriteria = FieldTestProtocolCriteria(),
        )

        assertEquals(FieldTestRunReviewStatus.NEEDS_ATTENTION, checklist.status)
        assertTrue(FieldTestProtocolEvidence.DISTANCE_ERROR in checklist.missingRequiredEvidence)
        assertEquals(
            FieldTestRunCheckStatus.MISSING,
            checklist.checks.first { it.key == "route" }.status,
        )
        assertEquals("NOT_EVALUATED", completed.acceptanceOverall)
    }

    @Test
    fun latchedReferenceRecomputesAcceptanceInsteadOfKeepingLaterReferenceVerdict() {
        val criteria = FieldTestAcceptanceCriteria(maximumDistanceErrorPercent = 10)
        val raw = diagnostic(
            referenceDistanceMeters = 500,
            distanceErrorPercent = 0,
            acceptanceConfigured = true,
            acceptanceOverall = "PASS",
        )
        val completed = raw.withSessionPlan(
            plan = FieldTestSessionPlan(
                areaProfile = FieldTestAreaProfile.NEW_AREA,
                trackingPreset = LocationTrackingPreset.PRECISE,
                referenceDistanceMeters = 1000,
            ),
            acceptanceCriteria = criteria,
        )

        assertEquals(50, completed.distanceErrorPercent)
        assertEquals("FAIL", completed.acceptanceOverall)
        assertTrue("distanceErrorPercent" in completed.acceptanceFailedKeys)
    }

    @Test
    fun correctedAreaProfileControlsRepeatFatigueAcceptance() {
        val criteria = FieldTestAcceptanceCriteria(maximumRepeatAreaFatiguePercent = 30)
        val plan = FieldTestSessionPlan(
            areaProfile = FieldTestAreaProfile.NEW_AREA,
            trackingPreset = LocationTrackingPreset.BALANCED,
            referenceDistanceMeters = 520,
        )
        val repeatClassified = diagnostic(repeatAreaFatigueProxyPercent = 50).withSessionPlan(
            plan = plan,
            acceptanceCriteria = criteria,
            areaProfileForAcceptance = FieldTestAreaProfile.REPEAT_AREA,
        )
        val newClassified = diagnostic(repeatAreaFatigueProxyPercent = null).withSessionPlan(
            plan = plan,
            acceptanceCriteria = criteria,
            areaProfileForAcceptance = FieldTestAreaProfile.NEW_AREA,
        )

        assertEquals("FAIL", repeatClassified.acceptanceOverall)
        assertEquals("NOT_EVALUATED", newClassified.acceptanceOverall)
        assertFalse(newClassified.acceptanceConfigured)
    }

    @Test
    fun mapReadyRequirementUsesExistingHumanCriterion() {
        val criteria = FieldTestAcceptanceCriteria(requiredMapHealth = MapHealthStatus.READY)
        val completed = diagnostic(mapHealthStatus = "AUTH_ERROR").withSessionPlan(
            plan = FieldTestSessionPlan(
                areaProfile = FieldTestAreaProfile.NEW_AREA,
                trackingPreset = LocationTrackingPreset.BALANCED,
                referenceDistanceMeters = 520,
            ),
            acceptanceCriteria = criteria,
        )
        val checklist = evaluator.evaluate(
            diagnostic = completed,
            areaProfile = FieldTestAreaProfile.NEW_AREA,
            acceptanceCriteria = criteria,
            protocolCriteria = FieldTestProtocolCriteria(),
        )

        assertEquals(FieldTestRunReviewStatus.NEEDS_ATTENTION, checklist.status)
        assertEquals(FieldTestRunCheckStatus.FAIL, checklist.checks.first { it.key == "map" }.status)
    }

    @Test
    fun repeatFatigueProtocolEvidenceDoesNotBlockNewAreaRun() {
        val checklist = evaluator.evaluate(
            diagnostic = diagnostic(repeatAreaFatigueProxyPercent = null),
            areaProfile = FieldTestAreaProfile.NEW_AREA,
            acceptanceCriteria = FieldTestAcceptanceCriteria(),
            protocolCriteria = FieldTestProtocolCriteria(
                requiredEvidence = setOf(FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE),
            ),
        )

        assertEquals(FieldTestRunReviewStatus.REFERENCE_ONLY, checklist.status)
        assertTrue(checklist.missingRequiredEvidence.isEmpty())
    }

    @Test
    fun safeRenderContainsOnlyDerivedChecklistMetadata() {
        val checklist = evaluator.evaluate(
            diagnostic = diagnostic(),
            areaProfile = FieldTestAreaProfile.REPEAT_AREA,
            acceptanceCriteria = FieldTestAcceptanceCriteria(maximumGpsRejectionRatePercent = 10),
            protocolCriteria = FieldTestProtocolCriteria(),
        )
        val rendered = checklist.render()

        assertTrue(rendered.contains("runReviewStatus="))
        assertFalse(rendered.contains("latitude", ignoreCase = true))
        assertFalse(rendered.contains("longitude", ignoreCase = true))
        assertFalse(rendered.contains("poiId", ignoreCase = true))
        assertFalse(rendered.contains("encounterId", ignoreCase = true))
        assertFalse(rendered.contains("NAVER_MAP_NCP_KEY_ID"))
    }

    private fun diagnostic(
        mapHealthStatus: String? = "READY",
        referenceDistanceMeters: Int? = 520,
        distanceErrorPercent: Int? = 4,
        batteryDrainPercentPerHour: Int? = 4,
        repeatAreaFatigueProxyPercent: Int? = 20,
        acceptanceConfigured: Boolean = false,
        acceptanceOverall: String = "NOT_EVALUATED",
    ): FieldTestDiagnostic = FieldTestDiagnostic(
        generatedAt = "2026-08-24T00:00:00Z",
        appVersion = "test",
        packageId = "com.dailytown.app",
        mapProvider = "NAVER",
        mapCredentialConfigured = false,
        mapHealthStatus = mapHealthStatus,
        mapHealthErrorCode = null,
        trackingPreset = "BALANCED",
        trackingDurationSeconds = 600,
        sessionDistanceMeters = 500,
        referenceDistanceMeters = referenceDistanceMeters,
        distanceErrorPercent = distanceErrorPercent,
        batteryMeasurementStatus = "VALID",
        batteryStartPercent = 90,
        batteryEndPercent = 89,
        batteryDrainPercentPoints = 1,
        batteryDrainPercentPerHour = batteryDrainPercentPerHour,
        batteryChargeConsumedMah = null,
        sessionEncounterOfferedCount = 2,
        sessionEncounterHintedCount = 2,
        sessionEncounterDiscoveredCount = 2,
        sessionEncounterResolvedCount = 1,
        sessionCluesCollectedCount = 1,
        sessionRevisitEncounterCount = 1,
        sessionEncounterDiscoveryRatePercent = 100,
        sessionEncounterResolutionRatePercent = 50,
        sessionRevisitSharePercent = 50,
        sessionRevisitResolutionRatePercent = 50,
        repeatAreaFatigueProxyPercent = repeatAreaFatigueProxyPercent,
        totalDistanceMeters = 500,
        exploredPoiCount = 2,
        resolvedEncounterCount = 1,
        inventoryClueCount = 1,
        companionBond = 12,
        companionMemoryCount = 1,
        acceptedLocationCount = 5,
        rejectedLocationCount = 0,
        rejectedLocationRatePercent = 0,
        dailyDistanceMeters = 500,
        dailyDiscoveries = 2,
        dailyResolutions = 1,
        weeklyDistanceMeters = 500,
        weeklyDiscoveries = 2,
        weeklyResolutions = 1,
        acceptanceConfigured = acceptanceConfigured,
        acceptanceOverall = acceptanceOverall,
        acceptanceFailedKeys = emptyList(),
    )
}
