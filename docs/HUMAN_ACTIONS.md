# Human TODOs

Daily Town continues development without blocking on credentials. The items below are deliberately left for a person because they involve account ownership, legal/content approval, real-device judgment, store ownership, or release secrets.

## Android application identity

- [x] Product working name: **Daily Town**.
- [x] Android `applicationId` / package: **`com.dailytown.app`**.
- [ ] Before external release, create/reserve the Android app in Google Play Console using `com.dailytown.app` and confirm the package is accepted. Treat the package as immutable after store publication.

## NAVER Maps account / credential

Status: a NAVER Dynamic Map NCP Key ID has been issued. The key value must stay outside source control and is consumed through `NAVER_MAP_NCP_KEY_ID`.

- [x] Create/select a NAVER Cloud Platform Maps application and enable Android Mobile Dynamic Map.
- [x] Obtain a Dynamic Map NCP Key ID.
- [ ] **TODO(NAVER Console):** edit the existing Maps application and register Android app package **`com.dailytown.app`** under Mobile Dynamic Map. Remove the temporary/random package after the new package is verified.
- [ ] Before a credentialed physical-device test, verify that the package restriction registered in NAVER Cloud Platform exactly matches the APK's actual `applicationId` (`com.dailytown.app`). A valid key alone is not sufficient when the registered Android package does not match.
- [ ] Put the issued NCP Key ID in local Gradle user properties or an environment variable, never in source control:

```properties
NAVER_MAP_NCP_KEY_ID=<issued-key-id>
```

Recommended local file: `~/.gradle/gradle.properties`.

Alternative shell injection:

```bash
export NAVER_MAP_NCP_KEY_ID=<issued-key-id>
```

- [ ] For GitHub-hosted internal APK builds, add repository Actions secret named `NAVER_MAP_NCP_KEY_ID` with the issued key value.
- [ ] Run the `Internal Debug APK` workflow once the secret and NAVER package restriction are configured; download and install the generated APK on a real Android device.
- [ ] Confirm NAVER Maps pricing, quota, display/caching, and location-game use terms for the intended product behavior.

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
- [ ] Verify NAVER map tiles, user-location overlay, encounter markers, and camera movement.
- [ ] Verify precise/approximate location permission behavior.
- [ ] Verify battery saver / balanced / precise tracking presets.
- [ ] Verify GPS rejection counters and impossible-jump filtering.
- [ ] Walk the same route twice and verify repeat handling feels intentional rather than empty.
- [ ] Verify daily/weekly counters reset only when the corresponding period changes.
- [ ] Enable/disable the optional local reminder and verify Android 13+ notification permission flow.
- [ ] Share a field-test diagnostic and verify it contains package/build/game counters but no coordinates or credential values.

## External beta / release

Before any external beta or store submission:

- [ ] Write/publish the location and privacy policy.
- [ ] Decide whether background location is truly required. Current MVP deliberately does **not** request it.
- [ ] Create signing/release credentials outside the repository.
- [ ] Decide analytics/crash-reporting vendor and consent policy before adding telemetry SDKs.
- [ ] Configure Play Console internal testers.
- [ ] Define field-test acceptance thresholds for session duration, battery impact, distance accuracy, encounters/session, completion rate, and repeat-area fatigue.

## Safe to continue without these TODOs

Replay exploration, GPS filtering, derived progress persistence, POI abstraction, encounter generation rules, mystery state machine, clue mechanics, companion reaction hooks, anti-repeat ranking, neighborhood progress, contextual/rare encounters, companion semantic memory, daily/weekly goal rotation, opt-in local reminders, and privacy-safe diagnostic export can all be developed and tested without production POI data or release credentials.
