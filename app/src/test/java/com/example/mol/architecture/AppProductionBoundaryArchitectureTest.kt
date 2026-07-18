package com.example.mol.architecture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppProductionBoundaryArchitectureTest {

    private val repoRoot: File = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }
    }

    @Test
    fun `appModule excludes ihsan plus modules`() {
        val text = File(repoRoot, "app/src/main/java/com/example/mol/di/AppModule.kt").readText()
        assertFalse(text.contains("ihsanPlusDailyModule"))
        assertFalse(text.contains("ihsanPlusCharityTrustModule"))
        assertTrue(text.contains("prayerDomainModule"))
        assertTrue(text.contains("homeDashboardModule"))
    }

    @Test
    fun `quran audio service remains non exported in manifest`() {
        val manifest = File(repoRoot, "app/src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("QuranAudioService"))
        assertTrue(
            Regex(
                """QuranAudioService[\s\S]{0,200}?android:exported="false""""
            ).containsMatchIn(manifest)
        )
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
    }

    @Test
    fun `no sebha production route remains`() {
        val screen = File(repoRoot, "app/src/main/java/com/example/mol/navigation/Screen.kt").readText()
        assertFalse(screen.contains("sebha", ignoreCase = true))
        assertTrue(screen.contains("tasbih_screen"))
    }
}
