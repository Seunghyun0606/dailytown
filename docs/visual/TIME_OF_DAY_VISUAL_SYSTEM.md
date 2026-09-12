# Daily Town time-of-day visual system

## Goal

Daily Town should feel like **the same neighborhood changing its mood over the course of a real day**, not like a static map with a different color filter.

The visual system therefore separates four concerns:

1. **day phase** — dawn, morning, midday, afternoon, sunset, evening, night
2. **character emotion/state** — neutral, curious, excited, surprised, clue found, resolved
3. **gameplay semantic state** — hinted, discoverable, active, solved, revisit, clue, POI category
4. **surface language** — live map/HUD vs journal/collection/story surfaces

These dimensions must not be baked into one giant set of duplicated images. Runtime code resolves semantic keys to a small set of approved production assets and theme tokens.

## Concept anchors

- `A1-morning-walk-garden` is the main reference for **morning / clean daylight**.
- `A2-sunset-alley-walk` is the main reference for **sunset / warm evening**.
- `A3-cozy-storybook-paper` is the reference for **journal / collection / memory** surfaces and remains largely stable regardless of clock time.
- `A0-cozy-neighborhood-baseline` remains a neutral product-family reference.

See `CONCEPT_ART_ARCHIVE.md` for the archived draft list and hashes.

## Day-phase model

Use semantic phases rather than exposing clock-specific art names in UI code:

```text
DAWN
MORNING
MIDDAY
AFTERNOON
SUNSET
EVENING
NIGHT
```

### Initial fallback clock mapping

The exact production boundaries are a design/product parameter, not a hard-coded product truth. For an initial deterministic implementation, use a local-time fallback with configurable boundaries. The first design session should create visual anchors for five primary families, while the intermediate phases can interpolate tokens.

| Semantic phase | Primary visual family | Draft anchor / design intent |
| --- | --- | --- |
| DAWN | cool soft light | new board required: mist blue, pale mint, soft lavender, first warm highlights |
| MORNING | fresh botanical | A1 |
| MIDDAY | clear high-readability daylight | new board required: brighter sky, cleaner neutrals, lowest atmospheric tint |
| AFTERNOON | warm neutral bridge | token interpolation between MIDDAY and SUNSET; dedicated board optional |
| SUNSET | warm emotional alley | A2 |
| EVENING | lamp-lit warm/dark bridge | derive from A2 + dark marker family; dedicated board recommended |
| NIGHT | quiet luminous neighborhood | new board required: deep navy/indigo, amber/cyan points of light |

Longer term, the resolver may accept locally computed sunrise/sunset windows so seasonal daylight changes do not feel wrong. That computation should remain app-owned/provider-neutral and should not require a map-provider API or cloud location upload.

## Runtime architecture target

The future implementation should follow this dependency direction:

```text
Local clock / optional solar window
        -> DayPhaseResolver
        -> VisualThemeProfile
        -> Compose theme tokens
        -> CompanionAssetResolver
        -> MarkerAssetResolver
        -> provider-neutral MapThemeSpec
        -> NAVER / future map adapter capabilities
```

Compose screens, encounter logic, progress logic, and map-provider code must not contain strings such as `morning_moru_happy.png` directly.

### Proposed semantic types

Conceptual model only; names may change during implementation:

```kotlin
enum class DayPhase {
    DAWN, MORNING, MIDDAY, AFTERNOON, SUNSET, EVENING, NIGHT
}

data class VisualThemeProfile(
    val phase: DayPhase,
    val surfaceFamily: SurfaceFamily,
    val markerFamily: MarkerFamily,
    val mapTheme: MapThemeSpec,
    val companionLighting: CompanionLighting,
)
```

The visual theme should be injectable/testable so replay and screenshot tests can force any phase without changing the device clock.

## Character design plan

### 1. Keep character identity constant

`모루` must remain immediately recognizable at every hour. Time of day should change **lighting and atmosphere**, not the character's anatomy, silhouette, face proportions, or primary identity colors.

Production character sheet should define first:

- front / 3-quarter canonical pose
- fixed body and face proportions
- leaf/sprout shape
- primary bag/scarf/accessory silhouette
- outline/edge treatment
- highlight/shadow rules
- minimum readable size for map avatar and HUD avatar

### 2. Emotion assets are independent of day phase

First production emotion set:

1. `neutral`
2. `happy`
3. `curious`
4. `surprised`
5. `clue_found`
6. `resolved`

Do **not** create 6 emotions × 7 time phases as separate full illustrations.

Instead use a layered system:

```text
base Moru expression
+ phase lighting treatment
+ optional context accessory/effect
```

Examples:

- DAWN: cool rim/light overlay, softer eyes/highlights
- MORNING: fresh warm face light, green/yellow environment bounce
- MIDDAY: neutral clean light, strongest readability
- SUNSET: peach/amber rim light
- EVENING/NIGHT: cooler shadow + warm local glow

If painterly final art makes runtime lighting overlays visually weak, export only two or three lighting families (`LIGHT`, `WARM_DUSK`, `DARK`) rather than one sprite set per phase.

### 3. Character contexts

Keep context effects separate from emotion assets:

- hint sparkle / question cue
- discovery exclamation
- clue-found small object/effect
- resolve celebration
- optional night lantern later
- weather accessories later only if weather becomes product scope

This prevents asset explosion and allows one emotion to appear in multiple scenarios.

### 4. Character usage sizes

Design source should support these derived usages:

- map avatar / user-companion marker
- compact HUD portrait
- encounter card half-body
- result/celebration large illustration
- journal sticker / memory stamp

One master source should drive exports so proportions do not drift between screens.

## Map design plan

### Principle: geographic truth stays stable; Daily Town atmosphere lives above it

The map must stay readable and trustworthy. Roads, labels, waterways, and navigation context are provided by the map provider. Daily Town should own the **overlay visual language**:

- semantic markers
- active encounter halo
- route/path treatment
- discovery/revisit emphasis
- companion/user marker
- lightweight ambient tint/scrim where appropriate
- HUD cards around the map

Do not turn the map into a full illustrated replacement tile set for MVP. That would create provider, maintenance, performance, and geographic-consistency problems.

### Provider-neutral map theme contract

Future `MapThemeSpec` should express intent rather than NAVER APIs:

```text
preferred brightness family: LIGHT / DARK
map atmosphere tint token
route token
active halo token
marker family: DAY / DARK
user-location treatment
optional provider capability requests
```

The NAVER adapter may map those intentions to supported SDK options. Other providers can map the same intent differently.

### Map phases

#### DAWN

- base map remains readable/light unless dark mode proves visually superior
- cool blue/mint atmosphere
- hints use a soft warm contrast so they feel like the first points of activity
- route line should be subdued, not neon

#### MORNING — A1 anchor

- sage/sky/butter palette
- clean, airy HUD
- plant/neighbor discovery markers feel natural
- minimal map tint

#### MIDDAY

- highest contrast and clearest map labels
- reduced decorative haze
- blue/green/yellow accents
- markers slightly stronger in silhouette because outdoor glare is a real-device concern

#### AFTERNOON

- bridge state; progressively warmer route/halo tokens
- avoid a full asset swap if token interpolation is sufficient

#### SUNSET — A2 anchor

- apricot/terracotta/olive family
- warm encounter halo and story cards
- richer illustration lighting while preserving map readability

#### EVENING

- warm local lights + cooler background
- switch to the dark marker family if base-map contrast requires it
- marker outlines/glows become more important than saturation

#### NIGHT

- provider dark/night capability where supported and product-approved
- deep navy/indigo UI surfaces with warm amber or cyan semantic highlights
- solved/active/hint states must remain identifiable by **shape/icon as well as color**
- avoid covering provider labels or legal/logo UI with atmospheric overlays

## Marker and icon system

Gameplay state must not be encoded only by time-dependent color.

First semantic marker family:

- `encounter.hinted`
- `encounter.discoverable`
- `encounter.active`
- `encounter.solved`
- `encounter.revisit`
- `clue`
- `poi.park`
- `poi.culture`
- `poi.landmark`
- `poi.daily_life`
- `poi.nature`
- `poi.other`

For MVP asset production, create **DAY and DARK marker sheets**, not seven independent phase sheets. Day-phase tokens may change halo, shadow, route, or accent treatment around those stable markers.

Marker requirements:

- distinct silhouette for active/hint/solved where practical
- consistent anchor point for map coordinates
- readable at outdoor brightness
- dark-map outline variant
- no critical state communicated only through hue
- selected/active marker clearly stronger than ambient POIs

## UI surface strategy

Not every screen should change as dramatically as the live map.

### Time-responsive surfaces

- main exploration map/HUD
- encounter discovery header/background treatment
- short companion reaction surfaces
- result/celebration atmosphere

### Mostly stable surfaces

Use A3/storybook language here to keep memory and collection feeling coherent across the day:

- journal
- collection
- clue notebook
- memory/story detail
- historical discoveries

### Neutral engineering surfaces

Field-test diagnostics, developer controls, permission/error states, and safety-critical status should remain mostly neutral and high-contrast. They should not become difficult to read because the user is testing at night.

## Transition behavior

Day-phase changes should feel continuous, not like switching themes abruptly at an exact minute.

Plan:

- resolve phase on app start/resume and periodically while active
- use a short crossfade/token interpolation for Compose surfaces
- delay or soften provider map-mode changes if they visually flash
- keep an open full-screen encounter latched to the phase in which it started; apply a new phase after returning to exploration
- map/HUD may transition while idle
- respect reduced-motion preferences; color/token transitions must work without animation

Exact motion duration is a design-session decision.

## Semantic asset keys

Final artwork should be referenced through stable semantic keys such as:

```text
companion.moru.neutral
companion.moru.happy
companion.moru.curious
companion.moru.surprised
companion.moru.clue_found
companion.moru.resolved

marker.encounter.hinted
marker.encounter.discoverable
marker.encounter.active
marker.encounter.solved
marker.encounter.revisit
marker.clue
marker.poi.park
marker.poi.culture

surface.journal.paper
surface.collection.paper
```

A resolver chooses the exported resource and lighting/marker family for the active `DayPhase`. Domain and gameplay code should know only semantic state, never drawable names.

## Production asset directory plan

Draft/source art stays outside Android runtime resources until approved.

After approval:

```text
design/
  source/                 # editable/source art location or external design link index
  export-spec/            # dimensions, safe areas, naming, checksums

app/src/main/res/
  drawable/               # vectors / shape resources
  drawable-nodpi/         # approved raster illustration assets where density scaling is undesirable
```

Do not copy concept-board screenshots into runtime resources.

## Dedicated design-art session deliverables

Before implementation, the next design session should produce and approve:

### Phase boards

1. DAWN board
2. MORNING board — refine A1
3. MIDDAY board
4. SUNSET board — refine A2
5. NIGHT board
6. optional dedicated EVENING bridge board

AFTERNOON can initially be specified by tokens between MIDDAY and SUNSET.

### Character package

1. Moru canonical model/proportion sheet
2. six expression masters
3. map-avatar treatment
4. HUD portrait treatment
5. large encounter/result treatment
6. LIGHT / WARM_DUSK / DARK lighting specification

### Map package

1. DAY marker sheet
2. DARK marker sheet
3. route/path tokens by phase
4. active-halo/discovery/revisit treatments
5. user/companion position marker
6. map/HUD overlay examples on real NAVER screenshots for readability review

### UI package

1. phase color/token matrix
2. map HUD cards
3. encounter card shell
4. result shell
5. A3-based journal/collection shell
6. typography hierarchy and icon stroke/radius rules

## Review gates before Android integration

Design approval should explicitly check:

- Moru is recognizable in every phase and size
- marker meanings do not change with time
- DAY/DARK markers remain readable on actual NAVER maps
- map labels and NAVER legal/logo UI remain unobstructed
- outdoor midday contrast is adequate on a physical device
- night UI does not rely on pure black or excessive glow
- color-blind users can distinguish critical states by shape/icon/text
- journal/collection remains visually stable enough that discoveries feel like one persistent collection
- phase transitions support the product mood without distracting from walking safety

## Engineering implementation order after art approval

1. add `DayPhase` + deterministic resolver with forced-phase debug/replay support
2. add phase token model and Compose `DailyTownVisualTheme`
3. add semantic companion/marker asset registry with missing-asset fallback
4. extend provider-neutral map port with `MapThemeSpec`
5. map `MapThemeSpec` to NAVER capabilities without leaking NAVER types upward
6. replace default SDK markers with approved semantic marker assets
7. wire character assets into HUD/encounter/result surfaces
8. add screenshot/instrumented tests for each forced phase
9. run real-device readability tests for midday sunlight and night conditions

Do not implement the production asset bindings before the dedicated art session approves the source sheets and export rules.
