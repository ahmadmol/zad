package com.example.feature.core.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Data-preserving migrations for [IhsanDatabase].
 *
 * Historical evidence (git):
 * - c0b7ac4: IhsanDatabase introduced at version 2
 * - a5b0fb8: version 3 + MIGRATION_2_3 (hadiths.explanation)
 * - 223c540: version bumped 3 → 5 with identical entity list (no schema SQL change)
 * - Version 4: never existed as a committed [IhsanDatabase] version (docs only)
 * - Version 6: UserEntity gained non-null [users.role] (no SQLite DEFAULT in final schema)
 */
object IhsanDatabaseMigrations {

    /**
     * v2 → v3: add nullable [hadiths.explanation].
     * Evidence: commit a5b0fb8 / HadithEntity + prior DatabaseModule migration.
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE hadiths ADD COLUMN explanation TEXT")
        }
    }

    /**
     * v3 → v5: no table/column/index changes.
     * Evidence: git diff a5b0fb8..223c540 for entity files is empty;
     * only Database version annotation changed from 3 to 5.
     *
     * Room still requires an explicit Migration when the version number increases.
     */
    val MIGRATION_3_5 = object : Migration(3, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Intentionally empty: schema identity between 3 and 5.
        }
    }

    /**
     * v5 → v6: add non-null [users.role] without a SQLite DEFAULT clause.
     *
     * Table-rebuild preserves all existing user rows and fills role = 'USER'.
     * Evidence: UserEntity + schema export identity change
     * 835d171ca156c029f2adb5df88a5c532 → 56390874ec553eb23c1300d91f52bfcd.
     * users has no indices / foreign keys in schema 5 or 6.
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `users_new` (
                  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  `firstName` TEXT NOT NULL,
                  `lastName` TEXT NOT NULL,
                  `phoneNumber` TEXT NOT NULL,
                  `city` TEXT NOT NULL,
                  `address` TEXT NOT NULL,
                  `role` TEXT NOT NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO `users_new` (
                  `id`, `firstName`, `lastName`, `phoneNumber`, `city`, `address`, `role`
                )
                SELECT
                  `id`, `firstName`, `lastName`, `phoneNumber`, `city`, `address`, 'USER'
                FROM `users`
                """.trimIndent()
            )
            database.execSQL("DROP TABLE `users`")
            database.execSQL("ALTER TABLE `users_new` RENAME TO `users`")
        }
    }

    /** v6 -> v7: local ownership and session state. Never infer ownership from names or phones. */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE donations ADD COLUMN ownerId TEXT")
            database.execSQL("ALTER TABLE donations ADD COLUMN origin TEXT NOT NULL DEFAULT 'LEGACY'")
            database.execSQL("ALTER TABLE users ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            database.execSQL("UPDATE users SET isActive = 0 WHERE id <> (SELECT MIN(id) FROM users)")
            // Neutralize only exact old bundled sample fingerprints. Preserve rows/IDs and all other legacy data.
            val samples = listOf(
                arrayOf("حقائب مدرسية لأطفال حلب", "جمعية الإحسان", "+963912345678", "drawable/ihsan_ehsan_school_backpack"),
                arrayOf("دعم أدوية لمرضى مزمن", "أبو أحمد", "+963912345679", "drawable/ihsan_ehsan_care_support"),
                arrayOf("ملابس شتوية للعائلات المحتاجة", "فاعل خير", "+963912345680", "drawable/ihsan_ehsan_winter_clothes")
            )
            samples.forEach { sample ->
                database.execSQL("UPDATE donations SET origin = 'DEMO', ownerId = 'demo:legacy', phoneNumber = '', title = '[DEMO] ' || title, description = 'مثال تجريبي محلي، لا تواصل أو تبرع حقيقي. ' || description WHERE title = ? AND donorName = ? AND phoneNumber = ? AND imageUrl = ?", sample)
            }
        }
    }

    /** Preserve existing behavior for older rows; new forms store the explicit selection. */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE donations ADD COLUMN contactMethod TEXT NOT NULL DEFAULT 'PHONE'")
        }
    }

    /** All migrations required to reach the current database version from supported sources. */
    val ALL: Array<Migration> = arrayOf(MIGRATION_2_3, MIGRATION_3_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
}
