package com.dailytown.app.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TrackingMode { OFF, DEVICE, REPLAY }

data class TrackingRuntimeState(
    val mode: TrackingMode = TrackingMode.OFF,
    val preset: LocationTrackingPreset = LocationTrackingPreset.BALANCED,
)

/**
 * Pure tracking-mode state machine. Android location sources stay outside this class,
 * which keeps mode/preset transitions deterministic and JVM-testable.
 */
class TrackingSessionCoordinator(
    initialState: TrackingRuntimeState = TrackingRuntimeState(),
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<TrackingRuntimeState> = _state.asStateFlow()

    fun start(mode: TrackingMode) {
        require(mode != TrackingMode.OFF) { "Use stop() to enter OFF mode." }
        _state.update { it.copy(mode = mode) }
    }

    fun stop() {
        _state.update { it.copy(mode = TrackingMode.OFF) }
    }

    /**
     * Switching precision while real device tracking is active intentionally pauses tracking
     * so the caller can restart with a newly-created location request. Replay can continue.
     */
    fun selectPreset(preset: LocationTrackingPreset) {
        _state.update { current ->
            current.copy(
                mode = if (current.mode == TrackingMode.DEVICE) TrackingMode.OFF else current.mode,
                preset = preset,
            )
        }
    }
}
