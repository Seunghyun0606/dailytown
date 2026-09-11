# Old Ginkgo runtime candidate v2

Status: **DESIGN RUNTIME CANDIDATE / NOT ACTIVATED**

This directory preserves the technical edge-cleanup checkpoint for the approved `오래된 가로수의 쪽지` folded-note and ginkgo-leaf assets.

What changed:
- no redesign and no new art direction
- semi-transparent edge RGB was decontaminated toward nearby solid interior color to reduce light fringe
- extremely faint extraction noise was removed without shrinking the approved silhouette
- static previews were checked on cream, dark, and map-heavy contexts
- target-size previews were checked for 48/64/96/144 px (note) and 32/48/64/96 px (leaf)

GitHub stores inspectable alpha-capable WebP references. The full PNG candidate masters remain identified by SHA-256 in `runtime-candidate-manifest.v2.json`; the GitHub references do not silently replace those masters.

Still blocked before runtime activation:
- actual Android usage-context QA
- source-quality acceptance at device density
- semantic manifest/runtime wiring
- outdoor readability Human Gate where applicable

Visual authority remains `docs/design/DESIGN_BASELINE_V2.md`. Do not change the note silhouette, botanical mark, ginkgo fan shape, palette, or rendering family during the remaining QA work.
