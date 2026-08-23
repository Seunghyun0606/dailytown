# Human TODOs

Daily Town continues development without blocking on credentials. The items below are deliberately left for a person because they involve account ownership, legal/content approval, real-device judgment, store ownership, or release secrets.

## Android application identity

- [x] Product working name: **Daily Town**.
- [x] Android `applicationId` / package: **`com.dailytown.app`**.
- [ ] Before external release, create/reserve the Android app in Google Play Console using `com.dailytown.app` and confirm the package is accepted. Treat the package as immutable after store publication.

## NAVER Maps account / credential

Status: a standalone NAVER Cloud **Maps** Dynamic Map NCP Key ID has been issued. The key value stays outside source control and is consumed through `NAVER_MAP_NCP_KEY_ID`.

- [x] Create/select a NAVER Cloud Platform standalone **Maps** application and enable Android Dynamic Map. (Human-confirmed 2026-08-24.)
- [x] Obtain a Dynamic Map NCP Key ID.
- [x] Register Android app package **`com.dailytown.app`** in NAVER Console and replace the temporary/random package restriction. (Human-confirmed 2026-08-24.)
- [x] Add repository Actions secret named `NAVER_MAP_NCP_KEY_ID` for credentialed GitHub-hosted internal APK builds. (Human-confirmed 2026-08-24.)
- [x] Confirm NAVER Maps pricing/cost conditions for the intended MVP usage. (Human-confirmed 2026-08-24.)
- [x] Confirm the credential is from the standalone **Maps** product, not legacy AI NAVER API Maps. No legacy Maps migration is required. (Human-confirmed 2026-08-24.)
- [ ] Before a credentialed physical-device test, verify the package restriction still exactly matches the installed APK's actual `applicationId` (`com.dailytown.app`).
- [ ] Run the `Internal Debug APK` workflow and install the generated APK on a real Android device. The workflow verifies secret injection without logging the key and includes a SHA-256 checksum with the artifact.

Optional local credentialed builds can use Gradle user properties or an environment variable; never commit the value to source control:

```properties
NAVER_MAP_NCP_KEY_ID=<issued-key-id>
```

Recommended local file: `~/.gradle/gradle.properties`.

Alternative shell injection:

```bash
export NAVER_MAP_NCP_KEY_ID=<issued-key-id>
```

## POI / public data

- [ ] Choose the production POI/public-data datasets after checking coverage in target neighborhoods.
- [ ] Confirm redistribution, caching, attribution, and commercial-use terms for those datasets.

The code uses a `PoiRepository` boundary and fixture data until these decisions are made.

## Content approval

- [ ] Approve narrative tone, age rating target, and prohibited themes.
- [ ] Approve the first authored scenario/copy pack.

Engineering does not need to wait: current templates define mechanics only and companion reactions return semantic keys rather than final authored dialogue.

## Closed field-test acceptance criteria

Engineering support is implemented, but the product thresholds must be chosen by a person. Until a value is configured, the diagnostic reports `NOT_EVALUATED` instead of assuming the route passed.

- [ ] Approve the minimum representative tracking-session duration.
- [ ] Approve the maximum acceptable GPS rejection rate.
- [ ] Decide whether provider-neutral map health **`READY`** is a mandatory closed-test gate.
- [ ] Approve the maximum acceptable route-distance error percentage.
- [ ] Approve the maximum acceptable battery percentage-point drain per hour.
- [ ] Approve the minimum number of encounters that must reach **`DISCOVERED`** during a representative session. Generated-but-never-reached candidates are intentionally not counted.
- [ ] Approve the minimum encounter resolution-rate percentage (`resolved / discovered`).
- [ ] Approve the maximum repeat-area-fatigue proxy percentage. The proxy is the unresolved share of **discovered revisit encounters** and should be used for comparative route testing, not treated as direct user sentiment.

Once approved, configure the supported criteria as Gradle properties or environment variables. They are normal configuration values, **not secrets**:

```properties
FIELD_TEST_MIN_SESSION_SECONDS=<non-negative integer>
FIELD_TEST_MAX_GPS_REJECTION_PERCENT=<0..100>
FIELD_TEST_REQUIRE_MAP_READY=true
FIELD_TEST_MAX_DISTANCE_ERROR_PERCENT=<0..100>
FIELD_TEST_MAX_BATTERY_DRAIN_PERCENT_PER_HOUR=<non-negative integer>
FIELD_TEST_MIN_ENCOUNTERS_PER_SESSION=<non-negative integer>
FIELD_TEST_MIN_ENCOUNTER_RESOLUTION_PERCENT=<0..100>
FIELD_TEST_MAX_REPEAT_AREA_FATIGUE_PERCENT=<0..100>
```

For GitHub-hosted `Internal Debug APK` builds, use repository Variables with the same names. The generated `field-test-policy.txt` records these non-secret policy values next to the APK. Do not put the NAVER credential into these variables; `NAVER_MAP_NCP_KEY_ID` remains a Secret.

Invalid configured values intentionally fail Gradle configuration rather than being silently ignored.

## Real-device validation

- [ ] Select representative test neighborhoods and walking routes, including at least one mostly-new area and one repeat-area route.
- [ ] For each route, record a trusted **total route distance in meters** from the chosen reference source. Do not copy route geometry into the app; only enter the scalar meter value in `기준 경로 거리` before sharing the diagnostic.
- [ ] Run battery-comparison routes with the phone **unplugged**. A session that starts or ends while externally powered intentionally reports battery measurement as non-comparable instead of producing a PASS/FAIL.
- [ ] Keep screen brightness, tracking preset, device, and route conditions reasonably consistent across battery-comparison runs.
- [ ] Run on a physical Android device outdoors after NAVER package restriction matches `com.dailytown.app`.
- [ ] Confirm the in-app provider-neutral map health reaches **`READY` / 지도 정상** after the NAVER map initializes.
- [ ] If map health becomes **`AUTH_ERROR`**, share the safe diagnostic and record only the provider error code (for example 401/429/800); do not copy or expose the credential value.
- [ ] Verify NAVER map tiles, user-location overlay, encounter markers, and camera movement.
- [ ] Verify precise/approximate location permission behavior.
- [ ] Verify battery saver / balanced / precise tracking presets.
- [ ] Verify changing the preset while DEVICE tracking is active pauses the session before a new location request is started and freezes that battery measurement window.
- [ ] Verify GPS accepted/rejected counters, rejection rate, impossible-jump filtering, and per-session distance.
- [ ] Verify the diagnostic shows `sessionDistanceMeters`, optional `referenceDistanceMeters`, and `distanceErrorPercent` without latitude/longitude fields.
- [ ] Verify supported devices report coarse battery start/end percentage and, where available, charge consumed in mAh. OEMs that do not expose a charge counter are allowed to omit the mAh field.
- [ ] Verify `batteryDrainPercentPerHour` is absent/unevaluated for external-power sessions or zero-duration evidence rather than being fabricated.
- [ ] During a representative route, verify the in-app session summary increments offered/discovered/resolved encounters and collected clues only when those transitions actually happen.
- [ ] Verify the diagnostic exports gameplay **counts/rates only** and contains no `poiId`, `encounterId`, `templateId`, or equivalent event identifiers.
- [ ] Compare new-area vs repeat-area diagnostics. If there are no discovered revisit encounters, confirm `repeatAreaFatigueProxyPercent` is omitted/unevaluated rather than shown as 0%.
- [ ] Walk the same route multiple times and judge whether the measured revisit resolution/fatigue proxy matches the subjective feeling of repetition closely enough to use as a product signal.
- [ ] Verify daily/weekly counters reset only when the corresponding period changes.
- [ ] Force/observe a progress-load failure in a debug test if practical and confirm the app shows the temporary progress mode while persistence remains disabled; this protects existing stored progress from being overwritten by a fallback state.
- [ ] Enable/disable the optional local reminder and verify Android 13+ notification permission flow.
- [ ] Share a field-test diagnostic and verify it contains package/build/map-health/session/distance/battery/gameplay/acceptance counters but no coordinates, provider exception payloads, event IDs, or credential values.
- [ ] After acceptance criteria are configured, verify the diagnostic reports `PASS`, `FAIL`, or `NOT_EVALUATED` as expected and lists only failed metric keys, not sensitive raw data.

## External beta / release

Before any external beta or store submission:

- [ ] Write/publish the location and privacy policy.
- [ ] Decide whether background location is truly required. Current MVP deliberately does **not** request it.
- [ ] Create signing/release credentials outside the repository.
- [ ] Decide analytics/crash-reporting vendor and consent policy before adding telemetry SDKs.
- [ ] Configure Play Console internal testers.

## Safe to continue without these TODOs

Replay exploration, GPS filtering, per-session distance, derived progress persistence, persistence-safe fallback, provider-neutral map health, POI abstraction, encounter generation rules, mystery state machine, clue mechanics, companion reaction hooks, anti-repeat ranking, neighborhood progress, contextual/rare encounters, companion semantic memory, daily/weekly goal rotation, coarse battery field telemetry, route-distance-error calculation, privacy-safe gameplay telemetry, configurable field-test acceptance evaluation, opt-in local reminders, privacy-safe diagnostic export, managed-emulator smoke testing, and credentialed internal APK generation can all continue without production POI data or release credentials.
