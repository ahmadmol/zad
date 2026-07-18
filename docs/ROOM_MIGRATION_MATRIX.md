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
