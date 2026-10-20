package com.dailytown.app.ui.visual

import android.provider.Settings
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dailytown.app.visual.CompanionMotion
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.MbCalmReactiveMotion
import com.dailytown.app.visual.MotionTransformSample

/** Android "Remove animations" (animator duration scale 0) must switch states immediately (M-B2 reduced motion). */
@Composable
fun rememberSystemAnimationsDisabled(): Boolean {
    val resolver = LocalContext.current.applicationContext.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/**
 * Applies the locked M-B2 presentation transform around already-resolved semantic companion art.
 * The transform is read inside graphicsLayer, so a running loop only invalidates the layer and never
 * recomposes the image. One-shot reactions restart when [restartKey] changes, then hold at rest.
 */
@Composable
fun MbCalmReactiveMotionBox(
    motion: CompanionMotion,
    usage: CompanionUsageContext,
    enabled: Boolean,
    restartKey: Any?,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    if (!enabled || motion == CompanionMotion.WALK) {
        // Static path is byte-for-byte the caller's original layout: no wrapper node, same tags/sizes.
        content(modifier)
        return
    }

    val idle = motion == CompanionMotion.IDLE_BREATHE
    var sample by remember(motion, restartKey) { mutableStateOf(MotionTransformSample.REST) }
    LaunchedEffect(motion, restartKey) {
        // The idle loop is an infinite animation: route it through the InfiniteAnimationPolicy-aware
        // frame API so UI tests (and any host that pauses infinite animations) stay idle. One-shot
        // reactions are finite and use the regular frame clock.
        suspend fun <R> frame(block: (Long) -> R): R =
            if (idle) withInfiniteAnimationFrameMillis(block) else withFrameMillis(block)

        val start = frame { it }
        while (true) {
            val next = frame { now -> MbCalmReactiveMotion.sample(motion, now - start) }
            sample = next
            if (next.finished) break
        }
    }

    val capDp = MbCalmReactiveMotion.idleCapDp(usage)
    Box(
        propagateMinConstraints = true,
        modifier = modifier.graphicsLayer {
            val current = sample
            val translation = current.translateYFraction * size.height
            val cap = capDp.dp.toPx()
            translationY = if (idle) translation.coerceIn(-cap, cap) else translation
            scaleX = current.scale
            scaleY = current.scale
            rotationZ = current.rotationDegrees
            transformOrigin = TransformOrigin(MbCalmReactiveMotion.PIVOT_X, MbCalmReactiveMotion.PIVOT_Y)
        },
    ) { content(Modifier) }
}
