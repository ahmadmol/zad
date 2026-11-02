# Android 16 / API 36 Compatibility

**Scope:** Release 1 · **Module:** `:app` (`compileSdk = 36`, `targetSdk = 36`)
**Status:** code-complete; the items marked *runtime* need a device or emulator.

---

## 1. SDK level

| Setting | Value | Verified |
|---------|-------|----------|
| `compileSdk` | 36 | Yes — `:app:assembleDebug` green |
| `targetSdk` | 36 | Yes |
| `minSdk` | see `app/build.gradle.kts` | unchanged by Section 2 |
| AGP | 8.7.3 | Emits an informational "newer AGP recommended" note for `compileSdk 36`; not a failure |

No library in `gradle/libs.versions.toml` required a version bump for API 36.

---

## 2. Behaviour changes reviewed

### 2.1 Edge-to-edge is enforced

Android 15+ makes edge-to-edge mandatory for `targetSdk >= 35`; API 36 keeps it.

* `MainActivity` calls `enableEdgeToEdge()`.
* `MainScreen` consumes `WindowInsets.statusBars` explicitly and paints only the
  status-bar inset, so the bottom navigation keeps its own inset handling.
* **RUNTIME VERIFICATION REQUIRED:** visual check that no content is clipped behind the
  status or navigation bars on a gesture-nav device and a 3-button-nav device.

### 2.2 Predictive back

Predictive back is on by default at `targetSdk = 36`.

* Navigation is Compose Navigation 2.8.5 (`NavHost`), which is predictive-back compatible.
* No custom `OnBackPressedCallback` intercepts the root back gesture.
* **RUNTIME VERIFICATION REQUIRED:** back-swipe from a child screen shows the predictive
  animation and lands on the correct parent; back from a root tab exits cleanly.

### 2.3 Exact alarms

* Prayer alarms use `setExactAndAllowWhileIdle` and the app already surfaces
  `PrayerAlarmPermissionState` (`GrantedExact` / `InexactOnly`) in the Prayer screen's
  system-status section, with reconciliation on permission change.
* The **Phase 7 widget deliberately uses an inexact alarm** (`AlarmManager.set`), so it
  does not consume the exact-alarm budget.
* **RUNTIME VERIFICATION REQUIRED:** revoke and re-grant "Alarms & reminders" and confirm
  the schedule reconciles.

### 2.4 Notification permission

`POST_NOTIFICATIONS` is requested at Home. Prayer alerts degrade to no-notification
rather than crashing when denied, and reconciliation cancels alarms when notifications
are disabled.

### 2.5 Foreground service types

`QuranAudioService` declares `android:foregroundServiceType="mediaPlayback"` and is
`exported="false"`. This satisfies the API 34+ typed-FGS requirement, unchanged at 36.

### 2.6 16 KB page size

`:app`, `:feature` and `:designsystem` ship **no native `.so` libraries**
(`mergeDebugNativeLibs` is `NO-SOURCE`). All dependencies — `play-services-location`,
`adhan2`, `media3`, Room, Koin — are Java/Kotlin only.

**Verdict: NOT APPLICABLE.** There is no native code to re-align. If a native dependency
is ever added, this must be re-evaluated before release.

### 2.7 Broadcast receivers

`PrayerNotificationReceiver` and `PrayerSystemReconciliationReceiver` are
`exported="false"`. The Phase 7 `PrayerWidgetProvider` is necessarily `exported="true"`
(the launcher host must reach it) but reads only local state and never trusts data from
the incoming intent; the widget's tap intent maps a fixed destination token onto an
allow-listed internal route rather than accepting a raw route string.

---

## 3. RTL and Arabic

* `android:supportsRtl="true"`.
* Screens wrap content in `CompositionLocalProvider(LocalLayoutDirection provides Rtl)`.
* The widget layout uses `layoutDirection="locale"` with `start`/`end` padding and
  `textAlignment="viewStart"`, so it mirrors without an `ldrtl` variant.
* **RUNTIME VERIFICATION REQUIRED:** Arabic rendering and numeral shaping at font scales
  1.0 / 1.15 / 1.3 / 1.5.

---

## 4. Outstanding runtime acceptance

None of the following can be simulated; all require a device or emulator running
Android 16:

1. Edge-to-edge visual pass.
2. Predictive back animation and destination correctness.
3. Exact-alarm permission revoke/grant reconciliation.
4. Notification permission revoke/grant.
5. Alarm firing after reboot and after package replace.
6. Widget placement, redraw at the next prayer, and tap-to-Prayer.
7. Database migration 6 → 7 on a real upgraded install.
