package com.dailytown.app.domain

import com.dailytown.app.location.LocationQualityPolicy
import com.dailytown.app.location.LocationSample

data class ExplorationSnapshot(
    val state: ExplorationState,
    val currentLocation: LocationSample? = null,
    val newlyDiscovered: List<MysterySpot> = emptyList(),
    val rejectedLocationCount: Int = 0,
)

class ExplorationSession(
    initialState: ExplorationState,
    private val spots: List<MysterySpot>,
    private val engine: ExplorationEngine = ExplorationEngine(),
    private val qualityPolicy: LocationQualityPolicy = LocationQualityPolicy(),
) {
    private var snapshot = ExplorationSnapshot(initialState)
    private var previousAccepted: LocationSample? = null

    fun current(): ExplorationSnapshot = snapshot

    fun onLocation(sample: LocationSample): ExplorationSnapshot {
        if (!qualityPolicy.accepts(previousAccepted, sample)) {
            snapshot = snapshot.copy(
                newlyDiscovered = emptyList(),
                rejectedLocationCount = snapshot.rejectedLocationCount + 1,
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
            rejectedLocationCount = snapshot.rejectedLocationCount,
        )
        return snapshot
    }

    fun reset(initialState: ExplorationState = snapshot.state.copy(
        visitedSpotIds = emptySet(),
        distanceWalkedMeters = 0.0,
        cluesFound = 0,
    )) {
        previousAccepted = null
        snapshot = ExplorationSnapshot(initialState)
    }
}
