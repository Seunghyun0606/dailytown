package com.dailytown.app.visualqa

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.graphics.writeToTestStorage
import com.dailytown.app.persistence.ExplorationProgress
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
        companionMemoryKeys = setOf("poi:tourapi:1001", "poi:tourapi:1002"),
    )

    @Test
    fun actualProductRecordFlowRendersAllFiveA3Destinations() {
        composeRule.setContent {
            DailyTownTheme {
                DailyTownRecordsScreen(progress)
            }
        }

        capture("record-journal-home", "journal-home")

        composeRule.onNodeWithTag("journal-entry-0").performClick()
        capture("record-discovery-detail", "discovery-detail")

        composeRule.onNodeWithTag("discovery-open-clue").performClick()
        capture("record-clue-note", "clue-note")

        composeRule.onNodeWithTag("clue-back").performClick()
        composeRule.onNodeWithTag("discovery-back").performClick()
        composeRule.onNodeWithTag("record-tab-collection").performClick()
        capture("record-collection-grid", "collection-grid")

        composeRule.onNodeWithTag("record-tab-memory").performClick()
        capture("record-memory-detail", "memory-detail")
    }

    private fun capture(tag: String, name: String) {
        composeRule.waitForIdle()
        // captureToImage() already fails when the tagged node does not exist, so keeping the
        // capture itself as the assertion avoids relying on a test API that is absent from the
        // Compose test artifact pinned by this project.
        composeRule.onNodeWithTag(tag)
            .captureToImage()
            .asAndroidBitmap()
            .writeToTestStorage("visual/product-a3/$name")
    }
}
