package com.dailytown.app.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationTrackingPresetTest {
    @Test
    fun `battery saver requests fewer updates than precise mode`() {
        val saver = LocationTrackingPreset.BATTERY_SAVER.config
        val precise = LocationTrackingPreset.PRECISE.config

        assertTrue(saver.intervalMillis > precise.intervalMillis)
        assertTrue(saver.minUpdateDistanceMeters > precise.minUpdateDistanceMeters)
        assertTrue(saver.maxAcceptedAccuracyMeters > precise.maxAcceptedAccuracyMeters)
        assertEquals(LocationPriorityMode.LOW_POWER, saver.priorityMode)
        assertEquals(LocationPriorityMode.HIGH_ACCURACY, precise.priorityMode)
    }

    @Test
    fun `balanced preset stays between saver and precise`() {
        val saver = LocationTrackingPreset.BATTERY_SAVER.config
        val balanced = LocationTrackingPreset.BALANCED.config
        val precise = LocationTrackingPreset.PRECISE.config

        assertTrue(balanced.intervalMillis < saver.intervalMillis)
        assertTrue(balanced.intervalMillis > precise.intervalMillis)
        assertTrue(balanced.minUpdateDistanceMeters < saver.minUpdateDistanceMeters)
        assertTrue(balanced.minUpdateDistanceMeters > precise.minUpdateDistanceMeters)
    }
}
