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
        assertEquals(3, second.trackingDurationSeconds)
        assertEquals(0.0, second.sessionDistanceMeters, 0.01)
    }

    @Test
    fun `quality counters report rejected sample rate without retaining raw samples`() {
        val session = ExplorationSession(
            initialState = ExplorationState(Companion("moru", "모루", 12)),
            spots = emptyList(),
        )
        val point = GeoPoint(37.5665, 126.9780)

        session.onLocation(LocationSample(point, 8f, null, 1_000L))
        val snapshot = session.onLocation(LocationSample(point, 120f, null, 6_000L))

        assertEquals(1, snapshot.acceptedLocationCount)
        assertEquals(1, snapshot.rejectedLocationCount)
        assertEquals(2, snapshot.totalLocationSampleCount)
        assertEquals(50, snapshot.rejectedLocationRatePercent)
        assertEquals(5, snapshot.trackingDurationSeconds)
        assertEquals(point, snapshot.currentLocation?.point)
    }

    @Test
    fun `session distance tracks accepted movement independently from lifetime distance`() {
        val session = ExplorationSession(
            initialState = ExplorationState(
                companion = Companion("moru", "모루", 12),
                distanceWalkedMeters = 500.0,
            ),
            spots = emptyList(),
        )
        session.restartTracking()
        val start = GeoPoint(37.5665, 126.9780)
        val end = GeoPoint(37.5675, 126.9780)

        session.onLocation(LocationSample(start, 8f, null, 1_000L))
        val snapshot = session.onLocation(LocationSample(end, 8f, null, 11_000L))

        assertTrue(snapshot.sessionDistanceMeters > 100.0)
        assertTrue(snapshot.sessionDistanceMeters < 120.0)
        assertEquals(500.0 + snapshot.sessionDistanceMeters, snapshot.state.distanceWalkedMeters, 0.1)
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
        session.onLocation(LocationSample(point, 120f, null, 6_000L))

        session.restartTracking()
        val restarted = session.current()

        assertEquals(321.0, restarted.state.distanceWalkedMeters, 0.0)
        assertEquals(0, restarted.acceptedLocationCount)
        assertEquals(0, restarted.rejectedLocationCount)
        assertEquals(0, restarted.trackingDurationSeconds)
        assertEquals(0.0, restarted.sessionDistanceMeters, 0.0)
        assertEquals(null, restarted.currentLocation)
    }

    @Test
    fun `non monotonic elapsed realtime never creates a negative session duration`() {
        val session = ExplorationSession(
            initialState = ExplorationState(Companion("moru", "모루", 12)),
            spots = emptyList(),
        )
        val point = GeoPoint(37.5665, 126.9780)

        session.onLocation(LocationSample(point, 8f, null, 5_000L))
        val snapshot = session.onLocation(LocationSample(point, 8f, null, 4_000L))

        assertEquals(0, snapshot.trackingDurationSeconds)
        assertEquals(1, snapshot.acceptedLocationCount)
        assertEquals(1, snapshot.rejectedLocationCount)
    }
}
