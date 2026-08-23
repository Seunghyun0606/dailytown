package com.dailytown.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentRotationTest {
    @Test
    fun excludesRecentlySeenContentAndRanksRemaining() {
        val ranked = ContentRotation().rank(
            candidates = listOf(
                ContentCandidate("old", "a", 1.0, 100.0, 1.0),
                ContentCandidate("fresh", "a", 0.9, 200.0, 0.8),
                ContentCandidate("far", "b", 0.5, 1800.0, 0.4),
            ),
            recentlySeenIds = setOf("old"),
        )
        assertEquals(listOf("fresh", "far"), ranked.map { it.id })
    }
}
