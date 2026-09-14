from __future__ import annotations
import hashlib, json, subprocess
from pathlib import Path
from PIL import Image

ROOT=Path.cwd()
BASE=ROOT/'design/reference/moru-v2/native-usage-context-v1'
PROD=ROOT/'design/export-spec/moru-production-manifest.v2.json'
MASTER=ROOT/'design/reference/moru-v2/native-master-v1/master/moru_candidate3_neutral_native_master_v1.png'
MASTER_SHA='b9e4d08d3af8cc7fe0ddc354e4086d2c9c18c3b18212917515aefa581cba3692'

def sha(p):
    h=hashlib.sha256()
    with open(p,'rb') as f:
        for b in iter(lambda:f.read(1048576),b''): h.update(b)
    return h.hexdigest()

def blob(p): return subprocess.check_output(['git','hash-object',str(p)],text=True).strip()
def introduced(p): return subprocess.check_output(['git','log','-1','--format=%H','--',str(p.relative_to(ROOT))],text=True).strip()

if sha(MASTER)!=MASTER_SHA: raise SystemExit('MASTER_REMOTE_HASH_MISMATCH')
prod=json.loads(PROD.read_text())
spec=prod['native_usage_context_v1']
if spec['status']!='PASS_MORU_02_NEUTRAL_USAGE_V1': raise SystemExit('MANIFEST_STATUS_NOT_PASS')
metrics=json.loads((BASE/'qa/moru_native_usage_acceptance_metrics_v1.json').read_text())
if metrics['verdict']!='PASS_MORU_02_NEUTRAL_USAGE_V1': raise SystemExit('METRICS_STATUS_NOT_PASS')
out={}
for name,meta in spec['assets'].items():
    p=BASE/'candidate'/f'companion_moru_v2_{name}_neutral_LIGHT_base.png'
    actual=sha(p)
    if actual!=meta['sha256']: raise SystemExit(f'{name}_SHA_MISMATCH {actual} != {meta["sha256"]}')
    im=Image.open(p)
    if list(im.size)!=meta['dimensions']: raise SystemExit(f'{name}_DIM_MISMATCH')
    if im.mode!='RGBA': raise SystemExit(f'{name}_MODE_MISMATCH {im.mode}')
    lo,hi=im.getchannel('A').getextrema()
    if (lo,hi)!=(0,255): raise SystemExit(f'{name}_ALPHA_MISMATCH {(lo,hi)}')
    out[name]={'path':str(p.relative_to(ROOT)),'sha256':actual,'dimensions':list(im.size),'mode':im.mode,'alpha_minmax':[lo,hi],'git_blob_sha1':blob(p),'bytes':p.stat().st_size,'introduced_commit':introduced(p)}
for qname,qmeta in spec['qa'].items():
    p=BASE/'qa'/qname
    if sha(p)!=qmeta['sha256']: raise SystemExit(f'{qname}_SHA_MISMATCH')
record={'version':'moru-native-usage-context-remote-verification-v1-2026-09-14','status':'PASS_REMOTE_BINARY_REVERIFY','branch':'design/production-consolidated-v2','remote_checkout_commit':subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip(),'source_master_sha256':sha(MASTER),'usage_verdict':spec['status'],'assets':out,'qa_artifacts':spec['qa'],'runtime_activation':False}
path=BASE/'qa/remote_binary_verification.v1.json'; path.write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record,indent=2))
