package com.dailytown.app.map

import com.dailytown.app.domain.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapMarkerLayerComposerTest {
    @Test
    fun productionNearbyMarkerWinsOverMatchingFieldTestFixture() {
        val point = GeoPoint(37.5665, 126.9780)
        val composer = MapMarkerLayerComposer(showFixtureMarkers = true)

        val markers = composer.compose(
            runtimeMarkers = emptyList(),
            nearbyPoiMarkers = listOf(
                MapMarkerSpec("poi:tourapi:cityhall", "서울시청", GeoPoint(37.56651, 126.97801)),
            ),
            fixtureMarkers = listOf(MapMarkerSpec("fixture:cityhall", "서울시청", point)),
        )

        assertEquals(listOf("poi:tourapi:cityhall"), markers.map { it.id })
    }

    @Test
    fun fixtureMarkersAreExcludedWhenDisabled() {
        val composer = MapMarkerLayerComposer(showFixtureMarkers = false)

        val markers = composer.compose(
            runtimeMarkers = emptyList(),
            nearbyPoiMarkers = emptyList(),
            fixtureMarkers = listOf(
                MapMarkerSpec("fixture:one", "fixture", GeoPoint(37.5665, 126.9780)),
            ),
        )

        assertTrue(markers.isEmpty())
    }

    @Test
    fun legacyDemoMarkersAreSuppressedButActiveEncounterRemains() {
        val composer = MapMarkerLayerComposer()
        val legacy = MapMarkerSpec("cityhall-echo", "legacy", GeoPoint(37.5665, 126.9780))
        val active = MapMarkerSpec(
            id = "active:test",
            title = "active",
            position = GeoPoint(37.5700, 126.9800),
            selected = true,
        )

        val markers = composer.compose(
            runtimeMarkers = listOf(legacy, active),
            nearbyPoiMarkers = emptyList(),
            fixtureMarkers = emptyList(),
        )

        assertFalse(markers.any { it.id == legacy.id })
        assertTrue(markers.any { it.id == active.id })
    }

    @Test
    fun activeEncounterSuppressesUnderlyingNearbyPoiMarker() {
        val point = GeoPoint(37.5665, 126.9780)
        val composer = MapMarkerLayerComposer()

        val markers = composer.compose(
            runtimeMarkers = listOf(
                MapMarkerSpec(
                    id = "active:one",
                    title = "active",
                    position = point,
                    selected = true,
                ),
            ),
            nearbyPoiMarkers = listOf(MapMarkerSpec("poi:one", "one", point)),
            fixtureMarkers = emptyList(),
        )

        assertEquals(listOf("active:one"), markers.map { it.id })
    }

    @Test
    fun gameplayMarkerIdWinsOverBaseMarkerWithSameId() {
        val point = GeoPoint(37.5665, 126.9780)
        val composer = MapMarkerLayerComposer(suppressLegacyDemoMarkers = false)

        val markers = composer.compose(
            runtimeMarkers = listOf(MapMarkerSpec("shared", "gameplay", point)),
            nearbyPoiMarkers = listOf(MapMarkerSpec("shared", "nearby", GeoPoint(37.5700, 126.9800))),
            fixtureMarkers = emptyList(),
        )

        assertEquals(1, markers.size)
        assertEquals("gameplay", markers.single().title)
    }
}
