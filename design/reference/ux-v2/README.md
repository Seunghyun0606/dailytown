# DailyTown UX v2 reference mockups

Status: approved UX hierarchy / visual treatment candidate / Moru canonical v2 art pending Human Gate.

These WebP renders are the Figma-free design reference for the approved exploration UX in `docs/product/EXPLORATION_EXPERIENCE_SCENARIO_V2.md`.

## Included screens

- `01_explore_prepare.webp` — map-first ready-to-go state
- `02_explore_signal.webp` — hinted/unknown-signal state
- `03_explore_discovery.webp` — discovery reveal sheet
- `04_explore_resolved.webp` — resolved/record/continue state
- `05_companion_relationship.webp` — relationship-notebook hierarchy
- `06_records_journal.webp` — journal hierarchy
- `00_ux_v2_reference_board.webp` — review board combining all six

## Important status rule

The UI hierarchy is approved, but the raster game-art slots are not final production art. `Moru` and discovery/place illustration areas intentionally show labeled raster-art slots so the development session does not invent SVG/VectorDrawable/Compose-Canvas substitutes while Moru canonical v2 remains pending.

The schematic map shown in these references is only a layout stand-in. Runtime Explore must preserve the real provider map, attribution, location state, marker semantics, and outdoor readability.

## Production handoff

Use `manifest.json` for screen IDs and status. UI implementation belongs in Jetpack Compose. True UI symbols may use vector assets. Characters, companion portraits, place/discovery art, clues, collectibles, mysteries and rewards remain raster PNG/WebP according to `docs/design/`.

Do not promote these reference WebP renders as runtime production game-art assets. They define composition, hierarchy, density, and visual surface treatment for T5 implementation and review.

## Figma-free source

`index.html` is the editable review source for layout/surface treatment. It intentionally uses explicit `RASTER ART SLOT` placeholders for non-UI game art. The source is suitable for browser review and implementation reference without making Figma a project dependency.

If binary reference renders cannot be transferred through an integration safely, `index.html` + `manifest.json` remain the repository-owned visual reference and the review renders are hash-locked separately rather than silently regenerated as vector/game art.