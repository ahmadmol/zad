# Part 3 — Product Surface Implementation Report

**Date:** 2026-07-18  
**Branch:** `refactor/product-surface-ownership-cleanup`

---

## 1. Starting branch and commit

| Item | Value |
|------|--------|
| Base branch | `refactor/home-dashboard-decomposition` |
| Expected Part 2 commit | `c5dec6e` |
| Actual tip used | `f5271cb` (docs-only after `c5dec6e`, same pattern as Part 2) |
| Working tree at start | Untracked `.project-preservation/` only (not committed) |

---

## 2. Final branch and commit

| Item | Value |
|------|--------|
| Branch | `refactor/product-surface-ownership-cleanup` |
| Final commit | `4261981` |

---

## 3. Baseline build/test results

| Check | Result |
|-------|--------|
| `./gradlew :app:assembleDebug` | **PASS** |
| `./gradlew :feature:testDebugUnitTest` | **PASS** |

---

## 4. Counter decision

**Tasbih** is the sole production counter surface. Sebha is legacy (unreachable route; removed).

See `docs/PART_3_COUNTER_SURFACE_DECISION.md`.

---

## 5–6. Counter ownership / compatibility

| | Before | After |
|--|--------|-------|
| Production route | `tasbih_screen` | `tasbih_screen` (unchanged) |
| Mutation owner | `TasbihViewModel` (+ unused `SebhaViewModel`) | `TasbihViewModel` only |
| Persistence | Azkar Room | Same Azkar Room (`تسبيح` rows preserved) |
| Legacy Sebha | Code + Koin, no nav | Removed |

Custom `"سبحة"` rows remain in Room but are not shown in Tasbih (no data deletion).

---

## 7. Settings ownership

| | Before | After |
|--|--------|-------|
| Screen state owner | `AzkarViewModel` | `SettingsViewModel` |
| Persistence | `SettingsManager` + `UserPreferences` | Same (no duplicate store) |
| Theme host (`MainActivity`) | `AzkarViewModel.isDarkMode` | `SettingsViewModel.isDarkMode` |
| Prayer calculation / alarms | N/A on this screen | Still not performed |

Existing Settings UI reused (font, dark mode, vibration, adhan sound, share). No invented prayer Settings rows.

---

## 8. Statistics ownership

| | Before | After |
|--|--------|-------|
| Screen state owner | `AzkarViewModel` | `StatisticsViewModel` |
| Data path | `GetLast7DaysStatsUseCase` + azkar list inside Azkar VM | `StatisticsRepository` → AzkarRepository (read-only) |
| Mutations | Coupled to Azkar actions | Read-only (`Retry` no-op) |

Existing chart / total / daily list UI reused.

---

## 9. Donation-details decision

**Canonical:** `IhsanDetails`.  
Legacy `DonationDetail` UI removed; `donation_detail_screen/{id}` redirects to `ihsan_details/{id}`.

See `docs/PART_3_DONATION_DETAIL_DECISION.md`.

---

## 10. Inbox decision

Inbox was unimplemented. Route removed. Deferred until a real messaging contract exists.

See `docs/PART_3_INBOX_SCOPE_DECISION.md`.

---

## 11. Routes

| Route | Change |
|-------|--------|
| `tasbih_screen` | Retained (sole counter) |
| Sebha | Never had a route; code removed |
| `settings_screen` | Owner changed to `SettingsViewModel` |
| `statistics_screen` | Owner changed to `StatisticsViewModel` |
| `ihsan_details/{id}` | Canonical details |
| `donation_detail_screen/{id}` | Redirect-only (`LegacyDonationDetail`) |
| `inbox_screen` | Removed |
| IhsanPlus | None added |

---

## 12. Koin changes

**Added:** `SettingsViewModel`, `StatisticsViewModel`, `StatisticsRepository` → `StatisticsRepositoryImpl`  
**Removed:** `SebhaViewModel`, `DonationDetailViewModel`  
**Unchanged:** `TasbihViewModel`, `IhsanDetailsViewModel`, `AzkarViewModel` (slimmed), Home/Prayer modules

---

## 13–15. Files

### Added
- `settings/presentation/SettingsUiState.kt`, `SettingsViewModel.kt`
- `statistics/domain/StatisticsRepository.kt`
- `statistics/data/StatisticsRepositoryImpl.kt`
- `statistics/presentation/StatisticsUiState.kt`, `StatisticsViewModel.kt`
- Docs: inventory, counter/donation/inbox decisions, this report
- Tests under `feature/.../settings|statistics|tasbih|surface|ehsan` and `app/.../navigation`

### Modified
- `SettingsScreen.kt`, `StatisticsScreen.kt`
- `AzkarViewModel.kt`, `AzkarScreenState.kt`
- `AppNavHost.kt`, `Screen.kt`, `MainActivity.kt`
- `ViewModelModule.kt`, `RepositoryModule.kt`

### Removed
- `SebhaScreen.kt`, `SebhaViewModel.kt`
- `DonationDetailScreen.kt`, `DonationDetailViewModel.kt`

---

## 16. UI components reused

Material Scaffold/TopAppBar, Settings Slider/Switch/Ringtone picker/Share button, Statistics Canvas chart/Badge rows, Tasbih counter controls, IhsanDetails contact buttons and layout.

---

## 17. Tests added

- `ProductSurfaceCharacterizationTest`
- `ProductSurfaceNavigationCharacterizationTest`
- `SettingsViewModelTest`
- `StatisticsViewModelTest`
- `TasbihViewModelTest`
- `IhsanDetailsViewModelTest`

Part 1/2 existing tests retained (`ReconcilePrayerScheduleUseCaseTest`, `HomeDashboardDecompositionTest`, etc.).

---

## 18. Commands executed

```bash
./gradlew :app:assembleDebug
./gradlew :feature:testDebugUnitTest
./gradlew :app:testDebugUnitTest
```

---

## 19–20. Part 1 / Part 2 regression

| Area | Result |
|------|--------|
| Prayer unit tests | Included in `:feature:testDebugUnitTest` |
| Home dashboard tests | Included in `:feature:testDebugUnitTest` |
| Home thin coordinator | Untouched |
| Prayer facade / reconciliation | Untouched |
| IhsanPlus DI/routes | None added |

---

## 21. Lint

| Check | Result |
|-------|--------|
| `./gradlew :app:lintDebug --no-parallel` | **PASS** (HTML report written) |
---

## 22. Device/emulator

**Not executed.** No connected device/emulator verification claimed.

---

## 23. Remaining risks

- Custom Sebha (`سبحة`) Room rows are orphaned from UI (data retained).
- Statistics route has no in-app entry point (pre-existing).
- Legacy donation redirect should be removed once deep-link compatibility is confirmed unused.
- Settings screen still does not expose Part 1 prayer method/madhab UI (honest scope; not invented).

---

## 24. Deferred work

- Inbox / messaging
- IhsanPlus wiring
- Prayer settings rows on Settings (if product wants them)
- Sebha custom-bead CRUD in Tasbih (if product wants it)
- Part 4 and beyond

---

## 25. Rollback

```bash
git checkout refactor/home-dashboard-decomposition
# or revert commits on refactor/product-surface-ownership-cleanup
```

---

## 26. Acceptance checklist

| Criterion | Status |
|-----------|--------|
| One production counter route | Pass |
| One counter ViewModel mutations | Pass |
| One persistence source | Pass |
| Progress preserved for `تسبيح` | Pass |
| Legacy Sebha removed | Pass |
| Settings uses SettingsViewModel | Pass |
| Settings not AzkarViewModel | Pass |
| Statistics uses StatisticsViewModel | Pass |
| Statistics read-only / real data | Pass |
| Canonical IhsanDetails | Pass |
| Legacy donation redirect | Pass |
| Inbox removed | Pass |
| No IhsanPlus | Pass |
| assembleDebug | Pass |
| Unit tests | Pass |
| Device verification | Not run |
| Screens reused | Pass |
