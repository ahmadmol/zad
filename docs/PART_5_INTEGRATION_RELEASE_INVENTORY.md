# Part 5 — Integration & Release Inventory

**Branch:** `chore/integration-release-readiness`  
**Baseline commit:** `aa8196d`  
**Date:** 2026-07-18

---

## Baseline

| Command | Result |
|---------|--------|
| `git status -sb` | Clean except untracked `.project-preservation/` |
| HEAD | `aa8196d` |
| `:app:assembleDebug` | PASS |
| `:designsystem:testDebugUnitTest` | PASS |
| `:feature:testDebugUnitTest` | PASS |
| `:app:testDebugUnitTest` | PASS |
| `:app:lintDebug` | PASS |
| `connectedDebugAndroidTest` | Not run (no device claimed) |

---

## 1. IhsanPlus packages

| Area | Path | Notes |
|------|------|-------|
| Daily | `ihsanplus/daily/**` | Screen, VM, fake DS, module |
| Prayer assist | `ihsanplus/prayerassist/**` | Screen, fake DS, no DI module file found separately |
| Charity trust | `ihsanplus/charitytrust/**` | Screen, fake DS, module with fake verification claims |
| Integration | `ihsanplus/integration/IhsanPlusIntegrationNotes.kt` | Notes only |

**Not in production navigation or `appModule`.**

---

## 2. Fake / demo data

| Class | Classification |
|-------|----------------|
| `DemoIhsanPlusDailyDataSource` | Demo |
| `DemoIhsanPlusDailyRepository` | Demo wrapper |
| `DemoIhsanPlusPrayerAssistDataSource` | Demo |
| `DemoIhsanPlusCharityTrustDataSource` | Demo + fabricated trust/verification |
| `DemoIhsanPlusCharityTrustRepository` | Demo wrapper |

Modules `ihsanPlusDailyModule` / `ihsanPlusCharityTrustModule` remain **unregistered** in production `appModule`.

---

## 3–6. Navigation / DI

- Production routes: Home, Prayer, Tasbih, Settings, Statistics, IhsanDetails, etc. (Part 3 cleaned Inbox)
- `appModule` includes: core, database, repository, useCase, viewModel, quran, prayerDomain, homeDashboard
- **No** IhsanPlus modules in `includes`
- IhsanPlus screens use `koinViewModel()` but modules are **not** registered — unreachable without wiring

---

## 7–10. Build / identity / signing

| Item | Value |
|------|--------|
| applicationId | `com.example.mol` |
| namespace | `com.example.mol` |
| Build types | debug (default), release |
| minifyEnabled (release) | **false** |
| shrinkResources | not set |
| Signing | default debug; no release signing config |
| CI | **None** (no `.github/workflows`) |

---

## 11–13. Tests / Room

- Unit tests: designsystem, feature, app
- Room: `IhsanDatabase` version **5**
- Migrations: `MIGRATION_2_3`, `MIGRATION_3_5` (4 never existed)
- Schema export: `feature/schemas/.../5.json`
- `fallbackToDestructiveMigration`: not used in DatabaseModule
- Migration instrumented tests: limited / matrix documented in Part 5

---

## 14–16. Manifest / permissions / backup

| Component | Exported | Notes |
|-----------|----------|-------|
| MainActivity | true | Launcher |
| QuranAudioService | **false** | mediaPlayback FGS |
| PrayerNotificationReceiver | false | |
| PrayerSystemReconciliationReceiver | false | BOOT/TIME intents |
| allowBackup | **false** | |
| Permissions | INTERNET, LOCATION, FGS media, POST_NOTIFICATIONS, BOOT, EXACT_ALARM | |

---

## 17. Logging / crash reporting

- Ad-hoc Android Log / exceptions in places
- No vendor crash reporter
- Part 5 adds privacy-safe observability contracts (no-op release)

---

## 18. Release blockers (documented)

1. Application ID still `com.example.mol` (unapproved production identity)
2. Release signing not configured
3. R8/minify disabled on release
4. IhsanPlus fake trust data exists in tree (isolated, must stay unwired)
5. Device/emulator verification not claimed for Part 5
6. Privacy policy / Play listing assets outside code scope
