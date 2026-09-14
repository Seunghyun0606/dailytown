package com.dailytown.app.visualqa

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.PeriodProgress
import com.dailytown.app.ui.DailyTownRecordsScreen
import com.dailytown.app.ui.visual.DailyTownTheme
import org.junit.Rule
import org.junit.Test

class ProductFlowVisualQaTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val progress = ExplorationProgress(
        distanceWalkedMeters = 2840.0,
        cluesFound = 3,
        companionBond = 31,
        inventoryClueIds = setOf("clue-a", "clue-b", "clue-c"),
        resolvedEncounterIds = setOf("encounter-a"),
        encounterVisitedPoiIds = setOf("tourapi:1001", "tourapi:1002", "tourapi:1003"),
        recentPoiIds = listOf("tourapi:1001", "tourapi:1002", "tourapi:1003"),
        recentPoiTitles = listOf("서울광장", "덕수궁", "인사동 문화거리"),
        companionMemoryKeys = setOf("poi:tourapi:1001", "poi:tourapi:1002", "mechanic:TRACE_CHAIN"),
        daily = PeriodProgress(
            periodKey = "2026-09-11",
            discoveredPoiIds = setOf("tourapi:1001"),
            clueIds = setOf("clue-a"),
            resolvedEncounterIds = setOf("encounter-a"),
        ),
    )

    @Test
    fun recordsRendersApprovedFivePartJournalHierarchyAndDetails() {
        composeRule.setContent {
            DailyTownTheme {
                DailyTownRecordsScreen(progress)
            }
        }

        capture("record-journal-home", "records-home")
        composeRule.onNodeWithTag("records-today").assert(hasTestTag("records-today"))
        composeRule.onNodeWithTag("records-places").assert(hasTestTag("records-places"))
        composeRule.onNodeWithTag("records-mysteries").performScrollTo().assert(hasTestTag("records-mysteries"))
        composeRule.onNodeWithTag("records-clues").performScrollTo().assert(hasTestTag("records-clues"))
        composeRule.onNodeWithTag("records-memories").performScrollTo().assert(hasTestTag("records-memories"))

        composeRule.onNodeWithTag("journal-entry-0").performScrollTo().performClick()
        capture("record-discovery-detail", "place-detail")

        composeRule.onNodeWithTag("discovery-open-clue").performScrollTo().performClick()
        capture("record-clue-note", "clue-detail")

        composeRule.onNodeWithTag("clue-back").performClick()
        composeRule.onNodeWithTag("records-memories").performScrollTo().performClick()
        capture("record-memory-detail", "memory-detail")
    }

    private fun capture(tag: String, name: String) {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(tag).assert(hasTestTag(tag))
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("Device screenshot unavailable for $tag")
        bitmap.writeToTestStorage("visual/product-records-v2/$name")
    }
}
