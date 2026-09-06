#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

source "$SCRIPT_DIR/mvp_baseline.env"

if [[ -z "${NAVER_MAP_NCP_KEY_ID:-}" || "${NAVER_MAP_NCP_KEY_ID}" == TODO_* ]]; then
  echo "NAVER_MAP_NCP_KEY_ID is not configured in this shell." >&2
  echo "Set it without echoing the value, then rerun this script:" >&2
  echo '  read -rsp "NAVER_MAP_NCP_KEY_ID: " NAVER_MAP_NCP_KEY_ID; echo; export NAVER_MAP_NCP_KEY_ID' >&2
  exit 2
fi

if ! command -v gradle >/dev/null 2>&1; then
  echo "gradle is not available on PATH." >&2
  exit 3
fi

if ! command -v adb >/dev/null 2>&1; then
  echo "adb is not available on PATH." >&2
  exit 4
fi

python3 -m unittest tools.field_test.test_mvp_baseline

gradle verifyNaverMapCredential
gradle installDebug

adb start-server >/dev/null
adb shell am force-stop com.dailytown.app || true
adb shell am start -n com.dailytown.app/.MainActivity

echo
printf '%s\n' \
  "Daily Town physical field-test build installed." \
  "Baseline: BALANCED / 600s / GPS reject<=15% / distance error<=15% / battery<=12%p/h." \
  "Run order: Seoul NEW -> Seoul REPEAT -> Jungwon-gu NEW -> Jungwon-gu REPEAT." \
  "Before each session, enter the NAVER walking-route distance as the reference distance." \
  "Export structured JSON before closing or force-stopping the app."
