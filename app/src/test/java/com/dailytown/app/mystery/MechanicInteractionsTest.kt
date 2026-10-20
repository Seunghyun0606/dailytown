package com.dailytown.app.mystery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MechanicInteractionsTest {
    @Test
    fun everyMechanicHasAValidInteractionForEveryClueStep() {
        MysteryMechanic.entries.forEach { mechanic ->
            (1..3).forEach { clue ->
                val interaction = MechanicInteractions.forStep(mechanic, "enc-s1-0:poi:$mechanic", clue)
                assertEquals(mechanic, interaction.mechanic)
                assertTrue(interaction.prompt.isNotBlank())
                assertTrue(interaction.options.all { it.isNotBlank() })
                assertTrue("at most 3 actions", interaction.answer.size <= 3)
            }
        }
    }

    @Test
    fun displayOrderIsDeterministicPerEncounterAndStep() {
        val a = MechanicInteractions.forStep(MysteryMechanic.TIME_LAYER, "enc-x", 2)
        val b = MechanicInteractions.forStep(MysteryMechanic.TIME_LAYER, "enc-x", 2)
        assertEquals(a, b)
        assertEquals("the answer always points at the authored correct option", "닳아서 흐려진 숫자 표식", a.options[a.answer.single()])
    }

    @Test
    fun choiceSolvesOnTheCorrectOptionAndWrongPicksOnlyReset() {
        val interaction = MechanicInteractions.forStep(MysteryMechanic.SYMBOL_MATCH, "enc-y", 2)
        val wrong = interaction.options.indices.first { it != interaction.answer.single() }
        assertEquals(InteractionProgress.Wrong, MechanicInteractions.pick(interaction, emptyList(), wrong))
        assertEquals(InteractionProgress.Solved, MechanicInteractions.pick(interaction, emptyList(), interaction.answer.single()))
    }

    @Test
    fun sequenceNeedsTheWholeOrderIncludingRepeatedBeats() {
        val rhythm = MechanicInteractions.forStep(MysteryMechanic.SOUND_PATTERN, "enc-z", 2)
        assertEquals(InteractionKind.SEQUENCE, rhythm.kind)
        assertEquals(listOf("짧게 ♪", "길게 ♩"), rhythm.options)
        assertEquals(listOf(0, 0, 1), rhythm.answer)
        var picks = emptyList<Int>()
        listOf(0, 0).forEach { beat ->
            val step = MechanicInteractions.pick(rhythm, picks, beat)
            assertTrue(step is InteractionProgress.Partial)
            picks = (step as InteractionProgress.Partial).picks
        }
        assertEquals(InteractionProgress.Solved, MechanicInteractions.pick(rhythm, picks, 1))
        assertEquals(InteractionProgress.Wrong, MechanicInteractions.pick(rhythm, listOf(0), 1))
    }

    @Test
    fun traceChainFollowsTheNoteOrderWhateverTheShuffle() {
        val trace = MechanicInteractions.forStep(MysteryMechanic.TRACE_CHAIN, "enc-shuffled", 2)
        assertEquals(listOf("잎자국이 남은 벽", "돌계단 모서리", "가로등 아래"), trace.answer.map { trace.options[it] })
    }
}
