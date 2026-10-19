# إحسان — الخطة الهندسية الرئيسية

2026-10-18 — Asia/Damascus. **Planning Only؛ الوثائق لا تمنح إذن تنفيذ تلقائي.**

## القرار التنفيذي

المشروع Android Native Kotlin + Compose + Material 3 بثلاث وحدات فعلية: app → feature/designsystem وfeature → designsystem. الأساس المحلي واسع: قرآن عثماني وصفحات ومفضلات وبحث، مواقيت وقبلة، أذكار وسبحة ومحتوى وأقسام تعلم ومجتمع محلي. لا يصح وصف التطبيق بأنه مكتمل القبول الحالي؛ تقارير الهاتف والفجوات والفيديو تعود إلى artifacts مختلفة. آخر Release المسماة في RCA هي dba6df9f، وتشغيلها من واجهات الأقسام مسجل Not Tested. نتائج 477 وحدة تاريخية مؤيدة بملفات XML، ولم نعد تشغيلها أثناء التخطيط.

تثبت المعاينة 58 نمط route و58 موضع registration ساكنًا، أحدها gated (Debug58/Release57)؛ مصفوفة الاختبار الحالية 59 حالة، تستثني Splash وLegacy redirect وتكرر add/manage/detail لحالات مختلفة. CSV يضم 61 صفًا = 59 حالة الاختبار + هاتين الحالتين، وكل الصفوف مراجعة مصدرية وليست PASS هاتف جديدًا. الفيديو: 28 Sanhya +29 Nabi+29 Fahm =86 فريدًا. لا تُحسب story_uzair أو person_pray فيديو لمجرد أنها 11 حرفًا.

## خريطة الوثائق

- [الوضع الحالي والأدلة](IHSAN_AS_IS_AUDIT.md)
- [مصفوفة الشاشات](IHSAN_UI_UX_SCREEN_MATRIX.csv)
- [فجوات الميزات](IHSAN_FEATURE_GAP_MATRIX.csv)
- [العقود والترحيلات](IHSAN_ARCHITECTURE_AND_DATA_PLAN.md)
- [QA وبوابات الإصدار](IHSAN_QA_AND_RELEASE_GATES.md)
- [40 أمر Gemini مستقلًا](IHSAN_GEMINI_EXECUTION_PROMPTS.md)
- [القرارات والمخاطر](IHSAN_RISKS_AND_DECISIONS.md)

## قواعد التنفيذ

كل Task ID دفعة واحدة لها مراجعة وقبول قبل التالية؛ عند احتواء QA على عدة أقسام، يمكن استئناف القسم المتبقي داخل ID نفسه دون خلط artifacts. قائمة الملفات في سجل المهمة نقاط بداية فعلية لا أمرًا بتغييرها جميعًا. لا تعديل بلا دليل؛ مشاكل التباين والقص واللمس التي لا يمكن إثباتها من المصدر تظل فحوصًا مطلوبة، لا عيوبًا مؤكدة. هوية Dark Emerald/Charcoal ثابتة، المصحف ورقه الدافئ ونصه واتجاهه وزخرفته ثابتة؛ الصور المميزة لا تستبدل لمجرد التوحيد. Flutter snippet في وثيقة الدارك القديمة غير صالح للمشروع؛ نأخذ منها intent اللوني فقط.

لا Git Commit/Push/Reset/Rebase، لا حذف بيانات، لا Emulator/AVD، لا تثبيت أو فحص هاتف أثناء التخطيط. التنفيذ المستقبلي يقتصر على Xiaomi 23129RAA4G / ac190123؛ أي تعذر في الهاتف يبقي Device QA Pending. لا نقل بيانات المستخدم للخادم أو أدوات قياس خارجية. تراخيص التلاوات/HLS/الأصول وشروح المحتوى تُعالج كقرارات خارجية، لا كأخطاء برمجية ينبغي التحايل عليها.

## أهم عشر أولويات

| الترتيب | الدليل/الفرصة | سبب الأولوية | Task IDs |
|---|---|---|---|
|1|تعارض القبول وبصمات APK|منع قبول نسخة لم تُختبر|G0-01، G0-02|
|2|آخر Release فيديو دون قبول داخلي شامل|إصلاح renderer موجود لكنه يحتاج إثبات نفس artifact|G0-04|
|3|58/59 لا يساوي تغطية الأفعال وTalkBack|قبول كل شاشة وحالاتها دون redesign|G0-03، UX-01|
|4|مسارات v8 مقابل مستند v5 قديم|حماية IDs والمفضلات والبيانات|G0-05|
|5|receiver/ledger لا يثبت delivery أو sensor accuracy|وظائف الصلاة الحساسة وقيود Xiaomi|G0-06|
|6|توقيع release يعود debug وحقوق أصول معلقة|حاجز نشر عام؛ لا يحجب تلقائيًا Beta داخلي محدود|PS-04، R7-02|
|7|isSearching لا يطفأ في catch وquery بـremember|فشل بحث/استعادة يمكن تحديده من المصدر|AR-01|
|8|قراءة lastRead كFlowين وreset اليوم منفصل|سباق/اتساق يحتاج إثباتًا واختبارًا قبل الإصلاح|AR-02|
|9|N+1 للمفضلات وnormalization متكرر|قابلية القياس؛ لا دعوى بطء مقاس الآن|AR-03، PS-01، PS-02|
|10|foundations أهداف/صلاة/Widget غير موصولة|تطوير P1 بالتكامل بدل التكرار|F4-01 إلى F4-06|

## حدود الإلهام والميزات

استُخدمت قائمة المستخدم كمدخل تخطيطي مسموح: WeMuslim وQuran Majeed وأنا مسلم وMuslim Pro وTarteel وQuranly وPillars وQuran.com. لم ننسب ميزة حالية إلى إصدار محدد من منافس ولم ننفذ دراسة سوق جديدة؛ لا نسخ تصميم/كود/أصول. الأنماط المستفادة: أهداف قراءة قابلة للتعديل، سجل يدوي بلا لوم، widgets قليلة الاستهلاك، تدريب يدوي ومكتبة تعلم ومصادر واضحة.

P1 المخطط: تطوير الختمة والسجل والبحث، استكمال Widget foundation، وإضافة calendar تفاعلي فوق formatter موجود. P2 المخطط: حفظ ومراجعة يدويان، notes خاصة، صيام ومركز رمضان، وتطوير learning الحالي. P3 مؤجل: اختبار صوت/AI/الحج الموثق/مجتمع وتبرعات حقيقية؛ R7-01 وثائق فقط. لا ميزة مستقبلية شرط لإغلاق Beta الحالي.

## الترتيب والاعتماديات

مسار Beta الحالي: G0-01 → G0-02 → G0-03/04/05/06 → فقط إصلاح مانع مثبت واختباره → قرار قبول Beta مستقل في QA. لا ينتظر F4 أو F5 أو F6 أو R7-01. مسار التطوير التالي: Phase1 ثم Phase2 ثم Phase3 ثم Phase4 ثم Phase5 ثم Phase6؛ يمكن إنجاز قراءة/تحضير مستقلة لكن لا تُنفذ دفعة قبل اعتماد متطلباتها. R7-02 يبدأ جمع مستندات الحقوق مبكرًا، لكن النشر العام محجوب حتى إغلاقه. R7-03 بوابة إصدار الميزات، لا تعريف Beta القديم.

| المرحلة | عدد الدفعات | المعرفات |
|Gate 0 — تأهيل Beta الحالي|6|G0-01، G0-02، G0-03، G0-04، G0-05، G0-06|
|Phase 1 — تحسين UI/UX المثبت|5|UX-01، UX-02، UX-03، UX-04، UX-05|
|Phase 2 — اتساق المنطق والبيانات|5|AR-01، AR-02، AR-03، AR-04، AR-05|
|Phase 3 — الأداء والأمان والموثوقية|4|PS-01، PS-02، PS-03، PS-04|
|Phase 4 — الختمة والصلاة وWidgets|6|F4-01، F4-02، F4-03، F4-04، F4-05، F4-06|
|Phase 5 — البحث والتقويم والحفظ والملاحظات|7|F5-01، F5-02، F5-03، F5-04، F5-05، F5-06، F5-07|
|Phase 6 — الصيام ورمضان والتعلم|4|F6-01، F6-02، F6-03، F6-04|
|Phase 7 — القرارات المشروطة وتجهيز النشر|3|R7-01، R7-02، R7-03|

الإجمالي **40**: 6 +5 +5 +4 +6 +7 +4 +3. S=دفعة صغيرة محصورة؛ M=عقود/واجهات واختبارات محدودة؛ L=تخزين أو قبول هاتف أو تكامل عابر للأسطح. هذه أحجام نسبية، وليست ساعات أو مواعيد. الثقة العالية في الدليل البنيوي، متوسطة في حجم التغيير، ومنخفضة في مدة فحوص الهاتف وإغلاق الحقوق. لا تاريخ تسليم قبل G0-01 وتوفر الجهاز والقرارات.

## Gate 0 — تأهيل Beta الحالي

1. **Objective:** تحديد حالة الإصدار الحالي بالأدلة وإغلاق فحوصه الحرجة.
2. **In Scope:** G0-01 تثبيت خط الأساس وتسوية سجل القبول؛ G0-02 إعادة تأهيل البناء والاختبارات المحلية؛ G0-03 قبول التنقل والحالات على البناء المختار؛ G0-04 إغلاق قبول الفيديو والبث؛ G0-05 قبول الترحيل وحماية البيانات؛ G0-06 قبول الصلاة والقبلة ووصول الإشعارات.
3. **Out of Scope:** لا ميزات جديدة أو إعادة تصميم؛ فصل المتعطل تقنيًا عن المحجوب خارجيًا.
4. **Dependencies:** اعتماد المراجعة النهائية؛ التبعيات المشروطة حسب مسار الإصدار في السجل المرجعي؛ أرقام المراحل تنظيمية ولا تفرض انتظار ميزات مستقلة.
5. **Risks:** مصادر متعددة متعارضة والهاتف قد لا يكون متاحًا؛ قرار المالك في سجل D-xx مقدم على الافتراض.
6. **Exact Implementation Tasks:** سجلات G0-01، G0-02، G0-03، G0-04، G0-05، G0-06 أدناه وأوامر Gemini المقابلة؛ لا مهمة عامة «حسن التطبيق».
7. **Expected Files and Modules:** `docs/qa/FINAL_PHYSICAL_DEVICE_QA.md`؛ `docs/qa/FINAL_BETA_GAP_REPORT.md`؛ `docs/release/VIDEO_BLACK_SCREEN_RCA_2026-10-17.md`؛ `docs/release/external-evidence/2026-10-17/video-fix-quality-final/quality-summary.json`؛ `settings.gradle.kts`؛ `app/build.gradle.kts`؛ `feature/build.gradle.kts`؛ `designsystem/build.gradle.kts`؛ `gradle/libs.versions.toml`؛ `app/src/main/java/com/example/mol/navigation/Screen.kt`؛ `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`؛ `app/src/androidTest/java/com/example/mol/ui/DarkFinalRouteMatrixTest.kt`؛ `app/src/androidTest/java/com/example/mol/ui/PreserveDevicePreferences.kt`؛ `feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt`؛ `feature/src/main/java/com/example/feature/learning/presentation/components/VideoWebChromeClient.kt`؛ `feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt`؛ `app/src/androidTest/java/com/example/mol/ui/PhysicalDeviceMediaAuditTest.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt`؛ `feature/src/androidTest/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationTest.kt`؛ `docs/ROOM_MIGRATION_MATRIX.md`؛ `feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt`؛ `feature/src/main/java/com/example/feature/prayer/data/scheduler/AndroidPrayerAlarmGateway.kt`؛ `feature/src/main/java/com/example/feature/prayer/worker/PrayerSystemReconciliationReceiver.kt`؛ `feature/src/main/java/com/example/feature/qibla/util/QiblaManager.kt`؛ `feature/src/main/java/com/example/feature/reminders/AzkarReminderCoordinator.kt`. أضف ملفات جديدة فقط ضمن نطاق الميزة بعد إعادة الجرد.
8. **Database and Migration Impact:** لا تغيير schema في هذه المرحلة إلا إصلاح مانع مثبت بدفعة مستقلة؛ الاختبارات fixtures.
9. **UI/UX Changes:** لا تغييرات بصرية تلقائية؛ تثبيت defects من فحص شامل.
10. **Unit Tests:** G0-01: تحقق آليًا من وجود الأدلة ومطابقة البصمات؛ احسب suites ونتائج XML ولا تعتمد مجموع التقرير وحده؛ G0-02: assembleDebug وassembleRelease وtestDebugUnitTest وlintDebug وlintRelease للوحدات الثلاث؛ احسب XML حسب module؛ G0-03: Compose navigation + journeys + keyboard/insets + TalkBack/font 1/1.3/2؛ تحقق من الهوية قبل كل جلسة؛ G0-04: VideoRenderingRootCauseTest وVideoLifecyclePhoneTest ثم سجل per-ID لصورة/صوت/ثيمين/fullscreen/background/Release؛ G0-05: MigrationTestHelper مع schemas وfixture تاريخي موثق؛ fresh/current/upgrade/missing path؛ rollback rehearsal على fixture؛ G0-06: PrayerIndependentReferenceTest وHomePrayerParityTest وQiblaSensorAccuracyTest وAuditSystemDeliveryTest وAuditTimezoneChangeTest؛ دليل إشعار مرئي ومسموع؛ استخدم pure/fake للمنطق المعزول فقط.
11. **Integration Tests:** DI resolution، repository→DAO/DataStore، navigation callbacks، فشل I/O والمعاملات وفق المهام؛ كل مخرجات مسجلة مع المصدر.
12. **Physical Device Tests:** Xiaomi ac190123 فقط؛ سيناريوهات المهام أعلاه وQA؛ مرحلة وثائق P3 لا تستدعي الجهاز. حفظ الإعدادات قبل/بعد وعدم المساس بالبيانات.
13. **Acceptance Criteria:** جميع شروط المهام أدناه + عدم انحدار G0 للأجزاء المتأثرة؛ لا PASS بلا دليل ولا تحويل Blocked إلى Pass.
14. **Rollback and Recovery Strategy:** استعادة تفضيلات الجلسة؛ لا downgrade أو مسح بيانات؛ نفس شهادة الحزمة؛ أي تغيير تخزين يجرب على fixture قبل الإنتاج.
15. **Definition of Done:** مراجعة الفرق، اختبارات مناسبة ناجحة، CSV/QA محدثان عند تغير route/capability، قرارات معتمدة، بيانات محفوظة، ثم قبول المالك للدفعة. Pending هاتف يمنع ادعاء Device QA Passed.
16. **Estimated Effort and Confidence:** G0-01=S، G0-02=M، G0-03=L، G0-04=L، G0-05=L، G0-06=L. ثقة scope متوسطة؛ مصادر متعددة متعارضة والهاتف قد لا يكون متاحًا مصدر عدم اليقين؛ لا تحويل تلقائي إلى مدة.

## Phase 1 — تحسين UI/UX المثبت

1. **Objective:** تعديلات موضعية قائمة على إخفاق قابل للإعادة.
2. **In Scope:** UX-01 قياس المكونات المشتركة وإتاحة الوصول؛ UX-02 اتجاه اللغة والنصوص القابلة للترجمة؛ UX-03 تدقيق أسطح العبادة واستعادة الوصول؛ UX-04 نماذج المجتمع والملف وحالات الاستعادة؛ UX-05 التعلم والبث والحالات المشتركة.
3. **Out of Scope:** لا إعادة تصميم جماعية أو تغيير صفحة المصحف أو استبدال السماء والمآذن والكعبة.
4. **Dependencies:** G0-03، G0-04؛ التبعيات المشروطة حسب مسار الإصدار في السجل المرجعي؛ أرقام المراحل تنظيمية ولا تفرض انتظار ميزات مستقلة.
5. **Risks:** تأثير المكونات المشتركة وغياب دليل بصري لكل حالة؛ قرار المالك في سجل D-xx مقدم على الافتراض.
6. **Exact Implementation Tasks:** سجلات UX-01، UX-02، UX-03، UX-04، UX-05 أدناه وأوامر Gemini المقابلة؛ لا مهمة عامة «حسن التطبيق».
7. **Expected Files and Modules:** `designsystem/src/main/java/com/example/designsystem/component/IhsanButton.kt`؛ `designsystem/src/main/java/com/example/designsystem/component/IhsanBottomNavigation.kt`؛ `designsystem/src/main/java/com/example/designsystem/theme/IhsanSemanticColors.kt`؛ `designsystem/src/main/java/com/example/designsystem/theme/Typography.kt`؛ `app/src/main/java/com/example/mol/ui/MainScreen.kt`؛ `feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt`؛ `feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt`؛ `feature/src/main/res/values/strings.xml`؛ `feature/src/main/res/values-ar/strings.xml`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`؛ `feature/src/main/java/com/example/feature/azkar/AzkarScreen.kt`؛ `feature/src/main/java/com/example/feature/tasbih/presentation/TasbihScreen.kt`؛ `feature/src/main/java/com/example/feature/duas/DuaDetailScreen.kt`؛ `feature/src/main/java/com/example/feature/hadith/presentation/HadithScreen.kt`؛ `feature/src/main/java/com/example/feature/asma/presentation/AsmaScreen.kt`؛ `feature/src/main/java/com/example/feature/ehsan/presentation/AddEhsanScreen.kt`؛ `feature/src/main/java/com/example/feature/ehsan/presentation/RequestHelpScreen.kt`؛ `feature/src/main/java/com/example/feature/profile/presentation/EditProfileScreen.kt`؛ `feature/src/main/java/com/example/feature/ehsan/presentation/IhsanDetailsScreen.kt`؛ `feature/src/main/java/com/example/feature/profile/DonationHistoryScreen.kt`؛ `feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt`؛ `feature/src/main/java/com/example/feature/learning/presentation/components/LearningSettingsScreen.kt`؛ `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaStoryDetailScreen.kt`؛ `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanEpisodeDetailScreen.kt`؛ `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmEpisodeDetailScreen.kt`؛ `feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt`. أضف ملفات جديدة فقط ضمن نطاق الميزة بعد إعادة الجرد.
8. **Database and Migration Impact:** لا migrations؛ تغييرات نصوص UI فقط لا نصوص دينية.
9. **UI/UX Changes:** ثيم/حدود لمس/اتجاه لغة/حواف ونماذج وتفاصيل عبر CSV.
10. **Unit Tests:** UX-01: IhsanContrastTest وbottom-navigation tests؛ Compose semantics وحواف touch غير متداخلة؛ snapshots المظهرين؛ UX-02: resource key parity + Arabic RTL/English LTR navigation + mixed digits/URLs/font scale؛ UX-03: Quran integrity + Compose verse actions + font/zoom/pan + empty/favorite/source flows؛ UX-04: OfferDraftRestorationTest وAddEhsanAlbumFailureTest وPhysicalCommunityJourneyTest على fixtures؛ IME وcancel/delete confirm؛ UX-05: offline/retry/missing detail/saved empty + fullscreen/back/background وfont 2 + external-intent failure؛ استخدم pure/fake للمنطق المعزول فقط.
11. **Integration Tests:** DI resolution، repository→DAO/DataStore، navigation callbacks، فشل I/O والمعاملات وفق المهام؛ كل مخرجات مسجلة مع المصدر.
12. **Physical Device Tests:** Xiaomi ac190123 فقط؛ سيناريوهات المهام أعلاه وQA؛ مرحلة وثائق P3 لا تستدعي الجهاز. حفظ الإعدادات قبل/بعد وعدم المساس بالبيانات.
13. **Acceptance Criteria:** جميع شروط المهام أدناه + عدم انحدار G0 للأجزاء المتأثرة؛ لا PASS بلا دليل ولا تحويل Blocked إلى Pass.
14. **Rollback and Recovery Strategy:** عكس فرق المكون وحده وإعادة snapshots على مستخدميه؛ أي تغيير تخزين يجرب على fixture قبل الإنتاج.
15. **Definition of Done:** مراجعة الفرق، اختبارات مناسبة ناجحة، CSV/QA محدثان عند تغير route/capability، قرارات معتمدة، بيانات محفوظة، ثم قبول المالك للدفعة. Pending هاتف يمنع ادعاء Device QA Passed.
16. **Estimated Effort and Confidence:** UX-01=M، UX-02=M، UX-03=L، UX-04=M، UX-05=M. ثقة scope متوسطة؛ تأثير المكونات المشتركة وغياب دليل بصري لكل حالة مصدر عدم اليقين؛ لا تحويل تلقائي إلى مدة.

## Phase 2 — اتساق المنطق والبيانات

1. **Objective:** تقليل أخطاء state والحفظ والتهيئة دون تغيير سلوك العبادة.
2. **In Scope:** AR-01 استعادة البحث وتصفير حالات الفشل؛ AR-02 ذرية تقدم اليوم والقراءة؛ AR-03 سلامة Favorites والاستعلامات؛ AR-04 أخطاء التخزين والتهيئة والحدود؛ AR-05 توثيق الملكية وتقليل الكود غير الموصول.
3. **Out of Scope:** لا modularization شامل أو تبديل Room/DataStore/Koin.
4. **Dependencies:** G0-02، G0-05؛ التبعيات المشروطة حسب مسار الإصدار في السجل المرجعي؛ أرقام المراحل تنظيمية ولا تفرض انتظار ميزات مستقلة.
5. **Risks:** سباقات محتملة وبيانات legacy لا يجوز حذفها؛ قرار المالك في سجل D-xx مقدم على الافتراض.
6. **Exact Implementation Tasks:** سجلات AR-01، AR-02، AR-03، AR-04، AR-05 أدناه وأوامر Gemini المقابلة؛ لا مهمة عامة «حسن التطبيق».
7. **Expected Files and Modules:** `feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranViewModel.kt`؛ `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`؛ `feature/src/main/java/com/example/feature/dashboard/data/DailyActivityRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/quran/data/local/dao/QuranDao.kt`؛ `feature/src/main/java/com/example/feature/quran/data/local/entity/BookmarkEntity.kt`؛ `feature/src/main/java/com/example/feature/hadith/data/repository/HadithRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt`؛ `app/src/main/java/com/example/mol/di/DatabaseModule.kt`؛ `app/src/main/java/com/example/mol/BarakahApp.kt`؛ `feature/src/main/java/com/example/feature/core/observability/AppObservability.kt`؛ `app/src/main/java/com/example/mol/di/AppModule.kt`؛ `app/src/main/java/com/example/mol/di/RepositoryModule.kt`؛ `app/src/main/java/com/example/mol/di/ViewModelModule.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/prayer/presentation/ManualPrayerTrackerViewModel.kt`؛ `feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt`. أضف ملفات جديدة فقط ضمن نطاق الميزة بعد إعادة الجرد.
8. **Database and Migration Impact:** مفاتيح DataStore تحفظ؛ JOIN دون migration؛ UNIQUE/FK قرار بعد فحص.
9. **UI/UX Changes:** حالة error/retry مستقلة وعدم spinner عالق واستعادة البحث.
10. **Unit Tests:** AR-01: ViewModel fake failure/cancellation/debounce latest + recreate query/filter + empty after clear؛ AR-02: concurrent increment/reset/midnight/timezone؛ لا pair هجين من lastRead؛ إعادة تشغيل مع قيم قديمة؛ AR-03: query-count قبل/بعد + favorite toggle concurrency/idempotent save + legacy duplicate fixture؛ AR-04: fault injection: asset missing/invalid JSON/Room open/IO/cancel؛ retry دون reseed مدمر؛ AR-05: Koin graph + production route characterization + source-reference inventory؛ استخدم pure/fake للمنطق المعزول فقط.
11. **Integration Tests:** DI resolution، repository→DAO/DataStore، navigation callbacks، فشل I/O والمعاملات وفق المهام؛ كل مخرجات مسجلة مع المصدر.
12. **Physical Device Tests:** Xiaomi ac190123 فقط؛ سيناريوهات المهام أعلاه وQA؛ مرحلة وثائق P3 لا تستدعي الجهاز. حفظ الإعدادات قبل/بعد وعدم المساس بالبيانات.
13. **Acceptance Criteria:** جميع شروط المهام أدناه + عدم انحدار G0 للأجزاء المتأثرة؛ لا PASS بلا دليل ولا تحويل Blocked إلى Pass.
14. **Rollback and Recovery Strategy:** forward fix إذا تغير schema؛ حفظ المفاتيح القديمة وإيقاف dual-write؛ أي تغيير تخزين يجرب على fixture قبل الإنتاج.
15. **Definition of Done:** مراجعة الفرق، اختبارات مناسبة ناجحة، CSV/QA محدثان عند تغير route/capability، قرارات معتمدة، بيانات محفوظة، ثم قبول المالك للدفعة. Pending هاتف يمنع ادعاء Device QA Passed.
16. **Estimated Effort and Confidence:** AR-01=M، AR-02=M، AR-03=M، AR-04=M، AR-05=S. ثقة scope متوسطة؛ سباقات محتملة وبيانات legacy لا يجوز حذفها مصدر عدم اليقين؛ لا تحويل تلقائي إلى مدة.

## Phase 3 — الأداء والأمان والموثوقية

1. **Objective:** تأسيس baseline ثم تحسين سبب مثبت وإغلاق حدود الأمان.
2. **In Scope:** PS-01 قياس خط الأساس للأداء؛ PS-02 إصلاح عنق زجاجة واحد مثبت؛ PS-03 تدقيق WebView وIntents والملفات؛ PS-04 سياسة التوقيع والأذونات والبيانات.
3. **Out of Scope:** لا أرقام أداء مفترضة أو analytics أو ترقيات عشوائية.
4. **Dependencies:** G0-03، AR-01، AR-03، G0-04، UX-04، G0-02؛ التبعيات المشروطة حسب مسار الإصدار في السجل المرجعي؛ أرقام المراحل تنظيمية ولا تفرض انتظار ميزات مستقلة.
5. **Risks:** قياسات حساسة للحرارة وقرار التوقيع والأذونات؛ قرار المالك في سجل D-xx مقدم على الافتراض.
6. **Exact Implementation Tasks:** سجلات PS-01، PS-02، PS-03، PS-04 أدناه وأوامر Gemini المقابلة؛ لا مهمة عامة «حسن التطبيق».
7. **Expected Files and Modules:** `app/src/main/java/com/example/mol/BarakahApp.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`؛ `feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt`؛ `feature/src/main/java/com/example/feature/hadith/data/repository/HadithRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt`؛ `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt`؛ `feature/src/main/java/com/example/feature/learning/presentation/components/YouTubePlaybackBridge.kt`؛ `feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt`؛ `feature/src/main/java/com/example/feature/ehsan/data/image/EhsanImageStore.kt`؛ `app/src/main/AndroidManifest.xml`؛ `app/build.gradle.kts`؛ `app/src/main/res/xml/backup_rules.xml`؛ `app/src/main/res/xml/data_extraction_rules.xml`؛ `docs/RELEASE_SIGNING_GUIDE.md`؛ `docs/store/DATA_SAFETY_DISCLOSURE.md`. أضف ملفات جديدة فقط ضمن نطاق الميزة بعد إعادة الجرد.
8. **Database and Migration Impact:** لا توسعة backup؛ سياسة تصدير مستقلة؛ لا DB encryption تلقائيًا بلا threat model.
9. **UI/UX Changes:** لا layout redesign؛ فقط ما يخدم نتيجة مقاسة أو إصلاحًا مثبتًا.
10. **Unit Tests:** PS-01: Perfetto/System Trace + Android Profiler + dumpsys meminfo + APK Analyzer؛ 10 عينات لكل سيناريو مع median/p95 والظروف؛ PS-02: repeat نفس baseline + content hash/ordering + cancellation + memory teardown؛ PS-03: unit allowlist/URI + instrumentation WebView/file grants/renderer + lint exported-components؛ PS-04: signingReport بلا أسرار + cert APK + merged manifest + backup exclusion assertions + PII redaction؛ استخدم pure/fake للمنطق المعزول فقط.
11. **Integration Tests:** DI resolution، repository→DAO/DataStore، navigation callbacks، فشل I/O والمعاملات وفق المهام؛ كل مخرجات مسجلة مع المصدر.
12. **Physical Device Tests:** Xiaomi ac190123 فقط؛ سيناريوهات المهام أعلاه وQA؛ مرحلة وثائق P3 لا تستدعي الجهاز. حفظ الإعدادات قبل/بعد وعدم المساس بالبيانات.
13. **Acceptance Criteria:** جميع شروط المهام أدناه + عدم انحدار G0 للأجزاء المتأثرة؛ لا PASS بلا دليل ولا تحويل Blocked إلى Pass.
14. **Rollback and Recovery Strategy:** تراجع optimization وحده؛ لا تغيير cert/appId إلا خطة تحديث معتمدة؛ أي تغيير تخزين يجرب على fixture قبل الإنتاج.
15. **Definition of Done:** مراجعة الفرق، اختبارات مناسبة ناجحة، CSV/QA محدثان عند تغير route/capability، قرارات معتمدة، بيانات محفوظة، ثم قبول المالك للدفعة. Pending هاتف يمنع ادعاء Device QA Passed.
16. **Estimated Effort and Confidence:** PS-01=M، PS-02=M، PS-03=M، PS-04=M. ثقة scope متوسطة؛ قياسات حساسة للحرارة وقرار التوقيع والأذونات مصدر عدم اليقين؛ لا تحويل تلقائي إلى مدة.

## Phase 4 — الختمة والصلاة وWidgets

1. **Objective:** استكمال foundations الحالية إلى ميزات P1 محلية.
2. **In Scope:** F4-01 عقد خطة الختمة وحساب الصفحات؛ F4-02 تخزين الختمة وترحيلها الآمن؛ F4-03 واجهة الختمة والإحصاء البسيط؛ F4-04 وصل سجل الصلاة الموجود؛ F4-05 تقويم سجل الصلاة والملخصات؛ F4-06 إطلاق Widgets من العقد الموجود.
3. **Out of Scope:** لا إعادة إنشاء tracker/goals من الصفر؛ لا calendar/AI/backend.
4. **Dependencies:** AR-02، AR-05، G0-05، UX-03، UX-01، PS-01، PS-04، G0-06؛ التبعيات المشروطة حسب مسار الإصدار في السجل المرجعي؛ أرقام المراحل تنظيمية ولا تفرض انتظار ميزات مستقلة.
5. **Risks:** تعريف الإنجاز مقابل الموضع وخصوصية سجل العبادة وقيود launcher؛ قرار المالك في سجل D-xx مقدم على الافتراض.
6. **Exact Implementation Tasks:** سجلات F4-01، F4-02، F4-03، F4-04، F4-05، F4-06 أدناه وأوامر Gemini المقابلة؛ لا مهمة عامة «حسن التطبيق».
7. **Expected Files and Modules:** `feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`؛ `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`؛ `feature/src/main/assets/mushaf_page_starts.json`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt`؛ `app/src/main/java/com/example/mol/di/DatabaseModule.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranListScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeQuranContinueCard.kt`؛ `app/src/main/java/com/example/mol/navigation/Screen.kt`؛ `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`؛ `feature/src/main/java/com/example/feature/prayer/data/local/entity/PrayerLogEntity.kt`؛ `feature/src/main/java/com/example/feature/prayer/data/local/dao/PrayerLogDao.kt`؛ `feature/src/main/java/com/example/feature/prayer/data/repository/ManualPrayerLogRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/prayer/presentation/ManualPrayerTrackerViewModel.kt`؛ `feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt`؛ `app/src/main/java/com/example/mol/di/ViewModelModule.kt`؛ `feature/src/main/java/com/example/feature/statistics/domain/PrayerTrackerStatsCalculator.kt`؛ `feature/src/main/java/com/example/feature/statistics/data/StatisticsRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/statistics/StatisticsScreen.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeDailyJourneyCard.kt`؛ `feature/src/main/java/com/example/feature/prayer/widget/PrayerWidgetState.kt`؛ `feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt`؛ `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`؛ `app/src/main/AndroidManifest.xml`. أضف ملفات جديدة فقط ضمن نطاق الميزة بعد إعادة الجرد.
8. **Database and Migration Impact:** جداول plan/completion وprayer_log تدريجيًا؛ رقم schema يستخرج من HEAD عند كل دفعة.
9. **UI/UX Changes:** نقطة خطة داخل القرآن؛ tracker في الصلاة؛ widget launcher؛ لا بطاقة رحلة ثانية.
10. **Unit Tests:** F4-01: 7/30/60، due today، overdue، partial page، ختمة جديدة، leap day، noncontiguous read؛ F4-02: migration all supported starts + reopen + interrupted transaction + unique day/range + cancel/reschedule؛ F4-03: VM + Compose setup/edit/replan/rotate/offline + font/RTL؛ قرآن integrity؛ F4-04: ManualPrayerLogRepositoryTest + DI + migration + duplicate rapid taps + midnight + private deletion؛ F4-05: range boundaries/zero records/month roll/leap + VM/error + Compose calendar/history/Home parity؛ F4-06: factory + PendingIntent immutable identity + no-location + resize + launcher/reboot بعد الإذن + resume Quran target؛ استخدم pure/fake للمنطق المعزول فقط.
11. **Integration Tests:** DI resolution، repository→DAO/DataStore، navigation callbacks، فشل I/O والمعاملات وفق المهام؛ كل مخرجات مسجلة مع المصدر.
12. **Physical Device Tests:** Xiaomi ac190123 فقط؛ سيناريوهات المهام أعلاه وQA؛ مرحلة وثائق P3 لا تستدعي الجهاز. حفظ الإعدادات قبل/بعد وعدم المساس بالبيانات.
13. **Acceptance Criteria:** جميع شروط المهام أدناه + عدم انحدار G0 للأجزاء المتأثرة؛ لا PASS بلا دليل ولا تحويل Blocked إلى Pass.
14. **Rollback and Recovery Strategy:** إخفاء entry بعد خطأ مع حفظ الجداول؛ forward fix؛ لا إسقاط schema؛ أي تغيير تخزين يجرب على fixture قبل الإنتاج.
15. **Definition of Done:** مراجعة الفرق، اختبارات مناسبة ناجحة، CSV/QA محدثان عند تغير route/capability، قرارات معتمدة، بيانات محفوظة، ثم قبول المالك للدفعة. Pending هاتف يمنع ادعاء Device QA Passed.
16. **Estimated Effort and Confidence:** F4-01=M، F4-02=L، F4-03=M، F4-04=L، F4-05=M، F4-06=L. ثقة scope متوسطة؛ تعريف الإنجاز مقابل الموضع وخصوصية سجل العبادة وقيود launcher مصدر عدم اليقين؛ لا تحويل تلقائي إلى مدة.

## Phase 5 — البحث والتقويم والحفظ والملاحظات

1. **Objective:** تطوير البحث وإضافة calendar وحفظ يدوي وملاحظات خاصة.
2. **In Scope:** F5-01 تطوير البحث العربي الموجود؛ F5-02 محرك التقويم الهجري والتعديل المحلي؛ F5-03 واجهة التقويم والأحداث الموثقة؛ F5-04 عقد الحفظ والمراجعة وتخزينهما؛ F5-05 واجهة الاختبار الذاتي اليدوي؛ F5-06 ملاحظات الآيات الخاصة وتخزينها؛ F5-07 تصدير اختياري آمن للملاحظات.
3. **Out of Scope:** لا tafsir بديل مولد أو اختبار صوت أو cloud sync أو restore شامل.
4. **Dependencies:** AR-01، PS-01، AR-02، UX-02، AR-05، G0-05، UX-03، PS-04؛ التبعيات المشروطة حسب مسار الإصدار في السجل المرجعي؛ أرقام المراحل تنظيمية ولا تفرض انتظار ميزات مستقلة.
5. **Risks:** اعتماد أحداث دينية ومعنى offset وحماية المحتوى الخاص؛ قرار المالك في سجل D-xx مقدم على الافتراض.
6. **Exact Implementation Tasks:** سجلات F5-01، F5-02، F5-03، F5-04، F5-05، F5-06، F5-07 أدناه وأوامر Gemini المقابلة؛ لا مهمة عامة «حسن التطبيق».
7. **Expected Files and Modules:** `feature/src/main/java/com/example/feature/core/search/ArabicSearch.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt`؛ `feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/hadith/presentation/HadithViewModel.kt`؛ `feature/src/main/java/com/example/feature/core/util/HijriDateFormatter.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/DashboardDatePolicy.kt`؛ `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`؛ `feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt`؛ `app/src/main/java/com/example/mol/navigation/Screen.kt`؛ `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`؛ `feature/src/main/java/com/example/feature/quran/domain/model/QuranRepeat.kt`؛ `feature/src/main/java/com/example/feature/quran/domain/model/QuranNavigation.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranRepeatSheet.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/TafsirBottomSheet.kt`؛ `feature/src/main/java/com/example/feature/ehsan/data/image/EhsanImageStore.kt`؛ `app/src/main/AndroidManifest.xml`؛ `app/src/main/res/xml/backup_rules.xml`. أضف ملفات جديدة فقط ضمن نطاق الميزة بعد إعادة الجرد.
8. **Database and Migration Impact:** provider إعداد هجري؛ review/notes additive migrations؛ export اختياري منفصل.
9. **UI/UX Changes:** تقويم وتدريب يدوي وملاحظات واضحة المصدر؛ القرآن الحالي يبقى أصلًا.
10. **Unit Tests:** F5-01: ArabicSearchTest/QuranSearchTest/DuaSearchRegressionTest + stale-query/source error + cold/warm p95؛ F5-02: boundaries/offset +/-/DST/API25 fallback/unit conversion + consistency Home/Profile/Prayer؛ F5-03: Compose month navigation/RTL/font + event source + timezone restore؛ F5-04: spaced review deterministic + same-day repeat/overdue/clock + migration/orphan references؛ F5-05: Compose select/hide/reveal/back/TalkBack + offline restore + full Quran hash؛ F5-06: migration + duplicate/long text/delete/cancel/recreate + Arabic note search + privacy deletion؛ F5-07: UTF-8/Arabic/empty/cancel/IO failure/oversize + exported content allowlist؛ استخدم pure/fake للمنطق المعزول فقط.
11. **Integration Tests:** DI resolution، repository→DAO/DataStore، navigation callbacks، فشل I/O والمعاملات وفق المهام؛ كل مخرجات مسجلة مع المصدر.
12. **Physical Device Tests:** Xiaomi ac190123 فقط؛ سيناريوهات المهام أعلاه وQA؛ مرحلة وثائق P3 لا تستدعي الجهاز. حفظ الإعدادات قبل/بعد وعدم المساس بالبيانات.
13. **Acceptance Criteria:** جميع شروط المهام أدناه + عدم انحدار G0 للأجزاء المتأثرة؛ لا PASS بلا دليل ولا تحويل Blocked إلى Pass.
14. **Rollback and Recovery Strategy:** تعطيل entries الجديدة مع حفظ البيانات؛ rollback يجرّب على fixture؛ أي تغيير تخزين يجرب على fixture قبل الإنتاج.
15. **Definition of Done:** مراجعة الفرق، اختبارات مناسبة ناجحة، CSV/QA محدثان عند تغير route/capability، قرارات معتمدة، بيانات محفوظة، ثم قبول المالك للدفعة. Pending هاتف يمنع ادعاء Device QA Passed.
16. **Estimated Effort and Confidence:** F5-01=M، F5-02=M، F5-03=M، F5-04=L، F5-05=M، F5-06=L، F5-07=M. ثقة scope متوسطة؛ اعتماد أحداث دينية ومعنى offset وحماية المحتوى الخاص مصدر عدم اليقين؛ لا تحويل تلقائي إلى مدة.

## Phase 6 — الصيام ورمضان والتعلم

1. **Objective:** ربط الميزات الموسمية ومسار تعلم واحد.
2. **In Scope:** F6-01 متابعة الصيام المحلية؛ F6-02 مركز رمضان يربط الموجود؛ F6-03 تطوير مسار التعلم الموجود؛ F6-04 قبول الميزات المتكامل.
3. **Out of Scope:** لا فتاوى صيام أو تنزيل YouTube أو ربط تعليمي مولد.
4. **Dependencies:** F5-03، F4-05، F4-03، UX-05، AR-05، F5-07؛ التبعيات المشروطة حسب مسار الإصدار في السجل المرجعي؛ أرقام المراحل تنظيمية ولا تفرض انتظار ميزات مستقلة.
5. **Risks:** اختلاف الرؤية وتوقيت السحور/الإفطار وتفاعل features؛ قرار المالك في سجل D-xx مقدم على الافتراض.
6. **Exact Implementation Tasks:** سجلات F6-01، F6-02، F6-03، F6-04 أدناه وأوامر Gemini المقابلة؛ لا مهمة عامة «حسن التطبيق».
7. **Expected Files and Modules:** `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/statistics/StatisticsScreen.kt`؛ `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`؛ `feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt`؛ `feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranListScreen.kt`؛ `app/src/main/java/com/example/mol/navigation/Screen.kt`؛ `feature/src/main/java/com/example/feature/learning/data/LearningBookmarkStore.kt`؛ `feature/src/main/java/com/example/feature/fahmanallah/data/repository/FahmRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmJourneyScreen.kt`؛ `feature/src/main/java/com/example/feature/sanhya/data/repository/SanhyaRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/nabiihsan/data/repository/NabiIhsanRepositoryImpl.kt`؛ `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`؛ `app/src/androidTest/java/com/example/mol/ui/ReleaseFunctionalJourneyTest.kt`؛ `app/src/androidTest/java/com/example/mol/ui/ReleaseCorePersistenceTest.kt`. أضف ملفات جديدة فقط ضمن نطاق الميزة بعد إعادة الجرد.
8. **Database and Migration Impact:** fasting_log بإدخال يدوي؛ learning keys migrate فقط عند الحاجة.
9. **UI/UX Changes:** مركز رمضان كروابط وتجميع canonical؛ سجل تعلم بدل مكتبة مكررة.
10. **Unit Tests:** F6-01: migration + duplicate date/undo/month/offset + privacy delete + offline VM؛ F6-02: parity Fajr/Maghrib+timezone/location + no-location + seasonal entry + offline navigation؛ F6-03: existing repository persistence + completion/reopen/reorder/missing episode/no network + migration keys؛ F6-04: offline-first E2E + migration old/current/new + process restore + font/RTL + security+baseline performance؛ استخدم pure/fake للمنطق المعزول فقط.
11. **Integration Tests:** DI resolution، repository→DAO/DataStore، navigation callbacks، فشل I/O والمعاملات وفق المهام؛ كل مخرجات مسجلة مع المصدر.
12. **Physical Device Tests:** Xiaomi ac190123 فقط؛ سيناريوهات المهام أعلاه وQA؛ مرحلة وثائق P3 لا تستدعي الجهاز. حفظ الإعدادات قبل/بعد وعدم المساس بالبيانات.
13. **Acceptance Criteria:** جميع شروط المهام أدناه + عدم انحدار G0 للأجزاء المتأثرة؛ لا PASS بلا دليل ولا تحويل Blocked إلى Pass.
14. **Rollback and Recovery Strategy:** تعطيل entry الموسمي لا يحذف السجلات؛ استعادة keys القديمة؛ أي تغيير تخزين يجرب على fixture قبل الإنتاج.
15. **Definition of Done:** مراجعة الفرق، اختبارات مناسبة ناجحة، CSV/QA محدثان عند تغير route/capability، قرارات معتمدة، بيانات محفوظة، ثم قبول المالك للدفعة. Pending هاتف يمنع ادعاء Device QA Passed.
16. **Estimated Effort and Confidence:** F6-01=L، F6-02=M، F6-03=L، F6-04=L. ثقة scope متوسطة؛ اختلاف الرؤية وتوقيت السحور/الإفطار وتفاعل features مصدر عدم اليقين؛ لا تحويل تلقائي إلى مدة.

## Phase 7 — القرارات المشروطة وتجهيز النشر

1. **Objective:** تجهيز قرار نشر مستقل ودراسة P3 دون تنفيذها.
2. **In Scope:** R7-01 دراسة الميزات المشروطة دون تشغيل خدمات؛ R7-02 إغلاق حقوق المحتوى وهوية النشر؛ R7-03 قرار الإصدار النهائي المستقل.
3. **Out of Scope:** لا نشر تلقائي أو تشغيل API/مدفوعات أو مصادقة/مجتمع خادم.
4. **Dependencies:** PS-04، G0-01، G0-02، G0-03، G0-04، G0-05، G0-06؛ التبعيات المشروطة حسب مسار الإصدار في السجل المرجعي؛ أرقام المراحل تنظيمية ولا تفرض انتظار ميزات مستقلة.
5. **Risks:** موافقة قانونية/حقوق/هوية وسلامة نشر مستقلة؛ قرار المالك في سجل D-xx مقدم على الافتراض.
6. **Exact Implementation Tasks:** سجلات R7-01، R7-02، R7-03 أدناه وأوامر Gemini المقابلة؛ لا مهمة عامة «حسن التطبيق».
7. **Expected Files and Modules:** `docs/release/FUTURE_BACKEND_CONTRACTS.md`؛ `docs/release/EXTERNAL_BLOCKERS_DECISIONS.md`؛ `docs/store/PRIVACY_POLICY.md`؛ `docs/release/EXTERNAL_ASSET_REGISTER.csv`؛ `docs/release/CONTENT_SOURCES_AND_LICENSES.md`؛ `docs/release/ASSET_CREDITS.md`؛ `docs/PRODUCTION_IDENTITY_DECISION.md`؛ `docs/RELEASE_SIGNING_GUIDE.md`؛ `docs/planning/IHSAN_QA_AND_RELEASE_GATES.md`؛ `docs/release/BETA_ACCEPTANCE_MATRIX.md`؛ `docs/qa/FINAL_BETA_GAP_REPORT.md`. أضف ملفات جديدة فقط ضمن نطاق الميزة بعد إعادة الجرد.
8. **Database and Migration Impact:** لا migration لمجرد دراسة P3؛ أي backend مستقبل يحتاج عقد sync منفصل.
9. **UI/UX Changes:** لا UI توحي بخدمة حقيقية قبل الموافقات والمراجعات.
10. **Unit Tests:** R7-01: مراجعة completeness والقرارات ومعايير go/no-go؛ لا اختبار خدمات حقيقية؛ R7-02: asset-to-license coverage + missing-license blocker + cert/appId/update fixture؛ R7-03: signed artifact hash + full regression matrix + zero critical open + explicit owner acceptance؛ استخدم pure/fake للمنطق المعزول فقط.
11. **Integration Tests:** DI resolution، repository→DAO/DataStore، navigation callbacks، فشل I/O والمعاملات وفق المهام؛ كل مخرجات مسجلة مع المصدر.
12. **Physical Device Tests:** Xiaomi ac190123 فقط؛ سيناريوهات المهام أعلاه وQA؛ مرحلة وثائق P3 لا تستدعي الجهاز. حفظ الإعدادات قبل/بعد وعدم المساس بالبيانات.
13. **Acceptance Criteria:** جميع شروط المهام أدناه + عدم انحدار G0 للأجزاء المتأثرة؛ لا PASS بلا دليل ولا تحويل Blocked إلى Pass.
14. **Rollback and Recovery Strategy:** لا تسليم عام إن بقي license/signing blocker؛ حفظ artifact المقبول؛ أي تغيير تخزين يجرب على fixture قبل الإنتاج.
15. **Definition of Done:** مراجعة الفرق، اختبارات مناسبة ناجحة، CSV/QA محدثان عند تغير route/capability، قرارات معتمدة، بيانات محفوظة، ثم قبول المالك للدفعة. Pending هاتف يمنع ادعاء Device QA Passed.
16. **Estimated Effort and Confidence:** R7-01=M، R7-02=L، R7-03=S. ثقة scope متوسطة؛ موافقة قانونية/حقوق/هوية وسلامة نشر مستقلة مصدر عدم اليقين؛ لا تحويل تلقائي إلى مدة.

## سجل المهام المرجعي

هذا السجل وأوامر Gemini يستخدمان نفس IDs. يمكن للمراجع الانتقال من finding/CSV إلى هنا ثم إلى الأمر.

### G0-01 — تثبيت خط الأساس وتسوية سجل القبول (منجزة - جاهزة للقبول التوثيقي)

- **الحالة الحالية والتنفيذ**: تم إنجاز التدقيق المكتبي المكتمل لسلسلة الأدلة والمصدر والبناء والمثبت والمختبر بالأدلة المحفوظة فقط، دون تشغيل بناء أو ADB أو هاتف أو تعديل كود.
- **التنفيذ المحدد وتثبيت الأدلة وتصنيفها**:
  - **تسوية ملفات APK**:
    - `4BF1DD4F...` -> **Reported Only / Historical Evidence** (مذكور بتقرير QA التاريخي بـ12 صورة، بينما الملف غير محفوظ بالمسارات المباشرة).
    - `C415EA4A...` -> **Verified Historically & File Confirmed** (الـAPK وسجل البناء بـ603 ملفات موجودان بمجلد `release/apk/` و provenance، ولم يُختبر هاتفياً لانقطاع ADB).
    - `dba6df9f...` -> **Verified Historically & File Confirmed** (الـAPK وسجل التثبيت بـ`installedApkByteIdentical=true` وملف `.sha256` المرفق موجودان بمجلد `release/apk/` دون مسح بيانات، والقبول الأمامي مسجل `Not Tested`).
  - **جرد مدخلات المصدر والنظام**: Git HEAD `7dcf417038c90cb599326abd50afcf82321d6544` (**Verified Historically**) | 777 ملفًا مراقبًا ببصمة `6ac93ec8...` (**Reported Only**) | git status digest `c0e781fb...` (**Reported Only**) | Gradle Wrapper **8.10.2** و AGP **8.7.3** (**Verified Now** من `gradle-wrapper.properties` و `libs.versions.toml`).
  - **حساب نتائج XML التاريخية**: 477 اختبارًا ناجحًا (app: 33, feature: 415, designsystem: 29) عبر 105 suites بصفر أخطاء/فشل/تخطٍ (**Verified Historically** عبر `quality-summary.json` المؤرخ 17 أكتوبر 2026).
  - **ربط REC-01..REC-06**: ربطت معايير القبول بـ G0-03 و UX-01/UX-03/UX-05 دون إضافة مهام جديدة، وتم إثبات فجوة الكود لـ REC-04 (**Verified Now**: البطاقات لا تستخدم الـCleaner).
  - **سجل النواقص لـ G0-02..06**: حُددت الأدلة النواقص وطرق إثباتها المستقبلية (**Verified Now**).
- **البداية والملفات المتأثرة**: `docs/planning/IHSAN_AS_IS_AUDIT.md`؛ `docs/planning/IHSAN_QA_AND_RELEASE_GATES.md`؛ `docs/planning/IHSAN_MASTER_IMPROVEMENT_PLAN.md`؛ `docs/qa/FINAL_PHYSICAL_DEVICE_QA.md`؛ `docs/qa/FINAL_BETA_GAP_REPORT.md`؛ `docs/release/VIDEO_BLACK_SCREEN_RCA_2026-10-17.md`.
- **الاعتماديات**: اعتماد الخطة. الحجم: S؛ أولوية التنفيذ: P0. التبعيات المشروطة: لا توجد.
- **البيانات**: لا تغيير Schema.
- **الاختبارات**: تحقق آلياً ومكتبياً من وجود الأدلة ومطابقة البصمات والأرقام؛ حساب XMLs تاريخي وليس تشغيل جديد.
- **القبول المنجز**: سجّل موحد يبين نطاق كل PASS وتعارض التقارير صراحة.
- **المخاطر والتعافي**: منع نقل PASS بين البصمات؛ التعافي بتحديث الوثائق فقط دون Git Mutation.
- **نطاق G0-01 النهائي**: Ready for Documentation Acceptance فقط، لا Build/Device/Release PASS جديد.

### G0-02 — إعادة تأهيل البناء والاختبارات المحلية

- الحالة الحالية/لماذا: 477 نتيجة تاريخية وليست إثباتًا لحالة الملفات اليوم.
- التنفيذ المحدد: بعد G0-01 شغّل بناء Debug وRelease والوحدات وLint للوحدات الثلاث؛ سجل JDK والأوامر والتحذيرات وhash المصدر. لا ترقي الاعتماديات ولا تغير التوقيع لحل فشل. أي إصلاح يحتاج دفعة منفصلة موثقة.
- البداية والملفات المتأثرة: `settings.gradle.kts`؛ `app/build.gradle.kts`؛ `feature/build.gradle.kts`؛ `designsystem/build.gradle.kts`؛ `gradle/libs.versions.toml`.
- الاعتماديات: G0-01. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: assembleDebug وassembleRelease وtestDebugUnitTest وlintDebug وlintRelease للوحدات الثلاث؛ احسب XML حسب module.
- القبول: صفر failures/errors مع توثيق skipped والتحذيرات؛ الفشل يمنع الانتقال ولا يمحى.
- المخاطر: بيئة JDK وتوقيع داخلي. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P0. التبعيات المشروطة: لا توجد
### G0-03 — قبول التنقل والحالات على البناء المختار

- الحالة الحالية/لماذا: 58 موضع تسجيل ساكنًا (واحد gated؛ Release57/Debug58) و59 حالة اختبار؛ Splash وLegacy خارج قائمة الـ59.
- التنفيذ المحدد: حضّر fixture معزولة، ثم على الهاتف المصرح حصريًا سجل 59 حالة في المظهرين، وSplash الحقيقي وLegacy redirect منفصلين. افحص الأفعال والرجوع والاستعادة لا مجرد render. أضف الحالات الثانوية المدرجة في CSV على دفعات أقسام.
- البداية والملفات المتأثرة: `app/src/main/java/com/example/mol/navigation/Screen.kt`؛ `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`؛ `app/src/androidTest/java/com/example/mol/ui/DarkFinalRouteMatrixTest.kt`؛ `app/src/androidTest/java/com/example/mol/ui/PreserveDevicePreferences.kt`.
- الاعتماديات: G0-02. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: Compose navigation + journeys + keyboard/insets + TalkBack/font 1/1.3/2؛ تحقق من الهوية قبل كل جلسة.
- القبول: كل صف له دليل وبصمة أو سبب حجب؛ لا اعتماد شامل مع صف وظيفي حرج معلق.
- المخاطر: التأثير على التفضيلات والبيانات الحقيقية. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P0. التبعيات المشروطة: لا توجد

- تغطية الإصلاحات الأصلية: REC-01، REC-02، REC-03، REC-04، REC-05، REC-06 وفق معايير وأدلة كلREC المستقلة في IHSAN_QA_AND_RELEASE_GATES.md. لا تكرر المهمة ولا تعِد إصلاح كود صحيح؛ افصل Source Reviewed عن Visual Accepted. عند ثبوت عيب متعلق بBeta أصلحه وحده وأعد قبوله؛ لا تنتظر كل تحسيناتPhase1 أوالمزاياالجديدة.

### G0-04 — إغلاق قبول الفيديو والبث

- الحالة الحالية/لماذا: إصلاح MATCH_PARENT وfullscreen موجود؛ أدلة نهائية جزئية وصوت/Release معلقان.
- التنفيذ المحدد: أعد استخراج EpisodeRecord فقط: Sanhya=28 Nabi=29 Fahm=29 ثم اطبع الفرق مع 86 السابق. اختبر صورة متحركة وتقدم زمن وصوت مسموع لكل عنصر وفق نطاق القبول المعتمد، وعينة lifecycle في كل قسم على Release. سجّل 150 كرفض تضمين واختبر البديل الرسمي مستقلاً؛ لا تعدّل أصل الفيديو.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt`؛ `feature/src/main/java/com/example/feature/learning/presentation/components/VideoWebChromeClient.kt`؛ `feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt`؛ `app/src/androidTest/java/com/example/mol/ui/PhysicalDeviceMediaAuditTest.kt`.
- الاعتماديات: G0-02. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: VideoRenderingRootCauseTest وVideoLifecyclePhoneTest ثم سجل per-ID لصورة/صوت/ثيمين/fullscreen/background/Release.
- القبول: لا تستبدل تشغيل الداخل بفتح YouTube؛ كل بعد له نتيجته؛ قرار D-01 يحسم منع Beta بسبب live embed.
- المخاطر: تغير سياسات المصدر وعدم توافر الهاتف. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P0. التبعيات المشروطة: لا توجد
### G0-05 — قبول الترحيل وحماية البيانات

- الحالة الحالية/لماذا: Room v8 بينما مصفوفة قديمة تقول v5.
- التنفيذ المحدد: حدّث مصفوفة 2→3→5→6→7→8 من الاختبارات والكود. افحص كل نقطة بدء مدعومة ببيانات اصطناعية في مساحة اختبار معزولة، وقارن الصفوف والمعرفات والمفضلات والملفات. لا تفتح قواعد المستخدم الحقيقية ولا تفترض دعم v1 أو v4.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt`؛ `feature/src/androidTest/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationTest.kt`؛ `docs/ROOM_MIGRATION_MATRIX.md`.
- الاعتماديات: G0-02. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: MigrationTestHelper مع schemas وfixture تاريخي موثق؛ fresh/current/upgrade/missing path؛ rollback rehearsal على fixture.
- القبول: هوية v8 صحيحة وعدم فقد البيانات؛ كل مسار مدعوم له إثبات هاتف؛ لا destructive fallback.
- المخاطر: ملفات schemas التاريخية والتباس حزمة المستخدم. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P0. التبعيات المشروطة: لا توجد
### G0-06 — قبول الصلاة والقبلة ووصول الإشعارات

- الحالة الحالية/لماذا: حساب وledger وReceiver موجودة؛ وجودها لا يثبت delivery أو دقة المستشعر.
- التنفيذ المحدد: نفذ السيناريوهات في وثيقة QA على الهاتف فقط بعد التحضير. قارن المواقيت بمرجع مستقل مطابق الطريقة والمذهب والتاريخ والمنطقة. افصل bearing الرياضي عن معايرة المستشعر. تحقق deny/grant وDoze والوقت والمنطقة وإعادة التشغيل؛ reboot خطوة منفصلة تتطلب موافقة صريحة بعد حفظ الوضع.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt`؛ `feature/src/main/java/com/example/feature/prayer/data/scheduler/AndroidPrayerAlarmGateway.kt`؛ `feature/src/main/java/com/example/feature/prayer/worker/PrayerSystemReconciliationReceiver.kt`؛ `feature/src/main/java/com/example/feature/qibla/util/QiblaManager.kt`؛ `feature/src/main/java/com/example/feature/reminders/AzkarReminderCoordinator.kt`.
- الاعتماديات: G0-02. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: PrayerIndependentReferenceTest وHomePrayerParityTest وQiblaSensorAccuracyTest وAuditSystemDeliveryTest وAuditTimezoneChangeTest؛ دليل إشعار مرئي ومسموع.
- القبول: لا تكرار/صمت غير مفسر؛ استعادة الإعدادات؛ reboot يبقى Not Tested دون الموافقة؛ فرق الحساب موثق.
- المخاطر: قيود Xiaomi وتغيير إعدادات الجهاز. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P0. التبعيات المشروطة: لا توجد
### UX-01 — قياس المكونات المشتركة وإتاحة الوصول

- الحالة الحالية/لماذا: نظام ثيم ومكونات موجود مع اختبارات contrast؛ الحدود التفاعلية الفعلية غير مثبتة لكل شاشة.
- التنفيذ المحدد: قسّم فحص المكونات ثم عالج فقط إخفاقات موثقة في touch/focus/contrast/font/insets. استخدم Material 3 وأبق geometry والهوية؛ لا تستبدل الصور ولا لون صفحة المصحف.
- البداية والملفات المتأثرة: `designsystem/src/main/java/com/example/designsystem/component/IhsanButton.kt`؛ `designsystem/src/main/java/com/example/designsystem/component/IhsanBottomNavigation.kt`؛ `designsystem/src/main/java/com/example/designsystem/theme/IhsanSemanticColors.kt`؛ `designsystem/src/main/java/com/example/designsystem/theme/Typography.kt`؛ `app/src/main/java/com/example/mol/ui/MainScreen.kt`.
- الاعتماديات: G0-03. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: IhsanContrastTest وbottom-navigation tests؛ Compose semantics وحواف touch غير متداخلة؛ snapshots المظهرين.
- القبول: 48dp للأفعال المستهدفة وحدود لمس غير متداخلة وتباين مقاس 4.5:1 للنص العادي و3:1 للكبير؛ لا قص عند 200%.
- المخاطر: تأثير واسع على جميع الشاشات. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد

- تغطية الإصلاحات الأصلية: REC-01 وفق معايير وأدلة كلREC المستقلة في IHSAN_QA_AND_RELEASE_GATES.md. لا تكرر المهمة ولا تعِد إصلاح كود صحيح؛ افصل Source Reviewed عن Visual Accepted. عند ثبوت عيب متعلق بBeta أصلحه وحده وأعد قبوله؛ لا تنتظر كل تحسيناتPhase1 أوالمزاياالجديدة.

### UX-02 — اتجاه اللغة والنصوص القابلة للترجمة

- الحالة الحالية/لماذا: MainScreen وبعض الشاشات تفرض RTL؛ نصوص عربية literal موجودة.
- التنفيذ المحدد: استخرج نصوص UI فقط تدريجيًا؛ اجعل shell يتبع لغة الواجهة حين اعتماد الإنجليزية، مع RTL صريح للنص القرآني فقط واتجاه معزول للهاتف والروابط. لا تترجم النص الديني تلقائيًا.
- البداية والملفات المتأثرة: `app/src/main/java/com/example/mol/ui/MainScreen.kt`؛ `feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt`؛ `feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt`؛ `feature/src/main/res/values/strings.xml`؛ `feature/src/main/res/values-ar/strings.xml`.
- الاعتماديات: UX-01. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: resource key parity + Arabic RTL/English LTR navigation + mixed digits/URLs/font scale.
- القبول: لا انعكاس للنص القرآني؛ لا مفاتيح ناقصة؛ قرار D-04 يحدد نطاق الإنجليزية.
- المخاطر: انعكاس التنقل وتغير طول النص. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### UX-03 — تدقيق أسطح العبادة واستعادة الوصول

- الحالة الحالية/لماذا: المصحف paragraph مع pointerInput؛ تفاصيل العبادة متعددة وحالات مصدر متاحة.
- التنفيذ المحدد: من CSV عالج مشكلات مثبتة فقط: أكشن الآية المتاح عبر TalkBack دون تغيير الترتيب/الأحرف، تكبير الصفحة وpan وselection، وصول التفصيل والنسخ والمشاركة والمصدر. نفذ قسمًا واحدًا في كل اعتماد فرعي.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`؛ `feature/src/main/java/com/example/feature/azkar/AzkarScreen.kt`؛ `feature/src/main/java/com/example/feature/tasbih/presentation/TasbihScreen.kt`؛ `feature/src/main/java/com/example/feature/duas/DuaDetailScreen.kt`؛ `feature/src/main/java/com/example/feature/hadith/presentation/HadithScreen.kt`؛ `feature/src/main/java/com/example/feature/asma/presentation/AsmaScreen.kt`.
- الاعتماديات: UX-01. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: Quran integrity + Compose verse actions + font/zoom/pan + empty/favorite/source flows.
- القبول: خلفية المصحف والزخارف والنص محفوظة؛ أفعال الآيات قابلة للوصول دون الاعتماد على النقر الإحداثي وحده.
- المخاطر: المساس بعرض النص أو آليات تحديد الآيات. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد

- تغطية الإصلاحات الأصلية: REC-02، REC-03 وفق معايير وأدلة كلREC المستقلة في IHSAN_QA_AND_RELEASE_GATES.md. لا تكرر المهمة ولا تعِد إصلاح كود صحيح؛ افصل Source Reviewed عن Visual Accepted. عند ثبوت عيب متعلق بBeta أصلحه وحده وأعد قبوله؛ لا تنتظر كل تحسيناتPhase1 أوالمزاياالجديدة.

### UX-04 — نماذج المجتمع والملف وحالات الاستعادة

- الحالة الحالية/لماذا: نماذج محلية وصور وملكية USER/LEGACY/DEMO قائمة.
- التنفيذ المحدد: افحص ثم أصلح فقط حالات keyboard/validation/focus/draft/duplicate submit المثبتة. أبق إفصاح المحلية وDEMO وحوار الحذف وملكية السجل. لا تختبر إرسالًا أو حذفًا على بيانات فعلية.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/ehsan/presentation/AddEhsanScreen.kt`؛ `feature/src/main/java/com/example/feature/ehsan/presentation/RequestHelpScreen.kt`؛ `feature/src/main/java/com/example/feature/profile/presentation/EditProfileScreen.kt`؛ `feature/src/main/java/com/example/feature/ehsan/presentation/IhsanDetailsScreen.kt`؛ `feature/src/main/java/com/example/feature/profile/DonationHistoryScreen.kt`.
- الاعتماديات: UX-01. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: OfferDraftRestorationTest وAddEhsanAlbumFailureTest وPhysicalCommunityJourneyTest على fixtures؛ IME وcancel/delete confirm.
- القبول: لا submission مزدوج؛ المسودة تستعاد حيث يوعد بها؛ آخر حقل وزر ظاهر؛ missing-ID قابل للاستعادة.
- المخاطر: PII والصور والضغط السريع. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### UX-05 — التعلم والبث والحالات المشتركة

- الحالة الحالية/لماذا: مشغل مشترك وحالات offline/error وfallback؛ مصفوفة الثيم/font غير مكتملة.
- التنفيذ المحدد: وحّد فقط رسائل الحالات وأفعال الاستعادة التي يفشل اختبارها، وافحص labels الطويلة ونسبة الصور وعدم حجب controls. أبق الحالة غير المتاحة للوصفات/المحطات دون محتوى مولد.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt`؛ `feature/src/main/java/com/example/feature/learning/presentation/components/LearningSettingsScreen.kt`؛ `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaStoryDetailScreen.kt`؛ `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanEpisodeDetailScreen.kt`؛ `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmEpisodeDetailScreen.kt`؛ `feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt`.
- الاعتماديات: G0-04، UX-01. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: offline/retry/missing detail/saved empty + fullscreen/back/background وfont 2 + external-intent failure.
- القبول: لا فقد مكتبة المحفوظات ولا overlay على YouTube؛ بديل واضح دون ادعاء نجاح التضمين.
- المخاطر: انحدار المشغل الذي أصلح حديثًا. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد

- تغطية الإصلاحات الأصلية: REC-04، REC-05، REC-06 وفق معايير وأدلة كلREC المستقلة في IHSAN_QA_AND_RELEASE_GATES.md. لا تكرر المهمة ولا تعِد إصلاح كود صحيح؛ افصل Source Reviewed عن Visual Accepted. عند ثبوت عيب متعلق بBeta أصلحه وحده وأعد قبوله؛ لا تنتظر كل تحسيناتPhase1 أوالمزاياالجديدة.

### AR-01 — استعادة البحث وتصفير حالات الفشل

- الحالة الحالية/لماذا: query/filter في remember؛ catch البحث القرآني لا يطفئ isSearching.
- التنفيذ المحدد: ثبّت query/filter في SavedStateHandle أو rememberSaveable المناسب؛ وحّد انتقال search error بحيث isSearching=false؛ اختبر clear بعد failure وسرعة الكتابة والإلغاء ومنع نتائج الطلب القديم. لا تعيد كتابة محركات البحث.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranViewModel.kt`.
- الاعتماديات: G0-02. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: ViewModel fake failure/cancellation/debounce latest + recreate query/filter + empty after clear.
- القبول: لا spinner دائم ولا نتائج قديمة مع query أحدث؛ إعادة إنشاء تحافظ على الاستعلام والفلتر.
- المخاطر: اختلاط state بين ViewModels ونتائج أقسام مستقلة. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### AR-02 — ذرية تقدم اليوم والقراءة

- الحالة الحالية/لماذا: edit للزوج atomically لكن القراءة بcombine لتدفقين؛ reset من observeToday منفصل عن increment.
- التنفيذ المحدد: انشر snapshot واحدًا لموضع القراءة وتاريخ/عدادات اليوم. اجعل rollover+mutation داخل edit واحد بشرط التاريخ وأدخل clock مشتركًا. أثبت race قبل تغيير الكود ولا تصفر سجلًا جديدًا استجابة لانبعاث قديم.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`؛ `feature/src/main/java/com/example/feature/dashboard/data/DailyActivityRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`.
- الاعتماديات: G0-02. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: concurrent increment/reset/midnight/timezone؛ لا pair هجين من lastRead؛ إعادة تشغيل مع قيم قديمة.
- القبول: عدم ضياع الزيادات أو انبعاث سورة/آية غير متطابقتين؛ preserve existing keys.
- المخاطر: سباق محتمل يحتاج إعادة إنتاج؛ اختلاف clock الاختباري. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### AR-03 — سلامة Favorites والاستعلامات

- الحالة الحالية/لماذا: observeAllBookmarks يستعلم مرتين لكل صف؛ toggle read-then-write.
- التنفيذ المحدد: أثبت N+1 والضغط المتزامن ثم استخدم JOIN projection لأسماء/نصوص bookmarks وmutation ذرية مطابقة لسلوك toggle المعتمد. افحص duplicates الحالية قبل أي UNIQUE/FK؛ لا تحذف orphan تلقائيًا.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/quran/data/local/dao/QuranDao.kt`؛ `feature/src/main/java/com/example/feature/quran/data/local/entity/BookmarkEntity.kt`؛ `feature/src/main/java/com/example/feature/hadith/data/repository/HadithRepositoryImpl.kt`.
- الاعتماديات: AR-02، G0-05. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: إضافة index/unique فقط بعد فحص البيانات وترحيل معتمد؛ JOIN وحده لا يحتاج schema.
- الاختبارات: query-count قبل/بعد + favorite toggle concurrency/idempotent save + legacy duplicate fixture.
- القبول: عدد استعلامات محدود مستقل عن N؛ حفظ IDs والمفضلات وعدم حذف بيانات غير مصنفة.
- المخاطر: معنى double tap وازدواج سابق. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### AR-04 — أخطاء التخزين والتهيئة والحدود

- الحالة الحالية/لماذا: DataStore تدفقات مباشرة؛ loader يمسك بعض أخطاء assets بصمت؛ Room.build لا يضمن فتح DB في try.
- التنفيذ المحدد: صمم استرداد IOException دون إخفاء corruption أو استخدام default ثم الكتابة فوقه. اجعل فشل التهيئة ظاهرًا مع retry، واحفظ CancellationException. راقب الفشل عند الاستعمال الفعلي لRoom لا وقت إنشاء builder فقط.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`؛ `feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt`؛ `app/src/main/java/com/example/mol/di/DatabaseModule.kt`؛ `app/src/main/java/com/example/mol/BarakahApp.kt`؛ `feature/src/main/java/com/example/feature/core/observability/AppObservability.kt`.
- الاعتماديات: G0-05. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: fault injection: asset missing/invalid JSON/Room open/IO/cancel؛ retry دون reseed مدمر.
- القبول: حالة خطأ قابلة للاستعادة؛ لا blank يبدو نجاحًا؛ السجلات لا تحتوي PII.
- المخاطر: طمس corruption وإعادة البذر. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### AR-05 — توثيق الملكية وتقليل الكود غير الموصول

- الحالة الحالية/لماذا: core feature module كبير؛ قواعد legacy ونماذج أهداف وصلاة غير موصولة.
- التنفيذ المحدد: ارسم dependency/DI graph واختبر resolution للـproduction roots. وثق active/legacy/foundation ولا تحذف legacy. اعزل orchestration الجديد بعقود صغيرة ضمن الوحدات الحالية. تحديث عقود الحذف متسق مع ownership الفعلية والتعليقات القديمة.
- البداية والملفات المتأثرة: `app/src/main/java/com/example/mol/di/AppModule.kt`؛ `app/src/main/java/com/example/mol/di/RepositoryModule.kt`؛ `app/src/main/java/com/example/mol/di/ViewModelModule.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/prayer/presentation/ManualPrayerTrackerViewModel.kt`؛ `feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt`.
- الاعتماديات: AR-04. الحجم: S؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: Koin graph + production route characterization + source-reference inventory.
- القبول: لا اعتماد جديد دائري ولا ViewModel/DAO غير قابل للحل يعلن جاهزًا؛ لا نقل جذري للوحدات.
- المخاطر: تعليق قديم يخالف التنفيذ ومكونات غير مستخدمة. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### PS-01 — قياس خط الأساس للأداء

- الحالة الحالية/لماذا: لا قياسات حالية معتمدة؛ توجد قراءة JSON وtext fitting وفهارس ذاكرة.
- التنفيذ المحدد: قِس startup cold/warm وframe times وsearch cold/warm وmemory وbattery وAPK وDB على هاتف Release. أعد توزيع التحسينات بعد trace فقط؛ أي إضافة dependency قياس تستلزم نطاقًا واضحًا.
- البداية والملفات المتأثرة: `app/src/main/java/com/example/mol/BarakahApp.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`؛ `feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt`؛ `feature/src/main/java/com/example/feature/hadith/data/repository/HadithRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt`.
- الاعتماديات: G0-03، AR-01. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: Perfetto/System Trace + Android Profiler + dumpsys meminfo + APK Analyzer؛ 10 عينات لكل سيناريو مع median/p95 والظروف.
- القبول: baseline قابل للإعادة وبصمة APK؛ أهداف عددية تعتمد بعد القياس لا نسب تحسن افتراضية.
- المخاطر: ضجيج القياس وthermal state. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### PS-02 — إصلاح عنق زجاجة واحد مثبت

- الحالة الحالية/لماذا: مرشحون فقط: parsing/fitting وإعادة normalization وذاكرة index.
- التنفيذ المحدد: اختر أسوأ bottleneck مثبت في PS-01. انقل parsing عن main/cache immutable edition أو precompute keys أو حد نطاق recomposition حسب الدليل. لا تغير JSON ولا النص ولا تحمل cache غير محدود.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`؛ `feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt`؛ `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`.
- الاعتماديات: PS-01، AR-03. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: repeat نفس baseline + content hash/ordering + cancellation + memory teardown.
- القبول: trace يثبت إزالة السبب؛ المقاييس ضمن budget المعتمد D-08؛ لا انحدار صحة أو ذاكرة.
- المخاطر: cache stale أو نقل عمل بلا مكسب مثبت. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### PS-03 — تدقيق WebView وIntents والملفات

- الحالة الحالية/لماذا: JS bridge محدود وfile/content off وmain frame allowlist؛ لا اختراق مثبت.
- التنفيذ المحدد: اختبر adversarial URLs وredirects وrenderer death وحجم/نوع URI الصور وshare grants وIntent failures. أصلح فقط failure مثبت؛ لا توسع hosts ولا تسمح cleartext أو raw path أو JS عند arbitrary origins.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt`؛ `feature/src/main/java/com/example/feature/learning/presentation/components/YouTubePlaybackBridge.kt`؛ `feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt`؛ `feature/src/main/java/com/example/feature/ehsan/data/image/EhsanImageStore.kt`؛ `app/src/main/AndroidManifest.xml`.
- الاعتماديات: G0-04، UX-04. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: unit allowlist/URI + instrumentation WebView/file grants/renderer + lint exported-components.
- القبول: لا وصول ملفات أو intent غير مصرح من محتوى خارجي؛ المصادر الرسمية تبقى قابلة للفتح.
- المخاطر: حدود الثقة داخل iframe/bridge. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### PS-04 — سياسة التوقيع والأذونات والبيانات

- الحالة الحالية/لماذا: release يعود إلى debug signing عند غياب المفاتيح؛ exact permission مزدوجة؛ backups ممنوعة.
- التنفيذ المحدد: بعد D-02 افصل قبول QA الداخلي عن نشر المتجر وافشل نشر production دون هوية صحيحة. راجع الحاجة لكل permission وmerged manifest وسياسة alarm المختارة. حدّث privacy disclosure وdeletion لكل سجل مستقبلي؛ لا تطبع secrets ولا تضف analytics.
- البداية والملفات المتأثرة: `app/build.gradle.kts`؛ `app/src/main/AndroidManifest.xml`؛ `app/src/main/res/xml/backup_rules.xml`؛ `app/src/main/res/xml/data_extraction_rules.xml`؛ `docs/RELEASE_SIGNING_GUIDE.md`؛ `docs/store/DATA_SAFETY_DISCLOSURE.md`.
- الاعتماديات: G0-02، PS-03. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: signingReport بلا أسرار + cert APK + merged manifest + backup exclusion assertions + PII redaction.
- القبول: لا Debug certificate في مسار production المعتمد؛ لا توسيع backup/permissions دون D-02/D-03/D-05.
- المخاطر: فقد قابلية التحديث إذا تغيرت الحزمة/الشهادة. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F4-01 — عقد خطة الختمة وحساب الصفحات

- الحالة الحالية/لماذا: QuranGoal foundation غير موصول؛ pagesToAyahs تقريب 10.3 وموضع القراءة ليس سجل إكمال.
- التنفيذ المحدد: طوّر calculator موجودًا لخطط 7/30/60/custom بتوزيع صفحات 604 الفعلية ونطاق بداية/نهاية. عرف اليوم الشامل والموعد وحدود skipped/reschedule وإكمال explicit مع اقتراح reader اختياري. لا تحوّل قفزة القراءة إلى إنجاز تلقائي.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`؛ `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`؛ `feature/src/main/assets/mushaf_page_starts.json`.
- الاعتماديات: AR-02، AR-05. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: 7/30/60، due today، overdue، partial page، ختمة جديدة، leap day، noncontiguous read.
- القبول: اتحاد daily ranges يساوي نطاق الخطة بلا نقص/تكرار؛ خطة 7 أيام توزع 604 إلى 86/87 صفحة؛ D-06 معتمد.
- المخاطر: خلط last-read بالإنجاز. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F4-02 — تخزين الختمة وترحيلها الآمن

- الحالة الحالية/لماذا: لا تخزين فعلي لـQuranGoal مثبت.
- التنفيذ المحدد: أضف plan/day completion repository وجداول بإصدارات ومعرف ثابت وtransactions للإكمال/replan. version التالي يستخرج وقت التنفيذ؛ لا تفترض 9 إذا تغير HEAD. لا تكتب تقدمًا من موضع القراءة القديم إلا بموافقة المستخدم.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt`؛ `feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt`؛ `app/src/main/java/com/example/mol/di/DatabaseModule.kt`.
- الاعتماديات: F4-01، G0-05. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: جداول khatma مقترحة؛ رقم الإصدار يحدد وقت التنفيذ.
- الاختبارات: migration all supported starts + reopen + interrupted transaction + unique day/range + cancel/reschedule.
- القبول: كل بيانات v8 تبقى؛ خطة واحدة فعالة بحسب D-06؛ استعادة الخطة دون تغيير bookmarks.
- المخاطر: تغيير schema وإعادة جدولة تفقد تاريخًا. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F4-03 — واجهة الختمة والإحصاء البسيط

- الحالة الحالية/لماذا: استكمال القراءة ونسبة موضع موجودان؛ واجهة خطة غير مثبتة.
- التنفيذ المحدد: أضف نقطة خطة ضمن القرآن ثم setup/progress/reschedule/confirm completion. اعرض المقترح مقابل المنجز صراحة، ولا تنشئ بطاقة رحلة ثانية. الشاشات الجديدة أسماء مقترحة تحدد بعد إعادة الجرد.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/quran/presentation/QuranListScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeQuranContinueCard.kt`؛ `app/src/main/java/com/example/mol/navigation/Screen.kt`؛ `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`.
- الاعتماديات: F4-02، UX-03. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: VM + Compose setup/edit/replan/rotate/offline + font/RTL؛ قرآن integrity.
- القبول: تقدم وحفظ محلي وحساب الصفحات واضح؛ reader الحالي والهوية لا يتغيران.
- المخاطر: ازدواج مؤشر الختمة مع موضع القراءة. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F4-04 — وصل سجل الصلاة الموجود

- الحالة الحالية/لماذا: Entity وDAO وVM وcard موجودة دون Room/DI أو عرض في PrayerScreen.
- التنفيذ المحدد: استكمل foundation نفسه بجداول/DAO/bindings وواجهة الصلاة. أصلح day rollover وwrite errors وtap serialization؛ لا تسجل Sunrise أو أداء الصلاة آليًا. أضف حذف السجل وفق سياسة الخصوصية المعتمدة.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/prayer/data/local/entity/PrayerLogEntity.kt`؛ `feature/src/main/java/com/example/feature/prayer/data/local/dao/PrayerLogDao.kt`؛ `feature/src/main/java/com/example/feature/prayer/data/repository/ManualPrayerLogRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/prayer/presentation/ManualPrayerTrackerViewModel.kt`؛ `feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `app/src/main/java/com/example/mol/di/ViewModelModule.kt`.
- الاعتماديات: AR-05، G0-05. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: PrayerLogEntity داخل schema التالي الآمن.
- الاختبارات: ManualPrayerLogRepositoryTest + DI + migration + duplicate rapid taps + midnight + private deletion.
- القبول: خمس صلوات فقط؛ performed/late يدويان؛ error لا يبدو حفظًا؛ neutral unrecorded؛ D-03 معتمد.
- المخاطر: بيانات عبادة حساسة. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F4-05 — تقويم سجل الصلاة والملخصات

- الحالة الحالية/لماذا: statistics النشط للأذكار؛ Home يعرض شرطة للصلاة عمدًا؛ calculator صلاة foundation.
- التنفيذ المحدد: وسّع repository والواجهة لليوم/آخر7/شهر مع اختيار يوم وتعديل يدوي. اربط بطاقة Home بالمصدر نفسه، واعرض unrecorded مستقلًا عن not performed؛ لا رسائل لوم أو حكم.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/statistics/domain/PrayerTrackerStatsCalculator.kt`؛ `feature/src/main/java/com/example/feature/statistics/data/StatisticsRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/statistics/StatisticsScreen.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeDailyJourneyCard.kt`.
- الاعتماديات: F4-04، UX-01. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: range boundaries/zero records/month roll/leap + VM/error + Compose calendar/history/Home parity.
- القبول: لا تضارب counters؛ unrecorded لا يصنف تقصيرًا؛ قراءة تاريخية خاصة ومحلية.
- المخاطر: استنتاج سلوك ديني من غياب الإدخال. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F4-06 — إطلاق Widgets من العقد الموجود

- الحالة الحالية/لماذا: PrayerWidgetStateFactory فقط؛ لا AppWidgetProvider/receiver metadata فعلي.
- التنفيذ المحدد: بعد D-07 اختر RemoteViews أو Glance بمبرر كلفة؛ أنشئ widget provider/config/resources للحالة القادمة واستكمال القرآن، واستخدم canonical calculator/NextPrayerSelector. لا ticker ثانية بالخلفية؛ اعرض last-updated/stale وحدود دقة العد. حدّث على settings/location/timezone/date ومع transition مناسب.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/prayer/widget/PrayerWidgetState.kt`؛ `feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt`؛ `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`؛ `app/src/main/AndroidManifest.xml`؛ `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`.
- الاعتماديات: AR-02، AR-05، PS-01، PS-04، G0-06. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا schema مطلوب؛ لا snapshot يخزن PII زائد.
- الاختبارات: factory + PendingIntent immutable identity + no-location + resize + launcher/reboot بعد الإذن + resume Quran target.
- القبول: Widget من launcher يعمل offline ويتطابق مع الصلاة؛ لا نشاط foreground خفي أو حساب مستقل.
- المخاطر: تحديثات launcher وقيود البطارية. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F5-01 — تطوير البحث العربي الموجود

- الحالة الحالية/لماذا: تطبيع عربي وفلاتر ونتائج sources موجودة؛ تجميع UI بسبعة VMs.
- التنفيذ المحدد: قِس ثم أضف ranking/category/error-per-source وتحسين empty للمحرك الحالي. precompute keys للقوائم الثابتة إذا برهن القياس. FTS مشروط بمقارنة query plan وحجم لا قرار تلقائي. لا تستبدل النص الأصلي بالمطبع.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/core/search/ArabicSearch.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt`؛ `feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/hadith/presentation/HadithViewModel.kt`.
- الاعتماديات: AR-01، PS-01. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: ArabicSearchTest/QuranSearchTest/DuaSearchRegressionTest + stale-query/source error + cold/warm p95.
- القبول: بحث في السور/الحديث/الأذكار يحافظ على المصدر والترتيب المقبول وميزانية D-08.
- المخاطر: ترتيب النتائج والذاكرة؛ index يتقادم. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F5-02 — محرك التقويم الهجري والتعديل المحلي

- الحالة الحالية/لماذا: HijrahDate.now نص فقط؛ API25 يرجع اليوم؛ لا interactive calendar.
- التنفيذ المحدد: أدخل Clock/Zone وخدمة HijriDateProvider مشتركة، مع offset محلي محدود حسب D-09 وتسمية حسابي/محلي. افصل اليوم المدني عن بداية اليوم الشرعي التي تحتاج قرارًا؛ لا تؤثر على مواقيت الصلاة.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/core/util/HijriDateFormatter.kt`؛ `feature/src/main/java/com/example/feature/dashboard/presentation/DashboardDatePolicy.kt`؛ `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`.
- الاعتماديات: AR-02، UX-02. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: boundaries/offset +/-/DST/API25 fallback/unit conversion + consistency Home/Profile/Prayer.
- القبول: تاريخ واحد لكل مواضع العرض وإفصاح الرؤية؛ لا event ديني بلا مصدر.
- المخاطر: اختلاف الحساب والرؤية المحلية. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F5-03 — واجهة التقويم والأحداث الموثقة

- الحالة الحالية/لماذا: لا route تقويم تفاعلي ضمن 58.
- التنفيذ المحدد: أضف شهر/يوم والتنقل لليوم الحالي وإعداد offset؛ assets للأحداث فقط بعد D-09 بمصدر وإصدار ومراجعة. أبق العرض خاليًا إن لم يعتمد المحتوى؛ لا تخترع مناسبة.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/core/util/HijriDateFormatter.kt`؛ `feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt`؛ `app/src/main/java/com/example/mol/navigation/Screen.kt`؛ `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`.
- الاعتماديات: F5-02. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: Compose month navigation/RTL/font + event source + timezone restore.
- القبول: واجهة تعمل offline وتعكس offset وتفصح الاحتمال الحسابي دون ادعاء رؤية رسمية.
- المخاطر: صحة المحتوى والتواريخ. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P1. التبعيات المشروطة: لا توجد
### F5-04 — عقد الحفظ والمراجعة وتخزينهما

- الحالة الحالية/لماذا: repeat audio موجود/foundation؛ لا سجل مراجعة متباعدة موصول.
- التنفيذ المحدد: عرّف نطاق آيات محفوظة وmanual self-check وdue dates deterministic. أضف تخزين review events مع source verse keys وschema التالي. الإخفاء عرض فقط ولا يحذف/يعدل نصًا. لا صوت AI.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/quran/domain/model/QuranRepeat.kt`؛ `feature/src/main/java/com/example/feature/quran/domain/model/QuranNavigation.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranRepeatSheet.kt`.
- الاعتماديات: AR-05، G0-05، UX-03. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: جداول memorization/review مقترحة.
- الاختبارات: spaced review deterministic + same-day repeat/overdue/clock + migration/orphan references.
- القبول: جدول مراجعة خاص قابل للتعديل؛ لا تقييم ديني ولا تنزيل صوت لتفعيل الحفظ.
- المخاطر: فقد الارتباط بالآيات. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P2. التبعيات المشروطة: لا توجد
### F5-05 — واجهة الاختبار الذاتي اليدوي

- الحالة الحالية/لماذا: لا UI حفظ مثبتة؛ أفعال الآية موجودة.
- التنفيذ المحدد: أضف اختيار نطاق وإخفاء/كشف واختبار ذاتي وتسجيل النتيجة باليد؛ حافظ على صفحة المصحف الحالية وأضف وضعًا اختياريًا منفصلًا لعرض الاختبار. لا ألوان توحي بحذف الآيات أو تغيير النص.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt`؛ `app/src/main/java/com/example/mol/navigation/Screen.kt`.
- الاعتماديات: F5-04. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: Compose select/hide/reveal/back/TalkBack + offline restore + full Quran hash.
- القبول: الآيات تكشف كاملة بالترتيب؛ مراجعة due تعمل دون شبكة؛ لا امتداد صوتي ضمن الدفعة.
- المخاطر: تداخل gestures وتحديد الآيات. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P2. التبعيات المشروطة: لا توجد
### F5-06 — ملاحظات الآيات الخاصة وتخزينها

- الحالة الحالية/لماذا: tafsir محجوب مصدرًا؛ لا notes repository مثبت.
- التنفيذ المحدد: أضف note CRUD/search برابط surah/ayah، كاتب محلي وتواريخ. افصل عنوان ملاحظة شخصية عن تفسير معتمد؛ لا تستعمل الملاحظة لتعويض tafsir. حفظ transaction وdraft error واضح.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/TafsirBottomSheet.kt`؛ `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`.
- الاعتماديات: UX-03، AR-05، G0-05، PS-04. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: جدول ayah_notes مقترح؛ فهرس verse key.
- الاختبارات: migration + duplicate/long text/delete/cancel/recreate + Arabic note search + privacy deletion.
- القبول: لا تعديل للآيات ولا التفسير؛ ملاحظة لا تظهر كقول ديني؛ D-03 يحدد الحذف.
- المخاطر: محتوى شخصي حساس. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P2. التبعيات المشروطة: لا توجد
### F5-07 — تصدير اختياري آمن للملاحظات

- الحالة الحالية/لماذا: backup عام ممنوع ولا export notes موجود.
- التنفيذ المحدد: بعد D-05 أضف export محدد النطاق عبر SAF يختاره المستخدم مع preview وتحذير ما سيخرج. لا توسع cloud backup. نسخة أولى للتصدير فقط؛ restore شامل مشروع آخر يحتاج عقد version/validation/conflicts.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/ehsan/data/image/EhsanImageStore.kt`؛ `app/src/main/AndroidManifest.xml`؛ `app/src/main/res/xml/backup_rules.xml`.
- الاعتماديات: F5-06. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: UTF-8/Arabic/empty/cancel/IO failure/oversize + exported content allowlist.
- القبول: لا أسماء/هاتف/موقع/سجل عبادة ضمن export notes دون اختيار؛ لا path traversal أو محتوى زائد.
- المخاطر: تسريب بيانات عبر التصدير. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P2. التبعيات المشروطة: لا توجد
### F6-01 — متابعة الصيام المحلية

- الحالة الحالية/لماذا: لا سجل صيام موصول ضمن مصادر الإنتاج.
- التنفيذ المحدد: بعد D-03/D-09 أضف manual log/date/type مع CRUD وملخص خاص. لا تحسب قضاءً أو فدية أو حكمًا آليًا ولا mark automatic. اختياريًا اربط يوم calendar بلا inference.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`؛ `feature/src/main/java/com/example/feature/statistics/StatisticsScreen.kt`؛ `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`.
- الاعتماديات: F5-03، F4-05. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: جدول fasting_log مقترح.
- الاختبارات: migration + duplicate date/undo/month/offset + privacy delete + offline VM.
- القبول: حفظ يدوي وخصوصية ومصدر مناسبات؛ لا أحكام فقهية أو لوم.
- المخاطر: حساسية البيانات وحدود الفقه. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P2. التبعيات المشروطة: لا توجد
### F6-02 — مركز رمضان يربط الموجود

- الحالة الحالية/لماذا: لا مركز موحد؛ الصلاة والرحلة والقرآن موجودة.
- التنفيذ المحدد: اجمع روابط الختمة والصيام والتقويم؛ الفجر/المغرب من facade مع تسمية حدود السحور/الإفطار المعتمدة D-09؛ لا calculator أو counters جديدة. قواعد إبراز الموسم قابلة للإيقاف.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt`؛ `feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt`؛ `feature/src/main/java/com/example/feature/quran/presentation/QuranListScreen.kt`؛ `app/src/main/java/com/example/mol/navigation/Screen.kt`.
- الاعتماديات: F6-01، F4-03. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: parity Fajr/Maghrib+timezone/location + no-location + seasonal entry + offline navigation.
- القبول: كل رقم من مصدره الأصلي؛ لا يدعي موعدًا معتمدًا محليًا إن كان حسابيًا.
- المخاطر: تفسير التوقيت الشرعي وازدواج البيانات. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P2. التبعيات المشروطة: لا توجد
### F6-03 — تطوير مسار التعلم الموجود

- الحالة الحالية/لماذا: favorites/watch later وFahm completion موجودة؛ stations بلا episode links وبعض recipes غير متاحة.
- التنفيذ المحدد: وحّد تقدمًا يدويًا/مشاهدة موضحة دون ازدواج library. أضف ترتيب playlist وتنقل واستكمال اختياري؛ ربط المحطات والعناوين فقط ببيانات معتمدة. احتفظ keys الحالية أو migrate بإصدارات.
- البداية والملفات المتأثرة: `feature/src/main/java/com/example/feature/learning/data/LearningBookmarkStore.kt`؛ `feature/src/main/java/com/example/feature/fahmanallah/data/repository/FahmRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmJourneyScreen.kt`؛ `feature/src/main/java/com/example/feature/sanhya/data/repository/SanhyaRepositoryImpl.kt`؛ `feature/src/main/java/com/example/feature/nabiihsan/data/repository/NabiIhsanRepositoryImpl.kt`.
- الاعتماديات: UX-05، AR-05. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: existing repository persistence + completion/reopen/reorder/missing episode/no network + migration keys.
- القبول: لا فيديو ينزل خارج إذن المصدر؛ لا محطة/وصفة/ربط ديني مولد؛ التاريخ لا يضيع.
- المخاطر: حقوق المحتوى وتغيير معرفات library. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P2. التبعيات المشروطة: لا توجد
### F6-04 — قبول الميزات المتكامل

- الحالة الحالية/لماذا: المزايا المستقبلية ستزيد routes/schema؛ 58/59 baseline لا يكفي لها.
- التنفيذ المحدد: حدّث matrix من routes الجديدة وقبول رحلات plan→reader→manual completion وprayer→history→widget وcalendar→fasting→Ramadan وnotes/export؛ لا تعيد تسمية المزايا القديمة.
- البداية والملفات المتأثرة: `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`؛ `app/src/androidTest/java/com/example/mol/ui/ReleaseFunctionalJourneyTest.kt`؛ `app/src/androidTest/java/com/example/mol/ui/ReleaseCorePersistenceTest.kt`.
- الاعتماديات: F6-02، F6-03، F5-07. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: offline-first E2E + migration old/current/new + process restore + font/RTL + security+baseline performance.
- القبول: جميع routes الجديدة مسجلة وأدلة same APK؛ المستقبل لا يعيد فتح تعريف Beta القديم تلقائيًا.
- المخاطر: تفاعل الميزات والترحيلات المتسلسلة. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P2. التبعيات المشروطة: لا توجد
### R7-01 — دراسة الميزات المشروطة دون تشغيل خدمات

- الحالة الحالية/لماذا: لا موافقة تنفيذ صوت AI أو مجتمع حقيقي أو تبرعات.
- التنفيذ المحدد: أنشئ decision briefs مستقلة للصوت/مساعد القرآن/الحج والعمرة/المجتمع/التبرعات. حدد accuracy/evaluation/privacy/retention/provider license/moderation/payment/legal owner/cost. وثائق فقط؛ لا API key ولا backend ولا dataset مرفوع.
- البداية والملفات المتأثرة: `docs/release/FUTURE_BACKEND_CONTRACTS.md`؛ `docs/release/EXTERNAL_BLOCKERS_DECISIONS.md`؛ `docs/store/PRIVACY_POLICY.md`.
- الاعتماديات: PS-04. الحجم: M؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: مراجعة completeness والقرارات ومعايير go/no-go؛ لا اختبار خدمات حقيقية.
- القبول: كل ميزة مشروطة تبقى Deferred إلى موافقة مستقلة؛ الحج يحتاج مراجع معتمدة.
- المخاطر: حقوق وصحة وخصوصية وتشغيل خارجي. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P3. التبعيات المشروطة: لا توجد
### R7-02 — إغلاق حقوق المحتوى وهوية النشر

- الحالة الحالية/لماذا: recordings/HLS/رسومات/أذان/محتوى تحريري لها حالات معلقة؛ هوية QA ليست متجرًا.
- التنفيذ المحدد: طابق asset hash بمستند حقوق فعلي ونطاق territory/distribution/attribution؛ سجل إزالة/استبدال مقترح فقط إذا لم يوافق المالك. ثبّت package/cert وتحديث البيانات مع D-02. لا تراسل أصحاب الحقوق دون إذن منفصل.
- البداية والملفات المتأثرة: `docs/release/EXTERNAL_ASSET_REGISTER.csv`؛ `docs/release/CONTENT_SOURCES_AND_LICENSES.md`؛ `docs/release/ASSET_CREDITS.md`؛ `docs/PRODUCTION_IDENTITY_DECISION.md`؛ `docs/RELEASE_SIGNING_GUIDE.md`.
- الاعتماديات: G0-01، PS-04. الحجم: L؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: asset-to-license coverage + missing-license blocker + cert/appId/update fixture.
- القبول: لا blanket approved بسبب HTTP؛ D-01/D-02/D-10 وحواجز النشر موثقة؛ المنتج محدود داخليًا إن لم تغلق.
- المخاطر: ترخيص خارجي وتوقيع غير قابل للتبديل بلا أثر. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.


- أولوية التنفيذ: P0. التبعيات المشروطة: لا توجد
### R7-03 — قرار الإصدار النهائي المستقل

- الحالة الحالية/لماذا: لا Release Accepted يثبت كل أبعاد الحالة الحالية.
- التنفيذ المحدد: اجمع build/unit/device/privacy/content/performance على artifact واحد. أصدر تقرير قبول/رفض محدد التوزيع. Beta الحالي يعتمد G0 والإصلاحات الحرجة فقط؛ إصدار الميزات يعتمد F6-04، والمتجر يعتمد R7-02؛ لا تنشر أو ترفع تلقائيًا.
- البداية والملفات المتأثرة: `docs/planning/IHSAN_QA_AND_RELEASE_GATES.md`؛ `docs/release/BETA_ACCEPTANCE_MATRIX.md`؛ `docs/qa/FINAL_BETA_GAP_REPORT.md`.
- الاعتماديات: G0-01، G0-02، G0-03، G0-04، G0-05، G0-06. الحجم: S؛ الثقة متوسطة ما لم يحجبها قرار/هاتف.
- البيانات: لا تغيير Schema.
- الاختبارات: signed artifact hash + full regression matrix + zero critical open + explicit owner acceptance.
- القبول: التفريق بين Code Implemented/Build/Unit/Device/Release محفوظ؛ لا نشر قبل قرار المالك.
- المخاطر: قبول أوسع من نطاق الدليل. الاسترداد: فرق موضعي أو forward fix للتخزين؛ لا downgrade أو wipe.




- أولوية التنفيذ: P0. التبعيات المشروطة: F6-04 فقط لمسار Feature-Release؛ R7-02 فقط عند اختيار النشر العام. Beta-Current لا يعتمد أي ميزة مستقبلية أوكل تحسيناتPhase1؛ يعالج فقط عيوبه المثبتة.
## قيد Variant في جرد المسارات

Screen يعرّف58 route، والمصدر يحتوي58 موضع composable. لكن IhsanPlusDaily يسجل فقط عند dailyEnabled: Debug=true وRelease=false في feature/build.gradle.kts، لذلك graph الإنتاج Release57 وDebug58. قائمة الاختبار59 تحتوي controlled_daily، فتتطلب58 حالة إيجابية في Release واختبار غياب route/entry للحالة controlled؛ لا تفعّل flag لتزييف المرور. Splash وLegacy خارج الـ59 ويختبران منفصلين. CSV يميز declared route عن registered route للوسائط الاختيارية nameId/hadithId/zikrId.

## سجل الاتساق المرجعي للمهام — مراجعة نهائية

أولوية المهمة ترتيب تنفيذ وليست شدة عيب. هذا الجدول مرجع IDs/dependencies/priority؛ أوامرGemini مطابقة له. لا توجد مهام جديدة؛ العدد40. عزل اعتمادWidget عنسجل الصلاة، والحفظ عنالختمة،والملاحظات عنالحفظ،ومركزرمضان عنWidget،لعدم وجوداعتمادوظيفي مثبت؛ تبقىترحيلاتschema متسلسلة علىالإصدارالفعلي دونتعارض.

| Task ID | Phase | Task Priority | Mandatory Dependencies | Conditional Dependencies |
|---|---|---|---|---|
|G0-01|0|P0|اعتماد المراجعة النهائية|لا توجد|
|G0-02|0|P0|G0-01|لا توجد|
|G0-03|0|P0|G0-02|لا توجد|
|G0-04|0|P0|G0-02|لا توجد|
|G0-05|0|P0|G0-02|لا توجد|
|G0-06|0|P0|G0-02|لا توجد|
|UX-01|1|P1|G0-03|لا توجد|
|UX-02|1|P1|UX-01|لا توجد|
|UX-03|1|P1|UX-01|لا توجد|
|UX-04|1|P1|UX-01|لا توجد|
|UX-05|1|P1|G0-04، UX-01|لا توجد|
|AR-01|2|P1|G0-02|لا توجد|
|AR-02|2|P1|G0-02|لا توجد|
|AR-03|2|P1|AR-02، G0-05|لا توجد|
|AR-04|2|P1|G0-05|لا توجد|
|AR-05|2|P1|AR-04|لا توجد|
|PS-01|3|P1|G0-03، AR-01|لا توجد|
|PS-02|3|P1|PS-01، AR-03|لا توجد|
|PS-03|3|P1|G0-04، UX-04|لا توجد|
|PS-04|3|P1|G0-02، PS-03|لا توجد|
|F4-01|4|P1|AR-02، AR-05|لا توجد|
|F4-02|4|P1|F4-01، G0-05|لا توجد|
|F4-03|4|P1|F4-02، UX-03|لا توجد|
|F4-04|4|P1|AR-05، G0-05|لا توجد|
|F4-05|4|P1|F4-04، UX-01|لا توجد|
|F4-06|4|P1|AR-02، AR-05، PS-01، PS-04، G0-06|لا توجد|
|F5-01|5|P1|AR-01، PS-01|لا توجد|
|F5-02|5|P1|AR-02، UX-02|لا توجد|
|F5-03|5|P1|F5-02|لا توجد|
|F5-04|5|P2|AR-05، G0-05، UX-03|لا توجد|
|F5-05|5|P2|F5-04|لا توجد|
|F5-06|5|P2|UX-03، AR-05، G0-05، PS-04|لا توجد|
|F5-07|5|P2|F5-06|لا توجد|
|F6-01|6|P2|F5-03، F4-05|لا توجد|
|F6-02|6|P2|F6-01، F4-03|لا توجد|
|F6-03|6|P2|UX-05، AR-05|لا توجد|
|F6-04|6|P2|F6-02، F6-03، F5-07|لا توجد|
|R7-01|7|P3|PS-04|لا توجد|
|R7-02|7|P0|G0-01، PS-04|لا توجد|
|R7-03|7|P0|G0-01، G0-02، G0-03، G0-04، G0-05، G0-06|F6-04 فقط لمسار Feature-Release؛ R7-02 فقط عند اختيار النشر العام. Beta-Current لا يعتمد أي ميزة مستقبلية أوكل تحسيناتPhase1؛ يعالج فقط عيوبه المثبتة.|

### ربط الإصلاحات الأصلية دون تكرار المهام

| Acceptance ID | الحالة الأصلية | Gate 0 | Phase 1 |
|---|---|---|---|
|REC-01|تداخل Status Bar أثناء التمرير|G0-01، G0-03|UX-01|
|REC-02|عداد الأذكار RTL|G0-01، G0-03|UX-03|
|REC-03|زر التلاوات غير المرخصة|G0-01، G0-03|UX-03|
|REC-04|تنظيف عناوين نبي الإحسان|G0-01، G0-03|UX-05|
|REC-05|إخفاء الأوصاف غير المتوفرة|G0-01، G0-03|UX-05|
|REC-06|الشارات الفارغة في سنحيا بالقرآن|G0-01، G0-03|UX-05|

REC IDs معايير قبول وليست Task IDs. التفاصيل والـsource symbols والأدلة المستقلة فيQA؛ المصفوفة61 صفًا تحمل original_fix_ids. اعتماد تقريرالفحص يسمح بإصلاحنتائجه دون اعتباره Product Pass،وبذلك لا دورة G0-03→UX→G0-03. مسارBeta-Current يصل إلىR7-03 دونF4/F5/F6؛ F6-04 تبعيةشرطيةلإصدارالميزاتفقط. لا تنتظرBetaكل Phase1،بل الإصلاحاتالمثبتةالمطلوبةفقط.
