# Final Release Checklist — إحسان Release 1

**Derived from:** `Ihsan_Final_Completion_Master_Plan_2026-08-23.md` §2.12 and §3.10
**Maintained by:** Section 2 · **Last updated:** end of Section 2 execution

Legend: **[x]** done and verified · **[~]** implemented, needs device/runtime acceptance ·
**[ ]** not done / blocked externally.

---

## 1. Platform

- [x] `compileSdk = 36`, `targetSdk = 36`
- [x] `:app:assembleDebug` green
- [x] No native `.so` in any module → 16 KB page size **NOT APPLICABLE**
      (re-evaluate if a native dependency is ever added)
- [x] Foreground service typed (`mediaPlayback`) and `exported="false"`
- [x] Prayer receivers `exported="false"`; widget provider exported but does not trust
      intent data
- [~] Edge-to-edge visual pass on device
- [~] Predictive back on device
- See `docs/ANDROID_16_COMPATIBILITY.md`

## 2. Data

- [x] `IhsanDatabase` at version 7, `exportSchema = true`
- [x] Migration chain 2→3→5→6→7, contiguity asserted by test
- [x] `MIGRATION_6_7` proven additive (no `DROP TABLE` / `DELETE FROM` / table rewrite)
- [x] No `fallbackToDestructiveMigration` in `DatabaseModule`
- [x] Legacy `Donation.status` strings preserved via total `DonationStatus.fromStorage`
- [~] Migration 6 → 7 on a real upgraded install
- [~] Instrumented migration test run on device (`:feature:androidTest`)

## 3. Offline / local-first

- [x] Prayer, Qibla, Quran text, Azkar, Tasbih, Duas, Hadith, Asma open without network
- [x] Quran audio is local-first; offline without a local file yields a clear offline
      state rather than a silent remote attempt
- [x] Qibla falls back to stored/manual location
- [x] Azkar self-healing seed
- [~] Airplane-mode journey on device

## 4. Prayer / alarms

- [x] Single canonical `PrayerTimesFacade`; Home/Prayer parity proven by test
- [x] PRE_PRAYER / SUNRISE / IQAMAH / END_REMINDER can never play the adhan
- [x] Per-prayer MUTED / NOTICE / ADHAN; muted prayers are not scheduled at all
- [x] Reconciliation idempotent; cancels on unavailable location / disabled notifications
- [x] Timezone, DST and day-boundary characterization
- [~] Alarm fires on device; reboot; package replace; process death
- [~] Exact-alarm and notification permission revoke/grant

## 5. Quran

- [x] Repeat Range (single ayah / range / count / clean stop) with downloaded audio
- [x] Quran Goal (ayahs/day, pages/day, khatma-by-date) derived from the existing
      khatma reader position — no second progress store
- [x] Download reliability: transient vs permanent classification, atomic finalization,
      no partial file left behind
- [x] Interrupted-download recovery: cancellation, transient IO failure, permanent
      failure, stale `.part` file, empty target, missing source, finalisation
      contract (7 tests in `QuranDownloadInterruptionTest`)
- [~] Media3 notification controls, headset/Bluetooth, background/foreground,
      process recreation on device

## 6. Worship UX

- [x] Manual Prayer Tracker with real persistence; never infers a missed prayer
- [x] Statistics reads the real tracker data (weekly, with previous-week comparison)
- [x] Tasbih: undo + reset confirmation added; canonical `TasbihScreen` only
- [x] Per-prayer MUTED / NOTICE / ADHAN; muted prayers are not scheduled at all
- [x] Sunrise no-Adhan policy enforced at audio-policy and schedule-builder layers
- [ ] Daily Activities customize / reorder — **not implemented** (optional in the plan,
      "only if the current structure supports it safely")

## 7. Home / navigation

- [x] Every Home destination appears exactly once (asserted by test)
- [x] One Live entry opening the Haram/Nabawi chooser
- [x] Independent per-section loading/error/empty states
- [x] No Inbox / messaging implemented (deliberately out of scope)

## 8. Ehsan

- [x] App-owned image persistence; legacy `content://` values still resolve
- [x] Typed `DonationStatus` lifecycle enforced in the use case end-to-end
      (`EhsanManagementUseCases` and `ProfileViewModel`)
- [x] Delete local profile/data with an explicit ownership + PII policy
- [x] Local-only / no-verification / no-payment honesty preserved
- [x] `EhsanImageStore` instrumented coverage (12 cases) pins the on-device
      contract: persist+resolve across a fresh store, legacy `content://` /
      `file://` passthrough, missing file, path-traversal, separator
      rejection, delete, no-op on legacy, orphan cleanup, restart-resilient
- [~] Image survives reboot; delete-data flow on device

## 9. Widget

- [x] One Prayer widget reading the canonical prayer domain (no second calculator)
- [x] RTL via `layoutDirection="locale"`; light/dark via `values`/`values-night`
- [x] No continuous GPS, no network, no polling — one inexact alarm at the next prayer
- [x] Tap opens the Prayer screen through an allow-listed destination token
- [~] Placement, redraw at prayer time, and tap on device

## 10. Release engineering

- [x] Release signing **fails closed** — no silent debug-key fallback
- [x] Keystore backup/recovery policy documented
- [x] No secrets in the repository
- [x] App-level R8 (`isMinifyEnabled` + `isShrinkResources`) retained
- [x] Automated release-graph test: no IhsanPlus Demo/Fake source reachable
- [ ] **Real release R8 build not run** — no keystore available, and signing is now
      deliberately fail-closed. Keep-rule inspection is therefore outstanding.
- [ ] **PRODUCT OWNER DECISION REQUIRED** — production `applicationId`
      (still `com.example.mol`; see `docs/APPLICATION_ID_DEFERRED_DECISION.md`)

## 11. Store listing

- [x] Privacy policy draft (`docs/store/PRIVACY_POLICY.md`) — **pending legal review**
- [x] Data Safety answers (`docs/store/DATA_SAFETY_DISCLOSURE.md`)
- [x] Content rating prepared answers (`docs/store/CONTENT_RATING_NOTES.md`)
- [x] Permissions disclosure (`docs/store/PERMISSIONS_DISCLOSURE.md`)
- [ ] Public privacy-policy URL — needs hosting
- [ ] App icon / adaptive icon final review
- [ ] Feature graphic
- [ ] Screenshots (phone + tablet)
- [ ] Arabic store description final copy
- [ ] Support contact address confirmed
- [ ] Play Console account, developer verification, AAB upload

## 12. Observability

- [x] No third-party crash/analytics SDK; no PII in logs
- [ ] Play Android vitals review after first internal-test upload

---

## Release gate

Release 1 must not ship while any **[ ]** item in sections 1–10 is open. The remaining
**[ ]** items are:

1. Real release R8 build + keep-rule inspection — blocked on a keystore.
2. Production `applicationId` — blocked on an owner decision.
3. Daily Activities customize/reorder — optional scope, explicitly deferred.
4. Store listing assets and Play Console actions — blocked on account access.

All **[~]** items require a physical device or emulator and cannot be simulated.
