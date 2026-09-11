# Moru v2 raster reference

Status: **Candidate 3 selected / canonical character direction approved / runtime export pending**.

This directory tracks the Figma-free Moru v2 raster reference package selected by Human Gate on 2026-09-11.

## Canonical direction

Use `docs/design/MORU_CANONICAL_V2.md` as the design Source of Truth.

The selected character direction is the third raster illustration candidate: a small botanical neighborhood explorer with a wearable leaf hood, asymmetric sprout, warm scarf, cross-body field satchel and sturdy walking boots.

## Artifact tracking

`manifest.json` records the expected dimensions and SHA-256 for the generated raster reference masters:

- `moru-candidate3-selected.png`
- `moru-canonical-v2-sheet.png`

The binary files are raster design artifacts. If they are transferred into GitHub through a binary-capable path, verify the exact SHA-256 against `manifest.json` before treating the repository copy as authoritative.

Do not replace these with SVG redraws, VectorDrawable reconstructions, Compose Canvas approximations, or flattened geometric mascot substitutes.

## Production status

The selected raster direction is approved, but runtime replacement is not automatic. Production work still needs transparent-background exports/crops, expression fit, lighting variants, affinity decoration fit, 48 dp readability, semantic asset manifest versioning and Android QA.

Keep the previous production/fallback pack available until the new raster family passes that gate.

## Related visual polish

The approved UX structure remains under `design/reference/ux-v2/`. New screen-polish work should use this Moru direction while preserving the UX v2 hierarchy rather than redesigning the navigation/information architecture.
