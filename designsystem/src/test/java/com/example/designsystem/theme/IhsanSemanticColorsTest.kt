package com.example.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IhsanSemanticColorsTest {

    @Test
    fun `brand fill stays PrimaryTeal in light and dark`() {
        assertEquals(PrimaryTeal, LightIhsanSemanticColors.brand)
        assertEquals(PrimaryTeal, DarkIhsanSemanticColors.brand)
    }

    @Test
    fun `dark surface hierarchy is distinct`() {
        assertNotEquals(DarkIhsanSemanticColors.surfaceBase, DarkIhsanSemanticColors.surfaceElevated)
        assertNotEquals(DarkBackground, DarkSurface)
        assertNotEquals(DarkSurface, DarkSurfaceElevated)
    }

    @Test
    fun `dark interactive primary differs from dark background`() {
        assertNotEquals(DarkInteractivePrimary, DarkBackground)
        assertNotEquals(DarkIhsanSemanticColors.selectedContent, DarkIhsanSemanticColors.surfaceBase)
    }

    @Test
    fun `semantic tokens have distinct light and dark mappings where required`() {
        assertNotEquals(LightIhsanSemanticColors.surfaceMuted, DarkIhsanSemanticColors.surfaceMuted)
        assertNotEquals(LightIhsanSemanticColors.navigationSurface, DarkIhsanSemanticColors.navigationSurface)
        assertNotEquals(LightIhsanSemanticColors.selectedContent, DarkIhsanSemanticColors.selectedContent)
        assertNotEquals(LightIhsanSemanticColors.fieldContainer, DarkIhsanSemanticColors.fieldContainer)
        assertNotEquals(LightIhsanSemanticColors.textPrimary, DarkIhsanSemanticColors.textPrimary)
    }

    @Test
    fun `whatsapp remains intentional third party green`() {
        val c = LightIhsanSemanticColors.whatsapp
        assertTrue(c.green > c.red)
        assertTrue(c.green > c.blue)
    }

    @Test
    fun `charity offer and request remain distinct`() {
        assertNotEquals(
            LightIhsanSemanticColors.charityOffer,
            LightIhsanSemanticColors.charityRequest
        )
    }

    @Test
    fun `minimum touch target is material 48dp`() {
        assertEquals(48f, IhsanDimens().minTouchTarget.value, 0.01f)
    }
}
