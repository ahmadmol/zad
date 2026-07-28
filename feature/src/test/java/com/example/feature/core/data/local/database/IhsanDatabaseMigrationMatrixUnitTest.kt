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
    fun `supported migrations cover 2_3 and 3_5 and 5_6`() {
        val migrations = IhsanDatabaseMigrations.ALL
        assertEquals(3, migrations.size)
        assertEquals(2, migrations[0].startVersion)
        assertEquals(3, migrations[0].endVersion)
        assertEquals(3, migrations[1].startVersion)
        assertEquals(5, migrations[1].endVersion)
        assertEquals(5, migrations[2].startVersion)
        assertEquals(6, migrations[2].endVersion)
    }

    @Test
    fun `schema exports exist for versions 5 and 6`() {
        val schema5 = File(
            repoRoot,
            "feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase/5.json"
        )
        val schema6 = File(
            repoRoot,
            "feature/schemas/com.example.feature.core.data.local.database.IhsanDatabase/6.json"
        )
        assertTrue(schema5.exists())
        assertTrue(schema6.exists())
        val text5 = schema5.readText()
        val text6 = schema6.readText()
        assertTrue(text5.contains("\"version\": 5"))
        assertTrue(text5.contains("835d171ca156c029f2adb5df88a5c532"))
        assertFalse(text5.contains("\"columnName\": \"role\""))
        assertTrue(text6.contains("\"version\": 6"))
        assertTrue(text6.contains("\"columnName\": \"role\""))
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
        assertTrue(text.contains("migrate2To5") || text.contains("migrate2To6"))
        assertTrue(text.contains("migrate3To5") || text.contains("migrate3To6"))
        assertTrue(text.contains("migrate5To6") || text.contains("MigrationTestHelper"))
        assertTrue(text.contains("IhsanDatabaseMigrations.ALL"))
    }
}
