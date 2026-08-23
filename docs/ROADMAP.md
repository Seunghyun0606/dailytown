# MVP Roadmap

## Foundation — implemented

- [x] Android native Kotlin + Jetpack Compose project
- [x] Pure exploration engine and distance calculation
- [x] Provider-neutral map contract
- [x] Provider-neutral map health model (`UNCONFIGURED` / `INITIALIZING` / `READY` / `AUTH_ERROR` / `ERROR` / `DESTROYED`)
- [x] Android CI: unit tests + lint + debug APK
- [x] Instrumented-test APK compilation in normal CI
- [x] Gradle Managed Device + credential-free replay UI smoke-test lane
- [x] Android application identity finalized as `com.dailytown.app`
- [x] Credentialed `Internal Debug APK` GitHub Actions workflow
- [x] Real-device smoke-test documentation
- [x] Emulator vs physical-device test strategy documented
- [x] Replay/emulator path no longer eagerly initializes Google Play Services fused location
- [x] Explicit tested APK ABI for AOSP ATD to avoid host-dependent ABI inference
- [x] Value-blind Gradle verification for credential injection and APK SHA-256 metadata

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
- [x] Extract OFF / DEVICE / REPLAY + preset transitions into framework-free `TrackingSessionCoordinator`
- [x] JVM coverage for tracking-mode and preset transitions
- [x] Per-tracking-session accepted/rejected GPS sample counters and rejection-rate metric
- [x] Per-tracking-session elapsed-duration metric derived from monotonic sample timestamps
- [x] Reset short-lived GPS quality/duration counters on a new tracking session while preserving game progress

Human TODO:
- [ ] Select representative real test neighborhoods and acceptance walking routes
- [ ] Perform outdoor physical-device validation

## Progress runtime + retention — implemented

- [x] Neighborhood progress / collection summary
- [x] Deterministic daily/weekly goal catalog and rotation
- [x] Daily/weekly derived counters with automatic period rollover
- [x] Goal progress evaluation against the correct period rather than lifetime totals
- [x] Persist current/recent goal IDs and avoid immediate repeats when alternatives exist
- [x] Extract persisted progress + daily/weekly goals + readiness into `ProgressRuntimeCoordinator`
- [x] Route exploration/encounter/clue/memory mutations through the progress runtime boundary
- [x] Ignore mutations before restore completes so unloaded progress cannot be overwritten
- [x] Explicit in-memory fallback after load failure
- [x] Disable persistence during fallback sessions so default progress cannot overwrite valid stored data
- [x] JVM coverage for restore, period normalization, mutation, persistence, and fallback safety
- [x] Soft fallback instead of hard content exhaustion when all local POIs have been seen
- [x] Revisit preference for local-memory/time-layer mechanics
- [x] Time-of-day weighting for encounter mechanics
- [x] Companion-memory weighting in encounter selection
- [x] Low-frequency rare encounters with bond-sensitive eligibility
- [x] In-app opt-in local exploration reminder with Android 13+ notification permission handling
- [x] Battery-friendly inexact reminder scheduling and reboot/app-update restoration

## Map + POI — provider boundary implemented

- [x] NAVER Maps selected for MVP
- [x] `NaverMapAdapter` behind `MapViewAdapter`
- [x] Keyless placeholder so development/CI are not blocked by credentials
- [x] Provider-neutral `MapHealth` state flow for readiness/auth/runtime diagnostics
- [x] NAVER 401/429/800 failures translated into safe health/error-code state without exposing credentials or SDK exceptions
- [x] Player and encounter marker rendering
- [x] Provider-neutral `PoiRepository`
- [x] Fixture POIs and radius query
- [x] Provider-neutral padded POI cache with TTL, deduplication, radius filtering, and limited stale fallback
- [x] Cache is wired around the current fixture repository and can wrap a future production repository unchanged
- [x] Google Maps replacement contract documented
- [x] Dynamic Map key injection path via Gradle property or environment variable
- [x] Safe build diagnostic flag showing whether a NAVER credential was injected without exposing the key

Human TODO:
- [x] Obtain NAVER Dynamic Map NCP Key ID
- [x] Decide Android `applicationId`: `com.dailytown.app`
- [x] Update NAVER Console Android package restriction to `com.dailytown.app` (confirmed 2026-08-24)
- [x] Add GitHub Actions repository secret `NAVER_MAP_NCP_KEY_ID` for credentialed internal APK builds (confirmed 2026-08-24)
- [x] Confirm NAVER Maps pricing/cost conditions for intended MVP usage (confirmed 2026-08-24)
- [x] Confirm the issued credential is on the standalone NAVER Cloud **Maps** product; legacy AI NAVER API migration is not required (confirmed 2026-08-24)
- [ ] Choose production POI/public-data sources and validate licensing, attribution, and allowed cache duration/redistribution terms

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
- [x] Extract short-lived encounter sequencing/proximity transitions from Compose into a pure `EncounterCoordinator`
- [x] JVM coverage for encounter selection, hinted/discovered transitions, resolved stability, and runtime reset

Human TODO:
- [ ] Approve narrative tone and prohibited themes
- [ ] Approve the first authored scenario/copy pack

## Field-test diagnostics + acceptance — engineering implemented

- [x] Privacy-safe field-test diagnostic sharing with derived metrics only
- [x] Diagnostic includes package/build/map-health, session duration, and GPS acceptance/rejection metrics but never raw coordinates, provider exception payloads, or credential values
- [x] Configurable `FieldTestAcceptanceEvaluator` with PASS / FAIL / NOT_EVALUATED states
- [x] No hard-coded product thresholds; unset criteria cannot silently pass
- [x] Optional build/runtime criteria injection through `FIELD_TEST_MIN_SESSION_SECONDS`
- [x] Optional build/runtime criteria injection through `FIELD_TEST_MAX_GPS_REJECTION_PERCENT`
- [x] Optional build/runtime criteria injection through `FIELD_TEST_REQUIRE_MAP_READY`
- [x] Invalid configured criteria fail Gradle configuration instead of being silently ignored
- [x] Diagnostic exports whether criteria are configured, overall acceptance state, and failed metric keys

Human TODO:
- [ ] Approve minimum representative session duration
- [ ] Approve maximum GPS rejection-rate threshold
- [ ] Decide whether `MapHealth.READY` is a mandatory closed-test gate (recommended for credentialed physical-device validation, but not hard-coded)
- [ ] Define later criteria for battery impact, route-distance error, encounters/session, completion rate, and repeat-area fatigue after measurement sources exist

Blocked on a human/product/data decision rather than engineering:
- [ ] Implement the concrete production POI upstream adapter after the source/licensing choice is approved; the cache/degradation layer is already ready
- [ ] Replace semantic companion/content copy with the approved authored scenario pack

## Closed field test — human-gated

- [x] Register `com.dailytown.app` in NAVER Console
- [x] Add `NAVER_MAP_NCP_KEY_ID` as a GitHub Actions repository secret
- [x] Confirm the key belongs to standalone NAVER Cloud Maps
- [ ] Run the hardened `Internal Debug APK` workflow and install the credentialed artifact
- [ ] Validate that provider-neutral map health reaches `READY` on a physical device
- [ ] Validate the real NAVER map tiles/markers/location/camera on a physical device
- [ ] Configure approved field-test acceptance criteria through Gradle/Actions properties
- [ ] Reserve/create `com.dailytown.app` in Google Play Console before external release
- [ ] Crash-reporting vendor/consent decision
- [ ] Privacy disclosure and location retention policy
- [ ] Signing/release credentials
- [ ] Play Console/internal testers
- [ ] Approve remaining field-test acceptance thresholds for battery impact, distance accuracy, encounters/session, completion rate, and repeat-area fatigue
