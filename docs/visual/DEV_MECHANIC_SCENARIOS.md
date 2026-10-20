# Mechanic scenarios — art + investigation (v1)

Status: **runtime-bound for all 8 mechanics (2026-10-20).** TRACE_CHAIN keeps the approved Old Ginkgo
first scenario; the other 7 mechanics have their own place scene, clue item, keepsake item and
shared-memory crops, plus a mechanic-specific investigation interaction.

## Art

Generated with the game_analysis image pipeline (local ComfyUI + SDXL) using a DailyTown config that
targets the approved Old Ginkgo production v3 look (painterly anime background, ginkgo-yellow /
sage / cream). It is a derivation of the approved baseline, not a new style family.

| Mechanic | Place scene | Clue item | Keepsake item |
| --- | --- | --- | --- |
| TRACE_CHAIN | Old Ginkgo stone stairs (approved v3) | folded note | ginkgo leaf |
| SOUND_PATTERN | alley under shop eaves | wind chime | feather |
| TIME_LAYER | stone wall corner, old steps | weathered stone tablet | antique key |
| SYMBOL_MATCH | stepped alley with lanterns | leaf-symbol tile | ginkgo bookmark |
| LOST_OBJECT | bench under a big tree | knitted pouch | acorn |
| PHOTO_ANGLE | stair railing over rooftops | magnifier | map scrap |
| LOCAL_MEMORY | shop front covered in notes | pressed-flower card | dried flower bundle |
| COMPANION_SENSE | garden path with fence | sprout pot | sprout ribbon |

Reproduce / re-curate:

```bash
GA=../game_analysis/asset_pipeline
cd design/pipeline
python "$GA/scripts/refill.py" --config dailytown.config.json --manifest mechanic_manifest.csv
# edit mechanic_selection.v1.json after reviewing output/sheets and output/qc
cd ../..
python tools/visual/package_mechanic_scenarios.py
```

The packager writes reference masters + provenance to `design/reference/discovery/<mechanic>/`,
exact runtime bytes to `app/src/main/assets/scenario/<mechanic>/` (same 7 usages as Old Ginkgo), and
regenerates `GeneratedMechanicScenarioAssets.kt` with SHA-256 bindings. On-device tests verify every
packaged byte and dimension. Auto-QC false passes (a bird instead of a feather, twice) were rejected
during curation and recorded in `mechanic_selection.v1.json`.

## Investigation

`mystery/MechanicInteractions.kt` gives each mechanic two authored steps (one per extra clue):

- SEQUENCE: TRACE_CHAIN (follow the note's order), SOUND_PATTERN (repeat a rhythm, repeats allowed)
- CHOICE: TIME_LAYER, SYMBOL_MATCH, LOST_OBJECT, PHOTO_ANGLE, LOCAL_MEMORY, COMPANION_SENSE

At most 3 taps, no timing or precision, wrong answers only reset with a Moru hint, and display order is
shuffled deterministically per encounter. A solved step collects the next clue through the existing
idempotent reducer. QA mode keeps a "QA 단서 바로 수집" shortcut for field tests.

## Tests

- JVM: `MechanicInteractionsTest`, `ProductionScenarioAssetRegistryTest` (all mechanics × 7 usages).
- Instrumented: `MechanicInteractionSolver` plays interactions without knowing answers; used by
  `DailyTownReplaySmokeTest`, `PrototypeJourneyTest`, `ProductJourneyCaptureTest`;
  `OldGinkgoProductionVisualAssetBindingTest` checks all 56 packaged scenario files on device.
