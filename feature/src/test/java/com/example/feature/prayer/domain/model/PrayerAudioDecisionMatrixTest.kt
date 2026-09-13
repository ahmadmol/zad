package com.example.feature.prayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * P1-03 — Explicit, single-table decision matrix for prayer audio.
 *
 * The audio policy is "may this alert play the bundled adhan recording?".
 * It must be deterministic and frozen by tests so a regression that
 * accidentally promotes a sunrise / pre-prayer / iqamah / end-reminder /
 * muted-prayer event to the adhan channel fails immediately.
 *
 * Each row asserts the canonical answer for one (prayer, kind) pair.
 * If a future refactor flips a row to ADHAN, this test fails loudly.
 */
class PrayerAudioDecisionMatrixTest {

    private val adhan = PrayerAlertAudio.ADHAN
    private val notice = PrayerAlertAudio.NOTICE

    private data class MatrixRow(
        val prayer: PrayerName,
        val kind: PrayerAlarmKind,
        val expected: PrayerAlertAudio
    )

    private val matrix: List<MatrixRow> = listOf(
        // EXACT: the only kind that is adhan-capable, and only for the
        // five obligatory prayers. SUNRISE has its own line below.
        MatrixRow(PrayerName.FAJR,     PrayerAlarmKind.EXACT, adhan),
        MatrixRow(PrayerName.DHUHR,    PrayerAlarmKind.EXACT, adhan),
        MatrixRow(PrayerName.ASR,      PrayerAlarmKind.EXACT, adhan),
        MatrixRow(PrayerName.MAGHRIB,  PrayerAlarmKind.EXACT, adhan),
        MatrixRow(PrayerName.ISHA,     PrayerAlarmKind.EXACT, adhan),

        // SUNRISE: never adhan for any kind, even EXACT.
        MatrixRow(PrayerName.SUNRISE,  PrayerAlarmKind.EXACT,        notice),
        MatrixRow(PrayerName.SUNRISE,  PrayerAlarmKind.PRE_PRAYER,   notice),
        MatrixRow(PrayerName.SUNRISE,  PrayerAlarmKind.IQAMAH,       notice),
        MatrixRow(PrayerName.SUNRISE,  PrayerAlarmKind.END_REMINDER, notice),

        // PRE_PRAYER: never adhan for any prayer.
        MatrixRow(PrayerName.FAJR,     PrayerAlarmKind.PRE_PRAYER,   notice),
        MatrixRow(PrayerName.DHUHR,    PrayerAlarmKind.PRE_PRAYER,   notice),
        MatrixRow(PrayerName.ASR,      PrayerAlarmKind.PRE_PRAYER,   notice),
        MatrixRow(PrayerName.MAGHRIB,  PrayerAlarmKind.PRE_PRAYER,   notice),
        MatrixRow(PrayerName.ISHA,     PrayerAlarmKind.PRE_PRAYER,   notice),

        // IQAMAH: never adhan for any prayer.
        MatrixRow(PrayerName.FAJR,     PrayerAlarmKind.IQAMAH,       notice),
        MatrixRow(PrayerName.DHUHR,    PrayerAlarmKind.IQAMAH,       notice),
        MatrixRow(PrayerName.ASR,      PrayerAlarmKind.IQAMAH,       notice),
        MatrixRow(PrayerName.MAGHRIB,  PrayerAlarmKind.IQAMAH,       notice),
        MatrixRow(PrayerName.ISHA,     PrayerAlarmKind.IQAMAH,       notice),

        // END_REMINDER: never adhan for any prayer.
        MatrixRow(PrayerName.FAJR,     PrayerAlarmKind.END_REMINDER, notice),
        MatrixRow(PrayerName.DHUHR,    PrayerAlarmKind.END_REMINDER, notice),
        MatrixRow(PrayerName.ASR,      PrayerAlarmKind.END_REMINDER, notice),
        MatrixRow(PrayerName.MAGHRIB,  PrayerAlarmKind.END_REMINDER, notice),
        MatrixRow(PrayerName.ISHA,     PrayerAlarmKind.END_REMINDER, notice)
    )

    @Test
    fun `decision matrix is frozen - any future promotion to ADHAN fails here`() {
        matrix.forEach { row ->
            assertEquals(
                "${row.prayer} + ${row.kind} must resolve to ${row.expected}",
                row.expected,
                PrayerAlertAudioPolicy.decide(row.prayer, row.kind)
            )
        }
    }

    @Test
    fun `exactly five ADHAN capable cells exist in the matrix`() {
        // Locks the contract: only the five EXACT rows of the five real
        // prayers may resolve to ADHAN. If a future refactor adds another
        // ADHAN row (e.g. SUNRISE + EXACT), this test fails before the
        // per-row test does.
        val adhanCells = matrix.filter { it.expected == adhan }
        assertEquals(5, adhanCells.size)
        assertEquals(
            setOf(
                PrayerName.FAJR to PrayerAlarmKind.EXACT,
                PrayerName.DHUHR to PrayerAlarmKind.EXACT,
                PrayerName.ASR to PrayerAlarmKind.EXACT,
                PrayerName.MAGHRIB to PrayerAlarmKind.EXACT,
                PrayerName.ISHA to PrayerAlarmKind.EXACT
            ),
            adhanCells.map { it.prayer to it.kind }.toSet()
        )
    }

    @Test
    fun `mode NOTICE keeps the EXACT alert as notification only - no ADHAN`() {
        // NOTICE mode is the "I want a quiet ping" choice. The exact alert
        // for that prayer must never play the adhan, even with the loud
        // EXACT kind. This is the mode-aware overload.
        val noticeModes = PrayerNotificationModes.DEFAULT
            .with(PrayerName.ASR, PrayerAlertMode.NOTICE)
        assertEquals(
            notice,
            PrayerAlertAudioPolicy.decide(
                PrayerName.ASR,
                PrayerAlarmKind.EXACT,
                noticeModes
            )
        )
    }

    @Test
    fun `mode MUTED keeps the EXACT alert as notification only - no ADHAN`() {
        // Safety net: a MUTED prayer is normally never scheduled at all
        // (PrayerScheduleBuilder skips the EXACT row), but if a stale
        // alarm from before the mode change still fires, it must arrive
        // on the notice channel, never as adhan.
        val mutedModes = PrayerNotificationModes.DEFAULT
            .with(PrayerName.ASR, PrayerAlertMode.MUTED)
        assertEquals(
            notice,
            PrayerAlertAudioPolicy.decide(
                PrayerName.ASR,
                PrayerAlarmKind.EXACT,
                mutedModes
            )
        )
    }

    @Test
    fun `mode MUTED never ADHAN for every real prayer`() {
        // Belt-and-braces for P1-03: even if someone in the future adds
        // a code path that bypasses the schedule builder and re-checks
        // the policy directly, the mode-aware overload must never
        // resolve to ADHAN for a muted prayer.
        val mutedAll = PrayerName.entries.fold(PrayerNotificationModes.DEFAULT) { acc, p ->
            acc.with(p, PrayerAlertMode.MUTED)
        }
        PrayerName.entries.forEach { prayer ->
            PrayerAlarmKind.entries.forEach { kind ->
                assertEquals(
                    "MUTED $prayer + $kind must never be ADHAN",
                    notice,
                    PrayerAlertAudioPolicy.decide(prayer, kind, mutedAll)
                )
            }
        }
    }
}
