package com.dailytown.app.map

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.visual.MapOverlaySemanticState
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.MarkerSemantic
import com.dailytown.app.visual.VisualArgb
import kotlinx.coroutines.flow.StateFlow

enum class MapProviderId { NAVER, GOOGLE }
enum class MapBrightnessFamily { LIGHT, DARK }

enum class MapHealthStatus {
    UNCONFIGURED,
    INITIALIZING,
    READY,
    AUTH_ERROR,
    ERROR,
    DESTROYED,
}

/**
 * Provider-neutral health signal for UI and field-test diagnostics.
 * `errorCode` is provider-safe metadata only; credentials and raw provider exceptions never cross this boundary.
 */
data class MapHealth(
    val status: MapHealthStatus,
    val errorCode: String? = null,
    val userMessage: String? = null,
)

data class MapThemeSpec(
    val preferredBrightness: MapBrightnessFamily = MapBrightnessFamily.LIGHT,
    val markerFamily: MarkerFamily = MarkerFamily.DAY,
    val routeColor: VisualArgb? = null,
)

data class MapMarkerSpec(
    val id: String,
    val title: String,
    val position: GeoPoint,
    val semantic: MarkerSemantic = MarkerSemantic.POI_OTHER,
    val selected: Boolean = false,
)

data class MapMarkerBitmap(
    val bitmap: Bitmap,
    val anchorX: Float,
    val anchorY: Float,
)

/** Android-owned, provider-neutral bridge from semantic marker intent to renderable pixels. */
fun interface MapMarkerVisualSource {
    fun resolve(marker: MapMarkerSpec, theme: MapThemeSpec): MapMarkerBitmap?
}

enum class MapAreaEmphasis {
    /** Signal detected: where the player must walk to discover it. */
    DISCOVERY_ZONE,
    /** Discovered and being investigated. */
    ACTIVE,
}

/**
 * Geographic area tied to real encounter rules (centre = POI, radius = the domain discovery radius).
 * It is gameplay geometry, not decoration, so it never depends on unapproved route/effect inputs.
 */
data class MapEncounterAreaSpec(
    val id: String,
    val center: GeoPoint,
    val radiusMeters: Double,
    val emphasis: MapAreaEmphasis,
) {
    init {
        require(radiusMeters > 0.0)
    }
}

data class UserLocationSpec(
    val position: GeoPoint,
    val bearingDegrees: Float? = null,
)

/** Provider-neutral contract consumed by UI/application code. */
interface MapViewAdapter {
    val providerId: MapProviderId
    val health: StateFlow<MapHealth>
    fun createView(context: Context): View
    fun setCamera(target: GeoPoint, zoom: Double = 16.0)
    fun setTheme(theme: MapThemeSpec) = Unit
    fun setMarkers(markers: List<MapMarkerSpec>)
    /**
     * Semantic live-overlay state. Provider adapters may render only dimensions backed by an
     * approved implementation. A default no-op keeps providers swappable while route/halo/effect
     * rendering is integrated independently from gameplay/domain state.
     */
    fun setOverlayState(state: MapOverlaySemanticState) = Unit
    fun setUserLocation(location: UserLocationSpec?)
    /** Draws (or clears with null) the active encounter's discovery area. */
    fun setEncounterArea(area: MapEncounterAreaSpec?) = Unit
    /** Receives the spec of a tapped marker; null removes the listener. */
    fun setMarkerClickListener(listener: ((MapMarkerSpec) -> Unit)?) = Unit
    fun onStart() = Unit
    fun onResume() = Unit
    fun onPause() = Unit
    fun onStop() = Unit
    fun onDestroy() = Unit
    fun onLowMemory() = Unit
}
