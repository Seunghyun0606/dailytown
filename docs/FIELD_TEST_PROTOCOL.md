# Field-test comparison protocol

Daily Town separates **single-session acceptance** from **multi-session comparison readiness**.

## Status model

`FieldTestProtocolEvaluator` reports one of three states:

- `DATA_INSUFFICIENT`: one cohort is empty or there is no metric with evaluable evidence in both cohorts.
- `COMPARABLE`: NEW_AREA and REPEAT_AREA can be compared, but the human-approved product-review protocol is unset or not fully satisfied.
- `PRODUCT_REVIEW_READY`: comparison is possible and every configured protocol gate is satisfied.

`PRODUCT_REVIEW_READY` is not a verdict that the product is good. It only means there is enough consistently collected evidence to begin a product decision. Metric deltas still require human interpretation.

## Session preparation and latching

A field-test route is prepared **before tracking starts**. The tester selects `NEW_AREA` or `REPEAT_AREA`, chooses the normal tracking preset, and may enter a trusted scalar reference distance in the existing field-test diagnostic input. The setup card previews the next-session plan.

When DEVICE or REPLAY tracking starts, `FieldTestSessionPlan` latches only:

- area profile
- tracking preset
- optional positive scalar reference distance in meters

The plan is process-memory-only and contains no coordinates, route geometry, place label, POI/encounter/template identifier, or raw event payload.

The profile controls are locked while that session is active. When tracking stops with usable location samples, the latched plan becomes the completed-session suggestion. The tester may still correct the NEW_AREA/REPEAT_AREA classification before recording if the preparation choice was wrong.

A completed diagnostic reapplies the **latched** tracking preset and scalar reference distance. Distance error is recalculated from the completed session distance and the latched reference distance. Later edits to the next-session setup therefore cannot silently rewrite the preset/reference evidence of an already completed session.

Configured required-evidence keys are shown before the route. After stop, the comparison card names any missing required evidence. Missing values remain missing and are never converted to zero. The tester can still record the incomplete session so evidence coverage remains visible in `evidenceCount/sessionCount` rather than disappearing from the cohort.

## Optional policy

The protocol has no hard-coded product sample size or evidence requirements. Closed-test builds may receive these non-secret values through Gradle properties, environment variables, or GitHub Repository Variables:

```properties
FIELD_TEST_COMPARISON_MIN_SESSIONS_PER_COHORT=<positive integer>
FIELD_TEST_COMPARISON_REQUIRE_MATCHING_PRESET=true|false
FIELD_TEST_COMPARISON_REQUIRED_EVIDENCE=<comma-separated evidence keys>
```

Supported evidence keys are:

```text
SESSION_DURATION
SESSION_DISTANCE
GPS_REJECTION_RATE
DISTANCE_ERROR
BATTERY_DRAIN
DISCOVERED_ENCOUNTERS
ENCOUNTER_RESOLUTION
REVISIT_SHARE
REPEAT_AREA_FATIGUE
```

Invalid numbers, booleans, or evidence keys fail Gradle configuration rather than being ignored.

If `FIELD_TEST_COMPARISON_MIN_SESSIONS_PER_COHORT` is configured, every generally required evidence type must be available in at least that many sessions in **each** cohort. Without a configured minimum, a required evidence type needs at least one valid sample in each relevant cohort.

`REPEAT_AREA_FATIGUE` is intentionally scoped to the REPEAT_AREA cohort. NEW_AREA sessions commonly have no discovered revisit encounter, so they are not required to fabricate a fatigue value merely to satisfy the protocol.

If matching preset is required, all recorded NEW_AREA and REPEAT_AREA summaries must use one tracking preset. Mixed `BATTERY_SAVER`, `BALANCED`, or `PRECISE` cohorts remain viewable as `COMPARABLE`, but cannot become `PRODUCT_REVIEW_READY` until the configured protocol is satisfied.

## Missing evidence

Missing evidence is never converted to zero. Each cohort keeps `evidenceCount/sessionCount` per metric. Examples:

- externally powered battery run -> no battery-drain evidence
- no scalar reference distance at session start -> no route-distance-error evidence
- no discovered revisit encounter -> no repeat-area-fatigue evidence

This prevents a partially measured cohort from appearing stronger simply because unavailable measurements were treated as good zeros.

## Automated UI coverage

The credential-free AOSP ATD lane exercises the comparison protocol through the real Compose UI rather than only testing the pure evaluator. Stable semantics tags are attached to the setup/comparison controls and status text without changing production behavior.

The managed-device scenario verifies:

1. an empty recorder shows `DATA_INSUFFICIENT` and `0/0` cohorts
2. the first session starts with the default `NEW_AREA` setup and locks setup controls while tracking is active
3. stopping exposes the latched completed-session plan, which can be recorded once
4. the same stopped session cannot be recorded twice
5. `REPEAT_AREA` can be selected **before** the second replay session starts and is latched into that session
6. stopping the second session suggests `REPEAT_AREA` without requiring post-hoc classification
7. with protocol policy intentionally unset in the normal PR build, the two cohorts advance to `COMPARABLE`
8. resetting the comparison returns the recorder to `DATA_INSUFFICIENT` and `0/0`

Pure JVM tests additionally cover positive-reference validation, latched preset/reference application, distance-error recalculation, clearing reference evidence when a session started without one, required-evidence inspection, and the NEW_AREA exception for repeat-fatigue evidence.

`PRODUCT_REVIEW_READY` remains covered by pure JVM protocol tests because that state depends on human-approved Repository Variables and must not be fabricated in the normal credential-free PR build.

## Privacy boundary

The comparison protocol operates on bounded, process-memory-only `FieldTestSessionSummary` values. Session preparation likewise exists only in process memory. It does not receive or retain:

- latitude/longitude or route geometry
- free-form place labels
- POI, encounter, template, or session identifiers
- raw gameplay event payloads
- NAVER or release credentials

The in-app process-local session token is used only to identify a new tracking session and prevent duplicate recording of the same stopped session. It is never exported or persisted.

## Human decisions still required

Before using `PRODUCT_REVIEW_READY` as a closed-test gate, a person must approve:

- minimum valid sessions per NEW_AREA and REPEAT_AREA cohort
- whether tracking preset must match across cohorts
- which evidence keys are mandatory
- the existing single-session acceptance thresholds
- whether the repeat-area-fatigue proxy correlates sufficiently with subjective repetition/fatigue in real walking tests

The generated `field-test-policy.txt` in the Internal Debug APK artifact records the configured comparison protocol next to the existing non-secret acceptance policy so testers can verify exactly which gates were active for a build.
