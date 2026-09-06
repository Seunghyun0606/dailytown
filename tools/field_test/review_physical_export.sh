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

python3 "$SCRIPT_DIR/validate_export.py" "$INPUT"
python3 "$SCRIPT_DIR/review_report.py" "$INPUT" --format markdown > "$REPORT"
python3 "$SCRIPT_DIR/collection_plan.py" "$INPUT" > "$PLAN"

printf '%s\n' \
  "Field-test export validated." \
  "Review report: $REPORT" \
  "Collection plan: $PLAN" \
  "Keep these files outside the Git repository and delete session-derived files within the approved retention window."
