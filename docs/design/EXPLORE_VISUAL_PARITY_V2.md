# Daily Town — Explore Visual Parity v2

> Status: **APPROVED-BASELINE DERIVATION — implementation detail spec**
>
> This document does not redesign Explore. It translates the exact Design Baseline v2 image into implementation-facing composition and surface rules while preserving current domain semantics.

## 1. Sources and precedence

Visual source: `docs/design/DESIGN_BASELINE_V2.md` → `dailytown_ux_visual_baseline_v2.png`.

Behavior source: current domain/runtime. If decorative copy or a number in the concept board conflicts with runtime behavior, runtime/domain wins. In particular, the concept-board distance text is illustrative; the implemented hint/discovery thresholds remain the current ~180 m / ~60 m rules.

## 2. Global Explore composition

- real provider map remains the primary surface during prepare/detect/approach
- map is visually continuous behind overlays; do not box it into a small dashboard card
- player-facing diagnostics, GPS controls and field-test tools do not occupy the ordinary Explore surface
- bottom navigation is quiet, warm-neutral and visually subordinate to map/discovery content
- Moru appears compactly as a companion cue, not as a large blocking mascot
- cream paper/field-note surfaces float over the map with soft elevation and restrained botanical decoration
- use one primary interaction surface at a time
- provider attribution and essential map controls remain unobscured

## 3. State A — Prepare

Baseline read: familiar neighborhood map + greeting/Moru cue + one small goal + one strong Explore CTA.

Required hierarchy:

1. map and current location
2. short greeting / contextual Moru presence near the top
3. optional tiny discovery prompt, never a dense stats card
4. today’s small walking goal near the bottom
5. primary CTA: start/continue exploration
6. bottom nav

Do not show the current main-branch dashboard stack of cumulative distance, collection stats, rotating goals, location preset controls, reminder controls and field-test diagnostics on this player-facing surface. Those belong to their dedicated surfaces or debug/QA areas.

## 4. State B — Detect / Approach

Baseline read: the same map stays mounted; an unknown signal becomes the focus and Moru reacts.

Required hierarchy:

- unknown signal/leaf marker is the strongest map overlay after the player location
- show live distance from runtime, never a hardcoded board value
- one short atmospheric hint line
- compact Moru contextual line at the top or upper-map region
- route/approach cue may be emphasized, but must remain compatible with provider-map truth
- optional bottom prompt such as “조금만 더 가볼까?” is supportive, not a modal interruption

State mapping:

- `HIDDEN` → no primary encounter surface; prepare/normal map
- `HINTED` → detect/approach presentation
- distance text derives from the existing encounter controller
- no new gameplay threshold is introduced for visual staging

## 5. State C — Discover

Baseline read: discovery becomes a focused story moment. A place illustration can temporarily dominate while the player is already in discovery range.

Composition:

- top/hero region: raster place/discovery illustration in the locked DailyTown art family
- lower paper sheet: title, place metadata, one short descriptive line, discovery object/clue art where available
- small Moru reaction is allowed as a secondary illustration layer
- primary CTA: inspect/open/details according to supported mechanic
- secondary defer action appears only when the product flow allows deferral

The discovery illustration is game art and must be raster PNG/WebP. Do not recreate it in Compose Canvas or vector.

## 6. State D — Resolved

Baseline read: the event turns into a warm paper memory rather than a generic success dialog.

Required hierarchy:

1. completion headline
2. discovery/place memory image or artifact
3. acquired clue/result
4. relationship/memory consequence with Moru
5. optional goal/progress contribution, visually secondary
6. two clear actions: `계속 탐험` and `기록 보기`

Use the Records/A3 paper language so the user feels the discovery has become part of their journal.

## 7. Surface language

### Map HUD / floating note

- rounded, warm cream surface
- soft tonal shadow, no hard Material elevation edge
- compact text; avoid multiline dashboard density
- botanical decoration is sparse and never competes with map labels

### Primary CTA

- muted olive/moss fill from the approved baseline
- cream/light label
- rounded pill/soft rectangle
- minimum touch target 48 dp
- one primary CTA per surface

### Secondary CTA

- paper/cream fill with warm-brown or olive text
- lower visual weight than the primary CTA

### Discovery / result paper

- warm paper base with slight tonal variation
- rounded corner treatment compatible with journal/card mounting
- small leaf/field-note decoration allowed
- no glossy glassmorphism or generic Material card stack

## 8. Implementation starting tokens

These are parity starting values, not a new art direction. Outdoor QA may adjust contrast while keeping the same baseline family.

- screen horizontal inset: 16 dp
- compact overlay internal padding: 12–16 dp
- primary paper sheet corner radius: 24–28 dp
- compact HUD radius: 20–24 dp
- CTA height: 52–56 dp
- minimum tap target: 48 dp
- ordinary component gap: 8–12 dp
- section gap: 16–20 dp
- map marker/halo must remain legible without covering adjacent labels
- primary body copy should remain at least 14 sp; critical outdoor distance/status text at least 12 sp

## 9. Map readability rules

- do not recolor or filter the NAVER/provider map into a fantasy map
- keep player location visually dominant and stable
- unknown-signal treatment must be distinguishable from ordinary POI markers
- decorative leaf/route motifs may frame UI but do not become map data
- provider attribution must remain visible
- outdoor contrast is a physical-device Human Gate, not an emulator-only pass

## 10. Current implementation gap

The current `main` Explore surface is built as a vertically scrolling Material screen with a 250 dp map plus multiple `ElevatedCard`, `AssistChip`, `FilterChip` and diagnostic sections. The baseline requires the inverse hierarchy: map-first live exploration with small contextual overlays, focused discovery/result surfaces, and player-facing stats moved out of the walk flow.

Development should preserve the existing domain, location, persistence, encounter and map-provider contracts while replacing only the player-facing presentation hierarchy.

## 11. Design QA

Before visual parity is considered ready for runtime review:

- Prepare reads as “go walk”, not “dashboard”
- Detect keeps the map readable while making the signal obvious
- Discover feels like a small story event, not a generic dialog
- Resolved feels recorded/remembered rather than merely completed
- Moru stays consistent with Candidate 3
- non-UI art is raster
- touch targets and distance/status text remain readable at phone size
- reduced-motion mode communicates the same states without relying on animation

## 12. Next production work

After this parity spec, create the matching discovery/place/background raster family for the first validation scenario and then refine Companion/Records surfaces. No alternate Explore composition or Moru redesign is part of that work.
