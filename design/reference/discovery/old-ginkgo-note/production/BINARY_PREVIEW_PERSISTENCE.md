# Transparent candidate binary persistence

The production work-item tracker and manifests are committed. The authoritative transparent master candidates are tracked by checksum in `transparent-candidate-manifest.v1.json`.

Repository-visible raster preservation currently remains the production reference board `old_ginkgo_production_board_ref_v2.webp` plus the prior isolation refs. The extracted transparent candidate bytes are not yet runtime-authoritative and must not be promoted merely because their checksums are recorded.

Before closing `DT-DES-OG-02` / `DT-DES-OG-03`, persist at least one inspectable alpha-capable raster derivative for each candidate in this directory, then run edge/source-quality/Android-size QA.
