# Part 1 — Unified Prayer Domain Implementation Report

**Date:** 2026-07-18  
**Phase:** Unified prayer calculation, location, next-prayer, and schedule reconciliation

---

## 1. Starting branch and commit

| Item | Value |
|------|--------|
| Started from | `fix/phase1-audio-auth-honesty` |
| Starting HEAD | `c02a10d` |
| Working branch | `refactor/unified-prayer-domain` |

---

## 2. Final branch and commit

| Item | Value |
|------|--------|
| Final branch | `refactor/unified-prayer-domain` |
| Final commit | cdc1948 |

---

## 3. Baseline build / test results

| Command | Result | Notes |
|---------|--------|-------|
| Initial `:app:assembleDebug` + unit tests (before edits completed) | **FAILED** mid-run | Kotlin daemon / compile race while files were still being written; not attributed to final code |
| `:feature:testDebugUnitTest --tests com.example.feature.prayer.*` | **PASS** (EXIT=0) | After architecture landed |
| `:app:assembleDebug` | **PASS** (EXIT=0) | After architecture landed |
| `:app:lintDebug` | Not re-run in final pass (memory pressure) | Deferred |
| `connectedDebugAndroidTest` | **Not run** | No emulator/device session in this environment |

---

## 4. Files added

### Domain
- `feature/.../prayer/domain/model/PrayerDomainModels.kt`
- `feature/.../prayer/domain/calculator/PrayerCalculator.kt`
- `feature/.../prayer/domain/calculator/NextPrayerSelector.kt`
- `feature/.../prayer/domain/clock/PrayerClock.kt`
- `feature/.../prayer/domain/repository/*` (location, settings, alarm, events)
- `feature/.../prayer/domain/usecase/PrayerObservationUseCases.kt`
- `feature/.../prayer/domain/usecase/ReconcilePrayerScheduleUseCase.kt`
- `feature/.../prayer/domain/facade/PrayerTimesFacade.kt`
- `feature/.../prayer/domain/scheduler/PrayerScheduleBuilder.kt`

### Data
- `feature/.../prayer/data/calculator/AdhanPrayerCalculator.kt`
- `feature/.../prayer/data/location/PrayerLocationRepositoryImpl.kt`
- `feature/.../prayer/data/settings/PrayerSettingsRepositoryImpl.kt`
- `feature/.../prayer/data/scheduler/AndroidPrayerAlarmGateway.kt`
- `feature/.../prayer/data/diagnostics/DataStorePrayerEventRepository.kt`

### DI / workers / docs / tests
- `feature/.../prayer/di/PrayerDomainModule.kt`
- `feature/.../prayer/worker/PrayerSystemReconciliationReceiver.kt`
- `docs/PART_1_UNIFIED_PRAYER_INVENTORY.md`
- `docs/PART_1_UNIFIED_PRAYER_IMPLEMENTATION_REPORT.md`
- Characterization + calculator + reconciliation unit tests under `feature/src/test/.../prayer/`

---

## 5. Files modified

- `PrayerViewModel.kt` — facade-only; no FusedLocation/Geocoder/Adhan
- `PrayerUiState.kt` / `PrayerScreen.kt` — location source + compact system status
- `HomeDashboardViewModel.kt` / `HomeDashboardUiState.kt` — consume `PrayerTimesFacade`
- `AdhanWorker.kt` — reconciles via use case (no Riyadh fallback calc)
- `AppModule.kt` — includes `prayerDomainModule`
- `AndroidManifest.xml` — boot/time/timezone receiver + `RECEIVE_BOOT_COMPLETED`
- `util/PrayerCalculator.kt` — marked `@Deprecated`

---

## 6. Files removed

None (legacy util calculator retained for characterization; scheduler class retained but unused by Home).

---

## 7. Architecture before / after

**Before:** Duplicated Adhan + location in `PrayerViewModel` and `HomeDashboardViewModel`; Home scheduled alarms; `AdhanWorker` calculated with Riyadh fallback; silent Aleppo defaults.

**After:** Single `AdhanPrayerCalculator` + `PrayerTimesFacade` + `ReconcilePrayerScheduleUseCase` + `AndroidPrayerAlarmGateway`. Home and Prayer observe the same facade flows.

---

## 8. Duplicate logic removed

- Home no longer owns Adhan, FusedLocation, Geocoder, next-prayer selection, or alarm scheduling
- Prayer screen no longer owns Adhan/location infrastructure
- AdhanWorker no longer independently calculates times with fixed-city fallback

---

## 9. Location fallback before / after

| Before | After |
|--------|--------|
| Silent Aleppo `36.2021, 37.1343` in VMs | `Unavailable` when auto and no device/saved fix |
| Geocoder failure labeled Aleppo | Generic “موقعك الحالي” / unavailable messaging |
| AdhanWorker → Riyadh defaults | Worker only reconciles; no silent city calc |
| Manual city list may still include Aleppo | Allowed only as explicit **Manual** source |

---

## 10. Scheduling reconciliation design

`ReconcilePrayerScheduleUseCase`:
1. Read location, settings, permissions  
2. Skip/cancel when location unavailable or notifications denied  
3. Calculate today+tomorrow via shared calculator  
4. Build schedule with stable IDs  
5. `replaceSchedule` (idempotent fingerprint)  
6. Record operational events (no coordinates)  
7. Publish `PrayerSystemStatus`

Reasons: ApplicationStart, SettingsChanged, LocationChanged, DeviceBooted, TimeChanged, TimezoneChanged, AppUpdated, ManualRetry, permission reasons.

---

## 11. Alarm identity strategy

`stableBaseId = PrayerName.english.hashCode() + (dayOfYear * 10)`  
Offsets: PRE +10000, EXACT +20000, IQAMAH +30000, END +40000  
No random request codes.

---

## 12. DI changes

`prayerDomainModule` registers calculator, repositories, gateway, events, use cases, facade. Included from `appModule`. IhsanPlus modules **not** registered.

---

## 13. Reused UI components

Existing `PrayerScreen`, Home next-prayer / dashboard header, Material3 cards/buttons/icons. Compact collapsible status section added to Prayer screen only.

---

## 14. Tests added

- `PrayerCalculatorCharacterizationTest`
- `AdhanPrayerCalculatorTest`
- `NextPrayerSelectorTest`
- `ReconcilePrayerScheduleUseCaseTest` (idempotency, location change, unavailable, notifications denied)

---

## 15. Commands executed

```text
git status -sb
git rev-parse --short HEAD
git checkout -b refactor/unified-prayer-domain
.\gradlew.bat :feature:testDebugUnitTest --tests "com.example.feature.prayer.*" --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon
```

---

## 16. Device verification results

**Not executed** — no emulator/physical device verification in this session. Items 1–20 from the order remain pending for a device soak.

---

## 17. Remaining risks

- Kotlin daemon OOM / flaky compile on this machine under memory pressure
- Legacy `PrayerNotificationScheduler` still in DI but unused by Home (dead path risk if something else calls it)
- SettingsManager still defaults manual lat/lng to Aleppo values when user switches to manual without choosing a city
- Exact-alarm inexact fallback needs real-device confirmation
- Facade auto-reconciles on every settings/location emission (idempotent, but busy on rapid changes)

---

## 18. Deferred work

- Full lint clean run
- Instrumented / device matrix from §17
- Delete or fully retire `PrayerNotificationScheduler` after confirmation
- Part 2+ of improvement plan (not started)
- IhsanPlus integration (still blocked)

---

## 19. Rollback procedure

```bash
git checkout fix/phase1-audio-auth-honesty
# or
git reset --hard c02a10d
```

Uninstall app builds from this branch before testing older alarm IDs if needed.

---

## 20. Acceptance checklist

| Criterion | Status |
|-----------|--------|
| One calculation implementation for Home + Prayer | **Yes** (`AdhanPrayerCalculator` via facade) |
| One location abstraction | **Yes** |
| One settings source (`SettingsManager`) | **Yes** |
| One next-prayer selection | **Yes** (`NextPrayerSelector`) |
| One reconciliation path | **Yes** |
| No silent fixed-city calculation fallback | **Yes** (Unavailable instead) |
| Domain Android-independent | **Yes** (models/calculator/use cases) |
| Existing Prayer screen preserved | **Yes** |
| Existing Home card preserved | **Yes** |
| No IhsanPlus fake wiring | **Yes** |
| Build passes | **Yes** (assembleDebug) |
| Prayer unit tests pass | **Yes** |
| Device verification | **No** (documented) |

**CONTROLLED INTEGRATION / IhsanPlus remains blocked.** Part 2 not started.
