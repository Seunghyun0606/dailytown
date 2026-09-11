# Old Ginkgo transparent extraction candidate status

Status: **candidate extracted / runtime promotion blocked**

The folded-note and ginkgo-leaf technical extraction candidates have been produced from the locked production reference board without redesign.

## Current assets

- Folded note master candidate: 768×768 transparent PNG, SHA-256 `c9b44fd1d89326a3b85c0086aee9f649dd2a3c96780a3c3f6d19692f1f7dedc8`
- Ginkgo leaf master candidate: 512×512 transparent PNG, SHA-256 `647a2fec78c70bcbb05efde0be800722c8db754d9c880aa0d697d341a4a2186b`
- Candidate QA board: SHA-256 `ef7d6ddd21dae7e6c2a7792a788eb5f207486f9cd0e01cc79d79cff586b8640b`

These are technical extractions from a composite reference. Upscaling does not restore original illustration detail, so they are not yet `production/runtime` assets.

## Completed in this pass

- alpha extraction candidate for `clue.old_ginkgo.folded_note`
- alpha extraction candidate for `clue.old_ginkgo.ginkgo_leaf`
- target-size candidate derivatives prepared at 48/64/96/144 px and 32/48/64/96 px respectively
- cream / dark / map-heavy context precheck prepared
- semantic/checksum metadata recorded in `transparent-candidate-manifest.v1.json`

## Remaining before promotion

1. inspect and clean fringe/edge pixels against the locked reference
2. decide whether source-resolution quality is acceptable for real 144dp/high-density presentation; otherwise recreate a high-resolution master while matching the same locked design
3. run actual Android usage-context QA
4. update `production-manifest.v1.json` to `runtime_candidate` only after QA passes

Do not redesign the folded note or ginkgo leaf during cleanup.
