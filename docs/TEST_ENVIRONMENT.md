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

This catches Android/Compose/instrumented-test compilation problems without booting a virtual device.

### 3. Managed Android emulator — UI/integration smoke tests

The app configures a Gradle Managed Device named `pixel2Api30Atd` using a Google ATD image. Google services remain available for the fused-location dependency, while Daily Town's smoke tests use the deterministic replay route and therefore do not require real GPS or a NAVER Maps credential.

```bash
gradle pixel2Api30AtdDebugAndroidTest
```

On CI/server environments without hardware rendering, add:

```bash
gradle pixel2Api30AtdDebugAndroidTest \
  -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
```

GitHub Actions also provides the manually triggered **Emulator Replay Smoke Test** workflow.

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

A real phone is still preferable for motion/battery judgments.

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

Credential-free replay testing deliberately renders the safe NAVER placeholder, so most gameplay remains testable before account setup is complete.

After the NAVER Console package restriction is changed to `com.dailytown.app`, a credential can be injected locally or through the internal APK workflow. A credentialed emulator may be useful as an extra map-rendering smoke test, but it does **not** replace the final physical-device outdoor validation.

## Suggested routine

Use JVM tests continuously, run normal CI for every branch/PR, run the emulator smoke workflow before merging a meaningful UI/location-loop change, and use a real device at field-test milestones rather than for every code iteration.
