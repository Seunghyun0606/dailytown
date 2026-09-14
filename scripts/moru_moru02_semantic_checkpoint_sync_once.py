from __future__ import annotations

import hashlib
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
HANDOFF = ROOT / "design/reference/moru-v2/native-semantic-authoring-handoff-v1"
CONTRACT = HANDOFF / "semantic-authoring-contract.v1.json"
VERIFY = HANDOFF / "qa/remote_binary_verification.v1.json"
WORK = ROOT / "docs/design/PRODUCTION_WORK_ITEMS_V2.md"
SOURCE = ROOT / "docs/design/MORU_SOURCE_SUFFICIENCY_V2.md"
REGISTRY = ROOT / "design/reference/REFERENCE_IMAGE_REGISTRY_V2.md"


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify_handoff() -> dict:
    c = json.loads(CONTRACT.read_text())
    if c.get("status") != "HANDOFF_READY_EXPRESSION_AFFINITY_NOT_PRODUCED":
        raise SystemExit("HANDOFF_STATUS_MISMATCH")
    files = {}
    for group in ("expressions", "affinity"):
        for name, rec in c["reference_crops"][group].items():
            path = HANDOFF / rec["path"]
            if sha256(path) != rec["sha256"]:
                raise SystemExit(f"CROP_HASH_MISMATCH:{group}:{name}")
            with Image.open(path) as im:
                if list(im.size) != rec["dimensions"] or im.mode != rec["mode"]:
                    raise SystemExit(f"CROP_METADATA_MISMATCH:{group}:{name}")
            files[f"{group}:{name}"] = {
                "path": path.relative_to(ROOT).as_posix(),
                "sha256": rec["sha256"],
                "dimensions": rec["dimensions"],
                "mode": rec["mode"],
                "bytes": path.stat().st_size,
            }
    qa_name, qa_rec = next(iter(c["qa_artifacts"].items()))
    qa = HANDOFF / "qa" / qa_name
    if sha256(qa) != qa_rec["sha256"]:
        raise SystemExit("HANDOFF_QA_HASH_MISMATCH")
    for key in ("accepted_neutral_png", "accepted_neutral_ora"):
        rec = c["authorities"][key]
        path = ROOT / rec["path"]
        if sha256(path) != rec["sha256"]:
            raise SystemExit(f"AUTHORITY_HASH_MISMATCH:{key}")
    out = {
        "version": "moru-semantic-authoring-handoff-remote-verification-v1-2026-09-14",
        "status": "PASS_REMOTE_BINARY_REVERIFY",
        "handoff_status": c["status"],
        "reference_files": files,
        "qa_artifact": {"path": qa.relative_to(ROOT).as_posix(), "sha256": qa_rec["sha256"], "bytes": qa.stat().st_size},
        "accepted_neutral_png_sha256": c["authorities"]["accepted_neutral_png"]["sha256"],
        "accepted_neutral_ora_sha256": c["authorities"]["accepted_neutral_ora"]["sha256"],
        "lighting_status": c["authorities"]["accepted_lighting_family"]["status"],
        "runtime_activation": False,
    }
    VERIFY.write_text(json.dumps(out, indent=2) + "\n")
    return out


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        raise SystemExit(f"DOC_SYNC_PATTERN_MISSING:{label}")
    return text.replace(old, new, 1)


def patch_docs():
    work = WORK.read_text()
    work = replace_once(
        work,
        "Status: **IN PROGRESS — native neutral master + neutral/LIGHT/base usage-context family PASS; semantic family pending; not activated**",
        "Status: **IN PROGRESS — native neutral + neutral usage + LIGHT/WARM_DUSK/DARK lighting family PASS; expression/affinity authoring pending; not activated**",
        "moru01-status",
    )
    work = replace_once(
        work,
        "- the neutral source-resolution/clean-transparency blocker and neutral usage-context/mobile-edge baseline are closed; remaining MORU-02 work is expression/lighting/affinity consistency and semantic coverage",
        "- the neutral source-resolution/clean-transparency blocker, neutral usage-context/mobile-edge baseline, and LIGHT/WARM_DUSK/DARK lighting family are closed; remaining MORU-02 work is expression/affinity authoring, consistency and semantic coverage",
        "moru01-remaining",
    )
    anchor = "- fresh checkout `PASS_REMOTE_BINARY_REVERIFY`: all six asset SHA-256 values, dimensions, RGBA alpha 0–255 and QA artifacts match the production manifest; accepted usage commit `c2e17ff28a66cb6b256ae82a77ea4687a0d44a2e`\n"
    insert = anchor + "- 2026-09-14 `PASS_MORU_02_LIGHTING_FAMILY_V1`: canonical LIGHT/WARM_DUSK/DARK reference row calibrated a deterministic Lab transfer; 12 WARM_DUSK/DARK assets across all six usage contexts PASS with LIGHT-identical alpha/geometry and face/sprout readability preserved; evidence: `design/reference/moru-v2/native-lighting-family-v1/`\n- lighting family fresh-checkout binary re-verification PASS; runtime activation remains false\n- expression/affinity authoring handoff is ready at `design/reference/moru-v2/native-semantic-authoring-handoff-v1/`: 6 exact expression crops + 4 exact affinity crops, accepted neutral ORA layer inventory, compact authoring plan (5 new expression masters + 3 affinity overlay stages), acceptance contract and QA board; crops are guide-only and must not be upscaled into native masters\n"
    work = replace_once(work, anchor, insert, "moru01-lighting-insert")
    work = replace_once(
        work,
        "- do not remake the neutral master or neutral usage family; continue only with required expression/lighting/affinity production and MORU-02 consistency QA before semantic/runtime activation",
        "- do not remake the neutral master, neutral usage family, or accepted lighting family; consume the semantic authoring handoff for five non-neutral expression masters + three restrained affinity overlay stages, then run MORU-02 cross-family/fallback QA before semantic/runtime activation",
        "moru01-next",
    )
    work = replace_once(
        work,
        "Status: **IN PROGRESS — neutral/LIGHT/base native usage-context + Android-size/edge baseline PASS; expression/lighting/affinity QA pending**",
        "Status: **IN PROGRESS — neutral usage/mobile-edge + LIGHT/WARM_DUSK/DARK lighting family PASS; expression/affinity QA pending**",
        "moru02-status",
    )
    old_para = "The native neutral gate and the six-context `neutral / LIGHT / base / static` Android-size/transparent-edge baseline now pass. Evidence is persisted under `design/reference/moru-v2/native-usage-context-v1/`, including cream/dark/map-heavy mobile QA and fresh remote binary re-verification. The earlier reference-derived map/HUD/journal results remain historical evidence only. MORU-02 is **not complete** until six-expression identity consistency, LIGHT/WARM_DUSK/DARK readability, and base/familiar/trusted/best_friend anatomy-invariant progression are produced and validated. Runtime activation remains blocked."
    new_para = "The native neutral gate, six-context `neutral / LIGHT / base / static` Android-size/transparent-edge baseline, and deterministic `LIGHT / WARM_DUSK / DARK` lighting family now pass. Evidence is persisted under `design/reference/moru-v2/native-usage-context-v1/` and `design/reference/moru-v2/native-lighting-family-v1/`, both with fresh remote binary re-verification. The earlier reference-derived map/HUD/journal results remain historical evidence only. MORU-02 is **not complete** until the five non-neutral expression masters and familiar/trusted/best_friend affinity overlays are authored from the semantic handoff and cross-family identity/fallback QA passes. Runtime activation remains blocked."
    work = replace_once(work, old_para, new_para, "moru02-paragraph")
    current_old = "2. Keep the accepted neutral authority at `design/reference/moru-v2/native-master-v1/` unchanged; derive required usage contexts from it and run MORU-02 Android-size/edge QA.\n3. After MORU-02 passes, produce only the required expression/lighting/affinity family and prepare MORU-03 semantic activation readiness."
    current_new = "2. Keep the accepted neutral, neutral usage-context, and lighting authorities unchanged; consume `design/reference/moru-v2/native-semantic-authoring-handoff-v1/` to author the five non-neutral expression masters + three restrained affinity overlay stages and finish MORU-02 cross-family/fallback QA.\n3. After full MORU-02 passes, prepare MORU-03 semantic activation readiness without silently repointing the legacy profile."
    work = replace_once(work, current_old, current_new, "current-next-action")
    WORK.write_text(work)

    source = SOURCE.read_text()
    section = """

## 10. Native lighting family + semantic authoring handoff — 2026-09-14

Lighting result: **PASS_MORU_02_LIGHTING_FAMILY_V1**.

- Source authority: accepted `neutral / LIGHT / base / static` native usage family only.
- Canonical lighting-row crops calibrated deterministic Lab-space transfer from LIGHT to WARM_DUSK/DARK.
- 12 new assets were produced for all six usage contexts × two new lighting states.
- Alpha and geometry are unchanged from each LIGHT source; no expression or affinity pixels were invented.
- WARM_DUSK warmth/readability and DARK luminance/face/sprout texture checks PASS across all six contexts.
- Evidence: `design/reference/moru-v2/native-lighting-family-v1/`; fresh remote binary re-verification PASS.

Expression/affinity art is **not** fabricated from the low-resolution board crops. Instead, `design/reference/moru-v2/native-semantic-authoring-handoff-v1/` now contains six exact expression reference crops, four exact affinity reference crops, the accepted 6-layer ORA inventory, an authoring/acceptance contract and a QA reference board. The compact authoring source plan is five new 1280×1600 LIGHT/base expression masters plus three restrained affinity overlay stages; lighting and six usage contexts remain deterministic fan-out steps after semantic master acceptance.

Next valid production step: author the five non-neutral expression masters against the accepted neutral identity, then the three affinity overlay stages, run cross-family identity/mobile/fallback QA, and only after full MORU-02 PASS prepare MORU-03. Runtime activation remains false.
"""
    if "## 10. Native lighting family + semantic authoring handoff — 2026-09-14" not in source:
        SOURCE.write_text(source.rstrip() + section + "\n")

    registry = REGISTRY.read_text()
    row_anchor = "| `design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png` | 1280×1600 | `b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692` | `native_neutral_master_pass_not_runtime_active`; genuine RGBA; paired 6-layer ORA + acceptance/remote-verification evidence under `native-master-v1/` |\n"
    if "native-lighting-family-v1/qa/moru_native_lighting_family_qa_v1.png" not in registry:
        extra = row_anchor + "| `design/reference/moru-v2/native-lighting-family-v1/qa/moru_native_lighting_family_qa_v1.png` | QA board | `563a66c61c3c0dc4c98d09c5e0ef837d6e2f09c5ee6f994507e6eff12b985dd7` | `PASS_MORU_02_LIGHTING_FAMILY_V1`; LIGHT/WARM_DUSK/DARK native lighting QA, not runtime-active |\n| `design/reference/moru-v2/native-semantic-authoring-handoff-v1/qa/moru_semantic_authoring_reference_board_v1.png` | QA/reference board | `60baff5ee79a93a0259b11ef6c0e5724a4e216f50b0f5177253b0e73ecc95a50` | semantic authoring handoff reference; exact expression/affinity crop guide, not a production master |\n"
        registry = replace_once(registry, row_anchor, extra, "registry-rows")
    old_status = "- Moru canonical source board: **exact PNG persisted**. Strict no-upscale deterministic reference QA remains **map PASS; HUD/journal FAIL** as historical evidence. Native neutral master v1 is **PASS and remote-binary-verified** at `design/reference/moru-v2/native-master-v1/`; native usage-context family/MORU-02 remain pending and runtime promotion remains blocked."
    new_status = "- Moru canonical source board: **exact PNG persisted**. Strict no-upscale deterministic reference QA remains **map PASS; HUD/journal FAIL** as historical evidence. Native neutral master v1, six-context neutral usage family, and LIGHT/WARM_DUSK/DARK native lighting family are **PASS and remote-binary-verified**; expression/affinity authoring remains pending under MORU-02 and runtime promotion remains blocked."
    registry = replace_once(registry, old_status, new_status, "registry-status")
    REGISTRY.write_text(registry)


def main():
    result = verify_handoff()
    patch_docs()
    print(json.dumps({"verification": result["status"], "docs_synced": True}, indent=2))


if __name__ == "__main__":
    main()
