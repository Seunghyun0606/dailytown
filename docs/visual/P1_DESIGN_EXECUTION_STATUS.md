# Daily Town P1 Design Execution Status

Status date: 2026-08-26

## Approved P1 directions

- Affinity: **AF-1 Memory Keepsakes + restrained AF-3 Explorer Patina**
- EVENING: **EV-1 Interpolation-first**
- A-3: five-screen component-fit contract using the approved reusable source kit

## Completed in this pass

### Affinity manifest — COMPLETE AT APPROVED_SOURCE LEVEL

Added:

- `design/export-spec/affinity-moru-manifest.v1.json`

The manifest defines:

- `appearance.moru.base`
- `appearance.moru.familiar`
- `appearance.moru.trusted`
- `appearance.moru.best_friend`
- modular keepsake/patina slots
- compact map-avatar profiles
- historical-memory fallback
- animation/combinatorial-risk policy

`best_friend` is a safe default profile, not authorization for unlimited transformation. The final maximum transformation ceiling remains a human gate.

### EV-1 interpolation token spec — COMPLETE AT APPROVED_SOURCE LEVEL

Added:

- `design/export-spec/evening-interpolation-tokens.v1.json`

Checkpoints:

`E0 / E1 / E2 / E3 / E4`

Rules:

- no EVENING marker family
- no EVENING companion-lighting family
- route interpolates from SUNSET to NIGHT
- E2 is the mandatory evening-core QA state
- marker DAY/DARK selection may use real map background luminance at E2
- HUD text role switches by contrast evaluation rather than hard-coded clock time

### A-3 five-screen component-fit — COMPLETE AT APPROVED_SOURCE CONTRACT LEVEL

Added:

- `design/export-spec/a3-five-screen-component-fit.v1.json`

Validated screen contracts:

1. Journal Home
2. Discovery Detail
3. Clue Note
4. Collection Grid
5. Memory Detail

Baseline review width: 360 dp. Additional review widths: 412 dp / 600 dp.

No screen-specific one-off art is required by the contract. The same A-3 source kit composes all five screens.

### Integrated P1 package — COMPLETE

Added:

- `docs/visual/P1_APPROVED_AFFINITY_EVENING_A3_PACKAGE_V1.md`

Updated:

- `design/export-spec/p1-design-decisions.v1.json`
- `design/export-spec/visual-pack-manifest.v1.json` -> pack version `1.1.0`

## Development-session handoff now unblocked

The development session can proceed with:

1. affinity appearance-profile resolver
2. compact map-avatar profile handling
3. historical affinity fallback for A-3 records
4. EV-1 continuous interpolation plus forced E0..E4 debug checkpoints
5. E2 real-map readability capture
6. A-3 five-screen screenshot QA at narrow Android widths

No Android/Kotlin code was modified by this design session.

## Remaining production / QA work

- isolated Moru/affinity slot art exports and checksum promotion
- real marker export + real-map QA
- actual A-3 five-screen screenshot fit validation after integration
- sprite-gen Moru pilot output + human motion QA
- E2 + existing map-overlay matrix real Android capture
- physical outdoor readability approval

## Remaining human product/design gates

- Luca/Pino/Beri shipping selection
- maximum Best Friend transformation ceiling beyond the current safe default
- final motion timing/easing/intensity
- physical outdoor readability approval
- app icon / Daily Town logo final lock

## P1 conclusion

The requested **Affinity manifest + EV-1 interpolation token spec + A-3 five-screen component-fit** package is complete at the design-contract / approved-source level. The next highest-value work is production export/QA plus P1 follow-up art for affinity modular slots and final A-3 integrated-screen validation.