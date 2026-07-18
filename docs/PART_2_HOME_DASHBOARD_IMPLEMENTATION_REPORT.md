# Part 2 — Home Dashboard Decomposition Implementation Report

**Date:** 2026-07-18  
**Branch:** `refactor/home-dashboard-decomposition`

---

## 1. Starting branch and commit

| Item | Value |
|------|--------|
| Base branch | `refactor/unified-prayer-domain` |
| Part 1 commit | `cdc1948` |
| Actual tip used | `934bc21` (docs-only commits after `cdc1948`) |
| Working tree | Untracked `.project-preservation/` only (not included) |

---

## 2. Final branch and commit

| Item | Value |
|------|--------|
| Branch | `refactor/home-dashboard-decomposition` |
| Final commit | *(filled after commit)* |

---

## 3. Baseline build/test results

| Check | Result |
|-------|--------|
| Initial assemble/test (before edits) | **Failed** — Gradle distribution download socket timeout (network), not a code failure |
| Later compile / assemble / unit tests | **PASS** with `GRADLE_USER_HOME=C:\Users\WIN 10\.gradle` |

---

## 4–7. ViewModel responsibilities

### Original constructor (8 deps)
`GetAzkarUseCase`, `GetAsmaUseCase`, `GetDonationsUseCase`, `UserPreferences`, `SettingsManager`, `PrayerTimesFacade`, `QuranRepository`, `Context`

### Original responsibilities
Combined azkar/asma/charity/profile; daily activity rollover; Quran last-read; prayer facade mapping; settings mutations; Context string defaults; global loading/error.

### Final constructor
`ObserveHomeProfile/Prayer/Quran/DailyActivities/Dhikr/Asma/Charity` use cases, `RefreshHomeDashboardUseCase`, `UpdatePrayerSettingsUseCase`, `PrayerLocationRepository`, `DailyActivityRepository`

### Final responsibilities
Thin coordinator: combine section flows, clock/hijri ephemeral UI, refresh, map actions to feature contracts. No Context, no SettingsManager, no DAOs, no prayer calculation/scheduling.

---

## 8–10. Files

### Added
- `dashboard/domain/model/HomeDashboardModels.kt`, `DailyActivityMapper.kt`
- `dashboard/domain/repository/DailyActivityRepository.kt`
- `dashboard/domain/usecase/HomeDashboardUseCases.kt`
- `dashboard/data/DailyActivityRepositoryImpl.kt`
- `dashboard/di/HomeDashboardModule.kt`
- `docs/PART_2_HOME_DASHBOARD_INVENTORY.md`, this report
- `feature/src/test/.../HomeDashboardDecompositionTest.kt`

### Modified
- `HomeDashboardViewModel.kt`, `HomeDashboardUiState.kt`
- `AppModule.kt`, `ViewModelModule.kt`
- `PrayerSettingsRepository` (+ sound type), impl, `UpdatePrayerSettingsUseCase`
- Prayer reconcile fake test (new interface method)

### Removed
None (legacy util calculator already deprecated in Part 1).

---

## 11–12. Read models & use cases

Read models: Profile, Prayer, Quran, DailyActivity, Dhikr, Asma, Charity (+ `HomeSectionState`).  
Use cases: Observe* for each section + `RefreshHomeDashboardUseCase`.

---

## 13. Daily-activity ownership

**Before:** Home VM + raw `UserPreferences` keys + templates.  
**After:** `DailyActivityRepository` / `DailyActivityMapper` with clock-aware rollover; Home only calls `increment`.

---

## 14–15. Error isolation & refresh

Each observer has `.catch → HomeSectionState.Error`. Sections combine independently; one failure does not clear others.  
`RefreshHomeDashboardUseCase` resets daily activities if needed and calls `prayerFacade.refreshLocation()` (Part 1 path; no direct AlarmManager).

---

## 16. DI

`homeDashboardModule` registers repository, use cases, and `HomeDashboardViewModel`. Removed duplicate `viewModelOf(::HomeDashboardViewModel)` from `viewModelModule`.

---

## 17–18. UI & navigation

`HomeDashboardScreen` unchanged visually; consumes `uiState.data` compatibility projection from section state. No IhsanPlus routes. Existing nav callbacks preserved.

---

## 19. Part 1 regression

Prayer unit tests still pass. Home prayer section uses `PrayerTimesFacade` only. No location/scheduling APIs in Home VM. No silent city fallback reintroduced.

---

## 20. Tests added

- Daily activity mapping / id validation  
- Section state isolation  
- AsmaTodayResolver characterization  

---

## 21. Commands executed

```text
git checkout -b refactor/home-dashboard-decomposition
.\gradlew.bat :feature:compileDebugKotlin :app:compileDebugKotlin --no-daemon
.\gradlew.bat :feature:testDebugUnitTest --tests com.example.feature.dashboard.* --tests com.example.feature.prayer.* :app:assembleDebug --no-daemon
```

---

## 22. Lint

Not run in final pass (memory/time). Do **not** claim lint passed.

---

## 23. Device/emulator

**Not run.**

---

## 24. Remaining risks

- Compatibility `data` getter rebuilds projection each read (acceptable; consider memoization if profiling shows cost).
- Charity empty listings show Empty section (offers/requests 0) — community card may hide until data exists; previous code showed zeros with hardcoded volunteers when donations empty via same combine — verify UI still acceptable.
- `activeVolunteersCount = 12` remains hardcoded (unchanged product behavior).

---

## 25. Deferred

Compose UI tests, full lint, instrumented suite, Part 3.

---

## 26. Rollback

```bash
git checkout refactor/unified-prayer-domain
# or reset to 934bc21 / cdc1948
```

---

## 27. Acceptance checklist

| Criterion | Status |
|-----------|--------|
| Thin coordinator ViewModel | **Yes** |
| No Context / SettingsManager / DAO in Home VM | **Yes** |
| No prayer calc/schedule in Home | **Yes** |
| Section isolation | **Yes** |
| Daily activity boundary | **Yes** |
| Existing Home screen preserved | **Yes** |
| No IhsanPlus | **Yes** |
| assembleDebug | **PASS** |
| Dashboard + prayer unit tests | **PASS** |
| Device verification | **No** |
| Lint | **Not run** |
