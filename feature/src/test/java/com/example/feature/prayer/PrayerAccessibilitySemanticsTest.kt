package com.example.feature.prayer

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Accessibility guard for the current prayer screen's consolidated active card. */
class PrayerAccessibilitySemanticsTest {
    private val screen by lazy {
        val root = File(".").canonicalFile.let { dir ->
            generateSequence(dir) { it.parentFile }
                .firstOrNull { File(it, "settings.gradle.kts").exists() } ?: dir
        }
        File(root, "feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt").readText()
    }

    @Test
    fun `active prayer card merges semantics to prevent double read`() {
        val body = functionBody("fun ActivePrayerCard(")
        assertTrue(body.contains("semantics(mergeDescendants = true)"))
        assertTrue(body.contains("contentDescription = \"${'$'}{prayer.nameAr}"))
    }

    @Test
    fun `active prayer description includes countdown and location`() {
        val body = functionBody("fun ActivePrayerCard(")
        assertTrue(body.contains("الوقت المتبقي ${'$'}countdown"))
        assertTrue(body.contains("${'$'}location"))
    }

    @Test
    fun `visible active prayer details remain rendered inside merged semantics`() {
        val body = functionBody("fun ActivePrayerCard(")
        assertTrue(body.contains("text = prayer.nameAr"))
        assertTrue(body.contains("text = \"الوقت المتبقي: ${'$'}countdown\""))
        assertTrue(body.contains("text = location"))
    }

    private fun functionBody(marker: String): String {
        val start = screen.indexOf(marker)
        assertTrue("$marker missing", start >= 0)
        val end = screen.indexOf("fun ", start + marker.length).let { if (it < 0) screen.length else it }
        return screen.substring(start, end)
    }
}
