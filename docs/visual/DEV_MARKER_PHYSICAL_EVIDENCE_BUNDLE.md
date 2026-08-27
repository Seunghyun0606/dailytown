# Marker physical evidence bundle

The physical NAVER runner packages one successful real-device marker QA run into a marker-fingerprint and physical-session-bound review bundle. This does not promote any marker asset and does not replace human review.

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
- `bundle-manifest.v1.json`: marker batch/fingerprint, physical session SHA-256, `REVIEW.md` SHA-256, per-capture SHA-256, non-emulator runner metadata, and `promotion_performed=false`;
- `marker-promotion-approval.v1.json`: a copy of the committed approval template with both the exact current marker fingerprint and the exact physical `session.json` SHA-256 filled in, while `decision` and every human check remain `PENDING`;
- `REVIEW.md`: the human inspection checklist, physical session SHA-256, and readiness-check instructions.

`package_marker_physical_evidence.py` fails closed unless:

- the current 24 marker candidate files still match `marker-split-export-v1.json` SHA-256 values and semantic/anchor contract;
- `session.json` is schema v3, clean PASS, `emulator=false`, `runnerHint=physical-connected-device`, package/client/network valid, and bound to the exact current marker fingerprint;
- the matrix is exactly 18 baseline + 10 EV-1 completed captures;
- every referenced `visual/naver-matrix/...` PNG exists and no duplicate or path-traversal storage name is present;
- the approval template is still PENDING and still contains the physical-session placeholder before packaging.

The packager computes the physical `session.json` SHA-256 once and writes the same value to both `bundle-manifest.v1.json` and the pending approval copy. It also hashes the generated `REVIEW.md`. The completed approval must not be copied between physical runs, even when the marker fingerprint is unchanged.

No coordinate, route geometry, credential value, device serial, automatic upload, production asset mutation, or PR merge is added by this tooling.

## Bundle integrity verification

Before human review, after copying a bundle between machines, and again before promotion readiness, verify the bundle directory or ZIP independently:

```bash
python3 tools/visual/verify_marker_physical_evidence_bundle.py \
  --bundle /safe/local/path/physical-<UTC timestamp>
```

or:

```bash
python3 tools/visual/verify_marker_physical_evidence_bundle.py \
  --bundle /safe/local/path/physical-<UTC timestamp>.zip
```

The verifier is non-mutating and never promotes assets. It fails closed unless all of the following remain true:

- the current marker candidate batch is still the exact 24-file DAY/DARK candidate set and fingerprint;
- `session.json` is still a clean physical `physical-connected-device` NAVER PASS bound to that fingerprint;
- the manifest physical-session SHA-256 matches the actual `session.json` bytes;
- the manifest `REVIEW.md` SHA-256 matches the actual review instructions;
- all 28 manifest capture records match the session `id`, `kind`, and derived bundle path;
- every capture file exists and matches its manifest SHA-256;
- the bundle contains exactly the four root files plus the 28 expected capture files, with no missing or unexpected files;
- approval schema, marker fingerprint, physical-session SHA-256, and human-check keys remain valid;
- a human-modified `APPROVED` approval has all five checks `PASS` plus non-empty reviewer and review timestamp;
- ZIP input has exactly one top-level bundle directory and contains no absolute/traversal paths, duplicate entries, symlinks, or oversized extraction payloads.

A `PENDING` approval is valid for bundle integrity because integrity verification does not replace human review. Only `verify_marker_promotion_readiness.py` decides whether the completed human approval is sufficient for promotion readiness.

## Human completion

Inspect the 28 captures and the app on the same physical phone. Update the bundle's approval JSON only after checking:

- marker readability;
- selected-state anchor;
- route/HUD/companion readability;
- provider road/place comprehension;
- NAVER attribution/legal UI.

Keep any uncertain item as `PENDING` or mark it `FAIL`; do not force an approval. Do not edit `marker_candidate_fingerprint_sha256` or `physical_session_sha256`. After all checks pass, add reviewer and review timestamp, set every check to `PASS`, and set the top-level decision to `APPROVED`.

Run the bundle integrity verifier again after editing the approval JSON. Then run the existing `verify_marker_promotion_readiness.py` with a passing emulator session, this exact physical `session.json`, and the completed approval JSON.

The readiness checker recomputes the supplied physical session SHA-256 and rejects an approval created for a different session. A readiness PASS is still only permission for a separate explicit development change. Marker promotion and PR merge remain separate actions.

## Human TODO tracking

The real-device marker review remains a human-owned gate and is tracked in the existing Daily Town physical Android/NAVER TODO rather than duplicated in a separate task. The M-B timing/easing/intensity approval, R-B outdoor acceptance, and ID-A final lockup remain tracked in the existing visual approval TODO.
