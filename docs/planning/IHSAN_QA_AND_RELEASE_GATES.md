# إحسان — الاختبارات وبوابات قبول الإصدار

2026-10-18 — Asia/Damascus. Planning Only. **لم ننفذ اختبارات أو تثبيت APK أو فحص هاتف أثناء التخطيط.**

## فصل مستويات الإثبات

| الحالة | الإثبات المطلوب | ما لا تثبته |
|---|---|---|
|Code Implemented|diff مراجع وsymbols/callsites مع hash|ليس build أو نجاح الوظيفة|
|Build Passed|الأمر/exit0/JDK/variant/hash APK وsource|لا unit أو device أو licensing|
|Unit Tests Passed|XML/suite/case counts/failures/errors/skipped لكل module|لا Android lifecycle/hardware أو schema فعلي|
|Device QA Passed|سيناريو ونتيجة وartifact والجهاز والدليل والاستعادة|لا حق توزيع أو قبول نشر تلقائي|
|Release Accepted|كل gate المطلوبة لنطاق توزيع محدد + قرار مالك مؤرخ|ليس وصفًا لكل إصدار لاحق أو موافقة على الميزات المؤجلة|

حالات الفحص: Pass،Fail،Not Tested،Blocked(سبب)،Not Applicable(تبرير). PendingLicense وPendingSourceReview حواجز خارجية مستقلة عن TechnicalFail. Raw Blocked في CSV الفيديو قد يعني انتظار السماع/المراجعة لا منع تضمين؛ حافظ rawStatus وأضف adjudication معللًا. لا تحول thumbnail/HTTP/READY/PLAYING/volume100/فتح خارجي إلى PASS للصوت والصورة الداخلية.

## متطلبات كل سجل دليل

Task ID،scenario ID،route/state،source tree digest/HEAD مع dirty diff،APK/test APK SHA-256،package/cert/variant،serial/model/API/WebView،timezone/locale/font/theme/network،preconditions،خطوات وأوقات،expected/actual،نتيجة كل بعد،paths لlogs/screenshots/video عند الحاجة،بيانات اصطناعية المستخدمة،ما لم يُختبر،before/after preferences وcleanup. الصور تُنقح من إشعارات/PII؛ لا تُرفع قواعد أو هاتف/صور/عنوان المستخدم في التقرير. صورة ثابتة وحدها لا تثبت animation أوصوتًا.

## بوابات منفصلة

| البوابة | الشروط | الحاجب |
|---|---|---|
|G0-A توحيد baseline|G0-01: حل تعارض PASS4BF1/C415/آخرdba6؛ مصفوفة الأدلة والبصمات|أي ادعاء بلا artifact مطابق|
|G0-B محلي|G0-02: Debug/Release+unit+Lint؛ نتائج module واضحة وتحذيرات مبررة|build/test failure أو skipped حرج غير معلل|
|G0-C تجربة beta|G0-03 navigation/actions/restore؛ G0-04 video/live؛ G0-05 migration/data؛ G0-06 prayer/qibla/delivery|عيب crash/data loss/سلوك عبادة خاطئ أو فحص حرج Not Tested|
|Beta Current Accepted|G0-A/B/C + إصلاحات ضرورية مثبتة فقط؛ scope/restrictions معتمدة|المزايا F4/F5/F6 ليست شرطًا؛ حقوق محجوبة تبقى خارج الوعد المعتمد|
|Feature Release Candidate|40-task scope المختار: كل feature accepted حسب dependency + F6-04 + انحدار G0 المتأثر|ميزة مقدمة للمستخدم بلا acceptance/rollback/privacy|
|Public/Store Ready|PS-04 وR7-02: شهادة/حزمة وتحديث وحقوق الأصول/أذان/content disclosures|بقاء public license/signing/PII blocker|
|Release Accepted|R7-03 وقرار المالك على artifact واحد وتوزيعه؛ لا نشر تلقائي|أدلة بين نسخ مختلفة أو قرار توزيع غير موجود|

إذا أخفق G0 في سلوك حرج له task لاحقة (مثل AR-01)، يُستخرج إصلاح صغير ضمن task نفسها ويُعتمد محليًا وهاتفيًا قبل Beta؛ لا تدخل ميزة جديدة في gate. اكتمال الخطة لا يعني اكتمال Gate0.

## تنفيذ محلي مستقبلي فقط

أوامر الجرد والبناء التالية أمثلة للدفعة G0-02 بعد اعتمادها، لا أوامر شُغلت هنا. تحقق JDK/toolchain الحالي أولًا؛ لا تغير Gradle أوالاعتماديات للإكراه على النجاح.

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleRelease -PihsanPhysicalQa=true --max-workers=2 --console=plain
.\gradlew.bat :app:testDebugUnitTest :feature:testDebugUnitTest :designsystem:testDebugUnitTest --max-workers=2 --console=plain
.\gradlew.bat :app:lintDebug :feature:lintDebug :designsystem:lintDebug :app:lintRelease :feature:lintRelease :designsystem:lintRelease --max-workers=2 --console=plain
```

يحفظ كل command مع exit code؛ إذا كانت مهمة up-to-date سجلها ولا تصفها rerun؛ XML محسوب بالـmodule. baseline التاريخي477 لا يفرض عددًا ثابتًا مستقبلاً عند إضافة اختبارات؛ أي نقص suite/skipped يفسر مقارنة باسمه ومصدره. لا تستخدم connectedAndroidTest العام دون ضمان serial/preservation؛ شغّل classes/scenarios محددة وفق runner الموجود بعد فحصه.

## سياسة الهاتف الإلزامية

الجهاز الوحيد Xiaomi **23129RAA4G**، ADB **ac190123**. لا Emulator/AVD ولا جهاز آخر؛ mocks/fakes في unit للدوال المعزولة فقط. قبل كل جلسة مستقبلية:

```powershell
adb -s ac190123 get-state
adb -s ac190123 shell getprop ro.product.model
adb -s ac190123 shell getprop ro.build.version.sdk
```

تحقق أيضًا من package/cert/backup policy وhash للـAPK المرشح قبل أي install-r؛ حزمة QA التاريخية com.example.mol.referenceqa لا تُستبدل بحزمة com.example.mol دون قرار. إذا لم تتطابق الشهادة أوالحزمة، توقف ولا تستخدم uninstall أو downgrade. لا install خلال هذه المهمة. افتح نشاط الاختبار فقط عند تفرغ الهاتف، ولا تغلق تطبيقات أخرى للمستخدم. عند عدم توافره سجل Blocked أوNot Tested وحدد ما يمكن إنجازه محليًا.

التجهيز: fixtures خاصة بالاختبار، PreserveDevicePreferences rule، عدم استبدال مدينة المستخدم الفعلية، عدم reset demo الحقيقي أوحذف صور، pre/post snapshot للتفضيلات المسموح بها وسجل cleanup. لا لمس قواعد المستخدم. اختبارات migration تنشئ DB اختبار خاصة؛ لا تستهدف ihsan_master_db الحقيقية. reboot وتغيير وقت/منطقة الجهاز خطوات حساسة مستقلة: حضّر الاختبار والعودة أولًا ثم اطلب الموافقة الصريحة الخاصة بها، ولا تعتبر موافقة الخطة إذن reboot. لا تغيير system timezone تلقائيًا كاختبار روتيني.

جهاز واحد API35 تاريخيًا لا يثبت API25/Android16 أو أجهزة launcher أخرى. دوّن فجوة التغطية؛ unit/desugaring/static manifest يفيد لكن ليس DevicePass. لا تعالج القيد باستخدام emulator المحظور. يتغير نطاق الأجهزة فقط بإذن جديد صريح.

## طبقات الاختبار وملفات البداية

| المستوى | تغطية عملية مطلوبة | بداية فعلية |
|---|---|---|
|Unit/domain|page distribution/date/Arabic keys/prayer reference/bearing/review scheduler/cancel/error|feature/src/test/.../quran/domain/model/QuranGoalCalculatorTest.kt؛ core/search/ArabicSearchTest.kt؛ prayer-reference fixtures|
|Repository|favorites/ownership/ranges/daily snapshot/learning restore/malformed state|ManualPrayerLogRepositoryTest،FahmRepositoryTest،SanhyaBookmarkPersistenceTest،NabiIhsanRepositoryTest|
|ViewModel|loading/error/empty/latest-query/saved-state/write failure/clock rollover|feature/src/test/.../statistics/presentation/StatisticsViewModelTest.kt؛ tasbih/presentation/TasbihViewModelTest.kt؛ اختبارات جديدة في نفس convention|
|Compose UI|state semantics/actions/Back/IME/insets/theme/font|app/src/androidTest/.../DarkFinalRouteMatrixTest.kt؛ UiPolishMatrixTest.kt؛ HeaderGeometryTest.kt|
|DB migration|all supported start versions→current؛ IDs/defaults/favorites/rows preserved|feature/src/androidTest/.../core/data/local/database/IhsanDatabaseMigrationTest.kt|
|Integration|Koin production roots وDAO→repo→VM وAlarmManager/Worker/Intent contract|ProductSurfaceNavigationCharacterizationTest؛ SharedFoundationGateTest؛ ReleaseCorePersistenceTest|
|E2E|read→last read→relaunch؛ query→exact detail؛ local listing fixture→manage/cancel؛ future plan/prayer/widget/notes|ReleaseFunctionalJourneyTest؛ PhysicalCommunityJourneyTest؛ OfferDraftRestorationTest|
|Accessibility|TalkBack/focus/labels/touch bounds/font2/no overlap/contrast|semantics assertions + Accessibility Scanner وTalkBack على الهاتف، لا نتائج مخترعة|
|Performance|cold/warm startup/scroll/search/memory/battery/size/query count|PS-01 measurement protocol أدناه|
|Regression/release|نفس production navigation وartifact الحالي؛ لا test-only substitutes تخفي errors|ReleaseContentAndPlayerTest؛ VideoLifecyclePhoneTest؛ VideoRenderingRootCauseTest|

اختبارات source regex الحالية useful كعقود عدم تراجع لكنها لا تثبت سلوكًا runtime. لا نكتب tests لمجرد تصوير implementation؛ نختار failure modes وقواعد دينية/بيانات حقيقية. أسماء اختبارات جديدة تحدد وقت التنفيذ ويشار إليها كمقترحة حتى تُنشأ.

## مصفوفة الوظائف الحساسة

| Scenario ID/Tasks | الخطوات والقبول | ما يبقى خارج الاستنتاج |
|---|---|---|
|QUR-01 / G0-05،UX-03|114/6236 compare exact bundled XML؛604 anchors monotonic/coverage once؛30 juz؛ first/last/سورة transition؛ علامات/حروف/اتجاه محفوظة|لا ادعاء مطابقة سطور مصحف مطبوع؛ لا تغيير النص ليطابق صورة|
|QUR-02 / AR-02|save surah+ayah→reopen/process recreate؛ no transient mixed pair؛ bookmark→reload→unfavorite؛ invalid route bounds recovery|القراءة/قفزة موضع ليست إكمال ختمة|
|QUR-03 / UX-03|page zoom/pan/navigate/verse select/TalkBack/copy/share/source؛ ورق ثابت في dark؛ controls خارج system bars|لا تفعيل reciter/tafsir pending license|
|PRA-01 / G0-06|مدينة وطريقة ومذهب وتاريخ/zone معلنة؛ مقارنة fixtures المرجع المستقل بنفس الإعدادات؛ Home/Prayer parity؛ UTC/DST/midnight/next-day Fajr|لا توافق رسمي محلي مفترض؛ tolerance يحدد مسبقًا من fixture/method ولا يوسع بعد الفشل|
|QIB-01 / G0-06|pure bearing known coords/normalization؛ test offline/manual/no-location/no-sensor؛ تحقق true-vs-magnetic/declination والاتجاه عند الدوران والمعايرة بمرجع مستقل|وصف «المستشعر جيد» ليس دقة زاوية مقاسة؛ tolerance مرجعي يعتمد قبل قبول|
|NOT-01 / G0-06|permission deny/grant/exact denied→fallback؛ alarm replace/cancel/IDs وعدم ازدواج؛ notification visible+audible في الوقت المتوقع؛ app background/Doze|ledger/WorkInfo أوReceiver test ليس delivery|
|NOT-02 / G0-06|time/zone/date/app-update/reboot reconciliation، قبل/بعد بالإذن؛ weekday/civil time للأذكار مع window مرن موثق|WorkManager24h لا ضمان دقيقة ثابتة؛ قبل unlock غير معتمد بمجرد filter LOCKED_BOOT|
|VID-01 / G0-04|86 per-ID mapping/title/channel؛ frame nonzero وصورتان متقدمتان/time advance؛ Light+Dark؛ audio human confirmation موسوم بعينه|READY/PLAYING/muted=false/فتح خارجي لا يثبت image/audio|
|VID-02 / G0-04|latest Release من الأقسام الثلاثة؛ fullscreen استمرار ثم Back ثم background pause ثم resume explicit؛ offline/timeouts/retry/renderer recovery|عينة مشتركة لا PASS فردي لكل86 فيfullscreen؛سجل ما عُمم وما لم يُختبر|
|LIVE-01 / G0-04|Haram/Nabawi على نفس Release، ثم كل بديل خارجي مستقلاً؛150→honest unavailable وبديل رسمي|رفض platform ليس عيبًا يجب تجاوزه؛ D-01 يحدد شرط embed|
|DAT-01 / G0-05|fresh/upgrades/reopen/failure fixture؛ compare row IDs/counts/values/ownership/contactMethod/favorites/files/preferences؛ no destructive fallback|inode وحده لا يثبت محتوى كل البيانات|
|NEW-01 / F6-04|plans manual completion/reschedule،prayer manual→history→widget parity،calendar offset→fasting→Ramadan،notes→export explicit|لا تخص Beta الحالي قبل إضافتها؛ لا حكم ديني أوsync خفي|

## checklist الشاشات التي تحيل إليها CSV

كل خلية أدناه **مراجعة مطلوبة/غير مقاسة الآن** ما لم يذكر finding دليلًا محددًا. يسجل المنفذ pass/fail/evidence لكلscreen/state وثيم قبل وبعد؛ لا يملأها PASS تلقائيًا من وجود Material3.

| الرمز | الاختبار |
|---|---|
|V01|Visual hierarchy: وظيفة الشاشة والفعل الأساسي والمصدر/الحالة واضحون|
|V02|spacing بين العناصر والـsection/padding متسق دون فراغ/تزاحم غير مقصود|
|V03|alignment بصري ونصي مع RTL/LTR المناسب وmixed digits|
|V04|Arabic typography/Amiri metrics/diacritics/line height؛ لا قص أحرف أوحذف|
|V05|color harmony وفق Dark Emerald/Charcoal؛ semantic exception Quran/brand/status/player|
|V06|surface elevation والطبقات واضحة دون borders وحدها أوneon|
|V07|cards/containers/radius وتباعد المحتوى؛ لا ارتفاع ثابت يقص النص|
|V08|iconography/labels متسقة ومرئية والأفعال مكتشفة|
|V09|buttons primary/secondary/disabled/error consistency|
|V10|crop/aspect صور السماء/مآذن/الكعبة طبيعية؛ لا global dark filter؛ iframe مرئي|
|V11|responsive وLight/Dark geometry متطابقة؛ المصحف warm exception|
|U01|وضوح كل action ووجهته وأن لا callback بلا نتيجة مرئية|
|U02|user flow/minimum steps/discoverability وعدم routes مكررة|
|U03|Loading/Error/Empty/Success/Offline/PendingLicense مميزة لكل مصدر|
|U04|retry/back/fallback عند failure وIntent بدون handler|
|U05|Back/up/deep route والاستعادة وinvalid ID بلا loop|
|U06|Keyboard/IME/focus scroll؛ زر الإرسال/آخر حقل غير محجوب|
|U07|status/nav/system insets وbottom bar؛ gesture/3-button navigation|
|U08|process recreation/persisted preferences/query/scroll عند الوعد بها|
|U09|منع double-submit وتراجع/cancel للتعديلات المدمرة وحفظ الخصوصية|
|A01|Arabic RTL وEnglish LTR-shell إن اعتمدت؛ Quran RTL دائمًا|
|A02|fontScale1 و1.3 و2؛ dynamic layout مع labels طويلة|
|A03|touch semantics bounds≥48dp وعدم overlap؛ icon size ليس touch size|
|A04|تباين مقاس للحالة الفعلية: النص العادي4.5:1 والكبير3:1؛ compositing/image scrim مشمول|
|A05|TalkBack names/roles/actions/selected/disabled/stateDescription الصحيح دون إعلان كل ثانية|
|A06|content descriptions للأفعال؛ decorative icons null صحيح؛ لا ازدواج نطق النص|
|A07|focus order وتركيز الحوار/الخطأ واسترجاعه؛ secondary verse actions متاحة|
|A08|dynamic text/long names/phone/url والدوران لا يصنع تداخلًا|
|A09|no clipping/overflow؛ ellipsis فقط في metadata غير جوهرية؛ النص الديني كامل في تفاصيله|

CSV61صف: 59 concrete test cases +Splash وlegacy redirect، و58 pattern unique. يتضمن بعض routes أكثر منcomposable حسب type/flags؛ الاختبار يثبت المنتج بالـvariant الحقيقي. sheets/dialogs/source/license states ضمن secondary_states ولا تعد routes إضافية. أي route جديدة في الميزات تعد وتربط من جديد دون التمسك58 إلى الأبد.

## بروتوكول الأداء PS-01 وPS-02

البيئة ثابتة قدر الإمكان: نفس الهاتف/Release APK/بيانات اصطناعية/ثيم/font/network/حالة شحن وحرارة؛ لا قياس أول run مع seeding ثم مقارنته steady state.10 عينات لكل سيناريو أولي، median وp95 مع بيان أن10 عينات استكشافية قليلة لتقدير tail؛ إذا الضجيج كبير زد العينات قبل هدف نهائي. التغيير يقاس قبل/بعد بالشروط نفسها؛ لا نسبة تحسن مخترعة.

| المقياس | خط الأساس المطلوب | الأداة | قبول التحسن |
|---|---|---|---|
|Startup|TTID/TTFD cold/warm،with/without first seed؛ trace للmain|Perfetto/System Trace؛ Macrobenchmark فقط إذا أضيف test harness معتمد|D-08 budget يعتمد بعد baseline؛ bottleneck واضح removed|
|Scroll/render|frame duration/jank أثناء Quran/Home/Hadith lists وhero|System Trace/JankStats أوProfiler tools المتاحة|لا تراجع خارج noise المعتمد؛ حافظ refresh-rate budget|
|Recomposition|count/scope عند clock tick/query/theme على node محدد|Compose Layout Inspector/tracing|التقليل لا يغير state أويخفي تحديثًا|
|Search|cold/warm query عربية short/long/with diacritics،empty/latest؛memory index|monotonic timing+trace؛dataset/version ثابتان|p95 budget بعد القياس؛ correctness suite يسبق السرعة|
|DB|queries/bookmarksN=0/10/100 وEXPLAIN plans|Room query callback في QA فقط/Database Inspector علىfixture|AR-03 يقلل2N+1 إلى ثابت؛ لا log user payload|
|Memory|heap/RSS/PSS قبل وبعد20 فتح/إغلاق reader/player وdecode assets|Android Profiler/dumpsys meminfo؛ heap fixtures فقط|لا نمو مستمر unexplained؛ لا retention بعدdispose|
|Battery|wakeups/timers/sensor subscription/24h alarm schedule/widget updates|Batterystats/System Trace/Profiler|لا ticker ثانية لـwidget ولاsensor بعد مغادرة؛ energy budget بعد baseline|
|Network|timeouts/retries/transient/permanent/offline/data usage|controlled fake responses + phone network traces إن سمح|bounded retries؛ no retry لإذن/ترخيص مرفوض|
|APK size|compressed/uncompressed/assets/native/resources ومن أين جاءت الزيادة|APK Analyzer/zip inventory|كل delta مبرر؛ لا حذف قرآن/notice/حقوق لتصغير|
|ANR/crash|main-thread parse/IO وlifecycle loops وstress/recreate|StrictMode فيQA/trace/logcat sanitized|صفر crash/ANR ضمن السيناريوهات المنفذة؛ لا وعد صفر مطلق|

## الأمن والخصوصية PS-03 وPS-04

أذونات merged manifest لا ملفات manifests منفردة فقط؛ exported components/intent filters/pending intents immutable/URI validation؛ WebView HTTPS allowlist/redirect renderer/bridge methods لا arbitrary commands/files؛ لا file/content access. image picking MIME/size/path/cancel وحفظ نسخة app-private بدل افتراض permission دائم. sharing عبر SAF/URI grant محدود إن احتاج، لا raw path أوpersisted broad permission. افحص deep route numeric/string bounds وencoded query وIntent handler absent. افحص network hosts/TLS/cleartext/secrets signing دون طباعة القيم. backup exclusions وPII logs/deletion scopes والداتا الجديدة تتراجع مع كل feature.

لا analytics/tracking أوSDK خارجي دون قرار خصوصية مستقل. provider/startup غيرexported كما في المصدر ليس ثغرة، MainActivity launcher exported طبيعي؛ فحص merged manifest يثبت النتيجة النهائية. current JS bridge محدود وfile access off؛ adversarial tests لا تعني وجود exploit مثبت.

## Definition of Done وRecovery

لكل دفعة: change review/scope،unit/integration ذات صلة،migration إن تغير storage،صور وTalkBack/physical للأثر،hash manifest،CSV/sources/disclosures محدثة،مخاطر معتمدة،لا data loss ولا regression blocker ثم قبول الدفعة. تبقى الهاتف Pending عند غيابه دون تزوير قبول. راجع R7-03 فقط عندما شروطه كاملة.

التراجع للـUI/logic يعكس فرق الدفعة وحده ويحفظ العمل السابق؛ بعد migration لا downgrade. اختبر forward repair علىfixture واحتفظ schema والبيانات الجديدة،أخف entry عند الحاجة دون حذف. Restore/export العام يحتاج D-05؛ لا نسخ قواعد حقيقية إلى تقارير. لا Commit/Push/Reset/Rebase أوpublish ضمن هذه الخطة.

المراجع: [Compose accessibility/Material defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)، [Alarm APIs](https://developer.android.com/develop/background-work/services/alarms)، [Room migration testing](https://developer.android.com/training/data-storage/room/migrating-db-versions)، [YouTube errors](https://developers.google.com/youtube/iframe_api_reference#onError). راجع API versions المثبتة قبل نقل snippets حديثة.


## قيد Variant في جرد المسارات

Screen يعرّف58 route، والمصدر يحتوي58 موضع composable. لكن IhsanPlusDaily يسجل فقط عند dailyEnabled: Debug=true وRelease=false في feature/build.gradle.kts، لذلك graph الإنتاج Release57 وDebug58. قائمة الاختبار59 تحتوي controlled_daily، فتتطلب58 حالة إيجابية في Release واختبار غياب route/entry للحالة controlled؛ لا تفعّل flag لتزييف المرور. Splash وLegacy خارج الـ59 ويختبران منفصلين. CSV يميز declared route عن registered route للوسائط الاختيارية nameId/hadithId/zikrId.

## مراجعة الاتساق النهائية — الإصلاحات الست الأصلية

REC-01 إلى REC-06 معرفات **معايير قبول** داخل المهام القائمة، وليست مهام إضافية. المصدر التاريخي هو إفادة المستخدم عن تسجيل الشاشة؛ لا نفترض اسم ملف تسجيل خام أو زمنًا داخله لم يُقدم في هذه الجولة. الأرقام «إصلاح1..6» في تقرير الهاتف تشمل مجموعة أخرى، ولا تُساوى آليًا بهذه القائمة.

يسجل G0-01 provenance كل دليل تاريخي، وينفذ G0-03 قبول هذه الحالات الست مستقلًا بعد اعتماده. Phase1 مالكة الإصلاح الموضعي إذا ثبتت حاجة: UX-01/UX-03/UX-05 كما أدناه. G0-04 يبقى مسؤولًا عن تشغيل الفيديو والبث؛ لا يثبت صلاحية زر تلاوات القرآن المحجوبة بالحقوق.

### REC-01 — تداخل Status Bar أثناء التمرير

- المصدر/الرموز: feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt — statusBarAlpha / windowInsetsTopHeight(WindowInsets.statusBars).
- الحالة: أصل تاريخي أبلغ عنه المستخدم؛ حماية scroll/120f موجودة في المصدر؛ تقرير PASS بصري سابق لا يثبت البناء الحالي.
- المالك: G0-03 للتحقق؛ UX-01 للإصلاح المثبت والحماية من الانحدار؛ G0-01 لربط الأدلة.
- القبول: على نفس APK الموثق: scroll=0 و60 و120 ثم تمرير عكسي وسريع؛ Light/Dark وخط1/1.3/2. أيقونات النظام مقروءة دون تراكب نص hero أو وميض، حماية الشريط تتبع موضع التمرير، ولا يتغير inset أو تصميم السماء والمآذن.
- الدليل المستقل المطلوب: فيديو قصير مستقل للمسار كاملًا وصور عند0/60/120 لكل مظهر، مع theme/font/scroll position وAPK hash؛ الصور التاريخية01/02/06/07 مراجع مقارنة فقط.
- الحالة الحالية: Source Reviewed؛ Visual Acceptance Pending؛ لا فحص هاتف جديد في هذه المراجعة. لا يغلق المعيار بوجود if/اختبار helper أوPASS من بصمة مختلفة.

### REC-02 — عداد الأذكار RTL

- المصدر/الرموز: feature/src/main/java/com/example/feature/azkar/AzkarScreen.kt — counterDescription / azkar_counter_format؛ feature/src/main/res/values-ar/strings.xml وvalues/strings.xml.
- الحالة: النص الحالي «المكتمل: %1$d من %2$d» وsemantics موجودان؛ نتيجة العرض واللمس على الهاتف الحالي معلقة.
- المالك: G0-03 للتحقق؛ UX-03 للإصلاح المثبت والحماية من الانحدار؛ G0-01 لربط الأدلة.
- القبول: قيم اصطناعية0/33 و1/33 و12/33 و33/33 و100/100 وحالةtarget=0؛ لا انعكاس current/target ولا خلط بالأيقونات، والأزرار في RTL الصحيح؛ increment/reset والمفضلة لا يتغير سلوكها؛ TalkBack يقرأ الحالي ثم الهدف مرة واحدة.
- الدليل المستقل المطلوب: صور RTL بمواضع الأرقام، سجل semantic values وتسجيل TalkBack مستقل، مقارنة الحالة قبل/بعد increment/reset؛ اختبار resource وحده لا يثبت العرض.
- الحالة الحالية: Source Reviewed؛ Visual Acceptance Pending؛ لا فحص هاتف جديد في هذه المراجعة. لا يغلق المعيار بوجود if/اختبار helper أوPASS من بصمة مختلفة.

### REC-03 — زر التلاوات غير المرخصة

- المصدر/الرموز: feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt — ReaderPickerSheet / AudioPlayerBar / isApproved؛ feature/src/main/java/com/example/feature/quran/util/RecitationAvailability.kt — RECORDINGS_APPROVED=false؛ AudioPlayerHandler.kt.
- الحالة: الأذونات الحقوقية محجوبة فعلًا؛ picker يحفظ تفضيلًا فقط وأزرار player محكومة بالعلم. لا عيب صلاحية مثبت جديد؛ وضوح الزر يحتاج تحققًا مستقلاً.
- المالك: G0-03 للتحقق؛ UX-03 للإصلاح المثبت والحماية من الانحدار؛ G0-01 لربط الأدلة.
- القبول: طالما RECORDINGS_APPROVED=false: لا تشغيل أو تنزيل أو enqueue أو طلب تسجيل شبكي من اختيار قارئ/Play/Next/Previous/retry. زر اختيار القارئ إما تفضيل محلي موضح صراحة أو حالة غير متاحة بإفصاح واضح؛ لا زر يبدو معطلًا بلا تفسير. لا تغيير العلم أو الترخيص أو نص القرآن، ولا تعتبر fixture WAV إذن تلاوة.
- الدليل المستقل المطلوب: صور picker وexpanded/collapsed player وحالةتعطيل semantic، وسجل callbacks/work/network في fixture الاختبار مستقبلًا. PendingLicense منفصل عن قبول وضوح UI.
- الحالة الحالية: Source Reviewed؛ Visual Acceptance Pending؛ لا فحص هاتف جديد في هذه المراجعة. لا يغلق المعيار بوجود if/اختبار helper أوPASS من بصمة مختلفة.

### REC-04 — تنظيف عناوين نبي الإحسان

- المصدر/الرموز: feature/src/main/java/com/example/feature/nabiihsan/util/NabiIhsanTitleCleaner.kt — cleanEpisodeTitle؛ presentation/NabiIhsanEpisodeDetailScreen.kt؛ presentation/components/NabiEpisodeCard.kt.
- الحالة: فجوة كود مثبتة: التفاصيل تستعمل cleanEpisodeTitle بينما البطاقات تستعمل substringAfter/replace فقط؛ اختبار helper لا يثبت استعماله في البطاقات.
- المالك: G0-03 للتحقق؛ UX-05 للإصلاح المثبت والحماية من الانحدار؛ G0-01 لربط الأدلة.
- القبول: نفس سياسة display-only عبر الرئيسية/كل الحلقات/المفضلة/لاحقًا/البحث/التفاصيل. عينتا12 و29، prefix عربي/لاتيني، hashtags ونقاط متكررة: عنوان مقروء غيرفارغ دون تكرار رقم الحلقة، مع بقاء المعنى ورقم الحلقة metadata وIDs وraw source title وURLs محفوظة. النص المصدر لا يعاد تحريره ولا يولد وصف.
- الدليل المستقل المطلوب: جدول raw→display لكل الكتالوج مع اختبار caller integration للبطاقة والتفاصيل، وصور مستقلة لستةأسطح بالمظهرين. استعمل helper الموجود؛ لا formatter جديد.
- الحالة الحالية: Source Reviewed؛ Visual Acceptance Pending؛ لا فحص هاتف جديد في هذه المراجعة. لا يغلق المعيار بوجود if/اختبار helper أوPASS من بصمة مختلفة.

### REC-05 — إخفاء الأوصاف غير المتوفرة

- المصدر/الرموز: feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanEpisodeDetailScreen.kt — aboutText/subtitle/seerahContext/practicalReflection.isNotBlank؛ feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaStoryDetailScreen.kt — summary.isNotBlank؛ learning/presentation/components/MediaCard.kt.
- الحالة: حراس isNotBlank موجودة في الأسطح المعاينة؛ اختبارات بيانات فارغة لا تثبت خلو layout من عناوين أوspacers يتيمة.
- المالك: G0-03 للتحقق؛ UX-05 للإصلاح المثبت والحماية من الانحدار؛ G0-01 لربط الأدلة.
- القبول: fixtures: نص فارغ،whitespace فقط،ونص موثق غيرفارغ. أخفِ القسم وعنوانه والفاصل والمسافة التابعة له عند غياب الوصف؛ اعرض النص الصحيح كاملًا في موضعه عند توفره. أبق إفصاح المصدر أوعدم توفر المحتوى اللازم،ولا تعوض الفراغ بوصف ديني مولد. Light/Dark وخط2 وTalkBack بلا heading يتيم.
- الدليل المستقل المطلوب: صور empty/whitespace/present منفصلة لكل surface مع فحص شجرةsemantics وغياب header/spacer التابع، واختبار رجوع للفيديو/المفضلة.
- الحالة الحالية: Source Reviewed؛ Visual Acceptance Pending؛ لا فحص هاتف جديد في هذه المراجعة. لا يغلق المعيار بوجود if/اختبار helper أوPASS من بصمة مختلفة.

### REC-06 — الشارات الفارغة في سنحيا بالقرآن

- المصدر/الرموز: feature/src/main/java/com/example/feature/sanhya/presentation/components/QuranStoryCard.kt؛ presentation/SanhyaStoryDetailScreen.kt — formattedSurahLabel / durationLabel؛ feature/src/main/java/com/example/feature/learning/presentation/components/MediaCard.kt — badge.takeIf/isNotBlank.
- الحالة: حراس badge/metadata وformattedSurahLabel/durationLabel موجودة؛ قبولعدم ظهور شارة أوسطح فارغ على artifact الحالي معلق.
- المالك: G0-03 للتحقق؛ UX-05 للإصلاح المثبت والحماية من الانحدار؛ G0-01 لربط الأدلة.
- القبول: badge=null/empty/whitespace ومدة/مرجع سورة فارغ وtotalEpisodesCount=0/1/>1 في fixtures؛ لا pill أوborder أوpadding أونطقشارة فارغة. الشارة الفعلية ذات القيمةتبقى مقروءة ومعناها صحيح. لا حذف مراجع موثقة أو اختراع مدة/عدد/وصف.
- الدليل المستقل المطلوب: صور مستقلة blank/nonblank لشاشة القائمة والتفاصيل والحلقات،semantic tree يثبت غياب badge عند الفراغ،وانحدار لكل مستعملي MediaCard.
- الحالة الحالية: Source Reviewed؛ Visual Acceptance Pending؛ لا فحص هاتف جديد في هذه المراجعة. لا يغلق المعيار بوجود if/اختبار helper أوPASS من بصمة مختلفة.


## سلسلة إثبات المصدر ← البناء ← المثبت ← المختبر (نتائج تسوية G0-01)

تمت التسوية المكتبية الشاملة لسلسلة الأدلة والمستندات المحفوظة داخل المهمة G0-01 وفق القواعد P1..P5، وجاءت النتائج كالتالي:

### 1. جدول تسوية ملفات APK والأدلة المرتبطة بها (Reconciled APK Artifacts Matrix)

| البصمة SHA-256 | اسم الملف والمجال | الحزمة والنسخة | حالة التثبيت | حالة القبول الميداني | تصنيف الوصلة (Classification) | المبرر والحدود الأدلتية |
|---|---|---|---|---|---|---|
| `4BF1DD4FEB1E85DB6FB77C1133C7E0E18EBF661A91DCEF74CBBDF01126B67B3D` | `app-release.apk` (مذكور في تقرير QA القديم) | `com.example.mol.referenceqa` v1.0 (Debug Cert) | غ.م (غير موجود بالمسارات المباشرة) | Pass (بصري تاريخي لـ12 شاشة) | **Reported Only / Historical Evidence** | التقرير التاريخي يعلن PASS بـ12 صورة، لكن الـAPK غير محفوظ بالمسارات المباشرة وسلسلة البناء P1/P2 غير مكتملة. |
| `C415EA4A88F3EA11AA7472A2387C62F0CD81D1543233438CAC93F6E6F1F72EA7` | `Ihsan-physical-beta-20261017-rc-c415ea4a.apk` | `com.example.mol.referenceqa` v1.0 (Debug Cert) | `releaseInstalled=false` | NOT_EXECUTED (انقطاع ADB) | **Verified Historically & File Confirmed** (Build) / **Unproven** (Device) | الـAPK موجود بمجلد `release/apk/` ومطابق للبصمة وموثق بـ provenance فوق 603 ملفًا، لكن لم يُثبت أو يُختبر على الهاتف. |
| `dba6df9f1d969206edca1eb901f6b93341134d4960a76c1fda4c3cd88db3ca83` | `Ihsan-video-fixed-20261017-referenceqa-dba6df9f.apk` | `com.example.mol.referenceqa` v1.0 (Debug Cert) | `installedApkByteIdentical=true` | `releaseRuntimeAcceptance = Not Tested` | **Verified Historically & File Confirmed** (Build & Install) / **Unproven** (Device Foreground) | الـAPK موجود بمجلد `release/apk/` مع ملف `.sha256` المرفق، وموثق التثبيت بـ `adb install -r` فوق Inode التخزين دون مسح بيانات، لكن القبول الأمامي للتشغيل مسجل Not Tested. |

---

### 2. شروط وقواعد الإثبات الشاملة P1..P5 (مستعادة بالكامل من النص الأصلي المعتمد)

**P1 مصدر كامل:** manifest بأسماء نسبية مرتبة وSHA-256 لكل مدخل build فعلي: source/resources/assets/manifests/schemas/config/Gradle wrapper/catalog/scripts المستخدمة، والـuntracked المؤثر، وdirty diff موثق؛ test inputs في قائمة مستقلة. يسجل HEAD والجرد وخوارزمية digest والاستبعادات وvariant/properties وJDK/SDK/Gradle والاعتماديات المحلولة بما يمكن جمعه دون أسرار. مستندات التخطيط وbuild outputs/cache والأسرار خارج digest المدخلات؛ يصرح بأي configuration سري مؤثر دون قيمها. HEAD وحده أوgit status أوعينة8ملفات لا يكفي.

**P2 صلة البناء:** input manifest مثبت قبل البناء وبعده، دون تغير inputs أثناءه؛ command/exit/log مربوطان بbuild-run ID فريد؛ APK نفسه محفوظ بمسار ثابت وبصمته وpackage/certificate/version/variant، واختبارAPK إن وجد. hash مدخلات المصدر ليس hash الـAPK ولا يجوز مساواتهما. إعادة بناء من مصدر يبدو مطابقًا لا تثبت artifact تاريخيًا ولا نفترض reproducible APK bytes بسبب timestamps/signing/toolchain. عند غياب provenance كامل نقول Source→Artifact Unproven؛ يمكن لـG0-02 إنشاء مرشح جديد بسلسلة كاملة بعد اعتماده، ولا يُعاد نسب PASS تاريخي إليه.

**P3 المثبت:** في دفعة هاتف لاحقة معتمدة فقط، تحقق serial/model/package/cert وpm path لكلbase/split؛ قارن hashes النسخ المقروءة من المسارات المثبتة مع المرشح المحفوظ. سجل مجموعةsplit كاملة عند وجودها، لاbase وحده. إذا تعذر التحقق لا تدع byte-identical. تاريخ/اسمAPK/versionCode1 أوالشهادة المشتركة لا يثبتان نفس الـAPK. سجل عدم لمسcom.example.mol عند استهدافreferenceqa. لا قراءة بيانات المستخدم لهذا الإثبات.

**P4 المختبر:** قبل كلsession وبعدها تؤكد بصمة المثبتة وعدم تبديلها، وبصمةtest APK/runner/config والهدف والـvariant والجهاز؛ timestamp وسيناريو وtheme/font ومراجع raw logs/screens/فيديو وصوت وأبعاد النتيجة. لو احتاج instrumentation بناءDebug بدلRelease فهو دليلDebug فقط؛ لا ننسبه إلىRelease. اختلاف أي artifact أوتغييرinputs/variant بعد الاختبار يبطل تعميمPASS ويستلزم إعادة الفحوص المتأثرة على المرشح المختار.

**P5 adjudication:** كلscenario يحتوي
source_manifest_status،source_digest،build_run_id،artifact_sha256،installed_set_hashes،
test_apk_sha256،target_package،variant،device،scenario_id،evidence_paths،
code_result،build_result،unit_result،visual_result،functional_result،license_status،
unverified_links.
مراجعة G0-01 تملأ ما تثبته الملفات فقط وتوسم الباقي Unknown/Not Tested.
يمكن حفظ PASS تاريخي محدود لـAPK معروف، لكن قبول المصدر/الإصدار الحالي يحتاج اكتمال الوصلات ذات الصلة؛ عدم اكتمالها ليس إذنًا بتلفيقها أوتشغيل الهاتف ضمنG0-01.

---

### 3. جرد مدخلات البناء والمصدر الحالي (Source & Build Inputs Manifest)

| العنصر / المجال | القيمة الموثقة | التصنيف الأدلتي | البيان والحدود |
|---|---|---|---|
| **Git HEAD Commit** | `7dcf417038c90cb599326abd50afcf82321d6544` | Verified Historically | موضع HEAD أثناء الجرد والتدقيق |
| **الملفات المراقبة (Tracked Files)** | **777 ملفًا** | Reported Only (Codex Baseline) | تشمل src/ للوحدات الثلاث، الشيمات، السكريبتات، وإعدادات البناء |
| **SHA-256 Digest الجماعي** | `6ac93ec8e655a66e539752ae96ba2eab49456477eb1d33accfc4c14b38a0956a` | Reported Only (Codex Baseline) | خوارزمية SHA-256 الموحدة فوق مسارات الملفات النسبية المرتبة |
| **git status Digest الخارجي** | `c0e781fb8519fcffd52df580f7a2d7b8bd90bcb09e5ee592c66040528bb45d5a` | Reported Only (Codex Baseline) | ملخص حالة المستودع خارج التخطيط |
| **بيئة البناء والـToolchain** | Gradle Wrapper **8.10.2**, AGP **8.7.3**, Java **21.0.3** (JBR), Kotlin **2.1.0**, Compose BOM **2024.12.01**, Room **2.6.1**, Koin **3.5.6** | Verified Now (gradle-wrapper.properties & libs.versions.toml) | Gradle Wrapper 8.10.2 تم التثبت منه من `gradle-wrapper.properties` مباشرة، و AGP 8.7.3 من `libs.versions.toml`. minSdk 25, compile/targetSdk 35. |

---

### 4. ملخص نتائج اختبارات الوحدات التاريخية (Historical Unit Test XML Summary)

| الوحدة (Module) | عدد الاختبارات | Suites | الفشل Failures | الأخطاء Errors | المتخطى Skipped | Lint Warnings (Debug) | Lint Warnings (Release) | التصنيف الأدلتي |
|---|---|---|---|---|---|---|---|---|
| `:app` | 33 | 8 | 0 | 0 | 0 | 118 | 93 | Verified Historically |
| `:feature` | 415 | 90 | 0 | 0 | 0 | 22 (+1 Info) | 22 (+1 Info) | Verified Historically |
| `:designsystem` | 29 | 7 | 0 | 0 | 0 | 9 | 9 | Verified Historically |
| **المجموع الموحد** | **477** | **105** | **0** | **0** | **0** | **149** | **124** | **Verified Historically** |

*تنبيه أدلتي*: الأرقام أعلاه مستمدة ومفحوصة تاريخيًا من سجل `docs/release/external-evidence/2026-10-17/video-fix-quality-final/quality-summary.json` (والذي فهرس الـ 105 ملفات `TEST-*.xml` أثناء تشغيل البناء والجودة بتاريخ 17 أكتوبر 2026)، ولم تُحسب بقراءة إفرادية لكل XML أو تُعد تشغيلها في G0-01.

---

### 5. مصفوفة معايير قبول الإصلاحات الستة (REC-01..REC-06 Acceptance Matrix)

| الرمز | العنوان والأصل | ملفات المصدر الحساسة | حالة مراجعة الكود (Code Status) | حالة القبول البصري (Visual Acceptance) | المهمة المالك | الدليل المطلوب لإغلاق القبول |
|---|---|---|---|---|---|---|
| **REC-01** | تداخل Status Bar أثناء التمرير | `HomeDashboardScreen.kt` | Source Reviewed (حماية 120px موجودة) | Visual Acceptance Pending | G0-03 / UX-01 | فيديو قصير وصور عند 0/60/120px بالمظهرين للـAPK المرشح الجديد. |
| **REC-02** | عداد الأذكار RTL | `AzkarScreen.kt`, `strings.xml` | Source Reviewed (النص المنسق موجود) | Visual Acceptance Pending | G0-03 / UX-03 | صور بمواضع الأرقام وسجل Semantics وTalkBack أثناء التزايد والاستعادة. |
| **REC-03** | زر التلاوات غير المرخصة | `QuranReaderScreen.kt`, `RecitationAvailability.kt` | Source Reviewed (علمRECORDINGS_APPROVED محجوب) | Visual Acceptance Pending | G0-03 / UX-03 | صور أزرار Picker وPlayer وتوضيح حالة عدم التوفر دون إظهار الزر كمعطل أعمى. |
| **REC-04** | عناوين نبي الإحسان | `NabiIhsanTitleCleaner.kt`, `NabiEpisodeCard.kt`, `NabiIhsanEpisodeDetailScreen.kt` | **Confirmed Code Gap** (البطاقات لا تستخدم الـCleaner) | Visual Acceptance Pending | G0-03 / UX-05 | جدول الكتالوج واختبار Integration وتوصيل الـCleaner بالبطاقة والتفاصيل بوضوح. |
| **REC-05** | إخفاء الأوصاف غير المتوفرة | `NabiIhsanEpisodeDetailScreen.kt`, `SanhyaStoryDetailScreen.kt` | Source Reviewed (حراس isNotBlank موجودة) | Visual Acceptance Pending | G0-03 / UX-05 | صور الحالات الثلاث (فارغ/فراغ أبيض/متوفر) لكل شاشة وتأكيد عدم وجود عناوين يتيمة. |
| **REC-06** | الشارات الفارغة في سنحيا | `QuranStoryCard.kt`, `SanhyaStoryDetailScreen.kt`, `MediaCard.kt` | Source Reviewed (حراس الشارات موجودة) | Visual Acceptance Pending | G0-03 / UX-05 | صور blank/nonblank للقائمة والتفاصيل وتأكيد عدم ظهور إطار أو مساحة فارغة. |

---

### 6. سجل النواقص والأدلة المطلوبة للمهام القادمة (Missing Evidence Log for G0-02..G0-06)

| المهمة Target | عنوان المهمة | الأدلة النواقص المطلوب إثباتها | طريقة وشروط الإثبات المطلوبة | التبعيات المتطلبة |
|---|---|---|---|---|
| **G0-02** | إعادة تأهيل البناء والاختبارات المحلية | **PASS & CLOSED (BUILD-20261018-G002-01)**: تطابق 629 ملف مصدر بـ app_inputs_manifest.json 100%؛ مطابقة SHA-256 للنسختين المؤرشفتين والبرمجيتين (Debug: `F4795258...`, Release: `C71F8945...`) بـ Byte-Identical؛ تأكيد versionCode 1 و versionName 1.0 عبر SDK aapt؛ 481 unit test بـ 0 فشل/خطأ؛ 0 Lint Errors (149 Debug warnings, 124 Release warnings across 3 modules). فجوة نطاق الإدخال: عدم تضمين `gradle.properties`/`gradlew` تصنف P1/P2 لهذه الإعدادات كـ Unproven دون إعاقة اختبار الهاتف. | تم التدقيق المستنداتي والأدلي المباشر لجميع ملفات القرص الصلب والـ APK وXML للوحدات الثلاث دون إعادة بناء أو تشغيل. | G0-01 (مكتمل) |
| **G0-03** | قبول التنقل والحالات بالهاتف | أدلة قبول 59 حالة شاشات، وSplash و Legacy، والقبول البصري لـ REC-01..06 | تشغيل الهاتف الفعلي (Xiaomi ac190123) على الـAPK المرشح المنتج في G0-02 | G0-02 |
| **G0-04** | إغلاق قبول الفيديو والبث بالهاتف | أدلة تشغيل 86 فيديو، ملء الشاشة، توقف الخلفية، واستعادة البث المباشر | تشغيل اختبارات `VideoRenderingRootCauseTest` و `VideoLifecyclePhoneTest` وسجل 86 فيديو بالهاتف | G0-02 |
| **G0-05** | قبول الترحيل وحماية البيانات | إثبات ترحيل Room v8 من الإصدارات القديمة وحفظ تفضيلات DataStore | تشغيل `MigrationTestHelper` واختبارات التخزين الفعلي بالهاتف | G0-02 |
| **G0-06** | قبول الصلاة والقبلة والإشعارات | إثبات مطابقة المواقيت للمراجع، دقة الققبلة، ووصول إشعارات الأذان المجدولة | تشغيل اختبارات المواقيت المرجعية والقبلة واختبارات الوصول بالألارمات على الهاتف الفعلي | G0-02 |

---

## مسارا الإغلاق وتفسير اعتماد دفعة الفحص

- مسار Beta الداخلي الحالي: G0-01→G0-02→G0-03/04/05/06، ثم الإصلاح الموضعي الذي ثبت لزومه وإعادة السيناريو المتأثر، ثم R7-03 بوضع Beta-Current. REC-01..06 ضمن هذا المسار بمعاييرها؛ لا ربطها بـF4/F5/F6 أوبكل تحسيناتPhase1.
- اعتماد ناتج دفعة فحص قد يعني اعتماد تقرير كشف عيوب بصدق حتى لو نتيجة المنتجFail؛ هذا يسمح ببدءUX-01/03/05 التابعة لإصلاح عيبها، ولا يمنحBetaPass. إغلاق عيبG0 يعاد فحصه في دفعة الإصلاح ويحدث سجله المرتبط بـG0-03؛ لا نضيف اعتمادًا دائريًا.
- مسار الميزات الجديدة: اعتماد كلTask حسب dependency،ثمF6-04،ثمR7-03 بوضعFeature-Release. لا يصبح F6-04 شرطBeta-Current.
- مسار النشر العام: يضيفR7-02 وقراراتالحقوق/هويةالإنتاجفقطعند اختياره. بقاءPENDING_LICENSE لا يتحولإلىTechnicalFail ولا إذن توزيع داخلي تلقائي؛ النطاقالمسموح يثبت بقرارD-02/D-10.
- أولوياتالمهامTask Priority هيترتيبتنفيذوليستشدةعيبمقاسة؛ عيباالمصدرمثبتانلايساويانوجودP0بصري. مصفوفةUI تفصلconfirmed_defect عنimprovement_opportunity وعنphone_verification_status وvisual_acceptance_status.
