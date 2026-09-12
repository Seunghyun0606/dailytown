package com.dailytown.app.diagnostics

const val FIELD_TEST_EXPORT_SCHEMA_NAME = "dailytown.field_test_export"
const val FIELD_TEST_EXPORT_SCHEMA_VERSION = 1

/**
 * Versioned, privacy-safe export of the currently buffered field-test summaries.
 *
 * The export deliberately omits timestamps, coordinates, route geometry, place labels, event IDs,
 * session tokens, device identifiers, provider exception payloads, and credentials. The app still
 * keeps no durable field-test history: this bundle exists only when the tester explicitly shares it.
 */
data class FieldTestExportBundle(
    val appVersion: String,
    val packageId: String,
    val sessions: List<FieldTestSessionSummary>,
    val comparison: FieldTestComparisonReport,
    val acceptanceCriteria: FieldTestAcceptanceCriteria,
    val protocolCriteria: FieldTestProtocolCriteria,
    val protocolAssessment: FieldTestProtocolAssessment,
) {
    init {
        require(sessions.size <= 20) { "field-test export must remain bounded to 20 sessions" }
    }

    fun renderJson(): String = buildString {
        append('{')
        append("\"schema\":")
        appendJsonString(FIELD_TEST_EXPORT_SCHEMA_NAME)
        append(",\"schemaVersion\":")
        append(FIELD_TEST_EXPORT_SCHEMA_VERSION)
        append(",\"app\":{")
        append("\"version\":")
        appendJsonString(appVersion)
        append(",\"packageId\":")
        appendJsonString(packageId)
        append('}')
        append(",\"privacy\":{")
        append("\"rawGps\":false,\"routeGeometry\":false,\"placeLabels\":false,")
        append("\"eventIdentifiers\":false,\"sessionLinkage\":false,\"deviceLinkage\":false,")
        append("\"credentials\":false,\"appPersistence\":false")
        append('}')
        append(",\"policies\":{")
        appendAcceptanceCriteria(acceptanceCriteria)
        append(',')
        appendProtocolCriteria(protocolCriteria)
        append('}')
        append(",\"protocol\":")
        appendProtocolAssessment(protocolAssessment)
        append(",\"sessions\":[")
        sessions.forEachIndexed { index, session ->
            if (index > 0) append(',')
            appendSession(index + 1, session)
        }
        append(']')
        append(",\"comparison\":")
        appendComparison(comparison)
        append('}')
    }

    private fun StringBuilder.appendAcceptanceCriteria(criteria: FieldTestAcceptanceCriteria) {
        append("\"acceptance\":{")
        append("\"minimumSessionDurationSeconds\":")
        appendNullableLong(criteria.minimumSessionDurationSeconds)
        append(",\"maximumGpsRejectionRatePercent\":")
        appendNullableInt(criteria.maximumGpsRejectionRatePercent)
        append(",\"requiredMapHealth\":")
        appendNullableString(criteria.requiredMapHealth?.name)
        append(",\"maximumDistanceErrorPercent\":")
        appendNullableInt(criteria.maximumDistanceErrorPercent)
        append(",\"maximumBatteryDrainPercentPerHour\":")
        appendNullableInt(criteria.maximumBatteryDrainPercentPerHour)
        append(",\"minimumDiscoveredEncountersPerSession\":")
        appendNullableInt(criteria.minimumDiscoveredEncountersPerSession)
        append(",\"minimumEncounterResolutionRatePercent\":")
        appendNullableInt(criteria.minimumEncounterResolutionRatePercent)
        append(",\"maximumRepeatAreaFatiguePercent\":")
        appendNullableInt(criteria.maximumRepeatAreaFatiguePercent)
        append('}')
    }

    private fun StringBuilder.appendProtocolCriteria(criteria: FieldTestProtocolCriteria) {
        append("\"comparison\":{")
        append("\"minimumSessionsPerCohort\":")
        appendNullableInt(criteria.minimumSessionsPerCohort)
        append(",\"requireMatchingTrackingPreset\":")
        appendNullableBoolean(criteria.requireMatchingTrackingPreset)
        append(",\"requiredEvidence\":[")
        criteria.requiredEvidence.sortedBy { it.name }.forEachIndexed { index, evidence ->
            if (index > 0) append(',')
            appendJsonString(evidence.name)
        }
        append("]}")
    }

    private fun StringBuilder.appendProtocolAssessment(assessment: FieldTestProtocolAssessment) {
        append('{')
        append("\"configured\":")
        append(assessment.configured)
        append(",\"status\":")
        appendJsonString(assessment.status.name)
        append(",\"issues\":[")
        assessment.issues.forEachIndexed { index, issue ->
            if (index > 0) append(',')
            append('{')
            append("\"key\":")
            appendJsonString(issue.key)
            append(",\"detail\":")
            appendJsonString(issue.detail)
            append('}')
        }
        append("]}")
    }

    private fun StringBuilder.appendSession(ordinal: Int, session: FieldTestSessionSummary) {
        append('{')
        append("\"ordinal\":")
        append(ordinal)
        append(",\"areaProfile\":")
        appendJsonString(session.areaProfile.name)
        append(",\"trackingPreset\":")
        appendJsonString(session.trackingPreset)
        append(",\"runReviewStatus\":")
        appendNullableString(session.runReviewStatus?.name)
        append(",\"mapHealthStatus\":")
        appendNullableString(session.mapHealthStatus)
        append(",\"metrics\":{")
        append("\"sessionDurationSeconds\":")
        appendNullableInt(session.sessionDurationSeconds)
        append(",\"sessionDistanceMeters\":")
        appendNullableInt(session.sessionDistanceMeters)
        append(",\"gpsRejectionRatePercent\":")
        appendNullableInt(session.gpsRejectionRatePercent)
        append(",\"distanceErrorPercent\":")
        appendNullableInt(session.distanceErrorPercent)
        append(",\"batteryDrainPercentPerHour\":")
        appendNullableInt(session.batteryDrainPercentPerHour)
        append(",\"discoveredEncountersPerSession\":")
        appendNullableInt(session.discoveredEncountersPerSession)
        append(",\"encounterResolutionRatePercent\":")
        appendNullableInt(session.encounterResolutionRatePercent)
        append(",\"revisitSharePercent\":")
        appendNullableInt(session.revisitSharePercent)
        append(",\"repeatAreaFatigueProxyPercent\":")
        appendNullableInt(session.repeatAreaFatigueProxyPercent)
        append('}')
        append(",\"acceptanceOverall\":")
        appendJsonString(session.acceptanceOverall)
        append('}')
    }

    private fun StringBuilder.appendComparison(report: FieldTestComparisonReport) {
        append('{')
        append("\"newArea\":")
        appendCohort(report.newArea)
        append(",\"repeatArea\":")
        appendCohort(report.repeatArea)
        append(",\"deltas\":[")
        report.deltas.forEachIndexed { index, delta ->
            if (index > 0) append(',')
            append('{')
            append("\"key\":")
            appendJsonString(delta.key)
            append(",\"repeatMinusNew\":")
            appendNullableInt(delta.repeatMinusNew)
            append('}')
        }
        append("]}")
    }

    private fun StringBuilder.appendCohort(cohort: FieldTestCohortSummary) {
        append('{')
        append("\"areaProfile\":")
        appendJsonString(cohort.areaProfile.name)
        append(",\"sessionCount\":")
        append(cohort.sessionCount)
        append(",\"trackingPresets\":[")
        cohort.trackingPresets.sorted().forEachIndexed { index, preset ->
            if (index > 0) append(',')
            appendJsonString(preset)
        }
        append(']')
        append(",\"metrics\":{")
        appendMetric("sessionDurationSeconds", cohort.sessionDurationSeconds)
        append(',')
        appendMetric("sessionDistanceMeters", cohort.sessionDistanceMeters)
        append(',')
        appendMetric("gpsRejectionRatePercent", cohort.gpsRejectionRatePercent)
        append(',')
        appendMetric("distanceErrorPercent", cohort.distanceErrorPercent)
        append(',')
        appendMetric("batteryDrainPercentPerHour", cohort.batteryDrainPercentPerHour)
        append(',')
        appendMetric("discoveredEncountersPerSession", cohort.discoveredEncountersPerSession)
        append(',')
        appendMetric("encounterResolutionRatePercent", cohort.encounterResolutionRatePercent)
        append(',')
        appendMetric("revisitSharePercent", cohort.revisitSharePercent)
        append(',')
        appendMetric("repeatAreaFatigueProxyPercent", cohort.repeatAreaFatigueProxyPercent)
        append('}')
        append(",\"acceptance\":{")
        append("\"pass\":${cohort.acceptancePassCount},")
        append("\"fail\":${cohort.acceptanceFailCount},")
        append("\"notEvaluated\":${cohort.acceptanceNotEvaluatedCount}")
        append("}}")
    }

    private fun StringBuilder.appendMetric(key: String, metric: FieldTestMetricAverage) {
        appendJsonString(key)
        append(":{")
        append("\"average\":")
        appendNullableInt(metric.average)
        append(",\"evidenceCount\":${metric.evidenceCount}")
        append(",\"sessionCount\":${metric.sessionCount}")
        append('}')
    }
}

private fun StringBuilder.appendNullableInt(value: Int?) {
    if (value == null) append("null") else append(value)
}

private fun StringBuilder.appendNullableLong(value: Long?) {
    if (value == null) append("null") else append(value)
}

private fun StringBuilder.appendNullableBoolean(value: Boolean?) {
    if (value == null) append("null") else append(value)
}

private fun StringBuilder.appendNullableString(value: String?) {
    if (value == null) append("null") else appendJsonString(value)
}

private fun StringBuilder.appendJsonString(value: String) {
    append('"')
    value.forEach { char ->
        when (char) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (char.code < 0x20) {
                append("\\u")
                append(char.code.toString(16).padStart(4, '0'))
            } else {
                append(char)
            }
        }
    }
    append('"')
}
