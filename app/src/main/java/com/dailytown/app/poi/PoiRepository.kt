package com.dailytown.app.poi

import com.dailytown.app.domain.ExplorationEngine
import com.dailytown.app.domain.GeoPoint

enum class PoiCategory { PARK, CULTURE, LANDMARK, STREET, PUBLIC_SPACE, OTHER }

data class Poi(
    val id: String,
    val name: String,
    val position: GeoPoint,
    val districtKey: String,
    val category: PoiCategory,
)

interface PoiRepository {
    suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi>
}

class FixturePoiRepository(
    private val items: List<Poi> = defaultFixturePois(),
    private val distance: ExplorationEngine = ExplorationEngine(),
) : PoiRepository {
    override suspend fun nearby(center: GeoPoint, radiusMeters: Double): List<Poi> =
        items.filter { distance.distanceMeters(center, it.position) <= radiusMeters }
}

fun defaultFixturePois(): List<Poi> = listOf(
    Poi("seoul-city-hall", "서울시청", GeoPoint(37.56650, 126.97800), "jung-gu", PoiCategory.LANDMARK),
    Poi("deoksugung-wall", "덕수궁 돌담길", GeoPoint(37.56711, 126.97676), "jung-gu", PoiCategory.STREET),
    Poi("deoksugung", "덕수궁", GeoPoint(37.56580, 126.97515), "jung-gu", PoiCategory.CULTURE),
    Poi("seoul-plaza", "서울광장", GeoPoint(37.56560, 126.97798), "jung-gu", PoiCategory.PUBLIC_SPACE),
)
