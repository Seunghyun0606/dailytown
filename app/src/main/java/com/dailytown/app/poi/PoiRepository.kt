package com.dailytown.app.poi

import com.dailytown.app.domain.ExplorationEngine
import com.dailytown.app.domain.GeoPoint

enum class PoiCategory { PARK, CULTURE, LANDMARK, STREET, PUBLIC_SPACE, OTHER }

enum class PoiSourceRole { FIXTURE, CANONICAL, ENRICHMENT }

data class PoiSourceMetadata(
    val id: String,
    val displayName: String,
    val role: PoiSourceRole,
    val attributionText: String,
    val licenseSummary: String,
    val sourceUrl: String? = null,
)

data class Poi(
    val id: String,
    val name: String,
    val position: GeoPoint,
    val districtKey: String,
    val category: PoiCategory,
)

interface PoiRepository {
    suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi>

    /**
     * Human-readable source/licensing metadata for settings, review, and future attribution UI.
     * Credentials and provider exception payloads must never be exposed here.
     */
    fun sourceMetadata(): List<PoiSourceMetadata> = emptyList()
}

/**
 * Compatibility wrapper for internal tests and QA callers. The actual fixture catalog is selected
 * by Android build variant: debug owns the authored field-test anchors while release supplies none.
 */
class FixturePoiRepository(
    private val items: List<Poi> = defaultFixturePois(),
    private val distance: ExplorationEngine = ExplorationEngine(),
) : PoiRepository {
    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> =
        items.filter { distance.distanceMeters(center, it.position) <= radiusMeters }

    override fun sourceMetadata(): List<PoiSourceMetadata> = if (items.isEmpty()) {
        emptyList()
    } else {
        listOf(
            PoiSourceMetadata(
                id = "fixture",
                displayName = "Daily Town field-test fixture",
                role = PoiSourceRole.FIXTURE,
                attributionText = "개발/필드테스트용 고정 POI",
                licenseSummary = "Production 데이터 소스로 사용하지 않음",
            ),
        )
    }
}

fun defaultFixturePois(): List<Poi> = BuildVariantFixtureCatalog.items
