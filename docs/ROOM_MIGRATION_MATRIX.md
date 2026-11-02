# Room Migration Matrix — current target v11

2026-10-21 — Batch 03. `ihsan_master_db` retains its name. **10→11** only adds `fasting_log` (civil-day PK, explicit status, optional personal type label, update timestamp). No historical schema or migration is changed. No destructive fallback/downgrade.

| Start | Path | Isolated desktop SQLite | Android device |
|---|---|---|---|
|2/3/5/6/7/8/9|Historical chain→10→11|PASS: schema/indices/FKs/all prior fields/integrity/reopen|PENDING|
|10|10→11|PASS: Batch01/02 journals and history retained|MigrationTestHelper + real DAO/logout/reopen/isolation test compiled, not run|
|11|Fresh/reopen|PASS: upsert/edit/delete/clear-fasting-only leave every other table unchanged|PENDING|

`LocalJourneyMigrationSqlTest` captures actual Kotlin SQL. `scripts/verify_batch03_migration.py` uses only generated synthetic fixtures and temporary files; validates data, schema and reopening and executes the actual fasting DAO deletion templates against these fixtures. This is not Android Room runtime acceptance or a historical user database. No phone/user database accessed. Versions1/4/future remain unsupported.

## Historical v10 matrix (superseded above)

# Room Migration Matrix — current target v10

2026-10-20 — pre-backend Batch 02. Database name remains `ihsan_master_db`. New **9→10** creates only `memorization_plans`, `memorization_reviews` and its planId index/foreign key. No existing table or row is changed. Schemas 5–9 are preserved; 10 is compiler-exported. No destructive fallback/downgrade.

| Start | Path | Desktop SQLite | Device |
|---|---|---|---|
|2/3/5/6/7/8|Existing chain →9→10|PASS: schema/indices/FKs, prior fields, integrity and file reopen|PENDING|
|9|9→10|PASS: Batch 01 data retained|New MigrationTestHelper + Room reopen test compiled, not run|
|10|Fresh schema + file reopen twice|PASS: plan/review persistence and orphan rejection|PENDING|

`LocalJourneyMigrationSqlTest` exports actual Kotlin SQL; `scripts/verify_batch02_migration.py` executes it on isolated synthetic fixtures, including file-backed reopen. This is not Android Room runtime acceptance or a historic user database. Versions 1/4/future remain unsupported. No phone/user database accessed.

## Historical Batch 01 report (superseded above)

# Room Migration Matrix — current target v9

Updated 2026-10-20 for pre-backend batch 01. Database remains `ihsan_master_db`.

| Start | Supported path | Desktop SQLite synthetic-fixture result | Xiaomi / MigrationTestHelper |
|---|---|---|---|
| 2 | 2→3→5→6→7→8→9 | PASS | Pending on this artifact |
| 3 | 3→5→6→7→8→9 | PASS | Pending on this artifact |
| 5 | 5→6→7→8→9 | PASS | Pending on this artifact |
| 6 | 6→7→8→9 | PASS | Pending on this artifact |
| 7 | 7→8→9 | PASS | Pending on this artifact |
| 8 | 8→9 | PASS | New test added; not executed on phone |
| 9 | Fresh creation / reopen | Schema, data, integrity PASS | Pending |

- v8→v9 only adds `reading_plans`, `reading_page_completions`, `private_ayah_notes`, and the existing `prayer_log` entity plus their new indices. No existing table is rebuilt or altered by this step.
- Schemas 5–8 are unchanged. Schema 9 is compiler-exported. No destructive fallback or downgrade path is enabled.
- `LocalJourneyMigrationSqlTest` captures SQL by invoking the actual Kotlin migration objects. `scripts/verify_local_journey_migration.py` executes those statements on isolated **in-memory synthetic fixtures**, compares all v9 table/column/index/foreign-key definitions against Room's fresh schema, verifies every pre-existing field, and runs `integrity_check`.
- v3 uses the v5 schema, reflecting the existing no-op 3→5 migration. v2 additionally omits `hadiths.explanation`. These are synthetic structural fixtures, not historic user databases; this does not establish physical migration acceptance.
- Versions 1, 4 and unknown future schemas remain unsupported. No production database or phone data was accessed.

## Historical v5 report (retained for provenance; superseded above)

# Room Migration Matrix

**Database:** `IhsanDatabase` (`ihsan_master_db`)  
**Current version:** `5`  
**Destructive fallback:** **not used** in production `DatabaseModule`

## Supported upgrade paths

| Source | Target | Migration object | Schema file | Automated test | Device verification | Status |
|--------|--------|------------------|-------------|----------------|---------------------|--------|
| 2 | 3 | `IhsanDatabaseMigrations.MIGRATION_2_3` | historical SQL in instrumented test | `migrate2To5_addsExplanationAndPreservesData` (covers 2→5 chain) | Not run in Part 5 CI | Supported |
| 3 | 5 | `IhsanDatabaseMigrations.MIGRATION_3_5` | `feature/schemas/.../5.json` | `migrate3To5_preservesRepresentativeData` | Not run in Part 5 CI | Supported |
| 2 | 5 | `MIGRATION_2_3` then `MIGRATION_3_5` | v5 export | instrumented chain test | Not run in Part 5 CI | Supported |

## Unsupported / unknown paths

| Source | Target | Status | Notes |
|--------|--------|--------|-------|
| 1 → * | any | Unsupported | No historical schema JSON for v1 in repo |
| 4 → 5 | 5 | N/A | Version 4 never shipped; `MIGRATION_3_5` jumps 3→5 intentionally |
| Fresh install | 5 | Supported | `onCreate` + asset seed callback |

## Required checks

* Every supported upgrade path reaches schema version 5 — covered by instrumented tests when a device is available.
* No supported path uses `fallbackToDestructiveMigration` — enforced by architecture unit tests.
* User / donation / Quran / Azkar / Dua / Hadith / bookmark / download survival — partially asserted in `IhsanDatabaseMigrationTest`.
* Schema export present for v5 — unit-tested.
* Missing migration fails Room open — by Room contract (no destructive fallback).

## Honesty note

Only schema export committed is **v5**. Paths below v2 are undocumented and must not be claimed as supported without recovered historical schemas.
