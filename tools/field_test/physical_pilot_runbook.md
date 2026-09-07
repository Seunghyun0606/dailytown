# Daily Town physical pilot runbook

## Scope

This runbook is for the initial four-session Android MVP pilot. It uses the approved provisional baseline in `mvp_baseline.env` and must be revisited after the pilot.

The physical pilot now validates both layers in one debug APK:

- production nearby POIs from the configured 한국관광공사 TourAPI canonical feed;
- fixed Seoul/Jungwon QA anchors used to make the route repeatable.

Release builds do not expose the fixed QA anchors.

## One-command preflight

Use one real authorized Android phone and configure both credentials in the same Bash shell without printing their values:

```bash
read -rsp "NAVER_MAP_NCP_KEY_ID: " NAVER_MAP_NCP_KEY_ID; echo; export NAVER_MAP_NCP_KEY_ID
read -rsp "TOUR_API_SERVICE_KEY: " TOUR_API_SERVICE_KEY; echo; export TOUR_API_SERVICE_KEY
bash tools/field_test/prepare_physical_test.sh
```

The script validates the approved field-test baseline, release-policy baseline, NAVER/TourAPI wiring, JVM tests, Android lint, installs the APK, and starts Daily Town. It rejects emulator/multiple-device setups.

## Fixed conditions

- Device: use the same physical Android device for all four sessions.
- Tracking preset: BALANCED for all four sessions.
- App build: use the same commit for all four sessions.
- Network: keep ordinary mobile/Wi-Fi conditions; note any meaningful outage rather than restarting silently.
- Reference distance: before each session, confirm the planned walking route in NAVER and enter only the total walking distance in meters.
- App data: do not clear app data between a NEW_AREA session and its paired REPEAT_AREA session.
- Export: structured JSON is in-memory only; export before closing or force-stopping the app.
- POI source: before the first session, Settings must show 한국관광공사 TourAPI as the canonical POI source.

## Session order

### 1. Seoul — NEW_AREA

Use the Seoul downtown fixture POIs as repeatable visual/encounter anchors while also observing production TourAPI POIs encountered nearby:

- 서울시청
- 덕수궁
- 을지로입구역
- SK서린빌딩
- 광화문 Gate
- 인사동 대표 anchor

Start Field Test / QA, select NEW_AREA, confirm BALANCED, enter the NAVER walking-route distance, start actual-location tracking, and walk the planned route for at least 10 minutes.

### 2. Seoul — REPEAT_AREA

Repeat the same route without clearing app data. Select REPEAT_AREA and keep BALANCED plus the same reference route definition.

### 3. Jungwon-gu — NEW_AREA

Use the Jungwon-gu test area centered on:

- 중원구청
- 성남종합운동장
- existing Seongnam fixture POIs, including 스타벅스 성남모란DT점 when it is on the chosen route

Also observe any production TourAPI POIs that appear along the route. Select NEW_AREA, confirm BALANCED, enter the NAVER walking-route distance, and walk for at least 10 minutes.

### 4. Jungwon-gu — REPEAT_AREA

Repeat the same Jungwon-gu route without clearing app data. Select REPEAT_AREA and keep the same preset/reference-route definition.

## Before each session

- Map status is READY.
- Current-location overlay is at the real current location.
- Starting actual-location tracking does not jump to a stale Seoul/test coordinate.
- Map can be panned without scrolling the parent screen.
- Visible POI markers match the physical surroundings closely enough for the pilot.
- Known QA anchors remain visible in the debug build.
- Nearby production POIs can appear without replacing the fixed QA anchors.
- A production POI and QA anchor for the same physical place do not render as obvious duplicate markers.
- Tracking preset is BALANCED.
- NEW_AREA or REPEAT_AREA profile is correct.
- Reference distance is entered.
- Battery percentage is sufficient to finish without charging mid-session when possible.

## During each session

Do not optimize behavior to make the metrics pass. Use the app as a normal tester would.

Observe, but do not stop the session solely because of:

- small GPS drift,
- temporary map loading,
- a missed encounter,
- a companion reaction that feels late.

Stop only for safety, unusable app state, wrong route/profile/preset, or a test-invalidating external interruption.

For POI behavior, note only meaningful defects such as stale POIs that remain far behind the user, dense overlapping markers, duplicate same-place markers, or a production POI clearly placed at the wrong physical location.

## After each session

- Stop tracking.
- Review the run checklist and acceptance result.
- Record the current session in comparison.
- Export structured JSON before app termination.
- Keep exported JSON outside the Git repository.
- Record only a short usability note for anything that felt confusing, annoying, invisible, or unexpectedly delightful.

## Provisional acceptance baseline

- session duration >= 600 seconds
- GPS rejection <= 15%
- map health = READY
- distance error <= 15%
- battery drain <= 12 percentage points/hour
- discovered encounters >= 2/session
- encounter resolution >= 50%
- REPEAT_AREA fatigue proxy <= 40%

The four-session pilot requires two NEW_AREA and two REPEAT_AREA sessions, one pair per region, matching tracking presets, and all nine protocol evidence categories.

## Data handling

- Store JSON/Markdown/CSV outside the repository in a personal local folder.
- Do not commit raw/session exports to GitHub.
- Do not auto-upload the exports to analytics or cloud storage.
- Delete session/export files within 30 days after product review is complete.
- Keep long-lived conclusions and threshold changes in the project Source of Truth rather than retaining the session files indefinitely.

## Final one-command review

After the final session, validate the exported JSON and generate the Markdown review/collection plan with:

```bash
bash tools/field_test/review_physical_export.sh <export.json>
```

## Usability review after the pilot

Review the separate Daily Town MVP usability checklist after all four sessions. In particular, revisit current-location trust, POI accuracy/discoverability, map readability, companion presence, encounter pacing, repeat-area fatigue, battery/heat, accessibility, and whether the provisional acceptance thresholds are still appropriate.
