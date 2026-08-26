# NAVER Map Overlay Matrix QA

Status: **runtime/test harness implemented; credentialed real-map execution and human readability review remain open.**

This document describes the Android implementation of the approved map-overlay QA handoff without changing the approved visual direction.

## Source of truth

The instrumentation test reads these files directly as test-only assets:

- `design/export-spec/map-overlay-qa-matrix.v1.json`
- `design/export-spec/evening-interpolation-tokens.v1.json`

The test fails if the approved matrix dimensions, required stack, expected baseline count, EV-1 route checkpoints, DAY/DARK policy, or E2 luminance threshold drift from the runtime contract.

No source/concept master is added to the main APK. `design/export-spec` is exposed to `androidTest` only.

## Baseline real-map matrix

`MapOverlayQaMatrix` expands the approved matrix to exactly 18 cases:

```text
MORNING / SUNSET / NIGHT
× simple_residential / dense_urban / mixed_poi
× normal / reduced_motion
= 18 captures
```

Every technical capture requests the same semantic stack:

1. real NAVER provider map;
2. promoted Moru compact map avatar;
3. active/discoverable/solved/revisit encounter markers;
4. POI marker;
5. following route;
6. active halo;
7. discovery effect sample or reduced-motion static fallback;
8. top/bottom HUD surfaces.

The screen-space route/halo/effect/HUD geometry inside `NaverMapOverlayQaSceneView` is **QA framing only**, not a production visual token. It uses approved semantic/color language and deliberately contains no authored motion timing/easing values.

## EV-1 checkpoint captures

The baseline matrix already covers three map complexity classes. In addition, the credentialed NAVER test captures all EV-1 forced checkpoints on the dense-map fixture:

```text
E0 / E1 / E2 / E3 / E4
× normal / reduced_motion
= 10 checkpoint captures
```

Rules enforced before capture:

- no EVENING marker family is created;
- marker family remains DAY or DARK;
- route values match E0..E4 approved source values;
- E0/E1 use DAY + WARM_DUSK;
- E3/E4 use DARK + DARK;
- E2 first captures a marker-free real map, measures central luminance, and passes that value to `EveningVisualInterpolator`;
- reduced motion keeps EV-1 color/atmosphere interpolation and changes only the procedural motion fallback representation.

## Marker-free provider proof remains first

Before every full-stack capture, the QA fixture is hidden and markers are removed. The provider surface must independently pass the existing marker-free texture gate.

This prevents route/marker/HUD pixels from making a blank or failed provider surface look healthy.

Authentication, network validation, and provider texture failures remain fail-closed.

## Technical capture is not design approval

`visual/naver-diagnostics/session.json` records each completed matrix item with:

- kind (`baseline` or `ev1_checkpoint`);
- semantic id;
- phase/checkpoint;
- map complexity;
- motion mode;
- DAY/DARK marker family;
- companion id;
- artifact storage name;
- `technicalCaptureCompleted=true`;
- `humanVisualReview=required`.

A technical test PASS does **not** claim that marker readability, provider label comprehension, or NAVER attribution/legal UI have passed human review.

## R-B classification contract

The provider-neutral runtime now exposes only the approved result vocabulary:

- `PASS`
- `PASS_WITH_DECORATIVE_DEGRADATION`
- `FAIL`

Critical layers are fail-closed:

1. user location/navigation;
2. active route;
3. active encounter;
4. provider map information;
5. companion presence;
6. secondary gameplay markers.

Only decorative layers may be sacrificed. The encoded degradation order is:

1. atmosphere;
2. active glow;
3. discovery decoration.

If any critical layer remains unreadable, the result is `FAIL`; decorative reduction cannot convert that failure into PASS.

Final outdoor R-B acceptance remains a physical-device human gate.

## Evidence counts after a complete credentialed run

A fully completed technical run should report:

- `baselineCaptureCount = 18`
- `ev1CheckpointCaptureCount = 10`
- `matrixCaptureCount = 28`

These counts mean the requested screenshots were technically produced. Human review is still required before marker promotion.

## Current blocker

The most recent completed credentialed managed-device run before this matrix expansion reached NAVER READY with a validated network and then failed with provider authentication code `401`.

Until the intended NAVER Maps application/client is configured for Android package `com.dailytown.app` and the credentialed run proceeds past authentication, no new real-map matrix PASS is claimed and the marker pack remains `production_export_candidate`.
