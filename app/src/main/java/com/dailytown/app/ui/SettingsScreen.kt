package com.dailytown.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.dailytown.app.BuildConfig
import com.dailytown.app.poi.PoiSourceMetadata
import com.dailytown.app.reminder.LocalReminderManager

@Composable
internal fun SettingsScreen(
    reminderManager: LocalReminderManager,
    poiSources: List<PoiSourceMetadata>,
    onOpenQa: () -> Unit,
) {
    val initialPreference = remember { reminderManager.preference() }
    var reminderEnabled by remember { mutableStateOf(initialPreference.enabled) }
    var reminderHour by remember { mutableIntStateOf(initialPreference.hour) }
    var reminderError by remember { mutableStateOf<String?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            reminderManager.enable(reminderHour)
            reminderEnabled = true
            reminderError = null
        } else {
            reminderManager.disable()
            reminderEnabled = false
            reminderError = "알림 권한이 없어 탐험 리마인더를 켤 수 없습니다."
        }
    }

    ScreenColumn(title = "설정", subtitle = "Daily Town의 탐험과 알림 방식을 조정합니다.") {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Daily Town", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text("앱 버전 ${BuildConfig.VERSION_NAME}")
                Text("NAVER 지도 ${if (BuildConfig.NAVER_MAP_CONFIGURED) "연결됨" else "설정 필요"}")
                Text("Production POI ${if (BuildConfig.TOUR_API_CONFIGURED) "TourAPI 활성" else if (BuildConfig.DEBUG) "Field Test fixture" else "설정 필요"}")
            }
        }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings-poi-attribution"),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("POI 데이터 출처", style = MaterialTheme.typography.titleMedium)
                if (poiSources.isEmpty()) {
                    Text("출처 정보 없음")
                } else {
                    poiSources.forEach { source ->
                        Text(source.displayName, style = MaterialTheme.typography.labelLarge)
                        Text(source.attributionText)
                        Text(source.licenseSummary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("탐험 리마인더", style = MaterialTheme.typography.titleMedium)
                Text("위치 정보는 사용하지 않고 선택한 시간대에만 알림을 보냅니다.")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                reminderManager.disable()
                                reminderEnabled = false
                                reminderError = null
                            } else if (Build.VERSION.SDK_INT >= 33 && !reminderManager.canPostNotifications()) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                reminderManager.enable(reminderHour)
                                reminderEnabled = true
                                reminderError = null
                            }
                        },
                    )
                    Text(if (reminderEnabled) "매일 ${reminderHour}시 전후 알림" else "알림 끔")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(12, 18, 20).forEach { hour ->
                        FilterChip(
                            selected = reminderHour == hour,
                            onClick = {
                                reminderHour = hour
                                if (reminderEnabled) reminderManager.enable(hour)
                            },
                            label = { Text("${hour}시") },
                        )
                    }
                }
                reminderError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
        if (BuildConfig.DEBUG) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("개발 / Field Test", style = MaterialTheme.typography.titleMedium)
                    Text("GPS 품질, replay, 진단 리포트, NEW_AREA/REPEAT_AREA 비교 도구는 일반 탐험 화면과 분리되어 있습니다.")
                    Button(onClick = onOpenQa, modifier = Modifier.testTag("settings-open-qa")) {
                        Text("Field Test / QA 열기")
                    }
                }
            }
        }
    }
}
