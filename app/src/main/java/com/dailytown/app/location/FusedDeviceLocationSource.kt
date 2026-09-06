package com.dailytown.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.dailytown.app.domain.GeoPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

private const val MAX_PRE_REQUEST_LOCATION_AGE_MILLIS = 5_000L

/**
 * Fused Location may deliver cached/batched samples immediately after a new request starts.
 * Reject samples that substantially predate this tracking request so a new physical field-test
 * session cannot briefly jump to a stale location from an earlier run.
 */
internal fun isFreshDeviceLocation(
    locationElapsedRealtimeMillis: Long,
    requestStartedElapsedRealtimeMillis: Long,
    maxPreRequestAgeMillis: Long = MAX_PRE_REQUEST_LOCATION_AGE_MILLIS,
): Boolean =
    locationElapsedRealtimeMillis >= requestStartedElapsedRealtimeMillis - maxPreRequestAgeMillis

class FusedDeviceLocationSource(
    private val context: Context,
    private val config: LocationTrackingConfig = LocationTrackingPreset.BALANCED.config,
    client: FusedLocationProviderClient? = null,
) : LocationSource {
    override val name: String = "device"

    // Keep Google Play Services lazy so replay-only/emulator paths do not initialize
    // fused location until the user explicitly starts real device tracking.
    private val client: FusedLocationProviderClient by lazy(LazyThreadSafetyMode.NONE) {
        client ?: LocationServices.getFusedLocationProviderClient(context)
    }

    private var callback: LocationCallback? = null

    override fun start(onLocation: (LocationSample) -> Unit, onError: (Throwable) -> Unit) {
        stop()
        if (!hasLocationPermission()) {
            onError(SecurityException("Location permission is required"))
            return
        }

        val requestStartedElapsedRealtimeMillis = SystemClock.elapsedRealtime()
        val request = LocationRequest.Builder(priority(config.priorityMode), config.intervalMillis)
            .setMinUpdateDistanceMeters(config.minUpdateDistanceMeters)
            .setMinUpdateIntervalMillis(config.minUpdateIntervalMillis)
            .build()

        val newCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    val elapsedRealtimeMillis = location.elapsedRealtimeNanos / 1_000_000L
                    if (!isFreshDeviceLocation(
                            locationElapsedRealtimeMillis = elapsedRealtimeMillis,
                            requestStartedElapsedRealtimeMillis = requestStartedElapsedRealtimeMillis,
                        )
                    ) {
                        return@forEach
                    }

                    onLocation(
                        LocationSample(
                            point = GeoPoint(location.latitude, location.longitude),
                            accuracyMeters = location.accuracy,
                            bearingDegrees = if (location.hasBearing()) location.bearing else null,
                            elapsedRealtimeMillis = elapsedRealtimeMillis,
                        ),
                    )
                }
            }
        }
        callback = newCallback

        try {
            requestUpdates(request, newCallback, onError)
        } catch (security: SecurityException) {
            callback = null
            onError(security)
        }
    }

    override fun stop() {
        val activeCallback = callback ?: return
        callback = null
        client.removeLocationUpdates(activeCallback)
    }

    // start() performs the runtime fine/coarse permission check immediately before
    // entering this helper. Keep the lint suppression scoped only to the privileged call.
    @SuppressLint("MissingPermission")
    private fun requestUpdates(
        request: LocationRequest,
        callback: LocationCallback,
        onError: (Throwable) -> Unit,
    ) {
        client.requestLocationUpdates(request, callback, context.mainLooper)
            .addOnFailureListener(onError)
    }

    private fun priority(mode: LocationPriorityMode): Int = when (mode) {
        LocationPriorityMode.LOW_POWER -> Priority.PRIORITY_LOW_POWER
        LocationPriorityMode.BALANCED -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        LocationPriorityMode.HIGH_ACCURACY -> Priority.PRIORITY_HIGH_ACCURACY
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
