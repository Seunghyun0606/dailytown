package com.dailytown.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dailytown.app.map.NaverMapAdapter
import com.dailytown.app.persistence.DataStoreProgressStore
import com.dailytown.app.poi.CachingPoiRepository
import com.dailytown.app.poi.FixturePoiRepository
import com.dailytown.app.reminder.LocalReminderManager
import com.dailytown.app.ui.DailyTownApp
import com.dailytown.app.ui.visual.AndroidProductionMarkerAssetCatalog
import com.dailytown.app.ui.visual.MapThemeRefreshController
import com.dailytown.app.ui.visual.ProductionMarkerSvgVisualSource

class MainActivity : ComponentActivity() {
    private lateinit var mapThemeRefreshController: MapThemeRefreshController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val markerVisualSource = ProductionMarkerSvgVisualSource(
            AndroidProductionMarkerAssetCatalog(assets),
        )
        val mapAdapter = NaverMapAdapter(
            ncpKeyId = BuildConfig.NAVER_MAP_NCP_KEY_ID,
            markerVisualSource = markerVisualSource,
        )
        mapThemeRefreshController = MapThemeRefreshController(mapAdapter)
        val progressStore = DataStoreProgressStore(applicationContext)
        val poiRepository = CachingPoiRepository(FixturePoiRepository())
        val reminderManager = LocalReminderManager(applicationContext).also { it.restoreIfEnabled() }
        setContent {
            DailyTownApp(
                mapAdapter = mapAdapter,
                progressStore = progressStore,
                poiRepository = poiRepository,
                reminderManager = reminderManager,
            )
        }
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
        mapThemeRefreshController.close()
        super.onDestroy()
    }
}
