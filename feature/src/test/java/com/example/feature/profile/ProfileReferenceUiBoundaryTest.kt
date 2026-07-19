package com.example.feature.profile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProfileReferenceUiBoundaryTest {

    private val repoRoot = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .firstOrNull { File(it, "settings.gradle.kts").exists() }
            ?: dir
    }

    @Test
    fun `profile presentation components stay free of repositories and viewmodels`() {
        val dir = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/profile/presentation/components"
        )
        assertTrue(dir.isDirectory)
        dir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            assertFalse(file.name, text.contains("Repository"))
            assertFalse(file.name, text.contains("koinViewModel"))
            assertFalse(file.name, text.contains("RoomDatabase"))
            assertFalse(file.name, text.contains("DataStore"))
        }
    }

    @Test
    fun `profile screen keeps ProfileViewModel and local-profile wording`() {
        val screen = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/profile/ProfileScreen.kt"
        ).readText()
        assertTrue(screen.contains("ProfileViewModel"))
        assertTrue(screen.contains("viewModel.logout()"))
        assertFalse(screen.contains("محسن متميز"))
        assertTrue(screen.contains("نشاطك المحلي"))
    }

    @Test
    fun `main screen hides bottom bar only on profile route`() {
        val main = File(
            repoRoot,
            "app/src/main/java/com/example/mol/ui/MainScreen.kt"
        ).readText()
        assertTrue(main.contains("Screen.Home.route"))
        assertTrue(main.contains("Screen.Donations.route"))
        assertTrue(main.contains("Screen.Profile"))
        assertFalse(
            main.contains("mainItems.any { it.screen.route == currentDestination?.route }")
        )
    }
}
