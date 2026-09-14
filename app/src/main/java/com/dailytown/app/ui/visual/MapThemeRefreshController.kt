package com.dailytown.app.ui.visual

import android.os.Handler
import android.os.Looper
import com.dailytown.app.map.MapViewAdapter
import java.time.LocalTime

/**
 * Schedules map-theme reevaluation only while the owning Activity is resumed.
 *
 * DayPhaseResolver currently consumes clock minutes, including EV-1 evening interpolation, so the
 * next useful automatic refresh is the next exact minute boundary. This controller changes only
 * provider-neutral map theme state; marker, overlay, camera, and location semantics stay untouched.
 */
internal class MapThemeRefreshController(
    private val mapAdapter: MapViewAdapter,
    private val themeResolver: MapRuntimeThemeResolver = MapRuntimeThemeResolver(),
    private val clock: () -> LocalTime = LocalTime::now,
    private val scheduler: MapThemeRefreshScheduler = MainThreadMapThemeRefreshScheduler(),
) {
    private var started = false

    private val tick = object : Runnable {
        override fun run() {
            if (!started) return
            val now = clock()
            mapAdapter.setTheme(themeResolver.resolve(now).mapTheme)
            scheduler.schedule(this, MapThemeRefreshCadence.millisUntilNextMinute(now))
        }
    }

    fun start() {
        if (started) return
        started = true
        tick.run()
    }

    fun stop() {
        if (!started) return
        started = false
        scheduler.cancel(tick)
    }

    fun close() = stop()
}

internal fun interface MapThemeRefreshScheduler {
    fun schedule(task: Runnable, delayMillis: Long)

    fun cancel(task: Runnable) = Unit
}

private class MainThreadMapThemeRefreshScheduler(
    private val handler: Handler = Handler(Looper.getMainLooper()),
) : MapThemeRefreshScheduler {
    override fun schedule(task: Runnable, delayMillis: Long) {
        handler.postDelayed(task, delayMillis)
    }

    override fun cancel(task: Runnable) {
        handler.removeCallbacks(task)
    }
}

internal object MapThemeRefreshCadence {
    private const val MINUTE_MILLIS = 60_000L

    fun millisUntilNextMinute(time: LocalTime): Long {
        val elapsedMillis = time.second * 1_000L + time.nano / 1_000_000L
        return (MINUTE_MILLIS - elapsedMillis).coerceIn(1L, MINUTE_MILLIS)
    }
}
