package com.example.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.*
import org.junit.Test

class IhsanPaletteTest {
    private fun ratio(a: Color, b: Color): Float {
        val x = a.compositeOver(b).luminance(); val y = b.luminance()
        return (maxOf(x, y) + .05f) / (minOf(x, y) + .05f)
    }

    @Test fun `all six palettes keep primary text actions and selected text readable`() {
        IhsanPalette.entries.forEach { palette -> listOf(false, true).forEach { dark ->
            val c = ihsanSemanticColors(palette, dark)
            listOf(c.textPrimary to c.surfaceBase, c.textBody to c.surfaceElevated,
                c.textSecondary to c.surfaceMuted, c.onBrand to c.brand, c.onBrand to c.brandElevated,
                c.selectedContent to c.selectedContainer, c.accentContent to c.surfaceElevated,
                c.unselectedContent to c.navigationSurface).forEach { (fg, bg) ->
                assertTrue("$palette dark=$dark: ${ratio(fg, bg)}", ratio(fg, bg) >= 4.5f)
            }
        } }
    }

    @Test fun `unknown persisted palettes preserve a usable default`() {
        assertEquals(IhsanPalette.EMERALD, IhsanPalette.fromStorage(null))
        assertEquals(IhsanPalette.EMERALD, IhsanPalette.fromStorage("FUTURE_VALUE"))
        IhsanPalette.entries.forEach { assertEquals(it, IhsanPalette.fromStorage(it.name)) }
    }

    @Test fun `all six Material schemes have readable paired control and container roles`() {
        IhsanPalette.entries.forEach { palette -> listOf(false, true).forEach { dark ->
            val c = (if (dark) DarkColorScheme else LightColorScheme).withPalette(palette, dark)
            listOf(c.onPrimary to c.primary, c.onSecondary to c.secondary, c.onTertiary to c.tertiary,
                c.onPrimaryContainer to c.primaryContainer, c.onSecondaryContainer to c.secondaryContainer,
                c.onTertiaryContainer to c.tertiaryContainer, c.onSurface to c.surface,
                c.onSurfaceVariant to c.surfaceVariant).forEach { (fg, bg) ->
                assertTrue("Material $palette dark=$dark: ${ratio(fg, bg)}", ratio(fg, bg) >= 4.5f)
            }
        } }
    }

    @Test fun `palettes leave neutral night surfaces and reading paper independent`() {
        IhsanPalette.entries.forEach { palette ->
            val c = ihsanSemanticColors(palette, true)
            assertEquals(DarkBackground, c.surfaceBase)
            assertEquals(DarkSurfaceElevated, c.surfaceElevated)
            assertTrue(ratio(QuranInk, QuranPaper) >= 4.5f)
        }
    }
}
