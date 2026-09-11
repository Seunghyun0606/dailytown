package com.dailytown.app.persistence

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressPreferencesCodecTest {
    @Test
    fun `round trips current progress schema without changing legacy fields`() {
        val progress = ExplorationProgress(
            visitedSpotIds = setOf("spot-1"),
            distanceWalkedMeters = 1234.5,
            cluesFound = 3,
            companionBond = 17,
            inventoryClueIds = setOf("clue-1", "clue-2"),
            resolvedEncounterIds = setOf("enc-1"),
            encounterVisitedPoiIds = setOf("poi-1"),
            recentPoiIds = listOf("poi-1", "poi-2"),
            recentPoiTitles = listOf("첫 장소", "둘째 장소"),
            recentTemplateIds = listOf("template-1"),
            recentPairKeys = listOf("poi-1:template-1"),
            companionMemoryKeys = setOf("poi:poi-1", "mechanic:TRACE_CHAIN"),
            daily = PeriodProgress(
                periodKey = "2026-09-11",
                distanceWalkedMeters = 456.0,
                discoveredPoiIds = setOf("poi-1"),
                clueIds = setOf("clue-1"),
                resolvedEncounterIds = setOf("enc-1"),
            ),
            weekly = PeriodProgress(
                periodKey = "2026-W37",
                distanceWalkedMeters = 789.0,
                discoveredPoiIds = setOf("poi-1", "poi-2"),
                clueIds = setOf("clue-1", "clue-2"),
                resolvedEncounterIds = setOf("enc-1"),
            ),
            dailyGoalPeriodKey = "2026-09-11",
            dailyGoalIds = listOf("daily-1", "daily-2"),
            recentDailyGoalIds = listOf("daily-old"),
            weeklyGoalPeriodKey = "2026-W37",
            weeklyGoalIds = listOf("weekly-1"),
            recentWeeklyGoalIds = listOf("weekly-old"),
        )
        val prefs = mutablePreferencesOf()

        ProgressPreferencesCodec.encode(prefs, progress)

        assertEquals(
            ProgressPreferencesCodec.CURRENT_SCHEMA_VERSION,
            prefs[intPreferencesKey("progress_schema_version")],
        )
        assertEquals(progress, ProgressPreferencesCodec.decode(prefs))
    }

    @Test
    fun `decodes legacy schema zero when version key is absent`() {
        val progress = ExplorationProgress(
            distanceWalkedMeters = 42.0,
            companionBond = 9,
            recentPoiIds = listOf("legacy-poi"),
            recentPoiTitles = listOf("예전 장소"),
        )
        val prefs = mutablePreferencesOf()
        ProgressPreferencesCodec.encode(prefs, progress)
        prefs.remove(intPreferencesKey("progress_schema_version"))

        assertEquals(progress, ProgressPreferencesCodec.decode(prefs))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects unknown future schema instead of silently decoding`() {
        val prefs = mutablePreferencesOf()
        prefs[intPreferencesKey("progress_schema_version")] =
            ProgressPreferencesCodec.CURRENT_SCHEMA_VERSION + 1

        ProgressPreferencesCodec.decode(prefs)
    }
}
