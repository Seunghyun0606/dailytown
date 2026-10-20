package com.dailytown.app.ui.presentation

import com.dailytown.app.mystery.EncounterPhase
import com.dailytown.app.mystery.EncounterSelection
import com.dailytown.app.mystery.ScenarioContentPack
import com.dailytown.app.visual.OldGinkgoVisualAssets
import com.dailytown.app.visual.SemanticAssetKey

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
    val placeAssetKey: SemanticAssetKey? = null,
    val noteAssetKey: SemanticAssetKey? = null,
    val clueAssetKey: SemanticAssetKey? = null,
    val memoryAssetKey: SemanticAssetKey? = null,
    val moruLine: String? = null,
    val isRevisit: Boolean = false,
)

/**
 * Presentation-only mapping for the approved UX scenario v2.
 *
 * Domain proximity semantics remain owned by EncounterProximityController (180 m / 60 m).
 * The 120 m value below is intentionally a UI-only anticipation boundary while an encounter is
 * already HINTED; it must never be fed back into encounter progression.
 *
 * Copy comes from the versioned [ScenarioContentPack] (approved Old Ginkgo story for TRACE_CHAIN,
 * engineering drafts with the real place name for the other mechanics). The Old Ginkgo note/leaf
 * rasters are the only approved discovery art family, so every mechanic currently reuses them.
 * Old Ginkgo raster keys are also presentation-only. PREPARE/DETECT/APPROACH intentionally expose
 * no scenario art so an unknown signal is not visually spoiled before discovery.
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

        val authored = ScenarioContentPack.copy(
            mechanic = selection.template.mechanic,
            placeName = selection.poi.name,
            isRevisit = selection.isRevisit,
        )
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
                    placeAssetKey = OldGinkgoVisualAssets.PlaceMain,
                    noteAssetKey = OldGinkgoVisualAssets.FoldedNote,
                    clueAssetKey = OldGinkgoVisualAssets.GinkgoLeaf,
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
                placeAssetKey = OldGinkgoVisualAssets.PlaceMain,
                noteAssetKey = OldGinkgoVisualAssets.FoldedNote,
                clueAssetKey = OldGinkgoVisualAssets.GinkgoLeaf,
                memoryAssetKey = OldGinkgoVisualAssets.Keepsake,
                moruLine = authored.resolveMoruLine,
                isRevisit = selection.isRevisit,
            )
        }
    }
}
