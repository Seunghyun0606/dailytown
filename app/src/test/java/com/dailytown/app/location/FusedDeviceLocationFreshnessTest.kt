package com.dailytown.app.location

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FusedDeviceLocationFreshnessTest {
    @Test
    fun rejectsLocationThatPredatesRequestBeyondTolerance() {
        assertFalse(
            isFreshDeviceLocation(
                locationElapsedRealtimeMillis = 90_000L,
                requestStartedElapsedRealtimeMillis = 100_000L,
                maxPreRequestAgeMillis = 5_000L,
            ),
        )
    }

    @Test
    fun acceptsLocationJustBeforeRequestWithinTolerance() {
        assertTrue(
            isFreshDeviceLocation(
                locationElapsedRealtimeMillis = 97_000L,
                requestStartedElapsedRealtimeMillis = 100_000L,
                maxPreRequestAgeMillis = 5_000L,
            ),
        )
    }

    @Test
    fun acceptsLocationProducedAfterRequestStarts() {
        assertTrue(
            isFreshDeviceLocation(
                locationElapsedRealtimeMillis = 101_000L,
                requestStartedElapsedRealtimeMillis = 100_000L,
                maxPreRequestAgeMillis = 5_000L,
            ),
        )
    }
}
