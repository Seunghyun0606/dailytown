# Daily Town — Moru Production Export v2

> Status: **PRODUCTION DERIVATION SPEC READY — transparent masters/mobile QA pending**
>
> This document derives production rules from the approved Design Baseline v2. It does not introduce a new Moru design.

## 1. Baseline lock

Moru is fixed to the exact Candidate 3 visual baseline tracked by `docs/design/DESIGN_BASELINE_V2.md` and `design/reference/baseline-v2/manifest.json`.

Do not redraw, restyle, simplify into a different mascot family, or replace the approved silhouette/anatomy/costume. Any generated or edited production asset must visually resolve back to the same Moru.

The composite baseline is a reference/master board, not a runtime bitmap. Production assets are context-specific transparent raster exports derived from the same identity.

## 2. Production profile

Semantic profile: `companion.moru.canonical.v2`

Keep the previous production/fallback profile available until v2 passes export and mobile QA. Runtime integration must add v2 as a new version/profile instead of silently overwriting the existing pack.

### Semantic dimensions

- expressions: `neutral`, `happy`, `curious`, `surprised`, `clue_found`, `resolved`
- lighting: `LIGHT`, `WARM_DUSK`, `DARK`
- affinity: `base`, `familiar`, `trusted`, `best_friend`
- fallback: `neutral / LIGHT / base / static`

## 3. Usage-context exports

### `map_avatar`

- master canvas: 512×512 transparent PNG
- runtime review sizes: 48 / 56 / 64 dp
- anchor: bottom center
- required read: asymmetric sprout, hood contour, face/hood negative space, warm scarf accent, compact boot/base silhouette
- remove or soften micro-detail before increasing edge sharpness

### `hud_portrait`

- master canvas: 768×768 transparent PNG
- target presentation: 56 / 64 / 72 dp
- anchor: face center
- framing: head + upper torso
- sprout and scarf diagonal must survive crop

### `encounter_halfbody`

- master canvas: 1024×1280 transparent PNG
- framing: head through mid-thigh
- preserve expressive arms/hand pose and satchel strap
- designed for discovery sheet and focused encounter surface

### `result_large`

- master canvas: 1280×1600 transparent PNG
- full expressive pose
- never crop the sprout or boots
- contact shadow may be separate when the surface requires independent compositing

### `companion_portrait`

- master canvas: 1024×1280 transparent PNG
- portrait-biased framing for relationship notebook
- mood/expression can change, canonical hood/sprout/scarf/satchel language cannot

### `journal_crop`

- master canvas: 768×768 transparent PNG or lossless cropped master
- intended for paper artifact / memory-card composition
- crop can be tighter than Companion but must still read as the same Moru

## 4. Expression fit

All expressions preserve the approved anatomy and silhouette.

- `neutral`: relaxed mouth/eyes, stable upright posture
- `happy`: smiling eyes/mouth, small forward/open gesture; no exaggerated mascot deformation
- `curious`: focused gaze, slight head tilt/lean, investigative hand pose
- `surprised`: widened eyes, small mouth, limited recoil/lean; body envelope remains close to canonical
- `clue_found`: bright recognition, affirmative gesture, keepsake/clue interaction allowed
- `resolved`: warm settled satisfaction rather than victory-pose exaggeration

The baseline expression row is the visual reference when interpretation is ambiguous.

## 5. Lighting fit

- `LIGHT`: ordinary daytime walk; face and cream jacket remain naturally readable
- `WARM_DUSK`: warmer key/rim and richer ochre accents without orange color cast swallowing skin/hood separation
- `DARK`: darker environment but face/eyes remain readable; controlled warm local light/rim may be used

Lighting must never alter anatomy, costume layout, sprout count, or affinity state.

## 6. Affinity fit

Affinity uses AF-1 + restrained AF-3 / BF-B only.

- `base`: canonical outfit
- `familiar`: subtle stitching, tag or small shared-travel accent
- `trusted`: route charm / compass / richer satchel keepsake
- `best_friend`: restrained cluster of meaningful keepsakes or personal patching

Forbidden: species/anatomy change, body growth, crown/royal evolution, unrelated costume replacement, large magical aura that changes the character read.

## 7. File naming

Pattern:

`companion_moru_v2_<usage>_<expression>_<lighting>_<affinity>.<ext>`

Examples:

- `companion_moru_v2_map_avatar_neutral_LIGHT_base.webp`
- `companion_moru_v2_hud_portrait_curious_WARM_DUSK_familiar.webp`
- `companion_moru_v2_result_large_resolved_DARK_trusted.webp`

Master editing/export uses PNG; Android delivery may use WebP after quality comparison.

## 8. Reference-extraction rule

`design/reference/baseline-v2/extraction-map.v2.json` records pixel regions from the exact approved composite board. Those crops are **reference-only** and help production framing; they are not transparent runtime masters and must not be treated as completed exports.

A clean transparent master must preserve the approved artwork instead of repainting it into a new style.

## 9. QA gate before runtime promotion

Required before v2 is activated:

1. transparent edge/halo check against cream, dark and map-heavy surfaces
2. 48 dp / 56 dp / 64 dp silhouette review
3. expression family consistency
4. LIGHT / WARM_DUSK / DARK consistency
5. affinity progression without anatomy drift
6. each usage-context crop checked at its actual Android presentation size
7. semantic key/fallback resolver verification
8. previous production pack retained as rollback until v2 passes
9. outdoor readability remains a physical-device Human Gate
10. M-B final motion timing/intensity remains a separate Human Gate

## 10. Next design step

With the export contract fixed, the next production task is to create/obtain clean transparent Moru masters without changing the approved art, then run the mobile-size QA matrix. In parallel, Explore screen-detail parity can proceed because its composition and surface language are already locked by Design Baseline v2.
