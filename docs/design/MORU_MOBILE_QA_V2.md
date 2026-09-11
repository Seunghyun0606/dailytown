# Daily Town — Moru Mobile QA v2

> Status: **REFERENCE PRECHECK COMPLETE / RUNTIME ACTIVATION BLOCKED**
>
> Visual authority: `docs/design/DESIGN_BASELINE_V2.md`
>
> QA image target: `design/reference/moru-v2/qa/moru_mobile_qa_v2.webp`

## Scope

This QA pass uses only the locked Candidate 3 Moru baseline. It does not introduce a new pose family, costume, anatomy, palette, or rendering direction.

## Reference precheck

- 48dp silhouette reference: **PASS** — asymmetric sprout, leaf hood, scarf diagonal and lower-body/satchel mass remain distinguishable on the approved board.
- six-expression family consistency: **PASS** — `neutral / happy / curious / surprised / clue_found / resolved` keep the same identity cues.
- lighting family: **PASS at reference-board level** — `LIGHT / WARM_DUSK / DARK` preserve face and hood read.
- affinity anatomy invariance: **PASS** — `base / familiar / trusted / best_friend` changes are accessory/keepsake progression rather than anatomy change.

## Still blocked

The following cannot be closed from the composite baseline board alone:

- transparent-edge / halo QA on light, dark and live-map backgrounds
- actual Android display tests at 48 / 56 / 64dp
- runtime semantic resolver/fallback activation for v2 assets
- physical outdoor readability

Transparent/layer-safe production masters are required before those checks.

## Runtime rule

Expected v2 fallback remains `neutral / LIGHT / base / static`. Do not retire the current rollback pack or promote the v2 profile until transparent exports, resolver fallback tests, mobile-size checks and rollback verification pass.

The legacy/procedural companion renderer must not be treated as the production implementation of the approved v2 raster Moru.

## Human Gate

Outdoor readability and final M-B motion timing/intensity remain Human Gates. This reference precheck does not close either gate.
