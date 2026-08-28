# Daily Town P1 Design Execution Status

Status date: 2026-08-26

## Approved P1 directions

- Affinity: **AF-1 Memory Keepsakes + restrained AF-3 Explorer Patina**
- Best Friend shipping ceiling: **BF-B Signature Explorer**
- Future affinity variation path: **BF-C Evolved Character Variant** as post-MVP TODO only
- Shipping companion set: **SC-B Moru + Luca**
- Motion personality: **M-B Responsive Soft**
- Outdoor readability: **R-B Balanced**
- EVENING: **EV-1 Interpolation-first**
- A-3: five-screen storybook/paper component-fit system
- App identity direction: **ID-A Sprout Town Mark**

## P1 design-owned source work — COMPLETE

Approved source exists for Moru, Luca, DAY/DARK markers, route/halo/discovery, A-3, EV-1, M-B storyboard, affinity slots and ID-A candidates.

## Companion production-promotion candidates — DESIGN QA COMPLETE

Two production manifests now track **32 isolated SVG candidates** with SHA-256:

- `design/production/production-promotion-batch-01.v1.json`
- `design/production/production-promotion-batch-01-luca-derivatives.v1.json`

### Moru — 18 candidates

- affinity profiles: 4
- canonical views: 5
- expressions: 6
- lighting layers: 3

### Luca — 14 candidates

- canonical views: 5
- expressions: 6
- lighting layers: 3

Design-side QA completed:

- SVG/XML parse
- vector raster-render verification
- contact-sheet visual review
- 48dp-equivalent compact check
- 32dp fallback spot check
- expression distinction
- lighting identity preservation
- Moru/Luca silhouette separation
- SHA-256 generation

These remain `production_export_candidate` until runtime integration verifies semantic resolution and expression+lighting composition.

## Development smoke-test contract — READY

Added:

- `design/export-spec/companion-static-smoke-test.v1.json`
- `docs/visual/DEV_HANDOFF_COMPANION_PROMOTION_BATCH_01.md`

Development must verify:

1. semantic asset resolution without raw filename/domain coupling
2. 6 expression × 3 lighting composition for Moru and Luca
3. 48dp compact renders and 32dp fallback spot checks
4. Moru affinity replacement without semantic mutation
5. Moru/Luca switching under identical semantic requests
6. deterministic fallbacks
7. checksum match before authoritative promotion

No Android/Kotlin implementation is modified by this design session.

## Current visual pack

`design/export-spec/visual-pack-manifest.v1.json` is **1.4.2**.

Status: `p1_design_source_complete_companion_promotion_candidates_ready`.

## Next priority after development smoke test

1. DAY/DARK marker split export + real NAVER map QA
2. A-3 split export + 360/412/600dp Android screenshot QA
3. M-B sprite-gen pilot output + human motion QA
4. EV-1 E2 + baseline map capture matrix
5. physical outdoor R-B final acceptance
6. ID-A1/A2/A3 final human lock

## Remaining current human gates

1. final M-B timing/easing/intensity after visible prototype review
2. physical-device outdoor readability final acceptance under R-B
3. final ID-A icon/logo candidate lock after visual comparison

## Future enhancement TODOs

- explicit future decision may activate BF-C evolved Moru variants using a separate `variantId`
- Beri is preferred next companion expansion candidate
- Pino requires another silhouette-separation pass before shipping
