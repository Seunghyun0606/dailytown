# Field-test structured export

Daily Town can explicitly share the currently buffered field-test comparison data as a versioned, privacy-safe JSON document.

## Purpose and boundary

The in-app comparison recorder is process-memory-only and bounded to 20 completed sessions. `구조화 JSON 공유` creates an explicit derived-data handoff only when the tester taps it. The app does not retain the export, automatically upload it, import it, or assign durable field-test session/device identifiers.

Schema v1 (`dailytown.field_test_export`, version `1`) preserves missing evidence as JSON `null` and excludes raw latitude/longitude, route geometry, place labels, POI/encounter/template/raw-event IDs, persistent session/device IDs, generated timestamps, provider exception payloads, and NAVER credentials.

## Physical-device flow

1. Confirm NAVER package restriction `com.dailytown.app`.
2. Run **Internal Debug APK** on the field-test branch and download the artifact.
3. Verify build/policy metadata and APK SHA-256, then install on a real Android device.
4. Outdoors, confirm real NAVER tiles and provider-neutral `MapHealth.READY`.
5. Select NEW_AREA / REPEAT_AREA, tracking preset, and trusted scalar reference distance when needed.
6. For battery evidence, disconnect power and keep test conditions reasonably comparable.
7. Walk the route, stop, review the start-latched completed plan and run summary, then record the completed session once.
8. Share structured JSON only to the approved destination and validate it locally.
9. For multiple batches: export/validate, reset the in-app comparison buffer, then collect the next batch.

## Offline tools

Validate one or more exports:

```bash
python3 tools/field_test/validate_export.py export.json
```

Aggregate operator-confirmed non-overlapping batches:

```bash
python3 tools/field_test/aggregate_exports.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

Calculate what the approved comparison protocol still needs:

```bash
python3 tools/field_test/collection_plan.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

Render a human-readable report:

```bash
python3 tools/field_test/review_report.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

Render CSV instead:

```bash
python3 tools/field_test/review_report.py \
  --format csv \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

All tools use Python standard library only and write to stdout unless the operator explicitly redirects output.

## Validation and aggregation semantics

The validator checks exact schema/privacy structure and recomputes exported cohort tracking presets, all nine metric averages/evidence counts, acceptance counts, and repeat-minus-new deltas from session rows.

Aggregation rejects exact duplicate documents and policy mismatches. Because durable session IDs/timestamps are intentionally absent, partial overlap between different snapshots cannot be detected, so 2+ exports require explicit non-overlap confirmation.

Batch protocol readiness mirrors Android `FieldTestProtocolEvaluator`: missing cohorts/shared evidence can produce `DATA_INSUFFICIENT`; comparable data without configured policy produces `COMPARABLE`; human-approved minimum cohort size, matching preset, and required evidence are applied; REPEAT_AREA_FATIGUE is REPEAT_AREA-only; satisfied configured evidence gates produce `PRODUCT_REVIEW_READY`.

`PRODUCT_REVIEW_READY` is evidence readiness, never a product-quality or release verdict. Offline aggregate/report/planner results always retain `productVerdict=NOT_COMPUTED`.

## Collection planner semantics

The planner uses only structural protocol rules and the comparison policy embedded in validated exports. It never invents thresholds.

It reports current cohort counts, approved targets, session/evidence deficits, next actions, blockers, and a `minimumAdditionalSessionsLowerBound` for each cohort. Evidence deficits are not summed because one real session can satisfy several requirements; the maximum relevant deficit is used as a lower bound. This is not a guarantee that the next session will successfully produce every evidence item.

If matching tracking preset is required and existing aggregate data already mixes presets, adding sessions cannot remove the mismatch. The planner reports a blocker and calls for clean same-preset collection or separate same-preset review. If no comparison policy exists, it reports `POLICY_NOT_CONFIGURED` rather than inventing a readiness target. If evidence requirements are satisfied, it reports `NO_ADDITIONAL_PROTOCOL_EVIDENCE` and hands off to human product review.

## Human-review report

Markdown/CSV reports include batch protocol status/issues, collection-plan status/deficits/blockers/actions, NEW/REPEAT metrics with evidence counts and deltas, run-review/acceptance distributions, and an explicit interpretation boundary. Report files, if created by shell redirection, must follow the same human-approved retention/access/deletion rules as structured JSON exports.

## Human decisions still required

Engineering does not invent the eight single-session acceptance thresholds, comparison cohort minimum, matching-preset requirement, mandatory evidence keys, representative routes/reference distances, fatigue-proxy validity, export/report retention policy, app-side persistence permission, production POI/licensing/content decisions, or Play Console/signing/privacy/analytics decisions.

## Automated coverage

The repository-local offline suite contains 41 tests: 13 validator, 12 aggregation/protocol, 9 collection-planner, and 7 review-report tests. Normal Android CI and Internal Debug builds discover the full suite before Android build steps. Android CI additionally runs 103 JVM tests, instrumented-test compilation, lint, and debug APK build; AOSP managed-device coverage continues to exercise replay/field-test/export E2E.
