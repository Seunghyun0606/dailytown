# M-B Runtime Playback — Development Integration

Status: **internal/debug visible bitmap sprite-atlas prototype implemented; final timing/easing/intensity approval still pending.**

This document records the Android/runtime boundary for the approved `M-B_responsive_soft` motion direction without treating prototype tuning as a final design approval.

## Runtime contract

- `MotionAssetResolver` and `MotionPlaybackCore` retain the provider-neutral semantic atlas contract.
- `MoruPrototypeSpriteManifest` now provides a visible development prototype for the three approved pilot motions.
- `ProductionMoruSpriteMotionVisual` renders promoted Moru vector layers into multiple bitmap frames, packs those frames into one in-memory bitmap atlas, and displays cropped atlas frames sequentially.
- `sprite-gen` remains an optional offline authoring/curation tool and is not an Android runtime dependency.
- production callers continue to use semantic companion state; raw design filenames remain behind the production asset registry/renderer boundary.

## Visible prototype states

- `idle_breathe` -> multi-frame loop.
- `clue_react` -> multi-frame one shot ending in `clue_found`.
- `resolved_settle` -> multi-frame one shot ending in `resolved`.
- `walk` -> remains experimental/static.
- reduced motion -> static semantic expression.

The prototype creates real bitmap sprite frames from the promoted Moru vector layers. It is no longer a single SVG/vector bitmap shown unchanged on every frame.

## Human-gate policy

The frame durations, small scale offsets, vertical offsets, and rotation offsets in `MoruPrototypeSpriteManifest` are **prototype-only values** used so the motion can be reviewed on a real Android device.

They are intentionally labeled:

`prototype_pending_human_tuning`

Until the human M-B timing/easing/intensity gate is explicitly approved:

- `BuildConfig.DEBUG` / internal builds may show the visible sprite prototype;
- release builds use the existing static production companion renderer;
- the prototype values must not be described as the final approved M-B tuning;
- `walk` must not be promoted from experimental status.

## Product integration

`ProductionCompanionVisual` now routes eligible Moru debug/internal rendering through the bitmap sprite prototype. This means the installed internal APK can review motion in the actual Explore/Companion product surfaces instead of only a synthetic motion test screen.

Journal stamp usage stays static so historical record surfaces remain stable and reduced-motion-safe.

## Tests

JVM coverage now verifies:

- all three approved pilot motions have multi-frame prototype sequences;
- idle loops while clue/resolved are one-shot;
- clue/re-solved sequences end on the correct semantic expression;
- `walk` remains unavailable;
- prototype tuning is explicitly not marked final-approved.

Existing `MotionPlaybackCoreTest` continues to guard the generic authored-atlas planner and fail-closed production behavior.

## Remaining human/design work

- review the visible internal APK prototype on a real Android device;
- choose PASS/TUNE for timing, easing feel, and intensity;
- if tuning changes, update the prototype manifest and repeat visible QA;
- only after explicit approval may release builds enable the authored M-B motion profile.
