package com.dailytown.app.mystery

import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.poi.Poi
import com.dailytown.app.poi.PoiCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EncounterGeneratorTest {
    private val template = MysteryTemplate(
        id = "trace",
        mechanic = MysteryMechanic.TRACE_CHAIN,
        requiredClues = 1,
    )
    private val poi = Poi(
        id = "poi-1",
        name = "test poi",
        position = GeoPoint(37.56650, 126.97800),
        districtKey = "test-district",
        category = PoiCategory.LANDMARK,
    )
    private val context = EncounterContext(
        dayKey = "2026-08-23",
        timeBand = TimeBand.DAY,
        companionBond = 20,
    )

    @Test
    fun `generator creates deterministic selection from candidate pool`() {
        val selection = EncounterGenerator(templates = listOf(template)).choose(
            encounterKey = "session-1",
            center = GeoPoint(37.56660, 126.97800),
            pois = listOf(poi),
            context = context,
            visitedPoiIds = emptySet(),
            history = EncounterHistory(),
        )

        assertNotNull(selection)
        assertEquals("poi-1", selection?.poi?.id)
        assertEquals("trace", selection?.template?.id)
        assertEquals("session-1:poi-1:trace", selection?.encounter?.id)
    }

    @Test
    fun `revisit prefers local memory mechanic over generic common mechanic`() {
        val localMemory = MysteryTemplate(
            id = "memory-common",
            mechanic = MysteryMechanic.LOCAL_MEMORY,
            requiredClues = 1,
        )
        val selection = EncounterGenerator(templates = listOf(template, localMemory)).choose(
            encounterKey = "revisit",
            center = poi.position,
            pois = listOf(poi),
            context = context.copy(memoryKeys = setOf("poi:${poi.id}")),
            visitedPoiIds = setOf(poi.id),
            history = EncounterHistory(),
        )

        assertEquals("memory-common", selection?.template?.id)
    }

    @Test
    fun `proximity advances hidden to hinted then discovered`() {
        val reducer = MysteryReducer(mapOf(template.id to template))
        val controller = EncounterProximityController(reducer = reducer)
        val encounter = MysteryEncounter("e1", template.id, poi.id)

        val hinted = controller.advance(
            encounter = encounter,
            user = GeoPoint(37.56750, 126.97800),
            poi = poi.position,
        )
        assertEquals(EncounterPhase.HINTED, hinted.phase)

        val discovered = controller.advance(
            encounter = hinted,
            user = GeoPoint(37.56670, 126.97800),
            poi = poi.position,
        )
        assertEquals(EncounterPhase.DISCOVERED, discovered.phase)
    }
}
