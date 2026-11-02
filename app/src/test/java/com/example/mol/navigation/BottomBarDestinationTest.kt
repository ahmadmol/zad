package com.example.mol.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarDestinationTest {

    @Test
    fun `Home root resolves to HOME`() {
        assertEquals(BottomBarDestination.HOME, resolveBottomBarDestination(Screen.Home.route))
    }

    @Test
    fun `Ehsan root resolves to EHSAN`() {
        assertEquals(BottomBarDestination.EHSAN, resolveBottomBarDestination(Screen.Donations.route))
    }

    @Test
    fun `Profile root resolves to PROFILE and is not null`() {
        val result = resolveBottomBarDestination(Screen.Profile.route)
        assertEquals(BottomBarDestination.PROFILE, result)
        assertTrue(result != null)
    }

    @Test
    fun `child and detail routes are hidden`() {
        assertNull(resolveBottomBarDestination(Screen.EditProfile.route))
        assertNull(resolveBottomBarDestination(Screen.DonationHistory.route))
        assertNull(resolveBottomBarDestination(Screen.Settings.route))
        assertNull(resolveBottomBarDestination(Screen.Reminders.route))
        assertNull(resolveBottomBarDestination(Screen.AddDonation.route))
        assertNull(resolveBottomBarDestination(Screen.RequestHelp.route))
        assertNull(resolveBottomBarDestination(Screen.IhsanDetails.route))
        assertNull(resolveBottomBarDestination(Screen.QuranReader.route))
        assertNull(resolveBottomBarDestination(Screen.DuaDetail.route))
        assertNull(resolveBottomBarDestination(Screen.HaramLive.route))
        assertNull(resolveBottomBarDestination(Screen.NabawiLive.route))
        assertNull(resolveBottomBarDestination(Screen.Quran.route))
        assertNull(resolveBottomBarDestination(Screen.Prayer.route))
        assertNull(resolveBottomBarDestination(Screen.Azkar.route))
        assertNull(resolveBottomBarDestination(Screen.Hadith.route))
        assertNull(resolveBottomBarDestination(Screen.Asma.route))
        assertNull(resolveBottomBarDestination(Screen.GlobalSearch.route))
        assertNull(resolveBottomBarDestination(Screen.DailyActivities.route))
        assertNull(resolveBottomBarDestination(Screen.Statistics.route))
        assertNull(resolveBottomBarDestination(Screen.Tasbih.route))
    }

    @Test
    fun `unknown route is hidden and does not fall back to Home`() {
        assertNull(resolveBottomBarDestination("unknown_route"))
        assertNull(resolveBottomBarDestination(null))
        assertNotEquals(
            BottomBarDestination.HOME,
            resolveBottomBarDestination("unknown_route")
        )
    }

    @Test
    fun `enum always includes Profile حسابي destination`() {
        val routes = BottomBarDestination.entries.map { it.route }.toSet()
        assertTrue(routes.contains(Screen.Home.route))
        assertTrue(routes.contains(Screen.Donations.route))
        assertTrue(routes.contains(Screen.Profile.route))
        assertEquals(3, BottomBarDestination.entries.size)
    }

    @Test
    fun `exactly one root is selected for each known route`() {
        val roots = listOf(Screen.Home.route, Screen.Donations.route, Screen.Profile.route)
        roots.forEach { route ->
            val matches = BottomBarDestination.entries.count { it.route == route }
            assertEquals("Expected exactly one match for $route", 1, matches)
        }
    }
}
