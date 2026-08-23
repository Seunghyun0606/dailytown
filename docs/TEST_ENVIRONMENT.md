# Development and test environments

Daily Town does **not** require a physical-device APK for every development/test cycle.

## Recommended test pyramid

### 1. JVM unit tests — default development loop

Use for exploration math, GPS quality filtering, encounter selection, persistence rules, POI caching, goal rotation, reminder calculations, privacy-safe diagnostics, battery/distance derivation, gameplay-session counters/rates, multi-session cohort comparison, missing-evidence handling, and acceptance evaluation.

```bash
gradle testDebugUnitTest
```

No emulator, device, NAVER credential, or GPS is required.

### 2. Build/compile verification — every CI change

```bash
gradle assembleDebugAndroidTest lintDebug assembleDebug
```

This catches Android/Compose/instrumented-test compilation problems without booting a virtual device. CI also rejects credential-shaped values hard-coded next to `NAVER_MAP_NCP_KEY_ID` in tracked files.

### 3. Managed Android emulator — UI/integration smoke tests

The app configures a Gradle Managed Device named `pixel2Api30Atd` using the lightweight AOSP ATD image. Daily Town defers Google Play Services fused-location initialization until real device tracking starts, so the replay smoke lane is independent from Google Play Services, real GPS, and NAVER credentials.

```bash
gradle pixel2Api30AtdDebugAndroidTest
```

On CI/server environments without hardware rendering, add:

```bash
gradle pixel2Api30AtdDebugAndroidTest \
  -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
```

GitHub Actions provides the **Emulator Replay Smoke Test** workflow. It is still manually triggerable, and it also runs automatically on pull requests that change the UI, location stack, Android instrumentation tests, managed-device Gradle configuration, or the emulator workflow itself.

The current smoke test verifies that the app starts and that the Seoul City Hall → Deoksugung replay exploration can begin without location permission or a map credential. Replay can also exercise session gameplay counters and the longer field-test UI without generating raw telemetry logs.

### 4. Android Studio emulator — interactive development

For interactive UI/gameplay checks, install and run the debug app on an Android Studio virtual device. Useful scenarios include:

- Compose layout and navigation/interaction
- replay-route gameplay
- encounter phase transitions
- session encounter/discovery/resolution counters
- stop a replay session and classify it in the in-memory `신규 지역 / 반복 지역` comparison card
- verify comparison averages, evidence counts, raw deltas, duplicate-session protection, sharing, and reset behavior
- local persistence across app restarts
- notification/reminder UX
- approximate UI behavior across API levels/screen sizes
- simulated device GPS using Android Emulator location controls when testing the `DEVICE` path

The comparison recorder itself is framework-free and JVM-tested. Emulator comparison checks are UI/interaction checks only; replay is not evidence for real GPS, battery, or real-world repeat-area fatigue.

Use an emulator image with Google Play Services when explicitly testing the real fused-location path. A real phone is still preferable for motion/battery judgments.

### 5. Physical Android device — field validation only

A real device is required before a closed/external field test for behavior that a VM cannot validate credibly:

- outdoor GPS drift/accuracy and impossible-jump behavior
- real walking distance and encounter timing
- battery impact of tracking presets
- manufacturer-specific location/background behavior
- real NAVER map credential + Android package restriction validation
- map tile/network behavior under real mobile connectivity
- notification behavior under OEM power-management policies
- new-area vs repeat-area encounter density, resolution rate, fatigue proxy, and subjective play-feel while actually walking
- whether cohort averages have enough **valid evidence counts** to support a product decision

## Multi-session comparison behavior

`FieldTestComparisonRecorder` keeps at most 20 derived session summaries in process memory. It does not use DataStore, a backend, or an analytics SDK. Each completed session is manually classified as `NEW_AREA` or `REPEAT_AREA` after tracking stops.

For each cohort, every metric shows both a rounded average and `evidenceCount/sessionCount`. Missing battery, route-reference, or revisit evidence is omitted from that metric's average instead of being converted to zero. `repeat - new` deltas appear only when both cohort averages exist, and the app intentionally does not turn the delta sign into an automatic product judgment.

The in-app short-lived session token only prevents the same stopped session from being recorded repeatedly. It is not persisted or exported. **비교 초기화** or process restart removes all comparison summaries.

## Closed-test policy injection

The credentialed `Internal Debug APK` workflow can receive non-secret acceptance criteria from GitHub Repository Variables. Unset values remain `NOT_EVALUATED`.

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

The generated artifact contains `field-test-policy.txt` so a tester can verify which criteria were compiled into that APK. These are policy values, not secrets. `NAVER_MAP_NCP_KEY_ID` remains an Actions Secret and is never written to that metadata file.

## NAVER Maps and emulator testing

Credential-free replay testing deliberately renders the safe NAVER placeholder, so most gameplay remains testable independently from account credentials.

The NAVER Console Android package restriction has been human-confirmed as updated to `com.dailytown.app`. A credential can be injected locally through Gradle user properties/environment variables, or in GitHub-hosted internal builds through the repository Actions secret `NAVER_MAP_NCP_KEY_ID`. Supplying a value in chat does not create that repository secret.

A credentialed emulator is useful as an extra map-rendering smoke test, but it does **not** replace final physical-device outdoor validation.

## Suggested routine

Use JVM tests continuously, run normal CI for every branch/PR, let the emulator smoke workflow automatically cover meaningful UI/location-loop changes, and use a real device at field-test milestones rather than for every code iteration. For real field comparison, collect several new-area and repeat-area sessions under reasonably comparable device/preset conditions and inspect evidence counts before interpreting averages or deltas.
