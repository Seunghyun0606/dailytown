# Design production closure audit — 2026-09-12

Status: **PARTIAL / exact attachment persistence complete; native transparent masters remain blocked**

## Scope and invariants

- Visual authority: `DESIGN_BASELINE_V2.md` and Moru Candidate 3.
- No new design direction, motif, lettering, seal, costume, anatomy, or silhouette is accepted.
- PR #10 and `main` are untouched.
- This audit consolidates only validated design-branch work.

## Branch comparison

| Ref | Head at audit | Valid contribution |
| --- | --- | --- |
| `main` | `b9551df` | baseline for comparison only |
| `design/old-ginkgo-production-v3` | `434ce16` | OG-04/05 masters, crops, QA and OG-02/03 Android QA |
| `design/session-2026-09-12-reference-pngs` | `4c4c0ab` | historical transfer branch; two exact attached Old Ginkgo source boards |
| `design/production-finish-v2-active` | `5ebe6e4` | Old Ginkgo plus Moru source audit and Companion/Records finish QA |

The merge-ready consolidation branch is `design/production-consolidated-v2`.

## A. P0 closure

### OG-02 folded note

- Declared 768×768 exact master SHA remains recorded, but its bytes are absent from all compared refs.
- A same-design raster reproduction was attempted from the locked 128×128 reference.
- QA rejected it because the fold structure and lettering changed and the transparency request produced a checkerboard background.
- Result: **BLOCKED**. The rejected output was not committed.

### OG-03 ginkgo leaf

- Declared 512×512 exact master SHA remains recorded, but its bytes are absent from all compared refs.
- A same-design raster reproduction was attempted from the locked 96×96 reference.
- QA rejected it because silhouette/stem proportions drifted and the transparency request produced a checkerboard background.
- Result: **BLOCKED**. The rejected output was not committed.

Existing Android API 30 / 2.625× cream, dark, map-heavy, edge and alpha-context QA evidence remains PASS_WITH_UPSCALE_SOFTNESS. High-density source-quality remains blocked, so OG-06 cannot be closed.

## B. Exact attached PNG persistence

- The newly supplied seven PNG byte streams were inspected independently; no filename or role assumption was used to establish identity.
- Exact persisted and remote-verified: **7/7**.
- Two Old Ginkgo production-pack boards matched existing committed blobs exactly and reuse those paths.
- Moru Candidate 3 and the overall UX baseline match the locked canonical SHA-256 values exactly and are now committed at their canonical PNG paths.
- The Old Ginkgo discovery-family, Companion/Records finish-pass, and Explore parity boards had no exact Git blob. They are committed byte-for-byte under `design/reference/session-2026-09-12/attachments/`.
- Those three boards have the same visual roles as existing lightweight WebP references but are byte-distinct from previously declared original hashes; they are recorded as versioned exact session sources, not silently substituted for prior bytes.
- Full provenance, dimensions, byte sizes, color/alpha facts and SHA-256 values are in `design/reference/session-2026-09-12/attachments/manifest.json`.

## C. Moru production

- Canonical board exact source is now persisted and checksum-verified, but it is fully opaque and remains insufficient as a native transparent master source.
- A neutral front master reproduction was attempted before fan-out.
- Identity QA rejected it due to changed costume, leaf structure, body proportions and non-alpha checkerboard output.
- None of the six usage-context masters or expression/lighting/affinity derivatives are promoted or committed.
- Result: **BLOCKED_IDENTITY_AND_ALPHA** pending exact original bytes or a reproduction that passes Candidate 3 identity lock.

## D. Persistence and QA rule

Only validated assets are committed. Failed reproduction attempts are intentionally excluded from canonical/reference/runtime paths and manifests. Existing committed PNG/WebP hashes and dimensions remain authoritative in their manifests and registry.

## E. Safe continuation requirements

- Obtain the exact 768×768 transparent OG-02 native master; do not crop or regenerate it from a board.
- Obtain the exact 512×512 transparent OG-03 native master; do not crop or regenerate it from a board.
- Obtain same-design native transparent Moru masters before production fan-out.
- Keep OG-02/03 BLOCKED and OG-06 OPEN until those source-quality gates pass.

