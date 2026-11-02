"""Extract full publisher records verbatim; never compose or shorten religious text."""
from pathlib import Path
import json

root = Path(__file__).resolve().parents[1]
assets = root / 'feature/src/main/assets'
corpus = json.loads((assets / 'hadeethenc_ar_v1.7.0.json').read_text(encoding='utf-8-sig'))
by_id = {row['id']: row for row in corpus['records']}
rows = []
for key in ('65913', '6076'):
    record = by_id[key]
    rows.append({'title': f'قراءة حديث ذكر النوم — {key}', 'text': record['hadith_text'],
                 'category': 'أذكار النوم', 'targetCount': 1, 'publisherId': key,
                 'source': f"HadeethEnc.com — v1.7.0؛ {record['grade']}؛ {record['takhrij']}؛ {record['link']} — النص الكامل بسياقه؛ هدف قراءة واحدة، وأعداد الذكر في النص الأصلي",
                 'publisher_notice': corpus['publisher_notice']})
(assets / 'verified_sleep_azkar.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Extracted {len(rows)} whole publisher records, with notice and source links')
