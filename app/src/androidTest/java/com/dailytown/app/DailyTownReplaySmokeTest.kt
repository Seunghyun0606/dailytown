package com.dailytown.app

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
        composeRule.onNodeWithTag("tracking-replay")
            .performScrollTo()
            .performClick()

        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tracking-status")
            .assert(hasText("서울시청 → 덕수궁 테스트 경로 재생 중"))
    }

    @Test
    fun replaySessionsLatchSetupBeforeStartThenDriveComparisonProtocol() {
        composeRule.onNodeWithTag("field-test-cohort-counts")
            .assert(hasText("신규 0회 · 반복 0회"))
        composeRule.onNodeWithTag("field-test-protocol-status")
            .assert(hasText("프로토콜: 데이터 부족"))
        composeRule.onNodeWithTag("field-test-setup-profile-new")
            .assertIsSelected()

        startReplayAndStop(
            expectedActivePlan = "진행 중 계획: 신규 지역 · 균형 · 기준거리 없음",
            expectedCompletedPlan = "종료 세션 계획: 신규 지역 · 균형 · 기준거리 없음 · 아래에서 확인 후 비교에 기록하세요.",
        )

        composeRule.onNodeWithTag("field-test-run-status")
            .performScrollTo()
            .assert(hasText("런 요약: 참고용"))
        composeRule.onNodeWithTag("field-test-record-suggestion")
            .performScrollTo()
            .assert(hasText("종료된 세션이 준비되었습니다. 시작 계획은 신규 지역이며, 필요하면 아래에서 수정 후 기록할 수 있습니다."))
        composeRule.onNodeWithTag("field-test-record")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("field-test-cohort-counts")
            .assert(hasText("신규 1회 · 반복 0회"))
        composeRule.onNodeWithTag("field-test-protocol-status")
            .assert(hasText("프로토콜: 데이터 부족"))
        composeRule.onNodeWithTag("field-test-record")
            .assertIsNotEnabled()

        composeRule.onNodeWithTag("field-test-setup-profile-repeat")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        composeRule.onNodeWithTag("field-test-draft-plan")
            .assert(hasText("다음 세션: 반복 지역 · 균형 · 기준거리 없음"))

        startReplayAndStop(
            expectedActivePlan = "진행 중 계획: 반복 지역 · 균형 · 기준거리 없음",
            expectedCompletedPlan = "종료 세션 계획: 반복 지역 · 균형 · 기준거리 없음 · 아래에서 확인 후 비교에 기록하세요.",
        )

        composeRule.onNodeWithTag("field-test-run-status")
            .performScrollTo()
            .assert(hasText("런 요약: 참고용"))
        composeRule.onNodeWithTag("field-test-record-suggestion")
            .performScrollTo()
            .assert(hasText("종료된 세션이 준비되었습니다. 시작 계획은 반복 지역이며, 필요하면 아래에서 수정 후 기록할 수 있습니다."))
        composeRule.onNodeWithTag("field-test-profile-repeat")
            .assertIsSelected()
        composeRule.onNodeWithTag("field-test-record")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("field-test-cohort-counts")
            .assert(hasText("신규 1회 · 반복 1회"))
        composeRule.onNodeWithTag("field-test-protocol-status")
            .assert(hasText("프로토콜: 비교 가능"))

        composeRule.onNodeWithTag("field-test-reset")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("field-test-cohort-counts")
            .assert(hasText("신규 0회 · 반복 0회"))
        composeRule.onNodeWithTag("field-test-protocol-status")
            .assert(hasText("프로토콜: 데이터 부족"))
    }

    private fun startReplayAndStop(
        expectedActivePlan: String,
        expectedCompletedPlan: String,
    ) {
        composeRule.onNodeWithTag("tracking-replay")
            .performScrollTo()
            .performClick()
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tracking-status")
            .assert(hasText("서울시청 → 덕수궁 테스트 경로 재생 중"))
        composeRule.onNodeWithTag("field-test-active-plan")
            .performScrollTo()
            .assert(hasText(expectedActivePlan))
        composeRule.onNodeWithTag("field-test-setup-profile-new")
            .assertIsNotEnabled()
        composeRule.onNodeWithTag("field-test-setup-profile-repeat")
            .assertIsNotEnabled()

        composeRule.onNodeWithText("중지")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("field-test-completed-plan")
            .performScrollTo()
            .assert(hasText(expectedCompletedPlan))
    }
}
