# Field-test structured export

Daily Town can explicitly share the currently buffered field-test comparison data as a versioned, privacy-safe JSON document.

## Purpose

The in-app comparison recorder is intentionally process-memory-only and bounded to 20 completed sessions. This protects the MVP from silently creating a durable location-derived dataset while still allowing a tester to explicitly take derived evidence outside the app for review.

`구조화 JSON 공유` creates that handoff only when the tester taps the button. The app itself does not persist the export or session history.

## Schema

- schema name: `dailytown.field_test_export`
- schema version: `1`
- maximum session rows per app export: `20`
- session ordering: current in-memory recorder order, exposed only as an export-local `ordinal`
- missing evidence: JSON `null`; never converted to zero

The export includes app/package metadata, non-secret acceptance/protocol criteria, protocol assessment, derived per-session metrics and run-review state, and NEW_AREA / REPEAT_AREA cohort aggregates.

## Privacy boundary

The export intentionally contains no latitude/longitude, route geometry, place/free-form route labels, POI/encounter/template/raw-event identifiers, session token or persistent session ID, device ID, generated timestamp, provider exception payload, or NAVER credential.

The export is useful for comparing derived field evidence without becoming a replayable route/location history.

## Why there is no import or automatic persistence yet

Importing files back into the app, assigning durable run IDs, or retaining field-test history would establish a new product data-retention boundary. Those decisions require human approval for retention duration, access rules, deletion behavior, and privacy notice implications.

Until then:

- the app keeps comparison sessions only in process memory
- relaunch/reset clears the in-app buffer
- export is tester-initiated only
- the receiving app/service chosen from the Android share sheet controls external retention
- there is no automatic backend/cloud/analytics upload

## Required physical-device procedure

1. Confirm NAVER Cloud Console Android package restriction is exactly `com.dailytown.app`.
2. In GitHub, run **Actions -> Internal Debug APK -> Run workflow** on the field-test branch.
3. Download `dailytown-internal-debug-apk` after the workflow succeeds.
4. Verify `build-metadata.txt`, `field-test-policy.txt`, and the SHA-256 file are present beside `app-debug.apk`.
5. Install the APK on the real Android test device.
6. Outdoors, confirm real NAVER tiles render and the app reaches `지도 정상` / provider-neutral `MapHealth.READY`.
7. Before a route, select `신규 지역` or `반복 지역`, confirm tracking preset, and enter a trusted scalar route distance when distance-error evidence is required.
8. For battery evidence, disconnect external power and keep device/brightness/preset conditions reasonably comparable.
9. Start tracking, walk the route, stop, and confirm the completed plan still shows the start-latched area/preset/reference.
10. Review `런 요약`: `확인 필요` means a configured required item is missing/failed; `참고용` means no applicable run-level policy is configured; `검토 가능` means the configured single-run requirements are complete and passing.
11. Record the session once into the comparison buffer and repeat for NEW_AREA / REPEAT_AREA sessions.
12. Tap **구조화 JSON 공유** and send/save the JSON only to the human-approved destination.
13. Validate the shared JSON locally before product review.

## Local export validation

Run:

```bash
python3 tools/field_test/validate_export.py path/to/field-test-export.json
```

The validator is offline and uses only the Python standard library. It does not upload, rewrite, or persist the export.

In addition to schema/privacy checks, v1 validation recomputes the exported comparison from session rows and requires the following to match exactly:

- schema/package/privacy boundary and exact v1 field shapes
- 1..20 sessions and contiguous ordinals
- all nine per-session metric fields
- NEW_AREA / REPEAT_AREA session counts
- tracking-preset sets
- each metric average, evidence count, and session count
- acceptance PASS / FAIL / NOT_EVALUATED counts
- all repeat-minus-new deltas
- missing evidence staying `null`
- absence of known location/route/event/session/device/timestamp/credential-shaped fields

The validator is a structural/privacy validator, not a product-quality gate. A valid file may still contain `REFERENCE_ONLY`, `NEEDS_ATTENTION`, `DATA_INSUFFICIENT`, or missing evidence.

## Offline multi-export batch review

After collecting multiple **non-overlapping** exports under the same policy, aggregate them locally:

```bash
python3 tools/field_test/aggregate_exports.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

Use `--json` to print the derived batch summary as JSON to stdout instead of text. The tool does not create a file unless the operator explicitly redirects stdout.

The batch tool:

- validates every source export first
- rejects exact duplicate JSON documents using a local SHA-256 digest
- rejects exports whose acceptance/comparison policies differ
- recomputes NEW_AREA / REPEAT_AREA averages directly from session rows rather than averaging already-rounded cohort averages
- preserves missing evidence as `null` with an explicit evidence count
- reports tracking presets, acceptance/run-review counts, app versions, and repeat-minus-new deltas
- recomputes the comparison protocol using the same semantics as the Android `FieldTestProtocolEvaluator`
- exposes the non-secret comparison policy to downstream offline review/planning tools
- always emits `productVerdict=NOT_COMPUTED`

### Batch protocol readiness vs product verdict

Batch protocol readiness is allowed to be recomputed because it only evaluates the **human-approved comparison protocol** already embedded in the exports.

The offline evaluator mirrors the app rules:

- missing NEW_AREA or REPEAT_AREA cohort => `DATA_INSUFFICIENT`
- both cohorts but no common evaluable metric => `DATA_INSUFFICIENT`
- structurally comparable with no configured comparison policy => `COMPARABLE`
- configured minimum cohort size, matching-preset requirement, and required evidence are applied
- `REPEAT_AREA_FATIGUE` evidence is required only for the REPEAT_AREA cohort
- all configured gates satisfied => `PRODUCT_REVIEW_READY`

`PRODUCT_REVIEW_READY` means **evidence is ready for a human product review**. It does not mean the product is good, the metric deltas are acceptable, or the app is ready for release. The aggregate always keeps `productVerdict=NOT_COMPUTED`.

### Non-overlap limitation

The privacy design deliberately exports no durable session IDs or timestamps. Therefore two different JSON snapshots can partially contain the same sessions and software cannot prove that they are disjoint.

For more than one export, `--confirm-non-overlapping` is mandatory. The recommended collection procedure is:

1. collect one batch
2. share and validate its export
3. reset the in-app comparison buffer
4. only then collect the next batch

Exact duplicate files are rejected automatically, but partial overlap between distinct snapshots is not detectable. Do not aggregate overlapping snapshots for product decisions.

## Next evidence collection planner

Once at least one validated export exists, calculate what the approved protocol still needs:

```bash
python3 tools/field_test/collection_plan.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

Use `--json` for machine-readable stdout.

The planner deliberately does **not** invent product thresholds. It only uses:

- structural requirements already defined by the app protocol evaluator
- the human-approved `minimumSessionsPerCohort`
- the human-approved matching-tracking-preset requirement
- the human-approved required evidence list
- current NEW_AREA / REPEAT_AREA session and evidence counts

The plan reports:

- current session count per cohort
- configured minimum session target, when one exists
- session-count deficit
- each required evidence item's current count, target count, and deficit
- a **minimum additional session lower bound** per cohort
- structural actions when a cohort or shared evidence is missing
- a blocker when matching tracking preset is required but the current aggregate already contains multiple presets
- a human-decision action when comparison policy is not configured
- a human-product-review action when the approved evidence protocol is already satisfied

### Lower-bound semantics

The planner never adds all evidence deficits together. One additional session can satisfy multiple required evidence items at the same time, so the planner uses the maximum relevant deficit as a **lower bound**.

A lower bound is not a guarantee. A new session may fail to produce battery, distance-error, or other evidence, so more real sessions may still be needed.

Tracking-preset mismatch is different: if the current aggregate already mixes presets while matching is required, adding sessions cannot remove those existing preset values. The planner therefore reports a blocker and recommends reviewing separate same-preset data or collecting a clean same-preset batch instead of presenting a fake numeric fix.

If no comparison policy is configured, the planner does not invent a target. It reports `POLICY_NOT_CONFIGURED`; only structural missing-cohort/shared-evidence requirements can still produce a minimal collection lower bound.

## Human-review report

For a compact review document, render Markdown to stdout:

```bash
python3 tools/field_test/review_report.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

For spreadsheet-friendly output, render CSV to stdout:

```bash
python3 tools/field_test/review_report.py \
  --format csv \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

The Markdown report contains:

- source/session/app-version summary
- recomputed batch protocol status and issue list
- collection-plan status
- current sessions, approved target, deficits, and minimum additional-session lower bounds
- required-evidence current/target counts and deficits
- collection blockers and next actions
- NEW_AREA / REPEAT_AREA metric averages
- evidence count beside every average
- repeat-minus-new delta
- run-review distribution
- acceptance distribution
- an explicit interpretation boundary separating protocol readiness from product quality

The CSV report contains summary, protocol-issue, collection-plan, collection-evidence, collection-blocker/action, metric, run-review, and acceptance rows. Missing numeric evidence is emitted as an empty CSV cell while evidence counts remain explicit.

Both report formats are stdout-only. If a human chooses to redirect them into a file, that destination must follow the approved export retention/access policy.

## Human decisions still required

Engineering must not invent:

- the eight single-session acceptance threshold values
- minimum valid sessions per NEW_AREA / REPEAT_AREA cohort
- whether matching tracking preset is mandatory
- mandatory comparison evidence keys
- representative mostly-new/repeat-area routes and trusted scalar reference distances
- whether repeat-area-fatigue proxy correlates sufficiently with subjective repetition/fatigue
- approved external export/report destination and authorized access
- retention duration and deletion procedure
- whether future app-side import/persistence is allowed
- production POI/public-data licensing and authored content approval
- Play Console, release signing, privacy, analytics/crash collection, and external tester decisions

Until export retention/access decisions are approved, do not automatically upload these JSON documents or derived reports to analytics, cloud storage, a backend, or source control.

## Automated coverage

Android/JVM coverage continues to verify the app-side export schema, privacy boundary, missing-as-null behavior, bounded recorder, and managed-device export enable/reset flow.

Repository-local Python coverage now contains 41 tests:

- 13 strict validator tests
- 12 batch-aggregation/protocol tests covering non-overlap confirmation, duplicate/policy mismatch rejection, cross-file recomputation, missing evidence, invalid-source rejection, protocol readiness, structural insufficiency, and repeat-only fatigue evidence
- 9 collection-planner tests covering unconfigured policy, structural missing cohorts/shared evidence, minimum session deficits, required evidence deficits, repeat-only fatigue, tracking-preset blockers, lower-bound semantics, and evidence-ready human review handoff
- 7 review-report tests covering Markdown/CSV output, protocol issues, evidence counts, missing-value rendering, collection-plan lower bounds/evidence deficits, and the permanent `NOT_COMPUTED` product verdict boundary

Both normal Android CI and the manually triggered Internal Debug APK workflow run the full `tools/field_test/test_*.py` suite before Android build steps.
