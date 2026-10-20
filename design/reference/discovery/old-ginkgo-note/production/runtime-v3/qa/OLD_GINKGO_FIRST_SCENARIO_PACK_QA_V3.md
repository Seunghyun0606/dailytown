# Old Ginkgo first-scenario pack QA v3

Status: **PASS — DESIGN_RUNTIME_CANDIDATE_PACK_READY; NOT ACTIVATED**

## Integrated result

| Work item | Semantic key | Source/candidate persistence | Static QA | Promotion |
| --- | --- | --- | --- | --- |
| OG-02 | `clue.old_ginkgo.folded_note` | exact 768×768 source + corrected v4 persisted and checksummed | immutable alpha, silhouette IoU 1.0, cream/dark/map and 48/64/96/144px + 432px PASS | `design_runtime_candidate_v4`; not activated |
| OG-03 | `clue.old_ginkgo.ginkgo_leaf` | exact 512×512 source + selective v5 persisted and checksummed | immutable alpha, silhouette IoU 1.0, warm-brown perimeter preserved, cream/dark/map and 32/48/64/96px + 288px PASS | `design_runtime_candidate_v5`; not activated |
| OG-04 | `place.old_ginkgo.main` | master/crops/refs persisted | identity/style/crop/static hierarchy PASS | `design_runtime_candidate_v3`; not activated |
| OG-05 | `memory.old_ginkgo.keepsake` | same-scene master/crops/refs persisted | same-scene derivation, Records/Companion crops and A3 mount PASS | `design_runtime_candidate_v3`; not activated |

## OG-03 selective edge result

- Immutable exact source: `../runtime-v5/source/clue_old_ginkgo_ginkgo_leaf_runtime_candidate_v2_exact.png`
- Source SHA-256: `ec37263d8e736809629d6d3d304712df1148110794a61e2b36286108f490d064`
- Corrected candidate: `../runtime-v5/candidate/clue_old_ginkgo_ginkgo_leaf_selective_edge_retouch_v5.png`
- Corrected SHA-256: `775059cd22db997234e8012534a7e73f9a347d8f1208117c3babee1c9ca1f366`
- Decision: **PASS_SELECTIVE_EDGE_RETOUCH**
- Palette analysis locked 14,148 warm-brown and 7,428 warm-yellow fractional-edge pixels. No visible-support pixel qualified as a contamination outlier.
- The candidate changes only 19,189 alpha-zero pixels in a 12px transparent-side bleed ring, seeded from valid local boundary color. Alpha and every visible-support RGB pixel remain byte-identical.
- Alpha changed pixels: 0. Silhouette IoU: 1.0. Fan/notch/stem geometry, vein, highlight and material are unchanged.
- Previous rejected nearest-opaque candidate SHA `ef8f589ebf6a76a0be653e32491675502fddb4ee969f1e8ea0886ec7c777a360` was comparison-only and remains uncommitted.
- QA evidence: `../runtime-v5/qa/og03/`.

## Context and target-size evidence

- Cream, dark and map-heavy boards: PASS; v5 preserves the source's natural brown perimeter and does not reproduce the previous bright-yellow jagged edge.
- 32/48/64/96px target renders: PASS.
- 288px high-density render: PASS; source material impression is unchanged.
- Straight-alpha edge sampling MAE improved at every tested size on cream and dark contexts. Full values are in `../runtime-v5/qa/og03/og03_selective_edge_retouch_metrics.v5.json`.
- Genuine PNG RGBA/color type 6, alpha range 0–255 and four transparent corners: PASS.

## Android and Human Gates

- The existing PR #10 API 30 / 2.625× test-only matrix remains historical decode/context evidence and was not modified.
- Runtime assets were not bound, copied into the app, or activated by this design work.
- Android runtime binding/activation is a separate Development task.
- Physical-device outdoor readability remains a Human Gate.

## OG-06 disposition

OG-02, OG-03, OG-04 and OG-05 all pass design-side source/crop/static QA. Therefore OG-06 is **CLOSED — DESIGN_RUNTIME_CANDIDATE_PACK_READY**. This does not imply Android runtime activation.
