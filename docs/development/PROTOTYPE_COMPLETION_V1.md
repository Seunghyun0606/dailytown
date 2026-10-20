# Prototype completion v1 — minimum playable product

Status: **all approved-scope prototype flows implemented and E2E-tested on emulator (2026-10-20).**

Scope baseline: approved product scenario v2 (`design/final-human-gates-v3` →
`docs/product/EXPLORATION_EXPERIENCE_SCENARIO_V2.md`, `docs/development/UX_SCENARIO_V2_HANDOFF.md`)
plus the candidate §8 onboarding and §10 walk summary. The first planning wireframe
("Wandering With You") was mapped against those documents; features they explicitly defer stay out.

## Flow now covered end to end

`first run → prepare → detect → approach → discover → investigate/resolve → record →
relationship feedback → continue / stop → walk summary → Records / Companion / Goals → Settings`

| Area | Implementation |
| --- | --- |
| First run | `ui/OnboardingFlow.kt`: promise → how a walk works + location/privacy explanation (before any permission prompt) → meet Moru → Explore. Skippable, replayable from Settings. State in `onboarding/OnboardingStore.kt` (separate from progress). |
| Encounter copy | `mystery/ScenarioContentPack.kt` (`scenario-pack-v1`): approved Old Ginkgo story for `TRACE_CHAIN`, engineering drafts with the real place name for the other 7 mechanics, revisit variants, load-time validation. |
| Encounter ids | `EncounterCoordinator.reset(sessionKey)`: per-session ids, so re-solving the same place/template in a later walk counts again. |
| Walk summary | `progress/WalkSessionSummary.kt` + `ui/WalkSummaryCard.kt`: on stop, distance/time, new vs revisited places, clues, resolutions, memories, bond change, goals completed, Moru closing line; `오늘의 기록 보기` / `끝내기`. Derived from existing progress only. |
| Companion | `companion/CompanionContent.kt`: relationship tiers → affinity appearance layer (HUD, portrait, result), bond meter to next tier, authored rule-based dialogue with two replies per topic, keepsakes (one per mechanic) unlocked by resolved mechanics. |
| Records | System Back returns to Records home from details; developer-facing sentences replaced with player copy. |
| Settings | Location/privacy explanation, replay onboarding, "탐험 기록 모두 지우기" with confirmation (`ProgressRuntimeCoordinator.resetAll`, refused in fallback mode), 19시 reminder chip, POI status from the actual source, QA entry gated by `BuildVariantPolicy`. |
| Map without key | Player-facing placeholder text instead of a developer TODO. |
| Mechanic scenarios | Own place/clue/keepsake art for all 8 mechanics and mechanic-specific investigation steps (`docs/visual/DEV_MECHANIC_SCENARIOS.md`). |
| Moru art | v2 Candidate 3 is the default in debug and release after the R-B physical-device PASS (`docs/validation/R_B_OUTDOOR_READABILITY_RESULT.md`). |

## Tests

- JVM: `ScenarioContentPackTest`, `CompanionContentTest`, `WalkSessionSummaryTest`,
  `ProgressRuntimeCoordinatorTest` (reset), `EncounterCoordinatorTest` (session keys),
  `MoruV2SemanticResolverTest` (debug default).
- Instrumented: `PrototypeJourneyTest` runs the whole first-run → replay encounter → walk summary →
  Records → Companion dialogue/keepsake → reset path. Existing tests use `OnboardingStateRule`.
- Opt-in capture: `ProductJourneyCaptureTest` (`-e dailytownCapture true`) writes 17 review screens.

## Deliberately not implemented (deferred by the approved scenario)

| Planned in the first wireframe | Decision source |
| --- | --- |
| Companion selection (모리/보리/누비) | scenario v2 deferred scope; needs a companion roster |
| AR / camera capture | P1 optional journal enhancement, must not block completion |
| 1/4 → 4/4 fragment chains, secret places | "not part of the approved MVP runtime scope" |
| Free-text AI chat | "authored scenario rules … rather than unconstrained chatbot" |
| Friends, co-op, ranking | backend/social is P2 (`ENGINEERING_REMAINING.md` §9) |
| Fog of war / map %, creatures, collection hub, yearly recap | not in v2 documents; map must stay provider truth |

## Still human-gated

- Final authored copy for the 7 draft mechanics (pack entries are marked `ENGINEERING_DRAFT`).
- Affinity tier cut-offs (20/50 reused from the existing labels) and a BEST_FRIEND stage.
- Owner review of the generated mechanic art picks (`design/pipeline/mechanic_selection.v1.json`).
