from __future__ import annotations

import hashlib
import json
import subprocess
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
FAMILY = ROOT / "design/reference/moru-v2/native-lighting-family-v1"
METRICS = FAMILY / "qa/moru_native_lighting_acceptance_metrics_v1.json"
OUT = FAMILY / "qa/remote_binary_verification.v1.json"


def sha256(path: Path) -> str:
    h = hashlib.sha256(path.read_bytes()).hexdigest()
    return h


def git(*args: str) -> str:
    return subprocess.check_output(["git", *args], cwd=ROOT, text=True).strip()


def main() -> int:
    data = json.loads(METRICS.read_text())
    if data.get("verdict") != "PASS_MORU_02_LIGHTING_FAMILY_V1":
        raise SystemExit("LIGHTING_METRICS_NOT_PASS")
    assets = {}
    for usage, rec in data["assets"].items():
        for lighting in ("WARM_DUSK", "DARK"):
            item = rec[lighting]
            path = FAMILY / item["path"]
            actual = sha256(path)
            if actual != item["sha256"]:
                raise SystemExit(f"HASH_MISMATCH:{usage}:{lighting}")
            with Image.open(path) as im:
                if im.mode != "RGBA" or list(im.size) != item["dimensions"]:
                    raise SystemExit(f"PNG_CONTRACT_MISMATCH:{usage}:{lighting}")
                lo, hi = im.getchannel("A").getextrema()
                if [lo, hi] != [0, 255]:
                    raise SystemExit(f"ALPHA_RANGE_MISMATCH:{usage}:{lighting}")
            rel = path.relative_to(ROOT).as_posix()
            blob = git("hash-object", rel)
            assets[f"{usage}:{lighting}"] = {
                "path": rel,
                "sha256": actual,
                "git_blob_sha1": blob,
                "dimensions": item["dimensions"],
                "mode": "RGBA",
                "alpha_minmax": [0, 255],
                "bytes": path.stat().st_size,
            }
    qa = FAMILY / "qa/moru_native_lighting_family_qa_v1.png"
    if sha256(qa) != data["qa_artifacts"][qa.name]["sha256"]:
        raise SystemExit("QA_HASH_MISMATCH")
    out = {
        "version": "moru-native-lighting-remote-verification-v1-2026-09-14",
        "status": "PASS_REMOTE_BINARY_REVERIFY",
        "branch": "design/production-consolidated-v2",
        "remote_checkout_commit": git("rev-parse", "HEAD"),
        "lighting_verdict": data["verdict"],
        "assets": assets,
        "qa_artifact": {
            "path": qa.relative_to(ROOT).as_posix(),
            "sha256": sha256(qa),
            "bytes": qa.stat().st_size,
        },
        "runtime_activation": False,
    }
    OUT.write_text(json.dumps(out, indent=2) + "\n")
    print(json.dumps(out, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
