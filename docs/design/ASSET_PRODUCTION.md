# Daily Town Asset Production Rules

This document defines the production and handoff rules for visual assets.

## 1. Core format rule

### Non-UI game art

Use raster-oriented production and delivery:

- PNG for transparent assets, master exports, and lossless needs
- WebP where Android delivery size benefits from it and quality remains acceptable
- layered raster source files outside the runtime repository when needed for editing
- raster sprite sheets / atlases / separated raster parts for animation when needed

### UI-only assets

SVG/vector production is allowed only for interface symbols and controls. In the Android app these may ultimately be represented as Android vector drawables or equivalent UI resources.

## 2. Prohibited fallback

Do not use SVG/vector as a shortcut for non-UI game art.

For characters, companions, backgrounds, buildings, props, collectibles, rewards, mystery art, or place illustrations:

- do not generate SVG as the first draft
- do not use SVG as an intermediate production format by default
- do not suggest converting an SVG into PNG as a way to satisfy the raster rule
- do not use Android VectorDrawable as a substitute for game artwork
- do not produce code-drawn geometric artwork as the final visual asset

If a non-UI asset needs revision, regenerate or edit it as raster game art.

## 3. Recommended asset families

### Characters / companions

Preferred:

- transparent PNG/WebP exports
- consistent canvas and anchor rules within a character family
- separated raster parts if future animation requires them
- optional Spine-compatible raster-part workflow if animation is adopted

Avoid:

- SVG puppet construction
- generic sticker-like character icons
- inconsistent proportions between expressions or poses

### Collectibles / rewards / mystery objects

Preferred:

- transparent raster asset
- clear silhouette
- consistent lighting direction
- sufficient master resolution for clean downscaling
- visually coherent category/rarity treatment

A practical master size for many square collectible assets is 512×512, with runtime variants generated as needed. This is a production default, not a hard gameplay requirement.

### Background / place / mystery scenes

Preferred:

- raster scene illustration
- cropped variants based on actual UI containers
- modular raster layers only when interaction or parallax requires them
- WebP delivery when file-size reduction is useful and image quality remains acceptable

### Buildings / props / decorative world art

Preferred:

- transparent raster assets for independently composited objects
- consistent perspective/camera angle within one family
- consistent contact-shadow convention

### UI icons

Allowed:

- SVG source
- Android VectorDrawable
- simple geometric/vector construction

UI vector rules must never be copied into game-art categories.

## 4. Asset naming

Use semantic names rather than visual guesses.

Suggested pattern:

`<category>_<subject>_<variant>_<state>.<ext>`

Examples:

- `companion_moru_default_idle.webp`
- `companion_moru_happy.webp`
- `collectible_letter_blue_rare.webp`
- `mystery_musicbox_gold.webp`
- `place_cafe_evening.webp`
- `ui_icon_collection.svg`

Do not encode temporary prompt numbers or AI-generation IDs into final runtime names.

## 5. Mobile readability

Before accepting an asset:

- test it at the actual display size
- verify silhouette recognition
- verify important facial/item details survive downscaling
- reduce micro-detail rather than increasing sharpness/noise
- ensure contrast against the intended container or map treatment

## 6. AI-assisted production rule

When an image-generation model is used for non-UI assets:

1. Request polished 2D raster game art explicitly.
2. Include Daily Town's art-direction constraints.
3. Explicitly prohibit SVG, vector, clip-art, corporate illustration, and flat icon aesthetics.
4. Generate transparent-background assets when the asset must be composited independently.
5. Review against `QUALITY_GATE.md` before accepting.
6. Regenerate or edit inconsistent assets instead of masking poor art with UI effects.

Do not ask an LLM to write SVG markup for a non-UI visual asset.

## 7. Runtime repository guidance

Keep runtime asset organization separate from design documentation. When asset volume grows, prefer clear category directories under Android resources/assets, for example character/companion, collectible, mystery, place/background, and UI groups.

Do not reorganize implementation directories solely to match this document without checking the current Android resource strategy and build constraints.

## 8. Asset-category precedence

When format rules conflict, classification wins:

- interface control/symbol → vector allowed
- game-world/story/character/collection/reward content → raster required

Where an asset appears on screen does not change its category.

## 9. Reference-image persistence

Design-review imagery must not exist only in chat, temporary sandbox storage, or an external design tool.

When a design task creates, accepts, or materially relies on a raster board/reference:

1. Commit an inspectable raster reference under `design/reference/` before treating the design task as repository-complete.
2. Mark the file explicitly as one of: `approved baseline`, `reference-only`, or `production/runtime`.
3. For approved baseline/master imagery, record the original dimensions and SHA-256 even when GitHub stores a smaller review-quality WebP derivative.
4. A reference derivative must never silently replace the full-resolution canonical/master source or be promoted to runtime art without the normal export and QA process.
5. Generated follow-up boards must preserve the locked design baseline; storing them in GitHub does not grant permission to reopen the visual direction.

Use `design/reference/REFERENCE_IMAGE_REGISTRY_V2.md` as the current registry for DailyTown baseline and derived raster review imagery.