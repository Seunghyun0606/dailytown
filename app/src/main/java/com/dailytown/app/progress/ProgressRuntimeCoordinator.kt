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
        return replace(goalRotationCoordinator.ensure(restored, date), ready = true)
    }

    fun ensureCurrentPeriod(date: LocalDate): ProgressRuntimeState {
        val current = _state.value
        if (!current.ready) return current
        return replace(goalRotationCoordinator.ensure(current.progress, date), ready = true)
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
        return replace(goalRotationCoordinator.ensure(transformed, date), ready = true)
    }

    suspend fun persist() {
        val current = _state.value
        if (current.ready) store.save(current.progress)
    }

    private fun replace(result: GoalRotationResult, ready: Boolean): ProgressRuntimeState {
        val next = ProgressRuntimeState(
            progress = result.progress,
            dailyGoals = result.dailyGoals,
            weeklyGoals = result.weeklyGoals,
            ready = ready,
        )
        if (_state.value != next) _state.value = next
        return _state.value
    }
}
