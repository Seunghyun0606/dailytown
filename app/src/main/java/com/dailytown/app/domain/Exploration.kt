package com.dailytown.app.domain

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

class ExplorationEngine(
    private val distance: GeoDistance = HaversineGeoDistance,
) {
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

    /** Compatibility API for gameplay callers; geographic math is owned by [GeoDistance]. */
    fun distanceMeters(a: GeoPoint, b: GeoPoint): Double = distance.meters(a, b)
}
