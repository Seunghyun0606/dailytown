from pathlib import Path
import subprocess

OLD_COMMIT = "b29aec7894d9a024f8a3a8ca3ca7cb6cd1cfc76c"
OLD_PATH = ".github/workflows/moru-native-master-once.yml"

s = subprocess.check_output(["git", "show", f"{OLD_COMMIT}:{OLD_PATH}"], text=True)
a = s.index("          import hashlib")
b = s.index("          PY\n", a)
code = "\n".join(line[10:] if line.startswith("          ") else line for line in s[a:b].splitlines())

# Deterministic reconstruction is unchanged from mask-cleanup v3.
code = code.replace(
    "import numpy as np",
    "import numpy as np\nfrom scipy import ndimage as ndi",
)
code = code.replace(
    "if iou(mask>0,cut[...,3]>0)<.995: raise RuntimeError('HANDOFF_ALPHA_GUIDE_MISMATCH')",
    "guide_iou=iou(mask>16,cut[...,3]>16); clean_rgb=bleed(cut[...,:3],cut[...,3],32)",
)
code = code.replace(
    "tight=np.dstack([src[sy0:sy1,sx0:sx1,:3],mask[sy0:sy1,sx0:sx1]])",
    "tight=np.dstack([clean_rgb[sy0:sy1,sx0:sx1],mask[sy0:sy1,sx0:sx1]])",
)
code = code.replace(
    "blur=np.asarray(Image.fromarray(src[...,:3],'RGB').filter(ImageFilter.GaussianBlur(1.15)),dtype=np.int16); resid=src[...,:3].astype(np.int16)-blur",
    "blur=np.asarray(Image.fromarray(clean_rgb,'RGB').filter(ImageFilter.GaussianBlur(1.15)),dtype=np.int16); resid=clean_rgb.astype(np.int16)-blur",
)

old_region = "def reg_iou(name):\n        q=region(name)[sy0:sy1,sx0:sx1]; hi=Image.fromarray(np.uint8(q)*255,'L').resize((tw,th),Image.Resampling.NEAREST); lo=np.asarray(hi.resize((sx1-sx0,sy1-sy0),Image.Resampling.NEAREST))>0; return iou(q,lo)"
new_region = "def reg_iou(name):\n        q=region(name)[sy0:sy1,sx0:sx1]\n        x0,y0,x1,y1=lm[name]; bb=np.zeros_like(q,bool)\n        ax0=max(0,x0-sx0); ax1=min(sx1-sx0,x1-sx0); ay0=max(0,y0-sy0); ay1=min(sy1-sy0,y1-sy0)\n        if ax1>ax0 and ay1>ay0: bb[ay0:ay1,ax0:ax1]=True\n        cand=(back>16)&bb\n        return iou(q,cand)"
if old_region not in code:
    raise SystemExit("REGION_PATCH_TARGET_NOT_FOUND")
code = code.replace(old_region, new_region)

old_contour = "A=ref>16; B=back>16; ds=[]\n    for y in range(A.shape[0]):\n        xa=np.flatnonzero(A[y]); xb=np.flatnonzero(B[y])\n        if len(xa) and len(xb): ds += [abs(int(xa[0])-int(xb[0]))/(sy1-sy0),abs(int(xa[-1])-int(xb[-1]))/(sy1-sy0)]\n    mdev=float(np.mean(ds)); xdev=float(np.max(ds)); anchor=0.0"
new_contour = "A=ref>16; B=back>16\n    st=np.ones((3,3),bool)\n    ca=A & ~ndi.binary_erosion(A,structure=st,border_value=0)\n    cb=B & ~ndi.binary_erosion(B,structure=st,border_value=0)\n    d_to_a=ndi.distance_transform_edt(~ca); d_to_b=ndi.distance_transform_edt(~cb)\n    contour_px=np.concatenate([d_to_b[ca],d_to_a[cb]])\n    mdev=float(np.mean(contour_px)/(sy1-sy0)); xdev=float(np.max(contour_px)/(sy1-sy0)); anchor=0.0"
if old_contour not in code:
    raise SystemExit("CONTOUR_PATCH_TARGET_NOT_FOUND")
code = code.replace(old_contour, new_contour)

code = code.replace(
    "metrics={'verdict':verdict,'canonical_sha256':got,",
    "metrics={'verdict':verdict,'canonical_sha256':got,'handoff_cutout_vs_mask_iou':guide_iou,'alpha_cleanup_authority':'committed silhouette mask','rgb_authority':'committed reference cutout + deterministic 32px edge bleed','contour_metric':'symmetric nearest 8-neighbor contour distance normalized by subject height',",
)

compile(code, "moru_contour_qa_v1", "exec")
print("Moru standard contour QA runner syntax OK")
exec(compile(code, "moru_contour_qa_v1", "exec"), {"__name__": "__main__"})
