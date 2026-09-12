# Development Marker Production Runtime Boundary

Status: implemented, **marker promotion still blocked by physical-device + human approval**.

## Why a separate marker registry exists

Companion/A-3 production assets are unique by semantic key, but marker DAY/DARK families intentionally reuse the same 12 marker semantic keys. A semantic-key-only registry would collapse 24 approved candidate files into 12 records and could silently select the wrong time-of-day family.

The production marker boundary therefore keys assets by:

`(MarkerFamily, SemanticAssetKey)`

This matches the existing provider-neutral `MarkerAssetResolver` contract and keeps NAVER SDK types below the map adapter boundary.

## Implemented runtime pieces

- `MarkerProductionAssetIndex`
  - family-aware lookup
  - duplicate family/semantic rejection
  - `marker.*` semantic enforcement
  - safe relative SVG path enforcement
  - family/path correspondence enforcement
- `ProductionMarkerAssetRegistry`
  - currently contains **0** records
  - remains intentionally empty while the marker batch is `production_export_candidate`
- `ProductionMarkerSvgVisualSource`
  - uses `MarkerAssetResolver`
  - returns `null` when no promoted record exists so provider default markers remain active
  - uses the SVG export's declared width/height rather than inventing a runtime raster size
  - preserves the canonical geographic anchor from `ResolvedMarkerAsset`
  - malformed/transparent assets fail closed to provider fallback
- `MainActivity`
  - now wires the production marker visual source into `NaverMapAdapter`
  - because the production registry is empty, this produces no marker candidate exposure and no visible marker-family change yet

## Candidate isolation

No marker directory was added to the main Android asset source set.

Current main APK visual source roots remain:

- promoted companion assets
- promoted A-3 assets

Marker SVGs remain accessible only to `androidTest` through the existing test-only `design/production` asset root. Runtime code never references `design/production/markers/...` candidate repository paths.

## QA added

JVM tests verify:

- DAY and DARK may share the same semantic key without collision
- duplicate family/semantic pairs fail
- unsafe/cross-family/non-SVG runtime paths fail
- the singleton production marker registry remains empty

Instrumented tests verify:

- the family-aware production renderer can rasterize the current approved marker SVG shape from the **test-only** candidate catalog
- exported `96 x 128` dimensions are honored for the tested marker
- the canonical anchor remains unchanged
- DARK-to-DAY resolver fallback works when only a DAY record is supplied
- the actually wired production source returns `null` while the real production registry is empty

## Activation after readiness PASS

Do not activate marker assets until `verify_marker_promotion_readiness.py` passes against:

1. current clean emulator NAVER evidence,
2. physical non-emulator NAVER evidence with the same marker fingerprint,
3. completed human approval bound to that fingerprint.

Only then should a separate explicit promotion change:

1. change the marker batch/manifest from candidate to production export,
2. add `design/production/markers/v1` as a **main** asset source root,
3. populate exactly 24 family-aware production marker records,
4. set `PROMOTED_MARKER_COUNT = 24`,
5. extend target-APK SHA binding tests to all 24 marker files,
6. rerun NAVER real-map, selected-anchor, EV-1 and physical readability QA.

That promotion must remain a distinct commit/change so it can be reviewed or reverted without changing gameplay/domain semantics.
