package com.dailytown.app.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GoalPlannerTest {
    @Test
    fun avoidsRecentlyUsedGoalsWhenEnoughFreshGoalsExist() {
        val catalog = GoalCatalog.defaults().filter { it.period == GoalPeriod.DAILY }
        val planned = GoalPlanner().plan("2026-08-23", catalog, setOf("daily-walk-800"), 2)
        assertEquals(2, planned.size)
        assertFalse(planned.any { it.id == "daily-walk-800" })
    }

    @Test
    fun planningIsDeterministicForSamePeriod() {
        val catalog = GoalCatalog.defaults()
        val planner = GoalPlanner()
        assertEquals(
            planner.plan("2026-W34", catalog, emptySet(), 3),
            planner.plan("2026-W34", catalog, emptySet(), 3),
        )
    }
}
