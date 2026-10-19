package com.example.designsystem.component

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards that bottom-navigation motion polish stays presentation-only.
 */
class IhsanBottomNavigationMotionBoundaryTest {

    private val navFile = File(
        "src/main/java/com/example/designsystem/component/IhsanBottomNavigation.kt"
    )

    @Test
    fun `bottom navigation source exists`() {
        assertTrue(navFile.exists())
    }

    @Test
    fun `compact selection preserves tab semantics and presentation animation`() {
        val source = navFile.readText()
        assertFalse(source.contains("SelectedIconCircle"))
        assertFalse(source.contains("absoluteOffset"))
        assertTrue(source.contains("BottomNavigationLabel"))
        assertTrue(source.contains("animateFloatAsState"))
        assertTrue(source.contains("BottomNavBarHeight"))
        assertTrue(source.contains("role = Role.Tab"))
        assertTrue(source.contains("ihsan_bottom_nav_indicator"))
    }

    @Test
    fun `does not introduce navigation or data-layer dependencies`() {
        val source = navFile.readText()
        assertFalse(source.contains("NavController"))
        assertFalse(source.contains("rememberNavController"))
        assertFalse(source.contains("ViewModel"))
        assertFalse(source.contains("Repository"))
        assertFalse(source.contains("DataStore"))
        assertFalse(source.contains("Room"))
        assertFalse(source.contains("koin"))
        assertFalse(source.contains("org.koin"))
    }

}
