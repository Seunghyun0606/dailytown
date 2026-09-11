# Daily Town Design System

This directory is the single source of truth (SSOT) for Daily Town visual design.
Project-wide routing, Notion persistence, Human Gate, and deduplication rules live in the ChatGPT Project master instructions. This directory contains only design-owned rules.

## Design-session entry point

For a design-related task, first read:

1. `/README.md` — current product/MVP context
2. `/docs/design/DESIGN_SESSION_INSTRUCTIONS.md` — design-session execution rules
3. `/docs/design/README.md` — this manifest
4. `/docs/design/ART_DIRECTION.md` — visual language and asset-category policy
5. `/docs/design/ASSET_PRODUCTION.md` — production/output rules
6. `/docs/design/QUALITY_GATE.md` — review criteria
7. Relevant implementation/domain files for the screen/feature being designed

Do not rely on chat memory alone for substantive design work. Read the latest `main` branch.

## Source-of-truth rule

- GitHub `main` is authoritative for the visual design system and repository-owned product implementation.
- Re-read the latest design docs at the start of a new design session and before a substantial art-direction decision.
- If a design document changed since a previous session, the newest GitHub version wins.
- If GitHub cannot be read, state that clearly instead of reconstructing the current design rules from memory.

## Current product context

Daily Town is currently a location-based exploration game prototype built with Android native Kotlin + Jetpack Compose. The experience centers on real-world walking, lightweight mysteries, companion interactions, map-based discovery, goals, and collectible/progression feedback.

The art direction must support the actual current product rather than assuming a generic town-builder UI.

## Design document ownership

- `DESIGN_SESSION_INSTRUCTIONS.md` — scoped rules used only for design sessions
- `ART_DIRECTION.md` — stable visual principles and asset classification
- `ASSET_PRODUCTION.md` — file formats, technical production rules, and asset handoff
- `QUALITY_GATE.md` — mandatory QA checklist before accepting an asset or screen
- `CHATGPT_PROJECT_MASTER_PROMPT.md` — deprecated compatibility path that redirects to `DESIGN_SESSION_INSTRUCTIONS.md`

## Change policy

When a durable design decision is explicitly accepted and repository modification is allowed by the Project-level Human Gate:

1. Update the relevant document in this directory.
2. Prefer changing the rule in GitHub rather than leaving it only in chat.
3. Keep temporary exploration notes out of the core guide until a direction is accepted.
4. If a new rule conflicts with an existing rule, resolve the conflict in the documents instead of preserving both.

The goal is for a future design session to reconstruct the current design system from this directory without depending on previous chat history.
