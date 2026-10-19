# إصلاح الفيديو على Xiaomi — 2026-10-17

## نطاق العمل

هذا التقرير يكمل تدقيق الموارد المعتمد، ولا يعيد تدقيق الروابط أو يغير الصور والأصول. الجهاز الفعلي Xiaomi 23129RAA4G، الرقم ac190123، Android 15 / API 35، WebView 154.0.8037.57. جميع فحوص Android في هذه المهمة على هذا الجهاز؛ لا Emulator أو AVD. لا حذف بيانات أو Uninstall أو Commit أو Push أو Git Reset.

## السبب المثبت

المكونات `FahmVideoPlayer` و`QuranStoryVideoPlayer` و`NabiIhsanVideoPlayer` تستخدم `IhsanYouTubePlayer` نفسه. أنشأ هذا المشغل WebView دون تحديد LayoutParams، فكانت قيمتاه WRAP_CONTENT. كان Compose يقيس العرض الأصلي إلى 1080×608، لكن حساب الارتفاع النسبي داخل صفحة HTML انتهى إلى body وiframe بارتفاع صفر. لهذا استمر الصوت وحدث PLAYING والوقت، بينما لم تكن هناك مساحة لرسم محتوى الفيديو.

في اختبار A/B على الفيديو نفسه `Kx9Wj4vP69A`، دون إعادة تحميل الصفحة أو تغيير المصدر أو إعدادات الأمان:

| القياس | قبل | بعد MATCH_PARENT |
|---|---|---|
| WebView الأصلي | 1080×608 | 1080×608 |
| LayoutParams | ‎-2/‎-2 | ‎-1/‎-1 |
| viewport CSS | 392×221 | 392×221 |
| body CSS | 393×0 | 393×221 |
| iframe CSS | 392.73×0 | 392.73×221.09 |
| حالة المشغل | PLAYING | PLAYING |
| الوقت بالثواني | 2.864 | 6.13 |
| الصورة | سوداء | إطار البرنامج ظاهر |

الدليل الأصلي: [قبل الإصلاح](external-evidence/2026-10-17/video-rca-1792222196573/original.json)، [بعده في الجلسة نفسها](external-evidence/2026-10-17/video-rca-1792222196573/explicit-match-parent.json)، [الصورة بعده](external-evidence/2026-10-17/video-rca-1792222196573/explicit-match-parent.png). توجد نسخة قابلة لإعادة التجربة في `VideoRenderingRootCauseTest`. بعد الإصلاح يعيد الاختبار WRAP_CONTENT عمدًا ثم MATCH_PARENT؛ لا يغير إعدادات النظام أو بيانات التطبيق.

التسارع العتادي كان يعمل في الحالتين، layerType=0 وalpha=1 وVisibility=VISIBLE وisShown=true. الطبقات والحجم الأصلي وحدهما لم يفسرا الخلل. لم يثبت عطل MediaCodec أو Surface؛ عادت الصورة في جلسة الفيديو نفسها بمجرد إصلاح القياس. لم يلزم تغيير clip أو z-index أو codec أو Manifest. مشغل البث كان يستخدم MATCH_PARENT أصلًا، ولذلك لا ينطبق عليه هذا العطل.

## التغيير المحدود

1. تعيين FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT) عند إنشاء WebView المشترك.
2. تنفيذ `onShowCustomView` و`onHideCustomView` في `VideoWebChromeClient` لاستضافة العرض الذي يقدمه المتصفح بملء الشاشة **داخل نافذة النشاط نفسها**. الرجوع يغلقه ويستدعي callback، وتُعاد حالة أشرطة النظام السابقة ويُزال معالج الرجوع؛ المغادرة/التخلص من المشغل يغلقانه أيضًا. يستخدم المشغل المشترك والبث هذا العميل، مع الحفاظ على زر ملء الشاشة الموجود في شاشة البث.
3. بقي إيقاف التشغيل عند ON_PAUSE/ON_STOP وonResume والتنظيف كما هو، مع تنظيف عرض ملء الشاشة. لم تتغير هوية إحسان أو الصور أو backend أو إعدادات TLS وFile/Content access أو mixed content أو سياسات التضمين.

غياب معالجة custom view كان نقصًا مستقلًا في دعم ملء الشاشة، وليس سبب ارتفاع iframe الصفري. محاولة الفحص الأولى لم تثبت ملء الشاشة، وفشلت بعد الرجوع لانتهاء نشاط الاختبار؛ لا تُحسب نجاحًا. المحاولة التالية لم تجد وصف الزر في accessibility؛ أُعيد الاختبار بنقرة أصلية على زر YouTube المرئي، دون طلب fullscreen برمجي يتجاوز شرط تفاعل المستخدم.

## مراجعة المتطلبات الرسمية

- [متطلبات YouTube للتضمين](https://developers.google.com/youtube/terms/required-minimum-functionality): استخدام WebView النظام، وإرسال تعريف التطبيق في Referer عبر HTTPS. يستخدم التطبيق `loadDataWithBaseURL` مع applicationId المثبت، وorigin مطابقًا، و`strict-origin-when-cross-origin`. لم تُغيّر هذه الإعدادات لمعالجة الارتفاع. أبعاد iframe المثبتة بعد الإصلاح تتجاوز 200×200 CSS px، ولا تُضاف طبقة تغطي ضوابط YouTube.
- [عقد Android لملء الشاشة](https://developer.android.com/reference/android/webkit/WebChromeClient#onShowCustomView(android.view.View,%20android.webkit.WebChromeClient.CustomViewCallback)): يتطلب استضافة custom view ومعالجة إخفائه؛ WebChromeClient الافتراضي لا يدعم هذا الطلب.
- [أخطاء IFrame الرسمية](https://developers.google.com/youtube/iframe_api_reference#onError): 150 يعادل 101، أي رفض صاحب المحتوى التشغيل المضمن. 153 يتعلق بتعريف العميل/Referer؛ لا يصح تفسير 150 تلقائيًا على أنه خطأ origin.

## نتيجة ملء الشاشة والخلفية

الدليل النهائي: [phone-same-window-final](external-evidence/2026-10-17/phone-same-window-final/instrumentation.txt)، المجلد `physical-media-1792223668036`، اختبار `VideoLifecyclePhoneTest` ناجح على الهاتف، مع Dark في سياق Compose. بصمة APK التشخيص D623DDDD5A688BF75FBA7AAB0C4DEABF72452B89E0B75A5B21AA68DB7BA3AA34.

- fullscreen=true، iframe=392.73×872.73 CSS px. الصورة ظاهرة في `fullscreen-progress.png`؛ الزمن 6.7234 → 9.1130 ثانية، state=1، muted=false، volume=100. الدخول حافظ على التشغيل دون نقرة play ثانية؛ الاختبار يؤكد ذلك صراحة.
- الرجوع: fullscreen=false، قياس iframe يعود إلى 392.73×221.09، والتشغيل مستمر عند 11.0274 ثانية.
- الخلفية: document hidden، state=2، الوقت ثابت 11.201637 في عينتين متباعدتين ثانيتين.
- العودة: يبدأ متوقفًا عمدًا؛ بعد إجراء التشغيل الصريح عاد state=1 والوقت 13.9300 والصورة ظاهرة. لا نعد عدم التشغيل التلقائي في الخلفية عطلًا.

استُخدمت نافذة Dialog أولًا لاستضافة custom view؛ أظهرت مراجعة Release أنها تعرض الصورة لكنها توقف الفيديو عند دخول ملء الشاشة. رُفض هذا السلوك واستُبدلت الاستضافة بالنافذة نفسها، ثم اجتاز الاختبار شرط استمرار التشغيل. جلسات `phone-lifecycle-*` وAPK `0b9d6af1` أدلة وسيطة، وليست نسخة التسليم النهائية. كذلك بدأت جلسة `phone-same-window-lifecycle` قبل انتهاء البناء فحملت APK السابق FAA6F147؛ لا تُعتمد للتحقق من العميل الجديد. جلسة `phone-same-window-final` تحقق بصمتي APK التطبيق والاختبار بعد اكتمال البناء.

أكد المستخدم بعد إصلاح الارتفاع في العينة الأولى: «الصورة المتحركة والصوت يعملان»، ثم أثناء فحص Release الوسيطة: «نعم، الصورة والصوت يعملان». لا يُعمم هذان التأكيدان على سماع جميع الـ86 أو على بصمة APK لاحقة؛ يسجل السجل النهائي نطاق كل إثبات مستقلاً.

## تفسير 477 مقابل 415

التقرير السابق جمع الوحدات الثلاث: app=33، feature=415، designsystem=29، الإجمالي 477. تقرير الموارد شغّل feature فقط، لذلك ذكر 415. المقارنة الحالية مع سجل 2026-10-16 لم تجد أي suite مفقودة أو تغييرًا في عدد اختبارات أي suite أو اختبار skipped. كذلك **ملفات مصدر اختبارات الوحدة السابقة الـ104 موجودة وتطابق بصماتها السابقة بالكامل**، فلا يعتمد الاستنتاج على العد وحده. [مقارنة المصادر](external-evidence/2026-10-17/video-fix-unit-source-comparison.json)، [نتائج البناء والوحدات وLint النهائية](external-evidence/2026-10-17/video-fix-quality-final/quality-summary.json).

## نتائج الجولة النهائية وتسليم APK

### APK المثبت الأخير

[Ihsan-video-fixed-20261017-referenceqa-dba6df9f.apk](apk/Ihsan-video-fixed-20261017-referenceqa-dba6df9f.apk) — حزمة `com.example.mol.referenceqa`، Release مصغرة، versionCode=1، versionName=1.0. شهادة Android Debug الداخلية المطابقة للمثبت؛ **ليست شهادة نشر المتجر**. لم تُحدّث حزمة المستخدم الأخرى `com.example.mol`.

SHA-256: `dba6df9f1d969206edca1eb901f6b93341134d4960a76c1fda4c3cd88db3ca83`.

ثُبتت عبر `adb install -r` وطابقت نسخة APK المسحوبة من الهاتف بايتًا ببايت. بقي appId ومجلد بيانات المستخدم دون تغيير. حُفظ APK السابق، ونسخة احتياطية خاصة محلية من 766 ملفًا قبل تحديثات Release؛ لا توجد نسخة من البيانات الخاصة في مجلد التسليم. [إثبات التثبيت](external-evidence/2026-10-17/phone-release-final/installation.json)، [بصمات المصادر والتوقيع](external-evidence/2026-10-17/video-fix-quality-final/provenance.json).

التثبيت الأخير تم في الخلفية دون إطلاق النشاط، لأن الهاتف أصبح مستخدمًا في تطبيق آخر. **فحص واجهات Release ذات البصمة dba6df9f ما زال Not Tested**؛ لا تُنسب إليه ملاحظات مشاهدة نسخة 0b9d6af1 الوسيطة.

### مصفوفة الأدلة الفعلية

| النطاق | الصورة والتقدم | الصوت | ملء الشاشة والخلفية | حدود النتيجة |
|---|---|---|---|---|
| جولة `phone-video-fixed-86`، APK تشخيص FAA6F147 | **86/86** هوية وتقدم وإطار محتوى ظاهر؛ مراجعة 172 لقطة Light/Dark | تأكيد المالك لعينة؛ 85 عنصرًا Not Tested للسماع الفردي | عينة منفصلة، وبها Dialog وسيطة استُبدلت لاحقًا | 530.879 ثانية للجولة؛ ليست مشاهدة الحلقات كاملة، وليست قبولًا نهائيًا لـRelease |
| العميل النهائي، `phone-same-window-final`، APK تشخيص D623DDDD | الصورة والتقدم ظاهران في العينة | واجهة المشغل unmuted وvolume=100؛ السماع المستقل لهذه البصمة Not Tested | **Pass للعينة** في Dark: استمرار التشغيل عند دخول fullscreen، الرجوع، توقف الخلفية، الاستئناف | لا تعميم لاختبار fullscreen/background الفردي على الـ85 الأخرى |
| إعادة الجولة على D623DDDD، `phone-video-final-86` | 7 سجلات؛ تقدم 6؛ صور صالحة ومراجعة لـ5؛ البقية Not Tested | Not Tested | نتيجة العينة أعلاه فقط | توقفت بسبب انتقال الهاتف إلى تطبيق آخر؛ العطل ليس رفض تضمين للفيديو السابع |
| Release الوسيطة 0b9d6af1، واجهة «الفهم عن الله» | صورة متحركة ظاهرة؛ لقطتان مختلفتان، ثم fullscreen بصورة عند 0:33 | المالك أكد الصورة والصوت | ظهر توقف الدخول إلى Dialog، فأُصلح قبل APK الأخير | لا تُحسب قبولًا لبصمة APK الأخيرة |
| Release الأخيرة dba6df9f | **Not Tested** للتشغيل من واجهات الأقسام | **Not Tested** | **Not Tested** | تثبيت وتوقيع وبصمة مؤكدون؛ ينتظر توفر الهاتف للفحص الأمامي |

سجل الجولة الكاملة يربط كل فيديو بعنوانه وقناته وقسمه ورابطه، دون إعادة جلب بيانات المصدر: [CSV للـ86](external-evidence/2026-10-17/phone-video-fixed-86/physical-media-1792222736085/adjudicated-video-results.csv)، [ملخصها](external-evidence/2026-10-17/phone-video-fixed-86/physical-media-1792222736085/adjudication-summary.json). كلمات Blocked الخام فيها تعني انتظار المشاهدة/السماع؛ ليست 86 حالة رفض تضمين. التحكيم النهائي احتفظ بالحالات الخام وفصل أبعاد الإثبات.

[السجل المنفصل للمحاولة الأخيرة المتوقفة](external-evidence/2026-10-17/phone-video-final-86/physical-media-1792223712276/adjudicated-video-results.csv) يبدأ كل عنصر غير مختبر بحالة Not Tested. حُجبت لقطتا عنصر ظهرت فوقه إشعارات لا تخص الاختبار، ونُقلتا إلى حفظ خاص محلي؛ لذلك لم تُحسبا إثبات صورة مقبولًا في التسليم. لا تُعد حالة Fail الخام الناتجة عن فقد التركيز دليل تعطل رابط أو مشغل.

### البث وخطأ 150

في الجولة المكتملة، الحرم المكي أعاد 150 عند epoch `1792223257.092` ضمن نافذة اختبار الحرم `1792223255764–1792223258189` مللي ثانية. ظهرت حالة عدم التوفر والبدائل. لم يصدر 153 في هذا الاختبار، ولم يُغيّر Referer أو الأمان للتحايل. القناة: [بث الحرم على YouTube](https://www.youtube.com/channel/UCos52azQNBgW63_9uDJoPDA/live). دلالة 150 الرسمية هي منع التشغيل المضمن؛ لا يثبت الكود وحده تفاصيل قرار صاحب البث أو مدته.

النبوي أظهر صورة الساحة فعليًا، وهوية الفيديو `Rs7St51oDDc` وتقدم الزمن من 1894926.4755 إلى 1894929.5337. السماع البشري الخاص بهذا البث Not Tested. [السجل الخام للبثين](external-evidence/2026-10-17/phone-video-fixed-86/physical-media-1792222736085/live-results.jsonl)، [الصورة](external-evidence/2026-10-17/phone-video-fixed-86/physical-media-1792222736085/live_nabawi.png).

احتُفظ بزر الفتح في YouTube وبـ[بوابة الحرمين الرسمية](https://haramain.prh.gov.sa/ar/live). لا استخراج HLS أو التفاف على رفض التضمين. مسار HLS المعطّل بحقوق الاستخدام بقي معطّلًا ولم يُختبر. إعادة فحص البثين من واجهة APK الأخيرة، وتجربة إطلاق البديلين فعليًا منها، ما زالت Not Tested.

### Build وUnit Tests وLint

الأوامر النهائية (JAVA_HOME=JBR 21.0.3؛ `-PihsanPhysicalQa=true --max-workers=2 --console=plain`):

```text
:app:assembleDebug :app:assembleDebugAndroidTest
:app:assembleRelease testDebugUnitTest
:app:lintDebug :feature:lintDebug :designsystem:lintDebug
:app:lintRelease :feature:lintRelease :designsystem:lintRelease
```

البناء الأخير نجح في 9m24s؛ 264 مهمة، 61 منفذة و203 up-to-date. الوحدات **477 Pass، صفر failures/errors/skipped** في 105 suites. إعادة استخدام نتائج designsystem الصحيحة up-to-date ظاهرة في السجل ولم تُخفَ أو تُستثنَ. Lint Debug وRelease ناجحان للوحدات الثلاث دون Error/Fatal؛ تفاصيل التحذيرات، XML الوحدات وتقارير Lint محفوظة في [مجلد الجودة النهائي](external-evidence/2026-10-17/video-fix-quality-final/quality-summary.json). وجود التحذيرات السابقة لا يساوي فشل البناء ولا يلغيها هذا الإصلاح.

### المتبقي فعليًا

1. توفير الهاتف قرابة 9 دقائق دون مغادرة نشاط الاختبار لإكمال إعادة جولة الـ86 على البناء الأخير، ثم عينات تشغيل Release من الأقسام الثلاثة والبث وبدائله. أكد المالك لاحقًا: «أكمل البناء والتقرير؛ الهاتف غير متاح حاليًا». اكتمل البناء والتقرير، وأُوقفت أي فحوص أمامية إضافية؛ لم تُستعمل نافذة تطبيق آخر ولم تُغلق. يلزم استئناف الفحص عند إتاحة الهاتف.
2. سماع فردي موثق لكل فيديو، واختبار fullscreen/background للعناصر التي تحمل Not Tested، إذا كان المطلوب قبول هذه الأبعاد لكل عنصر بدل اعتماد عينة المشغل المشترك. لا تُعد اختبارات الملف أو PLAYING بديلًا عن هذا الإثبات.
3. إذا كان تشغيل الحرم **داخل التطبيق** شرطًا إلزاميًا: تمكين صاحب [القناة الرسمية](https://www.youtube.com/channel/UCos52azQNBgW63_9uDJoPDA/about) للتضمين أو تزويد مصدر رسمي مصرح به. البديل الرسمي موجود بالفعل؛ لا يلزم اختراع حل يتجاوز 150.

مشكلة ارتفاع iframe والشاشة السوداء مشخصة ومصلحة. قبول الإصدار الأخير الكامل يبقى **غير مكتمل** بالنطاق المحدد أعلاه، ولا يغلق هذا التقرير حقوق الموارد أو المراجعات الدينية المعلقة في سجل التدقيق السابق.
