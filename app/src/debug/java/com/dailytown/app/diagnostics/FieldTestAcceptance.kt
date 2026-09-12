package com.dailytown.app.diagnostics

import com.dailytown.app.map.MapHealthStatus

enum class AcceptanceCheckStatus {
    PASS,
    FAIL,
    NOT_EVALUATED,
}

data class FieldTestAcceptanceCriteria(
    val minimumSessionDurationSeconds: Long? = null,
    val maximumGpsRejectionRatePercent: Int? = null,
    val requiredMapHealth: MapHealthStatus? = null,
    val maximumDistanceErrorPercent: Int? = null,
    val maximumBatteryDrainPercentPerHour: Int? = null,
    val minimumDiscoveredEncountersPerSession: Int? = null,
    val minimumEncounterResolutionRatePercent: Int? = null,
    val maximumRepeatAreaFatiguePercent: Int? = null,
) {
    init {
        require(minimumSessionDurationSeconds == null || minimumSessionDurationSeconds >= 0) {
            "minimumSessionDurationSeconds must be non-negative"
        }
        require(maximumGpsRejectionRatePercent == null || maximumGpsRejectionRatePercent in 0..100) {
            "maximumGpsRejectionRatePercent must be between 0 and 100"
        }
        require(maximumDistanceErrorPercent == null || maximumDistanceErrorPercent in 0..100) {
            "maximumDistanceErrorPercent must be between 0 and 100"
        }
        require(maximumBatteryDrainPercentPerHour == null || maximumBatteryDrainPercentPerHour >= 0) {
            "maximumBatteryDrainPercentPerHour must be non-negative"
        }
        require(minimumDiscoveredEncountersPerSession == null || minimumDiscoveredEncountersPerSession >= 0) {
            "minimumDiscoveredEncountersPerSession must be non-negative"
        }
        require(minimumEncounterResolutionRatePercent == null || minimumEncounterResolutionRatePercent in 0..100) {
            "minimumEncounterResolutionRatePercent must be between 0 and 100"
        }
        require(maximumRepeatAreaFatiguePercent == null || maximumRepeatAreaFatiguePercent in 0..100) {
            "maximumRepeatAreaFatiguePercent must be between 0 and 100"
        }
    }

    val isConfigured: Boolean
        get() = minimumSessionDurationSeconds != null ||
            maximumGpsRejectionRatePercent != null ||
            requiredMapHealth != null ||
            maximumDistanceErrorPercent != null ||
            maximumBatteryDrainPercentPerHour != null ||
            minimumDiscoveredEncountersPerSession != null ||
            minimumEncounterResolutionRatePercent != null ||
            maximumRepeatAreaFatiguePercent != null
}

data class FieldTestAcceptanceInput(
    val sessionDurationSeconds: Long? = null,
    val gpsRejectionRatePercent: Int? = null,
    val mapHealth: MapHealthStatus? = null,
    val distanceErrorPercent: Int? = null,
    val batteryDrainPercentPerHour: Int? = null,
    val discoveredEncountersPerSession: Int? = null,
    val encounterResolutionRatePercent: Int? = null,
    val repeatAreaFatigueProxyPercent: Int? = null,
)

data class FieldTestAcceptanceCheck(
    val key: String,
    val status: AcceptanceCheckStatus,
    val measuredValue: String?,
    val expectedValue: String,
)

data class FieldTestAcceptanceResult(
    val overall: AcceptanceCheckStatus,
    val checks: List<FieldTestAcceptanceCheck>,
) {
    val failedKeys: List<String>
        get() = checks.filter { it.status == AcceptanceCheckStatus.FAIL }.map { it.key }
}

/**
 * Evaluates field-test evidence against human-approved criteria.
 *
 * This class deliberately has no built-in product thresholds. Until a person approves the
 * acceptance numbers, callers can leave criteria unset and the evaluator will return
 * NOT_EVALUATED rather than silently inventing a pass/fail policy.
 */
class FieldTestAcceptanceEvaluator {
    fun evaluate(
        criteria: FieldTestAcceptanceCriteria,
        input: FieldTestAcceptanceInput,
    ): FieldTestAcceptanceResult {
        val checks = buildList {
            criteria.minimumSessionDurationSeconds?.let { minimum ->
                add(maximumOrMinimumCheck(
                    key = "sessionDurationSeconds",
                    measured = input.sessionDurationSeconds,
                    expected = minimum,
                    passes = { measured, expected -> measured >= expected },
                    expectedValue = ">=$minimum",
                ))
            }

            criteria.maximumGpsRejectionRatePercent?.let { maximum ->
                add(maximumOrMinimumCheck(
                    key = "gpsRejectionRatePercent",
                    measured = input.gpsRejectionRatePercent,
                    expected = maximum,
                    passes = { measured, expected -> measured <= expected },
                    expectedValue = "<=$maximum",
                ))
            }

            criteria.requiredMapHealth?.let { required ->
                val measured = input.mapHealth
                add(
                    FieldTestAcceptanceCheck(
                        key = "mapHealth",
                        status = when {
                            measured == null -> AcceptanceCheckStatus.NOT_EVALUATED
                            measured == required -> AcceptanceCheckStatus.PASS
                            else -> AcceptanceCheckStatus.FAIL
                        },
                        measuredValue = measured?.name,
                        expectedValue = required.name,
                    ),
                )
            }

            criteria.maximumDistanceErrorPercent?.let { maximum ->
                add(maximumOrMinimumCheck(
                    key = "distanceErrorPercent",
                    measured = input.distanceErrorPercent,
                    expected = maximum,
                    passes = { measured, expected -> measured <= expected },
                    expectedValue = "<=$maximum",
                ))
            }

            criteria.maximumBatteryDrainPercentPerHour?.let { maximum ->
                add(maximumOrMinimumCheck(
                    key = "batteryDrainPercentPerHour",
                    measured = input.batteryDrainPercentPerHour,
                    expected = maximum,
                    passes = { measured, expected -> measured <= expected },
                    expectedValue = "<=$maximum",
                ))
            }

            criteria.minimumDiscoveredEncountersPerSession?.let { minimum ->
                add(maximumOrMinimumCheck(
                    key = "discoveredEncountersPerSession",
                    measured = input.discoveredEncountersPerSession,
                    expected = minimum,
                    passes = { measured, expected -> measured >= expected },
                    expectedValue = ">=$minimum",
                ))
            }

            criteria.minimumEncounterResolutionRatePercent?.let { minimum ->
                add(maximumOrMinimumCheck(
                    key = "encounterResolutionRatePercent",
                    measured = input.encounterResolutionRatePercent,
                    expected = minimum,
                    passes = { measured, expected -> measured >= expected },
                    expectedValue = ">=$minimum",
                ))
            }

            criteria.maximumRepeatAreaFatiguePercent?.let { maximum ->
                add(maximumOrMinimumCheck(
                    key = "repeatAreaFatigueProxyPercent",
                    measured = input.repeatAreaFatigueProxyPercent,
                    expected = maximum,
                    passes = { measured, expected -> measured <= expected },
                    expectedValue = "<=$maximum",
                ))
            }
        }

        val overall = when {
            checks.isEmpty() -> AcceptanceCheckStatus.NOT_EVALUATED
            checks.any { it.status == AcceptanceCheckStatus.FAIL } -> AcceptanceCheckStatus.FAIL
            checks.any { it.status == AcceptanceCheckStatus.NOT_EVALUATED } -> AcceptanceCheckStatus.NOT_EVALUATED
            else -> AcceptanceCheckStatus.PASS
        }
        return FieldTestAcceptanceResult(overall = overall, checks = checks)
    }

    private fun <T : Comparable<T>> maximumOrMinimumCheck(
        key: String,
        measured: T?,
        expected: T,
        passes: (T, T) -> Boolean,
        expectedValue: String,
    ): FieldTestAcceptanceCheck = FieldTestAcceptanceCheck(
        key = key,
        status = when {
            measured == null -> AcceptanceCheckStatus.NOT_EVALUATED
            passes(measured, expected) -> AcceptanceCheckStatus.PASS
            else -> AcceptanceCheckStatus.FAIL
        },
        measuredValue = measured?.toString(),
        expectedValue = expectedValue,
    )
}
