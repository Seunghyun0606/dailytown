package com.dailytown.app.ui.presentation

import com.dailytown.app.mystery.EncounterPhase
import com.dailytown.app.mystery.EncounterSelection

enum class ExploreExperienceStep {
    PREPARE,
    DETECT,
    APPROACH,
    DISCOVER,
    INVESTIGATE,
    RECORD,
}

data class ExploreEncounterPresentation(
    val step: ExploreExperienceStep,
    val signalLabel: String? = null,
    val distanceMeters: Int? = null,
    val title: String? = null,
    val hint: String? = null,
    val premise: String? = null,
    val clueLabel: String? = null,
    val clueSemanticKey: String? = null,
    val moruLine: String? = null,
    val isRevisit: Boolean = false,
)

/**
 * Presentation-only mapping for the approved UX scenario v2.
 *
 * Domain proximity semantics remain owned by EncounterProximityController (180 m / 60 m).
 * The 120 m value below is intentionally a UI-only anticipation boundary while an encounter is
 * already HINTED; it must never be fed back into encounter progression.
 */
object ExploreEncounterPresentationMapper {
    const val NEAR_PRESENTATION_METERS = 120
    const val VALIDATION_CLUE_SEMANTIC_KEY = "leaf_mark_note"

    fun map(
        selection: EncounterSelection?,
        distanceMeters: Int?,
    ): ExploreEncounterPresentation {
        if (selection == null || selection.encounter.phase == EncounterPhase.HIDDEN) {
            return ExploreEncounterPresentation(step = ExploreExperienceStep.PREPARE)
        }

        val authored = authoredCopy(selection.isRevisit)
        return when (selection.encounter.phase) {
            EncounterPhase.HIDDEN -> ExploreEncounterPresentation(step = ExploreExperienceStep.PREPARE)
            EncounterPhase.HINTED -> {
                val step = if (distanceMeters != null && distanceMeters <= NEAR_PRESENTATION_METERS) {
                    ExploreExperienceStep.APPROACH
                } else {
                    ExploreExperienceStep.DETECT
                }
                ExploreEncounterPresentation(
                    step = step,
                    signalLabel = "알 수 없는 신호",
                    distanceMeters = distanceMeters,
                    title = authored.title,
                    hint = authored.hint,
                    moruLine = authored.detectMoruLine,
                    isRevisit = selection.isRevisit,
                )
            }
            EncounterPhase.DISCOVERED -> {
                val investigating = selection.encounter.clueIds.isNotEmpty()
                ExploreEncounterPresentation(
                    step = if (investigating) ExploreExperienceStep.INVESTIGATE else ExploreExperienceStep.DISCOVER,
                    distanceMeters = distanceMeters,
                    title = authored.title,
                    premise = authored.premise,
                    clueLabel = authored.clueLabel,
                    clueSemanticKey = VALIDATION_CLUE_SEMANTIC_KEY,
                    moruLine = if (investigating) authored.investigateMoruLine else authored.discoverMoruLine,
                    isRevisit = selection.isRevisit,
                )
            }
            EncounterPhase.RESOLVED -> ExploreEncounterPresentation(
                step = ExploreExperienceStep.RECORD,
                distanceMeters = distanceMeters,
                title = authored.title,
                premise = authored.resolution,
                clueLabel = authored.clueLabel,
                clueSemanticKey = VALIDATION_CLUE_SEMANTIC_KEY,
                moruLine = authored.resolveMoruLine,
                isRevisit = selection.isRevisit,
            )
        }
    }

    private fun authoredCopy(isRevisit: Boolean): AuthoredValidationCopy = if (isRevisit) {
        AuthoredValidationCopy(
            title = "오래된 가로수의 메모",
            hint = "전에 걸었던 길인데, 오래된 나무 근처에서 다시 종이 같은 게 반짝였어.",
            premise = "전에 살펴본 나무 밑에 새로운 접힘 자국이 남은 메모가 보인다.",
            clueLabel = "잎자국이 남은 메모",
            detectMoruLine = "전에 봤던 곳인데, 이번엔 조금 다른 흔적 같아.",
            discoverMoruLine = "같은 장소라도 남는 이야기는 달라질 수 있나 봐.",
            investigateMoruLine = "잎자국이 전보다 또렷해. 이건 기억해두자.",
            resolution = "이번 산책에서 확인한 흔적을 기록에 덧붙였다.",
            resolveMoruLine = "우리 기억에 한 장면이 더 생겼네.",
        )
    } else {
        AuthoredValidationCopy(
            title = "오래된 가로수의 메모",
            hint = "오래된 나무 근처에서 종이 같은 게 반짝였어.",
            premise = "나무 밑에 접힌 메모 한 장이 남아 있다. 글씨는 조금 번졌지만 마지막 문장은 읽을 수 있다.",
            clueLabel = "잎자국이 남은 메모",
            detectMoruLine = "저쪽이 조금 신경 쓰이는데?",
            discoverMoruLine = "여기야. 나무 밑에 메모가 하나 있어.",
            investigateMoruLine = "잎자국이 일부러 남긴 표시처럼 보여.",
            resolution = "메모에 남은 작은 표시를 확인하고 오늘의 발견으로 기록했다.",
            resolveMoruLine = "누군가 다음 사람에게 작은 힌트를 남긴 것 같아. 우리도 기억해두자.",
        )
    }

    private data class AuthoredValidationCopy(
        val title: String,
        val hint: String,
        val premise: String,
        val clueLabel: String,
        val detectMoruLine: String,
        val discoverMoruLine: String,
        val investigateMoruLine: String,
        val resolution: String,
        val resolveMoruLine: String,
    )
}
