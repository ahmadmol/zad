"""Execute actual Kotlin migration SQL against isolated in-memory SQLite fixtures.

Never opens ihsan_master_db or any device/user database. Compares generated Room v11
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
create(expected_db, schema(11), 11)
expected = structure(expected_db)
results = []
for start in (2, 3, 5, 6, 7, 8, 9, 10, 11):
    db = sqlite3.connect(":memory:")
    create(db, schema(start), start)
    db.execute("PRAGMA foreign_keys=ON")
    before = {}
    for table in structure(db):
        columns = list(db.execute(f'PRAGMA table_info("{table}")'))
        names = [r[1] for r in columns]
        values = [1 if r[2] in ("INTEGER", "REAL") else f"synthetic:{table}:{r[1]}" for r in columns]
        if table == "memorization_reviews":
            values[names.index("planId")] = "synthetic:memorization_plans:id"
        db.execute(f'INSERT INTO "{table}" ({",".join(names)}) VALUES ({",".join("?" for _ in values)})', values)
        before[table] = (names, tuple(values))
    version = start
    while version < 11:
        migration = next(m for m in MIGRATIONS if m["from"] == version)
        with db:
            for statement in migration["statements"]:
                db.execute(statement["sql"], statement["args"])
        version = migration["to"]
    assert structure(db) == expected, f"Room v11 schema mismatch from {start}"
    for table, (columns, values) in before.items():
        actual = db.execute(f'SELECT {",".join(columns)} FROM "{table}"').fetchall()
        assert actual == [values], f"Data changed in {table} from {start}"
    assert db.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
    if start < 9:
        for table in ("reading_plans", "reading_page_completions", "private_ayah_notes", "prayer_log"):
            assert db.execute(f'SELECT count(*) FROM {table}').fetchone()[0] == 0
    assert db.execute("PRAGMA foreign_key_check").fetchall() == []
    db.execute("INSERT INTO memorization_plans VALUES ('new-plan',1,1,7,20000,0,20000,20001,0,NULL)")
    db.execute("INSERT INTO memorization_reviews VALUES ('new-review','new-plan',20001,'GOOD',20004)")
    try:
        db.execute("INSERT INTO memorization_reviews VALUES ('orphan','missing',20001,'GOOD',20004)")
        raise AssertionError("Orphan review accepted")
    except sqlite3.IntegrityError:
        pass
    # File-backed synthetic reopen; never a user or device database.
    import tempfile
    with tempfile.TemporaryDirectory(prefix="ihsan-batch03-") as folder:
        path = Path(folder) / "synthetic.sqlite"
        disk = sqlite3.connect(path)
        db.commit()
        db.backup(disk)
        disk.close()
        for _ in range(2):
            reopened = sqlite3.connect(path)
            assert structure(reopened) == expected
            assert reopened.execute("SELECT result FROM memorization_reviews WHERE id='new-review'").fetchone() == ("GOOD",)
            assert reopened.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
            for table, (columns, values) in before.items():
                rows = reopened.execute(f'SELECT {",".join(columns)} FROM "{table}"').fetchall()
                assert values in rows, (start,table)
            reopened.close()
        # Exercise the real DAO deletion templates on this synthetic file only.
        import re
        dao_source = (ROOT / "feature/src/main/java/com/example/feature/fasting/data/FastingLogDao.kt").read_text(encoding="utf-8")
        templates = re.findall(r'@Query\("(DELETE FROM fasting_log[^"\n]*)"\)', dao_source)
        delete_day = next(q for q in templates if "WHERE" in q)
        delete_all = next(q for q in templates if "WHERE" not in q)
        saved = sqlite3.connect(path)
        other_tables = [t for t in structure(saved) if t != "fasting_log"]
        other_rows = {t: saved.execute(f'SELECT * FROM "{t}"').fetchall() for t in other_tables}
        saved.execute("INSERT OR REPLACE INTO fasting_log VALUES (20000,'FASTED','personal',1234)")
        saved.execute("INSERT OR REPLACE INTO fasting_log VALUES (20000,'NOT_FASTED','edited',2345)")
        assert saved.execute("SELECT status FROM fasting_log WHERE dateEpochDay=20000").fetchall() == [("NOT_FASTED",)]
        saved.execute(delete_day, {"day": 20000})
        assert saved.execute("SELECT count(*) FROM fasting_log WHERE dateEpochDay=20000").fetchone()[0] == 0
        saved.execute("INSERT OR REPLACE INTO fasting_log VALUES (20001,'FASTED','',3456)")
        saved.execute(delete_all)
        assert saved.execute("SELECT count(*) FROM fasting_log").fetchone()[0] == 0
        for table, rows in other_rows.items():
            assert saved.execute(f'SELECT * FROM "{table}"').fetchall() == rows
        saved.commit()
        saved.close()
        reopened = sqlite3.connect(path)
        assert reopened.execute("SELECT count(*) FROM fasting_log").fetchone()[0] == 0
        assert reopened.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
        reopened.close()
    db.close()
    results.append({"from": start, "to": 11, "schema": "PASS", "preservation": "PASS", "integrity": "PASS", "fastingCrudDeleteIsolationReopen": "PASS"})

import argparse
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--output", type=Path, default=ROOT / ".artifacts/prebackend-batch03-migration.json")
out = parser.parse_args().output
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps({"kind": "desktop SQLite synthetic fixtures, not device acceptance", "paths": results}, indent=2))
print(json.dumps(results, indent=2))
