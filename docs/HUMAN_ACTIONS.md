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

## Real-device validation

- [ ] Select representative test neighborhoods and walking routes.
- [ ] Run on a physical Android device outdoors after NAVER package restriction matches `com.dailytown.app`.
- [ ] Confirm the in-app provider-neutral map health reaches **`READY` / 지도 정상** after the NAVER map initializes.
- [ ] If map health becomes **`AUTH_ERROR`**, share the safe diagnostic and record only the provider error code (for example 401/429/800); do not copy or expose the credential value.
- [ ] Verify NAVER map tiles, user-location overlay, encounter markers, and camera movement.
- [ ] Verify precise/approximate location permission behavior.
- [ ] Verify battery saver / balanced / precise tracking presets.
- [ ] Verify changing the preset while DEVICE tracking is active pauses the session before a new location request is started.
- [ ] Verify GPS accepted/rejected counters, rejection rate, and impossible-jump filtering.
- [ ] Record derived session duration and GPS rejection rate for each representative walking route; raw GPS traces are not required for the MVP acceptance report.
- [ ] Walk the same route twice and verify repeat handling feels intentional rather than empty.
- [ ] Verify daily/weekly counters reset only when the corresponding period changes.
- [ ] Enable/disable the optional local reminder and verify Android 13+ notification permission flow.
- [ ] Share a field-test diagnostic and verify it contains package/build/map-health/session/game counters but no coordinates, provider exception payloads, or credential values.

## External beta / release

Before any external beta or store submission:

- [ ] Write/publish the location and privacy policy.
- [ ] Decide whether background location is truly required. Current MVP deliberately does **not** request it.
- [ ] Create signing/release credentials outside the repository.
- [ ] Decide analytics/crash-reporting vendor and consent policy before adding telemetry SDKs.
- [ ] Configure Play Console internal testers.
- [ ] Define field-test acceptance thresholds for session duration, battery impact, distance accuracy, GPS rejection rate, encounters/session, completion rate, and repeat-area fatigue.

## Safe to continue without these TODOs

Replay exploration, GPS filtering, derived progress persistence, provider-neutral map health, POI abstraction, encounter generation rules, mystery state machine, clue mechanics, companion reaction hooks, anti-repeat ranking, neighborhood progress, contextual/rare encounters, companion semantic memory, daily/weekly goal rotation, opt-in local reminders, privacy-safe diagnostic export, managed-emulator smoke testing, and credentialed internal APK generation can all continue without production POI data or release credentials.
