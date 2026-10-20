# Daily Town P0 production execution status

Status date: 2026-08-25

This document tracks P0 visual-production work. It contains no Android/Kotlin implementation.

## P0 design work completed without further human product decisions

### P0-1 Machine-readable visual package — COMPLETE

Created under `design/export-spec/`:

- `visual-tokens.v1.json`
- `moru-production-manifest.v1.json`
- `marker-manifest.v1.json`
- `effect-manifest.v1.json`
- `a3-ui-asset-manifest.v1.json`
- `map-overlay-qa-matrix.v1.json`
- `visual-pack-manifest.v1.json`
- `sprite-gen-moru-pilot.v1.json`
- `source-promotion-qa.v1.json`
- existing `companion-manifest.example.json`

The semantic package, fallback rules, approval states, checksum requirement, reduced-motion requirement and human gates are machine-readable.

### P0-2 Moru A-2 production source master — COMPLETE AT APPROVED_SOURCE LEVEL

Created:

- `design/source/companion/moru/moru-a2-vector-source-master-v1.svg`
- `design/source/companion/moru/moru-a2-source-record.v1.json`

The vector source master locks:

- canonical body geometry
- six semantic expressions
- LIGHT / WARM_DUSK / DARK treatment
- usage-context guides for map avatar / HUD / encounter
- replaceable scarf/bag/detail zones

The earlier generated production board is preserved only as a candidate/reference record and is explicitly not runtime-eligible.

Remaining promotion work is isolated transparent split export + visual QA + checksum. That is an export/QA gate, not a missing design-system decision.

### P0-3 Companion motion production scope / sprite-gen pilot preparation — COMPLETE

Created:

- `design/export-spec/sprite-gen-moru-pilot.v1.json`

Pilot states:

- `idle_breathe`
- `clue_react`
- `resolved_settle`

Next candidates after pilot:

- `investigate`
- `happy_bounce`

`walk` remains experimental.

`preview_fps` and frame targets are authoring defaults only; final timing/easing/intensity is a human approval gate after visible prototypes.

`route / halo / discovery shell / A-3 transitions` remain native/procedural motion, not sprite-gen runtime assets.

### P0-4 DAY / DARK marker source master — COMPLETE AT APPROVED_SOURCE LEVEL

Created:

- `design/source/markers/marker-family-v1.svg`

The source master contains the 12 semantic markers in DAY and DARK treatments with a common bottom geographic anchor and non-color state distinctions.

`marker-manifest.v1.json` now points to this source master and has moved from `pending_art_export` to `source_master_created`.

Remaining promotion work is split export + representative map readability QA.

### P0-5 A-3 reusable source asset kit — COMPLETE AT APPROVED_SOURCE LEVEL

Created:

- `design/source/a3/a3-ui-source-master-v1.svg`

Includes reusable approved-source primitives for:

- Journal / Collection / Memory paper surfaces
- discovery sticker
- unresolved / resolved clue cards
- companion stamp shell
- resolved memory stamp
- locked collection pattern

`a3-ui-asset-manifest.v1.json` now points to the source master and has moved to `source_master_created`.

Remaining promotion work is split export + five-screen fit QA.

### P0-6 Map Overlay QA specification — COMPLETE; EXECUTION HANDED TO APP/FIELD TEST

Machine-readable baseline remains:

`MORNING / SUNSET / NIGHT × sparse / dense / green-space × normal / reduced motion = 18 captures`

Design-side acceptance criteria are defined. Actual capture execution requires the Android map integration/debug harness, and final outdoor acceptance requires a person on a physical device.

### P0-7 Design → development visual handoff package — COMPLETE

`visual-pack-manifest.v1.json` now references source manifests, source masters, promotion QA, sprite-gen pilot contract, semantic asset groups, fallbacks and human gates.

No Kotlin/runtime code was changed by this design session.

## Current P0 promotion gates

The following are not missing design decisions; they are validation/export/integration gates:

1. Moru isolated transparent split exports + visual QA + SHA-256
2. marker split exports + representative real-map QA
3. A-3 split exports + five-screen fit QA
4. generated sprite-gen pilot atlas + human motion QA
5. Android 18-capture overlay test execution
6. final physical outdoor readability acceptance

Only assets that pass `source-promotion-qa.v1.json` may become `production_export`.

## Human product/design gates unchanged

- Luca/Pino/Beri shipping approval
- affinity Best Friend transformation ceiling
- final motion timing/easing/intensity
- physical outdoor readability approval
- EVENING dedicated board decision
- app icon/logo final lock

## P0 conclusion

All P0 work that can be completed in the design session without additional human product decisions or Android runtime execution is now complete at the **design contract + approved-source master** level.

The next phase is P1 design production plus development-session integration/QA.

## Recommended next work

### Design P1

1. Affinity appearance visual candidates for `familiar / trusted / best_friend` within the safe matrix
2. A-3 five-screen component fit board using the approved source kit
3. Luca/Pino/Beri final-shipping review package (only if those companions are being considered for launch)
4. EVENING bridge study: interpolation-first comparison before deciding whether a dedicated board is necessary
5. App icon / Daily Town logo concept options after the core identity stabilizes

### Development-session handoff next

1. consume `visual-pack-manifest.v1.json`
2. add semantic asset/token resolver scaffolding
3. implement/verify forced MORNING/SUNSET/NIGHT screenshot states
4. execute the 18-capture map overlay QA matrix
5. prepare sprite-atlas playback adapter independently of sprite-gen
6. keep route/halo/discovery/A-3 motion native/procedural
