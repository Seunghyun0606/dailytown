package com.dailytown.app.map

import com.dailytown.app.domain.GeoPoint

interface MapProvider {
    fun cameraTarget(): GeoPoint?
    fun setCameraTarget(point: GeoPoint)
}

class InMemoryMapProvider : MapProvider {
    private var target: GeoPoint? = null
    override fun cameraTarget(): GeoPoint? = target
    override fun setCameraTarget(point: GeoPoint) { target = point }
}
