package com.dailytown.app.diagnostics

import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.map.MapHealth
import com.dailytown.app.map.MapHealthStatus
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.PeriodProgress
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldTestDiagnosticTest {
    @Test
    fun `render contains derived metrics but no coordinate credential or event id fields`() {
        val progress = ExplorationProgress(
            distanceWalkedMeters = 1234.0,
            companionBond = 24,
            inventoryClueIds = setOf("c1", "c2"),
            resolvedEncounterIds = setOf("e1"),
            encounterVisitedPoiIds = setOf("p1", "p2", "p3"),
            companionMemoryKeys = setOf("poi:p1"),
            daily = PeriodProgress(distanceWalkedMeters = 500.0, discoveredPoiIds = setOf("p1")),
            weekly = PeriodProgress(distanceWalkedMeters = 1234.0, discoveredPoiIds = setOf("p1", "p2")),
        )
        val sessionMetrics = FieldTestSessionMetrics(
            sessionDistanceMeters = 950,
            referenceDistanceMeters = 1000,
            distanceErrorPercent = 5,
            batteryMeasurementStatus = BatteryMeasurementStatus.VALID,
            batteryStartPercent = 80,
            batteryEndPercent = 78,
            batteryDrainPercentPoints = 2,
            batteryDrainPercentPerHour = 4,
            batteryChargeConsumedMah = 60,
        )
        val gameplayMetrics = GameplaySessionMetrics(
            encounterOfferedCount = 3,
            hintedEncounterCount = 3,
            discoveredEncounterCount = 2,
            resolvedEncounterCount = 1,
            cluesCollectedCount = 2,
            revisitOfferedCount = 1,
            revisitDiscoveredCount = 1,
            revisitResolvedCount = 0,
        )
        val text = FieldTestDiagnosticBuilder.build(
            progress = progress,
            acceptedLocationCount = 9,
            rejectedLocationCount = 1,
            trackingDurationSeconds = 420,
            sessionMetrics = sessionMetrics,
            gameplayMetrics = gameplayMetrics,
            appVersion = "test",
            mapProvider = "naver",
            mapHealth = MapHealth(MapHealthStatus.READY),
            trackingPreset = LocationTrackingPreset.BALANCED,
            acceptanceCriteria = FieldTestAcceptanceCriteria(),
            generatedAt = Instant.parse("2026-08-23T14:00:00Z"),
        ).render()

        assertTrue(text.contains("mapHealthStatus=READY"))
        assertTrue(text.contains("trackingDurationSeconds=420"))
        assertTrue(text.contains("sessionDistanceMeters=950"))
        assertTrue(text.contains("referenceDistanceMeters=1000"))
        assertTrue(text.contains("distanceErrorPercent=5"))
        assertTrue(text.contains("batteryMeasurementStatus=VALID"))
        assertTrue(text.contains("batteryDrainPercentPerHour=4"))
        assertTrue(text.contains("batteryChargeConsumedMah=60"))
        assertTrue(text.contains("sessionEncounterOfferedCount=3"))
        assertTrue(text.contains("sessionEncounterDiscoveredCount=2"))
        assertTrue(text.contains("sessionEncounterResolvedCount=1"))
        assertTrue(text.contains("sessionEncounterDiscoveryRatePercent=67"))
        assertTrue(text.contains("sessionEncounterResolutionRatePercent=50"))
        assertTrue(text.contains("sessionRevisitSharePercent=33"))
        assertTrue(text.contains("repeatAreaFatigueProxyPercent=100"))
        assertTrue(text.contains("totalDistanceMeters=1234"))
        assertTrue(text.contains("exploredPoiCount=3"))
        assertTrue(text.contains("acceptedLocationCount=9"))
        assertTrue(text.contains("rejectedLocationCount=1"))
        assertTrue(text.contains("rejectedLocationRatePercent=10"))
        assertTrue(text.contains("acceptanceConfigured=false"))
        assertTrue(text.contains("acceptanceOverall=NOT_EVALUATED"))
        assertTrue(text.contains("privacy=derived_metrics_only_no_raw_gps_no_event_ids_no_credentials"))
        assertFalse(text.contains("latitude", ignoreCase = true))
        assertFalse(text.contains("longitude", ignoreCase = true))
        assertFalse(text.contains("NAVER_MAP_NCP_KEY_ID"))
        assertFalse(text.contains("apiKey", ignoreCase = true))
        assertFalse(text.contains("poiId", ignoreCase = true))
        assertFalse(text.contains("encounterId", ignoreCase = true))
        assertFalse(text.contains("templateId", ignoreCase = true))
    }

    @Test
    fun `configured distance and battery acceptance result is included`() {
        val text = FieldTestDiagnosticBuilder.build(
            progress = ExplorationProgress(),
            acceptedLocationCount = 8,
            rejectedLocationCount = 2,
            trackingDurationSeconds = 700,
            sessionMetrics = FieldTestSessionMetrics(
                sessionDistanceMeters = 800,
                referenceDistanceMeters = 1000,
                distanceErrorPercent = 20,
                batteryMeasurementStatus = BatteryMeasurementStatus.VALID,
                batteryStartPercent = 80,
                batteryEndPercent = 77,
                batteryDrainPercentPoints = 3,
                batteryDrainPercentPerHour = 15,
                batteryChargeConsumedMah = null,
            ),
            appVersion = "test",
            mapProvider = "naver",
            mapHealth = MapHealth(MapHealthStatus.READY),
            trackingPreset = LocationTrackingPreset.BALANCED,
            acceptanceCriteria = FieldTestAcceptanceCriteria(
                minimumSessionDurationSeconds = 600,
                maximumGpsRejectionRatePercent = 25,
                requiredMapHealth = MapHealthStatus.READY,
                maximumDistanceErrorPercent = 10,
                maximumBatteryDrainPercentPerHour = 10,
            ),
            generatedAt = Instant.parse("2026-08-23T14:00:00Z"),
        ).render()

        assertTrue(text.contains("acceptanceConfigured=true"))
        assertTrue(text.contains("acceptanceOverall=FAIL"))
        assertTrue(text.contains("acceptanceFailedKeys=distanceErrorPercent,batteryDrainPercentPerHour"))
    }

    @Test
    fun `configured gameplay acceptance uses only derived counters and rates`() {
        val text = FieldTestDiagnosticBuilder.build(
            progress = ExplorationProgress(),
            acceptedLocationCount = 10,
            rejectedLocationCount = 0,
            trackingDurationSeconds = 900,
            gameplayMetrics = GameplaySessionMetrics(
                encounterOfferedCount = 3,
                hintedEncounterCount = 2,
                discoveredEncounterCount = 2,
                resolvedEncounterCount = 1,
                cluesCollectedCount = 2,
                revisitOfferedCount = 1,
                revisitDiscoveredCount = 1,
                revisitResolvedCount = 0,
            ),
            appVersion = "test",
            mapProvider = "naver",
            trackingPreset = LocationTrackingPreset.BALANCED,
            acceptanceCriteria = FieldTestAcceptanceCriteria(
                minimumDiscoveredEncountersPerSession = 2,
                minimumEncounterResolutionRatePercent = 75,
                maximumRepeatAreaFatiguePercent = 50,
            ),
            generatedAt = Instant.parse("2026-08-23T14:00:00Z"),
        ).render()

        assertTrue(text.contains("acceptanceOverall=FAIL"))
        assertTrue(
            text.contains(
                "acceptanceFailedKeys=encounterResolutionRatePercent,repeatAreaFatigueProxyPercent",
            ),
        )
    }

    @Test
    fun `provider error code can be shared without provider exception or credential`() {
        val text = FieldTestDiagnosticBuilder.build(
            progress = ExplorationProgress(),
            acceptedLocationCount = 2,
            rejectedLocationCount = 0,
            appVersion = "test",
            mapProvider = "naver",
            mapHealth = MapHealth(
                status = MapHealthStatus.AUTH_ERROR,
                errorCode = "401",
                userMessage = "safe user message",
            ),
            trackingPreset = LocationTrackingPreset.BALANCED,
            acceptanceCriteria = FieldTestAcceptanceCriteria(),
            generatedAt = Instant.parse("2026-08-23T14:00:00Z"),
        ).render()

        assertTrue(text.contains("mapHealthStatus=AUTH_ERROR"))
        assertTrue(text.contains("mapHealthErrorCode=401"))
        assertFalse(text.contains("safe user message"))
    }

    @Test
    fun `older callers can omit optional evidence without inventing metrics`() {
        val text = FieldTestDiagnosticBuilder.build(
            progress = ExplorationProgress(),
            rejectedLocationCount = 2,
            appVersion = "test",
            mapProvider = "naver",
            trackingPreset = LocationTrackingPreset.BALANCED,
            acceptanceCriteria = FieldTestAcceptanceCriteria(),
            generatedAt = Instant.parse("2026-08-23T14:00:00Z"),
        ).render()

        assertTrue(text.contains("rejectedLocationCount=2"))
        assertFalse(text.contains("acceptedLocationCount="))
        assertFalse(text.contains("rejectedLocationRatePercent="))
        assertFalse(text.contains("mapHealthStatus="))
        assertFalse(text.contains("trackingDurationSeconds="))
        assertFalse(text.contains("sessionDistanceMeters="))
        assertFalse(text.contains("batteryMeasurementStatus="))
        assertFalse(text.contains("sessionEncounterOfferedCount="))
        assertFalse(text.contains("repeatAreaFatigueProxyPercent="))
        assertTrue(text.contains("acceptanceOverall=NOT_EVALUATED"))
    }
}
