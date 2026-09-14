# Daily Town · Development Handoff · Rebaseline v2 Candidate

Status: design handoff candidate. **No Android/Kotlin runtime modification is authorized by this document.** Canonical Moru v2 family and product parity still require Human Gate approval.

## 1. Moru canonical

Human selection pending. Recommended family: **MR-A · Sprout Scout**.

Do not replace the current production/fallback Moru until a family is explicitly approved and exported.

Candidate source:

- `design/source/companion/moru/moru-canonical-rebaseline-v2-candidates.svg`
- QA: `docs/visual/MORU_V2_FIT_QA_CANDIDATE.md`

## 2. Semantic asset contract to preserve

Reuse the existing runtime semantic selectors rather than introducing screen-specific filenames.

- expressions: `neutral`, `happy`, `curious`, `surprised`, `clue_found`, `resolved`
- lighting: `LIGHT`, `WARM_DUSK`, `DARK`
- affinity: `base`, `familiar`, `trusted`, `best_friend`
- missing expression → `neutral`
- missing lighting → `LIGHT`
- missing cosmetic → `none`
- missing animation → `static_semantic_pose`

The existing v1 manifest profile `companion.moru.canonical.a2` is a legacy production reference/rollback target. When v2 is approved, add a new canonical profile/version; do not silently repoint or overwrite the v1 source master.

Recommended v2 resolver dimensions remain `companion_id × expression × lighting × affinity × usage_context`, with motion treated separately from static visual selection.

Usage contexts to preserve:

- map avatar
- HUD portrait
- encounter half-body
- result large
- journal stamp

## 3. Five-tab screen hierarchy

### Explore

`Map provider surface` → `route / POI / encounter markers` → `time-of-day halo treatment` → `compact expedition header` → `Moru reaction HUD` → `single primary exploration/session control` → `DailyTown bottom navigation`.

Rules:

- NAVER map stays visually dominant.
- Do not make the map a fantasy map or apply a heavy time-of-day filter.
- Avoid stacked dashboard cards.
- During active walking, Moru is a map-edge companion vignette, not a large center card.
- State must use shape/icon/label/weight in addition to color.

### Companion

`Field-notebook portrait` → `identity caption` → `relationship/affinity strip` → `keepsake artifacts` → `walked together/shared discoveries` → `one dominant recent memory` → navigation.

Do not model this as a stat dashboard.

### Records

Default landing: `Journal Home`.

Nested artifact surfaces:

- `Discovery Detail`
- `Clue Note`
- `Collection Grid`
- `Memory Detail`

A3 paper is the content surface language, not a time-of-day theme.

### Goals

`Today's Walk` → `small neighborhood checklist` → `This Week` discovery/return/walk goals → lightweight completion stamps/checks → navigation.

No KPI wall, currency dashboard, or enterprise task-card styling.

### Settings

`App` utility group → `Location & Map` → `Motion / reduced motion` → `Notifications` → `Privacy / provider attribution` → `About` → isolated `Debug / Field Test` utility area where applicable.

Decoration is intentionally minimal. Legal/provider text stays plain and unambiguous.

## 4. Candidate design token mapping

Machine-readable candidate source: `design/export-spec/visual-tokens.v2.candidate.json`.

Required component family:

- `DTBottomNav`
- `DTMapHUD`
- `DTFieldNote`
- `DTPaperCard`
- `DTExpeditionChecklist`
- `DTCompanionNotebook`
- `DTClueBadge`
- `DTSectionHeader`
- `DTPrimaryCTA`

These are product visual roles. Implement using existing platform primitives where appropriate, but the visible result must not be the default Material 3 NavigationBar/ElevatedCard/chip/CTA styling.

## 5. Existing Compose surfaces to replace visually after approval

After Human Gate only:

- visible default Material 3 bottom navigation treatment → `DTBottomNav` visual language;
- generic ElevatedCard/card-wall presentation in primary tabs → DailyTown paper/field-note/notebook composition;
- generic chips/badges → semantic field tags/stamps with non-color cues;
- generic primary buttons → `DTPrimaryCTA` sizing/shape/contrast;
- generic section titles → `DTSectionHeader` field-label hierarchy;
- companion stat blocks → notebook portrait/memory/keepsake composition;
- goals KPI/task dashboard composition → expedition checklist;
- Records cards → A3 artifact hierarchy.

## 6. Existing functionality/components to preserve

Design application must not change domain/gameplay behavior. Preserve:

- NAVER Map provider abstraction / provider-neutral boundary;
- location tracking and filtering;
- POI repository and provider/data abstractions;
- encounter/mystery state machine;
- anti-repeat behavior;
- persistence and relationship/memory state;
- goals/history/reminder semantics;
- field-test tools;
- QA/debug tools and replay/evidence workflows;
- semantic companion resolver and fallback chain;
- existing production asset fallback;
- provider attribution, privacy and licensing behavior.

If visual restructuring would require changing any of these behaviors, stop and treat that as a separate product/runtime decision.

## 7. Reduced motion

- All semantic companion states require a static pose.
- Repeated bounce/scale pulse is removed.
- Use static semantic pose + opacity/halo transition <=160ms when reduced motion is enabled.
- Route/marker/gameplay state must remain understandable with animation fully disabled.

## 8. Accessibility / readability

- minimum interactive touch target: 48dp;
- critical outdoor text: target >=12sp equivalent and strong local contrast;
- selected/locked/completed/active states: never color only;
- map HUD must not cover provider attribution or critical map controls;
- Moru 48dp derivative must preserve sprout/head frame + explorer diagonal + base/boots;
- DARK companion: readable face and eyes, stable outline/rim, separated scarf/bag values;
- test font scaling at 1.0, 1.3 and 1.5 before visual sign-off;
- avoid dense decoration in direct-sunlight/active-walking surfaces.

## 9. Android visual QA cases after implementation is authorized

Minimum candidate matrix:

1. all five tabs at default text scale;
2. Explore at `MORNING`, `SUNSET`, `EVENING`, `NIGHT`;
3. Explore sparse/dense/green-space map backgrounds;
4. active discovery with route + marker + Moru HUD simultaneously;
5. all six Moru expressions in at least `LIGHT` and `DARK`;
6. all four affinity stages with silhouette comparison;
7. 48dp Moru against light, green and dark backgrounds;
8. reduced-motion enabled;
9. font scaling 1.3 and 1.5;
10. fallback resolution with one missing expression/lighting/motion asset.

## 10. Outdoor physical QA cases

Human approval required on a real Android device before final production promotion:

- bright midday sunlight;
- warm sunset / low-angle glare;
- evening/night street lighting;
- dense urban map labels;
- tree/park green map background;
- walking glance test at normal arm distance;
- route/marker/HUD overlap state;
- 48dp Moru recognition without zooming;
- provider attribution/control visibility;
- normal motion vs reduced motion.

## 11. Do not implement yet

Until Human Gate approval, do **not**:

- promote any Moru v2 candidate to production;
- overwrite `moru-a2-vector-source-master-v1.svg` or v1 production exports;
- change the semantic resolver to assume v2 exists;
- finalize motion timing/intensity as runtime constants;
- promote map marker visual changes based only on emulator review;
- lock App Icon / Daily Town logo;
- adopt Rive, Lottie, sprite atlas, or another runtime dependency solely from this design session;
- merge PR #10.

## 12. Human gates required before runtime application

1. Moru canonical family: MR-A / MR-B / MR-C.
2. Five-tab parity composition direction.
3. M-B motion timing/intensity candidate.
4. Outdoor readability physical sign-off.
5. ID-A App Icon / Daily Town logo final lockup.
