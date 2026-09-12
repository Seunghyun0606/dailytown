# Daily Town motion pipeline — sprite-gen evaluation

Status: design/tooling evaluation only. No Android runtime dependency is selected here.

## Naming

The reviewed tooling is **`sprite-gen`** (`aldegad/sprite-gen`). All Daily Town design documentation should use `sprite-gen` consistently.

## What sprite-gen is

`Sprite-gen` is an offline asset-generation/curation pipeline for character motion. It can take a base character drawing plus action definitions, generate candidate state rows, extract transparent frames, support curation/alignment correction, and compose a sprite atlas with machine-readable frame metadata.

Relevant production characteristics from the review:

- transparent sprite atlas output
- `manifest.json.frame_layout` with explicit frame rectangles
- fps/loop/duration metadata
- curation workflow with playback/alignment review
- GIF/contact-sheet QA
- alpha cleanup/frame extraction
- optional layered composition workflow
- deterministic breathe/idle support
- Apache-2.0 license

## Daily Town fit

### Strong fit

Use sprite-gen as a **production authoring/QA tool** for companion motion candidates:

- Moru `idle_breathe`
- `happy_bounce`
- `investigate`
- `clue_react`
- `resolved_settle`
- compact companion-stamp idle where motion is appropriate

Benefits:

- canonical source art remains the identity reference
- generated frames can be visually curated before approval
- atlas/frame metadata is compatible with replaceable semantic animation assets
- contact-sheet/GIF review exposes anatomy drift before runtime integration
- final asset can be replaced without changing gameplay/domain logic

### Conditional fit

`walk` is experimental until motion QA passes.

Moru and future companions require explicit review of:

- foot-contact continuity
- head/face stability
- body-mass/bounce consistency
- sprout/ear/tail continuity
- bag/scarf/accessory secondary motion
- loop seam at intended playback speed

No generated walk cycle is automatically considered production-approved.

### Poor fit / unnecessary

Do not use sprite atlases for procedural UI/map motion when native Compose/drawing is simpler:

- route progression
- active halo pulse
- discovery ring scale/alpha shell
- card/sticker settle
- color/token crossfade
- HUD transition

## Recommended hybrid architecture

### Asset-authored companion motion

```text
approved canonical companion art
 -> sprite-gen motion candidate
 -> frame curation + anatomy QA
 -> approved atlas + manifest
 -> semantic animation package
 -> Android playback adapter in development session
```

Recommended pilot order:

1. `idle_breathe`
2. `clue_react`
3. `resolved_settle`
4. `investigate`
5. `happy_bounce`
6. `walk` only after the earlier states prove the pipeline

### Native/procedural motion

Keep these in development/native animation:

- `effect.route.following`
- `effect.halo.active`
- `effect.halo.strong`
- discovery expansion/fade shell
- A-3 card/page transition
- journal sticker settle

## Semantic animation contract

```text
animation.companion.{companionId}.idle_breathe
animation.companion.{companionId}.walk
animation.companion.{companionId}.investigate
animation.companion.{companionId}.happy_bounce
animation.companion.{companionId}.clue_react
animation.companion.{companionId}.resolved_settle
```

Runtime-facing metadata should conceptually include:

- atlas id
- explicit frame layout/list
- frame duration or fps
- loop flag
- anchor/pivot
- static fallback semantic key
- reduced-motion fallback semantic key

Gameplay code must not know atlas coordinates or sprite-gen-specific filenames.

## Modular appearance concern

Daily Town supports affinity/cosmetic changes. Baking every appearance combination into every animation would create combinatorial growth.

Preferred production order:

1. animate canonical body + identity-critical parts
2. keep non-moving cosmetic overlays separate where visually acceptable
3. evaluate sprite-gen layered composition only for accessories that require synchronized motion
4. pre-bake only high-value approved appearance profiles
5. never produce the full Cartesian product of expression × lighting × affinity × animation

Lighting should stay a presentation dimension whenever possible rather than forcing separate motion generation for every time phase.

## Motion accessibility

Every animated semantic state requires:

- static fallback
- reduced-motion fallback
- no gameplay meaning communicated only by animation

Examples:

- `idle_breathe` -> neutral still
- `happy_bounce` -> happy/resolved still with static accent
- `clue_react` -> clue_found still + static discovery cue
- halo pulse -> static multi-ring halo
- route progression -> static route + direction symbol

## Production decision

**Adopt sprite-gen experimentally as an offline design/asset-pipeline tool, not as an Android runtime dependency.**

The first production pilot should use Moru A-2 and short states before attempting locomotion.

## Development-session handoff

Development may choose the atlas playback implementation independently. The design contract requires only a tool-neutral playback interface consuming approved atlas/frame metadata and semantic animation keys. Compose/native animation remains preferred for non-sprite UI/effect motion.
