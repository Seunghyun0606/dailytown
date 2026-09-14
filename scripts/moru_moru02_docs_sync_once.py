from pathlib import Path


def replace_once(path: Path, old: str, new: str):
    text = path.read_text()
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{path}: expected exactly one match, got {count}: {old[:80]!r}')
    path.write_text(text.replace(old, new, 1))

p = Path('docs/design/PRODUCTION_WORK_ITEMS_V2.md')
replace_once(p,
'Status: **IN PROGRESS — native neutral master v1 PASS; usage-context family pending; not activated**',
'Status: **IN PROGRESS — native neutral master + neutral/LIGHT/base usage-context family PASS; semantic family pending; not activated**')
replace_once(p,
'- `encounter_halfbody`, `result_large`, and final `companion_portrait` must now be derived from the accepted native neutral master and validated in MORU-02; board-crop upscaling remains forbidden\n- the neutral source-resolution/clean-transparency blocker is closed; the remaining blocker is the native usage-context family plus MORU-02 QA',
'- the six `neutral / LIGHT / base / static` usage contexts are now persisted under `design/reference/moru-v2/native-usage-context-v1/` and derive only from the accepted native neutral master; board-crop upscaling remains forbidden\n- the neutral source-resolution/clean-transparency blocker and neutral usage-context/mobile-edge baseline are closed; remaining MORU-02 work is expression/lighting/affinity consistency and semantic coverage')
replace_once(p,
'- do not remake the neutral master; derive required usage contexts only from the accepted neutral and run MORU-02 before semantic/runtime activation',
'- 2026-09-14 `PASS_MORU_02_NEUTRAL_USAGE_V1`: map 512², HUD 768², encounter 1024×1280, result 1280×1600, Companion 1024×1280 and journal 768² genuine-RGBA assets persisted under `design/reference/moru-v2/native-usage-context-v1/`; all target dimensions, alpha, distinct framing, transparent-side bleed and required feature-presence gates PASS\n- minimum-size mobile metrics now clear the prior source-crop blocker: map @48dp visible-content 43.406dp / face 7.289dp / sprout 7.375dp; HUD @56dp 49.948dp / face 13.524dp / sprout 13.685dp / scarf 9.016dp; journal @56dp 49.875dp / face 14.426dp / sprout 14.597dp / scarf 9.617dp\n- fresh checkout `PASS_REMOTE_BINARY_REVERIFY`: all six asset SHA-256 values, dimensions, RGBA alpha 0–255 and QA artifacts match the production manifest; accepted usage commit `c2e17ff28a66cb6b256ae82a77ea4687a0d44a2e`\n- do not remake the neutral master or neutral usage family; continue only with required expression/lighting/affinity production and MORU-02 consistency QA before semantic/runtime activation')
replace_once(p,
'Status: **READY — accepted native neutral exists; native usage-context/mobile QA pending**',
'Status: **IN PROGRESS — neutral/LIGHT/base native usage-context + Android-size/edge baseline PASS; expression/lighting/affinity QA pending**')
replace_once(p,
'The native neutral gate is now passed. MORU-02 must derive and evaluate native usage-context assets from `design/reference/moru-v2/native-master-v1/`; the earlier reference-derived map/HUD/journal results remain historical evidence and are not substitutes for native QA. Runtime activation remains blocked.',
'The native neutral gate and the six-context `neutral / LIGHT / base / static` Android-size/transparent-edge baseline now pass. Evidence is persisted under `design/reference/moru-v2/native-usage-context-v1/`, including cream/dark/map-heavy mobile QA and fresh remote binary re-verification. The earlier reference-derived map/HUD/journal results remain historical evidence only. MORU-02 is **not complete** until six-expression identity consistency, LIGHT/WARM_DUSK/DARK readability, and base/familiar/trusted/best_friend anatomy-invariant progression are produced and validated. Runtime activation remains blocked.')
replace_once(p,
'2. Keep the accepted neutral authority at `design/reference/moru-v2/native-master-v1/` unchanged; derive required usage contexts from it and run MORU-02 Android-size/edge QA.\n3. After MORU-02 passes, produce only the required expression/lighting/affinity family and prepare MORU-03 semantic activation readiness.',
'2. Keep both accepted authorities unchanged: `design/reference/moru-v2/native-master-v1/` and the PASS neutral usage family at `design/reference/moru-v2/native-usage-context-v1/`.\n3. Continue MORU-02 with only the required expression/lighting/affinity family, validate identity/readability/anatomy invariance and fallback coverage, then prepare MORU-03 semantic activation readiness.')

p = Path('docs/design/MORU_SOURCE_SUFFICIENCY_V2.md')
replace_once(p,
'The next valid work is native usage-context derivation and MORU-02 mobile/edge QA from that accepted neutral master. Do not regenerate or replace the accepted neutral master, and do not start semantic/runtime activation before the downstream gates pass.',
'The accepted neutral master has now also produced a passing six-context `neutral / LIGHT / base / static` native usage family under `design/reference/moru-v2/native-usage-context-v1/`. The next valid work is the remaining MORU-02 semantic family: expression, lighting and affinity production/consistency QA. Do not regenerate or replace either accepted authority, and do not start semantic/runtime activation before the downstream gates pass.')
replace_once(p,
'1. keep `design/reference/moru-v2/native-master-v1/` immutable as the accepted neutral authority;\n2. derive required usage contexts from that accepted neutral master;\n3. run MORU-02 Android-size/edge QA;\n4. only after the required neutral/usage gates pass, derive expressions, lighting and affinity as required by the resolver/fallback contract;\n5. prepare MORU-03 readiness without silently repointing the legacy profile.',
'1. keep `design/reference/moru-v2/native-master-v1/` immutable as the accepted neutral authority;\n2. keep `design/reference/moru-v2/native-usage-context-v1/` immutable as the passing neutral usage/mobile-edge baseline;\n3. author and validate only the required expression, LIGHT/WARM_DUSK/DARK and affinity variants under the locked Candidate 3 identity;\n4. verify resolver/fallback coverage and semantic invariance;\n5. prepare MORU-03 readiness without silently repointing the legacy profile.')
replace_once(p,
'The native neutral gate for `DT-DES-MORU-01` is now passed. The transparent usage-context family remains incomplete, so MORU-02 is the next active gate and MORU-03/runtime promotion remain blocked.',
'The native neutral gate for `DT-DES-MORU-01` is passed, and the six-context `neutral / LIGHT / base / static` usage/mobile-edge baseline also passes. MORU-02 remains the active gate because expression/lighting/affinity consistency is still incomplete; MORU-03/runtime promotion remain blocked.')
replace_once(p,
'Next: derive the required usage contexts only from this accepted neutral master and run MORU-02.',
'''Next: preserve this accepted neutral master and the passing neutral usage family; continue MORU-02 with expression/lighting/affinity production and consistency QA only.

## 10. Native neutral usage-context baseline PASS — 2026-09-14

Result: **PASS_MORU_02_NEUTRAL_USAGE_V1**.

- Source authority: accepted 1280×1600 neutral master SHA-256 `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692` only; no attachment, board crop, free-form generation or new semantic micro-detail was used.
- Persisted family: `design/reference/moru-v2/native-usage-context-v1/`.
- Six `neutral / LIGHT / base / static` context masters: `map_avatar` 512×512, `hud_portrait` 768×768, `encounter_halfbody` 1024×1280, `result_large` 1280×1600, `companion_portrait` 1024×1280, `journal_crop` 768×768; all genuine RGBA with alpha 0–255.
- Context-specific framing is distinct; `result_large` is a byte-for-byte copy of the accepted neutral master rather than a re-encoded lookalike.
- Transparent-side bleed QA PASS for derived contexts. Cream/dark/map-heavy review boards and framing evidence are persisted under `native-usage-context-v1/qa/`.
- Minimum-size metrics: map @48dp visible content 43.406dp / face 7.289dp / sprout 7.375dp; HUD @56dp 49.948dp / face 13.524dp / sprout 13.685dp / scarf 9.016dp; journal @56dp 49.875dp / face 14.426dp / sprout 14.597dp / scarf 9.617dp.
- The previous no-upscale reference-derived HUD/journal failures remain valid historical evidence; the accepted native source is what closes those size/readability blockers.
- Accepted usage-family commit: `c2e17ff28a66cb6b256ae82a77ea4687a0d44a2e`.
- Fresh remote checkout result: `PASS_REMOTE_BINARY_REVERIFY`; all six persisted PNG SHA-256 values, dimensions, RGBA modes/alpha ranges and QA SHA values match `design/export-spec/moru-production-manifest.v2.json`. Evidence: `design/reference/moru-v2/native-usage-context-v1/qa/remote_binary_verification.v1.json`.
- No expression, WARM_DUSK/DARK lighting, affinity progression or runtime activation was performed. Therefore full MORU-02 is still **IN PROGRESS**, not complete.

Next: produce and validate the locked semantic expression/lighting/affinity family, then verify fallback coverage before MORU-03.''')

p = Path('design/reference/REFERENCE_IMAGE_REGISTRY_V2.md')
needle='| `design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png` | 1280×1600 | `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692` | `native_neutral_master_pass_not_runtime_active`; genuine RGBA; paired 6-layer ORA + acceptance/remote-verification evidence under `native-master-v1/` |'
rows='''| `design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png` | 1280×1600 | `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692` | `native_neutral_master_pass_not_runtime_active`; genuine RGBA; paired 6-layer ORA + acceptance/remote-verification evidence under `native-master-v1/` |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_map_avatar_neutral_LIGHT_base.png` | 512×512 | `098df5b9b4b8e9cfe215c7fc8e9f1e9129b5673b7dc168893c5ea185f849d531` | `native_usage_qa_pass_not_runtime_active`; neutral/LIGHT/base map framing; 48/56/64dp QA PASS |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_hud_portrait_neutral_LIGHT_base.png` | 768×768 | `c67bac8dd8eb7e8cd13b8434b2cf92ce2ab6b82c657291109230a2a672af79ab` | `native_usage_qa_pass_not_runtime_active`; neutral/LIGHT/base HUD framing; 56/64/72dp face/sprout/scarf QA PASS |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_encounter_halfbody_neutral_LIGHT_base.png` | 1024×1280 | `00a643aa14661a9e8b511854b33c7f7f19c04230b781c786ab592e0504dd46e5` | `native_usage_qa_pass_not_runtime_active`; neutral/LIGHT/base encounter framing; satchel/pose support retained |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_result_large_neutral_LIGHT_base.png` | 1280×1600 | `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692` | `native_usage_qa_pass_not_runtime_active`; exact accepted neutral bytes used for result-large context |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_companion_portrait_neutral_LIGHT_base.png` | 1024×1280 | `b57d5732a1f65a0b1cc5f108e267c422bd0fcfdbd144c403c23627de558e6b14` | `native_usage_qa_pass_not_runtime_active`; portrait-biased neutral/LIGHT/base framing |
| `design/reference/moru-v2/native-usage-context-v1/candidate/companion_moru_v2_journal_crop_neutral_LIGHT_base.png` | 768×768 | `cbcae8d5a79bc5c68368fe0f36f57fbe385d3c35f5c069ce87a2224ba85540b4` | `native_usage_qa_pass_not_runtime_active`; neutral/LIGHT/base journal framing; 56/64/72dp paper-context QA PASS |'''
replace_once(p,needle,rows)
replace_once(p,
'- `design/reference/moru-v2/native-master-v1/` — accepted 1280×1600 neutral PNG, 6-layer ORA, acceptance metrics/QA and remote binary verification',
'- `design/reference/moru-v2/native-master-v1/` — accepted 1280×1600 neutral PNG, 6-layer ORA, acceptance metrics/QA and remote binary verification\n- `design/reference/moru-v2/native-usage-context-v1/` — six-context `neutral / LIGHT / base / static` native usage family, Android-size/edge QA and fresh remote binary verification')
replace_once(p,
'- Moru canonical source board: **exact PNG persisted**. Strict no-upscale deterministic reference QA remains **map PASS; HUD/journal FAIL** as historical evidence. Native neutral master v1 is **PASS and remote-binary-verified** at `design/reference/moru-v2/native-master-v1/`; native usage-context family/MORU-02 remain pending and runtime promotion remains blocked.',
'- Moru canonical source board: **exact PNG persisted**. Strict no-upscale deterministic reference QA remains **map PASS; HUD/journal FAIL** as historical evidence. Native neutral master v1 is **PASS and remote-binary-verified** at `design/reference/moru-v2/native-master-v1/`; the six-context `neutral / LIGHT / base / static` native usage family and Android-size/edge baseline are also **PASS and remote-binary-verified** at `design/reference/moru-v2/native-usage-context-v1/`. Expression/lighting/affinity MORU-02 coverage remains pending and runtime promotion remains blocked.')
replace_once(p,
'- Moru 48dp/expression/lighting/affinity reference precheck: **PASS**. The neutral genuine-alpha/edge gate now passes; actual native usage-context Android-size QA and semantic family fan-out remain pending under MORU-02.',
'- Moru 48dp/expression/lighting/affinity reference precheck: **PASS**. The neutral genuine-alpha gate and six-context native Android-size/edge baseline now pass; expression/lighting/affinity semantic family production and consistency QA remain pending under MORU-02.')

print('Moru MORU-02 status docs synchronized')
