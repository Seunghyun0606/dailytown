# Marker promotion readiness gate

This gate protects the 24 approved marker candidates from entering `production_export` before all existing technical and human gates are satisfied. It does **not** promote assets and it does not change marker design rules.

## Bound evidence

`NaverMapQaDiagnostics` now writes a `visualContract` object into `visual/naver-diagnostics/session.json` containing:

- `markerBatchId`
- `markerCandidateAssetCount`
- `markerCandidateFingerprintSha256`

The fingerprint is SHA-256 over the sorted rows `family|semantic_key|declared_asset_sha256` from `marker-split-export-v1.json`. The readiness tool separately hashes all 24 current marker files and requires their bytes to match the manifest, so evidence cannot be reused after a candidate byte changes.

Both emulator and physical-device NAVER evidence must therefore reference the exact current marker candidate fingerprint.

## Required inputs

Run:

```bash
python3 tools/visual/verify_marker_promotion_readiness.py \
  --emulator-session /path/to/emulator/visual/naver-diagnostics/session.json \
  --physical-session /path/to/physical/visual/naver-diagnostics/session.json \
  --human-approval /path/to/marker-promotion-approval.v1.json
```

The checker fails closed unless all of the following are true:

- marker batch is still `marker-split-export-v1`, candidate-only, exactly 24 assets, exact approved anchor, exact DAY/DARK semantic sets, and all SHA-256 values match current bytes;
- emulator NAVER evidence is schema v3, clean PASS, package/client/network contract passes, comes from `pixel2Api30Atd`, contains the current marker fingerprint, and has exactly 18 baseline + 10 EV-1 completed captures with passing marker-free base-map evidence;
- physical NAVER evidence has the same technical requirements but must report `emulator=false` and `runnerHint=physical-connected-device`;
- human approval binds to the same fingerprint, has `decision=APPROVED`, reviewer/timestamp, and PASS for marker readability, selected-state anchor, route/HUD/companion readability, provider road/place comprehension, and NAVER attribution/legal UI.

Use `design/export-spec/marker-promotion-approval.template.v1.json` as a local review template. The committed template intentionally remains `PENDING` and must never be interpreted as approval.

## CI behavior

Android CI runs `tools/visual/test_marker_promotion_readiness.py`. The tests prove that the checker rejects emulator evidence masquerading as physical evidence, stale marker fingerprints, pending human decisions, and changed candidate bytes. CI does **not** fabricate physical evidence or human approval and therefore does not run the real promotion command automatically.

## Promotion boundary

A readiness PASS is only evidence that promotion prerequisites are satisfied. Actual changes from `production_export_candidate` to `production_export`, main-APK registry inclusion, or PR merge remain separate explicit development actions. PR #10 must remain unmerged unless explicitly requested.
