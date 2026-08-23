package com.dailytown.app.map

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.dailytown.app.domain.GeoPoint
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.overlay.Marker

/**
 * NAVER-specific rendering adapter. Nothing outside this class needs NAVER SDK types.
 * When credentials are missing, it renders a safe placeholder while replay/location/domain flows stay testable.
 */
class NaverMapAdapter(
    private val ncpKeyId: String,
) : MapViewAdapter {
    override val providerId = MapProviderId.NAVER

    private var mapView: MapView? = null
    private var naverMap: NaverMap? = null
    private var pendingCamera: Pair<GeoPoint, Double>? = null
    private var pendingMarkers: List<MapMarkerSpec> = emptyList()
    private var pendingLocation: UserLocationSpec? = null
    private val renderedMarkers = mutableMapOf<String, Marker>()

    private val isConfigured: Boolean
        get() = ncpKeyId.isNotBlank() && !ncpKeyId.startsWith("TODO_")

    override fun createView(context: Context): View {
        if (!isConfigured) {
            return FrameLayout(context).apply {
                addView(
                    TextView(context).apply {
                        text = "NAVER Maps 준비 완료\nTODO: NAVER_MAP_NCP_KEY_ID 설정"
                        gravity = Gravity.CENTER
                    },
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    ),
                )
            }
        }

        NaverMapSdk.getInstance(context).client = NaverMapSdk.NcpKeyClient(ncpKeyId)
        return MapView(context).also { view ->
            mapView = view
            view.onCreate(null)
            view.getMapAsync { map ->
                naverMap = map
                flushState()
            }
        }
    }

    override fun setCamera(target: GeoPoint, zoom: Double) {
        pendingCamera = target to zoom
        naverMap?.moveCamera(CameraUpdate.scrollAndZoomTo(target.toLatLng(), zoom))
    }

    override fun setMarkers(markers: List<MapMarkerSpec>) {
        pendingMarkers = markers
        syncMarkers()
    }

    override fun setUserLocation(location: UserLocationSpec?) {
        pendingLocation = location
        syncUserLocation()
    }

    override fun onStart() { mapView?.onStart() }
    override fun onResume() { mapView?.onResume() }
    override fun onPause() { mapView?.onPause() }
    override fun onStop() { mapView?.onStop() }
    override fun onLowMemory() { mapView?.onLowMemory() }

    override fun onDestroy() {
        renderedMarkers.values.forEach { it.map = null }
        renderedMarkers.clear()
        mapView?.onDestroy()
        mapView = null
        naverMap = null
    }

    private fun flushState() {
        pendingCamera?.let { (target, zoom) -> setCamera(target, zoom) }
        syncMarkers()
        syncUserLocation()
    }

    private fun syncMarkers() {
        val map = naverMap ?: return
        val desiredIds = pendingMarkers.mapTo(mutableSetOf()) { it.id }
        renderedMarkers.keys.filterNot { it in desiredIds }.forEach { id ->
            renderedMarkers.remove(id)?.map = null
        }
        pendingMarkers.forEach { spec ->
            val marker = renderedMarkers.getOrPut(spec.id) { Marker() }
            marker.position = spec.position.toLatLng()
            marker.captionText = spec.title
            marker.map = map
        }
    }

    private fun syncUserLocation() {
        val map = naverMap ?: return
        val overlay = map.locationOverlay
        val location = pendingLocation
        overlay.isVisible = location != null
        if (location != null) {
            overlay.position = location.position.toLatLng()
            location.bearingDegrees?.let { overlay.bearing = it.coerceIn(0f, 360f) }
        }
    }

    private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)
}
