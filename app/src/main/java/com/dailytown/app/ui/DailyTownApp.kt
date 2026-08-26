package com.dailytown.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dailytown.app.BuildConfig
import com.dailytown.app.companion.CompanionMoment
import com.dailytown.app.companion.DefaultCompanionReactionPolicy
import com.dailytown.app.diagnostics.AndroidBatterySnapshotSource
import com.dailytown.app.diagnostics.FieldTestDiagnosticBuilder
import com.dailytown.app.diagnostics.FieldTestSessionMonitor
import com.dailytown.app.diagnostics.GameplaySessionMonitor
import com.dailytown.app.domain.*
import com.dailytown.app.location.*
import com.dailytown.app.map.MapHealthStatus
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.map.UserLocationSpec
import com.dailytown.app.mystery.*
import com.dailytown.app.persistence.toState
import com.dailytown.app.poi.PoiRepository
import com.dailytown.app.progress.*
import com.dailytown.app.reminder.LocalReminderManager
import com.dailytown.app.ui.visual.MapGameplayVisualBinder
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTownApp(
    mapAdapter: MapViewAdapter,
    progressStore: com.dailytown.app.persistence.ProgressStore,
    poiRepository: PoiRepository,
    reminderManager: LocalReminderManager,
) {
    val context = LocalContext.current
    val mapHealth by mapAdapter.health.collectAsState()
    val spots = remember { demoMysterySpots() }
    val defaultCompanion = remember { Companion("moru", "모루", 12) }
    val initialState = remember { ExplorationState(companion = defaultCompanion) }
    val session = remember {
        ExplorationSession(
            initialState = initialState,
            spots = spots,
            qualityPolicy = LocationQualityPolicy.forPreset(LocationTrackingPreset.BALANCED),
        )
    }

    val templates = remember { MysteryTemplateCatalog.defaults() }
    val reducer = remember { MysteryReducer(templates.associateBy { it.id }) }
    val encounterCoordinator = remember { EncounterCoordinator(templates = templates) }
    val reactionPolicy = remember { DefaultCompanionReactionPolicy() }
    val goalEvaluator = remember { GoalProgressEvaluator() }
    val trackingCoordinator = remember { TrackingSessionCoordinator() }
    val progressCoordinator = remember(progressStore) { ProgressRuntimeCoordinator(progressStore) }
    val fieldTestSessionMonitor = remember {
        FieldTestSessionMonitor(AndroidBatterySnapshotSource(context.applicationContext))
    }
    val gameplaySessionMonitor = remember { GameplaySessionMonitor() }
    val mapVisualBinder = remember(mapAdapter) { MapGameplayVisualBinder(mapAdapter) }

    val trackingRuntime by trackingCoordinator.state.collectAsState()
    val progressRuntime by progressCoordinator.state.collectAsState()
    val trackingMode = trackingRuntime.mode
    val trackingPreset = trackingRuntime.preset
    val gameProgress = progressRuntime.progress
    val dailyGoals = progressRuntime.dailyGoals
    val weeklyGoals = progressRuntime.weeklyGoals
    val persistenceReady = progressRuntime.ready

    var snapshot by remember { mutableStateOf(session.current()) }
    var activeEncounter by remember { mutableStateOf<EncounterSelection?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastCompanionMoment by remember { mutableStateOf<CompanionMoment?>(null) }
    var referenceDistanceText by remember { mutableStateOf("") }
    var sessionToken by remember { mutableIntStateOf(0) }

    val reminderPreference = remember { reminderManager.preference() }
    var reminderEnabled by remember { mutableStateOf(reminderPreference.enabled) }
    var reminderHour by remember { mutableIntStateOf(reminderPreference.hour) }

    val deviceSource = remember(trackingPreset) {
        FusedDeviceLocationSource(
            context = context.applicationContext,
            config = trackingPreset.config,
        )
    }
    val replaySource = remember { ReplayLocationSource() }
    val currentDate = LocalDate.now()

    LaunchedEffect(progressCoordinator) {
        try {
            val restored = progressCoordinator.restore(LocalDate.now())
            session.restore(restored.progress.toState(defaultCompanion))
            snapshot = session.current()
        } catch (error: Throwable) {
            errorMessage = "진행도 불러오기 실패: ${error.message ?: "unknown"}"
            val fallback = progressCoordinator.activateFallback(LocalDate.now())
            session.restore(fallback.progress.toState(defaultCompanion))
            snapshot = session.current()
        }
    }

    LaunchedEffect(currentDate, persistenceReady) {
        if (!persistenceReady) return@LaunchedEffect
        progressCoordinator.ensureCurrentPeriod(LocalDate.now())
    }

    LaunchedEffect(snapshot.state, persistenceReady) {
        if (!persistenceReady) return@LaunchedEffect
        progressCoordinator.syncExploration(snapshot.state, LocalDate.now())
    }

    LaunchedEffect(gameProgress, persistenceReady, progressRuntime.persistenceEnabled) {
        if (!persistenceReady || !progressRuntime.persistenceEnabled) return@LaunchedEffect
        try {
            progressCoordinator.persist()
        } catch (error: Throwable) {
            errorMessage = "진행도 저장 실패: ${error.message ?: "unknown"}"
        }
    }

    LaunchedEffect(trackingPreset) {
        session.setLocationQualityPolicy(LocationQualityPolicy.forPreset(trackingPreset))
        snapshot = session.current()
    }

    fun start(mode: TrackingMode) {
        sessionToken += 1
        session.restartTracking()
        encounterCoordinator.reset()
        gameplaySessionMonitor.reset()
        if (mode == TrackingMode.DEVICE) {
            fieldTestSessionMonitor.begin()
        } else {
            fieldTestSessionMonitor.reset()
        }
        snapshot = session.current()
        activeEncounter = null
        lastCompanionMoment = null
        errorMessage = null
        trackingCoordinator.start(mode)
    }

    fun applyReaction(moment: CompanionMoment) {
        lastCompanionMoment = moment
        val reaction = reactionPolicy.react(snapshot.state.companion, moment)
        session.applyCompanionBond(reaction.bondDelta)
        snapshot = session.current()
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) start(TrackingMode.DEVICE)
        else errorMessage = "위치 권한이 필요합니다. 리플레이 모드는 권한 없이 사용할 수 있습니다."
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            reminderManager.enable(reminderHour)
            reminderEnabled = true
        } else {
            reminderManager.disable()
            reminderEnabled = false
            errorMessage = "알림 권한이 없어 탐험 리마인더를 켤 수 없습니다."
        }
    }

    DisposableEffect(trackingMode, trackingPreset) {
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
        val previousEncounter = activeEncounter
        val nearbyPois = if (previousEncounter == null) {
            poiRepository.nearby(sample.point, radiusMeters = 900.0)
        } else {
            emptyList()
        }

        val step = encounterCoordinator.advance(
            current = previousEncounter,
            user = sample.point,
            nearbyPois = nearbyPois,
            runtime = EncounterRuntimeContext(
                visitedPoiIds = gameProgress.encounterVisitedPoiIds,
                recentPoiIds = gameProgress.recentPoiIds.toSet(),
                recentTemplateIds = gameProgress.recentTemplateIds.toSet(),
                recentPairKeys = gameProgress.recentPairKeys.toSet(),
                companionBond = snapshot.state.companion.bond,
                memoryKeys = gameProgress.companionMemoryKeys,
            ),
            date = LocalDate.now(),
            time = LocalTime.now(),
        )
        if (previousEncounter == null) {
            step.selection?.let { gameplaySessionMonitor.recordEncounterOffered(it.isRevisit) }
        }
        activeEncounter = step.selection

        when (step.transition) {
            EncounterTransition.HINTED -> {
                gameplaySessionMonitor.recordHinted()
                applyReaction(CompanionMoment.HINT_APPEARED)
            }
            EncounterTransition.DISCOVERED -> {
                val selection = step.selection ?: return@LaunchedEffect
                gameplaySessionMonitor.recordDiscovered(selection.isRevisit)
                progressCoordinator.mutate(LocalDate.now()) { progress ->
                    progress
                        .recordEncounterVisit(selection.poi.id, selection.template.id, LocalDate.now())
                        .recordMemory("poi:${selection.poi.id}")
                }
                applyReaction(CompanionMoment.SPOT_DISCOVERED)
            }
            EncounterTransition.NONE -> Unit
        }
    }

    LaunchedEffect(snapshot.currentLocation, activeEncounter) {
        val persistentMarkers = spots.map { MapMarkerSpec(it.id, it.title, it.position) }
        mapVisualBinder.applyEncounter(
            selection = activeEncounter,
            persistentMarkers = persistentMarkers,
        )
        snapshot.currentLocation?.let { sample ->
            mapAdapter.setUserLocation(UserLocationSpec(sample.point, sample.bearingDegrees))
            mapAdapter.setCamera(sample.point)
        }
    }

    val normalizedProgress = gameProgress.normalizePeriods(currentDate)
    val neighborhood = NeighborhoodProgress(
        districtKey = activeEncounter?.poi?.districtKey ?: "jung-gu",
        visitedPoiIds = gameProgress.encounterVisitedPoiIds,
        resolvedEncounterIds = gameProgress.resolvedEncounterIds,
        distanceWalkedMeters = snapshot.state.distanceWalkedMeters,
    )
    val distanceToEncounter = snapshot.currentLocation?.let { sample ->
        activeEncounter?.let { selection -> encounterCoordinator.distanceTo(sample.point, selection).roundToInt() }
    }
    val gameplayMetrics = gameplaySessionMonitor.snapshot()

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
                Text(
                    "지도: ${mapAdapter.providerId} · ${mapHealthLabel(mapHealth.status)} · 동행: ${snapshot.state.companion.name} · 호감도 ${snapshot.state.companion.bond}",
                )

                MapSurface(
                    mapAdapter = mapAdapter,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                )

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("누적 탐험 거리 ${snapshot.state.distanceWalkedMeters.roundToInt()}m")
                        Text("미스터리 단서 ${gameProgress.inventoryClueIds.size}개 · 해결 ${gameProgress.resolvedEncounterIds.size}건")
                        Text("탐험 POI ${gameProgress.encounterVisitedPoiIds.size}곳 · 동행 기억 ${gameProgress.companionMemoryKeys.size}개")
                        if (snapshot.totalLocationSampleCount > 0) {
                            Text(
                                "세션 ${snapshot.sessionDistanceMeters.roundToInt()}m · 추적 ${snapshot.trackingDurationSeconds}초 · GPS 수락 ${snapshot.acceptedLocationCount} · 제외 ${snapshot.rejectedLocationCount} · 제외율 ${snapshot.rejectedLocationRatePercent}%",
                            )
                        }
                        if (gameplayMetrics.encounterOfferedCount > 0) {
                            val resolutionRate = gameplayMetrics.encounterResolutionRatePercent?.let { "$it%" } ?: "-"
                            Text(
                                "세션 미스터리 발견 ${gameplayMetrics.discoveredEncounterCount} · 해결 ${gameplayMetrics.resolvedEncounterCount} · 해결률 $resolutionRate · 단서 ${gameplayMetrics.cluesCollectedCount}",
                            )
                            gameplayMetrics.repeatAreaFatigueProxyPercent?.let { fatigue ->
                                Text(
                                    "재방문 ${gameplayMetrics.revisitOfferedCount}건 · 반복 피로 proxy ${fatigue}%",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
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
                            gameplaySessionMonitor.recordClueCollected()
                            progressCoordinator.mutate(LocalDate.now()) { progress ->
                                progress.recordClue(clueId, LocalDate.now())
                            }
                            applyReaction(CompanionMoment.CLUE_FOUND)
                        }
                    },
                    onResolve = { resolved ->
                        val resolvingSelection = activeEncounter
                        activeEncounter = resolvingSelection?.copy(encounter = resolved)
                        resolvingSelection?.let { gameplaySessionMonitor.recordResolved(it.isRevisit) }
                        val mechanic = resolvingSelection?.template?.mechanic
                        progressCoordinator.mutate(LocalDate.now()) { progress ->
                            var updated = progress.recordResolution(resolved, LocalDate.now())
                            if (mechanic != null) {
                                updated = updated.recordMemory("mechanic:${mechanic.name}")
                            }
                            updated
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
                                    onClick = {
                                        if (trackingMode == TrackingMode.DEVICE) {
                                            fieldTestSessionMonitor.end()
                                        }
                                        trackingCoordinator.selectPreset(preset)
                                    },
                                    label = { Text(trackingPresetLabel(preset)) },
                                )
                            }
                        }
                    }
                }

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("탐험 리마인더", style = MaterialTheme.typography.titleMedium)
                        Text("기본은 꺼짐이며 위치를 사용하지 않습니다.", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (!enabled) {
                                        reminderManager.disable()
                                        reminderEnabled = false
                                    } else if (Build.VERSION.SDK_INT >= 33 && !reminderManager.canPostNotifications()) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        reminderManager.enable(reminderHour)
                                        reminderEnabled = true
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
                        if (reminderEnabled && !reminderManager.canPostNotifications()) {
                            Text(
                                "시스템 알림 권한이 꺼져 있어 리마인더가 표시되지 않습니다.",
                                color = MaterialTheme.colorScheme.error,
                            )
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
                            if (progressRuntime.persistenceEnabled) "진행도 저장 정상" else if (persistenceReady) "진행도 임시 모드 · 저장 비활성" else "진행도 복원 중",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedTextField(
                            value = referenceDistanceText,
                            onValueChange = { value ->
                                if (value.all(Char::isDigit)) referenceDistanceText = value
                            },
                            label = { Text("기준 경로 거리(m, 선택)") },
                            supportingText = { Text("좌표 대신 미리 확인한 총 거리 숫자만 입력합니다.") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedButton(onClick = {
                            val sessionMetrics = fieldTestSessionMonitor.metrics(
                                sessionDistanceMeters = snapshot.sessionDistanceMeters,
                                sessionDurationSeconds = snapshot.trackingDurationSeconds,
                                referenceDistanceMeters = referenceDistanceText.toIntOrNull(),
                            )
                            val report = FieldTestDiagnosticBuilder.build(
                                progress = normalizedProgress,
                                acceptedLocationCount = snapshot.acceptedLocationCount,
                                rejectedLocationCount = snapshot.rejectedLocationCount,
                                trackingDurationSeconds = snapshot.trackingDurationSeconds,
                                sessionMetrics = sessionMetrics,
                                gameplayMetrics = gameplayMetrics,
                                appVersion = BuildConfig.VERSION_NAME,
                                mapProvider = mapAdapter.providerId.name,
                                mapHealth = mapHealth,
                                trackingPreset = trackingPreset,
                            ).render()
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Daily Town field-test diagnostic")
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "진단 리포트 공유"))
                        }) { Text("진단 리포트 공유") }
                    }
                }

                FieldTestComparisonCard(
                    sessionToken = sessionToken,
                    canRecordCurrentSession = trackingMode == TrackingMode.OFF && snapshot.totalLocationSampleCount > 0,
                    buildDiagnostic = {
                        val sessionMetrics = fieldTestSessionMonitor.metrics(
                            sessionDistanceMeters = snapshot.sessionDistanceMeters,
                            sessionDurationSeconds = snapshot.trackingDurationSeconds,
                            referenceDistanceMeters = referenceDistanceText.toIntOrNull(),
                        )
                        FieldTestDiagnosticBuilder.build(
                            progress = normalizedProgress,
                            acceptedLocationCount = snapshot.acceptedLocationCount,
                            rejectedLocationCount = snapshot.rejectedLocationCount,
                            trackingDurationSeconds = snapshot.trackingDurationSeconds,
                            sessionMetrics = sessionMetrics,
                            gameplayMetrics = gameplayMetrics,
                            appVersion = BuildConfig.VERSION_NAME,
                            mapProvider = mapAdapter.providerId.name,
                            mapHealth = mapHealth,
                            trackingPreset = trackingPreset,
                        )
                    },
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (hasLocationPermission(context)) start(TrackingMode.DEVICE)
                        else locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }) { Text("실제 위치") }

                    OutlinedButton(
                        onClick = { start(TrackingMode.REPLAY) },
                        modifier = Modifier.testTag("tracking-replay"),
                    ) { Text("경로 리플레이") }
                    TextButton(onClick = {
                        if (trackingMode == TrackingMode.DEVICE) {
                            fieldTestSessionMonitor.end()
                        }
                        trackingCoordinator.stop()
                        mapAdapter.setUserLocation(null)
                    }) { Text("중지") }
                }

                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Text(
                    when (trackingMode) {
                        TrackingMode.DEVICE -> "실기기 위치 추적 중 · ${trackingPresetLabel(trackingPreset)}"
                        TrackingMode.REPLAY -> "서울시청 → 덕수궁 테스트 경로 재생 중"
                        TrackingMode.OFF -> when {
                            !persistenceReady -> "진행도 불러오는 중"
                            progressRuntime.persistenceEnabled -> "탐험 대기 중 · 게임 진행도 저장 활성"
                            else -> "탐험 대기 중 · 진행도 임시 모드"
                        }
                    },
                    modifier = Modifier.testTag("tracking-status"),
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
            Text("${rarityLabel(selection.rarity)} · ${selection.poi.name} · ${mechanicLabel(selection.template.mechanic)}")
            Text("컨텍스트 ${timeBandLabel(selection.context.timeBand)}${if (selection.isRevisit) " · 재방문" else ""}")
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
                    Text("해결 완료 · 동행에게 이 장소의 기억이 남았습니다.")
                    Button(onClick = onContinue) { Text("다음 탐험") }
                }
            }
        }
    }
}

private fun encounterMarkerTitle(selection: EncounterSelection): String = when (selection.encounter.phase) {
    EncounterPhase.HIDDEN -> "? · ${selection.poi.name}"
    EncounterPhase.HINTED -> "신호 · ${selection.poi.name}"
    EncounterPhase.DISCOVERED -> "조사 · ${selection.poi.name}"
    EncounterPhase.RESOLVED -> "해결 · ${selection.poi.name}"
}

private fun rarityLabel(rarity: EncounterRarity) = when (rarity) {
    EncounterRarity.COMMON -> "일반"
    EncounterRarity.UNCOMMON -> "특별"
    EncounterRarity.RARE -> "희귀"
}

private fun timeBandLabel(timeBand: TimeBand) = when (timeBand) {
    TimeBand.DAWN -> "새벽/아침"
    TimeBand.DAY -> "낮"
    TimeBand.EVENING -> "저녁"
    TimeBand.NIGHT -> "밤"
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

private fun phaseLabel(phase: EncounterPhase) = when (phase) {
    EncounterPhase.HIDDEN -> "잠김"
    EncounterPhase.HINTED -> "신호 포착"
    EncounterPhase.DISCOVERED -> "조사 가능"
    EncounterPhase.RESOLVED -> "해결 완료"
}

private fun mechanicLabel(mechanic: MysteryMechanic) = when (mechanic) {
    MysteryMechanic.TRACE_CHAIN -> "흔적 이어보기"
    MysteryMechanic.SOUND_PATTERN -> "소리 패턴"
    MysteryMechanic.TIME_LAYER -> "시간의 겹"
    MysteryMechanic.SYMBOL_MATCH -> "상징 맞추기"
    MysteryMechanic.LOST_OBJECT -> "잃어버린 물건"
    MysteryMechanic.PHOTO_ANGLE -> "시점 비교"
    MysteryMechanic.LOCAL_MEMORY -> "동네의 기억"
    MysteryMechanic.COMPANION_SENSE -> "동행의 감각"
}

private fun goalLabel(goal: GoalDefinition) = when (goal.metric) {
    GoalMetric.WALK_DISTANCE_METERS -> "걷기"
    GoalMetric.DISCOVER_SPOT -> "새 지점 발견"
    GoalMetric.RESOLVE_MYSTERY -> "미스터리 해결"
    GoalMetric.COLLECT_CLUE -> "단서 수집"
}

private fun companionMomentLabel(name: String, moment: CompanionMoment) = when (moment) {
    CompanionMoment.HINT_APPEARED -> "$name: 뭔가 가까이에 있는 것 같아."
    CompanionMoment.SPOT_DISCOVERED -> "$name: 여기, 한번 살펴보자."
    CompanionMoment.CLUE_FOUND -> "$name: 이건 기억해 둘 만한 단서야."
    CompanionMoment.MYSTERY_RESOLVED -> "$name: 이 장소는 오래 기억날 것 같아."
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

private fun demoMysterySpots() = listOf(
    MysterySpot("cityhall-echo", "시청 광장의 이상한 메아리", GeoPoint(37.56650, 126.97800), 55.0),
    MysterySpot("stone-trace", "돌담길의 희미한 흔적", GeoPoint(37.56711, 126.97676), 45.0),
    MysterySpot("hidden-note", "덕수궁 옆 숨겨진 쪽지", GeoPoint(37.56792, 126.97543), 50.0),
)
