#!/usr/bin/env bash
set -euo pipefail

resolve_python() {
  local candidate

  # Git Bash on Windows can expose Microsoft Store/App Execution Alias shims as
  # python3/python. Verify that a candidate can actually execute Python 3.9+.
  for candidate in python3 python; do
    if command -v "$candidate" >/dev/null 2>&1 && \
       "$candidate" -c 'import sys; raise SystemExit(0 if sys.version_info >= (3, 9) else 1)' >/dev/null 2>&1; then
      PYTHON_CMD=("$candidate")
      return 0
    fi
  done

  if command -v py >/dev/null 2>&1 && \
     py -3 -c 'import sys; raise SystemExit(0 if sys.version_info >= (3, 9) else 1)' >/dev/null 2>&1; then
    PYTHON_CMD=(py -3)
    return 0
  fi

  echo "Python 3.9+ is required to validate and package marker evidence, but no working interpreter was found." >&2
  echo "Install Python or make python/python3/py available on PATH." >&2
  exit 2
}

if ! command -v adb >/dev/null 2>&1; then
  echo "adb is required. Install Android platform-tools first." >&2
  exit 2
fi
resolve_python
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
PYTHON_LABEL="$("${PYTHON_CMD[@]}" -c 'import sys; print(f"Python {sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro}")')"
echo "NAVER physical evidence target detected: ${MODEL:-unknown-model}, Android ${ANDROID_RELEASE:-unknown}."
echo "Python runtime: $PYTHON_LABEL"
echo "Credential wiring is present; credential value will not be printed."

if [[ -x ./gradlew ]]; then
  GRADLE=(./gradlew)
elif command -v gradle >/dev/null 2>&1; then
  GRADLE=(gradle)
else
  echo "Gradle is required (./gradlew or gradle on PATH)." >&2
  exit 2
fi

# Remove known connected-device additional-output roots, then also timestamp this
# invocation. The timestamp lets discovery stay safe if AGP moves the output path.
rm -rf \
  app/build/outputs/connected_android_test_additional_output \
  app/build/intermediates/connected_android_test_additional_output
RUN_MARKER="$(mktemp "${TMPDIR:-/tmp}/dailytown-physical-evidence.XXXXXX")"
trap 'rm -f "$RUN_MARKER"' EXIT

set +e
"${GRADLE[@]}" connectedDebugAndroidTest \
  -Pandroid.enableAdditionalTestOutput=true \
  -Pandroid.testInstrumentationRunnerArguments.class=com.dailytown.app.visualqa.NaverMapVisualQaTest \
  -Pandroid.testInstrumentationRunnerArguments.dailytownQaRunner=physical-connected-device
STATUS=$?
set -e

echo "NAVER physical evidence files from this invocation:"
find app/build -type f -newer "$RUN_MARKER" \
  \( -path '*/visual/naver-*' -o -path '*/visual/naver-diagnostics/*' \) \
  -print 2>/dev/null || true

if [[ "$STATUS" -ne 0 ]]; then
  echo "NAVER physical test failed. Raw diagnostics were left under app/build; no review bundle was created." >&2
  exit "$STATUS"
fi

mapfile -t SESSIONS < <(
  find app/build -type f -newer "$RUN_MARKER" \
    -path '*/visual/naver-diagnostics/session.json' -print 2>/dev/null | sort -u
)
if [[ ${#SESSIONS[@]} -ne 1 ]]; then
  echo "Expected exactly one fresh connected-device NAVER session; found ${#SESSIONS[@]}." >&2
  exit 3
fi

RUN_STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
OUTPUT_ROOT="${DAILYTOWN_MARKER_EVIDENCE_DIR:-app/build/marker-physical-evidence}"
BUNDLE_DIR="${OUTPUT_ROOT%/}/physical-${RUN_STAMP}"
BUNDLE_ZIP="$BUNDLE_DIR.zip"

"${PYTHON_CMD[@]}" tools/visual/package_marker_physical_evidence.py \
  --session "${SESSIONS[0]}" \
  --output-dir "$BUNDLE_DIR"

# The packager creates both a directory and matching ZIP. Verify both independently
# before reporting success so a corrupt or unsafe review bundle cannot be handed off.
"${PYTHON_CMD[@]}" tools/visual/verify_marker_physical_evidence_bundle.py \
  --bundle "$BUNDLE_DIR"
"${PYTHON_CMD[@]}" tools/visual/verify_marker_physical_evidence_bundle.py \
  --bundle "$BUNDLE_ZIP"

echo ""
echo "Physical marker evidence is packaged and integrity-verified for human review."
echo "Review: $BUNDLE_DIR/REVIEW.md"
echo "Pending approval copy: $BUNDLE_DIR/marker-promotion-approval.v1.json"
echo "Archive: $BUNDLE_ZIP"
echo "Do not promote marker assets until the human approval is completed and the promotion readiness checker passes."
