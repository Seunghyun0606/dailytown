package com.dailytown.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import com.dailytown.app.domain.*
import com.dailytown.app.location.*
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.map.UserLocationSpec
import com.dailytown.app.persistence.ProgressStore
import com.dailytown.app.persistence.toProgress
import com.dailytown.app.persistence.toState
import com.dailytown.app.progress.GoalCatalog
import com.dailytown.app.progress.GoalPeriod
import com.dailytown.app.progress.GoalPlanner
import java.time.LocalDate
import kotlin.math.roundToInt

private enum class TrackingMode { OFF, DEVICE, REPLAY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTownApp(
    mapAdapter: MapViewAdapter,
    progressStore: ProgressStore,
) {
    val context = LocalContext.current
    val spots = remember { demoMysterySpots() }
    val defaultCompanion = remember { Companion("moru", "모루", 12) }
    val initialState = remember { ExplorationState(companion = defaultCompanion) }
    val session = remember { ExplorationSession(initialState, spots) }
    var snapshot by remember { mutableStateOf(session.current()) }
    var trackingMode by remember { mutableStateOf(TrackingMode.OFF) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var persistenceReady by remember { mutableStateOf(false) }

    val deviceSource = remember { FusedDeviceLocationSource(context.applicationContext) }
    val replaySource = remember { ReplayLocationSource() }
    val dailyGoals = remember {
        GoalPlanner().plan(
            periodKey = LocalDate.now().toString(),
            catalog = GoalCatalog.defaults().filter { it.period == GoalPeriod.DAILY },
            recentlyUsedIds = emptySet(),
            count = 2,
        )
    }

    LaunchedEffect(progressStore) {
        try {
            val restored = progressStore.load().toState(defaultCompanion)
            session.restore(restored)
            snapshot = session.current()
        } catch (error: Throwable) {
            errorMessage = "진행도 불러오기 실패: ${error.message ?: "unknown"}"
        } finally {
            persistenceReady = true
        }
    }

    LaunchedEffect(snapshot.state, persistenceReady) {
        if (!persistenceReady) return@LaunchedEffect
        try {
            progressStore.save(snapshot.state.toProgress())
        } catch (error: Throwable) {
            errorMessage = "진행도 저장 실패: ${error.message ?: "unknown"}"
        }
    }

    fun start(mode: TrackingMode) {
        session.restartTracking()
        snapshot = session.current()
        errorMessage = null
        trackingMode = mode
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) start(TrackingMode.DEVICE)
        else errorMessage = "위치 권한이 필요합니다. 리플레이 모드는 권한 없이 사용할 수 있습니다."
    }

    DisposableEffect(trackingMode) {
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

    LaunchedEffect(snapshot.currentLocation, spots) {
        mapAdapter.setMarkers(spots.map { MapMarkerSpec(it.id, it.title, it.position) })
        snapshot.currentLocation?.let { sample ->
            mapAdapter.setUserLocation(UserLocationSpec(sample.point, sample.bearingDegrees))
            mapAdapter.setCamera(sample.point)
        }
    }

    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Daily Town") }) }) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                Text("오늘의 동네 탐험", style = MaterialTheme.typography.headlineSmall)
                Text("지도: ${mapAdapter.providerId} · 동행: ${snapshot.state.companion.name} · 호감도 ${snapshot.state.companion.bond}")

                MapSurface(
                    mapAdapter = mapAdapter,
                    modifier = Modifier.fillMaxWidth().height(270.dp),
                )

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("누적 탐험 거리 ${snapshot.state.distanceWalkedMeters.roundToInt()}m")
                        Text("발견 단서 ${snapshot.state.cluesFound}개")
                        Text("방문 지점 ${snapshot.state.visitedSpotIds.size}개")
                        if (snapshot.rejectedLocationCount > 0) {
                            Text("GPS 품질 필터 제외 ${snapshot.rejectedLocationCount}회")
                        }
                        snapshot.newlyDiscovered.firstOrNull()?.let {
                            Text("새 발견: ${it.title}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("오늘의 목표", style = MaterialTheme.typography.titleMedium)
                        dailyGoals.forEach { goal -> Text("• ${goal.metric} × ${goal.target}") }
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
                        TrackingMode.DEVICE -> "실기기 위치 추적 중"
                        TrackingMode.REPLAY -> "서울시청 → 덕수궁 테스트 경로 재생 중"
                        TrackingMode.OFF -> if (persistenceReady) "탐험 대기 중 · 진행도 저장 활성" else "진행도 불러오는 중"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
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

private fun demoMysterySpots() = listOf(
    MysterySpot("cityhall-echo", "시청 광장의 이상한 메아리", GeoPoint(37.56650, 126.97800), 55.0),
    MysterySpot("stone-trace", "돌담길의 희미한 흔적", GeoPoint(37.56711, 126.97676), 45.0),
    MysterySpot("hidden-note", "덕수궁 옆 숨겨진 쪽지", GeoPoint(37.56792, 126.97543), 50.0),
)
