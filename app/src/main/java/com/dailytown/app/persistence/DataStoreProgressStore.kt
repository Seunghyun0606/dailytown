package com.dailytown.app.persistence

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dailyTownDataStore by preferencesDataStore(name = "daily_town_progress")

class DataStoreProgressStore(context: Context) : ProgressStore {
    private val dataStore = context.applicationContext.dailyTownDataStore

    override suspend fun load(): ExplorationProgress {
        val prefs = dataStore.data.first()
        return ExplorationProgress(
            visitedSpotIds = prefs[Keys.visitedSpotIds].orEmpty(),
            distanceWalkedMeters = prefs[Keys.distanceMeters] ?: 0.0,
            cluesFound = prefs[Keys.cluesFound] ?: 0,
            companionBond = prefs[Keys.companionBond] ?: 0,
        )
    }

    override suspend fun save(progress: ExplorationProgress) {
        dataStore.edit { prefs ->
            prefs[Keys.visitedSpotIds] = progress.visitedSpotIds
            prefs[Keys.distanceMeters] = progress.distanceWalkedMeters
            prefs[Keys.cluesFound] = progress.cluesFound
            prefs[Keys.companionBond] = progress.companionBond
        }
    }

    private object Keys {
        val visitedSpotIds = stringSetPreferencesKey("visited_spot_ids")
        val distanceMeters = doublePreferencesKey("distance_walked_meters")
        val cluesFound = intPreferencesKey("clues_found")
        val companionBond = intPreferencesKey("companion_bond")
    }
}
