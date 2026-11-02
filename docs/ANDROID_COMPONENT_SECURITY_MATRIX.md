# Android Component Security Matrix

| Component | Type | Exported | Permission | Intent filters | Reason | Risk | Verification |
|-----------|------|----------|------------|----------------|--------|------|--------------|
| `.MainActivity` | Activity | true | launcher | MAIN/LAUNCHER | App entry | Low | Reviewed |
| `QuranAudioService` | Service | **false** | FGS mediaPlayback | MediaSessionService | In-app Quran audio | Low if non-exported | Reviewed |
| `PrayerNotificationReceiver` | Receiver | **false** | — | (none / internal) | Prayer alarms | Low | Reviewed |
| `PrayerSystemReconciliationReceiver` | Receiver | **false** | — | BOOT_COMPLETED, LOCKED_BOOT_COMPLETED, MY_PACKAGE_REPLACED, TIME_SET, TIMEZONE_CHANGED, DATE_CHANGED | Reschedule prayer alarms | Medium (system events) | Reviewed — keep non-exported |

## Permissions

| Permission | Reason | Notes |
|------------|--------|-------|
| INTERNET | Live streams / downloads | Present |
| ACCESS_FINE/COARSE_LOCATION | Prayer / Qibla | Present |
| FOREGROUND_SERVICE + MEDIA_PLAYBACK | Quran audio | Present |
| POST_NOTIFICATIONS | Prayer / media | Present |
| RECEIVE_BOOT_COMPLETED | Prayer reschedule | Present |
| SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM | Prayer timing | Present |

## Backup

| Setting | Value | Status |
|---------|-------|--------|
| allowBackup | false | Safe for current product model |
| fullBackupContent | `@xml/backup_rules` | Present |
| dataExtractionRules | `@xml/data_extraction_rules` | Present |

## Part 5 changes

No component export flags were changed. Matrix documents current safe posture. Do not flip `QuranAudioService` or prayer receivers to exported without a Media3/system requirement review.
