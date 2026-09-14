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
| `design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png` | 1280×1600 | `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692` | `native_neutral_master_pass_not_runtime_active`; genuine RGBA; paired 6-layer ORA + acceptance/remote-verification evidence under `native-master-v1/` |
| `design/reference/moru-v2/native-lighting-family-v1/qa/moru_native_lighting_family_qa_v1.png` | QA board | `563a66c61c3c0dc4c98d09c5e0ef837d6e2f09c5ee6f994507e6eff12b985dd7` | `PASS_MORU_02_LIGHTING_FAMILY_V1`; LIGHT/WARM_DUSK/DARK native lighting QA, not runtime-active |
| `design/reference/moru-v2/native-semantic-authoring-handoff-v1/qa/moru_semantic_authoring_reference_board_v1.png` | QA/reference board | `60baff5ee79a93a0259b11ef6c0e5724a4e216f50b0f5177253b0e73ecc95a50` | semantic authoring handoff reference; exact expression/affinity crop guide, not a production master |
| `design/reference/moru-v2/native-semantic-family-v1/master/companion_moru_v2_result_large_happy_LIGHT_base.png` | 1280×1600 | `b96185d96bf92d5bfc2cea8ce06e82f9e583a4a1543b881caa66206a35ea0187` | `PASS_MORU_02_SEMANTIC_FAMILY_V1`; happy native expression master; alpha identity-locked; not runtime-active |
| `design/reference/moru-v2/native-semantic-family-v1/master/companion_moru_v2_result_large_curious_LIGHT_base.png` | 1280×1600 | `70daccb16a72f57189e5071b841601e1cbd96425b9d46eb0c021dd121b7b25be` | curious native expression master; alpha identity-locked; not runtime-active |
| `design/reference/moru-v2/native-semantic-family-v1/master/companion_moru_v2_result_large_surprised_LIGHT_base.png` | 1280×1600 | `ff8ac73cead939f96b7fd3be84df6211d4f4dab8c3d8483554cc7fe7de0e3c79` | surprised native expression master; alpha identity-locked; not runtime-active |
| `design/reference/moru-v2/native-semantic-family-v1/master/companion_moru_v2_result_large_clue_found_LIGHT_base.png` | 1280×1600 | `6fc559a3a8085c95250fb4a53335c8c58c4544158319e3d799f3188f5c5a09cb` | clue_found native expression master; source-supported botanical clue treatment; not runtime-active |
| `design/reference/moru-v2/native-semantic-family-v1/master/companion_moru_v2_result_large_resolved_LIGHT_base.png` | 1280×1600 | `a85e30e1782b0371b549c18c470a74f4107b31d43d8c427f4ab5b5f16cf185c3` | resolved native expression master; alpha identity-locked; not runtime-active |
| `design/reference/moru-v2/native-semantic-family-v1/overlay/companion_moru_v2_affinity_familiar_overlay.png` | 1280×1600 | `92294ff284d74fb0ce79d76c09b8517920f230ba347c7180844d608365f131d6` | familiar restrained affinity keepsake overlay; anatomy invariant |
| `design/reference/moru-v2/native-semantic-family-v1/overlay/companion_moru_v2_affinity_trusted_overlay.png` | 1280×1600 | `e8bbfc66b1326f1a93304f26299c14767595b64672768d99860be40d061021d2` | trusted restrained affinity keepsake overlay; anatomy invariant |
| `design/reference/moru-v2/native-semantic-family-v1/overlay/companion_moru_v2_affinity_best_friend_overlay.png` | 1280×1600 | `be1f699b6dc8dc1b244b8bd2b5f642687afdcf6a0b3c51953a041c8ffd356fa9` | best_friend BF-B restrained keepsake cluster overlay; anatomy invariant |
| `design/reference/moru-v2/native-semantic-family-v1/qa/moru_semantic_expression_hud64_qa_v1.png` | 660×440 QA board | `aef50cc13bf84b2b48077df5125ab90ca4f1c128d298e75b59d2e2a9e6d242c9` | six-expression HUD 64dp distinction QA; minimum pairwise changed fraction 0.004006 |
| `design/reference/moru-v2/native-semantic-family-v1/qa/moru_semantic_expression_lighting_hud64_qa_v1.png` | 615×1290 QA board | `0bca24ea25159e7cb953d9ebd9fbc7e215e48369e4ddc903ef18f78b8442661f` | expression × LIGHT/WARM_DUSK/DARK HUD readability QA |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_map_avatar_neutral_LIGHT_base.png` | 512×512 | `098df5b9b4b8e9cfe215c7fc8e9f1e9129b5673b7dc168893c5ea185f849d531` | `native_usage_qa_pass_not_runtime_active`; neutral/LIGHT/base map framing; 48/56/64dp QA PASS |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_hud_portrait_neutral_LIGHT_base.png` | 768×768 | `c67bac8dd8eb7e8cd13b8434b2cf92ce2ab6b82c657291109230a2a672af79ab` | `native_usage_qa_pass_not_runtime_active`; neutral/LIGHT/base HUD framing; 56/64/72dp face/sprout/scarf QA PASS |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_encounter_halfbody_neutral_LIGHT_base.png` | 1024×1280 | `00a643aa14661a9e8b511854b33c7f7f19c04230b781c786ab592e0504dd46e5` | `native_usage_qa_pass_not_runtime_active`; neutral/LIGHT/base encounter framing; satchel/pose support retained |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_result_large_neutral_LIGHT_base.png` | 1280×1600 | `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692` | `native_usage_qa_pass_not_runtime_active`; exact accepted neutral bytes used for result-large context |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_companion_portrait_neutral_LIGHT_base.png` | 1024×1280 | `b57d5732a1f65a0b1cc5f108e267c422bd0fcfdbd144c403c23627de558e6b14` | `native_usage_qa_pass_not_runtime_active`; portrait-biased neutral/LIGHT/base framing |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_journal_crop_neutral_LIGHT_base.png` | 768×768 | `cbcae8d5a79bc5c68368fe0f36f57fbe385d3c35f5c069ce87a2224ba85540b4` | `native_usage_qa_pass_not_runtime_active`; neutral/LIGHT/base journal framing; 56/64/72dp paper-context QA PASS |

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
- `design/reference/moru-v2/native-master-v1/` — accepted 1280×1600 neutral PNG, 6-layer ORA, acceptance metrics/QA and remote binary verification
- `design/reference/moru-v2/native-usage-context-v1/` — six-context `neutral / LIGHT / base / static` native usage family, Android-size/edge QA and fresh remote binary verification

## Production status

- Exact attachment persistence: **7/7 remote-verified**; provenance and byte/color/alpha metadata are in `design/reference/session-2026-09-12/attachments/manifest.json`.
- Moru canonical source board: **exact PNG persisted**. Strict no-upscale deterministic reference QA remains **map PASS; HUD/journal FAIL** as historical evidence. Native neutral master v1, six-context neutral usage family, and LIGHT/WARM_DUSK/DARK native lighting family are **PASS and remote-binary-verified**; expression/affinity authoring remains pending under MORU-02 and runtime promotion remains blocked.
- Old Ginkgo OG-04 place and OG-05 same-scene memory families: **design-side runtime candidate / static QA PASS / not activated**; full source/masters/runtime crops/QA board and lightweight refs are persisted in GitHub under `production/runtime-v3/`.
- Old Ginkgo OG-02 and OG-03: exact native-size sources and immutable-alpha edge-corrected candidates are persisted; design-side static source-quality QA PASS. OG-03 v5 preserves all visible-support RGB including the warm-brown perimeter and changes only a transparent-side color-bleed ring. OG-06 is closed as `DESIGN_RUNTIME_CANDIDATE_PACK_READY`; Android runtime activation and physical-device outdoor readability remain separate gates.
- Historical `old_ginkgo_production_board_ref_v2.webp` is retained, but the committed blob is truncated and is not valid inspectable QA evidence. Runtime-v3 derivation uses the locked baseline, discovery-family board, and isolation crops instead.
- Moru 48dp/expression/lighting/affinity reference precheck: **PASS**. The neutral genuine-alpha gate and six-context native Android-size/edge baseline now pass; expression/lighting/affinity semantic family production and consistency QA remain pending under MORU-02.
- Companion/Records UI implementation may use the hierarchy and surface treatment as reference, but must not embed these composite boards as runtime screenshots.
- Explore implementation must preserve current domain behavior; decorative numbers visible in concept art do not replace the authoritative runtime hint/discovery thresholds.
- Outdoor readability and M-B motion timing remain Human Gates.

## Persistence rule

A design task that creates or accepts a raster board is not considered repository-complete until an inspectable GitHub reference is committed and its role is explicitly marked as baseline, reference-only, or production. Full-resolution masters may live in the designated source/master storage, but they must be identifiable by checksum and must not be silently replaced by a newly generated lookalike.
