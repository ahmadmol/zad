package com.example.feature.prayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Current per-prayer MUTED / NOTICE / ADHAN domain behavior. */
class PrayerAlertModeTest {

    @Test
    fun `defaults use adhan for prayers and notice for sunrise`() {
        PrayerName.entries.forEach { prayer ->
            val expected = if (prayer.isNotifiable) PrayerAlertMode.ADHAN else PrayerAlertMode.NOTICE
            assertEquals(expected, PrayerNotificationModes.DEFAULT.modeFor(prayer))
        }
    }

    @Test
    fun `requesting adhan for sunrise is downgraded to notice`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.SUNRISE, PrayerAlertMode.ADHAN)
        assertEquals(PrayerAlertMode.NOTICE, modes.modeFor(PrayerName.SUNRISE))
    }

    @Test
    fun `sunrise can be muted`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.SUNRISE, PrayerAlertMode.MUTED)
        assertTrue(modes.isMuted(PrayerName.SUNRISE))
    }

    @Test
    fun `changing one prayer does not affect another`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.FAJR, PrayerAlertMode.MUTED)
        assertTrue(modes.isMuted(PrayerName.FAJR))
        assertFalse(modes.isMuted(PrayerName.DHUHR))
        assertEquals(PrayerAlertMode.ADHAN, modes.modeFor(PrayerName.DHUHR))
    }

    @Test
    fun `modes round trip through storage`() {
        val modes = PrayerNotificationModes.DEFAULT
            .with(PrayerName.FAJR, PrayerAlertMode.MUTED)
            .with(PrayerName.ISHA, PrayerAlertMode.NOTICE)
        val restored = PrayerNotificationModes.fromStorage(modes.toStorage())

        assertEquals(PrayerAlertMode.MUTED, restored.modeFor(PrayerName.FAJR))
        assertEquals(PrayerAlertMode.NOTICE, restored.modeFor(PrayerName.ISHA))
        assertEquals(PrayerAlertMode.ADHAN, restored.modeFor(PrayerName.ASR))
    }

    @Test
    fun `corrupt stored entries are ignored or degrade to default`() {
        val restored = PrayerNotificationModes.fromStorage(
            setOf("NOT_A_PRAYER:MUTED", "garbage", "FAJR:NOT_A_MODE", "ISHA:MUTED")
        )
        assertEquals(PrayerAlertMode.ADHAN, restored.modeFor(PrayerName.FAJR))
        assertEquals(PrayerAlertMode.MUTED, restored.modeFor(PrayerName.ISHA))
    }

    @Test
    fun `asMap covers every prayer with normalized values`() {
        val map = PrayerNotificationModes.DEFAULT.asMap()
        assertEquals(PrayerName.entries.size, map.size)
        assertEquals(PrayerAlertMode.NOTICE, map.getValue(PrayerName.SUNRISE))
    }
}
