# Daily Town P1 — A-3 Component Fit Spec v1

Status: P1 design specification. No Android/Kotlin implementation.

## Goal

Validate that the approved A-3 storybook/paper source kit can compose the five P0 screens without screen-specific one-off art or time-of-day recoloring.

Reference source master:

- `design/source/a3/a3-ui-source-master-v1.svg`

Reference semantic assets:

- `surface.journal.paper`
- `surface.collection.paper`
- `surface.memory.paper`
- `sticker.discovery.default`
- `card.clue.unresolved`
- `card.clue.resolved`
- `stamp.companion.default`
- `stamp.memory.resolved`
- `collection.locked.pattern`

## Shared composition rules

1. Paper is the persistent record surface; MORNING/SUNSET/NIGHT do not recolor the entire paper UI.
2. Original event lighting may appear only inside memory/discovery artwork or a contained hero vignette.
3. Texture is decorative. Removing texture must not break hierarchy or status meaning.
4. Locked/completed/resolved states use iconography, structure, label and/or pattern in addition to color.
5. Companion identity is displayed through semantic stamp/portrait slots, never a baked Moru-only card.
6. All companion stamps have a canonical 48 dp target review and remain acceptable in the 32–64 dp supported range.
7. Screen composition must survive a narrow Android width without horizontal clipping of core content.

## Screen 1 — Journal Home

### Purpose

Chronological record of walks, discoveries, clues and solved moments.

### Required composition

- A-3 page surface
- date/day grouping label
- event row/card
- discovery sticker or state icon
- companion stamp
- short location/title text
- optional event-time vignette

### Layout behavior

- prioritize vertical chronology
- decorative torn edge/tape may appear on selected or featured entries only
- companion stamp occupies a stable slot; it must not change card width unpredictably by companion
- route/location metadata remains typographic, not baked into paper imagery

### Fit acceptance

- at least 3 consecutive entries readable without decorative collision
- neutral, clue_found and resolved entries visually distinguishable without color alone
- 48 dp companion stamp recognisable

## Screen 2 — Discovery Detail

### Purpose

Show one found place/object/story fragment with evidence and companion reaction.

### Required composition

- paper frame
- discovery sticker
- hero image/vignette slot
- title + short narrative
- found-at metadata
- companion reaction stamp/portrait
- follow-up clue/action slot where relevant

### Fit acceptance

- hero art may carry original MORNING/SUNSET/NIGHT atmosphere while page remains A-3 stable
- clue state remains readable if hero is removed
- long title wraps without colliding with stamp or sticker

## Screen 3 — Clue Note

### Purpose

Track unresolved and resolved evidence.

### Required composition

- `card.clue.unresolved` or `card.clue.resolved`
- clue icon/object slot
- evidence text
- progress/state label
- optional companion reaction
- resolved mark only after semantic resolution

### State grammar

Unresolved:

- open edge / incomplete motif
- no completion seal
- active evidence affordance

Resolved:

- completion seal/check motif
- quieter visual priority than newly discovered clues
- resolution date/short conclusion slot

### Fit acceptance

- monochrome/grayscale review still distinguishes unresolved vs resolved
- no critical text embedded in texture

## Screen 4 — Collection Grid

### Purpose

Category/completion-oriented browsing.

### Required composition

- stable paper background
- collection category header/filter area
- repeatable collection tiles
- locked pattern
- completed identity/icon
- optional small count/progress

### Fit acceptance

- locked tile is distinguishable by structure/pattern, not dimming alone
- completed tile remains recognisable without decorative sticker
- grid supports mixed categories without requiring a unique card shape per category

## Screen 5 — Memory Detail

### Purpose

Preserve emotionally important solved moments.

### Required composition

- memory paper surface
- large hero frame
- original event-time atmosphere allowed inside hero
- resolved memory stamp
- companion stamp/portrait
- place/date metadata
- short reflective copy

### Fit acceptance

- hero is primary, paper frame is secondary
- resolved state remains clear with hero hidden
- companion stamp can use historical appearance if available, canonical/base fallback otherwise

## Reusable component families

```text
A3PaperSurface
A3RecordCard
A3DiscoverySticker
A3ClueCard
A3CompanionStamp
A3MemoryStamp
A3LockedPattern
A3HeroFrame
A3MetadataRow
```

Names above are design semantics, not required Kotlin class names.

## Motion compatibility

Preferred native/procedural motion:

- page/card crossfade
- sticker settle-in
- completion stamp one-shot
- tiny companion idle where appropriate

Avoid:

- perpetual paper wobble
- ambient particles across reading surfaces
- large parallax during text reading

Reduced motion:

- instant/static placement or brief opacity transition
- completion uses static resolved mark

## QA matrix

Each screen should be reviewed in:

- canonical Moru stamp
- one non-Moru candidate companion stamp
- normal motion
- reduced motion
- light device surface context
- dark surrounding device context while keeping A-3 paper stable

## Human gate

No new human product decision is required for this component-fit system. Actual high-fidelity image boards should be created only after the preferred affinity/companion visual candidates for inclusion are approved.