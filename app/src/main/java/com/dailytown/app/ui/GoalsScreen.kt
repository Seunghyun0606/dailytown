package com.dailytown.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.progress.GoalDefinition
import com.dailytown.app.progress.GoalMetric
import com.dailytown.app.progress.GoalProgressEvaluator
import java.time.LocalDate

@Composable
internal fun GoalsScreen(
    progress: ExplorationProgress?,
    dailyGoals: List<GoalDefinition>,
    weeklyGoals: List<GoalDefinition>,
) {
    val evaluator = remember { GoalProgressEvaluator() }
    ScreenColumn(title = "오늘의 산책", subtitle = "작은 목표를 채우면서 새로운 동네 이야기를 만나보세요.") {
        GoalGroup("오늘", dailyGoals, progress, evaluator)
        GoalGroup("이번 주", weeklyGoals, progress, evaluator)
    }
}

@Composable
private fun GoalGroup(
    title: String,
    goals: List<GoalDefinition>,
    progress: ExplorationProgress?,
    evaluator: GoalProgressEvaluator,
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (progress == null || goals.isEmpty()) {
                Text("진행도를 불러오는 중입니다.")
            } else {
                goals.forEach { goal ->
                    val state = evaluator.evaluate(goal, progress, LocalDate.now())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${if (state.isComplete) "✓" else "○"} ${goalLabel(goal)}")
                        Text("${state.current}/${state.target}")
                    }
                }
            }
        }
    }
}

private fun goalLabel(goal: GoalDefinition): String = when (goal.metric) {
    GoalMetric.WALK_DISTANCE_METERS -> "${goal.target}m 걷기"
    GoalMetric.DISCOVER_SPOT -> "새 장소 ${goal.target}곳 발견"
    GoalMetric.RESOLVE_MYSTERY -> "미스터리 ${goal.target}건 해결"
    GoalMetric.COLLECT_CLUE -> "단서 ${goal.target}개 모으기"
}
