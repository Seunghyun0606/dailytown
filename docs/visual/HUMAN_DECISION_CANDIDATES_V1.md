# Daily Town — Human Design Decision Candidates v1

Status: decision-candidate document. Options are not production approvals until explicitly selected.

## 1. Best Friend transformation ceiling

The current `appearance.moru.best_friend` remains inside the safe default and does not need a new decision. This choice controls **future maximum transformation beyond that safe default**.

### BF-A — Keepsake Only

Changes remain almost entirely additive:

- scarf patches
- bag tags / charms
- tiny sprout keepsake
- small accessory
- no meaningful silhouette growth

Pros:

- strongest canonical consistency
- easiest animation and small-size QA
- historical memories remain visually stable

Cons:

- Best Friend may feel only modestly different from Trusted

Best for: highly stable mascot identity.

### BF-B — Signature Explorer — RECOMMENDED

Keeps AF-1 memory logic, but permits one controlled signature silhouette accent at Best Friend:

- patchwork scarf with a slightly stronger readable edge
- one small pressed-leaf/thread motif near the sprout
- travel-worn bag/tag composition in large contexts
- compact map avatar still removes most bag/accessory detail
- no anatomy or face changes

Pros:

- visible emotional payoff without becoming a different character
- works with the current modular architecture
- preserves motion reuse and 48dp recognition

Cons:

- requires silhouette QA for the sprout/scarf accent

Best for: Daily Town's current direction.

### BF-C — Evolved Character Variant

Affinity may eventually unlock a noticeably different silhouette or clothing mass.

Rule: this is **not treated as a cosmetic profile**. It becomes a separately versioned character variant, e.g. `companion.moru.variant.best_friend_01`.

Pros:

- strongest long-term progression payoff

Cons:

- major art/animation/QA cost
- can weaken canonical Moru identity
- historical record/version management becomes more complex

Best for: post-MVP expansion, not first production package.

Recommended selection: **BF-B**.

---

## 2. Shipping companion set

All Luca/Pino/Beri candidates passed the common contract provisionally, but launch scope should avoid unnecessary production multiplication.

### SC-A — Moru only

Pros:
- smallest launch scope
- easiest canonical/motion QA

Cons:
- does not prove companion diversity to users
- replaceable architecture is technically present but not visibly demonstrated

### SC-B — Moru + Luca — RECOMMENDED MVP

Why Luca first:
- strongest silhouette contrast against Moru
- clearly validates that the companion contract is not plant-body-specific
- lowest ambiguity at compact map-avatar size among the three candidates

Pros:
- useful character variety at manageable asset cost
- good architecture validation

Cons:
- only one alternate companion at launch

### SC-C — Moru + Luca + Beri

Why Beri second:
- rounded/spined thinker silhouette adds a different body language from both Moru and Luca
- stronger family diversity than adding Pino first

Pros:
- meaningful launch variety
- good compatibility-test breadth

Cons:
- 6-expression / lighting / stamp / motion workload grows materially

### SC-D — Moru + Luca + Pino + Beri

Pros:
- broadest launch variety

Cons:
- highest production and QA cost
- Pino still deserves an extra silhouette-separation pass against Moru's botanical profile

Recommended MVP selection: **SC-B**. Recommended next expansion: add **Beri**, then revisit Pino silhouette.

---

## 3. Motion personality / intensity

Final values still require visible prototype QA. This decision establishes the target personality before tuning.

### M-A — Calm Ambient

- very small idle breathing
- clue reaction restrained
- resolved settle gentle
- low map distraction

Pros: safest outdoors and easiest reduced-motion parity.
Cons: character can feel under-responsive.

### M-B — Responsive Soft — RECOMMENDED

- small but visible idle breathe
- clear one-shot clue reaction
- happy/resolved states have one readable anticipation/settle beat
- no exaggerated perpetual bounce
- map-avatar motion remains more restrained than encounter/result views

Pros:
- balances character warmth and walking safety
- fits Option A's soft botanical language

Cons:
- needs context-specific amplitude limits

### M-C — Expressive Mascot

- stronger squash/bounce
- larger gesture arcs
- more frequent secondary sprout/scarf movement

Pros: strongest character presence.
Cons: higher anatomy-drift risk in sprite generation, more outdoor distraction, more expensive affinity-layer synchronization.

Recommended target: **M-B**, followed by human timing/easing QA on actual `sprite-gen`/native prototypes.

---

## 4. Outdoor readability acceptance policy

This is not a substitute for physical-device testing. It determines how strict the pass/fail gate should be when that test happens.

### R-A — Safety-first Strict — RECOMMENDED

A critical overlay failure in any representative map condition blocks production promotion:

- active/discoverable/solved meaning unclear
- route cannot be followed at a glance
- provider label/legal UI becomes obstructed
- halo implies wrong position
- HUD text loses practical readability

Decorative atmosphere must yield first.

### R-B — Balanced

Allows small non-critical atmosphere/decoration degradation when semantic fallbacks remain fully readable.

### R-C — Art-forward

Allows stronger atmosphere even when some secondary map information becomes less prominent.

Not recommended for the MVP because Daily Town is intended for real outdoor walking.

Recommended policy: **R-A**, while still allowing R-B-like tolerance for purely decorative elements.

---

## 5. App icon + Daily Town identity direction

No new icon/logo imagery should be generated until one direction is selected.

### ID-A — Sprout Town Mark — RECOMMENDED

Core mark:
- two-leaf sprout
- subtle path/door/town cue
- compact geometric silhouette

Logo:
- `Daily Town` wordmark paired with the mark
- warm botanical, not character-specific

Pros:
- survives future companion changes
- strongest long-term brand independence
- clear at launcher size

Cons:
- less immediately character-driven than Moru.

### ID-B — Moru Emblem

Core mark:
- simplified Moru face/head + sprout

Pros:
- highest mascot recognition
- emotionally friendly

Cons:
- makes Moru feel permanently equivalent to the product brand
- weaker if companion choice becomes a major user identity feature

### ID-C — Exploration Seal

Core mark:
- map/path + sprout + journal-stamp language
- circular or shield-like badge

Pros:
- directly communicates exploration + collection
- works well for badges/social graphics

Cons:
- can become visually busy at small launcher sizes

Recommended selection: **ID-A**. Moru can remain prominent in onboarding/store art without being permanently embedded in the service mark.

---

## Recommended decision bundle

For the first production release, the design-system recommendation is:

- Best Friend ceiling: **BF-B Signature Explorer**
- Shipping companions: **SC-B Moru + Luca**
- Motion personality: **M-B Responsive Soft**
- Outdoor QA policy: **R-A Safety-first Strict**
- App identity direction: **ID-A Sprout Town Mark**

These choices keep the MVP expressive while preserving replaceability, outdoor legibility and manageable production scope.
