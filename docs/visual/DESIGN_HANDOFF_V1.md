# Daily Town design handoff v1

This document is the design-to-development handoff for the first production visual sprint.

## Approved design

- Visual direction: `Option A · Soft Botanical Explorer`
- Moru canonical: `A-2 · balanced little explorer`
- Moru identity rule: one canonical character with replaceable expression, lighting, cosmetics, and effects
- Core expressions: `neutral / happy / curious / surprised / clue_found / resolved`
- Lighting families: `LIGHT / WARM_DUSK / DARK`
- Core time anchors: `MORNING / SUNSET / NIGHT`
- Map marker families: `DAY / DARK`
- Route states: `idle / following / completed`
- Halo states: `idle / active / strong`
- Discovery intensities: `small / medium / big`
- Journal/collection/memory surfaces: A-3 paper/storybook family, to be designed in the next UI sprint

## Production asset list

### Companion

- Moru canonical body master
- six expression masters
- map avatar derivative
- compact HUD portrait derivative
- encounter derivative
- result-large derivative
- journal-stamp derivative
- LIGHT lighting treatment
- WARM_DUSK lighting treatment
- DARK lighting treatment

### Modular appearance

- sprout slot
- scarf/cloth slot
- bag slot
- body accent slot
- accessory slot
- context-effect slot

### Map markers

DAY and DARK variants for:

- encounter hinted
- encounter discoverable
- encounter active
- encounter solved
- encounter revisit
- clue
- POI park
- POI culture
- POI landmark
- POI daily life
- POI nature
- POI other

### Effects

- route idle/following/completed
- halo idle/active/strong
- discovery small/medium/big

## Semantic asset keys

### Companion

```text
companion.moru.neutral
companion.moru.happy
companion.moru.curious
companion.moru.surprised
companion.moru.clue_found
companion.moru.resolved
```

### Lighting

```text
lighting.light
lighting.warm_dusk
lighting.dark
```

### Marker

```text
marker.encounter.hinted
marker.encounter.discoverable
marker.encounter.active
marker.encounter.solved
marker.encounter.revisit
marker.clue
marker.poi.park
marker.poi.culture
marker.poi.landmark
marker.poi.daily_life
marker.poi.nature
marker.poi.other
```

Marker family is resolved independently as `DAY` or `DARK`.

### Route / halo / discovery

```text
effect.route.idle
effect.route.following
effect.route.completed

effect.halo.idle
effect.halo.active
effect.halo.strong

effect.discovery.small
effect.discovery.medium
effect.discovery.big
```

## Color/token

Design source values are defined in `PRODUCTION_VISUAL_SYSTEM_V1.md`.

Important development rule:

- code consumes semantic color roles, not hard-coded art-board colors
- brand accent colors are not automatically text colors
- `brand.leaf.ink` / `text.ink` should be used for readable light-surface text rather than the softer leaf accent
- night UI uses `text.onDark`

## Required runtime states / variants

Companion appearance should resolve from independent dimensions:

```text
companionId
expression
lightingFamily
appearanceTier
styleVariant
accessoryVariant
contextEffect
```

Do not implement a Cartesian product of pre-baked filenames.

Marker appearance resolves from:

```text
semanticMarker
markerFamily(DAY/DARK)
selectionState
```

Time-of-day must not change gameplay semantic identity.

## Android development behavior required

The design session does not implement this code, but development should support:

1. **Semantic companion asset resolver**
   - companion id + expression + lighting + optional appearance slots
   - missing expression fallback to neutral
   - missing lighting fallback to LIGHT

2. **Replaceable companion architecture**
   - Moru must not be hard-coded as the only companion
   - future companions can have different silhouettes/anatomy
   - map/HUD/encounter/result screens request semantic companion contexts

3. **Appearance/affinity resolver**
   - affinity progression can select cosmetic slots
   - canonical anatomy remains independent unless a distinct character variant is explicitly selected

4. **Marker resolver**
   - semantic marker meaning + DAY/DARK family
   - no gameplay logic should depend on drawable filenames

5. **Time-of-day theme resolver**
   - phase selects visual tokens, lighting family, marker family, and map-theme intent
   - forced phase remains supported for screenshot/testing

6. **Effect state mapping**
   - route: idle/following/completed
   - halo: idle/active/strong
   - discovery: small/medium/big
   - reduced-motion path supported without changing semantics

7. **Map-provider neutrality**
   - Daily Town owns semantic overlay intent
   - provider-specific map styling remains inside map adapter
   - provider types must not leak into design/domain identifiers

8. **Asset fallback and diagnostics**
   - missing production asset should fall back visibly/safely rather than crash
   - debug tooling should allow identifying the resolved semantic key/family without exposing credentials

## Implementation constraints

- Do not package concept boards as runtime resources.
- Do not use concept screenshots as map backgrounds.
- Do not encode gameplay state only in color.
- Do not obstruct provider labels/legal/logo UI.
- Do not create seven complete marker families for seven day phases; use DAY/DARK plus phase tokens/effects.
- Do not create six expressions × seven phases as unrelated canonical character illustrations.
- Production assets remain replaceable at the semantic resolver layer.

## Human decisions still required

These are the only current design decisions intentionally left open:

1. **Affinity appearance ceiling**
   - how far Moru may visually evolve before the appearance should become a distinct character variant

2. **Motion timing/easing**
   - final pulse/crossfade/discovery animation timing after motion prototypes are reviewed

3. **Physical outdoor readability approval**
   - final acceptance on representative Android devices in bright daylight and night conditions

4. **EVENING dedicated board**
   - decide later whether it needs dedicated art or can remain a SUNSET→NIGHT bridge

5. **App icon / logo final lockup**
   - intentionally deferred until character and visual identity stabilize

## Not blocked by human decision

The following can proceed immediately in development/design production:

- semantic token model
- semantic asset registry/resolver
- forced day-phase support
- DAY/DARK marker integration scaffolding
- Moru six-expression slots
- lighting-family slots
- companion replacement architecture
- modular cosmetic slots
- fallback behavior
- screenshot test matrix
- production export manifest format

## DESIGN HANDOFF

- 승인된 디자인: Option A · Soft Botanical Explorer; Moru A-2 balanced little explorer; six expressions; LIGHT/WARM_DUSK/DARK; MORNING/SUNSET/NIGHT; DAY/DARK markers; route/halo/discovery state grammar
- 보류된 디자인: affinity appearance ceiling; final motion timing/easing; outdoor physical-device acceptance; optional EVENING board; final app icon/logo
- production asset 목록: canonical Moru + expression/lighting/context derivatives, modular appearance slots, DAY/DARK semantic marker family, route/halo/discovery assets
- semantic asset key: `companion.*`, `lighting.*`, `marker.*`, `effect.route.*`, `effect.halo.*`, `effect.discovery.*`
- 색상/token: `PRODUCTION_VISUAL_SYSTEM_V1.md` semantic palette
- 필요한 상태/variant: 6 expressions, 3 lighting families, DAY/DARK markers, 3 route states, 3 halo states, 3 discovery intensities, replaceable appearance slots
- Android 개발 시 필요한 동작: semantic resolvers, fallback, companion replacement, affinity cosmetic selection, forced phase, marker family selection, reduced-motion effect fallback
- 아직 사람이 결정해야 할 사항: five human gates listed above
