package com.dailytown.app.location

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingSessionCoordinatorTest {
    @Test
    fun `replay starts without changing selected preset`() {
        val coordinator = TrackingSessionCoordinator()

        coordinator.start(TrackingMode.REPLAY)

        assertEquals(TrackingMode.REPLAY, coordinator.state.value.mode)
        assertEquals(LocationTrackingPreset.BALANCED, coordinator.state.value.preset)
    }

    @Test
    fun `changing preset pauses device tracking`() {
        val coordinator = TrackingSessionCoordinator()
        coordinator.start(TrackingMode.DEVICE)

        coordinator.selectPreset(LocationTrackingPreset.PRECISE)

        assertEquals(TrackingMode.OFF, coordinator.state.value.mode)
        assertEquals(LocationTrackingPreset.PRECISE, coordinator.state.value.preset)
    }

    @Test
    fun `changing preset keeps replay active`() {
        val coordinator = TrackingSessionCoordinator()
        coordinator.start(TrackingMode.REPLAY)

        coordinator.selectPreset(LocationTrackingPreset.BATTERY_SAVER)

        assertEquals(TrackingMode.REPLAY, coordinator.state.value.mode)
        assertEquals(LocationTrackingPreset.BATTERY_SAVER, coordinator.state.value.preset)
    }

    @Test
    fun `stop preserves preset`() {
        val coordinator = TrackingSessionCoordinator()
        coordinator.selectPreset(LocationTrackingPreset.PRECISE)
        coordinator.start(TrackingMode.DEVICE)

        coordinator.stop()

        assertEquals(TrackingMode.OFF, coordinator.state.value.mode)
        assertEquals(LocationTrackingPreset.PRECISE, coordinator.state.value.preset)
    }
}
