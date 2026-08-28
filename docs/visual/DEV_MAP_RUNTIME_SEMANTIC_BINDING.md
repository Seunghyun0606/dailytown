# Daily Town live-map semantic runtime binding

Status: **application/runtime semantic binding implemented; provider-specific route/halo/effect drawing remains gated by approved render geometry/style and NAVER real-map QA.**

## Runtime flow now wired

The application path now follows the approved architecture without exposing raw assets or NAVER SDK types:

```text
LocalTime (injectable in binder tests)
  -> DayPhaseResolver
  -> VisualThemeProfile
  -> MapThemeSpec
  -> MapViewAdapter
  -> NAVER adapter

EncounterSelection
  -> EncounterMapVisualResolver
  -> MarkerSemantic + selected + MapOverlaySemanticState
  -> MapGameplayVisualBinder
  -> MapViewAdapter
  -> NAVER adapter
```

`DailyTownApp` owns only gameplay/application state. It no longer constructs the active encounter marker as an untyped/default `POI_OTHER` marker.

## Encounter mapping

Current application-layer mapping:

- `HIDDEN` -> no encounter marker;
- `HINTED` -> `ENCOUNTER_HINTED`;
- `DISCOVERED` -> `ENCOUNTER_ACTIVE`, selected, active halo semantic;
- `RESOLVED` -> `ENCOUNTER_SOLVED`;
- revisit before resolution -> dedicated `ENCOUNTER_REVISIT` semantic;
- reduced-motion changes only overlay motion mode, never marker/gameplay meaning.

The application does **not** invent a route. Daily Town does not yet own a route/navigation geometry source, so `MapRouteVisualState` remains `IDLE` in encounter mapping.

The application also does **not** invent discovery-effect intensity. A gameplay-to-effect-intensity mapping must be approved separately before `small/medium/big` becomes runtime behavior.

## Time-of-day binding

`MapRuntimeThemeResolver` converts:

```text
LocalTime + optional VisualDebugOverride + optional measured map luminance
  -> DayPhaseResolver
  -> VisualThemeProfile
  -> provider-neutral MapThemeSpec
```

Rules retained:

- DAY/DARK are the only marker families;
- EV-1 does not create an EVENING asset family;
- E2 may use measured map luminance to select DAY/DARK;
- route color is taken from the approved visual profile;
- screenshot/test callers can inject deterministic time/forced EV-1 state instead of relying on device clock.

`MapGameplayVisualBinder` uses an injectable time provider. Production defaults to `LocalTime.now()`. The existing `DailyTownApp` apply path runs initially and again as location/encounter state changes, so the map theme is refreshed without coupling the domain layer to the clock.

## R-B degradation application contract

`MapOverlayReadabilityPlanner` accepts semantic overlay state plus `RbReadabilityEvidence` and returns a render plan.

Critical semantics are immutable under degradation. If a critical layer is unreadable, the result stays `FAIL`.

Only optional decoration may be removed:

1. atmosphere;
2. active glow;
3. discovery decoration.

For a critical failure, all optional decoration is removed fail-closed, but route/encounter/marker semantic state is preserved and the result remains `FAIL`.

## NAVER adapter boundary

`NaverMapAdapter` now accepts and retains `MapOverlaySemanticState` through the provider-neutral `MapViewAdapter` contract.

It intentionally does **not** render provider-specific route/halo/discovery overlays yet. NAVER `PathOverlay` and other provider primitives require render geometry/style decisions that are not represented by the approved production handoff as exact Android px/runtime geometry. QA fixture pixels must not silently become production values.

When approved render inputs exist, implementation can be added inside the NAVER adapter (or an injected provider adapter component) without changing gameplay/domain state.

## Validation added

Pure/unit coverage now locks:

- encounter phase -> semantic marker mapping;
- revisit non-color semantic;
- HIDDEN marker suppression;
- reduced-motion semantic equivalence;
- no fabricated route/discovery intensity;
- binder propagation into `MapViewAdapter`;
- day/night/EV-1 map theme conversion;
- E2 luminance-based DAY/DARK selection;
- R-B decorative degradation without critical-semantic mutation.

Current connector-written head has not received a GitHub Actions run, and this environment cannot reach GitHub over DNS for a local clone. Therefore these new tests are **implemented but not claimed as executed PASS** yet.

## Remaining gates

1. Run current-head Android unit/instrumentation compile when CI/manual runner is available.
2. Correct NAVER 401 application/package configuration.
3. Run the credentialed 28-capture real-map matrix.
4. Human-review readability and NAVER attribution/legal UI.
5. Promote marker 24 assets only after all marker gates pass.
6. Add provider-specific route/halo/discovery drawing only from approved runtime geometry/style inputs; do not copy QA framing values into production.
7. Final physical outdoor R-B remains human-approved.
