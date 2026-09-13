# DailyTown Raster Reference Registry v2

Status: **reference persistence / baseline locked**

This registry preserves design-review imagery in GitHub so accepted/reference boards do not exist only in chat or temporary sandbox storage.

## Authority rule

- `docs/design/DESIGN_BASELINE_V2.md` remains the visual authority.
- Files explicitly labelled as WebP are **reference-only preservation derivatives**, not runtime assets and not permission to redesign the locked baseline.
- The two exact canonical PNG sources are now committed at the paths below. The smaller WebP files remain inspectable derivatives only.
- Generated composite boards are art/finish references only. They require production extraction/export before Android runtime use.

## Locked baseline references

| GitHub reference | Original source | Original dimensions | Original SHA-256 | Role |
| --- | --- | ---: | --- | --- |
| `design/reference/baseline-v2/dailytown_ux_visual_baseline_v2.png` | `ChatGPT Image 2026년 9월 12일 오전 02_06_25 (2)(3).png` | 1448×1086 | `f0fb31e1ea4aec946bba356ff2d084cc6bdd8dd05119e1b350f547f382f4d515` | `approved_baseline_exact_source`; RGBA, alpha present but fully opaque; inspectable derivative remains under `raster/` |
| `design/reference/baseline-v2/moru_candidate3_canonical_baseline_v2.png` | `ChatGPT Image 2026년 9월 12일 오전 02_06_24 (1)(3).png` | 1122×1402 | `541b53464bbc589d86323faa3a7dbc8790cf9ab9573e0dd66812986c4a430500` | `canonical_exact_source`; RGBA, alpha present but fully opaque; not a native transparent master |

## Derived production/reference boards

| GitHub reference | Original dimensions | Original SHA-256 | Role |
| --- | ---: | --- | --- |
| `design/reference/environment/environment_asset_family_v1.webp` | 1536×1024 | `ecc00d72934245afad6416a8fdb7a2f512f3259c11035b6fe5e3c10d840b5c95` | neighborhood environment/place family reference |
| `design/reference/session-2026-09-12/attachments/old_ginkgo_discovery_asset_family.png` | 1448×1086 | `49dfe9d94916a3ff9e6fbfcb9d843c0036e11015a9db3472087d6e7c8e6102e8` | `production_reference`; RGBA, alpha channel present but fully opaque; exact current session PNG, same board role as `discovery_asset_family_v1.webp` but byte-distinct from earlier declared source `32aa7c…` |
| `design/reference/discovery/old-ginkgo-note/production/old_ginkgo_production_board_ref_v2.webp` | 1536×1024 | `550352f37a4d541b5e70523e5472e263ef6dff2e07ad5b6abe1d2525a6f7f9f2` | production-derivation review board only; non-canonical, cannot override locked Moru/UX baseline or imply extra domain items |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v3/master/place_old_ginkgo_main_master_v3.png` | 2048×1536 | `81a9e87bb0bd4cc97b0bd024280c27bf93b0eb0ec2ea0f03d10e94cfa45f0273` | `place.old_ginkgo.main` full production master; design-side runtime candidate; not activated |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v3/master/memory_old_ginkgo_keepsake_master_v3.png` | 1536×1152 | `018b861cdcd1d1629e4b53690b3ac18dda3b28e8f9bfba21ac6c5cd94bf07784` | `memory.old_ginkgo.keepsake` full same-scene master; design-side runtime candidate; not activated |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v3/ref/place_old_ginkgo_main_master_v3_ref.webp` | 480×360 | `7066e5b36b3c6b007484ad089d8d52eeea967ebcd1865a3fa02d6a7937fd2c4a` | lightweight inspectable reference for the full place master |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v3/ref/memory_old_ginkgo_keepsake_master_v3_ref.webp` | 480×360 | `2c3dccfa62bdcb88e0b1e8763a74d79a13f9b0fdd7b652693ced08558fc03b10` | lightweight inspectable reference for the full keepsake master |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v3/ref/old_ginkgo_scene_crop_qa_board_v3_ref.webp` | QA artifact 1536×1647 | QA artifact `e48f5206cea26d052a84966f42a7199a49ab77cf7f39d03c62d56a8f3f5b4c24`; ref `78083708fc6e28c6d4e124baf0fbf0906cc3dd0078abd200d7dc743c30da4826` | OG-04/05 static crop and journal-mount QA evidence reference |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v2/android-qa/folded_note_android_density_qa_v2.png` | 1079×1874 | `0562c1b8056f0f8ef7b7a4a7ffc406b8900f72fe91ff66f645546ad0a89d06a0` | OG-02 Android API 30 / 2.625× context QA board; reference only; source-quality blocker is printed in the board |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v2/android-qa/ginkgo_leaf_android_density_qa_v2.png` | 1079×1496 | `e4cb1f896ceaa1eb48513902b2e986d162f0d72fa097a5b6c81cd8db1dfeae31` | OG-03 Android API 30 / 2.625× context QA board; reference only; source-quality blocker is printed in the board |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v5/source/clue_old_ginkgo_ginkgo_leaf_runtime_candidate_v2_exact.png` | 512×512 | `ec37263d8e736809629d6d3d304712df1148110794a61e2b36286108f490d064` | `immutable_exact_source`; exact attached RGBA PNG preserved byte-for-byte; processing input only, not runtime-active |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v5/candidate/clue_old_ginkgo_ginkgo_leaf_selective_edge_retouch_v5.png` | 512×512 | `775059cd22db997234e8012534a7e73f9a347d8f1208117c3babee1c9ca1f366` | `design_runtime_candidate_not_activated`; immutable alpha and visible-support RGB; 12px transparent-side local-boundary bleed only; PASS_SELECTIVE_EDGE_RETOUCH |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v5/qa/og03/00_source_previous_new.png` | 1560×610 | `e574a062abfac48f07a8c3882e99a179c8addb886325a95510dbed3eb69b6553` | OG-03 source / previous rejected / selective v5 review board; supporting alpha, overlay, difference, palette, context, target-size, high-density and edge-close-up artifacts live in the same QA directory |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v4/source/clue_old_ginkgo_folded_note_runtime_candidate_v2_exact.png` | 768×768 | `59171439fe9fefea4a5040fa9034a213983dc360496c3474767b8f58e79e2e92` | `immutable_exact_source`; recovered attached PNG bytes; genuine RGBA; not altered |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v4/candidate/clue_old_ginkgo_folded_note_edge_corrected_runtime_candidate_v4.png` | 768×768 | `2adc914f6ca2e5648339943bc02afe62059948d52a672e382fad69b5e6c08869` | `design_runtime_candidate_not_activated`; immutable alpha, exterior edge-RGB correction only; PASS_EDGE_RECONSTRUCTION |
| `design/reference/discovery/old-ginkgo-note/production/runtime-v4/qa/og02/00_source_vs_corrected.png` | 1040×610 | `24b9a0bb66384e965f2376d06dc127e2c0ac866bea7240fbf87852493db64c27` | OG-02 exact source vs corrected review board; supporting overlay/difference/context/target-size/edge-close-up artifacts live in the same QA directory |
| `design/reference/discovery/old-ginkgo-note/production/source-boards/old_ginkgo_asset_production_pack_v1_0.png` | 1536×1024 | `ed9a4d1b20dec0c5d6f72b5e31a5dd2a8e7eb819b1f7c880cb98e666b0ba523d` | `production_reference`; RGBA, alpha range 0–253; exact source board re-verified from the new attachment bytes |
| `design/reference/discovery/old-ginkgo-note/production/source-boards/old_ginkgo_asset_production_pack_v1_1.png` | 1536×1024 | `0f3f3ed101a3476c74dd9674d3dc98e2e53ad901d898cbf02be7cdb88d67d312` | `production_reference`; RGBA, alpha range 0–253; exact source board re-verified from the new attachment bytes |
| `design/reference/session-2026-09-12/attachments/companion_records_finish_pass.png` | 1448×1086 | `4132ff65177e86fe8bd360121b02970ad3a7d41df23f7f95ca233f1c43b300f9` | `production_reference`; RGBA, alpha channel present but fully opaque; exact current session PNG, same board role as `companion_records_finish_pass_v1.webp` but byte-distinct from earlier declared source `d9b427…` |
| `design/reference/session-2026-09-12/attachments/explore_detail_parity.png` | 1448×1086 | `ddfef207ac1725ecc360cb0012446938a705e056b86dfbfe00656034a0a9f20b` | `production_reference`; RGBA, alpha channel present but fully opaque; exact current session PNG, same board role as `explore_detail_parity_v1.webp` but byte-distinct from earlier declared source `959b79…` |
| `design/reference/ux-v2/raster/mobile_readability_qa_v2.webp` | 1800×1350 | `32b330c6e4a39ca2c154953cb326310b60dc1d4ed9d3fb7fee0278d237201c74` | reduced-size QA board made from locked baseline crops |
| `design/reference/moru-v2/reference-derived-small-context-v1/candidate/companion_moru_v2_map_avatar_neutral_LIGHT_base_reference_candidate.png` | 512×512 | `cddb4f6c1722c8519f23223159991b499bbe29a4809165e9af49017b98d34531` | `REFERENCE_DERIVED_CANDIDATE`; genuine RGBA; 48/56/64dp `REFERENCE_QA_PASS`; not native and not runtime-active |

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

Moru deterministic small-context evidence and reconstruction handoff are preserved at:

- `design/reference/moru-v2/reference-derived-small-context-v1/` — exact-pixel-derived map candidate, extraction evidence, map context board and 1/3 PASS / 2/3 FAIL manifest
- `design/reference/moru-v2/native-master-handoff-v1/` — exact canonical, masks/guides, sampled palette and native-master acceptance contract

## Production status

- Exact attachment persistence: **7/7 remote-verified**; provenance and byte/color/alpha metadata are in `design/reference/session-2026-09-12/attachments/manifest.json`.
- Moru canonical source board: **exact PNG persisted**. Strict no-upscale deterministic reference QA: **map PASS; HUD/journal FAIL**. Human reconstruction handoff: **ready**. Moru native transparent master/export and runtime promotion: **pending/blocked**.
- Old Ginkgo OG-04 place and OG-05 same-scene memory families: **design-side runtime candidate / static QA PASS / not activated**; full source/masters/runtime crops/QA board and lightweight refs are persisted in GitHub under `production/runtime-v3/`.
- Old Ginkgo OG-02 and OG-03: exact native-size sources and immutable-alpha edge-corrected candidates are persisted; design-side static source-quality QA PASS. OG-03 v5 preserves all visible-support RGB including the warm-brown perimeter and changes only a transparent-side color-bleed ring. OG-06 is closed as `DESIGN_RUNTIME_CANDIDATE_PACK_READY`; Android runtime activation and physical-device outdoor readability remain separate gates.
- Historical `old_ginkgo_production_board_ref_v2.webp` is retained, but the committed blob is truncated and is not valid inspectable QA evidence. Runtime-v3 derivation uses the locked baseline, discovery-family board, and isolation crops instead.
- Moru 48dp/expression/lighting/affinity reference precheck: **PASS**, while transparent-edge and actual Android usage-size QA remain blocked until clean masters exist.
- Companion/Records UI implementation may use the hierarchy and surface treatment as reference, but must not embed these composite boards as runtime screenshots.
- Explore implementation must preserve current domain behavior; decorative numbers visible in concept art do not replace the authoritative runtime hint/discovery thresholds.
- Outdoor readability and M-B motion timing remain Human Gates.

## Persistence rule

A design task that creates or accepts a raster board is not considered repository-complete until an inspectable GitHub reference is committed and its role is explicitly marked as baseline, reference-only, or production. Full-resolution masters may live in the designated source/master storage, but they must be identifiable by checksum and must not be silently replaced by a newly generated lookalike.
