package com.dailytown.app.diagnostics

import com.dailytown.app.BuildConfig
import com.dailytown.app.location.LocationTrackingPreset
import com.dailytown.app.map.MapHealth
import com.dailytown.app.map.MapHealthStatus
import com.dailytown.app.persistence.ExplorationProgress
import java.time.Instant

data class FieldTestDiagnostic(
    val generatedAt: String,
    val appVersion: String,
    val packageId: String,
    val mapProvider: String,
    val mapCredentialConfigured: Boolean,
    val mapHealthStatus: String?,
    val mapHealthErrorCode: String?,
    val trackingPreset: String,
    val trackingDurationSeconds: Int?,
    val totalDistanceMeters: Int,
    val exploredPoiCount: Int,
    val resolvedEncounterCount: Int,
    val inventoryClueCount: Int,
    val companionBond: Int,
    val companionMemoryCount: Int,
    val acceptedLocationCount: Int?,
    val rejectedLocationCount: Int,
    val rejectedLocationRatePercent: Int?,
    val dailyDistanceMeters: Int,
    val dailyDiscoveries: Int,
    val dailyResolutions: Int,
    val weeklyDistanceMeters: Int,
    val weeklyDiscoveries: Int,
    val weeklyResolutions: Int,
    val acceptanceConfigured: Boolean,
    val acceptanceOverall: String,
    val acceptanceFailedKeys: List<String>,
) {
    fun render(): String = buildString {
        appendLine("Daily Town field-test diagnostic")
        appendLine("generatedAt=$generatedAt")
        appendLine("appVersion=$appVersion")
        appendLine("packageId=$packageId")
        appendLine("mapProvider=$mapProvider")
        appendLine("mapCredentialConfigured=$mapCredentialConfigured")
        mapHealthStatus?.let { appendLine("mapHealthStatus=$it") }
        mapHealthErrorCode?.let { appendLine("mapHealthErrorCode=$it") }
        appendLine("trackingPreset=$trackingPreset")
        trackingDurationSeconds?.let { appendLine("trackingDurationSeconds=$it") }
        appendLine("totalDistanceMeters=$totalDistanceMeters")
        appendLine("exploredPoiCount=$exploredPoiCount")
        appendLine("resolvedEncounterCount=$resolvedEncounterCount")
        appendLine("inventoryClueCount=$inventoryClueCount")
        appendLine("companionBond=$companionBond")
        appendLine("companionMemoryCount=$companionMemoryCount")
        acceptedLocationCount?.let { appendLine("acceptedLocationCount=$it") }
        appendLine("rejectedLocationCount=$rejectedLocationCount")
        rejectedLocationRatePercent?.let { appendLine("rejectedLocationRatePercent=$it") }
        appendLine("dailyDistanceMeters=$dailyDistanceMeters")
        appendLine("dailyDiscoveries=$dailyDiscoveries")
        appendLine("dailyResolutions=$dailyResolutions")
        appendLine("weeklyDistanceMeters=$weeklyDistanceMeters")
        appendLine("weeklyDiscoveries=$weeklyDiscoveries")
        appendLine("weeklyResolutions=$weeklyResolutions")
        appendLine("acceptanceConfigured=$acceptanceConfigured")
        appendLine("acceptanceOverall=$acceptanceOverall")
        if (acceptanceFailedKeys.isNotEmpty()) {
            appendLine("acceptanceFailedKeys=${acceptanceFailedKeys.joinToString(",")}")
        }
        append("privacy=derived_metrics_only_no_raw_gps_no_credentials")
    }
}

object FieldTestDiagnosticBuilder {
    private val acceptanceEvaluator = FieldTestAcceptanceEvaluator()

    fun build(
        progress: ExplorationProgress,
        rejectedLocationCount: Int,
        appVersion: String,
        mapProvider: String,
        trackingPreset: LocationTrackingPreset,
        acceptedLocationCount: Int? = null,
        trackingDurationSeconds: Int? = null,
        mapHealth: MapHealth? = null,
        packageId: String = BuildConfig.APPLICATION_ID,
        mapCredentialConfigured: Boolean = BuildConfig.NAVER_MAP_CONFIGURED,
        acceptanceCriteria: FieldTestAcceptanceCriteria = buildConfigAcceptanceCriteria(),
        generatedAt: Instant = Instant.now(),
    ): FieldTestDiagnostic {
        val rejectedRate = acceptedLocationCount?.let { accepted ->
            val sampleCount = accepted + rejectedLocationCount
            if (sampleCount == 0) 0 else ((rejectedLocationCount * 100.0) / sampleCount).toInt()
        }
        val acceptance = acceptanceEvaluator.evaluate(
            criteria = acceptanceCriteria,
            input = FieldTestAcceptanceInput(
                sessionDurationSeconds = trackingDurationSeconds?.toLong(),
                gpsRejectionRatePercent = rejectedRate,
                mapHealth = mapHealth?.status,
            ),
        )

        return FieldTestDiagnostic(
            generatedAt = generatedAt.toString(),
            appVersion = appVersion,
            packageId = packageId,
            mapProvider = mapProvider,
            mapCredentialConfigured = mapCredentialConfigured,
            mapHealthStatus = mapHealth?.status?.name,
            mapHealthErrorCode = mapHealth?.errorCode,
            trackingPreset = trackingPreset.name,
            trackingDurationSeconds = trackingDurationSeconds,
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
            acceptanceConfigured = acceptanceCriteria.isConfigured,
            acceptanceOverall = acceptance.overall.name,
            acceptanceFailedKeys = acceptance.failedKeys,
        )
    }

    private fun buildConfigAcceptanceCriteria(): FieldTestAcceptanceCriteria =
        FieldTestAcceptanceCriteria(
            minimumSessionDurationSeconds = BuildConfig.FIELD_TEST_MIN_SESSION_SECONDS
                .takeIf { it >= 0L },
            maximumGpsRejectionRatePercent = BuildConfig.FIELD_TEST_MAX_GPS_REJECTION_PERCENT
                .takeIf { it >= 0 },
            requiredMapHealth = MapHealthStatus.READY
                .takeIf { BuildConfig.FIELD_TEST_REQUIRE_MAP_READY },
        )
}
