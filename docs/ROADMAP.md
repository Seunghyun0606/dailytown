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
- [x] Battery saver / balanced / precise location request presets
- [x] Match GPS acceptance tolerance to the active tracking preset

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
- [x] Dynamic Map key injection path via Gradle property or environment variable

Human TODO:
- [x] Obtain NAVER Dynamic Map NCP Key ID
- [ ] Decide final Android `applicationId` and update the temporary/random NAVER Console package restriction to match it
- [ ] Confirm NAVER Maps terms/pricing for intended location-game usage
- [ ] Choose production POI/public-data sources and validate licensing/caching terms

## Mystery loop — mechanics and playable loop implemented

- [x] Encounter state machine: hidden → hinted → discovered → resolved
- [x] Idempotent clue inventory primitives
- [x] Eight reusable mechanic templates
- [x] Companion reaction hooks based on semantic reaction keys
- [x] Soft anti-repeat planner for POI × template combinations
- [x] Connect nearby POI output to the live/replay location loop
- [x] 180m hint and 60m discovery proximity transitions
- [x] Clue investigation / resolve / continue interaction loop
- [x] Persist clue inventory, encounter visits, resolutions, and recent combination history
- [x] Context-aware selection by time band and revisit state
- [x] Deterministic uncommon/rare encounter eligibility
- [x] Semantic companion memories for visited POIs and resolved mechanics

Human TODO:
- [ ] Approve narrative tone and prohibited themes
- [ ] Approve the first authored scenario/copy pack

## Retention prototype — core implemented

- [x] Neighborhood progress / collection summary
- [x] Deterministic daily/weekly goal catalog and rotation
- [x] Daily/weekly derived counters with automatic period rollover
- [x] Goal progress evaluation against the correct period rather than lifetime totals
- [x] Soft fallback instead of hard content exhaustion when all local POIs have been seen
- [x] Revisit preference for local-memory/time-layer mechanics
- [x] Time-of-day weighting for encounter mechanics
- [x] Companion-memory weighting in encounter selection
- [x] Low-frequency rare encounters with bond-sensitive eligibility

Next engineering work that does not require production credentials:
- [ ] Persist recent goal IDs so goal rotation avoids immediately repeating across periods
- [ ] Add in-app notification opt-in UI and local reminder scheduler
- [ ] Add a fixture/public-data cache adapter once a production POI source is selected
- [ ] Add field-test diagnostic export for derived metrics only (no raw GPS trace)

## Closed field test — human-gated

- [ ] Crash-reporting vendor/consent decision
- [ ] Privacy disclosure and location retention policy
- [ ] Signing/release credentials
- [ ] Play Console/internal testers
- [ ] Field-test acceptance thresholds for session duration, distance, encounters/session, and repeat-area fatigue
