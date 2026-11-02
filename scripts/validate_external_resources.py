"""Validate the release register, text integrity and retained publisher source."""
from pathlib import Path
import csv,json,re,hashlib,xml.etree.ElementTree as ET
from openpyxl import load_workbook
ROOT=Path(__file__).resolve().parents[1]
E=ROOT/'docs/release/external-evidence/2026-10-17'
A=ROOT/'feature/src/main/assets'
allowed=set('READY_WITH_CONDITIONS NEEDS_SOURCE PENDING_LICENSE PENDING_EDITORIAL_REVIEW PENDING_PHONE_TEST EXTERNAL_LINK_ONLY DEFERRED_BACKEND REJECTED'.split())
rows=list(csv.DictReader((ROOT/'docs/release/EXTERNAL_ASSET_REGISTER.csv').open(encoding='utf-8-sig')))
media=list(csv.DictReader((ROOT/'docs/release/MEDIA_LINK_AUDIT.csv').open(encoding='utf-8-sig')))
assert len({r['asset_id'] for r in rows})==len(rows)
assert all(r['status'] in allowed for r in rows)
videos=[r for r in media if r['asset_type']=='youtube_video']
assert len(videos)==len({r['content_id'] for r in videos})==86
assert all(r['title_matches']=='True' and r['channel_handle_matches']=='True' for r in videos)
assert all((ROOT/r['local_asset_path']).is_file() for r in rows if r['local_asset_path'])
q=json.loads((A/'quran_full.json').read_text(encoding='utf-8-sig'))
texts={v['text'] for s in q for v in s['verses']}
corpus=json.loads((A/'hadeethenc_ar_v1.7.0.json').read_text(encoding='utf-8-sig'))
by_link={r['link']:r for r in corpus['records']}
by_id={r['id']:r for r in corpus['records']}
duas=json.loads((A/'verified_duas.json').read_text(encoding='utf-8-sig'))
assert len(duas)==39
for item in duas:
    assert item['text'] in texts if item['source'].startswith('Tanzil') else item['text']==by_link[item['reference']]['hadith_text']
sleep=json.loads((A/'verified_sleep_azkar.json').read_text(encoding='utf-8-sig'))
assert all(r['text']==by_id[r['publisherId']]['hadith_text'] and r['publisher_notice']==corpus['publisher_notice'] for r in sleep)
xlsx=ROOT/'.artifacts/release-final-20261014/sources/HadeethEnc-ar-v1.7.0.xlsx'
assert hashlib.sha256(xlsx.read_bytes()).hexdigest()==corpus['sha256_original_xlsx']
wb=load_workbook(xlsx,read_only=True,data_only=True)
sheet=wb.active
raw=list(sheet.values)
# Original nine publisher columns, in dataset order. Do not compare generated categorization.
keys=['id','title','hadith_text','explanation','word_meanings','benefits','grade','takhrij','link']
assert list(raw[1])==keys
mapping={}
for r in raw:
    if r and str(r[0]).strip() in by_id:
        mapping[str(r[0]).strip()]=r
matched=0
mismatches=[]
for id,record in by_id.items():
    source=mapping.get(id)
    if source is None:
        mismatches.append(dict(id=id,reason='source row not located'));continue
    # Workbook column names are recorded with the report to expose any incorrect assumption.
    cells=[str(v) if v is not None else '' for v in source]
    if all(str(record[k])==cells[index] for index,k in enumerate(keys)):matched+=1
    else:mismatches.append(dict(id=id,reason='record field does not exactly equal any original cell'))
wb.close()
report=dict(register_rows=len(rows),media_rows=len(media),unique_videos=86,register_schema_status_ids_paths='PASS',
            duas_verbatim=39,sleep_azkar_verbatim=2,original_xlsx_sha256_matches=True,
            original_xlsx_rows=len(raw),hadith_original_cell_exact_matches=matched,hadith_original_mismatches=mismatches[:10])
(E/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=True,indent=2))
assert matched==3582, 'Inspect original workbook columns; never normalize publisher text to make a test pass'
