# Ihsan Sensitive Files and Risk Areas

## 1. Database Files
`IhsanDatabase.kt`, entities, DAOs, and migrations are high risk. Changes can cause data loss or app crashes during migration.

## 2. Navigation Files
`Screen.kt` and `AppNavHost.kt` are high risk. Modifications can break the entire app navigation flow.

## 3. App Startup Files
`MainActivity.kt` and `IhsanApp` (Application class) are high risk. Changes here affect the app's initialization and startup performance.

## 4. Dependency Injection Files
Existing Koin modules (e.g., `DatabaseModule.kt`) are high risk. Incorrect changes can lead to runtime crashes due to missing dependencies.

## 5. Quran Import Files
`QuranAssetLoader.kt` and JSON import logic are high risk. They handle large data ingestion which is critical for the Quran feature.

## 6. Prayer and Notification Files
`PrayerNotificationScheduler.kt`, receivers, and WorkManager files are high risk. They manage background tasks and system alarms.

## 7. Qibla Files
`QiblaManager.kt` and related sensor math are high risk. They rely on hardware sensors and complex calculations.

## 8. Design System Files
Theme tokens and shared components in `:designsystem` are high risk. Changes can cause UI regressions across the entire app.

## 9. Safe Strategy
Always use isolated packages for new logic and document integration needs in `docs/APPEND_ONLY_INTEGRATION_REQUEST.md`.
