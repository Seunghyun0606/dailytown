# Remaining engineering after the current MVP harness

This document separates **engineering work that is still required** from work that is already implemented but waiting on human/product/legal/physical-device/design decisions.

## Current conclusion

The current branch is engineering-complete enough to begin a **technical physical-device verification** of NAVER authentication, map rendering, GPS behavior, and battery behavior. There is no additional unconditional code blocker for that engineering verification.

However, Daily Town is a visual exploration product. A **product-experience / gameplay field test must not be treated as representative until the minimum visual system is approved and integrated**. The current text/default-marker prototype can validate mechanics and instrumentation, but it cannot validate emotional engagement, discovery reward, companion appeal, or time-of-day atmosphere.

The next required actions before a representative product-experience field test are:

1. perform the technical Internal Debug APK verification on a physical Android device when convenient
2. approve the time-of-day visual direction, Moru character sheet, and DAY/DARK marker families defined in `docs/visual/TIME_OF_DAY_VISUAL_SYSTEM.md`
3. integrate the approved minimum visual asset set behind semantic asset keys and a provider-neutral map theme contract
4. approve the eight single-session acceptance criteria and comparison protocol
5. collect representative NEW_AREA / REPEAT_AREA sessions with the visually representative build
6. choose production POI/public-data sources and licensing terms
7. approve the first authored content/copy pack

Engineering must not invent physical-test results, design approval, product thresholds, or policy values.

## Newly completed: one-command offline field-test review

`tools/field_test/review_packet.py` now runs the existing safe review path through one command:

```bash
python3 tools/field_test/review_packet.py \
  --confirm-non-overlapping \
  export-a.json export-b.json
```

It validates every export, aggregates the non-overlapping batch, recomputes comparison-protocol readiness, builds the next-collection plan, and prints a concise summary. It never computes a product-quality verdict.

Supported stdout formats:

```bash
# concise operator summary
python3 tools/field_test/review_packet.py export.json

# versioned derived JSON packet
python3 tools/field_test/review_packet.py --format json export.json

# human review document
python3 tools/field_test/review_packet.py --format markdown export.json

# spreadsheet-friendly rows
python3 tools/field_test/review_packet.py --format csv export.json
```

After comparison policy is human-approved, an operator may use `--require-evidence-ready`. Exit code `2` then means the approved comparison protocol has not yet reached `PRODUCT_REVIEW_READY`. This is an **evidence-readiness gate only**, never a release/product-quality verdict.

The command is stdout-only and inherits the existing structured-export retention/access/deletion boundary. It does not persist or upload files automatically.

## P0 — required engineering after human/design decisions

### 0. Time-of-day visual asset integration

**Status:** design architecture is documented; production art is intentionally deferred to a dedicated design-art session.

Concept boards are archived separately from Android runtime resources and indexed in `docs/visual/CONCEPT_ART_ARCHIVE.md`. The implementation target is defined in `docs/visual/TIME_OF_DAY_VISUAL_SYSTEM.md`.

After the visual package is approved, required engineering is:

- semantic `DayPhase` resolver with forced-phase debug/replay support
- phase-aware Compose design tokens
- semantic companion/marker asset registry with missing-asset fallback
- provider-neutral `MapThemeSpec`
- NAVER mapping for supported light/dark/night behavior without leaking NAVER types upward
- approved DAY/DARK semantic marker assets instead of default SDK markers
- Moru assets in HUD/encounter/result surfaces
- screenshot/instrumented coverage for forced day phases
- physical-device readability checks in bright midday and night conditions

Do not bind draft concept-board images directly into `R.drawable`.

### 1. Production POI upstream adapter

**Status:** blocked on dataset/source/licensing approval.

Existing engineering already provides `PoiRepository`, radius queries, padded TTL cache, deduplication, filtering, and stale fallback. After the source is chosen, implement only the concrete upstream adapter and source-specific mapping.

Required work:

- API/client or local-dataset adapter behind `PoiRepository`
- source schema -> provider-neutral POI mapping
- pagination/rate-limit/error handling appropriate to the approved source
- attribution display if the license requires it
- cache duration/configuration matching approved terms
- contract/JVM tests using fixtures; no live network dependency in normal CI

### 2. Authored scenario/copy pack integration

**Status:** blocked on narrative/copy approval.

Mechanic templates and semantic reaction keys are already implemented. After copy approval, add a versioned content-pack boundary instead of embedding final text throughout Compose/domain code.

Recommended work:

- versioned content-pack model and loader
- semantic reaction/template key validation
- missing-key safe fallback
- locale-ready string/content organization
- deterministic fixture pack for tests
- content validation test that fails CI on duplicate/missing keys

This is required before an external beta if the prototype is expected to represent final narrative quality.

### 3. Release build/signing pipeline

**Status:** blocked on Play Console/signing ownership.

Debug/internal APK generation is implemented. Before external beta/store delivery, add the release-specific path after a human creates the app/signing credentials.

Required work:

- release build type and signing injection without committing secrets
- AAB build
- release artifact checksum/metadata
- versionCode/versionName release policy
- optional Play internal-track upload automation only after account/permission approval
- release smoke checklist

## P1 — conditionally required before external beta

### 4. Privacy/analytics/crash adapters

**Status:** intentionally not implemented until vendor + consent + privacy decisions exist.

If analytics or crash reporting is approved:

- wrap the vendor SDK behind app-owned ports
- default to no raw GPS/location trace upload
- define consent/opt-out behavior
- scrub POI/event/session identifiers according to the approved policy
- add initialization/offline/error tests

Do not add a telemetry SDK merely to make the architecture look complete.

### 5. Export retention/import/persistence

**Status:** intentionally deferred.

Current field-test data is process-memory-only and external export is explicit. App-side import/history/persistence becomes engineering work only if retention/access/deletion policy is approved.

If approved later, require:

- strict versioned parser/migrations
- bounded storage
- explicit delete/reset
- retention expiry
- no raw coordinates or durable device linkage
- corrupted/oversized input handling

This is not required for the current technical closed verification.

## P2 — optional / scope-expansion engineering

### 6. Google Maps adapter

Provider-neutral map contracts already exist. Implement `GoogleMapAdapter` only if NAVER is replaced, dual-provider operation becomes a product requirement, or a provider fallback strategy is explicitly requested.

### 7. AI-generated dialogue

The current MVP does not require a remote AI dialogue service to validate the walking/mystery loop. If added later, first define authored scenario constraints, safety rules, latency/offline fallback, cost budget, and privacy boundary. Keep AI generation behind a content/dialogue port rather than coupling encounter state to one model provider.

### 8. Background location

Current MVP is intentionally foreground-only. Background tracking should be added only after product need, battery impact, Android permission UX, and store/privacy requirements are approved.

### 9. Backend account/social synchronization

Not part of the current MVP architecture. Add only if the product scope expands to account sync, social features, remote inventory, or cross-device progress.

## Work that is NOT missing engineering

The following are already implemented and should not be rebuilt before field-test evidence says they are inadequate:

- NAVER map provider abstraction and safe health diagnostics
- app-owned DEVICE/REPLAY location sources
- GPS filtering and per-session distance/quality metrics
- progress persistence with safe read-failure fallback
- encounter/mystery/clue/anti-repeat mechanics
- companion semantic memory/reaction hooks
- daily/weekly goals and reminders
- privacy-safe battery/gameplay/route diagnostics
- configurable single-run acceptance
- NEW_AREA / REPEAT_AREA comparison
- comparison protocol readiness
- structured export validation
- multi-export aggregation
- Markdown/CSV human review
- next-evidence collection planner
- managed-device replay E2E and credentialed Internal Debug APK workflow

## Recommended execution order

1. **Technical device verification** — validate NAVER/GPS/battery; art is not required to prove those engineering boundaries.
2. **Dedicated design-art session** — approve dawn/morning/midday/sunset/night boards, Moru character package, DAY/DARK marker sheets, and UI token matrix.
3. Implement the minimum time-of-day visual system and semantic asset bindings.
4. Run a visually representative closed product-experience field test and collect NEW_AREA / REPEAT_AREA evidence.
5. Use `review_packet.py` to determine evidence readiness and next collection needs.
6. In parallel, decide POI source/licensing and authored-content/copy; then implement production POI adapter + content-pack loader.
7. Run another closed test with production-like visual/content/data quality.
8. Only then add release signing/AAB/Play internal delivery and any approved analytics/crash integration.
9. Defer Google Maps, AI dialogue, background location, backend/social, and app-side field-test persistence until evidence or product scope requires them.
