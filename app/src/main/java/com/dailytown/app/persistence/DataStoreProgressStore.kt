package com.dailytown.app.persistence

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dailyTownDataStore by preferencesDataStore(name = "daily_town_progress")

class DataStoreProgressStore(context: Context) : ProgressStore {
    private val dataStore = context.applicationContext.dailyTownDataStore

    override suspend fun load(): ExplorationProgress =
        ProgressPreferencesCodec.decode(dataStore.data.first())

    override suspend fun save(progress: ExplorationProgress) {
        dataStore.edit { prefs ->
            ProgressPreferencesCodec.encode(prefs, progress)
        }
    }
}
