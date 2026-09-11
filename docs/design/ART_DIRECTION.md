# Daily Town Art Direction

## 1. Visual identity

Daily Town should feel:

- cozy, warm, gentle, and emotionally inviting
- polished enough to feel like a commercial mobile game rather than a prototype
- cute and collectible without becoming childish clip-art
- clean and readable on a phone screen
- lightly storybook-like, with soft depth and handcrafted charm
- consistent across exploration, companion, mystery, collectible, reward, and progression experiences

The target is **polished 2D raster mobile-game illustration**, not generic flat vector art.

## 2. Asset category policy — critical

The distinction is based on **what the asset is**, not where it is displayed.

### SVG/vector allowed

Only simple interface assets may use SVG/vector production:

- navigation icons
- toolbar icons
- button glyphs
- back/close/confirm/settings symbols
- simple status or system symbols
- simple map UI symbols whose role is purely interface/navigation

### SVG/vector forbidden

The following are game-art assets and must not be created, proposed, or regenerated as SVG/vector art unless the user explicitly overrides this rule:

- player characters
- companion characters
- NPCs
- character portraits or expressions
- backgrounds and illustrated environment scenes
- buildings
- furniture
- props and decorative objects
- collectibles
- inventory item artwork
- rewards
- crafting/resources/material artwork
- mystery illustrations
- discovery artwork
- place/POI illustration cards
- decorative map-world art
- seasonal/event artwork

**Important:** an asset does not become UI art just because it appears inside a UI card, inventory slot, reward popup, map marker, or collection screen. A collectible shown in inventory is still collectible game art and remains raster-based.

## 3. Non-UI rendering language

Non-UI game art should use:

- raster-oriented 2D illustration
- soft, controlled shading
- subtle volume and depth
- warm, coherent color relationships
- rounded and friendly silhouettes
- restrained highlights
- readable material cues for wood, paper, fabric, glass, metal, plants, food, etc.
- clean silhouettes that survive downscaling
- enough texture to avoid flatness, but not enough to create visual noise

Avoid:

- flat SVG aesthetics
- stock-vector appearance
- corporate illustration style
- icon-pack appearance
- clip-art
- simple geometric construction as the final visual
- plain flat fills with no light or material response
- sterile vector outlines
- excessive realism
- over-detailed painterly rendering that becomes muddy on mobile
- generic AI-art inconsistency across assets

## 4. Lighting and depth

Use a stable, gentle lighting model across asset families:

- broad soft key light
- soft contact shadows
- restrained ambient occlusion where useful
- small highlights on glossy or polished materials
- no harsh photorealistic contrast
- avoid changing light direction arbitrarily between assets that belong to the same set

The objective is not realism. The objective is enough light and form information to make the world feel tangible and premium.

## 5. Shape language

- favor friendly, rounded forms
- simplify details for mobile readability
- avoid overly sharp, technical, or corporate geometry unless the object itself requires it
- use distinctive silhouettes for collectibles and characters
- keep small objects identifiable at inventory/reward scale

## 6. Characters and companions

Characters should be:

- expressive at small sizes
- recognizable by silhouette
- cute without losing personality
- capable of multiple emotions and interaction states
- visually compatible with future animation

Preferred production direction:

- layered raster artwork
- separated raster parts when animation requires it
- Spine-compatible raster-part production may be used later

Do not replace character art with SVG body-part construction.

## 7. Backgrounds and places

Background/location illustrations should:

- communicate a sense of place quickly
- support exploration and discovery rather than overwhelm the map/UI
- use atmospheric depth and soft lighting
- contain a limited number of memorable details
- remain stylistically connected to characters and collectibles

For Daily Town, backgrounds may include location cards, discovery scenes, mystery scenes, event scenes, or other illustrated place representations layered around the map-driven experience.

## 8. Collectibles and rewards

Collectibles should feel rewarding to obtain.

They need:

- a clear silhouette
- soft dimensional rendering
- material-specific highlights/shadows
- visual hierarchy for common/rare/special variants without excessive glow clutter
- transparent-background raster export where appropriate

Do not simplify them into ordinary flat UI icons.

## 9. UI relationship

UI may remain cleaner and flatter than game art. SVG is allowed for interface glyphs, but UI should still share the same overall personality through:

- rounded geometry
- gentle spacing
- warm-neutral surfaces
- compatible palette choices
- soft elevation rather than aggressive hard shadows

Game art should sit inside the UI as illustrated content, not inherit the UI's SVG treatment.

## 10. Decision rule

When uncertain whether an asset may be SVG, ask:

> Is this primarily an interface control/symbol, or is it something the player perceives as part of the game's world, character, collection, story, reward, or atmosphere?

If it belongs to the game world/content, use raster game art. If it is only an interface control/symbol, SVG is allowed.
