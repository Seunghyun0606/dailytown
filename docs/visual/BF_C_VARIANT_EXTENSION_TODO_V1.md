# Daily Town BF-C Variant Extension TODO v1

Status: post-MVP design TODO. Current shipping decision remains **BF-B Signature Explorer**.

## Purpose

Preserve a deliberate path from affinity cosmetics to a future **character variant** without breaking the current Moru canonical contract or historical records.

## Current shipping baseline

`BF-B Signature Explorer` is the active first-release ceiling:

- canonical Moru A-2 anatomy remains locked
- face geometry remains locked
- one controlled signature silhouette accent is allowed
- map-avatar compact profile may omit most accessory mass
- affinity remains a cosmetic/appearance-profile concern

## Future BF-C rule

If a future product decision allows a more substantial Best Friend transformation, do **not** overload `appearance.moru.best_friend`.

Create a separately versioned companion variant:

```text
companion.moru.variant.best_friend_01
companion.moru.variant.best_friend_02
...
```

A BF-C variant may change one or more of:

- outer clothing mass
- secondary silhouette accents
- carried-item mass
- locomotion silhouette
- head/body proportion within a newly approved variant sheet

It still must preserve enough identity to be recognisable as Moru unless product explicitly defines it as a different companion.

## Required variant contract

Each BF-C variant must provide:

- canonical front / 3-quarter / side / back
- black-silhouette QA
- 6 semantic expressions or documented fallbacks
- LIGHT / WARM_DUSK / DARK compatibility
- map_avatar / hud_portrait / encounter / result / journal_stamp contexts
- motion/static fallback map
- affinity migration/fallback policy
- historical record preservation policy

## Runtime/design separation

Domain state should distinguish:

```text
companionId = moru
variantId = base | best_friend_01 | ...
affinityStage = base | familiar | trusted | best_friend
appearanceProfile = replaceable cosmetic binding
```

Do not encode BF-C as a filename-specific branch.

## Historical-memory rule

Past Journal/Memory records should preserve `variantId` when recorded if available.

Fallback order:

1. historical variant + historical appearance profile
2. historical variant + canonical appearance
3. base Moru + historical-stage-safe appearance
4. base Moru canonical stamp

A deprecated BF-C asset must never make a historical record unrenderable.

## Migration rule

If a user unlocks/selects BF-C in the future:

- do not silently replace every historical Moru record
- current live companion may use BF-C
- historical records retain event-time variant where possible
- map-avatar may use a simplified BF-C compact profile

## Production cost warning

Each BF-C variant expands:

- canonical art QA
- expression QA
- lighting QA
- sprite-gen / motion QA
- compact map-avatar QA
- A-3 stamp compatibility

Therefore BF-C is a deliberate content-expansion feature, not an automatic affinity art multiplication rule.

## Activation gate

This TODO becomes active only after an explicit future product/user decision to introduce evolved Moru variants.
