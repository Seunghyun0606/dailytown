# Daily Town real-device test

This guide is for checking the current Android MVP on a physical device with the real NAVER Mobile Dynamic Map.

## 1. Make NAVER package restriction match the app

Daily Town Android package / `applicationId` is:

```text
com.dailytown.app
```

In NAVER Cloud Platform, edit the existing Maps application and register `com.dailytown.app` as the Android app package for Mobile Dynamic Map. During migration the old temporary package may remain registered, but remove it after the new package is verified.

If the NAVER package and the installed APK's application ID differ, map authentication can fail even when the NCP key itself is valid.

## 2A. Recommended: build an installable APK with GitHub Actions

1. GitHub repository → Settings → Secrets and variables → Actions.
2. Add repository secret named `NAVER_MAP_NCP_KEY_ID` with the issued NAVER key ID.
3. Actions → `Internal Debug APK` → Run workflow.
4. Confirm unit tests and APK build are green.
5. Download artifact `dailytown-internal-debug-apk`.
6. Extract `app-debug.apk`.

The workflow deliberately fails if the secret is missing. The credential is injected only during the build and is not committed to the repository.

## 2B. Alternative: build locally

Put the key in `~/.gradle/gradle.properties`:

```properties
NAVER_MAP_NCP_KEY_ID=<issued-key-id>
```

or export it for the current shell:

```bash
export NAVER_MAP_NCP_KEY_ID=<issued-key-id>
```

Then run:

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

The APK is produced at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 3. Install on an Android device

Enable Developer options and USB debugging on the device, connect it to the development machine, then run:

```bash
adb devices
adb install -r app-debug.apk
```

You can also transfer the debug APK to the device and install it manually if Android allows installs from that source.

## 4. Indoor smoke test before walking outside

Launch Daily Town and verify:

- NAVER map is rendered instead of the credential placeholder.
- The app shows Daily Town and does not crash on startup.
- `경로 리플레이` runs without location permission.
- Replay updates the user marker/camera and creates a nearby mystery encounter.
- Encounter state advances toward hint/discovery and clue/resolve controls work.
- Closing/reopening the app preserves derived progress.
- Daily/weekly goals remain stable during the same period.
- Optional reminder is off by default.
- Diagnostic sharing does not contain latitude, longitude, or credential values.

If the map does not render, first check both of these before changing code:

1. NAVER Console Android package is exactly `com.dailytown.app`.
2. The APK was built with `NAVER_MAP_NCP_KEY_ID` injected.

## 5. Outdoor walking test

Use `균형` tracking first. Walk a route of roughly 10–20 minutes and check:

- Fine/coarse location permission behavior.
- User marker follows the real position without large jumps.
- Camera movement is usable while walking.
- Hint appears around the configured approach radius and discovery occurs near the POI.
- Impossible GPS jumps are rejected rather than added to walked distance.
- Battery saver / balanced / precise modes behave differently as intended.
- At least one encounter can be completed end to end.
- Walking the same area again produces a different or contextually changed encounter instead of an empty experience.

After the walk, use `진단 리포트 공유` and record the derived metrics together with qualitative notes such as GPS quality, battery drain, encounter pacing, and confusing UI moments.

## 6. What this build is and is not

The debug APK is suitable for internal product verification. It is not a Play Store release build: it uses debug signing, fixture POIs, prototype content, and has not completed privacy/legal/store review.

Before external testers or store release, complete the checklist in `docs/HUMAN_ACTIONS.md`.
