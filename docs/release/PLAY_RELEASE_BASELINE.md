# Daily Town Google Play Release Baseline

Status: adopted MVP direction (2026-09-07)

## Policy baseline

- Keep `targetSdk = 37`.
- Use foreground location only; do not add `ACCESS_BACKGROUND_LOCATION` for the MVP.
- Continuous precise location is used only during an explicit, user-started exploration session and stops when the session is stopped.
- Prepare the Play Console precise-location declaration before the January 27, 2027 enforcement window for Android 17/API 37 apps.
- Do not use device location for advertising or analytics.
- Initial closed beta ships without analytics/crash-reporting SDKs. Revisit only after an explicit product/privacy decision.

## Signing baseline

Use Google Play App Signing. Daily Town keeps a separate **upload key** outside the repository.

Local/CI upload-bundle inputs:

- `DAILYTOWN_UPLOAD_STORE_FILE`
- `DAILYTOWN_UPLOAD_STORE_PASSWORD`
- `DAILYTOWN_UPLOAD_KEY_ALIAS`
- `DAILYTOWN_UPLOAD_KEY_PASSWORD`

Never commit the keystore or any password. Build the upload AAB with:

```bash
gradle bundleForPlay
```

`bundleForPlay` fails closed unless all four upload-key inputs are present and the keystore file exists.

## Human-owned values still required before external beta

- Play Console developer/account owner
- upload-key custodian / recovery owner
- public operator/developer name
- public support email
- public HTTPS privacy-policy URL
- closed-beta tester list/scope

These are account/legal ownership values and must not be invented in source.

## Location declaration rationale draft

Daily Town is a real-world exploration game. During an explicit exploration session, the product continuously evaluates walking distance, nearby POI/encounter transitions, and location quality. A one-time location button or coarse-only location cannot reliably support the ongoing distance/approach thresholds that form the core exploration loop. Precise foreground access is initiated by the user and is stopped when exploration tracking ends. Background location is not requested.

This rationale is a submission draft, not legal advice. Re-check the current Play Console form immediately before submission.
