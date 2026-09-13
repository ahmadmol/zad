package com.example.feature.prayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Phase 3 — Explicit audio-policy table for non-adhan alerts.
 *
 * The pre-existing PrayerAlertAudioPolicyTest already covers all kinds.
 * This test is the explicit "PRE_PRAYER, SUNRISE, IQAMAH can never be ADHAN"
 * policy assertion that Section 1 froze as a Release 1 product contract.
 *
 * Note on the domain shape: SUNRISE is a [PrayerName], not a [PrayerAlarmKind].
 * The sunrise contract is therefore asserted across every kind for that name.
 */
class PrayerAlertAudioPolicyNoAdhanTest {

    @Test
    fun `pre prayer is always notice regardless of prayer name`() {
        for (prayer in PrayerName.values()) {
            val audio = PrayerAlertAudioPolicy.decide(prayer, PrayerAlarmKind.PRE_PRAYER)
            assertEquals(
                "PRE_PRAYER for $prayer must not be ADHAN",
                PrayerAlertAudio.NOTICE,
                audio
            )
        }
    }

    @Test
    fun `sunrise is always notice regardless of alarm kind`() {
        for (kind in PrayerAlarmKind.values()) {
            val audio = PrayerAlertAudioPolicy.decide(PrayerName.SUNRISE, kind)
            assertEquals(
                "SUNRISE for kind $kind must not be ADHAN",
                PrayerAlertAudio.NOTICE,
                audio
            )
        }
    }

    @Test
    fun `iqamah is always notice regardless of prayer name`() {
        for (prayer in PrayerName.values()) {
            val audio = PrayerAlertAudioPolicy.decide(prayer, PrayerAlarmKind.IQAMAH)
            assertEquals(
                "IQAMAH for $prayer must not be ADHAN",
                PrayerAlertAudio.NOTICE,
                audio
            )
        }
    }

    @Test
    fun `end reminder is always notice regardless of prayer name`() {
        for (prayer in PrayerName.values()) {
            val audio = PrayerAlertAudioPolicy.decide(prayer, PrayerAlarmKind.END_REMINDER)
            assertEquals(
                "END_REMINDER for $prayer must not be ADHAN",
                PrayerAlertAudio.NOTICE,
                audio
            )
        }
    }

    @Test
    fun `exact is adhan only for notifiable prayers`() {
        for (prayer in PrayerName.values()) {
            val audio = PrayerAlertAudioPolicy.decide(prayer, PrayerAlarmKind.EXACT)
            val expected =
                if (prayer.isNotifiable) PrayerAlertAudio.ADHAN else PrayerAlertAudio.NOTICE
            assertEquals("EXACT for $prayer", expected, audio)
        }
    }

    @Test
    fun `lost alarm extras degrade to notice`() {
        assertEquals(PrayerAlertAudio.NOTICE, PrayerAlertAudioPolicy.decide(null, null))
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(PrayerName.ASR, null)
        )
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(null, PrayerAlarmKind.EXACT)
        )
    }

    @Test
    fun `notice is a distinct value from adhan`() {
        // Defensive: any future enum merge that makes NOTICE == ADHAN would
        // silently break the policy. This test fails loudly if that happens.
        assertNotEquals(PrayerAlertAudio.NOTICE, PrayerAlertAudio.ADHAN)
    }
}
