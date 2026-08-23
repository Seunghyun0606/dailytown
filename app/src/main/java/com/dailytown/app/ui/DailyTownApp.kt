package com.dailytown.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dailytown.app.companion.CompanionMoment
import com.dailytown.app.companion.DefaultCompanionReactionPolicy
import com.dailytown.app.domain.*
import com.dailytown.app.location.*
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.map.UserLocationSpec
import com.dailytown.app.mystery.*
import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.persistence.ProgressStore
import com.dailytown.app.persistence.dailyPeriodKey
import com.dailytown.app.persistence.toState
import com.dailytown.app.persistence.weeklyPeriodKey
import com.dailytown.app.poi.PoiRepository
import com.dailytown.app.progress.*
import java.time.LocalDate
import kotlin.math.roundToInt

private enum class TrackingMode { OFF, DEVICE, REPLAY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTownApp(
    mapAdapter: MapViewAdapter,
    progressStore: ProgressStore,
    poiRepository: PoiRepository,
) {
    val context = LocalContext.current
    val spots = remember { demoMysterySpots() }
    val defaultCompanion = remember { Companion("moru", "모루", 12) }
    val initialState = remember { ExplorationState(companion = defaultCompanion) }
    val session = remember { ExplorationSession(initialState, spots) }

    val templates = remember { MysteryTemplateCatalog.defaults() }
    val reducer = remember { MysteryReducer(templates.associateBy { it.id }) }
    val encounterGenerator = remember { EncounterGenerator(templates = templates) }
    val proximityController = remember { EncounterProximityController(reducer = reducer) }
    val reactionPolicy = remember { DefaultCompanionReactionPolicy() }
    val goalEvaluator = remember { GoalProgressEvaluator() }

    var snapshot by remember { mutableStateOf(session.current()) }
    var gameProgress by remember { mutableStateOf(ExplorationProgress()) }
    var activeEncounter by remember { mutableStateOf<EncounterSelection?>(null) }
    var encounterSequence by remember { mutableIntStateOf(0) }
    var trackingMode by remember { mutableStateOf(TrackingMode.OFF) }
    var trackingPreset by remember { mutableStateOf(LocationTrackingPreset.BALANCED) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastCompanionMoment by remember { mutableStateOf<CompanionMoment?>(null) }
    var persistenceReady by remember { mutableStateOf(false) }

    val deviceSource = remember(trackingPreset) {
        FusedDeviceLocationSource(context.applicationContext, trackingPreset.config)
    }
    val replaySource = remember { ReplayLocationSource() }

    val today = LocalDate.now()
    val dayKey = dailyPeriodKey(today)
    val weekKey = weeklyPeriodKey(today)
    val dailyGoals = remember(dayKey) {
        GoalPlanner().plan(
            periodKey = dayKey,
            catalog = GoalCatalog.defaults().filter { it.period == GoalPeriod.DAILY },
            recentlyUsedIds = emptySet(),
            count = 2,
        )
    }
    val weeklyGoals = remember(weekKey) {
        GoalPlanner().plan(
            periodKey = weekKey,
            catalog = GoalCatalog.defaults().filter { it.period == GoalPeriod.WEEKLY },
            recentlyUsedIds = emptySet(),
            count = 1,
        )
    }

    LaunchedEffect(progressStore) {
        try {
            val restored = progressStore.load().normalizePeriods(LocalDate.now())
            gameProgress = restored
            session.restore(restored.toState(defaultCompanion))
            snapshot = session.current()
        } catch (error: Throwable) {
            errorMessage = "진행도 불러오기 실패: ${error.message ?: "unknown"}"
        } finally {
            persistenceReady = true
        }
    }

    LaunchedEffect(snapshot.state, persistenceReady) {
        if (!persistenceReady) return@LaunchedEffect
        val synced = gameProgress.syncExploration(snapshot.state, LocalDate.now())
        if (synced != gameProgress) gameProgress = synced
    }

    LaunchedEffect(gameProgress, persistenceReady) {
        if (!persistenceReady) return@LaunchedEffect
        try {
            progressStore.save(gameProgress)
        } catch (error: Throwable) {
            errorMessage = "진행도 저장 실패: ${error.message ?: "unknown"}"
        }
    }

    LaunchedEffect(trackingPreset) {
        session.setLocationQualityPolicy(
            LocationQualityPolicy(maxAccuracyMeters = trackingPreset.config.maxAcceptedAccuracyMeters),
        )
        if (trackingMode == TrackingMode.DEVICE) {
            session.restartTracking()
            snapshot = session.current()
        }
    }

    fun start(mode: TrackingMode) {
        session.restartTracking()
        snapshot = session.current()
        activeEncounter = null
        lastCompanionMoment = null
        errorMessage = null
        trackingMode = mode
    }

    fun applyReaction(moment: CompanionMoment) {
        lastCompanionMoment = moment
        val reaction = reactionPolicy.react(snapshot.state.companion, moment)
        session.applyCompanionBond(reaction.bondDelta)
        snapshot = session.current()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) start(TrackingMode.DEVICE)
        else errorMessage = "위치 권한이 필요합니다. 리플레이 모드는 권한 없이 사용할 수 있습니다."
    }

    DisposableEffect(trackingMode, deviceSource) {
        val source = when (trackingMode) {
            TrackingMode.DEVICE -> deviceSource
            TrackingMode.REPLAY -> replaySource
            TrackingMode.OFF -> null
        }
        source?.start(
            onLocation = { sample -> snapshot = session.onLocation(sample) },
            onError = { error -> errorMessage = error.message ?: "위치 수집 오류" },
        )
        onDispose { source?.stop() }
    }

    LaunchedEffect(
        snapshot.currentLocation,
        activeEncounter?.encounter?.id,
        activeEncounter?.encounter?.phase,
    ) {
        val sample = snapshot.currentLocation ?: return@LaunchedEffect
        var selection = activeEncounter

        if (selection == null) {
            val pois = poiRepository.nearby(sample.point, radiusMeters = 900.0)
            val history = EncounterHistory(
                recentPoiIds = gameProgress.recentPoiIds.toSet(),
                recentTemplateIds = gameProgress.recentTemplateIds.toSet(),
                recentPairKeys = gameProgress.recentPairKeys.toSet(),
            )
            val encounterContext = EncounterContextFactory.create(
                companionBond = snapshot.state.companion.bond,
                memoryKeys = gameProgress.companionMemoryKeys,
            )
            selection = encounterGenerator.choose(
                encounterKey = "enc-${encounterSequence++}",
                center = sample.point,
                pois = pois,
                context = encounterContext,
                visitedPoiIds = gameProgress.encounterVisitedPoiIds,
                history = history,
            )
            activeEncounter = selection
        }

        val current = selection ?: return@LaunchedEffect
        if (current.encounter.phase == EncounterPhase.RESOLVED) return@LaunchedEffect

        val advanced = proximityController.advance(
            encounter = current.encounter,
            user = sample.point,
            poi = current.poi.position,
        )
        if (advanced == current.encounter) return@LaunchedEffect

        val previousPhase = current.encounter.phase
        activeEncounter = current.copy(encounter = advanced)
        if (previousPhase == EncounterPhase.HIDDEN && advanced.phase == EncounterPhase.HINTED) {
            applyReaction(CompanionMoment.HINT_APPEARED)
        }
        if (previousPhase != EncounterPhase.DISCOVERED && advanced.phase == EncounterPhase.DISCOVERED) {
            gameProgress = gameProgress
                .recordEncounterVisit(current.poi.id, current.template.id, LocalDate.now())
                .recordMemory("poi:${current.poi.id}")
            applyReaction(CompanionMoment.SPOT_DISCOVERED)
        }
    }

    LaunchedEffect(snapshot.currentLocation, activeEncounter) {
        val encounterMarker = activeEncounter?.let { selection ->
            MapMarkerSpec(
                id = "active-${selection.encounter.id}",
                title = encounterMarkerTitle(selection),
                position = selection.poi.position,
            )
        }
        mapAdapter.setMarkers(
            spots.map { MapMarkerSpec(it.id, it.title, it.position) } + listOfNotNull(encounterMarker),
        )
        snapshot.currentLocation?.let { sample ->
            mapAdapter.setUserLocation(UserLocationSpec(sample.point, sample.bearingDegrees))
            mapAdapter.setCamera(sample.point)
        }
    }

    val currentDate = LocalDate.now()
    val normalizedProgress = gameProgress.normalizePeriods(currentDate)
    val currentContext = EncounterContextFactory.create(
        companionBond = snapshot.state.companion.bond,
        memoryKeys = gameProgress.companionMemoryKeys,
    )
    val neighborhood = NeighborhoodProgress(
        districtKey = activeEncounter?.poi?.districtKey ?: "jung-gu",
        visitedPoiIds = gameProgress.encounterVisitedPoiIds,
        resolvedEncounterIds = gameProgress.resolvedEncounterIds,
        distanceWalkedMeters = snapshot.state.distanceWalkedMeters,
    )
    val distanceToEncounter = snapshot.currentLocation?.let { sample ->
        activeEncounter?.let { proximityController.distanceTo(sample.point, it.poi.position).roundToInt() }
    }

    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Daily Town") }) }) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                Text("오늘의 동네 탐험", style = MaterialTheme.typography.headlineSmall)
                Text("지도: ${mapAdapter.providerId} · 동행: ${snapshot.state.companion.name} · 호감도 ${snapshot.state.companion.bond}")
                Text("컨텍스트: ${timeBandLabel(currentContext.timeBand)} · 기억 ${gameProgress.companionMemoryKeys.size}개")

                MapSurface(
                    mapAdapter = mapAdapter,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                )

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("누적 탐험 거리 ${snapshot.state.distanceWalkedMeters.roundToInt()}m")
                        Text("미스터리 단서 ${gameProgress.inventoryClueIds.size}개 · 해결 ${gameProgress.resolvedEncounterIds.size}건")
                        Text("탐험 POI ${gameProgress.encounterVisitedPoiIds.size}곳")
                        if (snapshot.rejectedLocationCount > 0) {
                            Text("GPS 품질 필터 제외 ${snapshot.rejectedLocationCount}회")
                        }
                        snapshot.newlyDiscovered.firstOrNull()?.let {
                            Text("주변 발견: ${it.title}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                EncounterCard(
                    selection = activeEncounter,
                    reducer = reducer,
                    distanceMeters = distanceToEncounter,
                    onCollectClue = { clueId, updated ->
                        if (updated.clueIds.size > (activeEncounter?.encounter?.clueIds?.size ?: 0)) {
                            activeEncounter = activeEncounter?.copy(encounter = updated)
                            gameProgress = gameProgress.recordClue(clueId, LocalDate.now())
                            applyReaction(CompanionMoment.CLUE_FOUND)
                        }
                    },
                    onResolve = { resolved ->
                        val selected = activeEncounter
                        activeEncounter = selected?.copy(encounter = resolved)
                        gameProgress = gameProgress.recordResolution(resolved, LocalDate.now())
                        selected?.let {
                            gameProgress = gameProgress.recordMemory("mechanic:${it.template.mechanic.name}")
                        }
                        applyReaction(CompanionMoment.MYSTERY_RESOLVED)
                    },
                    onContinue = {
                        activeEncounter = null
                        lastCompanionMoment = null
                    },
                )

                lastCompanionMoment?.let { moment ->
                    AssistChip(
                        onClick = {},
                        label = { Text(companionMomentLabel(snapshot.state.companion.name, moment)) },
                    )
                }

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("동네 컬렉션", style = MaterialTheme.typography.titleMedium)
                        Text("${neighborhood.districtKey} · 탐험 ${neighborhood.discoveryCount}곳 · 해결 ${neighborhood.resolvedCount}건")
                        Text("동행 기억 ${gameProgress.companionMemoryKeys.size}개")
                    }
                }

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("회전 목표", style = MaterialTheme.typography.titleMedium)
                        (dailyGoals + weeklyGoals).forEach { goal ->
                            val progress = goalEvaluator.evaluate(goal, normalizedProgress, currentDate)
                            val prefix = if (goal.period == GoalPeriod.DAILY) "오늘" else "이번 주"
                            Text("${if (progress.isComplete) "✓" else "•"} $prefix ${goalLabel(goal)}: ${progress.current}/${progress.target}")
                        }
                    }
                }

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("위치 추적 모드", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LocationTrackingPreset.entries.forEach { preset ->
                                FilterChip(
                                    selected = trackingPreset == preset,
                                    onClick = { trackingPreset = preset },
                                    label = { Text(trackingPresetLabel(preset)) },
                                )
                            }
                        }
                        Text(
                            "간격 ${trackingPreset.config.intervalMillis / 1000}s · 최소 이동 ${trackingPreset.config.minUpdateDistanceMeters.roundToInt()}m",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (hasLocationPermission(context)) start(TrackingMode.DEVICE)
                        else permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }) { Text("실제 위치") }

                    OutlinedButton(onClick = { start(TrackingMode.REPLAY) }) { Text("경로 리플레이") }
                    TextButton(onClick = {
                        trackingMode = TrackingMode.OFF
                        mapAdapter.setUserLocation(null)
                    }) { Text("중지") }
                }

                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Text(
                    when (trackingMode) {
                        TrackingMode.DEVICE -> "실기기 위치 추적 중 · ${trackingPresetLabel(trackingPreset)}"
                        TrackingMode.REPLAY -> "서울시청 → 덕수궁 테스트 경로 재생 중"
                        TrackingMode.OFF -> if (persistenceReady) "탐험 대기 중 · 게임 진행도 저장 활성" else "진행도 불러오는 중"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EncounterCard(
    selection: EncounterSelection?,
    reducer: MysteryReducer,
    distanceMeters: Int?,
    onCollectClue: (String, MysteryEncounter) -> Unit,
    onResolve: (MysteryEncounter) -> Unit,
    onContinue: () -> Unit,
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("현재 미스터리", style = MaterialTheme.typography.titleMedium)
            if (selection == null) {
                Text("위치가 들어오면 주변 POI에서 후보를 생성합니다.")
                return@Column
            }

            val encounter = selection.encounter
            Text("${rarityLabel(selection.template.rarity)} · ${selection.poi.name} · ${mechanicLabel(selection.template.mechanic)}")
            distanceMeters?.let { Text("현재 위치에서 약 ${it}m") }
            Text("상태 ${phaseLabel(encounter.phase)} · 단서 ${encounter.clueIds.size}/${selection.template.requiredClues}")

            when (encounter.phase) {
                EncounterPhase.HIDDEN -> Text("주변을 이동하면 180m 안에서 신호가 나타납니다.")
                EncounterPhase.HINTED -> Text("신호 포착 · 약 60m 안으로 접근하면 조사할 수 있습니다.")
                EncounterPhase.DISCOVERED -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (encounter.clueIds.size < selection.template.requiredClues) {
                            Button(onClick = {
                                val clueId = "${encounter.id}:clue-${encounter.clueIds.size + 1}"
                                val updated = reducer.reduce(encounter, EncounterEvent.CollectClue(clueId))
                                onCollectClue(clueId, updated)
                            }) { Text("단서 조사") }
                        }
                        Button(
                            enabled = encounter.clueIds.size >= selection.template.requiredClues,
                            onClick = {
                                val resolved = reducer.reduce(encounter, EncounterEvent.Resolve)
                                if (resolved.phase == EncounterPhase.RESOLVED) onResolve(resolved)
                            },
                        ) { Text("해결") }
                    }
                }
                EncounterPhase.RESOLVED -> {
                    Text("해결 완료. 장소/메커닉 기억이 동행에게 남아 이후 후보 선택에 반영됩니다.")
                    OutlinedButton(onClick = onContinue) { Text("다음 탐험 찾기") }
                }
            }
        }
    }
}

@Composable
private fun MapSurface(mapAdapter: MapViewAdapter, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(factory = mapAdapter::createView, modifier = modifier)

    DisposableEffect(lifecycleOwner, mapAdapter) {
        val lifecycle = lifecycleOwner.lifecycle
        when {
            lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) -> {
                mapAdapter.onStart()
                mapAdapter.onResume()
            }
            lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) -> mapAdapter.onStart()
            else -> Unit
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapAdapter.onStart()
                Lifecycle.Event.ON_RESUME -> mapAdapter.onResume()
                Lifecycle.Event.ON_PAUSE -> mapAdapter.onPause()
                Lifecycle.Event.ON_STOP -> mapAdapter.onStop()
                Lifecycle.Event.ON_DESTROY -> mapAdapter.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapAdapter.onDestroy()
        }
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun encounterMarkerTitle(selection: EncounterSelection): String = when (selection.encounter.phase) {
    EncounterPhase.HIDDEN -> "탐험 후보 · ${selection.poi.name}"
    EncounterPhase.HINTED -> "? 신호 · ${selection.poi.name}"
    EncounterPhase.DISCOVERED -> "! 조사 가능 · ${selection.poi.name}"
    EncounterPhase.RESOLVED -> "✓ 해결 · ${selection.poi.name}"
}

private fun phaseLabel(phase: EncounterPhase): String = when (phase) {
    EncounterPhase.HIDDEN -> "숨김"
    EncounterPhase.HINTED -> "신호 포착"
    EncounterPhase.DISCOVERED -> "발견"
    EncounterPhase.RESOLVED -> "해결"
}

private fun mechanicLabel(mechanic: MysteryMechanic): String = when (mechanic) {
    MysteryMechanic.TRACE_CHAIN -> "흔적 연결"
    MysteryMechanic.SOUND_PATTERN -> "소리 패턴"
    MysteryMechanic.TIME_LAYER -> "시간의 겹"
    MysteryMechanic.SYMBOL_MATCH -> "상징 맞추기"
    MysteryMechanic.LOST_OBJECT -> "잃어버린 물건"
    MysteryMechanic.PHOTO_ANGLE -> "시점 비교"
    MysteryMechanic.LOCAL_MEMORY -> "동네 기억"
    MysteryMechanic.COMPANION_SENSE -> "동행 감각"
}

private fun rarityLabel(rarity: EncounterRarity): String = when (rarity) {
    EncounterRarity.COMMON -> "일반"
    EncounterRarity.UNCOMMON -> "특별"
    EncounterRarity.RARE -> "희귀"
}

private fun timeBandLabel(timeBand: TimeBand): String = when (timeBand) {
    TimeBand.DAWN -> "이른 아침"
    TimeBand.DAY -> "낮"
    TimeBand.EVENING -> "저녁"
    TimeBand.NIGHT -> "밤"
}

private fun trackingPresetLabel(preset: LocationTrackingPreset): String = when (preset) {
    LocationTrackingPreset.BATTERY_SAVER -> "절약"
    LocationTrackingPreset.BALANCED -> "균형"
    LocationTrackingPreset.PRECISE -> "정밀"
}

private fun goalLabel(goal: GoalDefinition): String = when (goal.metric) {
    GoalMetric.WALK_DISTANCE_METERS -> "걷기"
    GoalMetric.DISCOVER_SPOT -> "새 장소 발견"
    GoalMetric.RESOLVE_MYSTERY -> "미스터리 해결"
    GoalMetric.COLLECT_CLUE -> "단서 수집"
}

private fun companionMomentLabel(name: String, moment: CompanionMoment): String = when (moment) {
    CompanionMoment.HINT_APPEARED -> "$name: 주변에서 신호를 감지했어요"
    CompanionMoment.SPOT_DISCOVERED -> "$name: 조사할 장소를 찾았어요"
    CompanionMoment.CLUE_FOUND -> "$name: 단서를 기록했어요"
    CompanionMoment.MYSTERY_RESOLVED -> "$name: 해결 기록이 쌓였어요"
}

private fun demoMysterySpots() = listOf(
    MysterySpot("cityhall-echo", "시청 광장의 이상한 메아리", GeoPoint(37.56650, 126.97800), 55.0),
    MysterySpot("stone-trace", "돌담길의 희미한 흔적", GeoPoint(37.56711, 126.97676), 45.0),
    MysterySpot("hidden-note", "덕수궁 옆 숨겨진 쪽지", GeoPoint(37.56792, 126.97543), 50.0),
)
