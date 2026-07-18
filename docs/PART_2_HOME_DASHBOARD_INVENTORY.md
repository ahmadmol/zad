# Part 2 — Home Dashboard Inventory (pre-edit)

**Date:** 2026-07-18  
**Branch:** `refactor/home-dashboard-decomposition`  
**Starting tip:** `934bc21` (docs-only commits after Part 1 `cdc1948`)  
**Part 1 commit:** `cdc1948`  
**Working tree:** only untracked `.project-preservation/` (ignored for this work)

---

## 1. Constructor dependencies (current)

| Dependency | Type | Used for |
|------------|------|----------|
| `GetAzkarUseCase` | Use case | Dhikr summary + dailyDuas list |
| `GetAsmaUseCase` | Use case | Daily Asma spotlight |
| `GetDonationsUseCase` | Use case | Charity offer/request counts |
| `UserPreferences` | DataStore | userName, last-read, daily activities |
| `SettingsManager` | DataStore | Prayer settings mutations from Home sheets |
| `PrayerTimesFacade` | Part 1 facade | Next prayer, day, location |
| `QuranRepository` | Repository | Resolve surah name for last-read |
| `Context` | Android | String resources for defaults |

**Count:** 8

---

## 2. Public state (`HomeDashboardData`)

| Field | Source | UI consumer | Persisted? |
|-------|--------|-------------|------------|
| `userName` | UserPreferences | (available; header uses location/time) | Yes |
| `location` | Prayer facade location | DashboardHeader | Derived |
| `currentTime` | Clock ticker in VM | DashboardHeader | Derived |
| `hijriDate` | HijriDateFormatter | DashboardHeader | Derived |
| `nextPrayerName/TimeLeft/progress` | Prayer facade | DashboardHeader | Derived |
| `allPrayers` | Prayer facade day | Header prayer strip + details sheet | Derived |
| `dailyVerse*` | Hardcoded constants | Verse card | Constant |
| `spotlightAllah*` | AsmaTodayResolver | Spotlight card | Derived |
| `dailyZikr*` | GetAzkarUseCase aggregate | Progress card | Derived |
| `community*Count` | GetDonationsUseCase (+ hardcode 12 volunteers) | Community card | Derived / constant |
| `dailyDuas` | Azkar take(5) | LazyRow | Derived |
| `dailyActivities` | UserPreferences + templates | DailyActivityCard | Persisted counts |
| `lastRead*` | UserPreferences + QuranRepository | LastReadCard | Persisted |
| `selectedPrayerIndex` / sheet flags | Local UI | Bottom sheets | Ephemeral |

Global `isLoading` / `error` wrap azkar+asma+donations+name combine — **one failure clears whole block**.

---

## 3. Public actions

Refresh, prayer sheet UX, settings mutations via SettingsManager, city selection, daily activity increment.

---

## 4. Duplication / ownership issues

- Daily rollover + templates live in Home VM (should be DailyActivityRepository).
- Asma selection already in `AsmaTodayResolver` but defaults via Context.
- Prayer settings still mutate SettingsManager from Home (should use prayer settings use case).
- Quran last-read name resolution hits QuranRepository directly.
- Single combine means azkar failure hides asma/charity/profile.

---

## 5. Out of scope confirmed

No IhsanPlus, no new Home screen, no prayer calc changes, no module split.
