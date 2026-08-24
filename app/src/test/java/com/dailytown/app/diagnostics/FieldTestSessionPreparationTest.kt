package com.dailytown.app.diagnostics

import com.dailytown.app.location.LocationTrackingPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldTestSessionPreparationTest {
    @Test
    fun planKeepsOnlyProfilePresetAndPositiveReferenceDistance() {
        val plan = FieldTestSessionPlan(
            areaProfile = FieldTestAreaProfile.REPEAT_AREA,
            trackingPreset = LocationTrackingPreset.PRECISE,
            referenceDistanceMeters = 1234,
        )

        assertEquals(FieldTestAreaProfile.REPEAT_AREA, plan.areaProfile)
        assertEquals(LocationTrackingPreset.PRECISE, plan.trackingPreset)
        assertEquals(1234, plan.referenceDistanceMeters)
    }

    @Test(expected = IllegalArgumentException::class)
    fun planRejectsNonPositiveReferenceDistance() {
        FieldTestSessionPlan(
            areaProfile = FieldTestAreaProfile.NEW_AREA,
            trackingPreset = LocationTrackingPreset.BALANCED,
            referenceDistanceMeters = 0,
        )
    }

    @Test
    fun parserAcceptsOnlyPositiveIntegerDistance() {
        assertEquals(1500, parseReferenceDistanceMeters("1500"))
        assertEquals(42, parseReferenceDistanceMeters(" 42 "))
        assertNull(parseReferenceDistanceMeters(""))
        assertNull(parseReferenceDistanceMeters("0"))
        assertNull(parseReferenceDistanceMeters("-1"))
        assertNull(parseReferenceDistanceMeters("999999999999999"))
    }

    @Test
    fun inspectorReportsOnlyConfiguredMissingEvidence() {
        val diagnostic = diagnostic(
            distanceErrorPercent = null,
            batteryDrainPercentPerHour = null,
            sessionEncounterResolutionRatePercent = 60,
        )
        val assessment = FieldTestSessionEvidenceInspector().evaluate(
            diagnostic = diagnostic,
            areaProfile = FieldTestAreaProfile.REPEAT_AREA,
            requiredEvidence = setOf(
                FieldTestProtocolEvidence.DISTANCE_ERROR,
                FieldTestProtocolEvidence.BATTERY_DRAIN,
                FieldTestProtocolEvidence.ENCOUNTER_RESOLUTION,
            ),
        )

        assertFalse(assessment.isComplete)
        assertEquals(
            setOf(
                FieldTestProtocolEvidence.DISTANCE_ERROR,
                FieldTestProtocolEvidence.BATTERY_DRAIN,
            ),
            assessment.missingRequiredEvidence,
        )
    }

    @Test
    fun newAreaDoesNotRequireRepeatFatigueEvidence() {
        val assessment = FieldTestSessionEvidenceInspector().evaluate(
            diagnostic = diagnostic(repeatAreaFatigueProxyPercent = null),
            areaProfile = FieldTestAreaProfile.NEW_AREA,
            requiredEvidence = setOf(FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE),
        )

        assertTrue(assessment.isComplete)
        assertTrue(assessment.missingRequiredEvidence.isEmpty())
    }

    @Test
    fun repeatAreaReportsMissingRepeatFatigueEvidence() {
        val assessment = FieldTestSessionEvidenceInspector().evaluate(
            diagnostic = diagnostic(repeatAreaFatigueProxyPercent = null),
            areaProfile = FieldTestAreaProfile.REPEAT_AREA,
            requiredEvidence = setOf(FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE),
        )

        assertEquals(
            setOf(FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE),
            assessment.missingRequiredEvidence,
        )
    }

    private fun diagnostic(
        distanceErrorPercent: Int? = 5,
        batteryDrainPercentPerHour: Int? = 4,
        sessionEncounterResolutionRatePercent: Int? = 50,
        repeatAreaFatigueProxyPercent: Int? = 20,
    ): FieldTestDiagnostic = FieldTestDiagnostic(
        generatedAt = "2026-08-24T00:00:00Z",
        appVersion = "test",
        packageId = "com.dailytown.app",
        mapProvider = "NAVER",
        mapCredentialConfigured = false,
        mapHealthStatus = "READY",
        mapHealthErrorCode = null,
        trackingPreset = "BALANCED",
        trackingDurationSeconds = 60,
        sessionDistanceMeters = 500,
        referenceDistanceMeters = if (distanceErrorPercent == null) null else 520,
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
        sessionEncounterResolutionRatePercent = sessionEncounterResolutionRatePercent,
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
        acceptanceConfigured = false,
        acceptanceOverall = "NOT_EVALUATED",
        acceptanceFailedKeys = emptyList(),
    )
}
