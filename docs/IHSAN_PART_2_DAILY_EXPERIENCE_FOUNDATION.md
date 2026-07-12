# Ihsan Part 2: Daily Experience Foundation

## 1. Overview
Part 2 introduces an isolated layer for enhancing the daily user experience. This foundation is built to be "Add-Only," ensuring that no existing code is modified or broken.

## 2. Why Isolation?
By keeping the new daily experience features in `com.example.feature.ihsanplus.daily`, we can:
- Develop and test new UI components without affecting the production dashboard.
- Create a bridge for future real data integration (Quran, Prayer, Dhikr) using isolated domain models.
- Ensure the project remains buildable and stable at all times.

## 3. Future Integration
This layer is designed to eventually connect to:
- **Home Dashboard**: New cards like "Daily Worship Summary" and "Continue Quran" can be added as optional components.
- **Notifications**: The models support state that can be used for "Before-Prayer" reminders or "Daily Goal" notifications.
- **Data Layer**: Once approved, the `IhsanPlusDailyFakeDataSource` will be replaced by a repository that connects to `IhsanDatabase` and `UserPreferences`.

## 4. Protected Files
The following files were intentionally **NOT** modified during this part:
- `AppNavHost.kt`
- `Screen.kt`
- `MainActivity.kt`
- `HomeDashboardScreen.kt`
- `IhsanDatabase.kt`
- Existing DAOs, Entities, and ViewModels.

## 5. Components Added
- **IhsanPlusDailyHeader**: Personal greeting with Hijri/Gregorian dates.
- **IhsanPlusPrayerSummaryCard**: Real-time look at the next prayer.
- **IhsanPlusQuranContinueCard**: Quick access to last-read Ayah.
- **IhsanPlusDailyContentCards**: Informative cards for Dua, Hadith, and Dhikr.
- **IhsanPlusDailyActivityList**: Tracking of daily Islamic activities.
