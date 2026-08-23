package com.dailytown.app.diagnostics

import com.dailytown.app.persistence.ExplorationProgress
import com.dailytown.app.location.LocationTrackingPreset
import java.time.Instant

data class FieldTestDiagnostic(
    val generatedAt: String,
    val appVersion: String,
    val mapProvider: String,
    val trackingPreset: String,
    val totalDistanceMeters: Int,
    val exploredPoiCount: Int,
    val resolvedEncounterCount: Int,
    val inventoryClueCount: Int,
    val companionBond: Int,
    val companionMemoryCount: Int,
    val rejectedLocationCount: Int,
    val dailyDistanceMeters: Int,
    val dailyDiscoveries: Int,
    val dailyResolutions: Int,
    val weeklyDistanceMeters: Int,
    val weeklyDiscoveries: Int,
    val weeklyResolutions: Int,
) {
    fun render(): String = buildString {
        appendLine("Daily Town field-test diagnostic")
        appendLine("generatedAt=$generatedAt")
        appendLine("appVersion=$appVersion")
        appendLine("mapProvider=$mapProvider")
        appendLine("trackingPreset=$trackingPreset")
        appendLine("totalDistanceMeters=$totalDistanceMeters")
        appendLine("exploredPoiCount=$exploredPoiCount")
        appendLine("resolvedEncounterCount=$resolvedEncounterCount")
        appendLine("inventoryClueCount=$inventoryClueCount")
        appendLine("companionBond=$companionBond")
        appendLine("companionMemoryCount=$companionMemoryCount")
        appendLine("rejectedLocationCount=$rejectedLocationCount")
        appendLine("dailyDistanceMeters=$dailyDistanceMeters")
        appendLine("dailyDiscoveries=$dailyDiscoveries")
        appendLine("dailyResolutions=$dailyResolutions")
        appendLine("weeklyDistanceMeters=$weeklyDistanceMeters")
        appendLine("weeklyDiscoveries=$weeklyDiscoveries")
        appendLine("weeklyResolutions=$weeklyResolutions")
        append("privacy=derived_metrics_only_no_raw_gps")
    }
}

object FieldTestDiagnosticBuilder {
    fun build(
        progress: ExplorationProgress,
        rejectedLocationCount: Int,
        appVersion: String,
        mapProvider: String,
        trackingPreset: LocationTrackingPreset,
        generatedAt: Instant = Instant.now(),
    ): FieldTestDiagnostic = FieldTestDiagnostic(
        generatedAt = generatedAt.toString(),
        appVersion = appVersion,
        mapProvider = mapProvider,
        trackingPreset = trackingPreset.name,
        totalDistanceMeters = progress.distanceWalkedMeters.toInt(),
        exploredPoiCount = progress.encounterVisitedPoiIds.size,
        resolvedEncounterCount = progress.resolvedEncounterIds.size,
        inventoryClueCount = progress.inventoryClueIds.size,
        companionBond = progress.companionBond,
        companionMemoryCount = progress.companionMemoryKeys.size,
        rejectedLocationCount = rejectedLocationCount,
        dailyDistanceMeters = progress.daily.distanceWalkedMeters.toInt(),
        dailyDiscoveries = progress.daily.discoveredPoiIds.size,
        dailyResolutions = progress.daily.resolvedEncounterIds.size,
        weeklyDistanceMeters = progress.weekly.distanceWalkedMeters.toInt(),
        weeklyDiscoveries = progress.weekly.discoveredPoiIds.size,
        weeklyResolutions = progress.weekly.resolvedEncounterIds.size,
    )
}
