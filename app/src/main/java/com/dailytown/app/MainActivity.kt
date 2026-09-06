package com.dailytown.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.dailytown.app.location.FusedDeviceLocationSource
import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.map.FixturePoiOverlayMapAdapter
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.NaverMapAdapter
import com.dailytown.app.map.UserLocationSpec
import com.dailytown.app.persistence.DataStoreProgressStore
import com.dailytown.app.poi.CachingPoiRepository
import com.dailytown.app.poi.FixturePoiRepository
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

    private val initialLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted && ::mapAdapter.isInitialized) {
            centerMapOnCurrentLocationOnce()
        }
    }

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
                    id = poi.id,
                    title = poi.name,
                    position = poi.position,
                )
            },
        )
        mapThemeRefreshController = MapThemeRefreshController(mapAdapter)
        val progressStore = DataStoreProgressStore(applicationContext)
        val poiRepository = CachingPoiRepository(FixturePoiRepository())
        val reminderManager = LocalReminderManager(applicationContext).also { it.restoreIfEnabled() }
        setContent {
            DailyTownMvpShell(
                mapAdapter = mapAdapter,
                progressStore = progressStore,
                poiRepository = poiRepository,
                reminderManager = reminderManager,
            )
        }
        requestInitialMapLocation()
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

    private fun requestInitialMapLocation() {
        if (hasLocationPermission()) {
            centerMapOnCurrentLocationOnce()
        } else {
            initialLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
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
