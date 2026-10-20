package com.dailytown.app.persistence

import com.dailytown.app.domain.Companion
import com.dailytown.app.domain.ExplorationState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ExplorationProgressTest {
    @Test
    fun roundTripsDerivedProgressWithoutLocationTrace() {
        val state = ExplorationState(
            companion = Companion("moru", "모루", 27),
            visitedSpotIds = setOf("a", "b"),
            distanceWalkedMeters = 1234.5,
            cluesFound = 4,
        )
        val restored = state.toProgress().toState(Companion("moru", "모루", 12))

        assertEquals(state, restored)
    }

    @Test
    fun recentPoiSnapshotKeepsProductionTitleAlignedWithId() {
        val first = ExplorationProgress().recordEncounterVisit(
            poiId = "tourapi:101",
            templateId = "template-a",
            date = LocalDate.of(2026, 9, 8),
            poiTitle = "덕수궁",
        )
        val second = first.recordEncounterVisit(
            poiId = "tourapi:202",
            templateId = "template-b",
            date = LocalDate.of(2026, 9, 8),
            poiTitle = "서울광장",
        )

        assertEquals(listOf("tourapi:202", "tourapi:101"), second.recentPoiIds)
        assertEquals(listOf("서울광장", "덕수궁"), second.recentPoiTitles)
    }

    @Test
    fun revisitingPoiWithoutTitlePreservesPreviouslyKnownTitle() {
        val progress = ExplorationProgress(
            recentPoiIds = listOf("tourapi:101"),
            recentPoiTitles = listOf("덕수궁"),
        ).recordEncounterVisit(
            poiId = "tourapi:101",
            templateId = "template-a",
            date = LocalDate.of(2026, 9, 8),
        )

        assertEquals(listOf("tourapi:101"), progress.recentPoiIds)
        assertEquals(listOf("덕수궁"), progress.recentPoiTitles)
    }
}
