package com.dailytown.app.visualqa

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.MainActivity
import com.dailytown.app.OnboardingStateRule
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Opt-in design-review capture of the whole prototype journey (`-e dailytownCapture true`).
 * Uses Compose captureToImage so it also works on ATD images where device screenshots are black.
 * Output: connected_android_test_additional_output/.../visual/journey/. Resets local progress.
 */
@RunWith(AndroidJUnit4::class)
class ProductJourneyCaptureTest {
    @get:Rule(order = 0)
    val onboardingRule = OnboardingStateRule(completed = false)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureFirstRunTabsAndReplayEncounterJourney() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("dailytownCapture") == "true")
        waitFor("onboarding-welcome")
        capture("00-onboarding-welcome")
        composeRule.onNodeWithTag("onboarding-next").performClick()
        waitFor("onboarding-privacy")
        capture("01-onboarding-how")
        composeRule.onNodeWithTag("onboarding-next").performClick()
        waitFor("onboarding-meet-moru")
        capture("02-onboarding-moru")
        composeRule.onNodeWithTag("onboarding-next").performClick()
        composeRule.waitUntil(5_000L) { !exists("onboarding") }

        composeRule.onNodeWithTag("nav-settings").performClick()
        composeRule.onNodeWithTag("settings-reset").performScrollTo().performClick()
        composeRule.onNodeWithTag("settings-reset-confirm").performClick()
        composeRule.onNodeWithTag("nav-explore").performClick()
        composeRule.waitForIdle()
        capture("03-explore-prepare")
        listOf("nav-companion" to "04-companion", "nav-collection" to "05-records", "nav-goals" to "06-goals", "nav-settings" to "07-settings")
            .forEach { (tag, name) ->
                composeRule.onNodeWithTag(tag).performClick()
                composeRule.waitForIdle()
                capture(name)
            }

        composeRule.onNodeWithTag("settings-open-qa").performScrollTo().performClick()
        composeRule.onNodeWithTag("tracking-replay").performScrollTo().performClick()
        waitFor("explore-state-detect")
        capture("10-detect")
        waitFor("explore-state-discover", 20_000L)
        composeRule.onNodeWithTag("explore-state-discover").performScrollTo()
        capture("11-discover")
        composeRule.onNodeWithTag("encounter-start-investigation").performScrollTo().performClick()
        waitFor("explore-state-investigate")
        capture("12-investigate")
        repeat(3) {
            composeRule.waitForIdle()
            if (exists("encounter-collect-clue")) composeRule.onNodeWithTag("encounter-collect-clue").performScrollTo().performClick()
        }
        composeRule.onNodeWithTag("encounter-resolve").performScrollTo().performClick()
        waitFor("explore-state-record")
        composeRule.onNodeWithTag("explore-state-record").performScrollTo()
        capture("13-record")
        composeRule.onNodeWithTag("tracking-stop").performScrollTo().performClick()
        waitFor("walk-summary")
        composeRule.onNodeWithTag("walk-summary").performScrollTo()
        capture("14-walk-summary")

        composeRule.onNodeWithTag("nav-companion").performClick()
        composeRule.onNodeWithTag("companion-talk").performScrollTo().performClick()
        composeRule.onNodeWithTag("companion-dialogue-reply-0").performScrollTo().performClick()
        composeRule.onNodeWithTag("companion-dialogue-response").performScrollTo()
        capture("15-companion-dialogue")
        composeRule.onNodeWithTag("companion-gifts").performScrollTo().performClick()
        composeRule.onNodeWithTag("companion-keepsake").performScrollTo()
        capture("16-companion-keepsakes")
        composeRule.onNodeWithTag("nav-collection").performClick()
        composeRule.waitForIdle()
        capture("17-records-after")
        composeRule.onNodeWithTag("nav-goals").performClick()
        composeRule.waitForIdle()
        capture("18-goals-after")
    }

    private fun capture(name: String) {
        composeRule.waitForIdle()
        composeRule.onRoot().captureToImage().asAndroidBitmap().writeToTestStorage("visual/journey/$name")
    }

    private fun waitFor(tag: String, timeoutMillis: Long = 8_000L) {
        composeRule.waitUntil(timeoutMillis) { exists(tag) }
    }

    private fun exists(tag: String) = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
}
