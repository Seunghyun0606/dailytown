package com.dailytown.app

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
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
        openQaTools()
        composeRule.onNodeWithTag("tracking-replay")
            .performScrollTo()
            .performClick()

        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tracking-status")
            .assert(hasText("서울시청 → 덕수궁 테스트 경로 재생 중"))
    }

    @Test
    fun replaySessionSurvivesBottomNavigationRoundTrip() {
        openQaTools()
        composeRule.onNodeWithTag("tracking-replay")
            .performScrollTo()
            .performClick()
        composeRule.mainClock.advanceTimeBy(500L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav-companion")
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav-explore")
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tracking-status")
            .assert(hasText("서울시청 → 덕수궁 테스트 경로 재생 중"))
    }

    @Test
    fun replayUxV2TransitionsPersistAndSurviveTabBackContinue() {
        openQaTools()
        composeRule.onNodeWithTag("tracking-replay")
            .performScrollTo()
            .performClick()

        waitForTag("explore-state-detect")
        composeRule.onNodeWithTag("explore-domain-state")
            .performScrollTo()
            .assert(hasText("HINTED", substring = true))
        composeRule.onAllNodesWithTag("old-ginkgo-place-discovery").assertCountEquals(0)
        composeRule.onAllNodesWithTag("old-ginkgo-note-discovery").assertCountEquals(0)

        // Explore remains composed and replay keeps running behind another tab.
        composeRule.onNodeWithTag("nav-companion").performClick()
        composeRule.onNodeWithTag("companion-product-screen").assert(hasTestTag("companion-product-screen"))
        composeRule.onNodeWithTag("nav-explore").performClick()

        waitForTag("explore-state-discover", timeoutMillis = 10_000L)
        composeRule.onNodeWithTag("old-ginkgo-place-discovery")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-place-discovery"))
        composeRule.onNodeWithTag("old-ginkgo-note-discovery")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-note-discovery"))
        composeRule.onNodeWithTag("encounter-start-investigation")
            .performScrollTo()
            .performClick()
        waitForTag("explore-state-investigate")
        composeRule.onNodeWithTag("old-ginkgo-note-investigate")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-note-investigate"))
        composeRule.onNodeWithTag("old-ginkgo-leaf-investigate")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-leaf-investigate"))

        // Existing templates require two or three clues. The discovery CTA already collected clue 1.
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
        composeRule.onNodeWithText("오늘의 기록이 생겼어요").assert(hasText("오늘의 기록이 생겼어요"))
        composeRule.onNodeWithTag("old-ginkgo-memory-resolved")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-memory-resolved"))

        // Companion reads the same persisted bond/memory state, then system Back returns to live Explore.
        composeRule.onNodeWithTag("nav-companion").performClick()
        composeRule.onNodeWithTag("companion-recent-memory").performScrollTo().assert(hasTestTag("companion-recent-memory"))
        composeRule.onNodeWithTag("old-ginkgo-memory-companion")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-memory-companion"))
        composeRule.onAllNodesWithText("아직 이름이 남은 장소 기억은 없어요.").assertCountEquals(0)
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        waitForTag("explore-state-record")

        // Completion routes to Records without clearing the resolved encounter.
        composeRule.onNodeWithTag("encounter-open-records")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("records-today").assert(hasTestTag("records-today"))
        composeRule.onNodeWithTag("records-places").assert(hasTestTag("records-places"))
        composeRule.onNodeWithText("모루와 공유한 기억 있음").assert(hasText("모루와 공유한 기억 있음"))
        composeRule.onAllNodesWithText("관찰한 단서 0개").assertCountEquals(0)
        composeRule.onNodeWithTag("records-memories")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("old-ginkgo-memory-records")
            .performScrollTo()
            .assert(hasTestTag("old-ginkgo-memory-records"))

        composeRule.onNodeWithTag("nav-explore").performClick()
        waitForTag("explore-state-record")
        composeRule.onNodeWithTag("encounter-continue")
            .performScrollTo()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) { !tagExists("explore-state-record") }
        composeRule.onNodeWithTag("nav-explore").assertIsSelected()
    }

    @Test
    fun replaySessionsLatchSetupThenEnablePrivacySafeStructuredExport() {
        openQaTools()
        composeRule.onNodeWithTag("field-test-cohort-counts")
            .assert(hasText("신규 0회 · 반복 0회"))
        composeRule.onNodeWithTag("field-test-protocol-status")
            .assert(hasText("프로토콜: 데이터 부족"))
        composeRule.onNodeWithTag("field-test-export-json")
            .assertIsNotEnabled()
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
        composeRule.onNodeWithTag("field-test-export-json")
            .performScrollTo()
            .assertIsEnabled()
        composeRule.onNodeWithTag("field-test-export-privacy-note")
            .assert(hasText("공유 시점에만 최대 20개 파생 세션을 JSON으로 만듭니다. 앱에는 파일이나 세션 이력을 영구 저장하지 않습니다."))

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
        composeRule.onNodeWithTag("field-test-export-json")
            .assertIsEnabled()

        composeRule.onNodeWithTag("field-test-reset")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("field-test-cohort-counts")
            .assert(hasText("신규 0회 · 반복 0회"))
        composeRule.onNodeWithTag("field-test-protocol-status")
            .assert(hasText("프로토콜: 데이터 부족"))
        composeRule.onNodeWithTag("field-test-export-json")
            .assertIsNotEnabled()
    }

    private fun openQaTools() {
        composeRule.onNodeWithTag("nav-settings")
            .performClick()
        composeRule.onNodeWithTag("settings-open-qa")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
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

    private fun waitForTag(tag: String, timeoutMillis: Long = 6_000L) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) { tagExists(tag) }
        composeRule.onNodeWithTag(tag).assert(hasTestTag(tag))
    }

    private fun tagExists(tag: String): Boolean =
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
}
