# Daily Town Companion Asset Manifest v1

Status: design-to-development handoff specification. No Android/Kotlin implementation is included.

## Goal

Define one replaceable semantic manifest that can describe Moru and future companions without hard-coded screen-specific asset filenames.

The manifest is the visual binding layer between companion design and application runtime. Gameplay/domain logic should only know semantic states such as companion id, expression, lighting, affinity stage, and usage context.

## Companion-level contract

Required fields per companion:

- `companion_id`
- `canonical_profile`
- `silhouette_profile`
- `expression_map`
- `lighting_map`
- `usage_context_map`
- `appearance_profiles`
- `animation_profiles`
- `fallbacks`
- `asset_version`

## Usage contexts

Required semantic usage contexts:

- `map_avatar`
- `hud_portrait`
- `encounter_halfbody`
- `result_large`
- `journal_stamp`

Optional contexts:

- `collection_icon`
- `memory_vignette`
- `notification_portrait`

A missing optional context must resolve to an explicit fallback context rather than requiring screen-specific logic.

## Expression map

Shared semantic expressions:

- `neutral`
- `happy`
- `curious`
- `surprised`
- `clue_found`
- `resolved`

A companion may map one semantic expression to another visual expression plus effect when personality/anatomy makes a direct rendition unsuitable.

Example:

```text
clue_found -> curious + effect.discovery.small
```

## Lighting map

Shared lighting families:

- `LIGHT`
- `WARM_DUSK`
- `DARK`

Lighting is independent from expression and identity.

## Appearance profiles

Recommended affinity stages:

- `base`
- `familiar`
- `trusted`
- `best_friend`

Each appearance profile resolves modular slots:

- `head_or_sprout_style`
- `neck_or_scarf_style`
- `bag_or_carried_item`
- `body_accent`
- `accessory`
- `context_effect`

Unsupported slots resolve to `none`.

## Static fallback rules

Every companion must have:

1. a static `neutral` asset
2. a static asset for every required usage context, either direct or fallback
3. a static fallback for every animation-dependent context
4. a neutral lighting fallback when a dedicated lighting treatment is unavailable

Recommended fallback order:

```text
requested expression + requested lighting
-> requested expression + neutral lighting
-> neutral expression + requested lighting
-> neutral expression + neutral lighting
-> canonical fallback asset
```

## Example semantic manifest

```json
{
  "schema_version": 1,
  "companion_id": "moru",
  "asset_version": "1.0.0",
  "canonical_profile": "companion.moru.canonical.a2",
  "expressions": {
    "neutral": "companion.moru.expression.neutral",
    "happy": "companion.moru.expression.happy",
    "curious": "companion.moru.expression.curious",
    "surprised": "companion.moru.expression.surprised",
    "clue_found": "companion.moru.expression.clue_found",
    "resolved": "companion.moru.expression.resolved"
  },
  "lighting": {
    "LIGHT": "companion.moru.lighting.light",
    "WARM_DUSK": "companion.moru.lighting.warm_dusk",
    "DARK": "companion.moru.lighting.dark"
  },
  "usage_contexts": {
    "map_avatar": "companion.moru.usage.map_avatar",
    "hud_portrait": "companion.moru.usage.hud_portrait",
    "encounter_halfbody": "companion.moru.usage.encounter_halfbody",
    "result_large": "companion.moru.usage.result_large",
    "journal_stamp": "stamp.companion.moru"
  },
  "appearance_profiles": {
    "base": "appearance.moru.base",
    "familiar": "appearance.moru.familiar",
    "trusted": "appearance.moru.trusted",
    "best_friend": "appearance.moru.best_friend"
  },
  "animation_profiles": {
    "idle_breathe": "animation.companion.moru.idle_breathe",
    "investigate": "animation.companion.moru.investigate",
    "happy_bounce": "animation.companion.moru.happy_bounce",
    "clue_react": "animation.companion.moru.clue_react",
    "resolved_settle": "animation.companion.moru.resolved_settle"
  }
}
```

## Compatibility validation set

The contract has been design-validated against four distinct silhouette classes:

- Moru: round sprout explorer
- Luca: tall-ear explorer
- Pino: leaf-ear compact companion
- Beri: rounded/spined thinker companion

Validation targets:

- black-silhouette distinction at compact scale
- six semantic emotion outcomes or documented fallbacks
- LIGHT/WARM_DUSK/DARK compatibility
- all five required usage contexts
- modular appearance slots
- A-3 journal stamp compatibility
- animation/static fallback independence

## Acceptance gates

A production companion manifest is accepted only when:

- no UI screen needs to know raw source filenames
- missing optional art resolves predictably
- canonical identity survives affinity cosmetics
- map avatar remains recognizable at target compact size
- each animation has a static fallback
- unsupported cosmetic slots resolve to `none`
- semantic expression meaning remains stable across companion personalities
