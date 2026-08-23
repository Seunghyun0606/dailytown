# Human TODOs

Daily Town now continues development without blocking on credentials. The items below are deliberately left for a person because they involve account ownership, legal/content approval, real-device judgment, or release secrets.

## NAVER Maps account / credential

- [ ] In NAVER Cloud Platform, create/select a Maps application and enable the Android dynamic map capability.
- [ ] Register Android package `com.dailytown.app` and apply the provider's required app restrictions.
- [ ] Put the NCP Key ID in local Gradle user properties, never in source control:

```properties
NAVER_MAP_NCP_KEY_ID=<your-key-id>
```

Recommended location: `~/.gradle/gradle.properties`.

- [ ] Inject the same property securely for CI/release when credentialed map builds are required.
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
- [ ] Run on a physical Android device outdoors.
- [ ] Verify precise/approximate location permission behavior.
- [ ] Verify location marker/camera updates and GPS rejection counters.
- [ ] Walk the same route twice and verify repeat handling feels intentional rather than empty.

## External beta / release

Before any external beta or store submission:

- [ ] Write/publish the location and privacy policy.
- [ ] Decide whether background location is truly required. Current MVP deliberately does **not** request it.
- [ ] Create signing/release credentials outside the repository.
- [ ] Decide analytics/crash-reporting vendor and consent policy before adding telemetry SDKs.
- [ ] Configure Play Console/internal testers.

## Safe to continue without these TODOs

Replay exploration, GPS filtering, derived progress persistence, POI abstraction, encounter generation rules, mystery state machine, clue mechanics, companion reaction hooks, anti-repeat ranking, neighborhood progress, and goal rotation can all be developed and tested without production credentials.
