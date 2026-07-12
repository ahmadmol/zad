package com.example.feature.core.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented migration tests for [IhsanDatabase].
 *
 * Historical schemas are created with SQL derived from entity definitions at the
 * corresponding git versions (v2 vs v3/v5), not fabricated Room schema JSON.
 */
@RunWith(AndroidJUnit4::class)
class IhsanDatabaseMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "ihsan_migration_test.db"

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun migrate3To5_preservesRepresentativeData() {
        createDatabaseAtVersion(3) { db -> insertV3RepresentativeData(db) }

        val roomDb = openMigratedDatabase()
        try {
            assertPreservedV3Data(roomDb)
            assertEquals(5, roomDb.openHelper.readableDatabase.version)
        } finally {
            roomDb.close()
        }
    }

    @Test
    fun migrate2To5_addsExplanationAndPreservesData() {
        createDatabaseAtVersion(2) { db -> insertV2RepresentativeData(db) }

        val roomDb = openMigratedDatabase()
        try {
            val sqlite = roomDb.openHelper.readableDatabase
            assertEquals(5, sqlite.version)
            assertTrue(hasColumn(sqlite, "hadiths", "explanation"))

            sqlite.query("SELECT text, explanation FROM hadiths WHERE id = 201").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("حديث تجريبي", c.getString(0))
                assertTrue(c.isNull(1))
            }
            sqlite.query("SELECT phoneNumber FROM users WHERE id = 1").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("0500000001", c.getString(0))
            }
            sqlite.query("SELECT currentCount FROM azkar_table WHERE id = 11").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals(3, c.getInt(0))
            }
        } finally {
            roomDb.close()
        }
    }

    @Test
    fun migrate3To5_secondOpenDoesNotWipeData() {
        createDatabaseAtVersion(3) { db -> insertV3RepresentativeData(db) }
        openMigratedDatabase().close()

        val roomDb = openMigratedDatabase()
        try {
            assertPreservedV3Data(roomDb)
        } finally {
            roomDb.close()
        }
    }

    private fun openMigratedDatabase(): IhsanDatabase {
        return Room.databaseBuilder(context, IhsanDatabase::class.java, dbName)
            .addMigrations(*IhsanDatabaseMigrations.ALL)
            .allowMainThreadQueries()
            .build()
    }

    private fun createDatabaseAtVersion(version: Int, populate: (SupportSQLiteDatabase) -> Unit) {
        context.deleteDatabase(dbName)
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    createSchema(db, includeHadithExplanation = version >= 3)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    error("Unexpected upgrade in fixture open helper")
                }
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase
        populate(db)
        db.close()
        helper.close()
    }

    private fun createSchema(db: SupportSQLiteDatabase, includeHadithExplanation: Boolean) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `azkar_table` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `title` TEXT NOT NULL,
              `text` TEXT NOT NULL,
              `currentCount` INTEGER NOT NULL,
              `targetCount` INTEGER NOT NULL,
              `category` TEXT NOT NULL,
              `isFavorite` INTEGER NOT NULL,
              `source` TEXT NOT NULL,
              `dailyProgress` INTEGER NOT NULL,
              `lastUpdatedDate` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `daily_stats` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `date` TEXT NOT NULL,
              `totalCount` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `duas` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `title` TEXT NOT NULL,
              `text` TEXT NOT NULL,
              `category` TEXT NOT NULL,
              `source` TEXT NOT NULL,
              `reference` TEXT NOT NULL,
              `isFavorite` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `donations` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `title` TEXT NOT NULL,
              `description` TEXT NOT NULL,
              `category` TEXT NOT NULL,
              `location` TEXT NOT NULL,
              `type` TEXT NOT NULL,
              `status` TEXT NOT NULL,
              `donorName` TEXT NOT NULL,
              `phoneNumber` TEXT NOT NULL,
              `imageUrl` TEXT,
              `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `users` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `firstName` TEXT NOT NULL,
              `lastName` TEXT NOT NULL,
              `phoneNumber` TEXT NOT NULL,
              `city` TEXT NOT NULL,
              `address` TEXT NOT NULL
            )
            """.trimIndent()
        )
        if (includeHadithExplanation) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `hadiths` (
                  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  `text` TEXT NOT NULL,
                  `narrator` TEXT NOT NULL,
                  `source` TEXT NOT NULL,
                  `category` TEXT NOT NULL,
                  `isFavorite` INTEGER NOT NULL,
                  `explanation` TEXT
                )
                """.trimIndent()
            )
        } else {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `hadiths` (
                  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  `text` TEXT NOT NULL,
                  `narrator` TEXT NOT NULL,
                  `source` TEXT NOT NULL,
                  `category` TEXT NOT NULL,
                  `isFavorite` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `surahs` (
              `id` INTEGER NOT NULL,
              `name` TEXT NOT NULL,
              `englishName` TEXT NOT NULL,
              `revelationType` TEXT NOT NULL,
              `totalVerses` INTEGER NOT NULL,
              `startPage` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ayahs` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `surahId` INTEGER NOT NULL,
              `verseNumber` INTEGER NOT NULL,
              `text` TEXT NOT NULL,
              `tafsir` TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_ayahs_surahId_verseNumber` ON `ayahs` (`surahId`, `verseNumber`)"
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `bookmarks` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `surahId` INTEGER NOT NULL,
              `verseNumber` INTEGER NOT NULL,
              `timestamp` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `downloaded_ayahs` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `surahId` INTEGER NOT NULL,
              `verseNumber` INTEGER NOT NULL,
              `readerId` TEXT NOT NULL,
              `localPath` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun insertV2RepresentativeData(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO users (id, firstName, lastName, phoneNumber, city, address) VALUES (1, 'Test', 'User', '0500000001', 'Aleppo', 'Addr')"
        )
        db.execSQL(
            "INSERT INTO hadiths (id, text, narrator, source, category, isFavorite) VALUES (201, 'حديث تجريبي', 'راوي', 'مصدر', 'عام', 0)"
        )
        db.execSQL(
            "INSERT INTO azkar_table (id, title, text, currentCount, targetCount, category, isFavorite, source, dailyProgress, lastUpdatedDate) VALUES (11, 'تسبيح', 'سبحان الله', 3, 33, 'تسبيح', 0, '', 0, '2026-01-01')"
        )
    }

    private fun insertV3RepresentativeData(db: SupportSQLiteDatabase) {
        insertV2RepresentativeData(db)
        db.execSQL("UPDATE hadiths SET explanation = 'شرح تجريبي' WHERE id = 201")
        db.execSQL(
            "INSERT INTO donations (id, title, description, category, location, type, status, donorName, phoneNumber, imageUrl, createdAt) VALUES (301, 'مساعدة طبية', 'وصف الطلب', 'صحة', 'دمشق', 'REQUEST', 'AVAILABLE', 'متبرع', '0500000002', NULL, 1700000000000)"
        )
        db.execSQL(
            "INSERT INTO duas (id, title, text, category, source, reference, isFavorite) VALUES (401, 'دعاء', 'نص الدعاء', 'صباح', 'مصدر', 'مرجع', 1)"
        )
        db.execSQL(
            "INSERT INTO daily_stats (id, date, totalCount) VALUES (501, '2026-01-01', 42)"
        )
        db.execSQL(
            "INSERT INTO surahs (id, name, englishName, revelationType, totalVerses, startPage) VALUES (1, 'الفاتحة', 'Al-Fatiha', 'Meccan', 7, 1)"
        )
        db.execSQL(
            "INSERT INTO ayahs (id, surahId, verseNumber, text, tafsir) VALUES (601, 1, 1, 'بسم الله', NULL)"
        )
        db.execSQL(
            "INSERT INTO bookmarks (id, surahId, verseNumber, timestamp) VALUES (701, 1, 1, 1700000000001)"
        )
        db.execSQL(
            "INSERT INTO downloaded_ayahs (id, surahId, verseNumber, readerId, localPath) VALUES (801, 1, 1, 'Alafasy_128kbps', '/tmp/1.mp3')"
        )
    }

    private fun assertPreservedV3Data(roomDb: IhsanDatabase) {
        val db = roomDb.openHelper.readableDatabase
        db.query("SELECT firstName, phoneNumber FROM users WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Test", c.getString(0))
            assertEquals("0500000001", c.getString(1))
        }
        db.query("SELECT title, phoneNumber, createdAt FROM donations WHERE id = 301").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("مساعدة طبية", c.getString(0))
            assertEquals("0500000002", c.getString(1))
            assertEquals(1700000000000L, c.getLong(2))
        }
        db.query("SELECT currentCount, targetCount FROM azkar_table WHERE id = 11").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(3, c.getInt(0))
            assertEquals(33, c.getInt(1))
        }
        db.query("SELECT isFavorite FROM duas WHERE id = 401").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(1, c.getInt(0))
        }
        db.query("SELECT text, explanation FROM hadiths WHERE id = 201").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("حديث تجريبي", c.getString(0))
            assertEquals("شرح تجريبي", c.getString(1))
        }
        db.query("SELECT name FROM surahs WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("الفاتحة", c.getString(0))
        }
        db.query("SELECT surahId FROM bookmarks WHERE id = 701").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(1, c.getInt(0))
        }
        db.query("SELECT readerId FROM downloaded_ayahs WHERE id = 801").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Alafasy_128kbps", c.getString(0))
        }
        db.query("SELECT totalCount FROM daily_stats WHERE id = 501").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(42, c.getInt(0))
        }
        assertTrue(hasColumn(db, "hadiths", "explanation"))
    }

    private fun hasColumn(db: SupportSQLiteDatabase, table: String, column: String): Boolean {
        db.query("PRAGMA table_info(`$table`)").use { c ->
            val nameIndex = c.getColumnIndex("name")
            while (c.moveToNext()) {
                if (c.getString(nameIndex) == column) return true
            }
        }
        return false
    }
}
