# Phase 1B Room Migration Report — إحسان

**Date:** 2026-07-12  
**Branch:** `fix/room-migration-safety`  
**Phase:** 1B — Room Database Migration Safety only

---

## 1. Executive Result

| Item | Result |
|------|--------|
| Destructive fallback removed | **Yes** — `fallbackToDestructiveMigration()` removed from `DatabaseModule` |
| Supported migrations | `2→3`, `3→5` (chained `2→5`) |
| Version 4 | **Never existed** in committed `IhsanDatabase` code |
| Schema export | **Enabled** — `feature/schemas/.../IhsanDatabase/5.json` |
| Migration tests | Added under `feature/src/androidTest/.../IhsanDatabaseMigrationTest.kt` |
| Controlled Integration | **REMAINS BLOCKED** |

See §14–17 for build/test/lint outcomes captured at end of this phase.

---

## 2. Starting Branch and Commit

| Item | Value |
|------|--------|
| Started from | `wip/phase0-preserved-baseline` @ `23ad3212fed6381734ad2c43bfbd406ba2f54fc4` |
| Fix branch | `fix/room-migration-safety` |
| Preservation folder | `.project-preservation/` remains untracked |

---

## 3. Historical Schema Investigation

| Commit | Evidence |
|--------|----------|
| `c0b7ac4` Improve prayer and qibla | `IhsanDatabase` **created at version 2**, same 10 entities, `exportSchema = false` |
| `a5b0fb8` Improve hadith module | Version **2 → 3**; `HadithEntity.explanation` added; `MIGRATION_2_3` registered; destructive fallback **removed** at that time |
| `223c540` WIP preserve Phase 0 | Version **3 → 5** only; **no entity file diffs**; re-added `fallbackToDestructiveMigration()` |
| Docs `IHSAN_EXISTING_PROJECT_MAP.md` | Claims version 4 — **documentation only**, not a code schema |

Commands used (read-only): `git log`, `git show`, `git diff` on `IhsanDatabase.kt`, entities, `DatabaseModule.kt`.

---

## 4. Version 3 Schema

**Evidence:** `git show a5b0fb8:.../IhsanDatabase.kt` + entities at that commit.

Entities/tables (identical set to v5):

| Table | Entity |
|-------|--------|
| `azkar_table` | `ZikrEntity` |
| `daily_stats` | `DailyStatEntity` |
| `duas` | `DuaEntity` |
| `donations` | `DonationEntity` |
| `users` | `UserEntity` |
| `hadiths` | `HadithEntity` (+ nullable `explanation`) |
| `surahs` | `SurahEntity` |
| `ayahs` | `AyahEntity` (+ unique index `surahId`,`verseNumber`) |
| `bookmarks` | `BookmarkEntity` |
| `downloaded_ayahs` | `DownloadedAyahEntity` |

---

## 5. Version 4 Schema

**Not a real committed database version.**

No `IhsanDatabase` revision with `version = 4` exists in git history. Documentation references to v4 are stale/incorrect.

---

## 6. Version 5 Schema

**Evidence:** current `IhsanDatabase` + generated `feature/schemas/.../IhsanDatabase/5.json`.

Entity list and columns match version 3. Only the `@Database(version = …)` integer differs.

---

## 7. Schema Differences

| Transition | Real SQL / structure change? |
|------------|------------------------------|
| 2 → 3 | **Yes** — `ALTER TABLE hadiths ADD COLUMN explanation TEXT` |
| 3 → 4 | N/A — v4 never shipped |
| 4 → 5 | N/A — v4 never shipped |
| 3 → 5 | **No** — version number only; empty migration required by Room |

---

## 8. Supported Upgrade Matrix

| From | To | Evidence | Migration | Data preservation |
| ---- | -- | -------- | --------- | ----------------- |
| 2 | 3 | `a5b0fb8` | `MIGRATION_2_3` | Yes |
| 3 | 5 | `223c540` version bump only | `MIGRATION_3_5` (no-op) | Yes |
| 2 | 5 | Chained 2→3→5 | `ALL` | Yes |
| 4 | 5 | No real v4 | **Unsupported** | Room fails closed (no wipe) |
| 1 | 5 | IhsanDatabase never at v1 | **Unsupported** | Room fails closed |

Minimum supported production source: **version 3** (and **version 2** if any installs remain from early consolidate).

---

## 9. Migration Implementation

File: `feature/.../database/IhsanDatabaseMigrations.kt`

- `MIGRATION_2_3` — add `hadiths.explanation`
- `MIGRATION_3_5` — empty body (schema identity)
- `ALL` — array registered in DI

`DatabaseModule.kt` uses `.addMigrations(*IhsanDatabaseMigrations.ALL)` only.

---

## 10. Destructive Fallback Decision

| Decision | Detail |
|----------|--------|
| Broad `fallbackToDestructiveMigration()` | **Removed** |
| Unsupported versions (1, 4, gaps) | Room throws on open — **prefer fail over silent wipe** |
| Why safe | Supported paths 2→3→5 covered; wiping production data is worse than a visible crash for unknown versions |

---

## 11. Schema Export Configuration

| Item | Value |
|------|--------|
| `exportSchema` | `true` on `IhsanDatabase` |
| KSP arg | `room.schemaLocation` → `$projectDir/schemas` in `feature/build.gradle.kts` |
| Generated file | `feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase/5.json` |
| Historical 2.json / 3.json | Not recovered as Room exports (not fabricated); v2/v3 covered by SQL fixtures in tests |

---

## 12. Migration Test Coverage

File: `feature/src/androidTest/.../IhsanDatabaseMigrationTest.kt`

| Test | Intent |
|------|--------|
| `migrate3To5_preservesRepresentativeData` | v3 fixture → v5; assert user/donation/azkar/dua/hadith/quran/bookmark/download/stats |
| `migrate2To5_addsExplanationAndPreservesData` | v2 fixture → v5; assert explanation column + retained rows |
| `migrate3To5_secondOpenDoesNotWipeData` | reopen after migrate; data still present |

Dependency: `androidx.room:room-testing` via version catalog.

Version 4→5 test: **not added** (no real v4 schema).

---

## 13. Data-Preservation Assertions

Tests assert representative synthetic values (IDs, names, phones, counters, favorites, timestamps, readerId). No real PII.

---

## 14. Build Result

| Command | Result |
|---------|--------|
| `.\gradlew.bat --no-daemon :app:assembleDebug` | **BUILD SUCCESSFUL** (~1m 12s) |
| `:feature:compileDebugAndroidTestKotlin` | **SUCCESS** (same invocation) |

Kotlin compile daemon connection warnings occurred; fallback in-process compile succeeded.

---

## 15. Unit-Test Result

| Command | Result |
|---------|--------|
| `.\gradlew.bat --no-daemon test :feature:testDebugUnitTest` | **BUILD SUCCESSFUL** |
| Feature meaningful tests | 7 tests, **0 failures** (Asma/Qibla/GetSurah + Example) |

---

## 16. Instrumented Migration-Test Result

| Check | Result |
|-------|--------|
| `:feature:compileDebugAndroidTestKotlin` | **Compiled successfully** |
| Runtime (`connectedDebugAndroidTest`) | **Not executed** — `adb` not available on PATH / no device detected |
| Status | **Runtime verification pending** |

---

## 17. Lint Result

| Command | Result |
|---------|--------|
| `:app:lintDebug` | **BUILD SUCCESSFUL** |
| Errors | **0** (warnings only; pre-existing dependency/resource warnings expected) |

---

## 18. Changed Files

| Path | Role |
|------|------|
| `feature/.../IhsanDatabaseMigrations.kt` | Migrations |
| `feature/.../IhsanDatabase.kt` | `exportSchema = true` |
| `app/.../DatabaseModule.kt` | Register migrations; remove destructive fallback |
| `feature/build.gradle.kts` | schemaLocation + room-testing |
| `gradle/libs.versions.toml` | `room-testing` |
| `feature/schemas/.../5.json` | Exported schema |
| `feature/src/androidTest/.../IhsanDatabaseMigrationTest.kt` | Migration tests |
| `docs/PHASE_1B_ROOM_MIGRATION_REPORT.md` | This report |

Not modified: navigation, IhsanPlus, Ehsan UI/auth, Quran audio, Manifest, design system, `.project-preservation/`.

---

## 19. Unsupported Historical Versions

- Database version **1** (never used by `IhsanDatabase`)
- Database version **4** (docs only)
- Any other version not 2, 3, or 5

---

## 20. Remaining Database Risks

- Devices already wiped by prior destructive fallback cannot be restored from app data
- Manual SQL fixtures in tests must stay aligned with Room schema (validate against `5.json` when changing entities)
- Identity-hash / strict schema validation on odd hand-built DBs may still fail closed (desired vs silent wipe)
- Instrumented migration tests may be unexecuted until emulator/device available

---

## 21. Recovery and Rollback

```powershell
git switch fix/room-migration-safety
git log -1
# rollback branch tip if needed:
git switch wip/phase0-preserved-baseline
# or revert the Phase 1B commit hash once created
```

Local patches remain under `.project-preservation/phase-1a/` (do not commit).

---

## 22. Phase 1B Exit Criteria

| # | Criterion | Status |
|---|-----------|--------|
| 1 | Historical schemas evidenced | Met |
| 2 | Supported upgrade paths implemented | Met (2→3, 3→5) |
| 3 | Broad destructive fallback gone for production versions | Met |
| 4 | v3→v5 preservable | Implemented + tested in code |
| 5 | Representative data assertions | In androidTest |
| 6 | Room schema export | Met (`5.json`) |
| 7–9 | Build / unit / lint | Appendix A |
| 10 | Migration tests execute on device | May be pending |
| 11 | Diff isolated | Met |
| 12 | Commit created | After verification |
| 13 | `.project-preservation/` untracked | Met |
| 14 | Controlled Integration blocked | Met |

---

## 23. Final Decision

### PHASE 1B PARTIALLY PASSED — MIGRATIONS IMPLEMENTED BUT RUNTIME VERIFICATION PENDING

### P1-05 INCOMPLETE

(Reason: instrumented migration tests compiled but were not executed on a device/emulator.)

### CONTROLLED INTEGRATION REMAINS BLOCKED

---

## Appendix A — Gradle Outcomes (captured)

| Check | Result |
|-------|--------|
| `:app:assembleDebug` | SUCCESS |
| `test` / `:feature:testDebugUnitTest` | SUCCESS (0 failures) |
| `:feature:compileDebugAndroidTestKotlin` | SUCCESS |
| `:feature:connectedDebugAndroidTest` | **Not run** (no `adb` / device) |
| `:app:lintDebug` | SUCCESS — 81 warnings, **0 errors** |
| Phase 1B decision | **PARTIALLY PASSED** |
| P1-05 decision | **INCOMPLETE** (runtime pending) |
| Commit hash | See git log on `fix/room-migration-safety` after commit |
