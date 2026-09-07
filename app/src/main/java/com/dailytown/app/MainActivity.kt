package com.dailytown.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import com.dailytown.app.location.FusedDeviceLocationSource
import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.map.FixturePoiOverlayMapAdapter
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.NaverMapAdapter
import com.dailytown.app.map.UserLocationSpec
import com.dailytown.app.persistence.DataStoreProgressStore
import com.dailytown.app.poi.MapPublishingPoiRepository
import com.dailytown.app.poi.ProductionPoiRepositoryFactory
import com.dailytown.app.poi.defaultFixturePois
import com.dailytown.app.reminder.LocalReminderManager
import com.dailytown.app.ui.DailyTownMvpShell
import com.dailytown.app.ui.visual.AndroidProductionMarkerAssetCatalog
import com.dailytown.app.ui.visual.MapThemeRefreshController
import com.dailytown.app.ui.visual.ProductionMarkerSvgVisualSource

class MainActivity : ComponentActivity() {
    private lateinit var mapThemeRefreshController: MapThemeRefreshController
    private lateinit var mapAdapter: FixturePoiOverlayMapAdapter
    private var initialLocationSource: FusedDeviceLocationSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val markerVisualSource = ProductionMarkerSvgVisualSource(
            AndroidProductionMarkerAssetCatalog(assets),
        )
        val providerMapAdapter = NaverMapAdapter(
            ncpKeyId = BuildConfig.NAVER_MAP_NCP_KEY_ID,
            markerVisualSource = markerVisualSource,
        )
        mapAdapter = FixturePoiOverlayMapAdapter(
            delegate = providerMapAdapter,
            fixtureMarkers = defaultFixturePois().map { poi ->
                MapMarkerSpec(
                    id = "fixture:${poi.id}",
                    title = poi.name,
                    position = poi.position,
                )
            },
            // Internal/debug builds intentionally retain known Seoul/Jungwon anchors so one APK can
            // validate the production POI feed and the repeatable physical field-test route together.
            // Release builds never expose these fixed fixtures.
            showFixtureMarkers = BuildConfig.DEBUG,
            suppressLegacyDemoMarkers = true,
        )
        mapThemeRefreshController = MapThemeRefreshController(mapAdapter)
        val progressStore = DataStoreProgressStore(applicationContext)
        val basePoiRepository = ProductionPoiRepositoryFactory.create(
            tourApiServiceKey = BuildConfig.TOUR_API_SERVICE_KEY.takeIf { BuildConfig.TOUR_API_CONFIGURED },
            allowFixtureFallback = BuildConfig.DEBUG,
        )
        val poiRepository = MapPublishingPoiRepository(
            delegate = basePoiRepository,
            publish = mapAdapter::setNearbyPoiMarkers,
        )
        val reminderManager = LocalReminderManager(applicationContext).also { it.restoreIfEnabled() }
        setContent {
            DailyTownMvpShell(
                mapAdapter = mapAdapter,
                progressStore = progressStore,
                poiRepository = poiRepository,
                reminderManager = reminderManager,
            )
        }
        centerInitialMapIfPermissionAlreadyGranted()
    }

    override fun onResume() {
        super.onResume()
        mapThemeRefreshController.start()
    }

    override fun onPause() {
        mapThemeRefreshController.stop()
        super.onPause()
    }

    override fun onDestroy() {
        initialLocationSource?.stop()
        initialLocationSource = null
        mapThemeRefreshController.close()
        super.onDestroy()
    }

    /**
     * Cold start must never steal the app surface with a system permission dialog.
     * If location permission already exists we may center once; otherwise the explicit
     * "실제 위치" action inside DailyTownApp owns the permission request and tracking start.
     */
    private fun centerInitialMapIfPermissionAlreadyGranted() {
        if (hasLocationPermission()) {
            centerMapOnCurrentLocationOnce()
        }
    }

    private fun centerMapOnCurrentLocationOnce() {
        initialLocationSource?.stop()
        val source = FusedDeviceLocationSource(
            context = applicationContext,
            config = LocationTrackingPreset.BALANCED.config,
        )
        initialLocationSource = source
        var consumed = false
        source.start(
            onLocation = onLocation@{ sample ->
                if (consumed) return@onLocation
                consumed = true
                mapAdapter.setUserLocation(UserLocationSpec(sample.point, sample.bearingDegrees))
                mapAdapter.recenter(sample.point)
                source.stop()
                if (initialLocationSource === source) {
                    initialLocationSource = null
                }
            },
            onError = {
                source.stop()
                if (initialLocationSource === source) {
                    initialLocationSource = null
                }
            },
        )
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
