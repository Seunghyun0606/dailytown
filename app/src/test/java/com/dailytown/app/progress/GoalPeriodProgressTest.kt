package com.dailytown.app.progress

import com.dailytown.app.domain.Companion
import com.dailytown.app.domain.ExplorationState
import com.dailytown.app.mystery.MysteryEncounter
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.PeriodProgress
import com.dailytown.app.persistence.dailyPeriodKey
import com.dailytown.app.persistence.weeklyPeriodKey
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalPeriodProgressTest {
    @Test
    fun `sync adds only new distance to daily and weekly counters`() {
        val date = LocalDate.of(2026, 8, 23)
        val progress = ExplorationProgress(
            distanceWalkedMeters = 100.0,
            daily = PeriodProgress(periodKey = dailyPeriodKey(date), distanceWalkedMeters = 40.0),
            weekly = PeriodProgress(periodKey = weeklyPeriodKey(date), distanceWalkedMeters = 70.0),
        )
        val state = ExplorationState(
            companion = Companion("moru", "모루", 12),
            distanceWalkedMeters = 135.0,
        )

        val synced = progress.syncExploration(state, date)

        assertEquals(75.0, synced.daily.distanceWalkedMeters, 0.001)
        assertEquals(105.0, synced.weekly.distanceWalkedMeters, 0.001)
        assertEquals(135.0, synced.distanceWalkedMeters, 0.001)
    }

    @Test
    fun `new day resets daily while same week preserves weekly`() {
        val sunday = LocalDate.of(2026, 8, 23)
        val monday = sunday.plusDays(1)
        val progress = ExplorationProgress(
            daily = PeriodProgress(
                periodKey = dailyPeriodKey(sunday),
                distanceWalkedMeters = 500.0,
                discoveredPoiIds = setOf("p1"),
            ),
            weekly = PeriodProgress(
                periodKey = weeklyPeriodKey(sunday),
                distanceWalkedMeters = 1200.0,
                discoveredPoiIds = setOf("p1", "p2"),
            ),
        )

        val normalized = progress.normalizePeriods(monday)

        assertEquals(dailyPeriodKey(monday), normalized.daily.periodKey)
        assertEquals(0.0, normalized.daily.distanceWalkedMeters, 0.001)
        assertEquals(weeklyPeriodKey(monday), normalized.weekly.periodKey)
        assertEquals(0.0, normalized.weekly.distanceWalkedMeters, 0.001)
    }

    @Test
    fun `duplicate clue and resolution do not inflate period goals`() {
        val date = LocalDate.of(2026, 8, 23)
        val encounter = MysteryEncounter("enc-1", "trace-chain", "poi-1")
        val once = ExplorationProgress()
            .recordClue("clue-1", date)
            .recordResolution(encounter, date)
        val twice = once
            .recordClue("clue-1", date)
            .recordResolution(encounter, date)

        assertEquals(1, twice.daily.clueIds.size)
        assertEquals(1, twice.daily.resolvedEncounterIds.size)
        assertEquals(1, twice.inventoryClueIds.size)
        assertEquals(1, twice.resolvedEncounterIds.size)
    }

    @Test
    fun `goal evaluator reads the requested period only`() {
        val date = LocalDate.of(2026, 8, 23)
        val progress = ExplorationProgress(
            daily = PeriodProgress(
                periodKey = dailyPeriodKey(date),
                discoveredPoiIds = setOf("p1", "p2"),
            ),
            weekly = PeriodProgress(
                periodKey = weeklyPeriodKey(date),
                discoveredPoiIds = setOf("p1", "p2", "p3", "p4"),
            ),
        )
        val evaluator = GoalProgressEvaluator()

        val daily = evaluator.evaluate(
            GoalDefinition("d", GoalPeriod.DAILY, GoalMetric.DISCOVER_SPOT, 3),
            progress,
            date,
        )
        val weekly = evaluator.evaluate(
            GoalDefinition("w", GoalPeriod.WEEKLY, GoalMetric.DISCOVER_SPOT, 8),
            progress,
            date,
        )

        assertEquals(2, daily.current)
        assertEquals(4, weekly.current)
    }
}
