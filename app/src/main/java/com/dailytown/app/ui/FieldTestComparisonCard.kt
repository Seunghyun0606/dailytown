package com.dailytown.app.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dailytown.app.BuildConfig
import com.dailytown.app.diagnostics.FieldTestAreaProfile
import com.dailytown.app.diagnostics.FieldTestCohortSummary
import com.dailytown.app.diagnostics.FieldTestComparisonRecorder
import com.dailytown.app.diagnostics.FieldTestDiagnostic
import com.dailytown.app.diagnostics.FieldTestMetricAverage
import com.dailytown.app.diagnostics.FieldTestProtocolAssessment
import com.dailytown.app.diagnostics.FieldTestProtocolCriteria
import com.dailytown.app.diagnostics.FieldTestProtocolEvaluator
import com.dailytown.app.diagnostics.FieldTestProtocolEvidence
import com.dailytown.app.diagnostics.FieldTestProtocolIssue
import com.dailytown.app.diagnostics.FieldTestProtocolStatus

@Composable
internal fun FieldTestComparisonCard(
    sessionToken: Int,
    canRecordCurrentSession: Boolean,
    buildDiagnostic: () -> FieldTestDiagnostic,
) {
    val context = LocalContext.current
    val recorder = remember { FieldTestComparisonRecorder() }
    val protocolEvaluator = remember { FieldTestProtocolEvaluator() }
    val protocolCriteria = remember { buildConfigProtocolCriteria() }
    var revision by remember { mutableIntStateOf(0) }
    var selectedProfile by remember { mutableStateOf(FieldTestAreaProfile.NEW_AREA) }
    var lastRecordedSessionToken by remember { mutableIntStateOf(-1) }
    val report = remember(revision) { recorder.report() }
    val protocol = remember(revision, protocolCriteria) {
        protocolEvaluator.evaluate(report, protocolCriteria)
    }
    val alreadyRecorded = lastRecordedSessionToken == sessionToken

    ElevatedCard(Modifier.fillMaxWidth().testTag("field-test-comparison-card")) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("필드테스트 세션 비교", style = MaterialTheme.typography.titleMedium)
            Text(
                "신규/반복 지역의 파생 지표만 앱 메모리에서 비교합니다. 장소명·좌표·이벤트 ID는 저장하지 않습니다.",
                style = MaterialTheme.typography.bodySmall,
            )

            ProtocolSummary(protocol = protocol, criteria = protocolCriteria)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = selectedProfile == FieldTestAreaProfile.NEW_AREA,
                    onClick = { selectedProfile = FieldTestAreaProfile.NEW_AREA },
                    label = { Text("신규 지역") },
                    modifier = Modifier.testTag("field-test-profile-new"),
                )
                FilterChip(
                    selected = selectedProfile == FieldTestAreaProfile.REPEAT_AREA,
                    onClick = { selectedProfile = FieldTestAreaProfile.REPEAT_AREA },
                    label = { Text("반복 지역") },
                    modifier = Modifier.testTag("field-test-profile-repeat"),
                )
            }

            OutlinedButton(
                enabled = canRecordCurrentSession && !alreadyRecorded,
                onClick = {
                    recorder.record(selectedProfile, buildDiagnostic())
                    lastRecordedSessionToken = sessionToken
                    revision += 1
                },
                modifier = Modifier.testTag("field-test-record"),
            ) {
                Text(if (alreadyRecorded) "현재 세션 기록됨" else "현재 세션 비교에 기록")
            }
            if (!canRecordCurrentSession) {
                Text("추적을 중지한 뒤 위치 샘플이 있는 세션을 기록할 수 있습니다.", style = MaterialTheme.typography.bodySmall)
            }

            Text(
                "신규 ${report.newArea.sessionCount}회 · 반복 ${report.repeatArea.sessionCount}회",
                modifier = Modifier.testTag("field-test-cohort-counts"),
            )
            if (report.newArea.sessionCount > 0) {
                CohortSummary(prefix = "신규", cohort = report.newArea)
            }
            if (report.repeatArea.sessionCount > 0) {
                CohortSummary(prefix = "반복", cohort = report.repeatArea)
            }

            val discoveredDelta = report.deltas
                .first { it.key == "discoveredEncountersPerSession" }
                .repeatMinusNew
            val resolutionDelta = report.deltas
                .first { it.key == "encounterResolutionRatePercent" }
                .repeatMinusNew
            val fatigueDelta = report.deltas
                .first { it.key == "repeatAreaFatigueProxyPercent" }
                .repeatMinusNew
            if (discoveredDelta != null || resolutionDelta != null || fatigueDelta != null) {
                Text("반복 - 신규 차이", style = MaterialTheme.typography.labelLarge)
                discoveredDelta?.let { Text("발견 encounter/세션 ${signed(it)}") }
                resolutionDelta?.let { Text("해결률 ${signed(it)}%p") }
                fatigueDelta?.let { Text("반복 피로 proxy ${signed(it)}%p") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    enabled = recorder.sessionCount() > 0,
                    onClick = {
                        val shareText = buildString {
                            appendLine(recorder.report().render())
                            append(protocolEvaluator.evaluate(recorder.report(), protocolCriteria).render())
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Daily Town field-test comparison")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "세션 비교 리포트 공유"))
                    },
                    modifier = Modifier.testTag("field-test-share"),
                ) { Text("비교 리포트 공유") }
                TextButton(
                    enabled = recorder.sessionCount() > 0,
                    onClick = {
                        recorder.reset()
                        lastRecordedSessionToken = -1
                        revision += 1
                    },
                    modifier = Modifier.testTag("field-test-reset"),
                ) { Text("비교 초기화") }
            }
        }
    }
}

@Composable
private fun ProtocolSummary(
    protocol: FieldTestProtocolAssessment,
    criteria: FieldTestProtocolCriteria,
) {
    Text(
        "프로토콜: ${protocolStatusLabel(protocol.status)}",
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.testTag("field-test-protocol-status"),
    )
    if (!criteria.isConfigured) {
        Text(
            "제품 검토 기준 미설정 · 비교 결과는 참고용입니다.",
            style = MaterialTheme.typography.bodySmall,
        )
    } else {
        val configured = buildList {
            criteria.minimumSessionsPerCohort?.let { add("cohort 최소 ${it}회") }
            if (criteria.requireMatchingTrackingPreset == true) add("동일 preset")
            if (criteria.requiredEvidence.isNotEmpty()) {
                add("필수 evidence ${criteria.requiredEvidence.size}종")
            }
        }
        if (configured.isNotEmpty()) {
            Text(configured.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
        }
    }
    protocol.issues.take(4).forEach { issue ->
        Text("• ${protocolIssueLabel(issue)}", style = MaterialTheme.typography.bodySmall)
    }
    if (protocol.issues.size > 4) {
        Text("• 추가 미충족 ${protocol.issues.size - 4}건", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CohortSummary(
    prefix: String,
    cohort: FieldTestCohortSummary,
) {
    Text("$prefix 평균", style = MaterialTheme.typography.labelLarge)
    if (cohort.trackingPresets.isNotEmpty()) {
        Text(
            "추적 preset ${cohort.trackingPresets.sorted().joinToString(", ")}",
            style = MaterialTheme.typography.bodySmall,
        )
    }
    MetricText("발견 encounter/세션", cohort.discoveredEncountersPerSession)
    MetricText("해결률 (%)", cohort.encounterResolutionRatePercent)
    MetricText("GPS 제외율 (%)", cohort.gpsRejectionRatePercent)
    MetricText("거리 오차 (%)", cohort.distanceErrorPercent)
    MetricText("배터리 소모/시간", cohort.batteryDrainPercentPerHour)
    MetricText("재방문 비율 (%)", cohort.revisitSharePercent)
    MetricText("반복 피로 proxy (%)", cohort.repeatAreaFatigueProxyPercent)
    Text(
        "acceptance PASS ${cohort.acceptancePassCount} · FAIL ${cohort.acceptanceFailCount} · 미평가 ${cohort.acceptanceNotEvaluatedCount}",
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun MetricText(
    label: String,
    metric: FieldTestMetricAverage,
) {
    val value = metric.average?.toString() ?: "-"
    Text("$label $value · 유효 ${metric.evidenceCount}/${metric.sessionCount}", style = MaterialTheme.typography.bodySmall)
}

private fun buildConfigProtocolCriteria(): FieldTestProtocolCriteria = FieldTestProtocolCriteria(
    minimumSessionsPerCohort = BuildConfig.FIELD_TEST_COMPARISON_MIN_SESSIONS_PER_COHORT
        .takeIf { it > 0 },
    requireMatchingTrackingPreset = when (BuildConfig.FIELD_TEST_COMPARISON_REQUIRE_MATCHING_PRESET) {
        1 -> true
        0 -> false
        else -> null
    },
    requiredEvidence = FieldTestProtocolEvidence.parseCsv(BuildConfig.FIELD_TEST_COMPARISON_REQUIRED_EVIDENCE),
)

private fun protocolStatusLabel(status: FieldTestProtocolStatus): String = when (status) {
    FieldTestProtocolStatus.DATA_INSUFFICIENT -> "데이터 부족"
    FieldTestProtocolStatus.COMPARABLE -> "비교 가능"
    FieldTestProtocolStatus.PRODUCT_REVIEW_READY -> "제품 검토 가능"
}

private fun protocolIssueLabel(issue: FieldTestProtocolIssue): String = when {
    issue.key == "newAreaSessions" -> "신규 지역 세션이 없습니다."
    issue.key == "repeatAreaSessions" -> "반복 지역 세션이 없습니다."
    issue.key == "sharedEvidence" -> "양쪽 cohort에 공통으로 평가 가능한 지표가 없습니다."
    issue.key == "newAreaMinimumSessions" -> "신규 지역 표본 부족 ${issue.detail}"
    issue.key == "repeatAreaMinimumSessions" -> "반복 지역 표본 부족 ${issue.detail}"
    issue.key == "trackingPresetConsistency" -> "tracking preset 불일치 (${issue.detail.removePrefix("mismatch:")})"
    issue.key.startsWith("newAreaEvidence.") ->
        "신규 지역 ${issue.key.substringAfter('.')} evidence 부족 ${issue.detail}"
    issue.key.startsWith("repeatAreaEvidence.") ->
        "반복 지역 ${issue.key.substringAfter('.')} evidence 부족 ${issue.detail}"
    else -> "${issue.key}: ${issue.detail}"
}

private fun signed(value: Int): String = if (value > 0) "+$value" else value.toString()
