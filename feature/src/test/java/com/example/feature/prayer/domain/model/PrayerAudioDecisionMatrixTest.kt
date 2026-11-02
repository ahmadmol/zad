package com.example.feature.prayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Decision matrix for the current normalized per-prayer notification modes. */
class PrayerAudioDecisionMatrixTest {

    @Test
    fun `each notifiable prayer accepts every configured mode`() {
        PrayerName.entries.filter { it.isNotifiable }.forEach { prayer ->
            PrayerAlertMode.entries.forEach { mode ->
                val configured = PrayerNotificationModes.DEFAULT.with(prayer, mode)
                assertEquals("$prayer + $mode", mode, configured.modeFor(prayer))
            }
        }
    }

    @Test
    fun `sunrise matrix normalizes only adhan to notice`() {
        val expected = mapOf(
            PrayerAlertMode.MUTED to PrayerAlertMode.MUTED,
            PrayerAlertMode.NOTICE to PrayerAlertMode.NOTICE,
            PrayerAlertMode.ADHAN to PrayerAlertMode.NOTICE
        )
        expected.forEach { (requested, normalized) ->
            val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.SUNRISE, requested)
            assertEquals(normalized, modes.modeFor(PrayerName.SUNRISE))
        }
    }

    @Test
    fun `muting all prayers produces six muted cells`() {
        val modes = PrayerName.entries.fold(PrayerNotificationModes.DEFAULT) { acc, prayer ->
            acc.with(prayer, PrayerAlertMode.MUTED)
        }
        assertEquals(PrayerName.entries.size, modes.asMap().values.count { it == PrayerAlertMode.MUTED })
    }
}
