# Field-test run checklist

Daily Town now separates three questions during closed field testing:

1. **Single-run reviewability** — did this completed run satisfy the evidence and acceptance rules that are already configured?
2. **Single-session acceptance** — did the measured values pass the human-approved single-session thresholds?
3. **Multi-session protocol readiness** — are there enough comparable NEW_AREA / REPEAT_AREA sessions to begin product review?

The run checklist is intentionally a data-quality layer. It does not add product thresholds and it does not replace the comparison protocol.

## Run review states

A completed run shows one of three states:

- `REFERENCE_ONLY` / **참고용**: no run-level human policy applies to this area. Collected values may be inspected, but the app does not invent a pass/readiness rule.
- `REVIEWABLE` / **검토 가능**: every run-level requirement derived from the configured acceptance criteria and required comparison evidence is present, and configured single-session acceptance is not incomplete or failing.
- `NEEDS_ATTENTION` / **확인 필요**: at least one configured run-level requirement is missing or single-session acceptance is incomplete/failing.

`REVIEWABLE` is only a single-run data-quality state. It is not the same as multi-session `PRODUCT_REVIEW_READY` / **제품 검토 가능**.

## No new policy values

The checklist derives requirements only from the existing settings:

- single-session acceptance criteria such as map READY, maximum GPS rejection, distance error, battery drain, discovered encounters, resolution rate, and repeat-area fatigue
- `FIELD_TEST_COMPARISON_REQUIRED_EVIDENCE`

For example, reference-distance evidence is mandatory only if distance error is already required by single-session acceptance or comparison evidence. Battery evidence is mandatory only when an existing battery requirement applies. Unconfigured items remain optional.

`REPEAT_AREA_FATIGUE` remains repeat-area-only. A NEW_AREA run is not marked incomplete merely because it has no repeat-fatigue evidence.

## Completed-session consistency

The session plan latched at tracking start contains only:

- area intent
- tracking preset
- optional scalar reference distance

When a run stops, the latched preset/reference are reapplied to the completed derived diagnostic. Distance error is recalculated from that latched reference.

If single-session acceptance criteria are configured, acceptance is then recalculated from the adjusted completed evidence. This prevents a later edit to the next run's reference-distance input from producing a distance-error value and acceptance verdict based on different references.

A tester may still correct NEW_AREA / REPEAT_AREA classification before recording. Area-specific repeat-fatigue acceptance follows the final corrected classification, while the technical preset/reference remain those latched at session start.

## Checklist rows

The UI summarizes:

- provider-neutral map health
- session duration / distance evidence
- scalar reference distance / distance error
- comparable battery evidence and measurement status
- GPS rejection evidence and accepted/rejected sample counts
- gameplay discovery / resolution / revisit / repeat-fatigue evidence
- single-session acceptance state and failed metric keys

Missing values are never replaced by zero. A completed run can still be recorded in the comparison recorder so cohort `evidenceCount/sessionCount` remains truthful.

## Privacy boundary

The checklist operates only on the already-derived `FieldTestDiagnostic` and the privacy-safe session plan. It does not receive, store, or export:

- latitude / longitude
- route geometry
- free-form place names
- POI / encounter / template IDs
- raw gameplay events
- NAVER credentials or provider exception payloads

Its safe `render()` output contains only checklist state, requirement flags, derived scalar/status metadata, and missing evidence keys.

## Automated coverage

JVM tests cover:

- no-policy runs remain `REFERENCE_ONLY`
- configured complete evidence becomes `REVIEWABLE`
- missing distance evidence becomes `NEEDS_ATTENTION`
- latched reference distance recomputes single-session acceptance
- corrected area classification controls repeat-fatigue acceptance
- configured map READY failures are surfaced
- repeat-fatigue does not block NEW_AREA
- rendered checklist metadata stays inside the privacy boundary

The AOSP ATD Compose flow additionally verifies that a stopped replay session renders the run summary. Normal PR builds intentionally have no human-approved run policy, so replay shows **런 요약: 참고용** rather than fabricating readiness.
