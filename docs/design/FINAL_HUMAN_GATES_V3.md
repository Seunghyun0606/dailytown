# Daily Town — Final Human Gates v3

> Status: **M-B + ID-A HUMAN APPROVED / LOCKED — R-B PHYSICAL DEVICE PENDING**
>
> Branch scope: design-only finalization for `M-B`, `ID-A`, and `R-B`. M-B and ID-A are now selected. R-B remains an open physical-device Human Gate. This document does not authorize unrelated Android runtime changes.

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

No Moru redesign, native-master regeneration, semantic-family modification, runtime profile change, `main` merge, or PR #10 modification is part of this branch.

## 2. Human Gate A — M-B motion timing/intensity — LOCKED

Human decision: **M-B2 Calm Reactive**.

Review artifact: `design/review/final-human-gates-v3/motion/M_B_MOTION_REVIEW.md`

Interactive design-only preview: `design/review/final-human-gates-v3/motion/motion-review.html`

Locked contract:

- idle: 2600 ms, `cubic-bezier(.4,0,.2,1)`, ±1.4% bbox vertical amplitude, cap 1.0 dp map / 1.2 dp HUD, scale `1.000 ↔ 1.008`, rotation ±0.4°, loop
- `clue_found`: 720 ms total; 90 ms anticipation / 210 ms accent / 420 ms settle; -3.5% Y, +2.5% scale, 1.8° tilt, settle overshoot ≤0.5%
- `resolved`: 900 ms total; 100 ms anticipation / 240 ms accent / 560 ms settle; -2.0% Y, 1.2° nod
- easing: anticipation `cubic-bezier(.4,0,.6,1)`, reaction `cubic-bezier(.16,1,.3,1)`, settle `cubic-bezier(.22,.72,.24,1)`
- `clue_found` and `resolved`: one-shot, then semantic static hold
- reduced motion: no looping/translation/scale/rotation/bounce; semantic static state required; optional opacity-only crossfade ≤120 ms

`M-B1` and `M-B3` remain comparison history. The M-B design Human Gate is closed; runtime implementation/QA belongs to Development.

## 3. Human Gate B — ID-A app identity lock — LOCKED

Human decision: **ID-A2 Sprout Gate**.

Review artifact: `design/review/final-human-gates-v3/id-a/ID_A_ICON_LOCK_REVIEW.md`

Candidate sheet: `design/review/final-human-gates-v3/id-a/id-a-candidate-sheet.svg`

The locked identity remains:

- two-leaf sprout
- town/door arch whose opening doubles as the path cue
- Moru face/body excluded from the app logo

Locked layer masters:

- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/foreground-light.svg`
- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/foreground-dark.svg`
- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/background-light.svg`
- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/background-dark.svg`
- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/monochrome.svg`

Adaptive-icon handoff remains 108×108 dp layers with a centered 66×66 dp guaranteed safe zone and the core mark kept within the 48–66 dp target. Launcher identity is **mark-only for now**. Wordmark typography is not part of this lock.

`ID-A1` and `ID-A3` remain comparison history. The ID-A design Human Gate is closed; Android adaptive-icon resource binding/launcher QA belongs to Development.

## 4. Human Gate C — R-B outdoor readability — OPEN

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

Only an actual physical-device outdoor review may close R-B as `PASS`.

## 5. Decision state

- M-B: `PASS / LOCKED` — `M-B2 Calm Reactive`
- ID-A: `PASS / LOCKED` — `ID-A2 Sprout Gate`, launcher `mark-only`
- R-B: `PENDING_PHYSICAL_DEVICE`

No static/emulator artifact may be used to infer `PASS_RB_PHYSICAL_DEVICE`.

## 6. Development handoff

Development may now consume:

- the locked M-B2 timing/easing/intensity/reduced-motion contract;
- the locked ID-A2 light/dark foreground-background and monochrome design layers plus adaptive safe-zone constraints.

Development must still wait for a real-device R-B verdict before treating outdoor readability as passed.

Design does not modify runtime resources on this branch.
