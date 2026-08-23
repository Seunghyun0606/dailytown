package com.dailytown.app.domain

import com.dailytown.app.location.LocationSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExplorationSessionTest {
    @Test
    fun replayLocationsDiscoverSpotsOnlyOnce() {
        val spot = MysterySpot(
            id = "spot",
            title = "테스트 단서",
            position = GeoPoint(37.5665, 126.9780),
            discoveryRadiusMeters = 60.0,
        )
        val session = ExplorationSession(
            initialState = ExplorationState(Companion("moru", "모루", 12)),
            spots = listOf(spot),
        )
        val first = session.onLocation(LocationSample(spot.position, 8f, null, 1_000L))
        val second = session.onLocation(LocationSample(spot.position, 8f, null, 4_000L))

        assertEquals(1, first.newlyDiscovered.size)
        assertTrue(second.newlyDiscovered.isEmpty())
        assertEquals(1, second.state.cluesFound)
    }
}
