#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]
BUILD = ROOT / "app" / "build.gradle.kts"
MANIFEST = ROOT / "app" / "src" / "main" / "AndroidManifest.xml"
PRIVACY = ROOT / "docs" / "privacy" / "PRIVACY_POLICY_DRAFT.md"
DATA_SAFETY = ROOT / "docs" / "privacy" / "DATA_SAFETY_DRAFT.md"
RELEASE = ROOT / "docs" / "release" / "PLAY_RELEASE_BASELINE.md"
POI_POLICY = ROOT / "docs" / "poi" / "PRODUCTION_POI_POLICY.md"


def fail(message: str) -> None:
    print(f"ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)


def require(condition: bool, message: str) -> None:
    if not condition:
        fail(message)


def main() -> None:
    for path in (BUILD, MANIFEST, PRIVACY, DATA_SAFETY, RELEASE, POI_POLICY):
        require(path.is_file(), f"required release baseline file missing: {path.relative_to(ROOT)}")

    build = BUILD.read_text(encoding="utf-8")
    manifest = MANIFEST.read_text(encoding="utf-8")

    target_match = re.search(r"\btargetSdk\s*=\s*(\d+)", build)
    require(target_match is not None, "targetSdk is not declared")
    target_sdk = int(target_match.group(1))
    require(target_sdk >= 37, f"adopted baseline requires targetSdk >= 37; found {target_sdk}")

    require("android.permission.ACCESS_FINE_LOCATION" in manifest, "foreground precise-location permission missing")
    require("android.permission.ACCESS_COARSE_LOCATION" in manifest, "foreground coarse-location permission missing")
    require("android.permission.ACCESS_BACKGROUND_LOCATION" not in manifest, "MVP baseline forbids background location")

    for token in (
        "firebase-analytics",
        "firebase-crashlytics",
        "play-services-ads",
        "app-measurement",
    ):
        require(token not in build.lower(), f"no-analytics/no-ads baseline violated by dependency token: {token}")

    for token in (
        "TOUR_API_SERVICE_KEY",
        "TOUR_API_CONFIGURED",
        "verifyTourApiCredential",
        "DAILYTOWN_POI_API_BASE_URL",
        "DAILYTOWN_POI_API_CONFIGURED",
        "verifyDailyTownPoiGateway",
        "DAILYTOWN_UPLOAD_STORE_FILE",
        "DAILYTOWN_UPLOAD_KEY_ALIAS",
        "verifyReleaseSigningConfig",
        "bundleForPlay",
    ):
        require(token in build, f"release/POI wiring missing: {token}")

    require(
        'dailyTownPoiApiBaseUrl.startsWith("https://")' in build,
        "Daily Town POI gateway configuration must reject non-HTTPS endpoints",
    )

    # Direct TourAPI access is allowed only for internal/debug field validation. The release build
    # must override both BuildConfig fields so a Play AAB cannot contain the provider service key.
    # The non-secret app-owned HTTPS gateway URL remains available to release as the canonical POI
    # boundary once an operator configures/deploys it.
    release_block = re.search(
        r'getByName\("release"\)\s*\{(?P<body>.*?)if\s*\(releaseSigningConfigured\)',
        build,
        re.DOTALL,
    )
    require(release_block is not None, "release buildType block missing")
    release_body = release_block.group("body")
    require(
        re.search(
            r'buildConfigField\(\s*"String"\s*,\s*"TOUR_API_SERVICE_KEY"\s*,\s*"\\"\\""\s*\)',
            release_body,
        ) is not None,
        "release build must override TOUR_API_SERVICE_KEY to an empty string",
    )
    require(
        re.search(
            r'buildConfigField\(\s*"boolean"\s*,\s*"TOUR_API_CONFIGURED"\s*,\s*"false"\s*\)',
            release_body,
        ) is not None,
        "release build must override TOUR_API_CONFIGURED=false",
    )
    require(
        "DAILYTOWN_POI_API_BASE_URL" not in release_body,
        "release must inherit the non-secret gateway endpoint rather than overriding it with a provider credential path",
    )

    print(
        "Play release baseline verified: targetSdk>=37, foreground-only location, "
        "no analytics/ads baseline, internal-only TourAPI credential, HTTPS app-owned POI gateway boundary, "
        "upload signing, privacy docs"
    )


if __name__ == "__main__":
    main()
