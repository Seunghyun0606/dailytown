package com.dailytown.app

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OldGinkgoRuntimeFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun replayBindsOldGinkgoArtOnlyFromDiscoveryThroughRecordsAndCompanion() {
        openQaTools()
        composeRule.onNodeWithTag("tracking-replay")
            .performScrollTo()
            .performClick()

        waitForTag("explore-state-detect")
        composeRule.onAllNodesWithTag("old-ginkgo-place-discovery").assertCountEquals(0)
        composeRule.onAllNodesWithTag("old-ginkgo-note-discovery").assertCountEquals(0)

        waitForTag("explore-state-discover", timeoutMillis = 10_000L)
        composeRule.onNodeWithTag("old-ginkgo-place-discovery").assert(hasTestTag("old-ginkgo-place-discovery"))
        composeRule.onNodeWithTag("old-ginkgo-note-discovery").assert(hasTestTag("old-ginkgo-note-discovery"))

        composeRule.onNodeWithTag("encounter-start-investigation")
            .performScrollTo()
            .performClick()
        waitForTag("explore-state-investigate")
        composeRule.onNodeWithTag("old-ginkgo-note-investigate").assert(hasTestTag("old-ginkgo-note-investigate"))
        composeRule.onNodeWithTag("old-ginkgo-leaf-investigate").assert(hasTestTag("old-ginkgo-leaf-investigate"))

        repeat(3) {
            composeRule.waitForIdle()
            if (tagExists("encounter-collect-clue")) {
                composeRule.onNodeWithTag("encounter-collect-clue")
                    .performScrollTo()
                    .performClick()
            }
        }
        composeRule.onNodeWithTag("encounter-resolve")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        waitForTag("explore-state-record")
        composeRule.onNodeWithTag("old-ginkgo-memory-resolved")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-memory-resolved"))

        composeRule.onNodeWithTag("nav-companion").performClick()
        composeRule.onNodeWithTag("old-ginkgo-memory-companion")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-memory-companion"))

        composeRule.onNodeWithTag("nav-explore").performClick()
        waitForTag("explore-state-record")
        composeRule.onNodeWithTag("encounter-open-records")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("records-memories")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("old-ginkgo-memory-records")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-memory-records"))
    }

    private fun openQaTools() {
        composeRule.onNodeWithTag("nav-settings").performClick()
        composeRule.onNodeWithTag("settings-open-qa")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = 6_000L) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) { tagExists(tag) }
        composeRule.onNodeWithTag(tag).assert(hasTestTag(tag))
    }

    private fun tagExists(tag: String): Boolean =
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
}
