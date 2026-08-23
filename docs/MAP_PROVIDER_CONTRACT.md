# Map Provider Contract

## Goal

Daily Town uses NAVER Maps for the MVP, but no exploration/domain/location code may depend on NAVER SDK types.

The only provider-facing application contract is `MapViewAdapter`:

- create the provider's Android `View`
- camera movement
- mystery/POI marker rendering
- user-location rendering
- Android view lifecycle forwarding

Provider-neutral models (`GeoPoint`, `MapMarkerSpec`, `UserLocationSpec`) cross the boundary.

## Current implementation: NAVER

`NaverMapAdapter` is the only production class that imports `com.naver.maps.*`.

Current pinned SDK: `com.naver.maps:map-sdk:3.23.3`.

If the NCP Key ID is not configured, the adapter returns a placeholder view instead of initializing the SDK. This lets CI and replay development continue without credentials.

## Switching to Google Maps later

1. Add a `GoogleMapAdapter : MapViewAdapter` in the `map/google` implementation area.
2. Keep Google SDK types inside that adapter.
3. Map `GeoPoint` to Google's `LatLng`, `MapMarkerSpec` to Google markers, and `UserLocationSpec` to the chosen location overlay implementation.
4. Change only application composition (`MainActivity`/future DI container) from `NaverMapAdapter(...)` to `GoogleMapAdapter(...)`.
5. Run the same exploration/location unit tests unchanged.

Google Maps SDK for Android 20.x supports API 23+, so the current app `minSdk 26` does not require a minimum-SDK change.

## Important separation

`FusedDeviceLocationSource` belongs to Daily Town's location layer and is not a NAVER adapter detail. Replacing NAVER Maps with Google Maps therefore does not change GPS acquisition or exploration rules.

Do not use map-provider location-tracking callbacks as the authoritative exploration input. The application-owned `LocationSource` remains the source of truth.
