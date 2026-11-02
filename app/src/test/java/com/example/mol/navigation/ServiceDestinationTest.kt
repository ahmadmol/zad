package com.example.mol.navigation

import com.example.feature.dashboard.presentation.components.ihsanServices
import org.junit.Assert.*
import org.junit.Test

class ServiceDestinationTest {
    @Test fun `restored home onboarding and child routes release native splash`() {
        assertFalse(isStartupResolvedRoute(null))
        assertFalse(isStartupResolvedRoute(Screen.Splash.route))
        listOf(Screen.Home.route, Screen.Onboarding.route, Screen.Services.route, Screen.LocationPermission.route, Screen.QuranReader.createRoute(2, 25)).forEach {
            assertTrue(isStartupResolvedRoute(it))
        }
    }
    @Test fun `all advertised services have a destination or explicit external chooser owner`() {
        ihsanServices.filterNot { it.route in setOf("live_chooser", "mosques") }.forEach {
            assertNotNull("Missing ${it.route}", serviceDestinationRoute(it.route))
        }
        assertEquals("tasbih_screen", serviceDestinationRoute("tasbih"))
        assertNull(serviceDestinationRoute("untrusted_unknown"))
    }

    @Test fun `expanded surfaces have distinct stable route identities`() {
        val routes = listOf(Screen.Services.route, Screen.DailyGoals.route, Screen.Support.route, Screen.HaramainVisit.route)
        assertEquals(4, routes.distinct().size)
        assertTrue(routes.none { it in Screen.items.map { screen -> screen.route } })
    }
}
