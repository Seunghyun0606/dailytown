# Daily Town — Design Session Instructions

This document contains **design-session-only** execution rules for Daily Town.
It is subordinate to the ChatGPT Project master instructions. Project-wide rules such as Notion persistence, Human Gate, deduplication, and cross-source resolution continue to apply.

## 1. When to use this document

Apply this document only when the task is primarily about:

- UI/UX design
- visual design
- character / companion / NPC design
- backgrounds, place art, POI art
- collectibles, rewards, item art
- mystery / discovery illustration
- visual asset production
- design-system decisions
- design QA

For development-only or product/planning-only tasks, use the corresponding session instructions instead.
For mixed tasks, combine only the relevant session instructions.

## 2. Mandatory GitHub refresh

For every substantive design task, read the latest `main` branch rather than relying only on chat memory.

Read in this order:

1. `/README.md`
2. `/docs/design/README.md`
3. `/docs/design/ART_DIRECTION.md`
4. `/docs/design/ASSET_PRODUCTION.md`
5. `/docs/design/QUALITY_GATE.md`

Then inspect only the implementation/domain files relevant to the requested screen, feature, asset, or interaction.
Prefer targeted reads/searches over scanning the whole repository.

If the user says the repository or design guide changed, refresh the relevant files before continuing.
If GitHub is temporarily inaccessible, state that the design guidance was not verified against the latest repository.

## 3. Product-fit rule

Design for the actual current Daily Town product.

Daily Town is currently a location-based exploration experience centered on real-world walking, maps, lightweight mysteries, companion interactions, discovery, goals, collection, and progression feedback.
The repository is authoritative for the current implementation and may evolve.

Do not silently reinterpret the product as a generic town-builder, farming game, or decoration game unless the product direction explicitly changes.

## 4. Critical asset-format policy

Asset category determines production style.

### SVG/vector allowed only for true UI

Examples:

- navigation icons
- toolbar icons
- button glyphs
- close/back/confirm/settings symbols
- simple interface/system symbols
- purely navigational map UI symbols

### SVG/vector forbidden for non-UI game art

Never create, recommend, or fall back to SVG/vector treatment for:

- player characters
- companion characters
- NPCs
- character portraits or expressions
- backgrounds and place illustrations
- buildings
- furniture
- props or decorations
- collectibles
- inventory item artwork
- reward artwork
- resource/material artwork
- mystery/discovery illustrations
- POI/location artwork
- seasonal/event game art

Classification follows the content, not where it is displayed:

- a collectible inside an inventory screen is still collectible game art
- a character portrait inside a card is still character art
- a reward inside a dialog is still reward art

For non-UI art, use polished raster-oriented 2D mobile game art and target PNG/WebP delivery.
Do not generate SVG first and convert it to PNG as a workaround.
Do not use Android VectorDrawable, Compose Canvas primitives, or code-drawn geometric shapes as substitutes for game art.

## 5. Visual target

Non-UI game art should be:

- cozy
- warm
- emotionally inviting
- polished commercial mobile-game quality
- 2D raster illustration
- softly shaded
- subtly dimensional
- readable at small mobile sizes
- coherent across companion, mystery, place, collectible, and reward families

Avoid:

- flat SVG/vector aesthetics
- stock-vector or clip-art appearance
- corporate/web illustration
- generic app-icon treatment
- lifeless flat fills
- sterile geometric construction
- inconsistent generic AI-art styles
- excessive photorealism

## 6. Workflow

Before designing:

1. Refresh the mandatory design documents.
2. Identify whether each requested element is UI or game art.
3. Inspect current implementation when the task affects an existing screen or interaction.
4. Preserve current product behavior unless behavior change is explicitly requested.

While designing:

1. Separate interface structure from game-art content.
2. Keep vector/SVG decisions confined to true UI symbols.
3. Treat characters, collectibles, rewards, mystery art, and illustrated places as raster game art even when embedded in UI.
4. Optimize for actual mobile display size and map-heavy usage.

Before finalizing:

1. Evaluate the result against `/docs/design/QUALITY_GATE.md`.
2. Correct any accidental flat-vector/SVG drift in non-UI assets.
3. Check consistency with the current repository and established art direction.

## 7. Durable decisions

Do not let accepted design-system decisions live only in chat.

When a durable design decision is explicitly accepted and the Project-level Human Gate permits the write, update the appropriate repository document:

- `ART_DIRECTION.md` — visual language / asset classification
- `ASSET_PRODUCTION.md` — production format / handoff rules
- `QUALITY_GATE.md` — mandatory review criteria
- `README.md` — design-system navigation / reading protocol

Do not persist exploratory alternatives as established rules.

## 8. Output behavior

For design proposals, provide enough production detail to implement the result in the current Android project.

When recommending implementation, keep this distinction explicit:

- UI components/layout → Jetpack Compose; vector UI icons allowed
- game-art assets → raster assets required

When actual imagery is requested, produce or specify raster-oriented game art rather than SVG code.