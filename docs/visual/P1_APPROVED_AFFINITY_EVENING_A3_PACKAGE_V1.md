# Daily Town P1 approved affinity / EVENING / A-3 package v1

Status: approved design-source handoff. No Android/Kotlin implementation.

## Approved choices

- Affinity: **AF-1 Memory Keepsakes + restrained AF-3 Explorer Patina**
- EVENING: **EV-1 Interpolation-first**
- A-3: five-screen component-fit contract using the existing reusable paper/sticker/card/stamp kit

## 1. Moru affinity production binding

Machine-readable source:

- `design/export-spec/affinity-moru-manifest.v1.json`

Stages remain:

```text
base -> familiar -> trusted -> best_friend
```

The visual progression represents shared history rather than power growth.

### Base

Canonical Moru A-2 appearance.

### Familiar

One small memory object is introduced, represented by the first-walk bag charm. The compact map avatar may omit it.

### Trusted

Two coordinated memory/patina slots may appear:

- stitched scarf memory patch
- shared-route bag tag

The map avatar uses a compact profile and may simplify the bag detail.

### Best Friend safe default

A coordinated but silhouette-safe profile may use:

- subtle pressed-leaf thread detail at the sprout
- travel-worn scarf patchwork
- memory tag set on the bag
- pressed-leaf keepsake accessory in larger contexts

This is deliberately a **safe default below the still-unresolved maximum affinity transformation ceiling**. It does not authorize anatomy, face, body-ratio, or major silhouette changes.

### Historical memory

A-3 Memory/Journal should prefer the appearance profile active when the recorded event occurred if data is available. Missing/deprecated cosmetic state falls back to canonical/base and must never break an old record.

## 2. EV-1 interpolation token system

Machine-readable source:

- `design/export-spec/evening-interpolation-tokens.v1.json`

EVENING does not create a new visual family. It bridges the existing SUNSET and NIGHT systems.

Debug/QA checkpoints:

```text
E0 = SUNSET anchor
E1 = 25%
E2 = 50% EVENING core
E3 = 75%
E4 = NIGHT anchor
```

### Route

Checkpoint route targets:

| Checkpoint | Route |
| --- | --- |
| E0 | `#E8794F` |
| E1 | `#CC8479` |
| E2 | `#B090A2` |
| E3 | `#949CCC` |
| E4 | `#78A7F6` |

These values are visual QA checkpoints, not domain/gameplay states.

### Marker family

No EVENING marker family is allowed.

- E0/E1 fallback: DAY
- E2: choose DAY/DARK from real map background luminance
- E3/E4 fallback: DARK
- preferred selector: background luminance rather than fixed clock time

All semantic marker keys and geographic anchors remain unchanged.

### Companion lighting

No EVENING lighting family is added.

- E0/E1: WARM_DUSK
- E2: WARM_DUSK + cool ambient modifier
- E3: DARK + restrained warm local modifier
- E4: DARK

Moru canonical colors and facial contrast must remain readable throughout.

### HUD / halo / discovery

- HUD moves continuously toward dark contrast; text role switches by contrast evaluation.
- Halo geometry/state is unchanged; only warm/cool atmosphere bias may interpolate.
- Discovery semantic intensity is unchanged; a restrained warm point may remain through E3.
- Reduced Motion does not disable the non-motion atmosphere interpolation.

E2 is the mandatory EV-1 real-map QA checkpoint.

## 3. A-3 five-screen component-fit

Machine-readable source:

- `design/export-spec/a3-five-screen-component-fit.v1.json`

Reference source master:

- `design/source/a3/a3-ui-source-master-v1.svg`

Baseline review width is 360 dp, with 412 dp and 600 dp review widths.

Shared constraints:

- 16 dp horizontal content padding
- 12 dp normal component gap
- 48 dp minimum touch target
- 48 dp companion-stamp review target
- A-3 paper never recolors with the current time of day
- event-time lighting may exist only inside contained hero/vignette artwork

### Journal Home

Single-column chronological composition. At least three consecutive entries should remain readable in a normal viewport. Companion stamp occupies a stable 48 dp slot and cannot force card-width variation between companions.

### Discovery Detail

4:3 hero area with a separate storybook page surface. The original event lighting may remain in the hero. Title supports up to three lines before expansion and must not collide with the companion stamp.

### Clue Note

Full-width clue card using unresolved/resolved structural grammar. The two states must remain distinct in grayscale; completion is not represented by color alone.

### Collection Grid

- 360/412 dp: 2 columns
- 600 dp: 3 columns

Locked state uses pattern/structure, not dimming alone.

### Memory Detail

4:3 hero with resolved stamp, companion profile and reflective copy. Event-time companion affinity profile is preferred; canonical/base is the stable fallback.

## 4. Development-session handoff

The development session should consume semantic contracts rather than filenames.

Required behaviors:

1. affinity appearance-profile resolver
2. compact map-avatar affinity profile support
3. historical appearance fallback for A-3 records
4. continuous EV-1 interpolation with forced `E0..E4` debug states
5. marker family selection capable of background-luminance decision at E2
6. no new EVENING marker or companion-lighting family
7. A-3 components remain time-of-day stable
8. screenshot QA at 360/412 dp for five A-3 screens
9. E2 inclusion in map overlay screenshot QA

## Remaining human gates

- final maximum Best Friend transformation ceiling beyond the safe profile
- Luca/Pino/Beri shipping selection
- final motion timing/easing/intensity after visible prototype
- physical outdoor readability acceptance
- final app icon/logo lock

These gates do not block implementation of the semantic contracts in this package.