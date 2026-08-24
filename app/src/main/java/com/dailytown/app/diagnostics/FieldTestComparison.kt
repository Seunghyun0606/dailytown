package com.dailytown.app.diagnostics

import kotlin.math.roundToInt

enum class FieldTestAreaProfile {
    NEW_AREA,
    REPEAT_AREA,
}

/**
 * Derived, privacy-safe snapshot of one field-test session.
 *
 * The summary intentionally contains no coordinates, route geometry, POI/encounter/template IDs,
 * free-form place labels, or raw event payloads. It is suitable for comparing multiple sessions
 * without creating a new raw-location/event retention boundary.
 */
data class FieldTestSessionSummary(
    val areaProfile: FieldTestAreaProfile,
    val trackingPreset: String,
    val mapHealthStatus: String?,
    val sessionDurationSeconds: Int?,
    val sessionDistanceMeters: Int?,
    val gpsRejectionRatePercent: Int?,
    val distanceErrorPercent: Int?,
    val batteryDrainPercentPerHour: Int?,
    val discoveredEncountersPerSession: Int?,
    val encounterResolutionRatePercent: Int?,
    val revisitSharePercent: Int?,
    val repeatAreaFatigueProxyPercent: Int?,
    val acceptanceOverall: String,
) {
    companion object {
        fun fromDiagnostic(
            areaProfile: FieldTestAreaProfile,
            diagnostic: FieldTestDiagnostic,
        ): FieldTestSessionSummary = FieldTestSessionSummary(
            areaProfile = areaProfile,
            trackingPreset = diagnostic.trackingPreset,
            mapHealthStatus = diagnostic.mapHealthStatus,
            sessionDurationSeconds = diagnostic.trackingDurationSeconds,
            sessionDistanceMeters = diagnostic.sessionDistanceMeters,
            gpsRejectionRatePercent = diagnostic.rejectedLocationRatePercent,
            distanceErrorPercent = diagnostic.distanceErrorPercent,
            batteryDrainPercentPerHour = diagnostic.batteryDrainPercentPerHour,
            discoveredEncountersPerSession = diagnostic.sessionEncounterDiscoveredCount,
            encounterResolutionRatePercent = diagnostic.sessionEncounterResolutionRatePercent,
            revisitSharePercent = diagnostic.sessionRevisitSharePercent,
            repeatAreaFatigueProxyPercent = diagnostic.repeatAreaFatigueProxyPercent,
            acceptanceOverall = diagnostic.acceptanceOverall,
        )
    }
}

data class FieldTestMetricAverage(
    val average: Int?,
    val evidenceCount: Int,
    val sessionCount: Int,
)

data class FieldTestCohortSummary(
    val areaProfile: FieldTestAreaProfile,
    val sessionCount: Int,
    val trackingPresets: Set<String>,
    val sessionDurationSeconds: FieldTestMetricAverage,
    val sessionDistanceMeters: FieldTestMetricAverage,
    val gpsRejectionRatePercent: FieldTestMetricAverage,
    val distanceErrorPercent: FieldTestMetricAverage,
    val batteryDrainPercentPerHour: FieldTestMetricAverage,
    val discoveredEncountersPerSession: FieldTestMetricAverage,
    val encounterResolutionRatePercent: FieldTestMetricAverage,
    val revisitSharePercent: FieldTestMetricAverage,
    val repeatAreaFatigueProxyPercent: FieldTestMetricAverage,
    val acceptancePassCount: Int,
    val acceptanceFailCount: Int,
    val acceptanceNotEvaluatedCount: Int,
)

data class FieldTestComparisonDelta(
    val key: String,
    val repeatMinusNew: Int?,
)

data class FieldTestComparisonReport(
    val newArea: FieldTestCohortSummary,
    val repeatArea: FieldTestCohortSummary,
    val deltas: List<FieldTestComparisonDelta>,
) {
    fun render(): String = buildString {
        appendLine("Daily Town field-test comparison")
        appendLine("comparison=repeat_area_minus_new_area")
        appendCohort("newArea", newArea)
        appendCohort("repeatArea", repeatArea)
        deltas.forEach { delta ->
            delta.repeatMinusNew?.let { appendLine("delta.${delta.key}=$it") }
        }
        append("privacy=derived_session_summaries_only_no_raw_gps_no_event_ids_no_place_labels_no_credentials")
    }

    private fun StringBuilder.appendCohort(prefix: String, cohort: FieldTestCohortSummary) {
        appendLine("$prefix.sessionCount=${cohort.sessionCount}")
        if (cohort.trackingPresets.isNotEmpty()) {
            appendLine("$prefix.trackingPresets=${cohort.trackingPresets.sorted().joinToString(",")}")
        }
        appendMetric(prefix, "sessionDurationSeconds", cohort.sessionDurationSeconds)
        appendMetric(prefix, "sessionDistanceMeters", cohort.sessionDistanceMeters)
        appendMetric(prefix, "gpsRejectionRatePercent", cohort.gpsRejectionRatePercent)
        appendMetric(prefix, "distanceErrorPercent", cohort.distanceErrorPercent)
        appendMetric(prefix, "batteryDrainPercentPerHour", cohort.batteryDrainPercentPerHour)
        appendMetric(prefix, "discoveredEncountersPerSession", cohort.discoveredEncountersPerSession)
        appendMetric(prefix, "encounterResolutionRatePercent", cohort.encounterResolutionRatePercent)
        appendMetric(prefix, "revisitSharePercent", cohort.revisitSharePercent)
        appendMetric(prefix, "repeatAreaFatigueProxyPercent", cohort.repeatAreaFatigueProxyPercent)
        appendLine("$prefix.acceptancePassCount=${cohort.acceptancePassCount}")
        appendLine("$prefix.acceptanceFailCount=${cohort.acceptanceFailCount}")
        appendLine("$prefix.acceptanceNotEvaluatedCount=${cohort.acceptanceNotEvaluatedCount}")
    }

    private fun StringBuilder.appendMetric(
        prefix: String,
        key: String,
        metric: FieldTestMetricAverage,
    ) {
        metric.average?.let { appendLine("$prefix.$key.average=$it") }
        appendLine("$prefix.$key.evidence=${metric.evidenceCount}/${metric.sessionCount}")
    }
}

/**
 * In-memory recorder for closed field testing. The bounded list prevents accidental unbounded
 * retention; summaries disappear with the app process and contain only derived metrics.
 */
class FieldTestComparisonRecorder(
    private val maximumSessions: Int = 20,
) {
    init {
        require(maximumSessions > 0) { "maximumSessions must be positive" }
    }

    private val sessions = ArrayDeque<FieldTestSessionSummary>()

    fun record(summary: FieldTestSessionSummary) {
        while (sessions.size >= maximumSessions) sessions.removeFirst()
        sessions.addLast(summary)
    }

    fun record(
        areaProfile: FieldTestAreaProfile,
        diagnostic: FieldTestDiagnostic,
    ) {
        record(FieldTestSessionSummary.fromDiagnostic(areaProfile, diagnostic))
    }

    fun reset() {
        sessions.clear()
    }

    fun sessionCount(): Int = sessions.size

    fun report(): FieldTestComparisonReport {
        val snapshot = sessions.toList()
        val newArea = aggregate(FieldTestAreaProfile.NEW_AREA, snapshot)
        val repeatArea = aggregate(FieldTestAreaProfile.REPEAT_AREA, snapshot)
        return FieldTestComparisonReport(
            newArea = newArea,
            repeatArea = repeatArea,
            deltas = listOf(
                delta("sessionDurationSeconds", newArea.sessionDurationSeconds, repeatArea.sessionDurationSeconds),
                delta("sessionDistanceMeters", newArea.sessionDistanceMeters, repeatArea.sessionDistanceMeters),
                delta("gpsRejectionRatePercent", newArea.gpsRejectionRatePercent, repeatArea.gpsRejectionRatePercent),
                delta("distanceErrorPercent", newArea.distanceErrorPercent, repeatArea.distanceErrorPercent),
                delta("batteryDrainPercentPerHour", newArea.batteryDrainPercentPerHour, repeatArea.batteryDrainPercentPerHour),
                delta("discoveredEncountersPerSession", newArea.discoveredEncountersPerSession, repeatArea.discoveredEncountersPerSession),
                delta("encounterResolutionRatePercent", newArea.encounterResolutionRatePercent, repeatArea.encounterResolutionRatePercent),
                delta("revisitSharePercent", newArea.revisitSharePercent, repeatArea.revisitSharePercent),
                delta("repeatAreaFatigueProxyPercent", newArea.repeatAreaFatigueProxyPercent, repeatArea.repeatAreaFatigueProxyPercent),
            ),
        )
    }

    private fun aggregate(
        areaProfile: FieldTestAreaProfile,
        all: List<FieldTestSessionSummary>,
    ): FieldTestCohortSummary {
        val cohort = all.filter { it.areaProfile == areaProfile }
        return FieldTestCohortSummary(
            areaProfile = areaProfile,
            sessionCount = cohort.size,
            trackingPresets = cohort.map { it.trackingPreset }.filter { it.isNotBlank() }.toSet(),
            sessionDurationSeconds = average(cohort.map { it.sessionDurationSeconds }, cohort.size),
            sessionDistanceMeters = average(cohort.map { it.sessionDistanceMeters }, cohort.size),
            gpsRejectionRatePercent = average(cohort.map { it.gpsRejectionRatePercent }, cohort.size),
            distanceErrorPercent = average(cohort.map { it.distanceErrorPercent }, cohort.size),
            batteryDrainPercentPerHour = average(cohort.map { it.batteryDrainPercentPerHour }, cohort.size),
            discoveredEncountersPerSession = average(cohort.map { it.discoveredEncountersPerSession }, cohort.size),
            encounterResolutionRatePercent = average(cohort.map { it.encounterResolutionRatePercent }, cohort.size),
            revisitSharePercent = average(cohort.map { it.revisitSharePercent }, cohort.size),
            repeatAreaFatigueProxyPercent = average(cohort.map { it.repeatAreaFatigueProxyPercent }, cohort.size),
            acceptancePassCount = cohort.count { it.acceptanceOverall == AcceptanceCheckStatus.PASS.name },
            acceptanceFailCount = cohort.count { it.acceptanceOverall == AcceptanceCheckStatus.FAIL.name },
            acceptanceNotEvaluatedCount = cohort.count { it.acceptanceOverall == AcceptanceCheckStatus.NOT_EVALUATED.name },
        )
    }

    private fun average(values: List<Int?>, sessionCount: Int): FieldTestMetricAverage {
        val evidence = values.filterNotNull()
        return FieldTestMetricAverage(
            average = evidence.takeIf { it.isNotEmpty() }?.average()?.roundToInt(),
            evidenceCount = evidence.size,
            sessionCount = sessionCount,
        )
    }

    private fun delta(
        key: String,
        newArea: FieldTestMetricAverage,
        repeatArea: FieldTestMetricAverage,
    ): FieldTestComparisonDelta = FieldTestComparisonDelta(
        key = key,
        repeatMinusNew = if (newArea.average != null && repeatArea.average != null) {
            repeatArea.average - newArea.average
        } else {
            null
        },
    )
}
