package com.dailytown.app.map

import android.content.Context
import android.view.View
import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FixturePoiOverlayMapAdapterTest {
    @Test
    fun recenterMovesImmediatelyThenAllowsExactlyOneFreshSessionFollow() {
        val delegate = RecordingMapAdapter()
        val adapter = FixturePoiOverlayMapAdapter(delegate, emptyList())
        val initial = GeoPoint(37.4100, 127.1200)
        val firstFresh = GeoPoint(37.4110, 127.1210)
        val secondFresh = GeoPoint(37.4120, 127.1220)

        adapter.recenter(initial)
        adapter.setCamera(firstFresh)
        adapter.setCamera(secondFresh)

        assertEquals(listOf(initial, firstFresh), delegate.cameraTargets)
    }

    @Test
    fun clearingUserLocationRearmsOneCameraFollow() {
        val delegate = RecordingMapAdapter()
        val adapter = FixturePoiOverlayMapAdapter(delegate, emptyList())
        val first = GeoPoint(37.4100, 127.1200)
        val ignored = GeoPoint(37.4110, 127.1210)
        val nextSession = GeoPoint(37.4120, 127.1220)

        adapter.setCamera(first)
        adapter.setCamera(ignored)
        adapter.setUserLocation(null)
        adapter.setCamera(nextSession)

        assertEquals(listOf(first, nextSession), delegate.cameraTargets)
    }

    @Test
    fun productionNearbyMarkerWinsOverMatchingFieldTestFixture() {
        val delegate = RecordingMapAdapter()
        val point = GeoPoint(37.5665, 126.9780)
        val adapter = FixturePoiOverlayMapAdapter(
            delegate = delegate,
            fixtureMarkers = listOf(MapMarkerSpec("fixture:cityhall", "서울시청", point)),
            showFixtureMarkers = true,
        )

        adapter.setNearbyPoiMarkers(
            listOf(MapMarkerSpec("poi:tourapi:cityhall", "서울시청", GeoPoint(37.56651, 126.97801))),
        )
        adapter.setMarkers(emptyList())

        assertEquals(listOf("poi:tourapi:cityhall"), delegate.latestMarkers.map { it.id })
    }

    @Test
    fun legacyDemoMarkersAreSuppressedButActiveEncounterRemains() {
        val delegate = RecordingMapAdapter()
        val adapter = FixturePoiOverlayMapAdapter(delegate, emptyList())
        val legacy = MapMarkerSpec("cityhall-echo", "legacy", GeoPoint(37.5665, 126.9780))
        val active = MapMarkerSpec(
            id = "active:test",
            title = "active",
            position = GeoPoint(37.5700, 126.9800),
            selected = true,
        )

        adapter.setMarkers(listOf(legacy, active))

        assertFalse(delegate.latestMarkers.any { it.id == legacy.id })
        assertTrue(delegate.latestMarkers.any { it.id == active.id })
    }

    @Test
    fun activeEncounterSuppressesUnderlyingNearbyPoiMarker() {
        val delegate = RecordingMapAdapter()
        val point = GeoPoint(37.5665, 126.9780)
        val adapter = FixturePoiOverlayMapAdapter(delegate, emptyList())
        adapter.setNearbyPoiMarkers(listOf(MapMarkerSpec("poi:one", "one", point)))

        adapter.setMarkers(
            listOf(
                MapMarkerSpec(
                    id = "active:one",
                    title = "active",
                    position = point,
                    selected = true,
                ),
            ),
        )

        assertEquals(listOf("active:one"), delegate.latestMarkers.map { it.id })
    }

    private class RecordingMapAdapter : MapViewAdapter {
        override val providerId: MapProviderId = MapProviderId.NAVER
        private val healthFlow = MutableStateFlow(MapHealth(MapHealthStatus.READY))
        override val health: StateFlow<MapHealth> = healthFlow
        val cameraTargets = mutableListOf<GeoPoint>()
        var latestMarkers: List<MapMarkerSpec> = emptyList()

        override fun createView(context: Context): View = error("not used by this unit test")

        override fun setCamera(target: GeoPoint, zoom: Double) {
            cameraTargets += target
        }

        override fun setMarkers(markers: List<MapMarkerSpec>) {
            latestMarkers = markers
        }

        override fun setUserLocation(location: UserLocationSpec?) = Unit
    }
}
