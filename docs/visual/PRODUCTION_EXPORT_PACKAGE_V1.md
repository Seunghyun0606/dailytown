# Daily Town Production Export Package v1

Status: design packaging specification. This document defines what a runtime-ready visual package must contain; it does not add Android resources or Kotlin code.

## Purpose

Bundle approved Daily Town visual assets into a replaceable, versioned, machine-readable package that development can consume without knowing concept-board filenames or design-tool internals.

Only assets with approval state `production_export` are runtime candidates.

## Package identity

Recommended package id:

```text
visual-pack-v1.0.0
```

Package-level semantic versioning:

- PATCH: export cleanup only
- MINOR: new backward-compatible assets/variants
- MAJOR: semantic contract or canonical identity change

## Required package contents

```text
visual-pack-v1.0.0/
  manifest.json
  tokens/
    colors.json
    surfaces.json
    typography.json
  companions/
    moru/
    luca/
    pino/
    beri/
  markers/
    day/
    dark/
  effects/
    discovery/
  animation/
    companion/
  a3/
    paper/
    stickers/
    clue/
    stamps/
    collection/
    memory/
  qa/
    checksums.sha256
    export-report.json
```

Luca/Pino/Beri directories may remain `candidate` until human character approval; their schema should still be valid.

## Manifest record contract

Every runtime candidate record should include:

```json
{
  "semantic_key": "companion.moru.expression.happy",
  "family": "companion",
  "context": "hud_portrait",
  "variant": "base",
  "filename": "companions/moru/hud/companion_moru_happy_base_128.png",
  "width": 128,
  "height": 128,
  "format": "png",
  "color_space": "sRGB",
  "anchor": null,
  "source_revision": "moru-a2-r1",
  "approval": "production_export",
  "fallback": "companion.moru.expression.neutral",
  "sha256": "..."
}
```

Fields may be extended, but semantic meaning must remain independent from the filename.

## Companion package requirements

Per approved companion:

### Canonical references

- front
- 3/4 front
- side
- back
- black silhouette reference

Canonical references are design/archive inputs and are not necessarily all runtime resources.

### Required runtime contexts

- `map_avatar`
- `hud_portrait`
- `encounter_halfbody`
- `result_large`
- `journal_stamp`

Optional:

- `collection_icon`
- `memory_vignette`
- `notification_portrait`

### Expression semantics

- neutral
- happy
- curious
- surprised
- clue_found
- resolved

Missing direct assets must use the companion manifest fallback map.

### Lighting compatibility

- LIGHT
- WARM_DUSK
- DARK

Lighting may be implemented as presentation treatment or approved derivative exports; package metadata must not force a Cartesian product of every state.

## Motion package requirements

Character sprite motion may include:

- idle_breathe
- investigate
- happy_bounce
- clue_react
- resolved_settle
- walk, only when human QA approves it

When sprite-gen is used, package curated output only:

```text
sprite-sheet-alpha.png
manifest.json
```

Design archive may additionally keep contact sheets/GIFs/curation metadata outside runtime resources.

Animation manifest requirements:

- explicit frame rectangles/list
- frame duration or fps
- loop flag
- anchor/pivot
- static fallback semantic key
- reduced-motion fallback semantic key

Runtime must not assume a fixed sprite grid when explicit frame layout is available.

## Marker package requirements

DAY and DARK families share semantic meaning.

Required keys:

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

Requirements:

- identical geographic pivot across visual variants of a semantic marker
- consistent canvas bounds within a family
- no provider-map raster content baked into assets
- state meaning remains readable without color

## Route and halo policy

Route and halo are primarily procedural/native visuals and therefore are represented in the package mainly by semantic tokens/spec metadata, not large raster animations.

Runtime reference keys:

```text
effect.route.idle
effect.route.following
effect.route.completed

effect.halo.idle
effect.halo.active
effect.halo.strong
```

Reference images may live in design QA archives but should not be treated as mandatory runtime geometry.

## Discovery package

Required semantic intensities:

- `effect.discovery.small`
- `effect.discovery.medium`
- `effect.discovery.big`

Recommended owned primitives:

- leaf particles
- sparkle particles
- center light accent

Motion timing remains separate from the visual asset identity.

## A-3 package

P0 owned assets:

- paper base/raised surface primitives
- paper grain/texture where used
- discovery sticker shells
- clue state icons/stamps
- companion stamp shells/exports
- memory completion seal
- collection locked/unknown pattern
- memory frame treatment

A-3 screens must remain structurally readable if decorative texture assets fail to load.

## Machine-readable color tokens

The package should expose semantic tokens rather than screenshot names.

Minimum roles:

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
status.info
status.success
status.alert
status.warning
status.mystery
phase.morning.route
phase.sunset.route
phase.night.route
```

A-3 additions:

```text
a3.paper.base
a3.paper.raised
a3.paper.shadow
a3.ink.primary
a3.ink.secondary
a3.accent.botanical
a3.accent.memory
a3.rule
```

## Approval-state rules

Allowed metadata states:

- `concept`
- `candidate`
- `approved_source`
- `production_export`
- `deprecated`

Runtime package release must reject `concept` and `candidate` assets from the production set.

## Checksums

Every production file should have SHA-256 recorded.

Package QA should detect:

- missing file
- checksum mismatch
- duplicate semantic key
- duplicate filename for different content
- missing fallback
- invalid dimensions/format metadata
- non-sRGB raster when sRGB is required

## Fallback registry

The package manifest should explicitly define fallbacks.

Examples:

```text
missing expression -> neutral
missing lighting -> LIGHT
missing cosmetic -> none/base
missing optional context -> nearest approved context derivative
missing animation -> static semantic pose
missing discovery intensity -> nearest lower approved intensity
```

A missing DARK marker must not be auto-generated by arbitrary recolor; it uses an approved fallback family/asset defined by design.

## Runtime boundary

Design package provides:

- semantic keys
- owned assets
- metadata
- fallback relationships
- approval/version/checksum information

Development owns:

- resource loading
- cache strategy
- Compose/renderer implementation
- sprite playback adapter
- map-provider adapter
- debug diagnostics

## Pre-release QA checklist

Before a package is handed to development:

- all required semantic keys are unique
- Moru A-2 canonical exports match identity lock
- companion alpha edges are clean
- marker pivots are consistent
- A-3 paper texture preserves text contrast
- animation has static/reduced-motion fallback
- no provider map screenshots exist in owned runtime assets
- checksum file is complete
- concept boards are excluded
- manifest schema version is recorded

## Human-gated release items

The package can be structurally assembled before these are approved, but release status remains partial where applicable:

- final Luca/Pino/Beri character approval if they are to ship
- final affinity appearance ceiling
- final motion timing/easing
- physical-device outdoor readability acceptance
- app icon/logo lock

Moru + approved baseline assets can advance independently of unapproved sub-character shipping decisions.
