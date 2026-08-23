package com.dailytown.app

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
        composeRule.onNodeWithTag("tracking-replay").performClick()

        // Compose test v2 uses a StandardTestDispatcher. Advance queued composition work
        // explicitly and assert semantic state rather than relying on scroll position/text lookup.
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tracking-status")
            .assert(hasText("서울시청 → 덕수궁 테스트 경로 재생 중"))
    }
}
