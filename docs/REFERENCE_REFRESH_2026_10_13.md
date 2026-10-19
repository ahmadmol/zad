# تحديث مراجع إحسان — 13 أكتوبر 2026

## حدود التنفيذ

المشروع Kotlin / Jetpack Compose، بوحدات app وfeature وdesignsystem؛ لا ينطبق flutter analyze.
توجد تعديلات سابقة محفوظة. snapshot البداية في `.artifacts/reference-refresh-20261013/baseline.patch` و`baseline-status.txt`.
لا commit أو push. لا تغييرات Room أو models للصور في Phase 1.

## تحليل الشاشات ومصادر البيانات

| المرجع | الموجود | الحالة | التغيير المطلوب | البيانات / الخطر |
| --- | --- | --- | --- | --- |
| مجتمع إحسان وقائمتا العروض والطلبات | EhsanScreen / Donations | Existing / Needs Polish | هيدر وبطاقات وفلاتر مشتركة | Room محلي مع sample records؛ ليست منصة متصلة |
| تصفية إحسان | EhsanViewModel | Partially Existing | مدينة وتصنيف ونوع والأحدث فقط | لا بيانات مسافة أو تفاعل موثوقة |
| تفاصيل العرض والطلب | IhsanDetails | Existing / Needs Major Update | أقسام وعداد صور فعلي وأفعال مدعومة | إزالة محتوى ثابت غير مستمد من السجل |
| إضافة إحسان — المعلومات والتفاصيل | AddDonation / خطوات 1–2 | Existing / Needs Polish | مسودة محفوظة وكثافة compact | الحفاظ على business flow |
| إضافة إحسان — الصور | AddDonation / خطوة 3 | Partially Existing | حتى ثلاث صور محفوظة بالكامل | تقييم البنية عند الوصول لهذه الخطوة؛ migration معزولة إن لزمت |
| إدارة العروض والطلبات | لا وجهة مخصصة | Missing | شاشة مشتركة بنوع السجل | لا ملكية موثوقة بالاسم وحده؛ حالة نقص بدل إجراءات غير آمنة |
| سجل المساهمات | DonationHistory | Partially Existing | إزالة قائمة ثابتة | لا تنسب سجلات للمستخدم دون دليل ملكية |
| تعديل الملف | EditProfile | Partially Existing | الهاتف للعرض فقط، حفظ النبذة | الحفظ الحالي لا يخزن النبذة |
| الأدعية وأقسامها | Dua / DuaDetail | Existing / Needs Major Update | أقسام المرجع وربط نتائج فعلية | كتالوج محدود؛ لا اختلاق نصوص دينية |
| سنحيا — القائمة والبحث والتصفية | SanhyaMain / Search / FilterSheet | Existing / Needs Major Update | skyline وبطاقات وفلاتر مشتركة | كتالوج محلي وروابط مصادر |
| سنحيا — التفاصيل والحلقات | StoryDetail / Episodes | Existing / Needs Polish | VideoHero وأفعال وبطاقات مترابطة | تشغيل حسب توافر المصدر |
| سنحيا — الآيات والدروس | QuranVerses / التفاصيل | Partially Existing | عرض المحتوى المتاح فقط | لا مشغل صوت أو تقدم وهمي |
| سنحيا — المفضلة ولاحقًا | Favorites / WatchLater | Existing / Needs Polish | حفظ محلي وقائمة مشتركة | الاختيارات الحالية في الذاكرة |
| سنحيا — الفراغ والإعدادات | حالات البحث / Settings | Partially Existing | حالات وصف إعدادات القدرات | بعض المفاتيح لا تتحكم بوظيفة فعلية |
| نبي الإحسان — القائمة والبحث والتصفية | Main / Search / FilterSheet | Existing / Needs Major Update | بحث بإجراء صريح ومكونات مشتركة | مواضيع الكتالوج غير مكتملة |
| نبي الإحسان — تفاصيل الحلقة والسلسلة | EpisodeDetails / Episodes | Partially Existing | أقسام وhero والمصدر | الفيديوهات موجودة؛ نصوص وصفية عديدة فارغة |
| نبي الإحسان — الوصفات وتفاصيلها والعلاقات | Recipes / RecipeDetails | Partially Existing | واجهات نظيفة وحالات نقص | كتالوج الوصفات فارغ؛ لا اختلاق وصفات |
| نبي الإحسان — المفضلة ولاحقًا | Favorites / WatchLater | Existing / Needs Polish | حفظ محلي للأفعال المدعومة | لا نموذج لاحقًا مستقل للوصفات |
| نبي الإحسان — Empty / Offline / Error / Settings | حالات موزعة | Partially Existing | حالات داخل الصفحة وإعدادات صادقة | retry مرتبط بعملية فعلية |

## بوابة Phase 1

الحالة: مكتملة قبل بدء Phase 2. التُقطت عشر صور فعلية، وكُشف ضعف تباين الهيدر الداكن وفراغ صور الوسائط، وأُصلحا ثم أُعيد التصوير والمراجعة.
الشاشات: Home / Ihsan / Profile / Prayer / Sanhya، Light + Dark عند 393dp.
الألوان الداكنة الرسمية محفوظة؛ الهندسة واحدة بين الثيمين. ورقة القرآن كريمية مستقلة عن chrome.
بدأ بناء baseline بـJava 25 وفشل قبل التجميع؛ تستخدم حزمة QA Java 21 معزولة وحزمة تطبيق منفصلة على محاكي.

## النتائج النهائية

اكتملت تغييرات المراحل الخمس. أُعيد استخدام المعمارية والتنقل والمصادقة الحالية، دون commit أو push أو حذف تعديلات سابقة. APK التسليم يحمل applicationId الأصلي؛ التشغيل الآلي استخدم حزمة QA منفصلة على محاكي Android 14، دون تثبيت أو تغيير التطبيق على الهاتف المتصل.

### ما نُفذ

| المرحلة | النتيجة |
| --- | --- |
| 1 | Hero / Compact Skyline / Minimal، safe area، بحث وchips وsheet وشارات وحالات مشتركة، MediaCard / VideoHero / Favorite / WatchLater / RelatedContentCard، ألوان Dark الرسمية دون تغيير dimensions أو typography بين الثيمين |
| 2 | تصفية بالمدينة والتصنيف والنوع المدعومين، ترتيب الأحدث، تفاصيل مستمدة من السجل، صور حتى ثلاث، حفظ المرحلة والمدخلات عبر saved state، شاشة إدارة صادقة، سجل مساهمات بلا عناصر مختلقة، الهاتف للعرض والنبذة المحلية حتى 200 حرف |
| 3 | الأقسام السبعة، بحث ومفضلة وتفاصيل وكلمة اليوم الحالية؛ الأقسام غير المغطاة تُظهر نقص المحتوى |
| 4 | قائمة وتفاصيل وحلقات وآيات وبحث ومفضلة ولاحقًا وإعدادات سنحيا بالمكونات المشتركة؛ اختيارات محفوظة في DataStore |
| 5 | نفس الأساس لنبي الإحسان؛ البحث ينتقل فقط عند الإرسال الصريح؛ واجهات وصفات وتفاصيل وعلاقات مع حالة نقص للكتالوج الفارغ، إعدادات غير المنفذة معطلة |

### قرار الصور وRoom والملكية

عند Step 3 فُحص الاختيار والحفظ والعرض والحذف وقواعد النسخ الاحتياطي. حقل imageUrl الموجود يستوعب مرجع ملف manifest مرتب `ihsan-album:`، وأول صورة تُستخدم غلافًا. تُنسخ الملفات ثم يُنشر manifest ذريًا، ويُنظف الفشل الملفات الجديدة. المراجع القديمة والصورة الواحدة تبقى مدعومة. **لم تتغير Room schema أو Donation / DonationEntity أو رقم قاعدة البيانات، ولا توجد migration جديدة.** تفاصيل القرار في [step3-storage-decision.md](../.artifacts/reference-refresh-20261013/step3-storage-decision.md).

لا توفر البنية الحالية معرّف مالك موثوقًا للسجل. تطابق الاسم أو الهاتف لا يُستخدم لإثبات الملكية. شاشة «عروضي» و«طلباتي» وسجل المساهمات تعرض «تعذر تحديد سجلاتك»، ولا تعرض قوائم منسوبة للمستخدم أو أفعال إدارة. توجد حراسة أيضًا في ViewModel لمنع استدعاء التعديل والحذف دون دليل ملكية.

### الفحوص

- `assembleDebug` و`testDebugUnitTest` و`lintDebug` للوحدات المعنية: ناجحة عبر Java 21. اختبارات الوحدات: **452 اختبارًا، صفر فشل أو خطأ** (app:33 / feature:390 / designsystem:29).
- Lint: صفر أخطاء؛ التحذيرات المتبقية app:118 / feature:22 / designsystem:9. لا baseline جديدة ولا تعطيل عام لـLint. أُعلن إذن الشبكة في feature، وأُزيلت حاوية قيود غير مستخدمة، ووُثقت false positives محددة لمنتجي الحالة اللذين يحدثان value فعليًا.
- بناء Android instrumentation: ناجح. اختبارات الصور على Android تغطي الترتيب والغلاف وإعادة الفتح والحذف والمراجع القديمة والتراجع عند فشل النسخ وملفات manifest التالفة وتنظيف الملفات اليتيمة.
- حفظ DataStore الفعلي للنبذة وعزل مجموعات المفضلة ولاحقًا، البحث بإجراء صريح، استعادة مرحلة المسودة ومدخلاتها، وتطابق هندسة الهيدر بين الثيمين: تم اختبارها.
- Responsive: 224 حالة إنتاجية (14 شاشة × 4 عروض × 2 ثيم × 2 قياس خط). العروض 360 / 393 / 412 / 600dp، RTL، خط 1.0 و1.3، تمرير حقول وأفعال النماذج وشريط التنقل. المحاكي يعرض هذه العروض عبر LocalDensity وLocalConfiguration؛ ليست أجهزة مادية متعددة. فحص ارتفاع النص آلي، والقص الأفقي مراجعة بصرية؛ قياس امتداد الفقرة RTL لا يُستخدم كدليل قص لأنه أعطى false positives على chips كاملة الظهور.

سجل البناء النهائي: [final-delivery-checks.log](../.artifacts/reference-refresh-20261013/final-delivery-checks.log). الإحصاءات: [qa-results.json](../.artifacts/reference-refresh-20261013/qa-results.json).

مصفوفة الجهاز النهائية: [final-matrix-accepted.log](../.artifacts/reference-refresh-20261013/final-matrix-accepted.log)، 224 حالة ناجحة. اختبارات Android للمصحف والأساس وتخزين الصور وDataStore والبحث: [final-device-regressions.log](../.artifacts/reference-refresh-20261013/final-device-regressions.log)، 9 اختبارات ناجحة. لقطات كل مرحلة مع تأكيدات البيانات والحالات: final-phase2/3/4/5-capture.log، كل منها ناجح. استعادة المسودة ولوحة المفاتيح الفعلية: [final-keyboard-qa.log](../.artifacts/reference-refresh-20261013/final-keyboard-qa.log)، اختباران ناجحان، مع التمرير للوصول إلى حفظ وإلغاء في الثيمين. اختبار مستقل لفشل حفظ السجل يتحقق من rollback للألبوم الجديد.

### الصور والقبول البصري

المراجع الكبيرة هي المرجع الأعلى. المقارنة تشمل الكثافة والهيدر والبطاقات والألوان وقص الصور. للشاشات التي لا يوجد لها مرجع مباشر، حافظت المراجعة على تكوين التطبيق وهويته. لا توجد زيادة عامة للخطوط أو padding. أُصلح تباين الهيدر الداكن وأزرار حفظ الملف والكاميرا واختيار الصور، وأُعيد بناء compact header كسطر واحد.

- [صور بوابة Phase 1](../.artifacts/reference-refresh-20261013/foundation-gate/overview.png) — عشر لقطات Android فعلية عند 393dp، قبل توسعة الميزات.
- [Home / Ihsan / Profile / Prayer / Sanhya — النهائي](../.artifacts/reference-refresh-20261013/final-foundation-overview.png).
- [Nabi / Duas / Edit Profile / Add / Details — النهائي](../.artifacts/reference-refresh-20261013/final-features-overview.png).
- [الحالات والإعدادات](../.artifacts/reference-refresh-20261013/final-states-overview.png).
- [360dp، خط 1.3](../.artifacts/reference-refresh-20261013/final-360-font130-overview.png)، [600dp، خط 1.3](../.artifacts/reference-refresh-20261013/final-600-font130-overview.png).
- [ورقة القرآن في Dark](../.artifacts/reference-refresh-20261013/quran-background/360_dark_page1.png)، [ورقة القرآن في Light](../.artifacts/reference-refresh-20261013/quran-background/360_light_page1.png)؛ اختبار 16 حالة على صفحات 1/2/50/604 عند 360 و600dp، ومراجعة كريم/green/gold مستقلًا عن chrome.
- [لوحة المفاتيح Light](../.artifacts/reference-refresh-20261013/keyboard-qa/393_light_profile_keyboard.png)، [لوحة المفاتيح Dark](../.artifacts/reference-refresh-20261013/keyboard-qa/393_dark_profile_keyboard.png).
- اللقطات المفردة في `.artifacts/reference-refresh-20261013/refresh-matrix/` و`feature-refresh/`؛ تشمل تفاصيل وحلقات وآيات ومفضلة ولاحقًا وحالات نقص المحتوى.

### حدود البيانات والقدرات

المجتمع يعتمد على قاعدة البيانات المحلية الحالية، بما فيها سجلات تجريبية موجودة قبل العمل؛ ليس backend للمساهمات أو المدفوعات. لا إدارة آمنة لسجلات المستخدم دون هوية مالك في البيانات. لا تنزيل فيديو أو إعداد جودة أو إشعارات حلقات فعالة، لذلك إعداداتها معطلة. تشغيل YouTube الحالي محفوظ، والتضمين يخضع لعلم isEmbeddable الحقيقي. بعض صور سنحيا الخارجية لم تتحمل على المحاكي؛ يُعرض رمز كتاب محايد بدل صور مخترعة. بعض الأوصاف والمواضيع والمصادر غير متاحة، والوصفات فارغة. لا نصوص دينية أو مصادر مختلقة. عدم اختيار موقع/تعذر تحميل مواقيت الصلاة يظهر حالة نقص بدل أوقات مفبركة.

مسودة الإضافة محفوظة بين الخطوات وعند saved-instance restoration؛ ليست مسودة مستقلة محفوظة في قاعدة البيانات بعد إغلاق دائم أو force-stop. النسخ الاحتياطي للقاعدة والملفات الخاصة ما زال معطلًا وفق قواعد المشروع الحالية؛ لم يُنشأ مسار تصدير/استعادة غير مدعوم.

### الملفات والمكونات

توجد تعديلات سابقة كثيرة في checkout؛ `baseline.patch` و`baseline-status.txt` يميزان الحالة السابقة. أبرز ملفات هذا التنفيذ:

| المجموعة | الملفات / المسارات |
| --- | --- |
| الأساس | designsystem/.../component/IhsanSkylineHeader.kt, IhsanTopBar.kt, IhsanHeaderTitle.kt, IhsanSearchBar.kt, IhsanFilterComponents.kt, IhsanOfflineState.kt |
| الوسائط | feature/.../learning/presentation/components/MediaCard.kt, IhsanYouTubePlayer.kt, LearningSettingsScreen.kt |
| التخزين | feature/.../learning/data/LearningBookmarkStore.kt, core/preferences/UserPreferences.kt, ehsan/data/EhsanImageStore.kt |
| إحسان | ehsan/presentation/AddEhsanScreen.kt, AddEhsanViewModel.kt, IhsanDetailsScreen.kt, IhsanImageGallery.kt, OfferImagesPicker.kt, EhsanAdvancedFilters.kt، مكونات البحث والتصفية |
| الملف | profile/DonationHistoryScreen.kt, ManageListingsScreen.kt, ProfileScreen.kt, presentation/ProfileViewModel.kt, EditProfileScreen.kt, EditProfileViewModel.kt, EditProfileUiState.kt |
| الأدعية | duas/DuaScreen.kt, DuaViewModel.kt, DuaDetailScreen.kt, DuaSections.kt |
| سنحيا | sanhya/presentation/*Screen.kt, SanhyaViewModel.kt، components/{QuranStoryCard,StoryEpisodeCard,StorySearchBar,StoryEmptyState,QuranReferenceCard,QuranStoryVideoPlayer}.kt، data/repository/SanhyaRepositoryImpl.kt وdi/SanhyaModule.kt |
| نبي الإحسان | nabiihsan/presentation/*Screen.kt, NabiIhsanViewModel.kt، components/{NabiEpisodeCard,NabiHeroHeader,NabiFilterSheet,NabiSourceCard,NabiIhsanVideoPlayer}.kt، data/repository/NabiIhsanRepositoryImpl.kt وDI |
| الربط | app/.../navigation/Screen.kt ووجهات التنقل الحالية، app/.../di/RepositoryModule.kt |
| إصلاحات Lint | feature/src/main/AndroidManifest.xml، dashboard/DailyActivitiesScreen.kt، quran/presentation/QuranReaderScreen.kt، reminders/RemindersScreen.kt |
| الاختبارات | app/src/androidTest/.../ui/{SharedFoundationGateTest,FeatureRefreshAcceptanceTest,OfferAlbumStorageTest,LearningInteractionTest,RefreshResponsiveMatrixTest,HeaderGeometryTest,OfferDraftRestorationTest}.kt؛ اختبارات وحدة ownership وDuaSections وbookmarks وEditProfile |

نسخة التسليم: [Ihsan-reference-refresh-debug.apk](../.artifacts/reference-refresh-20261013/Ihsan-reference-refresh-debug.apk).
