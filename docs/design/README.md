# Daily Town Design System

This directory is the single source of truth (SSOT) for Daily Town visual design.

## Mandatory reading protocol

For every Daily Town design session or design-related task, do **not** rely on chat memory alone.
Read the latest files from the `main` branch in this order:

1. `/README.md` — current product/MVP context
2. `/docs/design/README.md` — this manifest and reading rules
3. `/docs/design/ART_DIRECTION.md` — visual language and asset-category policy
4. `/docs/design/ASSET_PRODUCTION.md` — production/output rules
5. `/docs/design/QUALITY_GATE.md` — review criteria
6. Relevant implementation files for the screen/feature being designed, especially `/app/src/main/java/com/dailytown/app/ui/` and related domain files when needed

When a design task concerns only one asset category, still read the three mandatory design documents above before working.

## Source-of-truth rule

- GitHub `main` is authoritative over prior chat summaries or remembered design decisions.
- Re-read the latest design docs at the start of each new design session and before making a substantial new art direction decision.
- If a design document changed since the previous session, the newest GitHub version wins.
- If GitHub cannot be read, state that clearly rather than silently reconstructing the design rules from memory.

## Current product context

Daily Town is currently a location-based exploration game prototype built with Android native Kotlin + Jetpack Compose. The experience centers on real-world walking, lightweight mysteries, companion interactions, map-based discovery, goals, and collectible/progression feedback.

The art direction must support this experience rather than assuming a generic town-builder UI.

## Design document ownership

- `ART_DIRECTION.md`: stable visual principles and asset classification
- `ASSET_PRODUCTION.md`: file formats, technical production rules, and asset handoff
- `QUALITY_GATE.md`: mandatory QA checklist before accepting an asset or screen
- `CHATGPT_PROJECT_MASTER_PROMPT.md`: prompt to use for a ChatGPT Project design session

## Change policy

When a durable design decision is made in a design session:

1. Update the relevant document in this directory.
2. Prefer changing the rule in GitHub rather than leaving the decision only in chat.
3. Keep temporary exploration notes out of the core guide until a direction is accepted.
4. If a new rule conflicts with an existing rule, resolve the conflict in the documents instead of keeping both.

The goal is for a future design session to reconstruct the current design system by reading this directory, without depending on the history of previous chats.
