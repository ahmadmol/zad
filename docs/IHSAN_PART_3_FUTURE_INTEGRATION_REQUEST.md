# Ihsan Part 3: Future Integration Request

This document outlines the steps required to integrate the Part 3 foundation into the main application. These steps are NOT implemented yet and serve as a roadmap for a later part.

## 1. Navigation Integration
- Append a new route to `Screen.kt`: `PrayerAssist`.
- Add a `composable` destination in `AppNavHost.kt`.
- Add an entry point button in `HomeDashboardScreen.kt` or `PrayerScreen.kt`.

## 2. Dependency Injection
- Create `IhsanPlusPrayerAssistModule.kt`.
- Register it in `AppModule.kt`.
- Connect real repositories to `GetIhsanPlusPrayerAssistDashboardUseCase`.

## 3. Data Integration
- Update `GetIhsanPlusPrayerAssistDashboardUseCase` to fetch data from:
    - `LocationManager` for real location.
    - `SettingsManager` for calculation methods.
    - `PrayerNotificationScheduler` for current alert status.

## 4. Notification Scheduling
- Update `PrayerNotificationScheduler.kt` to handle:
    - Pre-prayer reminders (X minutes before).
    - Iqama reminders (X minutes after Adhan).
    - Custom notification channels for different reminder types.

## 5. Qibla Sensor Connection
- Connect `IhsanPlusQiblaAssistCard` to the existing `QiblaManager` to show real-time compass data and sensor accuracy levels.

## 6. Permissions
- Ensure `POST_NOTIFICATIONS` and `ACCESS_FINE_LOCATION` are handled correctly at the app level when these features are activated.
