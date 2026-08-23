package com.dailytown.app.domain

import kotlin.math.*

data class GeoPoint(val latitude: Double, val longitude: Double)

data class Companion(
    val id: String,
    val name: String,
    val bond: Int,
)

data class MysterySpot(
    val id: String,
    val title: String,
    val position: GeoPoint,
    val discoveryRadiusMeters: Double = 80.0,
)

data class ExplorationState(
    val companion: Companion,
    val visitedSpotIds: Set<String> = emptySet(),
    val distanceWalkedMeters: Double = 0.0,
    val cluesFound: Int = 0,
)

data class ExplorationUpdate(
    val state: ExplorationState,
    val newlyDiscovered: List<MysterySpot>,
)

class ExplorationEngine {
    fun update(
        state: ExplorationState,
        previous: GeoPoint?,
        current: GeoPoint,
        spots: List<MysterySpot>,
    ): ExplorationUpdate {
        val segment = previous?.let { distanceMeters(it, current) } ?: 0.0
        val discoveries = spots.filter { spot ->
            spot.id !in state.visitedSpotIds && distanceMeters(current, spot.position) <= spot.discoveryRadiusMeters
        }
        return ExplorationUpdate(
            state = state.copy(
                visitedSpotIds = state.visitedSpotIds + discoveries.map { it.id },
                distanceWalkedMeters = state.distanceWalkedMeters + segment,
                cluesFound = state.cluesFound + discoveries.size,
            ),
            newlyDiscovered = discoveries,
        )
    }

    fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val earthRadius = 6_371_000.0
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return earthRadius * 2 * atan2(sqrt(h), sqrt(1 - h))
    }
}
