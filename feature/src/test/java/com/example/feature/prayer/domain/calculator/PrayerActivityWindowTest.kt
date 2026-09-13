package com.example.feature.prayer.domain.calculator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerActivityWindowTest {

    @Test
    fun `active window includes prayer start and excludes its end`() {
        val prayer = 1_000_000L

        assertTrue(PrayerActivityWindow.isActive(prayer, prayer))
        assertTrue(
            PrayerActivityWindow.isActive(
                prayer,
                prayer + PrayerActivityWindow.ACTIVE_WINDOW_MILLIS - 1
            )
        )
        assertFalse(
            PrayerActivityWindow.isActive(
                prayer,
                prayer + PrayerActivityWindow.ACTIVE_WINDOW_MILLIS
            )
        )
    }
}
