package com.dailytown.app.ui

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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dailytown.app.BuildConfig
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.ProgressStore
import com.dailytown.app.poi.PoiRepository
import com.dailytown.app.poi.defaultFixturePois
import com.dailytown.app.progress.GoalDefinition
import com.dailytown.app.progress.GoalMetric
import com.dailytown.app.progress.GoalProgressEvaluator
import com.dailytown.app.progress.GoalRotationCoordinator
import com.dailytown.app.reminder.LocalReminderManager
import com.dailytown.app.ui.visual.A3CompanionStamp
import com.dailytown.app.ui.visual.A3PaperSurface
import com.dailytown.app.ui.visual.MapRuntimeThemeResolver
import com.dailytown.app.ui.visual.ProductionCompanionVisual
import com.dailytown.app.ui.visual.rememberProductionA3AssetRenderer
import com.dailytown.app.visual.A3Screen
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

private enum class MvpSection(val label: String, val symbol: String) {
    EXPLORE("탐험", "⌖"),
    COMPANION("동행", "●"),
    COLLECTION("기록", "▦"),
    GOALS("목표", "✓"),
    SETTINGS("설정", "⚙"),
}

@Composable
fun DailyTownMvpShell(
    mapAdapter: MapViewAdapter,
    progressStore: ProgressStore,
    poiRepository: PoiRepository,
    reminderManager: LocalReminderManager,
) {
    var selectedSection by rememberSaveable { mutableStateOf(MvpSection.EXPLORE) }
    var progress by remember { mutableStateOf<ExplorationProgress?>(null) }
    var dailyGoals by remember { mutableStateOf<List<GoalDefinition>>(emptyList()) }
    var weeklyGoals by remember { mutableStateOf<List<GoalDefinition>>(emptyList()) }

    LaunchedEffect(selectedSection, progressStore) {
        if (selectedSection == MvpSection.EXPLORE) return@LaunchedEffect
        val loaded = progressStore.load()
        val rotation = GoalRotationCoordinator().ensure(loaded, LocalDate.now())
        progress = rotation.progress
        dailyGoals = rotation.dailyGoals
        weeklyGoals = rotation.weeklyGoals
        if (rotation.progress != loaded) progressStore.save(rotation.progress)
    }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    MvpSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = selectedSection == section,
                            onClick = { selectedSection = section },
                            icon = { Text(section.symbol) },
                            label = { Text(section.label) },
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
                // Keep Explore composed even while another tab is visible. Its location source,
                // encounter coordinator and field-test monitors therefore survive tab switches.
                SectionLayer(active = selectedSection == MvpSection.EXPLORE) {
                    DailyTownApp(
                        mapAdapter = mapAdapter,
                        progressStore = progressStore,
                        poiRepository = poiRepository,
                        reminderManager = reminderManager,
                    )
                }
                SectionLayer(active = selectedSection == MvpSection.COMPANION) {
                    CompanionScreen(progress)
                }
                SectionLayer(active = selectedSection == MvpSection.COLLECTION) {
                    CollectionScreen(progress)
                }
                SectionLayer(active = selectedSection == MvpSection.GOALS) {
                    GoalsScreen(progress, dailyGoals, weeklyGoals)
                }
                SectionLayer(active = selectedSection == MvpSection.SETTINGS) {
                    SettingsScreen()
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (active) 1f else 0f)
            .alpha(if (active) 1f else 0f),
    ) {
        content()
    }
}

@Composable
private fun CompanionScreen(progress: ExplorationProgress?) {
    val bond = progress?.companionBond?.takeIf { it > 0 } ?: 12
    val lighting = remember {
        MapRuntimeThemeResolver().resolve(LocalTime.now()).profile.companionLighting
    }
    val a3AssetRenderer = rememberProductionA3AssetRenderer()
    ScreenColumn(title = "동행 · 모루") {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProductionCompanionVisual(
                    request = CompanionVisualRequest(
                        companionId = "moru",
                        expression = CompanionExpression.NEUTRAL,
                        lightingFamily = lighting,
                        appearanceProfile = AppearanceProfile.BASE,
                        usageContext = CompanionUsageContext.RESULT_LARGE,
                    ),
                    modifier = Modifier.size(240.dp),
                    contentDescription = "동행 캐릭터 모루",
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    A3CompanionStamp(
                        assetRenderer = a3AssetRenderer,
                        sizeDp = 48,
                    )
                    Column {
                        Text("모루", style = MaterialTheme.typography.headlineSmall)
                        Text("호감도 $bond")
                    }
                }
                Text("함께 발견한 기억 ${progress?.companionMemoryKeys?.size ?: 0}개")
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("우리의 기록", style = MaterialTheme.typography.titleMedium)
                Text("탐험한 POI ${progress?.encounterVisitedPoiIds?.size ?: 0}곳")
                Text("해결한 미스터리 ${progress?.resolvedEncounterIds?.size ?: 0}건")
                Text("수집한 단서 ${progress?.inventoryClueIds?.size ?: 0}개")
            }
        }
    }
}

@Composable
private fun CollectionScreen(progress: ExplorationProgress?) {
    val a3AssetRenderer = rememberProductionA3AssetRenderer()
    val fixtureNames = remember {
        defaultFixturePois().associate { poi -> poi.id to poi.name }
    }
    A3PaperSurface(
        screen = A3Screen.COLLECTION_GRID,
        assetRenderer = a3AssetRenderer,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("동네 기록", style = MaterialTheme.typography.headlineSmall)
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("탐험 컬렉션", style = MaterialTheme.typography.titleMedium)
                    StatRow("발견한 장소", "${progress?.encounterVisitedPoiIds?.size ?: 0}곳")
                    StatRow("해결한 미스터리", "${progress?.resolvedEncounterIds?.size ?: 0}건")
                    StatRow("수집한 단서", "${progress?.inventoryClueIds?.size ?: 0}개")
                    StatRow("누적 이동", "${progress?.distanceWalkedMeters?.roundToInt() ?: 0}m")
                }
            }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("최근 탐험", style = MaterialTheme.typography.titleMedium)
                    val recent = progress?.recentPoiIds.orEmpty()
                    if (recent.isEmpty()) {
                        Text("아직 기록된 장소가 없습니다.")
                    } else {
                        recent.take(8).forEachIndexed { index, poiId ->
                            Text("${index + 1}. ${fixtureNames[poiId] ?: poiId}")
                        }
                    }
                }
            }
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
    ScreenColumn(title = "오늘의 목표") {
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
                    Text(
                        "${if (state.isComplete) "✓" else "•"} ${goalLabel(goal)}  ${state.current}/${state.target}",
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    ScreenColumn(title = "설정") {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Daily Town", style = MaterialTheme.typography.titleMedium)
                Text("앱 버전 ${BuildConfig.VERSION_NAME}")
                Text("NAVER 지도 ${if (BuildConfig.NAVER_MAP_CONFIGURED) "연결됨" else "설정 필요"}")
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("개발 상태", style = MaterialTheme.typography.titleMedium)
                Text("Field Test/진단 도구를 사용자 탐험 화면에서 분리하는 중입니다.")
                Text("세션 상태 보존을 먼저 고정한 뒤 설정의 개발/QA 영역으로 이동합니다.")
            }
        }
    }
}

@Composable
private fun ScreenColumn(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
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
        Text(value)
    }
}

private fun goalLabel(goal: GoalDefinition): String = when (goal.metric) {
    GoalMetric.WALK_DISTANCE_METERS -> "${goal.target}m 걷기"
    GoalMetric.DISCOVER_SPOT -> "새 장소 ${goal.target}곳 발견"
    GoalMetric.RESOLVE_MYSTERY -> "미스터리 ${goal.target}건 해결"
    GoalMetric.COLLECT_CLUE -> "단서 ${goal.target}개 모으기"
}
