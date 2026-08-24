package com.dailytown.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dailytown.app.diagnostics.FieldTestDiagnostic
import com.dailytown.app.diagnostics.FieldTestRunCheck
import com.dailytown.app.diagnostics.FieldTestRunCheckStatus
import com.dailytown.app.diagnostics.FieldTestRunChecklist
import com.dailytown.app.diagnostics.FieldTestRunReviewStatus

@Composable
internal fun FieldTestRunSummary(
    checklist: FieldTestRunChecklist,
    diagnostic: FieldTestDiagnostic,
) {
    OutlinedCard(Modifier.fillMaxWidth().testTag("field-test-run-summary")) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "런 요약: ${runReviewLabel(checklist.status)}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.testTag("field-test-run-status"),
            )
            Text(
                when (checklist.status) {
                    FieldTestRunReviewStatus.REFERENCE_ONLY ->
                        "런 단위 승인 기준이 없어 수집된 값만 참고합니다. 제품 판정으로 사용하지 않습니다."
                    FieldTestRunReviewStatus.REVIEWABLE ->
                        "현재 설정된 런 단위 필수 evidence와 acceptance를 충족했습니다. multi-session 제품 검토 상태와는 별개입니다."
                    FieldTestRunReviewStatus.NEEDS_ATTENTION ->
                        "현재 설정된 런 단위 조건 중 누락 또는 미충족 항목이 있습니다. 기록은 가능하며 누락값은 0으로 대체하지 않습니다."
                },
                style = MaterialTheme.typography.bodySmall,
            )

            RunCheckRow(
                check = checklist.checks.first { it.key == "map" },
                label = "지도",
                value = diagnostic.mapHealthStatus ?: "상태 없음",
            )
            RunCheckRow(
                check = checklist.checks.first { it.key == "session" },
                label = "세션",
                value = buildList {
                    diagnostic.trackingDurationSeconds?.let { add("${it}초") }
                    diagnostic.sessionDistanceMeters?.let { add("${it}m") }
                }.ifEmpty { listOf("evidence 없음") }.joinToString(" · "),
            )
            RunCheckRow(
                check = checklist.checks.first { it.key == "route" },
                label = "기준거리/오차",
                value = if (diagnostic.referenceDistanceMeters != null && diagnostic.distanceErrorPercent != null) {
                    "${diagnostic.referenceDistanceMeters}m · 오차 ${diagnostic.distanceErrorPercent}%"
                } else {
                    "evidence 없음"
                },
            )
            RunCheckRow(
                check = checklist.checks.first { it.key == "battery" },
                label = "배터리",
                value = diagnostic.batteryDrainPercentPerHour?.let {
                    "시간당 ${it}%p · ${diagnostic.batteryMeasurementStatus ?: "상태 없음"}"
                } ?: "evidence 없음 · ${diagnostic.batteryMeasurementStatus ?: "상태 없음"}",
            )
            RunCheckRow(
                check = checklist.checks.first { it.key == "gps" },
                label = "GPS",
                value = diagnostic.rejectedLocationRatePercent?.let {
                    "제외율 ${it}% · accepted ${diagnostic.acceptedLocationCount ?: 0} / rejected ${diagnostic.rejectedLocationCount}"
                } ?: "evidence 없음",
            )
            RunCheckRow(
                check = checklist.checks.first { it.key == "gameplay" },
                label = "게임플레이",
                value = buildList {
                    diagnostic.sessionEncounterDiscoveredCount?.let { add("발견 $it") }
                    diagnostic.sessionEncounterResolutionRatePercent?.let { add("해결률 $it%") }
                    diagnostic.sessionRevisitSharePercent?.let { add("재방문 $it%") }
                    diagnostic.repeatAreaFatigueProxyPercent?.let { add("피로 $it%") }
                }.ifEmpty { listOf("evidence 없음") }.joinToString(" · "),
            )
            RunCheckRow(
                check = checklist.checks.first { it.key == "acceptance" },
                label = "single-session acceptance",
                value = buildString {
                    append(diagnostic.acceptanceOverall)
                    if (diagnostic.acceptanceFailedKeys.isNotEmpty()) {
                        append(" · 실패 ${diagnostic.acceptanceFailedKeys.sorted().joinToString(", ")}")
                    }
                },
            )

            if (checklist.missingRequiredEvidence.isNotEmpty()) {
                Text(
                    "필수 evidence 누락: ${checklist.missingRequiredEvidence.sortedBy { it.name }.joinToString(", ") { evidenceLabel(it) }}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("field-test-run-missing-evidence"),
                )
            }
        }
    }
}

@Composable
private fun RunCheckRow(
    check: FieldTestRunCheck,
    label: String,
    value: String,
) {
    Text(
        "${runCheckMarker(check)} $label · $value",
        color = if (check.required && check.status != FieldTestRunCheckStatus.COMPLETE) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag("field-test-run-check-${check.key}"),
    )
}

private fun runReviewLabel(status: FieldTestRunReviewStatus): String = when (status) {
    FieldTestRunReviewStatus.REFERENCE_ONLY -> "참고용"
    FieldTestRunReviewStatus.REVIEWABLE -> "검토 가능"
    FieldTestRunReviewStatus.NEEDS_ATTENTION -> "확인 필요"
}

private fun runCheckMarker(check: FieldTestRunCheck): String = when {
    check.required && check.status == FieldTestRunCheckStatus.COMPLETE -> "✓ 필수"
    check.required && check.status == FieldTestRunCheckStatus.FAIL -> "✕ 필수"
    check.required -> "⚠ 필수"
    check.status == FieldTestRunCheckStatus.COMPLETE -> "✓ 수집"
    else -> "· 선택"
}
