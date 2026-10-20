package com.dailytown.app.domain

import com.dailytown.app.location.LocationQualityPolicy
import com.dailytown.app.location.LocationSample

data class ExplorationSnapshot(
    val state: ExplorationState,
    val currentLocation: LocationSample? = null,
    val newlyDiscovered: List<MysterySpot> = emptyList(),
    val acceptedLocationCount: Int = 0,
    val rejectedLocationCount: Int = 0,
    val trackingDurationSeconds: Int = 0,
    val sessionDistanceMeters: Double = 0.0,
) {
    val totalLocationSampleCount: Int
        get() = acceptedLocationCount + rejectedLocationCount

    val rejectedLocationRatePercent: Int
        get() = if (totalLocationSampleCount == 0) 0
        else ((rejectedLocationCount * 100.0) / totalLocationSampleCount).toInt()
}

class ExplorationSession(
    initialState: ExplorationState,
    private val spots: List<MysterySpot>,
    private val engine: ExplorationEngine = ExplorationEngine(),
    qualityPolicy: LocationQualityPolicy = LocationQualityPolicy(),
) {
    private var snapshot = ExplorationSnapshot(initialState)
    private var previousAccepted: LocationSample? = null
    private var trackingStartedElapsedRealtimeMillis: Long? = null
    private var trackingStartedDistanceMeters: Double = initialState.distanceWalkedMeters
    private var qualityPolicy: LocationQualityPolicy = qualityPolicy

    fun current(): ExplorationSnapshot = snapshot

    fun onLocation(sample: LocationSample): ExplorationSnapshot {
        val startedAt = trackingStartedElapsedRealtimeMillis ?: sample.elapsedRealtimeMillis.also {
            trackingStartedElapsedRealtimeMillis = it
        }
        val trackingDurationSeconds =
            ((sample.elapsedRealtimeMillis - startedAt).coerceAtLeast(0L) / 1_000L).toInt()

        if (!qualityPolicy.accepts(previousAccepted, sample)) {
            snapshot = snapshot.copy(
                newlyDiscovered = emptyList(),
                rejectedLocationCount = snapshot.rejectedLocationCount + 1,
                trackingDurationSeconds = trackingDurationSeconds,
            )
            return snapshot
        }

        val update = engine.update(
            state = snapshot.state,
            previous = previousAccepted?.point,
            current = sample.point,
            spots = spots,
        )
        previousAccepted = sample
        snapshot = ExplorationSnapshot(
            state = update.state,
            currentLocation = sample,
            newlyDiscovered = update.newlyDiscovered,
            acceptedLocationCount = snapshot.acceptedLocationCount + 1,
            rejectedLocationCount = snapshot.rejectedLocationCount,
            trackingDurationSeconds = trackingDurationSeconds,
            sessionDistanceMeters =
                (update.state.distanceWalkedMeters - trackingStartedDistanceMeters).coerceAtLeast(0.0),
        )
        return snapshot
    }

    fun applyCompanionBond(delta: Int) {
        if (delta == 0) return
        val companion = snapshot.state.companion
        snapshot = snapshot.copy(
            state = snapshot.state.copy(
                companion = companion.copy(bond = (companion.bond + delta).coerceAtLeast(0)),
            ),
        )
    }

    fun setLocationQualityPolicy(policy: LocationQualityPolicy) {
        qualityPolicy = policy
        previousAccepted = null
    }

    /** Restart GPS/replay tracking while preserving long-term derived progress. */
    fun restartTracking() {
        previousAccepted = null
        trackingStartedElapsedRealtimeMillis = null
        trackingStartedDistanceMeters = snapshot.state.distanceWalkedMeters
        snapshot = snapshot.copy(
            currentLocation = null,
            newlyDiscovered = emptyList(),
            acceptedLocationCount = 0,
            rejectedLocationCount = 0,
            trackingDurationSeconds = 0,
            sessionDistanceMeters = 0.0,
        )
    }

    /** Restore persisted derived progress. Raw samples and short-lived quality counters are never restored. */
    fun restore(state: ExplorationState) {
        previousAccepted = null
        trackingStartedElapsedRealtimeMillis = null
        trackingStartedDistanceMeters = state.distanceWalkedMeters
        snapshot = ExplorationSnapshot(state)
    }

    fun reset(initialState: ExplorationState = snapshot.state.copy(
        visitedSpotIds = emptySet(),
        distanceWalkedMeters = 0.0,
        cluesFound = 0,
    )) {
        previousAccepted = null
        trackingStartedElapsedRealtimeMillis = null
        trackingStartedDistanceMeters = initialState.distanceWalkedMeters
        snapshot = ExplorationSnapshot(initialState)
    }
}
