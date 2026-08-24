package com.dailytown.app.diagnostics

import com.dailytown.app.location.LocationTrackingPreset
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Privacy-safe plan latched when a field-test tracking session starts.
 * It deliberately contains no coordinates, route geometry, place labels, or event identifiers.
 */
data class FieldTestSessionPlan(
    val areaProfile: FieldTestAreaProfile,
    val trackingPreset: LocationTrackingPreset,
    val referenceDistanceMeters: Int? = null,
) {
    init {
        require(referenceDistanceMeters == null || referenceDistanceMeters > 0) {
            "referenceDistanceMeters must be positive when configured"
        }
    }
}

data class FieldTestSessionEvidenceAssessment(
    val missingRequiredEvidence: Set<FieldTestProtocolEvidence>,
) {
    val isComplete: Boolean
        get() = missingRequiredEvidence.isEmpty()
}

/**
 * Inspects one already-derived diagnostic against the human-configured comparison evidence policy.
 * This is advisory for the tester: it never fabricates missing evidence and never changes acceptance.
 */
class FieldTestSessionEvidenceInspector {
    fun evaluate(
        diagnostic: FieldTestDiagnostic,
        areaProfile: FieldTestAreaProfile,
        requiredEvidence: Set<FieldTestProtocolEvidence>,
    ): FieldTestSessionEvidenceAssessment {
        val missing = requiredEvidence
            .asSequence()
            .filterNot {
                it == FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE &&
                    areaProfile == FieldTestAreaProfile.NEW_AREA
            }
            .filterNot { diagnostic.hasEvidence(it) }
            .toSet()
        return FieldTestSessionEvidenceAssessment(missingRequiredEvidence = missing)
    }
}

fun parseReferenceDistanceMeters(raw: String): Int? =
    raw.trim().toIntOrNull()?.takeIf { it > 0 }

/**
 * Re-applies the values latched at session start to a completed derived diagnostic. This prevents
 * later edits to the setup UI from changing the profile-independent distance/preset evidence of an
 * already completed session.
 */
fun FieldTestDiagnostic.withSessionPlan(plan: FieldTestSessionPlan): FieldTestDiagnostic {
    val reference = plan.referenceDistanceMeters
    val distanceError = if (reference != null && sessionDistanceMeters != null) {
        ((abs(sessionDistanceMeters - reference).toDouble() / reference) * 100.0).roundToInt()
    } else {
        null
    }
    return copy(
        trackingPreset = plan.trackingPreset.name,
        referenceDistanceMeters = reference,
        distanceErrorPercent = distanceError,
    )
}

private fun FieldTestDiagnostic.hasEvidence(evidence: FieldTestProtocolEvidence): Boolean =
    when (evidence) {
        FieldTestProtocolEvidence.SESSION_DURATION -> trackingDurationSeconds != null
        FieldTestProtocolEvidence.SESSION_DISTANCE -> sessionDistanceMeters != null
        FieldTestProtocolEvidence.GPS_REJECTION_RATE -> rejectedLocationRatePercent != null
        FieldTestProtocolEvidence.DISTANCE_ERROR -> distanceErrorPercent != null
        FieldTestProtocolEvidence.BATTERY_DRAIN -> batteryDrainPercentPerHour != null
        FieldTestProtocolEvidence.DISCOVERED_ENCOUNTERS -> sessionEncounterDiscoveredCount != null
        FieldTestProtocolEvidence.ENCOUNTER_RESOLUTION -> sessionEncounterResolutionRatePercent != null
        FieldTestProtocolEvidence.REVISIT_SHARE -> sessionRevisitSharePercent != null
        FieldTestProtocolEvidence.REPEAT_AREA_FATIGUE -> repeatAreaFatigueProxyPercent != null
    }
