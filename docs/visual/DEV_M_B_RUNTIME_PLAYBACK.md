# M-B Runtime Playback — Development Integration

Status: **runtime playback core implemented; authored atlas + human tuning still pending.**

This document records the Android/runtime boundary for the approved `M-B_responsive_soft` motion direction without inventing the still-open timing/easing/intensity values.

## Runtime contract

- `MotionAssetResolver` resolves semantic animation keys only.
- `SpriteAtlasDescriptor` owns provider-neutral atlas pixel bounds and ordered frame rectangles.
- `MotionPlaybackPlanner` requires both an authored atlas descriptor and an injected `MotionPlaybackTuning` before animation can run.
- `SpriteAtlasFrameSelector` is a pure elapsed-time function. It never reads the device clock.
- `sprite-gen` remains an offline authoring tool and is not an Android/runtime dependency.

## Approved M-B source semantics carried into runtime

- `idle_breathe` -> loop.
- `clue_react` -> one shot.
- `resolved_settle` -> one shot.
- `walk` -> experimental only; runtime planner keeps it static.
- reduced motion -> static current semantic expression.

These are copied from `design/export-spec/m-b-motion-pilot.v1.json`; no new visual decision is introduced here.

## Fail-closed behavior

Animation remains static when any of the following is true:

- reduced motion is enabled;
- the motion is experimental-only;
- the semantic animation asset is unavailable;
- no authored atlas descriptor exists;
- no human-approved timing/easing/intensity tuning exists;
- approved tuning frame count does not match the authored atlas.

There is intentionally no default frame duration, easing, intensity, FPS, or total animation duration in production code.

## Human TODO

- TODO(human/design): approve visible M-B prototype timing, easing, and intensity.
- TODO(design/export): produce the runtime atlas + frame manifest from the approved storyboard/export pipeline.
- TODO(development): after both inputs exist, add the Android atlas bitmap adapter and visible Compose playback QA without changing the provider-neutral planner contract.
- TODO(human/design): approve the visible prototype before any M-B tuning is promoted to production data.

## Test boundary

Unit tests cover:

- no tuning -> static fallback even if an atlas exists;
- reduced motion -> static semantic expression;
- `walk` -> experimental/static;
- injected approved tuning -> atlas playback plan;
- frame-count mismatch -> static fail-closed;
- deterministic loop and one-shot frame selection;
- approved M-B loop/one-shot semantics.
