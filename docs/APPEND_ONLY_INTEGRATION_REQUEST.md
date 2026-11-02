# Append-Only Integration Request Log

This file lists future changes that may require editing existing files. These changes are not implemented automatically.

## Integration Request Format

Each future request must include:
- Request title
- Existing file that needs a minimal append-only change
- New file/component/use case to connect
- Reason for integration
- Minimal code snippet
- Fallback behavior
- Rollback steps
- Build command
- Risk level

---

## Pending Request 1: Home Daily Worship Cards
- **Status**: Not implemented.
- **Target existing file**: `HomeDashboardScreen.kt` (to be confirmed after Part 2).
- **Reason**: Add daily worship summary to the existing dashboard after approval.

## Pending Request 2: Quran Khatma Entry Point
- **Status**: Not implemented.
- **Target existing file**: `QuranListScreen.kt` (to be confirmed after Part 2).
- **Reason**: Add khatma/progress entry point after approval.

## Pending Request 3: Prayer Notification Preferences Entry Point
- **Status**: Not implemented.
- **Target existing file**: `SettingsScreen.kt` (to be confirmed after Part 3).
- **Reason**: Add notification settings entry point after approval.

## Pending Request 4: Charity Trust Badges in Ehsan List
- **Status**: Not implemented.
- **Target existing file**: `EhsanScreen.kt` (to be confirmed after Part 4).
- **Reason**: Show verification/trust badges after approval.

## Pending Request 5: Charity Trust Badges in Ehsan Details
- **Status**: Not implemented.
- **Target existing file**: `IhsanDetailsScreen.kt` (to be confirmed after Part 4).
- **Reason**: Improve charity trust transparency after approval.

## Pending Request 6: Profile Activity Summary
- **Status**: Not implemented.
- **Target existing file**: `ProfileScreen.kt` (to be confirmed after Part 5).
- **Reason**: Add worship and charity summaries after approval.

## Pending Request 7: Localization Extraction
- **Status**: Not implemented.
- **Target existing file**: Various UI files (to be confirmed after Part 5).
- **Reason**: Move hardcoded Arabic strings to resources after approval.

## Pending Request 8: Optional Backend/Auth Wiring
- **Status**: Not implemented.
- **Target existing file**: `AppModule.kt` / `MainActivity.kt` (to be confirmed later).
- **Reason**: Connect future backend/auth layer after product approval.
