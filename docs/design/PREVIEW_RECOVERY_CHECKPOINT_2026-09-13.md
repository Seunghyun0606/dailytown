# Design preview recovery checkpoint — 2026-09-13

Status: **SOURCE RECOVERY COMPLETE / PREVIEW REPRODUCTIONS REJECTED**

This checkpoint records one bounded continuation run on
`design/production-consolidated-v2`. It does not change the approved Design
Baseline v2, activate runtime assets, or persist rejected image candidates.

## Starting state

- branch: `design/production-consolidated-v2`
- starting HEAD: `11f2db3252487568c6be233e547aeddaee649818`
- main at start: `b9551dff148ab3458b21aa83a244de90b0a49f05`
- branch relation at start: ahead 12 / behind 0
- exact attachment persistence: 7/7 complete and intentionally not repeated
- OG-04 / OG-05: existing PASS / not activated; intentionally not reproduced
- Companion / Records: existing design-side PASS; no new concept board

## One-time native-source recovery sweep

The sweep covered the branch working tree, all reachable Git image blobs,
the current ai-remote temporary workspace, and the already-persisted
session-2026-09-12 attachment set. No new binary attachment was supplied in
this continuation turn.

| Source family | Result |
| --- | --- |
| OG-02 768×768 genuine-transparent folded-note master | **NOT FOUND** |
| OG-03 512×512 genuine-transparent ginkgo-leaf master | **NOT FOUND** |
| Moru Candidate 3 native transparent master | **NOT FOUND** |

The sweep is complete and must not be repeated without a newly supplied
source location or attachment.

## Preview-only reproduction results

Exactly one candidate was generated for each asset family. All three were
rejected before repository persistence.

### OG-02 folded note

- requested delivery: 768×768 genuine RGBA
- generated source: 1367×1151 RGB, no alpha channel
- result: **NOT READY / REJECT**
- failures:
  - baked checkerboard pixels instead of transparency
  - locked folded-envelope silhouette changed into a layered/open note
  - fold geometry and botanical arrangement changed
  - new large Korean body text was introduced
  - therefore transparent-corner, matte/halo, silhouette, motif/text and
    high-density gates do not pass

### OG-03 ginkgo leaf

- requested delivery: 512×512 genuine RGBA
- generated source: 1254×1254 RGB, no alpha channel
- result: **NOT READY / REJECT**
- failures:
  - baked checkerboard pixels instead of transparency
  - fan silhouette became wider and more segmented
  - upper lobe/notch structure changed
  - stem became materially longer/thicker
  - palette remained broadly warm-yellow, but the material/highlight treatment
    became glossier and sharper than the locked reference

### Moru Candidate 3 neutral

- generated source: 1122×1402 RGB, no alpha channel
- result: **NOT READY / REJECT**
- failures:
  - baked checkerboard pixels instead of transparency
  - sprout leaf shape/scale and hood outer contour drifted
  - shoulder leaf layering, satchel/compass scale and boot proportions changed
  - silhouette became narrower and more symmetrical
  - rendering became sharper/cleaner-lined than the canonical soft raster texture
  - identity lock and alpha gate therefore do not pass

No expressions, lighting variants, affinity variants, map avatars, HUD
portraits, Companion portraits, encounter art, result art, or journal crops
were generated.

## Persistence decision

- rejected binaries: **NOT COMMITTED**
- canonical/reference/runtime asset paths: **UNCHANGED**
- documentation-only checkpoint: **COMMIT ALLOWED**
- Human Gate: **NOT OPENED**, because no candidate reached PREVIEW_READY

## Safe next action

Do not repeat these prompts. Continue only when one of the following changes:

1. a genuine native transparent source is newly supplied, or
2. a transparency-capable identity-preserving edit/extraction path is available
   and a single targeted correction is explicitly authorized.

After that change, resume with OG-02 only, run alpha/silhouette/text/material QA,
and proceed to OG-03 and Moru only after each preceding preview reaches READY.

## OG-02 source-derived restoration validation

Continuation starting HEAD: `70a7b9e6b624b4babf2fbec6f7deea0e41433c9e`.

The one-time capability preflight found no preinstalled restoration stack, so
an isolated temporary CPU environment was created outside the repository. The
validated pipeline used the official `RealESRGAN_x4plus` v0.1.0 weights
(BSD-3-Clause) through Spandrel 0.4.1 (MIT) and PyTorch 2.8.0+cpu. RGB and
alpha were split; RGB was restored at 4× and resized to 768×768, the locked
source alpha was independently resampled to 768×768, and the result was
recombined using premultiplied alpha before straight-RGBA PNG serialization.

Exactly one OG-02 candidate was produced in the ai-remote temporary directory.
It is not included in Git.

Recorded candidate status:

- `native_source_recovered: false`
- `provenance: source_derived_restoration`
- `exact_source: false`
- `preview_only: true`

Deterministic binary and silhouette checks passed: 768×768 PNG color type 6,
alpha range 0–255, all four corners alpha 0, no transparent-pixel RGB,
no checkerboard or opaque matte, silhouette IoU 1.0, centroid shift 0,
bounding-box change 0, orientation change 0, and no new disconnected alpha
components.

The visual edge gate failed. On transparent, cream, dark and map-heavy
previews, restoration-created yellow/red/black chromatic fringe is visible
along fractional-alpha boundaries. Edge-band premultiplied RGB difference
against the locked reference measured mean 9.401, p95 24.857 and max 92.980
on an 8-bit channel scale. The model also sharpened botanical veins, cord and
fold shading relative to the soft 128×128 authority; no new text, mark, seal,
cord, leaf, fold or motif was introduced.

Decision: **OG02_RESTORATION_NOT_READY / REJECT**.

- candidate binary: temporary only, not committed
- canonical/reference/runtime paths: unchanged
- OG-03 and Moru: not started
- next minimum work: manual raster repaint constrained to the locked silhouette,
  or a different alpha-aware restoration/matting tool that constrains RGB at
  fractional-alpha edges

## OG-02 EDSR alpha-constrained continuation

Continuation starting HEAD: `a4e500bcb95bc1392616a0be7e290b78a2d9781d`.

One different source-preserving restoration method was attempted after the
Real-ESRGAN edge failure. The rejected Real-ESRGAN candidate was not used as
input. The locked 128×128 RGBA WebP was processed directly with the
`EDSR_x4.pb` model through OpenCV contrib 4.12.0.88 DNN Super Resolution
(Apache-2.0). RGB was edge-bled outside the alpha support, restored 4×, resized
to 768×768, then constrained by reprojecting locked-source RGB across a 6–18px
boundary band. Alpha remained an independent 6× upscale of the locked source
and was recombined using premultiplied alpha.

Exactly one EDSR alpha-constrained candidate was created outside the
repository. It retains:

- `native_source_recovered: false`
- `provenance: source_derived_restoration`
- `exact_source: false`
- `preview_only: true`

Binary and silhouette gates passed: 768×768 PNG color type 6, alpha 0–255,
four corners alpha 0, no checkerboard or opaque matte, silhouette IoU 1.0,
centroid/bounding-box/orientation change 0, and no new disconnected alpha
components.

Edge-band premultiplied RGB difference improved from the prior Real-ESRGAN
mean/p95/max of 9.401/24.857/92.980 to 4.399/14.224/54.678. The visual gate
still failed: yellow, red and dark pixel specks remain visible around the paper
and botanical boundary on transparent, dark and map-heavy previews. The
remaining contamination is present in the low-resolution locked WebP boundary;
removing it further while preserving the exact low-resolution silhouette would
require repainting rather than restoration.

Decision: **OG02_RESTORATION_NOT_READY / AUTOMATED RESTORATION EXHAUSTED**.

- EDSR candidate binary and QA artifacts: temporary only, not committed
- canonical/reference/runtime paths: unchanged

## OG-02 exact-candidate edge-reconstruction continuation

Continuation starting local HEAD:
`0c35163737b1ac54fa830cbc7c5886f5bbbd5afb`.
The remote branch HEAD at preflight remained
`a4e500bcb95bc1392616a0be7e290b78a2d9781d`; the local branch contained the
documentation-only EDSR checkpoint above and was not reset.

The newly authorized workflow treats the accepted native-size candidate as
the only valid processing input and its alpha as an immutable mask. It
explicitly prohibits another 128px-source upscale, Real-ESRGAN, generative
upscale, and use of either previously rejected restoration candidate.

Requested OG-02 input:

- dimensions: 768×768 RGBA PNG
- SHA-256:
  `59171439fe9fefea4a5040fa9034a213983dc360496c3474767b8f58e79e2e92`

A hash-specific, read-only lookup was performed across current session
scratch, the ai-remote workspace, repository image files, temporary outputs,
and the existing `.codex-transfer/` content. The checksum remains present in
manifests, but no file bytes matching it are available. The committed 128×128
WebP reference was not used as a processing source.

Decision: **FAIL_EDGE_RECONSTRUCTION / SOURCE_BYTES_UNAVAILABLE**.

- failure stage: source-input verification, before RGB/alpha separation
- alpha/silhouette modification: not performed
- candidate/QA artifacts: not produced
- canonical/reference/runtime paths: unchanged
- OG-03 and Moru: not started
- next minimum work: attach or otherwise place the exact OG-02 PNG bytes in
  the current ai-remote session; verify the stated SHA-256, then run one
  alpha-first edge-RGB reconstruction and QA pass


## Exact-candidate alpha-first edge reconstruction — final result

Starting HEAD: `5090462600ad6789c8946ad3a10363f28c997424`.

Both newly attached PNG files were verified before processing:

- OG-02: 768×768 PNG RGBA, SHA-256 `59171439fe9fefea4a5040fa9034a213983dc360496c3474767b8f58e79e2e92`
- OG-03: 512×512 PNG RGBA, SHA-256 `ec37263d8e736809629d6d3d304712df1148110794a61e2b36286108f490d064`

No repository/workspace source-recovery sweep was repeated. The 128×128 and
96×96 references, rejected Real-ESRGAN/EDSR candidates, generative tools and
upscale models were not used.

### OG-02 result

Decision: **PASS_EDGE_RECONSTRUCTION**.

The exact source was processed at 768×768. Alpha was immutable. Exterior
fractional-edge RGB within 20px was replaced by nearest fully opaque interior
RGB, and a 12px transparent-side color-bleed ring was added for safe straight-
alpha filtering. Fully opaque interior RGB and deeper fractional RGB remained
unchanged.

- corrected SHA-256: `2adc914f6ca2e5648339943bc02afe62059948d52a672e382fad69b5e6c08869`
- alpha changed pixels: 0
- silhouette IoU: 1.0
- opaque/deep-interior RGB changed pixels: 0
- cream/dark/map-heavy QA: PASS
- 48/64/96/144px and 432px QA: PASS
- exact source, corrected v4 candidate and QA: persisted under `production/runtime-v4/`
- runtime activation: not performed

### OG-03 result

Decision: **FAIL_EDGE_RECONSTRUCTION**.

The exact source was processed once at 512×512 using the same immutable-alpha
method. Binary invariants passed, but the visual gate failed.

- temporary corrected SHA-256: `ef8f589ebf6a76a0be653e32491675502fddb4ee969f1e8ea0886ec7c777a360`
- alpha changed pixels: 0
- silhouette IoU: 1.0
- opaque/deep-interior RGB changed pixels: 0
- dark context: fringe reduced
- cream/map-heavy/high-density: FAIL — legitimate warm-brown perimeter color
  was removed in places and replaced by a bright yellow jagged edge
- rejected OG-03 source/candidate/QA binaries: not committed
- next minimum work: locked-alpha manual edge retouch using the existing
  boundary palette; no new fan silhouette, stem, vein, highlight or material

### OG-06 gate

OG-06 remains **OPEN** because OG-03 did not pass source-quality visual QA.
OG-04 and OG-05 were not regenerated. Moru was not touched.

## OG-03 exact-source persistence and selective perimeter retouch

Continuation starting remote HEAD:
`2aa61b3c4a21280b7149e953eb0f9d1a2dcfd24b`.

The newly attached OG-03 PNG was verified before processing as 512×512 PNG
RGBA with SHA-256
`ec37263d8e736809629d6d3d304712df1148110794a61e2b36286108f490d064`,
alpha range 0–255 and four transparent corners. The bytes were first preserved
unchanged as the runtime-v5 `immutable_exact_source`; source-persistence
commit `310f95b0214262693e5550d13a7fdcf56dec33e9` was pushed and the remote
binary hash was verified.

Exactly one selective candidate was produced. It did not reuse the previous
rejected candidate as input, did not upscale, and did not use a generative
model. Local boundary-palette analysis classified and locked 14,148
warm-brown perimeter pixels and 7,428 warm-yellow transition pixels. No
visible-support pixel satisfied the conservative contamination-outlier rule,
so no visible RGB was recolored. A 12px transparent-side RGB bleed ring was
extended outward from valid local boundary colors to improve straight-alpha
runtime filtering.

Deterministic result:

- candidate SHA-256:
  `775059cd22db997234e8012534a7e73f9a347d8f1208117c3babee1c9ca1f366`
- changed RGB pixels: 19,189, all at alpha 0
- changed visible-support RGB pixels: 0
- changed alpha pixels: 0
- silhouette IoU: 1.0
- warm-brown/warm-yellow/opaque-interior/vein/highlight/material changed pixels: 0
- cream, dark and map-heavy QA: PASS
- 32/48/64/96px and 288px high-density QA: PASS
- decision: **PASS_SELECTIVE_EDGE_RETOUCH**

The exact source, corrected v5 candidate, metrics and QA boards are persisted
under `design/reference/discovery/old-ginkgo-note/production/runtime-v5/`.
The previous nearest-opaque candidate remains rejected and uncommitted.

OG-02/03/04/05 now all pass design-side QA, so OG-06 is
**CLOSED — DESIGN_RUNTIME_CANDIDATE_PACK_READY**. Android runtime activation
was not performed; physical-device outdoor readability remains a Human Gate.
Moru was not touched.
