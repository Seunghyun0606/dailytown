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
Pure runtime/domain (ExplorationSession / EncounterCoordinator / goals / companion)
   ↓
Ports: LocationSource / MapViewAdapter / PoiRepository / ProgressStore
   ↓
Adapters: Android Location / NAVER Map / public-data APIs / DataStore
```

Rules must not depend on a map SDK or Android framework. The Compose layer owns permission launchers and rendering, while `ExplorationSession` owns accepted movement state and `EncounterCoordinator` owns short-lived encounter selection/sequencing/proximity transitions. Derived progress remains behind `ProgressStore`.

This split allows replay/JVM tests, keeps map replacement feasible, and prevents encounter/content rules from accumulating inside the UI composable.

## 4. Map strategy

NAVER Maps is the MVP provider for the Korean market. Its SDK types are contained inside `NaverMapAdapter`; application/UI code consumes only `MapViewAdapter`, `MapMarkerSpec`, and `UserLocationSpec`.

The credential is from the standalone NAVER Cloud **Maps** product. The Android package restriction is `com.dailytown.app`. Credential values are supplied through Gradle/environment/GitHub Actions Secret and are never committed or printed by verification tasks.

The provider contract remains intentionally small so a future Google adapter can replace NAVER without changing exploration, encounter, location, or POI rules.

## 5. Location strategy

Location collection is app-owned rather than map-provider-owned. `LocationSource` has separate device and replay implementations. Fused Location Services is initialized lazily only when real device tracking starts, so replay and managed-emulator tests do not require Google Play Services or GPS.

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

- Pure domain/runtime behavior: JVM unit tests.
- Android UI/replay integration: AOSP ATD managed device, pinned to x86.
- Normal pull-request CI: unit tests, instrumented-test compilation, lint, debug build, credential hard-code guard.
- Credentialed internal APK: manual Actions workflow with value-blind credential verification and artifact SHA-256 metadata.
- Real GPS accuracy, battery behavior, OEM differences, and final NAVER package/key validation: physical Android device.

## 8. Privacy baseline

- Raw high-frequency location stays on device unless a future server feature explicitly requires upload.
- Persist derived visit/progress events rather than continuous traces by default.
- Never commit or log production credentials.
- Diagnostics contain package/build/derived counters but no raw coordinates or credential values.
- Add explicit consent and retention policy before analytics/location backend integration.
