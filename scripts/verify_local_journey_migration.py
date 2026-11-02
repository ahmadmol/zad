"""Execute actual Kotlin migration SQL against isolated in-memory SQLite fixtures.

Never opens ihsan_master_db or any device/user database. Compares generated Room v9
tables/indices and exact legacy field values; physical MigrationTestHelper QA is separate.
"""
import json
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCHEMAS = ROOT / "feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase"
MIGRATIONS = json.loads((ROOT / "feature/build/reports/local-journey/migration-sql.json").read_text(encoding="utf-8"))


def schema(version):
    data = json.loads((SCHEMAS / f"{max(version, 5)}.json").read_text(encoding="utf-8"))["database"]
    # v3 == v5 is historical; v2 differs by hadiths.explanation only.
    return data


def create(db, data, version):
    for entity in data["entities"]:
        sql = entity["createSql"].replace("${TABLE_NAME}", entity["tableName"])
        if version == 2 and entity["tableName"] == "hadiths":
            sql = sql.replace(", `explanation` TEXT", "")
        db.execute(sql)
        for index in entity["indices"]:
            db.execute(index["createSql"].replace("${TABLE_NAME}", entity["tableName"]))


def structure(db):
    tables = [r[0] for r in db.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")]
    result = {}
    for table in tables:
        indices = []
        for index in db.execute(f'PRAGMA index_list("{table}")'):
            if not index[1].startswith("sqlite_autoindex"):
                indices.append((index[1], index[2], tuple(r[2] for r in db.execute(f'PRAGMA index_info("{index[1]}")'))))
        result[table] = (tuple(db.execute(f'PRAGMA table_info("{table}")')), sorted(indices), tuple(db.execute(f'PRAGMA foreign_key_list("{table}")')))
    return result


expected_db = sqlite3.connect(":memory:")
create(expected_db, schema(9), 9)
expected = structure(expected_db)
results = []
for start in (2, 3, 5, 6, 7, 8, 9):
    db = sqlite3.connect(":memory:")
    create(db, schema(start), start)
    before = {}
    for table in structure(db):
        columns = list(db.execute(f'PRAGMA table_info("{table}")'))
        names = [r[1] for r in columns]
        values = [1 if r[2] in ("INTEGER", "REAL") else f"synthetic:{table}:{r[1]}" for r in columns]
        db.execute(f'INSERT INTO "{table}" ({",".join(names)}) VALUES ({",".join("?" for _ in values)})', values)
        before[table] = (names, tuple(values))
    version = start
    while version < 9:
        migration = next(m for m in MIGRATIONS if m["from"] == version)
        with db:
            for statement in migration["statements"]:
                db.execute(statement["sql"], statement["args"])
        version = migration["to"]
    assert structure(db) == expected, f"Room v9 schema mismatch from {start}"
    for table, (columns, values) in before.items():
        actual = db.execute(f'SELECT {",".join(columns)} FROM "{table}"').fetchall()
        assert actual == [values], f"Data changed in {table} from {start}"
    assert db.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
    if start < 9:
        for table in ("reading_plans", "reading_page_completions", "private_ayah_notes", "prayer_log"):
            assert db.execute(f'SELECT count(*) FROM {table}').fetchone()[0] == 0
    db.close()
    results.append({"from": start, "to": 9, "schema": "PASS", "preservation": "PASS", "integrity": "PASS"})

out = ROOT / ".artifacts/prebackend-batch01-migration.json"
out.write_text(json.dumps({"kind": "desktop SQLite synthetic fixtures, not device acceptance", "paths": results}, indent=2))
print(json.dumps(results, indent=2))
