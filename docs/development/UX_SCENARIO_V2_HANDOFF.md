# Daily Town — UX Scenario v2 Development Handoff

> Product/UX baseline: `docs/product/EXPLORATION_EXPERIENCE_SCENARIO_V2.md`
>
> Status: approved for presentation/state integration. Remaining character-art/motion/outdoor/identity Human Gates still apply.

## 1. Source and branch context

Always refresh `main` first because it remains the repository Source of Truth for merged implementation and architecture.

Current repository fact at handoff time:

- `main` contains the core `ui/DailyTownApp.kt` exploration implementation.
- PR #10 is intentionally **open and unmerged**.
- The PR head branch is `feat/emulator-test-harness` and contains the broader staged runtime/navigation/design-integration work used by the current validation effort.

For implementation work that depends on the staged 5-tab shell/integration, inspect `main`, then compare with PR #10 and continue on the existing PR branch rather than recreating those changes or merging the PR implicitly.

Do not merge PR #10 unless the user explicitly approves that separate gate.

## 2. Goal

Implement the approved UX hierarchy without changing established domain semantics:

`prepare → detect → approach → discover → investigate/resolve → record → relationship feedback → continue/finish`

The work is mainly presentation/state wiring and component separation, not a new encounter engine.

## 3. Implementation order

### T5.1 — presentation/component split

Resume the existing T5 refactor only where it helps implement the approved hierarchy.

Separate screen/presentation responsibilities so that Explore, Companion, Records, Goals, Settings, field-test/QA surfaces, and reusable UI components do not remain entangled in one large presentation file.

Preserve application/domain ownership and existing state flows.

### T5.2 — Explore live-state presentation

Map existing runtime concepts to visible UI states:

| Runtime/domain | Presentation |
| --- | --- |
| no active visible encounter / `hidden` | normal map / prepare state |
| `hinted` | unknown signal + distance + atmospheric hint |
| walking toward hinted encounter | approach state with active target emphasis |
| `discovered` | focused discovery reveal sheet |
| investigation active | compact mechanic-specific interaction surface |
| `resolved` | completion/result sheet with record/continue actions |

Requirements:

- map stays primary and mounted through active exploration
- avoid repeated modal interruption while walking
- keep provider attribution visible
- keep one primary encounter surface at a time
- preserve existing 180 m hint / 60 m discovery domain thresholds
- if a `near` presentation state is added between them, derive it in UI only; do not introduce a new domain transition/threshold contract

### T5.3 — discovery / resolution completion path

After resolution, surface already-available data as a coherent event:

- discovery/place title or semantic label
- clue/result
- Moru semantic reaction
- companion memory/bond feedback when available
- goal/progress contribution when available
- `기록 보기`
- `계속 탐험`

Do not add new persistence fields only to satisfy decoration. Prefer deriving presentation from existing persisted progress/domain state.

### T5.4 — Records journal integration

Implement the approved hierarchy around existing data:

- 오늘의 기록
- 장소
- 미스터리
- 단서
- Moru와의 기억

A detail presentation should connect a resolved encounter to the data currently available in persistence. Do not fake long-form `1/4 → 4/4 → secret location` progression before a corresponding domain model exists.

### T5.5 — Companion relationship presentation

Change visible hierarchy from stats-first to relationship-first:

1. Moru/current mood
2. contextual line
3. relationship stage + subtle bond progress
4. recent shared memory
5. conversation / memories / gift-keepsake entry actions
6. historical stats below the fold if still useful

Use existing semantic memories/bond data. Do not build an unconstrained AI-chat feature as part of this task.

### T5.6 — Goals and Settings cleanup

Goals should remain prompts back into exploration, not a KPI dashboard. Preserve the existing goal engine/evaluator.

Settings remains utility-only. Keep field-test/replay/diagnostic controls out of ordinary player exploration presentation and route them to debug/QA surfaces as already documented.

### T5.7 — first validation scenario copy

Use `오래된 가로수의 메모` as the first UX-validation scenario/copy pack while preserving the existing POI × mechanic/template planner.

Important: implement this as authored presentation/content over existing supported mechanics where possible. Do not create a new mystery engine solely for this scenario.

Candidate semantic clue key from product spec: `leaf_mark_note`. If the current clue/content contract has a different naming/registration requirement, adapt the authored content to that contract instead of weakening type/domain boundaries.

## 4. Design implementation rules

Before changing visual implementation, read the latest `docs/design/` rules.

- Compose implements layout/components.
- True UI controls/symbols may use vector assets.
- Moru, discovery art, place art, collectible/reward/mystery artwork must remain raster game art (PNG/WebP).
- Do not recreate missing Moru/discovery art with Compose Canvas, VectorDrawable, SVG body parts, or geometric placeholders presented as final art.
- Until Moru v2 canonical art is approved, preserve a semantic/fallback asset path and do not overwrite the current production pack.
- Avoid exposing generic Material 3 card/navigation styling as the final visual language where the approved design specifies map HUD / paper journal / relationship notebook surfaces.

## 5. Do not change in this implementation pass

Do **not** implement or promote the following without a new explicit decision:

- Camera/AR capture
- selectable-companion onboarding
- multi-fragment mystery-chain domain model
- new 120 m gameplay threshold
- Moru v2 production asset replacement/promotion
- final M-B motion constants or new animation dependency
- provider/map architecture
- production POI source/licensing behavior
- location privacy/persistence baseline
- PR #10 merge

## 6. Regression constraints

Preserve:

- `MapProvider` / map adapter boundary
- app-owned provider-neutral location boundary
- replay-friendly testing
- `hidden → hinted → discovered → resolved`
- current hint/discovery proximity logic
- idempotent clue inventory behavior
- progress persistence
- companion semantic memories/bond
- anti-repeat/content rotation
- revisit/time/companion weighting
- daily/weekly goal behavior
- raw high-frequency GPS non-persistence

## 7. Tests and verification

Minimum verification for changed presentation/state code:

1. focused JVM/domain tests remain green; no domain semantics should change unintentionally
2. relevant Compose/instrumented tests compile and pass
3. replay flow verifies hinted → discovered → resolved presentation transitions
4. back/continue/tab transitions do not reset active exploration unexpectedly
5. Records/Companion reflect persisted results after encounter resolution
6. reduced-motion state conveys the same information without required animation
7. map attribution and primary map controls are not obscured
8. semantic asset fallback still works when v2 raster assets are not yet approved/present
9. existing privacy/diagnostic tests remain unchanged/passing

Do not claim outdoor readability is final from emulator evidence; that remains a physical-device Human Gate.

## 8. Suggested development completion slices

Prefer small reviewable commits:

1. presentation extraction / component boundaries
2. Explore state surfaces
3. discovery/resolution result + navigation to Records
4. Records journal hierarchy
5. Companion relationship hierarchy
6. Goals/Settings visual hierarchy cleanup
7. authored validation scenario/copy + replay/UI tests

Keep behavior changes isolated from visual-only changes where practical.

## 9. Definition of done for this handoff

The development pass is ready for design/product review when:

- a replay or controlled location session visibly tells the approved story from signal detection through resolution
- the user can continue the walk without switching tabs for the live loop
- a resolved encounter becomes a readable Records artifact
- Companion visibly reflects a shared memory/relationship consequence
- Goals points back toward exploration
- no deferred feature is silently implemented
- no remaining Human Gate is falsely marked approved
- tests/build verification for the touched surface passes

After this pass, return to design/product review for visual parity and physical-device validation rather than expanding scope automatically.
