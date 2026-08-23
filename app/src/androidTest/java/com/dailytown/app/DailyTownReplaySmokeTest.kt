package com.dailytown.app

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyTownReplaySmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun replayRouteStartsWithoutLocationPermissionOrMapCredential() {
        // The controls live below a long scrollable dashboard. On managed devices,
        // scrolling the node into the viewport before clicking avoids an off-screen
        // semantics action being accepted without dispatching a real pointer click.
        composeRule.onNodeWithTag("tracking-replay")
            .performScrollTo()
            .performClick()

        // Compose test v2 uses a StandardTestDispatcher. Advance queued composition work
        // explicitly and assert semantic state rather than relying on raw screen position.
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tracking-status")
            .assert(hasText("서울시청 → 덕수궁 테스트 경로 재생 중"))
    }
}
