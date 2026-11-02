"""Summarize existing evidence only; never interprets a missing phone result as PASS."""
from pathlib import Path
import csv,json,re,xml.etree.ElementTree as ET,collections,datetime
ROOT=Path(__file__).resolve().parents[1]
R=ROOT/'docs/release';E=R/'external-evidence/2026-10-17'
phone=E/next(name for name in ['phone-verified','phone-stable','phone-foreground'] if (E/name).exists())
ledgers=list(phone.rglob('video-results.jsonl'))
results={}
for ledger in ledgers:
    for line in ledger.read_text(encoding='utf-8').splitlines():
        try:
            row=json.loads(line);results[row['videoId']]=row
        except (ValueError,KeyError): pass
transcript=(phone/'instrumentation.txt').read_text(encoding='utf-8-sig',errors='replace') if (phone/'instrumentation.txt').exists() else ''
host_status=dict((vid,status) for status,vid in re.findall(r'physicalMediaStatus=(\w+).*?physicalMediaVideoId=([\w-]{11})',transcript,re.S))
for file in ['EXTERNAL_ASSET_REGISTER.csv','MEDIA_LINK_AUDIT.csv']:
    path=R/file
    with path.open(encoding='utf-8-sig',newline='') as f:
        reader=csv.DictReader(f);fields=reader.fieldnames;rows=list(reader)
    for row in rows:
        if row['asset_type']!='youtube_video':continue
        vid=row['content_id']
        if vid in results:
            actual=results[vid]
            row['phone_playback_result']=json.dumps(actual,ensure_ascii=False,separators=(',',':'))
            row['notes']+='; VISUAL_ACCEPTANCE_BLOCKED: owner reports audible sound but no visible video during batch. Debug probe only; time progression does not prove picture rendering. See owner-observation.json.'
        elif vid in host_status:
            row['phone_playback_result']='HOST_REPORTED_'+host_status[vid].upper()+'_DETAIL_LEDGER_NOT_RETRIEVED'
            row['notes']+='; Host transcript contains this result but phone disconnected before detailed ledger retrieval. Do not infer successful playback.'
        else:
            row['phone_playback_result']='NOT_VERIFIED_PHONE_RUN_INTERRUPTED_BY_DISCONNECTION'
        row['status']='PENDING_PHONE_TEST'
        row['blocker']='Visual acceptance blocked: black picture reported on physical phone; see EXTERNAL_QA_RESULTS.md'
    with path.open('w',encoding='utf-8-sig',newline='') as f:
        writer=csv.DictWriter(f,fieldnames=fields);writer.writeheader();writer.writerows(rows)
unit=dict(tests=0,failures=0,errors=0,skipped=0,classes=0)
for path in (ROOT/'feature/build/test-results/testDebugUnitTest').glob('TEST-*.xml'):
    x=ET.parse(path).getroot();unit['classes']+=1
    for key in ['tests','failures','errors','skipped']:unit[key]+=int(x.attrib.get(key,0))
lint=ET.parse(ROOT/'app/build/reports/lint-results-debug.xml').getroot()
lint_counts=dict(collections.Counter(i.attrib['severity'] for i in lint.findall('issue')))
restored=(phone/'restoration.json').exists()
progress=sum(r.get('requestedIdentityConfirmed') is True and r.get('progressConfirmed') is True for r in results.values())
live_results=[]
for ledger in phone.rglob('live-results.jsonl'):
    live_results.extend(json.loads(line) for line in ledger.read_text(encoding='utf-8').splitlines() if line.strip())
log_path=phone/'player-logcat.txt'
if log_path.exists():
    log=log_path.read_text(encoding='utf-8-sig',errors='replace')
    pivot=log.rfind('VIDEO_START id=Kx9Wj4vP69A')
    if pivot>=0:
        start=log.rfind('\n',0,pivot)+1
        log=log[start:]
        (phone/'correlated-player-logcat.txt').write_text(log,encoding='utf-8')
    errors=[(float(t)*1000,int(code)) for t,code in re.findall(r'^\s*(\d+\.\d+).*?Official player error: (\d+)',log,re.M)]
    for row in live_results:
        row['correlated_error_codes']=[code for t,code in errors if row['startedAtEpochMs']<=t<=row['finishedAtEpochMs']]
for file in ['EXTERNAL_ASSET_REGISTER.csv','MEDIA_LINK_AUDIT.csv']:
    path=R/file
    with path.open(encoding='utf-8-sig',newline='') as f:
        reader=csv.DictReader(f);fields=reader.fieldnames;rows=list(reader)
    for row in rows:
        if row['asset_type']!='youtube_channel_live':continue
        actual=next((r for r in live_results if r['channelId']==row['content_id']),None)
        if actual:
            row['phone_playback_result']=json.dumps(actual,ensure_ascii=False,separators=(',',':'))
            row['notes']+='; Physical debug probe; error codes correlated by live start/end timestamps. Nabawi screenshot visibly renders; Haram shows unavailable message and official external actions. See live-visual-review.json.'
    with path.open('w',encoding='utf-8-sig',newline='') as f:
        writer=csv.DictWriter(f,fieldnames=fields);writer.writeheader();writer.writerows(rows)
summary=dict(verified_at=datetime.datetime.now(datetime.timezone.utc).isoformat(),feature_unit_tests=unit,lint_issue_counts=lint_counts,
             videos_with_retrieved_phone_ledger=len(results),host_statuses=host_status,
             videos_with_confirmed_identity_and_progress=progress,live_results=live_results,
             owner_observation=json.loads((E/'owner-observation.json').read_text(encoding='utf-8')),
             original_qa_apk_restore='BYTE_VERIFIED' if restored else 'PENDING_PHONE_RECONNECTION',
             public_release_acceptance=False)
(E/'qa-summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2),encoding='utf-8')
text=f'''# نتائج التحقق — 17 أكتوبر 2026

البناء المحلي والتحقق الساكن ناجحان؛ **قبول الفيديو المرئي فشل في المشاهدة التي أكدها المالك على الهاتف: الصوت يعمل والصورة لا تظهر**. تقدم الزمن أو PLAYING لا يثبتان نجاح عرض الفيديو. لا نتائج محاكي ضمن قبول هذه المهمة.

| الفحص | النتيجة والدليل |
|---|---|
| بناء Debug + بناء AndroidTest | `BUILD SUCCESSFUL`؛ [.artifacts/external-resources-build-final-20261017.log](../../.artifacts/external-resources-build-final-20261017.log). الحزمة `com.example.mol.referenceqa` فقط؛ لا تعديل تثبيت المستخدم الرئيسي. |
| اختبارات وحدة feature | {unit['tests']} اختبارًا، {unit['failures']} فشلًا، {unit['errors']} أخطاء، {unit['skipped']} متجاوزة؛ {unit['classes']} ملف نتيجة. Gradle يعيد استخدام نتائج UP-TO-DATE عند صلاحيتها؛ لا تشغيل محاكي. |
| Lint Debug | {json.dumps(lint_counts,ensure_ascii=False)}. نجاح المهمة لا يعني خلو المشروع السابق من التحذيرات. |
| سلامة سجلات التسليم | [validation.json](external-evidence/2026-10-17/validation.json): المعرفات والحالات والمسارات؛ 86 فيديو؛ مطابقة الأدعية وذكري النوم؛ جميع حقول 3582 حديثًا تطابق خلايا ملف XLSX الأصلي المحتفظ به، وبصمته تطابق سجل الاستيراد. |
| سلامة نص القرآن | [content-integrity.json](external-evidence/2026-10-17/content-integrity.json): 6236 آية مطابقة لنسخة Tanzil المرفقة. |
| فحص روابط الفيديو | 86/86 بيانات oEmbed وعنوان وقناة مطابقون. المدد/التواريخ وإتاحة التضمين الفعلية ليست نتائج هذا الفحص. |
| فحص الصور | 99 ملفًا قابلًا للفك؛ مراجعة لوحة 33 أصلًا رئيسيًا بصريًا. لا صور مضافة. حقوق الملفات القديمة ما زالت معلّقة. |
| فحص الهاتف | Xiaomi 23129RAA4G، serial ac190123. الجولة المعتمدة: `{phone.name}`. سجلات مسترجعة لـ{len(results)} فيديو؛ تأكيد هوية الفيديو وتقدم الزمن لـ{progress}. نتائج بث مسترجعة: {len(live_results)}. لا يثبت ذلك سماع الصوت أو قبول APK إصدار نهائي. |
| المشاهدة البشرية | [owner-observation.json](external-evidence/2026-10-17/owner-observation.json): المالك أكد أثناء الجولة «الصوت يعمل والصورة والفيديو لايظهر ابدا». لقطات فُحصت أظهرت مساحة سوداء أيضًا. قبول الصورة **BLOCKED**، وليس مجرد طلب تأكيد صوت. لا يُنسب ذلك إلى حذف الـ86 فيديو أو تغيير قنواتها. |
| البثان | الحرم أعاد 150 في فترة اختباره، وظهر عدم التوفر مع أزرار المصدر الخارجي والبوابة الرسمية. النبوي أظهر صورة فعلية في اللقطة وتقدم زمن الفيديو. [المراجعة البصرية](external-evidence/2026-10-17/live-visual-review.json) منفصلة عن قبول الدروس. |
| إعادة APK السابق | **{'تمت مطابقة إعادة النسخة بايتًا ببايت' if restored else 'معلقة: الهاتف غير متصل عند محاولة الإعادة'}**. الأصل محفوظ ببصمة `514b7d101750f791bb9583236c539ee9401db19a06c476be545223381b30d121` في `.artifacts/external-resources-phone-20261017/installed-before.apk`. لا مسح بيانات أو إزالة تثبيت. |

## استكمال الهاتف

طلبت إعادة توصيل الجهاز بعد انقطاع الجولة الأولى؛ أعاد المالك توصيله، فاسترجع السجل وأعيد APK الأصلي بايتًا ببايت. لإعادة نسخة جلسة محددة يستعمل `scripts/restore_external_phone_qa.ps1 -EvidenceName اسم-مجلد-الجلسة` بعد تحقق هوية الجهاز وبصمة الأصل. لا تُستبدل النسخة الاحتياطية.

المحاولة الأولى في `external-evidence/2026-10-17/phone/` توقفت بعد حظر MIUI قبل اختبار أي فيديو، وأُعيد APK القديم. في `phone-foreground/` تأكد تقدم أول ثلاثة فيديوهات، ثم غطت نافذة أخرى الاختبار واختفت شجرة Compose؛ لا تعتمد الحالات اللاحقة كأعطال روابط. توقفت `phone-stable` قبل أول فيديو بسبب انتظار تركيز النافذة. بعد ضبط الانتظار اكتملت `phone-verified` بسجلات الـ86 والبثين. لا Emulator أو تغيير لإعدادات OEM. كلمة Blocked في الأداة قد تعني طلب تأكيد الصوت رغم تقدم الفيديو؛ المرجع هو حقول الهوية والزمن والسبب والمشاهدة البشرية.

القراء محجوبون بالحقوق ولم تُشغل تسجيلاتهم. الأذان لا يملك إثبات حقوق نشر عام ولا ادعاء اختبار سمعي جديد. المالك أكد سماع صوت الدروس أثناء الجولة، وهو إثبات بشري للعينات التي لاحظها، وليس استماعًا كاملًا مستقلًا لكل فيديو. عائق الصورة وقبول APK النهائي يبقيان مفتوحين حتى مع تقدم زمن WebView. لا Backend أو إعادة تصميم أو Git Commit/Push/Reset. تفاصيل البث في `live-results.jsonl` وسجل الأخطاء المرتبط، ولا يُستخرج HLS عند الرفض.

فحص `git diff --check` العام كشف مسافات/أسطر قديمة في ملفات خارج تغييرات هذه المهمة؛ لم تُعدل لمجرد تنظيفها. البناء وLint لا يغلقان حقوق الصور أو الأذان أو المراجعة الدينية.

تجربتا iframe المباشر وإزالة القص على أول فيديوهين لم تصلحا السواد، ورُدّ التعديلان الإنتاجيان. السبب الدقيق غير مثبت، والتصميم الأصلي محفوظ. [أدلة التشخيص وحدوده](external-evidence/2026-10-17/RENDERER_DIAGNOSIS.md)؛ لا تُعد تجربة عينتين إعادة قبول لكل الـ86.
'''
(R/'EXTERNAL_QA_RESULTS.md').write_text(text,encoding='utf-8')
print(json.dumps(summary,ensure_ascii=True,indent=2))
