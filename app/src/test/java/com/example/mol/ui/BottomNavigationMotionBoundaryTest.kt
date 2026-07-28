package com.example.mol.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Ensures MainScreen motion wiring keeps navigation contracts unchanged.
 */
class BottomNavigationMotionBoundaryTest {

    private val mainScreen = File("src/main/java/com/example/mol/ui/MainScreen.kt").readText()
    private val resolver = File(
        "src/main/java/com/example/mol/navigation/BottomBarDestination.kt"
    ).readText()

    @Test
    fun `main screen keeps three destinations including حسابي`() {
        assertTrue(mainScreen.contains("Screen.Home"))
        assertTrue(mainScreen.contains("Screen.Donations"))
        assertTrue(mainScreen.contains("Screen.Profile"))
        assertTrue(mainScreen.contains("\"الرئيسية\""))
        assertTrue(mainScreen.contains("\"إحسان\""))
        assertTrue(mainScreen.contains("\"حسابي\""))
        assertTrue(mainScreen.contains("launchSingleTop = true"))
        assertTrue(mainScreen.contains("saveState = true"))
        assertTrue(mainScreen.contains("restoreState = true"))
        assertTrue(mainScreen.contains("findStartDestination()"))
    }

    @Test
    fun `main screen does not own indicator animation logic`() {
        assertFalse(mainScreen.contains("Animatable"))
        assertFalse(mainScreen.contains("AnimatedSelectionIndicator"))
        assertTrue(mainScreen.contains("IhsanBottomNavigation("))
        assertTrue(mainScreen.contains("onDestinationSelected"))
    }

    @Test
    fun `selection derives from resolver without Home fallback`() {
        assertTrue(mainScreen.contains("resolveBottomBarDestination"))
        assertTrue(mainScreen.contains("selectedIndex >= 0"))
        assertFalse(mainScreen.contains("coerceAtLeast(0)"))
        assertFalse(mainScreen.contains("coerceIn(0"))
        assertTrue(resolver.contains("PROFILE(Screen.Profile.route)"))
        assertTrue(resolver.contains("Screen.Profile.route -> BottomBarDestination.PROFILE"))
        assertFalse(resolver.contains("Screen.Home.route is hidden"))
    }

    @Test
    fun `profile tab navigates to Profile route once per selection`() {
        assertTrue(mainScreen.contains("navController.navigate(item.screen.route)"))
        assertTrue(mainScreen.contains("Screen.Profile"))
        // Single navigate call site for all tabs including حسابي.
        val navigateCount = Regex("navController\\.navigate\\(item\\.screen\\.route\\)")
            .findAll(mainScreen)
            .count()
        assertTrue(navigateCount == 1)
    }
}
