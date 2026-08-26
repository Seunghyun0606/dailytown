package com.dailytown.app.visualqa

import android.graphics.Bitmap
import android.os.SystemClock
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.BuildConfig
import com.dailytown.app.MainActivity
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.map.MapBrightnessFamily
import com.dailytown.app.map.MapHealthStatus
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapThemeSpec
import com.dailytown.app.map.NaverMapAdapter
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
import com.dailytown.app.visual.MapQaComplexity
import com.dailytown.app.visual.MapQaMotionMode
import com.dailytown.app.visual.MapQaTimeAnchor
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.MarkerSemantic
import com.dailytown.app.visual.VisualThemeProfile
import com.dailytown.app.visual.VisualThemeProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NaverMapVisualQaTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val catalog by lazy { CandidateAssetCatalog(instrumentation.context.assets) }
    private val sourceCatalog by lazy { MapOverlayQaSourceCatalog(instrumentation.context.assets) }
    private val companionResolver by lazy { CompanionAssetResolver(ProductionVisualAssetRegistry) }
    private val companionRenderer by lazy {
        ProductionCompanionCanvasRenderer(
            AndroidProductionVisualAssetCatalog(instrumentation.targetContext.assets),
        )
    }

    @Test
    fun approvedOverlayMatrixRendersOnRealNaverMapAndEv1UsesMeasuredE2Luminance() {
        assumeTrue("NAVER credential is required for real-map visual QA", BuildConfig.NAVER_MAP_CONFIGURED)
        val eveningCheckpoints = sourceCatalog.verifyApprovedSourcesAndRuntimeContract()
        val diagnostics = NaverMapQaDiagnostics(instrumentation.targetContext)
        val adapter = NaverMapAdapter(BuildConfig.NAVER_MAP_NCP_KEY_ID, CandidateMarkerVisualSource(catalog))
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        lateinit var sceneView: NaverMapOverlayQaSceneView
        var outcome = "FAIL"
        var failureCategory: String? = null
        val readyStartedAt = SystemClock.elapsedRealtime()

        try {
            scenario.onActivity { activity ->
                val root = FrameLayout(activity)
                root.addView(
                    adapter.createView(activity),
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    ),
                )
                sceneView = NaverMapOverlayQaSceneView(activity).also { it.hideForBaseMapProof() }
                root.addView(
                    sceneView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    ),
                )
                activity.setContentView(root)
                adapter.onStart()
                adapter.onResume()
            }
            waitForReady(adapter, diagnostics, readyStartedAt)

            val fixtures = mapOf(
                MapQaComplexity.SIMPLE_RESIDENTIAL to MapFixture("simple_residential", GeoPoint(37.5947, 126.9632)),
                MapQaComplexity.DENSE_URBAN to MapFixture("dense_urban", GeoPoint(37.5665, 126.9780)),
                MapQaComplexity.MIXED_POI to MapFixture("mixed_poi", GeoPoint(37.5444, 127.0374)),
            )

            var baselineCaptureCount = 0
            MapOverlayQaMatrix.baselineCases.forEach { case ->
                val fixture = fixtures.getValue(case.mapComplexity)
                val profile = VisualThemeProfiles.forPhase(case.timeAnchor.phase)
                val hudMix = if (case.timeAnchor == MapQaTimeAnchor.NIGHT) 1f else 0f
                captureFullStack(
                    kind = "baseline",
                    id = case.id,
                    phase = case.timeAnchor.semantic,
                    fixture = fixture,
                    motionMode = case.motionMode,
                    profile = profile,
                    hudDeepNavyMix = hudMix,
                    adapter = adapter,
                    sceneView = sceneView,
                    diagnostics = diagnostics,
                )
                baselineCaptureCount++
            }
            assertEquals(MapOverlayQaMatrix.EXPECTED_BASELINE_CAPTURE_COUNT, baselineCaptureCount)

            // EV-1 checkpoint proof is intentionally dense-map focused: all five forced states are captured
            // in normal/reduced modes, while the approved baseline matrix already covers all three complexities.
            val eveningFixture = fixtures.getValue(MapQaComplexity.DENSE_URBAN)
            var ev1CaptureCount = 0
            eveningCheckpoints.forEach { checkpoint ->
                val profile = if (checkpoint.id == "E2") {
                    onMain {
                        sceneView.hideForBaseMapProof()
                        adapter.setTheme(
                            MapThemeSpec(
                                preferredBrightness = MapBrightnessFamily.LIGHT,
                                markerFamily = MarkerFamily.DAY,
                            ),
                        )
                        adapter.setCamera(eveningFixture.center, 16.0)
                        adapter.setMarkers(emptyList())
                    }
                    val measurement = awaitBaseMapEvidence(
                        storageName = "visual/naver-base/ev1.e2.measurement",
                        adapter = adapter,
                        diagnostics = diagnostics,
                    )
                    EveningVisualInterpolator.profile(checkpoint.progress, centerLuminance(measurement))
                } else {
                    EveningVisualInterpolator.profile(checkpoint.progress)
                }

                MapQaMotionMode.values().forEach { motionMode ->
                    val id = "${checkpoint.id.lowercase()}.${eveningFixture.id}.${motionMode.semantic}"
                    captureFullStack(
                        kind = "ev1_checkpoint",
                        id = id,
                        phase = "EVENING_${checkpoint.id}",
                        fixture = eveningFixture,
                        motionMode = motionMode,
                        profile = profile,
                        hudDeepNavyMix = checkpoint.hudDeepNavyMix,
                        adapter = adapter,
                        sceneView = sceneView,
                        diagnostics = diagnostics,
                    )
                    ev1CaptureCount++
                }
            }
            assertEquals(10, ev1CaptureCount)
            outcome = "PASS"
        } catch (failure: NaverQaFailure) {
            failureCategory = failure.category
            throw failure
        } catch (failure: Throwable) {
            failureCategory = "unexpected_test_failure"
            throw failure
        } finally {
            diagnostics.recordHealth("final-before-destroy", adapter.health.value)
            runCatching { diagnostics.write(outcome, failureCategory) }
            onMain {
                adapter.onPause()
                adapter.onStop()
                adapter.onDestroy()
            }
            scenario.close()
        }
    }

    private fun captureFullStack(
        kind: String,
        id: String,
        phase: String,
        fixture: MapFixture,
        motionMode: MapQaMotionMode,
        profile: VisualThemeProfile,
        hudDeepNavyMix: Float,
        adapter: NaverMapAdapter,
        sceneView: NaverMapOverlayQaSceneView,
        diagnostics: NaverMapQaDiagnostics,
    ) {
        onMain {
            sceneView.hideForBaseMapProof()
            adapter.setTheme(profile.toMapTheme())
            adapter.setCamera(fixture.center, 16.0)
            adapter.setMarkers(emptyList())
        }
        awaitBaseMapEvidence(
            storageName = "visual/naver-base/$kind.$id",
            adapter = adapter,
            diagnostics = diagnostics,
        )

        val companion = renderCompanion(profile, motionMode)
        onMain {
            adapter.setMarkers(markerScene(fixture.center))
            sceneView.bind(
                profile = profile,
                motionMode = motionMode,
                companionBitmap = companion,
                hudDeepNavyMix = hudDeepNavyMix,
            )
        }
        val capture = awaitScreenshot(adapter, diagnostics, "$kind.$id")
        val storageName = "visual/naver-matrix/$kind/$id"
        capture.writeToTestStorage(storageName)
        diagnostics.recordMatrixCapture(
            kind = kind,
            id = id,
            phase = phase,
            mapComplexity = fixture.id,
            motionMode = motionMode.semantic,
            markerFamily = profile.markerFamily.name,
            companionId = "moru",
            storageName = storageName,
        )
    }

    private fun renderCompanion(profile: VisualThemeProfile, motionMode: MapQaMotionMode): Bitmap {
        val resolved = companionResolver.resolve(
            CompanionVisualRequest(
                companionId = "moru",
                expression = CompanionExpression.NEUTRAL,
                lightingFamily = profile.companionLighting,
                appearanceProfile = AppearanceProfile.BASE,
                usageContext = CompanionUsageContext.MAP_AVATAR,
                motion = CompanionMotion.IDLE_BREATHE,
                reducedMotion = motionMode.reducedMotion,
            ),
        )
        return companionRenderer.render(resolved, targetPx = 192)
    }

    private fun VisualThemeProfile.toMapTheme() = MapThemeSpec(
        preferredBrightness = if (markerFamily == MarkerFamily.DARK) MapBrightnessFamily.DARK else MapBrightnessFamily.LIGHT,
        markerFamily = markerFamily,
        routeColor = route,
    )

    private fun onMain(block: () -> Unit) {
        instrumentation.runOnMainSync(block)
    }

    private fun markerScene(center: GeoPoint): List<MapMarkerSpec> = listOf(
        MapMarkerSpec("hinted", "hinted", offset(center, -.0010, -.0010), MarkerSemantic.ENCOUNTER_HINTED),
        MapMarkerSpec("discoverable", "discoverable", offset(center, .0007, -.0007), MarkerSemantic.ENCOUNTER_DISCOVERABLE),
        MapMarkerSpec("active", "active", center, MarkerSemantic.ENCOUNTER_ACTIVE, selected = true),
        MapMarkerSpec("solved", "solved", offset(center, -.0006, .0008), MarkerSemantic.ENCOUNTER_SOLVED),
        MapMarkerSpec("revisit", "revisit", offset(center, .0009, .0009), MarkerSemantic.ENCOUNTER_REVISIT),
        MapMarkerSpec("park", "park", offset(center, 0.0, .0013), MarkerSemantic.POI_PARK),
        MapMarkerSpec("clue", "clue", offset(center, .0013, 0.0), MarkerSemantic.CLUE),
    )

    private fun offset(point: GeoPoint, lat: Double, lon: Double) =
        GeoPoint(point.latitude + lat, point.longitude + lon)

    private fun waitForReady(
        adapter: NaverMapAdapter,
        diagnostics: NaverMapQaDiagnostics,
        startedAtMs: Long,
    ) {
        repeat(40) {
            val health = adapter.health.value
            when (health.status) {
                MapHealthStatus.READY -> {
                    diagnostics.recordReady(SystemClock.elapsedRealtime() - startedAtMs, health)
                    return
                }
                MapHealthStatus.AUTH_ERROR -> throw NaverQaFailure(
                    "auth_error",
                    authFailureMessage(health.errorCode),
                )
                MapHealthStatus.ERROR -> throw NaverQaFailure(
                    "adapter_initialization_error",
                    "NAVER map failed visual-QA initialization: ${health.errorCode}",
                )
                else -> Thread.sleep(250)
            }
        }
        diagnostics.recordHealth("ready-timeout", adapter.health.value)
        throw NaverQaFailure("ready_timeout", "NAVER map did not reach READY during visual QA")
    }

    private fun awaitBaseMapEvidence(
        storageName: String,
        adapter: NaverMapAdapter,
        diagnostics: NaverMapQaDiagnostics,
    ): Bitmap {
        var latest: Bitmap? = null
        var latestEvidence: BaseMapEvidence? = null
        repeat(15) { index ->
            Thread.sleep(400)
            assertProviderHealthy(adapter, diagnostics, "$storageName.attempt-${index + 1}")
            val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return@repeat
            latest = bitmap
            val evidence = BaseMapEvidenceAnalyzer.analyze(bitmap)
            latestEvidence = evidence
            val passed = evidence.hasProviderTexture()
            diagnostics.recordEvidence(storageName, index + 1, bitmap, evidence, passed)
            if (passed) {
                bitmap.writeToTestStorage(storageName)
                return bitmap
            }
        }
        val result = latest ?: throw NaverQaFailure("screenshot_unavailable", "Device screenshot unavailable")
        result.writeToTestStorage("$storageName.failed")
        val evidence = latestEvidence ?: BaseMapEvidenceAnalyzer.analyze(result)
        val category = if (diagnostics.isNetworkValidated()) {
            "provider_texture_insufficient"
        } else {
            "network_not_validated"
        }
        throw NaverQaFailure(
            category,
            "NAVER marker-free base map did not show provider-map texture " +
                "(quantizedColors=${evidence.quantizedColors}, luminanceStdDev=${evidence.luminanceStdDev}, " +
                "strongEdgeRatio=${evidence.strongEdgeRatio})",
        )
    }

    private fun awaitScreenshot(
        adapter: NaverMapAdapter,
        diagnostics: NaverMapQaDiagnostics,
        stage: String,
    ): Bitmap {
        Thread.sleep(450)
        assertProviderHealthy(adapter, diagnostics, stage)
        return instrumentation.uiAutomation.takeScreenshot()
            ?: throw NaverQaFailure("screenshot_unavailable", "Device screenshot unavailable")
    }

    private fun assertProviderHealthy(
        adapter: NaverMapAdapter,
        diagnostics: NaverMapQaDiagnostics,
        stage: String,
    ) {
        val health = adapter.health.value
        diagnostics.recordHealth(stage, health)
        when (health.status) {
            MapHealthStatus.AUTH_ERROR -> throw NaverQaFailure(
                "auth_error_after_ready",
                authFailureMessage(health.errorCode),
            )
            MapHealthStatus.ERROR -> throw NaverQaFailure(
                "adapter_error_after_ready",
                "NAVER adapter failed after READY: ${health.errorCode}",
            )
            MapHealthStatus.READY -> Unit
            else -> throw NaverQaFailure(
                "provider_left_ready_state",
                "NAVER map left READY state during visual QA: ${health.status}",
            )
        }
    }

    private fun authFailureMessage(errorCode: String?): String = if (errorCode == "401") {
        "NAVER authentication failed (401). Verify that the injected NCP Key ID belongs to the intended " +
            "Maps application and that its Android package registration is exactly ${BuildConfig.APPLICATION_ID}."
    } else {
        "NAVER authentication failed during visual QA: $errorCode"
    }

    private fun centerLuminance(bitmap: Bitmap): Float {
        var sum = 0.0
        var count = 0
        val left = bitmap.width * 3 / 10
        val right = bitmap.width * 7 / 10
        val top = bitmap.height * 3 / 10
        val bottom = bitmap.height * 7 / 10
        val stepX = ((right - left) / 20).coerceAtLeast(1)
        val stepY = ((bottom - top) / 20).coerceAtLeast(1)
        for (y in top until bottom step stepY) for (x in left until right step stepX) {
            val color = bitmap.getPixel(x, y)
            fun linear(channel: Int): Double {
                val c = channel / 255.0
                return if (c <= .04045) c / 12.92 else Math.pow((c + .055) / 1.055, 2.4)
            }
            sum += .2126 * linear((color shr 16) and 0xFF) +
                .7152 * linear((color shr 8) and 0xFF) +
                .0722 * linear(color and 0xFF)
            count++
        }
        return (sum / count.coerceAtLeast(1)).toFloat().coerceIn(0f, 1f)
    }

    private data class MapFixture(val id: String, val center: GeoPoint)

    private class NaverQaFailure(
        val category: String,
        message: String,
    ) : IllegalStateException(message)
}
