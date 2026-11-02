package com.example.mol.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductSurfaceNavigationCharacterizationTest {

    @Test
    fun `production counter route is tasbih_screen`() {
        assertEquals("tasbih_screen", Screen.Tasbih.route)
    }

    @Test
    fun `settings and statistics routes exist`() {
        assertEquals("settings_screen", Screen.Settings.route)
        assertEquals("statistics_screen", Screen.Statistics.route)
    }

    @Test
    fun `ihsan details is the canonical donation details route`() {
        assertEquals("ihsan_details/42", Screen.IhsanDetails.createRoute(42L))
    }

    @Test
    @Suppress("DEPRECATION")
    fun `legacy donation detail route redirects to canonical path pattern`() {
        assertEquals("donation_detail_screen/42", Screen.LegacyDonationDetail.createRoute(42L))
        assertFalse(Screen.LegacyDonationDetail.route == Screen.IhsanDetails.route)
    }

    @Test
    fun `inbox route is removed from Screen definitions`() {
        assertFalse(Screen.items.any { it.route == "inbox_screen" })
        // Compile-time: Screen.Inbox no longer exists; bottom nav stays three tabs.
        assertEquals(3, Screen.items.size)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `canonical screens exclude charity trust and inbox`() {
        val routes = listOf(
            Screen.Home.route,
            Screen.Tasbih.route,
            Screen.Settings.route,
            Screen.Statistics.route,
            Screen.IhsanDetails.route,
            Screen.LegacyDonationDetail.route,
            Screen.Donations.route,
            Screen.Profile.route,
            Screen.Azkar.route,
            Screen.Prayer.route
        )
        assertTrue(routes.none { it.contains("charity_trust", ignoreCase = true) })
        assertTrue(routes.none { it.contains("inbox", ignoreCase = true) })
        assertEquals("ihsan_plus_daily", Screen.IhsanPlusDaily.route)
    }

    @Test
    fun `bottom nav remains home donations profile`() {
        assertEquals(
            listOf(Screen.Home.route, Screen.Donations.route, Screen.Profile.route),
            Screen.items.map { it.route }
        )
    }
}
