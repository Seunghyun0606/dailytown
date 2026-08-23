package com.dailytown.app.persistence

import com.dailytown.app.domain.Companion
import com.dailytown.app.domain.ExplorationState

data class ExplorationProgress(
    val visitedSpotIds: Set<String> = emptySet(),
    val distanceWalkedMeters: Double = 0.0,
    val cluesFound: Int = 0,
    val companionBond: Int = 0,
)

interface ProgressStore {
    suspend fun load(): ExplorationProgress
    suspend fun save(progress: ExplorationProgress)
}

fun ExplorationState.toProgress(): ExplorationProgress = ExplorationProgress(
    visitedSpotIds = visitedSpotIds,
    distanceWalkedMeters = distanceWalkedMeters,
    cluesFound = cluesFound,
    companionBond = companion.bond,
)

fun ExplorationProgress.toState(defaultCompanion: Companion): ExplorationState = ExplorationState(
    companion = defaultCompanion.copy(bond = companionBond.takeIf { it > 0 } ?: defaultCompanion.bond),
    visitedSpotIds = visitedSpotIds,
    distanceWalkedMeters = distanceWalkedMeters,
    cluesFound = cluesFound,
)
