package com.dailytown.app.visualqa

import android.graphics.Bitmap
import android.os.SystemClock
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
import com.dailytown.app.visual.EveningVisualInterpolator
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.MarkerSemantic
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NaverMapVisualQaTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val catalog by lazy {
        CandidateAssetCatalog(instrumentation.context.assets)
    }

    @Test
    fun dayDarkMarkersRenderOnRealNaverMapAndE2CanSelectByMeasuredLuminance() {
        assumeTrue("NAVER credential is required for real-map visual QA", BuildConfig.NAVER_MAP_CONFIGURED)
        val diagnostics = NaverMapQaDiagnostics(instrumentation.targetContext)
        val adapter = NaverMapAdapter(BuildConfig.NAVER_MAP_NCP_KEY_ID, CandidateMarkerVisualSource(catalog))
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        var outcome = "FAIL"
        var failureCategory: String? = null
        val readyStartedAt = SystemClock.elapsedRealtime()
        try {
            scenario.onActivity { activity ->
                activity.setContentView(adapter.createView(activity))
                adapter.onStart()
                adapter.onResume()
            }
            waitForReady(adapter, diagnostics, readyStartedAt)

            val fixtures = listOf(
                MapFixture("sparse-residential", GeoPoint(37.5947, 126.9632)),
                MapFixture("dense-urban", GeoPoint(37.5665, 126.9780)),
                MapFixture("green-space", GeoPoint(37.5444, 127.0374)),
            )
            fixtures.forEach { fixture ->
                listOf(MarkerFamily.DAY, MarkerFamily.DARK).forEach { family ->
                    onMain {
                        adapter.setTheme(
                            MapThemeSpec(
                                preferredBrightness = if (family == MarkerFamily.DARK) MapBrightnessFamily.DARK else MapBrightnessFamily.LIGHT,
                                markerFamily = family,
                            ),
                        )
                        adapter.setCamera(fixture.center, zoom = 16.0)
                        adapter.setMarkers(emptyList())
                    }
                    // A marker-free capture is mandatory. Marker pixels must never be able to make a blank
                    // provider surface look like a successful real-map render.
                    awaitBaseMapEvidence(
                        storageName = "visual/naver-base/${fixture.id}.${family.name.lowercase()}",
                        adapter = adapter,
                        diagnostics = diagnostics,
                    )

                    onMain { adapter.setMarkers(markerScene(fixture.center)) }
                    val capture = awaitScreenshot(adapter, diagnostics, "marker.${fixture.id}.${family.name.lowercase()}")
                    capture.writeToTestStorage("visual/naver-marker/${fixture.id}.${family.name.lowercase()}")
                }
            }

            onMain {
                adapter.setTheme(MapThemeSpec(preferredBrightness = MapBrightnessFamily.LIGHT, markerFamily = MarkerFamily.DAY))
                adapter.setCamera(fixtures[1].center, 16.0)
                adapter.setMarkers(emptyList())
            }
            val baseMap = awaitBaseMapEvidence(
                storageName = "visual/naver-base/e2.dense-urban.light",
                adapter = adapter,
                diagnostics = diagnostics,
            )
            val measuredLuminance = centerLuminance(baseMap)
            assertTrue(measuredLuminance in 0f..1f)
            val e2 = EveningVisualInterpolator.profile(.5f, measuredLuminance)
            onMain {
                adapter.setTheme(
                    MapThemeSpec(
                        preferredBrightness = if (e2.markerFamily == MarkerFamily.DARK) MapBrightnessFamily.DARK else MapBrightnessFamily.LIGHT,
                        markerFamily = e2.markerFamily,
                        routeColor = e2.route,
                    ),
                )
                adapter.setMarkers(markerScene(fixtures[1].center))
            }
            awaitScreenshot(adapter, diagnostics, "marker.e2.${e2.markerFamily.name.lowercase()}")
                .writeToTestStorage("visual/naver-marker/e2.dense-urban.${e2.markerFamily.name.lowercase()}")
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

    private fun offset(point: GeoPoint, lat: Double, lon: Double) = GeoPoint(point.latitude + lat, point.longitude + lon)

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
                    "NAVER map failed visual-QA authentication: ${health.errorCode}",
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
                "NAVER authentication failed after READY: ${health.errorCode}",
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
            sum += .2126 * linear((color shr 16) and 0xFF) + .7152 * linear((color shr 8) and 0xFF) + .0722 * linear(color and 0xFF)
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
