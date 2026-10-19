# إحسان — خطة الهندسة والبيانات

2026-10-18 — Asia/Damascus. Planning Only. المرجع [التدقيق](IHSAN_AS_IS_AUDIT.md) و[سجل40 مهمة](IHSAN_MASTER_IMPROVEMENT_PLAN.md).

## البنية الحالية وما يجب الحفاظ عليه

app مسؤول عن launcher/DI/navigation/theme host؛ feature يضم data/domain/presentation لأغلب الوظائف؛ designsystem يقدم المكونات وtokens. Koin وViewModel/Coroutines/Flow وRoom/DataStore وWorkManager/AlarmManager/Media3 موجودة. لا دليل يبرر Flutter أو Hilt أو تقسيم عشرات الوحدات أو تبديل قاعدة التخزين. فصل feature كبير على المدى البعيد قرار كلفة بناء واختبارات وحدود اعتماد، وليس هدفًا لهذه الخطة.

الاتجاه التدريجي: UI يرسل intent صغيرًا إلى ViewModel؛ Use Case يعبر قاعدة عمل؛ Repository يملك SSOT وtransaction؛ DAO/DataStore يديران التخزين؛ platform gateway يعزل Sensor/Alarm/Media/URI. الحفاظ على العقود القديمة واختبارات characterization قبل تغييرها. لا تحريك ملفات لمجرد الاصطلاحات، ولا إنشاء layer بلا مسؤولية قابلة للاختبار.

| Concern | SSOT الفعلي أو المقترح | حماية الحد |
|---|---|---|
|النص القرآني والصفحات|quran_full.json/Tanzil المرفق؛ QuranAssetLoader→QuranDao؛ page/juz anchors|بحث normalized منفصل؛ IDs وcodepoints وترتيب ثابت|
|المصحف المعروض|MushafPageSurface والورق/Amiri/decorations|لا تحويل dark page أو تغيير text لتناسب الإطار|
|موضع القراءة|UserPreferences.saveLastRead ثم QuranRepository|snapshot زوج واحد AR-02؛ ليس completion ledger|
|المفضلات/تنزيلات مرخصة|QuranDao/DownloadDao|AR-03 transaction/JOIN؛ لا remote download قبل الإذن|
|مواقيت الصلاة|PrayerTimesFacade/AdhanPrayerCalculator/PrayerClock/PrayerSettings|Home وWidget والرَمضاني canonical selectors؛ لا calculator ثان|
|موقع المستخدم|PrayerLocationRepository/settings ومفاتيح UserPreferences القديمة|تحديد canonical provider مع compatibility؛ لا background tracking جديد|
|جدولة التنبيهات|ReconcilePrayerScheduleUseCase→AndroidPrayerAlarmGateway ledger|تغيير settings/zone لا يترك alarms قديمة؛ registrar ليس receipt|
|دقة القبلة|bearing usecase/math منفصل عن QiblaManager accuracy/declination|لا دمج ثقة sensors بثقة الزاوية|
|عداد الأذكار/السبحة|Azkar Room؛ daily checklist UserPreferences|لا نسخ counters إلى جدول feature آخر؛ rollover ذري|
|التعلم|LearningBookmarkStore وFahmRepository DataStore state_v1|كل collection واضح؛ لا wipe عند sign-out local|
|المجتمع والهوية|Room USER/LEGACY/DEMO وownerId local-profile|لا إثبات ملكية من اسم/هاتف؛ لا ادعاء secure login/remote charity|
|خطة الختمة/سجل العبادة الجديد|repos وجداول محلية مقترحة بعد اعتماد feature|الإنجاز إدخال صريح مستقل عن الموضع؛ لا analytics|

## خطوات Architecture المحددة

1. **AR-01:** احتفظ بمحركات البحث الحالية وأصلح transition error/restoration. ViewModel البحث لا يخلط فشل مصدر مع empty ناجح لباقي المصادر. UI لا يعيد فتح نتائج قديمة لأن query تشابه مؤقتًا. تحقق من مسؤولية كل VM قبل تجميعها؛ façade موحد يضاف فقط إذا خفف تنسيقًا مثبتًا، لا لأن7 VMs رقم كبير.
2. **AR-02:** واجهة read-position snapshot من emission واحد بدل combine لفصل surah/ayah، وdaily snapshot/date mutator واحدة. Clock/Zone injectable ومتفق عليهما في repository/preferences؛ لا reset غير مشروط من observer. اختبارات interleavings قبل تعديلها؛ الضياع المحتمل ليس claim incident.
3. **AR-03:** projected bookmark query يرجع surah name وverse text في عدد ثابت من الاستعلامات، واختبار الضغط/تعارض mutations. atomic explicit setFavorite أكثر قابلية للفهم من toggle إن وافق السلوك المطلوب؛ تغيير معنى double tap لا يحدث خفية.
4. **AR-04:** error model محدود للعمليات الحساسة: Loading/Ready/Empty/Error(cause category,retryable)/Unavailable(reason)/PendingLicense. CancellationException تظل cancellation. IOException يعرض استردادًا؛ corruption لا يطمس بdefaults. لا تخزين Throwable أو PII في UI state persisted أو logs. Room.build lazy فلا يضمن catch في DI التقاط فشل القراءة؛ اختبر open/read boundary.
5. **AR-05:** DI graph اختبار لكل production root؛ active/legacy/foundation inventory. تحديث تعليقات ownership/hard-coded provider boundaries دون حذف موروث مجهول. ManualPrayerLog وQuranGoal foundations لا تُعلن نشطة حتى تتوفر كل طبقاتها. State hoisting تدريجي للمكونات ذات previews مستقلة؛ مؤقتات الساعة لا تعيد بناء corpus كله.

تقييم recomposition/memory في PS-01: scope DefaultPrayerTimesFacade وIhsanApp application scope مقبول مبدئيًا كعمر التطبيق؛ هذا لا يثبت leak. QiblaManager unregister موجود؛ افحص lifecycle subscriptions وتعدد المستمعين قبل أي إصلاح. text fitting داخل MushafPageSurface وloadMushafPages مرشحان trace؛ لا تخرج النص عن سياقه أو تصغر الحروف آليًا كحل وصول.

## Room الحالي: مصفوفة آمنة

النسخة المعلنة8؛ database name=ihsan_master_db. عشر جداول: azkar_table،daily_stats،duas،donations،users،hadiths،surahs،ayahs،bookmarks،downloaded_ayahs. schema8 لا foreign keys وفيه index واحد للآيات؛ هذه حقيقة schema وليست مطالبة إضافة قيود لكل جدول. PrayerLogEntity ليس جزءًا منه حاليًا. legacy database classes لا تثبت نشاطها؛ App DatabaseModule يربط IhsanDatabase.

| نقطة البداية | المسار في الكود | الإثبات المطلوب G0-05 |
|---|---|---|
|2|2→3→5→6→7→8|historical fixture موثق ونتيجة schema8 + جميع البيانات|
|3|3→5→6→7→8|nullable explanation؛ preservation|
|5|5→6→7→8|user.role rebuild دون فقد؛ دقة الأنواع/defaults|
|6|6→7→8|ownerId/origin/isActive؛ لا استنتاج USER من الاسم/الهاتف؛ DEMO exact fingerprint فقط|
|7|7→8|donations.contactMethod DEFAULT PHONE؛ لا تغيير الصفوف القائمة|
|8|reopen current|schema identity مطابق ومفضلات/قراءته/صور/PII سليمة|
|fresh|create8+seed recovery|114/6236 وcontents؛ لا seed duplicate أو reset عند restart|
|1 أو4 أو مجهول|غير معتمد|لا downgrade/fallback؛ استرجع التاريخ أولًا؛4 قفزة تاريخية وليست ترحيلًا موجودًا|

المصفوفة القديمة docs/ROOM_MIGRATION_MATRIX.md تقول5؛ تحديثها مستقبلي G0-05، ولم نعدلها الآن. الاختبارات المرجعية الفعلية: feature/src/androidTest/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationTest.kt وfeature/src/test/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationMatrixUnitTest.kt. اختبارات نص SQL وحدها لا تكفي؛ MigrationTestHelper على fixture في الهاتف المصرح هو الدليل التنفيذي. دعم SQLite API25 لا يثبت من جهازAPI35؛ يسجل كحد تغطية.

## مواصفات البيانات المقترحة — ليست كيانات موجودة

الأسماء أدناه عقود مرشحة تحدد في دفعاتها، وليست paths مؤكدة. **لا تخصيص رقم9 الآن**: كل دفعة تكتشف schema current ثم current+1 وتختبر جميع المسارات؛ يحظر تعديل historical schema بعد نشره. التنفيذ تسلسلي لا ترحيل واحد لكل أحلام المنتج.

| Task | aggregate والحقول الدنيا | القيود/المعاملة | تاريخ البيانات القديمة |
|---|---|---|---|
|F4-02|KhatmaPlan(id,type,startEpochDay,dueEpochDay,startPage,endPage,policy,status,createdAt,version)؛ PlanDay(planId,epochDay,fromPage,toPage)؛ Completion(planId,day/pageRange,confirmedAt,origin)|نطاق1..604؛ unique plan+day؛ create/replan transaction؛ preserves completed history؛ اكتمال نطاقات لا عد duplicate|لا استنتاج إكمال من last-read؛ عرض اقتراح لمرة واحدة فقط بموافقة|
|F4-04|PrayerLogEntity الموجود(dateEpochDay,prayerName,status,loggedAtEpochMillis)|تستعمل primary key الموجود day+prayer بعد فحص تعريفه؛ exclude Sunrise؛ upsert explicit status؛ serialize UI writes؛ clearAll خاص|لا توجد صفوف بschema8 لهذه entity؛ لا تفترض بياناتها من صفحة QA قديمة؛ أي مخزن تاريخي يظهر يُحلل أولًا|
|F4-05|summary مشتق من logs لا جدول counts جديد|day/week/month range query؛ unrecorded منفصل؛ Clock date rollover|لا ترحيل جديد للملخص؛ لا اعتماد العد المار مع الوقت|
|F5-04|MemorizationRange(id,startSurah,startAyah,endSurah,endAyah)؛ ReviewEvent(rangeId,date,result,nextDue,algorithmVersion)|verse keys صحيحة؛ مراجعة schedule حتمية وإكمال يدوي؛ transaction event+nextDue|لا استنتاج حفظ من تشغيل أو قراءة أو repeat|
|F5-06|AyahNote(id,surahId,ayahNumber,text,createdAt,updatedAt)|طول/ترميز معقول حسب UX؛ فهرس verse key؛ multi-note policy معلنة؛ لا HTML executable|note نوع خاص؛ لا إدماج في tafsir/source corpus|
|F6-01|FastingLog(epochDay,kind,userStatus,createdAt,updatedAt)|unique date حسب D-03؛ status يدوي قابل للمحو؛ timezone policy صريحة|لا inference من رمضان/الموقع/الصلاة|
|F5-02|Hijri prefs(offset,calculationLabel,version)|حد offset بحسب D-09؛ provider date pure/Clock؛ لا تعديل prayer times|تاريخ header القديم يصبح نفس provider بلا إسقاط تفضيلات|
|F4-06|widget snapshot مشتق مؤقت(nextInstant,lastUpdated,availability,resumeTarget)|لا duplicate calculator؛ sanitize PendingIntent arguments؛ لا worship history على launcher بلا اختيار|لا migration user DB؛ reload من repositories|

قبل إضافة FK/UNIQUE: تقرير orphan/duplicate على fixture من المخطط المعروف، جدول قرار لتسوية السجلات دون حذف صامت، indexes ذات فائدة مثبتة EXPLAIN QUERY PLAN، ثم اختبار upgrade مع rollback/forward recovery. القرآن مفاتيحه ثابتة، لكن legacy rows قد لا تتطابق؛ لا cascade deletes على محتوى ديني أو USER/LEGACY بلا قرار. لا usecase عابر لعدة مستودعات يدعي Room transaction تشمل DataStore والملفات.

## DataStore وتطور الحالة

UserPreferences يضم last-read،مكانًا قديمًا،أهداف checklist،user bio/avatar وlearning sets. SettingsManager وprayer settings مستودعات أخرى؛ جرد keys/runtime owners قبل تغييرها. قواعد التطوير:

- snapshot واحد للحقول المترابطة؛ mutations للـdate+count في edit واحدة. preserve learning IDs عند حذف local profile كما هو السلوك المقصود.
- تغيير key بإصدار واضح: read-old→map→write-new atomic حيث أمكن ثم mark migrated؛ لا dual writers إلى أجل مفتوح ولا حذف القديم قبل إثبات نجاح migration.
- Fahm state_v1 pipe-separated له parser يتسامح malformed؛ عند تمديد الحقول استخدم format/version صريحًا واختبر old/new/malformed/unknown fields دون فقد favorites أوlastPlayedSeconds.
- corruption سياسة منفصلة عن transient IO. لا catch عام يخفي الخطأ ويعرض صفراً يوحي بمحو السجل. أي reset يحتاج قرارًا واعيًا ونسخة مستردة وفق خصوصية المستخدم.
- pairing Room/Files: row changes أولًا ثم cleanup orphan قابل للإعادة. حذف profile/usecase لا يساوي حذف notes/fasting/plans تلقائيًا؛ D-03 يحدد النطاق ويحدّث disclosure والاختبارات.

## Offline-first والشبكة

المصحف والمحتوى والتقدم المحلي تعمل دون إنترنت؛ learning thumbnail/video والportal يتطلبان شبكة مع offline state/ retry صريح. حدود sync الحالية **لا خادم ولا sync**؛ لا نضيف backend لمعالجة state محلي. timeout25ث للمشغل موجود، downloader connect15ث/read30ث موجودان ويفصلان transient/permanent/cancel؛ هذه قيم فعلية لا مطالبة تقليلها عشوائيًا. قيّم تجربة بطء الشبكة والرندر وتطبيق retry bounded/backoff فقط عند فشل تقني مناسب، لا retry تلقائي لا نهائي على150 أو PendingLicense. لا تعيد تنزيلات YouTube ولا تستخرج HLS.

## الخصوصية والنسخ والتراجع

allowBackup=false واستبعادات cloud/device-transfer موجودة؛ لا تغيّرها لمجرد طلب note export. F5-07 يستخدم SAF اختيارًا صريحًا مع preview ويصدر notes المحددة فقط. restore عام مؤجل إلى contract version/checksum/validation/conflict/PII decision؛ لا استيراد SQLite مجهول. encryption at-rest قرار threat model/keystore/recovery وكلفة، لا توصية تغيير تقني تلقائي؛ sandbox وحده لا يساوي حماية من جهاز مفتوح أو export.

المسار المستقبلي للتعافي: snapshot fixture آمن قبل migration؛ ترحيل على نسخة اصطناعية؛ integrity/row IDs/bookmarks/download paths/settings comparison؛ forward fix عند إخفاق live migration. لا تثبيت schema أقدم فوق أحدث ولا fallbackToDestructiveMigration ولا نسخ ملف SQLite مفتوح دون WAL/SHM consistency. هذه خطة وليست إذن قراءة قواعد الهاتف أو بياناته أثناء التدقيق.

**قبول الهندسة:** العقود واضحة، لا duplication،اختبارات cancellation/error/concurrency وDI/migration ناجحة،المفاتيح والـIDs محفوظة،تعليقات capability مطابقة للتنفيذ،قبول الهاتف منفصل،والقرارات D-03/D-05/D-06/D-07/D-09 محسومة قبل أجزائها التابعة.


## عقد عدم ازدواج الأنظمة — مراجعة الاتساق النهائية

- F4-01..03 تستكمل QuranGoalCalculator وتستعمل604 page anchors وQuranRepository وموضع القراءة الحالي. التخزين الجديد خاص بخطة/إنجاز لم يكن موصولًا، ولا ينشئ Quran corpus أوreader أوlast-read store آخر. موضع القراءة اقتراح اختياري وليس إنجازًا.
- F4-04..05 توصل PrayerLogEntity/PrayerLogDao/ManualPrayerLogRepositoryImpl/ManualPrayerTrackerViewModel/Card وPrayerTrackerStatsCalculator الموجودة عبرRoom وKoin؛ لا tracker ثانٍ أوcounter متوقع من الوقت. الفحص الحالي لا يجدها ضمن IhsanDatabase v8 أوDI النشط، لذلك الإكمال لا يكرر منتجًا جاهزًا.
- F4-06 تستخدم PrayerWidgetStateFactory وNextPrayerSelector وPrayerTimesFacade canonical؛ الإضافة هي provider/launcher وتحديثاته. لا حساب مواقيت ثانٍ ولا سجل صلاة شرط لعرض الصلاة القادمة؛ resume target من موضع القرآن نفسه.
- AR-01 وF5-01 يطوران GlobalSearchScreen/GlobalLearningSearchViewModel وArabicSearch ومحركات repos القائمة؛ لا محرك جديد ولا نسخة مطبعة تحل محل النص الأصلي.
- قبل تنفيذ أي منها: جرد callsites وDI/schema والمفاتيح مع characterization test؛ إن وجد تنفيذ أحدث صالح أعِد استعماله وعدل gap بدل تكراره. أسماء الكيانات الجديدة المقترحة تحتاج مقارنة بschema وقت التنفيذ.
- أزيل اعتماد الحفظ على خطة الختمة، والملاحظات على ميزة الحفظ، ورمضان علىWidget، لأنها ميزات مستقلة وظيفيًا. التبعيات الجديدة دقيقة في سجل المهام الرئيسي؛ migrations تسلسلية ولا تفترض نسخة ثابتة أوترحيلًا متوازيًا.
