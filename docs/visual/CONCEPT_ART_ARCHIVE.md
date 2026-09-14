# Daily Town concept-art archive

## Storage boundary

Concept boards are **design references, not Android runtime assets**. They are intentionally kept outside `app/src/main/res` so that draft art is not packaged into the APK and the application code does not accidentally depend on an unapproved visual direction.

External archive name:

`dailytown-concept-art-drafts-2026-08-25.zip`

The archive contains the original PNG boards plus its own README and SHA-256 manifest.

| File | Role | Runtime status |
| --- | --- | --- |
| `A0-cozy-neighborhood-baseline.png` | original cozy-neighborhood baseline | reference only |
| `A1-morning-walk-garden.png` | morning / fresh botanical anchor | reference only |
| `A2-sunset-alley-walk.png` | sunset / warm alley anchor | reference only |
| `A3-cozy-storybook-paper.png` | journal, collection, memory surface language | reference only |

SHA-256 of the archived PNGs:

```text
41f2909b8c431df50718219109d7c08f6aa35a2824a5895fedf804e0d8a3447f  A0-cozy-neighborhood-baseline.png
04c5a5c51bb178c3522e93822afaf19c5a7e41cecc8f19e424fe88df6e141c57  A1-morning-walk-garden.png
f6b94a6fbe3d1b8c77b2bfcbff4d11039595e94f9794c680d9384b6d22bac3a5  A2-sunset-alley-walk.png
7fbe0af3eadf01e4285f178cd77fe705037a9a1ca9128a795362308c1bd591d0  A3-cozy-storybook-paper.png
```

## Direction decided from the drafts

Daily Town should not have one static art direction for every hour. The same neighborhood should change atmosphere with the user's local day phase while keeping gameplay semantics, character identity, map readability, and interaction positions stable.

- A1 anchors the morning/daylight family.
- A2 anchors the sunset/evening family.
- A3 is **not** a clock phase. It is the secondary paper/storybook language for journal, collection, memory, and story-detail surfaces.
- Dawn, midday, evening, and night derivative boards are intentionally deferred to a dedicated design-art session.

No concept board in this archive is approved production artwork. Production exports must go through the semantic asset pipeline defined in `TIME_OF_DAY_VISUAL_SYSTEM.md` before being referenced by Android code.
