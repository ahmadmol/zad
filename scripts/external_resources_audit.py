"""Read-only source/network audit. Writes release evidence; never runs a device or Git mutation.

Python standard library only. oEmbed proves metadata, never playback/ownership/offline rights.
Run: python scripts/external_resources_audit.py [--refresh]
"""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from urllib.parse import quote, urlencode, urlsplit
import csv, hashlib, json, re, sys, datetime, xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/release/external-evidence/2026-10-17'
OUT.mkdir(parents=True, exist_ok=True)
RELEASE = ROOT / 'docs/release'
ASSETS = ROOT / 'feature/src/main/assets'
NOW = datetime.datetime.now(datetime.timezone.utc).isoformat()
REFRESH = '--refresh' in sys.argv
COLS = 'asset_id,feature,screen,asset_type,title,source_org,source_url,author_or_reciter,source_version,license_url,rights_scope,attribution_required,attribution_text,local_asset_path,content_id,verified_at,verification_method,phone_playback_result,offline_allowed,status,blocker,notes'.split(',')
rows, media = [], []

def read(p): return p.read_text(encoding='utf-8-sig')
def save(p, obj): p.write_text(json.dumps(obj, ensure_ascii=False, indent=2), encoding='utf-8')
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def fetch(url, name, limit=4_000_000, headers=None):
    meta_path = OUT / (name + '.json')
    body_path = OUT / (name + '.body')
    if not REFRESH and meta_path.exists():
        cached = json.loads(read(meta_path))
        if cached['url'] == url:
            return cached, body_path.read_bytes() if body_path.exists() else b''
    meta = dict(url=url, checked_at=NOW, method='GET', http=None)
    body = b''
    try:
        safe_url = quote(url, safe=':/?=&%+#@')
        with urlopen(Request(safe_url, headers={'User-Agent': 'IhsanResourceAudit/1.0', **(headers or {})}), timeout=25) as r:
            body = r.read(limit)
            meta.update(http=r.status, final_url=r.url, content_type=r.headers.get('Content-Type'), truncated=len(body) == limit)
    except HTTPError as e: meta.update(http=e.code, error=str(e))
    except Exception as e: meta['error'] = str(e)
    if body:
        body_path.write_bytes(body)
        meta['sha256'] = hashlib.sha256(body).hexdigest()
    save(meta_path, meta)
    return meta, body

def add(id, feature, kind, title, status, **kw):
    row = dict.fromkeys(COLS, '')
    row.update(asset_id=id, feature=feature, screen=feature, asset_type=kind, title=title,
               status=status, verified_at=NOW, verification_method='Local source inspection',
               phone_playback_result='NOT_APPLICABLE', offline_allowed='UNKNOWN')
    row.update(kw)
    rows.append(row)
    return row

# Extract the CURRENT Kotlin records rather than trusting an earlier manifest.
videos = []
for group, name in [('sanhya', 'Sanhya'), ('nabiihsan', 'NabiIhsan'), ('fahmanallah', 'Fahm')]:
    path = ROOT / f'feature/src/main/java/com/example/feature/{group}/data/datasource/{name}FixtureData.kt'
    text = read(path)
    program = re.search(r'PROGRAM_URL = "([^"]+)"', text)[1]
    n = 0
    for line in text.splitlines():
        if not line.strip().startswith('EpisodeRecord('): continue
        n += 1
        strings = [json.loads('"' + s + '"') for s in re.findall(r'"((?:\\.|[^"\\])*)"', line)]
        vid = next((s for s in strings[1:2] if re.fullmatch(r'[\w-]{11}', s)), None)
        # Nullable Sanhya records have no video ID and must remain a missing slot.
        number_match = re.match(r'\s*EpisodeRecord\((\d+),', line)
        number = int(number_match[1]) if number_match else n
        if not vid:
            add(f'video-missing-{group}-{number}', group, 'video_slot', strings[0], 'NEEDS_SOURCE',
                source_url=program, blocker='Publisher video ID absent; do not invent replacement')
            continue
        videos.append(dict(collection=group, number=number, video_id=vid, declared_title=strings[0],
                           declared_duration=strings[2], declared_date=strings[3], local_asset_path=path.relative_to(ROOT).as_posix(),
                           expected_channel_id=re.search(r'OFFICIAL_CHANNEL_ID = "([^"]+)"', text)[1],
                           program_url=program,
                           episode_url=f'https://amrkhaled.net/episode/{strings[4]}' if group != 'fahmanallah' else f'https://amrkhaled.net/episode/{1028886-number}'))
assert len(videos) == len({v['video_id'] for v in videos}) == 86

def video_probe(v):
    meta, body = fetch('https://www.youtube.com/oembed?' + urlencode(dict(url='https://www.youtube.com/watch?v='+v['video_id'], format='json')), 'youtube-'+v['video_id'])
    result = {**v, **meta}
    try:
        data = json.loads(body)
        result.update(actual_title=data.get('title', ''), actual_channel=data.get('author_name', ''),
                      actual_channel_url=data.get('author_url', ''), thumbnail_url=data.get('thumbnail_url', ''),
                      title_matches=data.get('title') == v['declared_title'],
                      channel_handle_matches=data.get('author_url', '').lower().rstrip('/') == 'https://www.youtube.com/@amrkhaled')
    except Exception: result['metadata_error'] = 'No valid oEmbed JSON'
    return result

results = list(ThreadPoolExecutor(max_workers=4).map(video_probe, videos))
save(OUT / 'video-metadata.json', results)
def episode_probe(v):
    meta, body = fetch(v['episode_url'], 'episode-'+v['video_id'])
    text = body.decode('utf-8', errors='replace')
    return dict(video_id=v['video_id'], **meta, expected_video_id_present=v['video_id'] in text,
                youtube_ids=sorted(set(re.findall(r'(?:embed/|watch\?v=|youtu\.be/)([A-Za-z0-9_-]{11})', text))))
episode_results = list(ThreadPoolExecutor(max_workers=4).map(episode_probe, videos))
save(OUT / 'publisher-episode-checks.json', episode_results)
for result in episode_results:
    add('publisher-episode-'+result['video_id'], 'learning', 'publisher_episode_link', result['video_id'],
        'EXTERNAL_LINK_ONLY' if result.get('http') == 200 and result['expected_video_id_present'] else 'REJECTED' if result.get('http') == 404 else 'PENDING_EDITORIAL_REVIEW',
        source_org='Amr Khaled', source_url=result['url'], content_id=result['video_id'],
        rights_scope='External publisher page only',
        notes=f"Legacy ID-only URL: HTTP {result.get('http')}; expected ID present={result['expected_video_id_present']}. Production sourceUrl now uses the independently verified YouTube watch URL; publisher program retained separately.")
for v in results:
    good = v.get('http') == 200 and v.get('title_matches') and v.get('channel_handle_matches')
    r = add('youtube-'+v['video_id'], v['collection'], 'youtube_video', v.get('actual_title') or v['declared_title'],
            'PENDING_PHONE_TEST' if good else 'PENDING_EDITORIAL_REVIEW', source_org='YouTube / publisher channel',
            source_url='https://www.youtube.com/watch?v='+v['video_id'], author_or_reciter=v.get('actual_channel', ''),
            license_url='https://developers.google.com/youtube/terms/required-minimum-functionality',
            rights_scope='Official player/external link subject to platform and uploader restrictions; no rehosting/download grant',
            attribution_required='YES', attribution_text=v.get('actual_channel', '')+' — YouTube',
            local_asset_path=v['local_asset_path'], content_id=v['video_id'], offline_allowed='NO_GRANT',
            blocker='Phone playback / current embedding not established' if good else 'Metadata unavailable or mismatch; inspect evidence',
            notes='Title/channel from oEmbed, not proof of legal ownership; duration/date inherited, not reverified. '+v['episode_url'])
    r['phone_playback_result'] = 'NOT_RUN_THIS_AUDIT'
    r['verification_method'] = 'YouTube oEmbed GET; exact title and author URL comparison; external-evidence/2026-10-17/youtube-'+v['video_id']+'.json'
    media.append({**r, **v, 'embeddability': 'UNKNOWN_NOT_PROVEN_BY_OEMBED', 'duration_verified': False, 'failure_reason': v.get('error', '')})
    add('thumbnail-'+v['video_id'], v['collection'], 'remote_thumbnail', v['declared_title'], 'READY_WITH_CONDITIONS' if good else 'PENDING_EDITORIAL_REVIEW',
        source_url=v.get('thumbnail_url', ''), author_or_reciter=v.get('actual_channel', ''), content_id=v['video_id'],
        license_url='https://developers.google.com/youtube/terms/developer-policies', offline_allowed='NO_GRANT',
        rights_scope='Associated YouTube video preview only; no standalone artwork or APK redistribution grant',
        attribution_required='YES', attribution_text=v.get('actual_channel', '')+' — YouTube',
        notes='URL returned by oEmbed; image bytes not downloaded or separately licensed')

providers = {
 'tanzil-license':'https://tanzil.net/docs/Text_License',
 'hadeethenc-policy':'https://hadeethenc.com/ar/',
 'hadeethenc-update':'https://hadeethenc.com/en/check/ar/v1.7.0',
 'mp3quran-api':'https://www.mp3quran.net/ar/api',
 'mp3quran-contact':'https://www.mp3quran.net/ar/contact-us',
 'everyayah':'https://everyayah.com/',
 'haramain-official':'https://haramain.prh.gov.sa/ar/live',
 'sba-official':'https://saudiatv.sba.sa/',
 'amrkhaled-official':'https://amrkhaled.net/',
 'qf-terms':'https://api-docs.quran.com/legal/developer-terms/',
 'qf-audio':'https://api-docs.quran.com/docs/sdk/javascript/audio/',
 'youtube-policy':'https://developers.google.com/youtube/terms/required-minimum-functionality',
 'youtube-video-api':'https://developers.google.com/youtube/v3/docs/videos/list',
 'unsplash-license':'https://unsplash.com/license',
 'pexels-license':'https://www.pexels.com/legal-pages/license/',
 'commons-reuse':'https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia/en',
 'lucide-license':'https://lucide.dev/license',
 'amiri-license':'https://raw.githubusercontent.com/aliftype/amiri/main/OFL.txt',
 'adhan-license':'https://raw.githubusercontent.com/batoulapps/adhan-kotlin/master/LICENSE',
 'quranenc-moyassar':'https://quranenc.com/ar/browse/arabic_moyassar',
 'quranenc-home':'https://quranenc.com/ar/home',
 'quranenc-contact':'https://quranenc.com/ar/home/contact_us',
 'quranenc-list':'https://quranenc.com/api/v1/translations/list/ar',
 'quranenc-sample':'https://quranenc.com/api/v1/translation/sura/arabic_moyassar/1',
}
evidence = list(ThreadPoolExecutor(max_workers=4).map(lambda item: (item[0], fetch(item[1], item[0])[0]), providers.items()))
save(OUT / 'provider-checks.json', dict(evidence))
for key, meta in evidence:
    add('provider-'+key, 'sources', 'reference_link', key, 'DEFERRED_BACKEND' if key.startswith('qf-') else 'EXTERNAL_LINK_ONLY',
        source_url=meta['url'], rights_scope='Reference page only; not an individual content license',
        notes=f"HTTP {meta.get('http')}; {meta.get('error', '')}")

quran = json.loads(read(ASSETS/'quran_full.json'))
xml = ET.parse(ASSETS/'licenses/tanzil-uthmani-1.1.xml').getroot()
original = {(int(s.attrib['index']), int(a.attrib['index'])):a.attrib['text'] for s in xml.findall('sura') for a in s.findall('aya')}
flat = {(s['id'],v['id']):v['text'] for s in quran for v in s['verses']}
corpus = json.loads(read(ASSETS/'hadeethenc_ar_v1.7.0.json'))
checks = dict(surahs=len(quran), verses=len(flat), verbatim_tanzil=flat == original,
              hadith_records=len(corpus['records']), hadith_unique_ids=len({r['id'] for r in corpus['records']}),
              duas=len(json.loads(read(ASSETS/'verified_duas.json'))), asma=len(json.loads(read(ASSETS/'asma.json'))),
              sleep_azkar=len(json.loads(read(ASSETS/'verified_sleep_azkar.json'))))
save(OUT/'content-integrity.json', checks)
for p in sorted(ASSETS.rglob('*')):
    if not p.is_file(): continue
    name = p.name
    status = 'READY_WITH_CONDITIONS'
    source, org, license_url, scope = 'https://tanzil.net', 'Tanzil Project', providers['tanzil-license'], 'Verbatim offline text; notices/credit retained'
    if name.startswith('hadeethenc') or name.startswith('verified_sleep') or name.startswith('HadeethEnc'):
        source, org, license_url, scope = 'https://hadeethenc.com/ar/', 'HadeethEnc', 'https://hadeethenc.com/ar/', 'Unchanged publisher dataset with attribution/version/updates; no blanket audio rights'
    if name == 'asma.json': status, source, org, scope = 'PENDING_EDITORIAL_REVIEW', '', '', 'Source and religious review unestablished'
    if name == 'asma.json': license_url = ''
    if name == 'duas.json': status, source, org, license_url, scope = 'PENDING_EDITORIAL_REVIEW', '', '', '', 'Legacy asset retained; not the verified presentation dataset'
    if name == 'content_sources.json': source, org, license_url, scope = '', 'Ihsan provenance manifest', '', 'Local source manifest; each referenced content item keeps its own source-specific rights'
    if name in ('mushaf_page_starts.json','quran_juz_starts.json'):
        source, license_url, scope = 'https://tanzil.net/docs/Quran_Metadata', 'https://tanzil.net/docs/Quran_Metadata', 'Factual page/juz boundary offsets with publisher credit; no printed Mushaf image or recording grant inferred'
    if name == 'quran.json': org, source, license_url, scope = 'Risan Bagja Pradana / Tanzil factual metadata', 'https://github.com/risan/quran-json', 'https://creativecommons.org/licenses/by-sa/4.0/', 'Retained structural metadata; original CC BY-SA notice retained; ayahs array intentionally empty'
    if name == 'verified_duas.json': org, source, scope = 'Tanzil / HadeethEnc', 'https://tanzil.net ; https://hadeethenc.com/ar/', '30 whole ayahs + 9 full-context hadiths; retain respective notices'
    if 'Amiri' in name: org, source, license_url, scope = 'Amiri', 'https://github.com/aliftype/amiri', providers['amiri-license'], 'OFL 1.1; retain notice'
    if 'quran-json' in name: org, source, license_url, scope = 'Risan Bagja Pradana', 'https://github.com/risan/quran-json', 'https://creativecommons.org/licenses/by-sa/4.0/', 'Retained structural metadata under CC BY-SA 4.0; notice bundled'
    source_edition = 'Tanzil Uthmani 1.1' if org == 'Tanzil Project' else 'ar v1.7.0' if org == 'HadeethEnc' else 'Tanzil 1.1 / HadeethEnc ar v1.7.0' if org == 'Tanzil / HadeethEnc' else 'See retained notice or source manifest'
    if name in ('mushaf_page_starts.json','quran_juz_starts.json'): source_edition = 'Tanzil Quran metadata 1.0'
    add('file-'+p.relative_to(ASSETS).as_posix(), 'religious-content', 'dataset_or_notice', name, status, source_org=org,
        source_url=source, license_url=license_url, source_version=source_edition,
        local_asset_path=p.relative_to(ROOT).as_posix(), rights_scope=scope, attribution_required='YES',
        offline_allowed='YES_WITH_CONDITIONS' if status == 'READY_WITH_CONDITIONS' else 'UNESTABLISHED', notes='sha256='+sha(p))

# Per-entry traceability for modern editorial material, duas and all 3582 hadith links.
for record in corpus['records']:
    add('hadith-'+record['id'], 'hadith', 'religious_text', record['title'], 'READY_WITH_CONDITIONS', source_org='HadeethEnc',
        source_url='https://hadeethenc.com/ar/browse/hadith/'+record['id'], source_version='ar v1.7.0',
        license_url='https://hadeethenc.com/ar/', local_asset_path='feature/src/main/assets/hadeethenc_ar_v1.7.0.json',
        content_id=record['id'], rights_scope='Unchanged dataset record; retain publisher notice and updates',
        attribution_required='YES', attribution_text='HadeethEnc.com — العربية v1.7.0', offline_allowed='YES_WITH_CONDITIONS',
        notes='Local record inspected; individual web page NOT network-tested; original XLSX comparison is prior evidence')
for filename, feature in [('asma.json','asma'), ('verified_duas.json','duas'), ('verified_sleep_azkar.json','azkar')]:
    for i, item in enumerate(json.loads(read(ASSETS/filename)), 1):
        add(f'{feature}-{i}', feature, 'religious_text', item.get('name', item.get('title', str(i))),
            'PENDING_EDITORIAL_REVIEW' if feature == 'asma' else 'READY_WITH_CONDITIONS',
            local_asset_path='feature/src/main/assets/'+filename, content_id=str(item.get('id', i)),
            source_url='' if feature == 'asma' else ('https://tanzil.net' if item.get('source', '').startswith('Tanzil') else next(iter(re.findall(r'https://hadeethenc.com/[^\s]+', item.get('reference', '')+' '+item.get('source', ''))), 'https://hadeethenc.com/ar/')),
            source_version='' if feature == 'asma' else 'Tanzil 1.1 / HadeethEnc ar v1.7.0 (see record)',
            rights_scope='Source/review required' if feature == 'asma' else 'Full source text with publisher notice; no audio grant',
            notes=item.get('reference', item.get('source', 'Editorial meanings: provenance not established')),
            attribution_required='YES', attribution_text='Source and edition in bundled record' if feature != 'asma' else '',
            license_url=providers['tanzil-license'] if item.get('source', '').startswith('Tanzil') else providers['hadeethenc-policy'] if feature != 'asma' else '',
            offline_allowed='UNESTABLISHED' if feature == 'asma' else 'YES_WITH_CONDITIONS')
seed = ROOT/'feature/src/main/java/com/example/feature/azkar/data/local/database/AzkarSeedManager.kt'
for i, line in enumerate(read(seed).split('internal val SEED_ROWS:')[1].splitlines(), 1):
    if 'ZikrEntity(' not in line: continue
    title = re.search(r'title = "([^"]+)"', line)[1]
    source = re.search(r'source = "([^"]+)"', line)[1]
    add(f'azkar-seed-line-{i}', 'azkar', 'religious_text', title, 'PENDING_EDITORIAL_REVIEW',
        local_asset_path=seed.relative_to(ROOT).as_posix(), notes=source,
        blocker='Review text edition, counts and virtues separately; optional counter goal is not Sunnah certification')

# All production images, vectors, launch icons, font, adhan. Unknown ownership is not inferred.
for module in ('app','feature','designsystem'):
    for p in sorted((ROOT/module/'src/main/res').rglob('*')):
        if not p.is_file() or not (p.suffix.lower() in ('.png','.jpg','.jpeg','.webp','.ttf','.mp3') or (p.suffix == '.xml' and p.parent.name.startswith(('drawable','mipmap')))): continue
        rel = p.relative_to(ROOT).as_posix()
        font = p.suffix == '.ttf'
        kind = 'font' if font else 'adhan_recording' if p.suffix == '.mp3' else 'vector' if p.suffix == '.xml' else 'image'
        r = add('res-'+rel, module, kind, p.name, 'READY_WITH_CONDITIONS' if font else 'PENDING_LICENSE',
                local_asset_path=rel, source_url='https://github.com/aliftype/amiri' if font else '',
                license_url=providers['amiri-license'] if font else '',
                rights_scope='OFL 1.1; notice bundled' if font else 'Existing asset preserved; individual creator/grant not supplied',
                offline_allowed='YES_WITH_CONDITIONS' if font else 'UNESTABLISHED',
                blocker='' if font else 'Obtain source/creator and public app distribution evidence', notes='sha256='+sha(p))
        if kind == 'adhan_recording':
            r.update(phone_playback_result='NOT_RUN_THIS_AUDIT', notes=r['notes']+'; prior report records owner app-embedding grant, not independent public rights')
            media.append(r.copy())
readers = [('Alafasy_128kbps','مشاري العفاسي'),('Abdul_Basit_Murattal_192kbps','عبد الباسط عبد الصمد'),('MaherAlMuaiqly128kbps','ماهر المعيقلي')]
for reader, label in readers:
    url = f'https://everyayah.com/data/{reader}/001001.mp3'
    meta, body = fetch(url, 'audio-'+reader, 4096, {'Range':'bytes=0-4095'})
    # Keep the HTTP/MIME/hash evidence, not unlicensed recording bytes in the repository.
    sample_path = OUT / ('audio-'+reader+'.body')
    if sample_path.exists(): sample_path.unlink()
    r = add('reciter-'+reader, 'quran', 'recitation_url_family', label, 'PENDING_LICENSE', source_org='EveryAyah',
            source_url=url, author_or_reciter=label, content_id=reader, license_url='',
            rights_scope='Playback/download disabled; streaming/cache/offline/APK grants must be established separately',
            phone_playback_result='NOT_RUN_DISABLED_BY_RIGHTS_GATE', offline_allowed='NO_GRANT',
            blocker='Recording grant not found', notes=f"Sample 1:1 only HTTP {meta.get('http')}; URL family /{reader}/{{surah:03d}}{{ayah:03d}}.mp3; other verses not probed")
    media.append(r.copy())
for name, channel, hls in [('haram','UCos52azQNBgW63_9uDJoPDA','saudi_quran'), ('nabawi','UCROKYPep-UuODNwyipe6JMw','saudi_sunnah')]:
    r = add('live-'+name, 'live', 'youtube_channel_live', name, 'PENDING_PHONE_TEST',
            source_org='Saudi Broadcasting Authority (configured channel; corroboration in evidence)',
            source_url=f'https://www.youtube.com/channel/{channel}/live', content_id=channel,
            license_url=providers['youtube-policy'], rights_scope='Official iframe/external source; no download/rehost grant',
            offline_allowed='NO_GRANT', phone_playback_result='NOT_RUN_THIS_AUDIT', blocker='Live availability/embedding is dynamic; phone retest required')
    media.append(r.copy())
    r = add('hls-'+name, 'live', 'hls_stream', name, 'PENDING_LICENSE',
            source_url=f'https://cdn-globecast.akamaized.net/live/eds/{hls}/hls_roku/index.m3u8',
            rights_scope='Disabled; do not extract or rehost', offline_allowed='NO_GRANT', blocker='No direct stream distribution grant')
    media.append(r.copy())
add('tafsir-missing','quran','missing_dataset','تفسير موثق كامل','NEEDS_SOURCE', blocker='No approved versioned edition in current app; QuranEnc candidate must have a retrievable edition/license; QF requires future backend')
add('dua-audio-missing','duas','missing_recording','تسجيلات الدعاء','NEEDS_SOURCE', blocker='No licensed recordings; disabled action retained')
add('community-demo-fixture','community','local_demo_metadata','12 fictional DEMO records','READY_WITH_CONDITIONS',
    source_org='Ihsan local fictional fixtures', source_version='CommunityDemoManager VERSION 1',
    local_asset_path='feature/src/main/java/com/example/feature/ehsan/data/demo/CommunityDemoManager.kt',
    rights_scope='Local fictional sample metadata only; photo rights tracked separately',
    offline_allowed='YES', attribution_required='NO',
    notes='Six offers and six requests. Blank phone numbers. Three photo category references; food/furniture/housing intentionally use category placeholders. No claim of actual charity fulfillment.')
add('learning-editorial-maps','learning','missing_editorial_material','Fahm station mapping / Nabi recipe text','NEEDS_SOURCE',
    source_org='Publisher material required', source_url='https://amrkhaled.net/',
    blocker='Do not infer station mapping or recipes from video titles; obtain approved publisher material or retain honest empty states')
for policy in sorted((ROOT/'docs/store').glob('*.md')):
    add('policy-'+policy.stem,'policies','owner_policy_draft',policy.stem,'PENDING_EDITORIAL_REVIEW',
        local_asset_path=policy.relative_to(ROOT).as_posix(),
        blocker='Owner identity/contact, final scope and public publication links need approval',
        rights_scope='Project draft; no claim of published legal policy')
add('tafsir-moyassar-candidate','quran','candidate_not_bundled','التفسير الميسر — QuranEnc','PENDING_LICENSE',
    source_url=providers['quranenc-moyassar'], license_url=providers['quranenc-moyassar'],
    source_version='Publisher home catalog: V1.0.0 — 15/02/2017; API sample edition equivalence not established',
    rights_scope='External reading link allowed; no new text bundled. Shared republication policy requires edition preservation',
    blocker='Confirm Arabic-edition redistribution scope and versioned full file: Arabic home catalog has no export links and list/ar returns an empty list',
    notes='Version found on publisher home catalog; official sample API returned seven translations; QA evidence only, not a complete approved release corpus')

# Every literal production URL occurrence, including templated/generated ones. No secret scanning/output.
links = []
for module in ('app','feature','designsystem'):
    for p in sorted((ROOT/module/'src/main').rglob('*')):
        if not p.is_file() or p.suffix not in ('.kt','.json','.xml','.txt','.html'): continue
        for number, line in enumerate(read(p).splitlines(), 1):
            for url in re.findall(r'https?://[^\s"<>\\]+', line):
                if 'schemas.android.com' in url: continue
                links.append(dict(file=p.relative_to(ROOT).as_posix(), line=number, url=url,
                                  classification='TEMPLATE_NOT_FETCHED' if '$' in url or '{' in url else 'REFERENCE_LITERAL_NOT_INDIVIDUALLY_FETCHED'))
save(OUT/'literal-url-occurrences.json', links)
known_urls={r['source_url'] for r in rows}
known_urls.update(providers.values())
normalize_literal=lambda url: url if '$' in url else url.rstrip(').,')
uncovered={normalize_literal(x['url']) for x in links if normalize_literal(x['url']) not in known_urls}
for i,url in enumerate(sorted(uncovered),1):
    sites=[x for x in links if normalize_literal(x['url'])==url]
    templated='$' in url or '{' in url
    meta = None if templated else fetch(url, f'literal-link-{i}', limit=262144)[0]
    add(f'literal-link-{i}', 'cross-feature', 'runtime_url_template' if templated else 'reference_link', url,
        'REJECTED' if meta and meta.get('http')==404 else 'EXTERNAL_LINK_ONLY', source_url=url,
        rights_scope='Reference or runtime template only; not a redistribution grant',
        local_asset_path=sites[0]['file'],
        notes=f"{len(sites)} occurrence(s); first line {sites[0]['line']}; {'TEMPLATE: never request unresolved parameters' if templated else 'GET HTTP '+str(meta.get('http'))+'; '+str(meta.get('error',''))}; strip sentence-ending punctuation in prose only")

def csv_write(path, data, fields):
    with path.open('w', encoding='utf-8-sig', newline='') as f:
        writer=csv.DictWriter(f, fieldnames=fields, extrasaction='ignore'); writer.writeheader(); writer.writerows(data)
for row in rows:
    if row['status'] == 'READY_WITH_CONDITIONS' and row['attribution_required'] == 'YES' and not row['attribution_text']:
        row['attribution_text'] = row['source_org'] + ' — ' + row['source_version'] + '; retain bundled notice'
    if row['verified_at'] == NOW and row['asset_id'].startswith(('youtube-', 'thumbnail-')):
        match = next(v for v in results if v['video_id'] == row['content_id'])
        row['verified_at'] = match['checked_at']
csv_write(RELEASE/'EXTERNAL_ASSET_REGISTER.csv', rows, COLS)
extra = ['collection','number','declared_title','actual_title','declared_duration','actual_channel','actual_channel_url','expected_channel_id','title_matches','channel_handle_matches','http','thumbnail_url','episode_url','embeddability','duration_verified','failure_reason']
csv_write(RELEASE/'MEDIA_LINK_AUDIT.csv', media, COLS+extra)
summary = dict(checked_at=NOW, asset_rows=len(rows), media_rows=len(media), videos=len(results),
    metadata_http200=sum(v.get('http')==200 for v in results), exact_title_matches=sum(v.get('title_matches',False) for v in results),
    expected_channel_handle_matches=sum(v.get('channel_handle_matches',False) for v in results),
    content=checks, literal_url_occurrences=len(links),
    status_counts={s:sum(r['status']==s for r in rows) for s in sorted({r['status'] for r in rows})})
save(OUT/'summary.json', summary)
print(json.dumps(summary, ensure_ascii=False, indent=2))
