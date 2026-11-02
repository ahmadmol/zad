# إحسان — أوامر Gemini المتسلسلة

التاريخ: 2026-10-18 — Asia/Damascus. Planning Only.

هذه أوامر مستقبلية بعد اعتماد الخطة، ولم تُنفذ أثناء التدقيق. كل كتلة مستقلة وتبدأ بالدور المطلوب. توقف عند نهاية كل Task ID وانتظر اعتماد دفعتها. إعادة الجرد وقت التنفيذ إلزامية؛ مسارات البداية موجودة في baseline باستثناء وثيقة التخطيط المنشأة هنا. أسماء الجداول والشاشات الجديدة مقترحة وليست ملفات موجودة. إذا تغير الكود، اعرض الفرق وحدّث نطاق الدفعة.

## G0-01 — تثبيت خط الأساس وتسوية سجل القبول

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: G0-01
Task Title: تثبيت خط الأساس وتسوية سجل القبول
Required Role: الأدوار أعلاه مع Principal Android Architect لمراجعة سلسلة الإثبات.
Task Priority: P0
التبعيات المشروطة: لا توجد
المتطلبات السابقة: اعتماد المراجعة النهائية للخطة واختيار هذه الدفعة وحدها.

الهدف:
تدقيق مكتبي لسلسلة المصدر → البناء → المثبت → المختبر، بالأدلة الموجودة فقط.

ملفات البداية:
- docs/planning/IHSAN_AS_IS_AUDIT.md
- docs/planning/IHSAN_MASTER_IMPROVEMENT_PLAN.md
- docs/planning/IHSAN_QA_AND_RELEASE_GATES.md
- docs/planning/IHSAN_UI_UX_SCREEN_MATRIX.csv
- docs/qa/FINAL_PHYSICAL_DEVICE_QA.md
- docs/qa/FINAL_BETA_GAP_REPORT.md
- docs/release/VIDEO_BLACK_SCREEN_RCA_2026-10-17.md
- docs/release/external-evidence/2026-10-17/video-fix-quality-final/provenance.json
- docs/release/external-evidence/2026-10-17/video-fix-quality-final/quality-summary.json
- docs/release/external-evidence/2026-10-17/phone-release-final/installation.json
- .artifacts/physical-device-qa/session-20261016/delivery-provenance.json

السلوك الحالي:
PASS مذكور للبصمة 4BF1DD4F، والفجوات تشير إلى C415EA4A غير المثبتة تاريخيًا،
وRCA يشير إلى dba6df9f مع إثبات تثبيت تاريخي دون قبول واجهات Release.
تطابق عينة المصدر الثمانية لا يثبت كامل مدخلات البناء.
الحالة المثبتة الحالية على الهاتف غير متحقق منها في هذه الدفعة.

المطلوب:
1. اجرد مدخلات الإنتاج والاختبار والإعدادات المؤثرة، مع HEAD وdirty/untracked
   وبصمات الملفات ونطاق وخوارزمية digest. لا تقرأ أسرارًا أو بيانات مستخدم.
2. تحقق محليًا من ملفات APK المحفوظة وبصماتها والأدلة المرتبطة بها.
   طبّق P1..P5 في وثيقة QA، وصنّف كل وصلة Proven/Unproven/Unknown مع السبب.
   اسم الملف أوversionCode أو الشهادة المشتركة لا يثبت تطابق النسخ.
   لا تفترض أن hash المصدر يساوي hash APK أو أن إعادة البناء تثبت نسخة تاريخية.
3. افصل الدليل التاريخي عن تحقق المصدر الحالي، وDebug عن Release،
   ومطابقة التثبيت عن نجاح الاختبار. لا تنقل PASS بين بصمات مختلفة.
4. اربط REC-01..REC-06 مستقلاً بـG0-03 وUX-01/UX-03/UX-05 وفق سجل QA.
   لا تضف Task IDs؛ مراجعة المصدر ليست قبولًا بصريًا.
5. احسب نتائج XML المحفوظة حسب الوحدة وsuite وfailures/errors/skipped،
   مع إثبات نطاقها التاريخي؛ لا تصفها اختبارات أُجريت الآن.
6. سجل النواقص وخطوات إثباتها لاحقًا في G0-02..06.
   Beta-Current لا يعتمد ميزات F4/F5/F6؛ تبعيات R7-03 مشروطة بمسار الإصدار.

نطاق الكتابة:
أقسام أدلة G0-01 فقط داخل ملفات التخطيط الموجودة:
docs/planning/IHSAN_AS_IS_AUDIT.md
docs/planning/IHSAN_QA_AND_RELEASE_GATES.md
docs/planning/IHSAN_MASTER_IMPROVEMENT_PLAN.md
ضمّن manifest وجدول التسوية داخلها. بقية الملفات قراءة فقط؛ لا ملفات جديدة.

المحظورات:
لا كود أو Gradle أو Manifest أو موارد أو بيانات أو schema.
لا بناء أو تشغيل اختبارات أو APK أو ADB أو هاتف أو Emulator/AVD.
لا قراءة backup أو DB خاصة، ولا Commit/Push/Reset/Rebase أو تنزيل أو مراسلة أو نشر.

التحقق الإلزامي:
فحوص وثائق محلية فقط: SHA-256، وجود الملفات، ترابط الأدلة، نتائج XML المحفوظة،
وتطابق 40 Task IDs والاعتماديات وتغطية REC-01..06. لا PASS جديد للتطبيق.

أثر البيانات: لا تغيير تخزين أو ترحيل أو بيانات مستخدم.
القبول: كل ادعاء له artifact ونطاق ودليل؛ الوصلات غير المثبتة معلنة.
الناتج Documentation Ready for Acceptance، وليس Build/Device/Release Accepted.
المخاطر: خلط artifacts/variants، ونقص full-source manifest، وتعميم العينة.
التراجع: عكس إضافات هذه الدفعة في الوثائق وحدها دون طمس العمل السابق أو Git Reset.
التقرير: ما تغير وما ثبت وما تعذر إثباته، فحوص الوثائق، الوصلات المعلقة،
عدم تشغيل الهاتف، المخاطر والقرارات، وجاهزية ناتج الدفعة للقبول.
توقف بعد G0-01 وانتظر اعتمادها؛ لا تبدأ G0-02 تلقائيًا.
```

## G0-02 — إعادة تأهيل البناء والاختبارات المحلية

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: G0-02
Task Title: إعادة تأهيل البناء والاختبارات المحلية
Task Priority: P0
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: صفر failures/errors مع توثيق skipped والتحذيرات؛ الفشل يمنع الانتقال ولا يمحى.
المتطلبات السابقة: G0-01؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- settings.gradle.kts
- app/build.gradle.kts
- feature/build.gradle.kts
- designsystem/build.gradle.kts
- gradle/libs.versions.toml
السلوك الحالي بحسب baseline التخطيط: 477 نتيجة تاريخية وليست إثباتًا لحالة الملفات اليوم.
التغيير المحدد والخطوات: بعد G0-01 شغّل بناء Debug وRelease والوحدات وLint للوحدات الثلاث؛ سجل JDK والأوامر والتحذيرات وhash المصدر. لا ترقي الاعتماديات ولا تغير التوقيع لحل فشل. أي إصلاح يحتاج دفعة منفصلة موثقة.
حدود التنفيذ: دفعة G0-02 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: assembleDebug وassembleRelease وtestDebugUnitTest وlintDebug وlintRelease للوحدات الثلاث؛ احسب XML حسب module. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: صفر failures/errors مع توثيق skipped والتحذيرات؛ الفشل يمنع الانتقال ولا يمحى. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: بيئة JDK وتوقيع داخلي. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## G0-03 — قبول التنقل والحالات على البناء المختار

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: G0-03
Task Title: قبول التنقل والحالات على البناء المختار
Task Priority: P0
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: كل صف له دليل وبصمة أو سبب حجب؛ لا اعتماد شامل مع صف وظيفي حرج معلق.
المتطلبات السابقة: G0-02؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- app/src/main/java/com/example/mol/navigation/Screen.kt
- app/src/main/java/com/example/mol/navigation/AppNavHost.kt
- app/src/androidTest/java/com/example/mol/ui/DarkFinalRouteMatrixTest.kt
- app/src/androidTest/java/com/example/mol/ui/PreserveDevicePreferences.kt
السلوك الحالي بحسب baseline التخطيط: 58 موضع تسجيل ساكنًا (واحد gated؛ Release57/Debug58) و59 حالة اختبار؛ Splash وLegacy خارج قائمة الـ59.
التغيير المحدد والخطوات: حضّر fixture معزولة، ثم على الهاتف المصرح حصريًا سجل 59 حالة في قائمة Debug بالمظهرين، و58 حالة إيجابية منها في Release؛ controlled_daily اختبر غياب route/entry في Release دون تفعيل flag، وSplash الحقيقي وLegacy redirect منفصلين. افحص الأفعال والرجوع والاستعادة لا مجرد render. أضف الحالات الثانوية المدرجة في CSV على دفعات أقسام.
حدود التنفيذ: دفعة G0-03 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: Compose navigation + journeys + keyboard/insets + TalkBack/font 1/1.3/2؛ تحقق من الهوية قبل كل جلسة. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: كل صف له دليل وبصمة أو سبب حجب؛ لا اعتماد شامل مع صف وظيفي حرج معلق. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: التأثير على التفضيلات والبيانات الحقيقية. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
تغطية الإصلاحات الأصلية: REC-01، REC-02، REC-03، REC-04، REC-05، REC-06 وفق معايير وأدلة كلREC المستقلة في IHSAN_QA_AND_RELEASE_GATES.md. لا تكرر المهمة ولا تعِد إصلاح كود صحيح؛ افصل Source Reviewed عن Visual Accepted. عند ثبوت عيب متعلق بBeta أصلحه وحده وأعد قبوله؛ لا تنتظر كل تحسيناتPhase1 أوالمزاياالجديدة.
```

## G0-04 — إغلاق قبول الفيديو والبث

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: G0-04
Task Title: إغلاق قبول الفيديو والبث
Task Priority: P0
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا تستبدل تشغيل الداخل بفتح YouTube؛ كل بعد له نتيجته؛ قرار D-01 يحسم منع Beta بسبب live embed.
المتطلبات السابقة: G0-02؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt
- feature/src/main/java/com/example/feature/learning/presentation/components/VideoWebChromeClient.kt
- feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt
- app/src/androidTest/java/com/example/mol/ui/PhysicalDeviceMediaAuditTest.kt
السلوك الحالي بحسب baseline التخطيط: إصلاح MATCH_PARENT وfullscreen موجود؛ أدلة نهائية جزئية وصوت/Release معلقان.
التغيير المحدد والخطوات: أعد استخراج EpisodeRecord فقط: Sanhya=28 Nabi=29 Fahm=29 ثم اطبع الفرق مع 86 السابق. اختبر صورة متحركة وتقدم زمن وصوت مسموع لكل عنصر وفق نطاق القبول المعتمد، وعينة lifecycle في كل قسم على Release. سجّل 150 كرفض تضمين واختبر البديل الرسمي مستقلاً؛ لا تعدّل أصل الفيديو.
حدود التنفيذ: دفعة G0-04 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: VideoRenderingRootCauseTest وVideoLifecyclePhoneTest ثم سجل per-ID لصورة/صوت/ثيمين/fullscreen/background/Release. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا تستبدل تشغيل الداخل بفتح YouTube؛ كل بعد له نتيجته؛ قرار D-01 يحسم منع Beta بسبب live embed. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: تغير سياسات المصدر وعدم توافر الهاتف. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## G0-05 — قبول الترحيل وحماية البيانات

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: G0-05
Task Title: قبول الترحيل وحماية البيانات
Task Priority: P0
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: هوية v8 صحيحة وعدم فقد البيانات؛ كل مسار مدعوم له إثبات هاتف؛ لا destructive fallback.
المتطلبات السابقة: G0-02؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt
- feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt
- feature/src/androidTest/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationTest.kt
- docs/ROOM_MIGRATION_MATRIX.md
السلوك الحالي بحسب baseline التخطيط: Room v8 بينما مصفوفة قديمة تقول v5.
التغيير المحدد والخطوات: حدّث مصفوفة 2→3→5→6→7→8 من الاختبارات والكود. افحص كل نقطة بدء مدعومة ببيانات اصطناعية في مساحة اختبار معزولة، وقارن الصفوف والمعرفات والمفضلات والملفات. لا تفتح قواعد المستخدم الحقيقية ولا تفترض دعم v1 أو v4.
حدود التنفيذ: دفعة G0-05 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: MigrationTestHelper مع schemas وfixture تاريخي موثق؛ fresh/current/upgrade/missing path؛ rollback rehearsal على fixture. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: هوية v8 صحيحة وعدم فقد البيانات؛ كل مسار مدعوم له إثبات هاتف؛ لا destructive fallback. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: ملفات schemas التاريخية والتباس حزمة المستخدم. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## G0-06 — قبول الصلاة والقبلة ووصول الإشعارات

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: G0-06
Task Title: قبول الصلاة والقبلة ووصول الإشعارات
Task Priority: P0
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا تكرار/صمت غير مفسر؛ استعادة الإعدادات؛ reboot يبقى Not Tested دون الموافقة؛ فرق الحساب موثق.
المتطلبات السابقة: G0-02؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt
- feature/src/main/java/com/example/feature/prayer/data/scheduler/AndroidPrayerAlarmGateway.kt
- feature/src/main/java/com/example/feature/prayer/worker/PrayerSystemReconciliationReceiver.kt
- feature/src/main/java/com/example/feature/qibla/util/QiblaManager.kt
- feature/src/main/java/com/example/feature/reminders/AzkarReminderCoordinator.kt
السلوك الحالي بحسب baseline التخطيط: حساب وledger وReceiver موجودة؛ وجودها لا يثبت delivery أو دقة المستشعر.
التغيير المحدد والخطوات: نفذ السيناريوهات في وثيقة QA على الهاتف فقط بعد التحضير. قارن المواقيت بمرجع مستقل مطابق الطريقة والمذهب والتاريخ والمنطقة. افصل bearing الرياضي عن معايرة المستشعر. تحقق deny/grant وDoze والوقت والمنطقة وإعادة التشغيل؛ reboot خطوة منفصلة تتطلب موافقة صريحة بعد حفظ الوضع.
حدود التنفيذ: دفعة G0-06 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: PrayerIndependentReferenceTest وHomePrayerParityTest وQiblaSensorAccuracyTest وAuditSystemDeliveryTest وAuditTimezoneChangeTest؛ دليل إشعار مرئي ومسموع. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا تكرار/صمت غير مفسر؛ استعادة الإعدادات؛ reboot يبقى Not Tested دون الموافقة؛ فرق الحساب موثق. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: قيود Xiaomi وتغيير إعدادات الجهاز. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## UX-01 — قياس المكونات المشتركة وإتاحة الوصول

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: UX-01
Task Title: قياس المكونات المشتركة وإتاحة الوصول
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: 48dp للأفعال المستهدفة وحدود لمس غير متداخلة وتباين مقاس 4.5:1 للنص العادي و3:1 للكبير؛ لا قص عند 200%.
المتطلبات السابقة: G0-03؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- designsystem/src/main/java/com/example/designsystem/component/IhsanButton.kt
- designsystem/src/main/java/com/example/designsystem/component/IhsanBottomNavigation.kt
- designsystem/src/main/java/com/example/designsystem/theme/IhsanSemanticColors.kt
- designsystem/src/main/java/com/example/designsystem/theme/Typography.kt
- app/src/main/java/com/example/mol/ui/MainScreen.kt
السلوك الحالي بحسب baseline التخطيط: نظام ثيم ومكونات موجود مع اختبارات contrast؛ الحدود التفاعلية الفعلية غير مثبتة لكل شاشة.
التغيير المحدد والخطوات: قسّم فحص المكونات ثم عالج فقط إخفاقات موثقة في touch/focus/contrast/font/insets. استخدم Material 3 وأبق geometry والهوية؛ لا تستبدل الصور ولا لون صفحة المصحف.
حدود التنفيذ: دفعة UX-01 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: IhsanContrastTest وbottom-navigation tests؛ Compose semantics وحواف touch غير متداخلة؛ snapshots المظهرين. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: 48dp للأفعال المستهدفة وحدود لمس غير متداخلة وتباين مقاس 4.5:1 للنص العادي و3:1 للكبير؛ لا قص عند 200%. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: تأثير واسع على جميع الشاشات. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
تغطية الإصلاحات الأصلية: REC-01 وفق معايير وأدلة كلREC المستقلة في IHSAN_QA_AND_RELEASE_GATES.md. لا تكرر المهمة ولا تعِد إصلاح كود صحيح؛ افصل Source Reviewed عن Visual Accepted. عند ثبوت عيب متعلق بBeta أصلحه وحده وأعد قبوله؛ لا تنتظر كل تحسيناتPhase1 أوالمزاياالجديدة.
```

## UX-02 — اتجاه اللغة والنصوص القابلة للترجمة

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: UX-02
Task Title: اتجاه اللغة والنصوص القابلة للترجمة
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا انعكاس للنص القرآني؛ لا مفاتيح ناقصة؛ قرار D-04 يحدد نطاق الإنجليزية.
المتطلبات السابقة: UX-01؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- app/src/main/java/com/example/mol/ui/MainScreen.kt
- feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt
- feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt
- feature/src/main/res/values/strings.xml
- feature/src/main/res/values-ar/strings.xml
السلوك الحالي بحسب baseline التخطيط: MainScreen وبعض الشاشات تفرض RTL؛ نصوص عربية literal موجودة.
التغيير المحدد والخطوات: استخرج نصوص UI فقط تدريجيًا؛ اجعل shell يتبع لغة الواجهة حين اعتماد الإنجليزية، مع RTL صريح للنص القرآني فقط واتجاه معزول للهاتف والروابط. لا تترجم النص الديني تلقائيًا.
حدود التنفيذ: دفعة UX-02 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: resource key parity + Arabic RTL/English LTR navigation + mixed digits/URLs/font scale. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا انعكاس للنص القرآني؛ لا مفاتيح ناقصة؛ قرار D-04 يحدد نطاق الإنجليزية. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: انعكاس التنقل وتغير طول النص. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## UX-03 — تدقيق أسطح العبادة واستعادة الوصول

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: UX-03
Task Title: تدقيق أسطح العبادة واستعادة الوصول
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: خلفية المصحف والزخارف والنص محفوظة؛ أفعال الآيات قابلة للوصول دون الاعتماد على النقر الإحداثي وحده.
المتطلبات السابقة: UX-01؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt
- feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt
- feature/src/main/java/com/example/feature/azkar/AzkarScreen.kt
- feature/src/main/java/com/example/feature/tasbih/presentation/TasbihScreen.kt
- feature/src/main/java/com/example/feature/duas/DuaDetailScreen.kt
- feature/src/main/java/com/example/feature/hadith/presentation/HadithScreen.kt
- feature/src/main/java/com/example/feature/asma/presentation/AsmaScreen.kt
السلوك الحالي بحسب baseline التخطيط: المصحف paragraph مع pointerInput؛ تفاصيل العبادة متعددة وحالات مصدر متاحة.
التغيير المحدد والخطوات: من CSV عالج مشكلات مثبتة فقط: أكشن الآية المتاح عبر TalkBack دون تغيير الترتيب/الأحرف، تكبير الصفحة وpan وselection، وصول التفصيل والنسخ والمشاركة والمصدر. نفذ قسمًا واحدًا في كل اعتماد فرعي.
حدود التنفيذ: دفعة UX-03 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: Quran integrity + Compose verse actions + font/zoom/pan + empty/favorite/source flows. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: خلفية المصحف والزخارف والنص محفوظة؛ أفعال الآيات قابلة للوصول دون الاعتماد على النقر الإحداثي وحده. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: المساس بعرض النص أو آليات تحديد الآيات. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
تغطية الإصلاحات الأصلية: REC-02، REC-03 وفق معايير وأدلة كلREC المستقلة في IHSAN_QA_AND_RELEASE_GATES.md. لا تكرر المهمة ولا تعِد إصلاح كود صحيح؛ افصل Source Reviewed عن Visual Accepted. عند ثبوت عيب متعلق بBeta أصلحه وحده وأعد قبوله؛ لا تنتظر كل تحسيناتPhase1 أوالمزاياالجديدة.
```

## UX-04 — نماذج المجتمع والملف وحالات الاستعادة

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: UX-04
Task Title: نماذج المجتمع والملف وحالات الاستعادة
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا submission مزدوج؛ المسودة تستعاد حيث يوعد بها؛ آخر حقل وزر ظاهر؛ missing-ID قابل للاستعادة.
المتطلبات السابقة: UX-01؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/ehsan/presentation/AddEhsanScreen.kt
- feature/src/main/java/com/example/feature/ehsan/presentation/RequestHelpScreen.kt
- feature/src/main/java/com/example/feature/profile/presentation/EditProfileScreen.kt
- feature/src/main/java/com/example/feature/ehsan/presentation/IhsanDetailsScreen.kt
- feature/src/main/java/com/example/feature/profile/DonationHistoryScreen.kt
السلوك الحالي بحسب baseline التخطيط: نماذج محلية وصور وملكية USER/LEGACY/DEMO قائمة.
التغيير المحدد والخطوات: افحص ثم أصلح فقط حالات keyboard/validation/focus/draft/duplicate submit المثبتة. أبق إفصاح المحلية وDEMO وحوار الحذف وملكية السجل. لا تختبر إرسالًا أو حذفًا على بيانات فعلية.
حدود التنفيذ: دفعة UX-04 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: OfferDraftRestorationTest وAddEhsanAlbumFailureTest وPhysicalCommunityJourneyTest على fixtures؛ IME وcancel/delete confirm. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا submission مزدوج؛ المسودة تستعاد حيث يوعد بها؛ آخر حقل وزر ظاهر؛ missing-ID قابل للاستعادة. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: PII والصور والضغط السريع. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## UX-05 — التعلم والبث والحالات المشتركة

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: UX-05
Task Title: التعلم والبث والحالات المشتركة
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا فقد مكتبة المحفوظات ولا overlay على YouTube؛ بديل واضح دون ادعاء نجاح التضمين.
المتطلبات السابقة: G0-04، UX-01؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt
- feature/src/main/java/com/example/feature/learning/presentation/components/LearningSettingsScreen.kt
- feature/src/main/java/com/example/feature/sanhya/presentation/SanhyaStoryDetailScreen.kt
- feature/src/main/java/com/example/feature/nabiihsan/presentation/NabiIhsanEpisodeDetailScreen.kt
- feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmEpisodeDetailScreen.kt
- feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt
السلوك الحالي بحسب baseline التخطيط: مشغل مشترك وحالات offline/error وfallback؛ مصفوفة الثيم/font غير مكتملة.
التغيير المحدد والخطوات: وحّد فقط رسائل الحالات وأفعال الاستعادة التي يفشل اختبارها، وافحص labels الطويلة ونسبة الصور وعدم حجب controls. أبق الحالة غير المتاحة للوصفات/المحطات دون محتوى مولد.
حدود التنفيذ: دفعة UX-05 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: offline/retry/missing detail/saved empty + fullscreen/back/background وfont 2 + external-intent failure. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا فقد مكتبة المحفوظات ولا overlay على YouTube؛ بديل واضح دون ادعاء نجاح التضمين. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: انحدار المشغل الذي أصلح حديثًا. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
تغطية الإصلاحات الأصلية: REC-04، REC-05، REC-06 وفق معايير وأدلة كلREC المستقلة في IHSAN_QA_AND_RELEASE_GATES.md. لا تكرر المهمة ولا تعِد إصلاح كود صحيح؛ افصل Source Reviewed عن Visual Accepted. عند ثبوت عيب متعلق بBeta أصلحه وحده وأعد قبوله؛ لا تنتظر كل تحسيناتPhase1 أوالمزاياالجديدة.
```

## AR-01 — استعادة البحث وتصفير حالات الفشل

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: AR-01
Task Title: استعادة البحث وتصفير حالات الفشل
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا spinner دائم ولا نتائج قديمة مع query أحدث؛ إعادة إنشاء تحافظ على الاستعلام والفلتر.
المتطلبات السابقة: G0-02؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt
- feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt
- feature/src/main/java/com/example/feature/quran/presentation/QuranViewModel.kt
السلوك الحالي بحسب baseline التخطيط: query/filter في remember؛ catch البحث القرآني لا يطفئ isSearching.
التغيير المحدد والخطوات: ثبّت query/filter في SavedStateHandle أو rememberSaveable المناسب؛ وحّد انتقال search error بحيث isSearching=false؛ اختبر clear بعد failure وسرعة الكتابة والإلغاء ومنع نتائج الطلب القديم. لا تعيد كتابة محركات البحث.
حدود التنفيذ: دفعة AR-01 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: ViewModel fake failure/cancellation/debounce latest + recreate query/filter + empty after clear. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا spinner دائم ولا نتائج قديمة مع query أحدث؛ إعادة إنشاء تحافظ على الاستعلام والفلتر. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: اختلاط state بين ViewModels ونتائج أقسام مستقلة. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## AR-02 — ذرية تقدم اليوم والقراءة

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: AR-02
Task Title: ذرية تقدم اليوم والقراءة
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: عدم ضياع الزيادات أو انبعاث سورة/آية غير متطابقتين؛ preserve existing keys.
المتطلبات السابقة: G0-02؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt
- feature/src/main/java/com/example/feature/dashboard/data/DailyActivityRepositoryImpl.kt
- feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt
السلوك الحالي بحسب baseline التخطيط: edit للزوج atomically لكن القراءة بcombine لتدفقين؛ reset من observeToday منفصل عن increment.
التغيير المحدد والخطوات: انشر snapshot واحدًا لموضع القراءة وتاريخ/عدادات اليوم. اجعل rollover+mutation داخل edit واحد بشرط التاريخ وأدخل clock مشتركًا. أثبت race قبل تغيير الكود ولا تصفر سجلًا جديدًا استجابة لانبعاث قديم.
حدود التنفيذ: دفعة AR-02 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: concurrent increment/reset/midnight/timezone؛ لا pair هجين من lastRead؛ إعادة تشغيل مع قيم قديمة. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: عدم ضياع الزيادات أو انبعاث سورة/آية غير متطابقتين؛ preserve existing keys. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: سباق محتمل يحتاج إعادة إنتاج؛ اختلاف clock الاختباري. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## AR-03 — سلامة Favorites والاستعلامات

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: AR-03
Task Title: سلامة Favorites والاستعلامات
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: عدد استعلامات محدود مستقل عن N؛ حفظ IDs والمفضلات وعدم حذف بيانات غير مصنفة.
المتطلبات السابقة: AR-02، G0-05؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt
- feature/src/main/java/com/example/feature/quran/data/local/dao/QuranDao.kt
- feature/src/main/java/com/example/feature/quran/data/local/entity/BookmarkEntity.kt
- feature/src/main/java/com/example/feature/hadith/data/repository/HadithRepositoryImpl.kt
السلوك الحالي بحسب baseline التخطيط: observeAllBookmarks يستعلم مرتين لكل صف؛ toggle read-then-write.
التغيير المحدد والخطوات: أثبت N+1 والضغط المتزامن ثم استخدم JOIN projection لأسماء/نصوص bookmarks وmutation ذرية مطابقة لسلوك toggle المعتمد. افحص duplicates الحالية قبل أي UNIQUE/FK؛ لا تحذف orphan تلقائيًا.
حدود التنفيذ: دفعة AR-03 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: إضافة index/unique فقط بعد فحص البيانات وترحيل معتمد؛ JOIN وحده لا يحتاج schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: query-count قبل/بعد + favorite toggle concurrency/idempotent save + legacy duplicate fixture. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: عدد استعلامات محدود مستقل عن N؛ حفظ IDs والمفضلات وعدم حذف بيانات غير مصنفة. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: معنى double tap وازدواج سابق. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## AR-04 — أخطاء التخزين والتهيئة والحدود

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: AR-04
Task Title: أخطاء التخزين والتهيئة والحدود
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: حالة خطأ قابلة للاستعادة؛ لا blank يبدو نجاحًا؛ السجلات لا تحتوي PII.
المتطلبات السابقة: G0-05؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt
- feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt
- app/src/main/java/com/example/mol/di/DatabaseModule.kt
- app/src/main/java/com/example/mol/BarakahApp.kt
- feature/src/main/java/com/example/feature/core/observability/AppObservability.kt
السلوك الحالي بحسب baseline التخطيط: DataStore تدفقات مباشرة؛ loader يمسك بعض أخطاء assets بصمت؛ Room.build لا يضمن فتح DB في try.
التغيير المحدد والخطوات: صمم استرداد IOException دون إخفاء corruption أو استخدام default ثم الكتابة فوقه. اجعل فشل التهيئة ظاهرًا مع retry، واحفظ CancellationException. راقب الفشل عند الاستعمال الفعلي لRoom لا وقت إنشاء builder فقط.
حدود التنفيذ: دفعة AR-04 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: fault injection: asset missing/invalid JSON/Room open/IO/cancel؛ retry دون reseed مدمر. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: حالة خطأ قابلة للاستعادة؛ لا blank يبدو نجاحًا؛ السجلات لا تحتوي PII. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: طمس corruption وإعادة البذر. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## AR-05 — توثيق الملكية وتقليل الكود غير الموصول

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: AR-05
Task Title: توثيق الملكية وتقليل الكود غير الموصول
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا اعتماد جديد دائري ولا ViewModel/DAO غير قابل للحل يعلن جاهزًا؛ لا نقل جذري للوحدات.
المتطلبات السابقة: AR-04؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- app/src/main/java/com/example/mol/di/AppModule.kt
- app/src/main/java/com/example/mol/di/RepositoryModule.kt
- app/src/main/java/com/example/mol/di/ViewModelModule.kt
- feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt
- feature/src/main/java/com/example/feature/prayer/presentation/ManualPrayerTrackerViewModel.kt
- feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt
السلوك الحالي بحسب baseline التخطيط: core feature module كبير؛ قواعد legacy ونماذج أهداف وصلاة غير موصولة.
التغيير المحدد والخطوات: ارسم dependency/DI graph واختبر resolution للـproduction roots. وثق active/legacy/foundation ولا تحذف legacy. اعزل orchestration الجديد بعقود صغيرة ضمن الوحدات الحالية. تحديث عقود الحذف متسق مع ownership الفعلية والتعليقات القديمة.
حدود التنفيذ: دفعة AR-05 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: Koin graph + production route characterization + source-reference inventory. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا اعتماد جديد دائري ولا ViewModel/DAO غير قابل للحل يعلن جاهزًا؛ لا نقل جذري للوحدات. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: تعليق قديم يخالف التنفيذ ومكونات غير مستخدمة. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## PS-01 — قياس خط الأساس للأداء

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: PS-01
Task Title: قياس خط الأساس للأداء
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: baseline قابل للإعادة وبصمة APK؛ أهداف عددية تعتمد بعد القياس لا نسب تحسن افتراضية.
المتطلبات السابقة: G0-03، AR-01؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- app/src/main/java/com/example/mol/BarakahApp.kt
- feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt
- feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt
- feature/src/main/java/com/example/feature/hadith/data/repository/HadithRepositoryImpl.kt
- feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt
السلوك الحالي بحسب baseline التخطيط: لا قياسات حالية معتمدة؛ توجد قراءة JSON وtext fitting وفهارس ذاكرة.
التغيير المحدد والخطوات: قِس startup cold/warm وframe times وsearch cold/warm وmemory وbattery وAPK وDB على هاتف Release. أعد توزيع التحسينات بعد trace فقط؛ أي إضافة dependency قياس تستلزم نطاقًا واضحًا.
حدود التنفيذ: دفعة PS-01 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: Perfetto/System Trace + Android Profiler + dumpsys meminfo + APK Analyzer؛ 10 عينات لكل سيناريو مع median/p95 والظروف. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: baseline قابل للإعادة وبصمة APK؛ أهداف عددية تعتمد بعد القياس لا نسب تحسن افتراضية. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: ضجيج القياس وthermal state. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## PS-02 — إصلاح عنق زجاجة واحد مثبت

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: PS-02
Task Title: إصلاح عنق زجاجة واحد مثبت
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: trace يثبت إزالة السبب؛ المقاييس ضمن budget المعتمد D-08؛ لا انحدار صحة أو ذاكرة.
المتطلبات السابقة: PS-01، AR-03؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt
- feature/src/main/java/com/example/feature/quran/data/local/QuranAssetLoader.kt
- feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt
- feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt
السلوك الحالي بحسب baseline التخطيط: مرشحون فقط: parsing/fitting وإعادة normalization وذاكرة index.
التغيير المحدد والخطوات: اختر أسوأ bottleneck مثبت في PS-01. انقل parsing عن main/cache immutable edition أو precompute keys أو حد نطاق recomposition حسب الدليل. لا تغير JSON ولا النص ولا تحمل cache غير محدود.
حدود التنفيذ: دفعة PS-02 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: repeat نفس baseline + content hash/ordering + cancellation + memory teardown. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: trace يثبت إزالة السبب؛ المقاييس ضمن budget المعتمد D-08؛ لا انحدار صحة أو ذاكرة. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: cache stale أو نقل عمل بلا مكسب مثبت. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## PS-03 — تدقيق WebView وIntents والملفات

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: PS-03
Task Title: تدقيق WebView وIntents والملفات
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا وصول ملفات أو intent غير مصرح من محتوى خارجي؛ المصادر الرسمية تبقى قابلة للفتح.
المتطلبات السابقة: G0-04، UX-04؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/learning/presentation/components/IhsanYouTubePlayer.kt
- feature/src/main/java/com/example/feature/learning/presentation/components/YouTubePlaybackBridge.kt
- feature/src/main/java/com/example/feature/live/LiveStreamScreen.kt
- feature/src/main/java/com/example/feature/ehsan/data/image/EhsanImageStore.kt
- app/src/main/AndroidManifest.xml
السلوك الحالي بحسب baseline التخطيط: JS bridge محدود وfile/content off وmain frame allowlist؛ لا اختراق مثبت.
التغيير المحدد والخطوات: اختبر adversarial URLs وredirects وrenderer death وحجم/نوع URI الصور وshare grants وIntent failures. أصلح فقط failure مثبت؛ لا توسع hosts ولا تسمح cleartext أو raw path أو JS عند arbitrary origins.
حدود التنفيذ: دفعة PS-03 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: unit allowlist/URI + instrumentation WebView/file grants/renderer + lint exported-components. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا وصول ملفات أو intent غير مصرح من محتوى خارجي؛ المصادر الرسمية تبقى قابلة للفتح. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: حدود الثقة داخل iframe/bridge. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## PS-04 — سياسة التوقيع والأذونات والبيانات

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: PS-04
Task Title: سياسة التوقيع والأذونات والبيانات
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا Debug certificate في مسار production المعتمد؛ لا توسيع backup/permissions دون D-02/D-03/D-05.
المتطلبات السابقة: G0-02، PS-03؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- app/build.gradle.kts
- app/src/main/AndroidManifest.xml
- app/src/main/res/xml/backup_rules.xml
- app/src/main/res/xml/data_extraction_rules.xml
- docs/RELEASE_SIGNING_GUIDE.md
- docs/store/DATA_SAFETY_DISCLOSURE.md
السلوك الحالي بحسب baseline التخطيط: release يعود إلى debug signing عند غياب المفاتيح؛ exact permission مزدوجة؛ backups ممنوعة.
التغيير المحدد والخطوات: بعد D-02 افصل قبول QA الداخلي عن نشر المتجر وافشل نشر production دون هوية صحيحة. راجع الحاجة لكل permission وmerged manifest وسياسة alarm المختارة. حدّث privacy disclosure وdeletion لكل سجل مستقبلي؛ لا تطبع secrets ولا تضف analytics.
حدود التنفيذ: دفعة PS-04 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: signingReport بلا أسرار + cert APK + merged manifest + backup exclusion assertions + PII redaction. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا Debug certificate في مسار production المعتمد؛ لا توسيع backup/permissions دون D-02/D-03/D-05. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: فقد قابلية التحديث إذا تغيرت الحزمة/الشهادة. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F4-01 — عقد خطة الختمة وحساب الصفحات

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F4-01
Task Title: عقد خطة الختمة وحساب الصفحات
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: اتحاد daily ranges يساوي نطاق الخطة بلا نقص/تكرار؛ خطة 7 أيام توزع 604 إلى 86/87 صفحة؛ D-06 معتمد.
المتطلبات السابقة: AR-02، AR-05؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt
- feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt
- feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt
- feature/src/main/assets/mushaf_page_starts.json
السلوك الحالي بحسب baseline التخطيط: QuranGoal foundation غير موصول؛ pagesToAyahs تقريب 10.3 وموضع القراءة ليس سجل إكمال.
التغيير المحدد والخطوات: طوّر calculator موجودًا لخطط 7/30/60/custom بتوزيع صفحات 604 الفعلية ونطاق بداية/نهاية. عرف اليوم الشامل والموعد وحدود skipped/reschedule وإكمال explicit مع اقتراح reader اختياري. لا تحوّل قفزة القراءة إلى إنجاز تلقائي.
حدود التنفيذ: دفعة F4-01 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: 7/30/60، due today، overdue، partial page، ختمة جديدة، leap day، noncontiguous read. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: اتحاد daily ranges يساوي نطاق الخطة بلا نقص/تكرار؛ خطة 7 أيام توزع 604 إلى 86/87 صفحة؛ D-06 معتمد. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: خلط last-read بالإنجاز. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F4-02 — تخزين الختمة وترحيلها الآمن

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F4-02
Task Title: تخزين الختمة وترحيلها الآمن
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: كل بيانات v8 تبقى؛ خطة واحدة فعالة بحسب D-06؛ استعادة الخطة دون تغيير bookmarks.
المتطلبات السابقة: F4-01، G0-05؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt
- feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrations.kt
- feature/src/main/java/com/example/feature/quran/domain/model/QuranGoal.kt
- app/src/main/java/com/example/mol/di/DatabaseModule.kt
السلوك الحالي بحسب baseline التخطيط: لا تخزين فعلي لـQuranGoal مثبت.
التغيير المحدد والخطوات: أضف plan/day completion repository وجداول بإصدارات ومعرف ثابت وtransactions للإكمال/replan. version التالي يستخرج وقت التنفيذ؛ لا تفترض 9 إذا تغير HEAD. لا تكتب تقدمًا من موضع القراءة القديم إلا بموافقة المستخدم.
حدود التنفيذ: دفعة F4-02 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: جداول khatma مقترحة؛ رقم الإصدار يحدد وقت التنفيذ. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: migration all supported starts + reopen + interrupted transaction + unique day/range + cancel/reschedule. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: كل بيانات v8 تبقى؛ خطة واحدة فعالة بحسب D-06؛ استعادة الخطة دون تغيير bookmarks. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: تغيير schema وإعادة جدولة تفقد تاريخًا. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F4-03 — واجهة الختمة والإحصاء البسيط

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F4-03
Task Title: واجهة الختمة والإحصاء البسيط
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: تقدم وحفظ محلي وحساب الصفحات واضح؛ reader الحالي والهوية لا يتغيران.
المتطلبات السابقة: F4-02، UX-03؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/quran/presentation/QuranListScreen.kt
- feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt
- feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeQuranContinueCard.kt
- app/src/main/java/com/example/mol/navigation/Screen.kt
- app/src/main/java/com/example/mol/navigation/AppNavHost.kt
السلوك الحالي بحسب baseline التخطيط: استكمال القراءة ونسبة موضع موجودان؛ واجهة خطة غير مثبتة.
التغيير المحدد والخطوات: أضف نقطة خطة ضمن القرآن ثم setup/progress/reschedule/confirm completion. اعرض المقترح مقابل المنجز صراحة، ولا تنشئ بطاقة رحلة ثانية. الشاشات الجديدة أسماء مقترحة تحدد بعد إعادة الجرد.
حدود التنفيذ: دفعة F4-03 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: VM + Compose setup/edit/replan/rotate/offline + font/RTL؛ قرآن integrity. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: تقدم وحفظ محلي وحساب الصفحات واضح؛ reader الحالي والهوية لا يتغيران. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: ازدواج مؤشر الختمة مع موضع القراءة. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F4-04 — وصل سجل الصلاة الموجود

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F4-04
Task Title: وصل سجل الصلاة الموجود
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: خمس صلوات فقط؛ performed/late يدويان؛ error لا يبدو حفظًا؛ neutral unrecorded؛ D-03 معتمد.
المتطلبات السابقة: AR-05، G0-05؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/prayer/data/local/entity/PrayerLogEntity.kt
- feature/src/main/java/com/example/feature/prayer/data/local/dao/PrayerLogDao.kt
- feature/src/main/java/com/example/feature/prayer/data/repository/ManualPrayerLogRepositoryImpl.kt
- feature/src/main/java/com/example/feature/prayer/presentation/ManualPrayerTrackerViewModel.kt
- feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt
- feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt
- app/src/main/java/com/example/mol/di/ViewModelModule.kt
السلوك الحالي بحسب baseline التخطيط: Entity وDAO وVM وcard موجودة دون Room/DI أو عرض في PrayerScreen.
التغيير المحدد والخطوات: استكمل foundation نفسه بجداول/DAO/bindings وواجهة الصلاة. أصلح day rollover وwrite errors وtap serialization؛ لا تسجل Sunrise أو أداء الصلاة آليًا. أضف حذف السجل وفق سياسة الخصوصية المعتمدة.
حدود التنفيذ: دفعة F4-04 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: PrayerLogEntity داخل schema التالي الآمن. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: ManualPrayerLogRepositoryTest + DI + migration + duplicate rapid taps + midnight + private deletion. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: خمس صلوات فقط؛ performed/late يدويان؛ error لا يبدو حفظًا؛ neutral unrecorded؛ D-03 معتمد. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: بيانات عبادة حساسة. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F4-05 — تقويم سجل الصلاة والملخصات

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F4-05
Task Title: تقويم سجل الصلاة والملخصات
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا تضارب counters؛ unrecorded لا يصنف تقصيرًا؛ قراءة تاريخية خاصة ومحلية.
المتطلبات السابقة: F4-04، UX-01؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/statistics/domain/PrayerTrackerStatsCalculator.kt
- feature/src/main/java/com/example/feature/statistics/data/StatisticsRepositoryImpl.kt
- feature/src/main/java/com/example/feature/statistics/StatisticsScreen.kt
- feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeDailyJourneyCard.kt
السلوك الحالي بحسب baseline التخطيط: statistics النشط للأذكار؛ Home يعرض شرطة للصلاة عمدًا؛ calculator صلاة foundation.
التغيير المحدد والخطوات: وسّع repository والواجهة لليوم/آخر7/شهر مع اختيار يوم وتعديل يدوي. اربط بطاقة Home بالمصدر نفسه، واعرض unrecorded مستقلًا عن not performed؛ لا رسائل لوم أو حكم.
حدود التنفيذ: دفعة F4-05 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: range boundaries/zero records/month roll/leap + VM/error + Compose calendar/history/Home parity. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا تضارب counters؛ unrecorded لا يصنف تقصيرًا؛ قراءة تاريخية خاصة ومحلية. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: استنتاج سلوك ديني من غياب الإدخال. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F4-06 — إطلاق Widgets من العقد الموجود

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F4-06
Task Title: إطلاق Widgets من العقد الموجود
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: Widget من launcher يعمل offline ويتطابق مع الصلاة؛ لا نشاط foreground خفي أو حساب مستقل.
المتطلبات السابقة: AR-02، AR-05، PS-01، PS-04، G0-06؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/prayer/widget/PrayerWidgetState.kt
- feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt
- feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt
- app/src/main/AndroidManifest.xml
- app/src/main/java/com/example/mol/navigation/AppNavHost.kt
السلوك الحالي بحسب baseline التخطيط: PrayerWidgetStateFactory فقط؛ لا AppWidgetProvider/receiver metadata فعلي.
التغيير المحدد والخطوات: بعد D-07 اختر RemoteViews أو Glance بمبرر كلفة؛ أنشئ widget provider/config/resources للحالة القادمة واستكمال القرآن، واستخدم canonical calculator/NextPrayerSelector. لا ticker ثانية بالخلفية؛ اعرض last-updated/stale وحدود دقة العد. حدّث على settings/location/timezone/date ومع transition مناسب.
حدود التنفيذ: دفعة F4-06 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا schema مطلوب؛ لا snapshot يخزن PII زائد. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: factory + PendingIntent immutable identity + no-location + resize + launcher/reboot بعد الإذن + resume Quran target. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: Widget من launcher يعمل offline ويتطابق مع الصلاة؛ لا نشاط foreground خفي أو حساب مستقل. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: تحديثات launcher وقيود البطارية. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F5-01 — تطوير البحث العربي الموجود

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F5-01
Task Title: تطوير البحث العربي الموجود
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: بحث في السور/الحديث/الأذكار يحافظ على المصدر والترتيب المقبول وميزانية D-08.
المتطلبات السابقة: AR-01، PS-01؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/core/search/ArabicSearch.kt
- feature/src/main/java/com/example/feature/dashboard/presentation/GlobalLearningSearchViewModel.kt
- feature/src/main/java/com/example/feature/dashboard/GlobalSearchScreen.kt
- feature/src/main/java/com/example/feature/quran/data/repository/QuranRepositoryImpl.kt
- feature/src/main/java/com/example/feature/hadith/presentation/HadithViewModel.kt
السلوك الحالي بحسب baseline التخطيط: تطبيع عربي وفلاتر ونتائج sources موجودة؛ تجميع UI بسبعة VMs.
التغيير المحدد والخطوات: قِس ثم أضف ranking/category/error-per-source وتحسين empty للمحرك الحالي. precompute keys للقوائم الثابتة إذا برهن القياس. FTS مشروط بمقارنة query plan وحجم لا قرار تلقائي. لا تستبدل النص الأصلي بالمطبع.
حدود التنفيذ: دفعة F5-01 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: ArabicSearchTest/QuranSearchTest/DuaSearchRegressionTest + stale-query/source error + cold/warm p95. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: بحث في السور/الحديث/الأذكار يحافظ على المصدر والترتيب المقبول وميزانية D-08. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: ترتيب النتائج والذاكرة؛ index يتقادم. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F5-02 — محرك التقويم الهجري والتعديل المحلي

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F5-02
Task Title: محرك التقويم الهجري والتعديل المحلي
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: تاريخ واحد لكل مواضع العرض وإفصاح الرؤية؛ لا event ديني بلا مصدر.
المتطلبات السابقة: AR-02، UX-02؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/core/util/HijriDateFormatter.kt
- feature/src/main/java/com/example/feature/dashboard/presentation/DashboardDatePolicy.kt
- feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt
السلوك الحالي بحسب baseline التخطيط: HijrahDate.now نص فقط؛ API25 يرجع اليوم؛ لا interactive calendar.
التغيير المحدد والخطوات: أدخل Clock/Zone وخدمة HijriDateProvider مشتركة، مع offset محلي محدود حسب D-09 وتسمية حسابي/محلي. افصل اليوم المدني عن بداية اليوم الشرعي التي تحتاج قرارًا؛ لا تؤثر على مواقيت الصلاة.
حدود التنفيذ: دفعة F5-02 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: boundaries/offset +/-/DST/API25 fallback/unit conversion + consistency Home/Profile/Prayer. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: تاريخ واحد لكل مواضع العرض وإفصاح الرؤية؛ لا event ديني بلا مصدر. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: اختلاف الحساب والرؤية المحلية. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F5-03 — واجهة التقويم والأحداث الموثقة

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F5-03
Task Title: واجهة التقويم والأحداث الموثقة
Task Priority: P1
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: واجهة تعمل offline وتعكس offset وتفصح الاحتمال الحسابي دون ادعاء رؤية رسمية.
المتطلبات السابقة: F5-02؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/core/util/HijriDateFormatter.kt
- feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt
- app/src/main/java/com/example/mol/navigation/Screen.kt
- app/src/main/java/com/example/mol/navigation/AppNavHost.kt
السلوك الحالي بحسب baseline التخطيط: لا route تقويم تفاعلي ضمن 58.
التغيير المحدد والخطوات: أضف شهر/يوم والتنقل لليوم الحالي وإعداد offset؛ assets للأحداث فقط بعد D-09 بمصدر وإصدار ومراجعة. أبق العرض خاليًا إن لم يعتمد المحتوى؛ لا تخترع مناسبة.
حدود التنفيذ: دفعة F5-03 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: Compose month navigation/RTL/font + event source + timezone restore. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: واجهة تعمل offline وتعكس offset وتفصح الاحتمال الحسابي دون ادعاء رؤية رسمية. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: صحة المحتوى والتواريخ. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F5-04 — عقد الحفظ والمراجعة وتخزينهما

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F5-04
Task Title: عقد الحفظ والمراجعة وتخزينهما
Task Priority: P2
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: جدول مراجعة خاص قابل للتعديل؛ لا تقييم ديني ولا تنزيل صوت لتفعيل الحفظ.
المتطلبات السابقة: AR-05، G0-05، UX-03؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/quran/domain/model/QuranRepeat.kt
- feature/src/main/java/com/example/feature/quran/domain/model/QuranNavigation.kt
- feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt
- feature/src/main/java/com/example/feature/quran/presentation/QuranRepeatSheet.kt
السلوك الحالي بحسب baseline التخطيط: repeat audio موجود/foundation؛ لا سجل مراجعة متباعدة موصول.
التغيير المحدد والخطوات: عرّف نطاق آيات محفوظة وmanual self-check وdue dates deterministic. أضف تخزين review events مع source verse keys وschema التالي. الإخفاء عرض فقط ولا يحذف/يعدل نصًا. لا صوت AI.
حدود التنفيذ: دفعة F5-04 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: جداول memorization/review مقترحة. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: spaced review deterministic + same-day repeat/overdue/clock + migration/orphan references. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: جدول مراجعة خاص قابل للتعديل؛ لا تقييم ديني ولا تنزيل صوت لتفعيل الحفظ. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: فقد الارتباط بالآيات. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F5-05 — واجهة الاختبار الذاتي اليدوي

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F5-05
Task Title: واجهة الاختبار الذاتي اليدوي
Task Priority: P2
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: الآيات تكشف كاملة بالترتيب؛ مراجعة due تعمل دون شبكة؛ لا امتداد صوتي ضمن الدفعة.
المتطلبات السابقة: F5-04؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt
- feature/src/main/java/com/example/feature/quran/presentation/MushafPage.kt
- app/src/main/java/com/example/mol/navigation/Screen.kt
السلوك الحالي بحسب baseline التخطيط: لا UI حفظ مثبتة؛ أفعال الآية موجودة.
التغيير المحدد والخطوات: أضف اختيار نطاق وإخفاء/كشف واختبار ذاتي وتسجيل النتيجة باليد؛ حافظ على صفحة المصحف الحالية وأضف وضعًا اختياريًا منفصلًا لعرض الاختبار. لا ألوان توحي بحذف الآيات أو تغيير النص.
حدود التنفيذ: دفعة F5-05 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: Compose select/hide/reveal/back/TalkBack + offline restore + full Quran hash. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: الآيات تكشف كاملة بالترتيب؛ مراجعة due تعمل دون شبكة؛ لا امتداد صوتي ضمن الدفعة. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: تداخل gestures وتحديد الآيات. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F5-06 — ملاحظات الآيات الخاصة وتخزينها

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F5-06
Task Title: ملاحظات الآيات الخاصة وتخزينها
Task Priority: P2
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا تعديل للآيات ولا التفسير؛ ملاحظة لا تظهر كقول ديني؛ D-03 يحدد الحذف.
المتطلبات السابقة: UX-03، AR-05، G0-05، PS-04؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt
- feature/src/main/java/com/example/feature/quran/presentation/TafsirBottomSheet.kt
- feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt
السلوك الحالي بحسب baseline التخطيط: tafsir محجوب مصدرًا؛ لا notes repository مثبت.
التغيير المحدد والخطوات: أضف note CRUD/search برابط surah/ayah، كاتب محلي وتواريخ. افصل عنوان ملاحظة شخصية عن تفسير معتمد؛ لا تستعمل الملاحظة لتعويض tafsir. حفظ transaction وdraft error واضح.
حدود التنفيذ: دفعة F5-06 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: جدول ayah_notes مقترح؛ فهرس verse key. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: migration + duplicate/long text/delete/cancel/recreate + Arabic note search + privacy deletion. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا تعديل للآيات ولا التفسير؛ ملاحظة لا تظهر كقول ديني؛ D-03 يحدد الحذف. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: محتوى شخصي حساس. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F5-07 — تصدير اختياري آمن للملاحظات

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F5-07
Task Title: تصدير اختياري آمن للملاحظات
Task Priority: P2
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا أسماء/هاتف/موقع/سجل عبادة ضمن export notes دون اختيار؛ لا path traversal أو محتوى زائد.
المتطلبات السابقة: F5-06؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/ehsan/data/image/EhsanImageStore.kt
- app/src/main/AndroidManifest.xml
- app/src/main/res/xml/backup_rules.xml
السلوك الحالي بحسب baseline التخطيط: backup عام ممنوع ولا export notes موجود.
التغيير المحدد والخطوات: بعد D-05 أضف export محدد النطاق عبر SAF يختاره المستخدم مع preview وتحذير ما سيخرج. لا توسع cloud backup. نسخة أولى للتصدير فقط؛ restore شامل مشروع آخر يحتاج عقد version/validation/conflicts.
حدود التنفيذ: دفعة F5-07 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: UTF-8/Arabic/empty/cancel/IO failure/oversize + exported content allowlist. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا أسماء/هاتف/موقع/سجل عبادة ضمن export notes دون اختيار؛ لا path traversal أو محتوى زائد. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: تسريب بيانات عبر التصدير. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F6-01 — متابعة الصيام المحلية

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F6-01
Task Title: متابعة الصيام المحلية
Task Priority: P2
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: حفظ يدوي وخصوصية ومصدر مناسبات؛ لا أحكام فقهية أو لوم.
المتطلبات السابقة: F5-03، F4-05؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/core/data/local/database/IhsanDatabase.kt
- feature/src/main/java/com/example/feature/statistics/StatisticsScreen.kt
- feature/src/main/java/com/example/feature/core/preferences/UserPreferences.kt
السلوك الحالي بحسب baseline التخطيط: لا سجل صيام موصول ضمن مصادر الإنتاج.
التغيير المحدد والخطوات: بعد D-03/D-09 أضف manual log/date/type مع CRUD وملخص خاص. لا تحسب قضاءً أو فدية أو حكمًا آليًا ولا mark automatic. اختياريًا اربط يوم calendar بلا inference.
حدود التنفيذ: دفعة F6-01 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: جدول fasting_log مقترح. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: migration + duplicate date/undo/month/offset + privacy delete + offline VM. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: حفظ يدوي وخصوصية ومصدر مناسبات؛ لا أحكام فقهية أو لوم. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: حساسية البيانات وحدود الفقه. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F6-02 — مركز رمضان يربط الموجود

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F6-02
Task Title: مركز رمضان يربط الموجود
Task Priority: P2
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: كل رقم من مصدره الأصلي؛ لا يدعي موعدًا معتمدًا محليًا إن كان حسابيًا.
المتطلبات السابقة: F6-01، F4-03؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/prayer/domain/facade/PrayerTimesFacade.kt
- feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt
- feature/src/main/java/com/example/feature/quran/presentation/QuranListScreen.kt
- app/src/main/java/com/example/mol/navigation/Screen.kt
السلوك الحالي بحسب baseline التخطيط: لا مركز موحد؛ الصلاة والرحلة والقرآن موجودة.
التغيير المحدد والخطوات: اجمع روابط الختمة والصيام والتقويم؛ الفجر/المغرب من facade مع تسمية حدود السحور/الإفطار المعتمدة D-09؛ لا calculator أو counters جديدة. قواعد إبراز الموسم قابلة للإيقاف.
حدود التنفيذ: دفعة F6-02 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: parity Fajr/Maghrib+timezone/location + no-location + seasonal entry + offline navigation. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: كل رقم من مصدره الأصلي؛ لا يدعي موعدًا معتمدًا محليًا إن كان حسابيًا. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: تفسير التوقيت الشرعي وازدواج البيانات. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F6-03 — تطوير مسار التعلم الموجود

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F6-03
Task Title: تطوير مسار التعلم الموجود
Task Priority: P2
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا فيديو ينزل خارج إذن المصدر؛ لا محطة/وصفة/ربط ديني مولد؛ التاريخ لا يضيع.
المتطلبات السابقة: UX-05، AR-05؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- feature/src/main/java/com/example/feature/learning/data/LearningBookmarkStore.kt
- feature/src/main/java/com/example/feature/fahmanallah/data/repository/FahmRepositoryImpl.kt
- feature/src/main/java/com/example/feature/fahmanallah/presentation/FahmJourneyScreen.kt
- feature/src/main/java/com/example/feature/sanhya/data/repository/SanhyaRepositoryImpl.kt
- feature/src/main/java/com/example/feature/nabiihsan/data/repository/NabiIhsanRepositoryImpl.kt
السلوك الحالي بحسب baseline التخطيط: favorites/watch later وFahm completion موجودة؛ stations بلا episode links وبعض recipes غير متاحة.
التغيير المحدد والخطوات: وحّد تقدمًا يدويًا/مشاهدة موضحة دون ازدواج library. أضف ترتيب playlist وتنقل واستكمال اختياري؛ ربط المحطات والعناوين فقط ببيانات معتمدة. احتفظ keys الحالية أو migrate بإصدارات.
حدود التنفيذ: دفعة F6-03 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: existing repository persistence + completion/reopen/reorder/missing episode/no network + migration keys. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا فيديو ينزل خارج إذن المصدر؛ لا محطة/وصفة/ربط ديني مولد؛ التاريخ لا يضيع. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: حقوق المحتوى وتغيير معرفات library. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## F6-04 — قبول الميزات المتكامل

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: F6-04
Task Title: قبول الميزات المتكامل
Task Priority: P2
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: جميع routes الجديدة مسجلة وأدلة same APK؛ المستقبل لا يعيد فتح تعريف Beta القديم تلقائيًا.
المتطلبات السابقة: F6-02، F6-03، F5-07؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- app/src/main/java/com/example/mol/navigation/AppNavHost.kt
- app/src/androidTest/java/com/example/mol/ui/ReleaseFunctionalJourneyTest.kt
- app/src/androidTest/java/com/example/mol/ui/ReleaseCorePersistenceTest.kt
السلوك الحالي بحسب baseline التخطيط: المزايا المستقبلية ستزيد routes/schema؛ 58/59 baseline لا يكفي لها.
التغيير المحدد والخطوات: حدّث matrix من routes الجديدة وقبول رحلات plan→reader→manual completion وprayer→history→widget وcalendar→fasting→Ramadan وnotes/export؛ لا تعيد تسمية المزايا القديمة.
حدود التنفيذ: دفعة F6-04 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: offline-first E2E + migration old/current/new + process restore + font/RTL + security+baseline performance. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: جميع routes الجديدة مسجلة وأدلة same APK؛ المستقبل لا يعيد فتح تعريف Beta القديم تلقائيًا. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: تفاعل الميزات والترحيلات المتسلسلة. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## R7-01 — دراسة الميزات المشروطة دون تشغيل خدمات

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: R7-01
Task Title: دراسة الميزات المشروطة دون تشغيل خدمات
Task Priority: P3
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: كل ميزة مشروطة تبقى Deferred إلى موافقة مستقلة؛ الحج يحتاج مراجع معتمدة.
المتطلبات السابقة: PS-04؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- docs/release/FUTURE_BACKEND_CONTRACTS.md
- docs/release/EXTERNAL_BLOCKERS_DECISIONS.md
- docs/store/PRIVACY_POLICY.md
السلوك الحالي بحسب baseline التخطيط: لا موافقة تنفيذ صوت AI أو مجتمع حقيقي أو تبرعات.
التغيير المحدد والخطوات: أنشئ decision briefs مستقلة للصوت/مساعد القرآن/الحج والعمرة/المجتمع/التبرعات. حدد accuracy/evaluation/privacy/retention/provider license/moderation/payment/legal owner/cost. وثائق فقط؛ لا API key ولا backend ولا dataset مرفوع.
حدود التنفيذ: دفعة R7-01 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: مراجعة completeness والقرارات ومعايير go/no-go؛ لا اختبار خدمات حقيقية. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: كل ميزة مشروطة تبقى Deferred إلى موافقة مستقلة؛ الحج يحتاج مراجع معتمدة. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: حقوق وصحة وخصوصية وتشغيل خارجي. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## R7-02 — إغلاق حقوق المحتوى وهوية النشر

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: R7-02
Task Title: إغلاق حقوق المحتوى وهوية النشر
Task Priority: P0
التبعيات المشروطة: لا توجد
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: لا blanket approved بسبب HTTP؛ D-01/D-02/D-10 وحواجز النشر موثقة؛ المنتج محدود داخليًا إن لم تغلق.
المتطلبات السابقة: G0-01، PS-04؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- docs/release/EXTERNAL_ASSET_REGISTER.csv
- docs/release/CONTENT_SOURCES_AND_LICENSES.md
- docs/release/ASSET_CREDITS.md
- docs/PRODUCTION_IDENTITY_DECISION.md
- docs/RELEASE_SIGNING_GUIDE.md
السلوك الحالي بحسب baseline التخطيط: recordings/HLS/رسومات/أذان/محتوى تحريري لها حالات معلقة؛ هوية QA ليست متجرًا.
التغيير المحدد والخطوات: طابق asset hash بمستند حقوق فعلي ونطاق territory/distribution/attribution؛ سجل إزالة/استبدال مقترح فقط إذا لم يوافق المالك. ثبّت package/cert وتحديث البيانات مع D-02. لا تراسل أصحاب الحقوق دون إذن منفصل.
حدود التنفيذ: دفعة R7-02 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: asset-to-license coverage + missing-license blocker + cert/appId/update fixture. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: لا blanket approved بسبب HTTP؛ D-01/D-02/D-10 وحواجز النشر موثقة؛ المنتج محدود داخليًا إن لم تغلق. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: ترخيص خارجي وتوقيع غير قابل للتبديل بلا أثر. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```

## R7-03 — قرار الإصدار النهائي المستقل

```text
Senior Android Developer + Senior UI/UX Engineer + Senior QA Engineer
Task ID: R7-03
Task Title: قرار الإصدار النهائي المستقل
Task Priority: P0
التبعيات المشروطة: F6-04 فقط لمسار Feature-Release؛ R7-02 فقط عند اختيار النشر العام. Beta-Current لا يعتمد أي ميزة مستقبلية أوكل تحسيناتPhase1؛ يعالج فقط عيوبه المثبتة.
Required Role: الأدوار أعلاه؛ مراجعة Architecture/Data/Security عند أثرها دون توسيع النطاق.
الهدف: التفريق بين Code Implemented/Build/Unit/Device/Release محفوظ؛ لا نشر قبل قرار المالك.
المتطلبات السابقة: G0-01، G0-02، G0-03، G0-04، G0-05، G0-06؛ اعتماد ناتج فحص لا يساوي قبول المنتج.
ملفات البداية (اقرأ النسخة الفعلية وحدد الرموز واستعمالاتها):
- docs/planning/IHSAN_QA_AND_RELEASE_GATES.md
- docs/release/BETA_ACCEPTANCE_MATRIX.md
- docs/qa/FINAL_BETA_GAP_REPORT.md
السلوك الحالي بحسب baseline التخطيط: لا Release Accepted يثبت كل أبعاد الحالة الحالية.
التغيير المحدد والخطوات: اجمع build/unit/device/privacy/content/performance على artifact واحد. أصدر تقرير قبول/رفض محدد التوزيع. Beta الحالي يعتمد G0 والإصلاحات الحرجة فقط؛ إصدار الميزات يعتمد F6-04، والمتجر يعتمد R7-02؛ لا تنشر أو ترفع تلقائيًا.
حدود التنفيذ: دفعة R7-03 فقط. افحص git status ولا تطمس عملًا سابقًا. Kotlin + Compose + Material 3 وKoin الحاليون؛ لا Flutter أو إعادة معمارية شاملة. لا تعدّل القرآن ولا تولّد محتوى دينيًا. لا تتجاوز YouTube أو الحقوق. لا Backend/Analytics أو مراسلات أو نشر تلقائي. لا Commit/Push/Reset/Rebase أو حذف بيانات. فرّق بين إصلاح وميزة مستقبلية.
الملفات المتوقع تعديلها: ملفات البداية ذات الصلة بعد إثبات الحاجة + الاختبارات المقابلة + تقرير الدفعة. ملفات تقارير QA ليست إذنًا لتعديل الإنتاج. أي ملف إنتاج إضافي يحتاج سببًا محددًا؛ إن صح السلوك أثبته ولا تغيّره لمجرد التنسيق.
أثر البيانات والترحيل: لا تغيير Schema. لا destructive fallback أو downgrade أو رقم schema تخميني؛ حافظ على البيانات والمفاتيح القديمة. لا backup لخادم.
الاختبارات الإلزامية: signed artifact hash + full regression matrix + zero critical open + explicit owner acceptance. بعد تغيير الإنتاج شغّل الوحدة والبناء/Lint المتأثرين؛ احتفظ XML وcommand/exit code وAPK/source SHA-256. غير المنفذ Not Tested.
الهاتف: أي اختبار Android حصريًا Xiaomi 23129RAA4G / ADB ac190123؛ تحقق adb -s ac190123 get-state وmodel والحزمة والشهادة قبل التثبيت. لا Emulator/AVD أو جهاز بديل. لا uninstall أو clear data أو تغيير بيانات حقيقية. fixtures وحزمة اختبار معتمدة واستعادة التفضيلات. عند غياب الهاتف: Device QA Pending. reboot يحتاج موافقة خاصة بعد التحضير. مهام الوثائق G0-01 وR7-01 لا تشغّل الهاتف.
شروط القبول: التفريق بين Code Implemented/Build/Unit/Device/Release محفوظ؛ لا نشر قبل قرار المالك. افصل Implemented/Build Passed/Unit Passed/Device QA Passed/Release Accepted.
المخاطر: قبول أوسع من نطاق الدليل. إذا تطلب جزء قرارًا D-xx غير معتمد سجله وتوقف عن الجزء المعتمد عليه.
التراجع: نسخة فرق الدفعة وعكس تعديلاتها وحدها دون Reset؛ بعد ترحيل DB استخدم forward fix أو نسخة اختبار موثوقة، ولا ترجع APK أقدم فوق schema أحدث. استعادة التفضيلات جزء من القبول.
التقرير النهائي: Task ID؛ hashes المصدر وAPK والحزمة والشهادة؛ التغييرات والرموز؛ ما نجح وما فشل؛ أوامر ونتائج الاختبارات وskipped؛ أدلة الهاتف أو Not Tested؛ حفظ البيانات؛ المخاطر والقرارات؛ هل الدفعة Ready for Acceptance ولماذا. توقف ولا تبدأ التالية قبل اعتماد هذه الدفعة.
```


