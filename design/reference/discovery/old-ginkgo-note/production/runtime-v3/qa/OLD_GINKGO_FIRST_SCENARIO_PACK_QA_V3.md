# Old Ginkgo first-scenario pack QA v3

Status: **PARTIAL PASS — OG-04/05 DESIGN RUNTIME CANDIDATE; OG-02/03 SOURCE-QUALITY BLOCKED; NOT ACTIVATED**

## Integrated result

| Work item | Semantic key | Naming/path/checksum | Target/context QA | Promotion |
| --- | --- | --- | --- | --- |
| OG-02 | `clue.old_ginkgo.folded_note` | PASS for committed 128×128 reference; declared 768×768 master checksum retained | PASS on API 30 at 2.625× for 48/64/96/144 dp + cream/dark/map; visible softness confirms upscaling limit | BLOCKED — exact 768×768 master bytes are not persisted |
| OG-03 | `clue.old_ginkgo.ginkgo_leaf` | PASS for committed 96×96 reference; declared 512×512 master checksum retained | PASS on API 30 at 2.625× for 32/48/64/96 dp + cream/dark/map; visible softness confirms upscaling limit | BLOCKED — exact 512×512 master bytes are not persisted |
| OG-04 | `place.old_ginkgo.main` | PASS | Identity/style/crop/static hierarchy PASS | `design_runtime_candidate`; not activated |
| OG-05 | `memory.old_ginkgo.keepsake` | PASS | Exact same-scene derivation, Records/Companion crops, A3 mount PASS | `design_runtime_candidate`; not activated |

## Android usage-context evidence

- Test-only harness: `app/src/androidTest/java/com/dailytown/app/visualqa/OldGinkgoClueAssetQaTest.kt` on PR #10.
- Matrix: folded note 48/64/96/144 dp; ginkgo leaf 32/48/64/96 dp; cream journal, dark surface, and live/map-heavy fixture.
- The harness verifies Android decode, alpha retention, transparent corners, density conversion, compositing, and capture output.
- The refs live only under `app/src/androidTest/assets/`; they are not packaged into the runtime APK and do not activate either semantic key.
- Android CI run #758: PASS, including instrumented-test compilation.
- Managed-device workflow run #549, relevant `replay-and-static-visual-smoke` job: PASS; 19 tests completed with no failures and both clue boards emitted.
- Workflow #549 is red overall because its separate A3 capture-verification job failed; that unrelated job did not run or gate the Old Ginkgo clue test. The relevant managed-device job and both preserved captures are PASS evidence.
- Captured density: 2.625×. Folded-note targets rendered at 126/168/252/378 px; leaf targets at 84/126/168/252 px.
- Alpha/decode/transparent-corner assertions: PASS. No opaque rectangular matte or strong light halo is visible on the dark capture; silhouette and object identity remain recognizable in every requested context.
- Source-quality: BLOCKED. The 128 px note ref and 96 px leaf ref are upscaled for every requested target on this device, producing visible softness at larger sizes.
- Preserved QA captures: `production/runtime-v2/android-qa/folded_note_android_density_qa_v2.png` and `ginkgo_leaf_android_density_qa_v2.png`.

## Source-quality gate

Repository audit found only the inspectable 128×128 folded-note and 96×96 ginkgo-leaf WebP refs. The exact full-resolution PNG bytes named by the v2 manifest are not present in GitHub or the available persisted artifacts. Therefore Android can verify decoding/compositing of the locked refs, but those refs cannot prove high-density source adequacy for the largest dp targets.

The only valid closeout is to recover the exact masters or reproduce the same locked silhouettes/materials as native high-resolution raster. A replacement that changes seal, lettering, botanical motif, shape, palette, or material must be rejected and must not be registered as a candidate.

## OG-06 disposition

- Semantic naming, versioned paths, checksums, registry, manifests, OG-04/05 crop QA, and OG-02/03 Android context rendering: PASS.
- OG-04/05: promoted only to design-side `runtime_candidate` status.
- OG-02/03: not promoted; high-density source-quality gate remains BLOCKED.
- Integrated pack: partial, not runtime-ready, not activated.
- Physical outdoor readability remains a Human Gate.
