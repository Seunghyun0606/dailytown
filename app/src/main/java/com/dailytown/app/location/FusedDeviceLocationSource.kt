package com.dailytown.app.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.dailytown.app.domain.GeoPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class FusedDeviceLocationSource(
    private val context: Context,
    private val config: LocationTrackingConfig = LocationTrackingPreset.BALANCED.config,
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context),
) : LocationSource {
    override val name: String = "device"

    private var callback: LocationCallback? = null

    override fun start(onLocation: (LocationSample) -> Unit, onError: (Throwable) -> Unit) {
        stop()
        if (!hasLocationPermission()) {
            onError(SecurityException("Location permission is required"))
            return
        }

        val request = LocationRequest.Builder(priority(config.priorityMode), config.intervalMillis)
            .setMinUpdateDistanceMeters(config.minUpdateDistanceMeters)
            .setMinUpdateIntervalMillis(config.minUpdateIntervalMillis)
            .build()

        val newCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    onLocation(
                        LocationSample(
                            point = GeoPoint(location.latitude, location.longitude),
                            accuracyMeters = location.accuracy,
                            bearingDegrees = if (location.hasBearing()) location.bearing else null,
                            elapsedRealtimeMillis = location.elapsedRealtimeNanos / 1_000_000L,
                        ),
                    )
                }
            }
        }
        callback = newCallback

        try {
            client.requestLocationUpdates(request, newCallback, context.mainLooper)
                .addOnFailureListener(onError)
        } catch (security: SecurityException) {
            callback = null
            onError(security)
        }
    }

    override fun stop() {
        callback?.let(client::removeLocationUpdates)
        callback = null
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
