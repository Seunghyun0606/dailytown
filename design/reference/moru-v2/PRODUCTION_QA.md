# Moru v2 production QA

Status: Candidate 3 character direction approved; runtime promotion pending.

This checklist turns the approved Moru v2 direction into reviewable production derivatives without reopening character design.

## 1. Identity lock

Every derivative must preserve:

- approximately 2.1–2.3 head proportion
- wearable botanical hood with asymmetric two-leaf sprout
- warm mustard/ochre scarf
- cream field jacket / layered botanical cape
- dark olive-brown shorts
- brown walking boots
- cross-body field satchel
- soft human-like small explorer anatomy
- muted sage / cream / earth / ochre palette

Fail if the derivative reads as a plant monster, forest spirit blob, generic child, flat vector mascot, or a different costume family.

## 2. Expression fit

Required semantic expressions:

- neutral
- happy
- curious
- surprised
- clue_found
- resolved

Expressions may change eyes, mouth, head tilt, lean, hands/arms, scarf/satchel secondary pose, and small botanical movement. Core anatomy and recognizable silhouette must remain stable.

## 3. Lighting fit

Required families:

- LIGHT
- WARM_DUSK
- DARK

All variants must preserve face/eye readability, hood-to-face separation, scarf/satchel contrast, and boot/base readability. DARK may use a restrained rim/highlight but must not become high-contrast cinematic art that breaks the cozy family.

## 4. Affinity fit

Required stages:

- base
- familiar
- trusted
- best_friend

Use keepsakes, scarf stitching/patches, route charms, satchel decoration, subtle wear/patina and small botanical keepsakes. Do not change anatomy, species, body scale, crown/head silhouette, or the base outfit family.

## 5. Usage-context exports

The same canonical identity must support separate raster crops for:

| Context | Intended read | Minimum QA |
| --- | --- | --- |
| map_avatar | instant silhouette cue | recognizable at ~48 dp |
| hud_portrait | face + mood | eyes/mouth readable |
| encounter_halfbody | reaction + gear | hands/pose readable |
| result_large | emotional reward | full material/shading quality |
| companion_portrait | relationship focus | calm expressive face |
| journal_crop | memory/stamp support | survives paper surface/crop |

Do not reuse one identical crop for all contexts.

## 6. 48 dp gate

At small display size, Moru must still read through:

1. sprout
2. hood contour
3. face/hood negative space
4. warm scarf accent
5. satchel/gear diagonal
6. sturdy boot/base cue

Simplify micro-detail before increasing edge sharpness/noise.

## 7. File / format gate

- game art remains raster PNG/WebP
- transparent-background export where compositing is required
- no SVG, VectorDrawable, or Compose Canvas reconstruction
- use semantic names rather than generation IDs
- preserve a versioned manifest; never silently overwrite the v1 fallback family
- record dimensions and SHA-256 for approved reference/export artifacts

## 8. Runtime-promotion evidence

Before runtime replacement, provide evidence for:

- six expression crops
- three lighting families
- four affinity stages or explicit staged rollout plan
- 48 dp / HUD / half-body / result / companion / journal checks
- fallback resolution when a variant is absent
- reduced-motion/static semantic pose behavior
- Android map/HUD contrast check
- physical-device outdoor readability remains a separate Human Gate

## 9. Current production-fit board

`moru-candidate3-production-fit-board.webp` is a repository review artifact that demonstrates the locked Candidate 3 identity across front/side/back, expressions, usage contexts and UX placement. It is not a runtime sprite atlas and must not be cropped blindly into production assets.
