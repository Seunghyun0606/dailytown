package com.dailytown.app.ui.visual

import com.dailytown.app.mystery.EncounterPhase
import com.dailytown.app.visual.MapHaloVisualState
import com.dailytown.app.visual.MapOverlaySemanticState
import com.dailytown.app.visual.MarkerSemantic

data class EncounterMapVisualState(
    val markerSemantic: MarkerSemantic?,
    val selected: Boolean,
    val overlays: MapOverlaySemanticState,
)

/**
 * Application-layer mapping from encounter gameplay state to map visual semantics.
 *
 * HIDDEN encounters stay absent. The current runtime has no route-navigation geometry source, so
 * this resolver never fabricates FOLLOWING route geometry. Discovery-effect intensity also remains
 * unset until a gameplay-to-effect mapping is explicitly approved.
 */
object EncounterMapVisualResolver {
    fun resolve(
        phase: EncounterPhase,
        isRevisit: Boolean,
        reducedMotion: Boolean = false,
    ): EncounterMapVisualState = when (phase) {
        EncounterPhase.HIDDEN -> EncounterMapVisualState(
            markerSemantic = null,
            selected = false,
            overlays = MapOverlaySemanticState(reducedMotion = reducedMotion),
        )
        EncounterPhase.HINTED -> EncounterMapVisualState(
            markerSemantic = if (isRevisit) MarkerSemantic.ENCOUNTER_REVISIT else MarkerSemantic.ENCOUNTER_HINTED,
            selected = false,
            overlays = MapOverlaySemanticState(
                haloState = MapHaloVisualState.IDLE,
                reducedMotion = reducedMotion,
            ),
        )
        EncounterPhase.DISCOVERED -> EncounterMapVisualState(
            markerSemantic = if (isRevisit) MarkerSemantic.ENCOUNTER_REVISIT else MarkerSemantic.ENCOUNTER_ACTIVE,
            selected = true,
            overlays = MapOverlaySemanticState(
                haloState = MapHaloVisualState.ACTIVE,
                reducedMotion = reducedMotion,
            ),
        )
        EncounterPhase.RESOLVED -> EncounterMapVisualState(
            markerSemantic = MarkerSemantic.ENCOUNTER_SOLVED,
            selected = false,
            overlays = MapOverlaySemanticState(reducedMotion = reducedMotion),
        )
    }
}
