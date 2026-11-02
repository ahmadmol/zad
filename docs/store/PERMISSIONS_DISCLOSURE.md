# Permissions Disclosure

| Permission | Why |
|------------|-----|
| `INTERNET` | Live streams, Quran asset/audio download |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Prayer times and Qibla |
| `POST_NOTIFICATIONS` | Prayer and media notifications |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Precise prayer reminders |
| `RECEIVE_BOOT_COMPLETED` | Reschedule prayer alarms after reboot |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Quran audio service |

No SMS, contacts, camera, or microphone permissions are declared for core features in the current manifest.
