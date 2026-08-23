package com.dailytown.app.mystery

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EncounterContextTest {
    @Test
    fun `time bands cover the full day`() {
        assertEquals(TimeBand.NIGHT, EncounterContextFactory.timeBand(4))
        assertEquals(TimeBand.DAWN, EncounterContextFactory.timeBand(5))
        assertEquals(TimeBand.DAY, EncounterContextFactory.timeBand(12))
        assertEquals(TimeBand.EVENING, EncounterContextFactory.timeBand(18))
        assertEquals(TimeBand.NIGHT, EncounterContextFactory.timeBand(23))
    }

    @Test
    fun `context factory uses supplied date time and memories`() {
        val context = EncounterContextFactory.create(
            date = LocalDate.of(2026, 8, 23),
            time = LocalTime.of(19, 30),
            companionBond = 42,
            memoryKeys = setOf("poi:a"),
        )

        assertEquals("2026-08-23", context.dayKey)
        assertEquals(TimeBand.EVENING, context.timeBand)
        assertEquals(42, context.companionBond)
        assertEquals(setOf("poi:a"), context.memoryKeys)
    }

    @Test
    fun `rare eligibility is deterministic for the same day poi and template`() {
        val policy = RareEncounterPolicy()
        val template = MysteryTemplate(
            id = "rare",
            mechanic = MysteryMechanic.COMPANION_SENSE,
            requiredClues = 1,
            rarity = EncounterRarity.RARE,
        )
        val context = EncounterContext("2026-08-23", TimeBand.NIGHT, 80)

        val first = policy.isEligible(template, "poi-a", context)
        repeat(20) {
            assertEquals(first, policy.isEligible(template, "poi-a", context))
        }
    }

    @Test
    fun `common encounters are always eligible`() {
        val policy = RareEncounterPolicy()
        val template = MysteryTemplate("common", MysteryMechanic.TRACE_CHAIN, 1)
        val context = EncounterContext("2026-08-23", TimeBand.DAY, 0)

        assertTrue(policy.isEligible(template, "poi-a", context))
    }
}
