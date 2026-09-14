package com.dailytown.app.map

import android.content.Context
import android.view.View
import com.dailytown.app.domain.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
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
    fun userLocationListenerReceivesAcceptedNonNullPositionsOnly() {
        val delegate = RecordingMapAdapter()
        val adapter = FixturePoiOverlayMapAdapter(delegate, emptyList())
        val received = mutableListOf<GeoPoint>()
        val point = GeoPoint(37.5665, 126.9780)
        adapter.setUserLocationListener(received::add)

        adapter.setUserLocation(UserLocationSpec(point))
        adapter.setUserLocation(null)

        assertEquals(listOf(point), received)
    }

    private class RecordingMapAdapter : MapViewAdapter {
        override val providerId: MapProviderId = MapProviderId.NAVER
        private val healthFlow = MutableStateFlow(MapHealth(MapHealthStatus.READY))
        override val health: StateFlow<MapHealth> = healthFlow
        val cameraTargets = mutableListOf<GeoPoint>()

        override fun createView(context: Context): View = error("not used by this unit test")

        override fun setCamera(target: GeoPoint, zoom: Double) {
            cameraTargets += target
        }

        override fun setMarkers(markers: List<MapMarkerSpec>) = Unit
        override fun setUserLocation(location: UserLocationSpec?) = Unit
    }
}
