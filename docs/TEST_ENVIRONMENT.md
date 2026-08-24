# Development and test environments

Daily Town does **not** require a physical-device APK for every development/test cycle.

## Recommended test pyramid

### 1. JVM unit tests — default development loop

Use for exploration math, GPS quality filtering, encounter selection, persistence rules, POI caching, goal rotation, reminder calculations, privacy-safe diagnostics, battery/distance derivation, gameplay-session counters/rates, multi-session comparison, field-test session-plan latching/evidence inspection, comparison-protocol readiness, and acceptance evaluation.

```bash
gradle testDebugUnitTest
```

No emulator, device, NAVER credential, or GPS is required.

The field-test preparation JVM tests specifically verify that only profile/preset/optional scalar reference distance are latched, invalid reference values are rejected, later setup edits cannot rewrite completed-session preset/reference evidence, distance error is recalculated from the latched scalar reference, and configured missing evidence remains explicit.

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

The managed-device coverage now has two layers:

- basic replay smoke: the Seoul City Hall → Deoksugung replay can start without location permission or a map credential
- field-test flow E2E: empty comparison → preselect session area → replay start with setup controls locked → stop with completed plan → record once → select the next area before the second replay → record second cohort → reach `COMPARABLE` with policy unset → reset to `DATA_INSUFFICIENT`

This verifies the preparation/latching UI through real Compose semantics without generating raw GPS or gameplay telemetry logs. `PRODUCT_REVIEW_READY` remains a JVM-policy test because normal PR builds intentionally do not fabricate human-approved Repository Variables.

### 4. Android Studio emulator — interactive development

For interactive UI/gameplay checks, install and run the debug app on an Android Studio virtual device. Useful scenarios include:

- Compose layout and navigation/interaction
- replay-route gameplay
- encounter phase transitions
- session encounter/discovery/resolution counters
- pre-session NEW_AREA / REPEAT_AREA setup and active-plan locking
- optional scalar reference distance latching from the diagnostic input
- completed-session suggestion and missing-required-evidence guidance
- NEW_AREA / REPEAT_AREA comparison recording and reset
- comparison protocol status/issue rendering
- local persistence across app restarts
- notification/reminder UX
- approximate UI behavior across API levels/screen sizes
- simulated device GPS using Android Emulator location controls when testing the `DEVICE` path

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
- whether start-latched preset/reference values remain meaningful under real route interruptions and preset changes
- whether configured battery/reference/revisit evidence guidance matches what the device can actually measure
- new-area vs repeat-area encounter density, resolution rate, fatigue proxy, and subjective play-feel while actually walking
- whether the approved cohort-size/evidence protocol is sufficient for a real product decision

## Closed-test policy injection

The credentialed `Internal Debug APK` workflow can receive non-secret single-session acceptance criteria and multi-session comparison protocol from GitHub Repository Variables. Unset values remain `NOT_EVALUATED` or comparison-only rather than inventing product thresholds.

```text
FIELD_TEST_MIN_SESSION_SECONDS
FIELD_TEST_MAX_GPS_REJECTION_PERCENT
FIELD_TEST_REQUIRE_MAP_READY
FIELD_TEST_MAX_DISTANCE_ERROR_PERCENT
FIELD_TEST_MAX_BATTERY_DRAIN_PERCENT_PER_HOUR
FIELD_TEST_MIN_ENCOUNTERS_PER_SESSION
FIELD_TEST_MIN_ENCOUNTER_RESOLUTION_PERCENT
FIELD_TEST_MAX_REPEAT_AREA_FATIGUE_PERCENT
FIELD_TEST_COMPARISON_MIN_SESSIONS_PER_COHORT
FIELD_TEST_COMPARISON_REQUIRE_MATCHING_PRESET
FIELD_TEST_COMPARISON_REQUIRED_EVIDENCE
```

Allowed comparison evidence keys, the three protocol statuses, and session-plan latching semantics are documented in `docs/FIELD_TEST_PROTOCOL.md`.

The generated artifact contains `field-test-policy.txt` so a tester can verify which criteria/protocol gates were compiled into that APK. These are policy values, not secrets. `NAVER_MAP_NCP_KEY_ID` remains an Actions Secret and is never written to that metadata file.

## NAVER Maps and emulator testing

Credential-free replay testing deliberately renders the safe NAVER placeholder, so most gameplay remains testable independently from account credentials.

The NAVER Console Android package restriction has been human-confirmed as updated to `com.dailytown.app`. A credential can be injected locally through Gradle user properties/environment variables, or in GitHub-hosted internal builds through the repository Actions secret `NAVER_MAP_NCP_KEY_ID`. Supplying a value in chat does not create that repository secret.

A credentialed emulator is useful as an extra map-rendering smoke test, but it does **not** replace final physical-device outdoor validation.

## Suggested routine

Use JVM tests continuously, run normal CI for every branch/PR, let the emulator smoke workflow automatically cover meaningful UI/location-loop changes, and use a real device at field-test milestones rather than for every code iteration.
