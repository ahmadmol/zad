# Phase 1D Android Backup and Local PII Report — إحسان

**Date:** 2026-07-12  
**Branch:** `fix/backup-pii-policy`  
**Phase:** 1D — Android Backup and Local PII Protection only

---

## 1. Executive Result

| Item | Result |
|------|--------|
| Global Backup Disabled | **Yes** — `android:allowBackup="false"` in manifest |
| Device Transfer Disabled | **Yes** — Restrictive `data_extraction_rules.xml` |
| PII Protected | **Yes** — All local files, databases, and preferences excluded |
| Build / Test / Lint | **PASS** |
| Merged Manifest Verified | **Yes** |
| Controlled Integration | **REMAINS BLOCKED** |

---

## 2. Starting Branch and HEAD

| Item | Value |
|------|--------|
| Started from | `fix/request-help-phone-integrity` (Phase 1C tip) |
| HEAD | `e5af13c` |

---

## 3. Original Backup Configuration

- `android:allowBackup="true"` (default)
- `android:fullBackupContent="@xml/backup_rules"` (empty/sample)
- `android:dataExtractionRules="@xml/data_extraction_rules"` (empty/sample)

---

## 4. Sensitive Local Data Inventory

| Domain | File/Resource | Classification | Sensitivity |
|--------|---------------|----------------|-------------|
| Room DB | `ihsan_master_db` | Database | **High** (Name, Phone, Address) |
| DataStore | `user_preferences.preferences_pb` | Preferences | **High** (User Name, GPS Coords) |
| DataStore | `settings.preferences_pb` | Preferences | **Medium** (Manual Location) |
| Files | `files/` | Internal Storage | **Medium** (May contain user images) |

---

## 5. Room Data Classification

**Database Filename:** `ihsan_master_db`

| Table | Sensitive Fields | Data Type |
|-------|------------------|-----------|
| `users` | `firstName`, `lastName`, `phoneNumber`, `city`, `address` | PII |
| `donations` | `title`, `description`, `donorName`, `phoneNumber` | PII / User Content |
| `bookmarks` | `surahId`, `verseNumber` | Reading History |

---

## 6. DataStore and Preference Classification

| File | Key | Classification |
|------|-----|----------------|
| `user_preferences` | `user_name` | PII |
| `user_preferences` | `user_latitude`, `user_longitude` | GPS Coords |
| `user_preferences` | `last_read_*` | Reading History |
| `settings` | `manual_location_city`, `manual_location_lat`, `manual_location_lng` | Location PII |
| `settings` | `font_size`, `dark_mode`, `madhab` | Harmless Prefs |

---

## 7. Backup Policy Options Considered

- **Option A: Disable globally.** Safest for local-identity apps. Prevents cloning profile to new devices without verification.
- **Option B: Strict Exclusions.** Allows backing up harmless settings (font size, etc.) but risks missing sidecar files or new PII locations.

---

## 8. Selected Policy

**Option A — Disable application backup globally.**

---

## 9. Security and Privacy Rationale

Because the application stores sensitive PII (phone numbers, precise locations, addresses) in its main database and primary preference files, allowing Android Backup poses a significant privacy risk. Since there is no remote server-side authentication to verify the user's identity during a restore, any backup could be used to clone a user's local identity and private donation requests onto another device. 

While this results in the loss of "harmless" settings (like font size) during a device upgrade, the protection of user identity and PII is prioritized.

---

## 10. Manifest Changes

```xml
<application
    android:allowBackup="false"
    ... />
```

---

## 11. Legacy Backup Rule Changes

File: `backup_rules.xml`
- Updated to explicitly exclude all domains (`database`, `sharedpref`, `file`, `root`, `external`) as a defense-in-depth measure for older Android versions (6.0 - 11).

---

## 12. Android 12+ Extraction Rule Changes

File: `data_extraction_rules.xml`
- Updated both `<cloud-backup>` and `<device-transfer>` to explicitly exclude all storage domains. This ensures that even direct device-to-device transfers (via cable or Wi-Fi) do not copy sensitive local data.

---

## 13. Database Filename Verification

- **Primary:** `ihsan_master_db` (verified in `DatabaseModule.kt`)
- **Sidecars:** `ihsan_master_db-wal`, `ihsan_master_db-shm` (expected for Room/SQLite)

---

## 14. DataStore Filename Verification

- `user_preferences.preferences_pb`
- `settings.preferences_pb`

---

## 15. Merged Manifest Verification

- **Path:** `app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`
- **Result:** `android:allowBackup="false"` confirmed at the application level.

---

## 16. Build Result

| Command | Result |
|---------|--------|
| `.\gradlew.bat :app:assembleDebug` | **SUCCESS** |

---

## 17. Test Result

| Command | Result |
|---------|--------|
| `.\gradlew.bat :feature:testDebugUnitTest` | **SUCCESS** (17 tests passed) |

---

## 18. Lint Result

| Command | Result |
|---------|--------|
| `.\gradlew.bat :app:lintDebug` | **SUCCESS** (0 errors) |

---

## 19. Device Runtime Verification

- **Status:** **Pending** (No device/adb available for `bmgr` testing).
- **Static Verification:** Complete via Merged Manifest inspection.

---

## 20. Changed Files

| Path | Role |
|------|------|
| `app/src/main/AndroidManifest.xml` | Global policy |
| `app/src/main/res/xml/backup_rules.xml` | Legacy rules |
| `app/src/main/res/xml/data_extraction_rules.xml` | Modern rules |
| `docs/PHASE_1D_BACKUP_PII_REPORT.md` | This report |

---

## 21. Restore Behavior and User Impact

- **New Device:** Application will start with a fresh state. User must re-enter their name and re-download any media.
- **App Reinstall:** Data is cleared; no data is restored from cloud.
- **User Impact:** High security/privacy; low convenience for settings preservation.

---

## 22. Remaining Security Risks

- If a user manually copies the `/data/data/com.example.mol/` folder using root access, data is still exposed (SQLCipher is not yet implemented).
- Temporary cache files or external storage (if any used in future) must be continuously monitored for PII.

---

## 23. Rollback Instructions

```powershell
git switch fix/backup-pii-policy
git reset --hard HEAD~1
```

---

## 24. Exit Criteria

1. Current backup behavior documented? **Yes**
2. Room data protected? **Yes**
3. DataStore data protected? **Yes**
4. Legacy rules valid? **Yes**
5. Android 12+ rules valid? **Yes**
6. Merged manifest confirms policy? **Yes**
7. Build/Test/Lint success? **Yes**
8. Controlled Integration remains blocked? **Yes**

---

## 25. Final Decision

### PHASE 1D PASSED — LOCAL PII BACKUP POLICY VERIFIED

### P1-03 COMPLETE

### CONTROLLED INTEGRATION REMAINS BLOCKED
