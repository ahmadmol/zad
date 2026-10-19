# إحسان — تدقيق الوضع الحالي

2026-10-18 — Asia/Damascus. **تحليل وتخطيط فقط.**

## نطاق الإثبات وطريقة العمل

فحصنا working tree الحالي لا HEAD وحده. HEAD عند التحليل: `7dcf417038c90cb599326abd50afcf82321d6544`. المستودع يحتوي تغييرات سابقة كثيرة؛ لم نعدّلها. قرأنا Gradle/Manifest/DI/navigation وطبقات البيانات والمنطق والشاشات المشار إليها، وجردنا كل ملفات Kotlin الأساسية (app12، feature389، designsystem28 =429). استُخدم مسح مرجعي شامل للمصدر لاكتشاف feature callsites والاختبارات والمكونات، ثم قراءة مركزة للرموز الحساسة. المسح الآلي لا يعني مراجعة بشرية لكل سطر أو كل callback. مصفوفة الشاشات تسجل دليل كل route وcomposable ومخاطره.

لم نشغّل Gradle أو Unit/Instrumentation أو ADB أو APK أو Emulator/AVD في هذه المهمة؛ لم نفتح قواعد بيانات المستخدم أو ملفات النسخ الخاصة أو مفاتيح التوقيع. المقارنة الحرفية لـJSON/XML والجرد والقراءة تحليلات ساكنة، ليست PASS لاختبارات Android. عاينا بصريًا ثلاث صور سابقة فقط داخل `.artifacts/physical-device-qa/acceptance-20261017/`: 03_quran_light_page3،05_profile_dark،06_home_dark_scroll_0. وجود الصور وظهور التصميم لا يثبت توقيتها من current source أو نجاح callback أو نسبة contrast.

ثبتنا SHA-256 جماعيًا لـ777 ملفًا في src للوحدات الثلاث وschemas وgradle/scripts/.github وملفات build المحددة. الخوارزمية: map sorted path→SHA256 ثم JSON sort_keys ثم SHA256. baseline digest: `6ac93ec8e655a66e539752ae96ba2eab49456477eb1d33accfc4c14b38a0956a`. تحقق نهاية المهمة مذكور أسفل الملف. git status خارج docs/planning ثُبت بملخص digest، لا ننشر محتواه الطويل أو بياناته الخاصة.

## جرد النظام

| المجال | الملفات/المكونات الفعلية | الاستنتاج |
|---|---|---|
|تجميع التطبيق|settings.gradle.kts، app/build.gradle.kts، feature/build.gradle.kts، designsystem/build.gradle.kts|ثلاث وحدات؛ native وليس Flutter|
|DI/بدء التشغيل|app/.../di/AppModule.kt، DatabaseModule.kt، RepositoryModule.kt، ViewModelModule.kt؛ BarakahApp.kt يعرف IhsanApp|Koin؛ workers وasset seed؛ أسماء legacy لا تعني غياب السلوك|
|Navigation|app/.../navigation/Screen.kt وAppNavHost.kt وui/MainScreen.kt|58 موضع route registration ساكنًا؛57 فعليًا في Release و58 في Debug؛ restoreState/saveState وlaunchSingleTop للbottom tabs|
|UI system|designsystem/.../theme وcomponent؛ docs/Ihsan_Dark_Mode_Design_System.md|Dark Emerald/Charcoal مع semantic colors؛ warm Quran exception|
|Core persistence|feature/.../core/data/local/database/IhsanDatabase.kt ومigrations؛ UserPreferences وSettingsManager|Room v8 وDataStore وledger SharedPreferences؛ ليس تخزينًا واحدًا لكل شيء|
|وظائف العبادة|quran/prayer/qibla/azkar/tasbih/duas/hadith/asma|محلية في الأساس؛ مواقع ومصادر وmedia تخضع permissions/rights|
|الأقسام الإضافية|sanhya/nabiihsan/fahmanallah/learning/ihsanplus/profile/ehsan/dashboard/statistics|تعلم ومجتمع محلي؛ scaffolds ليست كلها production capabilities|
|الاختبارات|app/src/test وandroidTest؛ feature/src/test وandroidTest؛ designsystem/src/test وandroidTest|وحدات وعقود source وUI وmigration وmedia؛ لا كلها بنفس قوة الاختبار السلوكي|
|التقارير|docs/qa، docs/release، docs/ROOM_MIGRATION_MATRIX.md، backlog وقرارات integration|تعارض تاريخي؛ الكود والأدلة المحددة يتقدمان على الملخصات القديمة|

ملفات قواعد legacy QuranDatabase/AzkarDatabase/EhsanDatabase/HadithDatabase موجودة؛ DI الفعلي للمنتج يستخدم IhsanDatabase. لا نفترض قواعد مستخدم مستقلة بمجرد وجود فئاتها، ولا نحذفها دون callsite inventory. Experimental PrayerAssist/CharityTrust ليست routes إنتاج مسجلة، وIhsanPlusDaily controlled بتباين flags debug/release؛ يجب اختبار الـrelease state لا الصور التجريبية.

## قاموس الحالة

«موجود ويعمل» هنا مقيد بوظيفة يمكن إثباتها ساكنًا/بنتيجة تاريخية مسماة، وليس قبولًا شاملًا للهاتف الآن. بقية الفئات: موجود ويحتاج تحسينًا؛ موجود لكن يحتاج تحققًا ميدانيًا؛ غير مكتمل؛ غير موجود ضمن نطاق البحث؛ محجوب بترخيص أو قرار خارجي؛ غير قابل للجزم دون دليل. كل finding أدناه له مهمة أو قرار؛ لا تُعامل فرصة محتملة كعيب مؤكد.

## سجل الأدلة والنتائج

### E-01 — الوحدات والبناء

- **التصنيف:** موجود ويعمل.
- **الدليل:** `settings.gradle.kts` — `include` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** ثلاث وحدات app/feature/designsystem فقط؛ السلسلة app→feature/designsystem، feature→designsystem. لا وحدات لكل ميزة. minSdk25/compile-target35، Java11، AGP8.7.3/Kotlin2.1.0/Compose BOM2024.12.01/Room2.6.1/Koin3.5.6. هذا جرد إعدادات لا حكم توافق حديث ولا توصية ترقية.
- **المعالجة/التحقق:** G0-02، AR-05 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-02 — جرد routes

- **التصنيف:** موجود لكن يحتاج تحققًا ميدانيًا.
- **الدليل:** `app/src/main/java/com/example/mol/navigation/Screen.kt` — `Screen` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** 58 object Screen بما فيها ManageListings المكتوب inline؛ AppNavHost فيه 58 موضع composable ساكنًا؛ IhsanPlusDaily مشروط بـdailyEnabled وهو true في Debug وfalse في Release، لذا graph الفعلي57 في Release و58 في Debug. 59 زوج route/state في DarkFinalRouteMatrixTest، تستثني Splash وLegacy وتضيف variant لكل AddDonation/ManageListings/IhsanDetails. لا تخلط عددها بعدد ملفات Screen.kt.
- **المعالجة/التحقق:** G0-03 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-03 — قبول artifacts وتسوية البصمات (تمت التسوية في G0-01)

- **التصنيف:** معالجة وتسوية دقيقة بالأدلة المحفوظة فقط.
- **الدليل:** `docs/qa/FINAL_PHYSICAL_DEVICE_QA.md`؛ `docs/qa/FINAL_BETA_GAP_REPORT.md`؛ `docs/release/VIDEO_BLACK_SCREEN_RCA_2026-10-17.md`؛ `docs/release/external-evidence/2026-10-17/video-fix-quality-final/provenance.json`؛ `.artifacts/physical-device-qa/session-20261016/delivery-provenance.json`.
- **النتيجة والتسوية المثبتة في G0-01:**
  1. **بصمة 4BF1DD4FEB1E85DB6FB77C1133C7E0E18EBF661A91DCEF74CBBDF01126B67B3D (`app-release.apk`)**: مصنفة كـ **Unproven / Historical Partial**. التقرير القديم أعلن PASS بـ12 صورة، لكن ملف الـAPK غير موجود بالمستودع على القرص، وسلسلة البناء الكاملة من المصدر الحالي غير متوفرة.
  2. **بصمة C415EA4A88F3EA11AA7472A2387C62F0CD81D1543233438CAC93F6E6F1F72EA7 (`Ihsan-physical-beta-20261017-rc-c415ea4a.apk`)**: مصنفة كـ **Proven Build / Unproven Device Acceptance**. الـAPK موجود ومطابق للبصمة وموثق البناء فوق 603 ملفًا، لكن لم يُثبت أو يُختبر على الهاتف بسبب انقطاع ADB.
  3. **بصمة dba6df9f1d969206edca1eb901f6b93341134d4960a76c1fda4c3cd88db3ca83 (`Ihsan-video-fixed-20261017-referenceqa-dba6df9f.apk`)**: مصنفة كـ **Proven Build & Proven Installation / Unproven Device Foreground Acceptance**. الـAPK موجود ومطابق للبصمة وموثق التثبيت بـ`installedApkByteIdentical=true` عبر `adb install -r` دون مسح البيانات، ولكن القبول الميداني الأمامي للتشغيل من الأقسام والفيديو يسجل `releaseRuntimeAcceptance = Not Tested` لعدم توفر الهاتف أثناء الفحص المباشر.
- **المعالجة/التحقق:** إنجاز التسوية التوثيقية في G0-01؛ واستكمال الفحص الميداني الفعلي للـAPK المنتخب الجديد في G0-03..G0-06.

### E-04 — دليل وحدات تاريخي

- **التصنيف:** موجود ويعمل.
- **الدليل:** `docs/release/external-evidence/2026-10-17/video-fix-quality-final/quality-summary.json` — `summary/modules/suites` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** 477=app33+feature415+designsystem29؛ 105 suites، failures/errors/skipped=0 في السجل المؤرخ، لا تشغيل جديد. Lint warnings حسب module/variant: app debug118/release93؛ feature22 لكل variant +Information1؛ designsystem9 لكل variant. إذن «118 إجمالي المشروع» اختصار غير دقيق ولا تجمع warnings المكررة كعيوب فريدة.
- **المعالجة/التحقق:** G0-01، G0-02 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-05 — سلامة نص القرآن محليًا

- **التصنيف:** موجود ويعمل.
- **الدليل:** `feature/src/main/assets/quran_full.json` — `verses.text` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** مقارنة تحليلية أثناء هذه المهمة: 114 سورة و6236 زوج surah/ayah؛ مفاتيح ونصوص متطابقة حرفيًا مع licenses/tanzil-uthmani-1.1.xml المرفق، صفر اختلافات. هذه سلامة snapshot محلي لا مراجعة مصدر على الإنترنت ولا اختبار render.
- **المعالجة/التحقق:** G0-05، UX-03 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-06 — صفحات المصحف

- **التصنيف:** موجود لكن يحتاج تحققًا ميدانيًا.
- **الدليل:** `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt` — `loadMushafPages / MushafPageSurface` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** mushaf_page_starts.json فيه604 حدود وjuz starts30؛ loadMushafPages يحقق increasing offsets/6236. النص native Amiri مع زخرفة وصورة خلفية؛ ليس scan مطابق سطور مصحف مطبوع. fitting وzoom/pan حقيقيان؛ تفاعل الآية pointerInput وإحداثيات النص يحتاج فعل TalkBack بديل مثبت. فحص صورة سابقة أكد ورقًا دافئًا وزخرفة؛ لا يبرر redesign.
- **المعالجة/التحقق:** UX-03، PS-01 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-07 — استعادة القراءة وذرية العرض

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt` — `observeLastRead / getKhatmaProgress` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** saveLastRead يكتب الزوج في edit واحد في UserPreferences؛ observeLastRead يجمع Flowين منفصلين، فيحتاج اختبار الانبعاث الوسيط. getKhatmaProgress يستنتج نسبة من الموضع الحالي؛ هذه ليست إثبات إكمال صفحات، وقد تنقص بالرجوع أو ترتفع بالقفز.
- **المعالجة/التحقق:** AR-02، F4-01 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-08 — Bookmarks واستعلامات DAO

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt` — `observeAllBookmarks / toggleBookmark` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** لكل bookmark استعلام surah وآية: شكل N+1 مثبت من المصدر لا latency مقاس. toggle يقرأ first ثم يكتب خارج transaction؛ ضغط متزامن خطر يحتاج إعادة إنتاج. لا نضيف UNIQUE/FK قبل فحص بيانات legacy.
- **المعالجة/التحقق:** AR-03 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-09 — حالة بحث فاشل

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/quran/presentation/QuranViewModel.kt` — `search` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** catch يعيد رمي CancellationException بشكل صحيح، لكنه عند Exception أخرى يضبط isLoading=false وerrorMessage فقط ويبقي isSearching=true؛ GlobalSearchScreen يحجب النتائج طالما isSearching. ثغرة انتقال حالة من المصدر، لا claim crash ميداني.
- **المعالجة/التحقق:** AR-01 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-10 — بحث شامل واستعادة

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt` — `GlobalSearchScreen / LaunchedEffect(query)` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** البحث موجود مع فلاتر وsources و7 ViewModels مستقلة وcollectAsStateWithLifecycle. query/filter بـremember غير saveable، والبحث غير الفارغ فقط dispatch؛ results guards تمنع كثيرًا من stale hits. يلزم اختبار clear/recreate/error-per-source؛ لا إعادة بناء البحث من الصفر.
- **المعالجة/التحقق:** AR-01، F5-01 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-11 — تكرار normalization

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt` — `results / search` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** combine يفلتر العناوين والقصص عبر ArabicSearch.matches كل query؛ QuranRepository يستخدم index في الذاكرة وMutex وDispatchers.IO. إعادة تطبيع كل عنوان مرشح تحسين بالقياس؛ ليس بطئًا مثبتًا أو حاجة FTS مؤكدة.
- **المعالجة/التحقق:** PS-01، PS-02، F5-01 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-12 — حدود اليوم والتزامن

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/dashboard/data/DailyActivityRepositoryImpl.kt` — `observeToday / resetIfRequired / increment` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** تدفق date وتدفق counts منفصلان مع tick30ث وreset في transformLatest؛ resetIfRequired يقرأ ثم reset في عملية أخرى. UserPreferences.increment نفسه edit ذري ويضمن اليوم، ومع ذلك reset غير مشروط لاحق قد يمحو increment. هذا سيناريو race محتمل، لا فقد بيانات مرصود. clock repository اختياري مقابل LocalDate.now داخل preferences يحتاج توحيدًا.
- **المعالجة/التحقق:** AR-02 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-13 — أهداف الختمة

- **التصنيف:** غير مكتمل.
- **الدليل:** `feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt` — `QuranGoalCalculator / pagesToAyahs` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** domain foundation واختبار موجودان؛ لم نجد caller إنتاج خارج ملف النموذج أو persistence له في search مرجعي شامل. KHATMA_BY_DATE يوزع آيات متبقية؛ pages تقريب10.3. لا خطة7/30/60 موصولة أو إكمال يومي محفوظ. تطوير foundation الحالي لا ميزة مكررة.
- **المعالجة/التحقق:** F4-01، F4-02، F4-03 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-14 — سجل الصلاة

- **التصنيف:** غير مكتمل.
- **الدليل:** `feature/src/main/java/com/example/feature/prayer/presentation/ManualPrayerTrackerViewModel.kt` — `ManualPrayerTrackerViewModel / onPrayerTapped` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** PrayerLogEntity/DAO/Repository/Card/VM وPrayerTrackerStatsCalculator موجودة؛ IhsanDatabase v8 لا يدرج PrayerLogEntity ولا يعرض DAO، ولا binding فعلي لها في app DI/prayerDomainModule، ولا استدعاء Card في PrayerScreen. وجود اختبار fake repository لا يثبت التخزين الحقيقي. VM يثبت LocalDate عند init ويفتقر معالجة write errors/serialization.
- **المعالجة/التحقق:** AR-05، F4-04، F4-05 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-15 — Widgets

- **التصنيف:** غير مكتمل.
- **الدليل:** `feature/src/main/java/com/example/feature/prayer/widget/PrayerWidgetState.kt` — `PrayerWidgetStateFactory` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** حالة pure factory canonical وnextRefresh موجودة؛ لم يُعثر على AppWidgetProvider أو appwidget-provider resources أو APPWIDGET_UPDATE registration في الإنتاج. لا Android Home Screen Widget مكتمل؛ استكمال العقد الموجود هو النطاق.
- **المعالجة/التحقق:** F4-06 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-16 — Hijri formatter

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/core/util/HijriDateFormatter.kt` — `nowDateOnly / hijriFromJavaTime` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** HijrahDate.now وweekday Calendar يمثلان formatter موجودًا؛ API<26 يرجع «اليوم» رغم desugaring. لا calendar route أو local offset أو event model نشط. لا يجوز وصف التقويم كله غير موجود.
- **المعالجة/التحقق:** F5-02، F5-03 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-17 — حساب الصلاة الموحد

- **التصنيف:** موجود لكن يحتاج تحققًا ميدانيًا.
- **الدليل:** `feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt` — `DefaultPrayerTimesFacade` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** Koin يربط AdhanPrayerCalculator وPrayerClock/settings/location/facade؛ today/tomorrow وtimezone/date ticker وإعادة reconciliation. Home يستعمل نفس facade. وجود util PrayerCalculator وPrayerNotificationScheduler قديم لا يثبت أنه مسار الإنتاج؛ حدد callsites قبل تنظيفه. fixtures Aleppo/London DST واختبار مستقل موجودان.
- **المعالجة/التحقق:** G0-06، AR-05 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-18 — الإشعارات

- **التصنيف:** موجود لكن يحتاج تحققًا ميدانيًا.
- **الدليل:** `feature/src/main/java/com/example/feature/prayer/data/scheduler/AndroidPrayerAlarmGateway.kt` — `replaceSchedule / scheduleAlarm` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** Mutex وledger IDs محفوظ في SharedPreferences وimmutable PendingIntent وinexact fallback حالات قائمة. Receiver goAsync يعيد reconcile مع azkar. Manifest يذكر LOCKED_BOOT_COMPLETED لكن لا directBootAware؛ لا ادعاء delivery قبل unlock. Azkar periodic24h ليس ضمان ساعة مدنية دقيقة أو Doze delivery؛ CANCEL_AND_REENQUEUE يغير registration عند startup.
- **المعالجة/التحقق:** G0-06 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-19 — القبلة

- **التصنيف:** موجود لكن يحتاج تحققًا ميدانيًا.
- **الدليل:** `feature/src/main/java/com/example/feature/qibla/util/QiblaManager.kt` — `calculateQiblaDirection / getRotationFlow / onAccuracyChanged` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** bearing رياضي وdeclination وسلاسة دائرية ومعالجة accuracy للـmagnetometer فقط؛ unregister عند awaitClose. مستشعرات تحتاج calibration ومرجع حقيقي؛ لا نساوي صحة bearing بصحة دوران جهاز ولا نعلن leak من مجرد وجود singleton.
- **المعالجة/التحقق:** G0-06 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-20 — الفيديو 86

- **التصنيف:** موجود لكن يحتاج تحققًا ميدانيًا.
- **الدليل:** `feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt` — `EmbeddedYouTubeWebView / buildEmbedHtml` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** 28+29+29 EpisodeRecord IDs الفريدة بعد parsing records لا مطابقة كل string11. MATCH_PARENT صريح؛ timeout25ث يفرق READY عن PLAYING؛ pause عند ON_PAUSE/STOP وcleanup؛ VideoWebChromeClient للfullscreen. RCA يثبت سبب الارتفاع الصفري وإصلاحه قبل الخطة؛ لا نكرر الإصلاح بوصفه جديدًا.
- **المعالجة/التحقق:** G0-04، UX-05 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-21 — البث والتلاوات

- **التصنيف:** محجوب بترخيص أو قرار خارجي.
- **الدليل:** `feature/src/main/java/com/example/feature/live/LiveStreamSources.kt` — `مصادر البث` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** RCA يسجل150 للحرم وغياب153 في تلك الجولة؛ المصدر الرسمي وYouTube بديلان خارجيان، لا تشغيل داخلي ناجح. HLS والتلاوات/download محجوبة بحقوقها في المصدر/السجل؛ لا استخراج stream لتجاوز الحجب. Native audio service/download infrastructure موجودة لكن feature approval مستقل.
- **المعالجة/التحقق:** G0-04، R7-02 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-22 — المفضلة والتعلم المحلي

- **التصنيف:** موجود ويعمل.
- **الدليل:** `feature/src/main/java/com/example/feature/fahmanallah/data/repository/FahmRepositoryImpl.kt` — `restore / update / markCompleted` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** DataStore learning IDs لـSanhya/Nabi وFahm state_v1 موجودة مع favorites/watch-later/lastPlayedSeconds/manual completion. لا نكررها كميزة جديدة. stations لها emptyList links وبعض وصفات/المحتوى غير متاح؛ الشروح والربط تحتاج source review.
- **المعالجة/التحقق:** F6-03 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-23 — Room والترحيلات

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt` — `ALL` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** IhsanDatabase version8 بعشرة entities؛ سلسلة2→3→5→6→7→8 فعلية وexportSchema=true. schemas ومساعد اختبار موجودان. DatabaseModule لا destructive fallback. مصفوفة docs/ROOM_MIGRATION_MATRIX.md ما زالت5. 0 foreign keys في snapshot8؛ index واحد في ayahs، لا فرض FK على علاقات لم تُحسم.
- **المعالجة/التحقق:** G0-05، AR-03 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-24 — تهيئة الأصول وظهور الفشل

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt` — `loadIfNeeded / loadSurahs / loadAyahs` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** Mutex/editionLoaded وعد counts موجودة؛ بعض أخطاء فتح assets ترجع silently. applyCanonicalEdition transaction يحافظ IDs لكن يتجاهل tafsir لو النص متطابق؛ هذا ليس إذنًا لتفعيل تفسير غير مرخص. caller observe يعمل على سياقه حتى suspension، فلا نجزم parsing main دون trace؛ app seed على Default.
- **المعالجة/التحقق:** AR-04، PS-01 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-25 — DataStore وحدود الخطأ

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt` — `context.dataStore.data / learningIds` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** writes عبر edit ذرية ومفاتيح saved position/learning/activities قائمة؛ تدفقات هذه الفئة بلا catch/CorruptionHandler. عدم وجود سياسة استرداد واضحة لا يثبت corruption. لا تستبدل data افتراضيًا عند الخطأ ثم تكتب فوق بيانات المستخدم.
- **المعالجة/التحقق:** AR-04 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-26 — ملاحظات حذف البيانات

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `feature/src/main/java/com/example/feature/ehsan/domain/usecase/DeleteLocalProfileDataUseCase.kt` — `invoke` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** التعليقات تقول MINE_ONLY يحفظ listings لغياب ownership بينما التنفيذ يحذف USER matching local-profile:id؛ بنية ownership تغيرت. لا binding لهذه usecase مثبت ضمن DI الحالي. operations تشمل DB/files/prayer repository بلا transaction عابر؛ استكمال/استرداد تدريجي يحتاج سياسة لا وعد atomicity كامل.
- **المعالجة/التحقق:** AR-05، PS-04 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-27 — محلية المجتمع وحماية backup

- **التصنيف:** موجود ويعمل.
- **الدليل:** `app/src/main/AndroidManifest.xml` — `application / provider / service / receiver` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** allowBackup=false واستبعادات domains في backup_rules/data_extraction_rules؛ service وreceivers وstartup provider غير exported، MainActivity launcher exported. local profile لا OTP/cloud/payment؛ USER/LEGACY/DEMO وحدود محلية موثقة ومطبقة في البيانات المعاينة. صلاحية MainActivity ليست ثغرة وحدها.
- **المعالجة/التحقق:** PS-03، PS-04 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-28 — التوقيع والأذونات

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `app/build.gradle.kts` — `buildTypes.release.signingConfig` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** عند غياب release secrets fallback إلى debug؛ هذه حقيقة config لا إثبات تسرب secret. التطبيق يعلن SCHEDULE_EXACT_ALARM وUSE_EXACT_ALARM معًا ويحتاج قرار السياسة قبل متجر. local.properties/keystore لم تُقرأ أو تطبع؛ يلزم فحص آمن قبل النشر لا إدراج أسرار في تقرير.
- **المعالجة/التحقق:** PS-04، R7-02 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-29 — لغة الواجهة والاتجاه

- **التصنيف:** موجود ويحتاج تحسينًا.
- **الدليل:** `app/src/main/java/com/example/mol/ui/MainScreen.kt` — `CompositionLocalProvider` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** shell يفرض LayoutDirection.Rtl؛ GlobalSearch/Prayer كذلك ونصوص عربية literal موجودة بجانب resources. هذا يمنع اتباع LTR تلقائيًا؛ إذا كانت الإنجليزية ضمن النطاق تحتاج D-04. محتوى القرآن يبقى RTL مهما كانت لغة shell.
- **المعالجة/التحقق:** UX-02 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-30 — التباين واللمس والأداء

- **التصنيف:** غير قابل للجزم دون دليل.
- **الدليل:** `designsystem/src/main/java/com/example/designsystem/theme/IhsanSemanticColors.kt` — `IhsanTheme` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** tokens وcontrast tests وbottom-navigation geometry tests موجودة. لم نقس contrast composites أو TalkBack أو frame times/ANR/battery الآن؛ size40 لا يثبت touch target40 لأن Compose يوسع الحدود، وnull على icon زخرفي ليس مشكلة. CSV يفصل فحصًا مطلوبًا عن عيب مثبت.
- **المعالجة/التحقق:** UX-01، PS-01 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-31 — المصادر الدينية والأصول

- **التصنيف:** محجوب بترخيص أو قرار خارجي.
- **الدليل:** `docs/release/EXTERNAL_ASSET_REGISTER.csv` — `status / provenance` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** Tanzil/Amiri/HadeethEnc لها notices وإصدارات؛ شروح أسماء وبعض أذكار وخرائط تعلم/tafsir تحتاج مراجعة، أذان embedding تاريخي لا يثبت public distribution، رسومات عديدة PENDING_LICENSE. لا HTTP200 أو thumbnail أو generated description يغلق الحقوق.
- **المعالجة/التحقق:** R7-02 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

### E-32 — ميزات مستقبلية غير مسجلة

- **التصنيف:** غير موجود.
- **الدليل:** `app/src/main/java/com/example/mol/navigation/Screen.kt` — `جرد routes + source reference search` (أعد إيجاد السطر بالرمز بعد أي تغيير).
- **النتيجة وحدودها:** لم يُعثر على workflows إنتاجية للحفظ المتباعد أو notes CRUD أو fasting أو Ramadan hub أو حساب خادم/مدفوعات. repeat audio وjourney/goals models ليست بدائل لهذه workflows. التصنيف محصور بالمصادر الحالية، ويعاد البحث عند التنفيذ.
- **المعالجة/التحقق:** F5-04، F5-05، F5-06، F6-01، F6-02، R7-01 في [الخطة](IHSAN_MASTER_IMPROVEMENT_PLAN.md) و[الأوامر](IHSAN_GEMINI_EXECUTION_PROMPTS.md).

## مراجعة التقارير السابقة دون تكرار العمل

- FINAL_PHYSICAL_DEVICE_QA وFINAL_BETA_GAP_REPORT قرئا كاملين: لا إلغاء لصور PASS القديم، ولا تعميمه على فيديو86/ترحيل/إشعارات/الـAPK الجديد.
- VIDEO_BLACK_SCREEN_RCA_2026-10-17: renderer إصلاح موجود مثبت A/B؛ الفيديو PLAYING قد يكون iframe صفر الارتفاع سابقًا؛ round قديم كامل مقابل أحدث round توقف. قراءة adjudicated-video-results.csv في جولتي fixed/final تؤكد أعمدة audio/releasePlayback منفصلة وغالبها Not Tested في نطاق البصمة الأخيرة. لا تحويل unmuted أوvolume100 إلى سماع فعلي.
- SCREEN_INVENTORY وFEATURE_COMPLETENESS_MATRIX: الأعداد58/59 تأكدت، أما القول إن prayer log فعال فتخالفه Room/DI الحالية E-14؛ نحتفظ بالنماذج ونستكملها. الصفوف NOT_RUN التاريخية لا تصبح نجاحًا بالنسخ.
- ROOM_MIGRATION_MATRIX تقول5 بينما الكود8؛ CONTENT_SOURCES_AND_LICENSES يسجل قيودًا أحدث من IHSAN_PRODUCT_CAPABILITY_BOUNDARIES الذي يسمي audio/download بلا شرط. الخطة تعتمد حالة الحقوق المعاصرة.
- IHSAN_ENHANCEMENT_BACKLOG قديم: daily summary، last read، قبل الصلاة، reminders، calibration وfavorites أصبحت موجودة كليًا/جزئيًا؛ لا تكررها تحت أسماء جديدة. IHSANPLUS_CONTROLLED_INTEGRATION_PLAN وقرارات Counter/charity boundaries تمنع تضخيم scaffolds إلى خدمات فعلية.
- وثيقة الدارك تتضمن Flutter mapping قديمًا؛ المشروع Native ومنع التحويل صريح. نحتفظ بالهوية والـgeometry والمصحف ولا ننقل Dart.

## ما يحتاج تحققًا قبل الجزم

كل شاشة في CSV تحتاج مراجعة hierarchy/spacing/alignment/Arabic typography/surface/cards/icons/buttons/images/responsive/theme، ثم flows/states/back/keyboard/insets، ثم RTL/LTR/font/touch/contrast/TalkBack/focus/overflow. لا تغطية بصرية لكل هذه الأبعاد أثناء التخطيط؛ لا مشكلة مؤكدة من null contentDescription وحدها ولا color literal لأسود player أو Quran colors. القصور المثبت من المصدر محدد في E-09/E-10/E-13/E-14/E-15/E-16/E-28/E-29، ومخاطر السباق/الأداء تحتاج اختبارًا وتتبّعًا.

لا قياس startup/search/memory/jank/ANR/crashes/size/battery اليوم؛ PS-01 يحدد خط الأساس قبل تحديد أهداف التحسن. لا ادعاء تسريب أو leak دون دليل. Dependency versions جرد وليست مطالبة upgrade. لا استنتاج ترخيص من توفر الشبكة؛ كل مراجعة محتوى تحترم الحقول الأصلية والمصدر والإصدار.

## مراجع تقنية أولية راجعناها للتخطيط

- [Android/Material Compose accessibility defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults): فحص semantics وحدود التفاعل الفعلية؛ Material يقدم افتراضات يجب التحقق منها.
- [Android scheduling alarms](https://developer.android.com/develop/background-work/services/alarms): اختيار exact/inexact مع الإذن والقيود؛ registration ليس delivery.
- [Room migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions): ترحيلات واختبارات وحفظ بيانات؛ أمثلة أحدث قد تختلف عن Room2.6.1 الحالي ولا تبرر تغيير الاعتماديات.
- [Android app widgets](https://developer.android.com/develop/ui/views/appwidgets/overview): تكامل launcher مستقل عن نموذج الحالة.
- [YouTube IFrame API errors](https://developers.google.com/youtube/iframe_api_reference#onError): تفسير رفض التضمين مستقل عن HTTP/الفتح الخارجي.

هذه معايير تخطيط لا شهادات مطابقة أو مشورة قانونية. لم نتحقق من إصدارات المنافسين؛ قائمة المستخدم استُعملت كمدخل ميزات فقط وفق سماحه.


## E-33 — حدود التسجيل والتشخيص الفعلي

في app/src/main/java/com/example/mol/di/AppModule.kt ترتبط AppLogger وAppEventReporter بـNoOpAppLogger وNoOpAppEventReporter. وجود استدعاء report/error لا يثبت حفظ تشخيص أو رفعه. AR-04 يختبر مسار الخطأ بfake reporter، وPS-01 قد يستخدم تسجيلًا محليًا منقحًا في QA فقط؛ لا Analytics تلقائيًا.

العدد58 يشمل موضع IhsanPlusDaily المشروط؛ Release الفعلي57، وDebug58. وسائط nameId/zikrId/hadithId تضاف إلى الجذور داخل AppNavHost؛ CSV يحفظ النمطين المعلن والمسجل. هذا تصحيح لتعميم «58 شاشة إنتاج متاحة» في بعض التقارير، وليس إضافة routes جديدة.

## تحقق تسليم وثائق التخطيط

- الملفات المنشأة: الثمانية المطلوبة فقط داخل docs/planning؛ لا builders أو outputs إضافية.
- 40 Task ID فريدة، 40 كتلة أوامر تبدأ بالدور المطلوب، وتطابق مع سجل المهام الرئيسي؛ dependency DAG بلا دورات.
- مصفوفة الشاشات:61 صفًا،58 declared patterns،59 حالة القائمة الحالية +Splash/Legacy؛ registered patterns وvariant availability محفوظان. Release57 وDebug58 كما يثبت شرطdailyEnabled.
- مصفوفة الميزات:16 صفًا،بما فيها15 ميزة المستخدم وبند حقوق الصوت/التفسير المكمل. كلاCSV قابل للقراءة بUTF-8 BOM،والاقتباسات والأعمدة والمعرفات فُحصت برمجيًا.
- روابط الوثائق المحلية وملفات بداية الأوامر وsource_files فيCSV موجودة. لا مرجع Task ID مجهول.
- بصمات777 ملف مصدر/موارد/schema/config مراقب بقيت متطابقة: SHA-256 الجماعي 6ac93ec8e655a66e539752ae96ba2eab49456477eb1d33accfc4c14b38a0956a. ملخص git status خارج planning مطابق قبل/بعد: c0e781fb8519fcffd52df580f7a2d7b8bd90bcb09e5ee592c66040528bb45d5a؛ HEAD لم يتغير.
- هذه اختبارات اتساق للوثائق، وليست تشغيلًا لاختبارات التطبيق أوالهاتف. لا Build/Unit/Device PASS جديد. لم نثبت APK أو نشغّل ADB أو Emulator أو AVD أو ننفذ Git mutations.

## نتيجة مراجعة الاتساق النهائية للخطة — 2026-10-18

هذه مراجعة وثائق ومصدر وملفات أدلة محلية فقط؛ لا Build أو Unit Test أو ADB أو هاتف أو Emulator. اعتماد المستخدم مبدئي لهيكل المراحل الثماني والمهام الأربعين، والتنفيذ ينتظر اعتماد المراجعة النهائية.

### النواقص المثبتة التي صححت في التخطيط

1. كانت تغطية الإصلاحات الست عامة وغير قابلة للتتبع فرديًا. أضيفت REC-01..06 كمعايير قبول داخل G0-03 وUX-01/UX-03/UX-05، لا كمهام جديدة. كل معيار له رموز مصدر وحالة حالية وقبول وأدلة مستقلة في QA.
2. E-34: في NabiEpisodeCard تستخدم البطاقات substringAfter/replace، بينما NabiIhsanEpisodeDetailScreen تستعمل cleanEpisodeTitle من NabiIhsanTitleCleaner.kt. خمس شاشات تستعمل البطاقة: الرئيسية والحلقات والمفضلة ولاحقًا والبحث. هذه فجوة اتساق مصدر مثبتة، وليست نتيجة فشل بصري جديد. UiPolishFormattingAndEmptyStatesTest يختبر helper والبيانات وبعض resources؛ لا يثبت توصيل البطاقة أو رندر الأقسام والشارات.
3. حراس isNotBlank للشارات والأوصاف، ونص عداد الأذكار، وحظر التسجيلات، وحماية Status Bar موجودة في المصدر. لذلك لم نصف العيوب الستة جميعها بأنها ما زالت مفتوحة؛ نحتاج قبول الهاتف لكل معيار على artifact مطابق. لا ملف تسجيل خام محدد متاح من رسالة المراجعة، فلا نختلق مساره أو timestamps.
4. CSV ذو61صفًا كان يجمع الملاحظة والفرصة ودليل المصدر في حقول مختلطة. فصلنا confirmed_defect وimprovement_opportunity وphone_verification_status وvisual_acceptance_status، مع defect_priority مستقل وTask Priority map. ستة صفوف تحمل فجوة كود مثبتة (خمسة مستعملي البطاقة +GlobalSearch)، وكل61صفًا بلا قبول بصري جديد على البناء الحالي. هذا لا يعني أن بقية الصفوف اجتازت الهاتف.
5. سلسلة provenance السابقة كانت مختصرة. تطابقت SHA-256 المحلية للـAPK المحفوظ C415EA4A ولـdba6df9f مع تقاريرهما. installation.json التاريخي يسجل تطابق المثبت للأخيرة، دون foreground acceptance؛ لا تحقق هاتف حالي. sourceFiles الثمانية في provenance الأخير تطابق ملفاتها الحالية، لكن العينة لا تثبت كل build inputs. مطابقة603 ملفًا أو777 ملفًا تتطلب توحيد نطاق وخوارزمية الجرد، ولا تساوي بصمة APK.
6. R7-03 كانت تشير لغويًا إلى استقلال Beta، لكن قائمة اعتمادياتها كانت F6-04 وR7-02 بلا شرط. صححناها إلى G0-01..06 أساسًا، وF6-04 لإصدار الميزات فقط وR7-02 للنشر العام فقط. لا مستقبل شرط Beta-Current. قبول دفعة فحص يمكن أن يعتمد تقرير فشل موثق لبدء إصلاحه، دون منح قبول للمنتج أو دورة اعتماد خفية.
7. أزيلت تبعيات تسلسلية بلا حاجة وظيفية: Widget لا يحتاج prayer log؛ الحفظ لا يحتاج خطة ختمة؛ notes لا تحتاج الحفظ؛ مركز رمضان لا يحتاج Widget. تظل طبقات المصدر المشتركة والترحيلات والقرارات ضرورية، والتخزين ينفذ تدريجيًا على schema الحالية.
8. أضيف سجل مرجعي لكل40Task ID يحوي phase وTask Priority وmandatory/conditional dependencies، وطابقت معه أوامر Gemini ومراجع أولوية المهمات في المصفوفتين. أولوية feature ليست شدة عيب أو أولوية مهمة QA؛ أصبح الفرق صريحًا.

### حدود تصنيف العيوب

confirmed_defect يعرض عيبًا أو فجوة تثبت من المصدر مع نطاقها، ولا يساوي Visual FAIL أو Device FAIL. improvement_opportunity لا يعطي إذن تغيير جماعي. phone_verification_status وvisual_acceptance_status لا يصبحان Pass بمراجعة if أوhelper test أوصورة قديمة. أصل الإصلاحات الست تقرير المستخدم عن التسجيل، والربط الحالي يحافظ عليه دون اختلاق نتائج ميدانية.

### G0-01 — تثبيت خط الأساس وتسوية سجل القبول (تم التنفيذ والتسوية)

تمت تسوية سجل القبول وتثبيت خط الأساس المكتبي بالأدلة المتاحة فقط:

1. **تسوية بصمات APK المتباينة**:
   - `4BF1DD4F...` -> **Unproven / Historical Partial**: إثبات بصري تاريخي متوفر بـ12 صورة، بينما ملف الـAPK وسلسلة بنائه من المصدر الحالي غير متوفرين.
   - `C415EA4A...` -> **Proven Build / Unproven Device Acceptance**: ملف الـAPK وسجل البناء موجودان ومطابقان، لكن لم يُثبت أو يُختبر على الهاتف بسبب انقطاع ADB.
   - `dba6df9f...` -> **Proven Build & Proven Installation / Unproven Device Foreground Acceptance**: ملف الـAPK موجود، ومُثبت على الهاتف بـ`installedApkByteIdentical=true` دون مسح البيانات، ولكن القبول الميداني الأمامي للتشغيل من الأقسام والفيديو يسجل `Not Tested` لعدم توفر الهاتف أثناء الفحص المباشر.

2. **خط أساس مدخلات المصدر والنظام (HEAD & Digest)**:
   - Git HEAD: `7dcf417038c90cb599326abd50afcf82321d6544`.
   - إجمالي 777 ملفًا مراقبًا ببصمة جماعية `6ac93ec8e655a66e539752ae96ba2eab49456477eb1d33accfc4c14b38a0956a`.
   - بصمة حالة git status الخارجية: `c0e781fb8519fcffd52df580f7a2d7b8bd90bcb09e5ee592c66040528bb45d5a`.

3. **نتائج اختبارات الوحدات التاريخية**:
   - 477 اختبارًا ناجحًا (app: 33, feature: 415, designsystem: 29) عبر 105 suites، بصفر أخطاء/فشل/تخطٍ في سجل XML المؤرخ 17 أكتوبر 2026.

4. **ربط معايير الإصلاحات الستة REC-01..REC-06**:
   - ربطت مستقلًا بـ G0-03 وUX-01/UX-03/UX-05 دون إضافة Task IDs جديدة. مراجعة المصدر أثبتت وجود الحراس وفجوة كود واحدة في REC-04 (البطاقات لا تستخدم الـCleaner)، بينما القبول البصري الميداني معلق لـ G0-03 على البناء الجديد.

5. **سجل النواقص للمهام القادمة**:
   - سجلت الأدلة المطلوبة لكل من G0-02 (البناء المحلي والوحدات والـLint)، G0-03 (التنقل وحالات الشاشات بالهاتف)، G0-04 (الفيديو والبث بالهاتف)، G0-05 (ترحيل التخزين والبيانات بالهاتف)، G0-06 (المواقيت والقبلة والإشعارات بالهاتف).

النتيجة: **G0-01 Documentation Ready for Acceptance** (جاهزية التوثيق والتسوية للقبول المكتبي فقط، وليس قبول بناء أو تشغيل هاتف أو إطلاق).

### G0-02 — إعادة تأهيل البناء والاختبارات المحلية (تم الاعتماد والإغلاق الفعلي - PASS)

تم إجراء تدقيق واستعادة الإثباتات المباشرة للبناء المحلي واختبارات الوحدات والـ Lint لدفعة `BUILD-20261018-G002-01` دون إعادة بناء أو تشغيل، وجاءت النتائج كالتالي:

1. **إعادة حساب بصمات مدخلات المصدر (app_inputs_manifest.json Audit)**:
   - تم حساب SHA-256 وأحجام البايتات لجميع الـ **629 ملف مصدر/مورد** المدرجة داخل `app_inputs_manifest.json` على القرص الصلب.
   - **النتيجة**: تطابق 100% (629 ملفًا متطابقًا تمامًا، 0 ملف مفقود، 0 ملف متعدل).
   - **فجوة النطاق (Build Inputs Scope Gap)**: تبين عدم إدراج `gradle.properties` و `gradlew` و `gradlew.bat` في `app_inputs_manifest.json`. وبما أن هذه الملفات تؤثر على بيئة التنفيذ ولا يمكن إثباتها بأثر رجعي تاريخي، يُصنف اكتمال P1/P2 لهذه الدفعة تاريخيًا كـ **Unproven** في حدود هذه الإعدادات، دون أن يحجب سلامة الـ APK الناتج المستقل.

2. **التحقق المباشر من الـ APK المؤرشف ومطابقته (SDK Tool & Hash Verification)**:
   - استخدام أداة Android SDK (`aapt dump badging` الإصدار 35.0.0):
     - **Debug APK**: `com.example.mol.referenceqa` | `versionCode='1'` | `versionName='1.0'` | compileSdkVersion 35.
     - **Release APK**: `com.example.mol.referenceqa` | `versionCode='1'` | `versionName='1.0'` | compileSdkVersion 35.
   - **إعادة حساب SHA-256 للبصمات والأحجام والتطابق مع مخرجات البناء الأصلية**:
     - **Debug APK** (`Ihsan-BUILD-20261018-G002-01-debug-f4795258.apk`): SHA-256 `F4795258158D153868772FFC92E64EF4E83B4D63A9E80BB5DE8003EE9E9FAA0D` (62,339,647 bytes) — **مطابق بـ 100% Byte-Identical** لـ `app/build/outputs/apk/debug/app-debug.apk`.
     - **Release APK** (`Ihsan-BUILD-20261018-G002-01-release-c71f8945.apk`): SHA-256 `C71F894519AA60FCC7447685C818A56516654345B12FD57ED1FE988F63B14E9B` (31,660,830 bytes) — **مطابق بـ 100% Byte-Identical** لـ `app/build/outputs/apk/release/app-release.apk`.

3. **نتائج اختبارات الوحدات (Unit Tests)**:
   - تم التحقق من تقارير XML للاختبارات الحالية للوحدات الثلاث:
     - `:app`: 33 اختبارًا (8 suites) | 0 Failures / 0 Errors / 0 Skipped.
     - `:feature`: 419 اختبارًا (91 suites) | 0 Failures / 0 Errors / 0 Skipped.
     - `:designsystem`: 29 اختبارًا (7 suites) | 0 Failures / 0 Errors / 0 Skipped.
     - **الإجمالي الموحد**: **481 اختبارًا** عبر **106 suites** بـ **صفر أخطاء أو فشل أو تخطٍ**.

4. **تفكيك وحفظ أعداد Lint وآلية الحساب**:
   - قراءة وتقسيم تقارير Lint الرسمية لكل وحدة و Variant:
     - `:app` Debug: 118 تحذيرًا، 0 أخطاء | Release: 93 تحذيرًا، 0 أخطاء.
     - `:feature` Debug: 22 تحذيرًا + 1 معلومة (Info)، 0 أخطاء | Release: 22 تحذيرًا + 1 معلومة (Info)، 0 أخطاء.
     - `:designsystem` Debug: 9 تحذيرات، 0 أخطاء | Release: 9 تحذيرات، 0 أخطاء.
   - **المجموع المباشر**: **149 تحذيرًا للـ Debug** و **124 تحذيرًا للـ Release** عبر الوحدات الثلاث بـ **صفر أخطاء حرجة (0 Errors/Fatal)**.
   - **آلية التجميع وإزالة التكرار**: عند إزالة تكرار مواقع التحذيرات المزدوجة (Deduplication by File/Line/Col/ID) ينخفض العدد إلى 89 موقع تحذير فريد للـ Debug. ادعاء "120 تحذيرًا فريدًا" لا سند له بدون آلية تصفية خاصة.

5. **سجل البناء (Build Execution Log)**:
   - لا يوجد ملف `.log` مستثمر/محفوظ بشكل مستقل في مجلد الدفعة `.artifacts/g002-provenance/BUILD-20261018-G002-01/`. سجل التنفيذ المباشر متاح ضمن سياق المحادثة فقط.

النتيجة النهائية: **G0-02 Local Build & Quality Verification: APPROVED & READY FOR G0-03 PHYSICAL PHONE TESTING**.

### تحقق ختامي لمراجعة الاتساق

اجتازت فحوص الوثائق:40 مهمة و40 أمرًا؛ تطابق معرفات وأولويات واعتماديات MASTER/Prompts/السجل المرجعي؛ DAG دون دورة. التبعيات الإلزامية لـR7-03 في مسارBeta هيG0-01..06 فقط. مصفوفةUI بقيت61صفًا و58pattern و59حالة قائمة، معREC-01..06 جميعًا وأعمدة مستقلة للعيب والفرصة والهاتف والقبول البصري؛ لاVisual PASS جديد. المصفوفة تسجل ستةصفوف لفجوتي كود: خمسةمستعمليNabiEpisodeCard وصفGlobalSearch، لا ستةعيوب مستقلة ولا نتائج هاتف.

الثمانية ملفات التخطيط الموجودة فقط عُدّلت؛ لا ملف جديد. بصمات777ملفًا مراقبًا وحالةGit خارجdocs/planning وHEAD مطابقة قبل/بعد هذه المراجعة: source digest6ac93ec8e655a66e539752ae96ba2eab49456477eb1d33accfc4c14b38a0956a وstatus digestc0e781fb8519fcffd52df580f7a2d7b8bd90bcb09e5ee592c66040528bb45d5a. لا build أوtests أوADB أوهاتف أوEmulator/AVD أوGit mutation. هذه نتيجة اتساق وثائق فقط؛ G0-01 جاهزة بعد اعتماد المراجعة، ولم تُنفذ.
