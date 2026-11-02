# Ihsan Existing Project Map

## 1. Project Modules
- :app: The entry point of the application, handles navigation and dependency injection.
- :feature: Contains the business logic and UI for all features.
- :designsystem: Provides shared UI components and theme tokens.

## 2. App Module Responsibilities
- **MainActivity**: Entry point activity, handles location permission and sets up the root UI.
- **AppNavHost**: Manages all screen routes using Navigation Compose.
- **Screen**: Defines the navigation routes.
- **Koin Modules**: Configures dependency injection for the entire app.
- **Application Class (IhsanApp)**: Initializes Koin and sets up background workers.

## 3. Feature Module Responsibilities
List known feature packages:
- **dashboard**: Home dashboard and daily activities.
- **quran**: Quran list, reader, and search.
- **prayer**: Prayer times and notifications.
- **qibla**: Qibla direction finder.
- **azkar**: Azkar list and counter (Sebha).
- **duas**: Supplications (Dua) library.
- **hadith**: Hadith library.
- **ehsan**: Charity and donation features.
- **asma**: Names of Allah.
- **profile**: User profile and donation history.

## 4. Design System Module Responsibilities
- **IhsanTheme**: Defines the app's color palette, typography, and shapes.
- **Components**: Reusable UI elements like `EhsanTopBar`, `IhsanActionCard`, and `LastReadCard`.
- **RTL Support**: Arabic-first layout support.

## 5. Existing Features and Status
| Feature | Package path | ViewModel if known | Status | Notes |
|---|---|---|---|---|
| Dashboard | `feature.dashboard` | `HomeDashboardViewModel` | Complete | Main entry screen |
| Quran | `feature.quran` | `QuranViewModel` | Complete | List and Reader |
| Prayer Times | `feature.prayer` | `PrayerViewModel` | Complete | Localization dependent |
| Qibla | `feature.qibla` | `QiblaViewModel` | Complete | Sensor dependent |
| Azkar | `feature.azkar` | `AzkarViewModel` | Complete | Includes Sebha |
| Ehsan | `feature.ehsan` | `EhsanViewModel` | Complete | Donation flow |

## 6. Existing Architecture Pattern
The app follows a layered Clean Architecture within feature packages:
- **Presentation**: Composables and ViewModels.
- **Domain**: Use Cases and Models.
- **Data**: Repositories, DAOs, and Entities.
- **DI**: Koin modules for each feature.

## 7. Existing Database Summary
- **IhsanDatabase**: Version 4, Room-based.
- **Entities**: Zikr, DailyStat, Dua, Donation, User, Hadith, Surah, Ayah, Bookmark, DownloadedAyah.
- **DAOs**: Corresponding interfaces for all entities.
- **Asset Loaders**: `QuranAssetLoader` for initial data ingestion.

## 8. Existing Notification Summary
- **Workers**: `AdhanWorker`, `AzkarNotificationWorker`.
- **Scheduling**: Handled via WorkManager in `IhsanApp`.
- **Permissions**: POST_NOTIFICATIONS handling for API 33+.

## 9. Existing Charity / Ehsan Summary
- Local donation offer/request management.
- User profile history for donations.
- Contact options for charity cases.

## 10. Enhancement Direction
Enhance existing features through isolated add-only packages (e.g., `ihsanplus`). Do not rebuild or refactor existing features.
