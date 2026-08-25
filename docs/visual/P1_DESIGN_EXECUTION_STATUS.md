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

## P1 design-owned work — COMPLETE AT APPROVED_SOURCE LEVEL

### BF-B final source

Added:

- `design/source/companion/moru/moru-bf-b-final-board-v1.svg`
- `design/export-spec/moru-bf-b-export-jobs.v1.json`

The source fixes the Base / Familiar / Trusted / Best Friend progression, compact 48dp rules, and the boundary that BF-C is a future separately versioned character variant rather than a cosmetic profile.

### Luca canonical production source

Added:

- `design/source/companion/luca/luca-canonical-source-master-v1.svg`
- `design/export-spec/luca-production-manifest.v1.json`

The source defines:

- front / 3-quarter / side / back / black silhouette
- six semantic expressions
- LIGHT / WARM_DUSK / DARK lighting contract
- required usage contexts
- 48dp map-avatar rules
- ear-safe accessory constraint
- Moru-vs-Luca silhouette separation gate

### M-B Responsive Soft motion storyboard

Added:

- `design/source/motion/moru-m-b-storyboard-v1.svg`
- `design/export-spec/m-b-motion-pilot.v1.json`

Pilot states:

1. `idle_breathe`
2. `clue_react`
3. `resolved_settle`

Rules:

- context amplitude: Map < HUD < Encounter/Result
- reduced-motion static fallback remains mandatory
- `sprite-gen` is optional offline authoring only
- generated frame timing/easing/intensity is still a human prototype gate
- `walk` remains experimental

### ID-A Sprout Town candidate family

Added:

- `design/source/brand/id-a-sprout-town-candidates-v1.svg`
- `design/export-spec/id-a-brand-manifest.v1.json`

Candidate family:

- `ID-A1` Sprout + Path
- `ID-A2` Sprout + Door/Town
- `ID-A3` Sprout + Neighborhood Map

The direction is locked to ID-A, but the final A1/A2/A3 candidate remains a human visual lock before production export.

## Existing completed source work retained

- Moru A-2 canonical source and 6-expression/3-lighting contract
- DAY/DARK marker source family
- route / halo / discovery semantic motion contract
- A-3 reusable source kit and five-screen integrated fit board
- affinity profile manifest and modular slot source master
- EV-1 E0..E4 interpolation tokens
- R-B balanced outdoor acceptance policy
- BF-C future variant TODO

## Visual pack

`design/export-spec/visual-pack-manifest.v1.json` is now pack version **1.4.0** with status `p1_design_source_complete`.

## Remaining work that cannot be closed by design source generation alone

### Production promotion

- generate isolated production derivatives from approved SVG masters
- run visual QA at intended sizes
- calculate SHA-256
- only then mark each derivative `production_export`

This is mechanical production work, not an unresolved visual direction.

### Development-session integration QA

- real NAVER map marker/route/halo captures
- EV-1 E2 capture plus baseline map matrix
- A-3 360/412/600dp Android screenshot fit validation
- runtime sprite-atlas playback validation independent of sprite-gen authoring

### Human gates

1. review generated M-B motion prototype and lock final timing/easing/intensity
2. physical-device outdoor R-B final acceptance
3. choose final ID-A1/A2/A3 icon/logo candidate

## Future enhancement TODOs

- future explicit decision may activate BF-C evolved Moru variants using a separate `variantId`
- Beri is preferred next companion expansion candidate
- Pino needs another silhouette-separation pass before shipping

## Development-session handoff remains unblocked

Development can proceed with:

1. Moru + Luca companionId resolver
2. affinity modular slot binding
3. historical affinity/variant fallback data shape
4. EV-1 E0..E4 debug forcing and continuous interpolation
5. A-3 screenshot QA tooling
6. sprite-atlas playback adapter
7. R-B QA result bands: PASS / PASS_WITH_DECORATIVE_DEGRADATION / FAIL

No Android/Kotlin implementation is modified by this design session.
