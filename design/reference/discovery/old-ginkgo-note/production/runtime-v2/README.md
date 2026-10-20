# Old Ginkgo runtime candidate v2

Status: **ANDROID CONTEXT QA PASS / HIGH-DENSITY SOURCE QUALITY BLOCKED / NOT ACTIVATED**

This directory preserves the technical edge-cleanup checkpoint for the approved `오래된 가로수의 쪽지` folded-note and ginkgo-leaf assets.

What changed:
- no redesign and no new art direction
- semi-transparent edge RGB was decontaminated toward nearby solid interior color to reduce light fringe
- extremely faint extraction noise was removed without shrinking the approved silhouette
- static previews were checked on cream, dark, and map-heavy contexts
- target-size previews were checked for 48/64/96/144 px (note) and 32/48/64/96 px (leaf)
- Android API 30 at 2.625× rendered the exact committed refs at the required dp targets on cream, dark, and map-heavy fixtures
- decode, alpha retention, and transparent-corner assertions passed; no opaque rectangular matte or strong light halo is visible in the captures

GitHub stores inspectable alpha-capable WebP references. The full PNG candidate masters remain identified by SHA-256 in `runtime-candidate-manifest.v2.json`; the GitHub references do not silently replace those masters.

Still blocked before design-side promotion:
- exact 768×768 folded-note and 512×512 ginkgo-leaf master bytes are not persisted
- the 128×128 / 96×96 GitHub refs are visibly softened when upscaled and cannot pass high-density source-quality acceptance
- recover the exact masters or reproduce only the locked designs as native high-resolution raster, then rerun the same matrix

Still blocked before runtime activation:
- semantic manifest/runtime wiring
- outdoor readability Human Gate where applicable

Managed-device captures are preserved under `android-qa/`. The test source lives only on PR #10 under `app/src/androidTest/`; it does not package or activate these refs in the runtime app.

Visual authority remains `docs/design/DESIGN_BASELINE_V2.md`. Do not change the note silhouette, botanical mark, ginkgo fan shape, palette, or rendering family during the remaining QA work.
