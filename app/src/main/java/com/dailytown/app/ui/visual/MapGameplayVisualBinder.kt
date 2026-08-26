package com.dailytown.app.ui.visual

import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.mystery.EncounterPhase
import com.dailytown.app.mystery.EncounterSelection
import com.dailytown.app.visual.MapOverlaySemanticState

/**
 * Application-layer bridge from encounter gameplay state to provider-neutral map visual semantics.
 *
 * This binder does not know NAVER/Google types, raw visual assets, route geometry, or visual timing.
 * Existing ambient/persistent markers are supplied by the caller and kept independent from the
 * short-lived active encounter marker.
 */
class MapGameplayVisualBinder(
    private val mapAdapter: MapViewAdapter,
) {
    fun applyEncounter(
        selection: EncounterSelection?,
        persistentMarkers: List<MapMarkerSpec>,
        reducedMotion: Boolean = false,
    ) {
        val visual = selection?.let {
            EncounterMapVisualResolver.resolve(
                phase = it.encounter.phase,
                isRevisit = it.isRevisit,
                reducedMotion = reducedMotion,
            )
        }
        val encounterMarker = if (selection != null && visual?.markerSemantic != null) {
            MapMarkerSpec(
                id = "active-${selection.encounter.id}",
                title = encounterMarkerTitle(selection),
                position = selection.poi.position,
                semantic = visual.markerSemantic,
                selected = visual.selected,
            )
        } else {
            null
        }

        mapAdapter.setMarkers(persistentMarkers + listOfNotNull(encounterMarker))
        mapAdapter.setOverlayState(visual?.overlays ?: MapOverlaySemanticState(reducedMotion = reducedMotion))
    }

    private fun encounterMarkerTitle(selection: EncounterSelection): String =
        when (selection.encounter.phase) {
            EncounterPhase.HIDDEN -> "? · ${selection.poi.name}"
            EncounterPhase.HINTED -> "신호 · ${selection.poi.name}"
            EncounterPhase.DISCOVERED -> "조사 · ${selection.poi.name}"
            EncounterPhase.RESOLVED -> "해결 · ${selection.poi.name}"
        }
}
