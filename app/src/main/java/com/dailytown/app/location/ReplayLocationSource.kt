package com.dailytown.app.location

import android.os.Handler
import android.os.Looper
import com.dailytown.app.domain.GeoPoint

class ReplayLocationSource(
    private val samples: List<LocationSample> = seoulCityHallReplayRoute(),
    private val intervalMillis: Long = 900L,
    private val handler: Handler = Handler(Looper.getMainLooper()),
) : LocationSource {
    override val name: String = "replay"

    private var index = 0
    private var running = false
    private var consumer: ((LocationSample) -> Unit)? = null

    private val tick = object : Runnable {
        override fun run() {
            if (!running || samples.isEmpty()) return
            consumer?.invoke(samples[index])
            if (index >= samples.lastIndex) {
                running = false
                return
            }
            index += 1
            handler.postDelayed(this, intervalMillis)
        }
    }

    override fun start(onLocation: (LocationSample) -> Unit, onError: (Throwable) -> Unit) {
        stop()
        if (samples.isEmpty()) {
            onError(IllegalStateException("Replay route is empty"))
            return
        }
        index = 0
        running = true
        consumer = onLocation
        handler.post(tick)
    }

    override fun stop() {
        running = false
        consumer = null
        handler.removeCallbacks(tick)
    }
}

fun seoulCityHallReplayRoute(): List<LocationSample> {
    val points = listOf(
        GeoPoint(37.56650, 126.97800),
        GeoPoint(37.56666, 126.97763),
        GeoPoint(37.56688, 126.97719),
        GeoPoint(37.56711, 126.97676),
        GeoPoint(37.56737, 126.97631),
        GeoPoint(37.56765, 126.97588),
        GeoPoint(37.56792, 126.97543),
    )
    return points.mapIndexed { index, point ->
        LocationSample(
            point = point,
            accuracyMeters = 8f,
            bearingDegrees = 300f,
            elapsedRealtimeMillis = index * 3_000L,
        )
    }
}
