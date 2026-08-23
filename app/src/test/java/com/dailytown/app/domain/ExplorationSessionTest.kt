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
        assertEquals(2, second.acceptedLocationCount)
        assertEquals(0, second.rejectedLocationCount)
    }

    @Test
    fun `quality counters report rejected sample rate without retaining raw samples`() {
        val session = ExplorationSession(
            initialState = ExplorationState(Companion("moru", "모루", 12)),
            spots = emptyList(),
        )
        val point = GeoPoint(37.5665, 126.9780)

        session.onLocation(LocationSample(point, 8f, null, 1_000L))
        val snapshot = session.onLocation(LocationSample(point, 120f, null, 2_000L))

        assertEquals(1, snapshot.acceptedLocationCount)
        assertEquals(1, snapshot.rejectedLocationCount)
        assertEquals(2, snapshot.totalLocationSampleCount)
        assertEquals(50, snapshot.rejectedLocationRatePercent)
        assertEquals(point, snapshot.currentLocation?.point)
    }

    @Test
    fun `restart tracking clears only short lived location quality counters`() {
        val companion = Companion("moru", "모루", 12)
        val session = ExplorationSession(
            initialState = ExplorationState(companion, distanceWalkedMeters = 321.0),
            spots = emptyList(),
        )
        val point = GeoPoint(37.5665, 126.9780)
        session.onLocation(LocationSample(point, 8f, null, 1_000L))
        session.onLocation(LocationSample(point, 120f, null, 2_000L))

        session.restartTracking()
        val restarted = session.current()

        assertEquals(321.0, restarted.state.distanceWalkedMeters, 0.0)
        assertEquals(0, restarted.acceptedLocationCount)
        assertEquals(0, restarted.rejectedLocationCount)
        assertEquals(null, restarted.currentLocation)
    }
}
