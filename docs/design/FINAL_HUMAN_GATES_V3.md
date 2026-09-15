# Daily Town — Final Human Gates v3

> Status: **DESIGN REVIEW PACK READY — HUMAN DECISIONS PENDING**
>
> Branch scope: design-only preparation for `M-B`, `ID-A`, and `R-B`. This document does not close any Human Gate and does not authorize Android runtime changes.

## 1. Source lock

This pack derives only from the approved Design Baseline v2 and the completed Moru Candidate 3 production family.

Immutable in this work:

- Moru Candidate 3 canonical raster
- native neutral master
- six usage-context exports
- LIGHT / WARM_DUSK / DARK lighting family
- semantic expression family
- semantic resolver/export readiness manifest
- Android runtime profile/binding

No Moru redesign, native-master regeneration, semantic-family modification, runtime profile change, Kotlin/Compose edit, `main` merge, or PR #10 modification is part of this branch.

## 2. Human Gate A — M-B motion timing/intensity

Review artifact: `design/review/final-human-gates-v3/motion/M_B_MOTION_REVIEW.md`

Interactive design-only preview: `design/review/final-human-gates-v3/motion/motion-review.html`

Three timing/intensity candidates are prepared under the already-approved **calm-reactive M-B direction**:

- `M-B1 Quiet`: lowest motion energy; maximum map calmness.
- `M-B2 Calm Reactive`: balanced companion presence; design recommendation for Human Gate review.
- `M-B3 Warm Expressive`: strongest permitted reaction while keeping Candidate 3 restrained.

All three reuse the existing exact raster authorities. The preview applies only transform/opacity behavior to those assets; it does not redraw or edit Moru pixels.

Human must choose `M-B1`, `M-B2`, `M-B3`, or request a timing-only revision.

## 3. Human Gate B — ID-A app identity lock

Review artifact: `design/review/final-human-gates-v3/id-a/ID_A_ICON_LOCK_REVIEW.md`

Candidate sheet: `design/review/final-human-gates-v3/id-a/id-a-candidate-sheet.svg`

The existing direction remains fixed:

- two-leaf sprout
- path / town cue
- Moru face is not used as the app logo

Three lock candidates are prepared:

- `ID-A1 Sprout Trail` — strongest path cue, lightest town cue.
- `ID-A2 Sprout Gate` — strongest town/door cue, compact and symmetric.
- `ID-A3 Town Path` — strongest neighborhood/town read with a centered trail.

Each is shown in light, dark, monochrome, adaptive safe-zone, and small-size review states. These are design masters/candidates only; no Android resource binding is performed here.

Human must choose one candidate and optionally one wordmark lockup direction before production resource handoff.

## 4. Human Gate C — R-B outdoor readability

Review artifact: `design/review/final-human-gates-v3/outdoor/R_B_OUTDOOR_READABILITY_REVIEW.md`

Machine-readable capture matrix: `design/review/final-human-gates-v3/outdoor/r-b-outdoor-readability-matrix.v1.json`

Static design-only preflight: `design/review/final-human-gates-v3/outdoor/outdoor-preflight.html`

Required matrix:

- map avatar: 48 / 56 / 64 dp
- HUD: 56 / 64 / 72 dp
- lighting: LIGHT / WARM_DUSK / DARK
- background: cream / dark / map-heavy

The preflight uses accepted Moru assets only and exists to make physical review easier. **It is not physical-device evidence and cannot produce PASS_RB.**

Human checks:

- sprout silhouette
- face readability
- scarf diagonal
- costume identity
- boots/base
- alpha halo
- foreground/background separation
- glance readability

## 5. Decision boundaries

A Human Gate result is valid only when explicitly recorded as one of:

- `PASS` — acceptable without a design-side correction.
- `REVIEW` — usable evidence exists, but a specific issue needs one more comparison or confirmation.
- `FAIL` — a concrete criterion fails and must be corrected before promotion.

For `R-B`, only a physical Android-device review in representative outdoor conditions may result in `PASS`.

## 6. Development handoff after Human Gate

Development receives only the selected outputs:

- selected M-B timing/easing/intensity token set and reduced-motion behavior
- selected ID-A foreground/background/monochrome master direction and adaptive-icon constraints
- R-B physical-device outcome with failing cells/evidence if any

Design does not modify runtime resources on this branch.
