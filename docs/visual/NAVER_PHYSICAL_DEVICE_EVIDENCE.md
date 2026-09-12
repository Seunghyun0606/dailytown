# NAVER Physical-Device Evidence Gate

Status: **runner implemented; actual physical-device evidence remains human-operated and unclaimed.**

The latest credentialed managed-device evidence reaches the NAVER adapter `READY` state with a validated network and then fails with NAVER authentication `401`. The marker promotion gate remains intentionally fail-closed: authentication must be corrected before provider-texture, marker-readability, or EV-1 evidence can count as PASS.

## Current 401 blocker

Daily Town's authoritative Android application ID is:

`com.dailytown.app`

For the current NCP Key ID client flow, verify in the NAVER Maps application configuration that the Android application/package registration contains that package name **exactly**. Also verify that the injected NCP Key ID belongs to the intended Maps application. Do not paste or commit the credential while checking this.

A `401` remains a QA failure. Do not bypass it, lower the marker-free texture thresholds, or promote the marker pack based on screenshots produced before authentication is healthy.

After correcting NAVER Console/application configuration, rerun the credentialed CI job and then the physical-device evidence runner. A successful authentication check only unlocks the next evidence stage; it does not itself promote markers.

## What the runner verifies

Run from the repository root with exactly one USB/Wi-Fi-debuggable physical Android device connected:

```bash
NAVER_MAP_NCP_KEY_ID=<configured-in-your-shell> bash tools/android/run_naver_physical_evidence.sh
```

The script:

1. requires `adb` and exactly one authorized connected device;
2. rejects a target whose `ro.kernel.qemu` reports an emulator;
3. never prints the NAVER credential value;
4. runs `NaverMapVisualQaTest` through `connectedDebugAndroidTest`;
5. preserves the same fail-closed marker-free base-map gate used in CI;
6. prints only value-blind evidence summary fields and the output file paths.

Do not paste the credential into issue/PR comments or shell transcripts. Use a local environment variable or local Gradle property according to the existing credential policy.

## Evidence emitted

The Android test writes PNG captures plus:

`visual/naver-diagnostics/session.json`

The JSON is intentionally privacy-safe. It contains:

- pass/fail outcome and failure category;
- credential client mode (`NCP_KEY_ID`) without the credential value;
- expected registered Android package and whether the running target package matches it;
- expected runner hint;
- Android version, model/product, ABI, and emulator boolean;
- network INTERNET/VALIDATED booleans and transport category only;
- provider-neutral map health status/error code;
- READY latency;
- each marker-free screenshot attempt's width/height, quantized-color count, luminance standard deviation, edge ratio, and pass/fail result;
- each completed baseline/EV-1 matrix capture with semantic id, phase, map complexity, motion mode, marker family, companion id, and artifact storage name.

It intentionally does **not** contain:

- NAVER credential values;
- raw provider exception payloads;
- GPS coordinates or route geometry;
- test fixture coordinates;
- SSID/network names;
- Android serial numbers or durable device identifiers.

## Marker promotion gate

The marker pack remains `production_export_candidate` until all of the following are true:

1. NAVER authentication succeeds without `401`, `429`, `800`, or other provider auth errors;
2. physical device reports `emulator=false`;
3. NAVER adapter remains `READY` without auth/provider error;
4. network is available and every required provider surface passes the marker-free tile/texture guard before overlays are counted;
5. the approved 18-case baseline overlay matrix completes: `MORNING/SUNSET/NIGHT × simple_residential/dense_urban/mixed_poi × normal/reduced_motion`;
6. all EV-1 forced checkpoints `E0..E4` complete on the dense-map fixture in normal/reduced modes, including E2 luminance-based DAY/DARK family selection;
7. diagnostics report `baselineCaptureCount=18`, `ev1CheckpointCaptureCount=10`, and `matrixCaptureCount=28` for the complete technical run;
8. a human checks the resulting screenshots for marker/route/HUD/companion readability, provider road/place comprehension, and confirms NAVER attribution/provider legal UI is not obscured;
9. final outdoor R-B acceptance remains a separate human gate using `PASS`, `PASS_WITH_DECORATIVE_DEGRADATION`, or `FAIL`.

A passing physical technical run does not by itself close R-B or authorize motion/icon decisions.

## Interpreting failure categories

- `auth_error` / `auth_error_after_ready`: for `401`, verify the injected NCP Key ID and exact Android package registration `com.dailytown.app` without exposing the credential. Other auth codes remain provider/configuration failures and must stay fail-closed.
- `ready_timeout`: SDK/map initialization did not become ready in the test window.
- `network_not_validated`: Android did not report a validated active network when provider texture remained unavailable.
- `provider_texture_insufficient`: network and adapter health were acceptable, but the captured map surface still looked like a blank/grid/non-provider tile surface.
- `provider_left_ready_state` / `adapter_error_after_ready`: adapter health regressed during evidence capture.
- `screenshot_unavailable`: device-side screenshot acquisition failed.

Do not lower visual thresholds solely to turn a failure into PASS. Diagnose authentication, environment, and provider-rendering paths in that order.
