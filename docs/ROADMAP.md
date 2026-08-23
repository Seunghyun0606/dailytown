# MVP Roadmap

## Week 0 — foundation (implemented in bootstrap)

Required:
- Android native project
- Compose shell
- pure exploration engine
- map-provider boundary
- content-rotation primitive
- unit tests
- CI definition

## Week 1 — real movement loop

Required:
- runtime location permission UX
- fused location source adapter
- foreground exploration session
- route smoothing and impossible-jump filtering
- local session persistence

Enhancement:
- debug route playback from JSON
- battery/accuracy presets

Human work:
- choose real test neighborhoods and acceptance routes

## Week 2 — map + POI

Required:
- select Naver/Kakao/Google
- implement one `MapProvider` adapter
- render player, discovery radius, encounter markers
- POI repository interface + fixture data

Enhancement:
- public-data adapter
- offline marker cache

Human work:
- create map developer account/app and credentials
- confirm provider terms/pricing for intended game usage

## Week 3 — mystery loop

Required:
- encounter state machine: hidden → hinted → discovered → resolved
- clue inventory
- 8–12 reusable mystery templates
- companion reaction hooks

Enhancement:
- time/context modifiers
- revisit variants

Human work:
- approve tone, prohibited themes, and first scenario pack

## Week 4 — retention prototype

Required:
- neighborhood collection/progress
- daily/weekly rotating goals
- anti-repeat selection rules
- local notifications only after opt-in

Enhancement:
- rare encounters and companion memories

## Week 5 — closed field test

Required:
- crash reporting decision
- privacy disclosure
- signed internal build
- field-test metrics: session duration, distance, encounters/session, repeat-area fatigue

Human work:
- signing key handling
- Play Console setup/testers
- privacy policy/product decisions
