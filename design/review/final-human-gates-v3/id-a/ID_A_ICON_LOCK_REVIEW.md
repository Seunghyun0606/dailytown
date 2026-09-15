# ID-A App Icon / Logo Human Gate Review

Status: **HUMAN APPROVED / LOCKED — ID-A2 SPROUT GATE**

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

## Human decision · 2026-09-15

The recommended **ID-A2 Sprout Gate** candidate is approved and locked as the DailyTown app identity mark.

Locked design masters:

- light foreground: `candidates/id-a2-sprout-gate/foreground-light.svg`
- dark foreground: `candidates/id-a2-sprout-gate/foreground-dark.svg`
- light background: `candidates/id-a2-sprout-gate/background-light.svg`
- dark background: `candidates/id-a2-sprout-gate/background-dark.svg`
- monochrome: `candidates/id-a2-sprout-gate/monochrome.svg`

Launcher identity is **mark-only** for now. `W1 Horizontal` and `W2 Field-note` remain optional future brand lockups and do not block Android app-icon production. No final typeface is locked by this decision.

`ID-A1 Sprout Trail` and `ID-A3 Town Path` remain comparison history only.

## Android adaptive-icon frame

The locked handoff uses:

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

Disposition: comparison history.

## Candidate A2 — Sprout Gate — LOCKED

Visual idea: a compact town/door arch forms the base; the sprout grows from the roof and the doorway doubles as the path entrance.

Strengths:

- clearest `town` cue
- balanced central mass for adaptive masks
- robust 24–32 px silhouette
- strong wordmark lockup compatibility

Risk:

- slightly more architectural and less free-flowing than A1.

Decision: selected because it encodes both halves of the DailyTown identity without using Moru.

## Candidate A3 — Town Path

Visual idea: a low neighborhood roofline anchors the mark while a centered path rises into the sprout.

Strengths:

- strongest place/neighborhood feeling
- visually connected to DailyTown's walk → place → memory loop
- distinctive at medium launcher sizes

Risk:

- highest detail density; needs careful small-size check at 24–32 px.

Disposition: comparison history.

## Light / dark / monochrome

The selected ID-A2 mark must preserve the same identity in all three:

- Light: warm paper background + moss/leaf + ochre path.
- Dark: dark olive/ink background + warm cream mark + restrained ochre cue.
- Monochrome: single filled/stroked shape with no semantic loss when Android themes the icon.

Do not rely on the ochre path color alone to communicate path/town identity.

## Small-size gate

Review at minimum during runtime resource production:

- 48 px: full mark should be immediately recognizable.
- 32 px: two leaves and the path/door cue must remain distinct.
- 24 px: it is acceptable to lose interior micro-detail, but the mark must not collapse into an indistinct blob.

Fail if:

- one leaf disappears,
- the path closes into an accidental letter/symbol,
- the town cue becomes visually confused with Moru's hood,
- foreground touches unsafe mask edges,
- monochrome loses the path/town cue.

## Wordmark / lockup

Launcher icon is locked to **mark-only**.

For a future store/landing/contextual brand system, the following remain non-blocking options:

- `W1 Horizontal`: mark + `Daily Town` to the right.
- `W2 Field-note`: mark above `Daily Town`.

The candidate sheet uses a system-font placeholder only. This Human Gate does not lock a final typeface.

## Development handoff

Development may package **ID-A2 Sprout Gate only** into Android adaptive-icon resources, preserving the 108×108 layers, centered 66×66 safe zone, light/dark foreground-background separation, and monochrome identity.

The design Human Gate for ID-A is closed. Runtime resource generation/binding and launcher QA remain Development-owned.
