"""Replace Phase A placeholders only after the declared Android evidence is complete."""
from pathlib import Path
import json, re

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / '.artifacts/video-audit-20261015'
qa = json.loads((BASE / 'qa-results.json').read_text(encoding='utf-8'))
assert qa['unit_failures'] == 0 and qa['matrix_completed'] == 8
assert qa['render_cases'] == 944
assert all(m['geometry_differences'] == 0 and m['overflow_failures'] == 0 for m in qa['matrix'].values())
required = ['p0-final', 'core-community-final', 'functional-final', 'native-and-mushaf',
            'system-delivery', 'alarm-after-fix', 'restart-prepare', 'restart-verify',
            'media-all-videos', 'feature-instrumented', 'final-interactions', 'timezone-change']
for name in required:
    assert qa['android'][name]['status'] == 'PASS', name
integrity = json.loads((BASE / 'content-integrity.json').read_text(encoding='utf-8'))
video_file = BASE / 'runtime/final-source-captures/release-journeys/video-results.txt'
video_results = dict(line.split('=', 1) for line in video_file.read_text(encoding='utf-8').splitlines() if '=' in line)
videos = json.loads((BASE / 'videos.json').read_text(encoding='utf-8'))
assert set(video_results) == {v['videoId'] for v in videos}
assert set(video_results.values()) <= {'ANDROID_PLAYING_CONFIRMED', 'ANDROID_EMBED_UNAVAILABLE_EXTERNAL_FALLBACK'}
playing = sum(v == 'ANDROID_PLAYING_CONFIRMED' for v in video_results.values())
video_doc = ['# نتائج تشغيل الفيديو الفعلي — Android API34، 2026-10-15', '',
    f'{len(video_results)} معرفًا فريدًا: {playing} إشارة PLAYING فعلية مرصودة، و{len(video_results)-playing} بديلًا عربيًا عند تعذر التضمين. ليست مراجعة مشاهدة كاملة أو ضمان توفر دائم أو استمرار التشغيل؛ يمكن أن يعود المشغل إلى buffering بعد الإشارة وقبل اللقطة.', '',
    '[سجل assertions](../../.artifacts/video-audit-20261015/runtime/media-all-videos.log)، [callbacks والنتائج الخام](../../.artifacts/video-audit-20261015/runtime/final-source-captures/release-journeys/video-results.txt). لقطات كل ID في مجلد release-journeys المجاور. لم يُستخدم HTTP أو thumbnail كإثبات تشغيل. فحص زر البديل في الحلقة يسجل callback؛ فتح handler خارجي حقيقي مقاس منفصلًا في final-interactions.', '',
    '| المجموعة | المصدر الرسمي | نتيجة Android |', '|---|---|---|']
for video in videos:
    vid = video['videoId']
    video_doc.append(f"| {video['collection']} | [{vid}]({video['url']}) | {video_results[vid]} |")
(ROOT / 'docs/release/VIDEO_PLAYBACK_RESULTS_2026-10-15.md').write_text('\n'.join(video_doc) + '\n', encoding='utf-8')
prefix = '../../.artifacts/video-audit-20261015/runtime/'
def log(name, label=None): return f'[{label or name}]({prefix}{name}.log)'

matrix_path = ROOT / 'docs/release/FEATURE_COMPLETENESS_MATRIX.md'
old = matrix_path.read_text(encoding='utf-8')
rows = []
for line in old.splitlines():
    if not line.startswith('| `'): continue
    cells = [v.strip() for v in line.split('|')[1:-1]]
    name = re.search(r'\((\w+)\)$', cells[0])[1]
    tests = log('core-community-final', 'Room/DataStore/Android')
    cases = 'العرض والتخطيط ونهاية التمرير؛ الحالات الفرعية حسب السيناريو المرتبط'
    status = 'PASS ضمن الوظائف المقاسة؛ عرض الشاشة وحده لا يثبت كل callback'
    if name.startswith('Quran'):
        tests += '; ' + log('p0-final', 'بحث حقيقي') + '; ' + log('functional-final', 'قارئ/حفظ') + '; ' + log('native-and-mushaf', 'ورق المصحف')
        cases = 'بحث/تحميل فعلي؛ نص 114/6236/604؛ قارئ/موضع/علامة؛ حالة تعطيل الصوت'
        status = 'PASS النص/البحث/الحفظ/العرض؛ BLOCKED التسجيلات والتنزيل EXT-01 والتفسير DATA-01'
    elif name.startswith(('Sanhya', 'Nabi', 'Fahm')):
        tests += '; ' + log('p0-final', 'حفظ Fahm') + '; ' + log('restart-verify', 'عملية جديدة') + '; ' + log('media-all-videos', '86 فيديو')
        cases = 'قوائم/بحث/فارغ/تفاصيل؛ حفظ الحالات؛ PLAYING أو fallback حسب كل ID'
        status = 'PASS الوظائف المحلية المقاسة ومعالجة الخطأ؛ تشغيل الناشر حسب VIDEO_PLAYBACK_RESULTS؛ DATA-04 للبيانات الناقصة'
    elif name in ('Donations','AddDonation','RequestHelp','IhsanDetails','LegacyDonationDetail','ManageListings','DonationHistory','Profile','EditProfile'):
        tests += '; ' + log('functional-final', 'تعديل/إلغاء/حذف') + '; ' + log('final-interactions', 'صور/منتقي/مسار قديم/وسيلة تواصل')
        cases = 'DEMO وUSER؛ عرض/طلب عبر النموذج؛ تعديل/إلغاء/حذف؛ صور محفوظة ومفقودة وفشل نسخ؛ لوحة مفاتيح؛ سجل غير موجود؛ اختيار PHONE/WHATSAPP محفوظ في Room8'
        status = 'PASS الرحلات المحلية المقاسة؛ الخدمات متعددة المستخدمين خارج النطاق SCOPE-01'
    elif name == 'GlobalSearch':
        tests = log('p0-final', 'استعلامات وربط وعدّ') + '; ' + log('final-interactions', 'تشكيل فقط')
        cases = 'الكهف/الفاتحة/نص آية/دعاء السفر/حديث؛ فئات وعدّ؛ استعلام فارغ التطبيع'
        status = 'PASS الاستعلامات والمراجع المقاسة مع screenshots وassertions'
    elif name == 'Prayer':
        tests = log('alarm-after-fix', '4 حالات بعد إصلاحها') + '; ' + log('system-delivery', 'AlarmManager فعلي') + '; ' + log('timezone-change', 'تغيير منطقة Android')
        cases = 'مدينة يدوية؛ حساب/اختلاف طرق بوحدات الاختبار؛ إعادة جدولة/إلغاء؛ منع تنبيه موقوف؛ وصول فعلي على API34'
        status = 'PASS الحساب والجدولة المقاسة؛ BLOCKED شروط الهاتف DEV-02؛ المنطقة الزمنية من إعداد الجهاز'
    elif name == 'Reminders':
        tests = log('system-delivery', 'WorkManager ووصول/منع إشعار')
        cases = 'تفعيل/إيقاف محفوظ؛ coordinator وموعد قريب فعلي؛ worker وإشعار؛ منع نشر التذكير المعطل'
        status = 'PASS على API34؛ BLOCKED OEM/Doze/إعادة تشغيل هاتف DEV-02'
    elif name in ('HaramLive','NabawiLive'):
        tests = log('p0-final', 'callbacks/إعادة محاولة') + '; ' + log('final-interactions', 'فتح مصدر فعلي')
        cases = 'تحميل/PLAYING/error150/مهلة؛ fallback عربي وإعادة محاولة؛ مصدر خارجي'
        status = ('BLOCKED تضمين الحرم error150؛ PASS البديل وإعادة المحاولة' if name == 'HaramLive' else 'PASS ANDROID_PLAYING_CONFIRMED في جلسة الاختبار') + '؛ HLS محجوب EXT-02'
    elif name == 'Qibla':
        tests = 'اختبارات حساب القبلة + مصفوفة حالة المحاكي؛ [قيد الهاتف](VIDEO_AUDIT_EXECUTION_2026-10-15.md)'
        cases = 'العرض وحالة عدم توفر مستشعر؛ لا إثبات دقة اتجاه من محاكي'
        status = 'PASS حساب/عرض حسب الدليل؛ BLOCKED المعايرة والاتجاه الفعلي DEV-01'
    elif name == 'Asma':
        tests = log('final-interactions', 'بحث لا يعيد 99 نتيجة زائفة') + '; اختبارات AsmaTodayResolver'
        cases = '99 اسمًا؛ بحث؛ إفصاح مصدر الشرح؛ العرض الفاتح/الداكن'
        status = 'PASS العرض والبحث المقاس؛ BLOCKED اعتماد الشروح DATA-02'
    elif name in ('Azkar','Tasbih','DailyActivities','Statistics'):
        tests += '; ' + log('restart-verify', 'عداد بعد إيقاف العملية') + '; اختبارات rollover/إحصاء/ViewModel'
        cases = 'عداد/حفظ/يوم جديد/إجمالي ومفضلة؛ إحصاء مشتق؛ نصوص وأهداف طويلة'
        status = 'PASS العمليات المقاسة؛ مراجعة مجموعة الأذكار القديمة DATA-03'
    elif name in ('Dua','DuaDetail','Hadith'):
        tests += '; ' + log('p0-final', 'بحث حقيقي')
        if name.startswith('Dua'): tests += '; ' + log('final-interactions', 'حافظة ومشاركة Android')
        cases = 'تحميل corpus حقيقي/مفضلة/بحث؛ تفاصيل ومرجع؛ صوت غير متاح بوضوح'
        status = 'PASS البيانات والبحث والحفظ المقاس؛ لا صوت مرخص'
    elif name == 'Settings':
        tests += '; ' + log('restart-verify', 'الثيم والقارئ بعد force-stop')
        cases = 'إعدادات محفوظة/تبديل ثيم؛ مصادر وروابط؛ حالات الأذونات في المصدر'
    elif name == 'LocationPermission':
        status = 'PASS عرض المحاكي؛ BLOCKED GPS ودقة الإذن على هاتف DEV-02'
    elif name == 'Splash':
        tests = log('core-community-final', 'بدء التطبيق الفعلي'); cases = 'بدء Koin/seed وفتح واجهة التطبيق؛ الوجهة مؤقتة'
    elif name == 'IhsanPlusDaily':
        status = 'PASS عرض debug؛ بوابة feature flag معطلة في release بحسب إعداد المشروع'
    cells[4] = cases
    cells[6] = tests + '; [مصفوفة](../../.artifacts/video-audit-20261015/runtime/progress.txt)'
    cells[7] = status
    rows.append('| ' + ' | '.join(cells) + ' |')
assert len(rows) == 58
header = f'''# مصفوفة اكتمال الوظائف — تدقيق الفيديو 2026-10-15

المصدر: المشروع الفعلي وتعديلاته الحالية. فُصلت نتائج التخطيط عن اختبارات السلوك وعن عوائق البيانات والخدمات. لا Backend أو commit أو push.

الجرد: {integrity['route_patterns']} نمط مسار، {integrity['production_files']} ملف مصدر/محتوى مبصوم، {integrity['callback_sites']} موضع callback. [جرد الأزرار والأسطر](../../.artifacts/video-audit-20261015/callbacks.json)، [المصادر والبصمات](../../.artifacts/video-audit-20261015/production-files.json)، [سلامة المحتوى](../../.artifacts/video-audit-20261015/content-integrity.json).

اجتازت 59 وجهة concrete × وضعين × أربعة أحجام 360/393/412/600dp × خطين 1.0/1.3 = **944 حالة عرض**، مع assertions للتخطيط ونهاية التمرير ولقطات. هذه ليست 944 اختبار وظيفة مستقلًا. Splash اختُبر عند البدء؛ المسار القديم redirect اختُبر منفصلًا. يبيّن عمود الدليل نطاق السلوك المقاس؛ لا تعني PASS أن كل callback خضع لنقرة مستقلة أو أن هاتفًا فعليًا اختُبر.

| Route/Screen | الوظائف/الأزرار | مصدر البيانات | حقيقي/DEMO | تحميل/فارغ/خطأ | الحفظ | اختبار عملي/الدليل | الحالة والسبب |
|---|---|---|---|---|---|---|---|
'''
matrix_path.write_text(header + '\n'.join(rows) + '\n\nمرجع القيود EXT/DATA/DEV/SCOPE ونتائج النسخ والبصمات: [التقرير النهائي](VIDEO_AUDIT_EXECUTION_2026-10-15.md). أدلة المحاولات الفاشلة محفوظة ولا تُحسب قبولًا.\n', encoding='utf-8')
print('Updated 58 rows from executed evidence; no blanket function PASS inferred.')
