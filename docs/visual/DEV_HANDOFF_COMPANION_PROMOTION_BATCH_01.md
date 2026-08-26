# Daily Town — Development Handoff: Companion Promotion Batch 01

Status: ready for development-session integration smoke test.

This handoff does **not** authorize the design session to modify Android/Kotlin. It defines the exact verification needed before the prepared companion SVG derivatives can be promoted from `production_export_candidate` to `production_export`.

## Inputs

- `design/production/production-promotion-batch-01.v1.json`
- `design/production/production-promotion-batch-01-luca-derivatives.v1.json`
- `design/export-spec/companion-static-smoke-test.v1.json`

Prepared design-QA candidates: **32 assets**.

### Moru

- affinity: 4
- canonical: 5
- expressions: 6
- lighting: 3

### Luca

- canonical: 5
- expressions: 6
- lighting: 3

All have SHA-256 recorded in the production promotion manifests.

## Required runtime model

Runtime/domain code must resolve semantic roles rather than filenames.

Conceptual request:

```text
companionId
+ expression
+ lightingFamily
+ appearanceProfile
+ usageContext
-> resolved visual layers/assets
```

The visual implementation may internally use vectors, cached rasters, composed drawables, or another provider-neutral mechanism. Domain/gameplay code must not depend on those details.

## Required smoke tests

### 1. Semantic resolver

Request each prepared semantic key and confirm it resolves without raw filename references in domain state.

### 2. Expression + lighting composition

For both Moru and Luca test:

- 6 expressions
- 3 lighting families

The face/expression layer must remain geometrically aligned with the canonical body when lighting changes.

No expression may shift the canonical head/body anchor.

### 3. Compact-size test

At minimum capture:

- Moru neutral LIGHT at 48dp
- Moru clue_found WARM_DUSK at 48dp
- Moru resolved DARK at 48dp
- Luca neutral LIGHT at 48dp
- Luca clue_found WARM_DUSK at 48dp
- Luca resolved DARK at 48dp

Also spot-check 32dp fallback behavior.

### 4. Affinity replacement

Moru:

`base -> familiar -> trusted -> best_friend`

Switching profile must not mutate `companionId`, expression semantics, lighting semantics, or gameplay state.

### 5. Companion replacement

Switch Moru -> Luca while keeping the same semantic expression and lighting request.

This must require no companion-specific filename branch in domain/gameplay code.

### 6. Fallbacks

Required:

- missing expression -> `neutral`
- missing lighting -> `LIGHT`
- missing animation -> static current expression

Fallback should be observable in diagnostics but not become a gameplay-state change.

## Promotion rule

Only after all smoke tests pass:

1. verify SHA-256 against the design production manifests
2. mark the authoritative runtime mapping as `production_export`
3. keep source masters and concept boards outside runtime resources
4. preserve semantic fallback mappings

If composition fails, do **not** redesign domain APIs around the failure. Adjust the visual adapter/export form while retaining semantic keys.

## After this smoke test

Next design/development integration priorities:

1. DAY/DARK marker split export + real NAVER map QA
2. A-3 split export + 360/412/600dp screenshot QA
3. M-B sprite-gen pilot + human motion review
4. EV-1 E2 and baseline map capture QA
5. physical outdoor R-B final approval
6. ID-A1/A2/A3 final human lock
