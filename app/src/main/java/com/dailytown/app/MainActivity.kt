package com.dailytown.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dailytown.app.map.NaverMapAdapter
import com.dailytown.app.persistence.DataStoreProgressStore
import com.dailytown.app.poi.FixturePoiRepository
import com.dailytown.app.reminder.LocalReminderManager
import com.dailytown.app.ui.DailyTownApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mapAdapter = NaverMapAdapter(BuildConfig.NAVER_MAP_NCP_KEY_ID)
        val progressStore = DataStoreProgressStore(applicationContext)
        val poiRepository = FixturePoiRepository()
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
}
