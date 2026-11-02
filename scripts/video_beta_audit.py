"""Repeatable source inventory for the 2026-10-15 video audit. No Git mutations."""
from pathlib import Path
import hashlib, json, re, xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / '.artifacts/video-audit-20261015'
OUT.mkdir(parents=True, exist_ok=True)
production = []
callbacks = []
for module in ('app', 'feature', 'designsystem'):
    for file in sorted((ROOT / module / 'src/main').rglob('*')):
        if not file.is_file():
            continue
        if file.suffix in ('.kt', '.xml', '.json', '.txt'):
            raw = file.read_bytes()
            production.append({'file': file.relative_to(ROOT).as_posix(), 'bytes': len(raw), 'sha256': hashlib.sha256(raw).hexdigest()})
        if file.suffix == '.kt':
            for n, line in enumerate(file.read_text(encoding='utf-8-sig').splitlines(), 1):
                if re.search(r'onClick\s*=|onCheckedChange\s*=|onValueChange\s*=|onPrimaryAction\s*=', line):
                    callbacks.append({'file': file.relative_to(ROOT).as_posix(), 'line': n, 'expression': line.strip()})

screen = (ROOT / 'app/src/main/java/com/example/mol/navigation/Screen.kt').read_text(encoding='utf-8-sig')
routes = [{'name': m[1], 'route': m[2]} for m in re.finditer(r'object\s+(\w+)\s*:\s*Screen\(\s*(?:route\s*=\s*)?"([^"]+)"', screen)]
for name, data in [('production-files', production), ('callbacks', callbacks), ('routes', routes)]:
    (OUT / f'{name}.json').write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')

assets = ROOT / 'feature/src/main/assets'
quran = json.loads((assets / 'quran_full.json').read_text(encoding='utf-8-sig'))
flat = [(s['id'], v['id']) for s in quran for v in s['verses']]
pages = json.loads((assets / 'mushaf_page_starts.json').read_text())
offsets = [flat.index(tuple(p)) for p in pages] + [len(flat)]
xml = ET.parse(assets / 'licenses/tanzil-uthmani-1.1.xml').getroot()
original = {(int(s.attrib['index']), int(a.attrib['index'])): a.attrib['text'] for s in xml.findall('sura') for a in s.findall('aya')}
corpus = json.loads((assets / 'hadeethenc_ar_v1.7.0.json').read_text(encoding='utf-8-sig'))
checks = {'surahs': len(quran), 'ayahs': len(flat), 'pages': len(pages), 'unique_verse_keys': len(set(flat)),
          'page_partition_complete': offsets[0] == 0 and all(a < b for a, b in zip(offsets, offsets[1:])) and offsets[-1] == 6236,
          'verbatim_tanzil': all(original[(s['id'], v['id'])] == v['text'] for s in quran for v in s['verses']),
          'hadith_records': len(corpus['records']), 'hadith_unique_ids': len({r['id'] for r in corpus['records']}),
          'production_files': len(production), 'callback_sites': len(callbacks), 'route_patterns': len(routes)}
(OUT / 'content-integrity.json').write_text(json.dumps(checks, indent=2), encoding='utf-8')
videos = []
for feature, filename in [('sanhya','SanhyaFixtureData.kt'), ('nabiihsan','NabiIhsanFixtureData.kt'), ('fahmanallah','FahmFixtureData.kt')]:
    path = ROOT / f'feature/src/main/java/com/example/feature/{feature}/data/datasource/{filename}'
    videos += [{'collection': feature, 'videoId': vid, 'url': f'https://www.youtube.com/watch?v={vid}'}
               for vid in dict.fromkeys(re.findall(r'EpisodeRecord\([^\n]*?,\s*"([A-Za-z0-9_-]{11})"', path.read_text(encoding='utf-8-sig')))]
(OUT / 'videos.json').write_text(json.dumps(videos, indent=2), encoding='utf-8')
(OUT / 'video-ids.txt').write_text(','.join(dict.fromkeys(v['videoId'] for v in videos)), encoding='utf-8')

def family(name):
    if name.startswith('Quran'): return ('Quran', 'فتح سورة/صفحة/جزء/آية؛ بحث؛ حفظ موضع/علامة؛ قارئ؛ تنزيل/صوت', 'Tanzil assets + Room QuranDao + UserPreferences؛ التسجيلات PENDING_LICENSE', 'نص موثق؛ الصوت محجوب بحقوقه', 'Room + DataStore', 'ReleaseCorePersistenceTest; ReleaseFunctionalJourneyTest; VideoAuditRegressionTest')
    if name.startswith('Sanhya'): return ('Sanhya', 'تفاصيل/حلقات/آيات؛ بحث/فلترة؛ مفضلة/لاحقًا؛ مصدر/فيديو؛ رجوع', 'SanhyaFixtureData + PreferenceLearningBookmarkStore + YouTube', 'فهرس الناشر؛ لا إعادة استضافة', 'DataStore', 'LearningInteractionTest; VideoAuditRegressionTest; media-runtime')
    if name.startswith('Nabi'): return ('Nabi', 'حلقات/دروس/وصفات؛ بحث/فلترة؛ مفضلة/لاحقًا؛ مصدر/فيديو', 'NabiIhsanFixtureData + LearningBookmarkStore + YouTube', 'فهرس الناشر؛ بعض الوصفات غير متاحة', 'DataStore', 'LearningInteractionTest; media-runtime')
    if name.startswith('Fahm'): return ('Fahm', 'دروس/منازل/رحلة؛ بحث؛ مفضلة/لاحقًا؛ إتمام/استئناف؛ إعادة ضبط مع تأكيد؛ فيديو', 'FahmFixtureData + FahmRepositoryImpl + UserPreferences + YouTube', 'فهرس الناشر؛ خرائط المنازل التحريرية ناقصة', 'DataStore بعد إصلاح هذه المراجعة', 'VideoAuditRegressionTest; FahmRepositoryTest; media-runtime')
    if name in ('Donations','AddDonation','RequestHelp','IhsanDetails','LegacyDonationDetail','ManageListings','DonationHistory'):
        return ('Community','قائمة/بحث/فلترة؛ إنشاء عرض/طلب؛ صورة/ألبوم؛ تعديل/حالة/حذف مع تأكيد؛ تواصل؛ إعادة DEMO', 'Room DonationDao/UserDao + EhsanImageStore + CommunityDemoManager', '12 DEMO خيالية + USER محلي + LEGACY؛ لا خدمة متعددة المستخدمين', 'Room + private images؛ ملكية بمعرف ملف محلي', 'ReleaseCommunityPersistenceTest; ReleaseFunctionalJourneyTest; OfferAlbumStorageTest; OfferDraftRestorationTest')
    return {
      'Prayer': ('Prayer','مدينة/إذن موقع؛ حساب/عد تنازلي؛ إعداد طريقة/مذهب/تنبيهات؛ سجل صلاة','AdhanPrayerCalculator + facade + PrayerSettings DataStore + Room prayer log + AlarmManager','حساب فعلي محلي','Room + DataStore','PrayerIndependentReferenceTest; HomePrayerParityTest; alarm-runtime'),
      'Qibla': ('Qibla','إذن موقع؛ اتجاه/مستشعر؛ معايرة/تحذير غياب المستشعر','PrayerLocationRepository + QiblaManager + geomagnetic declination','حساب ومستشعر فعلي','الموقع DataStore؛ الاتجاه لحظي','GetQiblaDirectionUseCaseTest; physical-device BLOCKED'),
      'GlobalSearch': ('Search','استعلام؛ فئات؛ عد صحيح؛ نتيجة تفتح التفاصيل الصحيحة؛ مسح/رجوع','جميع المستودعات المحلية + ArabicSearch + QuranSearch','نتائج أصلية وDEMO موسومة حسب المصدر','الاستعلام مؤقت','VideoAuditRegressionTest; ArabicSearchTest; QuranSearchTest'),
      'Azkar': ('Azkar','بحث/فئات؛ عد/إعادة ضبط؛ مفضلة؛ نسخ/مشاركة؛ يوم جديد','AzkarSeedManager + Room AzkarDao/daily_stats','نصوص مصدرها جزئي المراجعة؛ لا صوت مرخص','Room','ReleaseCorePersistenceTest; AzkarDailyRolloverTest'),
      'Tasbih': ('Tasbih','عد بلا سقف؛ هدف؛ reset؛ سجل وإحصاء','Room AzkarDao + daily_stats','سجلات فعلية؛ الأهداف اختيارية','Room','TasbihViewModelTest; ReleaseCorePersistenceTest'),
      'Statistics': ('Statistics','أسبوع/شهر؛ أعداد وتقدم؛ سجل الصلاة','StatisticsRepository + Room daily_stats/prayer logs','قيم مشتقة من السجلات','Room','StatisticsViewModelTest; PrayerTrackerStatsCalculatorTest'),
      'DailyActivities': ('Daily','تنفيذ/تحديد الورد؛ فتح الوجهة؛ تقدم/يوم جديد','DailyActivityRepository + UserPreferences + date policy','قيم مشتقة من السجلات','DataStore','ReleaseCorePersistenceTest; DailyActivitiesRedesignTest'),
      'Dua': ('Dua','7 أقسام؛ بحث/فلترة؛ مفضلة؛ فتح تفاصيل','verified_duas.json + Room DuaDao','39 نصًا من Tanzil/HadeethEnc؛ تقسيم تحريري','Room','ReleaseContentAndPlayerTest; DuaViewModelTest; VerifiedDuaIntegrityTest'),
      'DuaDetail': ('Dua','عرض نص/مرجع؛ نسخ/مشاركة؛ مفضلة؛ مفقود','Room DuaDao','نص موثق؛ لا تسجيل صوتي مرخص','Room','ReleaseContentAndPlayerTest; VerifiedDuaIntegrityTest'),
      'Hadith': ('Hadith','بحث/فئة/مفضلة؛ تفاصيل/تخريج/مصدر؛ نسخ/مشاركة','HadeethEnc ar v1.7.0 + Room HadithDao','3582 سجلًا؛ الحكم للناشر','Room','ReleaseContentAndPlayerTest; search-runtime'),
      'Asma': ('Asma','99 اسمًا؛ بحث/مفضلة؛ تفاصيل؛ إفصاح المراجعة','asma.json + UserPreferences','شرح تحريري PENDING_SOURCE_REVIEW','DataStore','AsmaTodayResolverTest; responsive matrix'),
      'HaramLive': ('Live','تبديل الحرم؛ تحميل/فشل؛ إعادة محاولة؛ مصدر رسمي؛ ملء شاشة/مشاركة','SBA official channel iframe؛ HLS محجوب','الناشر الخارجي؛ لا إعادة بث','اختيار الجلسة','live-runtime'),
      'NabawiLive': ('Live','تبديل النبوي؛ تحميل/فشل؛ إعادة محاولة؛ مصدر رسمي؛ ملء شاشة/مشاركة','SBA official channel iframe؛ HLS محجوب','الناشر الخارجي؛ لا إعادة بث','اختيار الجلسة','live-runtime'),
      'Profile': ('Profile','ملف محلي؛ دخول/خروج؛ إدارة عروض/طلبات؛ حذف البيانات مع تأكيد','Room UserDao/DonationDao + UserPreferences','هوية جهاز محلية؛ الهاتف ليس تحقق خادم','Room + DataStore','ReleaseCommunityPersistenceTest; EditProfileViewModelTest'),
      'EditProfile': ('Profile','صورة/اسم/هاتف/مدينة/نبذة؛ حفظ/إلغاء؛ تحقق','Room UserDao + private avatar + UserPreferences','بيانات مستخدم محلية','Room + DataStore','EditProfileViewModelTest; LearningInteractionTest'),
      'Settings': ('Settings','ثيم؛ إذن/إعدادات؛ مصادر/خصوصية؛ روابط','SettingsManager + UserPreferences + Android settings intents','إعدادات فعلية','DataStore','SettingsViewModelTest; persistence-runtime'),
      'Reminders': ('Reminders','تفعيل/وقت/حفظ؛ إذن النظام؛ جدولة/إلغاء','DataStore + WorkManager + notifications','جدولة فعلية؛ التوصيل على هاتف مطلوب','DataStore + WorkManager','reminder-runtime'),
    }.get(name, (name,'تنقل/رجوع؛ أزرار الشاشة المسجلة في callbacks.json','AppNavHost + viewmodel/preferences المحلية','محلي؛ العرض ليس إثبات وظيفة','حسب الشاشة','responsive matrix / startup'))

lines = ['# مصفوفة اكتمال الوظائف — تدقيق الفيديو 2026-10-15', '',
         'المصدر: المشروع الحالي، بما فيه تعديلات سابقة غير محفوظة. فحص المصدر لا يساوي اختبار الوظيفة. هذه النسخة الأولية Phase A؛ تقرير النتائج النهائي يربط كل PASS بسجل تشغيل. لا Backend أو commit أو push.', '',
         f"الجرد الآلي: {len(routes)} نمط مسار؛ {len(production)} ملف مصدر/محتوى مقروء ومبصوم؛ {len(callbacks)} موضع callback. [الأزرار مع الأسطر](../../.artifacts/video-audit-20261015/callbacks.json)، [محتوى وبصمات](../../.artifacts/video-audit-20261015/production-files.json)، [سلامة البيانات](../../.artifacts/video-audit-20261015/content-integrity.json).", '',
         '| Route/Screen | الوظائف/الأزرار | مصدر البيانات | حقيقي/DEMO | تحميل/فارغ/خطأ | الحفظ | اختبار عملي/الدليل | الحالة والسبب |',
         '|---|---|---|---|---|---|---|---|']
for r in routes:
    group, actions, source, kind, saved, test = family(r['name'])
    lines.append(f"| `{r['route']}` ({r['name']}) | {actions} | {source} | {kind} | تدقيق حالات المصدر؛ إثباتها حسب السيناريو في سجل QA | {saved} | {test} — لم ينفذ بعد في Phase A | NOT_RUN؛ لا نجاح مفترض |")
lines += ['', '## حدود مستقلة يجب إثباتها', '',
          'كل عرض واجهة قابل للزيارة يُختبر مستقلًا عن اعتماد محتواه الخارجي. ترخيص التلاوات/HLS، التفسير، مراجعة معاني الأسماء، اعتماد فهرس المنازل، معايرة القبلة والإشعارات على هاتف فعلي تبقى قيودًا منفصلة؛ سجلها النهائي في VIDEO_AUDIT_EXECUTION_2026-10-15.md.', '',
          'الملفات القديمة EXECUTION_STATUS وFUNCTIONAL_GAPS محفوظة كأدلة سابقة، وليست إثبات نجاح لهذه المراجعة.']
matrix = ROOT / 'docs/release/FEATURE_COMPLETENESS_MATRIX.md'
if not matrix.exists(): matrix.write_text('\n'.join(lines) + '\n', encoding='utf-8')
print(json.dumps(checks, ensure_ascii=False))
