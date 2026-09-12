package com.dailytown.app.visualqa

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.ui.visual.AndroidProductionVisualAssetCatalog
import com.dailytown.app.ui.visual.ProductionCompanionCanvasRenderer
import com.dailytown.app.ui.visual.ProductionVisualAssetRegistry
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionAssetResolver
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionMotion
import com.dailytown.app.visual.CompanionUsageContext
import com.dailytown.app.visual.CompanionVisualRequest
import com.dailytown.app.visual.EveningVisualInterpolator
import com.dailytown.app.visual.MapOverlayQaMatrix
import com.dailytown.app.visual.MapQaMotionMode
import com.dailytown.app.visual.MapQaTimeAnchor
import com.dailytown.app.visual.VisualThemeProfile
import com.dailytown.app.visual.VisualThemeProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapOverlayQaContractInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun approvedMatrixAndEv1FixtureRenderWithoutProviderDependency() {
        val checkpoints = MapOverlayQaSourceCatalog(instrumentation.context.assets)
            .verifyApprovedSourcesAndRuntimeContract()
        assertEquals(18, MapOverlayQaMatrix.baselineCases.size)
        assertEquals(5, checkpoints.size)

        val resolver = CompanionAssetResolver(ProductionVisualAssetRegistry)
        val renderer = ProductionCompanionCanvasRenderer(
            AndroidProductionVisualAssetCatalog(instrumentation.targetContext.assets),
        )

        val representative = buildList {
            MapQaTimeAnchor.values().forEach { anchor ->
                add(anchor.semantic.lowercase() to VisualThemeProfiles.forPhase(anchor.phase))
            }
            checkpoints.forEach { checkpoint ->
                add("ev1-${checkpoint.id.lowercase()}" to EveningVisualInterpolator.profile(checkpoint.progress, .5f))
            }
        }
        assertEquals(8, representative.size)

        representative.forEach { (id, profile) ->
            MapQaMotionMode.values().forEach { motionMode ->
                val companion = renderer.render(
                    resolver.resolve(companionRequest(profile, motionMode)),
                    targetPx = 192,
                )
                val bitmap = renderFixture(profile, motionMode, companion)
                assertTrue("overlay fixture $id/${motionMode.semantic} must render pixels", hasVisiblePixel(bitmap))
                bitmap.writeToTestStorage("visual/map-overlay-contract/$id.${motionMode.semantic}")
            }
        }
    }

    private fun companionRequest(
        profile: VisualThemeProfile,
        motionMode: MapQaMotionMode,
    ) = CompanionVisualRequest(
        companionId = "moru",
        expression = CompanionExpression.NEUTRAL,
        lightingFamily = profile.companionLighting,
        appearanceProfile = AppearanceProfile.BASE,
        usageContext = CompanionUsageContext.MAP_AVATAR,
        motion = CompanionMotion.IDLE_BREATHE,
        reducedMotion = motionMode.reducedMotion,
    )

    private fun renderFixture(
        profile: VisualThemeProfile,
        motionMode: MapQaMotionMode,
        companion: Bitmap,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        instrumentation.runOnMainSync {
            val view = NaverMapOverlayQaSceneView(instrumentation.targetContext)
            view.layout(0, 0, bitmap.width, bitmap.height)
            view.bind(
                profile = profile,
                motionMode = motionMode,
                companionBitmap = companion,
                hudDeepNavyMix = when {
                    profile.phase.name == "NIGHT" -> 1f
                    profile.eveningProgress != null -> profile.eveningProgress
                    else -> 0f
                },
            )
            view.draw(Canvas(bitmap))
        }
        return bitmap
    }

    private fun hasVisiblePixel(bitmap: Bitmap): Boolean {
        val stepX = (bitmap.width / 40).coerceAtLeast(1)
        val stepY = (bitmap.height / 40).coerceAtLeast(1)
        for (y in 0 until bitmap.height step stepY) {
            for (x in 0 until bitmap.width step stepX) {
                if ((bitmap.getPixel(x, y) ushr 24) != 0) return true
            }
        }
        return false
    }
}
