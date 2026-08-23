# Human-required actions

Automation can proceed safely until external identity, legal/product approval, or physical field testing is required.

## First blocking decision: map provider

Before a real map adapter is committed, a human should select **Naver Maps, Kakao Maps, or Google Maps** and create the corresponding developer application/key.

Decision checklist:

- intended launch territory: Korea-only MVP vs near-term global expansion
- current pricing/quota confirmed in provider console/terms
- allowed use for location-based game overlays and POI display
- whether routing/geocoding is actually needed for MVP
- key restriction scheme and package/signing fingerprint

Do not paste production secrets into GitHub. Store local credentials in an ignored file or CI secret.

## Other later human actions

1. Pick 2–3 field-test neighborhoods and physically test routes.
2. Approve initial mystery themes/content safety boundaries.
3. Create/hold Android signing key.
4. Configure Play Console internal testing and tester accounts.
5. Approve location/privacy disclosure and retention policy before collecting remote telemetry.

## Recommended next implementation after map choice

Add `LocationSource`, Android Fused Location adapter, permission flow, route playback fixtures, then the selected map adapter. The domain layer should remain unchanged.
