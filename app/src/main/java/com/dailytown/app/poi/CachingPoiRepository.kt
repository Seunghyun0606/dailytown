package com.dailytown.app.poi

import com.dailytown.app.domain.GeoDistance
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.domain.HaversineGeoDistance
import kotlinx.coroutines.CancellationException

/**
 * Provider-neutral POI cache. It deliberately caches a padded search area and filters the result
 * back to the requested radius, so nearby queries can reuse data without losing edge POIs.
 *
 * When the upstream provider temporarily fails, the newest still-covering stale entry may be used
 * for a limited grace period. This is useful for a walking session with intermittent connectivity.
 */
class CachingPoiRepository(
    private val delegate: PoiRepository,
    private val clockMillis: () -> Long = System::currentTimeMillis,
    private val freshTtlMillis: Long = 5 * 60 * 1_000L,
    private val staleFallbackMillis: Long = 30 * 60 * 1_000L,
    private val paddingMeters: Double = 250.0,
    private val maxEntries: Int = 8,
    private val distance: GeoDistance = HaversineGeoDistance,
) : PoiRepository {
    private data class CacheEntry(
        val center: GeoPoint,
        val coverageRadiusMeters: Double,
        val fetchedAtMillis: Long,
        val pois: List<Poi>,
    )

    private val entries = mutableListOf<CacheEntry>()

    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
        require(radiusMeters > 0.0) { "radiusMeters must be positive" }
        val lookupAt = clockMillis()
        findCovering(center, radiusMeters, lookupAt, freshOnly = true)?.let { entry ->
            return filter(entry.pois, center, radiusMeters)
        }

        val upstreamRadius = radiusMeters + paddingMeters.coerceAtLeast(0.0)
        return try {
            val fetched = delegate.nearby(center, upstreamRadius)
                .distinctBy { it.id }
            // Cache freshness starts when the upstream result actually completed, not when a possibly
            // slow request began. This keeps the configured TTL meaningful under poor connectivity.
            val fetchedAt = clockMillis()
            remember(
                CacheEntry(
                    center = center,
                    coverageRadiusMeters = upstreamRadius,
                    fetchedAtMillis = fetchedAt,
                    pois = fetched,
                ),
            )
            filter(fetched, center, radiusMeters)
        } catch (error: CancellationException) {
            // Cancellation is lifecycle/control flow, not a provider outage. Returning stale data here
            // would keep abandoned searches alive after navigation/session shutdown.
            throw error
        } catch (error: Exception) {
            // Re-check age after the failed call. A slow timeout may itself cross the stale grace
            // boundary, so using the pre-request timestamp could serve evidence older than policy.
            val failedAt = clockMillis()
            val fallback = findCovering(center, radiusMeters, failedAt, freshOnly = false)
            if (fallback != null) filter(fallback.pois, center, radiusMeters) else throw error
        }
    }

    override fun sourceMetadata(): List<PoiSourceMetadata> = delegate.sourceMetadata()

    fun clear() {
        entries.clear()
    }

    private fun findCovering(
        center: GeoPoint,
        requestedRadiusMeters: Double,
        now: Long,
        freshOnly: Boolean,
    ): CacheEntry? {
        val maxAge = if (freshOnly) freshTtlMillis else staleFallbackMillis
        return entries
            .asSequence()
            .filter { now - it.fetchedAtMillis in 0..maxAge }
            .filter { entry ->
                val centerOffset = distance.meters(entry.center, center)
                centerOffset + requestedRadiusMeters <= entry.coverageRadiusMeters
            }
            .maxByOrNull { it.fetchedAtMillis }
    }

    private fun remember(entry: CacheEntry) {
        entries.removeAll { existing ->
            distance.meters(existing.center, entry.center) < 10.0 &&
                existing.coverageRadiusMeters == entry.coverageRadiusMeters
        }
        entries.add(0, entry)
        while (entries.size > maxEntries.coerceAtLeast(1)) {
            entries.removeAt(entries.lastIndex)
        }
    }

    private fun filter(items: List<Poi>, center: GeoPoint, radiusMeters: Double): List<Poi> =
        items.filter { distance.meters(center, it.position) <= radiusMeters }
}
