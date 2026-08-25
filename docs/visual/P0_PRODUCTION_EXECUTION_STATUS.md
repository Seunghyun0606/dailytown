# Daily Town P0 production execution status

Status date: 2026-08-25

This document tracks the immediately executable P0 visual-production work. It contains no Android/Kotlin implementation.

## Completed in this execution pass

### P0-1 Machine-readable visual package — started / contract files created

Created under `design/export-spec/`:

- `visual-tokens.v1.json`
- `moru-production-manifest.v1.json`
- `marker-manifest.v1.json`
- `effect-manifest.v1.json`
- `a3-ui-asset-manifest.v1.json`
- `map-overlay-qa-matrix.v1.json`
- `visual-pack-manifest.v1.json`

Existing:

- `companion-manifest.example.json`

The package now has machine-readable source contracts for tokens, Moru usage contexts, DAY/DARK markers, native effects, A-3 components, and the 18-capture map QA matrix.

### P0-2 Moru production asset split — production manifest ready

The actual art exports are not yet marked `production_export`, but the required export contexts are locked:

- `map_avatar`
- `hud_portrait`
- `encounter_halfbody`
- `result_large`
- `journal_stamp`

Expressions:

- neutral
- happy
- curious
- surprised
- clue_found
- resolved

Lighting:

- LIGHT
- WARM_DUSK
- DARK

Next art-production action: export canonical A-2 source derivatives against this manifest and attach SHA-256 records only after visual QA.

### P0-4 DAY/DARK marker production — manifest ready

Twelve semantic markers are fixed for DAY/DARK family production. State meaning must use silhouette/icon/outline/badge-or-ring, never hue alone.

Next art-production action: create the actual approved vector/raster source masters while preserving a common geographic anchor.

### P0-5 A-3 production UI asset kit — manifest ready

The reusable kit contract is fixed for:

- paper surfaces
- discovery sticker
- clue unresolved/resolved cards
- companion stamp shell
- memory resolved stamp
- locked collection pattern

Next art-production action: create reusable source masters, then validate the five P0 screens against them.

### P0-6 Map Overlay QA — machine-readable matrix ready

Baseline matrix:

`3 time anchors × 3 map-complexity classes × 2 motion modes = 18 captures`

Physical outdoor acceptance remains a human gate.

### P0-7 Handoff package — structure ready

`visual-pack-manifest.v1.json` now declares package contents, semantic groups, approval states, fallback requirement, checksum requirement, and human gates.

## Not yet production-exported

The following are intentionally not called production assets yet:

- Moru raster/vector derivatives
- DAY/DARK marker files
- A-3 paper/sticker/card/stamp files
- sprite-gen Moru pilot atlas

They require actual authored source artwork plus visual QA before promotion from `approved_source` or `candidate` to `production_export`.

## Next executable design-production batch

No additional product decision is needed to start these source assets because Option A and Moru A-2 are already approved:

1. Moru A-2 export master board for the five usage contexts
2. Moru six-expression production source set
3. DAY/DARK marker source master set
4. A-3 reusable source asset kit
5. Moru sprite-gen pilot preparation using approved canonical source (`idle_breathe`, `clue_react`, `resolved_settle`)

The final sprite-gen motion output remains human-QA gated and `walk` remains experimental.

## Human gates unchanged

- Luca/Pino/Beri shipping approval
- affinity Best Friend transformation ceiling
- final motion timing/easing/intensity
- physical outdoor readability approval
- EVENING dedicated board decision
- app icon/logo final lock
