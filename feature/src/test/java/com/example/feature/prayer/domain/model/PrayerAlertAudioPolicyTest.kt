package com.example.feature.prayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Phase A1 regression locks: only a real prayer at its exact time may play the adhan.
 * Every other combination must resolve to [PrayerAlertAudio.NOTICE].
 */
class PrayerAlertAudioPolicyTest {

    private val realPrayers = listOf(
        PrayerName.FAJR,
        PrayerName.DHUHR,
        PrayerName.ASR,
        PrayerName.MAGHRIB,
        PrayerName.ISHA
    )

    // TEST 1
    @Test
    fun `pre prayer reminder never resolves adhan`() {
        realPrayers.forEach { prayer ->
            assertEquals(
                "PRE_PRAYER must never be adhan for $prayer",
                PrayerAlertAudio.NOTICE,
                PrayerAlertAudioPolicy.decide(prayer, PrayerAlarmKind.PRE_PRAYER)
            )
        }
    }

    // TEST 2
    @Test
    fun `sunrise exact never resolves adhan`() {
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(PrayerName.SUNRISE, PrayerAlarmKind.EXACT)
        )
    }

    // TEST 3
    @Test
    fun `sunrise never resolves adhan for any event kind`() {
        PrayerAlarmKind.entries.forEach { kind ->
            assertEquals(
                "SUNRISE must never be adhan for kind $kind",
                PrayerAlertAudio.NOTICE,
                PrayerAlertAudioPolicy.decide(PrayerName.SUNRISE, kind)
            )
        }
    }

    // TEST 4
    @Test
    fun `iqamah never resolves adhan`() {
        realPrayers.forEach { prayer ->
            assertEquals(
                PrayerAlertAudio.NOTICE,
                PrayerAlertAudioPolicy.decide(prayer, PrayerAlarmKind.IQAMAH)
            )
        }
    }

    // TEST 5
    @Test
    fun `end reminder never resolves adhan`() {
        realPrayers.forEach { prayer ->
            assertEquals(
                PrayerAlertAudio.NOTICE,
                PrayerAlertAudioPolicy.decide(prayer, PrayerAlarmKind.END_REMINDER)
            )
        }
    }

    // TESTS 6-10
    @Test
    fun `exact event for each real prayer is adhan capable`() {
        realPrayers.forEach { prayer ->
            assertEquals(
                "$prayer EXACT must stay adhan-capable",
                PrayerAlertAudio.ADHAN,
                PrayerAlertAudioPolicy.decide(prayer, PrayerAlarmKind.EXACT)
            )
        }
    }

    @Test
    fun `exact is the only adhan capable kind`() {
        val adhanCombinations = PrayerName.entries.flatMap { prayer ->
            PrayerAlarmKind.entries.map { kind -> prayer to kind }
        }.filter { (prayer, kind) ->
            PrayerAlertAudioPolicy.decide(prayer, kind) == PrayerAlertAudio.ADHAN
        }

        assertEquals(
            realPrayers.map { it to PrayerAlarmKind.EXACT }.toSet(),
            adhanCombinations.toSet()
        )
    }

    @Test
    fun `missing event identity falls back to notice rather than adhan`() {
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(null, null)
        )
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(PrayerName.ASR, null)
        )
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(null, PrayerAlarmKind.EXACT)
        )
    }

    // TEST 11
    @Test
    fun `same event key yields the same deterministic notification id`() {
        val key = PrayerAlarmEventKey.of(
            LocalDate.of(2026, 8, 12), PrayerName.ASR, PrayerAlarmKind.EXACT
        )

        assertEquals("2026-08-12_ASR_EXACT", key)
        assertEquals(
            PrayerAlarmEventKey.notificationId(key),
            PrayerAlarmEventKey.notificationId(key)
        )
        assertEquals(
            PrayerAlarmEventKey.notificationId(key),
            PrayerAlarmEventKey.notificationId("2026-08-12_ASR_EXACT")
        )
    }

    @Test
    fun `different events yield different notification ids`() {
        val date = LocalDate.of(2026, 8, 12)
        val ids = PrayerName.entries.flatMap { prayer ->
            PrayerAlarmKind.entries.map { kind ->
                PrayerAlarmEventKey.notificationId(PrayerAlarmEventKey.of(date, prayer, kind))
            }
        }

        assertEquals("event keys must not collide within a day", ids.size, ids.toSet().size)
        assertNotEquals(
            PrayerAlarmEventKey.notificationId("2026-08-12_ASR_EXACT"),
            PrayerAlarmEventKey.notificationId("2026-08-13_ASR_EXACT")
        )
    }

    // TESTS 12-13
    @Test
    fun `event kind and prayer name survive the alarm extras round trip`() {
        val request = PrayerAlarmRequest(
            stableId = 78384,
            prayerName = PrayerName.ASR,
            kind = PrayerAlarmKind.PRE_PRAYER,
            triggerEpochMillis = 1_000L,
            title = "irrelevant",
            message = "irrelevant",
            eventKey = "2026-08-12_ASR_PRE_PRAYER"
        )

        val extras = PrayerAlertIdentity.of(request).encode()
        val decoded = PrayerAlertIdentity.decode { extras[it] }

        assertEquals(PrayerName.ASR, decoded?.prayerName)
        assertEquals(PrayerAlarmKind.PRE_PRAYER, decoded?.kind)
        assertEquals("2026-08-12_ASR_PRE_PRAYER", decoded?.eventKey)
        assertEquals(
            PrayerAlertAudio.NOTICE,
            PrayerAlertAudioPolicy.decide(decoded?.prayerName, decoded?.kind)
        )
    }

    @Test
    fun `every prayer and kind survives the alarm extras round trip`() {
        PrayerName.entries.forEach { prayer ->
            PrayerAlarmKind.entries.forEach { kind ->
                val identity = PrayerAlertIdentity(prayer, kind, "2026-08-12_${prayer.name}_${kind.name}")
                val extras = identity.encode()
                assertEquals(identity, PrayerAlertIdentity.decode { extras[it] })
            }
        }
    }

    @Test
    fun `identity from a legacy alarm without extras decodes to null`() {
        assertNull(PrayerAlertIdentity.decode { null })
        assertNull(PrayerAlertIdentity.decode { if (it == PrayerAlarmExtras.PRAYER_NAME) "ASR" else null })
        assertNull(PrayerAlertIdentity.decode { "GARBAGE" })
    }
}
