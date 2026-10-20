# Daily Town — Companion & Records Finish QA v2

> Status: **DESIGN-SIDE PASS — runtime parity implementation pending**
>
> Visual authority: `docs/design/DESIGN_BASELINE_V2.md`
>
> Surface contract: `docs/design/COMPANION_RECORDS_SURFACE_V2.md`
>
> Raster finish reference: `design/reference/ux-v2/raster/companion_records_finish_pass_v1.webp`

## 1. Scope

This review closes the remaining design-side ambiguity for Companion and Records without changing the approved hierarchy.

It does not approve Android implementation, outdoor readability, Moru runtime activation, or fabricated product state.

## 2. Companion relationship-notebook review

### PASS — hierarchy

The approved finish reference preserves the required order:

1. Moru portrait / current mood
2. contextual relationship line
3. relationship level / bond progress
4. recent shared memory
5. primary/secondary actions
6. longer history below the relationship content

The screen reads as a personal field notebook rather than a stat dashboard.

### PASS — material language

- warm cream paper canvas
- restrained botanical framing
- one dominant portrait/memory artifact instead of repeated generic cards
- soft paper edge / tonal separation rather than Material 3 elevation stacks
- moss / olive / ochre progress treatment consistent with the baseline

### PASS — production boundary

- Moru remains raster content and must use Candidate 3 semantic assets/fallbacks;
- botanical/keepsake illustrations remain raster;
- buttons, tabs and navigation symbols may remain true UI vector/Compose elements;
- no unsupported memory is invented for decorative purposes.

## 3. Records A3 journal review

### PASS — hierarchy

The finish reference supports the approved journal model:

- today/recent record context
- place/discovery imagery as the dominant artifact
- clue attachment only when the domain has the clue
- Moru/shared-memory reaction
- detail continuation

It does not read as a KPI dashboard or generic collection grid.

### PASS — paper/artifact treatment

- layered paper/inset treatment is coherent with A3
- tape/stamp/botanical decoration is restrained
- one dominant artifact per region is maintained
- type remains readable over paper texture
- clues are treated as raster game content rather than flat UI icons

### PASS — small-screen discipline

The reference keeps decoration concentrated around the record artifact instead of filling every card. Runtime should preserve:

- 48dp minimum interactive targets
- 14–15sp body copy target
- 11–12sp meta copy target
- high text contrast on paper textures
- no random rotation/noise on every repeated row

## 4. Design-side completion decision

`DT-DES-CR-01` and `DT-DES-CR-02` no longer require another visual concept pass.

They should move from `QUEUED` to:

**DESIGN SPEC / FINISH REFERENCE READY — Android parity implementation pending**

Further design work on these screens is limited to QA findings from the real implementation. Do not generate another Companion/Records style board unless the locked baseline is explicitly reopened.

## 5. Development handoff delta

When Android implementation resumes, replace visible generic Material surfaces with the component language already defined in `COMPANION_RECORDS_SURFACE_V2.md`:

- `DTCompanionNotebookHero`
- `DTAffinityNote`
- `DTRecentMemoryCard`
- `DTJournalCanvas`
- `DTJournalTabs`
- `DTRecordArtifact`
- `DTClueArtifact`
- `DTMemoryStamp`

Preserve existing data ownership, navigation, test tags, semantic memory, bond and Records persistence behavior.

## 6. Remaining QA

Not closed by this review:

- actual Android screenshot parity
- TalkBack/content-description verification
- actual Moru v2 raster asset binding
- actual Old Ginkgo clue/place/memory asset binding
- physical-device outdoor readability where these surfaces intersect map/exploration

Those are implementation/runtime QA rather than a reason to reopen the design.
