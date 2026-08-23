package com.dailytown.app.persistence

import com.dailytown.app.domain.Companion
import com.dailytown.app.domain.ExplorationState
import org.junit.Assert.assertEquals
import org.junit.Test

class ExplorationProgressTest {
    @Test
    fun roundTripsDerivedProgressWithoutLocationTrace() {
        val state = ExplorationState(
            companion = Companion("moru", "모루", 27),
            visitedSpotIds = setOf("a", "b"),
            distanceWalkedMeters = 1234.5,
            cluesFound = 4,
        )
        val restored = state.toProgress().toState(Companion("moru", "모루", 12))

        assertEquals(state, restored)
    }
}
