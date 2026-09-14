from __future__ import annotations

import hashlib
import json
import math
import shutil
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter
from scipy.ndimage import distance_transform_edt

ROOT = Path.cwd()
MASTER = ROOT / "design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png"
ORA = ROOT / "design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.ora"
MASTER_SHA = "b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692"
ORA_SHA = "2b64df529c308039fda608dbbde7514e7384b89e93ff76f384f98beb8e122505"
USAGE_ROOT = ROOT / "design/reference/moru-v2/native-usage-context-v1"
LIGHTING_ROOT = ROOT / "design/reference/moru-v2/native-lighting-family-v1"
HANDOFF = ROOT / "design/reference/moru-v2/native-semantic-authoring-handoff-v1/semantic-authoring-contract.v1.json"
OUT = ROOT / "design/reference/moru-v2/native-semantic-family-v1"
TMP = Path("/tmp/moru-semantic-family-v1")
PROD = ROOT / "design/export-spec/moru-production-manifest.v2.json"
MORU_MANIFEST = ROOT / "design/reference/moru-v2/manifest.json"
DENSITY = 2.625
EXPRESSIONS = ["neutral", "happy", "curious", "surprised", "clue_found", "resolved"]
AFFINITY = ["base", "familiar", "trusted", "best_friend"]
LIGHTINGS = ["LIGHT", "WARM_DUSK", "DARK"]
USAGE_FILES = {
    "map_avatar": "companion_moru_v2_map_avatar_neutral_LIGHT_base.png",
    "hud_portrait": "companion_moru_v2_hud_portrait_neutral_LIGHT_base.png",
    "encounter_halfbody": "companion_moru_v2_encounter_halfbody_neutral_LIGHT_base.png",
    "companion_portrait": "companion_moru_v2_companion_portrait_neutral_LIGHT_base.png",
    "journal_crop": "companion_moru_v2_journal_crop_neutral_LIGHT_base.png",
    "result_large": "companion_moru_v2_result_large_neutral_LIGHT_base.png",
}
LIGHTING_FILES = {
    "map_avatar": "companion_moru_v2_map_avatar_neutral_{lighting}_base.png",
    "hud_portrait": "companion_moru_v2_hud_portrait_neutral_{lighting}_base.png",
    "encounter_halfbody": "companion_moru_v2_encounter_halfbody_neutral_{lighting}_base.png",
    "companion_portrait": "companion_moru_v2_companion_portrait_neutral_{lighting}_base.png",
    "journal_crop": "companion_moru_v2_journal_crop_neutral_{lighting}_base.png",
    "result_large": "companion_moru_v2_result_large_neutral_{lighting}_base.png",
}


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def bbox(img: Image.Image, threshold: int = 8):
    a = np.asarray(img.getchannel("A"))
    ys, xs = np.nonzero(a > threshold)
    return (int(xs.min()), int(ys.min()), int(xs.max()) + 1, int(ys.max()) + 1)


def bleed(img: Image.Image, r: int = 16) -> Image.Image:
    arr = np.array(img, dtype=np.uint8)
    a = arr[..., 3]
    vis = a > 0
    tr = ~vis
    if not vis.any() or not tr.any():
        return img
    dist, idx = distance_transform_edt(tr, return_indices=True)
    ring = tr & (dist <= r)
    yy, xx = idx
    nearest = arr[yy, xx, :3]
    arr[..., :3][ring] = nearest[ring]
    return Image.fromarray(arr, "RGBA")


def edge_err(img: Image.Image, r: int = 8) -> float:
    arr = np.asarray(img, dtype=np.int16)
    a = arr[..., 3]
    vis = a > 0
    tr = ~vis
    dist, idx = distance_transform_edt(tr, return_indices=True)
    ring = tr & (dist <= r)
    if not ring.any():
        return 0.0
    yy, xx = idx
    near = arr[yy, xx, :3]
    d = np.abs(arr[..., :3] - near).mean(2)
    return float(d[ring].mean())


def srgb_to_linear(x: np.ndarray) -> np.ndarray:
    x = x / 255.0
    return np.where(x <= 0.04045, x / 12.92, ((x + 0.055) / 1.055) ** 2.4)


def linear_to_srgb(x: np.ndarray) -> np.ndarray:
    x = np.clip(x, 0.0, 1.0)
    return np.where(x <= 0.0031308, x * 12.92, 1.055 * (x ** (1 / 2.4)) - 0.055)


def rgb_to_lab(rgb: np.ndarray) -> np.ndarray:
    lin = srgb_to_linear(rgb.astype(np.float64))
    m = np.array([[0.4124564, 0.3575761, 0.1804375], [0.2126729, 0.7151522, 0.0721750], [0.0193339, 0.1191920, 0.9503041]])
    xyz = lin @ m.T
    xyz /= np.array([0.95047, 1.0, 1.08883])
    eps = 216 / 24389
    k = 24389 / 27
    f = np.where(xyz > eps, np.cbrt(xyz), (k * xyz + 16) / 116)
    return np.stack([116 * f[..., 1] - 16, 500 * (f[..., 0] - f[..., 1]), 200 * (f[..., 1] - f[..., 2])], axis=-1)


def lab_to_rgb(lab: np.ndarray) -> np.ndarray:
    fy = (lab[..., 0] + 16) / 116
    fx = fy + lab[..., 1] / 500
    fz = fy - lab[..., 2] / 200
    eps = 216 / 24389
    k = 24389 / 27
    def invf(t):
        t3 = t ** 3
        return np.where(t3 > eps, t3, (116 * t - 16) / k)
    xyz = np.stack([invf(fx), invf(fy), invf(fz)], axis=-1)
    xyz *= np.array([0.95047, 1.0, 1.08883])
    mi = np.array([[3.2404542, -1.5371385, -0.4985314], [-0.9692660, 1.8760108, 0.0415560], [0.0556434, -0.2040259, 1.0572252]])
    lin = xyz @ mi.T
    return np.clip(np.round(linear_to_srgb(lin) * 255.0), 0, 255).astype(np.uint8)


def relative_luminance(rgb: np.ndarray) -> np.ndarray:
    lin = srgb_to_linear(rgb.astype(np.float64))
    return 0.2126 * lin[..., 0] + 0.7152 * lin[..., 1] + 0.0722 * lin[..., 2]


def apply_transfer(img: Image.Image, params: dict, lighting: str) -> Image.Image:
    rgba = np.array(img.convert("RGBA"), dtype=np.uint8)
    rgb = rgba[..., :3]
    alpha = rgba[..., 3].copy()
    lab = rgb_to_lab(rgb)
    mu0 = np.array(params["mu_light"], dtype=np.float64)
    scale = np.array(params["scale"], dtype=np.float64)
    delta = np.array(params["delta"], dtype=np.float64)
    mapped = (lab - mu0) * scale + mu0 + delta
    blend = float(params["blend"])
    out_lab = lab * (1.0 - blend) + mapped * blend
    if lighting == "DARK":
        out_lab[..., 0] = np.maximum(out_lab[..., 0], np.clip(lab[..., 0] * 0.46, 13.0, 42.0))
    return Image.fromarray(np.dstack([lab_to_rgb(out_lab), alpha]).astype(np.uint8), "RGBA")


def fit_usage(src: Image.Image, sub, fr, target, fill, anchor):
    x0, y0, x1, y1 = sub
    sw, sh = x1 - x0, y1 - y0
    px, py = round(sw * 0.02), round(sh * 0.02)
    a, b, c, d = fr
    cb = (max(0, round(x0 + sw * a) - px), max(0, round(y0 + sh * b) - py), min(1280, round(x0 + sw * c) + px), min(1600, round(y0 + sh * d) + py))
    cr = src.crop(cb)
    cw, ch = cr.size
    tw, th = target
    s = min(tw * fill[0] / cw, th * fill[1] / ch)
    rw, rh = round(cw * s), round(ch * s)
    rs = cr.resize((rw, rh), Image.Resampling.LANCZOS)
    can = Image.new("RGBA", target, (0, 0, 0, 0))
    ox = (tw - rw) // 2
    oy = max(8, round(th * .02)) if anchor == "top" else th - rh - max(8, round(th * .02))
    can.alpha_composite(rs, (ox, oy))
    return bleed(can, max(12, round(s * 6)))


def derive_usage(src: Image.Image, usage: str) -> Image.Image:
    sub = bbox(src)
    specs = {
        "map_avatar": ((512, 512), (0, 0, 1, 1), (.90, .94), "bottom"),
        "hud_portrait": ((768, 768), (0, 0, 1, .60), (.92, .92), "top"),
        "encounter_halfbody": ((1024, 1280), (0, 0, 1, .78), (.90, .94), "top"),
        "companion_portrait": ((1024, 1280), (.02, 0, .98, .70), (.92, .94), "top"),
        "journal_crop": ((768, 768), (.08, 0, .92, .56), (.94, .92), "top"),
    }
    if usage == "result_large":
        return src.copy()
    return fit_usage(src, sub, *specs[usage])


def ellipse_mask(h, w, cx, cy, rx, ry):
    yy, xx = np.ogrid[:h, :w]
    return ((xx - cx) / rx) ** 2 + ((yy - cy) / ry) ** 2 <= 1


def skin_fill(arr: np.ndarray, cx, cy, rx, ry, ring_expand=16, noise=1.0, feather=4):
    h, w = arr.shape[:2]
    inner = ellipse_mask(h, w, cx, cy, rx, ry)
    outer = ellipse_mask(h, w, cx, cy, rx + ring_expand, ry + ring_expand)
    ring = outer & ~inner
    rgb = arr[..., :3]
    skin = (rgb[..., 0] > 155) & (rgb[..., 1] > 100) & (rgb[..., 2] > 85) & (rgb[..., 0] > rgb[..., 1] + 4) & (rgb[..., 1] > rgb[..., 2] - 15)
    pts = np.argwhere(ring & skin)
    if len(pts) < 60:
        raise SystemExit("FACE_SKIN_SAMPLE_INSUFFICIENT")
    y = pts[:, 0].astype(float)
    x = pts[:, 1].astype(float)
    A = np.stack([np.ones_like(x), x, y], axis=1)
    coeff = np.array([np.linalg.lstsq(A, rgb[pts[:, 0], pts[:, 1], c], rcond=None)[0] for c in range(3)])
    Y, X = np.mgrid[:h, :w]
    pred = np.stack([coeff[c, 0] + coeff[c, 1] * X + coeff[c, 2] * Y for c in range(3)], axis=-1)
    tex = (np.sin(X * .31 + Y * .17) + np.sin(X * .11 - Y * .23)) * .5 * noise
    pred = np.clip(pred + tex[..., None], 0, 255)
    m = Image.fromarray((inner * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(feather))
    mf = np.array(m).astype(float) / 255.0
    out = arr.copy()
    out[..., :3] = rgb * (1 - mf[..., None]) + pred * mf[..., None]
    out[..., 3] = arr[..., 3]
    return out


def aa_overlay(size, draw_fn, blur=.25, scale=4):
    ov = Image.new("RGBA", (size[0] * scale, size[1] * scale), (0, 0, 0, 0))
    d = ImageDraw.Draw(ov)
    draw_fn(d, scale)
    ov = ov.resize(size, Image.Resampling.LANCZOS)
    return ov.filter(ImageFilter.GaussianBlur(blur)) if blur else ov


def closed_eye_face(face: Image.Image) -> Image.Image:
    arr = np.array(face, dtype=np.float32)
    arr = skin_fill(arr, 107, 132, 44, 48)
    arr = skin_fill(arr, 251, 132, 44, 48)
    img = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA")
    dark = (68, 43, 33, 255)
    def draw(d, s):
        d.arc([72*s, 112*s, 145*s, 160*s], start=198, end=342, fill=dark, width=5*s)
        d.arc([216*s, 112*s, 289*s, 160*s], start=198, end=342, fill=dark, width=5*s)
        d.line([(76*s, 137*s), (69*s, 143*s)], fill=dark, width=2*s)
        d.line([(285*s, 137*s), (292*s, 143*s)], fill=dark, width=2*s)
    return Image.alpha_composite(img, aa_overlay(img.size, draw))


def mouth_face(face: Image.Image, mode: str) -> Image.Image:
    arr = np.array(face, dtype=np.float32)
    arr = skin_fill(arr, 173, 198, 66, 42, 18, 1.0, 5)
    img = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA")
    md, mi, mh = (112, 55, 45, 255), (212, 117, 101, 255), (250, 190, 177, 180)
    def draw(d, s):
        if mode == "curious":
            d.ellipse([164*s,188*s,180*s,205*s], fill=md); d.ellipse([168*s,191*s,177*s,201*s], fill=mi); d.ellipse([170*s,191*s,175*s,194*s], fill=mh)
        elif mode == "surprised":
            d.ellipse([158*s,182*s,188*s,213*s], fill=md); d.ellipse([163*s,187*s,183*s,208*s], fill=mi); d.ellipse([168*s,188*s,179*s,193*s], fill=mh)
        elif mode == "resolved":
            d.arc([145*s,180*s,202*s,215*s], start=18, end=162, fill=md, width=4*s)
            d.arc([153*s,190*s,195*s,213*s], start=20, end=160, fill=(174,93,80,200), width=2*s)
    return Image.alpha_composite(img, aa_overlay(img.size, draw))


def recolor(img: Image.Image, target, strength=.72, alpha_scale=.9) -> Image.Image:
    a = np.array(img, dtype=np.float32)
    rgb = a[..., :3]
    lum = (.2126*rgb[...,0] + .7152*rgb[...,1] + .0722*rgb[...,2])[...,None]
    targ = np.array(target, float)[None,None,:]
    tl = .2126*targ[...,0] + .7152*targ[...,1] + .0722*targ[...,2]
    scaled = targ * (lum / (tl[...,None] + 1e-6))
    a[..., :3] = np.clip(rgb*(1-strength) + scaled*strength, 0, 255)
    a[..., 3] *= alpha_scale
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def with_shadow(img: Image.Image, opacity=.24) -> Image.Image:
    out = Image.new("RGBA", (img.width+8, img.height+8), (0,0,0,0))
    a = img.getchannel("A").filter(ImageFilter.GaussianBlur(2))
    sh = Image.new("RGBA", img.size, (60,45,30,255))
    sh.putalpha(a.point(lambda v: int(v*opacity)))
    out.alpha_composite(sh, (3,4)); out.alpha_composite(img, (2,2))
    return out


def place_rot(ov: Image.Image, img: Image.Image, center, angle=0):
    r = img.rotate(angle, expand=True, resample=Image.Resampling.BICUBIC)
    ov.alpha_composite(r, (round(center[0]-r.width/2), round(center[1]-r.height/2)))


def clipped_composite(base: Image.Image, overlay: Image.Image) -> Image.Image:
    b = np.array(base, dtype=np.float32)
    o = np.array(overlay, dtype=np.float32)
    oa = (o[...,3:4]/255.0) * (b[...,3:4]/255.0)
    out = b.copy()
    out[..., :3] = o[..., :3]*oa + b[..., :3]*(1-oa)
    out[..., 3] = b[..., 3]
    return Image.fromarray(np.clip(out,0,255).astype(np.uint8), "RGBA")


def feature_stats(img: Image.Image, box) -> dict:
    arr = np.array(img)
    x0,y0,x1,y1 = [int(round(v)) for v in box]
    sub = arr[y0:y1,x0:x1]
    m = sub[...,3] > 32
    lum = relative_luminance(sub[..., :3])[m]
    return {"pixels": int(m.sum()), "mean_luminance": float(lum.mean()), "luminance_std": float(lum.std())}


def save_contact(images: dict, path: Path, crop_box=None, cell=(320,420), cols=3, bg=(248,243,230,255)):
    rows = math.ceil(len(images)/cols)
    board = Image.new("RGBA", (cell[0]*cols, cell[1]*rows), (236,233,225,255))
    for i,(name,img) in enumerate(images.items()):
        show = img.crop(crop_box) if crop_box else img.copy()
        show.thumbnail((cell[0]-30, cell[1]-55), Image.Resampling.LANCZOS)
        c = Image.new("RGBA", cell, bg)
        c.alpha_composite(show, ((cell[0]-show.width)//2, 32))
        ImageDraw.Draw(c).text((8,8), name, fill=(30,30,27,255))
        board.alpha_composite(c, ((i%cols)*cell[0], (i//cols)*cell[1]))
    board.convert("RGB").save(path, format="PNG", optimize=False)


def main():
    if sha256(MASTER) != MASTER_SHA: raise SystemExit("ACCEPTED_MASTER_HASH_MISMATCH")
    if sha256(ORA) != ORA_SHA: raise SystemExit("ACCEPTED_ORA_HASH_MISMATCH")
    if TMP.exists(): shutil.rmtree(TMP)
    TMP.mkdir(parents=True)
    with zipfile.ZipFile(ORA) as z:
        z.extract("data/00_sprout.png", TMP)
        z.extract("data/01_face_anchor.png", TMP)
        z.extract("data/03_satchel.png", TMP)
    master = Image.open(MASTER).convert("RGBA")
    neutral = np.array(master, dtype=np.uint8)
    face_layer = Image.open(TMP/"data/01_face_anchor.png").convert("RGBA")
    face_box = face_layer.getchannel("A").getbbox()
    if face_box != (477,420,815,664): raise SystemExit(f"FACE_LAYER_BBOX_MISMATCH:{face_box}")
    face = face_layer.crop(face_box)
    sprout = Image.open(TMP/"data/00_sprout.png").convert("RGBA")
    satchel = Image.open(TMP/"data/03_satchel.png").convert("RGBA")

    work = TMP/"out"; masters_dir=work/"master"; layers_dir=work/"layer"; overlays_dir=work/"overlay"; qa_dir=work/"qa"
    for p in (masters_dir,layers_dir,overlays_dir,qa_dir): p.mkdir(parents=True,exist_ok=True)

    face_variants = {"happy": closed_eye_face(face), "curious": mouth_face(face,"curious"), "surprised": mouth_face(face,"surprised"), "clue_found": closed_eye_face(face), "resolved": mouth_face(face,"resolved")}
    leaf = sprout.crop((472,145,675,290)).crop((0,20,195,138)); leaf=leaf.crop(leaf.getchannel("A").getbbox()).resize((54,34),Image.Resampling.LANCZOS)
    clue = Image.new("RGBA", master.size, (0,0,0,0)); d=ImageDraw.Draw(clue); cx,cy=648,755
    for dx,dy,ang in [(-26,-22,-35),(0,-30,5),(18,-18,45)]: place_rot(clue,leaf,(cx+dx,cy+dy),ang)
    d.line([(648,756),(648,790)],fill=(86,84,49,220),width=5)
    ca=np.array(clue); ca[...,3]=np.minimum(ca[...,3],neutral[...,3]); clue=Image.fromarray(ca,"RGBA")
    clue.save(layers_dir/"expression_clue_found_clover_overlay.png", optimize=False)

    expr_masters={"neutral":master}
    expr_records={"neutral":{"path":"../native-master-v1/master/moru_candidate3_neutral_native_master_v1.png","sha256":MASTER_SHA,"reuse":True}}
    face_mask=np.array(face_layer)[...,3]>0
    face_rect=np.zeros(neutral.shape[:2],bool); face_rect[face_box[1]:face_box[3],face_box[0]:face_box[2]]=True
    clue_zone=np.zeros_like(face_rect); clue_zone[680:830,570:720]=True
    for name,fv in face_variants.items():
        fl=np.array(face_layer).copy(); fl[face_box[1]:face_box[3],face_box[0]:face_box[2]]=np.array(fv)
        layer=Image.fromarray(fl,"RGBA"); layer.save(layers_dir/f"expression_{name}_face_override.png", optimize=False)
        out=neutral.copy(); out[face_mask,:3]=fl[face_mask,:3]; img=Image.fromarray(out,"RGBA")
        if name=="clue_found": img=clipped_composite(img,clue)
        p=masters_dir/f"companion_moru_v2_result_large_{name}_LIGHT_base.png"; img.save(p,optimize=False); expr_masters[name]=img
        arr=np.array(img); changed=np.any(arr[...,:3]!=neutral[...,:3],axis=2)
        allowed=face_rect | (clue_zone if name=="clue_found" else False)
        outside=int((changed & ~allowed).sum())
        if outside: raise SystemExit(f"EXPRESSION_CHANGED_OUTSIDE_ALLOWED:{name}:{outside}")
        if not np.array_equal(arr[...,3],neutral[...,3]): raise SystemExit(f"EXPRESSION_ALPHA_CHANGED:{name}")
        lab0=rgb_to_lab(neutral[...,:3]); lab1=rgb_to_lab(arr[...,:3]); de=np.linalg.norm(lab1-lab0,axis=2); visible=neutral[...,3]>32
        expr_records[name]={"path":f"master/{p.name}","sha256":sha256(p),"changed_rgb_pixels":int(changed.sum()),"changed_outside_allowed":outside,"alpha_bit_identical":True,"silhouette_iou":1.0,"face_anchor_deviation":0.0,"palette_deltaE76_median_visible":float(np.median(de[visible])),"palette_deltaE76_p95_visible":float(np.percentile(de[visible],95))}

    # Affinity overlays from accepted source pixels only.
    raw_leaf=sprout.crop((472,145,675,290)).crop((0,20,195,138)); raw_leaf=raw_leaf.crop(raw_leaf.getchannel("A").getbbox())
    gold=with_shadow(recolor(raw_leaf,(160,125,63),.78,.82).resize((42,26),Image.Resampling.LANCZOS))
    green=with_shadow(recolor(raw_leaf,(95,105,57),.45,.78).resize((38,24),Image.Resampling.LANCZOS),.22)
    flower=satchel.crop((400,960,485,1045)).crop((0,25,70,85)); fa=np.array(flower,dtype=np.float32); rgb=fa[...,:3]; alpha=fa[...,3]
    bright=(rgb[...,0]>170)&(rgb[...,1]>145)&(rgb[...,2]>100)&((rgb.max(-1)-rgb.min(-1))<90); yellow=(rgb[...,0]>150)&(rgb[...,1]>100)&(rgb[...,2]<100); mask=(bright|yellow)&(alpha>0)
    fa[...,3]=(mask.astype(np.uint8)*255); petal=mask & ~(yellow); lum=.2126*rgb[...,0]+.7152*rgb[...,1]+.0722*rgb[...,2]; target=np.array([132,149,142],float); tl=.2126*target[0]+.7152*target[1]+.0722*target[2]; scaled=target[None,None,:]*(lum[...,None]/tl); rgb[petal]=.35*rgb[petal]+.65*scaled[petal]; fa[...,:3]=rgb; fa[...,3]*=.78
    blue=with_shadow(Image.fromarray(np.clip(fa,0,255).astype(np.uint8),"RGBA").resize((42,36),Image.Resampling.LANCZOS),.20)
    affinity_overlays={}; affinity_records={"base":{"reuse":True,"alpha_pixels":0}}
    for stage in ["familiar","trusted","best_friend"]:
        ov=Image.new("RGBA",master.size,(0,0,0,0)); d=ImageDraw.Draw(ov)
        d.line([(483,980),(490,1002)],fill=(92,70,43,170),width=3); place_rot(ov,gold,(494,1020),18)
        if stage in ("trusted","best_friend"):
            d.line([(350,930),(345,948)],fill=(84,70,42,165),width=3); place_rot(ov,green,(340,962),-28); d.ellipse((402,920,416,934),fill=(139,104,58,190),outline=(82,61,38,190),width=2); d.line([(409,932),(415,944)],fill=(88,64,39,160),width=2)
        if stage=="best_friend":
            place_rot(ov,blue,(385,955),-8); d.line([(438,948),(439,968)],fill=(92,66,39,160),width=3); tass=Image.new("RGBA",(28,52),(0,0,0,0)); td=ImageDraw.Draw(tass); td.polygon([(8,3),(20,3),(18,43),(14,49),(10,43)],fill=(169,119,62,175)); td.line([(14,3),(14,45)],fill=(112,78,44,130),width=2); place_rot(ov,tass.filter(ImageFilter.GaussianBlur(.35)),(440,992),4)
        oa=np.array(ov); oa[...,3]=np.minimum(oa[...,3],neutral[...,3]); ov=Image.fromarray(oa,"RGBA")
        p=overlays_dir/f"companion_moru_v2_affinity_{stage}_overlay.png"; ov.save(p,optimize=False); affinity_overlays[stage]=ov
        ab=ov.getchannel("A").getbbox(); pix=int((np.array(ov)[...,3]>0).sum())
        if not (ab and ab[0]>=300 and ab[1]>=900 and ab[2]<=540 and ab[3]<=1060): raise SystemExit(f"AFFINITY_SCOPE_FAIL:{stage}:{ab}")
        comp=Image.alpha_composite(master,ov)
        if not np.array_equal(np.array(comp)[...,3],neutral[...,3]): raise SystemExit(f"AFFINITY_ALPHA_CHANGED:{stage}")
        affinity_records[stage]={"path":f"overlay/{p.name}","sha256":sha256(p),"alpha_bbox_xyxy":list(ab),"alpha_pixels":pix,"anatomy_invariant":True,"silhouette_iou":1.0}
    if not (affinity_records["familiar"]["alpha_pixels"] < affinity_records["trusted"]["alpha_pixels"] < affinity_records["best_friend"]["alpha_pixels"]): raise SystemExit("AFFINITY_NOT_MONOTONIC")

    # Exact neutral usage fan-out reproduction gate.
    usage_exact={}
    for usage,fn in USAGE_FILES.items():
        generated=derive_usage(master,usage); persisted=Image.open(USAGE_ROOT/"candidate"/fn).convert("RGBA")
        exact=np.array_equal(np.array(generated),np.array(persisted)); usage_exact[usage]=exact
        if not exact: raise SystemExit(f"USAGE_FANOUT_NOT_EXACT:{usage}")

    # Exact accepted lighting transfer reproduction gate.
    light_metrics=json.loads((LIGHTING_ROOT/"qa/moru_native_lighting_acceptance_metrics_v1.json").read_text())
    lighting_exact={}
    for usage,fn in USAGE_FILES.items():
        src=Image.open(USAGE_ROOT/"candidate"/fn).convert("RGBA")
        for lighting in ("WARM_DUSK","DARK"):
            generated=apply_transfer(src,light_metrics["transfer"][lighting],lighting)
            persisted=Image.open(LIGHTING_ROOT/"candidate"/LIGHTING_FILES[usage].format(lighting=lighting)).convert("RGBA")
            exact=np.array_equal(np.array(generated),np.array(persisted)); lighting_exact[f"{usage}:{lighting}"]=exact
            if not exact: raise SystemExit(f"LIGHTING_TRANSFER_NOT_EXACT:{usage}:{lighting}")

    # HUD 64dp expression distinguishability + lighting readability.
    hud_mobile={}; hud_master={};
    for e,img in expr_masters.items():
        h=derive_usage(img,"hud_portrait"); hud_master[e]=h; hud_mobile[e]=h.resize((round(64*DENSITY),round(64*DENSITY)),Image.Resampling.LANCZOS)
    pairwise={}; min_fraction=1.0
    for i,a in enumerate(EXPRESSIONS):
        for b in EXPRESSIONS[i+1:]:
            aa=np.array(hud_mobile[a],dtype=np.int16); bb=np.array(hud_mobile[b],dtype=np.int16); m=(aa[...,3]>0)|(bb[...,3]>0); diff=np.abs(aa[...,:3]-bb[...,:3]); changed=np.any(diff>2,axis=2)&m; frac=float(changed.sum()/m.sum()); mae=float(diff[m].mean()); pairwise[f"{a}:{b}"]={"changed_fraction":frac,"mae":mae}; min_fraction=min(min_fraction,frac)
    if min_fraction < 0.003: raise SystemExit(f"EXPRESSION_MOBILE_DISTINCTION_FAIL:{min_fraction}")

    usage_metrics=json.loads((USAGE_ROOT/"qa/moru_native_usage_acceptance_metrics_v1.json").read_text())
    face_box_hud=usage_metrics["assets"]["hud_portrait"]["feature_boxes"]["face"]
    lighting_read={}
    for e,h in hud_master.items():
        lighting_read[e]={}
        light_stat=feature_stats(h,face_box_hud); lighting_read[e]["LIGHT"]=light_stat
        for lighting in ("WARM_DUSK","DARK"):
            li=apply_transfer(h,light_metrics["transfer"][lighting],lighting); st=feature_stats(li,face_box_hud); lighting_read[e][lighting]=st
            if st["mean_luminance"] < (0.10 if lighting=="WARM_DUSK" else 0.045) or st["luminance_std"] < 0.03: raise SystemExit(f"EXPRESSION_LIGHTING_READABILITY_FAIL:{e}:{lighting}")

    # Cross-family composition / alpha / edge gate in Companion context.
    cross={}; sample_images={}
    for e,img in expr_masters.items():
        cross[e]={}
        for a in AFFINITY:
            comp=img if a=="base" else Image.alpha_composite(img,affinity_overlays[a])
            if not np.array_equal(np.array(comp)[...,3],neutral[...,3]): raise SystemExit(f"CROSS_ALPHA_FAIL:{e}:{a}")
            cp=derive_usage(comp,"companion_portrait"); ee=edge_err(cp); cross[e][a]={"alpha_exact":True,"edge_bleed_error":ee,"companion_bbox":list(bbox(cp))}
            if ee > 1e-6: raise SystemExit(f"CROSS_EDGE_FAIL:{e}:{a}:{ee}")
            if e in ("neutral","happy","resolved"):
                sample_images[f"{e}/{a}"]=cp

    # QA boards.
    save_contact({e:hud_mobile[e] for e in EXPRESSIONS},qa_dir/"moru_semantic_expression_hud64_qa_v1.png",cell=(220,220),cols=3)
    affinity_preview={"base":derive_usage(master,"companion_portrait")}
    for a in AFFINITY[1:]: affinity_preview[a]=derive_usage(Image.alpha_composite(master,affinity_overlays[a]),"companion_portrait")
    save_contact(affinity_preview,qa_dir/"moru_semantic_affinity_companion_qa_v1.png",crop_box=(180,520,660,1130),cell=(330,390),cols=2)
    save_contact(sample_images,qa_dir/"moru_semantic_cross_family_companion_qa_v1.png",cell=(250,315),cols=4)
    lighting_board={}
    for e,h in hud_master.items():
        for l in LIGHTINGS:
            li=h if l=="LIGHT" else apply_transfer(h,light_metrics["transfer"][l],l)
            lighting_board[f"{e}/{l}"]=li.resize((round(64*DENSITY),round(64*DENSITY)),Image.Resampling.LANCZOS)
    save_contact(lighting_board,qa_dir/"moru_semantic_expression_lighting_hud64_qa_v1.png",cell=(205,215),cols=3)

    metrics={"version":"moru-native-semantic-family-v1-2026-09-14","verdict":"PASS_MORU_02_SEMANTIC_FAMILY_V1","classification":"NATIVE_SEMANTIC_SOURCE_AND_CROSS_FAMILY_QA_NOT_RUNTIME_ACTIVE","source_master_sha256":MASTER_SHA,"source_ora_sha256":ORA_SHA,"expression":expr_records,"affinity":affinity_records,"usage_fanout_exact_reproduction":usage_exact,"lighting_transfer_exact_reproduction":lighting_exact,"hud64_pairwise_expression_difference":pairwise,"hud64_min_pairwise_changed_fraction":min_fraction,"expression_lighting_readability":lighting_read,"cross_family_companion":cross,"fallback_exact":{"neutral_master_sha256":MASTER_SHA,"neutral_usage_pixel_exact":all(usage_exact.values()),"accepted_lighting_pixel_exact":all(lighting_exact.values())},"runtime_activation":False}
    (qa_dir/"moru_semantic_acceptance_metrics_v1.json").write_text(json.dumps(metrics,indent=2)+"\n")
    manifest={"version":"moru-native-semantic-family-v1-2026-09-14","status":"PASS_MORU_02_SEMANTIC_FAMILY_V1","runtime_activation":False,"expressions":expr_records,"affinity":affinity_records,"lighting_authority":"../native-lighting-family-v1/","usage_authority":"../native-usage-context-v1/","qa":{"metrics":"qa/moru_semantic_acceptance_metrics_v1.json","expression_hud64":"qa/moru_semantic_expression_hud64_qa_v1.png","affinity_companion":"qa/moru_semantic_affinity_companion_qa_v1.png","cross_family_companion":"qa/moru_semantic_cross_family_companion_qa_v1.png","expression_lighting_hud64":"qa/moru_semantic_expression_lighting_hud64_qa_v1.png"},"next":"MORU-03 semantic manifest/export activation readiness; no runtime activation in this work"}
    (work/"manifest.native-semantic.v1.json").write_text(json.dumps(manifest,indent=2)+"\n")
    (work/"README.md").write_text("# Moru Native Semantic Family v1\n\nStatus: **PASS_MORU_02_SEMANTIC_FAMILY_V1 / not runtime-active**\n\nFive non-neutral 1280x1600 expression masters are deterministic layered edits of the accepted native neutral. Three affinity stages are restrained source-derived overlays clipped to the existing silhouette. Accepted usage framing and lighting algorithms are reproduced pixel-exact against the existing neutral families before cross-family QA. The full Cartesian runtime pack is intentionally not materialized here; MORU-03 owns semantic export/resolver/fallback activation readiness.\n")

    if OUT.exists(): shutil.rmtree(OUT)
    shutil.copytree(work,OUT)

    prod=json.loads(PROD.read_text()); prod["status"]["transparent_master_export"]="semantic_source_family_pass_moru03_pending"; prod["status"]["mobile_qa"]="moru02_semantic_cross_family_pass_moru03_pending"; prod["native_semantic_family_v1"]={"status":"PASS_MORU_02_SEMANTIC_FAMILY_V1","path":"design/reference/moru-v2/native-semantic-family-v1/","new_expression_masters":5,"affinity_overlay_stages":3,"usage_fanout_exact_reproduction":True,"lighting_transfer_exact_reproduction":True,"runtime_activation":False,"next":"MORU-03 semantic manifest/export readiness"}; prod["semantic_authoring_handoff_v1"]["status"]="HANDOFF_CONSUMED_MORU_02_SEMANTIC_FAMILY_PASS"; prod["semantic_authoring_handoff_v1"]["runtime_activation"]=False; PROD.write_text(json.dumps(prod,indent=2)+"\n")
    mm=json.loads(MORU_MANIFEST.read_text()); mm["native_semantic_family_v1"]={"status":"PASS_MORU_02_SEMANTIC_FAMILY_V1","path":"native-semantic-family-v1/","runtime_active":False}; mm["runtime_active"]=False; MORU_MANIFEST.write_text(json.dumps(mm,indent=2)+"\n")
    print(json.dumps({"verdict":metrics["verdict"],"expressions":5,"affinity_overlays":3,"min_hud64_pairwise_changed_fraction":min_fraction,"usage_exact":all(usage_exact.values()),"lighting_exact":all(lighting_exact.values())},indent=2))

if __name__ == "__main__": main()
