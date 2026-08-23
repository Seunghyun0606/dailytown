# Human TODOs

Naver Maps is the selected MVP provider. The application is intentionally functional in replay mode without credentials; the map area shows a placeholder until the following human-owned tasks are completed.

## TODO 1 — NAVER Cloud Maps credential

- [ ] In NAVER Cloud Platform, create/select a Maps application and enable the Android dynamic map capability.
- [ ] Register Android package `com.dailytown.app` and apply the provider's required app restrictions.
- [ ] Copy only the NCP Key ID (not a secret) into your local Gradle user properties:

```properties
NAVER_MAP_NCP_KEY_ID=<your-key-id>
```

Recommended local location: `~/.gradle/gradle.properties`. Do not commit the value.

For CI/release builds, inject `NAVER_MAP_NCP_KEY_ID` from the build environment or repository secret mechanism rather than source control.

## TODO 2 — real-device validation

- [ ] Run on a physical Android device outdoors.
- [ ] Verify precise/approximate location permission behavior.
- [ ] Verify location marker/camera updates and GPS rejection counters.
- [ ] Walk the same route twice and confirm already discovered content is not awarded twice.

## TODO 3 — release/privacy decisions

Before any external beta or store submission:

- [ ] Write and publish the location/privacy policy.
- [ ] Decide whether background location is actually required. Current MVP deliberately does **not** request background location.
- [ ] Create signing/release credentials and store them outside the repository.
- [ ] Decide analytics/crash-reporting vendor and consent policy before adding any telemetry SDK.

## Not blocked by these TODOs

The replay route, exploration engine, location quality policy, map-provider abstraction, Naver adapter, and tests can all be developed without a real map credential.
