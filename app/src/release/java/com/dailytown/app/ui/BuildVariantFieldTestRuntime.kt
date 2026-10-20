package com.dailytown.app.ui

import android.content.Context
import androidx.compose.runtime.Composable
import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.location.TrackingMode
import com.dailytown.app.map.MapHealth
import com.dailytown.app.persistence.ExplorationProgress

/** Release implementation: field-test runtime and UI are intentionally absent. */
@Suppress("UNUSED_PARAMETER")
internal class BuildVariantFieldTestRuntime(context: Context) {
    fun onTrackingStart(mode: TrackingMode) = Unit
    fun onTrackingStop(mode: TrackingMode) = Unit
    fun beforeTrackingPresetChange(mode: TrackingMode) = Unit
    fun recordEncounterOffered(isRevisit: Boolean) = Unit
    fun recordHinted() = Unit
    fun recordDiscovered(isRevisit: Boolean) = Unit
    fun recordClueCollected() = Unit
    fun recordResolved(isRevisit: Boolean) = Unit

    @Composable
    fun Content(
        trackingMode: TrackingMode,
        trackingPreset: LocationTrackingPreset,
        onSelectTrackingPreset: (LocationTrackingPreset) -> Unit,
        progress: ExplorationProgress,
        persistenceReady: Boolean,
        persistenceEnabled: Boolean,
        acceptedLocationCount: Int,
        rejectedLocationCount: Int,
        rejectedLocationRatePercent: Int,
        totalLocationSampleCount: Int,
        trackingDurationSeconds: Int,
        sessionDistanceMeters: Double,
        mapProvider: String,
        mapHealth: MapHealth,
    ) = Unit
}
