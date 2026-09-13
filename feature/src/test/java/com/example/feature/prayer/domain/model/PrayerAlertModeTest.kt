package com.example.feature.prayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase 6 — per-prayer MUTED / NOTICE / ADHAN modes. */
class PrayerAlertModeTest {

    // --- Defaults preserve historical behaviour ---------------------------

    @Test
    fun `default mode is adhan for the five prayers`() {
        TrackablePrayers.all.forEach {
            assertEquals(PrayerAlertMode.ADHAN, PrayerNotificationModes.DEFAULT.modeFor(it))
        }
    }

    @Test
    fun `default mode for sunrise is a quiet notice`() {
        assertEquals(
            PrayerAlertMode.NOTICE,
            PrayerNotificationModes.DEFAULT.modeFor(PrayerName.SUNRISE)
        )
    }

    @Test
    fun `nothing is muted by default`() {
        PrayerName.entries.forEach {
            assertFalse(PrayerNotificationModes.DEFAULT.isMuted(it))
        }
    }

    // --- Sunrise can never be adhan ----------------------------------------

    @Test
    fun `requesting adhan for sunrise is downgraded to notice`() {
        val modes = PrayerNotificationModes.DEFAULT
            .with(PrayerName.SUNRISE, PrayerAlertMode.ADHAN)
        assertEquals(PrayerAlertMode.NOTICE, modes.modeFor(PrayerName.SUNRISE))
    }

    @Test
    fun `sunrise can still be muted`() {
        val modes = PrayerNotificationModes.DEFAULT
            .with(PrayerName.SUNRISE, PrayerAlertMode.MUTED)
        assertTrue(modes.isMuted(PrayerName.SUNRISE))
    }

    @Test
    fun `no mode can make sunrise play the adhan`() {
        PrayerAlertMode.entries.forEach { mode ->
            val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.SUNRISE, mode)
            PrayerAlarmKind.entries.forEach { kind ->
                assertNotEquals(
                    "SUNRISE " + mode + "/" + kind + " must never be ADHAN",
                    PrayerAlertAudio.ADHAN,
                    PrayerAlertAudioPolicy.decide(PrayerName.SUNRISE, kind, modes)
                )
            }
        }
    }

    // --- Mode-aware audio policy -------------------------------------------

    @Test
    fun `adhan mode keeps the exact alert as adhan`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.ASR, PrayerAlertMode.ADHAN)
        assertEquals(
            PrayerAlertAudio.ADHAN,
            PrayerAlertAudioPolicy.decide(PrayerName.ASR, PrayerAlarmKind.EXACT, modes)
        )
    }

    @Test
    fun `notice mode downgrades the exact alert`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.ASR, PrayerAlertMode.NOTICE)
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(PrayerName.ASR, PrayerAlarmKind.EXACT, modes)
        )
    }

    @Test
    fun `a stale alarm for a muted prayer arrives quietly rather than as adhan`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.ASR, PrayerAlertMode.MUTED)
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(PrayerName.ASR, PrayerAlarmKind.EXACT, modes)
        )
    }

    @Test
    fun `a mode can never promote a reminder to adhan`() {
        val loud = PrayerName.entries.fold(PrayerNotificationModes.DEFAULT) { acc, p ->
            acc.with(p, PrayerAlertMode.ADHAN)
        }
        listOf(
            PrayerAlarmKind.PRE_PRAYER,
            PrayerAlarmKind.IQAMAH,
            PrayerAlarmKind.END_REMINDER
        ).forEach { kind ->
            PrayerName.entries.forEach { prayer ->
                assertEquals(
                    prayer.name + "/" + kind + " must stay a notice",
                    PrayerAlertAudio.NOTICE,
                    PrayerAlertAudioPolicy.decide(prayer, kind, loud)
                )
            }
        }
    }

    @Test
    fun `changing one prayer does not affect the others`() {
        val modes = PrayerNotificationModes.DEFAULT.with(PrayerName.FAJR, PrayerAlertMode.MUTED)
        assertTrue(modes.isMuted(PrayerName.FAJR))
        assertFalse(modes.isMuted(PrayerName.DHUHR))
        assertEquals(PrayerAlertMode.ADHAN, modes.modeFor(PrayerName.DHUHR))
    }

    @Test
    fun `lost alarm identity still degrades to notice under any modes`() {
        val loud = PrayerNotificationModes.DEFAULT.with(PrayerName.ASR, PrayerAlertMode.ADHAN)
        assertEquals(PrayerAlertAudio.NOTICE, PrayerAlertAudioPolicy.decide(null, null, loud))
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(null, PrayerAlarmKind.EXACT, loud)
        )
    }

    // --- Persistence --------------------------------------------------------

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
    fun `an empty store yields the defaults`() {
        val restored = PrayerNotificationModes.fromStorage(emptySet())
        assertEquals(PrayerAlertMode.ADHAN, restored.modeFor(PrayerName.ASR))
    }

    @Test
    fun `corrupt stored entries are ignored rather than crashing`() {
        val restored = PrayerNotificationModes.fromStorage(
            setOf("NOT_A_PRAYER:MUTED", "garbage", "FAJR:NOT_A_MODE", "ISHA:MUTED")
        )
        assertEquals(PrayerAlertMode.MUTED, restored.modeFor(PrayerName.ISHA))
        // An unknown mode string degrades to the default rather than dropping the prayer.
        assertEquals(PrayerAlertMode.ADHAN, restored.modeFor(PrayerName.FAJR))
    }

    @Test
    fun `asMap covers every prayer`() {
        val map = PrayerNotificationModes.DEFAULT.asMap()
        assertEquals(PrayerName.entries.size, map.size)
        assertTrue(map.keys.containsAll(PrayerName.entries.toList()))
    }
}
