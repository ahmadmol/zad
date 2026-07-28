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
    fun `main screen shows bottom bar on Home Ehsan and Profile roots`() {
        val main = File(
            repoRoot,
            "app/src/main/java/com/example/mol/ui/MainScreen.kt"
        ).readText()
        val resolver = File(
            repoRoot,
            "app/src/main/java/com/example/mol/navigation/BottomBarDestination.kt"
        ).readText()
        assertTrue(main.contains("Screen.Home"))
        assertTrue(main.contains("Screen.Donations"))
        assertTrue(main.contains("Screen.Profile"))
        assertTrue(main.contains("\"حسابي\""))
        assertTrue(main.contains("resolveBottomBarDestination"))
        assertTrue(main.contains("selectedIndex >= 0"))
        assertTrue(resolver.contains("BottomBarDestination.PROFILE"))
        assertTrue(resolver.contains("Screen.Profile.route -> BottomBarDestination.PROFILE"))
        assertFalse(main.contains("coerceAtLeast(0)"))
    }
}
