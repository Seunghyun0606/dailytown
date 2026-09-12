package com.dailytown.app.ui

import android.content.Context
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailytown.app.BuildConfig
import com.dailytown.app.diagnostics.AndroidBatterySnapshotSource
import com.dailytown.app.diagnostics.FieldTestDiagnostic
import com.dailytown.app.diagnostics.FieldTestDiagnosticBuilder
import com.dailytown.app.diagnostics.FieldTestSessionMonitor
import com.dailytown.app.diagnostics.GameplaySessionMetrics
import com.dailytown.app.diagnostics.GameplaySessionMonitor
import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.location.TrackingMode
import com.dailytown.app.map.MapHealth
import com.dailytown.app.map.MapHealthStatus
import com.dailytown.app.persistence.ExplorationProgress
import java.time.LocalDate

/** Debug-only owner for field-test counters, battery sampling, diagnostics and comparison UI. */
internal class BuildVariantFieldTestRuntime(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val fieldTestSessionMonitor = FieldTestSessionMonitor(AndroidBatterySnapshotSource(appContext))
    private val gameplaySessionMonitor = GameplaySessionMonitor()
    private var sessionToken by mutableIntStateOf(0)

    fun onTrackingStart(mode: TrackingMode) {
        sessionToken += 1
        gameplaySessionMonitor.reset()
        if (mode == TrackingMode.DEVICE) {
            fieldTestSessionMonitor.begin()
        } else {
            fieldTestSessionMonitor.reset()
        }
    }

    fun onTrackingStop(mode: TrackingMode) {
        if (mode == TrackingMode.DEVICE) fieldTestSessionMonitor.end()
    }

    fun beforeTrackingPresetChange(mode: TrackingMode) {
        if (mode == TrackingMode.DEVICE) fieldTestSessionMonitor.end()
    }

    fun recordEncounterOffered(isRevisit: Boolean) = gameplaySessionMonitor.recordEncounterOffered(isRevisit)
    fun recordHinted() = gameplaySessionMonitor.recordHinted()
    fun recordDiscovered(isRevisit: Boolean) = gameplaySessionMonitor.recordDiscovered(isRevisit)
    fun recordClueCollected() = gameplaySessionMonitor.recordClueCollected()
    fun recordResolved(isRevisit: Boolean) = gameplaySessionMonitor.recordResolved(isRevisit)

    @Composable
    fun Content(
        trackingMode: TrackingMode,
        trackingPreset: LocationTrackingPreset,
        onSelectTrackingPreset: (LocationTrackingPreset) -> Unit,
        progress: ExplorationProgress,
        persistenceReady: Boolean,
        persistenceEnabled: Boolean,
        acceptedLocationCount: Int,
        rejectedLocationCount: Int,
        rejectedLocationRatePercent: Int,
        totalLocationSampleCount: Int,
        trackingDurationSeconds: Int,
        sessionDistanceMeters: Double,
        mapProvider: String,
        mapHealth: MapHealth,
    ) {
        var referenceDistanceText by remember { mutableStateOf("") }
        val gameplayMetrics = gameplaySessionMonitor.snapshot()
        val normalizedProgress = progress.normalizePeriods(LocalDate.now())

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("위치 추적 모드", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LocationTrackingPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = trackingPreset == preset,
                            onClick = { onSelectTrackingPreset(preset) },
                            label = { Text(trackingPresetLabel(preset)) },
                        )
                    }
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("필드테스트 진단", style = MaterialTheme.typography.titleMedium)
                Text(
                    "패키지/빌드/파생 통계만 공유하며 좌표·이벤트 ID·지도 API 키는 제외합니다.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "패키지 ${BuildConfig.APPLICATION_ID} · NAVER 키 ${if (BuildConfig.NAVER_MAP_CONFIGURED) "주입" else "없음"} · 지도 ${mapHealthLabel(mapHealth.status)}",
                )
                Text(
                    if (persistenceEnabled) "진행도 저장 정상" else if (persistenceReady) "진행도 임시 모드 · 저장 비활성" else "진행도 복원 중",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (totalLocationSampleCount > 0) {
                    Text(
                        "추적 ${trackingDurationSeconds}초 · GPS 수락 ${acceptedLocationCount} · 제외 ${rejectedLocationCount} · 제외율 ${rejectedLocationRatePercent}%",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (gameplayMetrics.encounterOfferedCount > 0) {
                    val resolutionRate = gameplayMetrics.encounterResolutionRatePercent?.let { "$it%" } ?: "-"
                    Text(
                        "세션 발견 ${gameplayMetrics.discoveredEncounterCount} · 해결 ${gameplayMetrics.resolvedEncounterCount} · 해결률 $resolutionRate · 단서 ${gameplayMetrics.cluesCollectedCount}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    gameplayMetrics.repeatAreaFatigueProxyPercent?.let { fatigue ->
                        Text("재방문 ${gameplayMetrics.revisitOfferedCount}건 · 반복 피로 proxy ${fatigue}%", style = MaterialTheme.typography.bodySmall)
                    }
                }
                OutlinedTextField(
                    value = referenceDistanceText,
                    onValueChange = { value -> if (value.all(Char::isDigit)) referenceDistanceText = value },
                    label = { Text("기준 경로 거리(m, 선택)") },
                    supportingText = { Text("좌표 대신 미리 확인한 총 거리 숫자만 입력합니다.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(onClick = {
                    val report = buildDiagnostic(
                        progress = normalizedProgress,
                        acceptedLocationCount = acceptedLocationCount,
                        rejectedLocationCount = rejectedLocationCount,
                        trackingDurationSeconds = trackingDurationSeconds,
                        sessionDistanceMeters = sessionDistanceMeters,
                        referenceDistanceMeters = referenceDistanceText.toIntOrNull(),
                        gameplayMetrics = gameplayMetrics,
                        mapProvider = mapProvider,
                        mapHealth = mapHealth,
                        trackingPreset = trackingPreset,
                    ).render()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Daily Town field-test diagnostic")
                        putExtra(Intent.EXTRA_TEXT, report)
                    }
                    appContext.startActivity(
                        Intent.createChooser(shareIntent, "진단 리포트 공유")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }) { Text("진단 리포트 공유") }
            }
        }

        FieldTestComparisonCard(
            sessionToken = sessionToken,
            canRecordCurrentSession = trackingMode == TrackingMode.OFF && totalLocationSampleCount > 0,
            buildDiagnostic = {
                buildDiagnostic(
                    progress = normalizedProgress,
                    acceptedLocationCount = acceptedLocationCount,
                    rejectedLocationCount = rejectedLocationCount,
                    trackingDurationSeconds = trackingDurationSeconds,
                    sessionDistanceMeters = sessionDistanceMeters,
                    referenceDistanceMeters = referenceDistanceText.toIntOrNull(),
                    gameplayMetrics = gameplayMetrics,
                    mapProvider = mapProvider,
                    mapHealth = mapHealth,
                    trackingPreset = trackingPreset,
                )
            },
        )
    }

    private fun buildDiagnostic(
        progress: ExplorationProgress,
        acceptedLocationCount: Int,
        rejectedLocationCount: Int,
        trackingDurationSeconds: Int,
        sessionDistanceMeters: Double,
        referenceDistanceMeters: Int?,
        gameplayMetrics: GameplaySessionMetrics,
        mapProvider: String,
        mapHealth: MapHealth,
        trackingPreset: LocationTrackingPreset,
    ): FieldTestDiagnostic {
        val sessionMetrics = fieldTestSessionMonitor.metrics(
            sessionDistanceMeters = sessionDistanceMeters,
            sessionDurationSeconds = trackingDurationSeconds,
            referenceDistanceMeters = referenceDistanceMeters,
        )
        return FieldTestDiagnosticBuilder.build(
            progress = progress,
            acceptedLocationCount = acceptedLocationCount,
            rejectedLocationCount = rejectedLocationCount,
            trackingDurationSeconds = trackingDurationSeconds,
            sessionMetrics = sessionMetrics,
            gameplayMetrics = gameplayMetrics,
            appVersion = BuildConfig.VERSION_NAME,
            mapProvider = mapProvider,
            mapHealth = mapHealth,
            trackingPreset = trackingPreset,
        )
    }
}

private fun trackingPresetLabel(preset: LocationTrackingPreset) = when (preset) {
    LocationTrackingPreset.BATTERY_SAVER -> "절약"
    LocationTrackingPreset.BALANCED -> "균형"
    LocationTrackingPreset.PRECISE -> "정밀"
}

private fun mapHealthLabel(status: MapHealthStatus) = when (status) {
    MapHealthStatus.UNCONFIGURED -> "지도 키 없음"
    MapHealthStatus.INITIALIZING -> "지도 준비 중"
    MapHealthStatus.READY -> "지도 정상"
    MapHealthStatus.AUTH_ERROR -> "지도 인증 오류"
    MapHealthStatus.ERROR -> "지도 오류"
    MapHealthStatus.DESTROYED -> "지도 종료"
}
