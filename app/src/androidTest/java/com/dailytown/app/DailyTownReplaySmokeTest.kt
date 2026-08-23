package com.dailytown.app

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
        composeRule.onNodeWithText("Daily Town").assertExists()
        composeRule.onNodeWithText("경로 리플레이").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("서울시청 → 덕수궁 테스트 경로 재생 중"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText("서울시청 → 덕수궁 테스트 경로 재생 중").assertExists()
    }
}
