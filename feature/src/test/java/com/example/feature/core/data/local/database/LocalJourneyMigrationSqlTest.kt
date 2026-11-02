package com.example.feature.core.data.local.database

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.*
import org.junit.Test
import org.junit.Assert.*
import java.io.File

/** Capture the actual Kotlin migrations for execution by the desktop SQLite verifier. */
class LocalJourneyMigrationSqlTest {
    @Test fun `export actual supported migration SQL without accessing a user database`() {
        val migrations = buildJsonArray {
            IhsanDatabaseMigrations.ALL.forEach { migration ->
                val statements = mutableListOf<JsonObject>()
                val db = mockk<SupportSQLiteDatabase>()
                every { db.execSQL(any()) } answers {
                    statements += buildJsonObject { put("sql", firstArg<String>()); putJsonArray("args") {} }
                }
                every { db.execSQL(any(), any()) } answers {
                    statements += buildJsonObject {
                        put("sql", firstArg<String>())
                        putJsonArray("args") { secondArg<Array<Any?>>().forEach { add(JsonPrimitive(it?.toString())) } }
                    }
                }
                migration.migrate(db)
                if (migration.startVersion == 8) {
                    assertEquals(6, statements.size)
                    assertTrue(statements.all { it.getValue("sql").jsonPrimitive.content.startsWith("CREATE ") })
                }
                addJsonObject {
                    put("from", migration.startVersion); put("to", migration.endVersion)
                    put("statements", JsonArray(statements))
                }
            }
        }
        File("build/reports/local-journey").apply { mkdirs() }
            .resolve("migration-sql.json").writeText(migrations.toString())
    }
}
