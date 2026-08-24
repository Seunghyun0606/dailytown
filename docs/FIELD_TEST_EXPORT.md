# Field-test structured export

Daily Town can explicitly share the currently buffered field-test comparison data as a versioned, privacy-safe JSON document.

## Purpose

The in-app comparison recorder is intentionally process-memory-only and bounded to 20 completed sessions. This protects the current MVP from silently creating a durable location-derived dataset, but it also means a tester needs an explicit way to take a batch of derived results outside the app for review.

`구조화 JSON 공유` creates that external handoff only when the tester taps the button. The app itself still does not persist the export or the session history.

## Schema

- schema name: `dailytown.field_test_export`
- schema version: `1`
- maximum session rows: `20`
- session ordering: current in-memory recorder order, exposed only as an export-local `ordinal`
- missing evidence: JSON `null`; never converted to zero

The export includes:

- app version and package ID
- non-secret single-session acceptance criteria
- non-secret comparison protocol criteria
- current protocol assessment and issues
- each recorded session's area profile, tracking preset, single-run review status, map-health state, derived distance/GPS/battery/gameplay metrics, and acceptance state
- NEW_AREA / REPEAT_AREA cohort aggregates, evidence counts, acceptance counts, and repeat-minus-new deltas

## Privacy boundary

The export intentionally contains no:

- latitude / longitude
- route geometry
- place name or free-form route label
- POI, encounter, template, or raw-event identifiers
- session token
- persistent session identifier
- device identifier
- generated timestamp
- provider exception payload
- NAVER credential

The export is therefore useful for comparing derived field evidence without becoming a replayable route/location history.

## Why there is no import or automatic persistence yet

Importing many files back into the app, assigning durable run IDs, or retaining structured field-test history would establish a new product data-retention boundary. That decision requires a person to approve retention duration, access rules, deletion behavior, and privacy notice implications.

Until those decisions are made:

- the app keeps comparison sessions only in process memory
- relaunch/reset clears the in-app buffer
- export is tester-initiated only
- the receiving app/service chosen from the Android share sheet controls any external retention

## Required physical-device procedure

1. In NAVER Cloud Console, confirm the Android package restriction is exactly `com.dailytown.app`.
2. In GitHub, run **Actions -> Internal Debug APK -> Run workflow** on the field-test branch.
3. Download the `dailytown-internal-debug-apk` artifact after the workflow succeeds.
4. Verify `build-metadata.txt`, `field-test-policy.txt`, and the SHA-256 file are present beside `app-debug.apk`.
5. Install `app-debug.apk` on the real Android test device. If Android blocks the install, allow installation from the file/browser source only for this internal test flow.
6. Outdoors, open Daily Town and confirm the map reaches `지도 정상` / provider-neutral `MapHealth.READY` and real NAVER tiles render.
7. Before a representative route, select `신규 지역` or `반복 지역`, choose/confirm the tracking preset, and enter the trusted scalar route distance when distance-error evidence is required.
8. For battery evidence, disconnect external power before the session starts and keep brightness/device/preset conditions reasonably consistent across comparison runs.
9. Start tracking, walk the representative route, and stop the session. Confirm the completed plan still shows the start-latched area/preset/reference.
10. Review `런 요약`. `확인 필요` means a configured required item is missing or failed; `참고용` means no applicable run-level policy is configured; `검토 가능` means the configured requirements for this single run are complete and passing.
11. Record the session once into the comparison buffer. Repeat for NEW_AREA and REPEAT_AREA sessions.
12. Tap **구조화 JSON 공유** and send/save the JSON only to the human-approved test destination. The app does not retain a copy after process reset/relaunch.
13. Before using the export for a product decision, confirm the JSON has the expected `schemaVersion`, policy values, session counts, evidence counts, and no unexpected location/place/identifier fields.

## Human decisions still required

The following cannot be safely invented by engineering:

- the eight single-session acceptance threshold values
- minimum valid sessions per NEW_AREA / REPEAT_AREA cohort
- whether one matching tracking preset is mandatory
- which comparison evidence keys are mandatory
- representative mostly-new and repeat-area routes and trusted scalar reference distances
- whether the repeat-area-fatigue proxy is sufficiently correlated with subjective repetition/fatigue
- the approved external destination for structured exports and who may access them
- retention duration and deletion procedure for exported files
- whether future app-side import/persistence is allowed
- production POI/public-data licensing, authored content approval, Play Console/release/privacy/signing decisions

Until export retention/access decisions are approved, do not automatically upload these JSON documents to analytics, cloud storage, a backend, or source control.

## Automated coverage

JVM tests verify:

- schema/version and policy fields
- per-session run-review state
- missing evidence remains `null`
- bounded detached recorder snapshots
- exclusion of coordinates, event IDs, session tokens, device IDs, timestamps, and credential names/values

The AOSP managed-device flow verifies that structured export is disabled with an empty recorder, becomes enabled after a completed session is recorded, remains enabled across NEW_AREA/REPEAT_AREA comparison, and is disabled again after comparison reset. The emulator does not click the Android share sheet and does not claim to validate external retention behavior.
