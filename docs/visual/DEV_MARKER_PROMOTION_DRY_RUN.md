# Marker production activation dry-run gate

Status: development tooling complete; marker production activation remains blocked by physical-device evidence + human approval.

## Purpose

`tools/visual/verify_marker_activation_contract.py` is a non-mutating, fail-closed structural gate for the marker activation delta. It exists so a future marker promotion cannot silently combine unrelated runtime changes or collapse the approved DAY/DARK families.

The tool does **not** replace `verify_marker_promotion_readiness.py`. Evidence readiness must pass first. The activation contract only verifies repository structure before/after the explicit promotion commit.

## Candidate state (current)

Run:

```bash
python3 tools/visual/verify_marker_activation_contract.py --state candidate
```

The gate verifies all of the following:

- marker batch id is `marker-split-export-v1`
- exactly 24 marker assets exist
- DAY and DARK each contain the exact approved 12 semantic keys
- all marker bytes still match their manifest SHA-256
- every asset remains `production_export_candidate`
- batch status remains `design_qa_complete_production_export_candidate`
- main Android assets do **not** expose `../design/production/markers/v1`
- `ProductionMarkerAssetRegistry.PROMOTED_MARKER_COUNT == 0`
- the production marker index remains empty
- no family-aware production marker record is present

`--print-plan` may be used only in candidate state. It prints a deterministic, fingerprint-bound 24-record activation plan but writes nothing:

```bash
python3 tools/visual/verify_marker_activation_contract.py --state candidate --print-plan
```

The generated plan keeps raw filenames below the Android adapter boundary and identifies runtime records by `(MarkerFamily, SemanticAssetKey)`.

## Production state (future explicit activation commit)

Only after the physical + human marker readiness checker passes, the explicit activation change must switch the repository contract and then run:

```bash
python3 tools/visual/verify_marker_activation_contract.py --state production
```

Production mode requires all of the following together:

1. marker batch status is exactly `production_export`
2. all 24 asset approval states are exactly `production_export`
3. the same 24 source files still match their authoritative SHA-256 values
4. `app/build.gradle.kts` exposes exactly the approved marker production root to the main APK
5. `PROMOTED_MARKER_COUNT == 24`
6. the marker registry contains exactly 24 explicit `marker(MarkerFamily.*, semantic, runtimePath)` records
7. those records exactly match the batch's DAY/DARK family, semantic key and relative runtime path contract

Missing records, extra records, family swaps, path substitutions, stale bytes, partial metadata promotion, or candidate assets exposed through the main APK fail the gate.

## Tests

`tools/visual/test_marker_activation_contract.py` is part of the existing `tools/visual/test_*.py` CI discovery. It covers:

- the actual repository remains candidate-only today
- candidate activation plan is exactly 24 unique family/semantic pairs
- candidate main-APK marker exposure is rejected
- non-zero candidate production registry is rejected
- a simulated exact 24-record production state passes
- missing production records fail
- production structure with candidate metadata fails
- modified marker bytes fail checksum validation

## Boundaries retained

- No marker asset is promoted by this tooling.
- No source file is modified by the verifier.
- No NAVER credential is read or emitted.
- No physical-device or human approval is synthesized.
- No PR is merged.
- Marker coordinate anchor and semantic gameplay state remain unchanged.
- Provider-neutral map/domain boundaries remain unchanged.

## Activation sequence

The intended sequence remains:

1. obtain current-head physical NAVER marker evidence
2. complete fingerprint-bound human approval
3. run `verify_marker_promotion_readiness.py`
4. only if readiness PASSes, make a separate marker activation commit
5. switch the activation contract from candidate to production and verify it
6. extend target-APK SHA binding to the 24 marker assets
7. rerun production marker rendering, selected-anchor, NAVER/EV-1 and physical readability QA

Until step 3 passes, `PROMOTED_MARKER_COUNT` must remain zero and the marker source root must remain absent from the main APK.
