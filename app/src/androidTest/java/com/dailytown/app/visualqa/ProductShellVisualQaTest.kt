package com.dailytown.app.visualqa

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Captures the real five-tab Daily Town product shell, not a synthetic design fixture.
 *
 * Visual approval remains a human/design gate. This test only guarantees that every primary user
 * destination is reachable from the shipped MainActivity and produces a reviewable viewport image.
 */
@RunWith(AndroidJUnit4::class)
class ProductShellVisualQaTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun primaryFiveTabProductShellProducesReviewableCaptures() {
        composeRule.waitForIdle()
        capture("explore")

        openAndCapture("nav-companion", "companion")
        openAndCapture("nav-collection", "records")
        openAndCapture("nav-goals", "goals")
        openAndCapture("nav-settings", "settings")
    }

    private fun openAndCapture(navTag: String, name: String) {
        composeRule.onNodeWithTag(navTag).performClick()
        composeRule.waitForIdle()
        capture(name)
    }

    private fun capture(name: String) {
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
            ?: error("Device screenshot unavailable for product shell $name")
        bitmap.writeToTestStorage("visual/product-shell/$name")
    }
}
