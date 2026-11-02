package com.example.feature.prayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Replaces the removed audio-policy API with current prayer/alarm classification coverage. */
class PrayerAlertAudioPolicyNoAdhanTest {

    @Test
    fun `only sunrise is non-notifiable`() {
        assertFalse(PrayerName.SUNRISE.isNotifiable)
        PrayerName.entries.filterNot { it == PrayerName.SUNRISE }.forEach {
            assertTrue(it.isNotifiable)
        }
    }

    @Test
    fun `sunrise defaults to notice and cannot be promoted to adhan`() {
        assertEquals(PrayerAlertMode.NOTICE, PrayerNotificationModes.default(PrayerName.SUNRISE))
        val requested = PrayerNotificationModes.DEFAULT.with(PrayerName.SUNRISE, PrayerAlertMode.ADHAN)
        assertEquals(PrayerAlertMode.NOTICE, requested.modeFor(PrayerName.SUNRISE))
    }

    @Test
    fun `alarm kinds retain distinct reminder and exact identities`() {
        assertEquals(
            setOf(
                PrayerAlarmKind.PRE_PRAYER,
                PrayerAlarmKind.EXACT,
                PrayerAlarmKind.IQAMAH,
                PrayerAlarmKind.END_REMINDER
            ),
            PrayerAlarmKind.entries.toSet()
        )
    }

    @Test
    fun `unknown stored mode degrades to adhan default`() {
        assertEquals(PrayerAlertMode.ADHAN, PrayerAlertMode.fromStorage("obsolete"))
        assertEquals(PrayerAlertMode.ADHAN, PrayerAlertMode.fromStorage(null))
    }
}
