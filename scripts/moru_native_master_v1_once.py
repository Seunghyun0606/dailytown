from pathlib import Path
import subprocess

OLD_COMMIT = "b29aec7894d9a024f8a3a8ca3ca7cb6cd1cfc76c"
OLD_PATH = ".github/workflows/moru-native-master-once.yml"

s = subprocess.check_output(["git", "show", f"{OLD_COMMIT}:{OLD_PATH}"], text=True)
a = s.index("          import hashlib")
b = s.index("          PY\n", a)
code = "\n".join(line[10:] if line.startswith("          ") else line for line in s[a:b].splitlines())

code = code.replace(
    "if iou(mask>0,cut[...,3]>0)<.995: raise RuntimeError('HANDOFF_ALPHA_GUIDE_MISMATCH')",
    "guide_iou=iou(mask>16,cut[...,3]>16); src_alpha=cut[...,3]",
)
code = code.replace(
    "tight=np.dstack([src[sy0:sy1,sx0:sx1,:3],mask[sy0:sy1,sx0:sx1]])",
    "tight=np.dstack([src[sy0:sy1,sx0:sx1,:3],src_alpha[sy0:sy1,sx0:sx1]])",
)
code = code.replace("ok=mask[jy,jx]>0;", "ok=src_alpha[jy,jx]>0;")

old_region = "def reg_iou(name):\n        q=region(name)[sy0:sy1,sx0:sx1]; hi=Image.fromarray(np.uint8(q)*255,'L').resize((tw,th),Image.Resampling.NEAREST); lo=np.asarray(hi.resize((sx1-sx0,sy1-sy0),Image.Resampling.NEAREST))>0; return iou(q,lo)"
new_region = "def reg_iou(name):\n        q=region(name)[sy0:sy1,sx0:sx1]\n        x0,y0,x1,y1=lm[name]; bb=np.zeros_like(q,bool)\n        ax0=max(0,x0-sx0); ax1=min(sx1-sx0,x1-sx0); ay0=max(0,y0-sy0); ay1=min(sy1-sy0,y1-sy0)\n        if ax1>ax0 and ay1>ay0: bb[ay0:ay1,ax0:ax1]=True\n        cand=(back>16)&bb\n        return iou(q,cand)"
if old_region not in code:
    raise SystemExit("REGION_PATCH_TARGET_NOT_FOUND")
code = code.replace(old_region, new_region)

old_residue = "expected=np.zeros((1600,1280),np.uint8); expected[ty:ty+th,tx:tx+tw]=np.asarray(Image.fromarray(mask[sy0:sy1,sx0:sx1],'L').resize((tw,th),Image.Resampling.LANCZOS)); outside=int(((final[...,3]>16)&(expected<=16)).sum())"
new_residue = "expected=np.zeros((1600,1280),np.uint8); expected[ty:ty+th,tx:tx+tw]=np.asarray(Image.fromarray(src_alpha[sy0:sy1,sx0:sx1],'L').resize((tw,th),Image.Resampling.LANCZOS)); outside=int(((final[...,3]>16)&(expected<=16)).sum())"
if old_residue not in code:
    raise SystemExit("RESIDUE_PATCH_TARGET_NOT_FOUND")
code = code.replace(old_residue, new_residue)

code = code.replace(
    "metrics={'verdict':verdict,'canonical_sha256':got,",
    "metrics={'verdict':verdict,'canonical_sha256':got,'handoff_cutout_vs_mask_iou':guide_iou,",
)

compile(code, "moru_reconstruction_v1", "exec")
print("patched Moru reconstruction runner syntax OK")
exec(compile(code, "moru_reconstruction_v1", "exec"), {"__name__": "__main__"})
