# Daily Town — Figma-free Raster Design Workflow v2 Candidate

Status: candidate workflow for the current design rebaseline. This document does not approve or promote any production asset.

## Why this exists

Figma usage is constrained, while the current Daily Town design rules require non-UI game art to be raster-first. The design workflow therefore must not depend on Figma for character, companion, background, collectible, reward, mystery, or place-art production.

## Source-of-truth split

- GitHub `main` `/docs/design/` remains authoritative for art direction, asset classification, production rules, and quality gates.
- Notion Daily Town project remains authoritative for Human Gates, project state, decisions, and next actions.
- Figma is optional review/layout tooling, not a required production source.

## Production path

### Non-UI game art

Use raster-oriented image production from the start.

1. Create concept candidates as polished 2D raster illustration.
2. Review candidate direction in-chat or via exported comparison boards.
3. Human approves one canonical direction.
4. Produce canonical neutral / three-quarter master at high resolution.
5. Expand only after approval into semantic derivatives:
   - expressions
   - lighting families
   - affinity appearance stages
   - usage-context crops / scales
6. Export runtime candidates as transparent PNG/WebP where appropriate.
7. Run mobile-size and product-context QA before promotion.

Never recreate the approved art as SVG, VectorDrawable, or Compose Canvas art.

### UI layout and components

Figma is optional. Maintain implementation-ready specs through:

- Markdown screen hierarchy and component contracts
- JSON token/spec files
- true UI icon vectors only where allowed
- Android/Compose visual QA screenshots after development integration

This keeps layout, spacing, typography, surface, navigation, map-overlay, and accessibility behavior reproducible without a Figma dependency.

## Moru v2 workflow

The current Moru v2 SVG candidate board is a historical/reference artifact only.

The next canonical sequence is:

1. Raster concept comparison: 2–3 variants restoring the original Soft Botanical Explorer / small-explorer character intent.
2. Human Gate: choose character direction.
3. Canonical raster master lock.
4. Six expression derivatives: `neutral`, `happy`, `curious`, `surprised`, `clue_found`, `resolved`.
5. Lighting derivatives: `LIGHT`, `WARM_DUSK`, `DARK`.
6. Affinity derivatives: `base`, `familiar`, `trusted`, `best_friend`, preserving BF-B and invariant anatomy.
7. Usage-context derivatives for map avatar, HUD portrait, encounter, result, and journal/memory use.
8. Motion asset plan after canonical art is stable.

## Candidate-review board

A new raster concept board was generated during the 2026-09-11 design session to compare three conceptual directions:

- A — Sprout Scout: balanced botanical/explorer direction; current recommendation.
- B — Leafcap Wanderer: stronger storybook/fantasy character.
- C — Pocket Gardener: more practical/human explorer character.

This board is a review artifact, not canonical production art. Text rendering inside generated concept boards is not considered specification truth; GitHub/Notion text specs remain authoritative.

## Binary asset storage

Until a dedicated raster-art repository/LFS path is approved, generated PNG/WebP files should remain review candidates rather than production assets. When binary source storage is adopted, preserve semantic naming and versioned directories; do not overwrite legacy v1 assets.

Recommended naming examples:

- `companion_moru_v2_candidate_a_neutral.png`
- `companion_moru_v2_candidate_b_neutral.png`
- `companion_moru_v2_candidate_c_neutral.png`
- `companion_moru_v2_light_neutral.webp`

## Human Gates

Do not proceed past the corresponding boundary without approval:

1. Moru character direction / canonical family.
2. Full 5-tab visual parity direction.
3. Motion intensity and timing.
4. Outdoor physical-device readability.
5. Final app icon / logo lock.

## Runtime boundary

This workflow is design-only. It does not authorize:

- Kotlin/Compose runtime changes
- production asset replacement
- resolver repointing
- dependency adoption for animation runtimes
- PR merge
