# R-B outdoor readability — result

Status: **PASS (owner physical-device review, 2026-10-20)**

Gate definition: `design/final-human-gates-v3` → `docs/design/FINAL_HUMAN_GATES_V3.md` §4 and
`design/review/final-human-gates-v3/outdoor/R_B_OUTDOOR_READABILITY_REVIEW.md` ("Only an actual
physical-device outdoor review may close R-B as PASS").

## Decision record

- Reviewer: project owner, on their own Android device, outdoors.
- Recorded verdict (owner, in the development session on 2026-10-20): outdoor readability on the
  physical device is acceptable ("실기기에서 야외 가독성 나쁘지않아").
- No per-cell measurements of the 54-cell matrix were provided; this record captures the owner's
  overall PASS, not a cell-by-cell matrix. If a cell-level matrix is later required for release
  review, attach it here.

## Effects in code

- `CompanionRuntimeProfileSelector.defaultProfile` is now `MORU_CANONICAL_V2` for debug and release.
  `LEGACY_V1` stays as the rollback profile (debug intent extra `com.dailytown.app.extra.COMPANION_RUNTIME_PROFILE`).
- Marker promotion is a separate gate (NAVER physical evidence bundle) and is not changed by this record.
