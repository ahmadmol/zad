# إحسان — Pre-backend Batch 03

2026-10-21 — Asia/Damascus. استكمال العمل الذي بدأ 2026-10-20؛ حافظت الدفعة على Sprint01 وBatch01/02 وعلى بقية تغييرات المستخدم. لا قبول هاتف أو نشر.

## التنفيذ الحقيقي

| المعرف | المنفذ | الحالة المتبقية |
|---|---|---|
| F6-01 | تقويم صيام محلي بالشبكة ومحرك الهجري الموجودين؛ يوم مختار وحالة صريحة ووصف شخصي اختياري؛ تأكيد التسجيل والتعديل؛ حذف يوم ومسح الصيام وحده بتحذير وتأكيد مستقل؛ ملخص الشهر وسجل محلي محفوظ | المصدر/JVM/SQLite جاهزون؛ قبول Room/CRUD/sign-out/process restore والواجهة على Xiaomi معلق |
| F6-02 | مركز رمضان من الرئيسية؛ التاريخين، الفجر والمغرب من facade الحالية، حراسة اليوم/المنطقة/الموقع لمنع القيم القديمة، روابط القرآن والختمة والأذكار والأدعية والتقويم والصيام، تقدم يدوي حقيقي، إبراز موسمي اختياري محفوظ | قبول الملاحة/المواقيت/الخط والسمات على الهاتف معلق؛ لا ادعاء رؤية رسمية أو إمساك مستقل |
| F6-03 | تطوير مسار Fahm الحالي: ترتيب شخصي محفوظ للدروس، اختيار متابعة يدوي وموضع ثوانٍ اختياري، إكمال/إلغاء تسجيله، استكمال المشغل من الموضع المحفوظ، عدم بدء الدرس التالي تلقائيًا؛ بحث عربي واستعادة query/tabs؛ معالجة فشل الحفظ/القراءة والمحتوى المفقود | PARTIAL فعلًا: قبول الهاتف وبقية معالجة أخطاء/استعادة/سباقات Nabi/Sanhya والربط المحتوائي الموثق باقية؛ لا مكتبة جديدة |
| G0-05 | Room11 بترحيل10→11؛ فحص SQL الفعلي على fixtures، حفظ جميع الجداول السابقة، إثبات عزل حذف الصيام وإعادة الفتح؛ اختبار Android حقيقي للـDAO وlogout والترحيل جُمّع | Android MigrationTestHelper لم يُشغّل؛ ليس Device PASS |
| F6-04 / UX-05 | توسيع مصفوفة الشاشات إلى67 صفًا (61 أصلية +6 مسارات حملة) وتحسين حالات Fahm في الشاشات الموجودة | قبول الرحلات المتكامل والسمات/الإتاحة والهاتف وبقية التعلم/البث معلق |

## D-03 والبيانات

D-03 **APPROVED — المستخدم، Batch03** وفق رسالته في هذه المحادثة؛ موثق في سجل القرارات. «صمت» و«لم أصم» سجلات صريحة، وغياب الصف «غير مسجل». النوع نص شخصي اختياري حتى80 حرفًا دون تصنيف فقهي أو أحكام قضاء/فدية. تأكيد التعديل يوضح استبدال سجل اليوم. مسح الكل يخص الصيام فقط؛ لا ربط للحذف بتسجيل الخروج، ولا تُمس الصلاة أو الختمة أو الحفظ أو الملاحظات.

- Room الفعلي قبل الدفعة **10** وبعدها **11**. `MIGRATION_10_11` يضيف `fasting_log` فقط: PK اليوم المدني، status، typeLabel، updatedAtEpochMillis. اسم القاعدة `ihsan_master_db` ثابت؛ لا تغيير للمخططات أو الترحيلات التاريخية ولا destructive fallback.
- مفتاح اليوم المدني مستقل عن تعديل عرض الهجري. تعديل −2..+2 يغيّر نطاق الشهر المعروض فقط، ولا يحرك صفوف السجلات. يمنع المستودع تسجيل المستقبل. upsert لليوم نفسه يعدل صفًا واحدًا؛ لا inferred fasting من مرور اليوم أو دخول رمضان.
- الحالات المخزنة غير المعروفة تبقى موجودة، وتظهر بوصفها مجهولة ولا تُحتسب «صمت» أو «لم أصم» أو يومًا بلا صف. إحصاء الشهر حتى اليوم، ولا تُصنّف أيام المستقبل.
- `UserRepositoryImpl.logout` يوقف الملف المحلي فقط؛ لم يُربط بمستودع الصيام. اختبار Android المضاف يختبر بقاء سجل الصيام عند logout على قاعدة اختبار معزولة، لكنه لم يُشغّل على الهاتف.
- إعداد إبراز رمضان اختياري ومعطل افتراضيًا في DataStore؛ عند التفعيل يعمل خلال الشهر9 الحسابي فقط، ويمكن إيقافه دون حذف بيانات أو إخفاء الرابط الدائم. لا إعلان بداية رمضان الرسمية، ولا وقت إمساك أو حدود شرعية جديدة. الفجر والمغرب هما millis نفسها من `PrayerTimesFacade` ويُنسّقان بـ`PrayerTimeFormatter` الحالي. التعديل الهجري لا يدخل إلى حساب الصلاة.
- الختمة من `ReadingJourneyRepository` وخطة القراءة الحالية، وتحتسب فقط الصفحات المؤكدة داخل نطاقها؛ الموضع الأخير للقراءة ليس إنجازًا.

## التعلم دون تكرار النظام

لم يتغير catalog أو title/videoId أو ربط المحطات/الوصفات. تحافظ الحالة على مفتاح `fahm_episode_state_v1` الحالي، والمفضلة والمشاهدة لاحقًا والثواني والتقدم السابق. أضيف مفتاح منفصل لترتيب المستخدم `learning_fahm_playlist_order_v1`، مع الاحتفاظ بالمعرفات غير الموجودة حاليًا. تحديث الدروس المعروفة يحتفظ بسجلات المعرفات غير المعروفة بدل إسقاطها. لا ترحيل/تصفير لبيانات التعلم القديمة.

إكمال Fahm لا يضع الدرس التالي في IN_PROGRESS تلقائيًا. الاختيار اليدوي للمتابعة وحفظ موضع الثواني هما ما يحددان الاستكمال؛ لا تنزيل فيديو أو تقدم آلي جديد. انتقال واجهة نجاح الإكمال يحدث بعد نجاح التخزين فقط؛ الخطأ يبقى ظاهرًا مع إمكانية إعادة المحاولة. غياب الدرس أو خطأ القراءة لم يعد Loading بلا نهاية. البحث يستعمل ArabicSearch الحالي، ويلغي الطلب القديم ويستعيد النص والتبويبات عبر SavedStateHandle. ترتيب المستخدم ينعكس على السابق/التالي والمحفوظات دون تغيير ترتيب المصدر المنشور وبياناته.

## الواجهات

- جديد: `fasting_journal` و`ramadan_hub`، موصولان من الرئيسية والمركز بالمسارات الحالية.
- محسن: شبكة الهجري مشتركة بين التقويم والصيام، مع وصف يوم كامل وحالة التسجيل و48dp على الأقل وتمرير أفقي عند الحاجة للخط الكبير. نماذج الصيام واليوم والشهر والمسودات وحوارات التأكيد تستخدم rememberSaveable؛ أخطاء الكتابة لا تغلق المسودة.
- محسن: FahmJourney وFahmEpisodeDetail وFahmSearch وFahmSaved وFahmEpisodes وFahmMain. لا قارئ أو منصة تعلم أو حاسبة صلاة ثانية.
- Loading/Empty/Error/Retry، حالات offline وعدم الموقع، وLight/Dark/RTL من Design System الحالي. لا يُستنتج قبول بصري من المصدر؛ فحص الخط1/1.3/2 وTalkBack والقص والتباين والتنقل على الهاتف باقٍ.

## التحقق

- المستهدف الأول: **47 PASS**؛ الصيام ورمضان والتعلم والترحيلات، إضافة إلى تجميع اختبارات Android في app وfeature.
- **Full JVM مرة واحدة: 568 PASS** (app33، feature505، designsystem30)، failures/errors/skipped=0. أُجبرت مهام الاختبار الثلاث نفسها على التشغيل بـ`--rerun` دون إعادة المجموعة الشاملة مرة ثانية.
- بعد اكتشاف قراءة خاصية Semantics للكتابة فقط في وصف الأيام المسجلة، صُحّح بناء الوصف وأضاف اختبار JVM يستخدم `SemanticsConfiguration` الفعلي، وليس mock. المستهدف النهائي **43 PASS**، منه حالة واحدة جديدة. الإجمالي المجمّع **569 حالة فريدة ناجحة**؛ ليس ادعاء Full Suite من569 دفعة واحدة.
- فحص SQLite المعزول: actual Kotlin migration SQL من2/3/5/6/7/8/9/10→11 و11→11، تطابق جميع الجداول/الأعمدة/الفهارس/FKs والحقول السابقة، integrity_check، CRUD/إلغاء التسجيل ومسح الصيام من ملف اصطناعي باستخدام قوالب DELETE الفعلية من DAO، مقارنة كل الجداول الأخرى وإعادة فتح الملف. هذه fixtures اصطناعية، وليست قاعدة مستخدم أو قبول Android.
- DataStore بملفات مؤقتة حقيقية: حفظ الإبراز/الهجري واستعادتهما؛ ترتيب التعلم والمفضلة ولاحقًا والثواني والإكمال ومفاتيح السجلات غير المعروفة بعد إعادة الفتح وتسجيل الخروج المحلي.
- اختبارات تغطي عدم الكتابة عند الرصد، duplicate day/tap، future/long type rejection، leap/month/offset/unknown state، read/write recovery، Fajr/Maghrib parity وstale zone/location/day، اختيار الموسم، حفظ النجاح قبل الملاحة، البحث/الدرس المفقود/offline وتبويبات الاستعادة. الاختبارات السابقة وسلامة القرآن ضمن المجموعة الشاملة.
- المخططان9 و10 مطابقان حرفيًا لبداية Batch03. لم يُعدّل نص القرآن أو أصول التعلم أو Kotlin/Gradle/AGP/Manifest/dependencies.
- Release QA النهائي: **BUILD SUCCESSFUL — 4m 7s**؛ R8/resource shrinking مفعّلان. اختبارات Android للمشروع جُمّعت على آخر المصدر فقط، ولم تُشغّل على هاتف. أُوقف بناء وسيط لإدخال تصحيح Semantics واختباره قبل بناء النسخة النهائية.
- Android Lint: **0 errors, 118 warnings**؛ لم يُدّعَ انعدام التحذيرات.
- `git diff --check`: PASS. جميع ملفات المستخدم المعدّلة عند بداية Batch03 والخارجة عن قائمة تعديلات هذه الدفعة بقيت مطابقة للبداية.

## APK والهوية

`<LOCAL_USER_HOME>/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/prebackend-batch03-20261021/IHSAN-PREBACKEND-BATCH03-20261021-release-qa.apk`

SHA-256: `8FC10DDC09650365F7F087AFB51470585E76EF8347D14FE040117A1C9037DEE5`

الحجم: **31,811,830 bytes**؛ مطابق حرفيًا لخرج Gradle النهائي. الحزمة `com.example.mol.referenceqa`، versionCode=1، versionName=1.0، minSdk25، targetSdk35. توقيع **v2 PASS** بشهادة **Android Debug** القائمة للاختبار الداخلي، SHA-256 للشهادة:

`870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`

لا تغيير في هوية التطبيق؛ ليست شهادة نشر عام. لم يُثبّت APK.

## الملفات المعدلة أو الجديدة في Batch03

- `app/src/main/java/com/example/mol/di/DatabaseModule.kt`
- `app/src/main/java/com/example/mol/di/RepositoryModule.kt`
- `app/src/main/java/com/example/mol/di/ViewModelModule.kt`
- `app/src/main/java/com/example/mol/navigation/AppNavHost.kt`
- `app/src/main/java/com/example/mol/navigation/Screen.kt`
- `docs/ROOM_MIGRATION_MATRIX.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN_PROGRESS.csv`
- `docs/planning/IHSAN_RISKS_AND_DECISIONS.md`
- `docs/planning/IHSAN_UI_UX_SCREEN_MATRIX.csv`
- `docs/qa/IHSAN_PREBACKEND_BATCH03_20261021.md`
- `feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase/11.json`
- `feature/src/androidTest/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationTest.kt`
- `feature/src/main/java/com/example/feature/calendar/HijriMonthGrid.kt`
- `feature/src/main/java/com/example/feature/calendar/IslamicCalendarScreen.kt`
- `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt`
- `feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt`
- `feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt`
- `feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/data/repository/FahmRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/di/FahmModule.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/domain/model/FahmPlaylistOrder.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/domain/repository/FahmRepository.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmEpisodeDetailScreen.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmEpisodesScreen.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmJourneyScreen.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmMainScreen.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmSavedScreen.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmSearchScreen.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmUiState.kt`
- `feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmViewModel.kt`
- `feature/src/main/java/com/example/feature/fasting/data/FastingLogDao.kt`
- `feature/src/main/java/com/example/feature/fasting/data/FastingLogEntity.kt`
- `feature/src/main/java/com/example/feature/fasting/data/FastingRepository.kt`
- `feature/src/main/java/com/example/feature/fasting/domain/FastingRecord.kt`
- `feature/src/main/java/com/example/feature/fasting/presentation/FastingScreen.kt`
- `feature/src/main/java/com/example/feature/fasting/presentation/FastingViewModel.kt`
- `feature/src/main/java/com/example/feature/ramadan/RamadanScreen.kt`
- `feature/src/main/java/com/example/feature/ramadan/RamadanSummary.kt`
- `feature/src/main/java/com/example/feature/ramadan/RamadanViewModel.kt`
- `feature/src/test/java/com/example/feature/core/CalendarDaySemanticsTest.kt`
- `feature/src/test/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationMatrixUnitTest.kt`
- `feature/src/test/java/com/example/feature/fahmanallah/FahmPlaylistPersistenceTest.kt`
- `feature/src/test/java/com/example/feature/fahmanallah/FahmViewModelRecoveryTest.kt`
- `feature/src/test/java/com/example/feature/fasting/FastingRepositoryTest.kt`
- `feature/src/test/java/com/example/feature/fasting/FastingStatsTest.kt`
- `feature/src/test/java/com/example/feature/fasting/FastingViewModelTest.kt`
- `feature/src/test/java/com/example/feature/ramadan/RamadanPreferenceTest.kt`
- `feature/src/test/java/com/example/feature/ramadan/RamadanSummaryTest.kt`
- `feature/src/test/java/com/example/feature/ramadan/RamadanViewModelTest.kt`
- `scripts/verify_batch03_migration.py`

## المتبقي والاستئناف

**UI COMPLETE: NO — LOCAL FUNCTIONALITY COMPLETE: NO — BACKEND INTEGRATION PENDING — PHYSICAL QA PENDING.**

F6-01/02 محليان جاهزان للفحص؛ قبولهما على الهاتف منفصل. F6-03 ما زال PARTIAL لمتبقي Nabi/Sanhya والربط الموثق والقبول الفعلي. مصدر التحسينات لا يغلق G0-03/04/05/06 أو F6-04 أو R7-03.

Batch04 التالية: بقية المجتمع UX-04 (النماذج/المسودات/الملكية)، UX-05/F6-03 للتعلم Nabi/Sanhya وأخطاء الاستعادة/السباقات، ثم AR-01/03/04/05 حسب الدليل؛ قياس PS-01 قبل أي ادعاء تحسين PS-02، ثم تدقيق PS-03/04 وقبول F6-04 المتكامل وفق الاعتماديات. لا إعادة بناء الدفعات السابقة ولا توليد خرائط محتوى أو مناسبات غير معتمدة.

الهاتف المطلوب بعد إذن تثبيت منفصل: Xiaomi، الترحيل/إعادة الفتح وحفظ بيانات التحديث، تسجيل/تعديل/حذف الصيام ومسحه وعزل الحذف/logout، تغير اليوم والمنطقة والoffset، الرحلات والتنقل وإعادة إنشاء العملية، مقارنة مواقيت المركز بالصلاة، حفظ التعلم وترتيبه/موضعه/فشل تخزينه، RTL/Light/Dark/font/TalkBack. لم يستخدم Emulator/AVD، ولم يثبّت APK أو تُقرأ/تُعدل بيانات هاتف. لا Backend أو Commit/Push/Reset/Clean.

الأدلة: `.artifacts/prebackend-batch03-20261021/`؛ نقطة الاستئناف في وثيقة الحملة وسجل الأربعين مهمة، مع فصل التنفيذ عن القبول.
