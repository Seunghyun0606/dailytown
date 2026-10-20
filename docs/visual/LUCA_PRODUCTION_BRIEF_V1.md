# Daily Town Luca Production Brief v1

Status: shipping companion **approved for SC-B MVP set**. Production imagery still requires canonical-sheet QA before runtime promotion.

## Role

Luca is the first non-Moru shipping companion and exists to prove visible companion diversity while using the same semantic asset contract.

## Identity direction

- tall-ear explorer silhouette
- clearly non-botanical primary identity
- compact but more vertical than Moru
- friendly, observant, lightly adventurous
- body language should read as alert/curious rather than hyperactive

## Canonical lock targets

Must remain consistent across all outputs:

- tall-ear head silhouette
- face geometry and eye spacing
- ear attachment points and ear length class
- torso/limb proportion
- foot/hand scale
- neutral silhouette

## Accessory constraints

Ear-safe rule:

- no hats or head accessories that obscure the outer ear profile at compact size
- small ear-side charm only if it does not become the primary identity cue
- neck cloth, carried-item charm and body accent are preferred affinity slots

## Required semantic expressions

- neutral
- happy
- curious
- surprised
- clue_found
- resolved

Fallbacks should not be needed for Luca unless a specific pose fails compact QA.

## Lighting

Required:

- LIGHT
- WARM_DUSK
- DARK

Do not introduce Luca-specific time-of-day semantics.

## Usage contexts

Required:

- map_avatar
- hud_portrait
- encounter_halfbody
- result_large
- journal_stamp

## SC-B acceptance gates

Before production promotion:

- 48dp silhouette is clearly distinct from Moru
- no ear crop at supported context bounds
- six expressions remain readable
- DARK lighting preserves face/ear identity
- A-3 stamp remains recognisable at 32–64dp
- affinity slot resolver requires no Luca-specific runtime exception
- static/reduced-motion fallback exists

## Motion target

Use M-B Responsive Soft as the target personality.

Map-avatar amplitude should be lower than encounter/result contexts.

Initial Luca motion production should follow Moru pilot learnings rather than becoming a parallel blocker.

## Expansion note

Beri is the preferred next companion after Luca. Pino should undergo another silhouette-separation pass before shipping consideration.
