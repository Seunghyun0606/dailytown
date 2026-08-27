# Marker physical evidence bundle

The physical NAVER runner now packages one successful real-device marker QA run into a fingerprint-bound review bundle. This does not promote any marker asset and does not replace human review.

## Run

From the repository root, with exactly one authorized physical Android phone attached and the NAVER Maps client ID supplied only through the environment:

```bash
tools/android/run_naver_physical_evidence.sh
```

Optional output root:

```bash
DAILYTOWN_MARKER_EVIDENCE_DIR=/safe/local/path/marker-evidence \
  tools/android/run_naver_physical_evidence.sh
```

The runner refuses emulator targets, clears only stale connected-device additional NAVER output before starting, runs the existing `NaverMapVisualQaTest`, and creates a review bundle only when that test exits successfully.

## Bundle contract

A successful run creates `physical-<UTC timestamp>/` and a matching ZIP. The directory contains:

- `session.json`: the original physical NAVER diagnostic session;
- `captures/`: exactly the 28 technical matrix captures referenced by the session;
- `bundle-manifest.v1.json`: marker batch/fingerprint, physical session SHA-256, per-capture SHA-256, non-emulator runner metadata, and `promotion_performed=false`;
- `marker-promotion-approval.v1.json`: a copy of the committed approval template with the exact current marker fingerprint filled in, while `decision` and every human check remain `PENDING`;
- `REVIEW.md`: the human inspection checklist and readiness-check instructions.

`package_marker_physical_evidence.py` fails closed unless:

- the current 24 marker candidate files still match `marker-split-export-v1.json` SHA-256 values and semantic/anchor contract;
- `session.json` is schema v3, clean PASS, `emulator=false`, `runnerHint=physical-connected-device`, package/client/network valid, and bound to the exact current marker fingerprint;
- the matrix is exactly 18 baseline + 10 EV-1 completed captures;
- every referenced `visual/naver-matrix/...` PNG exists and no duplicate or path-traversal storage name is present;
- the approval template is still PENDING before it is copied into the bundle.

No coordinate, route geometry, credential value, device serial, automatic upload, production asset mutation, or PR merge is added by this tooling.

## Human completion

Inspect the 28 captures and the app on the same physical phone. Update the bundle's approval JSON only after checking:

- marker readability;
- selected-state anchor;
- route/HUD/companion readability;
- provider road/place comprehension;
- NAVER attribution/legal UI.

Keep any uncertain item as `PENDING` or mark it `FAIL`; do not force an approval. After all checks pass, add reviewer and review timestamp, set the checks and decision to `PASS` / `APPROVED`, then run the existing `verify_marker_promotion_readiness.py` with a passing emulator session, this physical `session.json`, and the completed approval JSON.

A readiness PASS is still only permission for a separate explicit development change. Marker promotion and PR merge remain separate actions.
