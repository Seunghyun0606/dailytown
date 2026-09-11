# Old Ginkgo place + memory runtime candidate v3

Status: **DESIGN RUNTIME CANDIDATE / NOT ACTIVATED**

This directory preserves the production derivation for:

- `place.old_ginkgo.main`
- `memory.old_ginkgo.keepsake`

The approved Design Baseline v2 and Old Ginkgo isolation references remain the visual authority. No alternate environment, UI composition, Moru design, or gameplay behavior is introduced here.

## Derivation chain

1. The clean scene was reconstructed from the locked place crop, Old Ginkgo discovery-family board, and approved overall UX/visual baseline.
2. UI, text, frames, and characters were removed. The established folded-note discovery locus remains at the ginkgo-tree base.
3. The selected 1448×1086 generated raster source was exported to a 2048×1536 PNG master with Lanczos resampling so the production master meets the required 4:3 canvas. Runtime crops are all downscaled from that master.
4. The keepsake master and both Records/Companion derivatives are exact crops from the same scene pixels. No new scene was generated for OG-05.

The full source, masters, runtime crops, and QA board are persisted as binary files in GitHub with exact dimensions/checksums. The smaller `ref/` WebP derivatives remain available for lightweight inspection and review.

## Files

- `master/place_old_ginkgo_main_generation_source_v3.png` — native generated source; production source evidence
- `master/place_old_ginkgo_main_master_v3.png` — 2048×1536 clean scene master
- `master/memory_old_ginkgo_keepsake_master_v3.png` — 1536×1152 same-scene keepsake master
- `crops/place_old_ginkgo_main_discovery_card_v3.webp`, `crops/place_old_ginkgo_main_records_header_v3.webp`, `crops/place_old_ginkgo_main_memory_thumbnail_v3.webp`
- `crops/memory_old_ginkgo_keepsake_records_card_v3.webp`, `crops/memory_old_ginkgo_keepsake_companion_recent_memory_v3.webp`
- `qa/old_ginkgo_scene_crop_qa_board_v3.webp`
- `ref/*_ref.webp` — inspectable GitHub derivatives for every master/crop/QA role
- `qa/OLD_GINKGO_SCENE_MEMORY_QA_V3.md`
- `scene-memory-manifest.v3.json`

## Final generation prompt

Built-in image generation/edit mode was used. The accepted pass kept the reconstructed scene geometry fixed and changed only the first pass's overly photographic finish to the approved simplified, softly shaded 2D storybook rendering language. Invariants were: same old ginkgo trunk, stone wall, alley steps, neighborhood background, discovery note, camera angle, warm daylight, crop-safe focal placement, no UI/text/frame/character/people/new objects, and no alternate palette or art direction.

## Runtime boundary

These artifacts and GitHub references are design-side candidates only. They are not copied into Android runtime resources and no resolver, Compose UI, map behavior, or gameplay threshold is changed by this checkpoint.
