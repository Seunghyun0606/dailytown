# NAVER Physical-Device Evidence Gate

Status: **runner implemented; actual physical-device evidence remains human-operated and unclaimed.**

This gate exists because the API 30 AOSP-ATD managed-device run can reach the NAVER adapter `READY` state while the marker-free provider surface still fails the real-map texture proof. The emulator gate is intentionally not weakened to make the run green.

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
- expected runner hint;
- Android version, model/product, ABI, and emulator boolean;
- network INTERNET/VALIDATED booleans and transport category only;
- provider-neutral map health status/error code;
- READY latency;
- each marker-free screenshot attempt's width/height, quantized-color count, luminance standard deviation, edge ratio, and pass/fail result.

It intentionally does **not** contain:

- NAVER credential values;
- raw provider exception payloads;
- GPS coordinates or route geometry;
- test fixture coordinates;
- SSID/network names;
- Android serial numbers or durable device identifiers.

## Marker promotion gate

The marker pack remains `production_export_candidate` until all of the following are true:

1. physical device reports `emulator=false`;
2. NAVER adapter remains `READY` without auth/provider error;
3. network is available and the provider surface passes the marker-free tile/texture guard;
4. sparse residential, dense urban, and green-space DAY/DARK captures complete;
5. EV-1 E2 luminance-based family selection capture completes;
6. a human checks the resulting screenshots for marker readability and confirms NAVER attribution/provider legal UI is not obscured;
7. final outdoor R-B acceptance remains a separate human gate.

A passing physical technical run does not by itself close R-B or authorize motion/icon decisions.

## Interpreting failure categories

- `auth_error` / `auth_error_after_ready`: check NAVER application/package registration and credential wiring without exposing the credential.
- `ready_timeout`: SDK/map initialization did not become ready in the test window.
- `network_not_validated`: Android did not report a validated active network when provider texture remained unavailable.
- `provider_texture_insufficient`: network and adapter health were acceptable, but the captured map surface still looked like a blank/grid/non-provider tile surface.
- `provider_left_ready_state` / `adapter_error_after_ready`: adapter health regressed during evidence capture.
- `screenshot_unavailable`: device-side screenshot acquisition failed.

Do not lower visual thresholds solely to turn a failure into PASS. Diagnose the environment/provider-rendering path first.
