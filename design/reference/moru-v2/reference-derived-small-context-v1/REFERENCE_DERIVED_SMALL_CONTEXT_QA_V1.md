# Moru reference-derived small-context QA v1

Status: **1/3 REFERENCE_QA_PASS; 2/3 REFERENCE_QA_FAIL**

These outputs were evaluated under the strict allowed pipeline: exact crop, segmentation/masking, background removal, alpha cleanup, transparent-side RGB bleed, canvas padding and downscale only. No source enlargement, generative redraw or super-resolution was accepted.

| Context | Result | Canvas / alpha | Mobile result | Persistence |
| --- | --- | --- | --- | --- |
| map_avatar | REFERENCE_QA_PASS | 512×512 RGBA, alpha 0–255 | 48/56/64dp silhouette, sprout, face, scarf, boots/base and cream/dark/map-heavy edge read pass | candidate + QA evidence persisted; SHA `cddb4f6c1722c8519f23223159991b499bbe29a4809165e9af49017b98d34531` |
| hud_portrait | REFERENCE_QA_FAIL | 768×768 RGBA, alpha 0–255 | exact-source-size content occupies only 28.8/32.9/37.0dp at 56/64/72dp; face anchors/scarf are not reliably readable | rejected candidate not persisted; evaluated SHA `c12ad1a990838e0683d1ef4028e1a3de441b5364102f35640b763e1aa95356e3` |
| journal_crop | REFERENCE_QA_FAIL | 768×768 RGBA, alpha 0–255 | exact-source-size content occupies only 29.9/34.2/38.4dp at 56/64/72dp; face/hood/scarf and A3 artifact presence are too small | rejected candidate not persisted; evaluated SHA `3142a26ca7871353f0c0fe16a95b8ce6644007e7fe2c9cd36588509bea8149b3` |

## Recommendation

Development may use the persisted map candidate only behind an explicit temporary/reference QA profile or debug flag for layout/readability tests. HUD and journal remain blocked until a compliant higher-resolution/native source exists. Do not bind any of these as the production `companion.moru.canonical.v2` profile, do not retire v1 rollback, and do not interpret the partial PASS as MORU-01/MORU-02/MORU-03 completion.
