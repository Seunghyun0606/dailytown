package com.dailytown.app.persistence

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

private const val LIST_SEPARATOR = "\u001F"

/**
 * Stable persistence boundary between DataStore preference keys and the domain progress aggregate.
 *
 * Schema 0 is the legacy key set written before an explicit version existed. Schema 1 preserves
 * every legacy key exactly and only adds the version marker, so existing installs decode without a
 * destructive migration. Unknown future schemas fail closed instead of being silently misread.
 */
internal object ProgressPreferencesCodec {
    const val CURRENT_SCHEMA_VERSION: Int = 1

    fun decode(prefs: Preferences): ExplorationProgress {
        val schemaVersion = prefs[Keys.schemaVersion] ?: 0
        require(schemaVersion in 0..CURRENT_SCHEMA_VERSION) {
            "Unsupported Daily Town progress schemaVersion=$schemaVersion"
        }
        return ExplorationProgress(
            visitedSpotIds = prefs[Keys.visitedSpotIds].orEmpty(),
            distanceWalkedMeters = prefs[Keys.distanceMeters] ?: 0.0,
            cluesFound = prefs[Keys.cluesFound] ?: 0,
            companionBond = prefs[Keys.companionBond] ?: 0,
            inventoryClueIds = prefs[Keys.inventoryClueIds].orEmpty(),
            resolvedEncounterIds = prefs[Keys.resolvedEncounterIds].orEmpty(),
            encounterVisitedPoiIds = prefs[Keys.encounterVisitedPoiIds].orEmpty(),
            recentPoiIds = decodeList(prefs[Keys.recentPoiIds]),
            recentPoiTitles = decodeList(prefs[Keys.recentPoiTitles]),
            recentTemplateIds = decodeList(prefs[Keys.recentTemplateIds]),
            recentPairKeys = decodeList(prefs[Keys.recentPairKeys]),
            companionMemoryKeys = prefs[Keys.companionMemoryKeys].orEmpty(),
            daily = readPeriod(prefs, daily = true),
            weekly = readPeriod(prefs, daily = false),
            dailyGoalPeriodKey = prefs[Keys.dailyGoalPeriodKey].orEmpty(),
            dailyGoalIds = decodeList(prefs[Keys.dailyGoalIds]),
            recentDailyGoalIds = decodeList(prefs[Keys.recentDailyGoalIds]),
            weeklyGoalPeriodKey = prefs[Keys.weeklyGoalPeriodKey].orEmpty(),
            weeklyGoalIds = decodeList(prefs[Keys.weeklyGoalIds]),
            recentWeeklyGoalIds = decodeList(prefs[Keys.recentWeeklyGoalIds]),
        )
    }

    fun encode(prefs: MutablePreferences, progress: ExplorationProgress) {
        prefs[Keys.schemaVersion] = CURRENT_SCHEMA_VERSION
        prefs[Keys.visitedSpotIds] = progress.visitedSpotIds
        prefs[Keys.distanceMeters] = progress.distanceWalkedMeters
        prefs[Keys.cluesFound] = progress.cluesFound
        prefs[Keys.companionBond] = progress.companionBond
        prefs[Keys.inventoryClueIds] = progress.inventoryClueIds
        prefs[Keys.resolvedEncounterIds] = progress.resolvedEncounterIds
        prefs[Keys.encounterVisitedPoiIds] = progress.encounterVisitedPoiIds
        prefs[Keys.recentPoiIds] = encodeList(progress.recentPoiIds)
        prefs[Keys.recentPoiTitles] = encodeList(progress.recentPoiTitles)
        prefs[Keys.recentTemplateIds] = encodeList(progress.recentTemplateIds)
        prefs[Keys.recentPairKeys] = encodeList(progress.recentPairKeys)
        prefs[Keys.companionMemoryKeys] = progress.companionMemoryKeys
        writePeriod(prefs, progress.daily, daily = true)
        writePeriod(prefs, progress.weekly, daily = false)
        prefs[Keys.dailyGoalPeriodKey] = progress.dailyGoalPeriodKey
        prefs[Keys.dailyGoalIds] = encodeList(progress.dailyGoalIds)
        prefs[Keys.recentDailyGoalIds] = encodeList(progress.recentDailyGoalIds)
        prefs[Keys.weeklyGoalPeriodKey] = progress.weeklyGoalPeriodKey
        prefs[Keys.weeklyGoalIds] = encodeList(progress.weeklyGoalIds)
        prefs[Keys.recentWeeklyGoalIds] = encodeList(progress.recentWeeklyGoalIds)
    }

    private fun readPeriod(prefs: Preferences, daily: Boolean): PeriodProgress = if (daily) {
        PeriodProgress(
            periodKey = prefs[Keys.dailyPeriodKey].orEmpty(),
            distanceWalkedMeters = prefs[Keys.dailyDistanceMeters] ?: 0.0,
            discoveredPoiIds = prefs[Keys.dailyDiscoveredPoiIds].orEmpty(),
            clueIds = prefs[Keys.dailyClueIds].orEmpty(),
            resolvedEncounterIds = prefs[Keys.dailyResolvedEncounterIds].orEmpty(),
        )
    } else {
        PeriodProgress(
            periodKey = prefs[Keys.weeklyPeriodKey].orEmpty(),
            distanceWalkedMeters = prefs[Keys.weeklyDistanceMeters] ?: 0.0,
            discoveredPoiIds = prefs[Keys.weeklyDiscoveredPoiIds].orEmpty(),
            clueIds = prefs[Keys.weeklyClueIds].orEmpty(),
            resolvedEncounterIds = prefs[Keys.weeklyResolvedEncounterIds].orEmpty(),
        )
    }

    private fun writePeriod(
        prefs: MutablePreferences,
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
        val schemaVersion = intPreferencesKey("progress_schema_version")
        val visitedSpotIds = stringSetPreferencesKey("visited_spot_ids")
        val distanceMeters = doublePreferencesKey("distance_walked_meters")
        val cluesFound = intPreferencesKey("clues_found")
        val companionBond = intPreferencesKey("companion_bond")
        val inventoryClueIds = stringSetPreferencesKey("inventory_clue_ids")
        val resolvedEncounterIds = stringSetPreferencesKey("resolved_encounter_ids")
        val encounterVisitedPoiIds = stringSetPreferencesKey("encounter_visited_poi_ids")
        val recentPoiIds = stringPreferencesKey("recent_poi_ids")
        val recentPoiTitles = stringPreferencesKey("recent_poi_titles")
        val recentTemplateIds = stringPreferencesKey("recent_template_ids")
        val recentPairKeys = stringPreferencesKey("recent_pair_keys")
        val companionMemoryKeys = stringSetPreferencesKey("companion_memory_keys")

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

        val dailyGoalPeriodKey = stringPreferencesKey("daily_goal_period_key")
        val dailyGoalIds = stringPreferencesKey("daily_goal_ids")
        val recentDailyGoalIds = stringPreferencesKey("recent_daily_goal_ids")
        val weeklyGoalPeriodKey = stringPreferencesKey("weekly_goal_period_key")
        val weeklyGoalIds = stringPreferencesKey("weekly_goal_ids")
        val recentWeeklyGoalIds = stringPreferencesKey("recent_weekly_goal_ids")
    }
}
