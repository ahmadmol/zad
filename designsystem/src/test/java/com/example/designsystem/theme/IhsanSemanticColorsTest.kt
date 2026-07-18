package com.example.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IhsanSemanticColorsTest {

    @Test
    fun `canonical brand matches PrimaryTeal`() {
        assertEquals(PrimaryTeal, LightIhsanSemanticColors.brand)
        assertEquals(PrimaryTeal, DarkIhsanSemanticColors.brand)
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
    fun `dark surface muted differs from light`() {
        assertNotEquals(
            LightIhsanSemanticColors.surfaceMuted,
            DarkIhsanSemanticColors.surfaceMuted
        )
    }

    @Test
    fun `minimum touch target is material 48dp`() {
        assertEquals(48f, IhsanDimens().minTouchTarget.value, 0.01f)
    }
}
