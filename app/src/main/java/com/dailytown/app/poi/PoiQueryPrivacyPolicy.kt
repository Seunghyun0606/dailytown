package com.dailytown.app.poi

import com.dailytown.app.domain.GeoPoint
import kotlin.math.round

/**
 * Shared outbound-location minimization for POI providers.
 *
 * Exact gameplay coordinates remain on-device. Network clients receive a neighborhood-scale origin
 * and a padded radius; the on-device cache/repository layer filters provider results back to the
 * precise requested radius.
 */
internal object PoiQueryPrivacyPolicy {
    private const val QUERY_GRID_DEGREES = 0.02

    fun snapCenter(center: GeoPoint): GeoPoint = GeoPoint(
        latitude = snap(center.latitude),
        longitude = snap(center.longitude),
    )

    fun paddedRadiusMeters(
        requestedRadiusMeters: Double,
        paddingMeters: Double,
        maximumRadiusMeters: Int,
    ): Int {
        require(requestedRadiusMeters > 0.0) { "radiusMeters must be positive" }
        require(maximumRadiusMeters > 0) { "maximumRadiusMeters must be positive" }
        return (requestedRadiusMeters + paddingMeters.coerceAtLeast(0.0))
            .toInt()
            .coerceIn(1, maximumRadiusMeters)
    }

    private fun snap(value: Double): Double =
        round(value / QUERY_GRID_DEGREES) * QUERY_GRID_DEGREES
}
