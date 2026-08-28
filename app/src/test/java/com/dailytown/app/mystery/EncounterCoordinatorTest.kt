package com.dailytown.app.mystery

import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.poi.Poi
import com.dailytown.app.poi.PoiCategory
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EncounterCoordinatorTest {
    private val template = MysteryTemplate(
        id = "test-template",
        mechanic = MysteryMechanic.TRACE_CHAIN,
        requiredClues = 1,
        rarity = EncounterRarity.COMMON,
    )
    private val poi = Poi(
        id = "test-poi",
        name = "테스트 장소",
        position = GeoPoint(37.56650, 126.97800),
        districtKey = "test-district",
        category = PoiCategory.LANDMARK,
    )

    @Test
    fun `selects and hints encounter inside hint radius`() {
        val coordinator = EncounterCoordinator(templates = listOf(template))

        val step = coordinator.advance(
            current = null,
            user = GeoPoint(37.56750, 126.97800),
            nearbyPois = listOf(poi),
            runtime = EncounterRuntimeContext(),
            date = LocalDate.of(2026, 8, 24),
            time = LocalTime.NOON,
        )

        assertNotNull(step.selection)
        assertEquals(EncounterPhase.HINTED, step.selection?.encounter?.phase)
        assertEquals(EncounterTransition.HINTED, step.transition)
    }

    @Test
    fun `can move directly to discovered when first sample is close`() {
        val coordinator = EncounterCoordinator(templates = listOf(template))

        val step = coordinator.advance(
            current = null,
            user = GeoPoint(37.56655, 126.97800),
            nearbyPois = listOf(poi),
            runtime = EncounterRuntimeContext(companionBond = 50),
            date = LocalDate.of(2026, 8, 24),
            time = LocalTime.of(18, 0),
        )

        assertEquals(EncounterPhase.DISCOVERED, step.selection?.encounter?.phase)
        assertEquals(EncounterTransition.DISCOVERED, step.transition)
    }

    @Test
    fun `resolved encounter is stable and does not emit another transition`() {
        val coordinator = EncounterCoordinator(templates = listOf(template))
        val initial = coordinator.advance(
            current = null,
            user = poi.position,
            nearbyPois = listOf(poi),
            runtime = EncounterRuntimeContext(),
            date = LocalDate.of(2026, 8, 24),
            time = LocalTime.NOON,
        ).selection!!
        val resolved = initial.copy(
            encounter = initial.encounter.copy(phase = EncounterPhase.RESOLVED),
        )

        val step = coordinator.advance(
            current = resolved,
            user = poi.position,
            nearbyPois = emptyList(),
            runtime = EncounterRuntimeContext(),
            date = LocalDate.of(2026, 8, 24),
            time = LocalTime.NOON,
        )

        assertEquals(resolved, step.selection)
        assertEquals(EncounterTransition.NONE, step.transition)
    }

    @Test
    fun `reset restarts encounter id sequence`() {
        val coordinator = EncounterCoordinator(templates = listOf(template))
        val argsDate = LocalDate.of(2026, 8, 24)
        val argsTime = LocalTime.NOON

        val first = coordinator.advance(
            current = null,
            user = poi.position,
            nearbyPois = listOf(poi),
            runtime = EncounterRuntimeContext(),
            date = argsDate,
            time = argsTime,
        ).selection!!

        coordinator.reset()
        val afterReset = coordinator.advance(
            current = null,
            user = poi.position,
            nearbyPois = listOf(poi),
            runtime = EncounterRuntimeContext(),
            date = argsDate,
            time = argsTime,
        ).selection!!

        assertEquals(first.encounter.id, afterReset.encounter.id)
    }
}
