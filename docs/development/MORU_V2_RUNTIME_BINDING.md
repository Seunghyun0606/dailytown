# Moru Candidate 3 v2 Android Runtime Binding

Status: **packaged/bound for Android QA; not default-promoted**
Runtime profile: `companion.moru.canonical.v2`
Legacy terminal fallback/default: `legacy/current-v1`
Design authority: `design/production-consolidated-v2@4e53243fe01df54ae4b85d8504c678cf6a81d7af`

## Packaging decision

Android does **not** persist 432 semantic PNG combinations. The v2 package contains the smallest authoritative raster surface needed to reproduce the semantic family:

- 6 exact `result_large / LIGHT / base` expression masters (`neutral`, `happy`, `curious`, `surprised`, `clue_found`, `resolved`)
- 3 exact affinity overlays (`familiar`, `trusted`, `best_friend`); `base` is no overlay
- the design-side 432-entry semantic resolver/export manifest
- the accepted usage transform metrics
- the accepted lighting transfer metrics
- the accepted native semantic-family manifest
- one Android package provenance manifest

Production raster binaries: **9**. Persisted 432-combination binaries: **0**.

The 9 PNGs are copied byte-for-byte from the design authority branch. `runtime-package-manifest.v1.json` records each source authority path, SHA-256, dimensions, format/mode, and design authority HEAD. Instrumented tests additionally cross-check those hashes against the copied design semantic-family manifest, so a self-consistent but wrong Android package cannot silently pass.

This layout is preferred to pre-materializing 432 PNGs because the six full-size expression masters alone are about 9 MiB, while multiplying full-size combinations would create a large APK and unnecessary decode pressure. Android instead performs deterministic **raster-to-raster** materialization at requested display size and caches the bitmap result. It does not redraw Moru with Compose primitives, VectorDrawable, or SVG.

## Runtime materialization boundary

`AndroidMoruV2RuntimeAssetCatalog` reads the packaged design authority JSON directly. `MoruV2RasterRenderer` then:

1. decodes the selected authoritative expression PNG at a bounded sample size,
2. raster-crops/scales using the accepted usage transform,
3. raster-composites the selected affinity overlay when applicable,
4. applies the accepted lighting transfer for `WARM_DUSK` / `DARK`,
5. caches the resulting display-size bitmap by semantic key + target size.

No image generation/compositing occurs every frame. The first materialization for a semantic key is cached; subsequent frames reuse the bitmap.

The design manifest's full-resolution `rgba_pixel_sha256`, `alpha_pixel_sha256`, dimensions, and recipe remain packaged and queryable. Android binary integrity is enforced at the authoritative source layer; the display-size materialization intentionally does not claim byte identity with the design-side full-resolution output because Android resampling is performed at the actual runtime target size.

## Resolver contract

The v2 resolver preserves the MORU-03 design-side order:

1. exact `usage / expression / lighting / affinity`
2. same usage + requested expression + `LIGHT / base`
3. same usage + `neutral / LIGHT / base`
4. legacy v1 terminal fallback

Motion is not part of the 432 raster key family. Any requested motion falls back to static v2 raster semantics. The existing M-B prototype remains legacy/debug-only until the motion Human Gate is approved.

Invalid external enum/semantic values fail closed. They do not silently normalize to another v2 semantic key.

## Profile selection and rollback

`legacy/current-v1` remains the default in all builds. Release builds fail closed to v1 even when an external request asks for v2.

Debug/emulator QA may opt into v2 explicitly with the MainActivity intent extra:

```text
com.dailytown.app.extra.COMPANION_RUNTIME_PROFILE=companion.moru.canonical.v2
```

No persistence schema migration is introduced. Rollback is therefore an immediate profile selection back to `legacy/current-v1`; the existing SVG/canvas v1 implementation is retained unchanged as the terminal fallback path.

## Usage bindings

When the v2 profile is selected:

- `map_avatar` → Explore map hero
- `hud_portrait` → companion HUD
- `encounter_halfbody` → discovery/investigation encounter surface
- `result_large` → resolved/result surface
- `companion_portrait` → Companion screen
- `journal_crop` → Records memory detail/A3 context

The additional map/encounter/result/journal visuals are v2-profile-only, so default v1 screen behavior is not expanded as a side effect of this work.

## Automated gates

JVM tests cover:

- 432 unique semantic keys
- 432 exact resolutions
- single-missing fallback across all requests
- cascade fallback
- legacy-v1 terminal fallback
- invalid external semantic input fail-closed
- motion-to-static behavior
- v2 selection and v1 rollback/default behavior

Instrumented tests cover:

- packaged authority JSON SHA-256
- all 9 production source binary SHA-256 values
- design semantic-family hash cross-check
- source dimensions and alpha range
- packaged 432-entry manifest/key coverage
- usage dimensions vs design authority
- representative raster materialization for all six usages and all three lighting families
- emulator visual artifacts for map `48/56/64dp`, HUD `56/64/72dp`, Encounter, Result, Companion, and Journal contexts over cream/dark/map-heavy review backgrounds

The emulator smoke workflow includes the Moru v2 binding and visual-QA classes.

## Promotion / Human Gate state

Android packaging/binding and emulator QA do **not** imply default promotion.

- Design-side MORU-02 / MORU-03 PASS: separate upstream evidence
- Android runtime automated/emulator QA: required before promotion, but still separate from physical-device approval
- R-B outdoor readability: **HUMAN GATE**
- M-B motion timing/intensity: **HUMAN GATE**
- ID-A icon/logo: **HUMAN GATE**

`companion.moru.canonical.v2` must remain non-default until the required physical-device/readability decision is explicitly made.
