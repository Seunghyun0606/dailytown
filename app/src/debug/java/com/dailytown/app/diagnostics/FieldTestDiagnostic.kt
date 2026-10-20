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
    val sessionDistanceMeters: Int?,
    val referenceDistanceMeters: Int?,
    val distanceErrorPercent: Int?,
    val batteryMeasurementStatus: String?,
    val batteryStartPercent: Int?,
    val batteryEndPercent: Int?,
    val batteryDrainPercentPoints: Int?,
    val batteryDrainPercentPerHour: Int?,
    val batteryChargeConsumedMah: Int?,
    val sessionEncounterOfferedCount: Int?,
    val sessionEncounterHintedCount: Int?,
    val sessionEncounterDiscoveredCount: Int?,
    val sessionEncounterResolvedCount: Int?,
    val sessionCluesCollectedCount: Int?,
    val sessionRevisitEncounterCount: Int?,
    val sessionEncounterDiscoveryRatePercent: Int?,
    val sessionEncounterResolutionRatePercent: Int?,
    val sessionRevisitSharePercent: Int?,
    val sessionRevisitResolutionRatePercent: Int?,
    val repeatAreaFatigueProxyPercent: Int?,
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
        sessionDistanceMeters?.let { appendLine("sessionDistanceMeters=$it") }
        referenceDistanceMeters?.let { appendLine("referenceDistanceMeters=$it") }
        distanceErrorPercent?.let { appendLine("distanceErrorPercent=$it") }
        batteryMeasurementStatus?.let { appendLine("batteryMeasurementStatus=$it") }
        batteryStartPercent?.let { appendLine("batteryStartPercent=$it") }
        batteryEndPercent?.let { appendLine("batteryEndPercent=$it") }
        batteryDrainPercentPoints?.let { appendLine("batteryDrainPercentPoints=$it") }
        batteryDrainPercentPerHour?.let { appendLine("batteryDrainPercentPerHour=$it") }
        batteryChargeConsumedMah?.let { appendLine("batteryChargeConsumedMah=$it") }
        sessionEncounterOfferedCount?.let { appendLine("sessionEncounterOfferedCount=$it") }
        sessionEncounterHintedCount?.let { appendLine("sessionEncounterHintedCount=$it") }
        sessionEncounterDiscoveredCount?.let { appendLine("sessionEncounterDiscoveredCount=$it") }
        sessionEncounterResolvedCount?.let { appendLine("sessionEncounterResolvedCount=$it") }
        sessionCluesCollectedCount?.let { appendLine("sessionCluesCollectedCount=$it") }
        sessionRevisitEncounterCount?.let { appendLine("sessionRevisitEncounterCount=$it") }
        sessionEncounterDiscoveryRatePercent?.let { appendLine("sessionEncounterDiscoveryRatePercent=$it") }
        sessionEncounterResolutionRatePercent?.let { appendLine("sessionEncounterResolutionRatePercent=$it") }
        sessionRevisitSharePercent?.let { appendLine("sessionRevisitSharePercent=$it") }
        sessionRevisitResolutionRatePercent?.let { appendLine("sessionRevisitResolutionRatePercent=$it") }
        repeatAreaFatigueProxyPercent?.let { appendLine("repeatAreaFatigueProxyPercent=$it") }
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
        append("privacy=derived_metrics_only_no_raw_gps_no_event_ids_no_credentials")
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
        sessionMetrics: FieldTestSessionMetrics? = null,
        gameplayMetrics: GameplaySessionMetrics? = null,
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
                distanceErrorPercent = sessionMetrics?.distanceErrorPercent,
                batteryDrainPercentPerHour = sessionMetrics?.batteryDrainPercentPerHour,
                discoveredEncountersPerSession = gameplayMetrics?.discoveredEncounterCount,
                encounterResolutionRatePercent = gameplayMetrics?.encounterResolutionRatePercent,
                repeatAreaFatigueProxyPercent = gameplayMetrics?.repeatAreaFatigueProxyPercent,
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
            sessionDistanceMeters = sessionMetrics?.sessionDistanceMeters,
            referenceDistanceMeters = sessionMetrics?.referenceDistanceMeters,
            distanceErrorPercent = sessionMetrics?.distanceErrorPercent,
            batteryMeasurementStatus = sessionMetrics?.batteryMeasurementStatus?.name,
            batteryStartPercent = sessionMetrics?.batteryStartPercent,
            batteryEndPercent = sessionMetrics?.batteryEndPercent,
            batteryDrainPercentPoints = sessionMetrics?.batteryDrainPercentPoints,
            batteryDrainPercentPerHour = sessionMetrics?.batteryDrainPercentPerHour,
            batteryChargeConsumedMah = sessionMetrics?.batteryChargeConsumedMah,
            sessionEncounterOfferedCount = gameplayMetrics?.encounterOfferedCount,
            sessionEncounterHintedCount = gameplayMetrics?.hintedEncounterCount,
            sessionEncounterDiscoveredCount = gameplayMetrics?.discoveredEncounterCount,
            sessionEncounterResolvedCount = gameplayMetrics?.resolvedEncounterCount,
            sessionCluesCollectedCount = gameplayMetrics?.cluesCollectedCount,
            sessionRevisitEncounterCount = gameplayMetrics?.revisitOfferedCount,
            sessionEncounterDiscoveryRatePercent = gameplayMetrics?.encounterDiscoveryRatePercent,
            sessionEncounterResolutionRatePercent = gameplayMetrics?.encounterResolutionRatePercent,
            sessionRevisitSharePercent = gameplayMetrics?.revisitSharePercent,
            sessionRevisitResolutionRatePercent = gameplayMetrics?.revisitResolutionRatePercent,
            repeatAreaFatigueProxyPercent = gameplayMetrics?.repeatAreaFatigueProxyPercent,
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
            maximumDistanceErrorPercent = BuildConfig.FIELD_TEST_MAX_DISTANCE_ERROR_PERCENT
                .takeIf { it >= 0 },
            maximumBatteryDrainPercentPerHour = BuildConfig.FIELD_TEST_MAX_BATTERY_DRAIN_PERCENT_PER_HOUR
                .takeIf { it >= 0 },
            minimumDiscoveredEncountersPerSession = BuildConfig.FIELD_TEST_MIN_ENCOUNTERS_PER_SESSION
                .takeIf { it >= 0 },
            minimumEncounterResolutionRatePercent = BuildConfig.FIELD_TEST_MIN_ENCOUNTER_RESOLUTION_PERCENT
                .takeIf { it >= 0 },
            maximumRepeatAreaFatiguePercent = BuildConfig.FIELD_TEST_MAX_REPEAT_AREA_FATIGUE_PERCENT
                .takeIf { it >= 0 },
        )
}
