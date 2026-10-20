package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

/**
 * Application-level owner for nearby POI queries.
 *
 * All runtime callers share this boundary so mutable cache/provider decorators are never entered
 * concurrently from map refresh and encounter selection. Each successful query also becomes the
 * latest immutable snapshot consumed by map presentation independently from encounter lifetime.
 */
class NearbyPoiCoordinator(
    private val delegate: PoiRepository,
) : PoiRepository {
    private val queryMutex = Mutex()
    private val completionGeneration = AtomicLong(0L)
    private val _snapshots = MutableStateFlow<NearbyPoiSnapshot?>(null)
    val snapshots: StateFlow<NearbyPoiSnapshot?> = _snapshots.asStateFlow()

    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
        require(radiusMeters > 0.0) { "radiusMeters must be positive" }
        val generationAtCall = completionGeneration.get()
        return queryMutex.withLock {
            val latest = _snapshots.value
            // If another identical request completed while this caller was waiting for the single
            // query lane, reuse that result instead of asking the mutable cache/provider chain twice.
            if (
                completionGeneration.get() != generationAtCall &&
                latest != null &&
                latest.center == center &&
                latest.radiusMeters == radiusMeters
            ) {
                return@withLock latest.pois
            }

            val pois = delegate.nearby(center, radiusMeters)
            _snapshots.value = NearbyPoiSnapshot(
                center = center,
                radiusMeters = radiusMeters,
                pois = pois,
            )
            completionGeneration.incrementAndGet()
            pois
        }
    }

    override fun sourceMetadata(): List<PoiSourceMetadata> = delegate.sourceMetadata()
}

data class NearbyPoiSnapshot(
    val center: GeoPoint,
    val radiusMeters: Double,
    val pois: List<Poi>,
)
