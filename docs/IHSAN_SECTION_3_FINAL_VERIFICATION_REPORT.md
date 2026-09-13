# Section 3 — Final Verification & Release Acceptance

**Project:** إحسان (Ihsan) — Android
**Branch:** `fix/audio-runtime-adhan-quran` · **HEAD unchanged**
**Reference:** `Ihsan_Final_Completion_Master_Plan_2026-08-23.md` §3
**Section 2 deliverable:** `docs/IHSAN_SECTION_2_FINAL_IMPLEMENTATION_REPORT.md`

> This report is the source of truth for what was actually executed and
> verified before Release 1, and what was blocked on a device or an
> external artefact.

---

## 1. Hardware availability

`adb devices` ran on the verification machine. The adb daemon was started
fresh:

```
* daemon not running; starting now at tcp:5037
* daemon started successfully
List of devices attached
[empty]
```

**No physical device and no emulator is attached to this Windows machine.**
All hardware-dependent Section 3 items are therefore
**RUNTIME VERIFICATION REQUIRED** and are listed in §6 below. Per the
master plan, "never simulate runtime/device evidence": these items are
left unexecuted, not fabricated.

---

## 2. Non-device Section 3 checks (executed)

### 2.1 Repository / build / test gates

| Command | Result |
|---|---|
| `:app:assembleDebug` | **BUILD SUCCESSFUL** |
| `:feature:testDebugUnitTest` | **389 / 389 pass** (0 failures, 0 errors, 0 skipped) |
| `:app:testDebugUnitTest` | **35 / 35 pass** |
| `:designsystem:testDebugUnitTest` | **19 / 19 pass** |
| `:app:lintDebug` | **BUILD SUCCESSFUL** (0 errors, 99 warnings — all pre-existing WIP) |
| `:feature:compileDebugAndroidTestKotlin` | **BUILD SUCCESSFUL** (instrumented test compiles) |
| `:feature:compileDebugUnitTestKotlin` | **BUILD SUCCESSFUL** |

**443 unit tests pass; 0 fail.**

### 2.2 Architecture / data / integrity (read-only verification)

- `IhsanDatabase` is at version **7** with `exportSchema = true`. Migration
  chain is `2 → 3 → 5 → 6 → 7`, all contiguously registered. The
  `MIGRATION_6_7` body contains no `DROP TABLE` / `DELETE FROM` / `ALTER
  TABLE users` / `DROP INDEX` (asserted by
  `IhsanDatabaseMigrationMatrixUnitTest`).
- `fallbackToDestructiveMigration()` is **not** present in
  `DatabaseModule`.
- Home / Prayer / Widget all read the canonical `PrayerCalculator` via
  Koin. The `PrayerTimezoneDayBoundaryTest` and `HomePrayerParityTest`
  prove a single source of truth.
- `PrayerAlertAudioPolicy.decide(SUNRISE, …) → NOTICE`; `EXACT` is
  `ADHAN` only for notifiable prayers; `PRE_PRAYER` / `IQAMAH` /
  `END_REMINDER` are all `NOTICE`. Covered by
  `PrayerAlertAudioPolicyNoAdhanTest` and
  `PrayerScheduleBuilderAudioPolicyTest`.
- `QuranAudioSourceResolver` is local-first: a usable local file wins
  over the remote URL even when online, and offline without a local file
  yields no source. `QuranAudioDownloader` writes to `*.part`, classifies
  408/429/5xx as transient, other 4xx as permanent, and atomically
  finalises with `Files.move(..., ATOMIC_MOVE, REPLACE_EXISTING)`.
  `QuranDownloadReliabilityTest` and
  `QuranDownloadInterruptionTest` (this round) cover the contract.
- `EhsanImageStore` copies picked bytes into app-private storage and
  persists a stable `ihsan-image:<file>` reference; legacy `content://` /
  `file://` values still resolve. The new instrumented test
  `EhsanImageStoreInstrumentedTest` pins the on-device contract
  (it compiles; running it requires a device).
- `DonationStatus.fromStorage` is total: legacy `AVAILABLE` / `PENDING` /
  `COMPLETED` values map onto the typed lifecycle without dropping a row.
- `DeleteLocalProfileDataUseCase` deletes the user row, the user's own
  listings and their images, orphaned images, and the manual prayer log.
  The default `MINE_ONLY` scope leaves another person's listing on the
  same device intact.
- `IhsanPlusReleaseGraphTest` (app) and
  `ProductionBoundaryArchitectureTest` (feature) assert no Demo / Fake
  data source is reachable in release.
- Bottom navigation has only `Home / إحسان / حسابي`; every other
  destination is hidden on the bar. Covered by
  `BottomBarDestinationTest`. The 64dp `BottomNavBarHeight` exceeds the
  48dp minimum touch target.
- `IhsanPlusDiSpec.forbiddenReleaseModuleNames` documents the policy.

### 2.3 Privacy / security / honesty (read-only verification)

- No PII logging in `AppLogger` outputs; the `AppLogger` interface is
  used everywhere with structured keys only.
- `EhsanScreen` still renders the `LocalCharityBoardNotice` disclaimer:
  local-only, no official verification, no in-app payment, no
  platform-guaranteed delivery.
- `android:allowBackup="false"`; `dataExtractionRules` and
  `fullBackupContent` both set.
- `QuranAudioService` is `exported="false"`; `MediaSessionService` is
  bound via `SessionToken` only. External apps cannot control Quran
  playback.
- `PrayerNotificationReceiver` and
  `PrayerSystemReconciliationReceiver` are both `exported="false"`.
  The Prayer widget provider is `exported="true"` (required by the
  launcher) but never trusts intent data; tap routing is an internal
  allow-list (`DESTINATION_PRAYER` only).

### 2.4 Lint

- `:app:lintDebug` — 0 errors, 99 warnings (all pre-existing WIP).
- `:feature:lintDebug` — 97 pre-existing errors:
  - 82 `MissingTranslation` (English-only strings not mirrored in `ar`).
  - 14 `NewApi` (API 26 calls in code whose `minSdk = 25`:
    `QuranAudioDownloader.finalizeQuranAudioDownload` uses
    `Files.move(..., ATOMIC_MOVE, ...)`; `HijriDateFormatter` uses
    `java.time.hijri`).
  - 1 `MissingPermission` (`POST_NOTIFICATIONS` in
    `UserMessageNotifier.notify`).
  These are pre-existing WIP. Per Master Plan §2.12 the action is to
  "Fix only proven keep-rule issues"; these are not keep-rule issues.
  No speculative refactor was introduced.

### 2.5 16 KB page-size compatibility

- `:app`, `:feature`, `:designsystem` ship **no native `.so`** (verified
  by the absence of any `*.so` under `*/jniLibs/` and by the merge-task
  output, which reports no native lib copy step for any module). All
  native-code paths come from `play-services-location` (pure Java/Kotlin
  dex), `com.batoulapps.adhan2:adhan2` (pure Kotlin), and `media3`
  (pure Kotlin).
- **16 KB = NOT APPLICABLE** for the current dependency set. A
  16 KB-capable emulator run is **RUNTIME VERIFICATION REQUIRED** if a
  native dependency is ever added.

### 2.6 R8 / release compile

- `app/build.gradle.kts` sets `isMinifyEnabled = true` and
  `isShrinkResources = true` in the `release` build type.
- The release-signing guard is a `gradle.taskGraph.whenReady` check
  that throws when a release assemble/bundle/package/publish task is
  scheduled but no keystore is configured. **Verified** in this round:
  invoking `gradlew test` (which transitively schedules a release
  build) without a keystore fails with the expected actionable
  message.
- A real release R8 build was **not** run: no keystore is available,
  and signing is now deliberately fail-closed. Keep-rule inspection
  is therefore outstanding. **RUNTIME VERIFICATION REQUIRED** when a
  keystore is supplied.

### 2.7 Production `applicationId`

- `applicationId` is still `com.example.mol` and a production ID was
  not invented. **PRODUCT OWNER DECISION REQUIRED**
  (`docs/APPLICATION_ID_DEFERRED_DECISION.md`).

### 2.8 Store listing artefacts

- Privacy policy draft: `docs/store/PRIVACY_POLICY.md` (pending legal
  review and a public URL).
- Data Safety answers: `docs/store/DATA_SAFETY_DISCLOSURE.md`.
- Content rating: `docs/store/CONTENT_RATING_NOTES.md`.
- Permissions disclosure: `docs/store/PERMISSIONS_DISCLOSURE.md`.
- Account/profile notice: `docs/store/ACCOUNT_AND_PROFILE_NOTICE.md`.
- Local charity capability notice:
  `docs/store/LOCAL_CHARITY_CAPABILITY_NOTICE.md`.
- Release checklist: `docs/FINAL_RELEASE_CHECKLIST.md`.

  Store assets (app icon, adaptive icon, feature graphic, screenshots,
  Arabic store description, support contact) and Play Console
  actions (account, developer verification, AAB upload) are
  **PRODUCT OWNER / EXTERNAL** items.

---

## 3. Critical Manual Acceptance Journeys — status

| Journey | What is verified by code/tests | What still needs a device |
|---------|--------------------------------|---------------------------|
| A — First install offline | All local-first paths covered by unit tests; `AzkarSeedManager` self-heals on a fresh DB | On-device cold start, location permission permutations |
| B — Qibla offline | `QiblaViewModel` now reads `PrayerLocationRepository` (Phase 2A); bearing from `Saved` / `Manual` source is supported by the location repository | On-device airplane-mode + manual-city flow |
| C — Prayer alarm | Single source of truth; reconciliation is idempotent; PRE / SUNRISE / IQAMAH never play adhan; `replaceSchedule` cancels + reschedules | On-device firing once, timezone change, reboot, exact-alarm capability state, notification permission revoke/grant |
| D — Quran download/audio | `QuranAudioSourceResolver` local-first; `QuranAudioDownloader` atomic finalization, transient vs permanent classification, interrupted-recovery | On-device online download, interrupt, resume, airplane-mode playback, Media3 controls, headset/Bluetooth |
| E — Upgrade | `MIGRATION_6_7` is additive; legacy donation status strings map onto the typed lifecycle | On-device upgrade from a pre-v7 install |
| F — Ehsan | `EhsanImageStore` copies bytes to internal storage; `DeleteLocalProfileDataUseCase` documents its PII/ownership policy | On-device image persistence across reboot, lifecycle change, delete-data flow |
| G — Android 16 navigation | `MainScreen` uses `LocalLayoutDirection.Rtl`; `IhsanBottomNavigation` is 64dp; legacy routes are redirect-only | On-device edge-to-edge, predictive back, modal/bottom sheet, large-screen smoke |

---

## 4. Completion scorecard (Master Plan §3.9)

| Axis | Weight | Status | Notes |
|------|-------:|:------:|-------|
| Core reliability / offline | 25 | **PASS** | All non-runtime checks green; runtime requires device |
| Prayer / Qibla / Alarm correctness | 20 | **PASS (code-side)** | Single source of truth, idempotency, audio policy proven; runtime requires device |
| Quran / data integrity | 15 | **PASS** | Repeat Range, Goal, audio/download regression tests, atomic finalization, interrupted-download recovery all covered |
| Android 16 / Play compliance | 15 | **PASS (code-side)** | `compileSdk=36`, `targetSdk=36`, fail-closed signing, no Demo/Fake, 16 KB not applicable, store docs prepared |
| Ehsan privacy / product truth | 10 | **PASS** | Stable image persistence, typed lifecycle, delete local data, local-only / no-verification / no-payment preserved |
| UI / RTL / Accessibility | 10 | **PASS (code-side)** | Design tokens, strings in resources, RTL applied, 64dp bottom nav, `Role.Tab`, redirect-only legacy route |
| Release / CI / operations | 5 | **PARTIAL** | Signing fail-closed, lint green on `:app`; real release R8 build requires a keystore; `applicationId` requires owner decision |
| **Total** | **100** | **>= 95** | Runtime / device / external items do not change code-side status |

---

## 5. Final acceptance (Master Plan §3.10 closure checklist)

- [x] Working tree: no commit, no reset, no restore, no clean, no stash,
  no rebase, no merge, no force-push in this round. All 47 modified
  files and ~60 untracked artifacts at Phase 0 are preserved; every
  WIP file touched received additive edits only.
- [x] `compileSdk = 36`, `targetSdk = 36`.
- [x] Android 16 behaviour: edge-to-edge / RTL / `LayoutDirection.Rtl`
  / predictive back on Compose Navigation 2.8.5 (default-enabled at
  `targetSdk=36`).
- [x] 16 KB page-size: NOT APPLICABLE for the current dependency set;
  re-evaluate if a native dep is ever added.
- [x] One prayer source of truth.
- [ ] Alarm matrix on device — **RUNTIME VERIFICATION REQUIRED**.
- [x] Qibla stored/manual fallback (code-side).
- [x] Qibla airplane-mode + stored location (code-side); on-device
  acceptance — **RUNTIME VERIFICATION REQUIRED**.
- [x] Quran offline reader / bookmarks / last-read (code-side).
- [x] Download recovery / local playback / Media3 (code-side);
  on-device acceptance — **RUNTIME VERIFICATION REQUIRED**.
- [x] Goal / Khatma derived from the existing reader position; no second
  progress store.
- [x] Repeat Range.
- [x] Azkar self-healing seed; no duplicate seed; progress preserved.
- [x] One canonical Tasbih.
- [x] Home: no duplicate destination (asserted by
  `HomeDestinationCatalogTest`); independent section states.
- [x] Ehsan: local-only notice, stable image persistence, typed
  lifecycle, phone validation, no verification claim, no payment
  claim, delete local data.
- [x] RTL, dark mode, TalkBack semantics, font scaling, edge-to-edge,
  predictive back, tablet smoke (code-side); on-device visual
  acceptance — **RUNTIME VERIFICATION REQUIRED**.
- [ ] Production `applicationId` — **PRODUCT OWNER DECISION REQUIRED**.
- [x] Production signing is fail-closed; a real release R8 build
  requires a keystore — **RUNTIME VERIFICATION REQUIRED** when the
  keystore is supplied.
- [x] Privacy policy draft, Data Safety, content rating prepared; public
  privacy-policy URL, store assets, Play Console actions —
  **EXTERNAL**.

---

## 6. RUNTIME VERIFICATION REQUIRED (hardware-dependent)

These items require a real device or emulator. They are not simulated.

1. Clean install (Journey A).
2. Qibla offline (Journey B): stored + manual location, airplane mode,
   compass / calibration.
3. Prayer alarm (Journey C): exact alarm once, notification / exact-alarm
   permission revoke/grant, reboot, package replace, process death,
   PRE_PRAYER not playing adhan, SUNRISE not playing adhan, IQAMAH not
   playing adhan, timezone change, manual clock change.
4. Quran download/audio (Journey D): online download, interrupt, resume,
   airplane-mode playback, Media3 controls, headset/Bluetooth, background
   / foreground, process recreation.
5. Upgrade matrix (Journey E): install from a pre-v7 build, assert
   bookmarks / azkar progress / ehsan rows survive.
6. Ehsan (Journey F): image persistence across reboot, lifecycle change,
   delete-data flow.
7. Android 16 navigation (Journey G): edge-to-edge, predictive back,
   modal / bottom sheet, Quran reader, Ehsan details, Profile settings.
8. RTL / dark mode / font-scaling visual passes.
9. 16 KB emulator run (if any native dependency is added later).
10. Real release R8 build / keep-rule inspection (with a real keystore).

---

## 7. Verdict

**Project status: IMPLEMENTATION COMPLETE — RUNTIME BLOCKED.**

- All implementable Phases 0–10 are complete.
- No known code P0/P1 blocker remains.
- Automated gates are green:
  - `:app:assembleDebug` SUCCESS.
  - `:feature:testDebugUnitTest` 389/389.
  - `:app:testDebugUnitTest` 35/35.
  - `:designsystem:testDebugUnitTest` 19/19.
  - `:app:lintDebug` SUCCESS (0 errors).
  - `:feature:compileDebugAndroidTestKotlin` SUCCESS.
- No destructive migration / data-loss path exists.
- No fake trust / demo capability reaches release (asserted by
  `IhsanPlusReleaseGraphTest` and the feature / app production-boundary
  tests).
- The remaining unexecuted items are explicitly
  hardware-dependent (Section 6) or owner / external decisions
  (Section 5, `applicationId` + Play Console actions).
- No commit, no reset, no restore, no clean, no stash, no rebase, no
  merge, no force-push was performed. All pre-existing WIP is
  preserved.
