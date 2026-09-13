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

Status: **CLOSED — exact source persisted; immutable-alpha edge reconstruction QA PASS; runtime candidate v4 not activated**

Target semantic key: `clue.old_ginkgo.folded_note`

Current checkpoint:
- exact 768×768 RGBA source persisted byte-for-byte; SHA-256 `59171439fe9fefea4a5040fa9034a213983dc360496c3474767b8f58e79e2e92`
- alpha-first correction changed only exterior fractional-edge RGB and a transparent-side bleed ring
- alpha changed pixels 0; silhouette IoU 1.0; opaque-interior/deep-fractional RGB changed pixels 0
- corrected v4 SHA-256: `2adc914f6ca2e5648339943bc02afe62059948d52a672e382fad69b5e6c08869`
- cream / dark / map-heavy and 48 / 64 / 96 / 144 px static QA: PASS
- source, candidate, metrics, difference/overlay/edge-close-up boards: `design/reference/discovery/old-ginkgo-note/production/runtime-v4/`
- Android runtime activation: NOT PERFORMED
- physical-device outdoor readability: Human Gate

### DT-DES-OG-03 · Ginkgo-leaf clean transparent master

Status: **CLOSED — exact source persisted; selective edge-retouch QA PASS; runtime candidate v5 not activated**

Target semantic key: `clue.old_ginkgo.ginkgo_leaf`

Current checkpoint:
- exact 512×512 RGBA source persisted byte-for-byte; SHA-256 `ec37263d8e736809629d6d3d304712df1148110794a61e2b36286108f490d064`
- local boundary-palette analysis locked 14,148 warm-brown and 7,428 warm-yellow fractional-edge pixels; no visible-support RGB outlier qualified for retouch
- v5 changes only a 12px alpha-zero transparent-side RGB bleed ring seeded from valid local boundary colors; changed RGB pixels 19,189
- alpha changed pixels 0; silhouette IoU 1.0; fan/notch/stem geometry and all visible-support RGB unchanged
- corrected v5 SHA-256: `775059cd22db997234e8012534a7e73f9a347d8f1208117c3babee1c9ca1f366`
- cream / dark / map-heavy, 32 / 48 / 64 / 96px and 288px high-density static QA: PASS
- exact source, candidate, metrics, difference/overlay/palette/edge-close-up boards: `design/reference/discovery/old-ginkgo-note/production/runtime-v5/`
- previous nearest-opaque candidate remains rejected and uncommitted
- Android runtime activation: NOT PERFORMED
- physical-device outdoor readability: Human Gate

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

Status: **CLOSED — DESIGN_RUNTIME_CANDIDATE_PACK_READY; not runtime-activated**

Current checkpoint:
- integrated QA manifest: `design/reference/discovery/old-ginkgo-note/production/runtime-v3/first-scenario-pack-qa.v3.json`
- semantic names, versioned paths, committed checksums, manifests, registry, OG-04/05 crop QA and OG-02/03 source-quality QA: PASS
- PR #10 Android API 30 / 2.625× test-only matrix passed the required dp targets, alpha assertions, and cream/dark/map-heavy contexts without packaging refs into the runtime APK
- OG-02 v4 and OG-03 v5 are persisted design-side runtime candidates; OG-04/05 retain their existing design-side PASS

Promotion rule:
- the design-side first-scenario runtime-candidate pack is ready
- Android runtime binding/activation remains a separate Development task and was not performed
- physical-device outdoor readability remains a Human Gate

## P1 — Moru Candidate 3 production export

### DT-DES-MORU-01 · Transparent master family

Status: **BLOCKED — neutral identity-lock attempt 1 failed; clean native transparent source/master required**

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
- deterministic, non-generative reference-derived candidates are persisted under `design/reference/moru-v2/reference-derived-small-context-v1/`
- map 48/56/64dp, HUD 56/64/72dp and journal 56/64/72dp static context QA: `REFERENCE_QA_PASS` (3/3); these are not native masters and were not runtime-activated
- the human/layered raster reconstruction package is ready under `design/reference/moru-v2/native-master-handoff-v1/`; no large-context asset was regenerated
- `encounter_halfbody`, `result_large`, and final `companion_portrait` require native same-design raster reproduction rather than simple upscaling
- this is a source-resolution/clean-transparency blocker, not a design-direction blocker
- 2026-09-13 neutral attempt 1: FAIL_MORU_IDENTITY_LOCK; candidate was 1122×1402 RGB with baked checkerboard, SHA-256 3c9ca6ddbc0f53a4dc40f1d335ac040646b7d1c3cf12c5f44de2ef7d25ed61da
- face/eye/mouth, hood/sprout silhouette, costume/leaf layering, satchel/compass, hands/boots and rendering drifted from the exact canonical
- rejected binary/QA evidence was not committed; do not repeat the same prompt or fan out variants
- resume native production with the committed reconstruction handoff and a human/layered raster source; validate against the acceptance contract before any native family fan-out

### DT-DES-MORU-02 · Actual Android-size QA

Status: **BLOCKED by missing native master; reference-derived small-context QA PASS 3/3**

Checks:
- 48 / 56 / 64 dp map/HUD read
- six semantic expressions
- LIGHT / WARM_DUSK / DARK
- base / familiar / trusted / best_friend invariance
- transparent edge/halo

Deterministic reference-derived map/HUD/journal context QA is PASS and may inform a temporary/reference Development profile only. Actual MORU-02 runtime-size QA must use the accepted native transparent export family, not these board-derived candidates.

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

1. Keep the Old Ginkgo design-side first-scenario pack closed and unchanged until a separate Development task performs Android runtime binding/activation.
2. Use `design/reference/moru-v2/native-master-handoff-v1/` for human/layered raster reconstruction of the locked Candidate 3; first deliver one native neutral master and pass the identity acceptance contract before deriving usage contexts.
3. Run MORU-02 Android-size/edge/lighting/affinity QA and only then prepare semantic activation readiness.
4. Keep physical-device outdoor readability, M-B final timing/intensity and ID-A icon/logo as Human Gates.

Do not start a new concept-board exploration while these production items remain open.
