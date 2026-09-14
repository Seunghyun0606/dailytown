# Daily Town Map Overlay Readability QA v1

Status: design QA specification. No Android/Kotlin implementation is included.

## Purpose

Define repeatable visual QA for Daily Town overlays on top of a real provider map. The goal is to validate that character avatars, semantic markers, routes, halos, effects, and HUD remain readable outdoors without obscuring geographic information.

## Test conditions

Use the same semantic scene under at least these conditions:

1. `MORNING / DAY marker family / LIGHT`
2. `SUNSET / DAY marker family / WARM_DUSK`
3. `NIGHT / DARK marker family / DARK`

Each condition should be checked against three map complexity classes:

- sparse residential
- dense urban/commercial
- park/river/green-space

## Required overlay stack

Every QA capture should exercise:

- provider base map
- companion map avatar
- one active encounter marker
- one discoverable marker
- one solved/revisit marker
- at least one POI marker
- route in `following` state
- active halo
- one discovery effect sample
- top/bottom HUD surfaces where applicable

## Priority hierarchy

When visual elements compete, preserve this order:

1. user location / navigation safety
2. active route direction
3. active encounter or immediate discovery
4. provider road/place legibility
5. companion presence
6. secondary POI and solved/revisit markers
7. decorative atmosphere/effects

Decorative effects must yield first.

## Marker readability gates

### Shape

- encounter state is distinguishable by silhouette/internal icon/state treatment, not hue only
- active state remains visible without requiring glow
- solved state is quieter but not ambiguous with disabled/locked
- revisit has a repeat/history cue independent of color

### Optical size

- marker family uses consistent outer bounds
- different icons should be optically balanced, not merely numerically equal
- selected/active scale increase must not move the geographic anchor

### DAY family

- outline survives pale roads, green parks, and beige building blocks
- white/light marker interiors do not disappear into provider-map whitespace

### DARK family

- relies on luminance, outline, and icon separation before saturation
- glow remains local and does not merge nearby markers
- warm amber and cool blue highlights remain semantically distinct by shape/icon as well as color

## Companion map-avatar gates

Target design context: compact map avatar approximately 48 dp visible size.

Must pass:

- Moru and future companions remain distinguishable in silhouette
- face/sprout/ear identity survives reduction
- affinity cosmetics do not become the primary identifier
- DARK lighting preserves face readability
- avatar does not appear to be a map POI marker

If avatar detail fails at compact size, simplify the export rather than enlarging it indefinitely.

## Route gates

### Idle

- low prominence
- does not compete with active markers

### Following

- direction is understandable at a glance
- line remains legible over roads, parks, water, and building textures
- no rapid marching-dash effect
- route never visually masks provider road names for long segments where avoidable

### Completed

- calmer than following
- completion endpoint clearly confirms finish
- avoids celebration loops along the whole route

## Halo gates

- center is geographically fixed
- rings do not imply a larger interaction radius than product semantics
- pulse amplitude remains small
- strong state is visually stronger through ring count/weight/luminance, not flash frequency alone
- reduced-motion static halo remains understandable

## Discovery effect gates

- one-shot effect clears quickly
- leaf/sparkle particles do not cover map labels for a sustained period
- `small / medium / big` differ by density/scale/composition as well as color
- routine discoveries never require full-screen celebration

## HUD gates

- HUD surfaces have sufficient separation from the map without becoming opaque panels that hide context
- text remains legible in bright daylight
- night HUD does not create high-brightness eye strain
- transparent/blurred treatments are optional; information hierarchy must survive without blur

## Time-of-day atmosphere gates

### MORNING

- fresh/bright mood without washing out DAY markers
- atmosphere overlay is subtle

### SUNSET

- warm tint does not collapse orange/yellow semantic accents
- route and discovery accents retain distinction

### NIGHT

- dark atmosphere does not force every semantic element into neon glow
- warm local lights are accents, not the primary readability mechanism

## Color-blind / non-color state check

For all critical gameplay states:

- inspect in grayscale
- inspect with reduced saturation
- confirm silhouette/icon/badge/ring treatment still communicates state

Critical states include:

- hinted
- discoverable
- active
- solved
- revisit
- locked/unknown where used

## Motion QA

For every animated overlay:

- normal-motion and reduced-motion variants must both communicate the same semantic state
- no critical state depends on continuous animation
- motion does not distract from walking safety
- repeated motion should be calm enough for multi-minute map exposure

## Screenshot matrix

Minimum design QA matrix before development sign-off:

```text
3 time anchors
× 3 map complexity classes
× 2 motion modes (normal/reduced)
= 18 baseline captures
```

Add representative zoom levels where marker clustering/route density changes materially.

## Physical-device human gate

Design can prepare the matrix and acceptance criteria, but final approval requires a person on representative Android devices under:

- bright outdoor daylight
- indoor normal light
- night/dark environment

This physical-device gate remains human-approved and is not auto-closed by concept screenshots.

## Acceptance result format

Each capture should record:

```text
phase
mapComplexity
motionMode
markerFamily
companionId
pass/fail
issues[]
notes
```

Issues should reference semantic elements, not raw filenames.

## Development-session handoff

Development should provide screenshot/debug tooling able to force:

- time phase
- marker family
- companion id
- semantic marker state
- route/halo/discovery state
- reduced-motion mode

Design owns the acceptance matrix; development owns rendering/instrumentation implementation.
