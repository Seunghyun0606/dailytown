# Daily Town Architecture

## 1. Product constraint

The MVP must validate whether walking around a familiar neighborhood can repeatedly produce meaningful exploration moments without requiring a large hand-authored content library.

## 2. Platform decision

Android native is the MVP default. This reduces uncertainty around background location, permissions, battery behavior, map SDK integration, and Play testing. Cross-platform is deferred until the gameplay loop is validated.

## 3. Dependency boundaries

```text
Compose UI
   ↓
Application state / use cases
   ↓
Pure domain (exploration, mystery, companion, content rotation)
   ↓
Ports: LocationSource / MapProvider / PoiRepository / StoryRepository
   ↓
Adapters: Android Location / Naver|Kakao|Google Map / public-data APIs / backend
```

Rules must not depend on a map SDK or Android framework. This allows replay tests with recorded routes and prevents vendor lock-in.

## 4. Map strategy

Do not select a map vendor by scattering SDK types throughout the app. Implement one adapter behind `MapProvider` after the vendor decision. Selection criteria:

1. Korean POI/map quality and Android SDK stability
2. Terms for location-based game display and caching
3. MAU/request pricing at MVP and scale
4. Marker/overlay performance
5. Geocoding/routing requirements
6. Ability to operate with public-data POIs independently

Naver/Kakao are primary Korean-market candidates; Google remains a fallback for global expansion. Vendor credentials are deliberately excluded from the bootstrap.

## 5. Content exhaustion mitigation

The content system separates a physical place from an encounter template. The same area can produce different experiences through:

- rotating mystery templates
- companion-specific reactions
- revisit state and time/weather context
- neighborhood completion sets
- low-frequency rare encounters
- generated dialogue constrained by authored scenario rules

`ContentRotation` starts this separation by ranking novelty independently from raw distance.

## 6. Privacy baseline

- Raw high-frequency location should stay on device unless a server feature explicitly requires upload.
- Persist derived visit/progress events rather than continuous traces by default.
- Never commit production API keys.
- Add explicit consent and retention policy before analytics/location backend integration.
