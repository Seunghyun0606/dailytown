package com.dailytown.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailytown.app.R
import com.dailytown.app.ui.visual.LocalDailyTownCompanionLighting
import com.dailytown.app.ui.visual.ProductionCompanionVisual
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest

/**
 * First-run guide (scenario v2 candidate §8): brief promise → how a walk works + location/privacy
 * explanation before any permission prompt → meeting Moru as the default companion → Explore.
 * Companion selection is intentionally absent (deferred until a companion roster exists).
 * The permission itself is still requested only when the player taps "탐험 시작".
 */
@Composable
fun OnboardingFlow(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = page > 0) { page -= 1 }

    Surface(modifier = modifier.fillMaxSize().testTag("onboarding"), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            PageDots(current = page, count = 3)
            when (page) {
                0 -> WelcomePage()
                1 -> HowItWorksPage()
                else -> MeetMoruPage()
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { if (page < 2) page += 1 else onFinished() },
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).testTag("onboarding-next"),
            ) {
                Text(
                    when (page) {
                        0 -> "시작하기"
                        1 -> "다음"
                        else -> "모루와 함께 걷기"
                    },
                )
            }
            if (page < 2) {
                TextButton(
                    onClick = onFinished,
                    modifier = Modifier.align(Alignment.CenterHorizontally).testTag("onboarding-skip"),
                ) { Text("건너뛰기") }
            }
        }
    }
}

@Composable
private fun PageDots(current: Int, count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            Box(
                Modifier
                    .size(if (index == current) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding-welcome"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .size(132.dp)
                .clip(MaterialTheme.shapes.large)
                .background(androidx.compose.ui.res.colorResource(R.color.id_a_icon_background)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Daily Town",
                modifier = Modifier.size(132.dp),
            )
        }
        Text("Daily Town", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "익숙한 동네에서 작은 발견을 찾아요.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            "조금만 걸어도 오늘의 이야기가 생겨요.\n멀리 갈 필요 없어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HowItWorksPage() {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding-how"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("이렇게 걸어요", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Step("1", "걷기 시작", "탐험 시작을 누르고 평소처럼 동네를 걸어요.")
        Step("2", "신호 따라가기", "근처에 작은 신호가 나타나면 가까이 다가가 보세요.")
        Step("3", "살펴보고 기록하기", "단서를 살펴 이야기를 풀면 오늘의 기록과 모루와의 기억이 남아요.")
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth().testTag("onboarding-privacy"),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("위치 정보는 이렇게만 써요", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("· 탐험 중일 때만, 앱이 화면에 있을 때만 위치를 사용해요.", style = MaterialTheme.typography.bodyMedium)
                Text("· 이동 경로는 저장하지 않고 거리·발견 같은 기록만 기기에 남겨요.", style = MaterialTheme.typography.bodyMedium)
                Text("· 위치 권한은 처음 탐험을 시작할 때 물어봐요.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun Step(number: String, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text(number, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MeetMoruPage() {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding-meet-moru"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProductionCompanionVisual(
            request = CompanionVisualRequest(
                companionId = "moru",
                expression = CompanionExpression.HAPPY,
                lightingFamily = LocalDailyTownCompanionLighting.current,
                appearanceProfile = AppearanceProfile.BASE,
                usageContext = CompanionUsageContext.COMPANION_PORTRAIT,
            ),
            modifier = Modifier.size(232.dp),
            contentDescription = "동행 캐릭터 모루",
            rasterTargetPx = 384,
        )
        Text("모루", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
            Text(
                "“안녕, 나는 모루야. 오늘은 가까운 골목부터 같이 걸어볼래? 작은 걸 발견하면 내가 먼저 알려줄게.”",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "함께 걸을수록 모루와 가까워지고, 같이 발견한 장소는 기억으로 남아요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
