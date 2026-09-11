# Daily Town — Exploration Experience Scenario v2

> Status: **APPROVED — 2026-09-11**
>
> Scope of approval: product/UX hierarchy and first UX-validation scenario. This approval does **not** approve Moru canonical v2 art, final motion timing, outdoor visual acceptance, or app identity lockup.

This document is the approved baseline derived from `EXPLORATION_EXPERIENCE_SCENARIO_V2_CANDIDATE.md` after Human Gate review.

## 1. Experience thesis

Daily Town should make a familiar neighborhood capable of producing a small new story each time the user walks through it.

The core session is:

`prepare → detect → approach → encounter/discover → investigate/resolve → record → relationship deepens → continue/finish`

The experience is not a marker-collection dashboard. Explore owns the live walk; the other tabs deepen or review what happened.

## 2. Approved five-tab roles

1. **Explore** — live session, signal detection, approach, discovery, investigation/resolution entry, completion feedback.
2. **Companion** — relationship notebook: current mood/context, bond stage, recent shared memory, conversation, keepsakes/gifts, long-term history.
3. **Records** — journal: places, mysteries, clues, discoveries, and Moru/shared memories.
4. **Goals** — gentle reasons to go outside again; goals point back to Explore/Records rather than becoming a separate game mode.
5. **Settings** — quiet utility for reminders, location/privacy explanations, map/data/app settings, and debug/QA access where appropriate.

## 3. Approved Explore state hierarchy

### Prepare

- real map is the primary surface
- current position
- compact companion HUD
- one recommended nearby discovery/mystery
- quieter secondary signals when useful
- today’s small goal summary
- primary CTA: `탐험 시작`

### Detect

Map the existing encounter `hidden → hinted` transition to a visible unknown signal.

Current gameplay threshold remains approximately **180 m** for hinting.

Presentation:

- unknown signal marker
- approximate distance
- one atmospheric hint
- Moru `curious`/`surprised` reaction
- actions such as `가보기` / `나중에`

### Approach

- active signal remains distinct
- distance updates without repeated modal interruption
- map attribution and outdoor legibility remain intact
- an optional presentation-only `near` state may strengthen anticipation between hint and discovery, but it must not become new domain gameplay logic without a separate decision

### Discover

Map the existing `hinted → discovered` transition to the reveal moment at the current discovery radius, approximately **60 m**.

Use an illustrated discovery sheet rather than a generic Material dialog:

1. place/mystery title
2. raster illustration or place image
3. one-sentence premise
4. Moru reaction
5. `살펴보기`
6. defer/continue action only where the mechanic permits it

### Investigate / Resolve

Reuse the existing clue/mechanic interaction loop. Ordinary encounters should remain short and safe to use outdoors:

- roughly 1–3 actions
- approximately 15–45 seconds
- no precision interaction that encourages unsafe attention while moving
- preserve idempotent clue updates and semantic companion reaction/memory hooks

### Record

A successful encounter produces or updates a journal artifact with:

- discovery title
- place/neighborhood context
- clue or mystery result
- Moru reaction/shared memory
- progress contribution
- `기록 보기` / `계속 탐험`

### Relationship feedback

Meaningful walks may affect bond, memories, future companion-aware encounter weighting, keepsake eligibility, or bond-sensitive rare content. Full relationship review belongs in Companion rather than interrupting Explore.

## 4. Approved Companion hierarchy

Companion is a field notebook about a relationship, not a statistics dashboard.

Order:

1. Moru portrait/current mood
2. one contextual line
3. relationship stage + subtle bond progress
4. recent shared memory
5. `대화하기` / `함께한 기억` / `선물·기념품`
6. long-term history below the fold

Dialogue should use authored scenario rules and semantic memories rather than unconstrained chatbot behavior.

Affinity remains:

`base → familiar → trusted → best_friend`

Relationship rewards may alter keepsakes/behavior/patina, but must not change anatomy or species.

## 5. Approved Records hierarchy

Records is the durable payoff of exploration.

Journal sections:

- 오늘의 기록
- 장소
- 미스터리
- 단서
- Moru와의 기억

A detail view should answer where the event happened, what happened, what clue/mystery it affected, how Moru reacted, and whether there is a reason to revisit.

A 3–4 fragment long-form mystery chain is **not** part of the approved MVP runtime scope. Add it only after the domain model is explicitly expanded.

## 6. Approved first UX-validation scenario

### 오래된 가로수의 메모

A suitable neighborhood POI is selected by the existing POI × mystery-template planner.

**Hint**

> 오래된 나무 근처에서 종이 같은 게 반짝였어.

Moru:

> 저쪽이 조금 신경 쓰이는데?

**Reveal**

The discovery shows an old street tree and folded note.

> 나무 밑에 접힌 메모 한 장이 남아 있다. 글씨는 조금 번졌지만 마지막 문장은 읽을 수 있다.

**Investigation**

The player inspects a small number of details and identifies the intentionally marked clue.

Candidate semantic clue key: `leaf_mark_note`.

**Resolution**

Moru:

> 누군가 다음 사람에게 작은 힌트를 남긴 것 같아. 우리도 기억해두자.

**Record**

Persist the existing supported information: place/encounter context, clue/progress, companion reaction/memory, and available time/revisit context without introducing new raw-location persistence.

On repeat visits, prefer existing anti-repeat/content-rotation behavior and companion memory references rather than replaying identical text when alternatives exist.

## 7. Deferred scope

The following are explicitly deferred and must not be presented as implemented MVP behavior:

- required Camera/AR capture
- selectable-companion onboarding
- 3–4 fragment authored mystery chains leading to a secret location
- production Moru v2 asset replacement before the character-art Human Gate

Photo capture remains a **P1 optional journal enhancement**. It must never block encounter completion.

## 8. Visual constraints

- Explore stays map-first.
- Characters, discoveries, places, collectibles, mystery/reward art use raster PNG/WebP according to `docs/design/`.
- Vector is reserved for true UI symbols.
- Provider map truth, attribution, touch targets, and outdoor readability take precedence over decorative treatment.
- Companion/Records use warm field-notebook / paper language rather than visible generic `ElevatedCard` stacks as the final product language.
- Reduced-motion presentation must preserve all state information.

## 9. Runtime contracts that remain unchanged

- Android native Kotlin + Jetpack Compose
- provider-neutral map/location boundaries
- foreground exploration session
- encounter `hidden → hinted → discovered → resolved`
- existing hint/discovery thresholds (~180 m / ~60 m)
- clue inventory/persistence
- companion semantic memory and bond
- anti-repeat/content rotation and revisit/time/companion weighting
- daily/weekly goals
- no raw high-frequency GPS persistence

## 10. Remaining Human Gates

This UX approval does not close:

- Moru canonical v2 raster illustration approval
- M-B final motion timing/intensity
- outdoor final visual/readability approval
- ID-A icon/logo final lock
- production POI/content/licensing/release decisions

Development may proceed on the approved presentation/state hierarchy while respecting those remaining gates.
