# Daily Town

Daily Town is a location-based exploration game prototype focused on real-world walking, lightweight mysteries, and companion interactions.

## MVP direction

- Android-first native MVP
- Kotlin + Jetpack Compose
- Location/game rules are provider-agnostic
- Map SDK is isolated behind a `MapProvider` boundary so Naver/Kakao/Google can be selected later
- Core loop can be tested without a map API key

## Run

Prerequisites: JDK 17+, Android Studio with Android SDK 37.

```bash
./gradlew test
./gradlew assembleDebug
```

The repository intentionally does **not** require a real map key yet. The first human-required integration step is documented in `docs/HUMAN_ACTIONS.md`.

## Project structure

- `app/src/main/java/com/dailytown/app/domain`: pure exploration/domain rules
- `app/src/main/java/com/dailytown/app/ui`: Compose presentation layer
- `docs/ARCHITECTURE.md`: technical architecture and dependency boundaries
- `docs/ROADMAP.md`: staged implementation plan
- `docs/HUMAN_ACTIONS.md`: decisions/credentials that cannot be automated safely
