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
) {
    init {
        require(minimumSessionDurationSeconds == null || minimumSessionDurationSeconds >= 0) {
            "minimumSessionDurationSeconds must be non-negative"
        }
        require(maximumGpsRejectionRatePercent == null || maximumGpsRejectionRatePercent in 0..100) {
            "maximumGpsRejectionRatePercent must be between 0 and 100"
        }
    }

    val isConfigured: Boolean
        get() = minimumSessionDurationSeconds != null ||
            maximumGpsRejectionRatePercent != null ||
            requiredMapHealth != null
}

data class FieldTestAcceptanceInput(
    val sessionDurationSeconds: Long? = null,
    val gpsRejectionRatePercent: Int? = null,
    val mapHealth: MapHealthStatus? = null,
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
                val measured = input.sessionDurationSeconds
                add(
                    FieldTestAcceptanceCheck(
                        key = "sessionDurationSeconds",
                        status = when {
                            measured == null -> AcceptanceCheckStatus.NOT_EVALUATED
                            measured >= minimum -> AcceptanceCheckStatus.PASS
                            else -> AcceptanceCheckStatus.FAIL
                        },
                        measuredValue = measured?.toString(),
                        expectedValue = ">=$minimum",
                    ),
                )
            }

            criteria.maximumGpsRejectionRatePercent?.let { maximum ->
                val measured = input.gpsRejectionRatePercent
                add(
                    FieldTestAcceptanceCheck(
                        key = "gpsRejectionRatePercent",
                        status = when {
                            measured == null -> AcceptanceCheckStatus.NOT_EVALUATED
                            measured <= maximum -> AcceptanceCheckStatus.PASS
                            else -> AcceptanceCheckStatus.FAIL
                        },
                        measuredValue = measured?.toString(),
                        expectedValue = "<=$maximum",
                    ),
                )
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
        }

        val overall = when {
            checks.isEmpty() -> AcceptanceCheckStatus.NOT_EVALUATED
            checks.any { it.status == AcceptanceCheckStatus.FAIL } -> AcceptanceCheckStatus.FAIL
            checks.any { it.status == AcceptanceCheckStatus.NOT_EVALUATED } -> AcceptanceCheckStatus.NOT_EVALUATED
            else -> AcceptanceCheckStatus.PASS
        }
        return FieldTestAcceptanceResult(overall = overall, checks = checks)
    }
}
