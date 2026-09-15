# ID-A App Icon / Logo Human Gate Review

Status: **CANDIDATES READY / HUMAN SELECTION PENDING**

## Direction lock

ID-A remains a **two-leaf sprout + path / town cue** identity. Moru's face/body is explicitly excluded from the app logo so the product identity can outlive one companion while still sharing DailyTown's botanical exploration language.

Palette is derived from the approved DailyTown surface system:

- paper canvas `#F4EBDD`
- paper card `#FFF8EB`
- ink primary `#453B30`
- botanical moss `#68704E`
- botanical leaf `#87906A`
- ochre accent `#B18B45`
- dark context `#2D342C`

Candidate sheet: `id-a-candidate-sheet.svg`

Candidate layer masters:

- `candidates/id-a1-sprout-trail/` — light foreground / dark foreground / light background / dark background / monochrome
- `candidates/id-a2-sprout-gate/` — light foreground / dark foreground / light background / dark background / monochrome
- `candidates/id-a3-town-path/` — light foreground / dark foreground / light background / dark background / monochrome

These are explicit 108×108 light/dark design-layer SVG masters for Human Gate comparison, not Android resources.

## Android adaptive-icon frame

Review uses current Android adaptive-icon geometry:

- layer canvas: 108 × 108 dp
- guaranteed safe zone: centered 66 × 66 dp
- outer 18 dp on each side reserved for masks/effects
- core logo target: 48–66 dp inside the safe zone
- color version must separate foreground and background layers
- monochrome layer must remain recognizable without relying on palette

No Android XML/VectorDrawable resource is added on this branch; these dimensions are design handoff constraints only.

## Candidate A1 — Sprout Trail

Visual idea: two leaves are the strongest silhouette; the stem flows directly into a single walking trail.

Strengths:

- fastest botanical recognition
- lowest small-size complexity
- strongest exploration/path cue
- clean monochrome behavior

Risk:

- town cue is subtle; can read as a generic nature/trail app.

Recommended if DailyTown should feel primarily like an **exploration product**.

## Candidate A2 — Sprout Gate

Visual idea: a compact town/door arch forms the base; the sprout grows from the roof and the doorway doubles as the path entrance.

Strengths:

- clearest `town` cue
- balanced central mass for adaptive masks
- robust 24–32 px silhouette
- strong wordmark lockup compatibility

Risk:

- slightly more architectural and less free-flowing than A1.

Recommended default Human Gate candidate because it encodes both halves of the name without using Moru.

## Candidate A3 — Town Path

Visual idea: a low neighborhood roofline anchors the mark while a centered path rises into the sprout.

Strengths:

- strongest place/neighborhood feeling
- visually connected to DailyTown's walk → place → memory loop
- distinctive at medium launcher sizes

Risk:

- highest detail density; needs careful small-size check at 24–32 px.

Recommended if the product should feel primarily like **neighborhood discovery**.

## Light / dark / monochrome

Human review must confirm the selected mark in all three:

- Light: warm paper background + moss/leaf + ochre path.
- Dark: dark olive/ink background + warm cream mark + restrained ochre cue.
- Monochrome: single filled/stroked shape with no semantic loss when Android themes the icon.

Do not rely on the ochre path color alone to communicate path/town identity.

## Small-size gate

Review at minimum:

- 48 px: full mark should be immediately recognizable.
- 32 px: two leaves and the path/door cue must remain distinct.
- 24 px: it is acceptable to lose interior micro-detail, but the mark must not collapse into an indistinct blob.

Fail if:

- one leaf disappears,
- the path closes into an accidental letter/symbol,
- the town cue becomes visually confused with Moru's hood,
- foreground touches unsafe mask edges,
- monochrome loses the path/town cue.

## Wordmark / lockup candidates

Launcher icon should remain mark-only. For store/landing/contextual branding, compare only after selecting the mark:

- `W1 Horizontal`: mark + `Daily Town` to the right; calm utility/default lockup.
- `W2 Field-note`: mark above `Daily Town`; centered editorial/store treatment.

The sheet uses a system-font placeholder only. It does **not** lock a final typeface. Long Korean text remains app typography, not handwritten display lettering.

## Human decision

Choose one mark: `ID-A1`, `ID-A2`, or `ID-A3`.

Optional second choice: `W1`, `W2`, or `mark-only for now`.

No candidate is final until explicitly selected.
