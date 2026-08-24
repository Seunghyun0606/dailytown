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
- [x] Per-tracking-session accepted distance independent from lifetime progress
- [x] Reset short-lived GPS quality/duration/distance counters on a new tracking session while preserving game progress

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
- [x] Diagnostic includes package/build/map-health, session duration/distance, GPS quality, route-distance error, and coarse battery metrics but never raw coordinates, provider exception payloads, or credential values
- [x] Android battery snapshot adapter using coarse battery level and optional charge counter
- [x] External-power sessions automatically excluded from battery-consumption acceptance
- [x] Optional scalar reference-route distance input; no route geometry/raw trace required
- [x] Session-local gameplay telemetry with offered/hinted/discovered/resolved encounter counts, clue counts, and revisit counts
- [x] Gameplay diagnostic rates for discovery, resolution, revisit share, revisit resolution, and repeat-area fatigue proxy
- [x] Do not export POI IDs, encounter IDs, template IDs, or raw gameplay event payloads
- [x] In-memory `NEW_AREA` / `REPEAT_AREA` field-session comparison with a bounded 20-summary window
- [x] Comparison averages expose per-metric valid evidence counts instead of treating missing evidence as zero
- [x] Comparison report emits `repeat - new` deltas only when both cohorts have evidence and never stores free-form place labels
- [x] Prevent duplicate recording of the same completed session through a process-local session token that is not exported or persisted
- [x] Pure `FieldTestProtocolEvaluator` with `DATA_INSUFFICIENT` / `COMPARABLE` / `PRODUCT_REVIEW_READY` states
- [x] Configurable minimum sessions per cohort, matching tracking-preset gate, and required comparison evidence set
- [x] Scope `REPEAT_AREA_FATIGUE` protocol evidence to the repeat cohort only
- [x] Show protocol status and unmet sample/preset/evidence issues in the in-app comparison card
- [x] Include protocol status/issues in privacy-safe comparison sharing
- [x] Validate comparison policy values during Gradle configuration and record them in Internal Debug artifact metadata
- [x] Configurable `FieldTestAcceptanceEvaluator` with PASS / FAIL / NOT_EVALUATED states
- [x] No hard-coded product thresholds; unset criteria cannot silently pass
- [x] Optional criteria injection through `FIELD_TEST_MIN_SESSION_SECONDS`
- [x] Optional criteria injection through `FIELD_TEST_MAX_GPS_REJECTION_PERCENT`
- [x] Optional criteria injection through `FIELD_TEST_REQUIRE_MAP_READY`
- [x] Optional criteria injection through `FIELD_TEST_MAX_DISTANCE_ERROR_PERCENT`
- [x] Optional criteria injection through `FIELD_TEST_MAX_BATTERY_DRAIN_PERCENT_PER_HOUR`
- [x] Optional criteria injection through `FIELD_TEST_MIN_ENCOUNTERS_PER_SESSION`
- [x] Optional criteria injection through `FIELD_TEST_MIN_ENCOUNTER_RESOLUTION_PERCENT`
- [x] Optional criteria injection through `FIELD_TEST_MAX_REPEAT_AREA_FATIGUE_PERCENT`
- [x] Invalid configured criteria fail Gradle configuration instead of being silently ignored
- [x] Diagnostic exports whether criteria are configured, overall acceptance state, and failed metric keys
- [x] Internal Debug artifact records the non-secret configured field-test policy

Human TODO:
- [ ] Approve minimum representative session duration
- [ ] Approve maximum GPS rejection-rate threshold
- [ ] Decide whether `MapHealth.READY` is a mandatory closed-test gate
- [ ] Approve maximum route-distance error percentage
- [ ] Approve maximum battery percentage-point drain per hour
- [ ] Approve minimum **discovered** encounters per representative session
- [ ] Approve minimum encounter resolution-rate percentage
- [ ] Approve maximum repeat-area-fatigue proxy percentage; interpret this as a comparative proxy, not direct user sentiment
- [ ] Decide the minimum valid NEW_AREA/REPEAT_AREA cohort size for product review
- [ ] Decide whether comparison sessions must use a matching tracking preset
- [ ] Decide which comparison evidence keys are mandatory

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
- [ ] Enter the pre-verified total route distance before sharing each representative route diagnostic
- [ ] Run battery-comparison tests unplugged; external-power sessions intentionally cannot produce battery-consumption acceptance evidence
- [ ] Walk representative new-area and repeat-area routes, stop each session, classify it in the in-app comparison card, and record it once
- [ ] Compare valid-evidence counts as well as averages; do not treat a cohort average based on one battery/revisit sample as equivalent to a fully measured cohort
- [ ] Configure approved comparison protocol variables and verify the state transitions from `DATA_INSUFFICIENT` → `COMPARABLE` → `PRODUCT_REVIEW_READY` as evidence accumulates
- [ ] Share the comparison report and evaluate discovered encounters/session, resolution rate, GPS/distance/battery behavior, revisit share, and repeat-area fatigue proxy together
- [ ] Configure approved field-test acceptance criteria through Gradle/Actions properties
- [ ] Reserve/create `com.dailytown.app` in Google Play Console before external release
- [ ] Crash-reporting vendor/consent decision
- [ ] Privacy disclosure and location retention policy
- [ ] Signing/release credentials
- [ ] Play Console/internal testers
