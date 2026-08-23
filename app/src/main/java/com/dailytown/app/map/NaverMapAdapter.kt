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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * NAVER-specific rendering adapter. Nothing outside this class needs NAVER SDK types.
 * When credentials are missing, it renders a safe placeholder while replay/location/domain flows stay testable.
 * Authentication failures are translated into safe provider-neutral health diagnostics without exposing the NCP key ID.
 */
class NaverMapAdapter(
    private val ncpKeyId: String,
) : MapViewAdapter {
    override val providerId = MapProviderId.NAVER

    private val _health = MutableStateFlow(
        MapHealth(
            status = if (isConfiguredValue(ncpKeyId)) MapHealthStatus.INITIALIZING else MapHealthStatus.UNCONFIGURED,
        ),
    )
    override val health: StateFlow<MapHealth> = _health

    private var mapView: MapView? = null
    private var naverMap: NaverMap? = null
    private var naverMapSdk: NaverMapSdk? = null
    private var authFailedListener: NaverMapSdk.OnAuthFailedListener? = null
    private var pendingCamera: Pair<GeoPoint, Double>? = null
    private var pendingMarkers: List<MapMarkerSpec> = emptyList()
    private var pendingLocation: UserLocationSpec? = null
    private val renderedMarkers = mutableMapOf<String, Marker>()

    private val isConfigured: Boolean
        get() = isConfiguredValue(ncpKeyId)

    override fun createView(context: Context): View {
        if (!isConfigured) {
            _health.value = MapHealth(
                status = MapHealthStatus.UNCONFIGURED,
                userMessage = "NAVER Maps credential is not configured.",
            )
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

        _health.value = MapHealth(MapHealthStatus.INITIALIZING)
        val container = FrameLayout(context)
        val view = MapView(context)
        val errorView = TextView(context).apply {
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(24, 16, 24, 16)
        }
        container.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        container.addView(
            errorView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP,
            ),
        )

        try {
            val sdk = NaverMapSdk.getInstance(context)
            val listener = NaverMapSdk.OnAuthFailedListener { exception ->
                val message = authFailureMessage(exception)
                _health.value = MapHealth(
                    status = MapHealthStatus.AUTH_ERROR,
                    errorCode = exception.errorCode,
                    userMessage = message,
                )
                errorView.text = message
                errorView.visibility = View.VISIBLE
            }
            naverMapSdk = sdk
            authFailedListener = listener
            sdk.client = NaverMapSdk.NcpKeyClient(ncpKeyId)
            sdk.onAuthFailedListener = listener

            mapView = view
            view.onCreate(null)
            view.getMapAsync { map ->
                naverMap = map
                _health.value = MapHealth(MapHealthStatus.READY)
                errorView.visibility = View.GONE
                flushState()
            }
        } catch (error: Throwable) {
            _health.value = MapHealth(
                status = MapHealthStatus.ERROR,
                errorCode = "initialization",
                userMessage = "NAVER Maps 초기화에 실패했습니다.",
            )
            errorView.text = "NAVER Maps 초기화에 실패했습니다."
            errorView.visibility = View.VISIBLE
        }
        return container
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
        val sdk = naverMapSdk
        val listener = authFailedListener
        if (sdk != null && listener != null && sdk.onAuthFailedListener === listener) {
            sdk.onAuthFailedListener = null
        }
        mapView?.onDestroy()
        mapView = null
        naverMap = null
        naverMapSdk = null
        authFailedListener = null
        _health.value = MapHealth(MapHealthStatus.DESTROYED)
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

    private fun authFailureMessage(exception: NaverMapSdk.AuthFailedException): String = when (exception.errorCode) {
        "401" -> "NAVER Maps 인증 실패 (401)\nNCP Key ID와 Android 패키지 등록을 확인하세요."
        "429" -> "NAVER Maps 사용 불가 (429)\nDynamic Map 선택 여부와 사용량 한도를 확인하세요."
        "800" -> "NAVER Maps 인증 정보 없음 (800)\nNAVER_MAP_NCP_KEY_ID 주입 상태를 확인하세요."
        else -> "NAVER Maps 인증 오류 (${exception.errorCode})\nNAVER Cloud Maps 설정을 확인하세요."
    }

    private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)

    private companion object {
        fun isConfiguredValue(value: String): Boolean =
            value.isNotBlank() && !value.startsWith("TODO_")
    }
}
