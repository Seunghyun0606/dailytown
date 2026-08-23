package com.dailytown.app.persistence

import org.junit.Assert.assertEquals
import org.junit.Test

class CompanionMemoryTest {
    @Test
    fun `recording the same semantic memory twice remains idempotent`() {
        val progress = ExplorationProgress()
            .recordMemory("poi:city-hall")
            .recordMemory("poi:city-hall")
            .recordMemory("mechanic:TIME_LAYER")

        assertEquals(2, progress.companionMemoryKeys.size)
        assertEquals(
            setOf("poi:city-hall", "mechanic:TIME_LAYER"),
            progress.companionMemoryKeys,
        )
    }
}
