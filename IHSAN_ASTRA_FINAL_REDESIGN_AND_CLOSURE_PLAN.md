# Ihsan Astra — Final Redesign and Product Closure Plan

> **نوع الوثيقة:** خطة فحص وتنفيذ فقط — المرحلة A
> **تاريخ الفحص:** 2026-09-10 (Asia/Damascus)
> **النطاق المسموح في هذه الجولة:** قراءة المستودع وتعديل هذه الوثيقة فقط
> **حالة التنفيذ:** **لم يبدأ التنفيذ؛ يلزم اعتماد المستخدم الصريح لكل دفعة**
> **قاعدة الإثبات:** الكود والـworking tree الحاليان يسبقان أي تقرير أو لقطة سابقة.

---

## 1. Executive Summary

«إحسان» تطبيق Android عربي أولًا، محليّ البيانات في أغلب وظائفه، مبني حاليًا على ثلاث وحدات (`:app`, `:feature`, `:designsystem`) وعلى Jetpack Compose وMaterial 3 وKoin وRoom وDataStore وWorkManager وMedia3. مسار التشغيل الفعلي يبدأ من `MainActivity` ثم `MainScreen` ثم `AppNavHost`، وتوجد ثلاثة جذور فقط في Bottom Navigation: الرئيسية، إحسان، وحسابي.

المنتج يملك أساسًا حديثًا ينبغي الحفاظ عليه: Home/Ehsan/Profile وBottom Navigation خضعت لأعمال حديثة في الـworking tree، و`designsystem` يحوي semantic colors وspacing/dimens ومكوّن Liquid Glass خفيفًا وحالات Loading/Empty/Error/Capability. المشكلة ليست نقص ألوان جديدة؛ بل عدم اكتمال التسلسل البصري واتساق عائلات المكوّنات، مع عدد من العقود المرئية غير الصادقة أو غير المكتملة.

أهم نتيجة من الفحص أن هناك مسارين مستقلين يجب عدم دمجهما في دفعة واحدة:

- **Track A — UI/UX Redesign:** توحيد التسلسل، top bars، أسطح المحتوى، القراءة، النماذج، الحالات، RTL، Dark Mode، typography العربية، وإمكانية الوصول عبر قرابة 29 وجهة/سطح Runtime مسجل. هذا المسار أوسع انتشارًا لأنه يمس معظم أسطح Runtime.
- **Track B — Logic / Product Closure:** إزالة أو إكمال no-op CTAs، إصلاح بوابة الموقع، ربط تنزيل القرآن بـWorkInfo الحقيقي، استمرارية الصور والتذكيرات، صدق الإبلاغ والإحصاءات، ودقة تسميات الحالة. هذا المسار أقل عددًا من حيث العائلات، لكنه أعلى مخاطرة لأن أخطاءه تمس الوصول والبيانات وصدق المنتج والخلفية.

لا توضع نسبة عددية قبل baseline قابل للقياس وتفكيك الدفعات؛ اتساع الواجهة لا يعني أن مخاطرها أعلى من مخاطر الاستمرارية أو الصلاحيات أو الجدولة.

الخطة تقترح **12 مرحلة تنفيذية**. يبدأ المسار الإلزامي بـbaseline آمن، ثم إصلاح بوابة الموقع، ثم إزالة النجاح الوهمي والبيانات الافتراضية والأزرار الصامتة. بعد ذلك تأتي دفعات مستقلة لصدق تنزيل القرآن، واستمرارية صور إحسان، وعقد التذكيرات. يمكن أن تبدأ مؤسسة Design System المصغرة والعينة الممثلة (Prayer + Quran Reader + Settings) بالتوازي مع دفعات المنطق المعتمدة عندما لا تتصادم الملفات وتكون الاعتماديات معلنة؛ لا يبدأ تعميم التصميم قبل إغلاق العيوب عالية المخاطر ذات الصلة. يلي ذلك تعميم التصميم، ثم التدقيق الشامل وQA والإغلاق.

### حكم المرحلة A

- **Verified in code:** البنية، الإصدارات، routes، ViewModels/UiState، مصادر التخزين، وعقود no-op المبينة أدناه.
- **Verified at runtime في هذه الجولة:** توفر ADB وجهاز Android 15/API 35 متصل، ووجود `com.example.mol` مثبتًا بإصدار 1.0 فقط. لم تُشغّل تدفقات التطبيق.
- **Not runtime-verified:** أي مظهر شاشة أو نجاح تدفق أو crash أو استمرارية؛ الـAPK المثبت بتاريخ 2026-09-06 لا يمكن إثبات مطابقته للـworking tree.
- **Blocked by current Stage A scope:** Build/compile/R8، unit/instrumented tests، إنشاء أو تثبيت APK جديد، screenshots جديدة، وفحص release signing الفعلي. هذا الحظر متعلق بصلاحية الجولة الحالية ولا يثبت وجود عائق تقني أو عائق credentials لكل نوع تحقق.
- **Handoff limitation:** لم يُرفق نص handoff فعلي؛ الرسالة احتوت placeholder فقط. أُعيد التحقق من قائمة العيوب الواردة في التكليف مباشرة من الكود بدل اعتبارها حقائق.

---

## 2. Current Product Understanding

### 2.1 Product model

التطبيق companion يومي محلي أولًا، وليس منصة تبرعات متصلة بخادم. الوظائف الحالية المثبتة في الكود:

| المجال | الوظيفة الحالية | مصدر البيانات/الخدمة | البقاء والاستمرارية |
|---|---|---|---|
| Home | تجميع الصلاة، الأنشطة، الأسماء، إحسان، وبعض اختصارات الخدمات | Observe use cases داخل `HomeDashboardUseCases.kt` | Room/DataStore حسب القسم؛ حالة sheets مؤقتة في VM |
| Prayer | حساب المواقيت، الموقع المحفوظ/اليدوي، إعداد الحساب، جدولة الإنذارات وتشخيصها | `PrayerTimesFacade` + repositories + AlarmManager | Settings في DataStore؛ حالة الجدولة runtime store؛ موقع محفوظ في DataStore |
| Quran | قائمة/بحث/مفضلة/قارئ/صوت/تنزيل | assets + Room + Media3 + WorkManager + files | آخر قراءة في DataStore/Repository؛ bookmarks/download rows في Room؛ الصوت في ملفات داخلية |
| Azkar/Tasbih | قراءة، تصنيف، بحث، مفضلة، عدادات | Room + use cases | Room؛ حجم الخط/الاهتزاز في DataStore |
| Dua/Hadith | محتوى محلي، بحث/تصنيف/مفضلة، تفاصيل ومشاركة | assets/Room + repositories | Room |
| Asma | قائمة/تفاصيل/مفضلة واسم يومي | asset/local datasource + DataStore | المفضلة في DataStore |
| Ehsan | لوحة عروض وطلبات محلية، إضافة، تفاصيل، اتصال مباشر | Room | الصفوف باقية؛ الصور حاليًا تحفظ URI خامًا وغير مضمونة بعد restart |
| Profile | ملف محلي وتاريخ القوائم وإعدادات ودعم | Room + package info + external intents | Room؛ لا توجد هوية سحابية |
| Settings | حجم النص، dark mode، vibration، صوت الأذان | Settings DataStore + UserPreferences | باقٍ محليًا |
| Statistics | إحصاءات الأذكار فقط رغم عنوان عام | Azkar repository/Room | Room |
| Reminders | تذكيران للأذكار بواسطة WorkManager | حالة Composable + WorkManager | العمل قد يبقى، لكن switch/time لا يُستعادان من مصدر حالة؛ WorkManager يؤخر عملًا قابلًا للتنفيذ لاحقًا ولا يضمن دقيقة محددة أو تسليمًا exactly-once |
| Qibla | اتجاه القبلة من GPS والمستشعر | Fused Location + sensor manager | لا يعيد استخدام fallback المحفوظ/اليدوي في Prayer حاليًا |
| Live | HLS مع fallback إلى YouTube WebView/Intent | شبكة + Media3/WebView | لا offline زائف؛ player يُحرر عند الخروج |
| Onboarding | ثلاث صفحات ثابتة ثم حفظ الاكتمال | DataStore | `hasCompletedOnboarding` |

### 2.2 Runtime route inventory

`Screen.kt` يعلن 30 route objects؛ 29 منها تسجل سطحًا أو redirect في `AppNavHost`، مع ملاحظتين:

1. `LegacyDonationDetail` redirect توافق فقط ولا يرسم UI.
2. `IhsanPlusDaily` يسجل فقط عندما `IHSANPLUS_DAILY_ENABLED=true`؛ هو مفعّل في debug ومعطل في release. Charity Trust demo غير مفعّل دائمًا.

الوجهات المسجلة: Splash، Onboarding، Location Permission، Home، IhsanPlus Daily (debug conditional)، Haram Live، Nabawi Live، Tasbih، Global Search، Daily Activities، Qibla، Asma، Prayer، Quran List، Quran Reader، Azkar، Hadith، Dua، Dua Detail، Settings، Statistics، Reminders، Ehsan، Add Donation، Request Help، legacy redirect، Ihsan Details، Profile، Donation History، Edit Profile.

### 2.3 Runtime path and ownership

```text
IhsanApp/Application
  ├─ starts Koin modules
  └─ schedules periodic Adhan + two Azkar workers

MainActivity
  ├─ SettingsViewModel → darkTheme
  ├─ Activity-level location permission gate
  └─ MainScreen (only when fine location is granted)
       ├─ NavController + 3-root Bottom Navigation
       └─ AppNavHost(start = Splash)
            ├─ Route Composable
            ├─ lifecycle-aware UiState collection in most runtime screens
            ├─ ViewModel / events
            ├─ UseCase / facade
            ├─ Repository contract
            └─ Room / DataStore / assets / Android service
```

الـActivity gate الحالية تقطع هذا المسار بالكامل عند غياب إذن الموقع؛ لذلك Splash/Onboarding/route-level permission لا تبدأ أصلًا في fresh install حتى يمنح المستخدم الإذن من gate أخرى. هذه ليست ملاحظة تصميمية بل تعارض تحكم P0.

---

## 3. Repository Verification and Evidence

### 3.1 Repository snapshot

| البند | النتيجة | التصنيف | الدليل |
|---|---|---|---|
| الفرع | `fix/audio-runtime-adhan-quran` | Verified in code/repo | `git branch --show-current` |
| HEAD | `da7967ca9d0b83d1d906081e962966625a8f918f` | Verified | `git rev-parse HEAD` |
| آخر commit | `da7967c` — `fix(home-ui): isolate localized dashboard resources` — 2026-07-28 | Verified | `git log -1` |
| worktree | Dirty: 25 tracked paths في diff (1802 insertions/1187 deletions) و250 untracked قبل إنشاء هذه الوثيقة | Verified | `git status`, `git diff --stat`, `git ls-files --others` |
| الوحدات | `:app`, `:feature`, `:designsystem` | Verified | `settings.gradle.kts:16-18` |
| Gradle wrapper | 8.10.2 | Verified from config، not executed | `gradle-wrapper.properties` |
| AGP / Kotlin / KSP | 8.7.3 / 2.1.0 / 2.1.0-1.0.29 | Verified | `gradle/libs.versions.toml:2,9,12` |
| Compose | BOM `2024.12.01`; Material 3 بلا رقم منفرد | Verified | catalog + module Gradle files |
| SDK | compile 35، target 35، min 25 | Verified | `app/build.gradle.kts:10-17` |
| DI | Koin 3.5.6، ownership في app modules مع feature modules included | Verified | `AppModule.kt:16-37` |
| DB | Room 2.6.1؛ `IhsanDatabase` version 6؛ migrations 2→3،3→5،5→6 | Verified | `IhsanDatabase.kt:23-37`, `IhsanDatabaseMigrations.kt:22-80` |
| Schema WIP | يوجد `7.json` untracked بينما annotation ما زال version 6 | Verified، غير مفسر | filesystem + current database source |
| Media/background | Media3 1.5.0، WorkManager 2.10.0، AlarmManager receivers | Verified | catalog + Manifest `41-64` |
| Release signing | wiring موجود؛ المفاتيح المطلوبة غير مهيأة في env/local properties وفق فحص أسماء فقط | Verified without secrets | `app/build.gradle.kts:20-47`; boolean-only inspection |
| جهاز | Xiaomi model 23129RAA4G متصل، Android 15/API 35، 1080×2400@440، font scale 1.0 | Verified environment only | read-only ADB queries |
| اختبارات موجودة | 84 ملفات test/androidTest؛ Compose UI test APIs موجودة؛ لا Paparazzi/Roborazzi/Shot | Verified files, not run | file inventory + Gradle |

### 3.2 Protected WIP state

الـworking tree ليس قاعدة نظيفة. التعديلات الحديثة تشمل Home/Ehsan/Profile/designsystem، بينما ملفات WIP غير المتصلة تشمل — من بين أمور أخرى — `EhsanImageStore`, Quran repeat/goal/download scheduler، manual prayer tracker، prayer widget، وschema 7. كل هذه ملك للمستخدم وتُعامل كمدخلات محمية:

- لا `reset/checkout/restore/stash/clean`.
- لا حذف لملف legacy أو WIP لمجرد أنه غير reachable.
- قبل كل دفعة: snapshot قراءة فقط لأسماء الملفات وdiff المسموح.
- لا commit شامل؛ أي commit لاحق — إن طلبه المستخدم — يقتصر على ملفات الدفعة.
- التعديل في ملف WIP مسموح فقط إذا شمل اعتماد الدفعة الملف ونطاق التعديل. يُسجل baseline وتُراجع الفروق قبل وبعد. إذا لم يشمله الاعتماد، أو ظهر تغيير جديد غير متوقع، يتوقف الوكيل عن تعديل الملف ويطلب توجيهًا.

### 3.3 Evidence classes

#### Verified in code

- MainActivity gate: `MainActivity.kt:42-66`.
- `AppNavHost` start destination/route map: `AppNavHost.kt:67-443`.
- Bottom Navigation الثلاثي والحفظ/الاستعادة: `MainScreen.kt:39-97`.
- current ViewModels/UiState/Actions ومصادر Room/DataStore/WorkManager كما هو موثق في الأقسام التالية.
- عيوب no-op والـhardcoded state الواردة في Logic Completion.

#### Verified at runtime

- الجهاز متصل وpackage `com.example.mol` مثبت (`versionCode=1`, `versionName=1.0`, last update 2026-09-06).
- لا توجد في هذه الجولة مشاهدة runtime لشاشة من التطبيق، ولا إثبات أن APK المثبت مبني من HEAD أو من الـworking tree الحالي.

#### Inferred

- كثافة cards/chips وتفاوت top bars مستنتجان من بنية Composables ومن لقطات تاريخية؛ يجب إعادة إثباتهما بصور baseline جديدة.
- مخاطر القص عند 2.0x مستنتجة من heights ثابتة و`maxLines=1` واستخدام أحجام `sp` متناثرة؛ لا تسجل FAIL قبل runtime.
- الصور المسماة `runtime-*` تاريخية وغير موثوقة وحدها: `runtime-quran.png` يعرض معرض صور خارجي، و`runtime-current.png` إطار أسود/تحميل، لذا لا تستخدم كـgolden.

#### Not verified / Blocked

- Build/lint/tests/APK/install: ممنوعة في المرحلة A.
- Light/Dark/RTL/TalkBack/font 1.3x/2.0x على التطبيق الحالي: NOT RUN.
- migration upgrade فعلي واستمرارية الصور/الأعمال: NOT RUN.
- Release compile/R8 وartifact behavior: NOT RUN و**محجوبان بصلاحية Stage A الحالية**؛ غياب مفاتيح التوقيع المقصودة لا يثبت وحده أن compile/R8 غير ممكنين، لأن ذلك يعتمد على إعداد variant.
- شهادة التوقيع المقصودة والتثبيت/الترقية بتوقيع متوافق: BLOCKED حاليًا بسبب عدم تهيئة credentials المقصودة وفق الفحص المحدود، وبسبب عدم وجود تفويض تنفيذ أو بيئة ترقية معتمدة. نجاح debug signing fallback مستقبلًا لا يثبت قابلية التوزيع.
- handoff الكامل: غير متاح.

### 3.4 Phase 0 provenance manifest — planned, not created

`HEAD` مع hash لـtracked dirty diff لا يكفيان لإعادة إنتاج baseline إذا دخلت ملفات untracked في source sets أو Gradle inputs. عند اعتماد Phase 0 فقط، ينشأ manifest إثبات خارج source ويشمل:

- commit SHA والفرع وحالة tracked/untracked ذات الصلة.
- بصمات محتوى ملفات source والإعدادات الداخلة فعليًا في البناء، بما فيها untracked inputs؛ لا يكتفى بأسمائها أو `git diff`.
- نسخ JDK/Gradle/AGP/Kotlin والأدوات المؤثرة التي يثبتها التنفيذ، لا القيم المفترضة.
- build variant وflavors/flags والخصائص غير السرية التي تحدد مسار البناء.
- بصمة artifact الناتج وربطها بتقرير build/test/runtime، إن سمح اعتماد الدفعة بإنتاجه.

يستبعد manifest الأسرار والمفاتيح والشهادات الخاصة وقيم properties الحساسة؛ يسجل وجود المتطلب أو بصمة عامة لازمة دون نسخ المحتوى. **لا ينشأ هذا manifest في الجولة التوثيقية الحالية.**

---

## 4. Existing Architecture to Preserve

### 4.1 Invariants

1. الحفاظ على الوحدات الثلاث واتجاه الاعتماد الحالي: `app → feature → designsystem`، مع بقاء `designsystem` خاليًا من feature/domain.
2. الحفاظ على Compose + ViewModels + StateFlow، واستخدام `collectAsStateWithLifecycle` عند حدود Runtime.
3. الحفاظ على Koin وملكية registrations الحالية؛ لا service locator جديد ولا DI framework بديل.
4. الحفاظ على use cases وrepository contracts وRoom/DataStore وlocal-first behavior.
5. الحفاظ على routes وأسماء الوجهات وBottom Navigation roots الثلاثة.
6. الحفاظ على Media3 service، WorkManager، AlarmManager، receivers، ومصادر الصوت والتنزيل.
7. الحفاظ على أعمال Home/Ehsan/Profile/forms/Bottom Navigation الحديثة وصقلها، لا استبدالها بواجهات legacy.
8. لا ScreenV2، لا parallel design system، لا schema change لأسباب رسم.

### 4.2 State boundaries for approved implementation

| دورة الحياة المطلوبة | الأداة/المالك الصحيح | الضمان والقيود | أمثلة |
|---|---|---|---|
| حالة محلية ما دام موضع Composition موجودًا | `remember` | تبقى عبر recomposition فقط؛ تفقد عند خروج الموضع من Composition، ولا تعد عقد استعادة بعد إعادة الإنشاء | sheet visibility، expanded menu، animation state |
| حالة عرض/عمل عبر تغيّر التهيئة ضمن نطاق الشاشة | `ViewModel` | يبقى عادة عبر configuration change ضمن نطاقه؛ ليس تخزينًا دائمًا ولا ينجو مستقلًا من process death | UiState مشتقة، طلب جارٍ، تنسيق أحداث الشاشة |
| حالة صغيرة قابلة لحفظ النظام واستعادته | `rememberSaveable` أو `SavedStateHandle` | للاستعادة عند إعادة الإنشاء التي يدعمها Android وضمن حدود saved state والحجم/النوع؛ ليست ضمانًا بعد force-stop أو إزالة المهمة، ولا مكانًا لملف أو payload كبير | query، tab، معرف/قيمة نصية صغيرة لمسودة معتمدة |
| بيانات يجب أن تبقى بعد إغلاق التطبيق/إعادة تشغيله أو الجهاز | Room / DataStore / app-owned files عبر repository | مصدر الحقيقة الدائم؛ يلزم عقد تنظيف وترحيل وملكية واضح | bookmarks، إعدادات الصلاة والتذكير، profile/listings، الصور والصوت المنزّل |
| Route argument | Navigation argument؛ و`SavedStateHandle` عند حاجة ViewModel | يعيد تعريف الوجهة/المعرف، ولا يحل محل persistence للكيان | surahId/ayahNumber، donationId، duaId |
| Side effects لمرة واحدة | event/effect boundary | لا تطلق مباشرة أثناء composition؛ لا تحول snackbar/navigation إلى persisted business state | snackbar، external Intent، permission launcher، navigation |

#### Draft policy decision — required before forms implementation

السياسة الموصى بها لـAdd Donation وRequest Help وEdit Profile هي **مسودة جلسة صغيرة وليست autosave دائمًا**:

- تحفظ الحقول النصية القصيرة والاختيارات اللازمة فقط عبر `rememberSaveable` أو `SavedStateHandle` للاستعادة المدعومة عند إعادة إنشاء الشاشة/configuration؛ لا تحفظ image bytes أو ملفات أو كائنات كبيرة في saved state.
- لا يُوعد باستعادة المسودة بعد force-stop أو إزالة المهمة اعتمادًا على saved state وحده. إضافة تخزين دائم لمسودات النماذج قرار persistence جديد خارج هذه الجولة ويحتاج موافقة مستقلة.
- Add Donation/Request Help: تمسح المسودة بعد إنشاء الصف المحلي بنجاح، أو بعد تأكيد المستخدم للإلغاء/التجاهل؛ لا تمسح عند فشل validation أو فشل الحفظ.
- Edit Profile: يبقى Room هو المصدر canonical للملف المحفوظ؛ التعديلات غير المحفوظة تتبع عقد مسودة الجلسة نفسه، وتمسح بعد حفظ ناجح أو تجاهل صريح، ولا تُكتب قيم غير صالحة أو جزئية إلى Room بصمت.
- يحتاج اعتماد الدفعة إلى اختيار واضح بين: السياسة الموصى بها، عدم استعادة المسودة، أو تصميم تخزين دائم منفصل. لا يُختار ولا يُنفذ تخزين دائم جديد ضمن تعديل الوثيقة هذا.

### 4.3 Explicitly out of scope without new approval

- إعادة تقسيم `:feature` إلى modules متعددة.
- تغيير Room entities/schema أو تحويل كل status strings إلى types دفعة واحدة.
- adaptive/tablet navigation architecture.
- backend/auth/payment/report moderation.
- توصيل IhsanPlus/demo/Prayer Tracker/Quran Goal/Repeat/Widget لمجرد وجود WIP.
- ترقية شاملة للإصدارات.

---

## 5. Current UI/UX Diagnosis

### 5.1 Cross-product diagnosis

1. **تعدد عائلات top bars:** بعض الشاشات تستخدم `TopAppBar`، وبعضها headers مخصصة، وبعضها يكرر اسم «إحسان» مع زر menu بلا فعل.
2. **تسلسل مرئي متنافس:** Home يجمع primary row + services carousel + Asma/charity + Qibla ثانية + daily card؛ Prayer يضع التشخيصات قبل قائمة المواقيت في اللقطة التاريخية.
3. **cards أكثر من الحاجة:** محتوى قراءة وقوائم بسيطة محاط بأسطح كثيرة بدل rows وwhitespace.
4. **Design tokens غير مكتملة:** `Typography.kt` يعرّف 5 أدوار فقط؛ colors جيدة البداية لكن لا تعرّف Info/focus/pressed API كامل؛ shapes/elevation/motion ليست نظامًا موحدًا.
5. **قيم متناثرة:** heights/radii/font sizes/paddings كثيرة داخل feature screens رغم وجود `IhsanDimens` و`Spacing`.
6. **Accessibility localization غير متسقة:** توجد content descriptions إنجليزية (`Back`, `Favorite`, `Share`, `Download Surah`…) في واجهة عربية.
7. **RTL مفروض عالميًا:** `MainScreen.kt:52` يفرض `LayoutDirection.Rtl` بصرف النظر عن locale؛ يلزم قرار منتج واضح بدل افتراض ضمني.
8. **حالات واجهة غير صادقة:** fake report success، download spinner مؤقت، reminders switches لا تستعيد الحقيقة، statistics error لا يملك retry فعليًا.
9. **محتوى ديني بلا typography مستقلة:** قارئ القرآن يستخدم `fontSize.sp` وlineHeight محسوبًا محليًا، ولا توجد font assets/قراءة typography منفصلة.
10. **واجهة حديثة لكنها غير مكتملة التعميم:** Home/Ehsan/Profile/Bottom Navigation تحمل لغة حديثة أحدث من بقية الشاشات؛ المطلوب بناء bridge لا revert.

### 5.2 Diagnostic inventory

| Screen | Current purpose | Current UI issues | Logic/state constraints | Redesign priority |
|---|---|---|---|---|
| Activity permission gate | منع الدخول دون fine location | full-screen block خارج navigation | يتعارض مع skip/manual/local content | P0 |
| Splash | تأخير ثم قرار onboarding/location/home | لون/زمن ثابت؛ لا adaptive content | لا يبدأ أصلًا عند رفض Activity gate | P2 بعد P0 |
| Onboarding | تعريف ثلاثي وحفظ الاكتمال | illustration placeholders ونصوص hardcoded | DataStore حقيقي | P2 |
| Location Permission route | طلب/تخطي | لا request-history/OS-aware model يكفي لتمييز rationale عن Settings؛ provider/location validity غير منفصلين | التخطي غير فعال عمليًا بسبب Activity gate | P0/P1 |
| Home | daily hub + routes | ازدحام واختصار Qibla مكرر؛ لا continue reading رغم state | state aggregation واسعة وWIP حديث | P2 |
| Prayer | مواقيت/إعداد/diagnostics | hierarchy معكوس وبعض CTAs مبهمة | toggle/action no-op؛ alarm states متعددة | P1/P2 |
| Quran List | browse/search/bookmarks/progress | tabs/actions كثيرة في top area | Room/assets؛ lastRead موجود | P2 |
| Quran Reader | قراءة وصوت وتنزيل/tafsir | controls تنافس النص؛ icon labels إنجليزية | Media3/local-first؛ WorkInfo غير مربوط | P1/P2 |
| Azkar | بحث/تصنيف/مفضلة/عداد | cards/chips كثيفة | Room counters؛ Retry no-op | P2 |
| Tasbih | عداد مركز | ملف كبير وتخطيط يحتاج 320dp/2x | Room counter + vibration/DataStore | P2 |
| Dua | browse/search/favorite/share | menu no-op؛ chips كثيفة | Room/local; share يعمل في card/detail | P1/P2 |
| Dua Detail | قراءة/مصدر/copy/share/favorite | أفعال مكررة وcards كثيرة | dua قد تكون null أثناء التحميل | P2 |
| Hadith | browse/search/category/details | card share no-op؛ details sheet يعمل | Room/local | P1/P2 |
| Asma | browse/favorites/details | يحتاج نمط قائمة/تفصيل متسق | asset + DataStore fav | P2 |
| Global Search | Quran/Dua/Zikr search | لا empty/loading sections واضحة موحدة | Zikr result navigation no-op | P1/P2 |
| Qibla | sensor compass/location | error نصي بسيط؛ hierarchy تحتاج capability state | GPS فقط رغم saved/manual source موجود في Prayer | P1/P2 |
| Daily Activities | checklist/progress/routes | progress + cards كثيفة | increment قد يتضاعف مع feature action | P1/P2 |
| Statistics | Azkar-only stats | العنوان عام؛ chart custom غير متكيف | Retry no-op؛ لا prayer stats | P1/P2 |
| Reminders | morning/evening Azkar | switch يظهر حالة مؤقتة | WorkManager + competing app startup schedule | P1/P2 |
| Haram Live | HLS/YouTube | loading overlay/fallback يحتاج وضوح | network only؛ lifecycle release موجود | P2 |
| Nabawi Live | HLS/YouTube | كما سبق | كما سبق | P2 |
| Ehsan root | local charity board | hero/actions/filter density؛ مع ذلك حديث ويُحفظ | Room local؛ activeProjects semantic خطأ | P1/P2 |
| Add Donation | local offer form | طول/keyboard/form hierarchy | raw URI؛ phone from local profile | P1/P2 |
| Request Help | local request form | duplicate form family مع فروق | raw URI؛ phone validation | P1/P2 |
| Ihsan Details | details/contact/share/report | fake report success؛ relative time ثابت | tel/WhatsApp external; no report backend | P1/P2 |
| Profile | local identity/settings/support | hero طويل قبل daily tasks؛ حديث ويُحفظ | local Room only | P2 |
| Donation History | user’s local listings | row click no-op | data filtered by donor name string | P1/P2 |
| Edit Profile | local edit | fixed default phone can mislead؛ form/IME | phone read-only؛ Room update | P1/P2 |
| IhsanPlus Daily (debug) | controlled read-only preview | خارج release system | debug flag only؛ لا تعميم | P3/Quarantine |
| Legacy Donation Detail | compatibility redirect | لا UI | contract يجب حفظه مؤقتًا | P3 |

---

## 6. Design Vision and Information Hierarchy

### 6.1 Product principles

- **Clean + Calm + Modern + Premium:** سطح هادئ، لون brand عميق للحظات المهمة، ودرجات surface بدل shadows كثيرة.
- **Arabic-first, locale-respectful:** محاذاة start/end، نص عربي واضح، mixed-direction مضبوط، دون فرض RTL خارج قرار locale النهائي.
- **One primary task per screen:** الإجراء الأول واضح، الثانوي tonal/outlined، والبقية في menu/sheet عند الحاجة.
- **Content before chrome:** القرآن/الدعاء/الحديث هو المركز؛ controls تتراجع بصريًا.
- **Truthful capability:** لا نجاح، download، offline، permission، أو verification غير مستند إلى state.
- **Preserve personality:** green/cream والهوية الحالية تبقيان، مع تقليل dashboard-like tiles.

### 6.2 Global hierarchy

```text
Root screens
  Context/identity → Primary daily task → Continue/Resume → Curated services → Secondary status

Detail screens
  Back + concise title → Primary content → Primary action → Metadata → Secondary actions

Reader screens
  Minimal reader bar → Sacred/long text → contextual verse actions → docked/compact media controls

Settings/forms
  Title + short scope → grouped fields/rows → inline validation → persistent reachable primary action
```

### 6.3 Liquid Glass policy

الموجود `Modifier.liquidGlass` (`LiquidGlass.kt:30-102`) لا يستخدم blur ثقيلًا، وهذا أساس مناسب. يسمح به فقط في:

- hero واحد في Home أو Ehsan عند بقاء contrast ≥ target.
- selected bottom navigation treatment إن كان السطح البديل المعتم مكافئًا.
- overlay صغير/mini player عند وجود محتوى خلفه.

لا يستخدم حول كل card أو خلف نص القرآن. fallback دائمًا `surfaceContainer` مع border خفيف، ويُعطّل sheen عند dark/high-contrast أو عند إثبات أثر أداء/قراءة سلبي.

### 6.4 Core textual wireframes

#### Home

```text
[Compact header: greeting/date] [reminders]
[Next prayer: name · time · countdown]  → details / Prayer
[Continue reading — only when persisted last-read exists]
[Primary row: Quran | Azkar | Prayer | More]
[Daily progress summary] → checklist
[Secondary: Asma + Ehsan local]
[All services as one calm list/grid; Qibla appears once]
[Bottom Navigation]
```

#### Prayer

```text
[Detail top bar: مواقيت الصلاة] [settings]
[Next prayer compact hero + countdown]
[Prayer times list; active/next identified by label + shape + color]
[Location row] [change/retry/manual when supported]
[Adhan & notification settings summary]
[Collapsible system diagnostics]
```

#### Quran List

```text
[Root/detail top bar: القرآن] [search] [go to]
[Continue reading — conditional]
[Search field when active]
[Segmented: السور | العلامات]
[Surah rows: number, Arabic name, verses, actual progress]
[Empty/error/loading truthfully]
```

#### Quran Reader

```text
[Reader bar: back · Surah title/progress · overflow]
[Scrollable ayahs — text is dominant]
  [ayah number] [Quran text, no ellipsis]
  [context actions on selection/overflow: play, bookmark, tafsir]
[Compact media control surface]
  [reader] [previous] [play/pause] [next] [seek/progress]
[Download state from WorkInfo: queued/running/%/failed/retry/downloaded]
```

#### Ehsan root

```text
[Root top bar: إحسان]
[Concise local-only disclosure; expandable details]
[Primary actions: أعرض فائضًا | أطلب مساعدة]
[Search]
[One filter row/sheet: type, city, category]
[Honest summary: available offers/requests/completed]
[Local listing rows]
[Bottom Navigation]
```

#### Add Donation / Request Help

```text
[Detail top bar + clear scope]
[Required fields first: title, details]
[Category selector]
[City selector]
[Optional image]
[Contact source: local profile phone + edit path]
[Local-only disclosure once]
[Inline errors]
[Sticky/reachable submit above IME]
```

#### Profile / Settings

```text
Profile: [compact identity + edit] → [local activity summary] → [history/reminders/settings] → [support/privacy/logout]
Settings: [appearance] → [reading] → [feedback] → [audio] → [about/data truth]
```

#### Tasbih

```text
[Reader/detail top bar]
[Selected zikr selector]
[Zikr text]
[Large semantic count button]
[count / target]
[Reset + vibration as secondary actions]
```

#### Global Search / Live

```text
Search: [search field] → grouped result sections → per-section empty/loading → destination click
Live: [title/back/open externally] → player → loading/error/fallback surface → no offline imitation
```

---

## 7. Design System Specification

### 7.1 Color roles

القيم التالية proposal قابل للتنفيذ داخل `:designsystem` وليست تعديلًا منفذًا. نستخدم Material `ColorScheme` أولًا، وsemantic extension فقط لما لا يغطيه Material. نسب التباين المكتوبة بجانب أزواج الألوان **حسابات مقترحة تحتاج إعادة تحقق بطريقة موثقة**؛ ليست PASS ولا دليلًا على تباين الواجهة المركبة فعليًا.

#### Light

| Role | Value | Mapping/usage |
|---|---:|---|
| background | `#F8FAF8` | `colorScheme.background` |
| surface | `#FFFFFF` | base content |
| surfaceContainerLowest | `#FFFFFF` | reader/form |
| surfaceContainerLow | `#F3F7F5` | grouped rows |
| surfaceContainer | `#EEF3F1` | tonal cards |
| surfaceContainerHigh | `#E7EEEB` | selected/support |
| surfaceContainerHighest | `#DDE7E3` | strong separation |
| primary / onPrimary | `#073028` / `#FFFFFF` | brand/action; 14.36:1 |
| primaryContainer / onPrimaryContainer | `#D2F1E5` / `#062820` | selected/tonal |
| secondary / onSecondary | `#3F6357` / `#FFFFFF` | secondary action; 6.70:1 |
| tertiary / onTertiary | `#795A2D` / `#FFFFFF` | warm emphasis; 6.34:1 |
| textPrimary | `#151A18` | on background; 16.78:1 |
| textSecondary | `#46514D` | on background; 7.87:1 |
| outline / divider | `#6F7975` / `#D7DEDB` | borders/separators |
| success / onSuccess | `#1B6B3A` / `#FFFFFF` | 6.54:1 + icon/text |
| warning / onWarning | `#8A4F00` / `#FFFFFF` | 6.56:1 + icon/text |
| error / onError | `#BA1A1A` / `#FFFFFF` | 6.46:1 |
| info / onInfo | `#005DB7` / `#FFFFFF` | 6.46:1 |

#### Dark

| Role | Value | Mapping/usage |
|---|---:|---|
| background | `#0E1110` | canvas |
| surface | `#141816` | reader/content |
| surfaceContainerLowest | `#0B0D0C` | deepest |
| surfaceContainerLow | `#171B19` | grouped rows |
| surfaceContainer | `#1B201E` | cards |
| surfaceContainerHigh | `#222825` | raised |
| surfaceContainerHighest | `#2C3430` | strongest tonal |
| primary / onPrimary | `#8ED9BD` / `#00382C` | 8.00:1 |
| primaryContainer / onPrimaryContainer | `#15513F` / `#AAF5D8` | selected |
| secondary / onSecondary | `#B7CDC4` / `#23362F` | secondary |
| tertiary / onTertiary | `#EBC177` / `#432C00` | warm accent |
| textPrimary | `#E5E9E7` | on background; 15.49:1 |
| textSecondary | `#BDC7C2` | on background; 10.95:1 |
| outline / divider | `#89938F` / `#3D4743` | borders/separators |
| success / onSuccess | `#7BDD9A` / `#003918` | 7.91:1 |
| warning / onWarning | `#FFB95C` / `#482900` | 7.75:1 |
| error / onError | `#FFB4AB` / `#690005` | 7.72:1 |
| info / onInfo | `#A6C8FF` / `#00315E` | 7.70:1 |

`disabled` لا يدعي WCAG للنص المعطل: container = onSurface 12%، content = onSurface 38%، مع semantics `disabled`. Selected/pressed/focused يجب ألا تعتمد على اللون وحده:

- selected: tonal container + icon variant + `selected=true`.
- pressed: state layer 12% لمدة Fast.
- focused: 2dp primary ring + focus semantics.
- disabled: opacity + disabled semantics + no click.
- error/warning/success/info: icon/label مع اللون.

Dynamic color يبقى `false` افتراضيًا (`Theme.kt:106-118`) لحماية الهوية والتباين. تفعيله يحتاج قرار منتج واختبار contrast لكل role.

التحقق اللاحق يقيس اللون النهائي بعد alpha وstate layers وLiquid Glass والخلفية الحقيقية، في Light وDark، وللحالات selected/pressed/focused/disabled. لا تستنتج المطابقة من token منفرد أو Preview، ولا تسجل PASS دون أداة/طريقة حساب موثقة ولقطة أو اختبار مرتبط بالسطح الفعلي.

### 7.2 Arabic typography

لا توجد font assets حاليًا؛ `Typography.kt:9-45` يستخدم `FontFamily.Default`. الدفعة الأولى توسع الأدوار داخل النظام الحالي دون إدخال خط أو تغيير رسم القرآن. proposal:

| Role | Size/line height/weight | الاستخدام |
|---|---|---|
| displaySmall | 32sp / 42sp / 700 | رقم tasbih أو لحظة hero فقط |
| headlineLarge | 28 / 38 / 700 | عنوان root نادر |
| headlineMedium | 24 / 34 / 700 | عنوان شاشة/قسم أول |
| headlineSmall | 22 / 32 / 600 | detail title |
| titleLarge | 20 / 30 / 600 | card/section title |
| titleMedium | 18 / 28 / 600 | row title |
| titleSmall | 16 / 24 / 600 | secondary row title |
| bodyLarge | 18 / 30 / 400 | قراءة Dua/Hadith/Azkar القصيرة |
| bodyMedium | 16 / 26 / 400 | واجهة ونصوص الشرح |
| bodySmall | 14 / 22 / 400 | metadata |
| labelLarge | 14 / 22 / 600 | buttons |
| labelMedium | 12 / 18 / 600 | chips/navigation |
| labelSmall | 11 / 16 / 500 | supporting labels بحذر |

**Reading typography منفصلة داخل `:designsystem` نفسه، لا نظام موازٍ:**

- `QuranVerse`: default 30sp، user range 24–40sp، line height 1.75–1.9×، weight Normal، لا letter spacing ولا ellipsis.
- `LongArabicReading`: 20sp/36sp للأذكار/الدعاء/الحديث.
- `ReadingMetadata`: 14sp/24sp للمصدر/رقم الآية.
- اختبار 2.0x يجب أن يعتمد reflow/scroll لا خفض الحجم.
- تحقق بصري من الفتحة/الضمة/الشدة/الألف الخنجرية وعدم قص أعلى/أسفل السطر.

أي اقتراح خط مضمّن مثل Noto Sans Arabic/Noto Naskh يحتاج موافقة منفصلة، مراجعة OFL/coverage/ملف الحجم، وgolden content QA. لا يُبدل رسم القرآن أو نص assets في دفعة التصميم.

### 7.3 Tokens

| العائلة | القيم المعتمدة المقترحة | القاعدة |
|---|---|---|
| Spacing | 0, 4, 8, 12, 16, 24, 32, 48dp | إضافة 12/48 إلى الموجود؛ لا inline إلا لسبب موثق |
| Radius | 8, 12, 16, 24, 32dp, Full | 8 chips،12 fields،16 cards،24 hero/nav،32 sheets |
| Elevation | 0, 1, 3, 6dp | tonal separation أولًا؛ 6 overlays فقط |
| Borders | subtle 1dp، strong 1dp، focus 2dp | semantic colors؛ لا alpha متناثر |
| Icon visual sizes | 16, 20, 24, 32dp | touch target مستقل ≥48dp |
| Control heights | 48 compact،56 default،64 large CTA فقط | يسمح بالـwrap عند 2x بدل القص |
| Content width | screen gutter 16 compact/24 expanded؛ form max 600dp؛ reading max 720dp | center عند العرض الأكبر، دون tablet nav |
| Motion | 0, 100, 200, 300ms | instant/fast/standard/emphasis؛ لا infinite غير وظيفي |

### 7.4 Shared component families

الجدول التالي **خريطة مستهدفة** للتعميم، وليس backlog يجب تنفيذه كاملًا قبل العينة. مؤسسة Design System المبكرة تقتصر على tokens الأساسية والمكونات التي تحتاجها Prayer وQuran Reader وSettings فعلًا. بعد العينة تستخرج abstractions المشتركة فقط عندما يظهر تكرار مثبت؛ لا ينشأ wrapper لأي مكوّن Material لمجرد توحيد الاسم، بل فقط عندما يضيف سياسة مرئية، semantics، state contract، أو API تقلل خطأ متكررًا.

| Component family | Responsibility / variants | API & state boundary | Semantics + Light/Dark | Required tests |
|---|---|---|---|---|
| Top bars | Root / Detail / Reader فقط | stateless: title, nav, actions, scroll behavior | localized action names؛ reader chrome منخفض | RTL order، 320dp،2x،dark،scroll |
| Bottom Navigation | الجذور الثلاثة فقط | يبقى stateless index/callback | selected role + label؛ no route fallback | existing motion tests + back stack/runtime |
| Buttons | Filled/Tonal/Outlined/Text/Destructive/Icon | `enabled`, loading, label, optional icon؛ لا business logic | ≥48dp؛ loading label remains announced | states/contrast/2x/double-click guard |
| Cards/Rows | Hero/Tonal/ListRow/Reading surface | data + callback؛ no repository | row clickable semantics once؛ no nested duplicate | TalkBack count،dark،font scaling |
| Chips/Tabs/Segmented | filter/single select/multi where valid | selected values hoisted | role/selected; horizontally scrollable only if discoverable | RTL order،focus،2x |
| Search field | inline/active full search | query + onQueryChange + clear + submit optional | search role،clear CD،IME | empty/query/results/RTL mixed text |
| Fields/Selectors | single/multiline/dropdown/image/contact | value/error/enabled callbacks | label + error association؛ no placeholder as label | IME, 320dp,2x,restore draft |
| Settings row | navigation/toggle/value selector | value/callback hoisted | switch state spoken once | persistence/relaunch/disabled |
| Form scaffold | scroll + IME + action area | stateless fields/callbacks؛ المالك يختار `remember` أو VM للتنسيق وsaved-state أو persistence حسب دورة الحياة في §4.2 | focus order/error summary | keyboard/config/system-recreation؛ force-stop فقط لعقد دائم |
| Dialog/Sheet | confirm/select/details/action menu | visibility local؛ result callback | dismiss semantics; focus trap | back/outside/dark/2x |
| Loading/Empty/Error/Offline/Permission | truthful feature states only | sealed/presentational model؛ retry nullable only when real | heading-first; action name | each applicable state, no generic misuse |
| Snackbar | transient effect feedback | event channel + host | localized, timeout/action | rotation/no duplicate events |
| Media controls | compact/full reader control | player state hoisted | play/pause/current/seek state | background, LTR media order, TalkBack |
| Glass surface | Hero/Selected/Overlay only | style + opaque fallback | contrast tested in both themes | screenshot/perf/reduced motion |

Existing components to evolve rather than replace: `IhsanBottomNavigation`, `IhsanButton`, `IhsanSearchBar`, `IhsanLoadingState`, `IhsanEmptyState`, `IhsanErrorState`, `IhsanCapabilityState`, `EhsanForm`, `LiquidGlass`, `DashboardHeader`, `DailyActivityCard`.

**نطاق foundation المصغر للعينة:** semantic color roles الضرورية، typography/spacing/shape/motion الأساسية، Detail/Reader top bars بالقدر المستخدم، Button/IconButton، List/Settings row، Search عند الحاجة، وحالات Loading/Empty/Error/Capability المنطبقة. Bottom Navigation، form scaffold، عائلات المحتوى الكاملة، وعموم wrappers تتوسع لاحقًا عندما يثبت استخدامها؛ المكونات الحديثة الحالية تصقل في مكانها ولا يبنى بجانبها نظام بصري موازٍ.

---

## 8. Library and Icon Strategy

### 8.1 Dependency decision table

| Need | Existing solution | Proposed option | Benefit | Compatibility | Size/performance cost | License | Decision |
|---|---|---|---|---|---|---|---|
| Material components | Compose BOM + Material 3 | Use current versions | no upgrade risk | already compiling historically; new build not run | none | AndroidX Apache-2.0 | **Keep** |
| Version alignment | BOM `2024.12.01` | Keep BOM; no individual Compose versions | consistent artifacts | verified catalog | none | Apache-2.0 | **Keep** |
| Adaptive layout | Foundation + width constraints | Use BoxWithConstraints/Window size only if needed | 320dp→larger resilience | no new dep | minimal | Apache-2.0 | **Do not add adaptive library now** |
| Images | Coil 2.6.0 | Reuse Coil | existing AsyncImage and caching | already present | no added size | Apache-2.0 | **Keep; no second loader** |
| Animation | Compose animation APIs | tween/animateContentSize/AnimatedVisibility sparingly | enough for stated motion | current BOM | minimal | Apache-2.0 | **No extra library** |
| UI tests | Compose UI test JUnit4 | extend semantics/layout tests | already configured | current BOM | test-only | Apache-2.0 | **Use** |
| Screenshot regression | historical manual PNGs only | first create controlled ADB baseline; optionally evaluate Roborazzi/Paparazzi later | deterministic visual diffs | must spike against AGP 8.7/Kotlin 2.1/BOM | test dependency + storage | license must be reviewed at approval | **Approval required; not baseline dependency** |
| Icons | `material-icons-extended` in all modules | keep short-term; audit actual set; later local vectors for stable required icons | avoids visual churn; possible size reduction later | already used widely | existing package is broad | Material Icons Apache-2.0 | **No new icon pack; measured migration only** |
| Glass | local `LiquidGlass.kt` | refine existing opaque/tonal recipes | identity without blur | Compose-only | negligible | project code | **Reuse** |
| Quran/audio/download | Media3 + WorkManager | preserve; wire existing scheduler/WorkInfo only after approval | truthful state | existing | no new runtime dep | AndroidX Apache-2.0 | **Keep** |

`designsystem` يحمل حاليًا dependency `adhan` بلا import مثبت في source. إزالته ليست مطلوبة للتصميم؛ تسجل كـP3 dependency hygiene منفصل بعد build/dependency analysis وبموافقة.

### 8.2 Icon rules

- عائلة واحدة بصريًا: current Material family، outlined عادي وfilled محدد عندما يتوفر الزوج.
- لا إضافة Material Symbols font/package كامل في هذه الخطة؛ إذا احتاجت رموزًا غير موجودة، تضاف VectorDrawable محلية للأيقونات المحددة فقط.
- visual size 20/24 غالبًا، touch target 48.
- auto-mirroring فقط Back/Forward/List directional ونحوها؛ لا mirroring لـPlay، compass، download، location، phone.
- interactive icons تملك `stringResource` عربيًا؛ decorative icons `contentDescription=null` وتدمج semantics مع النص.
- إزالة English content descriptions المثبتة في Azkar/Dua/Hadith/Quran/Prayer/Qibla/Live/Reminders/Global Search.

---

## 9. Screen-by-Screen Redesign Plan

> الصيغة المختصرة لكل شاشة تغطي البنود المطلوبة 1–15. «الربط» يعني route/runtime wiring المثبت في الكود، لا مشاهدة الجهاز.

### 9.1 Splash

1. **Current Function:** تأخير 1.5s ثم قراءة onboarding/location. 2. **Wiring:** `AppNavHost:72-90`، لكن محجوب ببوابة Activity عند غياب الإذن. 3. **Logic:** DataStore `hasCompletedOnboarding` + permission check. 4. **Problems:** gate conflict، delay ثابت، duplicate legacy splash غير مستخدم. 5–7. **Layout/Hierarchy/Actions:** logo/brand هادئ، status غير معلن كزر؛ لا CTA إلا عند فشل initialization الحقيقي. 8. **Reuse:** Theme/surface فقط. 9. **States:** initializing؛ error فقط إذا ظهر مصدر حقيقي. 10–12. **RTL/Dark/A11y:** شعار غير اتجاهي، contrast، announcement واحد، reduced motion. 13. **Files:** `feature/.../splashScreen/SplashScreen.kt`; route logic only with explicit approval. 14. **Dependencies:** UserPreferences + permission policy. 15. **Acceptance:** لا flash/blank، لا double-navigation، و≤300ms animation when reduced motion.

### 9.2 Onboarding

1. تعريف المنتج. 2. route `92-100`. 3. local pager + DataStore completion. 4. placeholder illustration، hardcoded copy، وقد يأتي بعد permission بسبب gate. 5–7. ثلاث صفحات ذات هدف واضح؛ Next/Start primary وSkip فقط إن اعتمد؛ لا permission request هنا. 8. shared button/page indicator. 9. content only. 10–12. RTL pager semantics، dark، 320dp/2x scroll. 13. `OnboardingScreen.kt` + strings. 14. onboarding persistence. 15. زر البدء غير مقصوص عند 320dp/2x وتسجيل الاكتمال مرة واحدة.

### 9.3 Location Permission

1. capability contextual للصلاة/القبلة. 2. يوجد سطحان: private Activity `MainActivity:99-125` وroute `LocationPermissionScreen.kt:21-84`. 3. system permission + rationale؛ route يملك skip. 4. ازدواج وتعارض؛ لا distinction كافٍ بين permission/provider/valid location؛ Activity يطلب Fine فقط. 5–7. لا full-app gate؛ capability intro ثم Continue without location وEnable location كإجراء اختياري، مع Settings فقط عندما يبرره سجل طلب الإذن وحالة الإصدار. 8. `IhsanCapabilityState`. 9. precise/approximate-only، provider off، revoked-on-resume، valid/stale saved، manual، وno-location حالات مستقلة بحسب دعم Android والميزة. 10–12. localized semantics، scroll 2x. 13. MainActivity/Nav/Splash/permission screen — **تغيير gate يحتاج موافقة**. 14. PrayerLocationRepository/manual city. 15. رفض الموقع لا يمنع Quran/Azkar/Ehsan local؛ Prayer/Qibla تشرحان الحاجة الفعلية ولا تفترضان Fine لكل وظيفة.

**مصفوفة قرار الموقع المطلوبة قبل التنفيذ:**

| Permission | Provider | Available location | Feature requirement | Expected product behavior |
|---|---|---|---|---|
| precise granted | on | fresh device fix | Prayer/Qibla | يستخدم المصدر المعتمد ويعرض source/age عند الحاجة؛ لا طلب جديد |
| approximate-only حيث يدعمه الإصدار | on | fresh approximate fix | Prayer | يقبل للحساب إن كانت دقة الحساب الفعلية كافية؛ يعرض تحسين الدقة كخيار لا gate |
| approximate-only | on | fresh approximate fix | Qibla | يحسب إن كان العقد الحالي يسمح، مع شرح حدود الدقة؛ لا يطلب Fine إلا لحاجة مثبتة وموافق عليها |
| fine/coarse granted | off | saved valid أو manual | Prayer/Qibla | يميز provider off عن permission؛ يستخدم fallback المعتمد إن كان صالحًا ويتيح فتح إعداد الخدمة |
| fine/coarse granted | off | none | Prayer/Qibla | capability state خاص بالمزود، لا “permission denied”؛ المحتوى المحلي يبقى متاحًا |
| denied | أي | saved valid أو manual | Prayer/Qibla | يستخدم الموقع المتاح بحسب عقد الميزة، ويعرض تحديث الموقع اختياريًا |
| denied | أي | saved stale | Prayer/Qibla | يعرض عمر/تقادم المصدر وحدود الدقة وخيارات التحديث/الموقع اليدوي الموجود فعليًا؛ لا يقدمه كبيانات حديثة |
| denied | أي | none | Prayer/Qibla | يشرح غياب الموقع ويتيح المسار المدعوم؛ لا يحجب القرآن والأذكار وباقي المحتوى المحلي |
| revoked while app in use ثم resume | أي | أي | الميزات التي تستخدم الموقع | يعيد فحص permission/provider/location validity عند resume ويوقف الاعتماد على نتيجة قديمة دون loop أو crash |
| returning from system settings | changed | أي | Prayer/Qibla | يعيد reconciliation للحالات الثلاث منفصلة ثم يحدث UI؛ لا يفترض نجاح التغيير بمجرد الرجوع |

`shouldShowRequestPermissionRationale=false` وحدها **ليست دليلًا على الرفض الدائم**؛ قد تكون false قبل أول طلب. تحديد مسار rationale/settings يحتاج معرفة أن طلبًا سابقًا أطلق، ونتيجته، وحالة grant الحالية، وسلوك إصدار Android. الموقع اليدوي في الجدول يعني التدفق الموجود فعلًا في Prayer؛ لا يخترع manual-location flow جديد لسطح لا يدعمه الكود.

### 9.4 Home

1. hub يومي. 2. `AppNavHost:112-138`. 3. seven observe sections + actions/sheets. 4. Qibla مكررة (`HomeDashboardScreen:127` و`302`)، 15 service/primary entries، last-read callback/state غير معروض، quran/dhikr/profile تجلب دون presentation واضح، actual list يخالف `HomeDestinationCatalog`. 5–7. wireframe §6.4؛ الصلاة ثم continue reading conditional ثم 3–4 primary tasks؛ More للخدمات، لا hero مستهلك. 8. DashboardHeader/LastReadCard/DailyActivityCard مع refinement. 9. section-scoped loading/error؛ لا card لكل state. 10–12. RTL carousel/order، dark glass fallback، 2x rows instead of fixed tiles. 13. Home screen/components/resources، no use-case contract initially. 14. existing section states/routes. 15. Qibla مرة واحدة؛ كل tile route فعلي؛ continue reading يظهر فقط عند IDs صحيحة؛ الشاشة usable على 320dp/2x.

### 9.5 Prayer

1. المواقيت والتنبيهات والإعداد/التشخيص. 2. route `225-229`. 3. `PrayerUiState` من facade؛ settings sheets وreconcile. 4. menu no-op (`PrayerScreen:75`)، notification action no-op (`PrayerViewModel:108`)، CTA في active card no-op (`PrayerScreen:306`)، settings offsets no-op. 5–7. next prayer hero compact → times list → location/settings → collapsed diagnostics؛ settings icon فقط إذا يعمل. 8. DetailTopBar/CapabilityState/SettingsRows/sheets. 9. loading/available/location unavailable/permission denied/provider off/schedule failed. 10–12. start/end، media icons irrelevant، time strings LTR-isolated، TalkBack prayer+time+status، 320dp/2x. 13. Prayer screen/VM/state/sheets/resources؛ repository only for approved logic. 14. alarm permission ≠ POST_NOTIFICATIONS ≠ adhan setting. 15. لا toggle مضلل؛ active/next not color-only؛ retry reports result.

### 9.6 Quran List

1. browse/search/bookmarks/progress. 2. route `231-247`. 3. shared Quran VM per destination, Room/assets. 4. actions/tabs crowded؛ loading reuse basic؛ subtitle truncates bookmark text intentionally but verse search context needs review. 5–7. wireframe؛ continue first if real، search mode clear، segmented surahs/bookmarks. 8. search/segmented/list rows/state surfaces. 9. loading/content/empty search/empty bookmarks/error. 10–12. Arabic names with numeric isolation، dark reading surface، 2x rows wrap. 13. list screen/resources/design components. 14. search/lastRead/bookmarks. 15. clear search restores list؛ result lands exact ayah؛ no critical/religious text ellipsis.

### 9.7 Quran Reader

1. Quran reading + playback/download/bookmark/tafsir. 2. route args `249-289`. 3. Media3 handler, Room, files, last read; `QuranUiState`. 4. toolbar/media/action density؛ English CDs؛ 32dp icon modifiers need target verification؛ `isDownloading` cleared after 1.5s regardless work. 5–7. wireframe؛ text dominates؛ secondary controls in overflow/sheet; media dock compact. 8. ReaderTopBar/QuranVerse/MediaControls/DownloadState. 9. loading/error/content، queued/running/%/failed/retry/downloaded، offline local/remote unavailable. 10–12. Quran text RTL، seek/time/media sequence not mirrored automatically، gestures and TalkBack، font 24–40 plus system scale. 13. Reader/List state/VM/download scheduler/resources. 14. do not change Media3 service/local-first source/last read. 15. scroll saves visible ayah without loop؛ background audio continues per intended policy؛ download UI equals WorkInfo.

### 9.8 Azkar

1. browse/search/category/favorites/counters. 2. route `291-298`. 3. Room/use cases/DataStore. 4. chips/cards dense؛ English semantics؛ Retry action no-op. 5–7. compact search + category selector، reading rows/surfaces، increment primary per item، reset secondary guarded. 8. shared reading/search/filter/count components. 9. loading/content/empty/error with real retry or no retry. 10–12. Arabic diacritics/long text no ellipsis، dark، counter semantics. 13. screen/state/VM/resources; use case only if retry contract approved. 14. preserve counter persistence. 15. increment once per activation؛ completed state icon+label؛ 2x scroll.

### 9.9 Tasbih

1. focused counter. 2. route `166-170`, linked from Home/Azkar. 3. same Azkar counter source + vibration setting. 4. large 464-line UI، layout risk on short screens؛ reset/select access. 5–7. wireframe؛ full center tap target، selector and reset remain reachable. 8. reader bar/count button/segmented selector. 9. loading/error/empty/content. 10–12. count announced once، haptic not sole feedback، 320dp/2x, landscape/short height scroll. 13. Tasbih screen/VM/resources. 14. no new counter store. 15. reset confirmation when count>0؛ selected zikr and count survive navigation per existing Room.

### 9.10 Dua

1. list/search/category/favorites/share/copy. 2. route `308-317`. 3. Room local. 4. menu no-op، duplicate tab+favorite filtering state، dense chips/cards، English CDs. 5–7. consistent content browser; working search/filter; remove menu or assign real actions. 8. shared content list/reading row/action sheet. 9. loading/empty/error/content. 10–12. Arabic text integrity، dark، 2x. 13. screens/VM/resources. 14. favorite/share contracts. 15. كل visible icon يعمل؛ copy/share feedback واضح وفشل intent handled.

### 9.11 Dua Detail

1. long reading and metadata. 2. route args `319-341`. 3. item looked up from VM list and open tracked. 4. favorite duplicated top/bottom؛ share/copy blocks compete. 5–7. reading first، metadata secondary، primary favorite once، share/copy action row/sheet. 8. ReaderTopBar/LongReading/MetadataRow. 9. loading/not-found/error/content؛ حاليًا null قد يبدو فراغًا ويجب تمثيله. 10–12. no ellipsis، selection/copy semantics، dark. 13. detail screen/resources. 14. item availability/favorite. 15. deep link not-found gives recoverable state؛ actions remain reachable 2x.

### 9.12 Hadith

1. list/search/categories/daily/details. 2. route `300-306`. 3. Room/use case. 4. card share no-op (`HadithScreen:261`) بينما sheet share يعمل؛ card density/English CDs. 5–7. list preview opens detail؛ share only in action sheet or implement same helper. 8. same content browser family, not same reading behavior as counters. 9. loading/empty/error/content. 10–12. source/text order، no religious truncation in detail، TalkBack. 13. screen/details/resources. 14. share intent/favorite. 15. لا share affordance صامت؛ copied/shared feedback and chooser failure handled.

### 9.13 Asma

1. browse/favorites/details/daily name. 2. route `219-223`. 3. asset repository + DataStore favorites. 4. bespoke cards/sheets not aligned with content family. 5–7. calm list/grid dependent on width، search not invented، favorite toggle and details. 8. content row/filter/detail sheet. 9. loading/empty favorite/error/content. 10–12. names/diacritics uncut، dark، 2x. 13. screen/state/resources. 14. favorite IDs and AsmaTodayResolver. 15. selected details survive recomposition; no decorative religious glyphs.

### 9.14 Global Search

1. combined Quran/Dua/Zikr search. 2. route `172-183`. 3. three VMs searched on each query. 4. Zikr click calls empty callback in nav؛ no debounce evidence؛ grouped states weak. 5–7. one search field، recent/empty guidance، grouped results with “view all” only if supported. 8. SearchField/SearchResultRow/section states. 9. idle/searching/no results/partial error/content. 10–12. mixed numbers Arabic/LTR، TalkBack section headings، keyboard. 13. GlobalSearch/AppNavHost/resources. 14. exact Zikr destination decision. 15. every result navigates to visible destination/focus؛ no result click no-op.

### 9.15 Qibla

1. compass from sensor/location. 2. route `215-217`. 3. QiblaViewModel directly uses fused location. 4. full-screen text error، no saved/manual fallback despite Prayer repository support، global location gate hides behavior. 5–7. compass primary; alignment state with text/haptic; capability card and retry/change location secondary. 8. QiblaCompass/CapabilityState. 9. permission/provider/loading/calibration/error/ready. 10–12. compass never mirrored، degrees LTR، haptic not sole signal، dark contrast. 13. Qibla screen/VM; repository injection is contract/ownership approval. 14. reuse PrayerLocationRepository preferred, no duplicate storage. 15. denied permission can still use valid saved/manual location if approved; sensor absence has honest state.

### 9.16 Daily Activities

1. checklist/progress and route launch. 2. route `185-213`, shared Home VM. 3. DataStore daily counts. 4. action “increase” plus route open may double-count against feature-specific increments; unknown routes silently ignored. 5–7. summary compact، rows with one meaning: open task أو mark manually، not both ambiguously. 8. DailyActivityCard/ListRow. 9. content/empty/error. 10–12. progress label not color-only، 2x. 13. screen/Home action/repository mapping. 14. counting ownership decision. 15. one user accomplishment produces at most one increment; date rollover deterministic.

### 9.17 Statistics

1. Azkar last-7-days + current counters. 2. route `353-357`. 3. `StatisticsRepositoryImpl` consumes Azkar only. 4. generic title can imply all worship؛ Retry UI null/no-op؛ custom canvas fixed bar sizes. 5–7. title “إحصاءات الأذكار” unless scope expands؛ weekly summary then details. 8. stat summary/chart/list states. 9. loading/empty/error/content with actual retry policy. 10–12. chart labels and content descriptions، dark، 320dp/2x. 13. screen/state/VM/resources. 14. no fake prayer stats. 15. all displayed totals derive from same snapshot; chart accessible text alternative.

### 9.18 Reminders

1. morning/evening Azkar reminders. 2. route `359-361` + Home/Profile callbacks. 3. local `remember` state + WorkManager. 4. switches reset false on recreation، Work remains; Application independently schedules `azkar_MORNING/EVENING`; cancel identity may not describe all workers. 5–7. grouped rows مبنية على ثلاثة عقود منفصلة: الإعداد المحفوظ (`enabled/time`)، حالة التسجيل/القبول (`workId/uniqueName/state`)، وحالة آخر تنفيذ معروفة دون الادعاء بالتسليم. 8. SettingsRow/TimePicker/CapabilityState. 9. config-loading/disabled/enqueueing/scheduled-or-registered/scheduling-error/permission state؛ لا تسمى “تم التذكير” من الإعداد وحده. 10–12. time LTR، switch semantics، 2x. 13. screen/worker/Application/settings repository — logic approval. 14. unique-work identity، migration، persistence، وسياسة الدقة. 15. restart reflects persisted config and owned registration؛ disable cancels all known owned identities؛ لا ازدواجية داخل التسجيل/التنفيذ الذي يملكه التطبيق، دون ضمان exactly-once delivery من النظام.

### 9.19 Haram Live

1. HLS with YouTube fallback. 2. route `148-155`. 3. local playback state + ExoPlayer/WebView. 4. needs consistent loading/error and external fallback wording. 5–7. minimal player; open official source secondary. 8. DetailTopBar/MediaState. 9. connecting/playing/HLS fail→YouTube/error/offline. 10–12. media controls not mirrored، dark system bars، TalkBack. 13. LiveStreamScreen/resources. 14. preserve DisposableEffect release. 15. leaving route stops/releases intended playback; no “offline stream”.

### 9.20 Nabawi Live

نفس 1–15 في Haram Live مع title/source مستقلين (`AppNavHost:157-164`). acceptance يختبر fallback لكل source على حدة ولا يفترض نجاحهما معًا.

### 9.21 Ehsan root

1. local offers/requests board. 2. route `363-377` + bottom root. 3. EhsanVM filters Room؛ AuthViewModel local user. 4. hierarchy كبيرة؛ disclosure دائم طويل؛ filters كثيفة؛ `activeProjects=list.size`. 5–7. wireframe؛ disclosure مختصر expandable؛ actions واضحان؛ truthful counts. 8. modern Ehsan components preserved. 9. loading/empty/filter-empty/error/content. 10–12. RTL card content، phone not exposed in list semantics، dark glass fallback، 2x. 13. Ehsan screen/components/VM/resources. 14. local-only wording/status semantics. 15. no verification/payment implication؛ counts match definitions؛ filters reset discoverable.

### 9.22 Add Donation

1. create local OFFER (and legacy type parameter). 2. route `379-388`. 3. local draft + AddEhsanVM/use case/Room. 4. raw image URI، `remember` draft lost، long form/IME risks، success then pop. 5–7. shared form scaffold with offer-specific copy/category؛ submit primary. 8. existing `EhsanForm` refined، no duplicate screen. 9. idle/validation/submitting/success/error/image unavailable. 10–12. keyboard next actions، error association، 320dp/2x save visible via scroll. 13. screen/VM/image adapter/resources. 14. EhsanImageStore wiring وdraft lifecycle approval؛ no schema. 15. created listing image survives restart؛ small unsaved draft restores/clears only per §4.2، لا promise بعد force-stop؛ double submit prevented؛ no backend wording.

### 9.23 Request Help

1. create local REQUEST. 2. route `390-394`. 3. shares AddEhsanVM but distinct categories/copy. 4. same raw URI/draft/IME risks. 5–7. same family، preserve privacy-oriented text and differences. 8–12. same form scaffold/state/accessibility with request-specific labels. 13. screen/VM/resources/image store. 14. phone validation، local disclosure، وdraft lifecycle §4.2. 15. no “sent to organization” implication؛ created row is local and immediately visible؛ draft clear/restore matches approved lifecycle only.

### 9.24 Ihsan Details

1. show local listing + tel/WhatsApp/share/report. 2. canonical route `411-420`. 3. repository by ID. 4. report flips local boolean to thanks with no backend؛ “منذ يومين” ثابت؛ URI intents lack consistent failure feedback. 5–7. content/status/contact primary based on type؛ report removed/disabled/reframed by decision؛ metadata real. 8. DetailTopBar/Metadata/ExternalAction. 9. loading/not-found/error/content/intent unavailable. 10–12. phone LTR/isolate، sensitive semantics، dark، 2x. 13. screen/state/VM/resources. 14. createdAt must reach UI mapping; report product decision. 15. relative time derives from timestamp/locale؛ no fake success؛ tel/WhatsApp failure visible.

### 9.25 Profile

1. local profile hub. 2. bottom root `422-430`. 3. Room user + own donations. 4. hero tall، support contacts hardcoded in code، local account wording requires discipline. 5–7. compact identity/edit؛ local impact؛ history/settings/reminders; support last. 8. recent Profile components preserved. 9. loading/no-local-profile/content/dialogs. 10–12. phone LTR، logout/delete wording explicit، dark، 2x. 13. Profile/component/resources. 14. no cloud auth. 15. “ملف على هذا الجهاز” clear; each row route/intent works with failure feedback.

### 9.26 Donation History

1. user’s local listings. 2. route `432-436`; Profile row is wired. 3. ProfileVM list. 4. item click is no-op (`DonationHistoryScreen:66`). 5–7. status-filtered list; row opens canonical Ihsan Details; management actions only if existing contracts. 8. list rows/state surfaces. 9. loading/empty/content/error. 10–12. dates/phones directional، destructive semantics. 13. screen/AppNavHost callback/VM only as needed. 14. ownership by donor name is fragile but contract change deferred. 15. every row opens correct ID; no edit/delete invented.

### 9.27 Edit Profile

1. edit local name/city/address؛ phone read-only. 2. route `438-442`. 3. Room update. 4. initial phone `0930000000` before load/when absent، form state in VM but missing robust validation، snackbars. 5–7. clear local scope؛ fields and save; phone marked read-only/source. 8. FormScaffold/TextFields. 9. loading/content/validation/saving/success/error/no profile. 10–12. IME/scroll، phone LTR، 320dp/2x. 13. screen/state/VM/resources. 14. no phone update contract currently؛ unsaved draft follows §4.2. 15. placeholder never displayed as user data؛ save button reachable above IME؛ config/system-recreation and explicit discard follow approved policy؛ no force-stop recovery claim; name parsing behavior tested.

### 9.28 Settings

1. app appearance/reading/vibration/adhan sound/share. 2. route `343-351`; Activity also consumes same VM for dark. 3. DataStore flows. 4. flat list، font size label English CD، Retry no-op، adhan sound semantics mixed with prayer settings. 5–7. grouped wireframe; value summaries; system picker as explicit external action. 8. SettingsRow/Slider/CapabilityState. 9. loading/content/error/picker unavailable. 10–12. value announced، 2x labels wrap، dark live preview safe. 13. screen/state/VM/resources. 14. distinguish UI font vs religious reading setting; current single font flow needs product decision before split. 15. relaunch restores values؛ cancel sound picker changes nothing؛ failure feedback.

### 9.29 IhsanPlus Daily / legacy redirect / unconnected WIP

1. debug-only controlled daily surface؛ redirect compatibility؛ WIP components. 2. conditional route/redirect proven. 3. production adapters may read real data, but feature flags quarantine release. 4. risk of accidental exposure. 5–12. لا redesign عام ولا navigation promotion؛ إن احتاج debug consistency يستخدم tokens فقط لاحقًا. 13. flags/route untouched by redesign unless separately approved. 14. release flags are dependency. 15. release graph proves no reachable demo؛ legacy deep link redirects once without blank-loop.

---

## 10. Logic Completion Plan

### 10.1 Verified issue ledger

| Issue | Evidence | User impact | Root cause | Minimal correction | Contract impact | Tests | Priority |
|---|---|---|---|---|---|---|---|
| Duplicate location gate | `MainActivity:51-66` vs Splash/Nav permission `AppNavHost:72-110` | app local content blocked; skip ineffective | two owners for startup permission | remove Activity-level blocking branch; keep contextual route/feature states | changes Activity gate policy; approval required | precise/approximate، provider off، revoke/resume، settings return، fresh/stale saved، manual، none؛ “permanent” only with request history | **P0** |
| Home last-read callback/state unused | callback `HomeDashboardScreen:100`; no invocation; state mapped `HomeDashboardUiState:91-93` | collected value gives no resume affordance | redesign removed card but contract remained | render one conditional continue card or remove collection/callback after product decision | no domain change if rendered | VM state + nav exact ayah | P1/P2 |
| Home destination duplication/drift | Qibla at `127` and `302`; actual services include reminders/direct live while catalog omits them | clutter; tests/spec drift | UI list and catalog have two sources | choose one catalog and one visual location per route | presentation-only | catalog uniqueness + UI semantics | P2 |
| Prayer visible no-ops | menu `PrayerScreen:75`; active CTA `306`; `OnToggleNotification` empty `PrayerViewModel:108` | misleading controls | presentation ahead of contract | remove/hide actions or wire to existing real settings/schedule result | notification ownership decision | action→state/reconcile tests + runtime permissions | P1 |
| Prayer offsets no-op | `PrayerSettingsRepositoryImpl:43-45` | future exposed offsets would not persist | SettingsManager lacks offsets | keep offsets unexposed; implement only via approved storage contract | repository/storage change if exposed | persistence/calculation | P3 unless visible |
| Search Zikr navigation no-op | `AppNavHost:181` | result tap does nothing | no exact zikr destination contract | navigate to Azkar with supported filter/ID only after contract; otherwise remove Zikr results | may need route/state argument | click result → focused item | P1 |
| Dua menu no-op | `DuaScreen:143` | dead affordance | placeholder action | remove menu or move real filter/share actions into it | none | node absent/works | P1 |
| Hadith card share no-op | `HadithScreen:261` vs working sheet share `HadithDetailsBottomSheet:112-120` | dead affordance | duplicate action implementation | remove card share or reuse one share helper | none | chooser/failure + semantics | P1 |
| Donation History row no-op | `DonationHistoryScreen:66`; root navigation itself is wired `AppNavHost:426,432-435` | cannot open history item | missing item callback | add `onItemClick(id)` to screen and navigate canonical details | presentation/navigation signature only | row ID route/back stack | P1 |
| Fake report success | `IhsanDetailsScreen:54,61-86` | implies moderation/backend that does not exist | local boolean masquerades as server success | remove report; or replace with honest local hide/block only if real contract approved | product decision; backend not invented | no success without data effect | P1 |
| Hardcoded relative time | `IhsanDetailsScreen:244` “منذ يومين” | incorrect trust signal | createdAt omitted from UI model | map `createdAt` and format relative time locally | presentation model addition, domain unchanged | time boundaries/locale | P1 |
| Phone placeholder as data | `EditProfileUiState:5` default `0930000000` | false identity/contact | sample default in runtime state | default empty; show loading/no-profile; validate before intents | none | no-user/load/edit tests | P1 |
| Ehsan image persistence gap | forms pass `uri.toString()`; `EhsanImageStore` exists but only used by delete WIP | image may break after restart/grant revoke | GetContent URI stored directly; store not wired | persist to app-owned file before Room insert; resolve at UI boundary; retain no-image fallback | DI/use-case input ownership approval; no schema needed | instrumented restart/revoke/orphan cleanup | P1 |
| Quran download false state | `QuranViewModel:268-287` enqueues then delays 1.5s; separate WorkInfo scheduler exists unreferenced | spinner lies; no retry/progress | duplicate scheduling path | inject/use existing `QuranDownloadScheduler.observe(tag)` and map WorkInfo | Koin/VM constructor change; approval | mapping/interruption/relaunch/offline | P1 |
| Reminder persistence/scheduling conflict | local `remember` `RemindersScreen:75-77`; Application always schedules `BarakahApp:44-48`; unique work paths differ | switch lies, duplicates possible, disable incomplete؛ delivery may be overstated | two scheduler owners + no persisted config + identities without migration contract | one persisted config source، one reconciliation path، explicit work identity migration، and separate registered/executed UI truth | DataStore/repository/worker ownership + approximate/exact product policy approval | restart/reboot/timezone/time change/late-missed policy؛ owned registration/execution no-duplicate | P1 |
| Statistics scope/retry mismatch | repository is Azkar-only `StatisticsRepositoryImpl:9-21`; retry null `StatisticsScreen:64-73` | title overclaims; error unrecoverable | generic copy + flow retry placeholder | rename scope; remove retry affordance or define refresh | no domain change | copy/source invariant/error state | P1/P2 |
| Daily activity double-count risk | detail screen offers increase and route open; feature VMs also increment preferences | inflated progress | multiple event owners | define per-activity ownership; navigation never increments unless explicitly manual | state ownership decision | one action → one delta; date rollover | P1 |
| `activeProjects` semantics | `EhsanViewModel:39-47` sets `activeProjects=list.size` including completed | misleading summary | name/count mismatch | count only AVAILABLE/PENDING or rename to total cases based product copy | none | status fixture matrix | P1 |
| Qibla ignores saved/manual location | QiblaVM directly uses fused provider; Prayer repository exposes saved/manual | feature unusable offline/denied despite valid location | duplicated location ownership | consume canonical PrayerLocationRepository after approval | VM dependency change, no schema | saved/manual/device/denied tests | P1 |
| Retry actions that cannot retry | Azkar Retry comment no-op; Settings Retry no-op; Statistics flow-only | false affordance | generic state API applied indiscriminately | nullable retry only when real; otherwise passive error/reload-on-source | none | semantics/action absence | P1/P2 |
| Runtime legacy/WIP ambiguity | unconnected `QuranScreen` placeholder, second Splash, widget/tracker/repeat/goal | accidental wiring/revert risk | accumulated WIP | inventory + quarantine; do not delete/wire | none until separate approval | release route/graph characterization | P3 |

### 10.2 CTA truth contract

كل CTA في التنفيذ المعتمد يجب أن يمر بهذا العقد: event معروف، enable condition، feedback، وoffline/permission behavior. الجرد الحالي/المطلوب:

| Surface / CTA group | Actual event now | Enable condition | Success / failure feedback | Offline / permission rule |
|---|---|---|---|---|
| Bottom roots | `navController.navigate` save/restore | route known | selection/back stack visual | always local |
| Home service tiles | callbacks→routes | callback/feature enabled | destination visible؛ unknown currently toast | hide disabled debug routes؛ network icon not “available offline” |
| Home next prayer | select index→sheet | prayer item exists | sheet / no-op forbidden | saved/manual data allowed; capability otherwise |
| Home continue Quran | callback exists but unused | valid surah+ayah | exact reader destination | local reading always; audio policy separate |
| Prayer refresh | facade refresh | not already loading | updated location or reason-specific error | permission/provider explained |
| Prayer notification controls | currently no-op | only notifiable prayer + permission model | reconciliation result | distinguish POST_NOTIFICATIONS/exact alarm/adhan mode |
| Quran search/go-to/bookmark | VM actions/routes | valid query/id | result/selection; error state | local DB works offline |
| Quran playback | Media3 action | playable local file or network source | playing/error | offline uses downloaded only; no silent remote attempt |
| Quran download | WorkManager | network policy/storage/work not active | queued/%/success/failure/retry from actual WorkInfo/files | no fake completion; reconnect behavior follows worker policy and evidence |
| Azkar/Tasbih count | use case | item exists/not complete as applicable | visible count+haptic optional/error | local |
| Copy | Clipboard | content exists | snackbar/announcement | local |
| Share | ACTION_SEND | content exists + handler available | chooser; failure snackbar | no network guarantee claimed |
| Favorite | repository toggle | item exists | selected semantics/state or error | local |
| Search result | route callback | destination contract exists | destination/focus | local except none currently |
| Ehsan add/request | AddDonationUseCase | valid fields + real phone + not submitting | local row created / explicit failure؛ draft cleared only per approved policy | local; image copied before success؛ saved-state restoration is not durable autosave |
| Call/WhatsApp | external intent | normalized non-placeholder phone + handler | launch / handler unavailable message | WhatsApp may require network; dialer does not imply call success |
| Report | fake local boolean now | **must not be enabled as report** | remove or truthful approved local effect | no backend/offline success |
| Profile rows/history | nav/external intents | route/handler exists | destination or failure message | local except support apps |
| Settings toggles | DataStore setters | loaded | flow reflects persisted value/error | local; system picker cancellation neutral |
| Reminder toggle/time | WorkManager currently | persisted config loaded؛ product precision policy known؛ one mutation in flight | setting saved، registration accepted/failed، and last execution shown separately؛ never infer delivery from enabled/time | notification permission surfaced؛ WorkManager means deferrable execution, not minute-exact delivery |
| Live retry/open YouTube | reset player/external URL | network/handler | playing or explicit error | offline state only، no cached claim |

### 10.3 Reminder timing, identity, and truth contract

`WorkManager` مناسب لعمل مؤجل يمكن تنفيذه لاحقًا عندما تسمح القيود، وليس ضمانًا للتنفيذ في دقيقة محددة. لذلك تفصل الواجهة والطبقات بين:

1. **Saved user configuration:** `enabled` ووقت التذكير والمنطقة/السياسة اللازمة، محفوظة في DataStore أو المصدر المعتمد.
2. **Scheduling registration state:** هل قبل التطبيق تسجيل العمل، وما `uniqueName/workId` والحالة المعروفة؛ النجاح هنا يعني enqueue/reconciliation فقط.
3. **Execution/delivery state:** آخر بدء/نجاح/فشل يعرفه worker إن كان مسجلًا؛ لا يدعي الإعداد أو enqueue أن notification سُلّمت أو ظهرت للمستخدم.

قبل التنفيذ يلزم قرار منتج: هل الوقت **تقريبي** وتقبل نافذة تأخير موضحة للمستخدم، أم **دقيق**؟ إذا طلبت الدقة، لا يستبدل WorkManager تلقائيًا؛ تفتح دراسة منفصلة لآلية Android المناسبة وإصدار النظام والأذونات وسياسات exact alarms/notifications والطاقة، ثم يعتمد العقد قبل تغيير scheduler.

يلزم كذلك اعتماد سياسات: مقدار التأخر المقبول؛ هل التذكير الفائت يرسل متأخرًا أو يتجاوز؛ ماذا يحدث عند تغيير الوقت أو timezone؛ وكيف تتم المصالحة بعد reboot/app update. القاعدة الموصى بها للتذكير اليومي التقريبي: لا catch-up بعد انتهاء نافذة المنتج، تغيير الوقت يلغي التسجيل القديم ثم يسجل canonical identity واحدة، timezone/reboot يعيدان reconciliation من الإعداد المحفوظ، و`enabled=false` يلغي جميع الهويات التي يملكها التطبيق.

**خطة انتقال الهوية:** يجرد التنفيذ أولًا أسماء/وسوم الأعمال الحالية، ومنها مسارات startup `azkar_MORNING/EVENING` وأسماء الشاشة الحالية `أذكار الصباح` و`أذكار المساء` (`RemindersScreen.kt:51,60,139-142`)، ثم يعرّف canonical unique names ثابتة لكل نوع. في reconciliation idempotent واحد: يقرأ config، يلغي كل legacy/canonical identity غير المطابقة، يسجل canonical المطلوبة فقط، ويحفظ/يعرض الهوية المقبولة. يلزم اختبار upgrade بوجود كل هوية قديمة منفردة ومجتمعة، ثم disable/change-time/reboot. معيار “لا ازدواجية” يخص التسجيل وبدء التنفيذ الذي يملكه التطبيق؛ لا يعد بضمان OS-level exactly-once delivery.

### 10.4 Track separation

- **Track A commits/diffs:** layout, components, tokens, strings, semantics، without constructor/repository/schema changes.
- **Track B commits/diffs:** one issue family per batch (location gate؛ truth/no-op cleanup؛ Quran WorkInfo؛ Ehsan images؛ reminders). UI changed only enough to expose truthful state، ولا تجمع عائلات الاستمرارية في دفعة واحدة.
- Any change to ViewModel constructor, repository interface, DI binding, persistence owner, or Activity gate is called out before editing and requires approval for that batch.

---

## 11. WIP Handling

### 11.1 Classification

| WIP | Current status | Handling |
|---|---|---|
| Home/Ehsan/Profile/DS tracked modifications | active modern work | preserve; diff around; no legacy replacement |
| `LiquidGlass.kt`, `EhsanForm.kt` | untracked but used by current source | protected dependency; adopt/refine only in approved DS batch |
| `EhsanImageStore` + tests | untracked, not wired to add/render | candidate for Logic batch; do not auto-wire |
| Quran scheduler/repeat/goal files | untracked; scheduler not injected; repeat/goal not runtime | quarantine; only WorkInfo scheduler candidate after approval; repeat/goal remain hidden |
| Manual Prayer Tracker | untracked, no runtime call | do not expose or add stats |
| Prayer Widget | untracked provider/resources, not declared in Manifest | do not register |
| Delete Local Profile Data use case | untracked, not DI/UI wired | do not surface until delete scope/UX approved |
| schema `7.json` | untracked vs DB version 6 | do not edit/delete; investigate provenance before any schema work |
| IhsanPlus demo/controlled UI | debug conditional/release disabled | keep quarantined; no production navigation |
| old `QuranScreen`/second Splash | unused/placeholder | label legacy in inventory; do not delete this project |
| runtime PNG/XML/DB/log artifacts | untracked historical/local | do not cite as current PASS; do not clean |

### 11.2 Collision protocol

قبل كل batch: `git status --short`، approved path list، و`git diff -- <paths>` مع بصمات inputs ذات الصلة وفق §3.4. **التعديل في ملف WIP مسموح فقط إذا شمل اعتماد الدفعة الملف ونطاق التعديل. يُسجل baseline وتُراجع الفروق قبل وبعد. إذا لم يشمله الاعتماد، أو ظهر تغيير جديد غير متوقع، يتوقف الوكيل عن تعديل الملف ويطلب توجيهًا.** بعد الدفعة تراجع حدود diff، ثم تشغل فقط build/tests المصرح بها. لا merge/rebase/commit إلا بطلب، ولا يعني وجود WIP وحده توقف كل المشروع ما دامت الدفعة لا تلمسه.

---

## 12. Priority Matrix

| Priority | Items | Exit condition |
|---|---|---|
| **P0 — Release Blocker** | duplicate Activity location gate؛ غياب baseline/provenance قابل لإعادة الربط عند بدء التنفيذ؛ أي crash أو data-loss يثبته baseline؛ نقص دليل release المطلوب عند ادعاء Release-ready | local content reachable on denial؛ first-launch matrix passes؛ baseline manifest يربط inputs/artifact؛ لا ادعاء release دون أدلته المنفصلة |
| **P1 — Must Finish** | all visible no-ops/fake report/default data؛ Quran WorkInfo truth؛ image persistence؛ reminder config/identity/execution truth؛ phone/time placeholders؛ history navigation؛ active count/statistics scope؛ qibla fallback؛ daily count ownership | كل CTA يعمل/يزال/يعطل بتفسير، ودفعات الاستمرارية مستقلة ومختبرة، ولا يؤجل الخطر لما بعد تعميم التصميم |
| **P2 — UI/UX Redesign** | foundation مصغرة؛ representative screens؛ ثم all runtime roots/content/forms/system screens؛ RTL/Dark/font scaling/touch/semantics في كل UI batch | all runtime surfaces aligned to approved current system without legacy visual patterns؛ كل دفعة تجتاز معاييرها غير الوظيفية قبل التوسع |
| **P3 — Polish/Future** | adaptive/tablet nav، embedded font، screenshot library، dependency pruning، WIP Goal/Repeat/Widget/Tracker/Delete Data، legacy deletion | separate approval/backlog; not required for redesign closure unless promoted |

لا تصنف كل مشكلة P0. P2 أوسع انتشارًا عبر الشاشات، بينما P1 أقل عددًا لكنه أعلى مخاطرة ويأتي مبكرًا عندما يؤثر في الوصول أو صدق الحالة أو الاستمرارية. الأولوية لا تستبدل اعتماد الدفعة ولا دليل runtime.

---

## 13. Execution Phases

> كل مرحلة أدناه stop gate مستقل. موافقة مرحلة لا تفوض التالية.

المسار الحرج يبدأ `Phase 0 → Phase 1 → Phase 2`. بعد ذلك يمكن تشغيل `Phase 3/4/5` كدفعات Logic مستقلة، ويمكن أن تسير `Phase 6` بالتوازي معها بعد اعتماد كل دفعة، بشرط ألا تتداخل قوائم الملفات وأن تكون عقود الحالة اللازمة للعينة معروفة. يبدأ كل جزء من `Phase 7` فقط بعد اكتمال foundation واعتماد/إغلاق Logic الذي يمس شاشته. لا يبدأ التعميم في `Phase 8/9` قبل إغلاق مخاطر الصدق والاستمرارية ذات الصلة.

### Phase 0 — Baseline and protected-state map

- **Goal:** إثبات inputs وما يبنى ويعمل من الـworking tree دون تعديل تصميم أو إتلاف بيانات.
- **Screens/files:** all routes read-only؛ docs/artifacts ومكان provenance manifest المعتمد خارج source، مع جرد tracked/untracked build inputs.
- **Changes:** لا source change؛ إنشاء manifest §3.4 عند الاعتماد، وربط build/test/runtime evidence به.
- **Will not change:** code/dependencies/schema/WIP.
- **Logic dependencies:** اعتماد أوامر التحقق؛ توفر بيئة اختبار. signing credentials ليست شرطًا تلقائيًا لـdebug أو لكل compile/R8 وتقيّم منفصلة.
- **Tests:** الأوامر المعتمدة فقط: debug build/unit/lint؛ release compile/R8 إن سمح الإعداد؛ route smoke/screenshots. fresh install على محاكي أو جهاز/بيئة اختبار منفصلة معتمدة فقط.
- **Regression risk:** artifact لا يمثل untracked inputs؛ overwriting installed app/data؛ اختلاف توقيع.
- **Acceptance/stop:** baseline matrix مؤرخة مرتبطة بـcommit وبصمات جميع build inputs ذات الصلة وvariant/flags/tool versions وartifact hash. لا `uninstall` ولا `pm clear` ولا مسح بيانات الجهاز الحالي. اختبار upgrade يحافظ على البيانات ويستخدم خطة تحقق/نسخ احتياطي معتمدة وحدود استعادة معلنة؛ لا يفترض backup كاملًا على كل جهاز. أي crash/P0 أو provenance ناقص يوقف التعميم.

### Phase 1 — P0 startup/location policy (separate Logic batch)

- **Goal:** إزالة حجب التطبيق غير الضروري وتوحيد مالك permission flow.
- **Files:** MainActivity، Splash، LocationPermission route، AppNavHost، strings/tests.
- **Changes:** أصغر gate correction بعد اعتماد الخيار؛ feature-scoped capability states.
- **Will not change:** location repository contracts/manual-city implementation/prayer calculation.
- **Dependencies:** اعتماد “continue without location”؛ قرار الحاجة الفعلية إلى precise لكل من Prayer/Qibla؛ لا اختراع manual flow جديد.
- **Tests:** مصفوفة §9.3 بحسب إصدار Android: precise/approximate-only، provider off مع permission، revoke أثناء الاستخدام ثم resume، العودة من Settings، fresh/stale saved، manual، none، onboarding complete/incomplete؛ وسجل first-request/rationale قبل وصف permanent denial.
- **Risk:** navigation loops/double request.
- **Acceptance/stop:** رفض الإذن يصل Home؛ Quran/Azkar وبقية local content تعمل؛ permission/provider/location validity/feature need لا تندمج في status واحد؛ Prayer/Qibla show contextual state؛ no loop. واجهة الدفعة تجتاز RTL/Dark/1.3x/2.0x/320dp/48dp/semantics بأدلة، وأي تعارض ملكية يوقف الدفعة.

### Phase 2 — Product truth: fake success, defaults, and silent actions

ينفذ هذا العنوان كمظلة لثلاث دفعات ذات موافقات وstop gates مستقلة، لا كتغيير واحد افتراضي: **2A** report/time/phone truth، **2B** navigation/share/menu no-ops، **2C** Prayer/retry controls. يجوز اعتماد واحدة دون الأخريين، وتحدد كل موافقة ملفاتها واختباراتها.

- **Goal:** منع التطبيق من عرض نجاح غير موجود، بيانات افتراضية كأنها حقيقية، أو CTA يبدو فعالًا وهو no-op قبل تعميم التصميم.
- **Screens/files:** Ihsan Details report/time، Edit Profile default phone، Prayer visible no-ops، Search Zikr، Dua menu، Hadith share، Donation History row، retry affordances؛ تقسم قائمة الملفات المعتمدة إلى diff صغير إذا تجاوزت مراجعة واحدة.
- **Changes:** إزالة/تعطيل/ربط minimal لكل CTA وفق §10.2؛ لا backend وهمي. createdAt mapping أو route callback يذكر صراحة إن دخل العقد المعتمد.
- **Will not change:** Room schema، backend، navigation architecture، offsets غير المعروضة، WIP features.
- **Dependencies:** report product decision، Zikr destination، Prayer notification ownership؛ أي constructor/DI change يدرج في اعتماد الدفعة.
- **Tests:** action→event/state، absent/disabled semantics، no-user/loading، timestamp locale boundaries، external intent failure، back stack.
- **Risk:** حذف affordance مطلوب أو توسيع presentation contract.
- **Acceptance/stop:** لا fake report success ولا phone placeholder ولا CTA صامت ضمن النطاق؛ كل نجاح مرتبط بأثر حقيقي. كل سطح UI متأثر يجتاز RTL/Dark/font 1.3x/2.0x/320dp/48dp/semantics؛ يظهر contract جديد غير معتمد فيوقف الجزء المتأثر.

### Phase 3 — Quran download truth and continuity (independent Logic batch)

- **Goal:** جعل تنزيل القرآن تابعًا لـWorkInfo/files الحقيقيين واستمراره صادقًا عبر interruption/relaunch.
- **Screens/files:** Quran ViewModel/state، existing scheduler/worker/DI، Reader download surface، tests؛ القائمة الدقيقة في الاعتماد.
- **Changes:** إزالة delay الوهمي، ملاحظة canonical work، mapping queued/running/progress/success/failure/retry، reconciliation مع الملف المحلي.
- **Will not change:** Media3 playback contract، Quran content، Room schema، Repeat/Goal WIP.
- **Dependencies:** اعتماد constructor/DI والسcheduler ownership؛ سياسة retry/partial file.
- **Tests:** mapping unit tests؛ offline/reconnect؛ interruption/relaunch؛ partial cleanup/retry؛ downloaded playback.
- **Risk:** duplicate work، orphan files، recomposition churn.
- **Acceptance/stop:** UI لا يعرض نجاحًا قبل evidence الفعلي؛ relaunch يعكس WorkInfo/file truth؛ لا worker duplicate ضمن ملكية التطبيق. سطح Reader المتأثر يجتاز RTL/Dark/font scaling/touch/semantics ولا يدعي performance PASS بلا قياس.

### Phase 4 — Ehsan image ownership and form draft policy (independent Logic batch)

- **Goal:** امتلاك التطبيق للصور التي يعد ببقائها، وتثبيت سياسة استعادة المسودات دون schema أو autosave دائم مخمّن.
- **Screens/files:** Add Donation/Request Help/Edit Profile state، EhsanImageStore، image rendering/DI/use-case boundary، tests؛ لا يدخل ملف WIP إلا إذا سماه الاعتماد.
- **Changes:** app-owned copy before local success، URI/file resolution/fallback/cleanup contract؛ تطبيق قرار draft policy §4.2 في أصغر نطاق.
- **Will not change:** Room schema، backend، cloud identity، durable drafts ما لم يعتمد نطاق جديد منفصل.
- **Dependencies:** اعتماد ownership/cleanup وقرار دورة حياة المسودة؛ حدود النسخ الاحتياطي/الاستعادة.
- **Tests:** restart/relaunch/grant revoke/orphan/failure؛ rotation/system-supported recreation لمسودة صغيرة؛ success/discard/validation failure. force-stop/task removal يختبر فقط إذا اختير عقد دائم، ولا يفترضه saved state.
- **Risk:** URI loss، orphan/delete wrong file، false restoration promise.
- **Acceptance/stop:** الصف الناجح يعرض صورة app-owned بعد restart أو fallback صادق؛ draft يمسح ويبقى وفق السياسة المعتمدة فقط. النماذج المتأثرة تجتاز IME/320dp/RTL/Dark/1.3x/2.0x/48dp/semantics.

### Phase 5 — Reminder persistence, timing, and identity migration (independent Logic batch)

- **Goal:** توحيد config owner وscheduler reconciliation وترحيل الهويات دون الادعاء بالتسليم الدقيق أو exactly-once.
- **Screens/files:** Reminders screen/state، DataStore/repository، Application startup scheduler، worker/unique names، notification capability، tests.
- **Changes:** العقود الثلاثة في §10.3؛ migration idempotent للـlegacy/canonical identities؛ disable/change-time/reboot/timezone reconciliation.
- **Will not change:** آلية exact alarm تلقائيًا، schema، Prayer alarm owner، أو product timing policy دون اعتماد.
- **Dependencies:** قرار approximate مقابل exact؛ late/missed/timezone/reboot policies؛ POST_NOTIFICATIONS behavior؛ قائمة legacy identities المثبتة.
- **Tests:** config persistence؛ legacy identity permutations؛ enable/disable/time change؛ delayed/missed window؛ timezone/reboot/update؛ owned enqueue/start duplicate assertions. لا يختبر exactly-once delivery كضمان OS.
- **Risk:** أعمال قديمة باقية، إشعارات مزدوجة، تضليل scheduled مقابل delivered، OEM delay.
- **Acceptance/stop:** UI يفصل config/registration/execution؛ disable يلغي كل owned identity المعروفة؛ لا duplicate enqueue/start يملكه التطبيق. الشاشة تجتاز RTL/Dark/font scaling/touch/semantics.

### Phase 6 — Minimal Design System foundation (Track A; may run in parallel)

- **Goal:** تأسيس tokens والمكونات المطلوبة فعلًا لعينة Prayer + Quran Reader + Settings، لا تنفيذ خريطة §7.4 كلها.
- **Screens/files:** current `designsystem/theme/*` والمكونات الحالية اللازمة للعينة واختباراتها فقط.
- **Changes:** semantic colors الأساسية، typography/spacing/shape/motion اللازمة، Detail/Reader bars، buttons/icon buttons، list/settings rows، search/state surfaces المنطبقة؛ refine opaque glass fallback فقط إن استخدمته العينة.
- **Will not change:** feature logic/routes/domain، Bottom Navigation/general form family غير المطلوبة، no new dependency، no parallel DS.
- **Dependencies:** palette/RTL/font decisions؛ قائمة usage من العينة؛ عدم تصادم WIP.
- **Tests:** Compose semantics/layout؛ Light/Dark/RTL؛ 320dp؛ font 1.3x/2.0x؛ 48dp؛ actual color-pair checks بالطريقة الموثقة. Preview supplemental فقط.
- **Risk:** wrapper بلا قيمة أو تغيير واسع للمكونات الحديثة.
- **Acceptance/stop:** كل API مستخدم فعليًا في العينة أو يضيف policy/semantics مثبتة؛ DS imports no feature/domain؛ لا PASS للتباين/الرسم العربي دون دليل فعلي.

### Phase 7 — Representative vertical slice: Prayer + Quran Reader + Settings

- **Goal:** validate النظام المصغر على archetypes البيانات والقراءة والإعدادات قبل استخراج مزيد من abstractions.
- **Screens/files:** three screens/components/resources؛ logic files لا تدخل إلا ضمن اعتماد Track B المقابل.
- **Changes:** hierarchy/wireframes، truthful state surfaces، top bars، semantics؛ reuse current components، ثم استخراج تكرار مثبت فقط.
- **Will not change:** audio service، prayer calculator، alarm contracts، Room، كامل target component map.
- **Dependencies:** Phase 6؛ Prayer part بعد 1/2، Reader part بعد 3، Settings/reminder presentation بعد 5 عند وجود تقاطع. يجوز التقدم screen-by-screen إذا كانت الملفات منفصلة.
- **Tests:** جميع الحالات المنطبقة، background audio، location/schedule status، picker، screenshots؛ Light/Dark/RTL/320dp/font 1.3x/2.0x/48dp/semantics في كل شاشة.
- **Risk:** playback/alarm regression، premature abstraction.
- **Acceptance/stop:** text remains central؛ no fake controls؛ actual composite contrast والglass/state layers موثقة حيث تستخدم؛ design review يعتمد العينة قبل propagation.

### Phase 8 — Root coherence and Design System expansion

- **Goal:** preserve/polish Home + Ehsan + Profile + Bottom Navigation ثم توسيع DS من تكرار مثبت.
- **Screens/files:** root screens/current components/MainScreen/resources؛ DS additions justified by at least repeated need.
- **Changes:** deduplicate routes، conditional continue، hierarchy، compact disclosures، current bottom navigation refinement؛ add only valuable shared policies.
- **Will not change:** three root destinations، local-first data، Home use cases إلا في Logic approval مستقل، modern WIP not reverted.
- **Dependencies:** representative review؛ Home catalog/active count decisions؛ collision protocol.
- **Tests:** tab restore/back stack/content states/screenshots؛ كل UI acceptance: RTL/Dark/1.0/1.3/2.0/320dp/touch/semantics/composite colors.
- **Risk:** collision with heavy WIP، generic dashboard drift.
- **Acceptance/stop:** one Qibla entry؛ roots recognizable؛ bottom selection correct؛ لا wrapper جديد بلا policy مثبت ولا regression غير وظيفي مؤجل.

### Phase 9 — Remaining screen families and progressive system expansion

- **Goal:** تعميم التصميم على reading/content، Ehsan forms/details/history، Daily/Statistics/Qibla/Onboarding/Live وبقية Runtime inventory.
- **Screens/files:** تقسم إلى دفعات عائلية صغيرة وفق §9؛ لا دفعة واحدة لكل التطبيق.
- **Changes:** common reading/search/form/list/state patterns مع حفظ الاختلافات؛ logic only in separately approved scope.
- **Will not change:** sacred assets، schema، backend، audio، widget/tracker/repeat/goal/demo activation.
- **Dependencies:** phases 2–8 ذات الصلة؛ Zikr/daily count/Qibla decisions؛ draft policy؛ live lifecycle.
- **Tests:** content/loading/empty/error/offline/permission المنطبقة، navigation/copy/share/count/IME/player؛ وفي **كل دفعة UI** RTL/Dark/1.3x/2.0x/320dp/48dp/semantics/actual composite contrast.
- **Risk:** over-generalization، religious text clipping، image/intent lifecycle.
- **Acceptance/stop:** no religious/critical ellipsis؛ no visible no-op؛ acceptance screen-by-screen §9؛ أي فجوة وصول أو Dark/RTL توقف نشر pattern نفسه.

### Phase 10 — Product-wide accessibility, visual, RTL, Dark, and performance audit

- **Goal:** تدقيق شامل وإغلاق الفجوات المتبقية؛ ليس أول وقت لفحص accessibility أو themes.
- **Screens/files:** smallest affected presentation/DS files + tests/resources، بدفعات إصلاح منفصلة عند الحاجة.
- **Changes:** semantic grouping/order/localization، contrast/reflow/insets/IME، stable keys/reduced motion فقط بعد إثبات الفجوة.
- **Will not change:** business contracts/navigation architecture.
- **Dependencies:** all runtime screens migrated؛ evidence من كل UI batch.
- **Tests:** matrix §14 across widths/font scales/TalkBack/bidi/themes؛ actual alpha/glass/state-layer composites؛ Arabic diacritics؛ measured performance عند الادعاء.
- **Risk:** semantics duplication، late pattern change، false PASS from token-only math.
- **Acceptance/stop:** 48dp؛ thresholds verified by documented method؛ primary actions not clipped؛ no double padding. لا PASS للتباين أو الأداء أو الرسم العربي دون evidence مناسب.

### Phase 11 — Release QA and closure

- **Goal:** prove UI-complete → Runtime-verified → distributable Release-ready separately.
- **Screens/files:** tests/reports/artifacts only unless a defect receives a new approved fix batch.
- **Changes:** none except scoped fixes with new approval.
- **Will not change:** unrelated pre-existing issues/WIP؛ لا uninstall/pm clear للجهاز الحالي.
- **Dependencies:** approved commands/test environments؛ build configuration؛ intended signing credentials للخطوات التي تحتاجها؛ compatible signed versions لترقية حقيقية.
- **Tests:** full matrix §14، مع فصل release compile/R8، artifact behavior، intended certificate، install، compatible-signature upgrade/data preservation.
- **Risk:** OEM/background، credentials، technical build failure، device data، signature mismatch.
- **Acceptance/stop:** كل دليل مطلوب PASS في طبقته؛ debug fallback لا يمنح distributable status؛ أي BLOCKED يمنع وصف “fully closed/Release-ready” دون تحويل ما لم يجر إلى PASS.

---

## 14. Runtime QA Plan

### 14.1 Test standards

- 48dp minimum interactive targets.
- contrast target ≥4.5:1 normal text، ≥3:1 large text and meaningful non-text elements؛ الأرقام في §7 proposals تحتاج إعادة تحقق، والاختبار يكون على composite النهائي مع alpha/glass/state layers والخلفية في الثيمين.
- TalkBack order matches visual/task order؛ no repeated card text from merged+child semantics.
- font scale 1.0/1.3/2.0؛ no critical/religious ellipsis.
- 320dp compact + common compact + larger width; no tablet navigation inference.
- start/end and explicit bidi isolation for phone/time/URL.
- media/compass icons not auto-mirrored.
- insets/status/navigation/IME applied once.
- no heavy blur/infinite decoration؛ Lazy keys stable where lists mutate.
- هذه المعايير تدخل acceptance لكل دفعة UI منذ foundation؛ Phase 10 تدقيق شامل وإغلاق فجوات، لا أول نقطة اختبار.
- state restoration تختبر حسب العقد: recomposition، configuration change، system-supported recreation، app restart/device restart كلٌّ على حدة؛ لا يعامل force-stop/task removal كمرادف لـsaved-state recreation.

### 14.2 Initial QA matrix

كل status أدناه يعكس **هذه الجولة**؛ لم تُشغّل الاختبارات.

| Screen/Flow | Scenario | Expected result | Evidence required | Current status |
|---|---|---|---|---|
| Build | debug compile/package | success from exact manifest inputs | Gradle log + artifact hash | NOT RUN — Stage A scope |
| Build | release compile + R8 | configuration compiles/shrinks؛ signing evaluated separately | Gradle/R8 logs + inputs/variant، no secrets | NOT RUN — Stage A scope; credential blocker not established for this row |
| Release artifact | behavior of produced release artifact | launches/behaves under release flags when a testable artifact can be produced | artifact hash + runtime report | NOT RUN — Stage A scope |
| Release signing | intended distribution certificate | artifact signed by intended cert; no private data exposed | certificate digest/verification report | BLOCKED — intended credentials not configured in limited inspection |
| Release upgrade | compatible signed source→target | installs as upgrade and preserves data | both cert digests + before/after checks | BLOCKED — compatible artifacts/test environment not approved |
| Tests | unit all modules | zero failures | fresh report | NOT RUN |
| Tests | instrumented/Compose UI | target tests pass | device report | NOT RUN |
| Static | lint | no blocking errors; warnings triaged | fresh lint report | NOT RUN |
| Install | new APK in separate approved test environment | installs/launches without touching current-device data | package timestamp/artifact mapping | NOT RUN |
| First launch | onboarding grant/deny/skip on separate emulator/device/profile | no loop; local content reachable on deny؛ no uninstall/pm clear of current device | video/dump/log | NOT RUN |
| Upgrade | existing v6 data with compatible signature | data preserved؛ approved pre/post checks and backup limits recorded | before/after row counts + user-visible checks + cert match | NOT RUN; environment/artifacts may remain BLOCKED |
| Theme | all roots light/dark | correct roles/no flash | screenshots + contrast sample | NOT RUN |
| RTL | all routes | logical order/back icons only mirrored | screenshots/UI semantics | NOT RUN |
| Font | 1.0/1.3/2.0 | no clipped primary actions | screenshot grid | NOT RUN |
| Navigation | bottom/back/deep detail | state restore and correct root selection | UI test + runtime | NOT RUN |
| Layout | 320dp/scroll/IME | save/submit reachable; no double inset | emulator/device evidence | NOT RUN |
| Offline | cold/offline/reconnect | local content works; network states honest | network toggle run | NOT RUN |
| Location | precise/approximate-only/provider off | permission/provider/location validity/feature need stay distinct | runtime matrix by supported Android version | NOT RUN |
| Location | revoke while active/resume + return from Settings | state reconciles without stale grant, loop, or crash | lifecycle/UI state evidence | NOT RUN |
| Location | fresh/stale saved/manual/none | valid source used honestly؛ stale age disclosed؛ local content never blocked | repository/UI/runtime evidence | NOT RUN |
| Permissions | first request/rationale/settings path | no “permanent denial” inference from `shouldShow...=false` alone | request-history + OS-version matrix | NOT RUN |
| Prayer | alarms/adhan/reboot/timezone | one correct schedule and audio policy | logs/notifications/time change | NOT RUN |
| Prayer | exact/inexact/POST_NOTIFICATIONS | labels/actions distinguish states | runtime permission matrix | NOT RUN |
| Quran | playback/background/reader switch | uninterrupted intended behavior | Media3/runtime log | NOT RUN |
| Quran | download interrupt/recover/offline play | WorkInfo truthful; partial safe; local plays | WorkManager/DB/files evidence | NOT RUN |
| Reminders | enabled/time persistence | UI restores saved configuration independently of worker state | DataStore + UI evidence | NOT RUN |
| Reminders | enqueue/reconcile/disable + legacy migration | canonical registration؛ all owned legacy work canceled؛ no owned duplicate enqueue/start | WorkInfo/unique-name/worker evidence | NOT RUN |
| Reminders | delayed/missed/timezone/reboot | follows approved approximate/exact and late/missed policy؛ delivery not inferred | timestamps/config/worker/notification evidence | NOT RUN |
| Ehsan | image add/restart/permission revoke | stable app-owned image or honest empty | DB ref + restart screenshot | NOT RUN |
| Forms | validation/IME/double submit | one local row; errors associated | UI test + DB check | NOT RUN |
| Forms | rotation/config/system recreation | small draft restores or clears exactly per approved policy | state + UI evidence | NOT RUN |
| Forms | force-stop/task removal/app restart | no restoration promise unless durable draft policy is separately approved | product assertion + runtime evidence if applicable | NOT RUN |
| Details | phone/WhatsApp/share/no handler | correct intent or feedback | runtime with/without handlers | NOT RUN |
| Report | visible action | no fake backend success | UI/product assertion | NOT RUN |
| Live | HLS/fallback/offline/leave | proper states; resources released | network/player lifecycle log | NOT RUN |
| Search | Quran/Dua/Zikr result | exact destination/focus | UI test/runtime | NOT RUN |
| Daily/stats | one task/date rollover | no double count; truthful scope | clock/repository fixtures | NOT RUN |
| TalkBack | roots/readers/forms/counters | meaningful order/state/no duplicates | manual TalkBack record | NOT RUN |
| Visual | before/after | approved deltas only | controlled same-device screenshots | NOT RUN |

### 14.3 Evidence levels

- **UI-complete:** source migrated + component/preview review + per-batch accessibility/theme acceptance؛ لا يعني runtime PASS.
- **Runtime-verified:** artifact tied to Phase 0 manifest، scenario run on approved device/emulator دون إتلاف baseline data.
- **Release compile/R8 verified:** release configuration compiled/shrunk successfully؛ لا يثبت signing أو install أو distribution.
- **Release artifact behavior verified:** artifact محدد البصمة اختبر تحت release flags؛ لا يثبت الشهادة المقصودة.
- **Distribution signing verified:** artifact يحمل الشهادة المقصودة المثبتة ببصمة عامة؛ debug fallback لا يحقق هذه الطبقة.
- **Release-ready:** compile/R8 + artifact behavior + intended signing + compatible upgrade/install + tests/lint/background/accessibility matrices pass، والسياسات/العوائق موثقة ومغلقة.

Preview أو build قديم أو PNG بلا provenance لا يرفع status. unsigned release artifact لا يفترض أنه قابل للتثبيت مباشرة، ولا يعالج اختلاف التوقيع بحذف النسخة الحالية. يميز كل BLOCKED بين: **current-scope authorization**، **credentials/signature**، و**technical/environment blocker**.

---

## 15. Risks, Dependencies, and Decisions Requiring Approval

### 15.1 Approval decisions

| ID | القرار المطلوب | Recommended option | البدائل/الأثر |
|---|---|---|---|
| A1 | مالك بوابة الموقع وسياسة الدقة | إزالة Activity block؛ request contextual؛ allow local content؛ لا Fine افتراضيًا إلا لحاجة feature مثبتة | يلزم اعتماد Prayer/Qibla precise-vs-approximate وسلوك stale/manual/provider-off/settings return؛ تغيير Activity يحتاج موافقة صريحة |
| A2 | Home information hierarchy | الصلاة + conditional continue + 3–4 primary + More؛ Qibla مرة | إبقاء كل tiles يقلل الهدوء؛ إزالة state يحتاج قرار analytics/product |
| A3 | RTL/language policy | locale-driven direction مع Arabic default resources | force RTL الحالي يحمي العربية لكنه يكسر أي locale future؛ قرار منتج |
| A4 | embedded Arabic/Quran font | لا خط جديد في baseline؛ فصل styles فقط | إضافة font تحتاج license/size/content QA وموافقة |
| A5 | Quran WorkInfo ownership | inject existing scheduler into VM and observe unique tag | constructor/DI change؛ إبقاء delay غير مقبول للإغلاق |
| A6 | Ehsan image persistence | wire app-owned copy before insert، no schema | requires DI/use-case boundary choice; raw URI remains unreliable |
| A7 | report action | remove from release until real backend، أو approved local hide—not “report sent” | لا يمكن اختراع moderation success |
| A8 | reminders owner/timing/migration | one persisted config + reconciliation؛ approximate daily window الموصى بها؛ migrate all known identities | يلزم اعتماد approximate أم exact، late/missed window، timezone/reboot، identity list؛ exact يتطلب تقييم Android مستقل لا swap تلقائي |
| A9 | Qibla location | reuse canonical PrayerLocationRepository | VM dependency change; alternative repeats fallback logic and is not recommended |
| A10 | Daily count ownership | feature completion owns automatic increment; checklist only opens unless activity explicitly manual | affects current “increase” CTA semantics |
| A11 | screenshot tooling | begin with controlled ADB baseline; evaluate test library only after compatibility spike | new dependency/license/storage cost requires approval |
| A12 | WIP activation | keep Repeat/Goal/Tracker/Widget/Delete/IhsanPlus quarantined | wiring any one is separate product scope |
| A13 | form draft lifecycle | session-only small saved state؛ clear on success/explicit discard؛ no force-stop/task-removal guarantee | no restore بديل أبسط؛ durable draft يحتاج persistence design واعتمادًا جديدًا |
| A14 | Phase 0 provenance/test data | manifest §3.4؛ fresh install في بيئة منفصلة؛ upgrade preserves data with approved checks/backup limits | لا uninstall/pm clear للجهاز الحالي؛ لا يفترض backup كاملًا |
| A15 | release evidence scope | approve compile/R8، artifact behavior، intended cert، compatible upgrade as distinct checks | debug signing fallback لا يثبت distribution؛ credentials block only relevant layers |
| A16 | staged parallelism | allow Phase 6 parallel with approved 3/4/5 only on disjoint files؛ Phase 7 per-screen prerequisites | sequential-only أبطأ؛ أي collision أو data-ownership change يوقف المسار المتأثر |

### 15.2 What constitutes a batch approval

كل اعتماد تنفيذي مستقل يحدد: الهدف؛ قائمة الملفات ونطاق الأسطر/المسؤولية المتوقع؛ أي تغيير constructor أو DI أو contract أو persistence؛ أوامر التحقق المسموحة؛ أثر الاختبار على الجهاز والبيانات؛ ومعايير القبول والتوقف. إذا شمل الاعتماد تعديل constructor/DI أو تشغيل اختبارات محددة، فلا يلزم طلب موافقة إضافية لكل إجراء داخل هذا النطاق.

تحتاج موافقة جديدة عند توسيع الملفات/الهدف، أو ظهور تغيير غير متوقع في ملكية البيانات، أو اصطدام WIP وفق §11.2، أو الحاجة إلى إجراء مدمر/بيئة بيانات مختلفة. الموافقة على تعديل هذه الوثيقة **ليست موافقة ضمنية على Phase 0 أو أي دفعة تنفيذ**.

### 15.3 Risks

- **Dirty-tree collision:** highest operational risk; mitigated by approved-path collision protocol، لا بتوقف مطلق ولا بتعديل WIP غير معتمد.
- **Startup regression:** Activity/Nav permission consolidation can loop; phase isolated.
- **Background/OEM variance:** WorkManager لا يضمن minute-exact execution أو exactly-once delivery؛ exact alarms وMedia3 تحتاج policy/device evidence.
- **Data loss:** image ownership and upgrade tests must precede cleanup; no Room migration planned for design.
- **Sacred text rendering:** any font/line-height change requires diacritic golden set and human Arabic review.
- **Over-abstraction:** shared families only after representative screens prove repetition.
- **False release confidence:** connected device/historical reports do not prove current tree.
- **Dependency compatibility:** no current version is called “latest”; upgrades are outside baseline.
- **Signing:** release fallback to debug signing exists in Gradle when credentials missing; compile/R8 قد يكونان قابلين للتحقق حسب الإعداد، لكن debug fallback لا يثبت الشهادة المقصودة أو قابلية التوزيع أو upgrade. QA يفصل الطبقات.
- **Schema 7 artifact:** unexplained untracked schema must not be normalized or deleted during UI work.

---

## 16. Final Definition of Done

يعد المشروع مغلقًا فقط عندما تتحقق الشروط كلها بأدلة fresh مرتبطة بحالة المستودع النهائية:

1. جميع Runtime screens الأساسية في route inventory تستخدم النظام المعتمد، والاستثناءات موثقة.
2. لا visual legacy pattern ظاهر خارج استثناءات معتمدة، ولا Home/Ehsan/Profile حديثة تم التراجع عنها.
3. كل CTA ظاهر يعمل فعلًا، أو أزيل، أو عُطّل مع تفسير واضح؛ لا no-op.
4. لا fake stats، fake report success، hardcoded relative state، production mocks، أو phone placeholders.
5. البيانات المحلية وRoom migrations وDataStore والصور والتنزيلات باقية بعد restart/upgrade حسب عقد كل منها؛ مسودات النماذج تستعاد وتمسح فقط ضمن دورة الحياة المعتمدة، ولا تنسب لها ديمومة saved state غير المضمونة.
6. رفض location/notification لا يمنع وظائف لا تحتاجها؛ permission/provider/location validity/feature requirement حالات منفصلة، وprecise لا يشترط بلا حاجة مثبتة.
7. Loading/Content/Empty/Error/Offline/Permission ممثلة فقط حيث تنطبق وبمصدر state حقيقي.
8. Light/Dark وRTL وfont 1.0/1.3/2.0 و320dp/common compact/larger تمر دون قص actions أو نص ديني.
9. touch targets/contrast/TalkBack/bidi/insets/IME تجتاز المعايير في §14 بأدلة السطح المركب الفعلي؛ لا token math أو Preview بوصفه PASS.
10. لا crashes معروفة في السيناريوهات المختبرة؛ performance claims مدعومة بقياس لا توقع.
11. debug build/tests/lint ناجحة، وrelease compile/R8 مثبتان منفصلين عن artifact behavior وعن signing؛ أي خطوة غير مطلوبة لادعاء محدد تبقى موسومة بوضوح ولا تتحول إلى PASS.
12. first launch، existing-user upgrade بتوقيع متوافق، alarms/reboot/timezone، Media3/background، WorkManager interruption، reminder config/registration/execution/migration، Ehsan images، external intents، live lifecycle كلها PASS ضمن ما يملكه التطبيق؛ لا وعد OS-level exactly-once.
13. artifact release محدد البصمة اختبر، وشهادة التوقيع المقصودة ثبتت دون كشف secrets، والتثبيت/الترقية اختبرا على بيئة معتمدة دون حذف النسخة الحالية لتجاوز signature mismatch.
14. WIP/debug/demo بقي quarantined ما لم يحصل على موافقة مستقلة واختبار مستقل.
15. مصفوفة QA المطلوبة PASS؛ أي BLOCKED يمنع وصف “Release-ready” أو “Final closure”.

---

## Stage A Stop Gate

هذه الوثيقة هي التغيير الوحيد المصرح في المرحلة A. لا ينشأ Phase 0 manifest، ولا يبدأ أي تعديل source أو dependency أو إعداد أو Build/Test/ADB حتى يوافق المستخدم صراحة على **مرحلة/دفعة محددة** بمحتوى الاعتماد في §15.2 وعلى القرارات المرتبطة بها، وبالأخص A1 قبل أي تغيير في بوابة Activity. اعتماد هذه المراجعة التوثيقية لا يعتمد Phase 0 ولا أي مرحلة تنفيذ.
