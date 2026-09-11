# DailyTown Raster Reference Registry v2

Status: **reference persistence / baseline locked**

This registry preserves design-review imagery in GitHub so accepted/reference boards do not exist only in chat or temporary sandbox storage.

## Authority rule

- `docs/design/DESIGN_BASELINE_V2.md` remains the visual authority.
- Files under the paths below are **reference-only WebP preservation derivatives**, not runtime assets and not permission to redesign the locked baseline.
- The exact canonical baseline is still identified by the original PNG SHA-256 and dimensions below; the smaller WebP files exist so the visual answer remains inspectable in the repository.
- Generated composite boards are art/finish references only. They require production extraction/export before Android runtime use.

## Locked baseline references

| GitHub reference | Original source | Original dimensions | Original SHA-256 | Role |
| --- | --- | ---: | --- | --- |
| `design/reference/baseline-v2/raster/dailytown_ux_visual_baseline_v2_ref.webp` | `dailytown_ux_visual_baseline_v2.png` | 1448×1086 | `f0fb31e1ea4aec946bba356ff2d084cc6bdd8dd05119e1b350f547f382f4d515` | approved overall UX/visual baseline derivative |
| `design/reference/baseline-v2/raster/moru_candidate3_canonical_baseline_v2_ref.webp` | `moru_candidate3_canonical_baseline_v2.png` | 1122×1402 | `541b53464bbc589d86323faa3a7dbc8790cf9ab9573e0dd66812986c4a430500` | approved Moru Candidate 3 baseline derivative |

## Derived production/reference boards

| GitHub reference | Original dimensions | Original SHA-256 | Role |
| --- | ---: | --- | --- |
| `design/reference/environment/environment_asset_family_v1.webp` | 1536×1024 | `ecc00d72934245afad6416a8fdb7a2f512f3259c11035b6fe5e3c10d840b5c95` | neighborhood environment/place family reference |
| `design/reference/discovery/old-ginkgo-note/discovery_asset_family_v1.webp` | 1448×1086 | `32aa7cb81a28074b063cdb9a61b4930a0491c6c19da59b01a9895be20c9435b0` | `오래된 가로수의 쪽지` discovery/place/item family reference |
| `design/reference/discovery/old-ginkgo-note/production/old_ginkgo_production_board_ref_v2.webp` | 1536×1024 | `550352f37a4d541b5e70523e5472e263ef6dff2e07ad5b6abe1d2525a6f7f9f2` | production-derivation review board only; non-canonical, cannot override locked Moru/UX baseline or imply extra domain items |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v3/ref/place_old_ginkgo_main_master_v3_ref.webp` | master artifact 2048×1536 | master `81a9e87bb0bd4cc97b0bd024280c27bf93b0eb0ec2ea0f03d10e94cfa45f0273`; ref `7066e5b36b3c6b007484ad089d8d52eeea967ebcd1865a3fa02d6a7937fd2c4a` | `place.old_ginkgo.main` design-side runtime candidate master reference; full PNG preserved as named Work artifact; not activated |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v3/ref/memory_old_ginkgo_keepsake_master_v3_ref.webp` | master artifact 1536×1152 | master `018b861cdcd1d1629e4b53690b3ac18dda3b28e8f9bfba21ac6c5cd94bf07784`; ref `2c3dccfa62bdcb88e0b1e8763a74d79a13f9b0fdd7b652693ced08558fc03b10` | `memory.old_ginkgo.keepsake` same-scene candidate master reference; full PNG preserved as named Work artifact; not activated |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v3/ref/old_ginkgo_scene_crop_qa_board_v3_ref.webp` | QA artifact 1536×1647 | QA artifact `e48f5206cea26d052a84966f42a7199a49ab77cf7f39d03c62d56a8f3f5b4c24`; ref `78083708fc6e28c6d4e124baf0fbf0906cc3dd0078abd200d7dc743c30da4826` | OG-04/05 static crop and journal-mount QA evidence reference |
| `design/reference/ux-v2/raster/companion_records_finish_pass_v1.webp` | 1448×1086 | `d9b4279da298ea9dbc1e151818ae82de59fcaa7a022528497185208581a56f88` | Companion relationship notebook + Records A3 finish reference |
| `design/reference/ux-v2/raster/explore_detail_parity_v1.webp` | 1448×1086 | `959b79223970132b23f2d8b6f9d19056eace5f5176c213d997bc59154b0d37fb` | Explore Prepare/Detect/Discover/Resolved detail parity reference |
| `design/reference/ux-v2/raster/mobile_readability_qa_v2.webp` | 1800×1350 | `32b330c6e4a39ca2c154953cb326310b60dc1d4ed9d3fb7fee0278d237201c74` | reduced-size QA board made from locked baseline crops |

## Isolated scenario references

The following are inspectable low-resolution reference crops extracted from the locked `오래된 가로수의 쪽지` board. They are intentionally **not** production masters or runtime assets.

- `design/reference/discovery/old-ginkgo-note/isolation/isolation_reference_board_v1.webp`
- `design/reference/discovery/old-ginkgo-note/isolation/place_old_ginkgo_scene_ref_v1.webp`
- `design/reference/discovery/old-ginkgo-note/isolation/clue_folded_note_ref_v1.webp`
- `design/reference/discovery/old-ginkgo-note/isolation/clue_ginkgo_leaf_ref_v1.webp`
- `design/reference/discovery/old-ginkgo-note/isolation/memory_keepsake_photo_ref_v1.webp`
- crop-role manifest: `design/reference/discovery/old-ginkgo-note/isolation/reference-crops.v1.json`

Moru reference-level mobile QA is preserved at:

- `design/reference/moru-v2/qa/moru_mobile_qa_v2.webp`
- `design/reference/moru-v2/qa/moru-mobile-qa.v2.json`
- QA interpretation: `docs/design/MORU_MOBILE_QA_V2.md`

## Production status

- Moru transparent master/export: **pending**.
- Old Ginkgo OG-04 place and OG-05 same-scene memory families: **design-side runtime candidate / static QA PASS / not activated**; full source/masters/runtime crops/QA board are preserved as named Work artifacts, with inspectable GitHub refs and manifest under `production/runtime-v3/`.
- Old Ginkgo OG-02/03 clue refs: static QA PASS; actual Android high-density/source-quality gate remains separate and may block OG-06.
- Historical `old_ginkgo_production_board_ref_v2.webp` is retained, but the committed blob is truncated and is not valid inspectable QA evidence. Runtime-v3 derivation uses the locked baseline, discovery-family board, and isolation crops instead.
- Moru 48dp/expression/lighting/affinity reference precheck: **PASS**, while transparent-edge and actual Android usage-size QA remain blocked until clean masters exist.
- Companion/Records UI implementation may use the hierarchy and surface treatment as reference, but must not embed these composite boards as runtime screenshots.
- Explore implementation must preserve current domain behavior; decorative numbers visible in concept art do not replace the authoritative runtime hint/discovery thresholds.
- Outdoor readability and M-B motion timing remain Human Gates.

## Persistence rule

A design task that creates or accepts a raster board is not considered repository-complete until an inspectable GitHub reference is committed and its role is explicitly marked as baseline, reference-only, or production. Full-resolution masters may live in the designated source/master storage, but they must be identifiable by checksum and must not be silently replaced by a newly generated lookalike.
