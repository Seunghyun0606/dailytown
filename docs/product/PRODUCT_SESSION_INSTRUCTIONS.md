# Daily Town — Product / Planning Session Instructions

This document contains **product/planning-session-only** execution rules for Daily Town.
It is subordinate to the ChatGPT Project master instructions. Project-wide Notion write rules, deduplication, Human Gate, and cross-source resolution continue to apply.

## 1. When to use this document

Apply this document when the task is primarily about:

- product direction
- feature planning
- MVP scope
- roadmap and prioritization
- user value / retention hypotheses
- game loop and progression
- content strategy
- business/product trade-offs
- next actions
- acceptance criteria

For design-only or development-only tasks, use the corresponding session instructions instead.
For mixed tasks, combine only the relevant session instructions.

## 2. Source resolution

Use the Project-level Notion `DailyTown` project as the authoritative source for:

- project status
- Next Action
- Actions / TODOs
- accepted Decisions
- reusable Knowledge
- Inbox / uncommitted ideas
- priority and execution state

Use GitHub `main` as the authoritative source for:

- current implementation
- technical architecture
- actual product behavior
- documented engineering constraints
- design system
- roadmap implementation checklist

Do not confuse an idea in discussion with a currently implemented feature.
Do not infer current product status from old chat context when Notion/GitHub can resolve it.

## 3. Default context read

For substantive product/planning work:

1. resolve the current `DailyTown` project context from Notion when project state, decisions, or next actions matter
2. read `/README.md`
3. read `/docs/ROADMAP.md` when scope/progress matters
4. read `/docs/ARCHITECTURE.md` when feasibility or technical constraints matter
5. inspect only the relevant implementation when the proposal depends on what currently exists
6. read `/docs/design/` only when the decision materially affects design/art direction

Prefer the minimum sufficient context rather than loading every source for every question.

## 4. Product evaluation discipline

When proposing or evaluating features, distinguish:

- user problem / desired behavior
- hypothesis
- currently implemented behavior
- missing capability
- validation method
- cost / complexity
- dependency / human decision
- accepted decision vs exploratory option

Avoid presenting speculative features as already decided.

For MVP decisions, optimize for learning and validation rather than feature count.
Preserve the core product question documented in `/docs/ARCHITECTURE.md` unless the user explicitly changes the product thesis.

## 5. Feasibility checks

Before recommending a significant product change, verify relevant constraints when they could change the answer, including:

- location/background behavior
- permissions/privacy
- map/POI provider constraints
- content exhaustion/replayability
- battery/device constraints
- Android MVP scope
- current implementation status

If a recommendation needs design or engineering judgment, route into the relevant session instructions instead of making unsupported assumptions.

## 6. Persistence and state changes

Use the Project-level Distillation Trigger and Write Rules.

Typical routing:

- unaccepted concept → Notion Inbox
- committed work → Notion Actions
- reusable research/learning → Notion Knowledge
- direction-affecting accepted choice → Notion Decisions
- changed execution state → update DailyTown Project / Next Action as appropriate

Do not create duplicate Notion items; search first according to the Project master instructions.

GitHub documentation should be updated only when a durable repository-owned rule, architecture contract, or design system actually changes and the Project-level Human Gate permits the write.

## 7. Mixed product + development/design work

Examples:

- feature concept + implementation feasibility → Product instructions + Development instructions
- new companion/collection system + visual language → Product instructions + Design instructions
- UI flow redesign + Compose implementation → Design instructions + Development instructions

When multiple disciplines are involved, identify which decisions belong to which source of truth so that product state, implementation, and design rules do not drift apart.