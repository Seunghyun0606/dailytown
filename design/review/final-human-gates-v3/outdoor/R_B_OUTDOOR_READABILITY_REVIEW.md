# R-B Outdoor Readability Human Gate Pack

Status: **REVIEW PACK READY / PHYSICAL PASS NOT CLAIMED**

## 1. Purpose

This pack prepares a repeatable human physical-device check for the accepted Moru v2 assets. It does not infer outdoor readability from emulator screenshots or this repository's static preflight.

The only valid final outcome is produced by a human looking at the real Android app on a real phone outdoors.

## 2. Required matrix

### Map avatar

- 48 dp
- 56 dp
- 64 dp

### HUD portrait

- 56 dp
- 64 dp
- 72 dp

### Lighting

- `LIGHT`
- `WARM_DUSK`
- `DARK`

### Background

- `cream`
- `dark`
- `map-heavy`

Total planned review cells: `54` (`27 map + 27 HUD`).

Machine-readable checklist: `r-b-outdoor-readability-matrix.v1.json`.

## 3. Physical review conditions

Use the actual device/runtime build selected by Development. Record device model, Android version, display brightness mode, approximate brightness, weather/light condition, and whether sunglasses are used.

Minimum recommended conditions:

1. open shade / bright overcast;
2. direct or near-direct daylight where the screen remains realistically usable;
3. evening/low-light for DARK family sanity.

Do not force impossible visibility under conditions where the provider map and ordinary system UI are themselves unreadable. The gate is comparative product readability, not display-panel certification.

## 4. Per-cell checklist

For every matrix cell, mark each criterion `PASS`, `FAIL`, or `REVIEW`.

### A. Sprout silhouette

PASS when the asymmetric two-leaf crown is recognized at a glance and does not merge into the hood/background.

### B. Face readability

PASS when eyes/face opening remain perceivable for the intended usage. HUD has a higher face requirement than map avatar.

### C. Scarf diagonal

PASS when the warm diagonal cue remains visually distinct enough to support Moru identity.

### D. Costume identity

PASS when hood + cream botanical field-jacket/cape + satchel mass still read as the same Candidate 3 family rather than a generic green mascot.

### E. Boots/base

PASS when the lower-body/base remains stable enough to anchor the map avatar. HUD may crop boots by design; in that case mark `N/A` only if the accepted HUD crop does not contain them.

### F. Alpha halo

PASS when no pale/dark fringe, matte rectangle, dirty transparent border, or map-colored edge is visible around the raster.

### G. Foreground/background separation

PASS when the character does not visually merge into the current surface and does not require a non-approved heavy glow/outline to remain visible.

### H. Glance readability

PASS when the reviewer can identify `Moru` and the broad state/presence with a short glance rather than inspection/zoom.

## 5. PASS / REVIEW / FAIL rule

### Cell PASS

- all applicable criteria PASS;
- no alpha halo;
- no identity-critical cue disappears;
- map/HUD remains readable without prolonged inspection.

### Cell REVIEW

Use only when:

- the issue is borderline or depends strongly on ambient condition;
- one non-critical cue weakens but identity/glance read remains intact;
- another physical capture is required before deciding.

A REVIEW cell keeps the Human Gate open.

### Cell FAIL

Any of these is sufficient:

- sprout collapses/merges;
- HUD face cannot be read at intended size;
- map avatar loses Candidate 3 identity;
- obvious alpha fringe/matte appears;
- foreground/background separation fails in a representative surface;
- reviewer repeatedly needs close inspection rather than a glance.

## 6. Overall R-B verdict

`PASS_RB_PHYSICAL_DEVICE` requires:

- all six target sizes checked;
- all three lighting families represented;
- cream, dark, and actual map-heavy contexts checked;
- no FAIL cells;
- REVIEW cells resolved with a second physical observation;
- at least one evidence screenshot/photo per failed/reviewed condition and representative PASS condition.

Do not convert a static preflight or emulator pass into `PASS_RB_PHYSICAL_DEVICE`.

## 7. Evidence naming

Use:

`rb_<usage>_<size>dp_<lighting>_<background>_<pass-review-fail>.<ext>`

Examples:

- `rb_map_48dp_LIGHT_map-heavy_pass.png`
- `rb_hud_56dp_DARK_dark_review.jpg`

Add one session note with device/ambient details and final reviewer decision.

## 8. Static preflight

`outdoor-preflight.html` provides a design-only comparison using accepted asset files and cream/dark/reference map-heavy surfaces. It helps catch obvious contrast/crop issues before going outside.

It cannot reproduce:

- display luminance/reflections;
- device density/scaling implementation;
- actual provider-map labels/POIs at a live location;
- sunlight/sunglasses/angle;
- runtime compositing/elevation differences.

Therefore it is explicitly **not a PASS artifact**.
