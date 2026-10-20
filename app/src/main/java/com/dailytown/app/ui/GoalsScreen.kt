package com.dailytown.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
    onOpenExplore: () -> Unit,
) {
    val evaluator = remember { GoalProgressEvaluator() }
    ScreenColumn(
        title = "오늘의 산책",
        subtitle = "점수를 채우는 화면보다, 다시 밖으로 나갈 작은 이유를 남겨둘게요.",
    ) {
        GoalGroup("오늘 해볼 작은 일", dailyGoals, progress, evaluator)
        GoalGroup("이번 주에 천천히", weeklyGoals, progress, evaluator)
        Button(
            onClick = onOpenExplore,
            modifier = Modifier.fillMaxWidth().testTag("goals-open-explore"),
        ) {
            Text("탐험으로 돌아가기")
        }
    }
}

@Composable
private fun GoalGroup(
    title: String,
    goals: List<GoalDefinition>,
    progress: ExplorationProgress?,
    evaluator: GoalProgressEvaluator,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            if (progress == null || goals.isEmpty()) {
                Text("산책 목표를 준비하고 있어요.")
            } else {
                goals.forEach { goal ->
                    val state = evaluator.evaluate(goal, progress, LocalDate.now())
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("${if (state.isComplete) "✓" else "○"} ${goalLabel(goal)}")
                            Text("${state.current}/${state.target}", style = MaterialTheme.typography.labelMedium)
                        }
                        Text(
                            if (state.isComplete) "완료했어요. 다음 산책에서는 다른 이야기를 찾아봐요."
                            else "이 목표는 탐험 중 자연스럽게 진행돼요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun goalLabel(goal: GoalDefinition): String = when (goal.metric) {
    GoalMetric.WALK_DISTANCE_METERS -> "${goal.target}m 가볍게 걷기"
    GoalMetric.DISCOVER_SPOT -> "새 장소 ${goal.target}곳 만나기"
    GoalMetric.RESOLVE_MYSTERY -> "동네 이야기 ${goal.target}건 마무리하기"
    GoalMetric.COLLECT_CLUE -> "산책 중 단서 ${goal.target}개 살펴보기"
}
