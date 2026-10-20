# Daily Town M-B Responsive Soft Motion Target v1

Status: approved motion-personality target. Final timing/easing still requires prototype QA.

## Principle

Motion should make the companion feel responsive and alive while remaining subordinate to map readability and outdoor walking safety.

## Context amplitude

### Map avatar

- lowest amplitude
- idle breathe is subtle
- no perpetual bounce
- clue reaction is short and bounded
- secondary sprout/ear/scarf motion is restrained

### HUD portrait

- low-to-medium amplitude
- one readable reaction beat allowed
- no repeated celebratory loop

### Encounter / result

- medium amplitude allowed
- anticipation + action + settle may be visible
- one-shot reactions can be more expressive than map avatar

## Pilot target

### idle_breathe

- loop: yes
- visual amplitude: small
- center-of-mass drift: near-zero
- face geometry drift: none
- secondary appendage motion: tiny, phase-delayed if used
- reduced-motion: static neutral

### clue_react

- loop: no
- clear anticipation
- one readable discovery/reaction beat
- short settle into clue_found pose
- must not read as generic happy
- reduced-motion: clue_found static + one static accent

### resolved_settle

- loop: no
- brief relief/satisfaction beat
- ends in stable resolved pose
- reduced-motion: resolved static

## Follow-up states after pilot

- investigate
- happy_bounce

`walk` remains experimental until anatomy/foot-contact QA is proven.

## sprite-gen policy

- optional offline authoring/curation pipeline only
- generated frames are candidates, never auto-approved
- canonical body is animated first
- static/low-motion affinity cosmetics should overlay where possible
- synchronized accessory motion may use layered or pre-baked frames only when needed
- every animated state requires a static fallback

## Prototype review dimensions

Human QA should review:

- perceived warmth
- map distraction
- anatomy drift
- face/ear/sprout continuity
- loop seam
- settle quality
- context amplitude difference
- reduced-motion parity

## Final values gate

Exact fps, frame duration, easing, anticipation length and settle length remain intentionally unlocked until visible prototypes exist.
