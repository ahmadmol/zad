# G0-03 — التقرير النهائي للجولة الميدانية — 2026-10-18

**القرار: غير مقبول / غير مغلق.** نجح التثبيت والتحقق من هوية النسخة وتشغيل QA، لكن توجد خمسة عيوب مثبتة، ومعايير قبول غير مكتملة. لا تُحتسب معاينة الشاشة قبولًا شاملًا.

## التثبيت وهوية النسخة

- الهاتف الحقيقي: Xiaomi 23129RAA4G، serial `ac190123`، Android API 35. لم يُستخدم محاكي.
- الحزمة الوحيدة المستهدفة: `com.example.mol.referenceqa`، البناء `BUILD-20261018-G002-01`.
- نفذ التحديث مرة واحدة بعد موافقة المستخدم الصريحة باستخدام `adb -s ac190123 install -r` على الملف المؤرشف المحدد؛ النتيجة `Success`.
- SHA-256 للمرشح وللـAPK المسحوب بعد التثبيت ومرة أخرى عند نهاية الجولة:
  `C71F894519AA60FCC7447685C818A56516654345B12FD57ED1FE988F63B14E9B`.
- مجموعة APK المثبتة: `base.apk` واحد، دون splits. لا يوجد اختلاف بصمة عند نهاية الجولة.
- شهادة الطرفين: SHA-256 `870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`، توقيع v2 متحقق، شهادة Android Debug. اسم الحزمة وversionCode=1 وminSdk=25/targetSdk=35 متوافقان.
- لم تُعد G0-01 أو G0-02، ولم يحدث بناء أو Unit/Lint أو Instrumentation أو تعديل لكود التطبيق أو Commit أو Push.
- لم تُشغّل أو تُحدّث أو تُعدّل نسخة الإنتاج `com.example.mol`.

## التشغيل والبيانات والإعدادات

- نجح فتح QA بعد التحديث، وجرى تشغيل بارد إضافي: `Status: ok`، `LaunchState: COLD`، زمن Activity المسجل 2498ms. لقطتا Splash بالمظهرين محفوظتان؛ XML التشغيل الأول التقط Home لاحقًا وليس في توقيت صورة Splash نفسها.
- بقي `ceDataInode=2276359` و`deDataInode=2225753` وتاريخ التثبيت الأول `2026-10-15 14:39:08` كما كانت. هذه مؤشرات حفظ التخزين، وليست مقارنة شاملة لسجلات قاعدة البيانات.
- ظهرت متابعة القراءة السابقة: البقرة، الآية 6؛ وعداد التسبيح 9/33، والإجمالي الأسبوعي والشهري 46، وإحصاءات 16 أكتوبر:36 و17 أكتوبر:10. لا توجد لقطة UI قبل التحديث لكل سجل، لذا لا يُدّعى إثبات تطابق كامل لكل البيانات.
- تحققت الواجهة النهائية من استعادة الوضع الداكن الأصلي، والاهتزاز «مفعل»، وخط الأذكار 24 نقطة، ومدينة حلب، وبقاء تذكير الأذكار غير مفعل كما ظهر في خط الأساس.
- إجراء اختبار زر الإحصائيات في التسبيح كشف تبديل الاهتزاز خطأً؛ عُكس بالضغط نفسه مرة ثانية ثم تحققت استعادة القيمة من الإعدادات العامة. لم تُصفّر العدادات ولم تُحذف بيانات.
- التنقل العادي إلى القرآن والدعاء أدى إلى تسجيلهما في نشاط اليوم داخل التطبيق، وظهر إنجاز اليوم 2/6. لم يُعدّل هذا التسجيل تلقائيًا أو يُمسح لإخفاء أثر الاختبار.
- لم تُنسخ ملفات التطبيق الخاصة أو تُقرأ قاعدة بياناته أو تُنشأ حسابات/عروض/طلبات أو تُحذف سجلات. لم يُستخدم Uninstall أو Clear Data أو تبديل التوقيع أو APK آخر.
- لم تظهر `FATAL EXCEPTION` أو `ANR in com.example.mol.referenceqa` في سجلي العملية المحفوظين؛ هذا لا يثبت خلو جميع المسارات من الأعطال.

## تغطية الحالات الـ59

شملت المحاسبة الحالات الـ59 كلها، إضافة إلى Splash وLegacy في صفين مستقلين. لكل حالة صف لكل مظهر، مع البصمة والهاتف والطابع الزمني وملفات الدليل.

| النطاق | النتيجة |
|---|---|
| 47 حالة متاحة | عُوين فتحها الأساسي بالمظهرين؛ لا قبول شامل لكل الحالات الفرعية |
| 12 حالة محجوبة | موثقة بسبب شرط ملف محلي، أو غياب fixture/نقطة دخول، أو حماية البيانات |
| صفوف الحالة×المظهر للحالات59 | 118: عدد86 PARTIAL، وعدد8 FAIL، وعدد24 BLOCKED |
| قبول كامل لكل V/U/A والحالات الفرعية | صفر؛ البوابة تبقى مفتوحة |

الحالات المحجوبة: `add_offer`، `add_request`، `request_help`، `missing_offer`، `edit_profile`، `onboarding`، `location`، `controlled_daily`، `sanhya_verses`، `sanhya_search` المنفصل، `nabi_recipe_detail`، `fahm_completion`.

- الإضافة والتحرير تتطلب فتح ملف محلي؛ لم أُدخل رقم هاتف أو أُنشئ ملفًا. السجل وإدارة العروض والطلبات فُحصت بحالة الزائر فقط.
- لم يُمسح الإعداد الأولي أو يُسحب إذن الموقع للوصول إلى Onboarding/LocationPermission.
- ميزة controlled daily لا تعرض نقطة دخول في Release، والعلم لم يتغير.
- بيانات سنحيا المتاحة بلا مراجع قرآنية؛ البحث المرصود يرشح القائمة نفسها. المسار المنفصل مسجل بلا caller ظاهر في المصادر الأساسية، ولم يُفتح عبر حقن مسارات.
- قائمة الوصفات فارغة، ولم تُضاف وصفة مصطنعة. لم يُضغط «أتممت هذا الدرس» لتصنيع حالة Completion.
- Legacy مسار إضافي منفصل غير مختبر، ولا توجد نقطة دخول آمنة مستخدمة له في هذه الجولة.
- Loading/Empty/Error وblank/whitespace/populated مسجلة مستقلًا؛ غياب fixture لا يُحوّلها إلى PASS. لا تغيير لخط النظام أو كثافته أو اللغة أو TalkBack: المقاس1080×2400، density440، fontScale1.0، النظامen-US والواجهة عربية، وخدمات accessibility غير مفعّلة.

## REC-01 إلى REC-06

| المعيار | النتيجة | الدليل والحدود |
|---|---|---|
| REC-01 | PARTIAL | صور وفيديو تمرير بالمظهرين؛ لا تداخل ظاهر في العينات. مسافة الإيماءة ليست قياس scroll offset دقيقًا 0/60/120، وfont1.3/2 غير مختبرين. فيديو الداكن الأول يغطي جزءًا من المسار. |
| REC-02 | PARTIAL | صيغة الحالي/الهدف وsemantics عند0/3 و0/1 بالمظهرين. لم تُصنع قيم0/33..100/100 أوtarget0، ولم ينفذ reset/increment أو TalkBack. |
| REC-03 | PARTIAL | إفصاح PENDING_LICENSE والتفضيل المحلي واضحان، والمشغل يعرض انتظار الترخيص. الضغط المرصود لم يبدأ تشغيلًا ظاهرًا. لا إثبات كامل لمسارات callbacks/work/network دون الاختبارات غير المنفذة. |
| REC-04 | FAIL | عناوين البطاقات والبحث تختلف عن التنظيف في التفاصيل؛ عينتا12 و29 موثقتان. لا تعديل للكود. |
| REC-05 | PARTIAL | لم تظهر عناوين وصف يتيمة في العينات المتاحة. empty/whitespace/present لم تُختبر كfixtures مستقلة؛ font2/TalkBack غير منفذين. |
| REC-06 | PARTIAL | عينات شارات/مدة فعلية وأخرى غير متاحة لا تظهر كpill فارغ. مصفوفةnull/empty/whitespace وcounts0/1/>1 لم تُصنع أو تُقبل كاملة. |

## العيوب الفعلية

| المعرف | الأولوية | العيب |
|---|---|---|
| G003-D01 | P2 | عدم اتساق تنظيف عناوين نبي الإحسان |
| G003-D02 | P2 | الرئيسية في شريط التسبيح لا تستجيب |
| G003-D03 | P2 | زر الإحصائيات في التسبيح مرتبط بتغيير الاهتزاز |
| G003-D04 | P2 | عرض التفاصيل في النشاط الأسبوعي لا ينفذ إجراء |
| G003-D05 | P3 | مقبض مفتاح الاهتزاز المفعّل غير مميز عن المسار |

التفاصيل التالية توثق الفعل والمتوقع والفعلي والأدلة، ولا تستبدل حالة السيناريوهات غير المختبرة.

### G003-D01 — عدم اتساق تنظيف عناوين نبي الإحسان

- إعادة الإنتاج: الخدمات ← نبي الإحسان ← بطاقة الحلقة 29 ثم التفاصيل؛ جميع الحلقات ← الحلقة 12 ثم التفاصيل؛ البحث عن 12.
- المتوقع: سياسة عرض واحدة تزيل تكرار رقم الحلقة والنقاط المتتابعة مع حفظ العنوان الخام.
- الفعلي: بطاقة 12 تعرض «حلقة 12/ مرض العصر..» مع metadata «الحلقة 12»؛ التفاصيل «مرض العصر. التفكير الزائد والتوتر والقلق ( over thinking ).». بطاقة 29 تحتفظ بالنقاط المتتابعة والتفاصيل تنظفها.
- المظهر: Dark;Light؛ الشاشة: UI-043;UI-045;UI-050؛ الأولوية: P2.
- الأدلة: [rec04-nabi-main](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/rec04-nabi-main.png>)، [rec04-rec05-nabi29](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/rec04-rec05-nabi29.png>)، [nabi12-card-light](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/nabi12-card-light.png>)، [nabi12-detail-light](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/nabi12-detail-light.png>)، [nabi-search-light](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/nabi-search-light.png>)؛ XML الموافق وسجل الأفعال محفوظان.

### G003-D02 — الرئيسية في شريط التسبيح لا تستجيب

- إعادة الإنتاج: الخدمات ← التسبيح ← اضغط الرئيسية في الشريط السفلي.
- المتوقع: فتح الصفحة الرئيسية.
- الفعلي: تبقى شاشة التسبيح والعداد 9 من 33؛ يلزم الرجوع العادي. لم تُختبر بقية عناصر الشريط كأفعال مستقلة.
- المظهر: Dark؛ الشاشة: UI-031؛ الأولوية: P2.
- الأدلة: [tasbih-dark](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/tasbih-dark.png>)، [current-after-statistics](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/current-after-statistics.png>)؛ XML الموافق وسجل الأفعال محفوظان.

### G003-D03 — زر الإحصائيات في التسبيح مرتبط بتغيير الاهتزاز

- إعادة الإنتاج: الخدمات ← التسبيح ← اضغط الأيقونة الموصوفة بالإحصائيات بجانب العداد.
- المتوقع: فتح الإحصائيات أو إجراء مطابق لاسم الزر دون تغيير إعداد آخر.
- الفعلي: لا يحدث انتقال. مصدر المرشح المطابق يرسل OnToggleVibration ويكتب إعداد الاهتزاز. أُعيد الضغط مرة ثانية لعكس التبديل، وتحققت الواجهة العامة من عودة القيمة مفعل.
- المظهر: Dark؛ الشاشة: UI-031;UI-011؛ الأولوية: P2.
- الأدلة: [tasbih-dark](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/tasbih-dark.png>)، [statistics-dark](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/statistics-dark.png>)، [tasbih-after-second-statistics-press](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/tasbih-after-second-statistics-press.png>)، [general-settings-dark](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/general-settings-dark.png>)، [settings-vibration-restored-dark](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/settings-vibration-restored-dark.png>)؛ XML الموافق وسجل الأفعال محفوظان.

### G003-D04 — عرض التفاصيل في النشاط الأسبوعي لا ينفذ إجراء

- إعادة الإنتاج: الخدمات ← جدول الأعمال ← نشاطي هذا الأسبوع ← عرض التفاصيل.
- المتوقع: فتح تفاصيل النشاط الأسبوعي أو عرض حالة واضحة.
- الفعلي: تبقى نشاطاتي اليومية دون تغيير. مصدر المرشح المطابق يحتوي clickable { } على النص.
- المظهر: Dark؛ الشاشة: UI-030؛ الأولوية: P2.
- الأدلة: [daily-dark](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/daily-dark.png>)، [statistics-actual-dark](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/statistics-actual-dark.png>)؛ XML الموافق وسجل الأفعال محفوظان.

### G003-D05 — مقبض مفتاح الاهتزاز المفعّل غير مميز عن المسار

- إعادة الإنتاج: افتح الإعدادات بالمظهر الفاتح بينما الاهتزاز مفعل.
- المتوقع: تمييز موضع مقبض المفتاح عن مساره.
- الفعلي: المفتاح يظهر ككتلة خضراء متصلة. عينات بكسل على خط المركز x=105/140/180/205,y=1374 كلها RGB(7,48,40). لا يُدعى قياس شامل لتوافق WCAG.
- المظهر: Light؛ الشاشة: UI-011؛ الأولوية: P3.
- الأدلة: [general-settings-light](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/general-settings-light.png>)، [general-settings-dark](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/general-settings-dark.png>)؛ XML الموافق وسجل الأفعال محفوظان.

## ملاحظات تشغيل وجودة الأدلة

- ظهر تعذر تشغيل البث داخل التطبيق للحرم المكي والنبوي مع إفصاح وخيار للمصدر الرسمي. هذا رصد تشغيل فعلي، وسبب الشبكة/المصدر/المشغل غير محسوم هنا؛ لا نجاح Playback أو حسم G0-04.
- بعض لقطات فهم القرآن تحوي إشعارًا عابرًا يغطي جزءًا من الرأس؛ لا قبول بصري كامل لهذا الجزء.
- التقطت لقطة واحدة واجهة النظام أثناء فحص البث، واستُبعدت من مصفوفة QA باسم `interrupted-system-ui-light`. أُغلقت اللوحة بمفتاح الرجوع دون التعامل مع خياراتها، وأعيد تصوير مساري البث مع التحقق من تركيز نافذة QA قبل الإدخال. لا يُسجل ذلك كعيب في التطبيق.
- أسماء `statistics-dark` و`statistics-actual-dark` تعني محاولتي الضغط، والصورتان بقيتا في التسبيح والنشاط اليومي على الترتيب؛ لا تُحتسبان فتحًا للإحصائيات. فتح الإحصائيات الفعلي موثق باسم `statistics-route-*`.
- ملفات REC-01 الداكنة الأولى كان في أسمائها light، ثم صُححت إلى dark مع خريطة `evidence-renames.json` والإبقاء على سجل الأفعال الأصلي. لا تُنقل أدلة تاريخية من APK آخر إلى هذه الجولة.
- الفحص المصدري المحدود الذي يسند العيوب طابق ملفات المرشح في خمسة مواضع فقط؛ لم يُكرر تدقيق G0-01/G0-02 ولا اختبار البناء.

## الملفات المسلّمة

- [مصفوفة الشاشات والحالات](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/screen-coverage.csv>)
- [الحالات الفرعية المستقلة](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/secondary-state-coverage.csv>)
- [معايير REC المستقلة](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/rec-acceptance.csv>)
- [العيوب وإعادة إنتاجها](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/defects.json>)
- [ملخص التغطية](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/coverage-summary.json>)
- [تثبيت وبصمة البداية](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/install-summary.json>)
- [بصمة نهاية الجولة](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/final-installed-sha256.txt>)
- [استعادة الإعدادات](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/final-settings-verification.json>)
- [سجل الأفعال وتوقيتها](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/g003-physical-qa/BUILD-20261018-G002-01/approved-install-20261018/actions.jsonl>)

**قرار النهاية:** التثبيت المعتمد ناجح، والبصمة النهائية مطابقة، والإعدادات المعاينة مستعادة. G0-03 غير مقبول بسبب العيوب المذكورة وحدود القبول المتبقية. انتهت الجولة دون بدء تطوير أو إصلاح أو Commit أو Push.
