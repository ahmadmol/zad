package com.example.mol.di

import androidx.room.Room
import com.example.feature.core.data.local.database.IhsanDatabase
import com.example.feature.core.data.local.database.IhsanDatabaseMigrations
import com.example.feature.core.observability.AppLogger
import com.example.feature.core.observability.AppOperationalCategory
import com.example.feature.core.observability.AppOperationalEvent
import com.example.feature.core.observability.AppEventReporter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<IhsanDatabase> {
        val context = androidContext()
        val logger = get<AppLogger>()
        val reporter = get<AppEventReporter>()

        try {
        Room.databaseBuilder(
            context,
            IhsanDatabase::class.java,
            "ihsan_master_db"
        )
        // Data-preserving migrations only. No broad destructive fallback for production versions.
        .addMigrations(*IhsanDatabaseMigrations.ALL)
        .build()
        } catch (t: Throwable) {
            logger.error("IhsanDatabase", "database_open_or_migration_failed", t)
            reporter.report(
                AppOperationalEvent(
                    name = "database_open_failed",
                    category = AppOperationalCategory.DatabaseMigration,
                    attributes = mapOf("errorType" to (t::class.simpleName ?: "Throwable"))
                )
            )
            throw t
        }
    }

    single { get<IhsanDatabase>().azkarDao() }
    single { get<IhsanDatabase>().duaDao() }
    single { get<IhsanDatabase>().donationDao() }
    single { get<IhsanDatabase>().userDao() }
    single { get<IhsanDatabase>().hadithDao() }
    single { get<IhsanDatabase>().quranDao() }
    single { get<IhsanDatabase>().downloadDao() }
}
