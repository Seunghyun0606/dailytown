# Daily Town P1 Approved Human Decisions v1

Status: approved design decisions for the current production direction.

## Approved selections

- Best Friend ceiling: **BF-B Signature Explorer**
- Future advanced variation path: **BF-C Evolved Character Variant** as post-MVP TODO, activated only by a future explicit decision
- Shipping companion set: **SC-B Moru + Luca**
- Motion personality: **M-B Responsive Soft**
- Outdoor readability acceptance policy: **R-B Balanced**
- App identity direction: **ID-A Sprout Town Mark**

## BF-B now / BF-C later

Current shipping affinity stays inside the modular appearance system.

BF-C is intentionally not modeled as a larger cosmetic. A future evolved Moru becomes a separately versioned `variantId`, preserving:

- current canonical Moru
- historical memory rendering
- affinity-stage semantics
- replaceable asset bindings

Reference: `docs/visual/BF_C_VARIANT_EXTENSION_TODO_V1.md`.

## SC-B launch set

The first shipping companion family is:

1. Moru
2. Luca

Luca receives a production canonical pass under the common companion contract.

Beri remains the preferred next expansion candidate. Pino remains pending extra silhouette-separation review.

Reference: `docs/visual/LUCA_PRODUCTION_BRIEF_V1.md`.

## M-B motion

M-B is the approved personality target, not a locked timing table.

- map-avatar motion is restrained
- encounter/result motion can be clearer
- no perpetual exaggerated bounce
- `sprite-gen` remains optional offline authoring tooling
- final frame timing/easing remains prototype/human QA gated

Reference: `docs/visual/MOTION_RESPONSIVE_SOFT_TARGET_V1.md`.

## R-B readability

R-B allows documented loss of non-critical decoration if critical semantics remain readable.

Critical semantics, route readability, provider/legal visibility and practical HUD readability are still hard blockers.

Reference: `docs/visual/OUTDOOR_READABILITY_POLICY_RB_V1.md`.

## ID-A identity

Core brand mark is companion-independent:

- two-leaf sprout
- restrained town/exploration cue
- compact launcher-readable geometry

Candidate artwork routes:

- ID-A1 Sprout + Path
- ID-A2 Sprout + Doorway
- ID-A3 Sprout + Town Tile

Reference: `docs/visual/APP_IDENTITY_ID_A_BRIEF_V1.md`.

## Next production design work now unblocked

1. BF-B final comparison/source board
2. Luca canonical production candidate sheet
3. M-B Moru motion pilot/storyboard and sprite-gen curation QA
4. ID-A1/A2/A3 icon + wordmark comparison board
5. R-B real-device QA result schema integration

No Android/Kotlin runtime implementation is included in this design decision package.
