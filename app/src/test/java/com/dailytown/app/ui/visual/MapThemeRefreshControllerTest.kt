package com.dailytown.app.ui.visual

import android.content.Context
import android.view.View
import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.map.MapBrightnessFamily
import com.dailytown.app.map.MapHealth
import com.dailytown.app.map.MapHealthStatus
import com.dailytown.app.map.MapMarkerSpec
import com.dailytown.app.map.MapProviderId
import com.dailytown.app.map.MapThemeSpec
import com.dailytown.app.map.MapViewAdapter
import com.dailytown.app.map.UserLocationSpec
import com.dailytown.app.visual.MapOverlaySemanticState
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapThemeRefreshControllerTest {
    @Test
    fun cadenceAlignsToExactNextMinute() {
        assertEquals(60_000L, MapThemeRefreshCadence.millisUntilNextMinute(LocalTime.of(12, 34)))
        assertEquals(47_655L, MapThemeRefreshCadence.millisUntilNextMinute(LocalTime.of(12, 34, 12, 345_000_000)))
        assertEquals(1L, MapThemeRefreshCadence.millisUntilNextMinute(LocalTime.of(12, 34, 59, 999_999_999)))
    }

    @Test
    fun startAppliesThemeImmediatelyAndSchedulesOnlyOneTick() {
        val adapter = RecordingMapAdapter()
        val scheduler = RecordingScheduler()
        var now = LocalTime.of(19, 0, 30, 500_000_000)
        val controller = MapThemeRefreshController(
            mapAdapter = adapter,
            clock = { now },
            scheduler = scheduler,
        )

        controller.start()
        controller.start()

        assertEquals(1, adapter.themes.size)
        assertEquals(1, scheduler.scheduleCount)
        assertEquals(29_500L, scheduler.delayMillis)
        assertEquals(0, adapter.markerCalls)
        assertEquals(0, adapter.overlayCalls)
        assertEquals(0, adapter.locationCalls)

        now = LocalTime.of(22, 0)
        scheduler.runScheduled()

        assertEquals(2, adapter.themes.size)
        assertEquals(MapBrightnessFamily.DARK, adapter.themes.last().preferredBrightness)
        assertEquals(2, scheduler.scheduleCount)
        assertEquals(60_000L, scheduler.delayMillis)
    }

    @Test
    fun stopCancelsPendingTickAndLateCallbackCannotMutateTheme() {
        val adapter = RecordingMapAdapter()
        val scheduler = RecordingScheduler()
        var now = LocalTime.of(8, 15)
        val controller = MapThemeRefreshController(
            mapAdapter = adapter,
            clock = { now },
            scheduler = scheduler,
        )

        controller.start()
        val pending = scheduler.task
        controller.stop()

        assertEquals(1, scheduler.cancelCount)
        assertNull(scheduler.task)

        now = LocalTime.of(22, 0)
        pending?.run()

        assertEquals(1, adapter.themes.size)
        assertEquals(MapBrightnessFamily.LIGHT, adapter.themes.single().preferredBrightness)
        assertEquals(1, scheduler.scheduleCount)
    }

    private class RecordingScheduler : MapThemeRefreshScheduler {
        var task: Runnable? = null
        var delayMillis: Long? = null
        var scheduleCount = 0
        var cancelCount = 0

        override fun schedule(task: Runnable, delayMillis: Long) {
            this.task = task
            this.delayMillis = delayMillis
            scheduleCount += 1
        }

        override fun cancel(task: Runnable) {
            cancelCount += 1
            if (this.task === task) this.task = null
        }

        fun runScheduled() {
            val pending = task ?: error("No scheduled theme refresh")
            task = null
            pending.run()
        }
    }

    private class RecordingMapAdapter : MapViewAdapter {
        override val providerId: MapProviderId = MapProviderId.NAVER
        private val mutableHealth = MutableStateFlow(MapHealth(MapHealthStatus.READY))
        override val health: StateFlow<MapHealth> = mutableHealth
        val themes = mutableListOf<MapThemeSpec>()
        var markerCalls = 0
        var overlayCalls = 0
        var locationCalls = 0

        override fun createView(context: Context): View = error("Not used in JVM refresh test")
        override fun setCamera(target: GeoPoint, zoom: Double) = Unit
        override fun setTheme(theme: MapThemeSpec) {
            themes += theme
        }
        override fun setMarkers(markers: List<MapMarkerSpec>) {
            markerCalls += 1
        }
        override fun setOverlayState(state: MapOverlaySemanticState) {
            overlayCalls += 1
        }
        override fun setUserLocation(location: UserLocationSpec?) {
            locationCalls += 1
        }
    }
}
