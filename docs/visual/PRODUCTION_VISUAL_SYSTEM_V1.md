# Daily Town production visual system v1

Status: design-approved direction, production specification v1

This document converts the approved **Option A · Soft Botanical Explorer** direction and **Moru A-2 · balanced little explorer** canonical choice into a replaceable production visual system. It is a design specification only. It must not be interpreted as Android/Kotlin runtime implementation.

## 1. Locked direction

### Product visual identity

Daily Town is a warm neighborhood exploration experience layered on top of a real geographic map.

Core visual principles:

- warm and friendly rather than childish or toy-like
- outdoor readable before decorative
- the real map remains geographically trustworthy
- time of day changes atmosphere, not gameplay meaning
- character identity, expression, lighting, gameplay state, and surface language are separate dimensions
- every production asset must be replaceable through semantic keys
- no gameplay-critical state may depend on hue alone

### Approved base direction

- art direction: `OPTION_A_SOFT_BOTANICAL_EXPLORER`
- canonical Moru direction: `A2_BALANCED_LITTLE_EXPLORER`
- primary day anchors: `MORNING`, `SUNSET`, `NIGHT`
- companion lighting families: `LIGHT`, `WARM_DUSK`, `DARK`
- map marker families: `DAY`, `DARK`
- stable paper/story surface family: A-3 language, reserved for journal/collection/memory work

## 2. Moru canonical lock

Moru is the first canonical companion, not a hard-coded application singleton. Future companions may use different silhouettes and anatomy while sharing the same semantic asset contract.

### Canonical proportions

Production target proportions for A-2:

- head: approximately 42% of total body height
- torso/body: approximately 36%
- legs/feet region: approximately 22%
- overall perceived height: approximately 2.4 heads

These are drawing constraints, not pixel assumptions.

### Identity-locked traits

The following define base Moru identity and should not drift between expressions, poses, or lighting families:

- round seed-like head/body mass
- face placement relative to head center
- eye scale and eye spacing
- mouth baseline position
- sprout attachment point at the top center
- hand and foot scale
- shoulder width and body taper
- base front / 3-quarter / side / back silhouette
- neutral body proportions

### Replaceable zones

These are intentionally modular:

- `sprout_style`
- `scarf_or_cloth`
- `bag`
- `body_accent`
- `accessory`
- `context_effect`

A part may be replaced without changing the semantic identity of the companion if the canonical silhouette remains readable.

### Variant boundary

A change becomes a new **character variant** rather than a cosmetic part when it changes one or more of:

- head/body proportion
- face geometry
- limb length class
- main silhouette mass
- locomotion silhouette
- canonical identity shape

A completely different companion is a distinct companion family even if it uses the same asset pipeline.

## 3. Companion asset composition model

Conceptual composition:

```text
companion identity
+ canonical body
+ expression
+ lighting family
+ optional style/cosmetic parts
+ optional context effect
= rendered companion appearance
```

Recommended semantic dimensions:

```text
companionId
expression
lightingFamily
appearanceTier
styleVariant
accessoryVariant
contextEffect
```

No production screen should depend on a monolithic filename such as `moru_happy_green_scarf_night.png`.

## 4. Core expressions

Required first-production expression set:

1. `neutral`
2. `happy`
3. `curious`
4. `surprised`
5. `clue_found`
6. `resolved`

### Expression rules

#### neutral

- relaxed open eyes
- small neutral mouth
- vertical center of gravity
- no context effect required

#### happy

- smile readable at compact HUD size
- eye treatment may close or arc upward
- hands/body may open slightly
- must remain distinguishable from `resolved`

#### curious

- attentive eyes
- mild head tilt or forward lean
- optional small question cue
- no discovery sparkle that could be confused with `clue_found`

#### surprised

- clearly wider eyes
- visibly open mouth
- body or hands move outward/upward
- optional short exclamation cue

#### clue_found

- focused/engaged face
- object, magnifier, sparkle, or equivalent semantic cue
- must communicate "found evidence" rather than generic happiness

#### resolved

- calm satisfaction / relief
- softer celebratory posture than `happy`
- optional small resolve sparkle or gesture
- should read as closure, not surprise

## 5. Lighting families

Expressions are not duplicated conceptually for every day phase. Lighting is a separate family.

### LIGHT

Primary use:

- `MORNING`
- `MIDDAY`
- early `AFTERNOON`

Rules:

- natural daylight bias
- soft cool-to-neutral shadow
- clean face readability
- preserve cream body identity
- avoid yellowing the entire character

### WARM_DUSK

Primary use:

- late `AFTERNOON`
- `SUNSET`
- warm `EVENING`

Rules:

- peach/amber directional warmth
- slightly deeper shadow separation
- warm rim permitted
- keep eyes and mouth high-contrast
- do not shift green identity parts into brown

### DARK

Primary use:

- dark `EVENING`
- `NIGHT`

Rules:

- cool ambient shadow
- selective warm local highlight allowed
- face remains readable without a neon glow
- avoid pure-black body shadows
- warm window/lamp light may be used as environmental bounce

## 6. Time-of-day core boards

### MORNING

Intent: fresh, bright, botanical, inviting.

Visual components:

- light map family by default
- low atmosphere tint
- sage/leaf accents
- butter discovery warmth
- clean HUD surfaces
- minimal glow

### SUNSET

Intent: warm, nostalgic, emotionally rich but still readable.

Visual components:

- warm map atmosphere tint
- apricot / peach route accents
- stronger but soft halo
- warm local card surfaces
- WARM_DUSK companion lighting

### NIGHT

Intent: quiet, luminous, mysterious without becoming ominous.

Visual components:

- dark provider map capability where supported and approved
- deep navy UI framing
- warm amber + muted sky/cyan semantic highlights
- DARK companion lighting
- marker meaning reinforced with shape, internal icon, and outline
- glow is local and restrained

## 7. Marker system

Production families:

- `DAY`
- `DARK`

The semantic marker set is stable across both families.

Required encounter markers:

- `encounter.hinted`
- `encounter.discoverable`
- `encounter.active`
- `encounter.solved`
- `encounter.revisit`
- `clue`

Required POI markers:

- `poi.park`
- `poi.culture`
- `poi.landmark`
- `poi.daily_life`
- `poi.nature`
- `poi.other`

### Marker state grammar

A marker may combine:

```text
base silhouette
+ internal semantic icon
+ outline family
+ state badge / state ring
+ optional halo for active selection
```

Critical rules:

- `hinted`, `active`, and `solved` must not differ by color only
- map-coordinate anchor stays identical across variants of one semantic marker
- selected state may scale modestly but cannot shift the geographic anchor
- solved state should feel quieter than active state
- revisit must remain identifiable independently of color
- dark family relies more on outline/luminance than saturation

## 8. Route system

Semantic states:

- `idle`
- `following`
- `completed`

### idle

- low-emphasis segmented or softly dashed route
- directional readability without implying urgency

### following

- stronger contrast
- directional cue may be repeated at long intervals
- animation, if used, remains subtle enough for walking safety

### completed

- calmer solid or simplified route
- destination/completion endpoint receives semantic confirmation
- no celebratory animation loop

## 9. Active halo

Semantic states:

- `idle`
- `active`
- `strong`

### Design grammar

- concentric soft rings
- center remains geographically precise
- active can pulse gently
- strong is reserved for immediate focus/discovery proximity
- reduced-motion fallback uses ring count, weight, and static luminance instead of animation

## 10. Discovery effect

Semantic intensity:

- `small`
- `medium`
- `big`

First effect language:

- botanical leaf burst
- small sparkle accents
- optional warm light point

Avoid:

- full-screen confetti for routine discovery
- excessive particles over map labels
- persistent looping particles after the discovery moment

## 11. Core semantic color tokens

These are v1 production defaults and remain replaceable at the token layer.

### Brand

| Token | Value | Use |
| --- | --- | --- |
| `brand.leaf.primary` | `#6B8F7A` | primary botanical accent |
| `brand.leaf.secondary` | `#A7C7A5` | secondary botanical accent |
| `brand.leaf.ink` | `#214F3B` | dark green text/icon emphasis |
| `brand.butter` | `#FFD36B` | discovery/accent warmth |
| `brand.warmPeach` | `#FFB184` | sunset warmth |
| `brand.sky` | `#9CC9FF` | cool daylight/info accent |
| `brand.deepNavy` | `#1E2A44` | night/deep surface family |

### Neutral surfaces

| Token | Value |
| --- | --- |
| `surface.white` | `#FFFFFF` |
| `surface.ivory` | `#F7F3E8` |
| `surface.paper` | `#F4F0E6` |
| `surface.coolLight` | `#EEF3F5` |
| `text.ink` | `#1A1F1C` |
| `text.onDark` | `#F7FAF5` |
| `stroke.soft` | `#D8D3C3` |

### Status accents

| Token | Value | Meaning |
| --- | --- | --- |
| `status.info` | `#4DA3FF` | informational |
| `status.success` | `#48836C` | completed/success |
| `status.alert` | `#FFB020` | attention |
| `status.warning` | `#FF5C5C` | warning/error emphasis |
| `status.mystery` | `#8B6CFF` | mystery/memory accent |

### Time atmosphere accents

| Token | Value | Use |
| --- | --- | --- |
| `phase.morning.route` | `#6F9F71` | morning route |
| `phase.sunset.route` | `#E8794F` | sunset route |
| `phase.night.route` | `#78A7F6` | night route |
| `phase.night.warmPoint` | `#FFBE68` | lamp/discovery point |

### Contrast policy

- `brand.leaf.primary` is an accent, not a default small-text color on white
- `brand.leaf.ink` is the preferred green for readable text/icons on light surfaces
- `text.ink` is the default body-text color on light surfaces
- `text.onDark` is the default text color on deep navy/dark surfaces
- semantic badges must be validated by luminance plus shape/icon, not hue alone

## 12. Typography direction

Korean production target:

- primary family: `Noto Sans KR` or platform-equivalent approved family
- display/title: Bold
- heading: Medium/Bold depending on size
- body: Regular
- caption/label: Regular/Medium

Typography assets are not rasterized into UI imagery except illustration-specific lettering approved later.

## 13. Affinity appearance progression

Affinity may change appearance while preserving character recognition.

Reference stages:

- `base`
- `familiar`
- `trusted`
- `best_friend`

Allowed default progression channels:

- scarf/cloth
- bag
- leaf decoration
- body accent
- accessory
- small context effect

Do not use affinity to silently change canonical anatomy.

The progression should communicate shared history, not raw power escalation.

## 14. Future companion family contract

A future companion may have completely different anatomy and silhouette. It still implements the same semantic usage contexts:

- map avatar
- compact HUD portrait
- encounter view
- result/celebration view
- journal sticker/memory stamp

Each companion independently defines:

- canonical body
- six core expressions or supported fallback map
- lighting compatibility
- appearance variants
- silhouette lock

## 15. UI surface families

### Live map/HUD

- time-responsive
- outdoor-readable
- compact
- low decorative noise

### Encounter/result

- time-responsive atmosphere
- larger companion art allowed
- discovery/result effects permitted

### Journal/collection/memory

- A-3 paper/storybook family
- mostly stable across clock phases
- discovery history should feel persistent, not recolored every hour

## 16. Replacement guarantees

Every visual binding must support replacement without gameplay/domain changes.

Required guarantees:

- companion replacement by semantic companion id
- expression replacement by semantic expression
- lighting replacement by lighting family
- cosmetic replacement by modular slot
- marker replacement by semantic marker + DAY/DARK family
- effect replacement by semantic effect key
- token replacement by semantic color role

Production art must never become a domain identifier.

## 17. Design acceptance gates

Before Android integration, production assets should pass:

- canonical Moru recognition at front/3-quarter/side/back
- small avatar recognition at 48 dp target context
- acceptable fallback at 32 dp where necessary
- six expressions distinguishable without relying on accompanying text
- DAY markers readable on representative light map backgrounds
- DARK markers readable on representative dark map backgrounds
- critical gameplay state distinguishable without hue alone
- route does not obscure road/label comprehension
- halo does not imply incorrect geographic precision
- night visuals avoid excessive glow
- map-provider legal/logo UI remains unobstructed

## 18. Human decision gates still open

The following should remain explicit human/product decisions rather than being silently locked by this document:

- affinity appearance ceiling: how far Moru may visually change before it should be a separate variant
- final motion timing/easing intensity after motion prototypes exist
- final real-device readability acceptance after outdoor physical-device review
- whether EVENING receives a dedicated visual board or remains an interpolation/bridge
- final app icon and logo lockup

Everything else in this document may proceed as production-v1 design work and remain replaceable through semantic assets/tokens.