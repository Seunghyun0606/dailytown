# Daily Town motion pipeline — sprite-gen evaluation

Status: design/tooling evaluation only. No Android runtime dependency is selected here.

## Name clarification

The library/tooling found for this requirement is `sprite-gen` (`aldegad/sprite-gen`). The requested name `stripe-gen` appears to refer to this project.

## What sprite-gen is

`stripe-gen` is not treated as an Android animation runtime library. `sprite-gen` is an asset-generation/curation pipeline that takes a base character image and action definitions, generates state rows, extracts transparent frames, curates them, and composes a runtime sprite atlas plus a machine-readable manifest.

Relevant characteristics at the time of review:
- transparent sprite atlas output
- `manifest.json.frame_layout` with frame rectangles, fps, and loop metadata
- curation webview with live playback and per-frame alignment correction
- per-state GIF/contact-sheet QA
- deterministic recolor workflow
- optional layered composition workflow
- a deterministic `breathe` operation for idle loops
- Apache-2.0 license
- short actions are described by the project as the safer/stable path; cyclic walk/run needs explicit motion QA

## Daily Town fit

### Strong fit

Use sprite-gen as a **production authoring/QA tool** for character motion assets:
- Moru `idle_breathe`
- short `happy_bounce`
- `curious/investigate`
- `clue_react`
- `resolved_settle`
- compact journal companion-stamp idle motion where motion is allowed

Why it fits:
- canonical source image can be used as the identity reference
- frame extraction/alpha cleanup reduces manual sheet preparation
- atlas metadata can support a replaceable semantic animation asset layer
- QA GIF/contact sheets make anatomy drift visible before runtime integration
- asset output can be replaced without changing domain logic

### Conditional fit

`walk` can be explored with sprite-gen, but should not be accepted automatically. Daily Town's Moru has a rounded silhouette and short legs, so foot contact, body bounce, bag/scarf secondary motion, and sprout motion need human review.

Treat walk-cycle generation as `candidate` until:
- foot-contact continuity passes
- head/face remains stable
- accessory/sprout motion does not flicker
- atlas loop passes a real-speed preview

### Poor fit / unnecessary

Do not use sprite sheets when Compose/native vector/draw animation is simpler:
- route dash movement
- active halo pulse
- simple scale/alpha discovery emphasis
- card/stamp settle animation
- color/token crossfade
- HUD transitions

Those are procedural UI/effect animations and should remain independent from character sprite production.

## Recommended hybrid motion architecture

### Asset-authored character motion

Authoring path:

```text
approved canonical companion art
 -> sprite-gen candidate states
 -> curation + QA
 -> approved atlas + manifest
 -> semantic animation asset package
 -> Android playback adapter in development session
```

Recommended first states:
- `idle_breathe`: loop, very subtle
- `happy_bounce`: short one-shot
- `investigate`: short one-shot / optional hold end frame
- `clue_react`: short one-shot
- `resolved_settle`: short one-shot
- `walk`: experimental candidate until QA passes

### Procedural Compose motion

Prefer native Compose animation/drawing for:
- `effect.route.following`
- `effect.halo.active`
- `effect.halo.strong`
- discovery light/scale burst shell
- UI page/card transitions
- journal sticker settle

This avoids sprite asset explosion and preserves theme-token control.

## Semantic animation contract

Suggested semantic keys:

```text
animation.companion.moru.idle_breathe
animation.companion.moru.walk
animation.companion.moru.investigate
animation.companion.moru.happy_bounce
animation.companion.moru.clue_react
animation.companion.moru.resolved_settle
```

Runtime metadata should conceptually include:
- atlas id
- frame rects or frame list
- fps
- loop flag
- anchor
- static fallback semantic key
- reduced-motion behavior

Do not bind gameplay code to atlas coordinates or sprite-gen-specific filenames.

## Modular appearance concern

Daily Town supports scarf/bag/accessory/affinity changes. Baking every cosmetic combination into independent animated atlases would cause combinatorial growth.

Preferred production order:
1. animate canonical body + identity-critical parts
2. test sprite-gen layered composition for accessories that require synchronized motion
3. keep non-moving cosmetic overlays separate when visually acceptable
4. pre-bake only approved high-value affinity variants
5. never generate `6 expressions × 3 lighting × N cosmetics × every animation` blindly

Lighting should preferably remain a runtime/presentation treatment or a small approved lighting-family export, not a new animation generation run for every day phase.

## Motion accessibility

Every animated semantic state requires:
- static fallback frame
- reduced-motion alternative
- no gameplay information communicated only by looping motion

Recommended reduced-motion behavior:
- `idle_breathe` -> neutral still
- `happy_bounce` -> resolved end pose with brief crossfade
- `clue_react` -> clue_found still + static sparkle
- halo pulse -> static multi-ring halo
- route animation -> static route with direction symbols

## Production recommendation

Decision: **adopt sprite-gen experimentally as a design/asset-pipeline tool, not as an Android runtime dependency.**

Pilot scope:
1. Moru `idle_breathe`
2. Moru `clue_react`
3. Moru `resolved_settle`
4. only then evaluate `walk`

Acceptance requires human visual QA of generated motion; tool output is never auto-approved as production art.

## Development-session handoff

Development may later choose the atlas playback implementation independently. The design contract requires only a provider/tool-neutral playback interface that consumes approved atlas/frame metadata and resolves semantic animation keys. Compose's native animation APIs remain preferable for non-sprite UI/effect motion.
