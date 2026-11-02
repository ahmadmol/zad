# Part 1 — Unified Prayer Domain Inventory (pre-edit)

**Date:** 2026-07-18  
**Starting branch:** `fix/phase1-audio-auth-honesty`  
**Working branch:** `refactor/unified-prayer-domain`  
**Starting HEAD:** `c02a10d`

---

## 1. Duplicated calculation / location paths

| Consumer | Calculation | Location | Fallback | Scheduling |
|----------|-------------|----------|----------|------------|
| `PrayerViewModel` | `PrayerCalculator.calculate` + Adhan enums | FusedLocation + Geocoder | Silent Aleppo `36.2021, 37.1343` | None (countdown only) |
| `HomeDashboardViewModel` | Same util + yesterday/today/tomorrow | FusedLocation + Geocoder | Falls back to manual coords (default Aleppo) | `PrayerNotificationScheduler.schedulePrayerNotifications` |
| `AdhanWorker` | `PrayerCalculator.calculate` | `UserPreferences` lat/lng | Silent Riyadh `24.7136, 46.6753` | WorkManager one-shots |
| `PrayerNotificationScheduler` | N/A (consumes `PrayerTime`) | N/A | N/A | AlarmManager exact alarms |
| `PrayerNotificationReceiver` | N/A | N/A | N/A | Shows notification only |

---

## 2. Shared util / persistence to reuse

- `feature/.../prayer/util/PrayerCalculator.kt` — Adhan2 wrapper (to become `AdhanPrayerCalculator`)
- `feature/.../prayer/PrayerTime` UI model in `PrayerScreen.kt`
- `SettingsManager` DataStore keys: method, madhab, auto/manual location, pre-prayer, iqamah, sound
- `PrayerNotificationScheduler` + `PrayerNotificationReceiver`
- `City` list for manual city selection (`domain/model/City.kt`)
- Existing `PrayerScreen`, Home next-prayer card / `DashboardHeader`
- Koin: `coreModule` registers `PrayerNotificationScheduler`; `viewModelOf(::PrayerViewModel)`

---

## 3. Manifest / system

- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` unrelated (Quran)
- `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`
- `ACCESS_FINE/COARSE_LOCATION`
- Receiver: `PrayerNotificationReceiver` `exported=false`
- No BOOT_COMPLETED receiver currently registered for prayer reconciliation

---

## 4. Fixed-city behavior (must eliminate as silent calc fallback)

- ViewModels init last/manual to Aleppo
- `SettingsManager` default manual lat/lng = Aleppo; default city name `"حلب"`
- Geocoder failure labels `"حلب، سوريا"`
- `AdhanWorker` defaults to Riyadh when prefs null
- `City` list includes Aleppo as a **manual selectable city** (allowed as explicit Manual source, not silent Device fallback)

---

## 5. Out of scope (do not touch)

IhsanPlus prayerassist, charity, Quran audio, Tasbih/Sebha, Inbox, applicationId, module split.
