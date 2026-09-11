package com.dailytown.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dailytown.app.location.FusedDeviceLocationSource
import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.map.FixturePoiOverlayMapAdapter
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.NaverMapAdapter
import com.dailytown.app.map.UserLocationSpec
import com.dailytown.app.persistence.DataStoreProgressStore
import com.dailytown.app.poi.NearbyPoiCoordinator
import com.dailytown.app.poi.NearbyPoiMarkerPublisher
import com.dailytown.app.poi.ProductionPoiRepositoryFactory
import com.dailytown.app.poi.defaultFixturePois
import com.dailytown.app.reminder.LocalReminderManager
import com.dailytown.app.ui.DailyTownMvpShell
import com.dailytown.app.ui.visual.AndroidProductionMarkerAssetCatalog
import com.dailytown.app.ui.visual.MapThemeRefreshController
import com.dailytown.app.ui.visual.ProductionMarkerSvgVisualSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var mapThemeRefreshController: MapThemeRefreshController
    private lateinit var mapAdapter: FixturePoiOverlayMapAdapter
    private var initialLocationSource: FusedDeviceLocationSource? = null
    private var poiRefreshJob: Job? = null

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
            fixtureMarkers = if (BuildVariantPolicy.showFixturePoiMarkers) {
                defaultFixturePois().map { poi ->
                    MapMarkerSpec(
                        id = "fixture:${poi.id}",
                        title = poi.name,
                        position = poi.position,
                    )
                }
            } else {
                emptyList()
            },
            // Build-variant policy, rather than scattered DEBUG checks, owns whether fixed field-test
            // anchors are visible. Release selects the fail-closed policy from src/release.
            showFixtureMarkers = BuildVariantPolicy.showFixturePoiMarkers,
            suppressLegacyDemoMarkers = true,
        )
        mapThemeRefreshController = MapThemeRefreshController(mapAdapter)
        val progressStore = DataStoreProgressStore(applicationContext)
        val basePoiRepository = ProductionPoiRepositoryFactory.create(
            // Direct upstream credentials are an internal/debug diagnostic path only. Release may
            // consume production POIs only through the app-owned HTTPS gateway.
            tourApiServiceKey = BuildConfig.TOUR_API_SERVICE_KEY.takeIf {
                BuildVariantPolicy.allowDirectPoiProvider && BuildConfig.TOUR_API_CONFIGURED
            },
            proxyBaseUrl = BuildConfig.DAILYTOWN_POI_API_BASE_URL.takeIf { BuildConfig.DAILYTOWN_POI_API_CONFIGURED },
            allowFixtureFallback = BuildVariantPolicy.allowFixturePoiFallback,
        )
        val poiCoordinator = NearbyPoiCoordinator(basePoiRepository)
        val poiMarkerPublisher = NearbyPoiMarkerPublisher(mapAdapter::setNearbyPoiMarkers)
        lifecycleScope.launch {
            poiCoordinator.snapshots.collect { snapshot ->
                if (snapshot != null) {
                    poiMarkerPublisher.publish(snapshot.center, snapshot.pois)
                }
            }
        }

        // Keep the visual POI feed independent from encounter lifetime. Both this map refresh and
        // encounter selection share one NearbyPoiCoordinator, which serializes mutable cache/provider
        // access and coalesces an identical request that completed while another caller was waiting.
        mapAdapter.setUserLocationListener { point ->
            if (poiRefreshJob?.isActive == true) return@setUserLocationListener
            poiRefreshJob = lifecycleScope.launch {
                poiCoordinator.nearby(point, radiusMeters = 900.0)
            }
        }
        val reminderManager = LocalReminderManager(applicationContext).also { it.restoreIfEnabled() }
        setContent {
            DailyTownMvpShell(
                mapAdapter = mapAdapter,
                progressStore = progressStore,
                poiRepository = poiCoordinator,
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
        poiRefreshJob?.cancel()
        poiRefreshJob = null
        mapAdapter.setUserLocationListener(null)
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
