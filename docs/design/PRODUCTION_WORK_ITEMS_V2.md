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

Status: **DONE — native neutral, usage, lighting and semantic source family PASS; runtime not activated**

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
- strict full-canvas/no-upscale QA: map 48/56/64dp `REFERENCE_QA_PASS`; HUD and journal 56/64/72dp `REFERENCE_QA_FAIL` because exact-source-size content is too small for reliable face/detail reading
- only the passed map candidate is persisted; rejected HUD/journal candidate binaries and QA boards are absent from the current tree; nothing was runtime-activated
- the reconstruction handoff under `design/reference/moru-v2/native-master-handoff-v1/` is retained as provenance/acceptance evidence; the accepted neutral master now lives under `design/reference/moru-v2/native-master-v1/`
- the six `neutral / LIGHT / base / static` usage contexts are now persisted under `design/reference/moru-v2/native-usage-context-v1/` and derive only from the accepted native neutral master; board-crop upscaling remains forbidden
- the neutral source-resolution/clean-transparency blocker, neutral usage-context/mobile-edge baseline, LIGHT/WARM_DUSK/DARK lighting family, and expression/affinity semantic source family are closed; MORU-02 repository-side semantic/cross-family QA is complete
- 2026-09-13 neutral attempt 1: FAIL_MORU_IDENTITY_LOCK; candidate was 1122×1402 RGB with baked checkerboard, SHA-256 3c9ca6ddbc0f53a4dc40f1d335ac040646b7d1c3cf12c5f44de2ef7d25ed61da
- face/eye/mouth, hood/sprout silhouette, costume/leaf layering, satchel/compass, hands/boots and rendering drifted from the exact canonical
- rejected binary/QA evidence was not committed; do not repeat the same prompt or fan out variants
- 2026-09-14 accepted neutral v1: 1280×1600 genuine-RGBA PNG `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692` + 6-layer ORA `2b64df529c308039fda608dbbde7514e7384b89e93ff76f384f98beb8e122505`; all acceptance-contract checks PASS
- accepted source/manifest/QA: `design/reference/moru-v2/native-master-v1/`; fresh remote checkout binary re-verification PASS
- 2026-09-14 `PASS_MORU_02_NEUTRAL_USAGE_V1`: map 512², HUD 768², encounter 1024×1280, result 1280×1600, Companion 1024×1280 and journal 768² genuine-RGBA assets persisted under `design/reference/moru-v2/native-usage-context-v1/`; all target dimensions, alpha, distinct framing, transparent-side bleed and required feature-presence gates PASS
- minimum-size mobile metrics now clear the prior source-crop blocker: map @48dp visible-content 43.406dp / face 7.289dp / sprout 7.375dp; HUD @56dp 49.948dp / face 13.524dp / sprout 13.685dp / scarf 9.016dp; journal @56dp 49.875dp / face 14.426dp / sprout 14.597dp / scarf 9.617dp
- fresh checkout `PASS_REMOTE_BINARY_REVERIFY`: all six asset SHA-256 values, dimensions, RGBA alpha 0–255 and QA artifacts match the production manifest; accepted usage commit `c2e17ff28a66cb6b256ae82a77ea4687a0d44a2e`
- 2026-09-14 `PASS_MORU_02_LIGHTING_FAMILY_V1`: canonical LIGHT/WARM_DUSK/DARK reference row calibrated a deterministic Lab transfer; 12 WARM_DUSK/DARK assets across all six usage contexts PASS with LIGHT-identical alpha/geometry and face/sprout readability preserved; evidence: `design/reference/moru-v2/native-lighting-family-v1/`
- lighting family fresh-checkout binary re-verification PASS; runtime activation remains false
- expression/affinity authoring handoff is ready at `design/reference/moru-v2/native-semantic-authoring-handoff-v1/`: 6 exact expression crops + 4 exact affinity crops, accepted neutral ORA layer inventory, compact authoring plan (5 new expression masters + 3 affinity overlay stages), acceptance contract and QA board; crops are guide-only and must not be upscaled into native masters
- 2026-09-14 `PASS_MORU_02_SEMANTIC_FAMILY_V1`: five non-neutral 1280×1600 RGBA expression masters + three restrained 1280×1600 affinity overlays persisted under `design/reference/moru-v2/native-semantic-family-v1/`; expression alpha is bit-identical to neutral, silhouette IoU 1.0, face-anchor deviation 0, changed-outside-allowed pixels 0, and usage/lighting algorithms reproduce their accepted authorities exactly
- HUD 64dp pairwise expression difference gate PASS (minimum changed fraction `0.00400600901352028`); LIGHT/WARM_DUSK/DARK face readability and all expression×affinity Companion alpha/edge gates PASS
- semantic family fresh-checkout `PASS_REMOTE_BINARY_REVERIFY`: five expression and three affinity SHA-256 values, dimensions/modes/alpha properties, four QA boards, production manifest state and fallback `neutral/LIGHT/base/static` all match; runtime activation remains false
- do not remake the neutral, usage, lighting or semantic source families; proceed only to MORU-03 resolver/export activation-readiness checks

### DT-DES-MORU-02 · Actual Android-size QA

Status: **DONE — PASS_MORU_02_SEMANTIC_FAMILY_V1 + fresh remote binary re-verification PASS; not activated**

Checks:
- 48 / 56 / 64 dp map/HUD read
- six semantic expressions
- LIGHT / WARM_DUSK / DARK
- base / familiar / trusted / best_friend invariance
- transparent edge/halo

The native neutral gate, six-context Android-size/transparent-edge baseline, deterministic LIGHT/WARM_DUSK/DARK lighting family, five non-neutral expression masters, and familiar/trusted/best_friend restrained affinity overlays now pass. Cross-family Companion alpha/edge invariance, HUD 64dp expression distinction, expression×lighting readability, exact usage/lighting reproduction and fallback `neutral / LIGHT / base / static` all pass. Evidence is persisted under `design/reference/moru-v2/native-semantic-family-v1/` with fresh remote binary re-verification. The earlier reference-derived map/HUD/journal results remain historical evidence only. **MORU-02 is complete.** Runtime activation remains blocked until MORU-03 resolver/export readiness passes.

### DT-DES-MORU-03 · Semantic manifest activation readiness

Status: **DONE — PASS_MORU_03_ACTIVATION_READINESS_V1 + persisted-manifest remote re-verification PASS; runtime not activated**

Checks:
- `neutral / LIGHT / base / static` fallback
- v1 rollback retained until v2 resolver/fallback tests pass
- no silent repoint of legacy profile
- semantic profile remains `companion.moru.canonical.v2`

Current checkpoint:
- `PASS_MORU_03_ACTIVATION_READINESS_V1`: all `6 expressions × 3 lighting × 4 affinity × 6 usage = 432` combinations were materialized ephemerally in a fresh GitHub Actions checkout; repository persistence is source authorities + deterministic recipe + expected RGBA/alpha pixel hashes rather than 432 duplicate PNGs
- dimensions / alpha invariance / authority-relative edge contract PASS `432/432`; the five derived usage contexts retain zero transparent-side edge error `360/360`; accepted neutral/base authority reproduces pixel-exactly for all six usage contexts × three lightings `18/18`
- resolver fault-injection PASS: exact `432`, single-exact-missing `432`, exact+same-expression-LIGHT/base-missing `432`, terminal legacy-v1 fallback `432`, invalid-enum fail-closed `4`
- coverage evidence: `design/reference/moru-v2/native-semantic-export-readiness-v1/semantic-resolver-export-manifest.v1.json`; QA metrics: `.../qa/moru_moru03_activation_readiness_metrics_v1.json`
- fresh persisted-manifest verification: `PASS_REMOTE_MANIFEST_REVERIFY`; exact 432-key set, dimensions, hashes/recipes, source-authority hashes, fallback counts and runtime-inactive state all match
- first MORU-03 dry-run failed only because the derived-context `edge == 0` measurement was incorrectly applied to the byte-exact `result_large` authority; corrected gate requires exact equality to the accepted result-large authority per lighting. No asset, source authority or quality threshold was modified
- `runtime_binding_mutated=false`, `runtime_activation=false`; Android packaging/binding and promotion remain a separate Development task, with v1 rollback preserved and physical-device outdoor readability still a Human Gate

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

Status: **HUMAN GATE — REVIEW PACK READY; PHYSICAL PASS PENDING**

Physical-device review for map/HUD/marker/discovery readability. Emulator screenshots cannot close this gate.

Prepared review evidence:
- checklist / verdict contract: `design/review/final-human-gates-v3/outdoor/R_B_OUTDOOR_READABILITY_REVIEW.md`
- 54-cell human matrix: `design/review/final-human-gates-v3/outdoor/r-b-outdoor-readability-matrix.v1.json`
- static accepted-asset preflight: `design/review/final-human-gates-v3/outdoor/outdoor-preflight.html`
- `physical_pass_claimed=false`; only real-device outdoor review may close R-B

### DT-DES-MOTION-01 · M-B motion timing/intensity

Status: **DONE — HUMAN APPROVED / LOCKED: M-B2 CALM REACTIVE**

The selected contract is `M-B2 Calm Reactive`.

- idle: 2600 ms, `cubic-bezier(.4,0,.2,1)`, cap 1.0 dp map / 1.2 dp HUD, scale `1.000 ↔ 1.008`, rotation ±0.4°, loop
- `clue_found`: 720 ms one-shot with 90 / 210 / 420 ms anticipation/accent/settle
- `resolved`: 900 ms one-shot with 100 / 240 / 560 ms anticipation/accent/settle
- reduced-motion: static semantic asset; no translation/scale/rotation/bounce; optional opacity crossfade ≤120 ms
- detailed contract: `design/review/final-human-gates-v3/motion/M_B_MOTION_REVIEW.md`

No new Moru art or runtime implementation was performed by Design.

### DT-DES-ID-01 · ID-A app icon / logo lock

Status: **DONE — HUMAN APPROVED / LOCKED: ID-A2 SPROUT GATE**

The selected identity mark is `ID-A2 Sprout Gate` with launcher `mark-only`.

Locked layers:
- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/foreground-light.svg`
- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/foreground-dark.svg`
- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/background-light.svg`
- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/background-dark.svg`
- `design/review/final-human-gates-v3/id-a/candidates/id-a2-sprout-gate/monochrome.svg`

Adaptive-icon design handoff remains 108×108 dp layers, centered 66×66 dp guaranteed safe zone, and 48–66 dp core mark target. W1/W2 wordmark directions remain non-blocking future options; no final typeface is locked. Android resource binding was not performed.

## Current next action

1. Keep Old Ginkgo and every accepted Moru canonical/native/semantic authority immutable. Moru design-side MORU-01 / MORU-02 / MORU-03 remain complete.
2. Development may consume the locked `M-B2 Calm Reactive` motion contract and `ID-A2 Sprout Gate` adaptive-icon layer set as separate implementation handoffs.
3. Human performs R-B on the physical Android device using the 54-cell matrix. Static/emulator preflight cannot produce `PASS_RB_PHYSICAL_DEVICE`.
4. Until R-B is physically reviewed, keep outdoor readability status pending. This design branch does not modify runtime resources, `main`, `feat/emulator-test-harness`, or PR #10.
