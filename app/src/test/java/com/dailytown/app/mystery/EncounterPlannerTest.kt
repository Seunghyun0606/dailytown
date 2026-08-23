package com.dailytown.app.mystery

import org.junit.Assert.assertEquals
import org.junit.Test

class EncounterPlannerTest {
    @Test
    fun softPenaltyPrefersFreshCombinationButKeepsFallbackCandidates() {
        val repeated = EncounterCandidate("poi-a", "trace", "district", 1.0, 1.0, 100.0)
        val fresh = EncounterCandidate("poi-b", "sound", "district", 0.85, 0.8, 150.0)
        val ranked = EncounterPlanner().rank(
            listOf(repeated, fresh),
            EncounterHistory(
                recentPoiIds = setOf("poi-a"),
                recentTemplateIds = setOf("trace"),
                recentPairKeys = setOf("poi-a:trace"),
            ),
        )

        assertEquals("poi-b", ranked.first().poiId)
        assertEquals(2, ranked.size)
    }
}
