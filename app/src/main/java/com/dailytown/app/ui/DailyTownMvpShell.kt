package com.dailytown.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dailytown.app.BuildConfig
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.ProgressStore
import com.dailytown.app.poi.PoiRepository
import com.dailytown.app.poi.PoiSourceMetadata
import com.dailytown.app.progress.GoalDefinition
import com.dailytown.app.progress.GoalMetric
import com.dailytown.app.progress.GoalProgressEvaluator
import com.dailytown.app.progress.GoalRotationCoordinator
import com.dailytown.app.reminder.LocalReminderManager
import com.dailytown.app.ui.visual.A3CompanionStamp
import com.dailytown.app.ui.visual.A3PaperSurface
import com.dailytown.app.ui.visual.DailyTownTheme
import com.dailytown.app.ui.visual.DailyTownTokens
import com.dailytown.app.ui.visual.LocalDailyTownCompanionLighting
import com.dailytown.app.ui.visual.ProductionCompanionVisual
import com.dailytown.app.ui.visual.rememberProductionA3AssetRenderer
import com.dailytown.app.visual.A3Screen
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest
import java.time.LocalDate

private enum class MvpSection(val label: String, val symbol: String, val testTag: String) {
    EXPLORE("탐험", "⌖", "nav-explore"),
    COMPANION("동행", "●", "nav-companion"),
    COLLECTION("기록", "▦", "nav-collection"),
    GOALS("목표", "✓", "nav-goals"),
    SETTINGS("설정", "⚙", "nav-settings"),
}

@Composable
fun DailyTownMvpShell(
    mapAdapter: MapViewAdapter,
    progressStore: ProgressStore,
    poiRepository: PoiRepository,
    reminderManager: LocalReminderManager,
) {
    var selectedSection by rememberSaveable { mutableStateOf(MvpSection.EXPLORE) }
    var qaMode by rememberSaveable { mutableStateOf(false) }
    var progress by remember { mutableStateOf<ExplorationProgress?>(null) }
    var dailyGoals by remember { mutableStateOf<List<GoalDefinition>>(emptyList()) }
    var weeklyGoals by remember { mutableStateOf<List<GoalDefinition>>(emptyList()) }
    val poiSources = remember(poiRepository) { poiRepository.sourceMetadata() }

    LaunchedEffect(selectedSection, progressStore) {
        if (selectedSection == MvpSection.EXPLORE) return@LaunchedEffect
        val loaded = progressStore.load()
        val rotation = GoalRotationCoordinator().ensure(loaded, LocalDate.now())
        progress = rotation.progress
        dailyGoals = rotation.dailyGoals
        weeklyGoals = rotation.weeklyGoals
        if (rotation.progress != loaded) progressStore.save(rotation.progress)
    }

    DailyTownTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    MvpSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = selectedSection == section,
                            onClick = {
                                selectedSection = section
                                if (section == MvpSection.EXPLORE) qaMode = false
                            },
                            icon = {
                                Text(
                                    section.symbol,
                                    fontWeight = if (selectedSection == section) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            label = { Text(section.label) },
                            modifier = Modifier.testTag(section.testTag),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = DailyTownTokens.LeafSecondary.copy(alpha = 0.42f),
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                // Keep Explore composed while another tab is visible so location/gameplay runtime survives.
                SectionLayer(active = selectedSection == MvpSection.EXPLORE) {
                    DailyTownApp(
                        mapAdapter = mapAdapter,
                        progressStore = progressStore,
                        poiRepository = poiRepository,
                        showQaTools = qaMode,
                    )
                }
                SectionLayer(active = selectedSection == MvpSection.COMPANION) {
                    CompanionScreen(progress)
                }
                SectionLayer(active = selectedSection == MvpSection.COLLECTION) {
                    DailyTownRecordsScreen(progress)
                }
                SectionLayer(active = selectedSection == MvpSection.GOALS) {
                    GoalsScreen(progress, dailyGoals, weeklyGoals)
                }
                SectionLayer(active = selectedSection == MvpSection.SETTINGS) {
                    SettingsScreen(
                        reminderManager = reminderManager,
                        poiSources = poiSources,
                        onOpenQa = {
                            qaMode = true
                            selectedSection = MvpSection.EXPLORE
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.SectionLayer(
    active: Boolean,
    content: @Composable () -> Unit,
) {
    val visibilityModifier = if (active) Modifier else Modifier.clearAndSetSemantics { }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (active) 1f else 0f)
            .alpha(if (active) 1f else 0f)
            .then(visibilityModifier),
    ) {
        content()
    }
}

@Composable
private fun CompanionScreen(progress: ExplorationProgress?) {
    val bond = progress?.companionBond?.takeIf { it > 0 } ?: 12
    val lighting = LocalDailyTownCompanionLighting.current
    val a3AssetRenderer = rememberProductionA3AssetRenderer()
    A3PaperSurface(
        screen = A3Screen.MEMORY_DETAIL,
        assetRenderer = a3AssetRenderer,
        modifier = Modifier.fillMaxSize().testTag("companion-product-screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("오늘의 동행", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("모루", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("같이 걸었던 시간이 모루의 기억이 됩니다.")
            }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProductionCompanionVisual(
                        request = CompanionVisualRequest(
                            companionId = "moru",
                            expression = CompanionExpression.NEUTRAL,
                            lightingFamily = lighting,
                            appearanceProfile = AppearanceProfile.BASE,
                            usageContext = CompanionUsageContext.RESULT_LARGE,
                        ),
                        modifier = Modifier.size(248.dp),
                        contentDescription = "동행 캐릭터 모루",
                        rasterTargetPx = 384,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        A3CompanionStamp(assetRenderer = a3AssetRenderer, sizeDp = 48)
                        Column {
                            Text("호감도 $bond", style = MaterialTheme.typography.titleMedium)
                            Text("함께 발견한 기억 ${progress?.companionMemoryKeys?.size ?: 0}개", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("우리의 산책", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    StatRow("발견한 장소", "${progress?.encounterVisitedPoiIds?.size ?: 0}곳")
                    StatRow("해결한 미스터리", "${progress?.resolvedEncounterIds?.size ?: 0}건")
                    StatRow("수집한 단서", "${progress?.inventoryClueIds?.size ?: 0}개")
                }
            }
            Text(
                if (BuildConfig.DEBUG) "M-B motion prototype · final timing pending" else "M-B static fallback",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GoalsScreen(
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
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                        Text("${state.current}/${state.target}", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
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

@Composable
private fun ScreenColumn(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
        content()
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun goalLabel(goal: GoalDefinition): String = when (goal.metric) {
    GoalMetric.WALK_DISTANCE_METERS -> "${goal.target}m 걷기"
    GoalMetric.DISCOVER_SPOT -> "새 장소 ${goal.target}곳 발견"
    GoalMetric.RESOLVE_MYSTERY -> "미스터리 ${goal.target}건 해결"
    GoalMetric.COLLECT_CLUE -> "단서 ${goal.target}개 모으기"
}
