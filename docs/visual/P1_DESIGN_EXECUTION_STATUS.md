# Daily Town P1 Design Execution Status

Status date: 2026-08-26

## Approved P1 directions

- Affinity: **AF-1 Memory Keepsakes + restrained AF-3 Explorer Patina**
- EVENING: **EV-1 Interpolation-first**
- A-3: five-screen component-fit contract using the approved reusable source kit

## Completed P1 design-contract work

### Affinity manifest — COMPLETE AT APPROVED_SOURCE LEVEL

- `design/export-spec/affinity-moru-manifest.v1.json`
- semantic profiles: `base / familiar / trusted / best_friend`
- compact map-avatar profile handling
- historical-memory fallback
- motion/combinatorial-risk policy

### EV-1 interpolation tokens — COMPLETE AT APPROVED_SOURCE LEVEL

- `design/export-spec/evening-interpolation-tokens.v1.json`
- checkpoints: `E0 / E1 / E2 / E3 / E4`
- no new EVENING marker or companion-lighting family
- E2 remains mandatory evening-core real-map QA state

### A-3 five-screen component-fit contract — COMPLETE

- `design/export-spec/a3-five-screen-component-fit.v1.json`
- Journal Home / Discovery Detail / Clue Note / Collection Grid / Memory Detail
- baseline 360dp, additional 412dp / 600dp review widths

## Completed P1 source-production follow-up

### Moru affinity modular slot source master — COMPLETE AT APPROVED_SOURCE LEVEL

Added:

- `design/source/companion/moru/moru-affinity-slots-source-master-v1.svg`
- `design/export-spec/affinity-slot-export-jobs.v1.json`

Source slots now exist for:

- `slot.moru.bag.charm_first_walk`
- `slot.moru.scarf.stitched_memory_patch`
- `slot.moru.bag.shared_route_tag`
- `slot.moru.sprout.pressed_leaf_thread`
- `slot.moru.scarf.travel_worn_patchwork`
- `slot.moru.bag.memory_tag_set`
- `slot.moru.accessory.pressed_leaf_keepsake`

The source respects the currently approved safe Best Friend profile and does not authorize larger silhouette/anatomy changes.

### A-3 integrated five-screen fit board — COMPLETE AT APPROVED_SOURCE LEVEL

Added:

- `design/source/a3/a3-five-screen-fit-board-v1.svg`
- `design/export-spec/a3-integrated-screen-qa.v1.json`

The board demonstrates that the same A-3 primitives compose all five screens without one-off screen-specific art. Actual Android screenshot validation remains an integration QA gate rather than a missing design decision.

### Human-decision candidates — COMPLETE

Added:

- `docs/visual/HUMAN_DECISION_CANDIDATES_V1.md`

Candidate groups:

- Best Friend ceiling: `BF-A / BF-B / BF-C`
- Shipping set: `SC-A / SC-B / SC-C / SC-D`
- Motion personality: `M-A / M-B / M-C`
- Outdoor QA policy: `R-A / R-B / R-C`
- App identity: `ID-A / ID-B / ID-C`

Design-system recommended first-release bundle:

- **BF-B Signature Explorer**
- **SC-B Moru + Luca**
- **M-B Responsive Soft**
- **R-A Safety-first Strict**
- **ID-A Sprout Town Mark**

No imagery for the not-yet-selected app identity directions has been produced.

## Visual pack state

`design/export-spec/visual-pack-manifest.v1.json` is now pack version `1.2.0` and includes the affinity slot source, A-3 integrated fit board and their QA/export contracts.

## Development-session handoff currently unblocked

The development session can proceed with:

1. affinity appearance-profile resolver and modular slot binding
2. compact map-avatar profile handling
3. historical affinity fallback for A-3 records
4. EV-1 continuous interpolation plus forced E0..E4 debug checkpoints
5. E2 real-map readability capture
6. A-3 five-screen screenshot QA at 360/412/600dp targets
7. sprite-atlas playback adapter independent of sprite-gen authoring tooling

No Android/Kotlin code was modified by this design session.

## Remaining production / QA work

- isolated Moru static exports + visual QA/checksum
- isolated affinity slot exports + 48dp QA/checksum
- marker split exports + real-map QA
- A-3 Android integrated screenshots and fit acceptance
- sprite-gen Moru pilot output + human motion QA
- E2 + existing map-overlay matrix Android capture
- physical outdoor readability final acceptance

## Remaining human choices

1. Best Friend maximum visual-change ceiling (`BF-A/B/C`)
2. shipping companion set (`SC-A/B/C/D`)
3. motion personality target before final timing/easing tuning (`M-A/B/C`)
4. outdoor readability acceptance policy (`R-A/B/C`)
5. app icon/logo identity direction (`ID-A/B/C`)

See `docs/visual/HUMAN_DECISION_CANDIDATES_V1.md` for trade-offs and recommendations.

## P1 conclusion

All design work that can safely proceed under the approved Affinity/EV-1/A-3 directions has been advanced to **approved-source + export/QA contract** level. The next visual image work should follow explicit human selection for the remaining candidate groups, while production promotion/Android QA proceeds independently where possible.
