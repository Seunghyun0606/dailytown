package com.dailytown.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dailytown.app.diagnostics.FieldTestAreaProfile
import com.dailytown.app.diagnostics.FieldTestProtocolEvidence
import com.dailytown.app.diagnostics.FieldTestSessionPlan
import com.dailytown.app.diagnostics.parseReferenceDistanceMeters
import com.dailytown.app.location.LocationTrackingPreset

@Composable
internal fun FieldTestSessionSetupCard(
    trackingActive: Boolean,
    trackingPreset: LocationTrackingPreset,
    draftProfile: FieldTestAreaProfile,
    referenceDistanceText: String,
    activePlan: FieldTestSessionPlan?,
    completedPlan: FieldTestSessionPlan?,
    requiredEvidence: Set<FieldTestProtocolEvidence>,
    onProfileChange: (FieldTestAreaProfile) -> Unit,
    onReferenceDistanceChange: (String) -> Unit,
) {
    val parsedReferenceDistance = parseReferenceDistanceMeters(referenceDistanceText)
    val invalidReferenceDistance = referenceDistanceText.isNotBlank() && parsedReferenceDistance == null
    val distanceEvidenceRequired = FieldTestProtocolEvidence.DISTANCE_ERROR in requiredEvidence

    ElevatedCard(Modifier.fillMaxWidth().testTag("field-test-session-setup")) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("필드테스트 세션 준비", style = MaterialTheme.typography.titleMedium)
            Text(
                "세션 시작 시 지역 분류·tracking preset·기준거리만 고정합니다. 장소명·좌표·경로는 저장하지 않습니다.",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = draftProfile == FieldTestAreaProfile.NEW_AREA,
                    enabled = !trackingActive,
                    onClick = { onProfileChange(FieldTestAreaProfile.NEW_AREA) },
                    label = { Text("신규 지역") },
                    modifier = Modifier.testTag("field-test-setup-profile-new"),
                )
                FilterChip(
                    selected = draftProfile == FieldTestAreaProfile.REPEAT_AREA,
                    enabled = !trackingActive,
                    onClick = { onProfileChange(FieldTestAreaProfile.REPEAT_AREA) },
                    label = { Text("반복 지역") },
                    modifier = Modifier.testTag("field-test-setup-profile-repeat"),
                )
            }

            OutlinedTextField(
                value = referenceDistanceText,
                onValueChange = { value ->
                    if (value.all(Char::isDigit)) onReferenceDistanceChange(value)
                },
                enabled = !trackingActive,
                isError = invalidReferenceDistance,
                label = { Text("기준 경로 거리(m, 선택)") },
                supportingText = {
                    Text(
                        when {
                            invalidReferenceDistance -> "1m 이상의 정수만 사용할 수 있습니다."
                            distanceEvidenceRequired && parsedReferenceDistance == null ->
                                "현재 protocol은 거리 오차 evidence를 요구합니다. 시작 전에 기준거리 입력을 권장합니다."
                            else -> "미리 확인한 총 거리 숫자만 사용하며 경로 geometry는 저장하지 않습니다."
                        },
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("field-test-reference-distance"),
            )

            Text(
                "다음 세션: ${profileLabel(draftProfile)} · ${presetLabel(trackingPreset)} · 기준거리 ${parsedReferenceDistance?.let { "${it}m" } ?: "없음"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("field-test-draft-plan"),
            )

            activePlan?.let { plan ->
                Text(
                    "진행 중 계획: ${planLabel(plan)}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.testTag("field-test-active-plan"),
                )
            }
            if (!trackingActive) {
                completedPlan?.let { plan ->
                    Text(
                        "종료 세션 계획: ${planLabel(plan)} · 아래 비교 카드에서 확인 후 기록하세요.",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.testTag("field-test-completed-plan"),
                    )
                }
            }

            if (requiredEvidence.isNotEmpty()) {
                Text(
                    "필수 evidence: ${requiredEvidence.sortedBy { it.name }.joinToString(", ") { evidenceLabel(it) }}",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (FieldTestProtocolEvidence.BATTERY_DRAIN in requiredEvidence) {
                    Text("• 배터리 evidence는 실기기에서 충전선을 분리한 세션이 필요합니다.", style = MaterialTheme.typography.bodySmall)
                }
                if (
                    draftProfile == FieldTestAreaProfile.REPEAT_AREA &&
                    FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE in requiredEvidence
                ) {
                    Text("• 반복 피로 evidence는 실제 재방문 encounter가 발견되어야 계산됩니다.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun planLabel(plan: FieldTestSessionPlan): String =
    "${profileLabel(plan.areaProfile)} · ${presetLabel(plan.trackingPreset)} · 기준거리 ${plan.referenceDistanceMeters?.let { "${it}m" } ?: "없음"}"

private fun profileLabel(profile: FieldTestAreaProfile): String = when (profile) {
    FieldTestAreaProfile.NEW_AREA -> "신규 지역"
    FieldTestAreaProfile.REPEAT_AREA -> "반복 지역"
}

private fun presetLabel(preset: LocationTrackingPreset): String = when (preset) {
    LocationTrackingPreset.BATTERY_SAVER -> "절약"
    LocationTrackingPreset.BALANCED -> "균형"
    LocationTrackingPreset.PRECISE -> "정밀"
}

private fun evidenceLabel(evidence: FieldTestProtocolEvidence): String = when (evidence) {
    FieldTestProtocolEvidence.SESSION_DURATION -> "세션 시간"
    FieldTestProtocolEvidence.SESSION_DISTANCE -> "세션 거리"
    FieldTestProtocolEvidence.GPS_REJECTION_RATE -> "GPS 제외율"
    FieldTestProtocolEvidence.DISTANCE_ERROR -> "거리 오차"
    FieldTestProtocolEvidence.BATTERY_DRAIN -> "배터리 소모"
    FieldTestProtocolEvidence.DISCOVERED_ENCOUNTERS -> "발견 encounter"
    FieldTestProtocolEvidence.ENCOUNTER_RESOLUTION -> "해결률"
    FieldTestProtocolEvidence.REVISIT_SHARE -> "재방문 비율"
    FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE -> "반복 피로 proxy"
}
