# Daily Town — Development Session Instructions

This document contains **development-session-only** execution rules for Daily Town.
It is subordinate to the ChatGPT Project master instructions. Project-wide rules such as Notion persistence, Human Gate, deduplication, and source-of-truth resolution continue to apply.

## 1. When to use this document

Apply this document when the task is primarily about:

- Android / Kotlin / Jetpack Compose
- code implementation or refactoring
- bugs and diagnostics
- architecture
- tests
- performance
- location / GPS
- map provider integration
- persistence
- domain logic
- build / CI / release engineering

For design-only or product/planning-only tasks, use the corresponding session instructions instead.
For mixed tasks, combine only the relevant session instructions.

## 2. Mandatory repository context

Before a substantive development task, read the latest `main` branch.

Default read order:

1. `/README.md`
2. `/docs/ARCHITECTURE.md`
3. the code directly related to the requested feature/problem
4. related technical documents under `/docs/`
5. related tests

Read `/docs/ROADMAP.md` when implementation scope, completion state, or next engineering work matters.
Read `/docs/design/` only when the development task affects visual design, UI treatment, game-art assets, or design-system compliance.

Prefer targeted search/fetch over scanning the entire repository.
Do not assume code structure from prior chat memory when current source can be checked.

## 3. Current architectural constraints

Preserve the repository's established boundaries unless the user explicitly asks to revisit them.

Current high-level direction includes:

- Android native Kotlin + Jetpack Compose for the MVP
- pure game/domain rules separated from Android/framework concerns
- provider-neutral boundaries for map/location/data dependencies
- `MapProvider` / adapter-based map integration
- replay-friendly location testing
- privacy-conscious handling of location data

Use `/docs/ARCHITECTURE.md` as the authoritative architecture document for the current version.

## 4. Change discipline

Before proposing a code change:

1. identify the current behavior from code/tests
2. identify the smallest relevant change surface
3. check architectural boundaries and public contracts
4. check existing tests and regression risk
5. distinguish a code defect from a product/design decision

Do not invent missing implementation details when repository inspection can resolve them.
Do not broaden a narrow fix into an unsolicited rewrite.

## 5. Human Gate

Repository writes are governed by the Project-level Human Gate.

Unless the current user request explicitly authorizes code/repository modification, keep the work at analysis/proposal/patch-plan level.
Do not interpret this document itself as permission to modify code, create PRs, or create GitHub Issues.

When modification is explicitly authorized:

- change only relevant files
- preserve existing behavior outside requested scope
- update/add tests when behavior changes
- update documentation when a durable architecture/contract changes
- report what was changed and what was not verified

## 6. Testing and verification

For implementation work, prefer verification in this order where applicable:

1. focused unit tests for changed domain behavior
2. existing related tests
3. build/lint checks
4. replay/integration checks for location and exploration flows
5. real-device validation only when the behavior genuinely depends on device sensors, map SDKs, permissions, battery, or OS behavior

Do not claim a test passed unless it was actually run or the repository evidence explicitly shows it.

## 7. Design interaction

When implementing a designed screen or visual asset:

1. read `/docs/design/DESIGN_SESSION_INSTRUCTIONS.md`
2. read the relevant design-system documents
3. keep SVG/vector usage limited to true UI symbols
4. do not recreate non-UI game art using Compose Canvas, VectorDrawable, SVG, or code-drawn approximations

UI implementation and game-art production are separate concerns.

## 8. Durable technical decisions

When a technical decision is explicitly accepted and belongs with the codebase, update the appropriate GitHub documentation when the Project-level Human Gate permits it.

Examples:

- architecture/boundary decision → `/docs/ARCHITECTURE.md`
- map integration contract → `/docs/MAP_PROVIDER_CONTRACT.md`
- device validation procedure → `/docs/REAL_DEVICE_TEST.md`
- human-owned setup or release prerequisite → `/docs/HUMAN_ACTIONS.md`

Project status, actions, decisions, and next actions remain managed through the Project-level Notion rules rather than being duplicated here.