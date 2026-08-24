# Field-test structured export

Daily Town can explicitly share the currently buffered field-test comparison data as a versioned, privacy-safe JSON document.

## Purpose

The in-app comparison recorder is intentionally process-memory-only and bounded to 20 completed sessions. This protects the MVP from silently creating a durable location-derived dataset while still allowing a tester to explicitly take derived evidence outside the app for review.

`구조화 JSON 공유` creates that handoff only when the tester taps the button. The app itself does not persist the export or session history.

## Schema and privacy boundary

- schema name: `dailytown.field_test_export`
- schema version: `1`
- maximum session rows per app export: `20`
- missing evidence: JSON `null`; never converted to zero
- no raw latitude/longitude, route geometry, place/free-form route labels, POI/encounter/template/raw-event identifiers, persistent session/device IDs, generated timestamp, provider exception payload, or NAVER credential

The app remains process-memory-only for field-test history. Export is tester-initiated only; there is no automatic backend/cloud/analytics upload or app-side import/persistence.

## Required physical-device procedure

1. Confirm NAVER Cloud Console Android package restriction is exactly `com.dailytown.app`.
2. Run **Actions -> Internal Debug APK -> Run workflow** on the field-test branch.
3. Download `dailytown-internal-debug-apk` and verify `build-metadata.txt`, `field-test-policy.txt`, SHA-256, and APK.
4. Install on a real Android device.
5. Outdoors, confirm real NAVER tiles and provider-neutral `MapHealth.READY`.
6. Select NEW_AREA / REPEAT_AREA, tracking preset, and trusted scalar reference distance when required.
7. For battery evidence, disconnect power and keep device/brightness/preset reasonably comparable.
8. Run the route, stop tracking, confirm the start-latched completed plan, and review the run summary.
9. Record each completed session once in the comparison buffer.
10. Share structured JSON only to the human-approved destination and validate it locally.

## Local validation

```bash
python3 tools/field_test/validate_export.py path/to/field-test-export.json
```

The validator uses the Python standard library only. It checks the exact v1 shape/privacy boundary and recomputes cohort tracking presets, all nine averages/evidence counts, acceptance counts, and repeat-minus-new deltas from session rows. It is a structural/privacy validator, not a product-quality gate.

## Multi-export aggregation

```bash
python3 tools/field_test/aggregate_exports.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

The tool validates every source, rejects exact duplicates and policy mismatches, recomputes metrics from session rows, preserves missing evidence, and mirrors Android `FieldTestProtocolEvaluator` semantics. `PRODUCT_REVIEW_READY` means evidence readiness only; `productVerdict` remains `NOT_COMPUTED`.

Because exports intentionally omit durable session IDs/timestamps, partial overlap between different snapshots cannot be detected. For multiple files, collect a batch, export/validate it, reset the in-app comparison buffer, then collect the next batch; `--confirm-non-overlapping` is mandatory.

## Next evidence collection planner

```bash
python3 tools/field_test/collection_plan.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

Use `--json` for machine-readable stdout.

The planner never invents product thresholds. It uses only structural protocol requirements and the human-approved comparison policy already embedded in the exports. It reports current sessions, configured targets, session/evidence deficits, collection actions, blockers, and a `minimumAdditionalSessionsLowerBound` per cohort.

One session can satisfy multiple evidence deficits, so deficits are not summed. The result is a lower bound, not a guarantee. If matching preset is required but the aggregate already mixes presets, adding sessions cannot remove that mismatch; the planner reports a blocker and calls for clean same-preset collection/separate review. With no configured comparison policy it reports `POLICY_NOT_CONFIGURED` instead of inventing a target. Once protocol evidence is satisfied it reports `NO_ADDITIONAL_PROTOCOL_EVIDENCE`, while product/release judgment remains human-only.

## Human-review report

Markdown:

```bash
python3 tools/field_test/review_report.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

CSV:

```bash
python3 tools/field_test/review_report.py \
  --format csv \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

Reports include protocol status/issues, collection-plan status/deficits/blockers/actions, NEW/REPEAT metrics with evidence counts and deltas, run-review/acceptance distributions, and the explicit `NOT_COMPUTED` product-verdict boundary. Output is stdout-only; if redirected to a file, the approved export/report retention policy applies.

## Human decisions still required

Engineering must not invent the eight single-session thresholds, comparison cohort minimum, matching-preset requirement, mandatory comparison evidence, representative routes/reference distances, fatigue-proxy validity, export/report retention/access/deletion policy, app-side persistence permission, production POI/licensing/content approval, or Play Console/signing/privacy/analytics decisions.

## Automated coverage

Repository-local Python coverage contains 41 tests: 13 validator, 12 aggregation/protocol, 9 collection-planner, and 7 review-report tests. Normal Android CI and manually triggered Internal Debug builds run the complete Python suite before Android build steps; Android CI also runs 103 JVM tests, instrumented-test compilation, lint, and debug APK build. AOSP managed-device coverage continues to exercise replay/field-test/export E2E.
