package com.example.feature.dashboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards visual-only Home redesign boundaries: no new domain/scheduler/repository
 * ownership under dashboard presentation components.
 */
class HomeReferenceUiBoundaryTest {

    private val repoRoot = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .firstOrNull { File(it, "settings.gradle.kts").exists() }
            ?: dir
    }

    @Test
    fun `home presentation components do not import repositories or schedulers`() {
        val componentsDir = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/dashboard/presentation/components"
        )
        assertTrue("components dir missing: $componentsDir", componentsDir.isDirectory)
        val sources = componentsDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertTrue(sources.isNotEmpty())
        val banned = listOf(
            "import com.example.feature.prayer.domain",
            "AlarmManager",
            "RoomDatabase",
            "Repository",
            "koinViewModel",
            "DataStore"
        )
        sources.forEach { file ->
            val text = file.readText()
            banned.forEach { token ->
                assertFalse("${file.name} must not contain $token", text.contains(token))
            }
        }
    }

    @Test
    fun `home dashboard screen still uses HomeDashboardViewModel`() {
        val screen = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt"
        )
        val text = screen.readText()
        assertTrue(text.contains("HomeDashboardViewModel"))
        assertFalse(text.contains("DemoIhsanPlus"))
        assertTrue(text.contains("onNavigateToIhsanPlusDaily"))
    }
}
