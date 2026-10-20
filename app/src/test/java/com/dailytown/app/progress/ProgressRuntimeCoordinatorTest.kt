package com.dailytown.app.progress

import com.dailytown.app.domain.Companion
import com.dailytown.app.domain.ExplorationState
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.ProgressStore
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressRuntimeCoordinatorTest {
    @Test
    fun `restore normalizes periods and selects goals`() = runBlocking {
        val store = FakeProgressStore(
            ExplorationProgress(distanceWalkedMeters = 120.0),
        )
        val coordinator = ProgressRuntimeCoordinator(store)

        val state = coordinator.restore(LocalDate.of(2026, 8, 24))

        assertTrue(state.ready)
        assertTrue(state.persistenceEnabled)
        assertEquals("2026-08-24", state.progress.daily.periodKey)
        assertTrue(state.dailyGoals.isNotEmpty())
        assertTrue(state.weeklyGoals.isNotEmpty())
    }

    @Test
    fun `sync exploration updates derived distance without losing rotated goals`() = runBlocking {
        val coordinator = ProgressRuntimeCoordinator(FakeProgressStore())
        val date = LocalDate.of(2026, 8, 24)
        val restored = coordinator.restore(date)
        val companion = Companion("moru", "모루", 12)

        val updated = coordinator.syncExploration(
            ExplorationState(
                companion = companion,
                distanceWalkedMeters = 350.0,
            ),
            date,
        )

        assertEquals(350.0, updated.progress.distanceWalkedMeters, 0.0)
        assertEquals(350.0, updated.progress.daily.distanceWalkedMeters, 0.0)
        assertEquals(restored.dailyGoals.map { it.id }, updated.dailyGoals.map { it.id })
    }

    @Test
    fun `mutations before restore are ignored to protect unloaded progress`() {
        val coordinator = ProgressRuntimeCoordinator(FakeProgressStore())

        val state = coordinator.mutate(LocalDate.of(2026, 8, 24)) {
            it.copy(companionBond = 99)
        }

        assertFalse(state.ready)
        assertFalse(state.persistenceEnabled)
        assertEquals(0, state.progress.companionBond)
    }

    @Test
    fun `fallback keeps runtime usable but disables persistence`() = runBlocking {
        val store = FakeProgressStore()
        val coordinator = ProgressRuntimeCoordinator(store)
        val date = LocalDate.of(2026, 8, 24)

        val state = coordinator.activateFallback(date)
        coordinator.mutate(date) { it.copy(companionBond = 55) }
        coordinator.persist()

        assertTrue(state.ready)
        assertFalse(state.persistenceEnabled)
        assertEquals("2026-08-24", state.progress.daily.periodKey)
        assertTrue(state.dailyGoals.isNotEmpty())
        assertNull(store.saved)
    }

    @Test
    fun `persist writes the current runtime progress`() = runBlocking {
        val store = FakeProgressStore()
        val coordinator = ProgressRuntimeCoordinator(store)
        val date = LocalDate.of(2026, 8, 24)
        coordinator.restore(date)
        coordinator.mutate(date) { it.copy(companionBond = 33) }

        coordinator.persist()

        assertEquals(33, store.saved?.companionBond)
    }

    @Test
    fun `reset wipes and persists progress and bumps the reset generation`() = runBlocking {
        val store = FakeProgressStore(
            ExplorationProgress(distanceWalkedMeters = 900.0, companionBond = 40, companionMemoryKeys = setOf("poi:a")),
        )
        val coordinator = ProgressRuntimeCoordinator(store)
        val date = LocalDate.of(2026, 10, 20)
        coordinator.restore(date)

        val reset = coordinator.resetAll(date)

        assertEquals(1, reset.resetGeneration)
        assertEquals(0.0, reset.progress.distanceWalkedMeters, 0.0)
        assertTrue(reset.progress.companionMemoryKeys.isEmpty())
        assertTrue(reset.dailyGoals.isNotEmpty())
        assertEquals(reset.progress, store.saved)
        assertEquals("generation survives later mutations", 1, coordinator.mutate(date) { it }.resetGeneration)
    }

    @Test(expected = IllegalStateException::class)
    fun `reset is refused in fallback mode so unreadable stored data is never wiped`() {
        runBlocking {
            val coordinator = ProgressRuntimeCoordinator(FakeProgressStore())
            coordinator.activateFallback(LocalDate.of(2026, 10, 20))
            coordinator.resetAll(LocalDate.of(2026, 10, 20))
        }
    }

    private class FakeProgressStore(
        private var loaded: ExplorationProgress = ExplorationProgress(),
    ) : ProgressStore {
        var saved: ExplorationProgress? = null

        override suspend fun load(): ExplorationProgress = loaded

        override suspend fun save(progress: ExplorationProgress) {
            saved = progress
            loaded = progress
        }
    }
}
