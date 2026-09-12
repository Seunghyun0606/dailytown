#!/usr/bin/env bash
set -euo pipefail

run_qa() {
  gradle pixel2Api30AtdDebugAndroidTest \
    -Pandroid.enableAdditionalTestOutput=true \
    -Pandroid.testInstrumentationRunnerArguments.class=com.dailytown.app.visualqa.NaverMapVisualQaTest \
    -Pandroid.testInstrumentationRunnerArguments.dailytownQaRunner=pixel2Api30Atd \
    -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
}

latest_failure_category() {
  python3 - <<'PY'
import glob
import json

files = sorted(glob.glob("app/build/**/visual/naver-diagnostics/session.json", recursive=True))
if not files:
    print("")
else:
    with open(files[-1], encoding="utf-8") as handle:
        print(json.load(handle).get("failureCategory") or "")
PY
}

set +e
run_qa
status=$?
set -e

if [[ $status -eq 0 ]]; then
  exit 0
fi

category="$(latest_failure_category)"
case "$category" in
  provider_texture_insufficient|network_not_validated|ready_timeout|screenshot_unavailable)
    echo "NAVER emulator QA hit transient category '$category'; retrying once with a fresh managed-device run." >&2
    sleep 3
    ;;
  *)
    echo "NAVER emulator QA failed with non-retryable category '${category:-unknown}'." >&2
    exit "$status"
    ;;
esac

# The first managed-device invocation failed, so Gradle will execute it again rather than treating it
# as up-to-date. A single retry absorbs provider/emulator tile-startup variance without hiding auth,
# package, adapter, or deterministic visual failures.
run_qa
