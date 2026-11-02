# إحسان — حملة ما قبل Backend / Batch 02

2026-10-20 — Asia/Damascus. استكمال Batch 01؛ لا إعادة Sprint 01. نتائج المصدر والبناء منفصلة عن قبول الهاتف.

## التنفيذ الفعلي

| المعرف | التنفيذ | القبول |
|---|---|---|
| F5-04 | خطط نطاق آيات بسورة واحدة، تحقق من الآيات في QuranDao المعتمد، تأكيد الحفظ الفعلي، جدولة حتمية، نتائج مراجعة مستقلة، تعديل الموعد، أرشفة وتعديل النطاق بمعاملة تحفظ النسخة القديمة | المصدر/JVM جاهزان؛ Room على الهاتف معلق |
| F5-05 | مسار حفظ من قائمة القرآن، اختيار سورة وآيات، مستحقات مرتبة، إخفاء/كشف الآيات كاملة وبالترتيب للاختبار، نتيجة يدوية، فتح القارئ والتكرار الحاليين، عرض التقدم والأرشيف وكل النتائج | القبول المرئي/Compose على الهاتف معلق |
| F5-02 | أم القرى بواسطة java.time.chrono.HijrahDate والبنية الحالية لـdesugaring، Clock ومنطقة ديناميكية، تعديل −2..+2 افتراضي 0 في DataStore، مزود عرض واحد للرئيسية/الملف/الصلاة/السبحة/واجهات المجتمع والتقويم | JVM واستعادة ملف DataStore ناجحان؛ API25 والمنطقة الزمنية على الهاتف معلقان |
| F5-03 | تقويم شهري هجري مع تواريخ ميلادية، شهر سابق/تالٍ/اليوم، اختيار يوم، تفضيل التعديل، حالة مناسبات خالية وفق اعتماد المستخدم | قبول RTL/Light/Dark/الخط/Touch/TalkBack/الاستعادة معلق |
| F4-05 | توسيع calculator ومستودع الصلاة الحاليين: ملخص الشهر، أيام التسجيل حتى اليوم، اختيار اليوم وتعديله، أداء/غير مؤداة بتسجيل صريح/غير مسجلة، بطاقة رئيسية من المستودع نفسه | JVM ناجح؛ قبول تكافؤ الرئيسية والسجل على الهاتف معلق |
| G0-05 | ترحيل إضافي 9→10 ومخطط مولّد، حفظ مخطط 9 وما قبله، فحص SQLite اصطناعي واختبار Android لإعادة فتح Room | فحص سطح المكتب ناجح؛ اختبار Android مجمّع فقط |

## العقود وحماية البيانات

- SHA-256 لمخطط 9 مطابق تمامًا لملف Batch 01 المؤرشف (`C894F9BA678FD200D7B8053BC238C2399920FA2BE6B128B642171CB88EB8A876`).
- Room الفعلي قبل الدفعة 9، وبعدها **10**. الجداول الجديدة فقط `memorization_plans` و`memorization_reviews` مع FK وindex إلى الخطة. لا تعديل على الجداول السابقة، ولا destructive migration أو downgrade.
- تأكيد الحفظ الصريح يحدد أول مراجعة غدًا. نتيجة «تذكرت جيدًا» تتقدم إلى 3 ثم 7 ثم 14 ثم 30 يومًا (وتبقى 30). «بصعوبة» تعيد غدًا دون تقدم مرحلة؛ «أحتاج إلى إعادة» تعيد المرحلة صفر وغدًا. المتأخر يبدأ من يوم تسجيله، ومراجعة مسجلة واحدة لكل خطة في اليوم. التدريب قبل الموعد متاح دون تسجيل نتيجة. تعديل الموعد متاح من الغد حتى سنة دون تعديل النتائج السابقة.
- تعديل النطاق يؤرشف النسخة السابقة ويُنشئ نطاقًا غير مؤكد؛ لا يُنقل تأكيد الحفظ تلقائيًا إلى آيات مختلفة. الأرشيف يحتفظ بالنطاق وتأكيده ونتائج مراجعاته. مجموع آيات النطاقات موسوم بأنه قد يتداخل، ولا يدعي عدد آيات فريدة.
- لا يُستنتج حفظ أو أداء صلاة من الفتح أو التلاوة أو غياب التسجيل. لا قارئ قرآن أو محرك صلاة أو نظام إحصاء بديل. نص الاختبار يأتي من الصفوف المعتمدة نفسها؛ إخفاؤه تغيير عرض فقط.
- **D-09 APPROVED — المستخدم، Batch 02:** −2..+2 يوم، افتراضي 0 في DataStore، منتصف الليل المدني حسب منطقة الجهاز. التعديل خاص بعرض التاريخ الهجري ولا يدخل في الصلاة/القبلة/التنبيهات/سجلات العبادات. المناسبات فارغة حتى اعتماد قائمة ذات مصدر وإصدار ومراجع مختص. الإفصاح عن اختلاف الحساب عن الرؤية ظاهر. تسميات وحدود رمضان لم تُعتمد بهذا القرار.
- رأس الصلاة يعرض التاريخين من اليوم المدني للجهاز؛ منطقة مواقيت المدينة تستمر في حساب الصلاة فقط. تأكيد الحفظ يلتقط اليوم مرة واحدة لاشتقاق تاريخ التأكيد وأول مراجعة، حتى لو عبرت الساعة منتصف الليل أثناء العملية.
- تفضيلات اليوم والتسجيلات تبقى بمفاتيحها الحالية. تغيّر المنطقة الزمنية يغيّر عرض اليوم الحالي ويعيد التحويل من اليوم المدني؛ لا يعيد كتابة السجلات التاريخية. تبقى الأشهر المحفوظة للاستعراض قابلة للاستعادة.

## الواجهات والحالات

- `memorization`: من قائمة القرآن؛ النموذج والاختبار والأرشيف في المسار نفسه. المصحف والتكرار عبر route القارئ الموجود.
- `islamic_calendar`: من الرئيسية؛ تمييز اليوم/المحدد ووصف إتاحة كامل لكل يوم. عرض الأسبوع من السبت إلى الجمعة في اتجاه الواجهة. عرض الخلية ≥48dp؛ الخط الكبير والشاشات الضيقة تستخدم تمريرًا أفقيًا معلنًا.
- `prayer_journal`: توسعة المسار السابق بملخص الشهر وأيامه، مع إبقاء تحرير الحالات اليدوية السابق. الرئيسية تربط به مباشرة.
- المكونات تتبع Material/Ihsan Light/Dark وRTL الموروثين. النماذج والمحددات ونطاق الاختبار وحالة كشفه تستعمل rememberSaveable. SavedStateHandle لتاريخ الصلاة مستمر. القراءة/الحفظ يعالجان الخطأ دون حذف التاريخ، والحفظ يمنع الإرسال المكرر؛ فشل الحفظ لا يغلق المسودة. إعادة القراءة متاحة؛ تغير نطاق الاختبار يلغي قراءة الآيات السابقة.
- لا تُدّعى موافقة بصرية من تجميع المصدر. تبقى فحوص قص النصوص والتباين واللمس وTalkBack عند الخط 1/1.3/2 على Xiaomi مطلوبة.

## التحقق

- اختبارات مستهدفة نهائية: **33 PASS** بلا إخفاق/تخطٍّ، تشمل السياسة، حدود النطاق، المراجع الناقصة، تأكيد idempotent، تعديل/أرشفة/تأجيل، المستحق والمتأخر والتكرار اليومي، أخطاء VM والإرسال المكرر/القراءات القديمة، التقويم/المناطق/DST، الشهر الكبيس وحدود الصلاة، ومسارات الترحيل.
- **Full JVM: 533 PASS**: app=33، feature=470، designsystem=30؛ failures/errors/skipped=0. استدعاء شامل واحد؛ designsystem كان UP-TO-DATE لعدم تغير مصدره ثم أُعيد تشغيل مهمته وحدها لإثبات نتائج جديدة. اختبارات مقارنة كل حرف من القرآن مع Tanzil والـ6236 مرجعًا ضمن المجموعة الناجحة.
- بعد توحيد التاريخ الميلادي/الهجري برأس الصلاة وتصحيح snapshot منتصف الليل، **22 اختبارًا مستهدفًا PASS**، بينها حالة جديدة لعبور منتصف الليل. الإجمالي المجمّع **534 حالة فريدة ناجحة**؛ ليس ادعاء تشغيل Full Suite من 534 مرة واحدة. المجموعة الشاملة بقيت 533 في استدعائها الوحيد.
- DataStore حقيقي بملف مؤقت: افتراضي 0، كل حدود −2..+2، رفض ±3، حفظ تفضيل مستقل سابق، إغلاق scope ثم إعادة فتح الملف وإثبات استعادة التعديل. هذا اختبار JVM، لا دليل إعادة تشغيل عملية Android.
- SQLite: actual Kotlin SQL على fixtures معزولة للمسارات 2/3/5/6/7/8/9→10 و10→10؛ تطابق جداول/أعمدة/فهارس/FK، جميع الحقول السابقة، integrity_check وforeign_key_check، رفض orphan review، وحفظ خطة/نتيجة وإعادة فتح ملف مرتين. ليست قواعد مستخدم أو قواعد تاريخية حقيقية.
- Android MigrationTestHelper + Room reopen واختبارات Android للمشروع: compile only؛ لم تُشغّل على هاتف.
- [توثيق Android الرسمي لدعم desugaring](https://developer.android.com/studio/write/java8-support-table) يدعم اختيار HijrahDate دون إضافة dependency. قبول API25 الفعلي معلق.
- Android Lint النهائي: **0 errors, 118 warnings**؛ مطابق لعدد تحذيرات Batch 01. لا يُدّعى انعدام التحذيرات.
- Release QA النهائي: **BUILD SUCCESSFUL — 7m 30s**، مع R8/resource shrinking. جُمعت اختبارات Android لكل من app وfeature دون تشغيلها. البناء الأول ناجح أيضًا؛ أوقفتُ بناءين وسيطين أثناء المراجعة النهائية لإدخال تصحيحات العرض/منتصف الليل/إعادة المحاولة، ثم أعدت البناء النهائي على آخر مصدر. لا ترقية أدوات؛ استُخدم JDK 21 المثبت أصلًا بعد تعذر بدء Gradle بـJDK 25 الافتراضي.
- `git diff --check`: PASS. لا تعديل في Gradle/Kotlin/AGP أو ملفات toolchain. لا تغيير في أصول القرآن أو المخططات 5–9؛ مخطط 9 مطابق لنسخة Batch 01.

## APK

`<LOCAL_USER_HOME>/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/prebackend-batch02-20261020/IHSAN-PREBACKEND-BATCH02-20261020-release-qa.apk`

الحجم: **31,778,850 bytes**. النسخة المؤرشفة مطابقة حرفيًا لخرج Gradle النهائي.

SHA-256: `D9F2A7B3BD56B9C23E3F9EC4EC5F1A427C298C950EC62AC4779B8F3EDCEB742B`

الحزمة **`com.example.mol.referenceqa`**؛ versionCode=1، versionName=1.0، minSdk=25، targetSdk=35. apksigner: توقيع **v2 PASS**، شهادة **Android Debug** الحالية للاختبار الداخلي، SHA-256 للشهادة:

`870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`

هذه هوية QA القائمة نفسها وليست شهادة نشر عام. لم يثبّت APK على الهاتف.

## الملفات

قائمة Batch 02 التفصيلية (تشمل الملفات المشتركة مع Batch 01 دون نسب بقية تغييرات المستخدم إلى هذه الدفعة):

- `app/src/main/java/com/example/mol/di/DatabaseModule.kt`
- `app/src/main/java/com/example/mol/di/RepositoryModule.kt`
- `app/src/main/java/com/example/mol/di/ViewModelModule.kt`
- `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`
- `app/src/main/java/com/example/mol/navigation/Screen.kt`
- `feature/src/main/java/com/example/feature/calendar/IslamicCalendarScreen.kt`
- `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`
- `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt`
- `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`
- `feature/src/main/java/com/example/feature/core/util/HijriDateProvider.kt`
- `feature/src/main/java/com/example/feature/core/util/HijriDateFormatter.kt`
- `feature/src/main/java/com/example/feature/core/util/HijriDateText.kt`
- `feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt`
- `feature/src/main/java/com/example/feature/ehsan/EhsanScreen.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/AddEhsanScreen.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/RequestHelpScreen.kt`
- `feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt`
- `feature/src/main/java/com/example/feature/prayer/presentation/PrayerDetailsBottomSheet.kt`
- `feature/src/main/java/com/example/feature/prayer/presentation/PrayerJournalScreen.kt`
- `feature/src/main/java/com/example/feature/prayer/presentation/ManualPrayerTrackerViewModel.kt`
- `feature/src/main/java/com/example/feature/prayer/presentation/HomePrayerJournalSummary.kt`
- `feature/src/main/java/com/example/feature/profile/ProfileScreen.kt`
- `feature/src/main/java/com/example/feature/quran/data/local/dao/MemorizationDao.kt`
- `feature/src/main/java/com/example/feature/quran/data/local/entity/MemorizationEntities.kt`
- `feature/src/main/java/com/example/feature/quran/data/repository/MemorizationRepository.kt`
- `feature/src/main/java/com/example/feature/quran/domain/model/ReviewSchedule.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/MemorizationScreen.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/MemorizationViewModel.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/QuranListScreen.kt`
- `feature/src/main/java/com/example/feature/statistics/domain/PrayerTrackerStatsCalculator.kt`
- `feature/src/main/java/com/example/feature/tasbih/presentation/TasbihScreen.kt`
- `feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase/10.json`
- `feature/src/androidTest/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationTest.kt`
- `feature/src/test/java/com/example/feature/core/HijriDateProviderTest.kt`
- `feature/src/test/java/com/example/feature/core/HijriPreferencePersistenceTest.kt`
- `feature/src/test/java/com/example/feature/core/IslamicCalendarViewModelTest.kt`
- `feature/src/test/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationMatrixUnitTest.kt`
- `feature/src/test/java/com/example/feature/prayer/PrayerMonthStatsTest.kt`
- `feature/src/test/java/com/example/feature/quran/data/MemorizationRepositoryTest.kt`
- `feature/src/test/java/com/example/feature/quran/presentation/MemorizationViewModelTest.kt`
- `scripts/verify_batch02_migration.py`
- `docs/ROOM_MIGRATION_MATRIX.md`
- `docs/planning/IHSAN_RISKS_AND_DECISIONS.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN_PROGRESS.csv`
- `docs/qa/IHSAN_PREBACKEND_BATCH02_20261020.md`

## المتبقي ونقطة الاستئناف

- **UI COMPLETE: NO** و**LOCAL FUNCTIONALITY COMPLETE: NO** على مستوى التطبيق كله. Backend integration pending. المهام الست المحدثة تبقى PARTIAL في سجل الأربعين مهمة حتى قبولها المطلوب؛ مصدر هذه الدفعة ووحداتها جاهزان للفحص، ولا تتحول إلى Device PASS.
- **PHYSICAL QA PENDING**: Xiaomi، RTL، Light/Dark، خط 1/1.3/2، TalkBack، التنقل، الاستعادة، انتقال منتصف الليل/تغير المنطقة، حفظ/أرشفة/تعديل/مراجعة مستحقة، تكافؤ الصلاة بالرئيسية، MigrationTestHelper وحفظ بيانات التحديث على artifact نفسه. يحتاج تثبيت APK إذنًا منفصلًا.
- التالي F6-01/02 للصيام ورمضان وفق القرارات المتبقية؛ ثم F6-03 وبقية UX/AR/PS والمجتمع والتعلم ومهام القبول كما في CSV. لم يبدأ Batch 03 أثناء بناء وفحص تسليم Batch 02؛ لا يُفترض إغلاق تلك المهام.
- لا Emulator/AVD، لا تثبيت، لا وصول لقاعدة الهاتف، لا Backend/Firebase/خادم، لا ترقية Kotlin/Gradle/AGP أو dependency، لا Commit/Push/Reset/Clean.
- الأدلة محفوظة في `.artifacts/prebackend-batch02-20261020/`؛ استأنف من هذا التقرير و[سجل التقدم](../planning/IHSAN_PREBACKEND_CAMPAIGN_PROGRESS.csv) و[وثيقة الاستئناف](../planning/IHSAN_PREBACKEND_CAMPAIGN.md) دون تكرار Batch 01/02.
