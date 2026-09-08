#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: bash tools/field_test/review_physical_export.sh <field-test-export.json>" >&2
  exit 2
fi

INPUT="$1"
if [[ ! -f "$INPUT" ]]; then
  echo "Field-test export not found: $INPUT" >&2
  exit 3
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INPUT_DIR="$(cd "$(dirname "$INPUT")" && pwd)"
INPUT_NAME="$(basename "$INPUT")"
BASE_NAME="${INPUT_NAME%.*}"
REPORT="$INPUT_DIR/${BASE_NAME}.review.md"
PLAN="$INPUT_DIR/${BASE_NAME}.collection-plan.txt"

resolve_python() {
  local candidate

  # Keep the final review path compatible with the same Windows Git Bash setups
  # accepted by prepare_physical_test.sh. App Execution Alias shims can exist on
  # PATH as python3/python while failing to execute real Python code.
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

resolve_python

"${PYTHON_CMD[@]}" "$SCRIPT_DIR/validate_export.py" "$INPUT"
"${PYTHON_CMD[@]}" "$SCRIPT_DIR/review_report.py" "$INPUT" --format markdown > "$REPORT"
"${PYTHON_CMD[@]}" "$SCRIPT_DIR/collection_plan.py" "$INPUT" > "$PLAN"

printf '%s\n' \
  "Field-test export validated." \
  "Review report: $REPORT" \
  "Collection plan: $PLAN" \
  "Keep these files outside the Git repository and delete session-derived files within the approved retention window."
