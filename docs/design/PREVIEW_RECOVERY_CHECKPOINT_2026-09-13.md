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
