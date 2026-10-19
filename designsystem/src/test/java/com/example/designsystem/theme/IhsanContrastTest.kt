package com.example.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class IhsanContrastTest {
    private fun contrast(foreground: Color, background: Color): Float {
        val bg = background.compositeOver(Color.White)
        val fg = foreground.compositeOver(bg)
        val a = fg.luminance(); val b = bg.luminance()
        return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
    }

    @Test fun `readable text and CTA pairs meet AA in both themes`() {
        listOf(LightIhsanSemanticColors, DarkIhsanSemanticColors).forEach { c ->
            listOf(
                c.textPrimary to c.surfaceElevated,
                c.textSecondary to c.surfaceElevated,
                c.textSecondary to c.surfaceMuted,
                c.textPrimary to c.fieldContainer,
                c.textBody to c.fieldContainer,
                c.onSuccess to c.success,
                c.selectedContent to c.selectedContainer,
                c.unselectedContent to c.navigationSurface,
                c.charityOffer to c.charityOfferContainer,
                c.charityRequest to c.charityRequestContainer,
                c.onBrand to c.brand
            ).forEach { (fg, bg) ->
                val paintedBackground = bg.compositeOver(c.surfaceElevated)
                assertTrue("Contrast ${contrast(fg, paintedBackground)} for $fg over $bg", contrast(fg, paintedBackground) >= 4.5f)
            }
        }
        assertTrue(contrast(OnDarkInteractivePrimary, DarkInteractivePrimary) >= 4.5f)
        assertTrue(contrast(DarkIhsanSemanticColors.textBody, DarkIhsanSemanticColors.selectedContainer) >= 4.5f)
        assertTrue(contrast(QuranInk, QuranPaper) >= 4.5f)
    }

    @Test fun `field borders remain visible in both themes`() {
        assertTrue(contrast(LightIhsanSemanticColors.fieldBorder, LightIhsanSemanticColors.fieldContainer) >= 3f)
        // The supplied night border is deliberately subtle; opaque input fill separates it from the card.
        assertTrue(DarkIhsanSemanticColors.fieldBorder == DarkOutline)
        assertTrue(DarkIhsanSemanticColors.fieldContainer != DarkIhsanSemanticColors.surfaceElevated)
    }

    @Test fun `reference hero supports dark sky text and white lower text`() {
        assertTrue(contrast(PrimaryTeal, PrimaryTeal.copy(alpha = .02f).compositeOver(Color.White)) >= 4.5f)
        assertTrue(contrast(OnPrimaryWhite, PrimaryTeal.copy(alpha = .72f).compositeOver(Color.White)) >= 4.5f)
        assertTrue(contrast(OnPrimaryWhite, PrimaryTeal.copy(alpha = .64f).compositeOver(Color.White)) >= 4.5f)
    }
}
