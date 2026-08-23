package com.dailytown.app

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
        composeRule.onNodeWithText("Daily Town").fetchSemanticsNode()
        composeRule.onNodeWithText("경로 리플레이").performClick()

        // Compose test v2 uses a StandardTestDispatcher. Advance the frame clock so state
        // written by the click is recomposed before inspecting the UI, then drain remaining work.
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("서울시청 → 덕수궁 테스트 경로 재생 중")
            .fetchSemanticsNode()
    }
}
