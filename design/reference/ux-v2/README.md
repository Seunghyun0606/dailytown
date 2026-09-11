# DailyTown UX v2 reference mockups

Status: **approved UX hierarchy / six-screen structure baseline approved / visual polish in progress / Moru v2 Candidate 3 direction approved**.

This directory is the Figma-free repository source for the approved exploration UX in `docs/product/EXPLORATION_EXPERIENCE_SCENARIO_V2.md`.

## Repository-owned visual source

- `index.html` — editable browser-based six-screen structure/reference
- `manifest.json` — screen IDs, approval state, asset-policy notes, render metadata
- `render-sha256.json` — SHA-256 lock for the first generated review renders

The baseline covers:

- Explore · Prepare — map-first ready-to-go state
- Explore · Detect — hinted/unknown-signal state
- Explore · Discover — discovery reveal sheet
- Explore · Resolve — resolved/record/continue state
- Companion — relationship-notebook hierarchy
- Records — journal hierarchy

The user approved this six-screen composition/hierarchy as the implementation baseline on 2026-09-11. Do not reopen navigation or information architecture solely to polish the visuals.

## Visual polish status

The baseline structure is approved, but background art, discovery/place illustration, micro-detail, surface finish and final design tone continue as design work.

The active polish direction uses:

- warm real-neighborhood storybook atmosphere
- real provider map truth in runtime Explore
- botanical but restrained map HUD framing
- illustrated discovery reveal moments
- warm paper / field-notebook Companion and Records surfaces
- fewer visible generic Material 3 card stacks
- soft dimensional surfaces and handcrafted detail without hurting outdoor readability

Moru v2 Candidate 3 is now the approved character direction. See `docs/design/MORU_CANONICAL_V2.md` and `design/reference/moru-v2/`.

## Generated review renders

The initial structural source was rendered as 1080×2160 WebP review artifacts plus a combined board:

- `01_explore_prepare.webp`
- `02_explore_signal.webp`
- `03_explore_discovery.webp`
- `04_explore_resolved.webp`
- `05_companion_relationship.webp`
- `06_records_journal.webp`
- `00_ux_v2_reference_board.webp`

Their expected bytes are recorded in `render-sha256.json`. Large binary transfer must be checksum-verified before any repository copy is treated as authoritative.

Subsequent raster polish boards are tracked through `design/reference/ux-v2/polish-manifest.json` rather than silently replacing the baseline structure.

## Important status rule

The UX structure is approved and the Moru character direction is approved, but not every generated illustration is a runtime production asset.

- Moru reference art still needs production crops/export QA before runtime replacement.
- Discovery/place/background illustrations remain an active production family and must be raster PNG/WebP.
- The schematic maps in design boards are layout/tone references only; Android runtime must preserve the real map provider, attribution, location state and outdoor readability.

## Production handoff

Use `manifest.json` for baseline screen IDs and `polish-manifest.json` for later visual-polish references. UI implementation belongs in Jetpack Compose. True UI symbols may use vector assets. Characters, companion portraits, place/discovery art, clues, collectibles, mysteries and rewards remain raster PNG/WebP according to `docs/design/`.

Do not promote an entire concept board as one runtime bitmap. It defines composition, art direction, density, surface treatment and crop intent; runtime receives separately prepared game-art assets plus Compose UI.

## Figma-free workflow

`index.html` remains the editable structural source. Raster game art is produced and reviewed separately, checksum-locked in manifests, and promoted only after the relevant Human Gate and mobile QA. Figma is not a required dependency for continuing the current design/development flow.
