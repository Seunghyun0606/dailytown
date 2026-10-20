# Daily Town — Moru Source Sufficiency v2

> Status: **AUDITED / MORU-03 ACTIVATION READINESS PASS — design-side export/resolver complete; runtime promotion pending**
>
> Visual authority: `docs/design/DESIGN_BASELINE_V2.md`
>
> Canonical source SHA-256: `541b53464bbc589d86323faa3a7dbc8790cf9ab9573e0dd66812986c4a430500`

## 1. Purpose

This document answers one production question only: how far can the approved Moru Candidate 3 composite board be taken into runtime assets without inventing a new design?

The answer is:

- the board is strong enough to lock identity, proportions, expression intent, lighting intent, affinity decoration direction and 48dp recognizability;
- it is **not** a native-resolution transparent master pack;
- small/medium usage-context candidates can be technically derived for QA, but large production contexts require same-design native raster reproduction rather than simple upscaling.

No alternate Moru family or style is introduced here.

## 2. Exact board crop inventory

The approved `1122×1402` board currently exposes these approximate production-reference regions:

| Reference | Crop size |
| --- | ---: |
| front turnaround | 340×540 px |
| 3/4 walking | 330×565 px |
| back turnaround | 315×545 px |
| neutral expression | 162×215 px |
| happy expression | 170×215 px |
| curious expression | 170×215 px |
| surprised expression | 170×215 px |
| clue_found expression | 180×215 px |
| resolved expression | 200×215 px |
| LIGHT reference | 171×192 px |
| WARM_DUSK reference | 177×192 px |
| DARK reference | 173×192 px |
| affinity base | 121×176 px |
| affinity familiar | 121×176 px |
| affinity trusted | 128×176 px |
| affinity best_friend | 144×176 px |
| 48dp read-test strip | 719×172 px |

These regions are framing/reference material, not equivalent to the master canvases required by `MORU_PRODUCTION_EXPORT_V2.md`.

## 3. Usage-context sufficiency

### `map_avatar`

Target: 512×512 master, runtime review at 48 / 56 / 64dp.

Assessment: **reference-derived QA candidate feasible**.

The canonical front/3-quarter artwork has enough source information to evaluate silhouette, sprout, hood, scarf and compact body mass at small display size. A technically isolated candidate may be used for mobile QA, but it must remain `reference_candidate` until transparent edge quality is checked.

### `hud_portrait`

Target: 768×768 master, 56 / 64 / 72dp presentation.

Assessment: **reference-derived QA candidate feasible with source-quality caution**.

A head/upper-torso crop is visually large enough for layout and expression-read QA, but native detail is still below the required master size. Do not claim an upscaled board crop is a native 768×768 master.

### `journal_crop`

Target: 768×768 or lossless crop master.

Assessment: **reference-derived QA candidate feasible** for small paper-artifact usage.

This context tolerates tighter crops and lower visual size, but transparent edge and A3 paper contrast must still be checked.

### `encounter_halfbody`

Target: 1024×1280 transparent PNG.

Assessment: **native same-design raster reproduction required**.

The board crop does not contain enough native pixel information for a high-quality encounter master. Upscaling is allowed only as a temporary review aid, not as production completion.

### `result_large`

Target: 1280×1600 transparent PNG.

Assessment: **native same-design raster reproduction required**.

This is the strongest source-quality blocker. The approved front/3-quarter reference must be reproduced at native production resolution while keeping anatomy, costume, pose language and rendering family unchanged.

### `companion_portrait`

Target: 1024×1280 transparent PNG.

Assessment: **native same-design raster reproduction preferred/required before production activation**.

A reference crop can validate layout; a final relationship-notebook portrait should be authored at native resolution from the locked Candidate 3 design.

## 4. Expression / lighting / affinity interpretation

The six expression thumbnails, three lighting samples and four affinity close-ups are **semantic visual references**.

They are sufficient to lock:

- eye/mouth/posture intent;
- LIGHT / WARM_DUSK / DARK color and readability intent;
- AF-1 + restrained AF-3 progression;
- BF-B keepsake density;
- anatomy/costume invariance.

They are not sufficiently large to serve as final production masters by simple extraction.

Therefore the production rule is:

1. reproduce the same Candidate 3 design at native target resolution;
2. compare every produced asset back to the exact board;
3. reject silhouette, costume, sprout, hood, scarf, satchel, boot or face-proportion drift;
4. do not add new evolution/costume concepts during reproduction.

## 5. Current production decision

Moru production is no longer blocked by design uncertainty or by the neutral-master gate. The accepted genuine-RGBA layered neutral master is persisted under `design/reference/moru-v2/native-master-v1/`.

The accepted neutral master has produced the passing six-context native usage family, the deterministic LIGHT/WARM_DUSK/DARK lighting family, and the accepted native semantic source family under `design/reference/moru-v2/native-semantic-family-v1/`. MORU-02 expression/lighting/affinity production and cross-family QA are complete. Do not regenerate or replace any accepted authority; the next valid work is MORU-03 resolver/export activation readiness, with runtime activation still blocked.

Recommended order:

1. keep `design/reference/moru-v2/native-master-v1/` immutable as the accepted neutral authority;
2. keep `design/reference/moru-v2/native-usage-context-v1/` immutable as the passing neutral usage/mobile-edge baseline;
3. keep `design/reference/moru-v2/native-lighting-family-v1/` and `design/reference/moru-v2/native-semantic-family-v1/` immutable as the accepted lighting/semantic authorities;
4. in MORU-03 verify resolver/export coverage and exact fallback while retaining the legacy v1 rollback pack;
5. prepare activation readiness without silently repointing the legacy runtime profile.

## 6. QA gate

Do not mark `DT-DES-MORU-01` complete until the following exist as persisted binary assets:

- transparent master family at required native canvas sizes;
- no paper/background residue or strong edge halo;
- actual 48 / 56 / 64dp map/HUD test;
- expression consistency;
- LIGHT / WARM_DUSK / DARK consistency;
- base / familiar / trusted / best_friend invariance;
- checksum + semantic manifest;
- previous v1 pack retained as rollback.

Outdoor readability and M-B final motion remain Human Gates.

## 7. Neutral identity-lock attempt 1 — 2026-09-13

Result: **FAIL_MORU_IDENTITY_LOCK**.

- The exact remote canonical was re-verified before the attempt: 1122×1402 RGBA, SHA-256 541b53464bbc589d86323faa3a7dbc8790cf9ab9573e0dd66812986c4a430500, fully opaque.
- Deterministic board extraction remains SOURCE_INSUFFICIENT for a native master family. The usable front-view reference is about 340×540 and is composited with paper, typography, garden and ground pixels.
- One reference-guided neutral/front/full-body reproduction was evaluated. Candidate facts: 1122×1402 RGB, SHA-256 3c9ca6ddbc0f53a4dc40f1d335ac040646b7d1c3cf12c5f44de2ef7d25ed61da.
- Alpha gate failed: the file had no alpha channel and the checkerboard was baked into RGB pixels.
- Identity gate failed: face/eye/mouth treatment, hood/sprout contour, flower and leaf layering, garment rendering, satchel/compass details, hands/boots and the overall raster finish differed from the exact canonical.
- Usage-context, expression, lighting and affinity fan-out was stopped. The rejected PNG and its QA images were not committed to canonical, reference or runtime paths.

Do not repeat the same reproduction prompt. Resume only with a production path that can provide genuine alpha and materially stronger exact-reference fidelity, or with a human-supplied native/layered transparent Moru source.

## 8. Deterministic small-context QA and native reconstruction handoff — 2026-09-13

No generative redraw or super-resolution was used. The canonical front reference was isolated with deterministic crop, manual mask, paper-background removal, alpha cleanup, transparent-side RGB bleed, canvas padding and resampling only.

| Context | Canvas | Review sizes | Result |
| --- | ---: | --- | --- |
| `map_avatar` | 512×512 RGBA | 48 / 56 / 64dp | `REFERENCE_QA_PASS` |
| `hud_portrait` | 768×768 RGBA | 56 / 64 / 72dp | `REFERENCE_QA_FAIL` — exact-source-size content too small without enlargement |
| `journal_crop` | 768×768 RGBA | 56 / 64 / 72dp | `REFERENCE_QA_FAIL` — exact-source-size content too small without enlargement |

The map output remains a persisted `REFERENCE_DERIVED_CANDIDATE`; Development may use it only in an explicitly temporary/reference profile. HUD and journal candidates failed and are not persisted in the current tree. None are native production masters or runtime-active.

The human/layered raster reconstruction package is persisted at `design/reference/moru-v2/native-master-handoff-v1/`. It contains the exact canonical, neutral crop, silhouette, face and anchor guides, hood/sprout contour, scarf/satchel/boots reference, sampled palette, target canvases, alpha requirements and forbidden-drift checklist.

The native neutral gate for `DT-DES-MORU-01` is passed, and the six-context `neutral / LIGHT / base / static` usage/mobile-edge baseline also passes. MORU-02 remains the active gate because expression/lighting/affinity consistency is still incomplete; MORU-03/runtime promotion remain blocked.


## 9. Native neutral master v1 PASS — 2026-09-14

Result: **PASS_MORU_NATIVE_NEUTRAL_V1**.

- Accepted neutral master: `design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png`
- Canvas / mode: 1280×1600 RGBA with genuine alpha 0–255
- PNG SHA-256: `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692`
- Layered OpenRaster source: `design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.ora`
- ORA SHA-256: `2b64df529c308039fda608dbbde7514e7384b89e93ff76f384f98beb8e122505`; 6 layers
- Acceptance metrics: silhouette IoU 0.996370, hood IoU 0.996897, sprout IoU 0.988035, mean/max normalized contour deviation 0.000219/0.002553, palette ΔE76 median/max 0.466/1.262, face-anchor deviation 0, visible residue outside reference 0.
- Reconstruction was deterministic/non-generative: committed silhouette mask for alpha cleanup, committed reference cutout RGB with deterministic transparent-side edge bleed, and source-supported raster texture only.
- Accepted asset commit: `38445e229c6937e0856a73071eb8fc4e9007eb4c`.
- Fresh remote checkout re-verification: `PASS_REMOTE_BINARY_REVERIFY`; PNG/ORA/QA SHA values match the manifest, PNG IHDR is 1280×1600 8-bit RGBA, and ORA ZIP/mimetype/6-layer integrity passed. Evidence: `design/reference/moru-v2/native-master-v1/qa/remote_binary_verification.v1.json`.
- No expression, lighting, affinity or usage-context fan-out was generated in this gate. Runtime activation remains false.

Next: preserve this accepted neutral master and the passing neutral usage family; continue MORU-02 with expression/lighting/affinity production and consistency QA only.

## 10. Native neutral usage-context baseline PASS — 2026-09-14

Result: **PASS_MORU_02_NEUTRAL_USAGE_V1**.

- Source authority: accepted 1280×1600 neutral master SHA-256 `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692` only; no attachment, board crop, free-form generation or new semantic micro-detail was used.
- Persisted family: `design/reference/moru-v2/native-usage-context-v1/`.
- Six `neutral / LIGHT / base / static` context masters: `map_avatar` 512×512, `hud_portrait` 768×768, `encounter_halfbody` 1024×1280, `result_large` 1280×1600, `companion_portrait` 1024×1280, `journal_crop` 768×768; all genuine RGBA with alpha 0–255.
- Context-specific framing is distinct; `result_large` is a byte-for-byte copy of the accepted neutral master rather than a re-encoded lookalike.
- Transparent-side bleed QA PASS for derived contexts. Cream/dark/map-heavy review boards and framing evidence are persisted under `native-usage-context-v1/qa/`.
- Minimum-size metrics: map @48dp visible content 43.406dp / face 7.289dp / sprout 7.375dp; HUD @56dp 49.948dp / face 13.524dp / sprout 13.685dp / scarf 9.016dp; journal @56dp 49.875dp / face 14.426dp / sprout 14.597dp / scarf 9.617dp.
- The previous no-upscale reference-derived HUD/journal failures remain valid historical evidence; the accepted native source is what closes those size/readability blockers.
- Accepted usage-family commit: `c2e17ff28a66cb6b256ae82a77ea4687a0d44a2e`.
- Fresh remote checkout result: `PASS_REMOTE_BINARY_REVERIFY`; all six persisted PNG SHA-256 values, dimensions, RGBA modes/alpha ranges and QA SHA values match `design/export-spec/moru-production-manifest.v2.json`. Evidence: `design/reference/moru-v2/native-usage-context-v1/qa/remote_binary_verification.v1.json`.
- No expression, WARM_DUSK/DARK lighting, affinity progression or runtime activation was performed. Therefore full MORU-02 is still **IN PROGRESS**, not complete.

Next: produce and validate the locked semantic expression/lighting/affinity family, then verify fallback coverage before MORU-03.

## 10. Native lighting family + semantic authoring handoff — 2026-09-14

Lighting result: **PASS_MORU_02_LIGHTING_FAMILY_V1**.

- Source authority: accepted `neutral / LIGHT / base / static` native usage family only.
- Canonical lighting-row crops calibrated deterministic Lab-space transfer from LIGHT to WARM_DUSK/DARK.
- 12 new assets were produced for all six usage contexts × two new lighting states.
- Alpha and geometry are unchanged from each LIGHT source; no expression or affinity pixels were invented.
- WARM_DUSK warmth/readability and DARK luminance/face/sprout texture checks PASS across all six contexts.
- Evidence: `design/reference/moru-v2/native-lighting-family-v1/`; fresh remote binary re-verification PASS.

Expression/affinity art is **not** fabricated from the low-resolution board crops. Instead, `design/reference/moru-v2/native-semantic-authoring-handoff-v1/` contains six exact expression reference crops, four exact affinity reference crops, the accepted 6-layer ORA inventory, an authoring/acceptance contract and a QA reference board. The compact authoring source plan is five new 1280×1600 LIGHT/base expression masters plus three restrained affinity overlay stages; lighting and six usage contexts remain deterministic fan-out steps after semantic master acceptance.

Next valid production step: author the five non-neutral expression masters against the accepted neutral identity, then the three affinity overlay stages, run cross-family identity/mobile/fallback QA, and only after full MORU-02 PASS prepare MORU-03. Runtime activation remains false.


## 11. Native semantic family v1 PASS — 2026-09-14

Result: **PASS_MORU_02_SEMANTIC_FAMILY_V1**.

- Accepted family: `design/reference/moru-v2/native-semantic-family-v1/`; runtime activation remains false.
- Five new 1280×1600 RGBA LIGHT/base expression masters: `happy`, `curious`, `surprised`, `clue_found`, `resolved`; `neutral` reuses the accepted neutral master.
- Expression identity locks: alpha bit-identical to neutral, silhouette IoU 1.0, face-anchor deviation 0, changed pixels outside allowed semantic regions 0.
- Three 1280×1600 restrained affinity overlays: `familiar`, `trusted`, `best_friend`, confined to keepsake/satchel treatment with anatomy invariant.
- HUD 64dp expression distinction PASS; minimum pairwise changed fraction is `0.00400600901352028`.
- All six expressions retain readable face statistics across LIGHT/WARM_DUSK/DARK.
- Cross-family Companion expression×affinity alpha exactness and transparent-edge error `0.0` PASS.
- Accepted usage fan-out and lighting transfer are reproduced exactly before semantic application, preventing silent algorithm drift.
- Fresh remote checkout result: **PASS_REMOTE_BINARY_REVERIFY**. All five expression SHA-256 values, three affinity SHA-256 values, dimensions, RGBA/alpha properties, four QA boards, production manifest state and `neutral/LIGHT/base/static` fallback contract match. Evidence: `native-semantic-family-v1/qa/remote_binary_verification.v1.json`.
- The first workflow run failed only because of an implementation argument-order error in the one-shot runner; no rejected production binaries were persisted. The framing call was corrected without changing any acceptance threshold, and the stale failure note was removed after PASS.

MORU-02 is now **complete**. Next: MORU-03 semantic resolver/export activation readiness only; Android runtime activation remains a separate Development task.

## 12. MORU-03 semantic export/resolver activation readiness PASS — 2026-09-14

Result: **PASS_MORU_03_ACTIVATION_READINESS_V1**.

- All 432 semantic combinations (`6 expressions × 3 lighting × 4 affinity × 6 usage`) were actually materialized ephemerally in a fresh GitHub Actions checkout.
- Every combination passed target dimensions, accepted-alpha invariance and the corresponding accepted-authority edge contract; maximum edge delta from authority was `0.0`.
- The five derived usage contexts retained zero transparent-side edge error for `360/360` combinations. `result_large` retains the accepted exact-master RGB-edge contract instead of rewriting transparent RGB; accepted edge references are LIGHT `23.764554936968064`, WARM_DUSK `20.741882639022208`, DARK `15.196563639854098`.
- Neutral/base reproduction is pixel-exact against the accepted usage/lighting authorities for `18/18` combinations.
- Resolver fault-injection passed exact `432`, single-exact-missing `432`, exact+same-expression-LIGHT/base-missing `432`, terminal `legacy_v1_static_semantic_fallback` `432`, and invalid-enum fail-closed `4`.
- Persisted coverage: `design/reference/moru-v2/native-semantic-export-readiness-v1/semantic-resolver-export-manifest.v1.json`. The manifest records all 432 semantic keys, expected RGBA/alpha pixel SHA-256 values and deterministic source/composition recipes without storing 432 duplicate production PNGs before the Android packaging decision.
- Fresh checkout persisted-manifest result: **PASS_REMOTE_MANIFEST_REVERIFY**. Coverage manifest SHA-256 `44f82c8f24223cf8eb2132a38c45e37621b8dd89de6d954afbf18473dc375833`; metrics SHA-256 `015cbe98313dccb5d1b64ad72ba32e93b336f77c679f92925d0a316d66caacd3`.
- The first MORU-03 dry-run was rejected because its QA incorrectly imposed derived-context `edge == 0` on the byte-exact `result_large` authority. The corrected authority-relative gate passed without changing any asset, source authority, or quality threshold.
- `runtime_binding_mutated=false` and `runtime_activation=false`. Legacy v1 rollback remains required; silent repoint is forbidden.

Moru design-side production/export readiness is complete through MORU-03. The next valid work is a separate Development session for Android packaging/binding of the separately versioned v2 semantic profile, followed by runtime QA and the physical-device outdoor Human Gate before promotion.
