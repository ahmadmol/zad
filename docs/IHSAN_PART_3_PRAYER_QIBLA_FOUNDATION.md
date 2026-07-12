# Ihsan Part 3: Prayer Notifications and Qibla Support Foundation

## 1. Overview
Part 3 establishes an isolated, add-only foundation for enhancing prayer notifications and Qibla support. It focuses on domain models and UI components to improve the user experience for Adhan reminders, pre-prayer alerts, and compass calibration.

## 2. Why Isolation?
This layer is intentionally isolated to:
- Avoid conflicts with existing `PrayerNotificationScheduler` and `QiblaManager`.
- Test new UI components and logic (like reminder previews) without platform dependencies (sensors, alarms).
- Provide a clean path for future integration through explicit requests.

## 3. Future Integration
Once approved, this foundation will connect to:
- **`PrayerNotificationScheduler`**: To schedule Adhan, pre-prayer, and Iqama reminders.
- **`QiblaManager`**: To use real sensor data for the Qibla assist card.
- **Settings Screen**: To allow users to configure these new reminder types.
- **App Navigation**: To add entry points from the Home or Prayer screens.

## 4. Protected Areas
The following systems were **NOT** modified:
- AlarmManager/WorkManager scheduling.
- Notification permission handling.
- Real-time compass sensor logic.
- `AndroidManifest.xml`.
- Existing navigation and DI modules.

## 5. Components Added
- **Prayer Assist Header**: Summary of location and calculation settings.
- **Next Prayer Card**: Clear indicator of the upcoming prayer.
- **Notification Preferences Card**: Preview of reminder settings.
- **Reminder Preview List**: Daily breakdown of expected alerts.
- **Qibla Assist Card**: Static preview of Qibla status and accuracy.
- **Compass Calibration Tips**: Guidance for improving sensor accuracy.
- **Quick Actions**: Shortcuts for common prayer and Qibla settings.
