package com.dailytown.app.diagnostics

import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.PeriodProgress
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldTestDiagnosticTest {
    @Test
    fun `render contains derived metrics but no coordinate or credential fields`() {
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
        val text = FieldTestDiagnosticBuilder.build(
            progress = progress,
            rejectedLocationCount = 3,
            appVersion = "test",
            mapProvider = "naver",
            trackingPreset = LocationTrackingPreset.BALANCED,
            generatedAt = Instant.parse("2026-08-23T14:00:00Z"),
        ).render()

        assertTrue(text.contains("totalDistanceMeters=1234"))
        assertTrue(text.contains("exploredPoiCount=3"))
        assertTrue(text.contains("privacy=derived_metrics_only_no_raw_gps"))
        assertFalse(text.contains("latitude", ignoreCase = true))
        assertFalse(text.contains("longitude", ignoreCase = true))
        assertFalse(text.contains("NAVER_MAP_NCP_KEY_ID"))
        assertFalse(text.contains("apiKey", ignoreCase = true))
    }
}
