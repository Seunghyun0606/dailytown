# Daily Town Motion Production Spec v1

Status: visual/motion design specification. No Android/Kotlin implementation is included.

## Goals

Daily Town motion should:

- make companions feel alive without demanding constant attention
- improve discovery feedback without hiding map information
- remain safe for an outdoor walking context
- support reduced-motion behavior
- keep character identity stable across generated frames
- separate authored character animation from native UI/map effects

## Motion categories

### A. Companion sprite/illustration motion

Candidate for sprite-atlas production:

- `idle_breathe`
- `investigate`
- `happy_bounce`
- `clue_react`
- `resolved_settle`

Experimental / higher-QA requirement:

- `walk`

### B. Native UI/map motion

Prefer native Compose/drawing implementation rather than raster sprite atlases:

- route directional progression
- active halo pulse
- discovery ring expansion
- opacity/scale settle for stickers and cards
- time-of-day color/token crossfade
- small badge/state transitions

## sprite-gen evaluation

The reviewed project is `aldegad/sprite-gen` (Apache-2.0), not an Android runtime animation dependency. It is suitable as an authoring/QA pipeline for character sprite atlases.

Relevant production capabilities:

- one base drawing + action list -> state-row generation
- identity-aware frame generation workflow
- chroma cleanup to transparent alpha
- frame extraction
- curated frame selection/transform corrections
- runtime atlas composition
- machine-readable `manifest.json.frame_layout`
- per-state fps/loop/duration data
- GIF/contact-sheet QA
- static fallback when generation is blocked

Daily Town usage decision:

- **adopt as an optional offline authoring pipeline for companion motion candidates**
- **do not add sprite-gen as an Android runtime dependency**
- **do not use it for route/halo/general UI motion**
- **do not automatically approve generated walk cycles without human motion QA**

## Recommended sprite-gen pilot

Pilot only with Moru A-2 first.

### Pilot states

1. `idle_breathe`
2. `clue_react`
3. `resolved_settle`

Optional after pilot success:

4. `investigate`
5. `happy_bounce`
6. `walk` (experimental)

### Why these first

- they are short, readable actions
- identity drift is easy to notice
- they cover idle, gameplay reaction, and completion feedback
- they test looping and one-shot behavior
- they provide a useful production test without making locomotion a blocking dependency

## Motion semantic contract

Recommended keys:

```text
animation.companion.{companionId}.idle_breathe
animation.companion.{companionId}.walk
animation.companion.{companionId}.investigate
animation.companion.{companionId}.happy_bounce
animation.companion.{companionId}.clue_react
animation.companion.{companionId}.resolved_settle
```

Runtime-facing state must not encode atlas coordinates in gameplay/domain logic.

## Default motion behavior bands

Exact timing remains a human approval gate after prototype review. Until then, use these design bands rather than locked durations.

### idle_breathe

- loop: yes
- intensity: very low
- frequency: slow
- translation: minimal
- silhouette change: minimal
- reduced-motion: static neutral pose

### investigate

- loop: optional short loop or one-shot hold
- intensity: low-to-medium
- body lean/head motion permitted
- optional magnifier/object layer
- reduced-motion: curious static pose

### happy_bounce

- loop: no for normal UI; at most short bounded repetition where explicitly approved
- intensity: medium
- vertical bounce must remain small
- reduced-motion: happy static pose + static accent

### clue_react

- loop: no
- intensity: medium
- discovery cue may use one-shot accent
- must not become indistinguishable from generic happy
- reduced-motion: clue_found static pose + discovery icon

### resolved_settle

- loop: no
- intensity: low-to-medium
- motion ends in stable resolved pose
- reduced-motion: resolved static pose

### walk

- loop: yes when used
- experimental until anatomy/motion QA passes
- should preserve foot contact rhythm and body mass
- must not cause face/sprout drift across frames
- if QA fails, use static map avatar + native route motion instead

## Character-frame acceptance criteria

Every motion candidate must pass:

- face geometry consistency
- eye spacing consistency
- sprout/ear/identity appendage continuity
- hand/foot count consistency
- no background fringe after alpha extraction
- no frame overlap/atlas collision
- center/grounding consistency
- silhouette remains character-identifiable
- loop seam acceptable where looping
- no frame causes accessory to teleport unexpectedly

## sprite-gen output contract for development handoff

When sprite-gen is used, the approved handoff should include only curated production outputs:

```text
sprite-sheet-alpha.png
manifest.json
qa/<state>-contact.png
qa/<state>.gif
curation.json (design archive only, not required by runtime)
```

Runtime must sample explicit frame rectangles from `manifest.json.frame_layout`; it must not infer a fixed grid.

Per-frame durations, when present, are preferred over assuming uniform fps.

## Native motion system

### Route

States:

- `idle`
- `following`
- `completed`

Motion rule:

- directional cue may progress slowly during `following`
- no rapid marching-dash animation
- `completed` settles to a calmer line
- reduced-motion: static state-specific line style

### Active halo

States:

- `idle`
- `active`
- `strong`

Motion rule:

- gentle radial pulse only
- geographic center stays fixed
- amplitude is small
- avoid continuous bright flashing
- reduced-motion: static ring-count/weight distinction

### Discovery effect

States:

- `small`
- `medium`
- `big`

Motion rule:

- one-shot expansion/fade
- leaf/sparkle particles leave quickly
- never block labels for a sustained period
- reduced-motion: static final accent for a short presentation window

### A-3 surfaces

Allowed:

- card/page crossfade
- sticker settle
- completion stamp one-shot
- tiny companion idle where appropriate

Avoid:

- ambient looping particles
- perpetual paper wobble
- aggressive parallax

## Motion QA gates

Before motion integration is called production-ready:

- normal and reduced-motion versions both communicate state
- map motion remains subordinate to walking safety/readability
- loops are reviewed at real device scale
- sprite animations preserve identity at compact use sizes
- no full-screen repetitive celebration is required for routine gameplay
- animation assets can be replaced without gameplay/domain changes
