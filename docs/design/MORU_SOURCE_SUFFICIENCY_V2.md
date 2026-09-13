# Daily Town — Moru Source Sufficiency v2

> Status: **AUDITED / NEUTRAL IDENTITY-LOCK ATTEMPT 1 FAILED — clean native transparent source/master still required**
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

Moru production is no longer blocked by design uncertainty. It is blocked by **source/master resolution and clean transparency**.

The next valid native work is human/layered raster reconstruction from the committed handoff, not concept exploration or another free-form generation attempt.

Recommended order:

1. reconstruct one genuine-RGBA native neutral master from `design/reference/moru-v2/native-master-handoff-v1/`;
2. pass the identity acceptance contract before any native fan-out;
3. derive usage contexts from that accepted native master;
4. run MORU-02, then expressions, lighting and affinity only as required by the resolver/fallback contract;
5. prepare MORU-03 readiness without silently repointing the legacy profile.

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
| `hud_portrait` | 768×768 RGBA | 56 / 64 / 72dp | `REFERENCE_QA_PASS` |
| `journal_crop` | 768×768 RGBA | 56 / 64 / 72dp | `REFERENCE_QA_PASS` |

These remain `REFERENCE_DERIVED_CANDIDATE` assets. They are not native production masters and were not runtime-activated. Development may use them only in an explicitly temporary/reference profile for layout/readability validation.

The human/layered raster reconstruction package is persisted at `design/reference/moru-v2/native-master-handoff-v1/`. It contains the exact canonical, neutral crop, silhouette, face and anchor guides, hood/sprout contour, scarf/satchel/boots reference, sampled palette, target canvases, alpha requirements and forbidden-drift checklist.

`DT-DES-MORU-01` remains blocked until a native neutral master passes that acceptance contract. Consequently MORU-02 native QA and MORU-03 semantic activation readiness remain incomplete, and runtime promotion remains blocked.
