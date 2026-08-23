# Human TODOs

Daily Town continues development without blocking on credentials. The items below are deliberately left for a person because they involve account ownership, legal/content approval, real-device judgment, or release secrets.

## NAVER Maps account / credential

Status: a NAVER Dynamic Map NCP Key ID has been issued. The key value must stay outside source control and is consumed through `NAVER_MAP_NCP_KEY_ID`.

- [x] Create/select a NAVER Cloud Platform Maps application and enable Android Dynamic Map.
- [x] Obtain a Dynamic Map NCP Key ID.
- [ ] **TODO(final Android package):** the NAVER application currently uses a temporary/random Android package restriction created only to issue the key. When the product `applicationId` is finalized, update the Android package restriction in NCP Console and update the app `applicationId` together.
- [ ] Decide the final Android `applicationId`. The code currently uses `com.dailytown.app` as an MVP placeholder only.
- [ ] Before a credentialed physical-device test, make the package restriction registered in NAVER Cloud Platform match the APK's actual `applicationId`. A valid key alone is not sufficient when the registered Android package does not match.
- [ ] Put the issued NCP Key ID in local Gradle user properties or an environment variable, never in source control:

```properties
NAVER_MAP_NCP_KEY_ID=<issued-key-id>
```

Recommended local file: `~/.gradle/gradle.properties`.

Alternative shell injection:

```bash
export NAVER_MAP_NCP_KEY_ID=<issued-key-id>
```

- [ ] Inject the same environment/property securely for CI/release when credentialed map builds are required.
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
- [ ] Run on a physical Android device outdoors after NAVER package restriction matches the APK.
- [ ] Verify precise/approximate location permission behavior.
- [ ] Verify location marker/camera updates and GPS rejection counters.
- [ ] Walk the same route twice and verify repeat handling feels intentional rather than empty.
- [ ] Verify daily/weekly counters reset only when the corresponding period changes.

## External beta / release

Before any external beta or store submission:

- [ ] Write/publish the location and privacy policy.
- [ ] Decide whether background location is truly required. Current MVP deliberately does **not** request it.
- [ ] Create signing/release credentials outside the repository.
- [ ] Decide analytics/crash-reporting vendor and consent policy before adding telemetry SDKs.
- [ ] Configure Play Console/internal testers.

## Safe to continue without these TODOs

Replay exploration, GPS filtering, derived progress persistence, POI abstraction, encounter generation rules, mystery state machine, clue mechanics, companion reaction hooks, anti-repeat ranking, neighborhood progress, and daily/weekly goal rotation can all be developed and tested without production package restrictions or release credentials.
