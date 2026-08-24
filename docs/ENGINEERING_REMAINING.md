# Remaining engineering after the current MVP harness

This document separates **engineering work that is still required** from work that is already implemented but waiting on human/product/legal/physical-device decisions.

## Current conclusion

The current branch is engineering-complete enough to begin a credentialed closed field test. There is **no additional unconditional code blocker before the first physical-device field-test run**.

The next required actions before more product-facing implementation are human-owned:

1. run the credentialed Internal Debug APK on a physical Android device and validate NAVER/GPS/battery behavior
2. approve the eight single-session acceptance criteria and the comparison protocol
3. collect representative NEW_AREA / REPEAT_AREA sessions
4. choose production POI/public-data sources and licensing terms
5. approve the first authored content/copy pack

Engineering must not invent those results or policy values.

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

## P0 — required engineering after human decisions

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

This is **not required for the current closed field test**.

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

1. **Physical-device field test now** — no new code blocker.
2. Approve criteria/protocol and collect evidence; use `review_packet.py` to determine evidence readiness and next collection needs.
3. In parallel, make the POI source/licensing and authored-content decisions.
4. Implement production POI adapter + authored content-pack loader.
5. Run another closed test with production-like content/data.
6. Only then add release signing/AAB/Play internal delivery and any approved analytics/crash integration.
7. Defer Google Maps, AI dialogue, background location, backend/social, and app-side field-test persistence until evidence or product scope requires them.
