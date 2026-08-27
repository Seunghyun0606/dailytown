#!/usr/bin/env bash
set -euo pipefail

if ! command -v adb >/dev/null 2>&1; then
  echo "adb is required. Install Android platform-tools first." >&2
  exit 2
fi
if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 is required to validate and package marker evidence." >&2
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

# Remove only connected-device NAVER additional output before the run so an old
# session cannot be mistaken for the evidence produced by this invocation.
rm -rf \
  app/build/outputs/connected_android_test_additional_output \
  app/build/intermediates/connected_android_test_additional_output

set +e
"${GRADLE[@]}" connectedDebugAndroidTest \
  -Pandroid.enableAdditionalTestOutput=true \
  -Pandroid.testInstrumentationRunnerArguments.class=com.dailytown.app.visualqa.NaverMapVisualQaTest \
  -Pandroid.testInstrumentationRunnerArguments.dailytownQaRunner=physical-connected-device
STATUS=$?
set -e

echo "NAVER physical evidence files:"
for ROOT in \
  app/build/outputs/connected_android_test_additional_output \
  app/build/intermediates/connected_android_test_additional_output; do
  if [[ -d "$ROOT" ]]; then
    find "$ROOT" -type f \
      \( -path '*/visual/naver-*' -o -path '*/visual/naver-diagnostics/*' \) \
      -print 2>/dev/null || true
  fi
done

if [[ "$STATUS" -ne 0 ]]; then
  echo "NAVER physical test failed. Raw diagnostics were left under app/build; no review bundle was created." >&2
  exit "$STATUS"
fi

mapfile -t SESSIONS < <(
  for ROOT in \
    app/build/outputs/connected_android_test_additional_output \
    app/build/intermediates/connected_android_test_additional_output; do
    if [[ -d "$ROOT" ]]; then
      find "$ROOT" -type f -path '*/visual/naver-diagnostics/session.json' -print 2>/dev/null
    fi
  done | sort -u
)
if [[ ${#SESSIONS[@]} -ne 1 ]]; then
  echo "Expected exactly one fresh connected-device NAVER session; found ${#SESSIONS[@]}." >&2
  exit 3
fi

RUN_STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
OUTPUT_ROOT="${DAILYTOWN_MARKER_EVIDENCE_DIR:-app/build/marker-physical-evidence}"
BUNDLE_DIR="${OUTPUT_ROOT%/}/physical-${RUN_STAMP}"

python3 tools/visual/package_marker_physical_evidence.py \
  --session "${SESSIONS[0]}" \
  --output-dir "$BUNDLE_DIR"

echo ""
echo "Physical marker evidence is packaged for human review."
echo "Review: $BUNDLE_DIR/REVIEW.md"
echo "Pending approval copy: $BUNDLE_DIR/marker-promotion-approval.v1.json"
echo "Archive: $BUNDLE_DIR.zip"
echo "Do not promote marker assets until the human approval is completed and the promotion readiness checker passes."
