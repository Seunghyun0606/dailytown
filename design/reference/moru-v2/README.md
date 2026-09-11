# Moru v2 raster reference

Status: **Candidate 3 selected / canonical character direction approved / production-fit work in progress / runtime export pending**.

This directory tracks the Figma-free Moru v2 raster reference package selected by Human Gate on 2026-09-11.

## Canonical direction

Use `docs/design/MORU_CANONICAL_V2.md` as the design Source of Truth.

The selected character direction is the third raster illustration candidate: a small botanical neighborhood explorer with a wearable leaf hood, asymmetric sprout, warm scarf, cross-body field satchel and sturdy walking boots.

The character direction is locked. Production work must refine/export this identity; it must not reopen alternate Moru families unless the user explicitly asks for a new direction.

## Artifact tracking

`manifest.json` records expected dimensions, roles and SHA-256 for the raster reference masters and production-fit preview.

Reference set:

- `moru-candidate3-selected.png` — Human Gate selection source
- `moru-canonical-v2-sheet.png` — canonical derivation / pose-expression-lighting-affinity fit reference
- `moru-candidate3-production-fit-board-preview.webp.base64` — exact repository payload for the 512×341 WebP review preview
- `BINARY_ASSET_NOTE.md` — decode path and SHA-256 verification for that preview

The `.base64` wrapper is a connector-transfer representation only. After decoding, the asset is WebP raster game art; the wrapper does not make base64/text a production format.

Do not replace these with SVG redraws, VectorDrawable reconstructions, Compose Canvas approximations, or flattened geometric mascot substitutes.

## Production contract

Use:

- `PRODUCTION_QA.md` for the export/fit/mobile acceptance checklist
- `export-manifest.v2.json` for semantic resolver dimensions, naming, fallback order and promotion rules

The current stage is **production candidate preparation**, not runtime replacement.

Production work still needs clean transparent-background exports/crops, all six expression derivatives, LIGHT/WARM_DUSK/DARK validation, affinity decoration fit, 48 dp and context-specific mobile checks, semantic fallback verification, and physical-device outdoor review.

Keep the previous runtime/fallback pack available until the v2 raster family passes that gate.

## Related visual polish

The approved UX structure remains under `design/reference/ux-v2/`. New screen-polish work should use this Moru direction while preserving the UX v2 hierarchy rather than redesigning navigation/information architecture.

The next design output should be production derivatives or screen/background/detail polish using Candidate 3 — not another character-direction comparison board.
