# DailyTown UX v2 reference mockups

Status: approved UX hierarchy / visual treatment candidate / Moru canonical v2 art pending Human Gate.

This directory is the Figma-free repository source for the approved exploration UX in `docs/product/EXPLORATION_EXPERIENCE_SCENARIO_V2.md`.

## Repository-owned visual source

- `index.html` — editable browser-based six-screen visual reference
- `manifest.json` — screen IDs, approval state, asset-policy notes, render metadata
- `render-sha256.json` — SHA-256 lock for the generated full-resolution review renders

The visual source covers:

- Explore · Prepare — map-first ready-to-go state
- Explore · Detect — hinted/unknown-signal state
- Explore · Discover — discovery reveal sheet
- Explore · Resolve — resolved/record/continue state
- Companion — relationship-notebook hierarchy
- Records — journal hierarchy

## Generated review renders

The source has also been rendered as the following 1080×2160 WebP review artifacts plus a combined board:

- `01_explore_prepare.webp`
- `02_explore_signal.webp`
- `03_explore_discovery.webp`
- `04_explore_resolved.webp`
- `05_companion_relationship.webp`
- `06_records_journal.webp`
- `00_ux_v2_reference_board.webp`

Their exact bytes are recorded in `render-sha256.json`. Large binary transfer must be checksum-verified before any GitHub commit; do not accept a silently truncated/corrupted binary as the design source. Until a safe binary transfer is available, `index.html` is the complete repository-owned visual reference rather than a text-only prose substitute.

## Important status rule

The UI hierarchy is approved, but the raster game-art slots are not final production art. `Moru` and discovery/place illustration areas intentionally show labeled raster-art slots so the development session does not invent SVG/VectorDrawable/Compose-Canvas substitutes while Moru canonical v2 remains pending.

The schematic map shown in this reference is only a layout stand-in. Runtime Explore must preserve the real provider map, attribution, location state, marker semantics, and outdoor readability.

## Production handoff

Use `manifest.json` for screen IDs and status. UI implementation belongs in Jetpack Compose. True UI symbols may use vector assets. Characters, companion portraits, place/discovery art, clues, collectibles, mysteries and rewards remain raster PNG/WebP according to `docs/design/`.

Do not promote the reference renders as runtime production game-art assets. They define composition, hierarchy, density, and visual surface treatment for T5 implementation and review.

## Figma-free workflow

`index.html` intentionally uses explicit `RASTER ART SLOT` placeholders for non-UI game art. It is the editable layout/surface source and can be reviewed in a browser without making Figma a project dependency. Moru/discovery production art is added separately only after the relevant raster-art Human Gate.