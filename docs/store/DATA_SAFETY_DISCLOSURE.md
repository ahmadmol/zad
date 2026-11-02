# Data Safety Disclosure (Play Console alignment)

| Data type | Collected | Shared | Purpose | Optional |
|-----------|-----------|--------|---------|----------|
| Name (local profile) | Yes (on device) | No | Personalization | Yes |
| Location | Yes (on device / OS) | No | Prayer / Qibla | Required for those features |
| Phone numbers in charity listings | Yes (on device) | No (user may share via tel/WhatsApp) | Local board contact | User-entered |
| App activity / preferences | Yes (on device) | No | App functionality | — |
| Financial info | No | No | — | — |
| Auth credentials | No | No | — | — |

**Encryption in transit:** N/A for local-only profile/charity board. Network used for public streams/downloads only.  
**Deletion:** Uninstall removes local app data. No cloud account to delete.


## Local campaign data — 2026-10-21, Batch 04

This describes current on-device storage; it is not final Play Console/legal approval. No backend or analytics was added.

- Reading plans/manual page confirmations, memorization/review history, prayer/fasting logs and private ayah notes remain in the existing private Room database (current schema 11).
- Nabi/Sanhya manual lesson completion/resume metadata and existing learning favorites/later collections remain in UserPreferences DataStore. A page-open, playback attempt or external YouTube launch is not completion.
- Community records and selected images are local. Saving does not publish, deliver a report, verify a phone number or create secure authentication. USER, LEGACY and fictional DEMO sources remain distinct.
- Signing out deactivates the local profile; it does not delete community listings, saved presentation, learning, reading, notes or worship history. This is device-local personalization, not a secure account isolation guarantee.
- Fasting-only clearing uses the approved separate warning/confirmation. Per-listing deletion removes that local row and attempts cleanup of its unshared private images. No general worship-data reset policy is introduced.
- Optional note export and contact/share actions are explicit user actions. A selected recipient/third-party app controls the exported/shared copy. Uninstalling cannot revoke those copies.
- Public media playback/source links still use the existing third-party providers and their policies; no license or public-release approval is implied. Cloud/device-transfer backup settings and permissions were not expanded.


## Batch 05 local widget/recovery — 2026-10-21

- D07-approved launcher widget displays the canonical next prayer/time, approximate native countdown and last-update time. It displays no profile, prayer/fasting/memorization history or personal reading progress. The optional reading button resolves the existing local position only when tapped and opens the current reader/list.
- The widget performs no location fetch or network poll and adds no permission. Its private receiver uses immutable explicit actions and a separate inexact update alarm; it does not replace the prayer notification ledger. Removing the final widget cancels its update subscription/alarm.
- Search keys are a bounded in-memory derivative of existing source fields, not a new religious edition or exported dataset. No local data is uploaded.
- Canonical content/storage failures surface retry states without resetting Room/DataStore. Operational Logcat messages redact credentials/URIs and omit raw exception messages/stacks. No analytics or crash-reporting vendor was added.
- Existing backup exclusions, data keys, local sign-out retention and approved fasting-only deletion scope remain unchanged. Device, public-release and content-rights acceptance remain separate.
