package com.dailytown.app.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Pure provider-neutral geographic distance contract used outside exploration gameplay. */
fun interface GeoDistance {
    fun meters(a: GeoPoint, b: GeoPoint): Double
}

/** Haversine distance over the WGS84-style latitude/longitude points used by Daily Town. */
object HaversineGeoDistance : GeoDistance {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    override fun meters(a: GeoPoint, b: GeoPoint): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return EARTH_RADIUS_METERS * 2 * atan2(sqrt(h), sqrt(1 - h))
    }
}
