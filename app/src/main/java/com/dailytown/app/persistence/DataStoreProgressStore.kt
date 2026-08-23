package com.dailytown.app.persistence

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dailyTownDataStore by preferencesDataStore(name = "daily_town_progress")
private const val LIST_SEPARATOR = "\u001F"

class DataStoreProgressStore(context: Context) : ProgressStore {
    private val dataStore = context.applicationContext.dailyTownDataStore

    override suspend fun load(): ExplorationProgress {
        val prefs = dataStore.data.first()
        return ExplorationProgress(
            visitedSpotIds = prefs[Keys.visitedSpotIds].orEmpty(),
            distanceWalkedMeters = prefs[Keys.distanceMeters] ?: 0.0,
            cluesFound = prefs[Keys.cluesFound] ?: 0,
            companionBond = prefs[Keys.companionBond] ?: 0,
            inventoryClueIds = prefs[Keys.inventoryClueIds].orEmpty(),
            resolvedEncounterIds = prefs[Keys.resolvedEncounterIds].orEmpty(),
            encounterVisitedPoiIds = prefs[Keys.encounterVisitedPoiIds].orEmpty(),
            recentPoiIds = decodeList(prefs[Keys.recentPoiIds]),
            recentTemplateIds = decodeList(prefs[Keys.recentTemplateIds]),
            recentPairKeys = decodeList(prefs[Keys.recentPairKeys]),
            daily = PeriodProgress(
                periodKey = prefs[Keys.dailyPeriodKey].orEmpty(),
                distanceWalkedMeters = prefs[Keys.dailyDistanceMeters] ?: 0.0,
                discoveredPoiIds = prefs[Keys.dailyDiscoveredPoiIds].orEmpty(),
                clueIds = prefs[Keys.dailyClueIds].orEmpty(),
                resolvedEncounterIds = prefs[Keys.dailyResolvedEncounterIds].orEmpty(),
            ),
            weekly = PeriodProgress(
                periodKey = prefs[Keys.weeklyPeriodKey].orEmpty(),
                distanceWalkedMeters = prefs[Keys.weeklyDistanceMeters] ?: 0.0,
                discoveredPoiIds = prefs[Keys.weeklyDiscoveredPoiIds].orEmpty(),
                clueIds = prefs[Keys.weeklyClueIds].orEmpty(),
                resolvedEncounterIds = prefs[Keys.weeklyResolvedEncounterIds].orEmpty(),
            ),
        )
    }

    override suspend fun save(progress: ExplorationProgress) {
        dataStore.edit { prefs ->
            prefs[Keys.visitedSpotIds] = progress.visitedSpotIds
            prefs[Keys.distanceMeters] = progress.distanceWalkedMeters
            prefs[Keys.cluesFound] = progress.cluesFound
            prefs[Keys.companionBond] = progress.companionBond
            prefs[Keys.inventoryClueIds] = progress.inventoryClueIds
            prefs[Keys.resolvedEncounterIds] = progress.resolvedEncounterIds
            prefs[Keys.encounterVisitedPoiIds] = progress.encounterVisitedPoiIds
            prefs[Keys.recentPoiIds] = encodeList(progress.recentPoiIds)
            prefs[Keys.recentTemplateIds] = encodeList(progress.recentTemplateIds)
            prefs[Keys.recentPairKeys] = encodeList(progress.recentPairKeys)
            writePeriod(prefs, progress.daily, daily = true)
            writePeriod(prefs, progress.weekly, daily = false)
        }
    }

    private fun writePeriod(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        period: PeriodProgress,
        daily: Boolean,
    ) {
        if (daily) {
            prefs[Keys.dailyPeriodKey] = period.periodKey
            prefs[Keys.dailyDistanceMeters] = period.distanceWalkedMeters
            prefs[Keys.dailyDiscoveredPoiIds] = period.discoveredPoiIds
            prefs[Keys.dailyClueIds] = period.clueIds
            prefs[Keys.dailyResolvedEncounterIds] = period.resolvedEncounterIds
        } else {
            prefs[Keys.weeklyPeriodKey] = period.periodKey
            prefs[Keys.weeklyDistanceMeters] = period.distanceWalkedMeters
            prefs[Keys.weeklyDiscoveredPoiIds] = period.discoveredPoiIds
            prefs[Keys.weeklyClueIds] = period.clueIds
            prefs[Keys.weeklyResolvedEncounterIds] = period.resolvedEncounterIds
        }
    }

    private fun encodeList(items: List<String>): String = items.joinToString(LIST_SEPARATOR)
    private fun decodeList(value: String?): List<String> =
        value?.takeIf { it.isNotBlank() }?.split(LIST_SEPARATOR).orEmpty()

    private object Keys {
        val visitedSpotIds = stringSetPreferencesKey("visited_spot_ids")
        val distanceMeters = doublePreferencesKey("distance_walked_meters")
        val cluesFound = intPreferencesKey("clues_found")
        val companionBond = intPreferencesKey("companion_bond")
        val inventoryClueIds = stringSetPreferencesKey("inventory_clue_ids")
        val resolvedEncounterIds = stringSetPreferencesKey("resolved_encounter_ids")
        val encounterVisitedPoiIds = stringSetPreferencesKey("encounter_visited_poi_ids")
        val recentPoiIds = stringPreferencesKey("recent_poi_ids")
        val recentTemplateIds = stringPreferencesKey("recent_template_ids")
        val recentPairKeys = stringPreferencesKey("recent_pair_keys")

        val dailyPeriodKey = stringPreferencesKey("daily_period_key")
        val dailyDistanceMeters = doublePreferencesKey("daily_distance_meters")
        val dailyDiscoveredPoiIds = stringSetPreferencesKey("daily_discovered_poi_ids")
        val dailyClueIds = stringSetPreferencesKey("daily_clue_ids")
        val dailyResolvedEncounterIds = stringSetPreferencesKey("daily_resolved_encounter_ids")

        val weeklyPeriodKey = stringPreferencesKey("weekly_period_key")
        val weeklyDistanceMeters = doublePreferencesKey("weekly_distance_meters")
        val weeklyDiscoveredPoiIds = stringSetPreferencesKey("weekly_discovered_poi_ids")
        val weeklyClueIds = stringSetPreferencesKey("weekly_clue_ids")
        val weeklyResolvedEncounterIds = stringSetPreferencesKey("weekly_resolved_encounter_ids")
    }
}
