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

Approved source exists for:

- Moru A-2 canonical body, six expressions and three lighting families
- BF-B Base / Familiar / Trusted / Best Friend progression
- Luca canonical source
- DAY/DARK marker family
- route / halo / discovery effect contract
- A-3 reusable source kit and five-screen fit board
- EV-1 E0..E4 interpolation tokens
- M-B motion storyboard/pilot contract
- ID-A1/A2/A3 brand candidate family

## Production promotion batch 01 — DESIGN QA COMPLETE

Added `design/production/production-promotion-batch-01.v1.json`.

The batch contains **23 isolated SVG production candidates** with SHA-256 and design-side render QA:

### Moru

- 4 affinity profiles: `base / familiar / trusted / best_friend`
- 5 canonical exports: `front / three_quarter / side / back / silhouette`
- 6 expression overlays: `neutral / happy / curious / surprised / clue_found / resolved`
- 3 lighting layers: `LIGHT / WARM_DUSK / DARK`

### Luca

- 5 canonical exports: `front / three_quarter / side / back / silhouette`

Design-side QA completed:

- SVG/XML parse
- transparent vector render
- contact-sheet visual inspection
- 48dp-equivalent compact readability review
- 32dp fallback spot check
- Moru expression distinction check
- Moru lighting identity check
- Moru/Luca silhouette separation
- checksum generation

These files intentionally remain `production_export_candidate` until runtime composition/asset-loader smoke tests pass. This avoids incorrectly promoting assets before development integration.

## Current visual pack

`design/export-spec/visual-pack-manifest.v1.json` is now **1.4.1**.

Status: `p1_design_source_complete_promotion_batch_01_qa_complete`.

## Next priority — development/integration bridge

Before authoritative `production_export` promotion for batch 01:

1. verify runtime composition of Moru expression + lighting layers
2. produce/verify Luca expression + lighting derivatives
3. smoke-test asset loader/vector adapter in the development session

After that, continue with:

4. DAY/DARK marker split export + real NAVER map QA
5. A-3 split export + 360/412/600dp Android screenshot QA
6. M-B sprite-gen pilot output + human motion QA
7. EV-1 E2 + baseline map capture matrix
8. physical outdoor R-B final acceptance
9. ID-A1/A2/A3 final human lock

## Remaining current human gates

1. final M-B timing/easing/intensity after visible prototype review
2. physical-device outdoor readability final acceptance under R-B
3. final ID-A icon/logo candidate lock after visual comparison

## Future enhancement TODOs

- explicit future decision may activate BF-C evolved Moru variants using a separate `variantId`
- Beri is preferred next companion expansion candidate
- Pino requires another silhouette-separation pass before shipping

## Development-session handoff

Development may proceed with:

- Moru + Luca companionId resolver
- modular Moru expression + lighting composition check
- affinity modular slot binding
- historical affinity/variant fallback shape
- EV-1 E0..E4 debug forcing
- A-3 screenshot QA tooling
- sprite-atlas playback adapter independent of sprite-gen authoring
- R-B QA result bands: `PASS / PASS_WITH_DECORATIVE_DEGRADATION / FAIL`

No Android/Kotlin implementation is modified by this design session.
