package com.example.feature.quran.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecitationSourceCatalogTest {
    @Test fun `reviewed sources stay documented and playback stays disabled`() {
        assertEquals(3, RecitationSourceCatalog.reviewed.size)
        assertTrue(RecitationSourceCatalog.reviewed.all { it.documentationUrl.startsWith("https://") })
        assertFalse(RecitationAvailability.RECORDINGS_APPROVED)
        assertTrue(RecitationSourceCatalog.reviewed.single { it.name == "mp3quran.net" }.decision.contains("Rejected"))
        assertTrue(RecitationSourceCatalog.singleReciterRequirements.any { it.contains("Written license") })
        assertTrue(RecitationSourceCatalog.singleReciterRequirements.any { it.contains("verse-level") })
    }

    @Test fun `the license notice is not rendered as a playback retry over the mushaf`() {
        val root = generateSequence(java.io.File(".").canonicalFile) { it.parentFile }
            .first { java.io.File(it, "settings.gradle.kts").exists() }
        val text = java.io.File(root, "feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt").readText()
        assertTrue(text.contains("state.errorMessage != com.example.feature.quran.util.RecitationAvailability.MESSAGE"))
        assertTrue(text.contains("chromeVisible && kotlin.math.abs(state.fontSize - 28f) > 1f"))
    }
}
