package com.example.mol.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Ensures MainScreen motion wiring keeps navigation contracts unchanged.
 */
class BottomNavigationMotionBoundaryTest {

    private val mainScreen = File("src/main/java/com/example/mol/ui/MainScreen.kt")

    @Test
    fun `main screen keeps three destinations and navigation flags`() {
        val source = mainScreen.readText()
        assertTrue(source.contains("Screen.Home"))
        assertTrue(source.contains("Screen.Donations"))
        assertTrue(source.contains("Screen.Profile"))
        assertTrue(source.contains("\"الرئيسية\""))
        assertTrue(source.contains("\"إحسان\""))
        assertTrue(source.contains("\"حسابي\""))
        assertTrue(source.contains("launchSingleTop = true"))
        assertTrue(source.contains("saveState = true"))
        assertTrue(source.contains("restoreState = true"))
        assertTrue(source.contains("findStartDestination()"))
    }

    @Test
    fun `main screen does not own indicator animation logic`() {
        val source = mainScreen.readText()
        assertFalse(source.contains("Animatable"))
        assertFalse(source.contains("AnimatedSelectionIndicator"))
        assertTrue(source.contains("IhsanBottomNavigation("))
        assertTrue(source.contains("onDestinationSelected"))
    }
}
