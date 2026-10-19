"""Publish locally verified candidate provenance without inventing phone acceptance."""
from pathlib import Path
import hashlib
import json
import os
import shutil
import subprocess
import xml.etree.ElementTree as ET
from datetime import datetime, timezone

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / '.artifacts/physical-device-qa/session-20261016'

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest().upper()

def main():
    build = OUT/'delivery-build.log'
    assert 'BUILD SUCCESSFUL' in build.read_text(encoding='utf-8-sig'), 'Final build not successful yet'
    source = json.loads((OUT/'delivery-source-before.json').read_text(encoding='utf-8'))
    for record in source:
        assert sha(ROOT/record['path']).lower() == record['sha256'], f"Source changed during build: {record['path']}"
    shutil.copy2(OUT/'delivery-source-before.json', OUT/'delivery-source.json')
    candidate = ROOT/'app/build/outputs/apk/release/app-release.apk'
    assert candidate.stat().st_mtime >= (ROOT/'app/src/main/java/com/example/mol/navigation/AppNavHost.kt').stat().st_mtime
    digest = sha(candidate)
    name = f'Ihsan-physical-beta-20261017-rc-{digest[:8].lower()}.apk'
    destination = ROOT/'docs/release/apk'/name
    shutil.copy2(candidate, destination)
    env = dict(os.environ, JAVA_HOME=r'C:\Users\molha\.jdks\jbr-21.0.3')
    signing = subprocess.run([r'C:\android_sdk\build-tools\35.0.0\apksigner.bat','verify','--verbose','--print-certs',str(destination)], env=env, capture_output=True, text=True, check=True).stdout
    assert '870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63' in signing
    (OUT/'delivery-signature.txt').write_text(signing,encoding='utf-8')
    manifest = subprocess.run([r'C:\android_sdk\build-tools\35.0.0\aapt.exe','dump','badging',str(destination)],capture_output=True,text=True,encoding='utf-8',check=True).stdout
    assert "package: name='com.example.mol.referenceqa'" in manifest and 'application-debuggable' not in manifest
    (OUT/'delivery-manifest.txt').write_text(manifest,encoding='utf-8')
    apkout = OUT/'delivery-apk'; apkout.mkdir(exist_ok=True)
    diagnostic = []
    for src, label in [
        (ROOT/'app/build/outputs/apk/debug/app-debug.apk','app-referenceqa-debug.apk'),
        (ROOT/'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk','app-referenceqa-tests.apk'),
        (ROOT/'feature/build/outputs/apk/androidTest/debug/feature-debug-androidTest.apk','feature-migration-tests.apk')
    ]:
        assert src.exists(), src
        shutil.copy2(src,apkout/label)
        diagnostic.append(dict(path=str((apkout/label).relative_to(ROOT)),sha256=sha(apkout/label)))
    suites = []
    for module in ['app','feature','designsystem']:
        source_dir = ROOT/module/'build/test-results/testDebugUnitTest'
        shutil.copytree(source_dir,OUT/'unit-results'/module,dirs_exist_ok=True)
        for xml in source_dir.glob('TEST-*.xml'):
            row = ET.parse(xml).getroot()
            suites.append(dict(module=module,file=xml.name,**{k:int(row.get(k,0)) for k in ['tests','failures','errors','skipped']}))
    totals = {k:sum(s[k] for s in suites) for k in ['tests','failures','errors','skipped']}
    assert totals['tests'] == 477 and totals['failures'] == totals['errors'] == totals['skipped'] == 0, totals
    (OUT/'unit-summary.json').write_text(json.dumps(dict(summary=totals,suites=suites),indent=2),encoding='utf-8')
    lint_src = ROOT/'app/build/reports/lint-results-debug.xml'
    shutil.copy2(lint_src,OUT/'delivery-lint.xml')
    issues = ET.parse(lint_src).getroot().findall('issue')
    lint = {severity:sum(x.get('severity') == severity for x in issues) for severity in ['Error','Fatal','Warning']}
    assert lint['Error'] == lint['Fatal'] == 0, lint
    (OUT/'delivery-lint-summary.json').write_text(json.dumps(lint,indent=2),encoding='utf-8')
    test_sources = sorted({p for module in ['app','feature','designsystem']
        for folder in ['src/androidTest','src/test'] for p in (ROOT/module/folder).rglob('*') if p.is_file()}
        | {p for p in (ROOT/'feature/schemas').rglob('*.json')})
    (OUT/'delivery-test-sources.json').write_text(json.dumps([
        dict(path=p.relative_to(ROOT).as_posix(),sha256=sha(p)) for p in test_sources
    ],indent=2),encoding='utf-8')
    provenance = dict(capturedAtUtc=datetime.now(timezone.utc).isoformat(),apk=str(destination.relative_to(ROOT)),sha256=digest,
        sourceDigest=sha(OUT/'delivery-source.json'),sourceFiles=len(source),certificateSha256='870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63',
        package='com.example.mol.referenceqa',releaseInstalled=False,releasePhoneAcceptance='NOT_EXECUTED_PHONE_DISCONNECTED',
        testSourceDigest=sha(OUT/'delivery-test-sources.json'),testSourceFiles=len(test_sources),
        unitTests=totals,lint=lint,diagnostics=diagnostic)
    (OUT/'delivery-provenance.json').write_text(json.dumps(provenance,indent=2),encoding='utf-8')
    for doc in ['FINAL_PHYSICAL_DEVICE_QA.md','FINAL_BETA_GAP_REPORT.md']:
        old = ROOT/'docs/qa'/doc
        archive = OUT/f'previous-{doc}'
        if not archive.exists(): shutil.copy2(old,archive)
    evidence = '../../.artifacts/physical-device-qa/session-20261016'
    physical = f'''# إحسان — قبول الهاتف الحقيقي

تحديث: 2026-10-17، Asia/Damascus. القرار: **RC محلية جاهزة لاستكمال الاختبار؛ قبول Feature-Complete Beta غير مكتمل**.

## النسخ وحدود الإثبات

- الهاتف الوحيد المستخدم: `ac190123`، Xiaomi `23129RAA4G`، Android 15 / API 35. اجتاز preflight في 16 أكتوبر. لم يُستخدم Emulator أو AVD.
- APK الهاتف الذي اختُبر مباشرة: `Ihsan-physical-device-20261016-rc-514b7d10.apk`؛ SHA-256 `514B7D101750F791BB9583236C539EE9401DB19A06C476BE545223381B30D121`.
- آخر نسخة مبنية من الإصلاحات: [{name}](../release/apk/{name})، الحزمة `com.example.mol.referenceqa`، versionCode 1، versionName 1.0؛ Release مصغرة وغير debuggable. **لم تثبت هذه النسخة الجديدة بعد انقطاع الهاتف**.
- SHA-256 الأخيرة: `{digest}`. شهادة Android Debug المطابقة للمثبت: `870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`؛ ليست شهادة متجر.
- [Provenance]({evidence}/delivery-provenance.json): بصمات {len(source)} ملف إنتاج، Manifest، التوقيع، APKات التشخيص، الوحدات وLint. لا تُعمم نتائج APK الهاتف السابق على الإصلاحات الجديدة.
- انقطع ADB أثناء رحلة البحث يوم 16 أكتوبر؛ فحص 17 أكتوبر لا يظهر هاتفًا. Xiaomi رفض حزمة instrumentation الجديدة بـ`INSTALL_FAILED_USER_RESTRICTED`. طلب المستخدم متابعة الفحص المباشر حتى يجهز الإذن؛ لم تصل إفادة بجاهزيته.

## حماية البيانات

حُفظ APK المثبت السابق وفُحص توقيعه قبل التغيير في `.artifacts/physical-device-qa/20261016T142231Z`. لأن Release غير debuggable وتعطل Android Backup، ثُبتت نسخة Debug بالتوقيع نفسه عبر `adb install -r`، وأوقفت عمليتها ثم نسخت بياناتها الخاصة بـ`run-as tar`. حُفظت قاعدة SQLite مع WAL/SHM وDataStore وSharedPreferences وملفات WorkManager، ثم أعيد تثبيت RC عبر `install -r`. لم يُنفذ Uninstall أو Clear Data أو Downgrade أو Reboot.

- [النسخة الخاصة]({evidence}/before-tests-private.tar) و[بصمات الملفات]({evidence}/before-tests-files.json): 13 ملفًا؛ هذه أدلة خاصة محلية وليست أصولًا للنشر.
- [فحص نسخة القاعدة]({evidence}/baseline-db-summary.json): `integrity_check=ok`، version 8، 114 سورة و6236 آية و15 سجل أذكار و12 مثال مجتمع؛ لا مستخدمين محليين أو علامات قرآن في خط الأساس.
- بقي inode مجلد البيانات `2276359` بعد تحديث Release، وبقيت أمثلة المجتمع ظاهرة. هذا يثبت بقاء التخزين المرصود، **ولا يغني عن مقارنة جميع الصفوف والتفضيلات بعد الجولة**؛ لم تنفذ المقارنة النهائية بسبب انقطاع الهاتف.
- قاعدة المستخدم كانت v8 بالفعل. ترحيل 7→8 يجب تشغيله على قاعدة v7 اختبارية مستقلة؛ لم تُخفض قاعدة المستخدم.
- التطبيق الأصلي `com.example.mol` لم يستهدف بالتثبيت أو الاختبارات. آخر حالة مظهر اختبرناها في QA كانت DARK؛ استعادة مظهر خط الأساس تنتظر عودة الهاتف.

## نتائج Release المنفذة فعليًا على الهاتف

`Pass` هنا خاص بالفعل المذكور ودليله؛ ظهور الشاشة لا يعني نجاح جميع وظائفها أو تغطية المسار في المظهرين.

| الفحص | النتيجة | الدليل وحدود النتيجة |
|---|---|---|
| هوية الهاتف والتوقيع وتحديث RC | Pass | preflight وهوية APKين متطابقة، ثم Success في `install-history.jsonl` |
| فتح RC والوصول للرئيسية | Pass للفعل | `release-home.xml`، ولقطات البداية/الرئيسية. لا قبول لكل الوظائف |
| تبديل المظهر Light→Dark | Pass للفعل | `release-settings-dark-verified.png/xml`: المفتاح true وألوان Dark؛ بعد force-stop/start ظهر Splash وواجهات المجتمع Dark. لم تتم استعادة خط الأساس بعد الانقطاع |
| عرض الملف الشخصي الزائر والإعدادات | جزئي | تفريغات `current.xml` وسجل `native-actions.jsonl`؛ لا إنشاء/تعديل ملف حقيقي |
| المجتمع: القائمة والتفاصيل والتمرير وإفصاح DEMO | Pass للأفعال المذكورة | `release-community-dark.png` و`release-demo-contact-disabled-dark.png/xml`؛ ظهر الإفصاح أن التواصل غير متاح. لم يختبر CRUD النهائي على الهاتف |
| القبلة: الفتح وعرض الحالة | جزئي | `release-qibla-actual.png/xml`: اتجاه حلب قرابة 170°، والدقة غير متاحة. رصد خلل وصف مصدر الموقع؛ لا إثبات دقة زاوية أو معايرة |
| بث الحرم المكي | فشل تشغيل داخلي مرصود | `release-haram-settled-dark.png/xml` ورسالة فشل. إعادة المحاولة نفذت؛ السبب النهائي لم يثبت، فلا يصنف تلقائيًا عطل خدمة خارجية |
| بث المسجد النبوي | تشغيل بصري وتقدم مرصودان؛ الصوت معلق | `release-nabawi-settled-dark.png` ثم `release-nabawi-progress-dark.png`: المشهد والوقت تقدما من 17:42 إلى 17:43؛ لا قياس IFrame رقمي في هذه الجولة |
| ملء شاشة بث النبوي والخروج | Pass للفعل | `release-nabawi-fullscreen-dark.png`، ثم العودة للأفعال التالية؛ لا ادعاء اختبار كل حالات الخلفية والانقطاع |
| فتح المصدر الرسمي للنبوي | Pass للتوجيه | Intent فعلي إلى `https://www.youtube.com/channel/UCROKYPep-UuODNwyipe6JMw/live` ونافذة YouTube. ظهر إعلان في التطبيق الخارجي؛ ليس دليل تشغيل المصدر الخارجي كاملًا |
| فتح البحث ثم اختيار سورة الكهف | غير مكتمل | ظهرت واجهة البحث؛ انقطع ADB قبل توثيق نتيجة الاستعلام. لا Pass لنتائج البحث |
| الفيديوهات الـ86 | Not Tested | لا دفتر تشغيل لكل ID على الهاتف بعد؛ محاولة instrumentation لم تبدأ أي اختبار |
| Room 7→8 | Not Tested على الهاتف | اختبارات منفصلة مجهزة؛ لم يُشغّل ترحيل v7 الاختباري بعد |
| القرآن/الأذكار/الأحاديث/الأدعية/التسبيح وبقية المسارات | القبول الوظيفي النهائي Not Tested | لا تُنقل نتائج الوحدات أو جولات سابقة إلى Pass هاتف |
| التنبيهات قبل/بعد الإقلاع | Not Tested / موافقة reboot غير مطلوبة بعد | إذن الإشعارات كان false. لم نغيره قسرًا؛ يلزم تجهيز اختبار قبل طلب reboot |

الجرد ثابت: **58 نمط مسار، 59 حالة مسار/بيانات، 86 معرف فيديو فريد**. السجلان `route-acceptance.json` و`video-acceptance.json` يفصلان الملاحظات الجزئية عن القبول الوظيفي. لا يوجد تنفيذ شامل لـ118 حالة Light/Dark في هذه الجولة.

## الإصلاحات الداخلية المنجزة

1. **مصدر موقع القبلة**: `refreshLocation()` يعيد الموقع اليدوي دون GPS، لكن ViewModel كان يسمي كل نجاح «الموقع الحالي». اختبار جديد أعاد إنتاج الفشل، ثم أصبح المصدر مأخوذًا من حالة المستودع المتطابقة مع الموقع. `qibla-source-before.log` ثم `qibla-source-after.log`. لم يغير حساب الاتجاه.
2. **تباين فشل البث**: نص أبيض وزر فوق صورة غروب ساطعة؛ أضيف scrim أسود وزر خارجي أبيض الحدود. لقطة العيب الأصلية محفوظة؛ التحقق البصري للإصلاح الجديد على الهاتف معلق.
3. **انتقال Splash**: ظهرت لقطة Splash مع شريط الرئيسية أثناء fade. أصبح خروج Splash فوريًا عند تغيير الوجهة؛ لا إعادة تصميم. إعادة اختبار الهاتف معلقة.
4. **تشخيص الوسائط**: تسجيل مهلة بدء التشغيل مع هوية الفيديو/القناة، لتفريق انتهاء المهلة عن خطأ صادر من YouTube؛ ليس إصلاحًا مثبتًا لكل أعطال التشغيل.
5. **سلامة QA**: أزيل تغيير مدينة المستخدم من مصفوفتَي المسارات والتنقل. أضيف حفظ/استعادة مفاتيح التفضيلات حول اختبارات الشاشات، واختبارات نماذج المجتمع بقاعدة مستقلة. أضيف اعتماد DataStore لحزمة الاختبار وعولج فشل compilation بإعادة تجميع Kotlin كاملة. النسخ الحالية بُنيت؛ تنفيذ هذه الاختبارات الجديدة لم يتم.
6. المشغّلان الآمنان يتطلبان Candidate صريحًا ويطابقان بايتاته مع المثبت وهوية الهاتف. لا تثبيت أو منح أذونات أو تغيير إعدادات أو مسح بيانات تلقائي.

إصلاح دقة المغناطيسية السابق باقٍ: حدث التسارع لا يمحو تحذير البوصلة؛ اختبار الوحدة ناجح. لا تزال دقة الاتجاه الحقيقي ومعايرته بحاجة لمقارنة مستقلة ودوران يدوي.

## التحقق المحلي النهائي

| الفحص | النتيجة |
|---|---|
| وحدات app/feature/designsystem | **477 Pass، 0 failure/error/skipped**؛ XML و`unit-summary.json` محفوظان |
| Lint Debug | **0 أخطاء، {lint['Warning']} تحذيرًا**؛ `delivery-lint.xml` |
| Release + Debug + app instrumentation | BUILD SUCCESSFUL؛ `delivery-build.log` |
| feature migration instrumentation | بُنيت في الجولة؛ لم تشغل على الهاتف |
| التوقيع والحزمة وكون Release غير debuggable | تحقق من ملف التسليم الفعلي؛ `delivery-signature.txt` و`delivery-manifest.txt` |
| مطابقة مصادر البناء | بصمات المصادر قبل/بعد متطابقة؛ `delivery-source-before.json` و`delivery-source.json` |

امتد البناء عبر Standby للمضيف ليلة 16→17 أكتوبر؛ زمن الجدار ليس قياس أداء. لم يُعد تشغيل الهاتف. لا Commit أو Push أو Git Reset أو Backend.

## المحتوى والحقوق

أعيدت مراجعة صفحات [Tanzil وشروط النص](https://tanzil.net/docs/Text_License) و[مصدر الأحاديث وشروطه](https://hadeethenc.com/ar/) و[شروط مشغل YouTube](https://developers.google.com/youtube/terms/required-minimum-functionality). اختبارات سلامة النص المحلية نجحت ضمن الوحدات. هذا لا يجعل كل محتوى التطبيق معتمدًا دينيًا أو مرخصًا للنشر.

- القرآن: Tanzil Uthmani 1.1 مع النص الأصلي وإشعار المصدر؛ 114/6236؛ لا تغيير للنص الأصلي.
- الأحاديث: 3582 سجلًا من إصدار HadeethEnc ar v1.7.0؛ الإسناد وحقول المصدر محفوظة. تعذر جلب endpoint فحص أحدث إصدار في المراجعة الشبكية؛ لا ادعاء أنه الأحدث في 17 أكتوبر.
- الأدعية: 39 قراءة مختارة موثقة؛ لا ملء ديني مولد.
- التلاوات والتنزيل وHLS المباشر: **PENDING_LICENSE**؛ تبقى القيود مفروضة.
- التفسير الكامل: غير متاح دون مصدر/إصدار/ترخيص. معاني الأسماء وبعض الأذكار وخرائط التعلم: مراجعة مصدر/تحرير مطلوبة.
- أمثلة المجتمع: 12 مثال DEMO خياليًا؛ لم تضف بيانات اتصال حقيقية أو خدمة Backend.
- الأذان الموروث: تقرير سابق يسجل إذن تضمين من المالك؛ حقوق التوزيع العام الأوسع لم تثبت، فلا يوصف مرخصًا للنشر العام.

تفاصيل البنود في [سجل المصادر والتراخيص](../release/CONTENT_SOURCES_AND_LICENSES.md).

## استكمال القبول عند عودة الهاتف

1. إعادة preflight لهاتف `ac190123`، ثم حماية الحالة ومقارنة خط الأساس والتفضيلات؛ تثبيت آخر RC عبر `install -r` فقط بعد مطابقة التوقيع.
2. تأكيد إذن تثبيت حزم الاختبار من Xiaomi؛ لا يُعاد طلب الإذن قبل إفادة المستخدم بأنه جهزه. استخدام Debug عمدًا لفحوص الأدوات، مع فصل نتائجه عن Release.
3. `scripts/run_physical_acceptance.ps1 -CandidateApk <QA-debug.apk> -Suite Data/Routes/Interactions/Migration` كل مجموعة على حدة. لا تشغيل شامل للاختبارات القديمة التي تستبدل الملف/الموقع.
4. `scripts/run_physical_media_audit.ps1 -CandidateApk <QA-debug.apk>` لإنتاج سجل الـ86؛ الصورة وPLAYING وهوية الفيديو وتقدم الوقت منفصلة عن تأكيد الصوت. فحص Release مستقل بعدها.
5. إعادة اختبار الإصلاحات وجميع الرحلات على Release، ثم مقارنة بيانات المستخدم قبل/بعد. لا استعادة كاملة لنسخة قديمة فوق بيانات أحدث دون سبب ومراجعة.
6. القبلة: مرجع مستقل وموقع معلوم ودوران 0°/90°/180°/270° بعد المعايرة، فرق مستهدف ≤10°؛ خلاف ذلك PENDING_MANUAL_VERIFICATION.
7. إشعار مجدول قبل الإقلاع أولًا، ثم فقط عند جاهزية الحالة تطلب موافقة صريحة على reboot؛ لا إعادة تشغيل ضمن هذه الجولة.

لا تُعلن Feature-Complete Beta قبل إغلاق القبول الإلزامي، ولا تنسب الاختبارات المحجوبة بغياب الجهاز إلى نجاح أو إلى عطل خارجي غير مثبت.
'''
    gaps = f'''# إحسان — الفجوات النهائية للـBeta

تحديث: 2026-10-17، Asia/Damascus. **لا اعتماد Feature-Complete Beta حتى الآن**.

| المعرف | الحالة الحالية | ما أغلق وما بقي |
|---|---|---|
| PHY-01 | متصل ومتحقق سابقًا ثم Blocked | نجح التحقق والتحديث على Xiaomi؛ انقطع ADB أثناء الاختبار وقائمة 17 أكتوبر فارغة |
| SAFE-01 | تحسن محلي؛ إعادة التحقق معلقة | نسخة خاصة للبيانات محفوظة؛ اختبارات UI لا تستبدل المدينة، ونماذج المجتمع معزولة؛ مقارنة جميع البيانات النهائية لم تنفذ |
| QA-01 | جزئي، غير مقبول نهائيًا | فحص Release مباشر للمظهر والمجتمع والقبلة والبث؛ مصفوفة 58/59 في المظهرين لم تنفذ بالكامل |
| QA-02 | 86 فيديو Not Tested | لم يبدأ probe بسبب INSTALL_FAILED_USER_RESTRICTED؛ لا نسبة نجاح تشغيل مفترضة |
| LIVE-01 | فشل مرصود للمكي؛ نجاح بصري جزئي للنبوي | المكي أظهر fallback؛ السبب لم يثبت. النبوي عرض مشاهد متقدمة وملء الشاشة وفتح القناة الصحيحة؛ الصوت/المظهر الآخر/الانحدار النهائي معلق |
| DATA-01 | Not Tested على الهاتف | قاعدة المستخدم v8 سليمة في النسخة المحفوظة؛ ترحيل v7 الاختباري منفصل ولم يشغل |
| FIX-01 | إصلاح محلي ناجح، هاتف معلق | إصلاح دقة المستشعر السابق؛ لا إثبات زاوي فعلي |
| FIX-02 | إصلاح محلي ناجح، هاتف معلق | وصف الموقع اليدوي في القبلة؛ اختبار إعادة الإنتاج فشل قبل الإصلاح ونجح بعده |
| FIX-03 | بُني، هاتف معلق | تحسين تباين فشل البث وإزالة fade خروج Splash الذي أظهر شريط الرئيسية تحته |
| MAN-01 | PENDING_MANUAL_VERIFICATION | معايرة القبلة وموقع/مرجع مستقل ودوران الهاتف |
| MAN-02 | Not Tested | إشعارات قبل reboot، ثم طلب موافقة reboot فقط بعد التحضير؛ لم يعد تشغيل الهاتف |
| EXT-01 | PENDING_LICENSE | تسجيلات التلاوة والتنزيل؛ القيود باقية |
| EXT-02 | PENDING_LICENSE | HLS مباشر؛ YouTube الرسمي مسار مستقل وليس إذن إعادة بث |
| CONTENT-01 | مصدر/مراجعة/ترخيص معلق | تفسير كامل، شروح أسماء، بعض الأذكار وخرائط تعلم، وحقوق التوزيع العام للأذان الموروث |
| SCOPE-01 | خارج النطاق | لا Backend أو مصادقة/مزامنة/إشراف متعدد المستخدمين ضمن المهمة |

نجحت **477 وحدة** دون فشل أو تخطٍّ، وLint بصفر أخطاء و{lint['Warning']} تحذيرًا، وبناء Release/Debug/instrumentation النهائي. هذه نتائج محلية منفصلة عن قبول الهاتف.

ملف التسليم الحالي: [{name}](../release/apk/{name})؛ SHA-256 `{digest}`. الحزمة `com.example.mol.referenceqa` وشهادة Android Debug مطابقة للمثبت؛ ليست شهادة متجر. **الملف النهائي الجديد غير مثبت/مختبر على الهاتف بسبب انقطاعه**. آخر Release جرى فحصه مباشرة هو RC `514b7d10`؛ لا تنقل أدلته إلى الإصلاحات الأحدث.

بقيت البيانات محفوظة في نسخة محلية خاصة؛ لم يحدث Uninstall أو Clear Data أو Reboot أو Git Commit/Push/Reset أو Backend. بقي مظهر حزمة QA على DARK بعد تجربته؛ إرجاع المظهر السابق ومقارنة البيانات ينتظران الهاتف. لا تصنيف شامل «كل المشاكل الداخلية مغلقة» مع قبول معلق.

راجع [تقرير الهاتف الكامل](FINAL_PHYSICAL_DEVICE_QA.md) لسجل الأدلة والنتائج والقيود وخطوات الاستكمال، و[Provenance]({evidence}/delivery-provenance.json) لربط ملف التسليم بمصادره وفحوصه.
'''
    (ROOT/'docs/qa/FINAL_PHYSICAL_DEVICE_QA.md').write_text(physical,encoding='utf-8')
    (ROOT/'docs/qa/FINAL_BETA_GAP_REPORT.md').write_text(gaps,encoding='utf-8')
    routes = json.loads((ROOT/'.artifacts/physical-device-qa/offline/routes-not-tested.json').read_text(encoding='utf-8-sig'))
    observed = {'Splash','Home','Profile','Settings','Donations','IhsanDetails','Qibla','HaramLive','NabawiLive','GlobalSearch'}
    for route in routes:
        route.update(light='Not Tested',dark='Not Tested',functionalAcceptance='Not Tested',reason='Full physical acceptance incomplete; phone disconnected.')
        if route['name'] in observed:
            route.update(observation='PARTIAL_RELEASE_NATIVE_OBSERVATION',evidence='See FINAL_PHYSICAL_DEVICE_QA.md; not full functional acceptance')
    (OUT/'route-acceptance.json').write_text(json.dumps(routes,ensure_ascii=False,indent=2),encoding='utf-8')
    videos = json.loads((ROOT/'.artifacts/physical-device-qa/offline/videos-not-tested.json').read_text(encoding='utf-8-sig'))
    for video in videos: video.update(status='Not Tested',reason='Instrumentation installation rejected; phone later disconnected. No per-ID phone probe executed.')
    (OUT/'video-acceptance.json').write_text(json.dumps(videos,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps(provenance,indent=2))

if __name__ == '__main__':
    main()
