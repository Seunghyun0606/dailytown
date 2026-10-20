package com.dailytown.app.progress

import com.dailytown.app.mystery.MysteryEncounter
import com.dailytown.app.persistence.ExplorationProgress
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkSessionSummaryTest {
    private val date = LocalDate.of(2026, 10, 20)

    @Test
    fun summaryDiffsStartAndEndProgressWithoutRawLocation() {
        val start = ExplorationProgress(
            encounterVisitedPoiIds = setOf("poi-old"),
            inventoryClueIds = setOf("c0"),
            companionMemoryKeys = setOf("poi:poi-old"),
        ).normalizePeriods(date)
        val end = start
            .recordEncounterVisit("poi-new", "trace-chain", date, poiTitle = "새 장소")
            .recordEncounterVisit("poi-old", "time-layer", date, poiTitle = "옛 장소")
            .recordClue("c1", date)
            .recordClue("c2", date)
            .recordResolution(MysteryEncounter("enc-s1-0:poi-new:trace-chain", "trace-chain", "poi-new"), date)
            .recordMemory("poi:poi-new")
            .recordMemory("mechanic:TRACE_CHAIN")

        val summary = WalkSessionSummaryBuilder.build(
            start = start,
            end = end,
            sessionDiscoveredPoiIds = listOf("poi-new", "poi-old", "poi-new"),
            distanceMeters = 812.4,
            durationSeconds = 1_260,
            bondBefore = 12,
            bondAfter = 18,
            goals = listOf(
                GoalDefinition("daily-resolve-1", GoalPeriod.DAILY, GoalMetric.RESOLVE_MYSTERY, 1),
                GoalDefinition("daily-clue-3", GoalPeriod.DAILY, GoalMetric.COLLECT_CLUE, 3),
            ),
            date = date,
        )

        assertEquals(812, summary.distanceMeters)
        assertEquals(1, summary.newPlaces)
        assertEquals(1, summary.revisitedPlaces)
        assertEquals(2, summary.cluesFound)
        assertEquals(1, summary.mysteriesResolved)
        assertEquals(2, summary.memoriesGained)
        assertEquals(6, summary.bondDelta)
        assertEquals(listOf("동네 이야기 1건 해결"), summary.goalsCompleted)
        assertEquals("오늘 같이 찾은 이야기, 오래 기억할게.", summary.moruClosingLine)
    }

    @Test
    fun quietWalkStillProducesAnHonestSummary() {
        val progress = ExplorationProgress().normalizePeriods(date)
        val summary = WalkSessionSummaryBuilder.build(
            start = progress,
            end = progress,
            sessionDiscoveredPoiIds = emptyList(),
            distanceMeters = 120.0,
            durationSeconds = 45,
            bondBefore = 12,
            bondAfter = 12,
            goals = emptyList(),
            date = date,
        )
        assertEquals(0, summary.newPlaces + summary.revisitedPlaces + summary.cluesFound + summary.mysteriesResolved)
        assertTrue(summary.goalsCompleted.isEmpty())
        assertEquals("오늘은 짧았지만, 다음엔 조금 더 걸어보자.", summary.moruClosingLine)
    }
}
