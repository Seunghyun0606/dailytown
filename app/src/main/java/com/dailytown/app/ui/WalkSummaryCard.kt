package com.dailytown.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailytown.app.progress.WalkSessionSummary

/** End-of-walk recap: what happened, how Moru felt, then back to Records or done. */
@Composable
internal fun WalkSummaryCard(
    summary: WalkSessionSummary,
    companionName: String,
    onOpenRecords: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("walk-summary"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("오늘의 산책을 마쳤어요", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                "${summary.distanceMeters}m · ${formatDuration(summary.durationSeconds)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("새 장소", "${summary.newPlaces}곳")
                SummaryStat("다시 온 곳", "${summary.revisitedPlaces}곳")
                SummaryStat("단서", "${summary.cluesFound}개")
                SummaryStat("해결", "${summary.mysteriesResolved}건", Modifier.testTag("walk-summary-resolved"))
            }
            if (summary.memoriesGained > 0 || summary.bondDelta > 0) {
                Text(
                    buildString {
                        append("${companionName}와의 기억 +${summary.memoriesGained}")
                        if (summary.bondDelta > 0) append(" · 호감도 ${summary.bondBefore} → ${summary.bondAfter}")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("walk-summary-relationship"),
                )
            }
            if (summary.goalsCompleted.isNotEmpty()) {
                Text(
                    "완료한 목표 · ${summary.goalsCompleted.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("walk-summary-goals"),
                )
            }
            Text("$companionName: “${summary.moruClosingLine}”", style = MaterialTheme.typography.bodyLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenRecords,
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("walk-summary-records"),
                ) { Text("오늘의 기록 보기") }
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("walk-summary-close"),
                ) { Text("끝내기") }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, modifier: Modifier = Modifier) {
    // One TalkBack unit per stat ("해결 1건") instead of two loose labels.
    Column(modifier.semantics(mergeDescendants = true) {}) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    return if (minutes < 1) "${seconds}초" else "${minutes}분"
}
