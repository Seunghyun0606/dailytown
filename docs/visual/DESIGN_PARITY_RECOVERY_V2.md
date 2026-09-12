# Daily Town Design Parity Recovery v2

Status: design-only recovery plan. This document does not modify Android/Kotlin runtime behavior and does not promote any candidate asset to `production_export`.

## Why this recovery exists

The repository contains a substantial visual system, but the shipped MVP shell still reads primarily as generic Material 3 UI with selective A-3 paper treatment. The visual source-of-truth and the product surface have drifted apart.

Moru has a second drift: the current canonical and BF-B boards preserve semantic slots, expressions, lighting, affinity and runtime binding well, but the anatomy collapsed into a large oval mascot body with decorative explorer props. That no longer carries the original **Soft Botanical Explorer / small explorer** identity strongly enough.

This recovery treats those as two separate problems:

1. **Product parity** — make A1/A2/A3 art direction visible in the actual five-tab product hierarchy.
2. **Character parity** — re-baseline Moru anatomy/silhouette before creating more motion or affinity derivatives.

## Non-negotiable direction

Daily Town should feel like **a real neighborhood becoming a gentle storybook expedition**, not a game skin placed on top of a map.

The hierarchy is:

- real map = navigation truth;
- botanical explorer layer = discovery/companion emotion;
- paper/storybook layer = memory/record truth;
- Material primitives = implementation mechanism, not the visible art direction.

## Product visual grammar v2

### 1. Exploration surfaces — A1/A2 live-world family

Use map-first composition.

- The NAVER map remains visually dominant.
- Top chrome becomes a compact expedition header instead of a generic app bar.
- Current objective is a translucent botanical ribbon/card with a visible icon and short copy.
- Moru is a map-edge companion vignette, never a large centered card while actively walking.
- Discovery states use halo, route stroke, marker shape, icon and label weight — never color alone.
- Time-of-day atmosphere enters through route/halo/HUD tint and companion lighting, not an opaque filter over the provider map.

### 2. Companion — field notebook portrait

The companion tab is not a generic statistics dashboard.

- Moru occupies the dominant upper portrait region.
- Bond/memory state is integrated into a field-notebook caption strip.
- Relationship keepsakes appear as small pinned artifacts, not dashboard badges.
- The lower half contains `walked together`, `shared discoveries`, `recent memory` as paper strips with one dominant memory card.

### 3. Records — A3 is the native surface, not decoration

A-3 remains the stable record language.

- Journal Home becomes the default Records landing screen.
- Discovery Detail, Clue Note, Collection Grid and Memory Detail remain nested record artifacts.
- The full content viewport may be paper-toned, while product navigation stays separate.
- Cards should resemble layered pages, clipped notes, stamps and collection mounts rather than generic elevated rectangles.

### 4. Goals — expedition checklist

Goals use a compact expedition-board metaphor.

- Daily goal = today card pinned at top.
- Weekly goals = folded route/checklist rows.
- Completion combines checkbox/stamp, weight and text; no color-only completion.
- Rewards are framed as exploration momentum, not currency dashboard modules.

### 5. Settings — utility remains quiet

Settings intentionally uses the least illustration.

- Warm neutral background and botanical section headers are enough.
- Debug/Field Test remains visually isolated and utilitarian.
- Provider attribution and privacy/legal text must remain plain and unambiguous.

## Shell parity requirements

The five primary destinations should share:

- 16–20dp horizontal page edge;
- 8/12/16/24 spacing rhythm;
- 16–22dp dominant corner radius family;
- botanical ink for headings, dark neutral ink for body text;
- one primary illustration/artifact region per screen;
- restrained shadows with warm paper edges instead of generic tonal elevation;
- semantic icons/stamps for every gameplay state;
- static and reduced-motion fallbacks.

Navigation should feel quieter than content. Selected destination may use a soft leaf-shaped/rounded indicator, but emoji/text symbols are not the final icon language.

## Moru canonical recovery

The current oval-body anatomy is frozen as **legacy v1**, not deleted.

Canonical v2 candidates must satisfy all of the following:

- 2.0–2.3 head-unit compact explorer proportion;
- readable head, torso, hands/arms and feet at portrait size;
- recognizable silhouette at 48dp after simplification;
- botanical identity is integrated into head/hood/sprout silhouette rather than pasted decoration;
- explorer identity is carried by scarf/cape, cross-body satchel/backpack, boots or walking accessory;
- one intentional asymmetry survives all lighting variants;
- facial geometry supports the six existing semantic expressions;
- affinity layers modify keepsakes/patina, not canonical anatomy;
- LIGHT/WARM_DUSK/DARK remains a separate selector;
- no motion authoring until v2 anatomy is approved.

See `MORU_CANONICAL_REBASELINE_V2.md` and `design/source/companion/moru/moru-canonical-rebaseline-v2-candidates.svg`.

## Candidate product board

`design/source/product/dailytown-five-tab-parity-v2-board.svg` is a design-review board, not a runtime screenshot. It exists to make the desired composition/hierarchy reviewable before Kotlin implementation.

## Tooling recommendation

### Static/vector source

Preferred: Figma plugin when connected. It provides a practical design-to-code/design-system handoff surface for this project.

Open alternative: Penpot MCP. Penpot is suitable when direct MCP-driven creation/maintenance of components, styles, tokens and pages is preferred.

### Character motion

Do not solve Moru's anatomy problem with an animation runtime. First lock the static canonical v2.

After approval:

- simple UI/halo/route transitions: Compose procedural animation;
- authored mascot loops with vector deformation/state-machine needs: evaluate Rive;
- After Effects/Bodymovin asset pipeline: Lottie Compose remains a viable fallback;
- legacy `sprite-gen` pilot remains optional for raster atlas experimentation, not the canonical source.

No new runtime dependency is adopted by this design document.

## Human gates

Before any v2 production promotion:

1. choose one Moru canonical v2 direction;
2. approve how botanical vs. humanoid the final silhouette should feel;
3. approve one five-tab parity board after comparing with the actual app;
4. provide/restore the original A0–A3/Moru concept images when available so drift can be checked visually;
5. connect Figma or Penpot if editable collaborative design-file ownership is desired;
6. only after parity is restored resume M-B motion final approval and outdoor visual final approval.

## Agent-owned next work

Without another product decision, design work can continue on:

- candidate token expansion (type/shape/spacing/iconography);
- screen composition board refinement;
- Moru expression-fit overlays for each candidate;
- 48dp silhouette QA board;
- A1/A2/A3 cross-surface component contract;
- development handoff diff listing which Compose surfaces must be replaced/retained.

Runtime code changes remain a separate human-gated implementation step.