package com.example.feature.ehsan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EhsanReferenceUiBoundaryTest {

    private val repoRoot = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .firstOrNull { File(it, "settings.gradle.kts").exists() }
            ?: dir
    }

    @Test
    fun `ehsan ui components stay free of repositories and viewmodels`() {
        val dir = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ehsan/presentation/components"
        )
        assertTrue(dir.isDirectory)
        dir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            assertFalse(file.name, text.contains("koinViewModel"))
            assertFalse(file.name, text.contains("GetDonationsUseCase"))
            assertFalse(file.name, text.contains("RoomDatabase"))
            assertFalse(file.name, text.contains("DataStore"))
        }
    }

    @Test
    fun `ehsan screen keeps viewmodel callbacks and local-board honesty symbol`() {
        val screen = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ehsan/EhsanScreen.kt"
        ).readText()
        val components = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ehsan/presentation/components/EhsanUiComponents.kt"
        ).readText()
        assertTrue(screen.contains("EhsanViewModel"))
        assertTrue(screen.contains("onSearchQueryChange"))
        assertTrue(screen.contains("onTypeChange"))
        assertTrue(screen.contains("onSortChange"))
        assertTrue(components.contains("LocalBoardNoticeCard"))
        assertTrue(screen.contains("LocalBoardNoticeCard()"))
        assertFalse(screen.contains("\"١٢\""))
        assertFalse(screen.contains("trust"))
        assertFalse(screen.contains("موثق"))
    }

    @Test
    fun `ehsan root renders no back affordance while keeping the callback wired`() {
        val screen = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ehsan/EhsanScreen.kt"
        ).readText()
        val components = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ehsan/presentation/components/EhsanUiComponents.kt"
        ).readText()

        // Ehsan is a root bottom-navigation destination: the icon is opt-in and off by default.
        assertTrue(screen.contains("showNavigationIcon: Boolean = false"))
        // The callback itself is preserved, not deleted.
        assertTrue(screen.contains("onNavigateBack: () -> Unit = {}"))
        // The root screen does not render an inline back affordance.
        assertFalse(screen.contains("EhsanTopBar("))
    }

    @Test
    fun `bottom navigation file unchanged by ehsan redesign`() {
        // Guard: this task forbids bottom-nav edits; file may still exist from prior work.
        val main = File(repoRoot, "app/src/main/java/com/example/mol/ui/MainScreen.kt")
        assertTrue(main.exists())
        val ehsanComponents = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ehsan/presentation/components/EhsanUiComponents.kt"
        ).readText()
        assertFalse(ehsanComponents.contains("IhsanBottomNavigation"))
        assertFalse(ehsanComponents.contains("NavigationBar"))
    }
}
