# Daily Town — Companion & Records Surface v2

> Status: **APPROVED-BASELINE DERIVATION**
>
> Visual authority: `docs/design/DESIGN_BASELINE_V2.md`
>
> Raster finish reference: `design/reference/ux-v2/raster/companion_records_finish_pass_v1.webp`

## 1. Scope

This document refines the already-approved Companion relationship-notebook and Records A3 journal hierarchy. It does **not** reopen layout, Moru character direction, navigation, or the overall illustration family.

The purpose is to replace visible generic Material 3 surface treatment with Daily Town-specific paper/notebook components while preserving current product behavior, persistence data, test tags, and routing.

## 2. Shared surface language

### Color family

- `paper.canvas`: `#F4EBDD`
- `paper.card`: `#FFF8EB`
- `paper.inset`: `#EFE3CF`
- `paper.edge`: `#D8C8AE`
- `ink.primary`: `#453B30`
- `ink.muted`: `#74685B`
- `botanical.moss`: `#68704E`
- `botanical.leaf`: `#87906A`
- `accent.ochre`: `#B18B45`
- `accent.terracotta`: `#B87358`

These values are implementation targets for Compose surfaces; raster art remains governed by the locked baseline rather than recolored to match tokens mechanically.

### Shape / spacing

- screen horizontal inset: `18dp`
- normal section gap: `14dp`
- tight content gap: `6–8dp`
- paper-card padding: `16dp`
- hero-card padding: `18–20dp`
- large paper radius: `22dp`
- memory-card radius: `18dp`
- tab / small control radius: `14dp`
- pill radius: `20dp`

### Elevation / stroke

- prefer warm paper edge + subtle tonal separation over Material elevation
- standard stroke: `1dp` using `paper.edge` at low contrast
- hero shadow: soft warm shadow, equivalent visual weight to `0 6dp 20dp` at about `8%` black
- ordinary journal cards should not stack multiple hard shadows

### Typography

- screen title: `26–30sp`, semibold/bold
- section title: `19–21sp`, semibold
- card title: `16–18sp`, semibold
- body: `14–15sp`, regular
- meta/date/location: `11–12sp`, regular/medium
- decorative handwritten copy is art/reference-only; runtime Korean body text must use a legible app font rather than a handwriting font for long passages

## 3. Companion — relationship notebook

The runtime hierarchy stays:

1. Moru current portrait / mood
2. contextual line
3. relationship stage / bond
4. recent shared memory
5. actions: talk / memories / gifts
6. long-term history below the primary relationship content

### `DTCompanionNotebookHero`

- Moru raster art occupies about `220–260dp` visual height on a phone portrait layout.
- Use `companion_portrait` or approved semantic fallback; do not redraw Moru in Compose.
- Background is a warm paper mount with one restrained botanical raster flourish maximum.
- Current mood label is secondary; contextual line is the emotional focal text.

### `DTAffinityNote`

- Relationship tier is primary; raw bond number is secondary.
- Meter height `8–10dp`, rounded, moss-to-ochre family only.
- Do not turn this into a stat dashboard.
- Affinity decoration follows approved `base / familiar / trusted / best_friend`; anatomy remains unchanged.

### `DTRecentMemoryCard`

- 16:9 or 4:3 memory-art thumbnail when available.
- Title + short place/date context + one Moru memory line.
- One paper edge, optional single tape/sticker raster decoration.
- Empty state stays textual and quiet; do not fabricate memories.

### Companion actions

- primary: `대화하기`
- secondary: `함께한 기억`, `선물·기념품`
- 48dp minimum touch target
- controls may use Compose/vector UI symbols; gifts/items shown as content remain raster game art

## 4. Records — A3 journal

Records is a **journal of lived exploration**, not a collection dashboard.

### `DTJournalCanvas`

- full-screen `paper.canvas`
- optional extremely subtle raster paper texture at low opacity; texture must not reduce text contrast
- botanical corner decoration is sparse and never repeated on every card

### `DTJournalTabs`

Approved hierarchy:

- 오늘의 기록
- 장소
- 미스터리
- 단서
- Moru와의 기억

Tabs use clean Compose surfaces and true UI symbols only. Avoid default `FilterChip` visual styling.

### `DTRecordArtifact`

Primary record card order:

1. date / small index mark
2. record title
3. place/discovery raster image
4. short story copy
5. attached clue artifact, when the domain actually has one
6. Moru reaction / shared-memory line when present
7. link to detail or memory

A record card may use a taped-photo / stacked-paper visual, but only one dominant artifact should exist per card.

### `DTClueArtifact`

- clue image itself is raster content
- paper mount/container is Compose UI
- target raster display sizes: `48 / 64 / 96dp` depending on context
- preserve clue silhouette; avoid shrinking detailed discovery art into an icon substitute

### `DTMemoryStamp`

- semantic state marker for shared memory
- true UI stamp outline may be vector/Compose, but illustrated leaf/flower/keepsake content is raster
- visual prominence lower than record title and discovery image

## 5. Decorative material rules

Allowed raster decorations from the locked family:

- small masking-tape pieces
- pressed flower / leaf accent
- paper scraps
- restrained handwritten motif as decorative image
- one botanical corner flourish

Rules:

- maximum 1–2 decorative accents in one viewport region
- decoration must never obscure body copy or touch target
- do not use random rotations on every card
- no scrapbook clutter wall
- no repeated AI-art motif with inconsistent drawing style

## 6. Mapping to current PR #10 implementation

Current branch implementation already has the correct information hierarchy, but uses generic `Surface`, `MaterialTheme.shapes.large`, `primaryContainer`, and `surfaceContainer` heavily.

### CompanionScreen

Preserve:

- `companion-product-screen`
- `companion-relationship`
- `companion-recent-memory`
- action panel behavior/test tags
- existing semantic memory / bond data

Replace visually:

- large generic portrait `Surface` → `DTCompanionNotebookHero`
- relationship `primaryContainer` card → `DTAffinityNote`
- recent-memory generic surface → `DTRecentMemoryCard`
- panel surfaces → paper-inset treatment
- long-history stats remain below the relationship content and use quieter separators rather than a dashboard card

### A3ProductRecords

Preserve:

- current routes and test tags
- current real persisted data only
- current rule against fabricating 1/4→4/4 mystery progress or unsupported 1:1 relations

Replace visually:

- repeated generic `RecordSection` surfaces → journal section/artifact components
- stat-only Today card → compact journal summary strip
- place rows → artifact/list entries with optional raster thumbnail
- clue and memory detail → paper-note detail surfaces

## 7. Raster reference persistence

The approved Companion/Records finish board is already preserved in GitHub under `design/reference/ux-v2/raster/companion_records_finish_pass_v1.webp` and registered in `design/reference/REFERENCE_IMAGE_REGISTRY_V2.md`.

Any future raster image created for this surface family is not considered repository-complete until an inspectable reference image is committed under `design/reference/` and registered with role/status/checksum.

## 8. QA gate

A screen passes this refinement only when:

- the approved hierarchy is unchanged
- Moru remains the locked Candidate 3 raster family
- no character/clue/place art is replaced by vector/Canvas drawing
- generic Material 3 card/chip styling is no longer visually dominant
- text remains legible over paper textures
- real persisted data is not decorated into fake product state
- 48dp touch targets are preserved
- TalkBack/content descriptions remain meaningful

Outdoor readability is not closed by this document; that remains a physical-device Human Gate.
