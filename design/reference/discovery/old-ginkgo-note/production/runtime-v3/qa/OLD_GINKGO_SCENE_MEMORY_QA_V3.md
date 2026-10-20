# Old Ginkgo scene + memory QA v3

Status: **STATIC DESIGN QA PASS / ANDROID ACTIVATION NOT PERFORMED**

## OG-04 · `place.old_ginkgo.main`

| Check | Result | Evidence |
| --- | --- | --- |
| Clean scene | PASS | No UI, text, frame, Moru, or people in the master |
| Place identity | PASS | Old ginkgo trunk, stone wall, stepped neighborhood alley, warm botanical background, and note locus remain readable |
| Baseline palette / lighting / material | PASS | Cream, moss/olive, ochre, muted warm brown, soft golden daylight, illustrated bark/stone treatment |
| UI hierarchy support | PASS (static) | Reduced contrast and simplified background detail keep focal content suitable for a mounted discovery surface; actual runtime composition remains a development gate |
| `discovery_card` crop | PASS | Trunk base and note locus are both retained in the 16:9 crop |
| `records_header` crop | PASS | Trunk/material identity and note locus survive the wide crop |
| `memory_thumbnail` crop | PASS | Ginkgo trunk, lane, and note remain legible at 640×480 |
| Alternate environment style | PASS | None introduced; selected pass is an approved-reference reconstruction |

## OG-05 · `memory.old_ginkgo.keepsake`

| Check | Result | Evidence |
| Same event / same day | PASS | Keepsake master is a pixel crop of OG-04, not a separately generated scene |
| Records crop | PASS | 4:3 crop keeps the trunk, lane, and discovery note together |
| Companion recent-memory crop | PASS | 16:9 crop keeps the same scene and discovery locus |
| A3 paper / journal compatibility | PASS (static) | Warm value range and restrained edges remain readable on the `#F4EBDD` QA-board mount |
| New scene or object | PASS | None introduced |

## Processing and source-quality note

- Native generated source: 1448×1086.
- Repository production master: 2048×1536 Lanczos export.
- Every runtime crop is smaller than the native generated source in both axes or uses the 2048×1536 production export only as the auditable crop source.
- This is sufficient for the prepared mobile crops, but the resampling step is recorded rather than described as newly generated native detail.

## Existing evidence audit

- `runtime-v2/runtime_candidate_qa_v2.webp` decodes correctly, but its manifest checksum did not match the committed file. The manifest is corrected to the repository byte checksum in this checkpoint.
- `production/old_ginkgo_production_board_ref_v2.webp` is retained for history, but the committed file is truncated and does not decode in the local clone. It is not used as visual input or QA evidence for OG-04/05. The locked baseline, discovery-family board, and isolation crops remain the inputs.

## Remaining gates

- Android runtime binding/activation is intentionally not performed.
- Outdoor readability remains a physical-device Human Gate.
- OG-02/03 source-quality and Android high-density results are tracked separately; they can block OG-06 without blocking this static scene/crop checkpoint.
