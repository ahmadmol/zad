# تقرير تحسين واجهات إحسان — 7 أكتوبر 2026

تم تحسين الواجهات مع الحفاظ على الصور والهوية الخضراء ومسارات التنقل. لم تُعدّل APIs أو models أو repositories أو قاعدة البيانات، ولم يُنفّذ commit أو push. التعديلات السابقة الموجودة في المشروع محفوظة.

## الإصلاحات

- الرئيسية: ترتيب معلومات الصلاة والعدّ التنازلي والآية والقبلة، وارتفاع مرن للـHero. بقيت جميع المعلومات والأفعال الموجودة.
- القرآن والأذكار والرحلة والتبرع: خلفيات ونصوص متوافقة في الثيمين، وارتفاع يتسع للنص. تتحول بطاقات المحتوى الصغيرة إلى قائمة عند العرض الضيق أو تكبير الخط.
- الخدمات: عمودان للهاتف وثلاثة عند مساحة محتوى كبيرة (560dp فأكثر، مناسبة لشاشة 600dp بعد الهوامش)، مع أسماء كاملة وأيقونات vector متجانسة 24dp وحاويات 36dp. مسارات الخدمات الست عشرة محفوظة.
- التنقل السفلي: ارتفاع مقاس فعليًا مع تكبير الخط، ويحجز Scaffold ارتفاع الشريط وهوامش النظام. مؤشر الاختيار موحد لكل عنصر مع انتقال 180ms.
- إحسان: تبويبات مرنة، بحث بارتفاع أدنى 56dp، فلاتر وأفعال بحد أدنى 48dp. بطاقة مشتركة للعروض والطلبات: badge ثم عنوان وصورة 80dp ووصف وموقع وتاريخ وأفعال تلتف عند ضيق المساحة. زر الإضافة صغير، مع مساحة إضافية أسفل القائمة.
- التنبيه المحلي: سطح محايد من الثيم، دون تغيير مضمون التنبيه أو ادعاءات التحقق والمدفوعات.
- الملف الشخصي: عنوان 28sp، صورة دائرية Crop بحجم 100dp مع fallback، كاميرا 48dp، ومجموعات حساب وإعدادات متناسقة بحواف 16dp وأيقونات 24dp.
- العربية: موارد localization للنصوص الجديدة، «ابحث»، «عروض التبرع»، «الصلاة القادمة»، «اطّلع على سجل تبرعاتك ومساهماتك»، وتاريخ مثل «19 سبتمبر 2026». الأسهم الاتجاهية AutoMirrored والمحاذاة start/end.

## توحيد التصميم وDark Mode

- أزواج اللون: background/onBackground، surface/onSurface، primary/onPrimary، وselectedContainer/selectedContent؛ أُصلحت أيضًا أفعال وفلاتر وأيقونات تشغيل فرعية تستخدم الأبيض فوق primary الفاتح.
- فصل الخلفية والسطح والسطح المرتفع والحدود في الوضع الداكن مع الحفاظ على الأخضر والتركواز.
- Typography الأساسي: 28/22/18/16/14 و12–13sp للتنقل، مع lineHeight مناسب للعربية.
- المكوّنان المشتركان IhsanHeroScrim وIhsanHeroTopRow يحافظان على الصور ويضمنان تباين النص الأبيض، مع مالك واحد لمظهر أيقونات status bar داخل Theme.kt: أيقونات فاتحة فوق صورة الـHero، وتعود لمظهر الثيم بعد التمرير.
- اختبارات التباين تتحقق من أزواج النص والأفعال بنسبة 4.5:1، وحدود الحقول 3:1، ونص الـHero فوق أسوأ بكسل خلفية فاتح.

## نتائج QA

| الفحص | النتيجة |
| --- | --- |
| assembleDebug وassembleDebugAndroidTest | نجاح بعد إصلاح ملكية شريط الحالة: BUILD SUCCESSFUL، وتجميع APK التطبيق والاختبار |
| Unit tests | 446 ناجحًا: app 33، designsystem 30، feature 383؛ صفر failures/errors/skipped |
| Android Lint | صفر أخطاء، 118 warning؛ أغلبها تحديثات dependencies وموارد غير مستخدمة، ولا أخطاء في فحص Lint |
| Responsive/RTL | 80/80 ناجحًا على محاكي Android 14؛ 160 PNG؛ STATUS_MATRIX_FINAL |
| التنقل الفعلي | تجربة MainActivity والتنقل والتبويبات ومفتاح الثيم على المحاكي، مع لقطات Home/Offers/Requests/Profile في الوضعين |

مصفوفة العرض: 320/360/412/600dp × Light/Dark × fontScale 1.0/1.3 = 16 إعدادًا. لكل إعداد تُرسم الرئيسية والخدمات والعروض والطلبات والملف الشخصي، أي 80 حالة و160 لقطة (أعلى/نهاية). الاختبار يستعمل مكوّنات الإنتاج مع بيانات ثابتة؛ يتحقق من تخطيط النص العربي، وعدم قصّه أو إخفاء نهاية المحتوى خلف الشريط، ومن بقاء مسار الأسماء الحسنى، ومن تموضع أعمدة الخدمات أثناء التشغيل، ومن مظهر أيقونات شريط الحالة فوق الصورة وبعد التمرير. عدد الأعمدة لا يُفحص من نص الكود فقط.

استُخدم JDK 17 وGradle 8.10.2. كان تنزيل بعض الاعتماديات عبر Java يتعثر، فاستُخدم relay محلي مؤقت لمستودعي Google Maven/Maven Central بواسطة init script؛ ملفات بناء المشروع وإصدارات الاعتماديات لم تتغير.

أدلة الفحص محفوظة محليًا في `.project-preservation/ui-polish-20261007/` (مجلد مستثنى من Git): سجل البناء `final-build.log`، سجل المصفوفة `matrix-emulator.log`، لقطات المصفوفة `rendered/`، ولقطات التنقل الفعلي `runtime/`.

## الحدود والملاحظات

- المصفوفة تحاكي العرض وحجم الخط داخل المحاكي؛ لا تعادل اختبارًا على أربعة أجهزة مادية مختلفة أو مراجعة TalkBack كاملة. لم تظهر مشكلة قص أو تراكب في الحالات المختبرة.
- لم يكتمل الفحص على الهاتف؛ توقف العمل عليه بناءً على طلبك. التطبيق الأصلي وبياناته لم يُستبدلا؛ استُخدمت نسخة QA مستقلة أثناء المحاولة الأولى بسبب اختلاف توقيع APK. بقيت نسخة QA التجريبية منفصلة على الهاتف باسم الحزمة `com.example.mol.uipolish`؛ لم أتعامل مع الهاتف لإزالتها بعد طلب إيقاف استخدامه.
- بيانات بطاقات المصفوفة اختبارية. في التشغيل الجديد للمحاكي ظهر تعذر تحميل قسم مواقيت الصلاة؛ بقيت حالة الخطأ وإعادة المحاولة الحالية، دون تعديل مصادر البيانات أو APIs.
- تحذيرات Lint الخاصة بتحديث الحزم وترتيب الموارد تقع خارج تحسين الواجهة؛ لم تُغيّر إصدارات dependencies لمعالجتها.
- `git diff --check` يجد سطرًا فارغًا زائدًا في AsmaScreen.kt وSplashScreen.kt من التعديلات السابقة؛ لم تُمسّ هاتان المخالفتان.

## الملفات المعدلة أو المضافة لهذه المهمة

- [app/src/androidTest/java/com/example/mol/ui/UiPolishMatrixTest.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/app/src/androidTest/java/com/example/mol/ui/UiPolishMatrixTest.kt>)
- [app/src/main/java/com/example/mol/ui/MainScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/app/src/main/java/com/example/mol/ui/MainScreen.kt>)
- [app/src/test/java/com/example/mol/ui/BottomNavigationMotionBoundaryTest.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/app/src/test/java/com/example/mol/ui/BottomNavigationMotionBoundaryTest.kt>)
- [designsystem/src/main/java/com/example/designsystem/component/IhsanBottomNavigation.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/main/java/com/example/designsystem/component/IhsanBottomNavigation.kt>)
- [designsystem/src/main/java/com/example/designsystem/component/IhsanButton.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/main/java/com/example/designsystem/component/IhsanButton.kt>)
- [designsystem/src/main/java/com/example/designsystem/component/IhsanHero.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/main/java/com/example/designsystem/component/IhsanHero.kt>)
- [designsystem/src/main/java/com/example/designsystem/theme/Color.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/main/java/com/example/designsystem/theme/Color.kt>)
- [designsystem/src/main/java/com/example/designsystem/theme/IhsanDimens.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/main/java/com/example/designsystem/theme/IhsanDimens.kt>)
- [designsystem/src/main/java/com/example/designsystem/theme/IhsanSemanticColors.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/main/java/com/example/designsystem/theme/IhsanSemanticColors.kt>)
- [designsystem/src/main/java/com/example/designsystem/theme/Typography.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/main/java/com/example/designsystem/theme/Typography.kt>)
- [designsystem/src/main/res/values/strings.xml](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/main/res/values/strings.xml>)
- [designsystem/src/test/java/com/example/designsystem/theme/IhsanContrastTest.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/test/java/com/example/designsystem/theme/IhsanContrastTest.kt>)
- [feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt>)
- [feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeContentCards.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeContentCards.kt>)
- [feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeContextualHero.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeContextualHero.kt>)
- [feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeDailyJourneyCard.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeDailyJourneyCard.kt>)
- [feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeDiscoverServicesGrid.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeDiscoverServicesGrid.kt>)
- [feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeEhsanCommunityCard.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeEhsanCommunityCard.kt>)
- [feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeQuranContinueCard.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeQuranContinueCard.kt>)
- [feature/src/main/java/com/example/feature/ehsan/EhsanScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/ehsan/EhsanScreen.kt>)
- [feature/src/main/java/com/example/feature/ehsan/presentation/components/EhsanUiComponents.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/ehsan/presentation/components/EhsanUiComponents.kt>)
- [feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmEpisodeDetailScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmEpisodeDetailScreen.kt>)
- [feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmMainScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmMainScreen.kt>)
- [feature/src/main/java/com/example/feature/fahmanallah/presentation/components/FahmCompletionCard.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/fahmanallah/presentation/components/FahmCompletionCard.kt>)
- [feature/src/main/java/com/example/feature/fahmanallah/presentation/components/FahmEpisodeCard.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/fahmanallah/presentation/components/FahmEpisodeCard.kt>)
- [feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt>)
- [feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt>)
- [feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanEpisodeDetailScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanEpisodeDetailScreen.kt>)
- [feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanMainScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanMainScreen.kt>)
- [feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanRecipeDetailScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanRecipeDetailScreen.kt>)
- [feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanRecipesScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanRecipesScreen.kt>)
- [feature/src/main/java/com/example/feature/nabiihsan/presentation/components/NabiFilterSheet.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/nabiihsan/presentation/components/NabiFilterSheet.kt>)
- [feature/src/main/java/com/example/feature/nabiihsan/presentation/components/NabiSourceCard.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/nabiihsan/presentation/components/NabiSourceCard.kt>)
- [feature/src/main/java/com/example/feature/profile/ProfileScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/profile/ProfileScreen.kt>)
- [feature/src/main/java/com/example/feature/profile/presentation/components/ProfileUiComponents.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/profile/presentation/components/ProfileUiComponents.kt>)
- [feature/src/main/java/com/example/feature/settings/SettingsScreen.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/java/com/example/feature/settings/SettingsScreen.kt>)
- [feature/src/main/res/drawable/polish_avatar_placeholder.xml](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/res/drawable/polish_avatar_placeholder.xml>)
- [feature/src/main/res/values-ar/strings.xml](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/res/values-ar/strings.xml>)
- [feature/src/main/res/values-ar/ui_polish_strings.xml](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/res/values-ar/ui_polish_strings.xml>)
- [feature/src/main/res/values/strings.xml](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/res/values/strings.xml>)
- [feature/src/main/res/values/ui_polish_strings.xml](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/main/res/values/ui_polish_strings.xml>)
- [feature/src/test/java/com/example/feature/dashboard/HomeFeaturePreservationTest.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/test/java/com/example/feature/dashboard/HomeFeaturePreservationTest.kt>)
- [feature/src/test/java/com/example/feature/profile/ProfileReferenceUiBoundaryTest.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/feature/src/test/java/com/example/feature/profile/ProfileReferenceUiBoundaryTest.kt>)

- [designsystem/src/main/java/com/example/designsystem/theme/Theme.kt](<C:/Users/molha/OneDrive/Desktop/New folder (2)/Sdk/Sdk/mol/designsystem/src/main/java/com/example/designsystem/theme/Theme.kt>)
