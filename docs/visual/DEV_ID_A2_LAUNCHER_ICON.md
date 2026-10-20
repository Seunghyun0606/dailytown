# ID-A2 Sprout Gate — Android launcher binding

Status: **bound to Android adaptive-icon resources; emulator launcher QA PASS; physical launcher check pending.**

Decision source: `design/final-human-gates-v3` →
`design/review/final-human-gates-v3/id-a/ID_A_ICON_LOCK_REVIEW.md` (human decision 2026-09-15,
launcher identity **mark-only**).

## Resources

| Resource | Source master | Notes |
| --- | --- | --- |
| `mipmap-anydpi-v26/ic_launcher.xml`, `ic_launcher_round.xml` | — | adaptive icon: background + foreground + monochrome |
| `drawable/ic_launcher_foreground.xml` | `foreground-light.svg` / `foreground-dark.svg` | 1:1 path conversion, 108×108 viewport |
| `values/id_a_identity_colors.xml` | light layers | background `#F4EBDD`, leaf `#87906A`, moss `#68704E`, path `#B18B45` |
| `values-night/id_a_identity_colors.xml` | dark layers | background `#2D342C`, mark `#F4EBDD`, path `#B18B45` |
| `drawable/ic_launcher_monochrome.xml` | `monochrome.svg` | Android 13+ themed icon |
| `drawable/ic_stat_daily_town.xml` | `monochrome.svg` re-framed | notification small icon (replaces the stock location glyph) |

`minSdk` is 26, so no raster mipmap fallbacks are needed. The mark occupies x 37–72 / y 26–85 of the
108 canvas: inside the centred 66×66 safe zone, core mark height ≈ 58 units (48–66 target).

Light/dark switching follows the system night mode where the launcher resolves app resources with
the current configuration; launchers that cache icons may keep the light layers until refreshed.

## QA

`app/src/androidTest/.../visualqa/IdA2LauncherIconQaTest.kt` (runs in `connectedDebugAndroidTest`):

- application icon is an `AdaptiveIconDrawable` (monochrome asserted on API 33+);
- foreground opaque bounds stay inside the 66/108 safe zone and use the 48–66 unit target;
- light/dark background colours match the locked palette;
- masked icon remains recognisable at 48 / 32 / 24 px in light and dark;
- notification icon is a white-only silhouette filling its 24 dp frame.

Pass `-e dailytownCapture true` to write review PNGs to the app's external files dir (`captures/id-a2`).

## Not done here

- Wordmark lockups (`W1`/`W2`) — explicitly non-blocking and not locked.
- Store listing icon (512 px PNG) and feature graphic — release/store task.
- Physical launcher check on a real device (shape masks, themed icons on Android 13+).
