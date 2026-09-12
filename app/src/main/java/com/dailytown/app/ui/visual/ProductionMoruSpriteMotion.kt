package com.dailytown.app.ui.visual

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.dailytown.app.BuildConfig
import com.dailytown.app.visual.CompanionAssetResolver
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionMotion
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest
import com.dailytown.app.visual.MotionLoopMode
import kotlinx.coroutines.delay

/**
 * Visible M-B prototype for internal/debug builds.
 *
 * This is intentionally not the final human-approved M-B tuning. It turns the approved Moru vector
 * layers into a real bitmap sprite atlas and then displays one cropped atlas frame at a time. Release
 * builds stay on the static production renderer until timing/easing/intensity receive final approval.
 */
internal object MoruPrototypeSpriteManifest {
    const val approvalState = "prototype_pending_human_tuning"

    data class Frame(
        val expression: CompanionExpression,
        val scale: Float = 1f,
        val translateYFraction: Float = 0f,
        val rotationDegrees: Float = 0f,
        val durationMs: Long,
    )

    data class Sequence(
        val motion: CompanionMotion,
        val loopMode: MotionLoopMode,
        val frames: List<Frame>,
    )

    private val idle = Sequence(
        motion = CompanionMotion.IDLE_BREATHE,
        loopMode = MotionLoopMode.LOOP,
        frames = listOf(
            Frame(CompanionExpression.NEUTRAL, scale = 0.985f, translateYFraction = 0.008f, durationMs = 170L),
            Frame(CompanionExpression.NEUTRAL, scale = 0.995f, translateYFraction = 0.004f, durationMs = 170L),
            Frame(CompanionExpression.NEUTRAL, scale = 1.008f, translateYFraction = -0.003f, durationMs = 170L),
            Frame(CompanionExpression.NEUTRAL, scale = 1.014f, translateYFraction = -0.006f, durationMs = 170L),
            Frame(CompanionExpression.NEUTRAL, scale = 1.003f, translateYFraction = -0.001f, durationMs = 170L),
            Frame(CompanionExpression.NEUTRAL, scale = 0.990f, translateYFraction = 0.006f, durationMs = 170L),
        ),
    )

    private val clue = Sequence(
        motion = CompanionMotion.CLUE_REACT,
        loopMode = MotionLoopMode.ONCE,
        frames = listOf(
            Frame(CompanionExpression.CURIOUS, scale = 0.99f, rotationDegrees = -1.5f, durationMs = 90L),
            Frame(CompanionExpression.CURIOUS, scale = 1.015f, translateYFraction = -0.012f, rotationDegrees = 1.5f, durationMs = 90L),
            Frame(CompanionExpression.SURPRISED, scale = 1.035f, translateYFraction = -0.025f, durationMs = 105L),
            Frame(CompanionExpression.CLUE_FOUND, scale = 1.02f, translateYFraction = -0.014f, durationMs = 120L),
            Frame(CompanionExpression.CLUE_FOUND, durationMs = 180L),
        ),
    )

    private val resolved = Sequence(
        motion = CompanionMotion.RESOLVED_SETTLE,
        loopMode = MotionLoopMode.ONCE,
        frames = listOf(
            Frame(CompanionExpression.CLUE_FOUND, scale = 1.015f, translateYFraction = -0.012f, durationMs = 110L),
            Frame(CompanionExpression.RESOLVED, scale = 1.035f, translateYFraction = -0.025f, durationMs = 120L),
            Frame(CompanionExpression.RESOLVED, scale = 0.995f, translateYFraction = 0.008f, durationMs = 130L),
            Frame(CompanionExpression.RESOLVED, scale = 1.006f, translateYFraction = -0.003f, durationMs = 130L),
            Frame(CompanionExpression.RESOLVED, durationMs = 190L),
        ),
    )

    fun sequence(motion: CompanionMotion): Sequence? = when (motion) {
        CompanionMotion.IDLE_BREATHE -> idle
        CompanionMotion.CLUE_REACT -> clue
        CompanionMotion.RESOLVED_SETTLE -> resolved
        CompanionMotion.WALK -> null
    }
}

private fun prototypeMotionFor(request: CompanionVisualRequest): CompanionMotion =
    request.motion ?: when (request.expression) {
        CompanionExpression.CLUE_FOUND -> CompanionMotion.CLUE_REACT
        CompanionExpression.RESOLVED -> CompanionMotion.RESOLVED_SETTLE
        else -> CompanionMotion.IDLE_BREATHE
    }

@Composable
internal fun ProductionMoruSpriteMotionVisual(
    request: CompanionVisualRequest,
    modifier: Modifier,
    contentDescription: String?,
    rasterTargetPx: Int,
) {
    val motion = prototypeMotionFor(request)
    val sequence = MoruPrototypeSpriteManifest.sequence(motion)
    if (
        !BuildConfig.DEBUG ||
        request.companionId != "moru" ||
        request.usageContext == CompanionUsageContext.JOURNAL_STAMP ||
        request.reducedMotion ||
        sequence == null
    ) {
        StaticProductionCompanionVisual(
            request = request,
            modifier = modifier,
            contentDescription = contentDescription,
            rasterTargetPx = rasterTargetPx,
        )
        return
    }

    val applicationContext = LocalContext.current.applicationContext
    val atlas = remember(request, sequence, rasterTargetPx, applicationContext) {
        val resolver = CompanionAssetResolver(ProductionVisualAssetRegistry)
        val renderer = ProductionCompanionCanvasRenderer(
            AndroidProductionVisualAssetCatalog(applicationContext.assets),
        )
        buildSpriteAtlas(
            request = request,
            sequence = sequence,
            targetPx = rasterTargetPx,
            resolver = resolver,
            renderer = renderer,
        )
    }

    var frameIndex by remember(request, sequence) { mutableIntStateOf(0) }
    LaunchedEffect(request, sequence) {
        frameIndex = 0
        while (true) {
            delay(sequence.frames[frameIndex].durationMs)
            if (frameIndex < sequence.frames.lastIndex) {
                frameIndex += 1
            } else if (sequence.loopMode == MotionLoopMode.LOOP) {
                frameIndex = 0
            } else {
                break
            }
        }
    }

    val frameBitmap = remember(atlas, frameIndex, rasterTargetPx) {
        Bitmap.createBitmap(
            atlas,
            frameIndex * rasterTargetPx,
            0,
            rasterTargetPx,
            rasterTargetPx,
        )
    }

    Image(
        bitmap = frameBitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

private fun buildSpriteAtlas(
    request: CompanionVisualRequest,
    sequence: MoruPrototypeSpriteManifest.Sequence,
    targetPx: Int,
    resolver: CompanionAssetResolver,
    renderer: ProductionCompanionCanvasRenderer,
): Bitmap {
    require(targetPx > 0)
    val atlas = Bitmap.createBitmap(
        targetPx * sequence.frames.size,
        targetPx,
        Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(atlas)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    sequence.frames.forEachIndexed { index, frame ->
        val resolved = resolver.resolve(
            request.copy(
                expression = frame.expression,
                motion = null,
            ),
        )
        val source = renderer.render(resolved, targetPx)
        val cellLeft = index * targetPx.toFloat()
        val scaled = targetPx * frame.scale
        val inset = (targetPx - scaled) / 2f
        val translateY = targetPx * frame.translateYFraction
        val destination = RectF(
            cellLeft + inset,
            inset + translateY,
            cellLeft + targetPx - inset,
            targetPx - inset + translateY,
        )

        canvas.save()
        canvas.rotate(
            frame.rotationDegrees,
            cellLeft + targetPx / 2f,
            targetPx / 2f,
        )
        canvas.drawBitmap(source, null, destination, paint)
        canvas.restore()
    }
    return atlas
}
