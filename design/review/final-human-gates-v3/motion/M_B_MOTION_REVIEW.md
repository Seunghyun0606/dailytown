# M-B Motion Human Gate Review

Status: **HUMAN APPROVED / LOCKED — M-B2 CALM REACTIVE**

## Locked scope

M-B is a timing/intensity decision inside the approved Candidate 3 identity. No new pose family, anatomy, costume, expression artwork, or semantic state is introduced.

Motion sources:

- `neutral / idle` → accepted `neutral` Moru authority
- `clue_found` → accepted `clue_found` Moru authority
- `resolved` → accepted `resolved` Moru authority

The candidates below change only presentation transforms/timing around those existing raster states.

## Human decision · 2026-09-15

The recommended **M-B2 Calm Reactive** candidate is approved and locked for Development handoff.

Locked behavior:

- idle: 2600 ms, `cubic-bezier(.4,0,.2,1)`, ±1.4% bbox vertical amplitude with cap 1.0 dp map / 1.2 dp HUD, scale `1.000 ↔ 1.008`, rotation ±0.4°, looping
- `clue_found`: 720 ms total = 90 ms anticipation + 210 ms accent + 420 ms settle; accent reaches -3.5% Y, +2.5% scale and 1.8° tilt; settle overshoot ≤0.5%
- `resolved`: 900 ms total = 100 ms anticipation + 240 ms accent + 560 ms settle; accent reaches -2.0% Y and 1.2° nod
- anticipation easing: `cubic-bezier(.4,0,.6,1)`
- reaction easing: `cubic-bezier(.16,1,.3,1)`
- settle easing: `cubic-bezier(.22,.72,.24,1)`
- `clue_found` and `resolved` are one-shot only and then hold their semantic static state
- reduced-motion contract below is mandatory

`M-B1 Quiet` and `M-B3 Warm Expressive` remain comparison history only and are not shipping defaults.

## Shared rules

- `idle` may loop.
- `clue_found` and `resolved` are one-shot reactions, then hold their semantic static state.
- No infinite bounce on `clue_found` or `resolved`.
- Transform origin: visual center / lower-body stable anchor; avoid foot skating.
- Sprout/scarf secondary motion is implied by whole-art transform in this review pack; no new raster-part authoring is performed.
- Map-scale motion must never compete with location/POI reading.
- Reduced motion removes translation, scale, and rotation. Preserve meaning through the semantic static image alone.

## Candidate comparison

| Parameter | M-B1 Quiet | M-B2 Calm Reactive | M-B3 Warm Expressive |
| --- | --- | --- | --- |
| Character | almost-static companion | calm everyday companion | warm but still restrained |
| Idle duration | 3200 ms | 2600 ms | 2200 ms |
| Idle easing | `cubic-bezier(.37,0,.63,1)` | `cubic-bezier(.4,0,.2,1)` | `cubic-bezier(.4,0,.2,1)` |
| Idle vertical amplitude | ±0.9% bbox, cap 0.7 dp map / 0.9 dp HUD | ±1.4% bbox, cap 1.0 dp map / 1.2 dp HUD | ±2.0% bbox, cap 1.4 dp map / 1.7 dp HUD |
| Idle scale | 1.000 ↔ 1.006 | 1.000 ↔ 1.008 | 1.000 ↔ 1.011 |
| Idle rotation | none | ±0.4° max | ±0.7° max |
| Loop | yes | yes | yes |
| Clue total | 820 ms | 720 ms | 640 ms |
| Clue anticipation | 100 ms, -0.7% settle-in | 90 ms, -1.0% settle-in | 80 ms, -1.4% settle-in |
| Clue accent | 230 ms, -2.4% Y, 1.5% scale, 1.0° tilt | 210 ms, -3.5% Y, 2.5% scale, 1.8° tilt | 180 ms, -4.8% Y, 4.0% scale, 2.6° tilt |
| Clue settle | 490 ms, no overshoot | 420 ms, tiny soft overshoot ≤0.5% | 380 ms, soft overshoot ≤0.8% |
| Resolved total | 980 ms | 900 ms | 780 ms |
| Resolved anticipation | 120 ms inhale | 100 ms inhale | 90 ms inhale |
| Resolved accent | 260 ms, -1.4% Y, 0.8° nod | 240 ms, -2.0% Y, 1.2° nod | 210 ms, -2.8% Y, 1.8° nod |
| Resolved settle | 600 ms | 560 ms | 480 ms |
| Disposition | lower-intensity reference | **LOCKED** | upper-intensity reference |

## Easing detail

### M-B1 Quiet

- anticipation: `cubic-bezier(.4,0,1,1)`
- reaction: `cubic-bezier(0,.0,.2,1)`
- settle: `cubic-bezier(.2,.8,.2,1)`

### M-B2 Calm Reactive — LOCKED

- anticipation: `cubic-bezier(.4,0,.6,1)`
- reaction: `cubic-bezier(.16,1,.3,1)`
- settle: `cubic-bezier(.22,.72,.24,1)`

### M-B3 Warm Expressive

- anticipation: `cubic-bezier(.45,0,.55,1)`
- reaction: `cubic-bezier(.12,.9,.22,1)`
- settle: `cubic-bezier(.18,.82,.24,1)`

Do not use spring values that introduce repeated oscillation. One perceptible settle is the maximum permitted character.

## Reduced-motion contract

When reduced motion is requested:

1. stop idle looping completely;
2. show the correct static semantic asset (`neutral`, `clue_found`, or `resolved`);
3. no translation, rotation, zoom, squash, bounce, or parallax;
4. optional opacity-only crossfade ≤120 ms when platform animation remains enabled;
5. if platform animation scale is zero/off, change state immediately with no transition.

Meaning must never depend on motion alone.

## Development handoff

Development may implement **M-B2 only** as the default motion contract after consuming this design decision. The implementation must reuse the existing Moru v2 semantic raster assets and must not introduce a new pose family or mutate the canonical/native/semantic source family.

The design Human Gate for M-B is closed. Runtime implementation and runtime QA remain Development-owned.
