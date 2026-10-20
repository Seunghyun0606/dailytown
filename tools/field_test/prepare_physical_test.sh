#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

source "$SCRIPT_DIR/mvp_baseline.env"

require_secret_env() {
  local name="$1"
  local hint="$2"
  local value="${!name:-}"
  if [[ -z "$value" || "$value" == TODO_* ]]; then
    echo "$name is not configured in this shell." >&2
    echo "$hint" >&2
    exit 2
  fi
}

resolve_python() {
  local candidate

  # Git Bash on Windows can expose Microsoft Store/App Execution Alias shims as
  # python3/python. Do not trust command -v alone; verify that the candidate can
  # actually execute Python code. Keep all probe output suppressed so broken
  # shims cannot print a misleading bare "Python" line during preflight.
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

  echo "Python 3.9+ is required, but no working interpreter was found." >&2
  echo "Install Python or make python/python3/py available on PATH." >&2
  exit 7
}

require_secret_env \
  NAVER_MAP_NCP_KEY_ID \
  'Set it without echoing the value: read -rsp "NAVER_MAP_NCP_KEY_ID: " NAVER_MAP_NCP_KEY_ID; echo; export NAVER_MAP_NCP_KEY_ID'
require_secret_env \
  TOUR_API_SERVICE_KEY \
  'Set it without echoing the value: read -rsp "TOUR_API_SERVICE_KEY: " TOUR_API_SERVICE_KEY; echo; export TOUR_API_SERVICE_KEY'

if ! command -v gradle >/dev/null 2>&1; then
  echo "gradle is not available on PATH." >&2
  exit 3
fi
if ! command -v adb >/dev/null 2>&1; then
  echo "adb is not available on PATH." >&2
  exit 4
fi

resolve_python

adb start-server >/dev/null
mapfile -t devices < <(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')
if [[ ${#devices[@]} -ne 1 ]]; then
  echo "Exactly one authorized Android device is required; found ${#devices[@]}." >&2
  adb devices -l >&2
  exit 5
fi
serial="${devices[0]}"
if [[ "$serial" == emulator-* ]] || [[ "$(adb -s "$serial" shell getprop ro.kernel.qemu 2>/dev/null | tr -d '\r')" == "1" ]]; then
  echo "The full physical pilot must run on one real Android phone, not an emulator." >&2
  exit 6
fi

python_label="$("${PYTHON_CMD[@]}" -c 'import sys; print(f"Python {sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro}")')"
printf '%s\n' \
  "Daily Town full physical preflight" \
  "- Python runtime: $python_label" \
  "- validates approved field-test thresholds" \
  "- verifies NAVER + TourAPI credential wiring without printing credential values" \
  "- runs JVM tests and Android lint" \
  "- installs one debug APK containing production TourAPI POIs plus known Seoul/Jungwon QA anchors"

"${PYTHON_CMD[@]}" -m unittest tools.field_test.test_mvp_baseline
"${PYTHON_CMD[@]}" tools/release/verify_play_release_baseline.py

gradle verifyNaverMapCredential verifyTourApiCredential
gradle testDebugUnitTest lintDebug installDebug

adb -s "$serial" shell am force-stop com.dailytown.app || true
adb -s "$serial" shell am start -n com.dailytown.app/.MainActivity >/dev/null

echo
printf '%s\n' \
  "Daily Town full physical field-test build installed on $serial." \
  "Production POI mode: TourAPI canonical feed enabled." \
  "Debug field-test anchors: Seoul/Jungwon fixtures enabled in the same APK." \
  "Baseline: BALANCED / 600s / GPS reject<=15% / distance error<=15% / battery<=12%p/h." \
  "Run order: Seoul NEW -> Seoul REPEAT -> Jungwon-gu NEW -> Jungwon-gu REPEAT." \
  "Before each session, enter the NAVER walking-route distance as the reference distance." \
  "In Settings, confirm POI source shows 한국관광공사 TourAPI before the first session." \
  "Export structured JSON before closing or force-stopping the app." \
  "After the final session run: bash tools/field_test/review_physical_export.sh <export.json>"
