import sqlite3

DB = r"ihsan_master_db"
c = sqlite3.connect(DB)
cur = c.cursor()

print("=== sqlite_master full dump (no filter) ===")
for r in cur.execute("SELECT * FROM sqlite_master"):
    print(" ", r[0], "|", r[1], "|", r[2])

print()
print("=== Try opening with isolation_level=None (no implicit txn) ===")
c2 = sqlite3.connect(DB, isolation_level=None)
cur2 = c2.cursor()
for r in cur2.execute("SELECT * FROM sqlite_master"):
    print(" ", r[0], "|", r[1], "|", r[2])

print()
print("=== After read, retry azkar count ===")
print(" azkar count:", cur.execute("SELECT COUNT(*) FROM azkar_table").fetchone()[0])
print(" ayahs count:", cur.execute("SELECT COUNT(*) FROM ayahs").fetchone()[0])
print(" duas count:", cur.execute("SELECT COUNT(*) FROM duas").fetchone()[0])
print(" hadiths count:", cur.execute("SELECT COUNT(*) FROM hadiths").fetchone()[0])
print(" bookmarks count:", cur.execute("SELECT COUNT(*) FROM bookmarks").fetchone()[0])
print(" donations count:", cur.execute("SELECT COUNT(*) FROM donations").fetchone()[0])
print(" daily_stats count:", cur.execute("SELECT COUNT(*) FROM daily_stats").fetchone()[0])
print(" users count:", cur.execute("SELECT COUNT(*) FROM users").fetchone()[0])
print(" surahs count:", cur.execute("SELECT COUNT(*) FROM surahs").fetchone()[0])
print(" downloaded_ayahs count:", cur.execute("SELECT COUNT(*) FROM downloaded_ayahs").fetchone()[0])
