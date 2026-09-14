# Daily Town — P1 Design Handoff Final v1

Status: design-owned P1 source work complete; production promotion and integration QA remain.

## Approved decisions

- art direction: Option A · Soft Botanical Explorer
- Moru canonical: A-2 balanced little explorer
- affinity: AF-1 + restrained AF-3
- Best Friend: BF-B Signature Explorer
- future variation: BF-C only as separate character variant after future explicit decision
- shipping companions: SC-B Moru + Luca
- motion: M-B Responsive Soft
- readability: R-B Balanced
- evening: EV-1 Interpolation-first
- A-3: storybook/paper five-screen system
- identity: ID-A Sprout Town Mark; A1/A2/A3 internal candidate lock remains

## New P1 approved-source masters

- `design/source/companion/moru/moru-bf-b-final-board-v1.svg`
- `design/source/companion/luca/luca-canonical-source-master-v1.svg`
- `design/source/motion/moru-m-b-storyboard-v1.svg`
- `design/source/brand/id-a-sprout-town-candidates-v1.svg`

## New machine-readable contracts

- `design/export-spec/moru-bf-b-export-jobs.v1.json`
- `design/export-spec/luca-production-manifest.v1.json`
- `design/export-spec/m-b-motion-pilot.v1.json`
- `design/export-spec/id-a-brand-manifest.v1.json`
- `design/export-spec/p1-production-promotion-queue.v1.json`

## Development session requirements

Development may implement, without changing the visual decisions:

- Moru/Luca semantic companion resolver
- affinity profile + modular slot resolver
- EV-1 continuous interpolation and E0..E4 forced debug states
- A-3 layouts and 360/412/600dp screenshot tooling
- atlas playback from explicit manifest frame rectangles
- R-B QA result bands: PASS / PASS_WITH_DECORATIVE_DEGRADATION / FAIL

Development must not:

- hard-code raw design filenames as domain IDs
- treat sprite-gen as an Android runtime dependency
- silently activate BF-C as a cosmetic profile
- substitute a new EVENING marker family
- treat approved-source SVGs as production runtime resources before promotion

## Remaining gates

### Production promotion

Approved masters still require isolated derivatives, size-context QA and SHA-256 before `production_export`.

### Human

- final M-B timing/easing/intensity after visible generated motion prototype
- physical outdoor R-B final pass
- final ID-A1/A2/A3 icon/logo lock

### Android / real-map integration

- DAY/DARK marker and route/halo readability on NAVER map
- EV-1 E2 real-map capture
- A-3 fit screenshots
- sprite atlas playback validation

## Future enhancement

BF-C remains a deliberate post-MVP character-variation path. When activated, it must introduce a separately versioned `variantId`, separate canonical source, expression/lighting compatibility mapping, map-avatar fallback and historical-record policy.

Beri is the preferred next companion expansion candidate. Pino requires silhouette separation work before shipping review.

## DESIGN HANDOFF

- 승인된 디자인: Option A / Moru A-2 / BF-B / SC-B / M-B / R-B / EV-1 / A-3 / ID-A
- 보류된 디자인: ID-A 내부 A1/A2/A3 최종 lock, generated motion 최종 timing/easing/intensity
- production asset 목록: Moru, BF-B affinity slots, Luca, DAY/DARK markers, A-3 asset kit, EV-1 token system, M-B atlas candidates, ID-A brand family
- semantic asset key: `companion.*`, `appearance.*`, `slot.*`, `animation.companion.*`, `lighting.*`, `phase.evening.*`, `marker.*`, `effect.*`, `surface.*`, `stamp.*`, `brand.*`
- 색상/token: visual-tokens v1 + EV-1 E0..E4
- 필요한 상태/variant: Moru/Luca 6 emotions, 3 lighting, affinity 4 stages, DAY/DARK, route/halo/discovery states, M-B motion/static/reduced-motion fallback
- Android 개발 시 필요한 동작: semantic resolver, interpolation/debug forcing, screenshot QA, atlas playback, fallback diagnostics
- 아직 사람이 결정해야 할 사항: M-B 최종 체감, R-B 실제 야외 최종 통과, ID-A1/A2/A3 최종 선택
