# Daily Town Production POI Policy

Status: adopted MVP direction (2026-09-07)

## Source model

1. **Canonical:** 한국관광공사 TourAPI `KorService2/locationBasedList2`.
2. **Enrichment:** NAVER/Kakao or other live place providers may be added only behind `EnrichingPoiRepository` after their storage, caching, attribution, and map-display terms are explicitly reviewed.
3. **Fixture:** `FixturePoiRepository` remains development/field-test data only and is not a production Source of Truth.

Canonical records win duplicate collisions. Live enrichment may fill coverage gaps but must not silently replace canonical IDs or metadata.

## TourAPI usage

- Credential: `TOUR_API_SERVICE_KEY` via Gradle property or environment only. Never commit or log the value.
- API family: `https://apis.data.go.kr/B551011/KorService2`.
- Nearby endpoint: `locationBasedList2`.
- `mapX` is longitude and `mapY` is latitude.
- Provider radius is capped at 20 km.
- Runtime requests snap the raw gameplay GPS location to a 0.02-degree neighborhood grid and add query padding. Returned records are filtered on-device by the exact requested radius through the provider-neutral cache.
- Textual POI data may be used under the public-data license summary recorded in `PoiSourceMetadata`. Images require separate rights/type review before use.

## Attribution

Repository source metadata must remain available to Settings/legal UI and release review. Current TourAPI attribution text is:

`출처: 한국관광공사 TourAPI`

Before any image is added, its 공공누리 type and subject/personality-right restrictions must be recorded separately.

## Live enrichment gate

A live provider adapter can be activated only after all are documented:

- commercial-use permission,
- cache/storage duration,
- redistribution restrictions,
- attribution requirements,
- compatibility with NAVER map display,
- API quota and failure behavior,
- whether raw or precise location leaves the device.

Until that gate passes, the enrichment list stays empty.
