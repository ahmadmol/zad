# إحسان — Pre-backend Batch 04

2026-10-21 — Asia/Damascus. متابعة Batch03 من شجرة العمل الحالية؛ لا إعادة تنفيذ للدفعات السابقة ولا قبول هاتف أو نشر عام.

## التنفيذ الفعلي

| المعرف | المنفذ في هذه الدفعة | المتبقي المنفصل |
|---|---|---|
| UX-04 | حفظ محلي صريح للعروض والطلبات؛ تحقق موحد قبل الإضافة والتعديل؛ صور اختيارية ومنح قراءة دائمة؛ استعادة مدخلات الصورة/الفلاتر/البحث وحوارات التعديل؛ حالات قراءة/كتابة/إعادة محاولة في المجتمع والملف | قبول الهاتف للنماذج/IME/إعادة إنشاء العملية/Light-Dark/RTL والخط والوصول |
| F6-03 / UX-05 | استكمال مساري نبي الإحسان وسنحيا: موضع استئناف يدوي بالثواني، تأكيد الإكمال والتراجع، استئناف آخر حلقة صحيحة، اختيار حلقة محفوظ، مفضلة/لاحقًا وأخطاء قابلة للاستعادة؛ بقاء Fahm Batch03 | التشغيل الفعلي/الشبكة/fullscreen/background على الهاتف؛ روابط ومحطات/وصفات غير متاحة تبقى غير متاحة إلى اعتماد بيانات موثقة |
| AR-01 / F5-01 | بحث التعلم العام يفصل فشل كل مصدر ويبقي نتائج المصادر السليمة؛ إعادة المحاولة وتصفير النتائج عند مسح النص؛ استعادة query/filter/tab؛ إعادة استخدام ArabicSearch | بقية مصادر البحث العامة ومعايير F5-01 الكاملة والقياسات |
| AR-03 | إسقاط مفضلات القرآن باستعلام LEFT JOIN واحد بدل استعلامات إضافية لكل صف؛ الحفاظ على الصفوف بلا محتوى مطابق؛ toggle داخل معاملة Room | تشغيل اختبار التزامن الفعلي على Android والقبول العام للمفضلات |
| AR-04 / AR-05 | إلغاء طلبات التفاصيل القديمة؛ أخطاء التخزين لا تظهر كنجاح أو مكتبة فارغة؛ تسلسل التعديلات؛ منع تعديل تقدم غير مقروء؛ توثيق ملاك البيانات وDI | بقية حدود startup/corruption/asset-loader/runtime DI والقبول الميداني |
| PS-03 / PS-04 | حد 8MiB و40 مليون بكسل للصور الجديدة ونوع raster مدعوم؛ منع arbitrary network/foreign file loading؛ قيد YouTube main-frame ضد credentials/ports/paths؛ رسائل فشل فتح التطبيقات؛ الإفصاح عن البيانات والتوقيع الداخلي | WebView/renderer/grants/Intents على الهاتف؛ D-02 وهوية الإنتاج وD-10 والحقوق/المتجر |
| PS-01 / PS-02 | لا تعديل أداء تخميني؛ حفظ EXPLAIN QUERY PLAN كدليل بنيوي فقط | قياس هاتف مصرح وميزانيات D-08 قبل تحسين الأداء |

## المجتمع والملف الشخصي

- أزرار «حفظ العرض محليًا» و«حفظ الطلب محليًا» وإفصاح بأن السجلات لا تُنشر ولا تصل إلى جهة، وأن الملف المحلي لا يثبت هوية أو ملكية رقم الهاتف. نجاح الحفظ لا يعرض إلا بعد اكتمال الكتابة.
- حدود مشتركة للإضافة/التعديل: عنوان120، وصف3000، فئة60، مدينة80؛ نوع OFFER/REQUEST وحالات AVAILABLE/PENDING/COMPLETED الموجودة. لا تغيير لعقد الحالات الدائم أو أرقام قاعدة البيانات.
- ملكية التعديل/الحذف من الصف المخزن والمستخدم المحلي الحالي؛ لا ملكية من الاسم أو قيم الطلب. USER/LEGACY/DEMO محفوظة؛ DEMO لا يفتح تواصلًا حقيقيًا. البحث والفلاتر تشمل الحالة والمصدر وتُستعاد.
- حوار حذف السجل يبقى عند الفشل، ولا يُغلق إلا بعد نجاح حذف الصف. فشل تنظيف الصور بعد نجاح حذف الصف لا يحول العملية إلى فشل كاذب. لا إزالة للصورة المرتبطة بصف التزم عند إلغاء ViewModel أثناء الكتابة.
- صور الطلب تستخدم OpenDocument مع grant دائم وحالة محفوظة؛ صور التحرير تستخدم OpenMultipleDocuments. بادئات `ihsan-image:` و`ihsan-album:` لا تتغير. تدعم الصور الجديدة JPEG/PNG/WebP/GIF ضمن الحدود؛ تُفحص أبعادها دون فك كامل في الذاكرة.
- مراجع content القديمة والملفات الخاصة الآمنة تبقى قابلة للقراءة. المراجع الشبكية/الملفات الأجنبية لا تُحمّل؛ تبقى القيمة المخزنة دون حذف. اختبار صور Android معزول الآن تحت مجلد fixture فريد، ولا يمس مخزن الصور الحقيقي حتى عند تشغيله مستقبلًا.
- الخروج من الملف المحلي يعطله فقط، ويبقي السجلات والصور والعرض الشخصي والتعلم وسائر تاريخ العبادة. لا مسح جماعي للبيانات ولا ادعاء secure account isolation.

## التعلم والشاشات

- تحسين الشاشات الحالية: المجتمع، إضافة العرض، طلب المساعدة، تفاصيل السجل، سجل المساهمات، إدارة العروض والطلبات، الملف وتعديله، البحث العام، وقوائم/تفاصيل/محفوظات/لاحقًا/بحث نبي الإحسان وسنحيا وآيات القصة.
- لا routes جديدة، ولا منصة تعلم أو corpus أو player ثانٍ. مصفوفة الشاشات **69 حالة**: الاحتفاظ بالـ67 السابقة وإضافة توثيق حالتي route الإدارة الموجودتين OFFER/REQUEST اللتين لم تكونا مسجلتين.
- LearningProgressStore يضيف metadata إلى UserPreferences نفسها: `learning_nabi_progress_v1` و`learning_sanhya_progress_v1`. صيغة version1/id/parentId/seconds/completed/updatedAt؛ التعديل يحفظ بقية IDs والصفوف ذات الصيغة غير المعروفة. مفاتيح المفضلة/لاحقًا ومفاتيح Fahm القديمة لم تتغير.
- الاستئناف والإكمال يدويان مع تأكيد الإكمال/التراجع. لا حفظ تقدم عند فتح الصفحة أو محاولة التشغيل أو الانتقال إلى YouTube. يُمرر موضع الثواني إلى المشغل المشترك الموجود. المكتبة تعرض آخر metadata مرتبطة بمعرّف محتوى موجود فقط، ويُمنع التعديل حتى تنجح القراءة.
- ترتيب المحتوى الرسمي ومعرفاته ومصادره والنص القرآني والأصول لم تتغير. لا ربط جديد لمحطات/وصفات أو فيديوهات غير موثقة. تعطل المحتوى أو المصدر يبقى حالة واضحة، وليس بيانات مولدة.
- الدعم البصري يستخدم Design System الحالي ومكونات مشتركة ورسائل عربية وحفظ الحالة وأزرار min48dp للتقدم؛ لا يُستنتج قبول بصري من المصدر/JVM. Light/Dark/RTL/font1/1.3/2/TalkBack والقص تحتاج APK نفسه على الهاتف.

## التحقق

- المستهدف النهائي قبل المجموعة الشاملة: **97 PASS**، failures/errors/skipped=0؛ اختبارات إضافية لرفض الصور، حدود المدخلات، حدود ملكية المصدر، فشل القراءة/التخزين، الطلبات المتأخرة، استعادة الاختيار والبحث، إعادة فتح DataStore حقيقي، عدم فقد المفضلة، إلغاء الكتابة بعد التزام الصورة، وتقييد روابط WebView.
- **Full JVM مرة واحدة: 594 PASS** = app33 + feature531 + designsystem30؛ failures/errors/skipped=0. مهام الاختبار الثلاث أُجبرت على التنفيذ بـ`--rerun`؛ لا إعادة للمجموعة الشاملة.
- بعد ذلك أُضيفت رسائل فشل فتح التطبيقات/المشاركة في callbacks الموجودة؛ تحقق الانحدار النهائي وبناء APK من المصدر الأخير: **97 PASS؛ failures/errors/skipped=0**. لا حالات فريدة إضافية فوق594.
- SQLite اصطناعي معزول: actual Kotlin migration SQL لجميع المسارات2/3/5/6/7/8/9/10→11 و11→11، حفظ الحقول والجداول، integrity/reopen وعزل مسح الصيام: PASS. query الفعلي للمفضلات يحتفظ بالـorphan وduplicate bookmark rows وترتيبها دون تعديلها: PASS؛ query plan مؤرشف دون ادعاء latency أو تحسن أداء هاتفي.
- اختبارات Android للهجرة/تزامن المفضلات/الصور/المشروع: **PASS**، جُمّعت فقط ولم تُشغّل. لا Emulator/AVD/ADB أو فتح قاعدة هاتف أو تثبيت APK.
- **Room 11 دون تغيير schema أو migration**. المخططات9/10/11 والترحيلات التاريخية مطابقة للبداية. النصوص والأصول وأرقام الهوية/الأدوات/الاعتماديات/Manifest لم تتغير.
- Android Lint: **0 errors, 118 warnings**.
- Release QA مع R8/resource shrinking: **BUILD SUCCESSFUL — 5m 42s**.
- `git diff --check`: **PASS**. عدد الملفات السابقة المحفوظة خارج ملكية الدفعة: **127**؛ كلها مطابقة لبداية الدفعة.

## APK والهوية

`<LOCAL_USER_HOME>/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/.artifacts/prebackend-batch04-20261021/IHSAN-PREBACKEND-BATCH04-20261021-release-qa.apk`

SHA-256: `5107DECCBEAD83BDC81345064AEAA9061B9185668409C325E5348FCDEB8287CC`

الحجم: **31,844,598 bytes**؛ مطابق لخرج Gradle. الحزمة `com.example.mol.referenceqa`، versionCode1/versionName1.0، minSdk25/targetSdk35. توقيع **v2 PASS** بشهادة Android Debug QA السابقة:

`870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`

نسخة QA داخلية، ليست هوية إنتاج معتمدة. لم تُثبّت على الهاتف.

## الملفات المعدلة أو الجديدة في Batch04

**71 ملفًا**؛ القائمة الدقيقة التالية خاصة بهذه الدفعة، مع الاحتفاظ بتغييرات Sprint01 وBatch01/02/03 السابقة:

- `app/src/main/java/com/example/mol/di/RepositoryModule.kt`
- `docs/RELEASE_SIGNING_GUIDE.md`
- `docs/planning/IHSAN_ARCHITECTURE_AND_DATA_PLAN.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN.md`
- `docs/planning/IHSAN_PREBACKEND_CAMPAIGN_PROGRESS.csv`
- `docs/planning/IHSAN_UI_UX_SCREEN_MATRIX.csv`
- `docs/qa/IHSAN_PREBACKEND_BATCH04_20261021.md`
- `docs/store/DATA_SAFETY_DISCLOSURE.md`
- `feature/src/androidTest/java/com/example/feature/core/data/local/database/BookmarkTransactionTest.kt`
- `feature/src/androidTest/java/com/example/feature/ehsan/data/image/EhsanImageStoreInstrumentedTest.kt`
- `feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt`
- `feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt`
- `feature/src/main/java/com/example/feature/ehsan/EhsanScreen.kt`
- `feature/src/main/java/com/example/feature/ehsan/data/image/EhsanImageStore.kt`
- `feature/src/main/java/com/example/feature/ehsan/data/image/ImageImportPolicy.kt`
- `feature/src/main/java/com/example/feature/ehsan/data/repository/EhsanRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/ehsan/domain/model/LocalListingValidation.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/AddEhsanScreen.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/AddEhsanViewModel.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/EhsanViewModel.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/IhsanDetailsScreen.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/IhsanDetailsViewModel.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/OfferImagesPicker.kt`
- `feature/src/main/java/com/example/feature/ehsan/presentation/RequestHelpScreen.kt`
- `feature/src/main/java/com/example/feature/learning/data/LearningProgressStore.kt`
- `feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt`
- `feature/src/main/java/com/example/feature/learning/presentation/components/LearningReadState.kt`
- `feature/src/main/java/com/example/feature/learning/presentation/components/ManualLearningProgressCard.kt`
- `feature/src/main/java/com/example/feature/learning/presentation/components/YouTubeNavigationPolicy.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/data/repository/NabiIhsanRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/di/NabiIhsanModule.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanEpisodeDetailScreen.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanEpisodesScreen.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanFavoritesScreen.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanMainScreen.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanRecipeDetailScreen.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanRecipesScreen.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanSearchScreen.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanViewModel.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanWatchLaterScreen.kt`
- `feature/src/main/java/com/example/feature/nabiihsan/presentation/components/NabiSourceCard.kt`
- `feature/src/main/java/com/example/feature/profile/DonationHistoryScreen.kt`
- `feature/src/main/java/com/example/feature/profile/ProfileScreen.kt`
- `feature/src/main/java/com/example/feature/profile/presentation/EditProfileScreen.kt`
- `feature/src/main/java/com/example/feature/profile/presentation/EditProfileViewModel.kt`
- `feature/src/main/java/com/example/feature/profile/presentation/ProfileViewModel.kt`
- `feature/src/main/java/com/example/feature/quran/data/local/dao/QuranDao.kt`
- `feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/sanhya/data/repository/SanhyaRepositoryImpl.kt`
- `feature/src/main/java/com/example/feature/sanhya/di/SanhyaModule.kt`
- `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaEpisodesScreen.kt`
- `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaFavoritesScreen.kt`
- `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaMainScreen.kt`
- `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaQuranVersesScreen.kt`
- `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaSearchResultsScreen.kt`
- `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaStoryDetailScreen.kt`
- `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaUiState.kt`
- `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaViewModel.kt`
- `feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaWatchLaterScreen.kt`
- `feature/src/test/java/com/example/feature/dashboard/GlobalLearningSearchRecoveryTest.kt`
- `feature/src/test/java/com/example/feature/ehsan/LocalCommunityRecoveryTest.kt`
- `feature/src/test/java/com/example/feature/ehsan/LocalListingValidationTest.kt`
- `feature/src/test/java/com/example/feature/ehsan/data/image/ImageImportPolicyTest.kt`
- `feature/src/test/java/com/example/feature/ehsan/presentation/AddEhsanAlbumFailureTest.kt`
- `feature/src/test/java/com/example/feature/learning/LearningProgressPersistenceTest.kt`
- `feature/src/test/java/com/example/feature/learning/LearningRecoveryTest.kt`
- `feature/src/test/java/com/example/feature/learning/YouTubeNavigationPolicyTest.kt`
- `feature/src/test/java/com/example/feature/profile/presentation/ProfileRecoveryTest.kt`
- `feature/src/test/java/com/example/feature/quran/data/QuranBookmarkProjectionTest.kt`
- `scripts/verify_batch03_migration.py`
- `scripts/verify_bookmark_projection.py`

## الاستئناف التالي والقبول

- حدّثت الحملة وسجل الأربعين مهمة ومصفوفة69؛ الحالات تعكس التنفيذ الفعلي مع إبقاء القبول الميداني منفصلًا. لا إغلاق شامل لـF6-03 أوUX/AR/PS استنادًا إلى العدد وحده.
- Batch05: استكمال UX-01/02/03 وبقية AR-01/04 على مصادر غير التعلم؛ مراجعة F4-06: factory موجود، وتكامل launcher/provider يحتاج قرار D-07 قبل جزئه التابع. لا تعِد بناء الأنظمة المكتملة.
- PS-01 يحتاج القياس الميداني وD-08 قبل PS-02. PS-03 media/URI وF6-04 قبول الرحلات المتكاملة على APK محدد يحتاجان الهاتف وموافقة التثبيت المنفصلة. D-02 للإنتاج وD-10 للحقوق باقيان، ولا توجد موافقة نشر ضمن هذه الدفعة.
- Backend فقط: النشر العام، التحقق من الهاتف، المصادقة الآمنة، المزامنة، وصول البلاغات إلى مسؤول، والإدارة/المعاملات الحقيقية. لم يُنشأ Backend أو Firebase/API Server أو إجراءات وهمية تدّعي ذلك.
- UI COMPLETE: **NO**.
- LOCAL FUNCTIONALITY COMPLETE: **NO**.
- BACKEND INTEGRATION: **PENDING**.
- PHYSICAL QA: **PENDING**.

لا Commit أوPush أوReset/Clean، ولا ترقية Kotlin/Gradle/AGP أو تغيير هوية التطبيق أو بيانات الهاتف.
