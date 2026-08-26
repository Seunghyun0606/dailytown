package com.dailytown.app.ui.visual

import android.content.Context
import android.view.View
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.map.MapHealth
import com.dailytown.app.map.MapHealthStatus
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapProviderId
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.map.UserLocationSpec
import com.dailytown.app.mystery.EncounterContext
import com.dailytown.app.mystery.EncounterPhase
import com.dailytown.app.mystery.EncounterSelection
import com.dailytown.app.mystery.MysteryEncounter
import com.dailytown.app.mystery.MysteryMechanic
import com.dailytown.app.mystery.MysteryTemplate
import com.dailytown.app.mystery.TimeBand
import com.dailytown.app.poi.Poi
import com.dailytown.app.poi.PoiCategory
import com.dailytown.app.visual.MapHaloVisualState
import com.dailytown.app.visual.MapOverlaySemanticState
import com.dailytown.app.visual.MarkerSemantic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapGameplayVisualBinderTest {
    @Test
    fun hiddenEncounterKeepsOnlyPersistentMarkers() {
        val adapter = RecordingMapAdapter()
        val binder = MapGameplayVisualBinder(adapter)
        val persistent = listOf(MapMarkerSpec("ambient", "ambient", GeoPoint(37.0, 127.0)))

        binder.applyEncounter(selection(EncounterPhase.HIDDEN), persistent)

        assertEquals(persistent, adapter.markers)
        assertEquals(MapOverlaySemanticState(), adapter.overlayState)
    }

    @Test
    fun discoveredEncounterBecomesSelectedActiveMarkerAndHalo() {
        val adapter = RecordingMapAdapter()
        val binder = MapGameplayVisualBinder(adapter)

        binder.applyEncounter(selection(EncounterPhase.DISCOVERED), emptyList())

        assertEquals(1, adapter.markers.size)
        val marker = adapter.markers.single()
        assertEquals(MarkerSemantic.ENCOUNTER_ACTIVE, marker.semantic)
        assertTrue(marker.selected)
        assertEquals(MapHaloVisualState.ACTIVE, adapter.overlayState.haloState)
    }

    @Test
    fun revisitUsesDedicatedSemanticAndReducedMotionPropagates() {
        val adapter = RecordingMapAdapter()
        val binder = MapGameplayVisualBinder(adapter)

        binder.applyEncounter(
            selection = selection(EncounterPhase.DISCOVERED, isRevisit = true),
            persistentMarkers = emptyList(),
            reducedMotion = true,
        )

        val marker = adapter.markers.single()
        assertEquals(MarkerSemantic.ENCOUNTER_REVISIT, marker.semantic)
        assertTrue(marker.selected)
        assertTrue(adapter.overlayState.reducedMotion)
    }

    @Test
    fun resolvedEncounterIsSolvedAndNoLongerSelected() {
        val adapter = RecordingMapAdapter()
        val binder = MapGameplayVisualBinder(adapter)

        binder.applyEncounter(selection(EncounterPhase.RESOLVED), emptyList())

        val marker = adapter.markers.single()
        assertEquals(MarkerSemantic.ENCOUNTER_SOLVED, marker.semantic)
        assertFalse(marker.selected)
        assertEquals(MapHaloVisualState.IDLE, adapter.overlayState.haloState)
    }

    @Test
    fun clearingEncounterAlsoClearsSemanticOverlayState() {
        val adapter = RecordingMapAdapter()
        val binder = MapGameplayVisualBinder(adapter)
        binder.applyEncounter(selection(EncounterPhase.DISCOVERED), emptyList())

        binder.applyEncounter(null, emptyList(), reducedMotion = true)

        assertTrue(adapter.markers.isEmpty())
        assertEquals(MapOverlaySemanticState(reducedMotion = true), adapter.overlayState)
    }

    private fun selection(phase: EncounterPhase, isRevisit: Boolean = false): EncounterSelection {
        val poi = Poi(
            id = "poi-1",
            name = "Test POI",
            position = GeoPoint(37.5, 127.0),
            districtKey = "test",
            category = PoiCategory.OTHER,
        )
        val template = MysteryTemplate("template-1", MysteryMechanic.TRACE_CHAIN, requiredClues = 1)
        return EncounterSelection(
            poi = poi,
            template = template,
            encounter = MysteryEncounter(
                id = "enc-1",
                templateId = template.id,
                poiId = poi.id,
                phase = phase,
            ),
            context = EncounterContext("2026-08-27", TimeBand.DAY, companionBond = 12),
            isRevisit = isRevisit,
        )
    }

    private class RecordingMapAdapter : MapViewAdapter {
        override val providerId: MapProviderId = MapProviderId.NAVER
        private val mutableHealth = MutableStateFlow(MapHealth(MapHealthStatus.READY))
        override val health: StateFlow<MapHealth> = mutableHealth
        var markers: List<MapMarkerSpec> = emptyList()
        var overlayState: MapOverlaySemanticState = MapOverlaySemanticState()

        override fun createView(context: Context): View = error("Not used in JVM binder test")
        override fun setMarkers(markers: List<MapMarkerSpec>) {
            this.markers = markers
        }
        override fun setOverlayState(state: MapOverlaySemanticState) {
            overlayState = state
        }
        override fun setUserLocation(location: UserLocationSpec?) = Unit
    }
}
