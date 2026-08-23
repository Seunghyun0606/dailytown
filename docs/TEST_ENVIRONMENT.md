# Development and test environments

Daily Town does **not** require a physical-device APK for every development/test cycle.

## Recommended test pyramid

### 1. JVM unit tests — default development loop

Use for exploration math, GPS quality filtering, encounter selection, persistence rules, POI caching, goal rotation, reminder calculations, and privacy-safe diagnostics.

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

The current smoke test verifies that the app starts and that the Seoul City Hall → Deoksugung replay exploration can begin without location permission or a map credential.

### 4. Android Studio emulator — interactive development

For interactive UI/gameplay checks, install and run the debug app on an Android Studio virtual device. Useful scenarios include:

- Compose layout and navigation/interaction
- replay-route gameplay
- encounter phase transitions
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
- repeat-area fatigue and play-feel while actually walking

## NAVER Maps and emulator testing

Credential-free replay testing deliberately renders the safe NAVER placeholder, so most gameplay remains testable independently from account credentials.

The NAVER Console Android package restriction has been human-confirmed as updated to `com.dailytown.app`. A credential can be injected locally through Gradle user properties/environment variables, or in GitHub-hosted internal builds through the repository Actions secret `NAVER_MAP_NCP_KEY_ID`. Supplying a value in chat does not create that repository secret.

A credentialed emulator is useful as an extra map-rendering smoke test, but it does **not** replace final physical-device outdoor validation.

## Suggested routine

Use JVM tests continuously, run normal CI for every branch/PR, let the emulator smoke workflow automatically cover meaningful UI/location-loop changes, and use a real device at field-test milestones rather than for every code iteration.
