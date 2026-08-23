package com.dailytown.app.progress

import com.dailytown.app.persistence.ExplorationProgress
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalRotationCoordinatorTest {
    private val coordinator = GoalRotationCoordinator()

    @Test
    fun `same period reuses persisted goal selection`() {
        val date = LocalDate.of(2026, 8, 23)
        val first = coordinator.ensure(ExplorationProgress(), date)
        val second = coordinator.ensure(first.progress, date)

        assertEquals(first.dailyGoals.map { it.id }, second.dailyGoals.map { it.id })
        assertEquals(first.weeklyGoals.map { it.id }, second.weeklyGoals.map { it.id })
        assertEquals(first.progress.dailyGoalIds, second.progress.dailyGoalIds)
    }

    @Test
    fun `next day avoids immediately repeating previous daily goals when alternatives exist`() {
        val firstDate = LocalDate.of(2026, 8, 23)
        val nextDate = firstDate.plusDays(1)
        val first = coordinator.ensure(ExplorationProgress(), firstDate)
        val next = coordinator.ensure(first.progress, nextDate)

        val previousIds = first.dailyGoals.map { it.id }.toSet()
        val nextIds = next.dailyGoals.map { it.id }.toSet()
        assertTrue(previousIds.intersect(nextIds).isEmpty())
        assertTrue(previousIds.all { it in next.progress.recentDailyGoalIds })
    }
}
