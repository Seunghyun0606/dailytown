package com.dailytown.app.visualqa

import android.graphics.Bitmap
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
        val adapter = NaverMapAdapter(BuildConfig.NAVER_MAP_NCP_KEY_ID, CandidateMarkerVisualSource(catalog))
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            scenario.onActivity { activity ->
                activity.setContentView(adapter.createView(activity))
                adapter.onStart()
                adapter.onResume()
            }
            waitForReady(adapter)

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
                        adapter.setMarkers(markerScene(fixture.center))
                    }
                    val capture = awaitNonFlatScreenshot()
                    capture.writeToTestStorage("visual/naver-marker/${fixture.id}.${family.name.lowercase()}")
                }
            }

            onMain {
                adapter.setTheme(MapThemeSpec(preferredBrightness = MapBrightnessFamily.LIGHT, markerFamily = MarkerFamily.DAY))
                adapter.setCamera(fixtures[1].center, 16.0)
                adapter.setMarkers(emptyList())
            }
            val baseMap = awaitNonFlatScreenshot()
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
            awaitNonFlatScreenshot().writeToTestStorage("visual/naver-marker/e2.dense-urban.${e2.markerFamily.name.lowercase()}")
        } finally {
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

    private fun waitForReady(adapter: NaverMapAdapter) {
        repeat(40) {
            when (adapter.health.value.status) {
                MapHealthStatus.READY -> return
                MapHealthStatus.AUTH_ERROR, MapHealthStatus.ERROR -> error("NAVER map failed visual-QA initialization: ${adapter.health.value.status}/${adapter.health.value.errorCode}")
                else -> Thread.sleep(250)
            }
        }
        error("NAVER map did not reach READY during visual QA")
    }

    private fun awaitNonFlatScreenshot(): Bitmap {
        var latest: Bitmap? = null
        repeat(12) {
            Thread.sleep(350)
            latest = instrumentation.uiAutomation.takeScreenshot()
            latest?.let { bitmap -> if (sampleVariance(bitmap) > 30.0) return bitmap }
        }
        val result = latest ?: error("Device screenshot unavailable")
        assertTrue("NAVER map capture is visually flat/blank", sampleVariance(result) > 30.0)
        return result
    }

    private fun sampleVariance(bitmap: Bitmap): Double {
        val values = mutableListOf<Double>()
        val stepX = (bitmap.width / 20).coerceAtLeast(1)
        val stepY = (bitmap.height / 20).coerceAtLeast(1)
        for (y in 0 until bitmap.height step stepY) for (x in 0 until bitmap.width step stepX) {
            val color = bitmap.getPixel(x, y)
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            values += (r + g + b) / 3.0
        }
        val mean = values.average()
        return values.map { (it - mean) * (it - mean) }.average()
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
}
