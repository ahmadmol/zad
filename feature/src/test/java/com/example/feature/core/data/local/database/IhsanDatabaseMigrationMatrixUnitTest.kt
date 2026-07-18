package com.example.feature.core.data.local.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IhsanDatabaseMigrationMatrixUnitTest {

    private val repoRoot: File = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }
    }

    @Test
    fun `supported migrations cover 2_3 and 3_5 only`() {
        val migrations = IhsanDatabaseMigrations.ALL
        assertEquals(2, migrations.size)
        assertEquals(2, migrations[0].startVersion)
        assertEquals(3, migrations[0].endVersion)
        assertEquals(3, migrations[1].startVersion)
        assertEquals(5, migrations[1].endVersion)
    }

    @Test
    fun `current schema export exists for version 5`() {
        val schema = File(
            repoRoot,
            "feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase/5.json"
        )
        assertTrue(schema.exists())
        val text = schema.readText()
        assertTrue(text.contains("\"version\": 5") || text.contains("\"identityHash\""))
    }

    @Test
    fun `database module forbids destructive fallback`() {
        val module = File(
            repoRoot,
            "app/src/main/java/com/example/mol/di/DatabaseModule.kt"
        ).readText()
        assertFalse(module.contains("fallbackToDestructiveMigration"))
    }

    @Test
    fun `instrumented migration test class exists`() {
        val test = File(
            repoRoot,
            "feature/src/androidTest/java/com/example/feature/core/data/local/database/IhsanDatabaseMigrationTest.kt"
        )
        assertTrue(test.exists())
        val text = test.readText()
        assertTrue(text.contains("migrate2To5"))
        assertTrue(text.contains("migrate3To5"))
        assertTrue(text.contains("IhsanDatabaseMigrations.ALL"))
    }
}
