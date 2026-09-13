# Moru reference-derived small-context QA v1

Status: **3/3 REFERENCE_QA_PASS**

These outputs are deterministic derivatives of the exact canonical board. They are not native masters and are not runtime-active.

| Context | Result | Output | SHA-256 | disclosed source scale |
| --- | --- | --- | --- | ---: |
| map_avatar | REFERENCE_QA_PASS | 512×512 RGBA | `cddb4f6c1722c8519f23223159991b499bbe29a4809165e9af49017b98d34531` | 0.866426 |
| hud_portrait | REFERENCE_QA_PASS | 768×768 RGBA | `8b1cb13831920e64f0e5b83d6ac7b7d7f7c1f5db30128e2b3966e2d1d74c51aa` | 1.746835 |
| journal_crop | REFERENCE_QA_PASS | 768×768 RGBA | `59d67630fcebf4318ca5f5362a788d3bfe142039793918b5d4fffc2fafcb8d3b` | 1.512195 |

## Recommendation

Development may use these only behind an explicit temporary/reference QA profile or debug flag for layout, readability and integration tests. Do not bind them as the production `companion.moru.canonical.v2` profile, do not retire v1 rollback, and do not interpret the PASS as MORU-01/MORU-02/MORU-03 completion.

The HUD and journal layouts use deterministic Lanczos interpolation of the limited board pixels. This creates no semantic detail and is fully disclosed in the manifest, but it does not become native production resolution.
