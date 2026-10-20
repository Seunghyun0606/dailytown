# Moru Canonical Rebaseline v2

Status: candidate brief. No direction in this document is approved production art yet.

## Problem statement

Moru v1 successfully established semantic expressions, three lighting families, affinity slots and runtime binding, but its canonical anatomy became too mascot-like: one large oval body with leaves/scarf/bag layered on top. The silhouette no longer communicates **small botanical explorer** strongly enough.

The v2 goal is to restore the original role without losing the production contracts already built around Moru.

## Shared locks for every candidate

- compact 2.0–2.3 head-unit proportion;
- clearly separate head and torso masses;
- visible short arms/hands and feet in portrait views;
- soft rounded geometry, never sharp heroic anatomy;
- one asymmetric botanical feature;
- explorer equipment participates in silhouette;
- face stays simple enough for `neutral / happy / curious / surprised / clue_found / resolved`;
- same canonical identity across LIGHT/WARM_DUSK/DARK;
- 48dp compact derivative may simplify hands/feet/equipment but must preserve head silhouette + scarf/strap cue;
- affinity is additive keepsake/patina only.

## Candidate MR-A — Sprout Scout

Recommended starting point because it is closest to the original **Soft Botanical Explorer** wording.

### Silhouette

- large soft hood/head shape with a single two-leaf sprout leaning to one side;
- small pear-shaped torso under a short explorer cape/scarf;
- cross-body satchel creates a clear diagonal line;
- short boots widen the base slightly;
- hands sit outside the torso silhouette when reacting.

### Personality

Gentle, observant, slightly shy, but visibly prepared to walk somewhere.

### Botanical strength

Medium. Botanical identity comes mainly from hood/sprout and color/material language.

### Explorer strength

High. Strap, bag, scarf/cape and boots are readable before small decorative details.

### Risk

If the hood becomes too large, it can drift toward generic forest-spirit imagery. Keep the face opening broad and equipment visible.

## Candidate MR-B — Leafcap Wanderer

### Silhouette

- wider leaf-cap/hood creates a mushroom-like top silhouette without literally becoming a mushroom;
- narrow torso and short cape underneath;
- compact backpack visible from 3/4 pose;
- round walking shoes and mitten hands.

### Personality

More whimsical and curious; stronger storybook flavor.

### Botanical strength

High.

### Explorer strength

Medium-high.

### Risk

May become too fantasy-creature-like and less like a companion who belongs in an ordinary Korean neighborhood.

## Candidate MR-C — Pocket Gardener

### Silhouette

- most humanoid of the three;
- rounded face under a small sprout cap;
- short field coat/tunic, scarf and side pouch;
- clear arms/legs, slightly larger shoes;
- one leaf charm or clipped specimen rather than a huge botanical crown.

### Personality

Friendly neighborhood companion; calm, practical, dependable.

### Botanical strength

Low-medium.

### Explorer strength

Highest.

### Risk

Can lose the mascot uniqueness if clothing detail becomes ordinary. Preserve an unmistakable sprout/head contour and simple face.

## Expression fit

All three candidates must preserve the existing semantic expression set.

- `neutral`: quiet open eyes, small resting smile/line;
- `happy`: eye arc + open smile, cheek cue secondary;
- `curious`: asymmetric eyebrow/eye + slight head tilt/question cue;
- `surprised`: expanded eye shape + small open mouth;
- `clue_found`: focused eyes + handheld/near-face clue cue, not a permanent face ornament;
- `resolved`: relieved closed/soft eyes + completion sparkle/seal nearby.

Expression props must not change the canonical head silhouette permanently.

## Pose language

The canonical is no longer a single front-facing blob.

Required source poses after direction approval:

1. front neutral;
2. 3/4 walking-ready;
3. side-ish map/HUD compact profile;
4. looking-up curious;
5. clue inspect;
6. settled/happy.

Motion can interpolate or deform those later, but the static poses must already communicate life.

## 48dp policy

Compact derivative keeps only:

- head/hood/sprout outer contour;
- face dots/line at sufficient contrast;
- scarf/cape block;
- one bag/strap or backpack cue;
- feet/base mass.

Remove tiny keepsakes, stitching, patches and secondary straps first.

## What happens to v1

Do not delete or overwrite:

- `moru-a2-vector-source-master-v1.svg`;
- BF-B affinity boards;
- expression/lighting semantic keys;
- existing runtime registry.

They remain a legacy reference and rollback path until v2 is explicitly approved and exported.

## Human review question

Do not ask for dozens of details. The first human decision should be only:

> Which base silhouette feels most like the Daily Town companion we originally wanted: MR-A Sprout Scout, MR-B Leafcap Wanderer, or MR-C Pocket Gardener?

After that choice, refine face proportion, gear amount and botanical intensity within the selected family rather than branching again.