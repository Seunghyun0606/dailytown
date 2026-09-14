# Daily Town · Moru v2 Fit QA Candidate

Status: `candidate_review`. This document does **not** approve a canonical family, overwrite v1 production assets, or modify runtime behavior.

## Recommendation before Human Gate

Use **MR-A · Sprout Scout** as the recommended canonical v2 family for human review. It best balances botanical identity, explorer role, ordinary-neighborhood grounding and 48dp recognition. MR-B remains the storybook/fantasy alternative; MR-C remains the more humanoid/explorer alternative.

## 48dp silhouette acceptance

A simplified 48dp Moru derivative passes only when all four cues survive without reading accessory detail:

1. asymmetric sprout + head/hood outer contour;
2. visually separate head and torso masses;
3. scarf/cape or cross-body diagonal explorer cue;
4. feet/boot base wide enough to read as a prepared walker rather than an oval mascot.

At 48dp, remove stitching, small patches, secondary straps, tiny keepsakes and finger detail before changing the four locked cues. The satchel may collapse into one solid side mass.

Fail when the character reads primarily as a plant blob, forest spirit, generic child, or one large oval with props.

## Six-expression fit

| Expression | Face | Head | Arms / body | Accessory reaction | Recognition guardrail |
|---|---|---|---|---|---|
| `neutral` | open quiet eyes, resting mouth | near upright | relaxed arms, balanced stance | scarf/satchel settled | baseline canonical |
| `happy` | soft eye arc, open/raised smile | +1–2° lift | chest opens, hands slightly away | scarf tip lifts once | no permanent head-shape change |
| `curious` | asymmetric eye/brow emphasis | 5–7° tilt | 3–4% forward lean, one hand may rise | satchel follows lean | sprout remains same attachment/size |
| `surprised` | wider eyes, small open mouth | 2–4° recoil | torso shifts back up to 4%, arms separate | scarf/satchel lag | pose peak may expand silhouette, identity unchanged |
| `clue_found` | focused/open eyes, compact smile/open cue | 4–6° toward clue | forward lean, one hand/arm indicates clue | bag swing 8–12° or clue prop near hand | clue is temporary prop, not face ornament |
| `resolved` | softened eyes, relieved smile | settles toward upright | shoulders drop 2–3%, centered stance | scarf settles, completion seal external | no new anatomy or crown |

Static expression poses should stay inside the canonical silhouette envelope within approximately ±8%, excluding brief motion peaks such as a single bounce or recoil.

## Lighting compatibility

### `LIGHT`

- Base material/color values remain canonical.
- Face is the highest-priority readable region after silhouette.
- Sprout/head framing must not merge into green map vegetation.
- Bag/scarf maintain value separation from torso.

### `WARM_DUSK`

- Warm highlight may enter face edge, scarf and upper gear.
- Do not apply one global orange tint.
- Preserve neutral-dark eyes and a stable face opening.
- Satchel and scarf need distinct lightness or edge separation even when hue converges.

### `DARK`

- Face remains warm/readable rather than blue-tinted.
- Add restrained cream/neutral rim or highlight on sprout + hood/head frame.
- Eyes stay near-black with a small readable catch/highlight where needed.
- Critical outline stays visible against night HUD/map surroundings.
- Scarf and bag separate from torso by value/edge, not hue alone.

Lighting must not modify anatomy, sprout placement or expression semantics.

## Explore time interpolation

Preserve the approved EV-1 evening direction. Provider map readability stays primary.

| Phase | Dominant treatment |
|---|---|
| `MORNING` | fresh botanical HUD, cool-soft ambient edge, LIGHT Moru |
| `MIDDAY` | most neutral/high-legibility route + HUD treatment, LIGHT Moru |
| `SUNSET` | warm route/halo accents, WARM_DUSK Moru |
| `EVENING` | EV-1 transition: warm residue + deepening neutral chrome; no fantasy-map filter |
| `NIGHT` | dark surrounding chrome/halo discipline, DARK Moru; provider labels remain readable |

Interpolation applies primarily to HUD, route, halo, companion lighting and surrounding UI. It must not aggressively recolor the NAVER map provider surface.

## Affinity compatibility · AF-1 + restrained AF-3

Canonical anatomy remains invariant across all stages.

- `base`: clean canonical explorer kit.
- `familiar`: one small route charm or subtle carried keepsake.
- `trusted`: restrained scarf patch, bag mark or small explorer patina; may retain familiar keepsake.
- `best_friend`: BF-B remains the production direction: a curated combination of keepsake + scarf/bag detail + subtle botanical maturation/patina, while the same silhouette remains immediately recognizable.

Allowed: keepsake, scarf patch, route charm, bag decoration, subtle botanical growth inside the existing sprout silhouette envelope, light explorer patina.

Forbidden before separate variant approval: taller/larger anatomy, new head family, major sprout crown, wing/cape transformation, species change, large clothing silhouette replacement.

BF-C remains a future user-selectable character variation and is not a production v2 affinity stage.

## Motion candidate · M-B / calm reactive

This is a design timing candidate for Human Gate, not a runtime commitment.

- `idle_breathe`: 2400ms total loop, torso scale peak ~1.015, sprout follow lag ~80ms, ease-in-out.
- `clue_react`: 480ms, head tilt up to 6°, body lean ~4%, bag swing ~10°, soft one-time overshoot.
- `resolved_settle`: 640ms, body/shoulder settle 2–3%, scarf follow lag ~70ms, ease-out.
- `investigate`: ~580ms follow-up candidate; one lean/hand cue.
- `happy_bounce`: ~520ms follow-up candidate; vertical travel ~5%, squash <=3%, one bounce only.
- `walk`: experimental until outdoor silhouette/readability QA.

Reduced motion: replace repeated/large movement with the static semantic pose plus opacity/halo transition <=160ms. Never require bounce or motion to understand gameplay state.

## Source / production boundary

- Existing v1 Moru source masters and production fallback remain untouched.
- Candidate rebaseline source: `design/source/companion/moru/moru-canonical-rebaseline-v2-candidates.svg`.
- No v2 source is promoted to `approved_source` or `production_export` until Human Gate selects one family and 48dp + six-expression + lighting compatibility pass.
