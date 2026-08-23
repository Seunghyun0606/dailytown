package com.dailytown.app.location

import com.dailytown.app.domain.GeoPoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationQualityPolicyTest {
    private val policy = LocationQualityPolicy(maxAccuracyMeters = 50f, maxWalkingSpeedMetersPerSecond = 10.0)

    @Test
    fun rejectsPoorAccuracy() {
        val sample = LocationSample(GeoPoint(37.56, 126.97), 80f, null, 1_000L)
        assertFalse(policy.accepts(null, sample))
    }

    @Test
    fun rejectsGpsTeleport() {
        val first = LocationSample(GeoPoint(37.5665, 126.9780), 8f, null, 1_000L)
        val teleported = LocationSample(GeoPoint(37.5765, 126.9880), 8f, null, 2_000L)
        assertTrue(policy.accepts(null, first))
        assertFalse(policy.accepts(first, teleported))
    }
}
