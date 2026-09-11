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

### DT-DES-OG-02 · Folded-note clean transparent master

Status: **BLOCKED — static + Android context QA PASS; high-density source quality blocked by missing native master bytes**

Target semantic key: `clue.old_ginkgo.folded_note`

Current checkpoint:
- 768×768 transparent runtime-candidate v2 produced without redesign
- PNG candidate SHA-256: `59171439fe9fefea4a5040fa9034a213983dc360496c3474767b8f58e79e2e92`
- 48 / 64 / 96 / 144 px previews plus 432 px high-density preview prepared
- semi-transparent edge RGB decontaminated toward nearby solid interior color; extremely faint extraction noise removed without silhouette shrink
- cream / dark / map-heavy static QA: PASS
- inspectable alpha-capable GitHub reference persisted under `design/reference/discovery/old-ginkgo-note/production/runtime-v2/`
- repository audit found the inspectable 128×128 reference but not the exact 768×768 master bytes named by the manifest
- PR #10 Android API 30 / 2.625× test-only matrix PASS at 48 / 64 / 96 / 144 dp on cream, dark, and map-heavy surfaces; alpha/transparent corners PASS
- no opaque rectangular matte or strong light halo in the capture; visible upscale softness remains at larger targets
- high-density source quality cannot pass from the 128×128 reference and requires exact-master recovery or same-design native raster reproduction

Required output remains:
- transparent PNG master, minimum 768×768 canvas
- warm handmade paper, subtle botanical mark, folded-paper silhouette from the locked reference
- no new seal, iconography, lettering system, or alternate prop design

### DT-DES-OG-03 · Ginkgo-leaf clean transparent master

Status: **BLOCKED — static + Android context QA PASS; high-density source quality blocked by missing native master bytes**

Target semantic key: `clue.old_ginkgo.ginkgo_leaf`

Current checkpoint:
- 512×512 transparent runtime-candidate v2 produced without redesign
- PNG candidate SHA-256: `ec37263d8e736809629d6d3d304712df1148110794a61e2b36286108f490d064`
- 32 / 48 / 64 / 96 px previews plus 288 px high-density preview prepared
- semi-transparent edge RGB decontaminated without changing fan silhouette/material language
- cream / dark / map-heavy static QA: PASS
- inspectable alpha-capable GitHub reference persisted under `design/reference/discovery/old-ginkgo-note/production/runtime-v2/`
- repository audit found the inspectable 96×96 reference but not the exact 512×512 master bytes named by the manifest
- PR #10 Android API 30 / 2.625× test-only matrix PASS at 32 / 48 / 64 / 96 dp on cream, dark, and map-heavy surfaces; alpha/transparent corners PASS
- no opaque rectangular matte or strong light halo in the capture; visible upscale softness remains at larger targets
- high-density source quality cannot pass from the 96×96 reference and requires exact-master recovery or same-design native raster reproduction

Required output remains:
- transparent PNG master, minimum 512×512 canvas
- single warm-yellow ginkgo silhouette matching the approved discovery family
- no UI-vector flattening

### DT-DES-OG-04 · Old-ginkgo place clean scene master

Status: **DESIGN RUNTIME CANDIDATE — static identity/crop/style QA PASS; not activated**

Target semantic key: `place.old_ginkgo.main`

Required output:
- clean scene master without UI/text overlays, minimum 1600×1200
- same neighborhood/ginkgo lighting, camera language, and storybook finish as the approved reference
- runtime crops: `discovery_card`, `records_header`, `memory_thumbnail`

QA:
- does not compete with player-facing UI
- preserves the approved place identity and warm neighborhood mood
- no alternate environment art direction

Current checkpoint:
- native 1448×1086 generated source and auditable 2048×1536 PNG master preserved as named Work artifacts; exact checksums and inspectable WebP refs are in GitHub
- clean scene contains no UI/text/frame/character; the established folded-note discovery locus remains at the tree base
- `discovery_card` 1600×900, `records_header` 1600×600, `memory_thumbnail` 640×480 WebP crops preserved
- static place identity / palette / lighting / material / crop readability QA: PASS
- manifest/QA: `design/reference/discovery/old-ginkgo-note/production/runtime-v3/`
- Android runtime binding/activation: not performed

### DT-DES-OG-05 · Keepsake / shared-memory master

Status: **DESIGN RUNTIME CANDIDATE — same-scene/A3/crop static QA PASS; not activated**

Target semantic key: `memory.old_ginkgo.keepsake`

Required output:
- derive from the same approved place/note scene
- minimum 1024×768 source
- runtime crops for Records and Companion recent-memory surfaces

QA:
- looks like a memory artifact from the same event, not a newly invented scene
- compatible with A3 paper/journal treatment

Current checkpoint:
- 1536×1152 PNG master is an exact crop from the OG-04 master; no separate scene generation; full master/crops are named Work artifacts with GitHub checksum/refs
- Records 1024×768 and Companion recent-memory 1280×720 WebP crops preserved
- same-event continuity / crop / cream A3 journal-mount static QA: PASS
- manifest/QA: `design/reference/discovery/old-ginkgo-note/production/runtime-v3/`
- Android runtime binding/activation: not performed

### DT-DES-OG-06 · First-scenario asset QA and runtime-candidate promotion

Status: **PARTIAL — OG-04/05 design candidates PASS; OG-02/03 Android context PASS but high-density source-quality BLOCKED**

Checks:
- actual target-size Android readability
- transparent edge / crop QA
- light/dark/map-heavy context QA
- semantic naming and manifest paths
- checksum/reference registry update

Promotion rule:
- assets may become `runtime_candidate` only after these checks pass
- runtime activation remains separate from this design checklist

Current checkpoint:
- integrated QA manifest: `design/reference/discovery/old-ginkgo-note/production/runtime-v3/first-scenario-pack-qa.v3.json`
- semantic names, versioned paths, committed-reference checksums, manifests, registry, and OG-04/05 crop QA: PASS
- PR #10 Android API 30 / 2.625× test-only matrix passed the required dp targets, alpha assertions, and cream/dark/map-heavy contexts without packaging the refs into the runtime APK
- OG-02/03 are not promoted because their exact full-resolution master bytes are unavailable

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

Keep hierarchy locked. Refine only paper/material depth, memory mounting, contextual-line surface, relationship progress, spacing/type rhythm, and true UI icon cleanup.

### DT-DES-CR-02 · Records A3 journal surface polish

Status: **QUEUED**

Keep hierarchy locked. Refine only journal paper/inset hierarchy, clue/memory mounting, tape/stamp/sticker accents, section rhythm, artifact density, and small-size legibility.

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

1. Recover the exact v2 PNG masters or reproduce only the locked designs as native high-resolution raster.
2. Rerun the same Android API 30 / high-density matrix and close the blocked OG-02/03 subset of OG-06.
3. Keep outdoor readability as a physical-device Human Gate; do not activate assets during design production.

Do not start a new concept-board exploration while these production items remain open.
