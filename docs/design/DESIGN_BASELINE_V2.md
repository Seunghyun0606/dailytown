# Daily Town — Design Baseline v2

> Status: **APPROVED BASELINE — 2026-09-11**
>
> This baseline is defined by the two exact user-confirmed raster references tracked in `design/reference/baseline-v2/manifest.json`.

## 1. Lock rule

The current DailyTown visual direction is no longer an exploration/candidate problem.

Use the approved references as the source for all follow-up design work. Do **not** generate, introduce, or substitute:

- another Moru character family
- another Moru costume/anatomy direction
- another illustration/rendering family
- another Explore screen composition direction
- another broad UI tone or brand treatment

unless the user explicitly reopens that decision.

Future work is **derivation and production**, not concept exploration.

## 2. Approved visual reference set

### A. Overall UX / visual baseline

Semantic repository target: `design/reference/baseline-v2/dailytown_ux_visual_baseline_v2.png`

SHA-256: `f0fb31e1ea4aec946bba356ff2d084cc6bdd8dd05119e1b350f547f382f4d515`

Dimensions: `1448 × 1086`

This image locks the intended visual read for:

- Explore · Prepare
- Explore · Detect
- Explore · Discover
- Explore · Resolved
- real-map-first composition
- warm botanical neighborhood framing
- cream/paper surfaces
- olive / moss / ochre / muted-brown UI accents
- storybook-like raster discovery art
- handwritten / field-journal decorative language
- compact Moru presence over the map
- discovery → record → continue flow

The image is a visual baseline, not a single runtime bitmap.

### B. Moru canonical baseline

Semantic repository target: `design/reference/baseline-v2/moru_candidate3_canonical_baseline_v2.png`

SHA-256: `541b53464bbc589d86323faa3a7dbc8790cf9ab9573e0dd66812986c4a430500`

Dimensions: `1122 × 1402`

This image is the canonical Moru Candidate 3 baseline and locks:

- front view
- 3/4 walking pose
- back view
- leaf hood / asymmetric two-leaf sprout
- warm scarf
- layered botanical cape/field-jacket treatment
- cross-body field satchel
- sturdy walking boots
- six semantic expressions: `neutral`, `happy`, `curious`, `surprised`, `clue_found`, `resolved`
- lighting families: `LIGHT`, `WARM_DUSK`, `DARK`
- affinity visual progression: `base`, `familiar`, `trusted`, `best_friend`
- 48 dp recognizability target

`docs/design/MORU_CANONICAL_V2.md` remains the detailed character contract, but this exact raster reference is the visual answer when wording and interpretation are ambiguous.

## 3. Production interpretation

The approved baseline does not mean that the composite boards themselves are runtime assets.

Production work should derive separate assets while preserving the baseline:

- transparent Moru crops / usage-context exports
- expression-specific character renders
- lighting derivatives
- affinity decoration derivatives
- discovery/place/background raster art in the same illustration family
- Compose UI surfaces matching the baseline layout, spacing, tone, shape, and hierarchy
- UI-only vector symbols where allowed by `ART_DIRECTION.md`

Do not approximate Moru or game-world art with SVG, VectorDrawable, or Compose Canvas.

## 4. Allowed polish vs prohibited redesign

Allowed without reopening the baseline:

- crop and export optimization
- spacing and type-rhythm correction
- shadow/stroke/elevation tuning
- mobile contrast tuning
- responsive layout adaptation
- exact icon cleanup for true UI symbols
- background/discovery/place art that matches the locked illustration family
- expression, lighting, affinity, and usage-context derivation from the locked Moru
- motion studies that keep anatomy and style unchanged

Requires explicit user approval because it changes the baseline:

- new Moru silhouette/anatomy/costume family
- materially different palette or rendering style
- replacement of botanical storybook tone
- major re-layout of approved Explore state hierarchy
- alternate navigation / product information architecture

## 5. Next design-production order

1. Moru production extraction and semantic export spec.
2. Explore screen-detail parity against the approved visual baseline.
3. Discovery/place/background raster asset family in the same locked style.
4. Companion relationship-notebook and Records A3 surface refinement without changing hierarchy.
5. Mobile-size / 48 dp / outdoor readability QA.
6. M-B motion prototype using the same canonical Moru.
7. ID-A app-icon/logo final lock after the in-app visual family is stable.

No new concept-board exploration is part of this sequence.