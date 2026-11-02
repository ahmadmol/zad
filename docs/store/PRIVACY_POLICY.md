# Privacy Policy (Source Draft)

**Status:** Draft for store listing — **not legal counsel certified**.  
**App:** إحسان (`com.example.mol` until an approved production ID is applied)

## What we store locally

* Display name / local profile preferences
* Prayer calculation settings and last known location coordinates used for prayer/Qibla
* Quran reading position, downloads, and bookmarks
* Azkar / Tasbih counters and daily activity counts
* Local charity board listings you create (including contact phone numbers you enter)
* App preferences (theme, notifications toggles)

## Permissions

* Location — prayer times and Qibla
* Notifications — prayer reminders and media playback notices
* Exact alarms — timely prayer reminders when granted
* Internet — Quran audio/download assets and public live streams
* Foreground service (media playback) — Quran audio

## What we do not do

* No cloud account or OTP authentication
* No server synchronization of profile or charity data
* No sale of user data
* No advertising SDK data sharing claimed in this draft
* No in-app payment processing
* No verified charity / beneficiary validation backend
* Automatic cloud backup is disabled (`allowBackup=false`)

## Contact handoff

Phone and WhatsApp actions open external apps with the number stored in a local listing. The app does not mediate payments.

## Observability

Operational logs are privacy-filtered and release defaults are no-op until a vendor is approved. Logs must not include phone numbers, precise coordinates, Quran text, or charity descriptions.

## Legal review

**Pending qualified counsel review before store publication.**
