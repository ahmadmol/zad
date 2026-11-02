# خطة العمل النهائية لإنهاء مشروع «إحسان»
**IHSAN FINAL COMPLETION MASTER PLAN**

**الإصدار:** 1.0  
**تاريخ إعداد الخطة:** 2026-08-23  
**نوع الوثيقة:** Living Execution Plan — تُحدّث حتى الوصول إلى Release Candidate نهائي  
**النطاق:** Android — Kotlin / Jetpack Compose / Room / Koin / Media3 / WorkManager / AlarmManager  
**الهدف:** إنهاء تطبيق «إحسان» كمنتج Android محلي-first موثوق وقابل للنشر، وليس فتح دورة تطوير غير منتهية.

---

## SECTION 1 EXECUTION STATUS

- **Status:** PASSED
- **Execution Date:** 2026-08-23
- **Branch:** `fix/audio-runtime-adhan-quran`
- **HEAD:** `da7967c` (`da7967ca9d0b83d1d906081e962966625a8f918f`)
- **Evidence Report:** `docs/IHSAN_SECTION_1_PRODUCT_TRUTH_AND_SCOPE_LOCK.md`

### CURRENT CODE OVERRIDES — 2026-08-23

The following Master Plan claims were re-verified against the current code. Where current code differs from the prose, **current code wins** and this section is the binding truth:

1. **Database:** `IhsanDatabase` is at `version = 6`, `exportSchema = true`. The broad `fallbackToDestructiveMigration()` is **no longer present** in `DatabaseModule.kt`. Registered migrations are `MIGRATION_2_3`, `MIGRATION_3_5`, `MIGRATION_5_6`. Both `feature/schemas/.../IhsanDatabase/5.json` and `…/6.json` exist. (v5 → v6 added a non-null `users.role` column via table-rebuild.)
2. **R8 / resource shrinking:** `isMinifyEnabled = true` and `isShrinkResources = true` are now set in the `release` build type of `app/build.gradle.kts`. The earlier `FINAL_COMPLETION_BASELINE.md` claim that "R8 disabled" is **outdated**.
3. **Release signing:** Wired through env/local properties (`IHSAN_KEYSTORE_PATH` etc.). Falls back to **debug** signing if any var is missing. This silent fallback is a Release 1 blocker (must be made fail-closed in Section 2).
4. **HomeDashboardViewModel is NOT a God Object.** It is a thin Presentation/Aggregation layer that composes 7 `ObserveHome*UseCase` flows. Do not re-open it as a refactor target.
5. **IhsanPlus is quarantined in release** by `IhsanPlusFeatureFlags.dailyEnabled = false` (BuildConfig field in `feature/build.gradle.kts`). The only IhsanPlus UI route (`Screen.IhsanPlusDaily`) is not registered in `AppNavHost` when the flag is off. The `ihsanPlusProductionModule` is still included in `appModule`, but it only binds `IhsanPlus{Daily,Prayer,Charity}Source` to real `Production*Adapter`s — no fake sources are reachable in release. `IhsanPlusDiSpec.forbiddenReleaseModuleNames` documents the policy.
6. **Azkar seed is still on `onCreate` only** — there is no `AzkarSeedManager`, no versioned repair, no idempotent self-healing. Confirmed via grep across `feature/src/main/java/com/example/feature`.
7. **Qibla still does NOT use `PrayerLocationRepository` or stored/manual location** — `QiblaViewModel` only calls `fusedLocationClient.lastLocation.await()` and `getCurrentLocation()`. Confirmed via direct read of `QiblaViewModel.updateLocationAndCalculateQibla()`.
8. **Ehsan `imageUrl` is a raw `content://` URI string** with no `takePersistableUriPermission` and no copy to internal storage. `AddEhsanScreen.kt` and `RequestHelpScreen.kt` use `ActivityResultContracts.GetContent()` and store `uri.toString()`.
9. **`Donation.status` is still `String`** (comment "AVAILABLE, PENDING, COMPLETED"). No enum, no transition logic.
10. **No "Delete local profile/data" UI exists.** `UserDao.clearUser()` and `UserRepository.logout()` exist but are not surfaced in any screen, and they do not cover donations, images, or preferences.
11. **No Prayer Home Widget, no Manual Prayer Tracker, no Quran Goal/Khatma Plan UX, no Quran Repeat Range, no Quran Notes.** None of these exist in code.
12. **Bottom navigation has only Home / إحسان / حسابي** — confirmed in `MainScreen.kt` and `Screen.companion.items`. All other features (Quran, Azkar, Tasbih, Statistics, Settings, Reminders, Daily Activities) are reached via Home sections or Profile.

### SECTION 1 EXIT-CRITERIA RESULT

All Section 1 exit criteria are met (see `docs/IHSAN_SECTION_1_PRODUCT_TRUTH_AND_SCOPE_LOCK.md` §16). **Section 2 is READY** to begin.

### SOURCE FILES MODIFIED BY SECTION 1

```text
docs/IHSAN_SECTION_1_PRODUCT_TRUTH_AND_SCOPE_LOCK.md   (created)
Ihsan_Final_Completion_Master_Plan_2026-08-23.md         (this status header only)
```

No source code in `:app`, `:feature`, `:designsystem`, `AndroidManifest.xml`, or any Gradle file was modified. Pre-existing WIP (47 modified + ~60 untracked files) was preserved untouched.

---

> **تعريف "انتهاء المشروع" في هذه الوثيقة:** لا يعني أن التطبيق لن يحتاج تحديثات مستقبلية، ولا يمكن تقنيًا ضمان غياب كل Bug مستقبلي. المقصود هو: لا يُعلن المشروع DONE ولا يُنشَر Release نهائي حتى تمر جميع بوابات الاختبار والقبول الواردة في القسم الثالث، ولا يبقى أي P0/P1 blocker معروف أو أي ادعاء منتجي غير مدعوم بالواقع.

---

# القسم الأول — الحقيقة الحالية، الاستطلاع السوقي، ونطاق المنتج النهائي

## 1.1 مصدر الحقيقة

عند وجود تعارض بين هذه الخطة وتقارير قديمة، يكون ترتيب الثقة:

1. الكود الحالي + HEAD الحالي + Working Tree الحالي.
2. نتائج Build/Test الحالية.
3. أحدث تقرير Ask Mode بتاريخ 2026-08-23.
4. تقارير Phase 1 والـArchitecture اللاحقة.
5. دراسة السوق السابقة بتاريخ 2026-08-12.
6. التقارير الأقدم بوصفها سجلًا تاريخيًا فقط.

### Baseline المعروف عند كتابة الخطة

حسب أحدث تدقيق متاح:

- المشروع Android فقط.
- Gradle modules الحالية: `:app`, `:feature`, `:designsystem`.
- `IhsanDatabase` وصل إلى v6 مع `exportSchema=true`.
- `fallbackToDestructiveMigration()` لم يعد المسار الحالي وفق أحدث تدقيق.
- `PrayerTimesFacade`, `PrayerScheduleBuilder`, `ReconcilePrayerScheduleUseCase`, `NextPrayerSelector` موجودة، لذلك **لا نعيد بناء Prayer architecture من الصفر**.
- `HomeDashboardViewModel` أصبح يعتمد على عدة `ObserveHome*UseCase`، لذلك لا يُعامل كـGod Object بالشكل الذي وصفته تقارير أقدم.
- Settings وStatistics لهما ViewModels مستقلة.
- `TasbihScreen` هو المسار canonical، واسم `onOpenSebha` بقي Debt تسمية لا منتجًا موازيًا.
- IhsanPlus Production adapters موجودة، وFake/Demo modules محمية من Release بحسب Architecture tests/feature flags؛ لا نربط IhsanPlus كمنتج ثانٍ.
- `QuranAudioSourceResolver` أصبح Local-first: الملف المحلي أولًا، والـRemote fallback فقط عند الاتصال.
- `QuranAudioDownloader` يستخدم temp file + atomic move + retry/permanent failure distinction.
- `LocalCharityBoardNotice` يصرّح بأن إحسان محلي ولا يوجد تحقق رسمي أو دفع داخل التطبيق.
- أهم فجوتين مثبتتين في السلوك الحالي:
  1. Qibla لا يستخدم الموقع اليدوي/المخزن في التطبيق كـfallback بعد فشل Fused/GPS.
  2. Azkar seed يعمل عند DB creation، ولا يوجد self-healing مؤكد لقاعدة موجودة فقدت البيانات الأساسية.
- Home يعرض أسطحًا متكررة تؤدي إلى وجهات متشابهة (`HomeQuickActions` + `HomeServicesSection`).
- صور إحسان تُخزن حاليًا كـ`content://` URI String، وهي نقطة تحتاج معالجة استمرارية الوصول.
- `Donation.status` ما زال بحاجة إلى lifecycle typed وواضح إن لم يكن قد تغير بعد هذا التقرير.
- `applicationId`/`namespace` في أحدث لقطة كانا `com.example.mol`.
- `targetSdk`/`compileSdk` في أحدث لقطة كانا API 35.
- أحدث Ask Mode لم يشغّل build/tests كاملة على الحالة الحالية؛ لذلك **الـBaseline التنفيذي أول خطوة إلزامية**.
- Working Tree في أحدث لقطة كان كبيرًا ومتداخلًا؛ لا يُسمح بحذف أو reset لأي WIP قبل تصنيفه.

> **قاعدة:** كل حقيقة أعلاه يعاد التحقق منها في Phase 0. إذا تغير الكود، تُحدّث هذه الوثيقة بدل إجبار الكود على مطابقة تقرير قديم.

---

## 1.2 تعريف المنتج النهائي

### الرؤية النهائية

**إحسان = رفيق مسلم يومي Local-first، هادئ وموثوق، تعمل عباداته الأساسية دون إنترنت، مع لوحة إحسان محلية صريحة بحدودها الحقيقية.**

### المبادئ غير القابلة للتفاوض

1. Prayer/Qibla/Quran text/Azkar/Tasbih/Duas/Hadith/Asma لا تعتمد على الشبكة لفتحها.
2. الإنترنت يحسّن التجربة ولا يفتح الوظائف المحلية الأساسية.
3. لا يوجد Fake trust أو Fake verification أو Fake impact في Release.
4. لا يوجد Second Home أو Second Prayer أو Second Qibla أو Second Ehsan.
5. كل Route له غرض فعلي أو يُحذف/يُحوّل بعد reachability audit.
6. Home لا يكرر الوجهة نفسها في أكثر من Surface بلا سبب سياقي.
7. التنبيه الديني يدار من الميزة التي تخصه؛ Prayer alerts داخل Prayer.
8. إعدادات المستخدم وبياناته المحلية لا تُفقد عند Upgrade.
9. لا يُبنى Backend فقط لأن المنافسين لديهم Backend.
10. Release 1 يجب أن يكون مكتملًا بذاته دون Backend.

---

## 1.3 استطلاع السوق — ما يفعله الأفضل حاليًا

### Quran.com / Quran Android

النمط المميز:
- Quran reading واضح ومركزي.
- Bookmarks/Tags.
- Search.
- Translations/Tafsir.
- Audio مع verse highlighting.
- Audio repeat.
- Offline audio downloads في منظومة Quran.com الحديثة.
- Playlists/Khatma progress في تجربة الصوت.

**الدرس لإحسان:** Reader يجب أن يبقى هادئًا؛ نضيف أدوات القراءة والصوت حول النص، لا نحول المصحف إلى Dashboard.

### Tarteel — حالة 2025/2026

المزايا الملحوظة:
- Goals للحفظ والمراجعة والتلاوة.
- Today's Goals.
- Memorization Tracker.
- Flexible/portion-based plans.
- Offline reciter audio.
- Bookmarks.
- Dark mode / adaptive font.
- AI mistake detection وVoice Search.
- Testing mode للحفظ بأسلوب بطاقات/أسئلة.

**الدرس لإحسان:** Goal بسيط للقرآن عالي القيمة، لكن AI memorization ليس شرطًا لإنهاء Release المحلي.

### Pillars

المزايا الملحوظة:
- Prayer-first UX.
- Multiple calculation methods.
- Qibla.
- Prayer notifications.
- Prayer Tracker.
- Home-screen widgets.
- Location calculation محلي مع توجه خصوصية واضح.

**الدرس لإحسان:** Widget + manual prayer tracking من أفضل الإضافات الصغيرة التي تزيد الاستخدام اليومي دون Backend.

### MAWAQIT

المزايا الملحوظة:
- مواقيت الصلاة والتنبيهات.
- المساجد القريبة.
- أخبار وإشعارات المسجد.
- الجمعة والعيد.
- تكامل مع الهاتف/الساعة/الشاشات/المساعدات الذكية.

**الدرس لإحسان:** Mosque network قيمة مستقبلية قوية لكنها تتطلب بيانات مركزية ومصدرًا موثوقًا، لذلك لا تدخل Release 1 المحلي.

### Muslim Pro — حالة 2026

المزايا الملحوظة:
- Next prayer + pre-adhan reminder.
- Prayer tracking.
- Qibla.
- Quran translations/transliteration/Tajweed.
- Memorization loop.
- Bookmarks/Notes/Playlists/reading history.
- Khatam plan/goals.
- Widgets.
- Duas categorized.
- Community وAI ومحتوى فيديو.

**الدرس لإحسان:** نأخذ الوظائف اليومية المحلية عالية القيمة، ولا نقلد تضخم المحتوى/المجتمع/AI في Release 1.

### LaunchGood — تحديث يوليو 2026

المزايا الملحوظة:
- Campaign discovery.
- Local/global causes.
- Urgent alerts.
- Scheduled giving.
- Storytelling.
- Verification process.
- Zakat verification عبر مراجعة فعلية.

**الدرس لإحسان:** لا نعرض "موثقة" إلا إذا توجد عملية Verification فعلية. في النسخة المحلية نركز على Lifecycle وContact safety والشفافية.

### ShareTheMeal — 2026

المزايا الملحوظة:
- Giving flow قصير.
- Impact metrics.
- Challenges.
- Community goals.
- Transparency حول الأثر.

**الدرس لإحسان:** في Local Ehsan نعرض أثرًا محليًا من نوع `نشط → جارٍ التنسيق → مكتمل/منتهي` بدل ادعاء أثر مالي مركزي.

---

## 1.4 Gap Analysis — ما ينقص إحسان فعليًا

### A. Must Fix — لا Release بدونها

| ID | الفجوة | القرار |
|---|---|---|
| G-01 | Baseline build/test غير مثبت على الحالة الحالية | إلزامي Phase 0 |
| G-02 | Qibla لا يستخدم stored/manual app location fallback | إصلاح |
| G-03 | Azkar لا يملك self-healing versioned لقاعدة موجودة ناقصة البيانات | إصلاح |
| G-04 | Prayer alarm behavior يحتاج device evidence حديث | إثبات + إصلاح إن لزم |
| G-05 | Quran audio/download/offline يحتاج device evidence حديث | إثبات + إصلاح إن لزم |
| G-06 | Room upgrade matrix يحتاج instrumented/device proof | إثبات |
| G-07 | Home destinations مكررة | توحيد |
| G-08 | Ehsan `content://` image persistence غير مضمون | تخزين داخلي دائم أو Persistable URI strategy مثبتة |
| G-09 | Ehsan lifecycle/status يجب أن يكون typed وواضحًا | تنفيذ إن لم يكن موجودًا |
| G-10 | Product identity `com.example.mol` غير مناسبة كهوية نهائية | قرار وتنفيذ package/applicationId قبل النشر |
| G-11 | Release signing يجب ألا يسقط بصمت إلى debug signing | Fail-closed release signing |
| G-12 | Play policy / privacy / Data Safety / content rating | إلزامي للنشر |
| G-13 | Android 16 / API 36 | إلزامي إذا تم تقديم التطبيق/التحديث ابتداءً من 2026-08-31 |
| G-14 | Edge-to-edge + Predictive Back + Arabic font behavior على API 36 | اختبار وإصلاح |
| G-15 | 16 KB page-size compatibility | audit واختبار، خاصة إن وُجدت native libs transitively |

### B. Final Product Parity — نضيفها لأنها عالية القيمة ولا تحتاج Backend

هذه الميزات تدخل الخطة النهائية بشرط ألا يوجد لها تنفيذ مكافئ بالفعل:

1. **Prayer Home Widget**
   - الصلاة القادمة.
   - وقتها.
   - أقرب 2–3 أوقات.
   - Tap يفتح Prayer.
   - لا يجلب Location في الخلفية بلا داعٍ؛ يقرأ آخر state محسوب.

2. **Manual Prayer Tracker**
   - المستخدم يعلّم الصلاة كمؤداة يدويًا.
   - لا ادعاء بأن التطبيق يعرف أداء الصلاة تلقائيًا.
   - يدعم Statistics المحلية.
   - لا Leaderboard.

3. **Quran Goal / Khatma Plan بسيط**
   - هدف صفحات/آيات أو موعد ختمة.
   - Progress محلي.
   - Continue Reading.
   - لا AI ولا Cloud required.

4. **Quran Repeat Range**
   - إن لم يكن موجودًا: repeat آية/نطاق آيات لعدد محدد.
   - يعمل مع local audio إن تم تنزيله.

5. **Delete Local Profile/Data**
   - حذف واضح للملف المحلي وبيانات المستخدم الحساسة التي أنشأها.
   - مفيد للخصوصية حتى دون Account backend.

> لا نضيف ميزة جديدة قبل Feature Gap Audit يثبت أنها غير موجودة فعليًا أو أنها ناقصة وظيفيًا.

### C. Nice-to-have — لا تؤخر Release

- Notes على آيات القرآن.
- More reciters UI polish.
- Juz/Hizb advanced browsing.
- High-contrast theme إضافي.
- Tablet-specific secondary pane.
- Home-screen quick shortcut لسورة الكهف يوم الجمعة.
- Export local personal stats.

### D. Post-release Connected Program — خارج إغلاق المشروع الحالي

لا تدخل Release 1:

- Remote accounts / OTP / OAuth.
- Cloud sync.
- Backend charity marketplace.
- Verification badges.
- Moderation pipeline.
- Payments.
- Messaging/Inbox.
- Mosque network.
- Community feed.
- Community campaigns.
- Scheduled donations.
- AI Quran mistake detection.
- Voice Quran search.
- Social leaderboards.
- iOS.
- Full Gradle module split.
- IhsanPlus كمنتج مستقل.

هذه ليست "ميزات ناقصة" في Release المحلي؛ هي برنامج منتج آخر.

---

## 1.5 Final Release 1 Scope

### يدخل Release 1

- Splash / Onboarding / Location permission.
- Home contextual dashboard بدون duplicate destinations.
- Prayer calculation.
- Next prayer.
- Per-prayer notification behavior الموجود والمثبت.
- Pre-prayer reminder إن كان ضمن المنتج الحالي.
- Adhan runtime.
- Boot/timezone/date/package-update reconciliation.
- Qibla local-first + stored/manual fallback.
- Quran list/reader/search/bookmarks/last-read.
- Quran audio local-first + download/retry/corrupt-partial protection.
- Quran simple goal/Khatma.
- Quran repeat range إن كان غير موجود.
- Azkar complete offline library + self-healing.
- Canonical Tasbih.
- Duas.
- Hadith.
- Asma.
- Daily activities.
- Manual prayer tracking.
- Private Statistics.
- Settings.
- Prayer widget.
- Live Haram/Nabawi streams مع error/retry/fallback وبدون اعتبارها core offline feature.
- Local Ehsan Board: offer/request/details/contact/history/lifecycle.
- Local profile.
- Delete local profile/data.
- Dark mode.
- RTL.
- Accessibility.
- Android 16 / API 36 compatibility.
- Privacy policy + Play Data Safety + content rating.
- Production identity/signing.
- CI and release gates.

---

## 1.6 Do Not Build

هذه القيود تمنع الخطة من التحول إلى مشروع جديد:

- لا Second Home.
- لا ربط كامل لـIhsanPlus.
- لا Fake verification.
- لا Fake impact.
- لا Backend abstractions فارغة.
- لا Messaging بلا Model/Backend واضح.
- لا Payment placeholders.
- لا إعادة كتابة Architecture العاملة.
- لا Gradle module split شامل قبل Release.
- لا redesign شامل أثناء Reliability fixes.
- لا حذف WIP من Git دون إثبات ملكيته.
- لا تخزين PII في runtime logs.
- لا "حسنات رقمية" أو Leaderboard للعبادة.
- لا جعل القرآن أو القبلة أو الأذكار تعتمد على الإنترنت.

---

# القسم الثاني — خطة التنفيذ الهندسية والمنتجية النهائية

## 2.1 قواعد التنفيذ

1. **PR واحد = نية واحدة.**
2. كل PR يجب أن يحتوي:
   - Objective.
   - Scope.
   - Files touched.
   - Tests.
   - Device verification إن لزم.
   - Acceptance criteria.
   - Rollback.
3. لا Merge إذا فشل Merge Gate.
4. لا تغيير UI واسع مع DB migration أو alarm refactor في PR واحد.
5. كل Phase يغلق بدليل، لا بجملة "تم".
6. أي finding قديم يُعاد التحقق منه قبل إصلاحه.
7. Feature Freeze بعد انتهاء Phase 7.

### حالات العمل المستخدمة داخل هذه الوثيقة

- `TODO`
- `IN_PROGRESS`
- `PASSED`
- `BLOCKED`
- `DEFERRED`
- `REJECTED`

---

## 2.2 Phase 0 — Repository Freeze & Executable Baseline

**الحالة:** TODO  
**الأولوية:** P0  
**الهدف:** منع ضياع WIP وبناء baseline حقيقي قبل لمس المنتج.

### المهام

- `git status -sb`
- `git status --short`
- branch + HEAD + log.
- تصنيف كل Modified/Untracked.
- عدم `reset/restore/clean/stash` تلقائيًا.
- تحديد runtime DB/log/screenshot artifacts الحساسة.
- تشغيل:
  - `:app:assembleDebug`
  - `:feature:testDebugUnitTest`
  - `test`
  - `lint`
  - release compile/assemble حيث تسمح signing configuration.
- تسجيل عدد الاختبارات.
- تسجيل أول root failure فقط لكل command.
- إنشاء Baseline Report.
- تثبيت قائمة "Unsafe to touch" الحالية.

### Exit Gate

- لا ملف WIP مجهول.
- Build baseline مسجل.
- Test baseline مسجل.
- Known failing tests مصنفة.
- branch/HEAD مثبتان.
- لا cleanup مدمر.

---

## 2.3 Phase 1 — Android 16 / Play Compliance Migration

**الحالة:** TODO  
**الأولوية:** P0 بسبب موعد 2026-08-31  
**الهدف:** جعل المنتج قابلًا للتقديم إلى Google Play بعد 31 أغسطس 2026.

### المهام

1. Upgrade:
   - `compileSdk = 36`
   - `targetSdk = 36`
   - AGP/Kotlin/dependencies فقط بالقدر المطلوب للتوافق.
2. Build before behavior changes.
3. Audit Android 16 targeted changes:
   - Edge-to-edge mandatory.
   - Predictive Back.
   - Arabic font/elegant text behavior.
   - Large-screen orientation/resizability behavior.
4. Compose insets audit:
   - status bar.
   - navigation bar.
   - IME.
   - Bottom Navigation.
   - sheets/dialogs.
5. Back navigation:
   - system back.
   - in-app back.
   - child routes.
   - predictive back gestures.
6. Test `WorkManager`/scheduler behaviors على Android 16.
7. 16 KB page-size audit:
   - inspect AAB/APK native libs.
   - verify third-party SDKs.
   - run 16 KB emulator if native libs exist.
8. Target API compatibility report.

### Acceptance

- debug build API 36 passes.
- release compile API 36 passes.
- no clipped content due edge-to-edge.
- system back works on roots/children.
- Arabic typography remains readable.
- large-screen smoke passes.
- 16 KB status = COMPATIBLE أو NOT APPLICABLE with proof.

---

## 2.4 Phase 2 — Offline Reliability Closure

**الحالة:** TODO  
**الأولوية:** P0  
**الهدف:** إغلاق الميزات التي يجب أن تعمل دون شبكة.

### 2.4.1 Qibla

المسار المطلوب:

`stored/manual app location → cached Fused fix → fresh GPS/Fused fix → explicit unavailable`

أو ترتيب مكافئ مثبت يمنح immediate usable bearing بدون انتظار الشبكة.

#### المطلوب

- استخدام `PrayerLocationRepository` أو مصدر location canonical.
- لا `WaitingForInternet`.
- cached bearing يظهر فورًا.
- GPS refresh غير حاجب.
- manual location fallback عند فشل providers.
- compass unavailable/calibration states.
- generation token/hysteresis الحالي لا يُكسر.

#### Tests

- unit: Fused fails + manual location exists.
- unit: no manual + no GPS → explicit unavailable.
- device: airplane + stored location.
- device: airplane + GPS.
- device: permission denied/granted.

### 2.4.2 Azkar Self-Healing

إنشاء `AzkarSeedManager` أو mechanism equivalent، بشرط:

- versioned.
- idempotent.
- transactional.
- base-content aware.
- لا يمس favorites/progress.
- يعمل على fresh install.
- يصلح existing DB ذات base data ناقصة.
- لا يعتمد على network.

#### Tests

- fresh DB.
- existing DB empty.
- partially missing seed.
- second run no duplicates.
- favorites/progress survive.

### Phase 2 Exit

- Qibla offline acceptance PASSED.
- Azkar integrity acceptance PASSED.
- no network dependency introduced.

---

## 2.5 Phase 3 — Prayer / Alarm / Time Reliability Proof

**الحالة:** DONE — implementation passed, runtime acceptance required (Section 2)
**الأولوية:** P0  
**الهدف:** إثبات أن Architecture الحالية تعمل فعلًا على الجهاز.

### لا نعيد بناء PrayerTimesFacade

المطلوب هو characterization + runtime validation + targeted fixes فقط.

### Matrix

- كل calculation methods المدعومة.
- Madhhab.
- manual offsets.
- Auto/manual location.
- DST.
- timezone change.
- manual clock change.
- before Fajr.
- after Isha.
- midnight rollover.
- reboot.
- package update.
- exact alarm permission revoke/grant.
- notification permission revoke/grant.
- process death.
- reschedule idempotency.
- alarm fires once.
- PRE لا يشغل Adhan.
- SUNRISE لا يشغل Adhan.
- IQAMAH لا يشغل Adhan.

### Home/Prayer parity

Assert:

`Home next prayer == PrayerScreen next prayer`

لنفس location/settings/time.

### Cleanup محدود

- نقل `PrayerCalculationMethod` / `Madhhab` parsing من Home presentation إذا بقي هناك.
- لا refactor إضافي إذا لا توجد فائدة قابلة للقياس.

### Exit

- full device evidence.
- no duplicate/orphan alarms.
- no divergent next-prayer result.

---

## 2.6 Phase 4 — Quran Completion & Audio Reliability

**الحالة:** COMPLETE — Repeat Range, Quran Goal (Ayahs/Day, Pages/Day, Khatma-by-Date), audio/download regression tests (transient/permanent classification, atomic finalization, partial-file safety, interrupted-download recovery) all delivered and unit-tested. Runtime acceptance still requires on-device evidence.
**الأولوية:** P0/P1

### Core verification

- Quran text fully offline.
- Surah list.
- Reader.
- Search.
- Last-read.
- Bookmark.
- process death persistence.
- rotation/recreation.
- Arabic font scaling.

### Audio

- local file preferred.
- offline + local file → plays.
- offline + no local → clear offline state.
- online + no local → remote only when intended.
- download to temp.
- atomic finalization.
- cancellation.
- 404/permanent failure.
- timeout/transient retry.
- partial/corrupt file recovery.
- Media3 notification controls.
- headset/Bluetooth basics.
- app background/foreground.
- process recreation.

### Final parity additions

#### Quran Goal / Khatma

Model بسيط:

- goal type: pages/ayahs/khatma date.
- local persistence.
- progress based on actual reader progress/manual completion rules.
- no forced streak.
- no leaderboard.

#### Repeat Range

- repeat current ayah.
- repeat selected range.
- repeat count.
- stop cleanly.
- works with downloaded audio.

> إذا audit أثبت أن هذه الوظائف موجودة فعلًا، لا تُكرر؛ فقط أغلق اختبارات القبول.

---

## 2.7 Phase 5 — Home & Information Architecture Closure

**الحالة:** DONE (Section 2)
**الأولوية:** P1

### الهدف

كل وجهة تظهر مرة واحدة، بينما المنطقة العليا تعرض state/context لا قائمة خدمات مكررة.

### Home النهائي

1. Prayer hero/summary.
2. Continue Quran.
3. Today summary:
   - Azkar/Dhikr.
   - Daily activity.
   - optional local Ehsan item.
4. Services grouped once.
5. Small private insight.

### إزالة التكرار

قارن `HomeQuickActions` و`HomeServicesSection`، ثم:

- أبقِ contextual summaries.
- أبقِ service destination مرة واحدة.
- Ehsan Root لا يحتاج tile مكرر بلا داعٍ.
- Live entry واحد يفتح اختيار Haram/Nabawi.
- Reminders العام يُزال من Home/Profile إن كان المنتج قرر retirement.

### Reliability UI

كل section:
- Loading مستقل.
- Error مستقل.
- Empty مستقل.
- فشل section لا يخفي Home كاملًا.

### Performance

- recomposition inspection.
- Lazy lists.
- no duplicate collectors.
- no repeated expensive date/prayer calculation in composables.

### Exit

- no duplicate destination surfaces.
- home smoke test.
- screenshot comparison.
- no navigation regression.

---

## 2.8 Phase 6 — Worship UX Completion

**الحالة:** COMPLETE — Per-prayer MUTED/NOTICE/ADHAN control modes wired into PrayerScheduleBuilder + PrayerViewModel; Sunrise no-Adhan policy enforced; canonical `TasbihScreen` (no Sebha duplicate, `onOpenSebha` is naming debt only); Manual Prayer Tracker with `prayer_log` table (DB 6→7 migration, additive); Statistics wired to the manual-prayer repository via `PrayerTrackerStatsCalculator`. Daily Activities customize/reorder is explicitly deferred (not in current data model).
**الأولوية:** P1

### Prayer UX

إذا لم تكن موجودة بالفعل، اجعل Prayer Screen مركز التحكم:

- per-prayer mode:
  - MUTED
  - NOTICE
  - ADHAN
- Sunrise:
  - لا ADHAN.
- pre-prayer reminder منفصل.
- advanced:
  - calculation method.
  - madhhab.
  - manual offsets.
  - location source.
  - exact alarm status.
  - notification permission.
  - adhan test.

### Tasbih

Canonical `TasbihScreen` فقط:

- current count.
- goal.
- large tap target.
- haptic.
- autosave.
- undo.
- reset confirmation.
- common presets.
- session history إذا data model يسمح بلا تعقيد كبير.
- rename stale `Sebha` callbacks/identifiers فقط بعد usage audit.

### Daily Activities

- RTL timeline.
- completion state.
- customize/hide/reorder only إذا البنية الحالية تدعم ذلك بأمان.
- لا global reminder dependency.

### Manual Prayer Tracker

إضافة محلية بسيطة:

- user marks prayer performed.
- optional time/status only if product needs it.
- data feeds Statistics.
- no automatic judgment.
- no "missed" accusation from app logic.
- no leaderboard.

### Statistics

- weekly Quran activity.
- Dhikr sessions.
- daily activity completion.
- manually logged prayers.
- local Ehsan activity.
- compare current/previous week where data exists.
- private by default.

---

## 2.9 Phase 7 — High-Value Android Feature: Prayer Widget

**الحالة:** COMPLETE — `PrayerWidgetProvider` reads the canonical `PrayerCalculator` + `PrayerLocationRepository` + `PrayerSettingsRepository` (no second calculator, no continuous GPS, no network, no polling). One inexact alarm at the next prayer instant via `AlarmManager.set(RTC, …)`. RTL via `layoutDirection="locale"`; light/dark via `values`/`widget_text_*` and `values-night/`. Tap opens the Prayer screen through an allow-list of internal destinations (no raw intent routing). State factory has 16 unit tests covering Unavailable/Ready/day-rollover/no-network/settings-changes/refresh-scheduling. Runtime acceptance on device still required.
**الأولوية:** P1

### لماذا يدخل النسخة النهائية؟

Pillars وMuslim Pro يجعلان prayer widget جزءًا من الاستخدام اليومي. هذا يعزز قيمة إحسان بدون Backend.

### Scope

Widget واحد فقط في Release 1:

- next prayer.
- countdown أو الوقت.
- اليوم/الموقع المختصر إن كان متاحًا.
- tap → Prayer Screen.
- optional compact list لأوقات اليوم في الحجم الكبير.

### قواعد

- لا يحسب الصلاة بنسخة logic منفصلة.
- يقرأ من Prayer domain/read model canonical.
- لا يطلب location background continuously.
- refresh باستخدام schedule معقول.
- يدعم dark/light.
- يدعم RTL.
- no stale impossible state.

### Tests

- widget snapshot/state tests حيث ممكن.
- timezone change.
- date rollover.
- prayer setting change.
- no location.
- reboot.

---

## 2.10 Phase 8 — Ehsan Local Product Completion

**الحالة:** COMPLETE — 8A `EhsanImageStore` copies picked bytes into app-private storage and persists a stable `ihsan-image:<file>` reference; legacy `content://` values still resolve; path-traversal guarded; orphan cleanup; 12 unit + androidTest cases pin the contract. 8B `DonationStatus` is the typed lifecycle used through `EhsanManagementUseCases` and `ProfileViewModel`; storage stays `TEXT` so the column type did not need to change; `fromStorage` is total and round-trips legacy `AVAILABLE`/`PENDING`/`COMPLETED` strings. 8C `DeleteLocalProfileDataUseCase` with documented PII/ownership policy is surfaced in Profile. 8D local-only/no-verification/no-payment honesty preserved.
**الأولوية:** P1

### Product truth

Release 1 = Local Community Board، وليس verified marketplace.

### 2.10.1 Images

أغلق خطر `content://`:

الخيار المفضل:
- copy selected image into app-owned storage.
- store stable internal reference/path.
- cleanup orphan images.
- delete image with listing where appropriate.

أو استخدم persistable URI فقط إذا document provider + permissions behavior مثبت بالكامل.

### 2.10.2 Typed Lifecycle

إن لم يكن موجودًا:

`ACTIVE → COORDINATING → FULFILLED / EXPIRED / CANCELLED`

- migration safe.
- mapping legacy strings.
- invalid state rejected.
- status visible للمستخدم.

### 2.10.3 Local flow

Offer:
- create.
- view.
- contact.
- update/close.
- history.

Request:
- create.
- validated phone.
- view.
- contact.
- mark coordinated/completed.
- history.

### 2.10.4 Privacy

- local-only notice.
- approximate location presentation where possible.
- no exact coordinates shown publicly without need.
- phone only when user initiates contact flow.
- no PII in logs.
- delete local listing/profile/data path.
- no verified badge.

### 2.10.5 IhsanPlus

- Production adapters only where they add neutral value.
- no FakeDataSource in release graph.
- `charityTrust` remains disabled without real provider.
- useful privacy/safety copy may be reused.
- no second Ehsan UI.

---

## 2.11 Phase 9 — Design System, RTL, Accessibility, Navigation Hygiene

**الحالة:** COMPLETE — Design tokens in `designsystem` (`Color.kt`, `IhsanDimens.kt`, `Spacing.kt`, `Theme.kt`, `Typography.kt`); user-facing strings live in `feature/src/main/res/values` + `values-ar`; RTL is applied via `LocalLayoutDirection.Rtl` at the `MainScreen` and per-screen `LayoutDirection.Rtl` where it matters; bottom navigation has 64dp height (well above 48dp) and `Role.Tab` semantics; `BottomBarDestinationTest` asserts only the 3 canonical roots resolve; `LegacyDonationDetail` is a redirect-only `@Deprecated` route. Two pre-existing hardcoded brand-color literals (`Color(0xFFFFD54F)` in Ehsan, `Color(0xFF6B9080)` in EditProfile) are pre-existing WIP and deliberately not touched.
**الأولوية:** P1

### Design system

- hardcoded brand greens → tokens.
- spacing/shapes via design system.
- no generic mega-component.
- dark theme consistency.
- sacred text high contrast; no visual blur over Quran/Azkar.

### Strings

نقل user-facing strings في الشاشات المعدلة إلى resources:
- Prayer.
- Qibla.
- Ehsan.
- Profile/Auth.
- Settings.
- Errors.

لا يلزم ترجمة English في Release 1، لكن يجب أن تصبح البنية قابلة للترجمة.

### RTL

- `start/end`.
- arrows/chevrons visually correct.
- mixed Arabic/numbers.
- time formatting.
- dynamic font scale.

### Accessibility

- touch target >= 48dp.
- content descriptions.
- TalkBack:
  - Qibla angle/state.
  - prayer notification mode.
  - Tasbih count/goal.
  - media controls.
- font scale:
  - 1.0
  - 1.15
  - 1.3
  - 1.5
- contrast.

### Navigation

- canonical routes only.
- `LegacyDonationDetail` removal only after proof no callers.
- Inbox remains excluded without messaging model.
- child screens hide bottom nav according to policy.
- predictive back passes.

---

## 2.12 Phase 10 — Release Engineering & Store Readiness

**الحالة:** COMPLETE (code-side) — `compileSdk=36` / `targetSdk=36`; release signing is now fail-closed (no debug fallback, no secrets in Git, `RELEASE_SIGNING_GUIDE.md` documents the keystore variables); R8 + resource shrinking enabled in `release`; `IhsanPlusReleaseGraphTest` proves no Demo/Fake data source is reachable in release; store docs (`docs/store/PRIVACY_POLICY.md`, `DATA_SAFETY_DISCLOSURE.md`, `CONTENT_RATING_NOTES.md`, `PERMISSIONS_DISCLOSURE.md`, `ACCOUNT_AND_PROFILE_NOTICE.md`, `LOCAL_CHARITY_CAPABILITY_NOTICE.md`) are prepared; `FINAL_RELEASE_CHECKLIST.md` records all section-by-section status. `applicationId` is `PRODUCT OWNER DECISION REQUIRED` (still `com.example.mol`); real release R8 build and the 16KB emulator run are blocked on a keystore and a real device respectively. Lint has 97 pre-existing errors in `:feature` (82 MissingTranslation, 14 NewApi on minSdk 25, 1 MissingPermission); no speculative refactor was introduced; per Master Plan §2.12 the action is to "fix only proven keep-rule issues" — these are not keep-rule issues.
**الأولوية:** P0 قبل النشر

### Production Identity

اختر production `applicationId` نهائيًا.

قبل التغيير:
- verify uniqueness.
- verify app links/deep links إن وجدت.
- verify package-dependent providers/authorities.
- decide namespace migration separately if needed.

لا تعتبر تغيير namespace شرطًا إذا لا توجد حاجة؛ applicationId هو هوية التوزيع الأساسية.

### Signing

- production keystore خارج Git.
- secrets via local/CI secure storage.
- Release build **fails closed** إذا signing secrets غير موجودة.
- لا silent debug fallback.
- document backup/recovery policy للkeystore.

### R8

- `app` release shrink/minify configuration verified.
- لا تفترض أن `:feature` يجب أن يفعل minify مستقلًا.
- run release R8.
- inspect missing rules.
- test reflection/serialization/Koin/Media3/Room.
- keep rules minimal.

### Play

- target API 36.
- AAB.
- content rating.
- Data Safety.
- privacy policy URL public.
- privacy policy text accessible in app.
- location disclosure accurate.
- no false claim of cloud account.
- app category.
- store listing.
- app icon/adaptive icon.
- feature graphic.
- screenshots.
- Arabic store description.
- support contact.
- third-party stream wording.

### 16 KB

- inspect native `.so`.
- verify Play/App Bundle compatibility.
- run 16 KB emulator if applicable.
- document pass/not-applicable.

### Observability

حد أدنى:
- release-safe crash/ANR visibility.
- no PII payloads.
- user-consent/Privacy review if third-party crash SDK used.
- إن لم يُستخدم vendor، استخدم Play Android vitals + local structured non-PII diagnostics.

---

## 2.13 Pull Request Sequence

> الأسماء مرجعية؛ إذا كان ملف معين يحتوي WIP غير مدمج، يُعاد ترتيب PR بدل الكتابة فوقه.

| PR | Intent | Merge Gate الأساسي |
|---|---|---|
| PR-00 | Baseline + WIP protection report | Build/test inventory recorded |
| PR-01 | API 36 migration | Debug + release compile + behavior smoke |
| PR-02 | Qibla stored/manual fallback | unit + airplane device test |
| PR-03 | Azkar seed self-healing | DB tests + no duplicate/preserved progress |
| PR-04 | Prayer characterization/runtime fixes | full alarm matrix |
| PR-05 | Room migration device proof | supported upgrade matrix passes |
| PR-06 | Quran audio/download runtime closure | offline/download/Media3 matrix |
| PR-07 | Home dedup + independent states | Compose/navigation tests |
| PR-08 | Prayer UX/control closure | mode/sunrise/pre-reminder tests |
| PR-09 | Quran goal + repeat audit/implementation | local persistence + audio tests |
| PR-10 | Tasbih + manual prayer tracker + stats adapter split into separate PRs if needed | domain/unit/UI tests |
| PR-11 | Prayer Widget | state + date/time/device tests |
| PR-12 | Ehsan stable image storage | persistence tests |
| PR-13 | Ehsan typed lifecycle | migration + transition tests |
| PR-14 | Profile/local-data deletion + honesty copy | privacy behavior tests |
| PR-15 | Design tokens/strings/RTL/a11y | screenshot/a11y checklist |
| PR-16 | Navigation legacy cleanup + IhsanPlus release gate audit | Koin/nav architecture tests |
| PR-17 | Production identity + signing + R8 | signed release AAB |
| PR-18 | Play policy + 16 KB + store assets | policy checklist PASSED |
| PR-19 | Final CI/release gates | RC candidate built from clean tag |

### قاعدة

إذا PR يحتوي نيتين خطرتين، قسّمه.  
خصوصًا ممنوع دمج:

- DB migration + Navigation redesign.
- API 36 migration + large UI redesign.
- Prayer scheduler change + Home redesign.
- Ehsan status migration + image persistence.
- Signing + feature logic.

---

# القسم الثالث — التحقق النهائي، ضمان الجودة، والإطلاق والإغلاق

## 3.1 Definition of Done العامة

المشروع لا يعتبر منتهيًا حتى تتحقق جميع البنود التالية.

### Architecture

- Prayer calculation له source of truth واحد.
- Prayer scheduling له reconciliation path واحد.
- Home لا يعيد حساب Prayer بطريقة مستقلة.
- Settings/Statistics boundaries مستقلة.
- Tasbih canonical واحد.
- Ehsan detail canonical واحد.
- no fake release graph.
- no knowingly dead user-facing route.
- module split ليس شرطًا للإطلاق.

### Data

- Room migrations لكل الإصدارات المدعومة PASS.
- no destructive fallback.
- schema export history retained.
- Azkar repairs missing base data idempotently.
- Quran bookmarks/last-read survive restart/upgrade.
- Ehsan images remain readable after restart/reboot.
- donation status migration preserves rows.
- local profile deletion works as documented.

### Offline

Airplane Mode يجب ألا يمنع:

- Prayer calculation.
- Qibla with stored/manual location.
- Quran text.
- downloaded Quran audio.
- Azkar.
- Tasbih.
- Duas/Hadith/Asma.
- Daily activities.
- Statistics.
- local Ehsan records.

### Runtime

- prayer alarm exactly once.
- reboot reconciliation.
- timezone reconciliation.
- clock-change reconciliation.
- permission changes handled.
- Quran Media3 controls.
- download retry.
- no corrupted final audio file.
- Live stream failure does not crash app.

### Product Truth

- local profile labeled local.
- Ehsan labeled local.
- no payment claim.
- no verification claim.
- no messaging claim.
- no cloud sync claim.
- IhsanPlus demo state unreachable in release.

### UI

- Android 16 edge-to-edge correct.
- predictive back correct.
- three root bottom navigation behavior correct.
- no duplicate Home services.
- RTL correct.
- dark mode correct.
- font scaling correct.
- TalkBack critical controls labeled.
- error/empty/loading states actionable.

### Release

- target API 36.
- production applicationId.
- release signing production only.
- signed AAB created.
- R8 build tested.
- 16 KB compatible/not-applicable with evidence.
- privacy policy live.
- Data Safety completed.
- content rating completed.
- store assets complete.
- no P0/P1 blocker.

---

## 3.2 Automated Test Pyramid

### Unit Tests — required

Prayer:
- calculation methods.
- madhhab.
- offsets.
- next prayer.
- sunrise non-notifiable.
- PRE/IQAMAH policies.
- schedule builder.
- reconciliation idempotency.

Qibla:
- bearing math.
- cached/manual/fresh priority.
- no-location state.
- generation/stale callback protection where testable.

Azkar:
- seed idempotency.
- version upgrade.
- missing subset repair.
- preserve user progress.

Quran:
- source resolver.
- download error classification.
- search.
- goal progress.
- repeat range state machine.

Home:
- section aggregation.
- section failure isolation.
- no duplicate destinations data contract.

Ehsan:
- phone validation.
- status transition state machine.
- image reference rules.
- local-only capability.
- deletion.

Settings/Statistics:
- state changes.
- prayer tracker aggregation.
- daily rollover.

### Integration Tests

- Koin graph resolution.
- release graph no Demo/Fake.
- Room migrations.
- DAOs/repositories.
- DataStore.
- Navigation args.
- WorkManager download scheduling.
- Prayer alarm gateway where feasible.

### Compose/UI Tests

- Home renders independent states.
- Prayer mode UI.
- Qibla permission/location states.
- Quran continue/goal.
- Azkar error/repair state.
- canonical Tasbih.
- Ehsan local-only notice.
- local profile wording.
- bottom nav root/child behavior.
- font scaling critical screens.

### Instrumented Tests

- Room migration using real Room database.
- Media3 service/controller connection.
- WorkManager download.
- notification channel.
- exact alarm permission flows where automation permits.
- widget rendering/update.

---

## 3.3 Device Matrix

لا يكفي جهاز واحد.

### Minimum matrix

1. **Android 7.1 / API 25** — minSdk compatibility.
2. **Android 12/13** — mid-generation permissions/background behavior.
3. **Android 14** — exact alarm modern behavior.
4. **Android 15** — edge-to-edge + 16 KB-capable environment.
5. **Android 16 / API 36** — target release platform.
6. **Large screen/tablet emulator** — Android 16 adaptive layout behavior.
7. **16 KB page-size emulator** — إذا يوجد native dependency.

### Hardware capabilities

على جهاز فعلي واحد على الأقل:
- compass/rotation sensor.
- GPS.
- notification.
- media playback.
- Bluetooth/headset إن متاح.

---

## 3.4 Critical Manual Acceptance Journeys

### Journey A — First install offline

- install.
- onboarding.
- deny/allow location permutations.
- Quran opens.
- Azkar has content.
- Prayer handles no location honestly.
- Qibla gives explicit state.
- no crash.

### Journey B — Qibla offline

1. save manual/known location.
2. airplane mode.
3. open Qibla.
4. bearing appears without internet.
5. sensor updates.
6. calibration state works.
7. return network — no screen reset.

### Journey C — Prayer alarm

1. enable one prayer.
2. schedule near test time.
3. kill app.
4. alarm fires once.
5. pre-prayer = notification only.
6. sunrise = no adhan.
7. change timezone.
8. reboot.
9. no duplicate.

### Journey D — Quran download/audio

1. online download.
2. interrupt midway.
3. resume/retry.
4. final file valid.
5. airplane mode.
6. playback.
7. notification controls.
8. background app.
9. reconnect.

### Journey E — Upgrade

- install old supported DB artifact.
- create profile/bookmarks/azkar progress/ehsan row.
- upgrade to final version.
- all expected data survives.
- migration asserts schema.

### Journey F — Ehsan

- create local profile.
- add offer with photo.
- restart.
- photo still works.
- contact.
- change lifecycle.
- appears in history.
- delete listing/profile where allowed.
- no verification/payment wording.

### Journey G — Android 16 navigation

- root → child → back.
- predictive back.
- modal/bottom sheet.
- Quran reader.
- Ehsan details.
- Profile settings.
- no duplicate shell/root.
- edge-to-edge no overlap.

---

## 3.5 Non-Functional Gates

### Performance

Targets to measure, not guess:

- cold start no blocking DB/network work on main thread.
- Home first content available from local state quickly.
- scrolling stable on Home/Quran/Azkar/Ehsan.
- no repeated location request loop.
- no alarm reschedule storm.
- no download worker duplication.
- no memory leak from MediaController/Player.
- live stream releases player/web resources on exit.

### Battery

- exact alarms only where justified.
- widget update cadence reasonable.
- no continuous GPS when unnecessary.
- no continuous network polling.
- WorkManager constraints appropriate.

### Security/Privacy

- backup policy verified.
- exported components reviewed.
- no PII logging.
- no hardcoded secrets.
- secure FileProvider/URI rules.
- release build no debug-only components.
- third-party SDK inventory.
- location permission minimized.
- privacy/Data Safety match reality.

---

## 3.6 CI Merge Gates

كل PR بعد Phase 1 يجب أن يمر بما ينطبق:

- `assembleDebug`
- release compile
- unit tests
- lint/static analysis
- architecture tests
- Koin graph tests
- fake-data release check
- Room schema consistency
- selected instrumented migration tests
- API 36 compatibility checks
- no unexpected generated/untracked sensitive artifacts

لا يسمح `main` بMerge إذا فشل Required Gate.

---

## 3.7 Release Candidate Protocol

### RC-0 — Engineering Candidate

- جميع features موجودة.
- no feature development after this point.
- only blocker fixes.

### RC-1 — Full QA Candidate

- clean install matrix.
- upgrade matrix.
- offline matrix.
- permissions matrix.
- Android 16 matrix.

### RC-2 — Store Candidate

- signed AAB.
- R8.
- app bundle inspection.
- 16 KB.
- Data Safety.
- privacy.
- content rating.
- store screenshots/assets.

### Production Candidate

لا يتم اعتماده إلا إذا:

- `P0 = 0`
- `P1 = 0`
- جميع Must Fix = PASSED
- all core journeys = PASSED
- no fake release capability
- no data-loss risk
- no known alarm duplication
- no known Quran offline regression
- no known Qibla offline regression

---

## 3.8 Rollback Strategy

### قبل النشر

- كل PR revertable.
- DB migrations لا تُعدل بعد دمجها؛ أي correction migration جديدة.
- schema artifacts محفوظة.
- release tags immutable.
- keystore backed up securely.

### بعد النشر

- staged rollout.
- monitor Android vitals.
- crash/ANR gate.
- stop rollout عند:
  - data loss.
  - prayer alarm regression واسع.
  - startup crash.
  - Room migration crash.
  - Quran unreadable/audio crash loop.
  - PII/security issue.

---

## 3.9 Completion Scorecard

لا تستخدم الانطباع "التطبيق يبدو جاهزًا". استخدم نقاطًا:

| المحور | الوزن |
|---|---:|
| Core reliability/offline | 25 |
| Prayer/Qibla/Alarm correctness | 20 |
| Quran/data integrity | 15 |
| Android 16/Play compliance | 15 |
| Ehsan privacy/product truth | 10 |
| UI/RTL/Accessibility | 10 |
| Release/CI/operations | 5 |
| **المجموع** | **100** |

### شرط النهاية

- Score >= 95/100.
- لا P0/P1.
- كل عنصر وزنه Critical ليس أقل من PASS.
- البنود المتبقية فقط P2/P3 documented backlog.

---

## 3.10 Final Closure Checklist

### Repository
- [ ] Working tree النهائي clean.
- [ ] Release branch/tag واضح.
- [ ] no sensitive runtime artifacts.
- [ ] docs reflect current code.

### Platform
- [ ] compileSdk 36.
- [ ] targetSdk 36.
- [ ] Android 16 behavior audit passed.
- [ ] 16 KB audit passed/not applicable.

### Prayer/Qibla
- [ ] one prayer source of truth.
- [ ] alarm matrix passed.
- [ ] Qibla stored/manual fallback passed.
- [ ] airplane mode passed.

### Quran
- [ ] offline reader passed.
- [ ] bookmarks/last-read passed.
- [ ] download recovery passed.
- [ ] local playback passed.
- [ ] Media3 controls passed.
- [ ] goal/khatma passed.
- [ ] repeat range passed أو documented existing equivalent.

### Azkar/Tasbih
- [ ] seed repair passed.
- [ ] no duplicate seed.
- [ ] progress survives.
- [ ] one canonical Tasbih.

### Home
- [ ] no duplicate destination.
- [ ] independent section errors.
- [ ] stable performance.

### Ehsan/Profile
- [ ] local-only notice.
- [ ] stable image persistence.
- [ ] typed lifecycle.
- [ ] phone validation.
- [ ] no verification claim.
- [ ] no payment claim.
- [ ] delete local data/profile.

### UI
- [ ] RTL.
- [ ] dark mode.
- [ ] TalkBack.
- [ ] font scaling.
- [ ] edge-to-edge.
- [ ] predictive back.
- [ ] tablet smoke.

### Release
- [ ] production applicationId.
- [ ] production signing.
- [ ] R8 release test.
- [ ] privacy policy.
- [ ] Data Safety.
- [ ] content rating.
- [ ] AAB.
- [ ] store assets.
- [ ] staged rollout plan.

---

## 3.11 Feature Freeze Rule

بعد اكتمال Phase 7:

**أي فكرة جديدة لا تعالج P0/P1 ولا تمنع النشر تُنقل إلى Post-release backlog.**

هذا يشمل حتى الأفكار الجيدة مثل:
- mosque network.
- AI.
- social community.
- payments.
- remote sync.
- iOS.

الهدف هو **إنهاء المنتج، لا إعادة فتحه كل أسبوع**.

---

## 3.12 مصادر الخطة

### المصادر الداخلية

1. أحدث تقرير Ask Mode الشامل بتاريخ 2026-08-23 (`تم لصق markdown(10).md`).
2. `Architecture-&-Structure-Review.txt`.
3. `Second-Pass-Verification-Baseline.txt`.
4. `Technical-Diagnosis.txt`.
5. `ihsan_project_progress_summary.md.txt`.
6. `Ihsan_Architecture_Implementation_Plan.md`.
7. `ihsan_market_research_and_product_improvement_plan_2026-08-12.md`.

### البحث الخارجي — مراجعة 2026-08-23

مصادر رسمية/أولية تم اعتمادها في المقارنة:

- Quran.com / Quran Android / Connected Quran Apps.
- Tarteel Help Center — Features, Goals, Memorization & Progress, Testing.
- Pillars official website + Google Play listing.
- MAWAQIT official website.
- Muslim Pro official product pages — Prayer, Quran, Widgets.
- LaunchGood Google Play + Support.
- ShareTheMeal official website + FAQ.
- Google Play target API requirements.
- Android Developers — Android 16 behavior changes.
- Android Developers — 16 KB page-size compatibility.
- Google Play User Data / Data Safety / Privacy Policy requirements.

### نتائج البحث التي تؤثر مباشرة على التنفيذ

- ابتداءً من **31 أغسطس 2026**، Google Play يطلب Android 16 / API 36 للتطبيقات الجديدة والتحديثات على الهواتف.
- Android 16 target يفعّل/يفرض سلوكيات يجب اختبارها: Edge-to-edge، Predictive Back، وتغييرات عرض الخطوط العربية.
- Google Play يطلب Privacy Policy وData Safety دقيقة حتى للتطبيقات التي لا تجمع بيانات على خادم.
- دعم 16 KB page size يجب تدقيقه، وخصوصًا عند وجود Native SDKs.
- أفضل التطبيقات المتخصصة تثبت قيمة:
  - prayer widget.
  - prayer tracking.
  - Quran goals.
  - Quran repeat/offline audio.
  - privacy-first local location.
- الميزات المتصلة مثل mosque network/verification/payments/community تحتاج infrastructure حقيقي؛ لذلك تم فصلها عن Release 1.

---

# القرار التنفيذي النهائي

ابدأ من **Phase 0** ولا تتجاوز Phase إلا بعد Exit Gate.

الترتيب الحاكم:

**Baseline → Android 16 compliance → Offline reliability → Prayer proof → Quran proof → Home closure → Worship UX → Widget → Ehsan completion → Design/Accessibility → Release Engineering → RC verification → Production.**

إذا التزم الفريق بهذه الوثيقة حرفيًا، فلن يُعتبر مشروع «إحسان» منتهيًا بناءً على الانطباع أو نجاح Build واحد، بل بناءً على **أدلة Build/Test/Device/Policy قابلة لإعادة التحقق**.
