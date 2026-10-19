"""Inspect existing production resources without changing any artwork."""
from pathlib import Path
from PIL import Image, ImageOps, ImageDraw
import csv,hashlib,json,re
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/release/external-evidence/2026-10-17'
texts=[(p.relative_to(ROOT).as_posix(),p.read_text(encoding='utf-8-sig')) for module in ['app','feature','designsystem'] for p in (ROOT/module/'src/main').rglob('*') if p.is_file() and p.suffix in ['.kt','.xml']]
rows=[]; previews=[]
for module in ['app','feature','designsystem']:
    for p in sorted((ROOT/module/'src/main/res').rglob('*')):
        if not p.is_file() or p.suffix.lower() not in ['.png','.jpg','.jpeg','.webp']:continue
        uses=[n for n,t in texts if re.search(r'\b'+re.escape(p.stem)+r'\b',t)]
        with Image.open(p) as img:
            row=dict(path=p.relative_to(ROOT).as_posix(),width=img.width,height=img.height,bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest(),references='; '.join(uses),status='EXISTING_PRESERVED_PROVENANCE_PENDING')
            rows.append(row)
            if p.parent.name=='drawable-nodpi' and not p.stem.startswith('ic_'):
                thumb=ImageOps.contain(img.convert('RGB'),(220,138))
                tile=Image.new('RGB',(240,180),'#ede8de');tile.paste(thumb,((240-thumb.width)//2,4))
                ImageDraw.Draw(tile).text((5,145),p.stem[:34],fill='#183e36')
                previews.append(tile)
with (OUT/'visual-assets.csv').open('w',encoding='utf-8-sig',newline='') as f:
    w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
sheet=Image.new('RGB',(240*5,180*((len(previews)+4)//5)),'white')
for i,tile in enumerate(previews):sheet.paste(tile,((i%5)*240,(i//5)*180))
sheet.save(OUT/'existing-assets-contact-sheet.jpg',quality=86)
print(json.dumps(dict(images=len(rows),contact_sheet_tiles=len(previews),zero_size=sum(r['width']==0 or r['height']==0 for r in rows))))
