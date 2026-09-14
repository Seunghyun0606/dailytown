from __future__ import annotations

import hashlib, json, shutil
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw
from scipy.ndimage import distance_transform_edt

ROOT=Path.cwd()
MASTER=ROOT/'design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png'
OUT=ROOT/'design/reference/moru-v2/native-usage-context-v1'
TMP=Path('/tmp/moru-native-usage-context-v1-v2')
PROD=ROOT/'design/export-spec/moru-production-manifest.v2.json'
FAIL=ROOT/'design/reference/moru-v2/native-master-v1/qa/FAILURE_MORU_02_NEUTRAL_USAGE_V1.md'
MASTER_SHA='b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692'
DENSITY=2.625

def sha(p):
    h=hashlib.sha256()
    with open(p,'rb') as f:
        for b in iter(lambda:f.read(1048576),b''): h.update(b)
    return h.hexdigest()

def bbox(img, t=8):
    a=np.asarray(img.getchannel('A')); ys,xs=np.nonzero(a>t)
    if not len(xs): raise RuntimeError('empty alpha')
    return (int(xs.min()),int(ys.min()),int(xs.max())+1,int(ys.max())+1)

def bleed(img,r=16):
    arr=np.array(img,dtype=np.uint8); a=arr[...,3]; vis=a>0; tr=~vis
    if not vis.any() or not tr.any(): return img
    dist,idx=distance_transform_edt(tr,return_indices=True); ring=tr&(dist<=r); yy,xx=idx
    nearest=arr[yy,xx,:3]; arr[...,:3][ring]=nearest[ring]
    return Image.fromarray(arr,'RGBA')

def edge_err(img,r=8):
    arr=np.asarray(img,dtype=np.int16); a=arr[...,3]; vis=a>0; tr=~vis
    dist,idx=distance_transform_edt(tr,return_indices=True); ring=tr&(dist<=r)
    if not ring.any(): return 0.0
    yy,xx=idx; near=arr[yy,xx,:3]; d=np.abs(arr[...,:3]-near).mean(2)
    return float(d[ring].mean())

def crop_box(sub,fr,pad=.02):
    x0,y0,x1,y1=sub; sw=x1-x0; sh=y1-y0; px=round(sw*pad); py=round(sh*pad)
    a,b,c,d=fr
    return (max(0,round(x0+sw*a)-px),max(0,round(y0+sh*b)-py),min(1280,round(x0+sw*c)+px),min(1600,round(y0+sh*d)+py))

def derive(src,sub,fr,target,fill,anchor):
    cb=crop_box(sub,fr); cr=src.crop(cb); cw,ch=cr.size; tw,th=target
    s=min(tw*fill[0]/cw,th*fill[1]/ch); rw,rh=round(cw*s),round(ch*s)
    rs=cr.resize((rw,rh),Image.Resampling.LANCZOS)
    can=Image.new('RGBA',target,(0,0,0,0))
    ox=(tw-rw)//2; oy=(max(8,round(th*.02)) if anchor=='top' else th-rh-max(8,round(th*.02)))
    can.alpha_composite(rs,(ox,oy)); can=bleed(can,max(12,round(s*6)))
    return can,{'source_crop_xyxy':list(cb),'source_crop_dimensions':[cw,ch],'scale':s,'resized_dimensions':[rw,rh],'paste_offset':[ox,oy],'anchor':anchor}

def map_feature(sub,norm,tr,target):
    sx0,sy0,sx1,sy1=sub; sw=sx1-sx0; sh=sy1-sy0; cx,cy,_,_=tr['source_crop_xyxy']; s=tr['scale']; ox,oy=tr['paste_offset']
    x0=sx0+norm[0]*sw; y0=sy0+norm[1]*sh; x1=sx0+norm[2]*sw; y1=sy0+norm[3]*sh
    return [max(0,ox+(x0-cx)*s),max(0,oy+(y0-cy)*s),min(target[0],ox+(x1-cx)*s),min(target[1],oy+(y1-cy)*s)]

def h(box): return max(0.0,box[3]-box[1])

def cell(asset,dp,bg):
    p=round(dp*DENSITY); im=asset.resize((p,p),Image.Resampling.LANCZOS); c=Image.new('RGBA',(230,230),(246,237,220,255)); d=ImageDraw.Draw(c)
    if bg=='dark': d.rectangle((0,0,229,229),fill=(43,42,37,255))
    if bg=='map':
        d.rectangle((0,0,229,229),fill=(205,211,185,255))
        for x in range(-90,280,52): d.line((x,0,x+120,230),fill=(235,226,191,255),width=9)
        for y in range(28,230,58): d.line((0,y,230,y-18),fill=(144,164,129,255),width=4)
    c.alpha_composite(im,((230-p)//2,(230-p)//2)); d.rectangle((0,204,229,229),fill=(255,255,255,220)); d.text((6,209),f'{dp}dp {bg}',fill=(20,20,20,255)); return c

def boards(assets,qdir):
    board=Image.new('RGBA',(2070,770),(248,244,234,255)); d=ImageDraw.Draw(board)
    for r,(name,sizes) in enumerate([('map_avatar',[48,56,64]),('hud_portrait',[56,64,72]),('journal_crop',[56,64,72])]):
        d.text((5,r*255+2),name,fill=(20,20,20,255)); k=0
        for dp in sizes:
            for bg in ['cream','dark','map']:
                board.alpha_composite(cell(assets[name],dp,bg),(k*230,r*255+24)); k+=1
    board.convert('RGB').save(qdir/'moru_native_usage_mobile_context_qa_v1.png',quality=95)
    fb=Image.new('RGBA',(1500,1020),(246,237,220,255)); d=ImageDraw.Draw(fb); pos=[(30,50),(520,50),(1010,50),(30,545),(520,545),(1010,545)]
    for (name,a),(x,y) in zip(assets.items(),pos):
        box=(420,420) if a.width==a.height else (380,450); t=a.copy(); t.thumbnail(box,Image.Resampling.LANCZOS)
        d.text((x,y-20),name,fill=(25,25,25,255)); fb.alpha_composite(t,(x+(box[0]-t.width)//2,y+(box[1]-t.height)//2))
    fb.convert('RGB').save(qdir/'moru_native_usage_framing_qa_v1.png',quality=95)

def main():
    if sha(MASTER)!=MASTER_SHA: raise SystemExit('ACCEPTED_MASTER_HASH_MISMATCH')
    src=Image.open(MASTER).convert('RGBA'); assert src.size==(1280,1600); sub=bbox(src)
    feat={'sprout':(.266,0,.861,.170),'face':(.274,.238,.745,.406),'scarf':(.328,.406,.712,.518),'satchel':(0,.500,.405,.731),'boots_base':(.15,.830,.86,1)}
    specs={
      'map_avatar':((512,512),(0,0,1,1),(.90,.94),'bottom'),
      'hud_portrait':((768,768),(0,0,1,.60),(.92,.92),'top'),
      'encounter_halfbody':((1024,1280),(0,0,1,.78),(.90,.94),'top'),
      'companion_portrait':((1024,1280),(.02,0,.98,.70),(.92,.94),'top'),
      'journal_crop':((768,768),(.08,0,.92,.56),(.94,.92),'top')}
    if TMP.exists(): shutil.rmtree(TMP)
    cdir=TMP/'candidate'; qdir=TMP/'qa'; cdir.mkdir(parents=True); qdir.mkdir()
    assets={}; rec={}; crops=set()
    for name,(target,fr,fill,anchor) in specs.items():
        a,tr=derive(src,sub,fr,target,fill,anchor); assets[name]=a; crops.add(tuple(tr['source_crop_xyxy']))
        p=cdir/f'companion_moru_v2_{name}_neutral_LIGHT_base.png'; a.save(p,optimize=True); bb=bbox(a); al=np.asarray(a.getchannel('A'))
        rec[name]={'path':f'candidate/{p.name}','dimensions':list(target),'mode':'RGBA','sha256':sha(p),'alpha_minmax':[int(al.min()),int(al.max())],'alpha_bbox_xyxy':list(bb),'content_coverage':{'width':(bb[2]-bb[0])/target[0],'height':(bb[3]-bb[1])/target[1]},'edge_bleed_mean_abs_rgb_error':edge_err(a),'transform':tr,'feature_boxes':{k:[round(x,3) for x in map_feature(sub,v,tr,target)] for k,v in feat.items()}}
    # result_large is byte-for-byte the accepted neutral master; no re-encoding.
    rp=cdir/'companion_moru_v2_result_large_neutral_LIGHT_base.png'; shutil.copyfile(MASTER,rp); ra=Image.open(rp).convert('RGBA'); assets['result_large']=ra
    tr={'source_crop_xyxy':[0,0,1280,1600],'source_crop_dimensions':[1280,1600],'scale':1.0,'resized_dimensions':[1280,1600],'paste_offset':[0,0],'anchor':'identity_copy'}; bb=bbox(ra); al=np.asarray(ra.getchannel('A')); crops.add((0,0,1280,1600))
    rec['result_large']={'path':f'candidate/{rp.name}','dimensions':[1280,1600],'mode':'RGBA','sha256':sha(rp),'alpha_minmax':[int(al.min()),int(al.max())],'alpha_bbox_xyxy':list(bb),'content_coverage':{'width':(bb[2]-bb[0])/1280,'height':(bb[3]-bb[1])/1600},'edge_bleed_mean_abs_rgb_error':edge_err(bleed(ra.copy(),16)),'transform':tr,'feature_boxes':{k:[round(x,3) for x in map_feature(sub,v,tr,(1280,1600))] for k,v in feat.items()}}
    mob={}
    for name,sizes in {'map_avatar':[48,56,64],'hud_portrait':[56,64,72],'journal_crop':[56,64,72]}.items():
        r=rec[name]; w=r['dimensions'][0]; mob[name]=[]
        for dp in sizes:
            f=dp/w; mob[name].append({'dp':dp,'physical_px_at_2_625x':round(dp*DENSITY),'visible_content_height_dp':round(r['content_coverage']['height']*dp,3),'face_height_dp':round(h(r['feature_boxes']['face'])*f,3),'sprout_height_dp':round(h(r['feature_boxes']['sprout'])*f,3),'scarf_height_dp':round(h(r['feature_boxes']['scarf'])*f,3)})
    checks={'accepted_master_hash':sha(MASTER)==MASTER_SHA,'six_contexts_generated':len(rec)==6,'all_rgba_genuine_alpha':all(v['alpha_minmax']==[0,255] for v in rec.values()),'all_dimensions_exact':all(rec[n]['dimensions']==list(specs[n][0]) for n in specs) and rec['result_large']['dimensions']==[1280,1600],'context_framing_not_identical':len(crops)>=5,'transparent_side_edge_bleed':all(v['edge_bleed_mean_abs_rgb_error']<=1.0 for n,v in rec.items() if n!='result_large'),'map_mobile_content':mob['map_avatar'][0]['visible_content_height_dp']>=42,'map_sprout':mob['map_avatar'][0]['sprout_height_dp']>=6,'map_face_negative_space_proxy':mob['map_avatar'][0]['face_height_dp']>=6,'hud_mobile_content':mob['hud_portrait'][0]['visible_content_height_dp']>=48,'hud_face':mob['hud_portrait'][0]['face_height_dp']>=12,'hud_sprout':mob['hud_portrait'][0]['sprout_height_dp']>=10,'hud_scarf':mob['hud_portrait'][0]['scarf_height_dp']>=7,'journal_mobile_content':mob['journal_crop'][0]['visible_content_height_dp']>=46,'journal_face':mob['journal_crop'][0]['face_height_dp']>=12,'journal_scarf':mob['journal_crop'][0]['scarf_height_dp']>=7,'encounter_includes_satchel':h(rec['encounter_halfbody']['feature_boxes']['satchel'])>0,'result_full_sprout':h(rec['result_large']['feature_boxes']['sprout'])>0,'result_full_boots':h(rec['result_large']['feature_boxes']['boots_base'])>0,'companion_face':h(rec['companion_portrait']['feature_boxes']['face'])>0,'result_is_exact_accepted_master':sha(rp)==MASTER_SHA}
    boards(assets,qdir)
    verdict='PASS_MORU_02_NEUTRAL_USAGE_V1' if all(checks.values()) else 'FAIL_MORU_02_NEUTRAL_USAGE_V1'
    res={'version':'moru-native-usage-context-v1-2026-09-14','verdict':verdict,'classification':'NATIVE_USAGE_QA_CANDIDATE_NOT_RUNTIME_ACTIVE','source':{'path':str(MASTER.relative_to(ROOT)),'sha256':MASTER_SHA,'dimensions':[1280,1600],'mode':'RGBA','subject_alpha_bbox_xyxy':list(sub)},'semantic_state':{'expression':'neutral','lighting':'LIGHT','affinity':'base','motion':'static'},'density_reference':DENSITY,'assets':rec,'mobile_checks':mob,'checks':checks,'qa_artifacts':{},'generation_used':False,'new_semantic_microdetail':False,'expression_fan_out':False,'lighting_fan_out':False,'affinity_fan_out':False,'runtime_activation':False}
    for n in ['moru_native_usage_mobile_context_qa_v1.png','moru_native_usage_framing_qa_v1.png']:
        p=qdir/n; res['qa_artifacts'][n]={'sha256':sha(p),'bytes':p.stat().st_size}
    (qdir/'moru_native_usage_acceptance_metrics_v1.json').write_text(json.dumps(res,indent=2)+'\n')
    (TMP/'README.md').write_text(f'# Moru native usage-context v1\n\nStatus: **{verdict}**\n\nOnly `neutral / LIGHT / base / static` derivatives from the accepted native neutral master are included. No semantic variant fan-out or runtime activation is performed.\n')
    print(json.dumps(res,indent=2)); Path('/tmp/moru02-result-v2.json').write_text(json.dumps(res,indent=2)+'\n')
    if not all(checks.values()): raise SystemExit(2)
    if OUT.exists(): shutil.rmtree(OUT)
    shutil.copytree(TMP,OUT)
    if FAIL.exists(): FAIL.unlink()
    prod=json.loads(PROD.read_text()); prod['status']['transparent_master_export']='neutral_usage_context_family_pass_semantic_family_pending'; prod['status']['mobile_qa']='neutral_usage_context_mobile_qa_pass_expression_lighting_affinity_pending'; prod['native_master_handoff']['status']='HANDOFF_CONSUMED_NATIVE_NEUTRAL_V1_PASS'; prod['native_neutral_master_v1']['fan_out']='neutral_usage_context_v1_pass'; prod['human_gates']['identity_lock']='native_neutral_v1_pass'; prod['native_usage_context_v1']={'status':verdict,'path':'design/reference/moru-v2/native-usage-context-v1/','semantic_state':res['semantic_state'],'assets':{n:{'dimensions':v['dimensions'],'sha256':v['sha256']} for n,v in rec.items()},'qa':res['qa_artifacts'],'runtime_activation':False,'next':'produce/validate expressions, LIGHT/WARM_DUSK/DARK and affinity stages before MORU-03'}; PROD.write_text(json.dumps(prod,indent=2)+'\n')

if __name__=='__main__': main()
