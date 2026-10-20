package com.dailytown.app

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end check of the complete prototype loop on a fresh install state:
 * onboarding → explore (replay) → discover/investigate/resolve → stop → walk summary → Records →
 * Companion dialogue/keepsakes → Settings reset. Uses replay so it needs no permission or map key.
 */
@RunWith(AndroidJUnit4::class)
class PrototypeJourneyTest {
    @get:Rule(order = 0)
    val onboardingRule = OnboardingStateRule(completed = false)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun firstRunThroughWalkSummaryCompanionAndReset() {
        // 1. Onboarding: welcome → how it works (privacy) → meet Moru → Explore.
        waitForTag("onboarding-welcome")
        composeRule.onNodeWithTag("onboarding-next").performClick()
        waitForTag("onboarding-privacy")
        composeRule.onNodeWithTag("onboarding-next").performClick()
        waitForTag("onboarding-meet-moru")
        composeRule.onNodeWithTag("onboarding-next").performClick()
        waitUntilGone("onboarding")

        // Start from a clean slate regardless of what earlier tests persisted.
        resetProgressFromSettings()

        // 2. Replay walk through a full encounter.
        composeRule.onNodeWithTag("settings-open-qa").performScrollTo().performClick()
        composeRule.onNodeWithTag("tracking-replay").performScrollTo().performClick()
        waitForTag("explore-state-discover", 20_000L)
        composeRule.onNodeWithTag("encounter-start-investigation").performScrollTo().performClick()
        waitForTag("explore-state-investigate")
        // Earn one clue the player way: solve the mechanic-specific interaction.
        MechanicInteractionSolver(composeRule).solveOne()
        repeat(3) {
            composeRule.waitForIdle()
            if (exists("encounter-collect-clue")) {
                composeRule.onNodeWithTag("encounter-collect-clue").performScrollTo().performClick()
            }
        }
        composeRule.onNodeWithTag("encounter-resolve").performScrollTo().assertIsEnabled().performClick()
        waitForTag("explore-state-record")

        // 3. Stopping the walk produces an honest summary, which routes to Records.
        composeRule.onNodeWithTag("tracking-stop").performScrollTo().performClick()
        waitForTag("walk-summary")
        composeRule.onNodeWithTag("walk-summary").performScrollTo()
        composeRule.onNodeWithTag("walk-summary-resolved")
            .assert(hasText("1건", substring = true))
        composeRule.onNodeWithTag("walk-summary-relationship").assert(hasText("호감도", substring = true))
        composeRule.onNodeWithTag("walk-summary-records").performScrollTo().performClick()
        waitForTag("records-today")

        // 4. Companion: authored dialogue with a reply, and the keepsake from the solved mystery.
        composeRule.onNodeWithTag("nav-companion").performClick()
        composeRule.onNodeWithTag("companion-talk").performScrollTo().performClick()
        waitForTag("companion-dialogue-line")
        composeRule.onNodeWithTag("companion-dialogue-reply-0").performScrollTo().performClick()
        waitForTag("companion-dialogue-response")
        composeRule.onNodeWithTag("companion-dialogue-next").performScrollTo().performClick()
        waitForTag("companion-dialogue-reply-0")
        composeRule.onNodeWithTag("companion-gifts").performScrollTo().performClick()
        waitForTag("companion-keepsake")

        // 5. Reset clears records and returns the keepsake list to empty.
        resetProgressFromSettings()
        composeRule.onNodeWithTag("nav-companion").performClick()
        // The gifts panel is still open (tabs stay composed); it must now show the empty state.
        waitForTag("companion-keepsakes-empty")
    }

    private fun resetProgressFromSettings() {
        composeRule.onNodeWithTag("nav-settings").performClick()
        composeRule.onNodeWithTag("settings-reset").performScrollTo().assertIsEnabled().performClick()
        composeRule.onNodeWithTag("settings-reset-confirm").performClick()
        waitForTag("settings-reset-done")
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = 8_000L) {
        composeRule.waitUntil(timeoutMillis) { exists(tag) }
        composeRule.onNodeWithTag(tag).assert(hasTestTag(tag))
    }

    private fun waitUntilGone(tag: String, timeoutMillis: Long = 5_000L) {
        composeRule.waitUntil(timeoutMillis) { !exists(tag) }
    }

    private fun exists(tag: String) = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
}
