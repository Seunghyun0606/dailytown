# Daily Town Affinity Appearance Matrix v1

Status: production visual-system specification. Final visual-change ceiling remains a human decision gate.

## Purpose

Define how companion appearance may evolve with affinity without hard-coding art filenames or allowing cosmetic progression to destroy character identity.

Affinity should communicate **shared history and familiarity**, not raw power escalation.

## Shared affinity stages

```text
base
familiar
trusted
best_friend
```

The semantic stage is domain/product meaning. The selected visual profile is a replaceable design binding.

## Shared modular slots

Preferred slots:

- `head_or_sprout_style`
- `neck_or_scarf_style`
- `bag_or_carried_item`
- `body_accent`
- `accessory`
- `context_effect`

Unsupported slots resolve to `none`.

## Default progression model

This matrix is the recommended non-blocking production default. It intentionally stays below the unresolved human-approved maximum transformation ceiling.

| Stage | Visual intent | Allowed default change |
| --- | --- | --- |
| `base` | first meeting | canonical clothing/identity only |
| `familiar` | repeated walks | one small accessory/accent OR subtle cloth detail |
| `trusted` | established bond | up to two coordinated cosmetic slots; bag/charm/detail may evolve |
| `best_friend` | shared-history signature | coordinated appearance profile, still preserving canonical anatomy and primary silhouette |

## Safe-change categories

Safe by default:

- scarf knot, trim, badge, or fabric motif
- bag charm, patch, tag, or carried keepsake
- small leaf/flower/head decoration that does not replace canonical identity geometry
- body accent marks that do not change anatomy
- tiny accessory linked to a shared memory
- restrained context effect shown only in suitable screens

Conditional:

- larger head decoration
- cape/outer-cloth silhouette changes
- carried objects with noticeable mass
- recurring particles/glow

Conditional changes require silhouette QA at 48 dp and must not become the only cue for companion identity.

Not a cosmetic affinity change:

- head/body proportion changes
- face geometry changes
- limb-length class changes
- locomotion/anatomy changes
- large silhouette transformation

These require a separate character-variant decision.

## Per-companion compatibility examples

The following are contract-validation examples, not final character art approval.

### Moru

Identity lock:

- round seed body
- central sprout attachment
- face geometry
- A-2 body proportion

Affinity-capable slots:

- scarf detail
- bag/charm
- small sprout decoration
- body accent
- memory accessory

### Luca

Identity lock:

- tall-ear silhouette
- face geometry
- explorer body mass

Affinity-capable slots:

- neck cloth
- carried-item charm
- ear-safe small accessory
- body accent

Large ear silhouette must not be obscured by affinity cosmetics.

### Pino

Identity lock:

- compact body
- leaf/ear head profile
- face geometry

Affinity-capable slots:

- neck/collar detail
- small carried item
- leaf-safe decoration
- body accent

### Beri

Identity lock:

- rounded/spined thinker silhouette
- face geometry
- back/head mass

Affinity-capable slots:

- hat/strap only if canonical spine profile remains readable
- carried notebook/tool
- small accessory
- body accent

## Appearance profile semantic keys

```text
appearance.{companionId}.base
appearance.{companionId}.familiar
appearance.{companionId}.trusted
appearance.{companionId}.best_friend
```

Each profile resolves slots rather than one mandatory flattened file.

Conceptual manifest:

```json
{
  "appearance_id": "appearance.moru.trusted",
  "slots": {
    "head_or_sprout_style": "slot.moru.sprout.trusted_01",
    "neck_or_scarf_style": "slot.moru.scarf.trusted_01",
    "bag_or_carried_item": "slot.moru.bag.base",
    "body_accent": "slot.moru.body_accent.trusted_01",
    "accessory": "none",
    "context_effect": "none"
  }
}
```

The exact file layout remains implementation-specific.

## Historical-memory rule

Journal/Memory may display the appearance profile that was active when the event occurred if product data preserves that historical state.

If historical appearance is unavailable:

- use current companion appearance only when it does not misrepresent the record
- otherwise use canonical/base stamp as stable fallback

A-3 record semantics must never break because an old cosmetic asset was deprecated.

## Motion compatibility

Affinity cosmetics and sprite animation create combinatorial risk.

Production order:

1. animate canonical identity/body first
2. keep static or low-motion cosmetics as overlays where visually acceptable
3. use sprite-gen layered composition only for accessories requiring synchronized motion
4. pre-bake only high-value approved appearance profiles
5. never blindly generate all affinity × expression × lighting × motion combinations

Every animated profile requires a static fallback.

## Map-avatar policy

At compact map size:

- canonical companion silhouette remains primary
- affinity decoration is secondary
- cosmetic detail may be simplified or omitted
- `map_avatar` may intentionally use a reduced appearance profile while larger contexts show the full profile

This is not a mismatch; it is context-specific readability optimization.

## Acceptance tests

For each appearance stage:

- companion remains recognisable in black silhouette where the cosmetic affects silhouette
- companion remains recognisable at 48 dp map-avatar context
- six semantic expressions still read correctly
- LIGHT/WARM_DUSK/DARK still preserve identity
- journal stamp remains recognisable
- unsupported slots safely resolve to `none`
- no gameplay feature depends on owning a specific cosmetic asset

## Human decision gate

Still unresolved:

**Affinity appearance ceiling** — how much silhouette/visual transformation is desirable at `best_friend` before the result should be treated as a separate character variant.

Until that decision is made, production should remain within the safe/default progression model in this document.
