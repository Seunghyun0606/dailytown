package com.dailytown.app.ui.visual

import com.dailytown.app.mystery.EncounterPhase
import com.dailytown.app.visual.MapHaloVisualState
import com.dailytown.app.visual.MapRouteVisualState
import com.dailytown.app.visual.MarkerSemantic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EncounterMapVisualResolverTest {
    @Test
    fun hiddenEncounterDoesNotExposeMarker() {
        val state = EncounterMapVisualResolver.resolve(EncounterPhase.HIDDEN, isRevisit = false)
        assertNull(state.markerSemantic)
        assertFalse(state.selected)
        assertEquals(MapHaloVisualState.IDLE, state.overlays.haloState)
    }

    @Test
    fun standardPhasesMapToHintedActiveSolvedSemantics() {
        val hinted = EncounterMapVisualResolver.resolve(EncounterPhase.HINTED, false)
        val discovered = EncounterMapVisualResolver.resolve(EncounterPhase.DISCOVERED, false)
        val resolved = EncounterMapVisualResolver.resolve(EncounterPhase.RESOLVED, false)

        assertEquals(MarkerSemantic.ENCOUNTER_HINTED, hinted.markerSemantic)
        assertEquals(MarkerSemantic.ENCOUNTER_ACTIVE, discovered.markerSemantic)
        assertTrue(discovered.selected)
        assertEquals(MapHaloVisualState.ACTIVE, discovered.overlays.haloState)
        assertEquals(MarkerSemantic.ENCOUNTER_SOLVED, resolved.markerSemantic)
        assertFalse(resolved.selected)
    }

    @Test
    fun revisitKeepsDedicatedNonColorSemantic() {
        assertEquals(
            MarkerSemantic.ENCOUNTER_REVISIT,
            EncounterMapVisualResolver.resolve(EncounterPhase.HINTED, true).markerSemantic,
        )
        assertEquals(
            MarkerSemantic.ENCOUNTER_REVISIT,
            EncounterMapVisualResolver.resolve(EncounterPhase.DISCOVERED, true).markerSemantic,
        )
    }

    @Test
    fun resolverDoesNotInventRouteGeometryOrDiscoveryIntensity() {
        EncounterPhase.values().forEach { phase ->
            val state = EncounterMapVisualResolver.resolve(phase, false)
            assertEquals(MapRouteVisualState.IDLE, state.overlays.routeState)
            assertNull(state.overlays.discoveryIntensity)
        }
    }

    @Test
    fun reducedMotionDoesNotChangeGameplaySemantic() {
        val normal = EncounterMapVisualResolver.resolve(EncounterPhase.DISCOVERED, false, false)
        val reduced = EncounterMapVisualResolver.resolve(EncounterPhase.DISCOVERED, false, true)

        assertEquals(normal.markerSemantic, reduced.markerSemantic)
        assertEquals(normal.selected, reduced.selected)
        assertEquals(normal.overlays.haloState, reduced.overlays.haloState)
        assertFalse(normal.overlays.reducedMotion)
        assertTrue(reduced.overlays.reducedMotion)
    }
}
