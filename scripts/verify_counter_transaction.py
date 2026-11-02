"""Synthetic desktop SQLite checks using the unchanged Room 11 schema and actual DAO SQL.

This is not Android/Room execution. The instrumented counterpart is compile-only
until separately authorized device acceptance.
"""
import json
import re
import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DAO = ROOT / "feature/src/main/java/com/example/feature/azkar/data/local/dao/AzkarDao.kt"
SCHEMA = ROOT / "feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase/11.json"


def main():
    source = DAO.read_text(encoding="utf-8")
    queries = re.findall(r'@Query\("""(.*?)"""\)|@Query\("([^"\n]*)"\)', source, re.S)
    queries = [(multi or single).strip() for multi, single in queries]
    count_sql = next(q for q in queries if q.startswith("UPDATE azkar_table") and "dailyProgress" in q)
    stats_sql = next(q for q in queries if q.startswith("UPDATE daily_stats"))
    schema = json.loads(SCHEMA.read_text(encoding="utf-8"))
    db = sqlite3.connect(":memory:")
    for entity in schema["database"]["entities"]:
        if entity["tableName"] in ("azkar_table", "daily_stats"):
            db.execute(entity["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
    db.execute("INSERT INTO azkar_table VALUES (1,'fixture','synthetic',0,3,'fixture',0,'',0,'2026-11-01')")
    db.commit()

    def increment(day):
        with db:
            db.execute(count_sql, {"zikrId": 1, "date": day})
            if db.execute("SELECT id FROM daily_stats WHERE date=?", (day,)).fetchone():
                db.execute(stats_sql, {"date": day})
            else:
                db.execute("INSERT INTO daily_stats(date,totalCount) VALUES (?,1)", (day,))

    db.execute("CREATE TRIGGER fail_stats BEFORE INSERT ON daily_stats BEGIN SELECT RAISE(ABORT,'synthetic'); END")
    try:
        increment("2026-11-02")
        raise AssertionError("Injected failure was not observed")
    except sqlite3.IntegrityError:
        pass
    assert db.execute("SELECT currentCount,dailyProgress,lastUpdatedDate FROM azkar_table").fetchone() == (0, 0, "2026-11-01")
    assert db.execute("SELECT COUNT(*) FROM daily_stats").fetchone()[0] == 0
    db.execute("DROP TRIGGER fail_stats")
    db.executemany("INSERT INTO daily_stats(date,totalCount) VALUES ('2026-11-02',?)", [(10,), (20,)])
    db.commit()
    increment("2026-11-02")
    assert db.execute("SELECT SUM(totalCount),COUNT(*) FROM daily_stats").fetchone() == (31, 2)
    assert db.execute("SELECT currentCount,dailyProgress FROM azkar_table").fetchone() == (1, 1)
    increment("2026-11-03")
    assert db.execute("SELECT currentCount,dailyProgress FROM azkar_table").fetchone() == (1, 1)
    db.execute("UPDATE azkar_table SET category='تسبيح',currentCount=7")
    db.commit()
    increment("2026-11-04")
    assert db.execute("SELECT currentCount,dailyProgress FROM azkar_table").fetchone() == (8, 1)
    assert db.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
    generated = ROOT / "feature/build/generated/ksp/debug/java/com/example/feature/azkar/data/local/dao/AzkarDao_IhsanDatabase_Impl.java"
    if generated.exists():
        body = generated.read_text(encoding="utf-8")
        method = body[body.index("incrementCounterAndStat("):]
        assert "withTransaction" in method[:1700] or "performInTransactionSuspending" in method[:1700]
    db.close()
    print("PASS: rollback, duplicate-row preservation, civil-day rollover, lifetime counter, integrity; synthetic desktop SQLite only")


if __name__ == "__main__":
    main()
