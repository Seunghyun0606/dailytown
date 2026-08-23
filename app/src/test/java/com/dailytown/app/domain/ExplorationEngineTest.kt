package com.dailytown.app.domain

import org.junit.Assert.*
import org.junit.Test

class ExplorationEngineTest {
    private val engine = ExplorationEngine()

    @Test
    fun discoversNearbySpotOnlyOnce() {
        val companion = Companion("moru", "모루", 12)
        val spot = MysterySpot("spot-1", "수상한 벤치", GeoPoint(37.5665, 126.9780), 100.0)
        val initial = ExplorationState(companion)

        val first = engine.update(initial, null, GeoPoint(37.5665, 126.9780), listOf(spot))
        val second = engine.update(first.state, GeoPoint(37.5665, 126.9780), GeoPoint(37.5665, 126.9780), listOf(spot))

        assertEquals(listOf("spot-1"), first.newlyDiscovered.map { it.id })
        assertTrue(second.newlyDiscovered.isEmpty())
        assertEquals(1, second.state.cluesFound)
    }

    @Test
    fun tracksWalkingDistance() {
        val state = ExplorationState(Companion("moru", "모루", 0))
        val result = engine.update(
            state,
            GeoPoint(37.5665, 126.9780),
            GeoPoint(37.5675, 126.9780),
            emptyList(),
        )
        assertTrue(result.state.distanceWalkedMeters in 100.0..120.0)
    }
}
