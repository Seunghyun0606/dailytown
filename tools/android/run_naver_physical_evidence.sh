#!/usr/bin/env bash
set -euo pipefail

if ! command -v adb >/dev/null 2>&1; then
  echo "adb is required. Install Android platform-tools first." >&2
  exit 2
fi

if [[ -z "${NAVER_MAP_NCP_KEY_ID:-}" ]]; then
  echo "NAVER_MAP_NCP_KEY_ID must be supplied through the environment." >&2
  exit 2
fi

mapfile -t DEVICES < <(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')
if [[ ${#DEVICES[@]} -ne 1 ]]; then
  echo "Exactly one authorized Android device must be connected; found ${#DEVICES[@]}." >&2
  exit 2
fi

SERIAL="${DEVICES[0]}"
QEMU="$(adb -s "$SERIAL" shell getprop ro.kernel.qemu 2>/dev/null | tr -d '\r')"
if [[ "$QEMU" == "1" ]]; then
  echo "Physical-device evidence runner refuses emulator targets." >&2
  exit 2
fi

MODEL="$(adb -s "$SERIAL" shell getprop ro.product.model 2>/dev/null | tr -d '\r')"
ANDROID_RELEASE="$(adb -s "$SERIAL" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')"
echo "NAVER physical evidence target detected: ${MODEL:-unknown-model}, Android ${ANDROID_RELEASE:-unknown}."
echo "Credential wiring is present; credential value will not be printed."

if [[ -x ./gradlew ]]; then
  GRADLE=(./gradlew)
elif command -v gradle >/dev/null 2>&1; then
  GRADLE=(gradle)
else
  echo "Gradle is required (./gradlew or gradle on PATH)." >&2
  exit 2
fi

set +e
"${GRADLE[@]}" connectedDebugAndroidTest \
  -Pandroid.enableAdditionalTestOutput=true \
  -Pandroid.testInstrumentationRunnerArguments.class=com.dailytown.app.visualqa.NaverMapVisualQaTest \
  -Pandroid.testInstrumentationRunnerArguments.dailytownQaRunner=physical-connected-device
STATUS=$?
set -e

echo "NAVER physical evidence files:"
find app/build -type f \
  \( -path '*connected_android_test_additional_output*' -o -path '*additional_test_output*' \) \
  \( -path '*visual/naver-*' -o -path '*visual/naver-diagnostics*' \) \
  -print 2>/dev/null || true

python3 - <<'PY'
import glob
import json

files = sorted(glob.glob("app/build/**/visual/naver-diagnostics/session.json", recursive=True))
if not files:
    print("NAVER diagnostic JSON was not found.")
for path in files:
    with open(path, encoding="utf-8") as handle:
        data = json.load(handle)
    env = data.get("environment", {})
    client = data.get("naverClient", {})
    network = data.get("networkFinal", {})
    attempts = data.get("baseMapAttempts", [])
    captures = data.get("matrixCaptures", [])
    baseline = [item for item in captures if item.get("kind") == "baseline"]
    ev1 = [item for item in captures if item.get("kind") == "ev1_checkpoint"]
    last = attempts[-1] if attempts else {}
    print(json.dumps({
        "path": path,
        "outcome": data.get("outcome"),
        "failureCategory": data.get("failureCategory"),
        "clientMode": client.get("mode"),
        "expectedRegisteredAndroidPackage": client.get("expectedRegisteredAndroidPackage"),
        "packageMatchesExpected": client.get("packageMatchesExpected"),
        "runnerHint": env.get("runnerHint"),
        "model": env.get("model"),
        "androidRelease": env.get("androidRelease"),
        "emulator": env.get("emulator"),
        "networkInternet": network.get("internet"),
        "networkValidated": network.get("validated"),
        "readyLatencyMs": data.get("readyLatencyMs"),
        "attemptCount": len(attempts),
        "matrixCaptureCount": len(captures),
        "baselineCaptureCount": len(baseline),
        "ev1CheckpointCaptureCount": len(ev1),
        "lastEvidence": {
            "quantizedColors": last.get("quantizedColors"),
            "luminanceStdDev": last.get("luminanceStdDev"),
            "strongEdgeRatio": last.get("strongEdgeRatio"),
            "passed": last.get("passed"),
        },
    }, ensure_ascii=False, sort_keys=True))
PY

exit "$STATUS"
