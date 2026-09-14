# Daily Town design handoff v2

This document supersedes the first-sprint status summary with the current production visual-system handoff state. It contains no Android/Kotlin implementation.

## Approved / established design system

- Visual direction: `Option A · Soft Botanical Explorer`
- Moru canonical: `A-2 · balanced little explorer`
- Core expressions: `neutral / happy / curious / surprised / clue_found / resolved`
- Lighting families: `LIGHT / WARM_DUSK / DARK`
- Core time anchors: `MORNING / SUNSET / NIGHT`
- Marker families: `DAY / DARK`
- Route: `idle / following / completed`
- Halo: `idle / active / strong`
- Discovery: `small / medium / big`
- A-3 record surface: stable `Journal / Collection / Memory` storybook/paper family
- Companion architecture: replaceable companion family contract + semantic manifest
- Motion architecture: sprite-gen for offline companion-motion authoring candidates; native/procedural motion for route/halo/general UI

## Companion family validation

The common contract has been design-validated against:

- Moru — round sprout explorer
- Luca — tall-ear explorer
- Pino — leaf-ear compact companion
- Beri — rounded/spined thinker companion

Validated dimensions:

- distinct compact silhouette class
- six semantic emotions or documented fallback
- LIGHT/WARM_DUSK/DARK compatibility
- map avatar / HUD portrait / encounter halfbody / result large / journal stamp contexts
- modular affinity appearance slots
- A-3 stamp compatibility
- static fallback for animation-dependent contexts

Luca/Pino/Beri remain candidate companion designs until final human character approval.

## A-3 UI system

High-fidelity specification exists for:

1. Journal Home
2. Discovery Detail
3. Clue Note
4. Collection Grid
5. Memory Detail

Core rule:

- record surfaces remain stable across time-of-day
- original event lighting may remain inside hero artwork
- status never depends on color alone
- paper texture/decorations are removable without breaking hierarchy

## Motion / sprite-gen decision

Use `sprite-gen` consistently as the tooling name.

Decision:

- optional offline authoring + curation pipeline for companion sprite animation
- not an Android runtime dependency
- first Moru pilot: `idle_breathe`, `clue_react`, `resolved_settle`
- then `investigate`, `happy_bounce`
- `walk` remains experimental and human-QA gated

Native/procedural animation remains preferred for:

- route progression
- active halo
- discovery expansion/fade shell
- A-3 page/card transitions
- sticker settle
- token/color transitions

Every animated state requires static and reduced-motion fallback.

## Map overlay QA

Design QA now defines an 18-capture baseline matrix:

```text
3 time anchors
× 3 map-complexity classes
× 2 motion modes
= 18 baseline captures
```

Required visual stack includes provider map, companion avatar, encounter/POI markers, following route, halo, discovery effect, and HUD.

Final outdoor physical-device acceptance remains human-gated.

## Affinity appearance

Default safe progression is defined:

- `base`: canonical appearance
- `familiar`: one small detail/accent
- `trusted`: up to two coordinated cosmetic slots
- `best_friend`: coordinated profile while preserving canonical anatomy and primary silhouette

Safe slots:

- head/sprout detail
- neck/scarf
- bag/carried item
- body accent
- accessory
- context effect

Canonical anatomy changes require a character-variant decision rather than a cosmetic swap.

The final maximum transformation ceiling at `best_friend` remains a human decision.

## Production export package

A runtime-oriented visual package structure is defined with:

- `manifest.json`
- machine-readable semantic tokens
- companion/marker/A-3/effect assets
- optional approved sprite atlases
- fallback relationships
- SHA-256 checksums
- approval-state metadata

Only `production_export` assets may enter runtime resources.

Candidate/concept boards are explicitly excluded.

## Semantic asset groups

```text
companion.*
appearance.*
animation.companion.*
lighting.*
marker.*
effect.route.*
effect.halo.*
effect.discovery.*
surface.journal.*
surface.collection.*
surface.memory.*
stamp.companion.*
sticker.discovery.*
card.clue.*
```

Code should consume semantic roles and resolvers, never design filenames.

## Android development behaviors required

The development session should support:

1. semantic companion resolver
2. companion replacement without Moru hard-coding
3. usage-context derivatives
4. expression + lighting fallback
5. affinity appearance-profile resolver
6. DAY/DARK marker resolver
7. time-of-day visual/theme resolver
8. animation semantic-key resolver with static/reduced-motion fallback
9. explicit atlas frame-layout playback when sprite assets are used
10. screenshot/debug controls for forced phase/state/motion mode
11. map-provider-neutral semantic overlays
12. manifest/checksum/fallback diagnostics as appropriate

No Android implementation is performed by this design session.

## Current production status

### Ready as design contract

- Option A visual system
- Moru A-2 canonical contract
- six-expression semantic set
- lighting families
- DAY/DARK marker grammar
- route/halo/discovery state grammar
- companion family contract
- companion semantic manifest
- A-3 UI information/visual system
- motion production specification
- sprite-gen tooling boundary
- map overlay QA specification
- affinity appearance matrix default safe range
- production export-package contract

### Candidate / requires art production or QA

- final individual production image exports
- curated Moru sprite-gen pilot atlas
- final Luca/Pino/Beri production masters if selected for shipping
- physical-device map screenshots/field QA results

## Human decisions still required

1. **Luca/Pino/Beri final character approval** if they are intended to ship
2. **Affinity appearance ceiling** at high friendship
3. **Final motion timing/easing/intensity** after prototypes are visible
4. **Physical outdoor readability approval** on representative devices
5. **EVENING dedicated board** vs SUNSET→NIGHT bridge
6. **App icon / Daily Town logo final lockup**

None of these block the semantic architecture, manifest, fallback, or export-package work.

## DESIGN HANDOFF

- 승인된 디자인: Option A; Moru A-2; 6 emotions; LIGHT/WARM_DUSK/DARK; MORNING/SUNSET/NIGHT; DAY/DARK marker grammar; route/halo/discovery grammar; A-3 Journal/Collection/Memory system; companion family contract
- 보류된 디자인: Luca/Pino/Beri shipping approval; affinity ceiling; final motion values; outdoor physical-device acceptance; optional EVENING board; app icon/logo
- production asset 목록: Moru canonical/context derivatives; candidate Luca/Pino/Beri derivatives; A-3 paper/sticker/clue/stamp assets; DAY/DARK markers; discovery primitives; approved companion sprite atlases where produced
- semantic asset key: `companion.*`, `appearance.*`, `animation.companion.*`, `lighting.*`, `marker.*`, `effect.*`, `surface.*`, `stamp.*`, `sticker.*`, `card.clue.*`
- 색상/token: Option A semantic palette + A-3 paper/ink/accent roles, machine-readable package planned
- 필요한 상태/variant: 6 expressions; 3 lighting families; DAY/DARK; route 3; halo 3; discovery 3; affinity 4 stages; static/reduced-motion fallback
- Android 개발 시 필요한 동작: semantic resolvers; replaceable companion/context/appearance binding; time/marker family selection; atlas playback adapter; native procedural motion; screenshot QA controls; fallback/diagnostics
- 아직 사람이 결정해야 할 사항: six human gates listed above
