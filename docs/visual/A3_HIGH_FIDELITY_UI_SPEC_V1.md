# Daily Town A-3 High-Fidelity UI Spec v1

Status: high-fidelity visual specification derived from the approved A-3 storybook/paper surface language. No Android/Kotlin implementation is included.

## Purpose

A-3 is the stable record-and-memory language of Daily Town. It must feel more tactile and intimate than the live exploration map while remaining legible, modular, and compatible with Compose layout primitives.

The five P0 screens are:

1. Journal Home
2. Discovery Detail
3. Clue Note
4. Collection Grid
5. Memory Detail

## Global shell

### Visual hierarchy

- outer app chrome remains simple and neutral
- content artifact uses warm paper surfaces
- section hierarchy is carried by spacing, typography, rules, cards, stamps, and illustration framing
- botanical decoration is optional and never carries information alone

### Shared visual tokens

- `a3.paper.base`: warm ivory
- `a3.paper.raised`: lighter paper/card
- `a3.paper.shadow`: warm low-contrast shadow
- `a3.ink.primary`: dark botanical ink
- `a3.ink.secondary`: muted gray-green metadata
- `a3.accent.botanical`: leaf/sage accent
- `a3.accent.memory`: butter/peach memory accent
- `a3.rule`: soft divider line
- `a3.locked.pattern`: non-color locked-state pattern

### Layout rhythm

Recommended design rhythm:

- large page edge margin
- 8/12/16/24 dp-like spacing rhythm for implementation translation
- one dominant content region per screen
- rounded paper cards with restrained elevation
- avoid dense ornamental frames around all elements

## 1. Journal Home

### Role

Chronological entry point for recent exploration history.

### Structure

- top title: Journal / 일지
- summary strip: recent discoveries, clues, resolved stories
- date-grouped vertical entry list
- each entry includes:
  - date/time metadata
  - place/story title
  - one discovery sticker or thumbnail
  - compact companion stamp
  - status badge/icon

### Visual behavior

- newest entry receives stronger paper elevation, not stronger saturation only
- solved story uses completion stamp/icon
- clue-only entry uses clue strip/icon
- companion reaction remains secondary to the record itself

### Semantic components

- `journal.summary.card`
- `journal.entry.default`
- `journal.entry.resolved`
- `journal.entry.clue`
- `journal.entry.companion_stamp`

## 2. Discovery Detail

### Role

Readable record of one place/discovery encounter.

### Structure

- location/story title
- date + neighborhood metadata
- main discovery image/sticker frame
- short story/description text
- clue strip if present
- small map snapshot/locator slot where useful
- companion reaction/stamp
- related memory/collection links

### Visual behavior

- illustration/image is dominant but does not consume the full screen
- map snapshot visually remains a utility artifact, not an illustrated fantasy map
- original time-of-day may tint the discovery image, but paper frame remains A-3 stable

### Semantic components

- `discovery.hero.frame`
- `discovery.metadata`
- `discovery.story.body`
- `discovery.map.snapshot`
- `discovery.related.card`

## 3. Clue Note

### Role

Focused notebook-like reading surface for one clue.

### Structure

- clue number/state
- clue title
- icon/illustration/object image
- short observation text
- source/place reference
- solved/unresolved state
- optional companion reaction

### State treatment

Unresolved:

- open question mark/notebook affordance
- incomplete stamp outline or patterned status

Resolved:

- explicit solved stamp/check symbol
- title/state text
- no dependence on green alone

### Semantic components

- `clue.note.default`
- `clue.note.unresolved`
- `clue.note.resolved`
- `clue.note.source`

## 4. Collection Grid

### Role

Category and completion-driven catalog of discoveries.

### Structure

- category tabs/filters
- completion summary
- grid of collection cards
- locked/unknown entries
- completed entries
- optional rarity/importance frame

### Card hierarchy

Each card supports:

- silhouette/icon or illustration
- title label
- completion state
- optional category marker

### Locked-state policy

Locked entries must differ by:

- silhouette treatment
- internal pattern
- lock/unknown icon
- label treatment

Blur or desaturation alone is insufficient.

### Semantic components

- `collection.filter.chip`
- `collection.card.default`
- `collection.card.locked`
- `collection.card.completed`
- `collection.card.important`

## 5. Memory Detail

### Role

Emotionally important record of a resolved story or meaningful moment.

### Structure

- large memory hero frame
- memory title
- date/place
- short caption or story excerpt
- companion vignette/stamp
- completion seal
- related discoveries/clues

### Visual behavior

- largest illustration allowance among A-3 screens
- fewer controls and more whitespace
- event time-of-day may remain inside hero artwork
- outer paper artifact remains stable across day phases
- affinity cosmetic may be shown if it represents the historical moment, but canonical recognition remains required

### Semantic components

- `memory.hero.frame`
- `memory.title`
- `memory.caption`
- `memory.companion_vignette`
- `memory.completion_stamp`
- `memory.related.strip`

## Shared components

### Companion stamp

- target: compact 32–64 dp family
- canonical silhouette remains readable
- expression may vary
- affinity cosmetic is optional
- monochrome/limited-color fallback required

### Discovery sticker

- organic rounded square/circle family
- category or place motif
- completion treatment separate from category color

### Memory completion seal

- one-shot animation allowed on first reveal
- static stamped state thereafter

### Chips/tags

- paper/outline style
- selected state combines weight/fill/icon, not hue only

## Bottom navigation relationship

A-3 screens may share the product-level navigation shell with exploration, but the paper artifact must not be visually fused into map chrome.

Recommended record destinations:

- Journal
- Collection
- Memory may be a Journal/Collection subdestination if product IA prefers fewer primary tabs

This document does not lock product navigation count; it locks visual treatment and screen hierarchy.

## Motion behavior

Allowed:

- short page/card crossfade
- sticker settle-in
- completion seal one-shot
- very subtle companion stamp idle

Reduced-motion:

- direct state transition or short opacity transition
- no required positional travel
- static completion seal

## Accessibility/readability

- body copy contrast takes priority over paper texture
- minimum tap targets follow platform guidance during implementation
- status does not depend on color
- paper grain must be visually suppressed behind dense text
- long story copy uses clear sans typography, not decorative handwriting
- companion stamps never replace text labels for critical state

## High-fidelity acceptance checklist

- all five P0 screens share one recognisable A-3 family
- Journal feels chronological, Collection categorical, Memory emotional
- paper surface remains stable MORNING/SUNSET/NIGHT
- decorative assets can be removed without losing information hierarchy
- Moru/Luca/Pino/Beri companion stamps fit the same component shell
- locked/completed collection cards are distinguishable without color
- resolved/unresolved clue note is distinguishable without color
- memory hero can preserve original time-of-day without recoloring the entire UI
- layouts remain feasible for narrow Android phone widths
