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
- cream / dark / map-heavy static QA: PASS
- PR #10 Android API 30 / 2.625× context QA: PASS at required dp targets, including alpha/transparent corners
- inspectable GitHub reference exists, but the exact 768×768 master bytes are not persisted
- high-density source quality remains blocked until exact-master recovery or same-design native raster reproduction

### DT-DES-OG-03 · Ginkgo-leaf clean transparent master

Status: **BLOCKED — static + Android context QA PASS; high-density source quality blocked by missing native master bytes**

Target semantic key: `clue.old_ginkgo.ginkgo_leaf`

Current checkpoint:
- 512×512 transparent runtime-candidate v2 produced without redesign
- PNG candidate SHA-256: `ec37263d8e736809629d6d3d304712df1148110794a61e2b36286108f490d064`
- 32 / 48 / 64 / 96 px previews plus 288 px high-density preview prepared
- cream / dark / map-heavy static QA: PASS
- PR #10 Android API 30 / 2.625× context QA: PASS at required dp targets, including alpha/transparent corners
- inspectable GitHub reference exists, but the exact 512×512 master bytes are not persisted
- high-density source quality remains blocked until exact-master recovery or same-design native raster reproduction

### DT-DES-OG-04 · Old-ginkgo place clean scene master

Status: **DESIGN RUNTIME CANDIDATE — static identity/crop/style QA PASS; not activated**

Target semantic key: `place.old_ginkgo.main`

Current checkpoint:
- native 1448×1086 generated source and auditable 2048×1536 PNG master persisted as GitHub binaries with exact checksums and inspectable WebP refs
- clean scene contains no UI/text/frame/character; the established folded-note discovery locus remains at the tree base
- `discovery_card` 1600×900, `records_header` 1600×600, `memory_thumbnail` 640×480 WebP crops preserved
- static place identity / palette / lighting / material / crop readability QA: PASS
- manifest/QA: `design/reference/discovery/old-ginkgo-note/production/runtime-v3/`
- Android runtime binding/activation: not performed

### DT-DES-OG-05 · Keepsake / shared-memory master

Status: **DESIGN RUNTIME CANDIDATE — same-scene/A3/crop static QA PASS; not activated**

Target semantic key: `memory.old_ginkgo.keepsake`

Current checkpoint:
- 1536×1152 PNG master is an exact crop from the OG-04 master; no separate scene generation
- full master/crops are persisted as GitHub binaries with checksums/refs
- Records 1024×768 and Companion recent-memory 1280×720 WebP crops preserved
- same-event continuity / crop / cream A3 journal-mount static QA: PASS
- manifest/QA: `design/reference/discovery/old-ginkgo-note/production/runtime-v3/`
- Android runtime binding/activation: not performed

### DT-DES-OG-06 · First-scenario asset QA and runtime-candidate promotion

Status: **PARTIAL — OG-04/05 design candidates PASS; OG-02/03 Android context PASS but high-density source-quality BLOCKED**

Current checkpoint:
- integrated QA manifest: `design/reference/discovery/old-ginkgo-note/production/runtime-v3/first-scenario-pack-qa.v3.json`
- semantic names, versioned paths, committed-reference checksums, manifests, registry, and OG-04/05 crop QA: PASS
- PR #10 Android API 30 / 2.625× test-only matrix passed the required dp targets, alpha assertions, and cream/dark/map-heavy contexts without packaging refs into the runtime APK
- OG-02/03 are not promoted because their exact full-resolution master bytes are unavailable

Promotion rule:
- design-side `runtime_candidate` promotion only after source-quality checks pass
- Android runtime activation remains separate from this design checklist

## P1 — Moru Candidate 3 production export

### DT-DES-MORU-01 · Transparent master family

Status: **IN PROGRESS — source-sufficiency audit complete; native transparent production masters still required**

Outputs:
- approved Candidate 3 only
- usage contexts: map avatar, HUD portrait, encounter half-body, result large, Companion portrait, journal crop
- preserve sprout, hood, scarf diagonal, satchel, boots, anatomy and costume

Current checkpoint:
- exact Candidate 3 board remains canonical: 1122×1402, SHA-256 `541b53464bbc589d86323faa3a7dbc8790cf9ab9573e0dd66812986c4a430500`
- source audit recorded in `docs/design/MORU_SOURCE_SUFFICIENCY_V2.md`
- machine-readable crop/source audit recorded in `design/reference/moru-v2/production/source-sufficiency.v2.json`
- board is sufficient for identity, framing, 48dp reference, expression/lighting/affinity intent
- board crops are not native production masters: full-body turnaround references are about 315–340×540–565 px; expression refs are about 162–200×215 px
- `map_avatar`, `hud_portrait`, and `journal_crop` can support reference-derived QA candidates
- `encounter_halfbody`, `result_large`, and final `companion_portrait` require native same-design raster reproduction rather than simple upscaling
- this is a source-resolution/clean-transparency blocker, not a design-direction blocker

### DT-DES-MORU-02 · Actual Android-size QA

Status: **BLOCKED by native/clean MORU-01 masters; reference precheck PASS**

Checks:
- 48 / 56 / 64 dp map/HUD read
- six semantic expressions
- LIGHT / WARM_DUSK / DARK
- base / familiar / trusted / best_friend invariance
- transparent edge/halo

Reference precheck is already PASS. Actual runtime-size QA must use the real transparent export family, not composite-board crops presented as final masters.

### DT-DES-MORU-03 · Semantic manifest activation readiness

Status: **BLOCKED by MORU-02**

Checks:
- `neutral / LIGHT / base / static` fallback
- v1 rollback retained until v2 resolver/fallback tests pass
- no silent repoint of legacy profile
- semantic profile remains `companion.moru.canonical.v2`

## P1 — Companion / Records finish

### DT-DES-CR-01 · Companion relationship-notebook surface polish

Status: **DESIGN SPEC / FINISH REFERENCE READY — Android parity implementation pending**

Design-side review:
- hierarchy remains locked
- paper/material depth, memory mounting, contextual-line surface, relationship progress, spacing/type rhythm and true UI icon language are defined
- raster finish reference already exists and no further concept board is required
- QA decision recorded in `docs/design/COMPANION_RECORDS_FINISH_QA_V2.md`

Runtime follow-up:
- replace generic Material 3 visual dominance with `DTCompanionNotebookHero`, `DTAffinityNote`, `DTRecentMemoryCard` treatment
- preserve existing semantic memory/bond data, routing and test tags

### DT-DES-CR-02 · Records A3 journal surface polish

Status: **DESIGN SPEC / FINISH REFERENCE READY — Android parity implementation pending**

Design-side review:
- journal paper/inset hierarchy, clue/memory mounting, tape/stamp/sticker accents, section rhythm, artifact density and small-size legibility are defined
- one dominant record artifact per region remains the rule
- clue/place/memory art remains raster content
- QA decision recorded in `docs/design/COMPANION_RECORDS_FINISH_QA_V2.md`

Runtime follow-up:
- use `DTJournalCanvas`, `DTJournalTabs`, `DTRecordArtifact`, `DTClueArtifact`, `DTMemoryStamp`
- preserve real persisted data and do not decorate unsupported mystery state into existence

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

1. Treat Old Ginkgo P0 as substantially produced on `design/old-ginkgo-production-v3`, but keep OG-02/03 high-density source-quality blocker explicit until native/exact masters are recovered.
2. Produce **native-resolution transparent Moru Candidate 3 masters** from the locked design; start with map/avatar + HUD for early Android QA, then Companion/journal, then encounter/result-large.
3. Run MORU-02 Android-size/edge/lighting/affinity QA and only then prepare semantic activation readiness.
4. Companion/Records design-side finish is closed; next work there belongs to Android visual parity implementation/QA rather than another design exploration.
5. Keep outdoor readability, M-B final timing/intensity and ID-A icon/logo as Human Gates.

Do not start a new concept-board exploration while these production items remain open.
