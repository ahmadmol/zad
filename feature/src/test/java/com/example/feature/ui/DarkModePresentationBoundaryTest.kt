package com.example.feature.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards critical screens against reintroducing Light-only scaffolds.
 */
class DarkModePresentationBoundaryTest {

    private fun read(path: String): String = File(path).readText()

    @Test
    fun `auth and prayer sheets do not hardcode white containers`() {
        val files = listOf(
            "src/main/java/com/example/feature/components/AuthBottomSheet.kt",
            "src/main/java/com/example/feature/prayer/presentation/PrayerDetailsBottomSheet.kt",
            "src/main/java/com/example/feature/prayer/presentation/PrayerSettingsBottomSheet.kt",
            "src/main/java/com/example/feature/prayer/presentation/CitySelectionBottomSheet.kt"
        )
        files.forEach { path ->
            val source = read(path)
            assertFalse("$path uses Color.White container", source.contains("containerColor = Color.White"))
            assertTrue("$path exists", File(path).exists())
        }
    }

    @Test
    fun `dua and request help do not use white scaffolds`() {
        val dua = read("src/main/java/com/example/feature/duas/DuaScreen.kt")
        val request = read("src/main/java/com/example/feature/ehsan/presentation/RequestHelpScreen.kt")
        assertFalse(dua.contains("containerColor = Color.White"))
        assertFalse(dua.contains(".background(Color.White)"))
        assertFalse(request.contains("containerColor = Color.White"))
    }

    @Test
    fun `live stream may keep black player chrome`() {
        val live = read("src/main/java/com/example/feature/live/LiveStreamScreen.kt")
        assertTrue(live.contains("Color.Black"))
    }
}
