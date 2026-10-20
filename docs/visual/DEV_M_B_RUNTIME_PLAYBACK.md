# M-B Runtime Playback — Development Integration

Status: **M-B2 Calm Reactive implemented as the default runtime motion contract (human gate locked 2026-09-15).**

Source of the decision: `design/final-human-gates-v3` →
`docs/design/FINAL_HUMAN_GATES_V3.md` §2 and
`design/review/final-human-gates-v3/motion/M_B_MOTION_REVIEW.md`.
Keyframe shapes follow the approved review preview `motion-review.html` (`mb2` keyframes).

## What changed

- The debug-only bitmap sprite **prototype** (`MoruPrototypeSpriteManifest`,
  `ProductionMoruSpriteMotionVisual`, `prototype_pending_human_tuning`) is removed.
- `MbCalmReactiveMotion` (pure Kotlin, `visual/`) holds the locked contract and samples a
  presentation transform for an injected elapsed time.
- `MbCalmReactiveMotionBox` (`ui/visual/`) applies that transform with `graphicsLayer` around the
  already-resolved semantic art. It never swaps poses or expressions, so it works identically for the
  v1 vector profile and the Moru v2 raster profile.
- Release and debug builds share the same behaviour; there is no debug-only motion path any more.

## Locked values

| Motion | Timing | Peak transform | Easing |
| --- | --- | --- | --- |
| idle (`neutral`, `happy`, `curious`, `surprised`) | 2600 ms loop, rest → peak → rest | −1.4% height (cap 1.0 dp map / 1.2 dp HUD), scale 1.008, 0.4° | `cubic-bezier(.4,0,.2,1)` per half |
| `clue_found` | 720 ms = 90 / 210 / 420 | anticipation +0.6% Y, scale 0.99 → accent −3.5% Y, scale 1.025, 1.8° → settle with ≤0.5% single overshoot | anticipation `(.4,0,.6,1)`, reaction `(.16,1,.3,1)`, settle `(.22,.72,.24,1)` |
| `resolved` | 900 ms = 100 / 240 / 560 | anticipation +0.6% Y → accent −2.0% Y, 1.2° nod → settle | same three curves |

- Transform origin: 50% x / 86% y (lower-body anchor, as in the approved preview).
- `clue_found` / `resolved` are one-shot and then hold the semantic static state; they restart only
  when the semantic expression changes.
- `walk` stays experimental and static.

## Reduced motion

Motion is fully disabled (static semantic image, no translation/scale/rotation/loop) when any of:

- `CompanionVisualRequest.reducedMotion` is true (call sites that are static by design: map avatar,
  encounter/discovery surfaces, Companion portrait, Records);
- Android "Remove animations" is on (`ANIMATOR_DURATION_SCALE == 0`);
- usage is `JOURNAL_CROP`, or the companion is not Moru.

The optional ≤120 ms opacity crossfade from the contract is not implemented; states change
immediately, which the contract also permits.

## Test behaviour

The idle loop is an infinite animation and runs on `withInfiniteAnimationFrameMillis`, so Compose UI
tests (which install an `InfiniteAnimationPolicy`) stay idle. Finite reactions use the regular
frame clock.

## Tests

- `MbCalmReactiveMotionTest` (JVM): locked timings, idle peak/caps/bounds, clue accent and overshoot
  ≤0.5%, resolved nod without scale, one-shot hold, anticipation direction, semantic → motion mapping,
  CSS cubic-bezier evaluation and monotonicity of the locked curves.
- `MotionPlaybackCoreTest` still guards the generic authored-atlas planner. Atlas providers continue to
  return no tuning because M-B2 is a whole-art transform contract, not per-frame atlas timing.

## Remaining

- Physical-device review of motion feel together with R-B outdoor readability (Human Gate R-B).
- Deciding whether the map avatar and Companion portrait should opt into idle motion (today they pass
  `reducedMotion = true`; the contract already defines the 1.0 dp map cap).
