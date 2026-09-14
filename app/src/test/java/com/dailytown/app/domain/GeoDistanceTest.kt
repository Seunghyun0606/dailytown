package com.dailytown.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoDistanceTest {
    @Test
    fun `same point has zero distance`() {
        val point = GeoPoint(37.5665, 126.9780)
        assertEquals(0.0, HaversineGeoDistance.meters(point, point), 0.001)
    }

    @Test
    fun `short latitude step is approximately eleven meters`() {
        val start = GeoPoint(37.5665, 126.9780)
        val end = GeoPoint(37.5666, 126.9780)
        val meters = HaversineGeoDistance.meters(start, end)
        assertTrue(meters in 10.5..11.7)
    }

    @Test
    fun `exploration engine compatibility distance delegates to shared implementation`() {
        val start = GeoPoint(37.5665, 126.9780)
        val end = GeoPoint(37.5670, 126.9780)
        assertEquals(
            HaversineGeoDistance.meters(start, end),
            ExplorationEngine().distanceMeters(start, end),
            0.000001,
        )
    }
}
