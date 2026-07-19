package com.example.designsystem.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Lightweight guards for Dark Mode foundation completeness.
 */
class DarkModeFoundationBoundaryTest {

    @Test
    fun `theme defines explicit dark surface hierarchy roles`() {
        val theme = File("src/main/java/com/example/designsystem/theme/Theme.kt").readText()
        assertTrue(theme.contains("surfaceContainerLow"))
        assertTrue(theme.contains("surfaceContainerHigh"))
        assertTrue(theme.contains("outlineVariant"))
        assertTrue(theme.contains("errorContainer"))
        assertTrue(theme.contains("scrim"))
        assertTrue(theme.contains("isAppearanceLightStatusBars"))
        assertTrue(theme.contains("isAppearanceLightNavigationBars"))
    }

    @Test
    fun `dark interactive primary is not the charcoal background`() {
        assertFalse(DarkInteractivePrimary == DarkBackground)
        assertFalse(DarkInteractivePrimary == DarkSurface)
    }

    @Test
    fun `semantic navigation tokens exist for both themes`() {
        val light = LightIhsanSemanticColors
        val dark = DarkIhsanSemanticColors
        assertTrue(light.navigationSurface != dark.navigationSurface)
        assertTrue(light.navigationIndicator != dark.navigationIndicator)
        assertTrue(light.fieldContainer != dark.fieldContainer)
        assertTrue(dark.surfaceElevated != dark.surfaceBase)
    }
}
