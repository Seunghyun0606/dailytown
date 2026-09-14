# Daily Town — Design Session Completion Handoff

Status: **all design work that can be completed without Android/runtime execution or a new human visual decision is complete.**

Visual pack: `design/export-spec/visual-pack-manifest.v1.json` v1.4.3.

## Prepared production candidates

- Companion static: 32 assets (Moru + Luca)
- DAY/DARK markers: 24 assets
- A-3 reusable storybook assets: 9 assets

All prepared candidates have semantic keys, design-side visual QA, and SHA-256 manifests. They remain `production_export_candidate` until the runtime/integration gates pass.

## Development-session priority order

1. Companion Promotion Batch 01 semantic resolver + expression/lighting composition smoke test.
2. Marker adapter integration and real NAVER Map QA; verify common anchor and DAY/DARK readability.
3. A-3 five-screen Android fit QA at 360/412/600dp and reduced-motion fallback.
4. EV-1 E0..E4 forcing plus E2 real-map capture and baseline overlay matrix.
5. M-B sprite atlas playback adapter and `sprite-gen` pilot review path. `sprite-gen` stays offline authoring only.
6. Physical outdoor R-B acceptance.
7. Present ID-A1/A2/A3 comparison to the user for final icon/logo lock.

## Required design invariants

- Domain/gameplay state resolves semantic keys, never raw design filenames.
- Moru/Luca remain replaceable through `companionId`.
- Expression, lighting, appearance/affinity, and animation remain orthogonal dimensions.
- Missing expression -> neutral; missing lighting -> LIGHT; missing animation -> static current expression.
- Marker state is not color-only and selected state must not move geographic anchor.
- EV-1 does not create a new EVENING marker or companion-lighting family.
- A-3 paper surfaces remain stable across time of day.
- Concept boards/source masters never enter runtime resources directly.
- `production_export` may be declared only after runtime QA and checksum verification.

## Human gates still open

1. M-B final timing/easing/intensity after visible prototype.
2. Physical outdoor R-B final pass.
3. ID-A1/A2/A3 final icon/logo selection.

## Future enhancement TODOs

- BF-C evolved Moru path must use a separate `variantId` after a future explicit product/user decision.
- Beri is the preferred next companion candidate.
- Pino needs another silhouette-separation pass before shipping.

No Android/Kotlin code is modified by this design handoff.
