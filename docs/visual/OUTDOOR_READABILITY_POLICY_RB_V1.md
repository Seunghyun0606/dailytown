# Daily Town R-B Balanced Outdoor Readability Policy v1

Status: approved acceptance-policy direction. Physical-device final pass remains required.

## Principle

Gameplay-critical semantics and provider/legal UI must remain readable. Small degradation of purely decorative atmosphere is acceptable if semantic fallbacks remain strong.

## Hard blockers

Any of the following blocks production promotion:

- active/discoverable/solved/revisit meaning becomes ambiguous
- route cannot be followed at a glance in representative conditions
- provider map labels or legal attribution are materially obstructed
- halo implies the wrong geographic center or interaction radius
- HUD text loses practical readability
- critical state becomes dependent on hue or continuous animation

## Tolerable degradation

May be accepted when all hard blockers pass:

- atmosphere tint becomes less noticeable in bright daylight
- decorative glow is partially lost
- tiny particles/sparkles are less visible
- secondary paper/map ornamentation is simplified
- non-critical affinity detail disappears at compact map-avatar size

## Degradation order

When conflicts occur, reduce in this order:

1. particles and decorative glow
2. atmosphere overlay strength
3. secondary companion cosmetic detail
4. secondary POI prominence
5. non-essential HUD decoration

Do not reduce:

- user-location clarity
- active route direction
- active encounter/discovery semantics
- provider/legal visibility
- core HUD readability

## Review result bands

- PASS: all critical semantics clear; atmosphere acceptable
- PASS_WITH_DECORATIVE_DEGRADATION: critical semantics clear; documented non-critical visual loss accepted
- FAIL: any hard blocker occurs

## Physical-device gate

Final acceptance requires representative Android-device review under:

- bright outdoor daylight
- normal indoor light
- night/dark environment

R-B does not remove this gate.

## EV-1 note

E2 evening-core capture is mandatory. At E2, DAY/DARK marker choice may follow actual map-background luminance, but semantic meaning cannot change.
