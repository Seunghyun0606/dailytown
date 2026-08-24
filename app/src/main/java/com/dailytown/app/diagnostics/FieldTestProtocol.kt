package com.dailytown.app.diagnostics

enum class FieldTestProtocolStatus {
    DATA_INSUFFICIENT,
    COMPARABLE,
    PRODUCT_REVIEW_READY,
}

enum class FieldTestProtocolEvidence {
    SESSION_DURATION,
    SESSION_DISTANCE,
    GPS_REJECTION_RATE,
    DISTANCE_ERROR,
    BATTERY_DRAIN,
    DISCOVERED_ENCOUNTERS,
    ENCOUNTER_RESOLUTION,
    REVISIT_SHARE,
    REPEAT_AREA_FATIGUE;

    companion object {
        fun parseCsv(raw: String): Set<FieldTestProtocolEvidence> {
            if (raw.isBlank()) return emptySet()
            return raw.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { valueOf(it) }
                .toSet()
        }
    }
}

data class FieldTestProtocolCriteria(
    val minimumSessionsPerCohort: Int? = null,
    val requireMatchingTrackingPreset: Boolean? = null,
    val requiredEvidence: Set<FieldTestProtocolEvidence> = emptySet(),
) {
    init {
        require(minimumSessionsPerCohort == null || minimumSessionsPerCohort > 0) {
            "minimumSessionsPerCohort must be positive when configured"
        }
    }

    val isConfigured: Boolean
        get() = minimumSessionsPerCohort != null ||
            requireMatchingTrackingPreset == true ||
            requiredEvidence.isNotEmpty()
}

data class FieldTestProtocolIssue(
    val key: String,
    val detail: String,
)

data class FieldTestProtocolAssessment(
    val status: FieldTestProtocolStatus,
    val configured: Boolean,
    val issues: List<FieldTestProtocolIssue>,
) {
    fun render(): String = buildString {
        appendLine("protocolConfigured=$configured")
        appendLine("protocolStatus=${status.name}")
        issues.forEach { issue -> appendLine("protocolIssue.${issue.key}=${issue.detail}") }
    }.trimEnd()
}

/**
 * Pure evaluator for deciding whether NEW_AREA vs REPEAT_AREA evidence is structurally comparable
 * and whether human-approved protocol gates are satisfied. It never receives raw location or event
 * data and does not decide whether a metric delta is good or bad.
 */
class FieldTestProtocolEvaluator {
    fun evaluate(
        report: FieldTestComparisonReport,
        criteria: FieldTestProtocolCriteria,
    ): FieldTestProtocolAssessment {
        val structuralIssues = buildList {
            if (report.newArea.sessionCount == 0) {
                add(FieldTestProtocolIssue("newAreaSessions", "missing"))
            }
            if (report.repeatArea.sessionCount == 0) {
                add(FieldTestProtocolIssue("repeatAreaSessions", "missing"))
            }
            if (report.newArea.sessionCount > 0 && report.repeatArea.sessionCount > 0 &&
                report.deltas.none { it.repeatMinusNew != null }
            ) {
                add(FieldTestProtocolIssue("sharedEvidence", "missing"))
            }
        }
        if (structuralIssues.isNotEmpty()) {
            return FieldTestProtocolAssessment(
                status = FieldTestProtocolStatus.DATA_INSUFFICIENT,
                configured = criteria.isConfigured,
                issues = structuralIssues,
            )
        }

        if (!criteria.isConfigured) {
            return FieldTestProtocolAssessment(
                status = FieldTestProtocolStatus.COMPARABLE,
                configured = false,
                issues = emptyList(),
            )
        }

        val protocolIssues = buildList {
            criteria.minimumSessionsPerCohort?.let { minimum ->
                if (report.newArea.sessionCount < minimum) {
                    add(
                        FieldTestProtocolIssue(
                            "newAreaMinimumSessions",
                            "${report.newArea.sessionCount}/$minimum",
                        ),
                    )
                }
                if (report.repeatArea.sessionCount < minimum) {
                    add(
                        FieldTestProtocolIssue(
                            "repeatAreaMinimumSessions",
                            "${report.repeatArea.sessionCount}/$minimum",
                        ),
                    )
                }
            }

            if (criteria.requireMatchingTrackingPreset == true) {
                val presets = report.newArea.trackingPresets + report.repeatArea.trackingPresets
                if (presets.size != 1) {
                    add(
                        FieldTestProtocolIssue(
                            "trackingPresetConsistency",
                            "mismatch:${presets.sorted().joinToString("+")}",
                        ),
                    )
                }
            }

            val requiredCount = criteria.minimumSessionsPerCohort ?: 1
            criteria.requiredEvidence.sortedBy { it.name }.forEach { evidence ->
                val newMetric = report.newArea.metric(evidence)
                val repeatMetric = report.repeatArea.metric(evidence)
                if (newMetric.evidenceCount < requiredCount) {
                    add(
                        FieldTestProtocolIssue(
                            "newAreaEvidence.${evidence.name}",
                            "${newMetric.evidenceCount}/$requiredCount",
                        ),
                    )
                }
                if (repeatMetric.evidenceCount < requiredCount) {
                    add(
                        FieldTestProtocolIssue(
                            "repeatAreaEvidence.${evidence.name}",
                            "${repeatMetric.evidenceCount}/$requiredCount",
                        ),
                    )
                }
            }
        }

        return FieldTestProtocolAssessment(
            status = if (protocolIssues.isEmpty()) {
                FieldTestProtocolStatus.PRODUCT_REVIEW_READY
            } else {
                FieldTestProtocolStatus.COMPARABLE
            },
            configured = true,
            issues = protocolIssues,
        )
    }
}

private fun FieldTestCohortSummary.metric(evidence: FieldTestProtocolEvidence): FieldTestMetricAverage =
    when (evidence) {
        FieldTestProtocolEvidence.SESSION_DURATION -> sessionDurationSeconds
        FieldTestProtocolEvidence.SESSION_DISTANCE -> sessionDistanceMeters
        FieldTestProtocolEvidence.GPS_REJECTION_RATE -> gpsRejectionRatePercent
        FieldTestProtocolEvidence.DISTANCE_ERROR -> distanceErrorPercent
        FieldTestProtocolEvidence.BATTERY_DRAIN -> batteryDrainPercentPerHour
        FieldTestProtocolEvidence.DISCOVERED_ENCOUNTERS -> discoveredEncountersPerSession
        FieldTestProtocolEvidence.ENCOUNTER_RESOLUTION -> encounterResolutionRatePercent
        FieldTestProtocolEvidence.REVISIT_SHARE -> revisitSharePercent
        FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE -> repeatAreaFatigueProxyPercent
    }
