package com.dailytown.app.mystery

import org.junit.Assert.assertEquals
import org.junit.Test

class MysteryReducerTest {
    private val template = MysteryTemplate("trace", MysteryMechanic.TRACE_CHAIN, requiredClues = 2)
    private val reducer = MysteryReducer(mapOf(template.id to template))
    private val initial = MysteryEncounter("enc-1", template.id, "poi-1")

    @Test
    fun followsEncounterStateMachineAndRequiresCluesToResolve() {
        val hinted = reducer.reduce(initial, EncounterEvent.EnterHintRadius)
        val discovered = reducer.reduce(hinted, EncounterEvent.ReachSpot)
        val oneClue = reducer.reduce(discovered, EncounterEvent.CollectClue("c1"))
        val tooEarly = reducer.reduce(oneClue, EncounterEvent.Resolve)
        val twoClues = reducer.reduce(tooEarly, EncounterEvent.CollectClue("c2"))
        val resolved = reducer.reduce(twoClues, EncounterEvent.Resolve)

        assertEquals(EncounterPhase.HINTED, hinted.phase)
        assertEquals(EncounterPhase.DISCOVERED, discovered.phase)
        assertEquals(EncounterPhase.DISCOVERED, tooEarly.phase)
        assertEquals(EncounterPhase.RESOLVED, resolved.phase)
    }

    @Test
    fun collectingSameClueIsIdempotent() {
        val discovered = initial.copy(phase = EncounterPhase.DISCOVERED)
        val once = reducer.reduce(discovered, EncounterEvent.CollectClue("c1"))
        val twice = reducer.reduce(once, EncounterEvent.CollectClue("c1"))
        assertEquals(setOf("c1"), twice.clueIds)
    }
}
