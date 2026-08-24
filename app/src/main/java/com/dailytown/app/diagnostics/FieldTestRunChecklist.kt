package com.dailytown.app.diagnostics

enum class FieldTestRunReviewStatus {
    REFERENCE_ONLY,
    REVIEWABLE,
    NEEDS_ATTENTION,
}

enum class FieldTestRunCheckStatus {
    COMPLETE,
    MISSING,
    FAIL,
    OPTIONAL,
}

data class FieldTestRunCheck(
    val key: String,
    val required: Boolean,
    val status: FieldTestRunCheckStatus,
    val detail: String,
)

data class FieldTestRunChecklist(
    val status: FieldTestRunReviewStatus,
    val policyConfigured: Boolean,
    val checks: List<FieldTestRunCheck>,
    val missingRequiredEvidence: Set<FieldTestProtocolEvidence>,
) {
    fun render(): String = buildString {
        appendLine("runPolicyConfigured=$policyConfigured")
        appendLine("runReviewStatus=${status.name}")
        if (missingRequiredEvidence.isNotEmpty()) {
            appendLine(
                "runMissingEvidence=${missingRequiredEvidence.sortedBy { it.name }.joinToString(",") { it.name }}",
            )
        }
        checks.forEach { check ->
            appendLine(
                "runCheck.${check.key}=${check.status.name};required=${check.required};detail=${check.detail}",
            )
        }
    }.trimEnd()
}

internal fun requiredFieldTestRunEvidence(
    acceptanceCriteria: FieldTestAcceptanceCriteria,
    protocolCriteria: FieldTestProtocolCriteria,
    areaProfile: FieldTestAreaProfile,
): Set<FieldTestProtocolEvidence> {
    val effectiveAcceptance = acceptanceCriteria.forAreaProfile(areaProfile)
    return buildSet {
        addAll(protocolCriteria.requiredEvidence)
        effectiveAcceptance.minimumSessionDurationSeconds?.let {
            add(FieldTestProtocolEvidence.SESSION_DURATION)
        }
        effectiveAcceptance.maximumGpsRejectionRatePercent?.let {
            add(FieldTestProtocolEvidence.GPS_REJECTION_RATE)
        }
        effectiveAcceptance.maximumDistanceErrorPercent?.let {
            add(FieldTestProtocolEvidence.DISTANCE_ERROR)
        }
        effectiveAcceptance.maximumBatteryDrainPercentPerHour?.let {
            add(FieldTestProtocolEvidence.BATTERY_DRAIN)
        }
        effectiveAcceptance.minimumDiscoveredEncountersPerSession?.let {
            add(FieldTestProtocolEvidence.DISCOVERED_ENCOUNTERS)
        }
        effectiveAcceptance.minimumEncounterResolutionRatePercent?.let {
            add(FieldTestProtocolEvidence.ENCOUNTER_RESOLUTION)
        }
        effectiveAcceptance.maximumRepeatAreaFatiguePercent?.let {
            add(FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE)
        }
    }.filterNot {
        it == FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE &&
            areaProfile == FieldTestAreaProfile.NEW_AREA
    }.toSet()
}

/**
 * Summarizes whether one completed field-test run satisfies already configured evidence/acceptance
 * policy. It never creates thresholds of its own and never interprets metric quality beyond the
 * existing acceptance result.
 */
class FieldTestRunChecklistEvaluator {
    private val evidenceInspector = FieldTestSessionEvidenceInspector()

    fun evaluate(
        diagnostic: FieldTestDiagnostic,
        areaProfile: FieldTestAreaProfile,
        acceptanceCriteria: FieldTestAcceptanceCriteria,
        protocolCriteria: FieldTestProtocolCriteria,
    ): FieldTestRunChecklist {
        val effectiveAcceptance = acceptanceCriteria.forAreaProfile(areaProfile)
        val requiredEvidence = requiredFieldTestRunEvidence(
            acceptanceCriteria = acceptanceCriteria,
            protocolCriteria = protocolCriteria,
            areaProfile = areaProfile,
        )
        val evidenceAssessment = evidenceInspector.evaluate(
            diagnostic = diagnostic,
            areaProfile = areaProfile,
            requiredEvidence = requiredEvidence,
        )
        val missing = evidenceAssessment.missingRequiredEvidence
        val policyConfigured = effectiveAcceptance.isConfigured || requiredEvidence.isNotEmpty()

        val checks = listOf(
            mapCheck(diagnostic, effectiveAcceptance),
            sessionCheck(diagnostic, requiredEvidence, missing),
            routeCheck(diagnostic, requiredEvidence, missing),
            batteryCheck(diagnostic, requiredEvidence, missing),
            gpsCheck(diagnostic, requiredEvidence, missing),
            gameplayCheck(diagnostic, requiredEvidence, missing),
            acceptanceCheck(diagnostic, effectiveAcceptance),
        )
        val needsAttention = checks.any {
            it.required && (it.status == FieldTestRunCheckStatus.MISSING || it.status == FieldTestRunCheckStatus.FAIL)
        }

        return FieldTestRunChecklist(
            status = when {
                !policyConfigured -> FieldTestRunReviewStatus.REFERENCE_ONLY
                needsAttention -> FieldTestRunReviewStatus.NEEDS_ATTENTION
                else -> FieldTestRunReviewStatus.REVIEWABLE
            },
            policyConfigured = policyConfigured,
            checks = checks,
            missingRequiredEvidence = missing,
        )
    }

    private fun mapCheck(
        diagnostic: FieldTestDiagnostic,
        criteria: FieldTestAcceptanceCriteria,
    ): FieldTestRunCheck {
        val required = criteria.requiredMapHealth != null
        val measured = diagnostic.mapHealthStatus
        val status = when {
            required && measured == null -> FieldTestRunCheckStatus.MISSING
            required && measured != criteria.requiredMapHealth?.name -> FieldTestRunCheckStatus.FAIL
            measured == "READY" -> FieldTestRunCheckStatus.COMPLETE
            required -> FieldTestRunCheckStatus.FAIL
            else -> FieldTestRunCheckStatus.OPTIONAL
        }
        return FieldTestRunCheck(
            key = "map",
            required = required,
            status = status,
            detail = measured ?: "missing",
        )
    }

    private fun sessionCheck(
        diagnostic: FieldTestDiagnostic,
        requiredEvidence: Set<FieldTestProtocolEvidence>,
        missing: Set<FieldTestProtocolEvidence>,
    ): FieldTestRunCheck {
        val relevant = setOf(
            FieldTestProtocolEvidence.SESSION_DURATION,
            FieldTestProtocolEvidence.SESSION_DISTANCE,
        ).intersect(requiredEvidence)
        val required = relevant.isNotEmpty()
        val relevantMissing = relevant.intersect(missing)
        val available = diagnostic.trackingDurationSeconds != null || diagnostic.sessionDistanceMeters != null
        return FieldTestRunCheck(
            key = "session",
            required = required,
            status = checkStatus(required, relevantMissing.isNotEmpty(), available),
            detail = "duration=${diagnostic.trackingDurationSeconds ?: "missing"};distance=${diagnostic.sessionDistanceMeters ?: "missing"}",
        )
    }

    private fun routeCheck(
        diagnostic: FieldTestDiagnostic,
        requiredEvidence: Set<FieldTestProtocolEvidence>,
        missing: Set<FieldTestProtocolEvidence>,
    ): FieldTestRunCheck {
        val required = FieldTestProtocolEvidence.DISTANCE_ERROR in requiredEvidence
        val isMissing = FieldTestProtocolEvidence.DISTANCE_ERROR in missing
        val available = diagnostic.referenceDistanceMeters != null && diagnostic.distanceErrorPercent != null
        return FieldTestRunCheck(
            key = "route",
            required = required,
            status = checkStatus(required, isMissing, available),
            detail = "reference=${diagnostic.referenceDistanceMeters ?: "missing"};error=${diagnostic.distanceErrorPercent ?: "missing"}",
        )
    }

    private fun batteryCheck(
        diagnostic: FieldTestDiagnostic,
        requiredEvidence: Set<FieldTestProtocolEvidence>,
        missing: Set<FieldTestProtocolEvidence>,
    ): FieldTestRunCheck {
        val required = FieldTestProtocolEvidence.BATTERY_DRAIN in requiredEvidence
        val isMissing = FieldTestProtocolEvidence.BATTERY_DRAIN in missing
        val available = diagnostic.batteryDrainPercentPerHour != null
        return FieldTestRunCheck(
            key = "battery",
            required = required,
            status = checkStatus(required, isMissing, available),
            detail = "measurement=${diagnostic.batteryMeasurementStatus ?: "missing"};drainPerHour=${diagnostic.batteryDrainPercentPerHour ?: "missing"}",
        )
    }

    private fun gpsCheck(
        diagnostic: FieldTestDiagnostic,
        requiredEvidence: Set<FieldTestProtocolEvidence>,
        missing: Set<FieldTestProtocolEvidence>,
    ): FieldTestRunCheck {
        val required = FieldTestProtocolEvidence.GPS_REJECTION_RATE in requiredEvidence
        val isMissing = FieldTestProtocolEvidence.GPS_REJECTION_RATE in missing
        val available = diagnostic.rejectedLocationRatePercent != null
        return FieldTestRunCheck(
            key = "gps",
            required = required,
            status = checkStatus(required, isMissing, available),
            detail = "rejection=${diagnostic.rejectedLocationRatePercent ?: "missing"};accepted=${diagnostic.acceptedLocationCount ?: "missing"};rejected=${diagnostic.rejectedLocationCount}",
        )
    }

    private fun gameplayCheck(
        diagnostic: FieldTestDiagnostic,
        requiredEvidence: Set<FieldTestProtocolEvidence>,
        missing: Set<FieldTestProtocolEvidence>,
    ): FieldTestRunCheck {
        val gameplayEvidence = setOf(
            FieldTestProtocolEvidence.DISCOVERED_ENCOUNTERS,
            FieldTestProtocolEvidence.ENCOUNTER_RESOLUTION,
            FieldTestProtocolEvidence.REVISIT_SHARE,
            FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE,
        )
        val relevant = gameplayEvidence.intersect(requiredEvidence)
        val required = relevant.isNotEmpty()
        val relevantMissing = relevant.intersect(missing)
        val available = diagnostic.sessionEncounterDiscoveredCount != null ||
            diagnostic.sessionEncounterResolutionRatePercent != null ||
            diagnostic.sessionRevisitSharePercent != null ||
            diagnostic.repeatAreaFatigueProxyPercent != null
        return FieldTestRunCheck(
            key = "gameplay",
            required = required,
            status = checkStatus(required, relevantMissing.isNotEmpty(), available),
            detail = "discovered=${diagnostic.sessionEncounterDiscoveredCount ?: "missing"};resolution=${diagnostic.sessionEncounterResolutionRatePercent ?: "missing"};revisit=${diagnostic.sessionRevisitSharePercent ?: "missing"};fatigue=${diagnostic.repeatAreaFatigueProxyPercent ?: "missing"}",
        )
    }

    private fun acceptanceCheck(
        diagnostic: FieldTestDiagnostic,
        criteria: FieldTestAcceptanceCriteria,
    ): FieldTestRunCheck {
        val required = criteria.isConfigured
        val status = when {
            !required -> FieldTestRunCheckStatus.OPTIONAL
            diagnostic.acceptanceOverall == AcceptanceCheckStatus.PASS.name -> FieldTestRunCheckStatus.COMPLETE
            diagnostic.acceptanceOverall == AcceptanceCheckStatus.FAIL.name -> FieldTestRunCheckStatus.FAIL
            else -> FieldTestRunCheckStatus.MISSING
        }
        val failed = diagnostic.acceptanceFailedKeys.sorted().joinToString(",").ifBlank { "none" }
        return FieldTestRunCheck(
            key = "acceptance",
            required = required,
            status = status,
            detail = "overall=${diagnostic.acceptanceOverall};failed=$failed",
        )
    }

    private fun checkStatus(
        required: Boolean,
        missing: Boolean,
        available: Boolean,
    ): FieldTestRunCheckStatus = when {
        required && missing -> FieldTestRunCheckStatus.MISSING
        available -> FieldTestRunCheckStatus.COMPLETE
        required -> FieldTestRunCheckStatus.MISSING
        else -> FieldTestRunCheckStatus.OPTIONAL
    }
}
