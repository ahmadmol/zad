# Part 3 — Product Surface Inventory

**Branch:** `refactor/product-surface-ownership-cleanup`  
**Baseline commit:** `f5271cb` (Part 2 tip; core Part 2 at `c5dec6e`)  
**Date:** 2026-07-18

---

## Baseline commands

| Command | Result |
|---------|--------|
| `git status -sb` | Clean except untracked `.project-preservation/` |
| `git rev-parse --short HEAD` | `f5271cb` |
| `./gradlew :app:assembleDebug` | PASS |
| `./gradlew :feature:testDebugUnitTest` | PASS |

---

## 1. Routes inventory

| Surface | Route constant | Route string | In AppNavHost? | Production callers |
|---------|----------------|--------------|----------------|--------------------|
| Tasbih | `Screen.Tasbih` | `tasbih_screen` | Yes → `TasbihScreen` | Home (`onNavigateToTasbih`), Azkar (`onOpenSebha`), DailyActivityMapper (`tasbih_screen`), GlobalSearch action |
| Sebha | *(none)* | — | No | None |
| Settings | `Screen.Settings` | `settings_screen` | Yes (via `AzkarViewModel`) | Profile `onNavigateToSettings` |
| Statistics | `Screen.Statistics` | `statistics_screen` | Yes (via `AzkarViewModel`) | **No in-app navigate caller found** |
| DonationDetail | `Screen.DonationDetail` | `donation_detail_screen/{id}` | Yes → `DonationDetailScreen` | **None** |
| IhsanDetails | `Screen.IhsanDetails` | `ihsan_details/{id}` | Yes → `IhsanDetailsScreen` | Ehsan list `onDonationClick` |
| Inbox | `Screen.Inbox` | `inbox_screen` | **No composable** | None |

---

## 2. Navigation call sites

| Call | Target |
|------|--------|
| `HomeDashboardScreen` / `AppNavHost` `onNavigateToTasbih` | `Screen.Tasbih.route` |
| `AzkarScreen` `onOpenSebha` | `Screen.Tasbih.route` (already redirected) |
| Daily activity template | `"tasbih_screen"` |
| Profile → Settings | `Screen.Settings.route` |
| Ehsan → details | `Screen.IhsanDetails.createRoute(id)` |
| Inbox | none |
| DonationDetail | none |
| Statistics | none (route registered only) |

---

## 3. Koin ViewModel registrations (`ViewModelModule`)

| ViewModel | Registered | Used by production nav |
|-----------|------------|------------------------|
| `AzkarViewModel` | Yes | Azkar, Settings*, Statistics*, MainActivity dark theme*, GlobalSearch |
| `SebhaViewModel` | Yes | **No** |
| `TasbihViewModel` | Yes | Tasbih route |
| `DonationDetailViewModel` | Yes | DonationDetail route (unreachable) |
| `IhsanDetailsViewModel` | Yes | IhsanDetails route |
| `SettingsViewModel` | **Missing** | — |
| `StatisticsViewModel` | **Missing** | — |

\*Incorrect ownership — Part 3 target to fix.

---

## 4. Screens sharing `AzkarViewModel`

| Screen / host | Usage |
|---------------|--------|
| `AzkarScreen` | List/counter/favorites (correct) |
| `SettingsScreen` | Dark mode, font, vibration, adhan sound (incorrect) |
| `StatisticsScreen` | `last7DaysStats` + `azkarList` daily progress (incorrect) |
| `MainActivity` | `isDarkMode` for `IhsanTheme` (should use Settings owner) |
| `GlobalSearchScreen` | Azkar search (acceptable) |

---

## 5–6. Counter persistence

| Item | Tasbih | Sebha |
|------|--------|-------|
| Storage | Shared Azkar Room (`ZikrEntity`) via `IncrementCounterUseCase` / `ResetCounterUseCase` | Same Room + use cases |
| Category filter | `category == "تسبيح"` | `category.contains("سبح")` (e.g. `"سبحة"`) |
| Daily activity | `DailyActivityIds.TASBEEH` / `incrementDailyActivityCount` | String `"tasbeeh"` on target complete |
| Vibration | `SettingsManager.vibrationEnabledFlow` | Not wired |
| Extra | Select index in memory | CRUD custom sebhas (`EditZikr`/`DeleteZikr`) |
| Same store? | **Yes — same Room DB** | Different category namespace |

Progress for seeded `"تسبيح"` items is shared if both wrote the same rows; custom `"سبحة"` items are Sebha-only and will not appear in Tasbih.

---

## 7. DonationDetail vs IhsanDetails

| Aspect | DonationDetail | IhsanDetails |
|--------|----------------|--------------|
| Data | `Donation` domain via `GetDonationByIdUseCase` | Mapped `IhsanDetailsUi` via `EhsanRepository` |
| Contact call | **No-op** buttons | `ACTION_DIAL` with phone |
| WhatsApp | **No-op** | `api.whatsapp.com` with phone |
| Share | Empty action | Works |
| Error / missing ID | Weak (blank if null) | Explicit Arabic error |
| Trust wording | Shows **"عضو موثق"** (misleading) | **"صاحب الفرصة"** (honest) |
| Production callers | None | Ehsan list |
| Tests | None | None |

**Canonical candidate:** `IhsanDetails`.

---

## 8. Inbox

| Artifact | Present? |
|----------|----------|
| Route `Screen.Inbox` | Yes |
| Composable / screen | **No** |
| Message entity / DAO / repository | **No** |
| Notification → inbox | **No** |
| Nav callers | **No** |
| Product workflow | **None** |

Deferred product scope — remove from production navigation definitions.

---

## 9. Reusable UI components

| Screen | Reused pieces |
|--------|---------------|
| Tasbih | Material3 Scaffold/TopAppBar, progress ring, LazyRow chips, haptic |
| Sebha (legacy) | Material cards, dialogs, counters |
| Settings | Scaffold, Slider, Switch, Ringtone picker, Share button |
| Statistics | Scaffold, Canvas chart (`DailyProgressChart`), Badge rows |
| IhsanDetails | Coil AsyncImage, contact buttons, report dialog |
| DonationDetail | Similar layout; contact stubs |

---

## 10. Settings currently on screen (truthful scope)

`SettingsScreen` today exposes only:

- Font size (`SettingsManager`)
- Dark mode (`SettingsManager`)
- Vibration (`SettingsManager`)
- Adhan sound URI (`UserPreferences`)
- Share app (local Intent, no persistence)

Prayer method/madhhab/notification minutes exist on `SettingsManager` / Part 1 repos but **are not rendered** on this Settings screen. Part 3 must not invent new Settings UI.
