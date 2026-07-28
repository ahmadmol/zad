package com.example.feature.dashboard

import com.example.feature.dashboard.presentation.ArabicClockFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Calendar
import java.util.GregorianCalendar

class HomeFeaturePreservationTest {

    private val repoRoot = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .firstOrNull { File(it, "settings.gradle.kts").exists() }
            ?: dir
    }

    private fun read(relativePath: String): String =
        File(repoRoot, relativePath).readText()

    private val homeScreen by lazy {
        read("feature/src/main/java/com/example/feature/dashboard/HomeDashboardScreen.kt")
    }

    private val quickActions by lazy {
        read(
            "feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeQuickActions.kt"
        )
    }

    private val contentCards by lazy {
        read(
            "feature/src/main/java/com/example/feature/dashboard/presentation/components/HomeContentCards.kt"
        )
    }

    private val splash by lazy {
        read("feature/src/main/java/com/example/feature/splashScreen/SplashScreen.kt")
    }

    @Test
    fun `home keeps all legacy service routes and callbacks`() {
        val requiredRoutes = listOf(
            "\"hadith\"",
            "\"azkar\"",
            "\"tasbih\"",
            "\"quran\"",
            "\"dua\"",
            "\"asma\"",
            "\"qibla\"",
            "\"prayer\"",
            "\"haram\"",
            "\"nabawi\"",
            "\"search\"",
            "\"daily\"",
            "\"reminders\"",
            "\"statistics\""
        )
        requiredRoutes.forEach { route ->
            assertTrue("Missing route $route", homeScreen.contains(route))
        }

        val requiredCallbacks = listOf(
            "onNavigateToHadith",
            "onNavigateToAzkar",
            "onNavigateToTasbih",
            "onNavigateToQuran",
            "onNavigateToDua",
            "onNavigateToAsma",
            "onNavigateToQibla",
            "onNavigateToPrayer",
            "onNavigateToHaramLive",
            "onNavigateToNabawiLive",
            "onNavigateToSearch",
            "onNavigateToDailyActivities",
            "onNavigateToReminders",
            "onNavigateToStatistics"
        )
        requiredCallbacks.forEach { callback ->
            assertTrue("Missing callback $callback", homeScreen.contains(callback))
        }
    }

    @Test
    fun `home does not hide services with take 5`() {
        assertFalse(homeScreen.contains("actions.take(5)"))
        assertTrue(homeScreen.contains("HomeServicesSection"))
        assertTrue(quickActions.contains("HomeServicesSection"))
        assertTrue(
            quickActions.contains("home_services_title") || quickActions.contains("الخدمات")
        )
    }

    @Test
    fun `live chooser and both streams remain reachable`() {
        assertTrue(homeScreen.contains("live_chooser"))
        assertTrue(homeScreen.contains("onNavigateToHaramLive()"))
        assertTrue(homeScreen.contains("onNavigateToNabawiLive()"))
        assertTrue(homeScreen.contains("\"haram\""))
        assertTrue(homeScreen.contains("\"nabawi\""))
    }

    @Test
    fun `shortcut card uses row layout without overlapping box icon`() {
        assertTrue(
            "HomeShortcutCard missing",
            contentCards.contains("internal fun HomeShortcutCard")
        )
        assertTrue("weight(1f) missing", contentCards.contains("weight(1f)"))
        assertTrue("width(42.dp) missing", contentCards.contains("width(42.dp)"))
        assertFalse(
            "TopStart overlay still present",
            contentCards.contains("Alignment.TopStart")
        )
        assertFalse(
            "TopEnd overlay still present",
            contentCards.contains("Alignment.TopEnd")
        )
        assertTrue("Row layout missing", contentCards.contains("Row("))
    }

    @Test
    fun `nearby charity empty state is Arabic`() {
        assertTrue(contentCards.contains("home_nearby_charity_empty_title"))
        assertTrue(contentCards.contains("home_nearby_charity_empty_body"))
        assertFalse(contentCards.contains("No data available yet"))
    }

    @Test
    fun `splash uses Fit and not Crop`() {
        assertTrue(splash.contains("ContentScale.Fit"))
        assertFalse(splash.contains("ContentScale.Crop"))
        assertTrue(splash.contains("aspectRatio"))
    }
}

class ArabicClockFormatterTest {

    @Test
    fun `formats morning and afternoon with Arabic markers`() {
        val morning = GregorianCalendar(2026, Calendar.JULY, 28, 3, 54, 0).time
        val afternoon = GregorianCalendar(2026, Calendar.JULY, 28, 16, 26, 0).time
        assertEquals("03:54 ص", ArabicClockFormatter.format(morning))
        assertEquals("04:26 م", ArabicClockFormatter.format(afternoon))
    }

    @Test
    fun `sanitize replaces AM PM`() {
        assertEquals("03:54 ص", ArabicClockFormatter.sanitize("03:54 AM"))
        assertEquals("12:39 م", ArabicClockFormatter.sanitize("12:39 PM"))
        assertFalse(ArabicClockFormatter.sanitize("03:54 AM").contains("AM"))
        assertFalse(ArabicClockFormatter.sanitize("12:39 PM").contains("PM"))
    }
}
