package com.dailytown.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dailytown.app.domain.*

@Composable
fun DailyTownApp() {
    var explored by remember { mutableIntStateOf(0) }
    var clues by remember { mutableIntStateOf(0) }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Daily Town") }) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("오늘의 동네 탐험", style = MaterialTheme.typography.headlineMedium)
                Text("지도 SDK를 연결하기 전에도 핵심 탐험 루프를 검증할 수 있는 MVP 골격입니다.")

                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("동행: 모루 · 호감도 12")
                        Text("탐험 거리: ${explored * 120}m")
                        Text("발견한 단서: $clues")
                    }
                }

                Button(
                    onClick = {
                        explored += 1
                        if (explored % 2 == 1) clues += 1
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("탐험 시뮬레이션")
                }

                Text(
                    "다음 실제 연동: 위치 권한 → 위치 스트림 → 지도 SDK → 공공데이터 POI",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
