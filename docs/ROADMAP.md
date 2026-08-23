# MVP Roadmap

## Foundation — implemented

- [x] Android native Kotlin + Jetpack Compose project
- [x] Pure exploration engine and distance calculation
- [x] Provider-neutral map contract
- [x] Android CI: unit tests + debug APK

## Real movement loop — implemented

- [x] Runtime fine/coarse location permission UX
- [x] App-owned fused location source
- [x] Foreground exploration session
- [x] Accuracy/impossible-jump filtering
- [x] Replay route for credential-free testing
- [x] Local persistence of derived exploration progress with DataStore
- [x] Do not persist raw high-frequency GPS traces

Human TODO:
- [ ] Select representative real test neighborhoods and acceptance walking routes
- [ ] Perform outdoor physical-device validation

## Map + POI — provider boundary implemented

- [x] NAVER Maps selected for MVP
- [x] `NaverMapAdapter` behind `MapViewAdapter`
- [x] Keyless placeholder so development/CI are not blocked by credentials
- [x] Player and encounter marker rendering
- [x] Provider-neutral `PoiRepository`
- [x] Fixture POIs and radius query
- [x] Google Maps replacement contract documented

Human TODO:
- [ ] Create/restrict NAVER Maps credential and inject `NAVER_MAP_NCP_KEY_ID`
- [ ] Confirm NAVER Maps terms/pricing for the intended location-game usage
- [ ] Choose production POI/public-data sources and validate their licensing/caching terms

## Mystery loop — mechanics implemented

- [x] Encounter state machine: hidden → hinted → discovered → resolved
- [x] Idempotent clue inventory primitives
- [x] Eight reusable mechanic templates
- [x] Companion reaction hooks based on semantic reaction keys
- [x] Soft anti-repeat planner for POI × template combinations

Human TODO:
- [ ] Approve narrative tone and prohibited themes
- [ ] Approve the first authored scenario/copy pack

## Retention prototype — core implemented

- [x] Neighborhood progress model
- [x] Deterministic daily/weekly goal catalog and rotation
- [x] Recent-goal avoidance
- [x] Soft fallback instead of hard content exhaustion when all local POIs have been seen

Next engineering work that does not require credentials:
- [ ] Connect POI repository output to encounter generation in the Compose screen
- [ ] Persist mystery inventory/resolution and recent encounter history
- [ ] Add explicit neighborhood collection UI
- [ ] Add goal progress calculation from exploration events
- [ ] Add local notification implementation only after in-app opt-in UX exists

## Closed field test — human-gated

- [ ] Crash-reporting vendor/consent decision
- [ ] Privacy disclosure and location retention policy
- [ ] Signing/release credentials
- [ ] Play Console/internal testers
- [ ] Field-test acceptance thresholds for session duration, distance, encounters/session, and repeat-area fatigue
