# Daily Town Documentation Map

This directory contains repository-owned knowledge for Daily Town.
Project-level operating rules (Notion Project OS, Human Gate, deduplication, session reporting, and source routing) live in the ChatGPT Project master instructions.
The files here provide **discipline-specific execution context**.

## Session instructions

Choose instructions by the current task rather than applying every document to every session.

### Design

Use:

- `/docs/design/DESIGN_SESSION_INSTRUCTIONS.md`
- `/docs/design/README.md`
- `/docs/design/ART_DIRECTION.md`
- `/docs/design/ASSET_PRODUCTION.md`
- `/docs/design/QUALITY_GATE.md`

For UI/UX, visual design, characters, companions, backgrounds, game art, collectibles, rewards, and design QA.

### Development

Use:

- `/docs/development/DEVELOPMENT_SESSION_INSTRUCTIONS.md`
- `/docs/ARCHITECTURE.md`
- related code, tests, and technical documents

For Kotlin/Compose implementation, architecture, bugs, tests, location/GPS, map integration, persistence, build/CI, and performance.

### Product / Planning

Use:

- `/docs/product/PRODUCT_SESSION_INSTRUCTIONS.md`
- `/docs/ROADMAP.md`
- `/docs/ARCHITECTURE.md` when feasibility matters
- Notion `DailyTown` project context according to the Project master instructions

For feature planning, MVP scope, prioritization, game loop, roadmap, user value, and next-action decisions.

### Mixed sessions

Use only the relevant combination.

Examples:

- UI redesign + Compose implementation → Design + Development
- new game system + feasibility → Product + Development
- collection system + art direction → Product + Design

Do not load unrelated discipline instructions merely because they exist.

## Repository-owned documents

- `ARCHITECTURE.md` — architecture and product/technical constraints
- `ROADMAP.md` — implementation checklist and human-blocked work
- `MAP_PROVIDER_CONTRACT.md` — map-provider abstraction/replacement boundary
- `REAL_DEVICE_TEST.md` — physical-device validation procedure
- `HUMAN_ACTIONS.md` — human-owned setup, credentials, and release checks
- `design/` — visual design system and asset-production rules

## Source-of-truth principle

The repository documents describe codebase-owned truth.
Project management state, accepted project Decisions, Actions, Knowledge, Inbox, and Next Action are managed in Notion according to the ChatGPT Project master instructions.

If a durable repository-owned rule changes, update the appropriate document instead of leaving the decision only in chat.