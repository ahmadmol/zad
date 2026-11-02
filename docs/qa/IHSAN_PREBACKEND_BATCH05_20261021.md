# إحسان — Pre-backend Batch 05

2026-10-21 — Asia/Damascus. متابعة مباشرة لشجرة Batch01–04؛ لا إعادة تخطيط أو إعادة تنفيذ للوظائف المكتملة.

## المنفذ ومعرفات المهام

| المهام | التنفيذ البرمجي الفعلي | القبول الباقي |
|---|---|---|
| UX-01 / UX-04 | زر النموذج المشترك يتوسع مع النص وmin48dp بدل44dp؛ أفعال رأس المجتمع48dp؛ إزالة جرس بلا إجراء عندما لا توجد وظيفة متصلة | قياس الحدود/القص/الخط200/Light-Dark/IME على الهاتف |
| UX-02 | عزل رقم الهاتف LTR داخل بطاقة RTL؛ نصوص Widget في resources؛ الحفاظ على النصوص والمظهر الحاليين | النصوص المتبقية وD04 للإنجليزية الكاملة؛ فحص bidi والتباين الميداني |
| UX-03 / AR-01 | قراءة/كتابة/Retry فعلية للأحاديث والأدعية والأذكار والأسماء والسبحة؛ حفظ query/filter؛ أخطاء المصادر العامة مستقلة؛ نتائج البحث لا تُعرض مع query أحدث | فحص المصدر/الفراغ/الخط/TalkBack/إعادة إنشاء العملية والتنقل على نفس APK |
| AR-02 / AR-03 | التاريخ داخل edit ذري؛ stale ensure-date لا يمحو العد الجديد؛ مفتاح سورة/آية واحد؛ toggle المفضلة للأدعية والحديث SQL واحد، وللأسماء edit واحد مع mutex | Room concurrency الفعلي وmidnight/timezone على Android |
| AR-04 / AR-05 | فشل الأصول والتخزين ظاهر؛ التحقق قبل كتابة القرآن على IO داخل transaction؛ Retry يعيد اشتراك الرئيسية والإعدادات والإحصائيات؛ clock الرئيسية مرتبط بالاشتراك؛ أخطاء facade تصبح unavailable وتُعاد المراقبة يدويًا | runtime DI/Room open/corruption/الاسترداد على الجهاز؛ لا reset أو claims من البناء فقط |
| PS-01 / PS-02 / F5-01 | قياس10 عينات على3582 حديثًا؛ تجهيز مفاتيح محدود4096 صفًا، خارج main، بنفس ArabicSearch والنتائج والترتيب؛ إلغاء الطلبات القديمة وإزالة scan مكرر في Compose | لا baseline هاتف أو ميزانية D08؛ startup/frames/memory/battery وRelease traces باقية |
| PS-03 / UX-05 / F6-03 | إلغاء callbacks المشغل عند dispose/renderer loss؛ external navigation من WebView يحتاج gesture وHTTPS موثوقًا؛ تنقيح tokens/URIs وإسقاط رسائل/stacktrace الاستثناء من السجل التشغيلي | قبول المشغل والشبكة وfullscreen/background/renderer/Intents على الهاتف؛ المحتوى غير المتاح يبقى دون اختلاق |
| D-07 / F4-06 | RemoteViews مفعّل باستخدام الـProvider والـlayout والـfactory الموجودين، من PrayerTimesFacade؛ صلاة قادمة/وقت/عد تقريبي وآخر تحديث، مع فتح الصلاة ومتابعة القراءة | placement/resize/RTL/theme/font/Doze/boot/settings/zone/parity/actions على Xiaomi |

الجدول يصف أعمالًا منجزة في المصدر؛ لا يُغلق تلقائيًا كامل UX/AR/PS أو70 حالة بمجرد نجاح JVM. سجل الأربعين مهمة يحتفظ بمعرفاته وحالات القبول المنفصلة.

## Widget المعتمد D-07

- المستخدم اعتمد RemoteViews دون مكتبات جديدة في هذه الدفعة، ووُثّق D07 APPROVED في سجل القرارات. استُكمل `PrayerWidgetProvider` الموجود بدل إنشاء Provider أو حاسبة ثانية، واستُعمل `widget_prayer` و`prayer_widget_info` الموجودان.
- اليوم والصلاة التالية من facade/factory الحاليين، مع حواجز التاريخ والمنطقة والموقع وانتهاء اللحظة. لا طلب موقع أو شبكة من Widget، ولا تعديل للهجري أو خوارزميات الصلاة أو جدول تنبيهاتها.
- عند الشروق تُستخدم تسمية «الموعد القادم» احترامًا لنتيجة selector الحالي دون اختراع صلاة أو تغيير الخوارزمية. بقية الأوقات تحمل أسماء facade نفسها.
- العد native Chronometer، مع إفصاح بأنه تقريبي وقد يتأخر تحديث الصلاة التالية، وآخر وقت تحديث. تحديث منفصل inexact عند حد الصلاة أو منتصف الليل المدني، ويعاد عند الإعدادات/الموقع/المنطقة/التاريخ/الوقت/اللغة/الإقلاع/تحديث التطبيق. لا ticker حساب ثانية؛ تجاهل remainingMillis المتغير كل ثانية في اشتراك Widget.
- goAsync محدود بانتظار محلي6 ثوانٍ، ثم تحديث أو unavailable؛ لا foreground service. إلغاء اشتراك Widget ومنبهه عند إزالة آخر نسخة. منشئ facade الموجود يحتفظ بعمره الأصلي في التطبيق؛ لا محرك صلاة أو alarm ledger مكرر.
- PendingIntents صريحة immutable بأكواد منفصلة، والـreceiver exported=false. النقر لا يقبل route أو URI من مدخل خارجي. يفتح الصلاة، أو أحدث موضع قرآن صالح من مخزنه، وإلا قائمة القرآن. ينتظر اكتمال Splash/Onboarding، ويستهلك الطلب حتى لا يُعاد عند تدوير الشاشة.
- لا عرض لسجل الصلاة أو الصيام أو الحفظ أو اسم الملف أو أي تقدم شخصي. زر المتابعة ينفذ الانتقال عند النقر فقط ولا يكتب إنجازًا.
- المصفوفة **70 حالة**: الـ69 السابقة محفوظة، وUI-070 سطح launcher للـWidget؛ لا إنشاء شاشة قرآن/صلاة ثانية.

## البيانات والهندسة

- **Room 11 دون schema/migration جديدة**. مخططات9/10/11 والترحيلات التاريخية باقية مطابقة لبداية الدفعة؛ لا destructive fallback أو downgrade أو حذف صفوف قديمة.
- QuranAssetLoader يقرأ الأصلين الحاليين على IO، ويفحص السور/مفاتيح الآيات/الأعداد قبل transaction واحدة. غياب/فساد الأصل أو فشل القاعدة لا يتحول إلى نجاح فارغ؛ warm load لا يعيد parsing بعد نجاح التحميل. نصوص القرآن والأحاديث والأدعية والأسماء وأصول التعلم لم تُعدّل.
- مفاتيح DataStore الحالية محفوظة. Clock اختباري اختياري؛ الإنتاج يستعمل المنطقة المدنية الحالية. حساب اليوم داخل edit، وensure-date القديم لا يصفر يومًا سجل حديثًا. اختبار حقيقي concurrent increments/atomic pairs/zone/reopen وIDs غير معروفة.
- حفظ/تحميل الأسماء تحت mutex، وtoggle ID واحد ذري يحفظ بقية IDs. مفضلات الحديث/الدعاء UPDATE ذري، متسلسل مع seed lock؛ لا كتابة نسخة صف قديمة فوق مفضلة جديدة. اختبار SQL اصطناعي يثبت even/odd toggles وحفظ كل الأعمدة؛ التزامن الفعلي Room مغطى باختبار Android مجمّع فقط.
- الرئيسية تعيد الاشتراك في المصادر عند Refresh وتوقف clock بعد انتهاء الاشتراك. Settings/Statistics تعرضان خطأ وإعادة محاولة فعلية، دون مسح أو إخفاء corruption بكتابة defaults. لا بيانات شخصية في رسائل الاستثناء المعروضة أو تشغيل reset عند الفشل.
- facade لا يختلق أوقاتًا عند فشل قراءة محلية؛ تصبح البيانات unavailable وتستأنف المراقبة في retry/update اليدوي. لم تُعدّل AdhanPrayerCalculator أو ReconcilePrayerScheduleUseCase أو بوابة تنبيهات الصلاة.
- أعمال Batch03/04 للتعلم والحفظ والصيام والمجتمع والصور باقية؛ لا استنتاج إنجاز من فتح صفحة أو تشغيل فيديو، ولا توصيل Backend أو محطات/وصفات غير معتمدة.

## قياس الأداء

على host JVM21.0.11: median البحث الأصلي **149.9675ms** مقابل **4.5506ms** بعد التجهيز؛ p95 **161.8696ms → 7.1105ms**. تكلفة التجهيز البارد **399.1727ms**. تطابق النتائج والترتيب PASS؛ القياس مؤرشف في `search-measurement-full.json`.

المقارنة تقيس وظيفة البحث على host JVM فقط، مع10 عينات و3582 سجلًا من الأصل نفسه وSHA في JSON. تكلفة تجهيز أول query مذكورة منفصلة؛ لا ادعاء تحسن cold startup أو frames أو ذاكرة/بطارية الهاتف. لا threshold زمني هش في الاختبارات ولا budget مفترض. PS01/PS02 يبقيان PARTIAL حتى القياس الميداني واعتماد D08.

## التحقق

- المستهدف أثناء التطوير: **65 PASS** (app2 + feature63)، failures/errors/skipped=0؛ ملفات JUnit مؤرشفة. الإجمالي الفريد يثبت من المجموعة الشاملة ولا يُحسب بجمع التشغيلات.
- **Full JVM مرة واحدة: 619 PASS**، failures/errors/skipped=0. مهام الاختبار الثلاث أُجبرت على التنفيذ بـ`--rerun`، ولم تعد المجموعة الشاملة.
- بعد Full أُصلح حجب نتائج الحديث القديمة خلال انتظار query الجديد، وطُوبقت مفاتيح Widget مع العربية وأزيلت موارد العداد القديم المستبدلة، وأُخفي العداد في العرض الأولي حتى تصل بيانات موثوقة. أُعيد الانحدار المستهدف والبناء من المصدر النهائي: **65 PASS بعد إصلاح query/resources؛ آخر تعديل XML فقط لإخفاء العداد الأولي تحقق منه Lint وبناء Release النهائي، دون إعادة Full JVM**. سجلات البناء الوسيط وإصلاح Lint محفوظة. لا حالات فريدة إضافية فوق619.
- SQLite المعزول: مسارات2/3/5/6/7/8/9/10→11 و11→11، حفظ كل الجداول والحقول وintegrity/reopen وعزل مسح الصيام: PASS. SQL المفضلات الفعلي والـprojection يحفظان بقية الأعمدة وorphan/duplicate bookmark rows: PASS. ليست قاعدة هاتف أو قبول Android.
- اختبارات Android للمشروع/الهجرة/مفضلة القرآن والأدعية والحديث والصور: **PASS لـ`:feature:compileDebugAndroidTestKotlin` و`:app:compileDebugAndroidTestKotlin`**؛ تجميع فقط. لا Emulator/AVD أوADB أو install أو قاعدة هاتف.
- Android Lint: **0 أخطاء / 114 تحذيرًا**. مقارنة التحذيرات مع Batch04: **0 تشخيصات جديدة؛ زالت4 تشخيصات مقارنة بـ118 تحذيرًا؛ تفاصيل XML/comparison مؤرشفة**.
- Release QA مع R8/resource shrinking: **PASS؛ البناء مع الانحدار وR8 استغرق4m27s، وإعادة التحقق النهائية بعد XML استغرقت55s؛ سجلاهما مؤرشفان**.
- Package/signature/SHA: **`com.example.mol.referenceqa`، version1/1.0، min25/target35، حجم31,866,270 bytes. توقيع v2 صحيح بشهادة Android Debug الحالية (QA فقط): `870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`. الملف `.artifacts/prebackend-batch05-20261021/IHSAN-PREBACKEND-BATCH05-20261021-release-qa.apk`؛ SHA-256 `79c41d653717481c590603e2e9e2b78017fc36e73ff2e8811af2915472eed7fe`**.
- `git diff --check`: **PASS (exit0؛ تحذيرات تحويل LF/CRLF إن وجدت لا تُعد فشلًا)**. ملفات البداية المعدلة/untracked والخارجة عن ملكية الدفعة والمحفوظة حرفيًا: **175 من أصل198 مسار بداية محفوظة بايتًا؛ لا مسار بداية مفقود. بقية المسارات ضمن التعديلات المقصودة الموثقة، لا reset للدفعات السابقة**.
- لا Kotlin/Gradle/AGP/dependency upgrade. Manifest يضيف receiver وmetadata المعتمدين فقط؛ الأذونات والنسخ والحزمة وإعدادات التوقيع لم تتوسع.

## الملفات المعدلة أو الجديدة

**75 ملفًا تغير أو أضيف مقارنة ببصمة بداية Batch05**؛ القائمة لا تنسب جميع ملفات Git المتسخة لهذه الدفعة. نسخة وبصمات كل ملف في `modified-files.txt` و`source-sha256.json` داخل مجلد الأدلة.

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/mol/BarakahApp.kt`
- `app/src/main/java/com/example/mol/MainActivity.kt`
- `app/src/main/java/com/example/mol/di/RepositoryModule.kt`
- `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`
- `app/src/main/java/com/example/mol/ui/MainScreen.kt`
- `app/src/main/java/com/example/mol/widget/PrayerWidgetProvider.kt`
- `app/src/main/java/com/example/mol/widget/WidgetReadingRoute.kt`
- `app/src/main/res/layout/widget_prayer.xml`
- `app/src/main/res/values-ar/widget.xml`
- `app/src/main/res/values-night/colors.xml`
- `app/src/main/res/values-night/widget.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/widget.xml`
- `app/src/main/res/xml/prayer_widget_info.xml`
- `app/src/test/java/com/example/mol/widget/Batch05WidgetRouteTest.kt`
- `designsystem/src/main/java/com/example/designsystem/component/EhsanForm.kt`
- `docs/RELEASE_SIGNING_GUIDE.md`
- `docs/planning/IHSAN_ARCHITECTURE_AND_DATA_PLAN.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN_PROGRESS.csv`
- `docs/planning/IHSAN_RISKS_AND_DECISIONS.md`
- `docs/planning/IHSAN_UI_UX_SCREEN_MATRIX.csv`
- `docs/qa/IHSAN_PREBACKEND_BATCH05_20261021.md`
- `docs/store/DATA_SAFETY_DISCLOSURE.md`
- `feature/src/androidTest/java/com/example/feature/core/data/local/database/LibraryFavoriteTransactionTest.kt`
- `feature/src/main/java/com/example/feature/asma/data/local/AsmaLocalDataSource.kt`
- `feature/src/main/java/com/example/feature/asma/presentation/AsmaViewModel.kt`
- `feature/src/main/java/com/example/feature/azkar/AzkarScreen.kt`
- `feature/src/main/java/com/example/feature/azkar/presentation/AzkarScreenState.kt`
- `feature/src/main/java/com/example/feature/azkar/presentation/AzkarViewModel.kt`
- `feature/src/main/java/com/example/feature/core/observability/AppObservability.kt`
- `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`
- `feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt`
- `feature/src/main/java/com/example/feature/dashboard/data/DailyActivityRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/dashboard/presentation/HomeDashboardViewModel.kt`
- `feature/src/main/java/com/example/feature/duas/data/local/dao/DuaDao.kt`
- `feature/src/main/java/com/example/feature/duas/data/repository/DuaRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/duas/presentation/DuaViewModel.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/components/EhsanUiComponents.kt`
- `feature/src/main/java/com/example/feature/hadith/data/local/dao/HadithDao.kt`
- `feature/src/main/java/com/example/feature/hadith/data/repository/HadithRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/hadith/domain/model/HadithSearchIndex.kt`
- `feature/src/main/java/com/example/feature/hadith/presentation/HadithScreen.kt`
- `feature/src/main/java/com/example/feature/hadith/presentation/HadithViewModel.kt`
- `feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt`
- `feature/src/main/java/com/example/feature/learning/presentation/components/YouTubeNavigationPolicy.kt`
- `feature/src/main/java/com/example/feature/learning/presentation/components/YouTubePlaybackBridge.kt`
- `feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt`
- `feature/src/main/java/com/example/feature/prayer/widget/PrayerWidgetState.kt`
- `feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt`
- `feature/src/main/java/com/example/feature/quran/data/local/dao/QuranDao.kt`
- `feature/src/main/java/com/example/feature/quran/presentation/QuranViewModel.kt`
- `feature/src/main/java/com/example/feature/settings/SettingsScreen.kt`
- `feature/src/main/java/com/example/feature/settings/presentation/SettingsUiState.kt`
- `feature/src/main/java/com/example/feature/settings/presentation/SettingsViewModel.kt`
- `feature/src/main/java/com/example/feature/statistics/StatisticsScreen.kt`
- `feature/src/main/java/com/example/feature/statistics/presentation/StatisticsViewModel.kt`
- `feature/src/main/java/com/example/feature/tasbih/presentation/TasbihScreen.kt`
- `feature/src/main/java/com/example/feature/tasbih/presentation/TasbihUiState.kt`
- `feature/src/main/java/com/example/feature/tasbih/presentation/TasbihViewModel.kt`
- `feature/src/test/java/com/example/feature/batch05/Batch05DataStoreTest.kt`
- `feature/src/test/java/com/example/feature/batch05/Batch05LoggingTest.kt`
- `feature/src/test/java/com/example/feature/batch05/Batch05PlaybackLifecycleTest.kt`
- `feature/src/test/java/com/example/feature/batch05/Batch05PrayerFacadeRecoveryTest.kt`
- `feature/src/test/java/com/example/feature/batch05/Batch05QuranLoaderTest.kt`
- `feature/src/test/java/com/example/feature/batch05/Batch05RecoveryTest.kt`
- `feature/src/test/java/com/example/feature/batch05/Batch05RootRecoveryTest.kt`
- `feature/src/test/java/com/example/feature/batch05/Batch05SearchMeasurementTest.kt`
- `feature/src/test/java/com/example/feature/batch05/Batch05WidgetTest.kt`
- `feature/src/test/java/com/example/feature/quran/presentation/QuranSearchUiRecoveryTest.kt`
- `feature/src/test/java/com/example/feature/settings/presentation/SettingsViewModelTest.kt`
- `feature/src/test/java/com/example/feature/statistics/presentation/StatisticsViewModelTest.kt`
- `scripts/verify_bookmark_projection.py`

## الإغلاق النهائي الحقيقي

1. **قبول الجهاز:** G0-03/04/05/06، UX-01..05، F6-04 وبقية معايير F4/F5/F6 الميدانية. على APK واحد ببصمته: نماذج/بحث/RTL/Light-Dark/font1/1.3/2/TalkBack، بيانات/reopen، مشغل/شبكة، وWidget placement/resize/clock/zone/reboot/Doze/remove/action parity. موافقة تثبيت Xiaomi منفصلة لم تصدر.
2. **الأداء:** PS01 device baseline بـ10 عينات/median/p95 وstartup/frame/memory/battery، ثم اعتماد D08 وفقط إصلاح ما يثبت. قياس JVM الحالي ليس بديلًا عن ذلك.
3. **قرارات الإصدار:** D07 حُسم ولا سؤال Widget باقٍ. D01 لقبول media/fallback، وD02 لهوية وشهادة الإنتاج، وD10 للحقوق والمراجعة ما زالت مفتوحة. D04 إنجليزية كاملة مؤجلة؛ لا إعلانها مدعومة. المناسبات/المحطات/الوصفات والربط غير الموثق يبقى غير متاح، دون توليد ديني.
4. **الأعمال الأخرى:** R7-01 دراسة الميزات المستقبلية وR7-02/03 عقود/حقوق/قرار النشر؛ هذه ليست تنفيذ Backend ضمن الدفعة. النشر العام والمصادقة الآمنة/التحقق من الهاتف والمزامنة والبلاغات والإدارة الحقيقية Backend-dependent.
5. **نقطة الاستئناف:** الإغلاق المتكامل وفق ledger و70 حالة، لا Batch جديدة لإعادة بناء الميزات أو تدقيق شامل. أصلح أي فشل ميداني/برمجي قابل للإعادة باختباره المحدد؛ لا تنسب نجاح هاتف إلى البناء.

- UI COMPLETE: **NO**.
- LOCAL FUNCTIONALITY COMPLETE: **NO**.
- BACKEND INTEGRATION: **PENDING**.
- PHYSICAL QA: **PENDING**.

لا Commit/Push/Reset/Clean أو Backend/Firebase أو ترقية أدوات أو مساس ببيانات الهاتف.
