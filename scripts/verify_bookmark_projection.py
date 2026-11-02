"""Exercise the actual Room bookmark projection SQL on isolated schema-11 SQLite fixtures."""
import json
import re
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "feature/src/main/java/com/example/feature/quran/data/local/dao/QuranDao.kt").read_text(encoding="utf-8")
query = next(q for q in re.findall(r'@Query\("""(.*?)"""\)', source, re.S) if "SELECT b.id" in q)
schema = json.loads((ROOT / "feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase/11.json").read_text(encoding="utf-8"))["database"]
db = sqlite3.connect(":memory:")
for table in schema["entities"]:
    db.execute(table["createSql"].replace("${TABLE_NAME}", table["tableName"]))
    for index in table["indices"]:
        db.execute(index["createSql"].replace("${TABLE_NAME}", table["tableName"]))
db.execute("INSERT INTO surahs VALUES (1,'QA source name','QA','QA',7,1)")
db.execute("INSERT INTO ayahs VALUES (1,1,1,'unchanged synthetic source text',NULL)")
db.executemany("INSERT INTO bookmarks VALUES (?,?,?,?)", [(1,1,1,100), (2,999,1,200), (3,1,1,300)])
before = db.execute("SELECT * FROM bookmarks ORDER BY id").fetchall()
rows = db.execute(query).fetchall()
assert [row[0] for row in rows] == [3,2,1]
assert rows[0][4] == "unchanged synthetic source text"
assert rows[1][3:] == ("", "")
assert db.execute("SELECT * FROM bookmarks ORDER BY id").fetchall() == before
assert db.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
# Record the query plan without inventing latency or device-performance claims.
result = {"scope":"desktop SQLite synthetic fixture; not Android acceptance", "projection":"PASS", "orphanRetention":"PASS", "legacyDuplicateBookmarksRetained":"PASS", "rowPreservation":"PASS", "queryPlan": db.execute("EXPLAIN QUERY PLAN " + query).fetchall()}
# Exercise the exact newly atomic favorite SQL without touching a user database.
for table, path in [("duas", "duas/data/local/dao/DuaDao.kt"), ("hadiths", "hadith/data/local/dao/HadithDao.kt")]:
    definition = next(e for e in schema["entities"] if e["tableName"] == table)
    columns = [f["columnName"] for f in definition["fields"]]
    values = [1 if f["affinity"] in ("INTEGER", "REAL") else "synthetic" for f in definition["fields"]]
    values[columns.index("isFavorite")] = 0
    db.execute(f'INSERT INTO "{table}" ({",".join(columns)}) VALUES ({",".join("?" for _ in values)})', values)
    before = db.execute(f'SELECT * FROM "{table}"').fetchall()
    dao = (ROOT / "feature/src/main/java/com/example/feature" / path).read_text(encoding="utf-8")
    update = next(q for q in re.findall(r'@Query\("([^"\n]+)"\)', dao) if "SET isFavorite = NOT isFavorite" in q)
    for _ in range(40): db.execute(update, {"id": 1})
    assert db.execute(f'SELECT * FROM "{table}"').fetchall() == before
    db.execute(update, {"id": 1})
    after = list(db.execute(f'SELECT * FROM "{table}"').fetchone())
    expected = list(before[0]); expected[columns.index("isFavorite")] = 1
    assert after == expected
result["atomicDuaHadithFavoriteSql"] = "PASS (40 even toggles then one odd toggle; every other column retained)"
import argparse
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--output", type=Path, default=ROOT/".artifacts/prebackend-batch04-20261021/bookmark-sql-verification.json")
out=parser.parse_args().output
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps(result,indent=2),encoding="utf-8")
print(json.dumps(result,indent=2))
