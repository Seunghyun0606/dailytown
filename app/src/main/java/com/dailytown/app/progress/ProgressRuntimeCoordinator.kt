package com.dailytown.app.progress

import com.dailytown.app.domain.ExplorationState
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.ProgressStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

data class ProgressRuntimeState(
    val progress: ExplorationProgress = ExplorationProgress(),
    val dailyGoals: List<GoalDefinition> = emptyList(),
    val weeklyGoals: List<GoalDefinition> = emptyList(),
    val ready: Boolean = false,
    val persistenceEnabled: Boolean = false,
    /** Increments on every explicit player data reset so live runtimes can drop in-memory session state. */
    val resetGeneration: Int = 0,
)

/**
 * Owns persisted progress plus goal rotation as one application-level runtime boundary.
 *
 * Compose should observe [state] and express user/game events as mutations instead of
 * separately coordinating load, period rollover, exploration sync, and goal selection.
 * Persistence failures are intentionally propagated so the UI can surface them without
 * corrupting the in-memory runtime state.
 */
class ProgressRuntimeCoordinator(
    private val store: ProgressStore,
    private val goalRotationCoordinator: GoalRotationCoordinator = GoalRotationCoordinator(),
) {
    private val _state = MutableStateFlow(ProgressRuntimeState())
    val state: StateFlow<ProgressRuntimeState> = _state.asStateFlow()

    suspend fun restore(date: LocalDate): ProgressRuntimeState {
        val restored = store.load()
        return replace(
            result = goalRotationCoordinator.ensure(restored, date),
            ready = true,
            persistenceEnabled = true,
        )
    }

    /**
     * Explicit fallback used only after a load failure. It keeps the app usable while preserving
     * the failure signal for UI/diagnostics instead of making restore() silently swallow errors.
     * Persistence remains disabled for the fallback session so an empty/default state cannot
     * overwrite previously stored progress after a transient read failure.
     */
    fun activateFallback(date: LocalDate): ProgressRuntimeState =
        replace(
            result = goalRotationCoordinator.ensure(_state.value.progress, date),
            ready = true,
            persistenceEnabled = false,
        )

    fun ensureCurrentPeriod(date: LocalDate): ProgressRuntimeState {
        val current = _state.value
        if (!current.ready) return current
        return replace(
            result = goalRotationCoordinator.ensure(current.progress, date),
            ready = true,
            persistenceEnabled = current.persistenceEnabled,
        )
    }

    fun syncExploration(explorationState: ExplorationState, date: LocalDate): ProgressRuntimeState =
        mutate(date) { progress -> progress.syncExploration(explorationState, date) }

    fun mutate(
        date: LocalDate,
        transform: (ExplorationProgress) -> ExplorationProgress,
    ): ProgressRuntimeState {
        val current = _state.value
        if (!current.ready) return current
        val transformed = transform(current.progress)
        return replace(
            result = goalRotationCoordinator.ensure(transformed, date),
            ready = true,
            persistenceEnabled = current.persistenceEnabled,
        )
    }

    /**
     * Player-initiated "delete my records": replaces progress with a fresh state for [date] and
     * persists it immediately. Only allowed when persistence is healthy so a fallback session can
     * never wipe stored data it could not read.
     */
    suspend fun resetAll(date: LocalDate): ProgressRuntimeState {
        val current = _state.value
        check(current.ready && current.persistenceEnabled) { "Progress reset requires a healthy restored store" }
        val next = replace(
            result = goalRotationCoordinator.ensure(ExplorationProgress(), date),
            ready = true,
            persistenceEnabled = true,
            resetGeneration = current.resetGeneration + 1,
        )
        store.save(next.progress)
        return next
    }

    suspend fun persist() {
        val current = _state.value
        if (current.ready && current.persistenceEnabled) store.save(current.progress)
    }

    private fun replace(
        result: GoalRotationResult,
        ready: Boolean,
        persistenceEnabled: Boolean,
        resetGeneration: Int = _state.value.resetGeneration,
    ): ProgressRuntimeState {
        val next = ProgressRuntimeState(
            progress = result.progress,
            dailyGoals = result.dailyGoals,
            weeklyGoals = result.weeklyGoals,
            ready = ready,
            persistenceEnabled = persistenceEnabled,
            resetGeneration = resetGeneration,
        )
        if (_state.value != next) _state.value = next
        return _state.value
    }
}
