# Daily Town Visual Quality Gate

Use this checklist before accepting a new visual asset, design mockup, or art-direction change.

## 1. Category check

- Is the asset correctly classified as UI or game art?
- If it is character, companion, background, place illustration, building, prop, collectible, reward, mystery art, or decorative world content, is it raster-based?
- Has any SVG/vector treatment leaked into non-UI game art?
- Was an inventory/reward/map context incorrectly used as a reason to treat game art as a UI icon?

Any non-UI asset that violates the raster-only rule fails review.

## 2. Visual quality check

The asset should:

- feel like polished mobile game art rather than prototype art
- use controlled light and soft dimensional shading
- have a clear silhouette at actual mobile size
- feel warm, inviting, and consistent with Daily Town
- contain enough texture/material information to avoid flatness
- avoid unnecessary micro-detail

Reject or revise assets that look like:

- flat SVG/vector illustration
- clip-art or stock-vector packs
- corporate/web illustration
- generic app icons
- code-drawn geometric placeholder art
- inconsistent AI-generated styles
- overly photorealistic inserts that break the art family

## 3. Consistency check

Compare with already accepted assets in the same family:

- lighting direction
- contrast range
- saturation/value balance
- silhouette language
- outline treatment, if any
- material rendering
- character proportions
- perspective/camera angle for environment/props

Do not accept a visually strong asset if it breaks the established family without an explicit art-direction change.

## 4. Mobile usability check

Review at the actual target size:

- Does the important content remain legible?
- Does the character face remain expressive?
- Is the collectible immediately recognizable?
- Does the scene compete with important map/UI information?
- Is contrast sufficient in light/dark or map-heavy contexts where applicable?

## 5. Product-fit check

The design should support Daily Town's current product experience:

- real-world walking and map exploration
- mystery/discovery moments
- companion interaction
- collection/progression feedback
- lightweight, warm emotional tone

Do not drift into a generic town-builder or decoration-game visual system unless the product direction itself changes.

## 6. Design-decision persistence

When a review leads to a durable new rule:

1. Update `ART_DIRECTION.md` if it changes visual language or classification.
2. Update `ASSET_PRODUCTION.md` if it changes production/output rules.
3. Update this checklist if it creates a new mandatory review criterion.
4. Do not leave an accepted durable rule only in chat history.

## 7. Pass criteria

An asset or screen passes only when:

- it follows the category/format policy
- it is visually coherent with the current design system
- it is readable at mobile size
- it supports the actual Daily Town product context
- no unresolved design-system conflict remains
