package com.dailytown.app.ui.visual

import com.dailytown.app.map.MapAreaEmphasis
import com.dailytown.app.map.MapEncounterAreaSpec
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.mystery.EncounterPhase
import com.dailytown.app.mystery.EncounterProximityController
import com.dailytown.app.mystery.EncounterSelection
import com.dailytown.app.visual.MapOverlaySemanticState
import java.time.LocalTime

/**
 * Application-layer bridge from current visual context and encounter gameplay state to
 * provider-neutral map semantics.
 *
 * This binder does not know NAVER/Google types, raw visual assets, route geometry, or visual timing.
 * Existing ambient/persistent markers are supplied by the caller and kept independent from the
 * short-lived active encounter marker. Time is injected so replay/screenshot callers can remain
 * deterministic instead of reading the device clock implicitly.
 */
class MapGameplayVisualBinder(
    private val mapAdapter: MapViewAdapter,
    private val themeResolver: MapRuntimeThemeResolver = MapRuntimeThemeResolver(),
    private val currentTime: () -> LocalTime = LocalTime::now,
) {
    fun applyEncounter(
        selection: EncounterSelection?,
        persistentMarkers: List<MapMarkerSpec>,
        reducedMotion: Boolean = false,
    ) {
        mapAdapter.setTheme(themeResolver.resolve(currentTime()).mapTheme)

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
        mapAdapter.setEncounterArea(encounterArea(selection))
        mapAdapter.setOverlayState(visual?.overlays ?: MapOverlaySemanticState(reducedMotion = reducedMotion))
    }

    /** The real 60 m discovery zone while a signal is followed or investigated; nothing otherwise. */
    private fun encounterArea(selection: EncounterSelection?): MapEncounterAreaSpec? {
        val emphasis = when (selection?.encounter?.phase) {
            EncounterPhase.HINTED -> MapAreaEmphasis.DISCOVERY_ZONE
            EncounterPhase.DISCOVERED -> MapAreaEmphasis.ACTIVE
            else -> return null
        }
        return MapEncounterAreaSpec(
            id = "area-${selection.encounter.id}",
            center = selection.poi.position,
            radiusMeters = EncounterProximityController.DISCOVERY_RADIUS_METERS,
            emphasis = emphasis,
        )
    }

    private fun encounterMarkerTitle(selection: EncounterSelection): String =
        when (selection.encounter.phase) {
            EncounterPhase.HIDDEN -> "? · ${selection.poi.name}"
            EncounterPhase.HINTED -> "신호 · ${selection.poi.name}"
            EncounterPhase.DISCOVERED -> "조사 · ${selection.poi.name}"
            EncounterPhase.RESOLVED -> "해결 · ${selection.poi.name}"
        }
}
