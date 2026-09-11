# DailyTown Design Production Work Items v2

Status: **ACTIVE — approved baseline derivation only**

This file is the repository checklist for the next design-production work under `DESIGN_BASELINE_V2.md`.
It is not a new art-direction document. The approved UX visual baseline and Moru Candidate 3 remain locked.

## Operating rule

- Do not generate alternate Moru families, alternate Explore compositions, or a new illustration style.
- Game-world art stays raster-first (PNG master / WebP runtime as appropriate).
- Every generated or accepted raster board/reference must be persisted under `design/reference/` and registered before the work item is considered repository-complete.
- Composite/reference boards are never runtime assets by themselves.
- Domain/gameplay behavior remains authoritative; decorative values visible in art do not change runtime rules.

## P0 — Old Ginkgo Note first-scenario production pack

### DT-DES-OG-01 · Production reference board persistence

Status: **DONE**

Deliverables:
- production/reference board preserved in GitHub under `design/reference/discovery/old-ginkgo-note/production/`
- registry entry with reference-only role
- production manifest links to the isolated-reference and clean-master workflow

Definition of Done:
- reference is inspectable in GitHub
- it cannot be mistaken for a runtime asset
- baseline lock is documented next to it

### DT-DES-OG-02 · Folded-note clean transparent master

Status: **IN PROGRESS — transparent extraction candidate created; edge/source-quality/Android QA pending**

Target semantic key: `clue.old_ginkgo.folded_note`

Current checkpoint:
- 768×768 transparent candidate extracted from the locked production reference without redesign
- candidate PNG SHA-256: `c9b44fd1d89326a3b85c0086aee9f649dd2a3c96780a3c3f6d19692f1f7dedc8`
- 48 / 64 / 96 / 144 px QA derivatives prepared and checksummed in `runtime-candidate-sizes.v1.json`
- candidate metadata recorded in `transparent-candidate-manifest.v1.json`
- not runtime-ready because the source is a composite-board extraction and upscaling does not restore original illustration detail

Required output:
- transparent PNG master, minimum 768×768 canvas
- no checkerboard baked into pixels
- warm handmade paper, subtle botanical mark, folded-paper silhouette from the locked reference
- runtime WebP derivatives for 48 / 64 / 96 / 144 dp use contexts
- no new seal, iconography, lettering system, or alternate prop design unless already present in the approved reference family

QA still required:
- inspect and clean fringe/edge pixels on cream, dark, and map-heavy surfaces
- confirm source-resolution quality at actual Android display sizes
- recognizable folded-paper silhouette at 48 dp
- no vector/flat-icon treatment
- persist an inspectable alpha-capable raster candidate in GitHub before closing this item

### DT-DES-OG-03 · Ginkgo-leaf clean transparent master

Status: **IN PROGRESS — transparent extraction candidate created; edge/source-quality/Android QA pending**

Target semantic key: `clue.old_ginkgo.ginkgo_leaf`

Current checkpoint:
- 512×512 transparent candidate extracted from the locked production reference without redesign
- candidate PNG SHA-256: `647a2fec78c70bcbb05efde0be800722c8db754d9c880aa0d697d341a4a2186b`
- 32 / 48 / 64 / 96 px QA derivatives prepared and checksummed in `runtime-candidate-sizes.v1.json`
- candidate metadata recorded in `transparent-candidate-manifest.v1.json`
- not runtime-ready because source-resolution/edge review is still required

Required output:
- transparent PNG master, minimum 512×512 canvas
- single warm-yellow ginkgo silhouette matching the approved discovery family
- runtime WebP derivatives for 32 / 48 / 64 / 96 dp

QA still required:
- leaf fan shape remains identifiable at 32–48 dp
- material/light treatment remains raster-illustrated, not UI-vector-like
- edge contrast works on cream and live-map backgrounds
- persist an inspectable alpha-capable raster candidate in GitHub before closing this item

### DT-DES-OG-04 · Old-ginkgo place clean scene master

Status: **QUEUED — next after OG-02/03 QA**

Target semantic key: `place.old_ginkgo.main`

Required output:
- clean scene master without UI/text overlays, minimum 1600×1200
- same neighborhood/ginkgo lighting, camera language, and storybook finish as the approved reference
- runtime crops: `discovery_card`, `records_header`, `memory_thumbnail`

QA:
- does not compete with player-facing UI
- preserves the approved place identity and warm neighborhood mood
- no alternate environment art direction

### DT-DES-OG-05 · Keepsake / shared-memory master

Status: **QUEUED**

Target semantic key: `memory.old_ginkgo.keepsake`

Required output:
- derive from the same approved place/note scene
- minimum 1024×768 source
- runtime crops for Records and Companion recent-memory surfaces

QA:
- looks like a memory artifact from the same event, not a newly invented scene
- compatible with A3 paper/journal treatment

### DT-DES-OG-06 · First-scenario asset QA and runtime-candidate promotion

Status: **BLOCKED by OG-02…05**

Checks:
- target-size readability
- transparent edge / crop QA
- light/dark/map-heavy context QA
- semantic naming and manifest paths
- checksum/reference registry update

Promotion rule:
- assets may become `runtime_candidate` only after these checks pass
- runtime activation remains separate from this design checklist

## P1 — Moru Candidate 3 production export

### DT-DES-MORU-01 · Transparent master family

Status: **BLOCKED — clean master derivation pending**

Outputs:
- approved Candidate 3 only
- usage contexts: map avatar, HUD portrait, encounter half-body, result large, Companion portrait, journal crop
- preserve sprout, hood, scarf diagonal, satchel, boots, anatomy and costume

### DT-DES-MORU-02 · Actual Android-size QA

Status: **BLOCKED by MORU-01**

Checks:
- 48 / 56 / 64 dp map/HUD read
- six semantic expressions
- LIGHT / WARM_DUSK / DARK
- base / familiar / trusted / best_friend invariance
- transparent edge/halo

Reference precheck is already PASS; this item is for the real exported asset family.

### DT-DES-MORU-03 · Semantic manifest activation readiness

Status: **BLOCKED by MORU-02**

Checks:
- `neutral / LIGHT / base / static` fallback
- v1 rollback retained until v2 resolver/fallback tests pass
- no silent repoint of legacy profile

## P1 — Companion / Records finish

### DT-DES-CR-01 · Companion relationship-notebook surface polish

Status: **QUEUED**

Keep hierarchy locked. Refine only:
- paper/material depth
- memory mounting treatment
- contextual-line surface
- relationship progress treatment
- spacing/type rhythm
- true UI icon cleanup

### DT-DES-CR-02 · Records A3 journal surface polish

Status: **QUEUED**

Keep hierarchy locked. Refine only:
- journal paper/inset hierarchy
- clue/memory mounting
- tape/stamp/sticker accents
- section rhythm and artifact density
- small-size legibility

## P2 — Final QA / Human Gates

### DT-DES-QA-01 · Outdoor readability

Status: **HUMAN GATE**

Physical-device review for map/HUD/marker/discovery readability. Emulator screenshots cannot close this gate.

### DT-DES-MOTION-01 · M-B motion timing/intensity

Status: **HUMAN GATE**

Use the existing Candidate 3 anatomy and asset family only. No alternate character design during motion work.

### DT-DES-ID-01 · ID-A app icon / logo lock

Status: **HUMAN GATE**

Do after the in-app production family is stable.

## Current next action

1. Complete **DT-DES-OG-02 / OG-03** edge/source-quality/Android-size QA and persist inspectable alpha-capable refs.
2. If the composite-derived source quality is insufficient for high-density Android display, recreate only the same locked folded-note/leaf assets at higher native resolution; do not redesign them.
3. Proceed to **DT-DES-OG-04** clean place scene master, then `OG-05 → OG-06`.

Do not start a new concept-board exploration while these production items remain open.