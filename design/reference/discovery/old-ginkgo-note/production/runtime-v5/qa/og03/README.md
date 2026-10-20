# OG-03 selective perimeter retouch v5

Deterministic and visual status: **PASS_SELECTIVE_EDGE_RETOUCH**

- exact source SHA-256: `ec37263d8e736809629d6d3d304712df1148110794a61e2b36286108f490d064`
- previous rejected SHA-256: `ef8f589ebf6a76a0be653e32491675502fddb4ee969f1e8ea0886ec7c777a360`
- selective candidate SHA-256: `775059cd22db997234e8012534a7e73f9a347d8f1208117c3babee1c9ca1f366`
- alpha changed pixels: 0
- silhouette IoU: 1.000000
- semi-transparent outlier pixels changed: 0
- transparent-side bleed pixels changed: 19189
- warm-brown perimeter pixels changed: 0
- warm-yellow transition pixels changed: 0
- opaque/vein/highlight/material support pixels changed: 0
- runtime activation: not performed

Visual review confirms the selective v5 preserves the source warm-brown perimeter and avoids the bright-yellow jagged edge of the previous rejected candidate on cream, dark, map-heavy, target-size and 288px high-density boards.
