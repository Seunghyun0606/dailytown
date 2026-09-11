# Daily Town — Exploration Experience Scenario v2 Candidate

> Status: candidate / Human Gate pending
>
> Purpose: reconnect the strongest parts of the initial scenario wireframe to the current Android MVP without undoing the implemented 5-tab IA or the current raster-first art direction.

## 1. Experience thesis

Daily Town should make a familiar neighborhood feel capable of producing a small new story each time the user walks through it.

The core session is not "open a map and collect markers." It is:

`prepare → detect → approach → encounter/discover → investigate/resolve → record → relationship deepens → continue or finish`

This loop should cross the existing five tabs rather than being split into disconnected dashboards.

Current repository constraints that remain unchanged:

- Android native MVP
- real-world walking and NAVER-map-first exploration
- provider-neutral map/location boundaries
- foreground exploration sessions
- POI × mystery-template encounters
- anti-repeat/content rotation
- companion bond and semantic memories
- daily/weekly goals
- persisted discoveries and clues
- no raw high-frequency GPS persistence

## 2. Navigation role

The five tabs remain:

1. Explore — live session and discovery state
2. Companion — relationship, memory, conversation, gifts/keepsakes
3. Records — journal of discovered places, clues, mysteries, and memories
4. Goals — gentle reasons to go out again
5. Settings — quiet utility/configuration

The user should not need to switch tabs to understand what to do next during an active walk. Explore owns the live loop; the other tabs deepen or review what happened.

## 3. Primary daily scenario

### Phase A — Ready to go

**Entry state**

The user opens Explore outside an active session.

**Primary user question**

"Where should I go today, and why?"

**Screen content**

- real map remains the primary surface
- current position
- Moru compact HUD
- one recommended nearby discovery or mystery
- optional secondary nearby signals, visually quieter
- today’s small goal summary
- one primary CTA: `탐험 시작`

**Moru role**

Moru gives one short, contextual line such as:

- "저 골목 쪽에서 처음 보는 느낌이 나."
- "오늘은 천천히 한 바퀴 돌아볼까?"

This line is flavor, not a blocking dialog.

**Do not**

- show KPI/stat dashboards before the walk
- present every nearby POI with equal visual weight
- force companion conversation before starting

### Phase B — Signal detected

**Runtime mapping**

The existing `hidden → hinted` encounter transition is surfaced as a player-visible signal.

The current proximity baseline is approximately 180 m for hinting.

**Primary user question**

"Something is nearby — is it worth walking toward?"

**Map treatment**

- exact content identity remains hidden
- an `unknown signal` marker appears
- compact bottom card shows approximate distance and a short atmospheric hint
- Moru reacts with `curious` or `surprised`
- optional gentle halo/pulse around the signal, respecting reduced motion

**Example**

`신호 감지 · 약 160m`

"오래된 나무 근처에서 작은 흔적이 보여."

Actions:

- `가보기`
- `나중에`

`가보기` makes this the active encounter target; it does not imply full turn-by-turn navigation.

### Phase C — Approach

**Runtime mapping**

The user physically walks toward the hinted encounter.

**Primary user question**

"Am I getting closer, and is something changing?"

**Explore behavior**

- active target stays visually distinct
- distance updates continuously
- map remains legible and attribution unobscured
- Moru HUD may give at most one lightweight reaction when crossing a meaningful threshold
- no repeated modal interruptions while walking

**Recommended micro-states**

- `far`: > 120 m — ordinary hinted state
- `near`: 60–120 m — stronger visual anticipation
- `discoverable`: ≤ 60 m — discovery transition available

Only the current 180 m/60 m product thresholds are implementation-backed. The 120 m UI micro-threshold is presentation-only candidate behavior and must not become gameplay logic without approval.

### Phase D — Discovery / Encounter reveal

**Runtime mapping**

The existing `hinted → discovered` transition becomes the reveal moment at the current discovery radius, approximately 60 m.

**Primary user question**

"What did I find?"

**Presentation**

Use a focused illustrated discovery sheet over the map rather than a generic Material dialog.

Content order:

1. place/mystery title
2. raster illustration or place image
3. one-sentence discovery premise
4. Moru reaction
5. primary action `살펴보기`
6. secondary action `기록만 하고 계속 걷기` when the mechanic permits deferment

**Example candidate**

`오래된 가로수 아래에서`

"나무 밑에 반쯤 접힌 작은 쪽지가 끼워져 있다."

Moru: "누가 일부러 남겨둔 걸까?"

This should feel like a small event, not simply a POI unlock.

### Phase E — Investigate and resolve

**Runtime mapping**

Use the existing clue investigation / resolve / continue loop and reusable mechanic templates.

**Primary user question**

"What do I do with this discovery?"

The MVP interaction should be short enough to complete while standing or walking safely.

Recommended interaction envelope:

- 1–3 short actions
- 15–45 seconds for an ordinary encounter
- no precision interaction that encourages unsafe phone attention while moving
- allow the user to dismiss and return later if the mechanic is not location-critical

Possible mechanic presentation, using already-supported template families rather than inventing a new engine:

- inspect an object/clue
- choose between two interpretations
- compare a simple detail
- reveal a clue card
- resolve with a short narrative beat

On resolution:

- encounter becomes `resolved`
- clue/inventory updates are idempotent
- Moru reaction uses the semantic reaction key
- a semantic companion memory is created when applicable
- reward/progress feedback is concise

### Phase F — Record the moment

**Primary user question**

"What did I just add to my story?"

A successful resolution creates or updates an A3-style journal entry in Records.

The completion sheet should show:

- discovery title
- place / neighborhood context
- clue or mystery result
- Moru memory/reaction
- progress contribution
- `기록 보기` and `계속 탐험` actions

**Photo policy**

The initial wireframe used AR/photo capture as a major beat. Camera/AR capture is not part of the currently documented MVP implementation, so it must remain an optional P1 concept rather than a required step.

If later adopted, photography should enrich the journal, never block encounter completion.

### Phase G — Relationship deepens

**Primary user question**

"Did walking together change anything between us?"

Relationship feedback should come from the event, not from a separate stat grind.

After a meaningful encounter, one of the following may happen:

- bond progress increases
- Moru references the just-created memory
- a keepsake/appearance detail becomes eligible
- a future encounter receives companion-memory weighting
- a rare bond-sensitive encounter becomes eligible

The default post-encounter feedback should be short. Full relationship review belongs in Companion.

## 4. Companion scenario

Companion should behave like a field notebook about a relationship, not a statistics dashboard.

### Companion home hierarchy

1. Moru portrait / current mood
2. one current contextual line
3. relationship stage and subtle bond progress
4. recent shared memory
5. actions: `대화하기`, `함께한 기억`, `선물/기념품`
6. long-term history below the fold

### Conversation rule

Dialogue should reference authored scenario rules and semantic memories rather than acting as an unconstrained chatbot.

Candidate conversation sources:

- place visited today
- recently resolved mystery
- revisit after several days
- time-of-day context
- relationship stage

### Affinity rule

`base → familiar → trusted → best_friend`

Progression should be visible through behavior and keepsakes as well as a number. Anatomy/species must not change as a relationship reward.

## 5. Records scenario

Records is the durable payoff of exploration.

### Journal Home

Sections:

- 오늘의 기록
- 장소
- 미스터리
- 단서
- Moru와의 기억

### Discovery Detail

A discovery detail should answer:

- where was it found?
- what happened there?
- which clue/mystery did it affect?
- how did Moru react?
- is there a reason to revisit?

### Mystery progression

Current implementation supports individual encounter states and clues. The multi-fragment "1/4 → secret location" chain from the initial wireframe is not yet established as current runtime behavior.

Therefore:

- MVP: show progress across related resolved encounters only when the current domain data supports it
- P1 candidate: authored multi-step mystery chain with 3–4 fragments leading to a final location/story reveal

Do not fake a chain in UI before the domain model supports it.

## 6. Goals scenario

Goals are not chores or KPI dashboards. They are gentle prompts that create reasons to walk.

Recommended catalog roles:

- movement: walk for N minutes / distance
- discovery: visit a new place
- mystery: check or resolve one signal
- relationship: complete a walk with Moru / create a shared memory
- revisit: revisit a known neighborhood under a different context

Goal completion should point back to Explore or Records, not become its own game mode.

## 7. Settings scenario

Settings remains deliberately quiet.

It owns:

- notifications/reminders
- location/privacy explanations
- map/settings utilities
- data management
- app information
- debug/QA access only in appropriate builds

No game progression should depend on visiting Settings.

## 8. First-session onboarding

The initial wireframe included choosing one of several companions. The current MVP is Moru-centric and does not document a selectable-companion onboarding system.

Candidate MVP onboarding:

1. brief promise: "익숙한 동네에서 작은 발견을 찾아요"
2. location permission explanation
3. meet Moru as the default companion
4. start a short guided nearby encounter
5. detect first signal
6. walk to discovery radius
7. resolve a simple clue
8. save first journal entry
9. Moru creates the first shared memory
10. return to Explore with the loop understood

Selectable companions should remain future scope until the companion roster/domain supports it.

## 9. Concrete authored example — "오래된 가로수의 메모"

This is a candidate for UX validation, not an approved scenario pack.

### Trigger

A suitable neighborhood POI is selected by the existing POI × template planner.

### Hint

At hinted distance:

"오래된 나무 근처에서 종이 같은 게 반짝였어."

Moru: "저쪽이 조금 신경 쓰이는데?"

### Reveal

At discovery distance:

The place card reveals a softly illustrated old street tree and a folded note.

Copy:

"나무 밑에 접힌 메모 한 장이 남아 있다. 글씨는 조금 번졌지만 마지막 문장은 읽을 수 있다."

### Investigation

The user inspects two visible details and selects the one that seems intentionally marked.

Result:

A simple clue is added: `leaf_mark_note`.

### Resolution

Moru reacts:

"누군가 다음 사람에게 작은 힌트를 남긴 것 같아. 우리도 기억해두자."

### Record

Records receives:

- place
- date/time band
- clue
- Moru reaction
- relationship memory key

### Revisit variation

If the area is revisited later, the planner should prefer a different mechanic/template where possible; when a revisit-context mechanic is selected, Moru may reference the prior memory rather than replaying the same text.

This directly serves the MVP hypothesis that familiar areas can remain meaningful without a huge authored library.

## 10. Session-end scenario

A walk ends either explicitly or after the user naturally stops the foreground exploration session.

Show a compact summary:

- distance walked
- new/revisited places
- encounters resolved
- clues/memories gained
- goal progress
- one Moru closing line

Primary CTA:

`오늘의 기록 보기`

Secondary:

`끝내기`

The summary should celebrate moments, not overwhelm with metrics.

## 11. Visual/state requirements

### Explore

- map-first
- game art is raster PNG/WebP, not SVG
- true UI symbols may be vector
- no provider-map recoloring that harms map truth/legibility
- maximum one primary encounter card plus one compact secondary prompt at a time
- outdoor legibility takes precedence over decorative texture

### Companion / Records

- field-notebook / warm paper treatment
- character and discovery artwork remain raster illustration
- avoid generic ElevatedCard stacks as the visible final language

### Motion

Motion should communicate state transitions, not decorate every element.

Priority moments:

- signal detected
- discovery reveal
- Moru clue reaction
- resolution settle

Reduced-motion mode must preserve the same information statically.

## 12. Runtime mapping

The candidate flow intentionally maps to existing product behavior:

| UX state | Current product/runtime concept |
| --- | --- |
| signal unavailable | encounter `hidden` |
| signal detected | encounter `hinted` |
| nearby reveal | encounter `discovered` |
| investigation complete | encounter `resolved` |
| clue reward | persisted clue inventory |
| remembered place/event | companion semantic memory |
| relationship feedback | companion bond / affinity presentation |
| repeat-area variety | content rotation + revisit/time/companion weighting |
| daily reason to walk | daily/weekly goals |

Presentation states such as `near` must remain UI-only until explicitly adopted into domain logic.

## 13. Validation criteria

This candidate passes product/design review when a reviewer can answer yes to all of the following:

- Can a new user explain the loop after one guided encounter?
- During a walk, is the next action understandable without opening another tab?
- Does signal detection feel like an event rather than a map marker appearing?
- Does discovery create a meaningful journal artifact?
- Does Moru feel like a companion affected by the walk rather than a decorative mascot?
- Can the same neighborhood plausibly feel different on repeat visits?
- Are the screens readable outdoors without covering too much of the map?
- Are game-art assets raster and visually coherent with Daily Town art direction?
- Does the flow avoid claiming camera/AR, companion selection, or multi-fragment mystery chains as implemented MVP features?

## 14. Human Gate

Before production promotion or runtime visual replacement, approve:

1. the six-step core loop and Explore state hierarchy
2. Companion relationship hierarchy
3. Records/mystery presentation hierarchy
4. whether the concrete "오래된 가로수의 메모" example is suitable as the first authored UX-validation scenario
5. whether photo capture remains deferred or enters P1

Until approval, this document is a candidate specification and must not silently change domain behavior or production assets.
