package com.example.feature.dashboard.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase 5 — Home information architecture rules. */
class HomeDestinationCatalogTest {

    @Test
    fun `no destination appears twice on home`() {
        val all = HomeDestinationCatalog.allRoutes
        val duplicates = all.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue("duplicate Home destinations: " + duplicates, duplicates.isEmpty())
        assertEquals(all.size, all.toSet().size)
    }

    @Test
    fun `quick actions and services do not overlap`() {
        val overlap = HomeDestinationCatalog.primaryRoutes
            .intersect(HomeDestinationCatalog.serviceRoutes.toSet())
        assertTrue("routes in both groups: " + overlap, overlap.isEmpty())
    }

    @Test
    fun `home exposes exactly one live entry and it is the chooser`() {
        val liveEntries = HomeDestinationCatalog.allRoutes.filter {
            it == "live_chooser" || it in HomeDestinationCatalog.directLiveRoutes
        }
        assertEquals(listOf("live_chooser"), liveEntries)
    }

    @Test
    fun `direct stream routes are not reachable as home tiles`() {
        HomeDestinationCatalog.directLiveRoutes.forEach { route ->
            assertFalse(
                route + " must be reached through the Live chooser, not a Home tile",
                HomeDestinationCatalog.allRoutes.contains(route)
            )
        }
    }

    @Test
    fun `no route is blank`() {
        assertTrue(HomeDestinationCatalog.allRoutes.none { it.isBlank() })
    }

    @Test
    fun `both groups are non empty`() {
        assertTrue(HomeDestinationCatalog.primaryRoutes.isNotEmpty())
        assertTrue(HomeDestinationCatalog.serviceRoutes.isNotEmpty())
    }

    @Test
    fun `home previews exactly six and the full catalog preserves reminder access`() {
        assertEquals(listOf("quran", "azkar", "qibla", "dua", "calendar", "asma"), HomeDestinationCatalog.primaryRoutes)
        assertFalse(HomeDestinationCatalog.primaryRoutes.contains("reminders"))
        assertTrue(HomeDestinationCatalog.serviceRoutes.contains("reminders"))
        assertTrue(HomeDestinationCatalog.serviceRoutes.containsAll(listOf("goals", "support", "visit")))
    }
}
