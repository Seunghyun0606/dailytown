# Daily Town Design → Runtime Parity Handoff v2

Status: implementation handoff only. No runtime code changes are authorized by this document.

## Current gap

The runtime already has the functional five-tab shell, production companion renderer, A-3 renderer, map visual binders and time-of-day resolvers. The remaining issue is composition and visible surface language, not missing infrastructure.

Observed runtime patterns that should not define final visual design:

- emoji/text symbols in bottom navigation;
- default Material `NavigationBar` as the dominant chrome;
- repeated generic `ElevatedCard` blocks;
- Companion screen behaving like a centered portrait + stats dashboard;
- Goals screen behaving like generic progress cards;
- A-3 paper treatment used selectively rather than defining Records composition;
- v1 Moru anatomy treated as final production truth despite visual drift.

## Preserve these runtime foundations

Do not rewrite working domain/location/test infrastructure for visual parity.

Preserve:

- `MapViewAdapter` provider boundary;
- actual NAVER provider map and attribution;
- `MapRuntimeThemeResolver` phase mapping;
- `EncounterMapVisualResolver` semantic gameplay-state mapping;
- production asset registries and checksum gates;
- Field Test / QA separation;
- progress persistence and goal evaluation;
- A-3 semantic screen model;
- reduced-motion behavior and accessibility state rules.

## Runtime implementation targets after human approval

### `DailyTownMvpShell.kt`

Replace visual composition, not navigation semantics.

- keep five destinations and state preservation;
- replace final emoji symbols with a semantic icon family;
- make bottom navigation quieter and less Material-default in appearance;
- replace generic `ScreenColumn`/`ElevatedCard` composition with destination-specific surfaces;
- Companion → notebook portrait hierarchy;
- Goals → expedition checklist hierarchy;
- Settings → quiet utility hierarchy.

### `DailyTownApp.kt`

Preserve map behavior, tracking and encounter logic.

Visual work:

- expedition header/ribbon;
- map-edge Moru vignette scale/position contract;
- discovery/proximity copy hierarchy;
- avoid large opaque UI that competes with provider map;
- confirm time-of-day tokens affect app overlays, not provider-map truth.

### `A3ProductRecords.kt`

Make Journal Home the visible native Records landing language.

- paper viewport/background should read as the artifact itself;
- use layered note/stamp/mount patterns instead of generic cards;
- keep locked/completed non-color semantics;
- preserve detail navigation and data binding.

### `ui/visual/*`

Keep current semantic renderers and resolvers where possible.

Add/adjust only after design approval:

- product surface primitives for paper strip / expedition ribbon / memory mount / checklist row;
- nav semantic icon resolver;
- Moru v2 registry/export mapping after canonical v2 promotion;
- token v2 adapter from `visual-tokens.v2.candidate.json` once human-approved.

## Moru migration rule

Do not silently replace bytes behind existing v1 checksums.

After one MR candidate is approved:

1. create canonical v2 editable source;
2. create six-expression fit board;
3. run 48dp compact silhouette QA;
4. derive LIGHT/WARM_DUSK/DARK source variants;
5. regenerate static usage-context exports;
6. produce a new manifest/version and checksums;
7. update runtime registry in a separate implementation change;
8. keep v1 fallback until v2 visual QA passes.

## Motion tooling

Motion is downstream of canonical approval.

Preferred decision tree:

- route/halo/surface transitions → Compose procedural;
- simple authored timeline exported from design tools → Lottie Compose candidate;
- vector mascot deformation + interactive state-machine behavior → Rive candidate;
- raster atlas experiment → legacy sprite-gen pipeline only when it offers a specific visual advantage.

No motion dependency should be added just to compensate for weak static character design.

## Acceptance screenshots for future implementation

After runtime implementation is approved, capture at least:

1. Explore morning sparse map;
2. Explore sunset dense map;
3. Explore night map;
4. Companion portrait screen;
5. Records Journal Home;
6. one Discovery Detail;
7. Goals checklist;
8. Settings utility;
9. reduced-motion Companion/Records;
10. 48dp Moru compact HUD/map usage.

Product parity review should compare those captures against `dailytown-five-tab-parity-v2-board.svg`, not against generic Material defaults.

## Human gate before code

Runtime implementation begins only after:

- one Moru v2 family is selected;
- five-tab parity composition is accepted as the target;
- the user explicitly approves code/runtime application.
