# Daily Town Architecture

## 1. Product constraint

The MVP must validate whether walking around a familiar neighborhood can repeatedly produce meaningful exploration moments without requiring a large hand-authored content library.

## 2. Platform decision

Android native is the MVP default. This reduces uncertainty around location permissions, battery behavior, map SDK integration, and Play testing. Cross-platform is deferred until the gameplay loop is validated.

## 3. Dependency boundaries

```text
Compose UI
   ↓
Application orchestration
   ↓
Pure runtime/domain (ExplorationSession / EncounterCoordinator / TrackingSessionCoordinator / goals / companion)
   ↓
Ports: LocationSource / MapViewAdapter / PoiRepository / ProgressStore
   ↓
Adapters: Android Location / NAVER Map / public-data APIs / DataStore
```

Rules must not depend on a map SDK or Android framework. The Compose layer owns permission launchers and rendering, while `TrackingSessionCoordinator` owns deterministic OFF/DEVICE/REPLAY + preset transitions, `ExplorationSession` owns accepted movement/session-quality state, and `EncounterCoordinator` owns short-lived encounter selection/sequencing/proximity transitions. Derived progress remains behind `ProgressStore`.

This split allows replay/JVM tests, keeps map replacement feasible, and prevents encounter/content/tracking rules from accumulating inside the UI composable.

## 4. Map strategy

NAVER Maps is the MVP provider for the Korean market. Its SDK types are contained inside `NaverMapAdapter`; application/UI code consumes only provider-neutral `MapViewAdapter`, `MapHealth`, `MapMarkerSpec`, and `UserLocationSpec` types.

`MapHealth` exposes only provider-safe state (`UNCONFIGURED`, `INITIALIZING`, `READY`, `AUTH_ERROR`, `ERROR`, `DESTROYED`) and an optional provider error code. Raw SDK exceptions and credential values do not cross the adapter boundary. A future Google adapter can report the same health model without changing field-test diagnostics or application logic.

The credential is from the standalone NAVER Cloud **Maps** product. The Android package restriction is `com.dailytown.app`. Credential values are supplied through Gradle/environment/GitHub Actions Secret and are never committed or printed by verification tasks.

The provider contract remains intentionally small so a future Google adapter can replace NAVER without changing exploration, encounter, location, POI, or diagnostic rules.

## 5. Location strategy

Location collection is app-owned rather than map-provider-owned. `LocationSource` has separate device and replay implementations. Fused Location Services is initialized lazily only when real device tracking starts, so replay and managed-emulator tests do not require Google Play Services or GPS.

`TrackingSessionCoordinator` is framework-free and owns mode/preset transitions. Selecting a new precision preset pauses active DEVICE tracking so a new Android location request can be created; REPLAY can continue across preset changes.

`ExplorationSession` tracks only privacy-safe session metrics: accepted sample count, rejected sample count/rate, and elapsed tracking duration derived from monotonic sample timestamps. These short-lived metrics reset on a new tracking session and are not restored from persistence.

The MVP is foreground-only and deliberately does not request background location. Raw high-frequency samples are not persisted.

## 6. Content exhaustion mitigation

The content system separates a physical place from an encounter template. The same area can produce different experiences through:

- rotating mystery templates
- companion-specific reactions and semantic memory
- revisit state and time context
- neighborhood completion sets
- low-frequency rare encounters
- soft anti-repeat scoring instead of permanently removing visited places
- generated dialogue constrained by authored scenario rules later

`EncounterGenerator` ranks POI × template candidates, while `EncounterCoordinator` owns the runtime transition from selection through hinted/discovered states. This keeps the ranking mechanics testable without Compose or Android dependencies.

## 7. Test boundaries

- Pure domain/runtime behavior: JVM unit tests, including encounter, tracking-state, GPS-quality, and session-duration rules.
- Android UI/replay integration: AOSP ATD managed device, with the tested APK ABI explicitly pinned.
- Normal pull-request CI: unit tests, instrumented-test compilation, lint, debug build, credential hard-code guard.
- Credentialed internal APK: manual Actions workflow with value-blind credential verification and artifact SHA-256 metadata.
- Real GPS accuracy, battery behavior, OEM differences, and final NAVER package/key/map-health validation: physical Android device.

## 8. Privacy baseline

- Raw high-frequency location stays on device unless a future server feature explicitly requires upload.
- Persist derived visit/progress events rather than continuous traces by default.
- Session duration and GPS accept/reject rates are derived metrics and are not raw location traces.
- Never commit or log production credentials.
- Diagnostics contain package/build/map-health/derived counters but no raw coordinates, provider exception payloads, or credential values.
- Add explicit consent and retention policy before analytics/location backend integration.
