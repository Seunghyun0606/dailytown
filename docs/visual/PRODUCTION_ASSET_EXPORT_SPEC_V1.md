# Daily Town production asset export specification v1

Status: production design specification

This document defines replaceable export rules for the approved Option A visual system and Moru A-2 canonical direction. It does not bind Android runtime implementation.

## 1. Asset classes

### Companion raster/vector masters

Use semantic source masters and export only approved runtime derivatives.

Recommended source classes:

- canonical body master
- expression master
- lighting treatment master
- cosmetic/accessory master
- context effect master

Preferred editable source:

- vector where practical for simple shapes/icons
- layered raster for painterly companion illustrations

Runtime export:

- PNG with transparency for companion illustration where alpha quality matters
- WebP may be used after visual validation if alpha edges remain clean
- vectors for simple icons/markers where the approved shape can be preserved exactly

## 2. Canonical companion export contexts

Each companion should support these semantic contexts:

| Context | Purpose | Target behavior |
| --- | --- | --- |
| `map_avatar` | map companion/user marker | strongest silhouette, minimal detail |
| `hud_compact` | compact HUD portrait | face/expression priority |
| `encounter` | encounter card/scene | half/full body allowed |
| `result_large` | resolution/celebration | largest expressive treatment |
| `journal_stamp` | memory/journal sticker | simplified sticker-like export |

Suggested raster target sizes are implementation-independent design targets, not Android density buckets:

- `map_avatar`: 96 px source minimum
- `hud_compact`: 128 px source minimum
- `encounter`: 512 px source minimum
- `result_large`: 1024 px source minimum
- `journal_stamp`: 256 px source minimum

Maintain master artwork at higher resolution than the largest runtime derivative.

## 3. Transparent padding and safe area

Companion assets:

- preserve transparent padding around sprout, hands, bag, and effects
- no important silhouette element may touch export bounds
- recommended minimum clear padding: 6% of canvas width/height
- result effects may exceed body safe area only when exported as separate effect layers

Marker assets:

- use consistent outer bounds for all semantic markers in one family
- map anchor point must stay identical for all state variants
- bottom coordinate anchor is the canonical location point unless a later map adapter requires an explicit pivot value

## 4. Moru canonical source layers

Recommended source structure:

```text
moru/
  canonical/
    body/
    face/
    sprout/
  expressions/
    neutral/
    happy/
    curious/
    surprised/
    clue_found/
    resolved/
  lighting/
    light/
    warm_dusk/
    dark/
  cosmetics/
    scarf/
    bag/
    body_accent/
    accessory/
  effects/
```

The source file may use different internal layer names, but exported semantic identity must match these roles.

## 5. Semantic companion keys

Base pattern:

```text
companion.{companionId}.{expression}
```

Examples:

```text
companion.moru.neutral
companion.moru.happy
companion.moru.curious
companion.moru.surprised
companion.moru.clue_found
companion.moru.resolved
```

Lighting family is a separate selector:

```text
lighting.light
lighting.warm_dusk
lighting.dark
```

Appearance/cosmetic selectors remain separate:

```text
appearance.base
appearance.familiar
appearance.trusted
appearance.best_friend

slot.sprout.{variant}
slot.scarf.{variant}
slot.bag.{variant}
slot.body_accent.{variant}
slot.accessory.{variant}
```

## 6. Runtime filename convention

Filenames are implementation details and must never become gameplay/domain identifiers.

Recommended export naming:

```text
{category}_{subject}_{semantic}_{variant}_{size}.{ext}
```

Examples:

```text
companion_moru_neutral_base_512.png
companion_moru_happy_base_128.png
marker_day_encounter_active_96.png
marker_dark_poi_park_96.png
effect_discovery_big_256.png
```

Use lowercase ASCII and underscores only for exported filenames.

## 7. Marker export specification

Required DAY and DARK families share identical semantic keys.

Recommended master canvas:

- square source canvas
- centered marker body
- common coordinate anchor
- consistent optical size across all marker categories

Required semantic exports:

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

Each semantic marker may have:

- base/normal
- selected/active emphasis where required
- disabled/locked only if product flow later requires it

Do not generate state variants that are not used by the product.

## 8. Route assets

Prefer vector or runtime-drawn primitives where possible.

Semantic states:

```text
effect.route.idle
effect.route.following
effect.route.completed
```

Design exports, if raster samples are needed, are reference-only. Production route geometry should remain adaptable to arbitrary path geometry.

## 9. Halo assets

Prefer parametric/vector treatment rather than large raster sprites.

Semantic states:

```text
effect.halo.idle
effect.halo.active
effect.halo.strong
```

If raster fallback is necessary:

- export centered square alpha assets
- preserve precise center
- avoid baked background color
- do not include route/marker graphics in the halo texture

## 10. Discovery effect exports

Semantic intensity:

```text
effect.discovery.small
effect.discovery.medium
effect.discovery.big
```

Recommended layer split:

- leaf particles
- sparkle particles
- center light point

If animation is later authored, keep effect identity independent of timing/easing.

## 11. Time-of-day atmosphere assets

Time-of-day should primarily use tokens and overlays, not full-screen baked map screenshots.

Allowed production assets:

- subtle transparent atmosphere overlays
- lightweight texture/noise overlays if needed
- approved decorative HUD accents

Forbidden as runtime production assets:

- concept map screenshots
- full illustrated replacement maps
- rasterized provider map labels/roads

## 12. Color token export

Design token source-of-truth should be machine-readable eventually, but this document remains the visual specification.

Token names must use semantic roles, e.g.:

```text
brand.leaf.primary
brand.leaf.secondary
brand.leaf.ink
brand.butter
brand.warmPeach
brand.sky
brand.deepNavy
surface.white
surface.ivory
surface.paper
text.ink
text.onDark
status.success
phase.morning.route
phase.sunset.route
phase.night.route
```

Do not name tokens after screenshots or art-board filenames.

## 13. Versioning

Production asset packs use semantic versioning at the visual package level:

```text
visual-pack-v1.0.0
```

Increment guidance:

- PATCH: export cleanup without semantic/visual change
- MINOR: new replaceable variants/assets, backward compatible
- MAJOR: canonical identity, semantic key, or state contract changes

Individual source files should also carry revision metadata where the design tool supports it.

## 14. Checksums and manifests

Each approved production export bundle should include a manifest containing:

- semantic key
- filename
- asset family
- source revision
- exported dimensions
- checksum
- approval state

Example conceptual record:

```text
semanticKey: companion.moru.happy
file: companion_moru_happy_base_512.png
family: companion
sourceRevision: moru-a2-r1
size: 512x512
approval: approved
checksum: SHA-256
```

## 15. Approval states

Every visual artifact belongs to one of:

- `concept`
- `candidate`
- `approved_source`
- `production_export`
- `deprecated`

Only `production_export` assets may be copied into Android runtime resources.

## 16. Fallback policy

Every semantic family must define a safe fallback.

Examples:

- missing companion expression -> `neutral`
- missing lighting family -> `LIGHT`
- missing cosmetic slot -> base/no cosmetic
- missing marker dark asset -> approved high-contrast fallback, not arbitrary recoloring
- missing discovery intensity -> nearest lower approved intensity

Fallback behavior belongs in development, but the design contract must make fallback relationships explicit.

## 17. Production quality checks

Before an export becomes `production_export`:

- transparent edges are clean
- silhouette matches canonical source
- facial proportions remain unchanged
- color profile is sRGB
- no baked provider map content exists in owned assets
- no accidental background pixels
- no text is embedded unless intentionally illustration-specific
- marker anchor is consistent
- semantic key is unique
- checksum is recorded

## 18. Human-gated items

The export system can proceed without further decisions except:

- exact affinity visual-change ceiling
- final motion timing/easing
- final physical-device readability approval
- app icon/logo final lock

Those gates do not block creation of the semantic asset system itself.