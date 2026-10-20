package com.dailytown.app.poi

import com.dailytown.app.domain.ExplorationEngine
import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.CancellationException

/**
 * Keeps the canonical POI source authoritative while allowing optional live providers to fill
 * coverage gaps. Canonical records always win on ID/name-nearby collisions so provider-specific
 * enrichment cannot silently replace the Source of Truth.
 */
class EnrichingPoiRepository(
    private val canonical: PoiRepository,
    private val enrichments: List<PoiRepository> = emptyList(),
    private val duplicateDistanceMeters: Double = 60.0,
    private val distance: ExplorationEngine = ExplorationEngine(),
) : PoiRepository {
    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> {
        val canonicalItems = canonical.nearby(center, radiusMeters)
        val merged = canonicalItems.toMutableList()

        enrichments.forEach { enrichment ->
            val extra = try {
                enrichment.nearby(center, radiusMeters)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                emptyList()
            }
            extra.forEach { candidate ->
                if (merged.none { existing -> isDuplicate(existing, candidate) }) {
                    merged += candidate
                }
            }
        }
        return merged
    }

    override fun sourceMetadata(): List<PoiSourceMetadata> =
        (canonical.sourceMetadata() + enrichments.flatMap { it.sourceMetadata() })
            .distinctBy { it.id }

    private fun isDuplicate(existing: Poi, candidate: Poi): Boolean {
        if (existing.id == candidate.id) return true
        if (normalizeName(existing.name) != normalizeName(candidate.name)) return false
        return distance.distanceMeters(existing.position, candidate.position) <= duplicateDistanceMeters
    }

    private fun normalizeName(value: String): String =
        value.lowercase()
            .filterNot { it.isWhitespace() || it == '-' || it == '_' || it == '·' }
}

/**
 * Final runtime safety boundary. Fresh network failures degrade to an empty candidate set while
 * CachingPoiRepository can still serve its own stale entry before an error reaches this wrapper.
 */
class DegradingPoiRepository(
    private val delegate: PoiRepository,
) : PoiRepository {
    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> = try {
        delegate.nearby(center, radiusMeters)
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        emptyList()
    }

    override fun sourceMetadata(): List<PoiSourceMetadata> = delegate.sourceMetadata()
}
