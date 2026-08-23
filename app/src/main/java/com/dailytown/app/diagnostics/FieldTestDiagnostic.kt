package com.dailytown.app.diagnostics

import com.dailytown.app.BuildConfig
import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.persistence.ExplorationProgress
import java.time.Instant

data class FieldTestDiagnostic(
    val generatedAt: String,
    val appVersion: String,
    val packageId: String,
    val mapProvider: String,
    val mapCredentialConfigured: Boolean,
    val trackingPreset: String,
    val totalDistanceMeters: Int,
    val exploredPoiCount: Int,
    val resolvedEncounterCount: Int,
    val inventoryClueCount: Int,
    val companionBond: Int,
    val companionMemoryCount: Int,
    val acceptedLocationCount: Int,
    val rejectedLocationCount: Int,
    val rejectedLocationRatePercent: Int,
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
        appendLine("packageId=$packageId")
        appendLine("mapProvider=$mapProvider")
        appendLine("mapCredentialConfigured=$mapCredentialConfigured")
        appendLine("trackingPreset=$trackingPreset")
        appendLine("totalDistanceMeters=$totalDistanceMeters")
        appendLine("exploredPoiCount=$exploredPoiCount")
        appendLine("resolvedEncounterCount=$resolvedEncounterCount")
        appendLine("inventoryClueCount=$inventoryClueCount")
        appendLine("companionBond=$companionBond")
        appendLine("companionMemoryCount=$companionMemoryCount")
        appendLine("acceptedLocationCount=$acceptedLocationCount")
        appendLine("rejectedLocationCount=$rejectedLocationCount")
        appendLine("rejectedLocationRatePercent=$rejectedLocationRatePercent")
        appendLine("dailyDistanceMeters=$dailyDistanceMeters")
        appendLine("dailyDiscoveries=$dailyDiscoveries")
        appendLine("dailyResolutions=$dailyResolutions")
        appendLine("weeklyDistanceMeters=$weeklyDistanceMeters")
        appendLine("weeklyDiscoveries=$weeklyDiscoveries")
        appendLine("weeklyResolutions=$weeklyResolutions")
        append("privacy=derived_metrics_only_no_raw_gps_no_credentials")
    }
}

object FieldTestDiagnosticBuilder {
    fun build(
        progress: ExplorationProgress,
        acceptedLocationCount: Int,
        rejectedLocationCount: Int,
        appVersion: String,
        mapProvider: String,
        trackingPreset: LocationTrackingPreset,
        packageId: String = BuildConfig.APPLICATION_ID,
        mapCredentialConfigured: Boolean = BuildConfig.NAVER_MAP_CONFIGURED,
        generatedAt: Instant = Instant.now(),
    ): FieldTestDiagnostic {
        val sampleCount = acceptedLocationCount + rejectedLocationCount
        val rejectedRate = if (sampleCount == 0) 0
        else ((rejectedLocationCount * 100.0) / sampleCount).toInt()

        return FieldTestDiagnostic(
            generatedAt = generatedAt.toString(),
            appVersion = appVersion,
            packageId = packageId,
            mapProvider = mapProvider,
            mapCredentialConfigured = mapCredentialConfigured,
            trackingPreset = trackingPreset.name,
            totalDistanceMeters = progress.distanceWalkedMeters.toInt(),
            exploredPoiCount = progress.encounterVisitedPoiIds.size,
            resolvedEncounterCount = progress.resolvedEncounterIds.size,
            inventoryClueCount = progress.inventoryClueIds.size,
            companionBond = progress.companionBond,
            companionMemoryCount = progress.companionMemoryKeys.size,
            acceptedLocationCount = acceptedLocationCount,
            rejectedLocationCount = rejectedLocationCount,
            rejectedLocationRatePercent = rejectedRate,
            dailyDistanceMeters = progress.daily.distanceWalkedMeters.toInt(),
            dailyDiscoveries = progress.daily.discoveredPoiIds.size,
            dailyResolutions = progress.daily.resolvedEncounterIds.size,
            weeklyDistanceMeters = progress.weekly.distanceWalkedMeters.toInt(),
            weeklyDiscoveries = progress.weekly.discoveredPoiIds.size,
            weeklyResolutions = progress.weekly.resolvedEncounterIds.size,
        )
    }
}
