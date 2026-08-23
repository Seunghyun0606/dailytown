# Daily Town Architecture

## 1. Product constraint

The MVP must validate whether walking around a familiar neighborhood can repeatedly produce meaningful exploration moments without requiring a large hand-authored content library.

## 2. Platform decision

Android native is the MVP default. This reduces uncertainty around location permissions, battery behavior, map SDK integration, and Play testing. Cross-platform is deferred until the gameplay loop is validated.

## 3. Dependency boundaries

```text
Compose UI
   ↓
Application runtime coordinators
   ↓
Pure runtime/domain
   ↓
Ports: LocationSource / MapViewAdapter / PoiRepository / ProgressStore
   ↓
Adapters: Android Location / NAVER Map / public-data APIs / DataStore
```

Current runtime coordinators are:

- `TrackingSessionCoordinator`: deterministic OFF/DEVICE/REPLAY and location-preset transitions.
- `ProgressRuntimeCoordinator`: persisted progress, period normalization, goal rotation, exploration synchronization, and persistence eligibility.
- `EncounterCoordinator`: short-lived encounter selection/sequencing/proximity transitions.
- `ExplorationSession`: accepted movement state plus privacy-safe session-quality counters and per-session derived distance.

Rules must not depend on a map SDK or Android framework. Compose owns permission launchers, rendering, and user-facing errors, while runtime coordinators own state transitions. `ProgressStore`, `PoiRepository`, `LocationSource`, and `MapViewAdapter` remain ports around platform/provider-specific implementations.

The progress runtime deliberately separates **ready** from **persistenceEnabled**. If DataStore restore fails, the app may enter an explicit in-memory fallback mode, but persistence stays disabled so default progress cannot overwrite previously stored data after a transient read failure.

This split allows replay/JVM tests, keeps map replacement feasible, and prevents encounter/content/tracking/persistence rules from accumulating inside the UI composable.

## 4. Map strategy

NAVER Maps is the MVP provider for the Korean market. Its SDK types are contained inside `NaverMapAdapter`; application/UI code consumes only provider-neutral `MapViewAdapter`, `MapHealth`, `MapMarkerSpec`, and `UserLocationSpec` types.

`MapHealth` exposes only provider-safe state (`UNCONFIGURED`, `INITIALIZING`, `READY`, `AUTH_ERROR`, `ERROR`, `DESTROYED`) and an optional provider error code. Raw SDK exceptions and credential values do not cross the adapter boundary. A future Google adapter can report the same health model without changing field-test diagnostics or application logic.

The credential is from the standalone NAVER Cloud **Maps** product. The Android package restriction is `com.dailytown.app`. Credential values are supplied through Gradle/environment/GitHub Actions Secret and are never committed or printed by verification tasks.

The provider contract remains intentionally small so a future Google adapter can replace NAVER without changing exploration, encounter, location, POI, or diagnostic rules.

## 5. Location strategy

Location collection is app-owned rather than map-provider-owned. `LocationSource` has separate device and replay implementations. Fused Location Services is initialized lazily only when real device tracking starts, so replay and managed-emulator tests do not require Google Play Services or GPS.

`TrackingSessionCoordinator` is framework-free and owns mode/preset transitions. Selecting a new precision preset pauses active DEVICE tracking so a new Android location request can be created; REPLAY can continue across preset changes.

`ExplorationSession` tracks only privacy-safe session metrics: accepted sample count, rejected sample count/rate, elapsed tracking duration derived from monotonic sample timestamps, and distance walked during the current tracking session. These short-lived metrics reset on a new tracking session and are not restored from persistence.

The MVP is foreground-only and deliberately does not request background location. Raw high-frequency samples are not persisted.

## 6. Progress and goal strategy

`ProgressRuntimeCoordinator` is the single application-level owner of:

- loading persisted `ExplorationProgress`
- daily/weekly period normalization
- deterministic goal rotation
- synchronization from `ExplorationState`
- encounter/clue/memory progress mutations
- deciding whether persistence is safe for the current session

Compose observes one `ProgressRuntimeState` instead of maintaining separate mutable copies of progress, daily goals, weekly goals, and persistence-ready flags.

A successful restore enables persistence. An explicit fallback after a read failure keeps the app usable but disables writes for that session. This avoids the common failure mode where a temporary DataStore read error causes an empty/default model to be saved over valid progress.

## 7. Content exhaustion mitigation

The content system separates a physical place from an encounter template. The same area can produce different experiences through:

- rotating mystery templates
- companion-specific reactions and semantic memory
- revisit state and time context
- neighborhood completion sets
- low-frequency rare encounters
- soft anti-repeat scoring instead of permanently removing visited places
- generated dialogue constrained by authored scenario rules later

`EncounterGenerator` ranks POI × template candidates, while `EncounterCoordinator` owns the runtime transition from selection through hinted/discovered states. This keeps the ranking mechanics testable without Compose or Android dependencies.

## 8. Field-test telemetry and acceptance strategy

`FieldTestSessionMonitor` records only coarse battery start/end snapshots and derived movement metrics. `GameplaySessionMonitor` records only session-local counters for offered/hinted/discovered/resolved encounters, clue collections, and revisit counts. Neither monitor stores GPS coordinates, POI IDs, encounter IDs, template IDs, or event payloads.

Battery evidence uses Android's battery level/scale and, where supported by the device, the remaining charge counter. Any session connected to external power is marked `EXTERNALLY_POWERED` and cannot create battery PASS/FAIL evidence. Missing battery properties likewise stay unevaluated.

For route accuracy, the tester may enter only a pre-verified total route distance in meters. The app compares that scalar reference with `ExplorationSession.sessionDistanceMeters` and calculates percentage error. No reference-route geometry or raw trace is required.

Gameplay quality is derived as:

- encounters/session = encounters that reached `DISCOVERED`, not merely generated candidates
- encounter resolution rate = resolved / discovered encounters
- revisit share = revisit offers / all offers
- repeat-area fatigue proxy = unresolved share of **discovered revisit** encounters

The repeat-area fatigue metric is explicitly a proxy, not a sentiment measurement. If the session contains no discovered revisits, it remains unavailable instead of reporting a misleading 0%.

`FieldTestAcceptanceEvaluator` evaluates recorded evidence against **human-approved** criteria and intentionally ships with no hard-coded product thresholds.

Supported criteria currently include:

- minimum tracking-session duration
- maximum GPS rejection rate
- required provider-neutral map health
- maximum route-distance error percentage
- maximum battery percentage-point drain per hour
- minimum discovered encounters per session
- minimum encounter resolution rate
- maximum repeat-area fatigue proxy

Closed-test builds can supply criteria without source changes through Gradle properties or environment variables:

```text
FIELD_TEST_MIN_SESSION_SECONDS
FIELD_TEST_MAX_GPS_REJECTION_PERCENT
FIELD_TEST_REQUIRE_MAP_READY
FIELD_TEST_MAX_DISTANCE_ERROR_PERCENT
FIELD_TEST_MAX_BATTERY_DRAIN_PERCENT_PER_HOUR
FIELD_TEST_MIN_ENCOUNTERS_PER_SESSION
FIELD_TEST_MIN_ENCOUNTER_RESOLUTION_PERCENT
FIELD_TEST_MAX_REPEAT_AREA_FATIGUE_PERCENT
```

Unset criteria remain `NOT_EVALUATED`; they never silently pass. Invalid configured values fail Gradle configuration rather than being ignored. Field-test diagnostics include only derived counters/rates, configured acceptance state, and failed metric keys while retaining the no-raw-GPS/no-event-ID/no-credential privacy boundary.

## 9. Test boundaries

- Pure domain/runtime behavior: JVM unit tests, including encounter, tracking-state, progress-runtime, GPS-quality, session-duration/distance, battery/distance telemetry, gameplay telemetry, and acceptance-evaluation rules.
- Android UI/replay integration: AOSP ATD managed device, with the tested APK ABI explicitly pinned.
- Normal pull-request CI: unit tests, instrumented-test compilation, lint, debug build, credential hard-code guard.
- Credentialed internal APK: manual Actions workflow with value-blind credential verification, optional acceptance-policy injection, and artifact SHA-256 metadata.
- Real GPS accuracy, battery behavior, OEM differences, gameplay feel, and final NAVER package/key/map-health validation: physical Android device.

## 10. Privacy baseline

- Raw high-frequency location stays on device unless a future server feature explicitly requires upload.
- Persist derived visit/progress events rather than continuous traces by default.
- Session duration/distance, GPS rates, route-distance error, coarse battery consumption, encounter counts/rates, and repeat-area fatigue proxy are derived metrics.
- Session diagnostics never export POI IDs, encounter IDs, template IDs, or event payloads.
- Never commit or log production credentials.
- Diagnostics contain package/build/map-health/derived counters/acceptance results but no raw coordinates, provider exception payloads, or credential values.
- Add explicit consent and retention policy before analytics/location backend integration.
