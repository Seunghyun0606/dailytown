# Daily Town Companion Family Asset Contract v1

Status: design/runtime handoff contract. No Android implementation is included.

## Purpose

Moru is the first companion, not the only shape supported by the product. This contract ensures future sub-characters, affinity variants, and cosmetic changes can be added without changing gameplay/domain semantics.

## Required companion-level definition

Each companion family defines:
- `companionId`
- canonical front / 3-quarter / side / back references
- silhouette lock
- supported expressions
- supported lighting families
- supported usage contexts
- modular appearance slots
- fallback assets

## Required usage contexts

Every companion should provide or intentionally fallback for:
- `map_avatar`
- `hud_portrait`
- `encounter_halfbody`
- `result_large`
- `journal_stamp`

## Core expression contract

Preferred shared semantic expression set:
- `neutral`
- `happy`
- `curious`
- `surprised`
- `clue_found`
- `resolved`

A future character may use a fallback map when anatomy or personality makes a literal expression unsuitable, e.g. `clue_found -> curious` plus a discovery effect. Semantic behavior remains stable even when visual expression assets differ.

## Lighting contract

Preferred shared families:
- `LIGHT`
- `WARM_DUSK`
- `DARK`

Lighting cannot alter canonical identity, face geometry, or silhouette. It is a presentation dimension layered above expression.

## Appearance slots

Common slots:
- `head_or_sprout_style`
- `neck_or_scarf_style`
- `bag_or_carried_item`
- `body_accent`
- `accessory`
- `context_effect`

A companion may mark a slot unsupported. Unsupported slots must resolve to `none` rather than requiring app logic changes.

## Variant boundary

Cosmetic variant:
- preserves canonical anatomy and main silhouette class
- preserves face geometry
- changes one or more appearance slots

Character variant:
- materially changes silhouette/anatomy while remaining narratively the same character
- gets its own canonical visual profile under the same companion identity if product-approved

Distinct companion:
- different identity, canonical shape, and companion id

## Affinity compatibility

Affinity stages may select appearance variants without becoming direct domain asset filenames.

Recommended semantic progression:
- `base`
- `familiar`
- `trusted`
- `best_friend`

A resolver should conceptually map:
`companionId + affinityStage + styleContext -> appearanceProfile`

AppearanceProfile then references modular semantic slots.

## Style-family consistency

Future companions should share:
- outline/edge softness family
- rendering/shading family
- map-avatar readability discipline
- eye/highlight contrast philosophy where anatomically relevant
- compatible lighting response

They do not need to share Moru's proportions or botanical anatomy.

## Silhouette diversity requirement

New companions should be distinguishable from Moru in black silhouette at compact scale. Diversity may come from:
- ear/head appendage profile
- torso proportion
- tail/back shape
- locomotion posture
- accessory mass if identity-locked

Color alone does not count as companion differentiation.

## Semantic key examples

```text
companion.moru.neutral
companion.moru.happy
companion.luca.neutral
companion.pino.curious

stamp.companion.moru.happy
stamp.companion.luca.resolved

appearance.moru.base
appearance.moru.familiar
appearance.pino.base
```

Exact implementation naming can change; the semantic dimensions must remain independent.

## Animation compatibility

Animated companions should preserve the same contract:
- animation state is a separate dimension from expression/identity
- atlas/frame data is replaceable
- static fallback is mandatory
- reduced-motion behavior is mandatory

Recommended first animation states:
- `idle_breathe`
- `walk`
- `investigate`
- `happy_bounce`
- `clue_react`
- `resolved_settle`

Locomotion is optional for contexts that only display portraits/stamps.

## Acceptance tests for a new companion design

- recognisable at 48 dp map-avatar context
- distinguishable from Moru in silhouette
- supports the six semantic emotion outcomes or documented fallbacks
- survives LIGHT/WARM_DUSK/DARK lighting
- does not require hard-coded screen-specific filenames
- affinity cosmetic replacement preserves identity
- has a static fallback for every animation-dependent usage
