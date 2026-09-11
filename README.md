# Daily Town

Daily Town is a location-based exploration game prototype focused on real-world walking, lightweight mysteries, and companion interactions.

## Current MVP

- Android native: Kotlin + Jetpack Compose
- NAVER Maps is the selected default provider
- Map rendering is isolated behind `MapViewAdapter` so Google Maps can replace it without changing exploration/location logic
- App-owned fused-device location source with runtime permissions
- GPS quality filtering for low-accuracy samples and implausible jumps
- Replayable Seoul City Hall → Deoksugung test route
- Mystery discovery, walking-distance accumulation, duplicate discovery protection
- Content rotation rules for novelty/affinity/proximity
- Unit tests and Android CI

## Design system

Daily Town visual-design rules are managed as a single source of truth under [`docs/design/`](docs/design/README.md).

Design sessions and agents should re-read the latest design documents from `main` instead of relying only on previous chat/session memory. The design guide explicitly keeps SVG/vector production limited to true UI symbols; characters, companions, backgrounds, collectibles, rewards, mystery/place illustrations, and other game-world art use raster-oriented game art.

See [`docs/design/CHATGPT_PROJECT_MASTER_PROMPT.md`](docs/design/CHATGPT_PROJECT_MASTER_PROMPT.md) for the GitHub-first master prompt used by ChatGPT design sessions.

## Running without a map key

The app builds and the exploration replay remains usable without NAVER credentials. The map area displays a configuration placeholder.

To enable the real NAVER map, set this in your local Gradle user properties (do not commit the value):

```properties
NAVER_MAP_NCP_KEY_ID=<your-key-id>
```

See `docs/HUMAN_ACTIONS.md` for the human-owned setup/checklist.

## Build

Prerequisites: JDK 17+, Android SDK 37.

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

## Architecture

- `domain/`: pure game/exploration rules
- `location/`: provider-independent location source, replay source, GPS quality policy
- `map/MapProvider.kt`: provider-neutral rendering contract
- `map/NaverMapAdapter.kt`: only NAVER Maps SDK integration
- `ui/`: Compose exploration UI consuming provider-neutral contracts
- `docs/MAP_PROVIDER_CONTRACT.md`: Google Maps replacement boundary
- `docs/HUMAN_ACTIONS.md`: credentials, device validation, release/privacy TODOs
