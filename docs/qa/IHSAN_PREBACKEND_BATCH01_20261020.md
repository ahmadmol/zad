# إحسان — إكمال التطبيق قبل Backend / الدفعة 01

2026-10-20 — Asia/Damascus. الحملة مستمرة؛ لا يُعلن اكتمال التطبيق أو قبول الهاتف.

## التنفيذ الفعلي

| المعرف الأصلي | المنفذ في المصدر | حالة المهمة |
|---|---|---|
| F4-01 | تطوير QuranGoalCalculator بعقد صفحات فعلية 1–604؛ نطاق مخصص، بداية/نهاية شاملة، تأكيد يدوي، اقتراح يومي يتكيف مع الصفحات المتبقية | COMPLETED للعقد والحساب واختباراته |
| F4-02/03 | خطة محفوظة، تأكيد/إلغاء لكل صفحة، فتح الصفحة من الفهرس المعتمد، تمديد الموعد، إنشاء خطة جديدة يؤرشف القديمة ويحفظ تاريخها | PARTIAL؛ الوظائف موصولة والقبول الميداني معلق |
| F4-04/05 | وصل PrayerLogEntity/DAO/Repository/ViewModel الموجودة إلى Room/Koin؛ سجل يوم مختار، حالات صريحة، إلغاء التسجيل، إحصاء آخر سبعة أيام ومقارنته بالسابقة | PARTIAL؛ ملخص الشهر والرئيسية والقبول الميداني باقيان |
| F5-06 | ملاحظات شخصية للآيات: إنشاء/تعديل/حذف بتأكيد، بحث بمحرك ArabicSearch الحالي، ربط بالقارئ، حفظ المسودة، فصل واضح عن التفسير | PARTIAL؛ تنفيذ محلي جاهز للفحص على الهاتف |
| F5-07 | تحديد الملاحظات، معاينة الخصوصية، تصدير JSON عبر SAF، كتابة IO، معالجة الإلغاء والفشل؛ لا قاعدة بيانات أو نص قرآني أو ملف شخصي داخل التصدير | PARTIAL؛ فحص SAF الفعلي معلق |
| AR-02 | قراءة موضع القرآن من snapshot واحدة، وقراءة تاريخ/عدادات اليوم معًا؛ إزالة إعادة الضبط من observers؛ reset شرطي داخل edit واحدة | PARTIAL؛ قبول السيناريوهات الزمنية على الهاتف معلق |
| UX-01/03 | بطاقة محتوى مشتركة، ألوان أفعال/تحديد مقروءة بالمظهرين، أفعال إتاحة لكل آية في المصحف دون تغيير النص أو الإطار | PARTIAL؛ TalkBack والخط الكبير والقص تحتاج الهاتف |
| G0-05 | ترحيل إضافي 8→9، مخطط مولّد، فحص SQL فعلي على fixtures اصطناعية، واختبار Android جديد | PARTIAL؛ ليس قبول MigrationTestHelper على Xiaomi |

أُزيل استنتاج الإنجاز من فتح القرآن أو الدعاء أو تفاصيل الأسماء. موضع القراءة يبقى محفوظًا للاستئناف. السجلات القديمة والعدادات والتفضيلات لا تُصفّر بسبب هذا التغيير. حالات الصلاة المخزنة غير المعروفة تُحفظ في القاعدة ولا تُحتسب كصلاة مؤداة.

## الشاشات والمسارات

- `reading_journey?surah={surah}&ayah={ayah}&tab={tab}`: تبويبا خطة القراءة والملاحظات، من قائمة القرآن ومن خيارات الآية.
- `prayer_journal`: سجل الصلاة والتاريخ والإحصاء، من شاشة المواقيت.
- شاشة المصحف: فعل الملاحظة وأفعال إتاحة للآيات فقط؛ الورق الكريمي والإطار والنص والترقيم محفوظة.
- النماذج والتحديدات تستخدم rememberSaveable؛ تاريخ السجل محفوظ في SavedStateHandle. الأخطاء لا تمحو المسودة أو تستبدل البيانات بالصفر.

## البيانات والخصوصية

المخطط الفعلي قبل الدفعة **8**، والمخطط الجديد **9**. الجداول الجديدة فقط: `reading_plans`، `reading_page_completions`، `private_ayah_notes`، و`prayer_log` الموجود سابقًا كـfoundation. لا تغيير في schemas 5–8 ولا destructive fallback. لا بيانات جهاز أو قاعدة مستخدم حقيقية قُرئت أو عُدّلت.

إنشاء الخطة وأرشفة السابقة معاملة واحدة، وعمليات الخطط متسلسلة في المستودع المشترك. تأكيد الصفحة idempotent بالمفتاح planId/pageNumber. إنشاء الملاحظة يتحقق من مرجع الآية الموجود. لا مزامنة ولا Backend ولا مزود مصادقة جديد ولا مكتبات/ترقيات أدوات.

## التحقق وAPK

- المجموعة الشاملة: **508 اختبارات JVM ناجحة**: app=33، feature=445، designsystem=30؛ لا إخفاق أو تخطٍّ.
- بعد تعديل معالجة المنطقة الزمنية: **11 اختبارًا مستهدفًا ناجحًا** للمستودع والسجل والترحيل، بينها حالة جديدة. التغطية المجمعة **509 حالات فريدة ناجحة**؛ هذا ليس ادعاء تشغيل مجموعة شاملة من 509 دفعة واحدة.
- جُمعت اختبارات Android وMigrationTestHelper حتى آخر مصدر، دون تشغيلها على جهاز.
- فحص SQLite المعزول: المسارات 2/3/5/6/7/8→9 و9→9 ناجحة في تطابق جميع الجداول/الأعمدة/الفهارس وسلامة البيانات السابقة وintegrity_check. هذه fixtures بنيوية اصطناعية وليست قواعد تاريخية فعلية أو قبول هاتف.
- بناء التسليم الأخير: **BUILD SUCCESSFUL — 8m 19s**؛ minify/resource shrinking مفعّلان كما في المشروع.
- Android Lint: **0 errors, 118 warnings**. `git diff --check` ناجح. آخر تعديل لتحديد ملاحظات التصدير اجتاز التجميع وLint؛ قبوله المرئي معلق.
- لم تُرفع Kotlin/AGP/Gradle أو تُضف مكتبات.

APK: `.artifacts/prebackend-batch01-20261020/IHSAN-PREBACKEND-BATCH01-20261020-release-qa.apk`

المسار المطلق: `<LOCAL_USER_HOME>/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/prebackend-batch01-20261020/IHSAN-PREBACKEND-BATCH01-20261020-release-qa.apk`

SHA-256: `4605980D92019C21D35C8D14D4F7562EF4271EF405280B8A791DBDC9F403DA95`

الحجم: 31,713,314 bytes. الحزمة: `com.example.mol.referenceqa`؛ versionCode=1، versionName=1.0، minSdk=25، targetSdk=35. تحقّق apksigner من توقيع v2 بشهادة **Android Debug** الحالية للاختبار الداخلي؛ بصمة الشهادة:

`870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`

بصمة APK المؤرشف مطابقة لخرج Gradle النهائي. المجلد نفسه يتضمن سجلات البناء وLint، ونتائج JVM الكاملة/المستهدفة، وملخص الحالات المجمعة، وSQL الترحيلات الفعلي وفحصه، والمخطط 9، وفحص الهوية والتوقيع. لا APK تثبّت على الهاتف.


## المتبقي وحالة المنتج

- **UI COMPLETE: NO**؛ المصدر الجديد ليس قبولًا بصريًا لجميع الشاشات.
- **LOCAL FUNCTIONALITY COMPLETE: NO**؛ الحفظ والمراجعة F5-04/05، التقويم F5-02/03، الصيام ورمضان F6-01/02، وبقية UX/AR/PS والتعلم والمجتمع ما زالت متبقية حسب سجل الأربعين مهمة.
- **BACKEND INTEGRATION PENDING**؛ المصادقة، النشر العام، تسليم البلاغات، الإشراف والمزامنة والمعاملات الحقيقية لم تُنفذ ولا يُدّعى نجاحها.
- **PHYSICAL QA PENDING**؛ لم يُستخدم Emulator/AVD ولم يُثبّت APK ولم تُمس بيانات الهاتف. يلزم قبول Xiaomi لكل من المظهرين، الخط 1/1.3/2، RTL، الحواف، التنقل، الحالات، التصدير، الترحيل والاستعادة.
- المرحلة التالية: F5-04/05 للحفظ والمراجعة اليدوية، ثم التقويم والصيام ورمضان وبقية مهام الحملة. سجل [الأربعين مهمة](../planning/IHSAN_PREBACKEND_CAMPAIGN_PROGRESS.csv) و[نقطة الاستئناف](../planning/IHSAN_PREBACKEND_CAMPAIGN.md) يحددان المتبقي دون إعادة Sprint 01.
- لا Commit أو Push أو تغيير لهوية التطبيق أو لتراخيص المحتوى.

## ملفات هذه الدفعة

- `app/src/main/java/com/example/mol/di/DatabaseModule.kt`
- `app/src/main/java/com/example/mol/di/RepositoryModule.kt`
- `app/src/main/java/com/example/mol/di/ViewModelModule.kt`
- `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`
- `app/src/main/java/com/example/mol/navigation/Screen.kt`
- `designsystem/src/main/java/com/example/designsystem/component/IhsanContentCard.kt`
- `docs/ROOM_MIGRATION_MATRIX.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN_PROGRESS.csv`
- `docs/qa/IHSAN_PREBACKEND_BATCH01_20261020.md`
- `feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase/9.json`
- `feature/src/androidTest/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationTest.kt`
- `feature/src/main/java/com/example/feature/asma/presentation/AsmaViewModel.kt`
- `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`
- `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt`
- `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`
- `feature/src/main/java/com/example/feature/dashboard/data/DailyActivityRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/dashboard/domain/usecase/HomeDashboardUseCases.kt`
- `feature/src/main/java/com/example/feature/duas/presentation/DuaViewModel.kt`
- `feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt`
- `feature/src/main/java/com/example/feature/prayer/data/repository/ManualPrayerLogRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/prayer/presentation/ManualPrayerTrackerViewModel.kt`
- `feature/src/main/java/com/example/feature/prayer/presentation/PrayerJournalScreen.kt`
- `feature/src/main/java/com/example/feature/quran/data/local/dao/ReadingJourneyDao.kt`
- `feature/src/main/java/com/example/feature/quran/data/local/entity/ReadingJourneyEntities.kt`
- `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/quran/data/repository/ReadingJourneyRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/quran/domain/model/PrivateNotesExport.kt`
- `feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt`
- `feature/src/main/java/com/example/feature/quran/domain/model/ReadingPlan.kt`
- `feature/src/main/java/com/example/feature/quran/domain/repository/ReadingJourneyRepository.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/QuranListScreen.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/QuranViewModel.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/ReadingJourneyScreen.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/ReadingJourneyViewModel.kt`
- `feature/src/test/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationMatrixUnitTest.kt`
- `feature/src/test/java/com/example/feature/core/data/local/database/LocalJourneyMigrationSqlTest.kt`
- `feature/src/test/java/com/example/feature/prayer/ManualPrayerLogRepositoryTest.kt`
- `feature/src/test/java/com/example/feature/prayer/ManualPrayerTrackerViewModelTest.kt`
- `feature/src/test/java/com/example/feature/quran/data/ReadingJourneyRepositoryTest.kt`
- `feature/src/test/java/com/example/feature/quran/domain/model/ReadingPlanTest.kt`
- `scripts/verify_local_journey_migration.py`
