# Daily Town — Moru Canonical v2

> Status: **APPROVED CHARACTER BASELINE — 2026-09-11**
>
> Scope: character art direction, silhouette/anatomy/gear, expression family, lighting family, affinity treatment and usage guidance. Runtime production replacement is still gated by export/crop/mobile QA.

## 1. Human Gate result

The user selected **Candidate 3** from the raster illustration comparison set as the Moru v2 canonical direction and subsequently confirmed the exact raster baseline tracked by `docs/design/DESIGN_BASELINE_V2.md`.

This supersedes the earlier MR-A/MR-B/MR-C vector-family selection problem as the current visual answer. Earlier SVG/vector Moru boards and later exploratory/generated composite boards remain historical/reference material only and must not be promoted over the approved baseline.

The selected direction restores the initial DailyTown intent: Moru should read as a **small everyday explorer who happens to carry a gentle botanical identity**, not as a plant monster, forest spirit blob, or generic flat app mascot.

Do not generate another Moru character direction unless the user explicitly reopens this decision.

## 2. Canonical visual read

Moru v2 should preserve these cues:

- small explorer proportion, approximately 2.1–2.3 heads tall
- clearly separated head, torso, arms and legs
- soft human-like child/explorer anatomy without becoming an ordinary human child
- oversized but wearable botanical hood framing the head
- asymmetric two-leaf sprout at the crown
- restrained flower/botanical accent
- warm mustard/ochre scarf
- cream field jacket / layered botanical cape treatment
- dark olive/brown shorts
- sturdy brown walking boots
- cross-body field satchel
- small explorer charm such as compass/magnifier/route keepsake where the usage context allows it
- muted sage / warm cream / earth-brown / ochre palette
- soft dimensional raster shading and light paper/storybook texture

The botanical identity should come mainly from hood, sprout, leaf layering, keepsakes and palette. Do not change Moru's species/anatomy as a progression reward.

## 3. Rendering language

Moru is non-UI game art and follows `ART_DIRECTION.md`, `ASSET_PRODUCTION.md`, `QUALITY_GATE.md`, and `DESIGN_BASELINE_V2.md`.

Required:

- polished 2D raster mobile-game illustration
- PNG master / WebP runtime derivatives where appropriate
- broad soft key light
- soft contact shadow / restrained ambient occlusion
- readable fabric, leather, leaf and metal cues
- consistent warm storybook rendering
- clean silhouette after downscaling

Forbidden as final game art:

- SVG character construction
- Android VectorDrawable body parts
- Compose Canvas character reconstruction
- geometric mascot approximation
- flat icon/clip-art treatment

## 4. Expression contract

Preserve the semantic expression family already used by DailyTown:

- `neutral`
- `happy`
- `curious`
- `surprised`
- `clue_found`
- `resolved`

Expression changes should use eyes, mouth, head tilt, lean, arm/hand pose, scarf/satchel secondary motion and small botanical accent movement while keeping the canonical silhouette and anatomy recognizable.

## 5. Lighting contract

Preserve the existing lighting families:

- `LIGHT`
- `WARM_DUSK`
- `DARK`

Moru's face and eyes must remain readable in every family. Preserve hood/face separation, scarf/satchel contrast, boot readability and a restrained rim/highlight in darker contexts.

Explore may map these families into broader presentation phases such as morning/midday/sunset/evening/night, but the provider map itself must not be recolored into a fantasy map.

## 6. Affinity contract

Affinity remains:

`base → familiar → trusted → best_friend`

Use the previously approved AF-1 + restrained AF-3 / BF-B direction.

Allowed progression cues:

- satchel keepsakes
- scarf patch or small stitching
- route charm
- small flower/leaf keepsake
- subtle patina and signs of shared travel
- slightly richer but restrained personal decoration

Not allowed:

- anatomy change
- species change
- major body growth
- crown/royal transformation
- visually unrelated costume replacement

## 7. Small-size readability

Moru must remain identifiable around a 48 dp presentation through:

1. asymmetric sprout
2. hood contour
3. face/hood negative space
4. scarf diagonal / warm accent
5. satchel diagonal or compact gear cue
6. sturdy boot/base silhouette

At very small sizes, simplify micro-detail rather than increasing sharpness or line noise.

## 8. Usage contexts

The canonical direction must support at least:

- map avatar / compact map presence
- compact HUD portrait
- discovery/encounter half-body
- result/completion large art
- Companion relationship portrait
- Records/journal stamp or memory crop

Do not use one identical crop for every context. Derive context-specific raster crops from the same canonical character identity.

## 9. Approved source reference

The exact approved Moru visual baseline is tracked in `design/reference/baseline-v2/manifest.json`.

Canonical semantic target:

- `design/reference/baseline-v2/moru_candidate3_canonical_baseline_v2.png`
- dimensions: `1122 × 1402`
- SHA-256: `541b53464bbc589d86323faa3a7dbc8790cf9ab9573e0dd66812986c4a430500`

The approved in-product context reference is:

- `design/reference/baseline-v2/dailytown_ux_visual_baseline_v2.png`
- dimensions: `1448 × 1086`
- SHA-256: `f0fb31e1ea4aec946bba356ff2d084cc6bdd8dd05119e1b350f547f382f4d515`

When any earlier generated sheet or wording conflicts with these exact references, the baseline references above win.

These are **design baseline artifacts**, not automatically runtime-ready production exports.

## 10. Runtime promotion gate

Before replacing the existing production Moru pack:

1. derive clean transparent-background crops/masters from the exact approved baseline
2. verify all six expressions against the canonical silhouette
3. verify LIGHT/WARM_DUSK/DARK variants
4. verify affinity decorations without anatomy drift
5. test actual Android target sizes including 48 dp
6. define semantic asset-key manifest/versioning without overwriting v1 silently
7. pass visual QA and fallback checks
8. only then promote the new raster family to runtime production

Until that gate passes, keep the existing semantic fallback/rollback pack available.

## 11. Next design work

With the visual baseline locked, design work proceeds without new concept-family generation:

- Moru transparent production extraction / crop spec
- Explore detail parity against the approved visual baseline
- discovery/place/background raster illustration family in the same locked style
- Companion relationship-notebook surface polish
- Records A3 journal surface polish
- M-B motion prototype using the same Moru design
- outdoor readability QA
- ID-A identity lock
