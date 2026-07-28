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

    /** All migrations required to reach the current database version from supported sources. */
    val ALL: Array<Migration> = arrayOf(MIGRATION_2_3, MIGRATION_3_5, MIGRATION_5_6)
}
