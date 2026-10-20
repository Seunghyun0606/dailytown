# Daily Town Play Data Safety Draft

Status: pre-submission review template. Final answers must be checked against the actual release APK/AAB and current third-party SDK disclosures.

## Current MVP baseline

| Data / capability | Current behavior | Submission review |
| --- | --- | --- |
| Precise/approximate location | Used during explicit foreground exploration | Declare location use; verify current Play classification and precise-location declaration |
| Background location | Not requested | Must remain absent unless a later decision explicitly changes it |
| POI provider query | TourAPI receives a neighborhood-grid query center rather than raw GPS when configured | Confirm whether this transmission is classified as collected/shared under the current Data Safety form |
| Game progress | Stored locally with Android DataStore | No Daily Town backend upload in current baseline |
| Field-test diagnostics | Created only after explicit share action; structured export excludes raw coordinates | Treat uploaded/shared files according to the separate field-test retention policy |
| Analytics | No dedicated analytics SDK in baseline | Re-run dependency audit before release |
| Crash reporting | No dedicated crash-reporting SDK in baseline | Re-run dependency audit before release |
| Advertising | No advertising SDK in baseline | Re-run dependency audit before release |

## Third-party review checklist

Before each external beta/store submission, review current disclosures for:

- NAVER Maps SDK
- Google Play services Location
- 한국관광공사 TourAPI
- Google Play / Play App Signing

For every library/service record:

- data type,
- whether data leaves the device,
- purpose,
- retention,
- encryption in transit,
- deletion controls,
- whether it is a service provider or independent third party,
- whether the current Data Safety form considers the transfer `collected` or `shared`.

## Release-blocking human fields

- [ ] Play Console account owner confirmed
- [ ] public developer/operator name confirmed
- [ ] public support email confirmed
- [ ] privacy-policy HTTPS URL published
- [ ] final release dependency/SDK list reviewed
- [ ] precise-location declaration completed when required by Play Console
- [ ] content rating / target audience / Families applicability confirmed

Do not submit this draft verbatim without completing the release-specific review.
