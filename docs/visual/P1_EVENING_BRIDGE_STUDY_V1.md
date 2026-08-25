# Daily Town P1 — EVENING Bridge Study v1

Status: candidate visual-strategy study. No new image board is approved yet.

## Goal

Determine whether `EVENING` needs a dedicated art board or can remain a controlled transition between the approved `SUNSET` and `NIGHT` systems.

Existing principles:

- semantic phases remain `DAWN / MORNING / MIDDAY / AFTERNOON / SUNSET / EVENING / NIGHT`
- core visual anchors already approved: `MORNING / SUNSET / NIGHT`
- marker families stay `DAY / DARK`; a seventh marker family is not introduced
- companion lighting families stay `LIGHT / WARM_DUSK / DARK`

## EV-1 — Interpolation-first bridge

`EVENING` is a controlled blend rather than an independent visual family.

Suggested visual behavior:

- early evening: SUNSET atmosphere decreases while WARM_DUSK remains dominant
- midpoint: deep-navy framing rises gradually; warm local points remain
- late evening: DARK lighting and DARK marker family take over
- route color interpolates from sunset route accent toward night route accent
- HUD surface moves toward night contrast without abrupt switch

Advantages:

- least asset duplication
- smooth real-world transition
- strongest semantic architecture
- simpler testing and replacement

Risks:

- midpoint can feel visually generic if the interpolation is purely numeric
- requires a few authored evening-specific decorative constraints so it does not look like a crossfade accident

Recommendation: **preferred baseline**.

## EV-2 — Soft Blue-Hour Accent

EVENING remains a bridge but receives one distinct blue-hour accent vocabulary.

Distinct additions:

- muted blue-lavender atmosphere accent
- warm window/lamp point emphasized against cooling sky
- companion remains WARM_DUSK early and DARK late
- no new marker semantics; DAY→DARK family transition still applies

Advantages:

- gives evening a memorable identity without a full separate system
- emotionally strong for neighborhood walks
- supports the transition from nostalgic sunset to quiet night

Risks:

- adds token/QA complexity
- can drift too purple or cinematic if over-stylized

Recommendation: **best fallback if EV-1 feels too generic in prototype**.

## EV-3 — Dedicated EVENING board

EVENING receives its own full composition board and authored atmosphere language.

Advantages:

- maximum art direction control
- useful if evening becomes a major content window

Risks:

- increases design and QA surface
- encourages accidental creation of extra marker/lighting families
- risks overfitting a transitional phase

Recommendation: **do not choose unless product/content behavior proves evening uniquely important**.

## Transition rules common to all options

- gameplay state does not change because of visual phase
- marker semantic key never changes with time
- geographic anchor never changes
- Moru remains one canonical character
- color is never the only state cue
- map-provider labels/legal UI remain unobstructed

## Proposed interpolation checkpoints for prototyping

These are visual QA checkpoints, not product-time boundaries:

- `E0`: SUNSET anchor
- `E1`: 25% transition
- `E2`: 50% transition
- `E3`: 75% transition
- `E4`: NIGHT anchor

At each checkpoint review:

- map readability
- marker family decision
- companion lighting family decision
- HUD contrast
- route visibility
- halo/discovery visibility

## Human selection required before new imagery

Choose:

- `EV-1` interpolation-first
- `EV-2` blue-hour bridge
- `EV-3` dedicated EVENING board

Recommended path: prototype `EV-1` first; only escalate to `EV-2` or `EV-3` if the bridge lacks identity or readability.