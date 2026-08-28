# Daily Town A-3 Journal / Collection / Memory System v1

Status: production visual specification draft based on approved Option A and A-3 storybook/paper surface language.

## Goal

A-3 is not a time-of-day theme. It is the stable storybook/paper language for persistent records: discoveries, clues, collections, memories, and completed neighborhood stories.

The system should feel warmer and more tactile than the live map while remaining structurally compatible with Compose UI and replaceable semantic assets.

## Surface families

### Journal
Purpose: chronological exploration log.

Visual grammar:
- warm paper base
- restrained botanical edge motifs
- date/location metadata in compact sans typography
- one dominant memory image/sticker area per entry
- clue and companion reactions appear as modular stamps, not baked into page background

Core semantic regions:
- `surface.journal.paper`
- `journal.entry.header`
- `journal.entry.memory_slot`
- `journal.entry.clue_strip`
- `journal.entry.companion_stamp`

### Collection
Purpose: structured completion and discovery catalog.

Visual grammar:
- cleaner grid than Journal
- paper cards with strong silhouette/icon hierarchy
- locked/unknown entries use shape and pattern, not blur only
- rarity/importance may use frame treatment, never color alone

Core semantic regions:
- `surface.collection.paper`
- `collection.card.default`
- `collection.card.locked`
- `collection.card.completed`
- `collection.group.header`

### Memory
Purpose: emotionally important resolved moments.

Visual grammar:
- largest use of illustration
- fewer controls, more breathing room
- Moru/companion stamp or vignette can appear as a secondary anchor
- time-of-day of the original event may tint the illustration, but the containing paper surface remains stable

Core semantic regions:
- `surface.memory.paper`
- `memory.hero.frame`
- `memory.caption`
- `memory.companion_stamp`
- `memory.tags`

## Shared A-3 token direction

Suggested semantic roles:
- `a3.paper.base` = warm ivory/paper
- `a3.paper.raised` = slightly lighter card paper
- `a3.paper.shadow` = warm neutral shadow
- `a3.ink.primary` = dark botanical ink
- `a3.ink.secondary` = muted gray-green metadata
- `a3.accent.botanical` = leaf accent
- `a3.accent.memory` = warm butter/peach accent
- `a3.rule` = soft divider/stroke

No A-3 screen should fully swap into a dark paper theme at night. If system dark-mode accommodation is needed, use a restrained outer chrome/background adaptation while preserving the identity of the paper artifact itself.

## Component set

### Discovery sticker
- square/rounded organic sticker silhouette
- supports POI/category illustration or symbol
- optional completion notch/stamp
- semantic key: `sticker.discovery.{category}`

### Clue card
- compact horizontal paper strip/card
- icon + short title + status affordance
- solved and unresolved states differ by stamp/icon/pattern, not hue only
- semantic key: `card.clue.{state}`

### Companion stamp
- simplified canonical companion portrait/silhouette
- intended for 32–64 dp range
- affinity cosmetic is optional; canonical face/silhouette remains readable
- semantic key: `stamp.companion.{companionId}.{expression}`

### Memory stamp
- decorative completion seal for resolved stories
- supports optional date/sequence number outside the artwork
- semantic key: `stamp.memory.{type}`

## Navigation hierarchy

Recommended information architecture:
- Journal = recent and chronological
- Collection = categorical and completion-driven
- Memory = curated emotionally significant outcomes

These screens should share a common top-level visual shell but not duplicate their information models.

## Motion policy for A-3

Persistent record screens should be calmer than the exploration map.

Allowed:
- page/card crossfade
- short sticker settle-in
- small companion stamp idle/breathe
- one-shot resolved seal/stamp animation

Avoid:
- continuous ambient particles over reading surfaces
- looping page wobble
- aggressive parallax
- large celebratory loops after first presentation

Reduced-motion fallback must preserve hierarchy using opacity, scale end-state, stamp shape, and static shadow.

## Production asset list

P0 assets:
- paper texture/surface primitives
- discovery sticker base + category variants
- clue card icon set
- companion stamp shell
- memory completion stamp
- empty/locked collection pattern

P1 assets:
- chapter divider illustrations
- seasonal paper accents
- rare-memory frame treatments

## Acceptance gates

- journal remains readable at small Android phone widths
- paper texture never reduces body-text contrast
- collection locked/completed state is understandable without color
- companion stamp remains recognizable at 48 dp
- memory hero artwork does not force all records into the time-of-day theme
- all decoration can be removed without breaking information hierarchy
