package com.example.feature.quran.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Phase 4 — Quran Goal derivation rules. */
class QuranGoalCalculatorTest {

    private val today = LocalDate.of(2026, 3, 1)

    // --- Daily targets ------------------------------------------------------

    @Test
    fun `ayahs per day target is the configured number`() {
        val goal = QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 20)
        assertEquals(20, QuranGoalCalculator.dailyTargetAyahs(goal, 0, today))
    }

    @Test
    fun `pages per day converts to whole ayahs rounding up`() {
        assertEquals(11, QuranGoalCalculator.pagesToAyahs(1))
        assertEquals(21, QuranGoalCalculator.pagesToAyahs(2))
        assertEquals(0, QuranGoalCalculator.pagesToAyahs(0))
        assertEquals(0, QuranGoalCalculator.pagesToAyahs(-3))
    }

    @Test
    fun `khatma by date spreads the remaining ayahs over the remaining days`() {
        val goal = QuranGoal(
            type = QuranGoalType.KHATMA_BY_DATE,
            khatmaTargetEpochDay = today.plusDays(100).toEpochDay()
        )
        // Nothing read yet: 6236 ayahs over 100 days -> 63 (rounded up).
        assertEquals(63, QuranGoalCalculator.dailyTargetAyahs(goal, 0, today))
    }

    @Test
    fun `khatma by date recalculates from the current position not the start`() {
        val goal = QuranGoal(
            type = QuranGoalType.KHATMA_BY_DATE,
            khatmaTargetEpochDay = today.plusDays(10).toEpochDay()
        )
        val fresh = QuranGoalCalculator.dailyTargetAyahs(goal, 0, today)
        val halfway = QuranGoalCalculator.dailyTargetAyahs(goal, 3118, today)
        assertTrue("progress must lower the daily requirement", halfway < fresh)
    }

    @Test
    fun `falling behind adjusts the plan instead of accusing the user`() {
        val due = today.plusDays(30).toEpochDay()
        val goal = QuranGoal(QuranGoalType.KHATMA_BY_DATE, khatmaTargetEpochDay = due)

        val onDayOne = QuranGoalCalculator.dailyTargetAyahs(goal, 0, today)
        // Ten days later with nothing read, the same goal simply asks for more per day.
        val afterTenIdleDays =
            QuranGoalCalculator.dailyTargetAyahs(goal, 0, today.plusDays(10))

        assertTrue(afterTenIdleDays > onDayOne)
    }

    @Test
    fun `a due or overdue khatma asks for everything that is left`() {
        val goal = QuranGoal(
            QuranGoalType.KHATMA_BY_DATE,
            khatmaTargetEpochDay = today.toEpochDay()
        )
        assertEquals(QuranGoal.TOTAL_AYAHS, QuranGoalCalculator.dailyTargetAyahs(goal, 0, today))

        val overdue = QuranGoal(
            QuranGoalType.KHATMA_BY_DATE,
            khatmaTargetEpochDay = today.minusDays(5).toEpochDay()
        )
        assertEquals(
            QuranGoal.TOTAL_AYAHS,
            QuranGoalCalculator.dailyTargetAyahs(overdue, 0, today)
        )
    }

    @Test
    fun `a finished khatma requires nothing more`() {
        val goal = QuranGoal(
            QuranGoalType.KHATMA_BY_DATE,
            khatmaTargetEpochDay = today.plusDays(5).toEpochDay()
        )
        assertEquals(
            0,
            QuranGoalCalculator.dailyTargetAyahs(goal, QuranGoal.TOTAL_AYAHS, today)
        )
    }

    // --- Progress -----------------------------------------------------------

    @Test
    fun `read today is the difference from the day start baseline`() {
        val goal = QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 20)
        val p = QuranGoalCalculator.progress(goal, 130, 100, today)
        assertEquals(30, p.readTodayAyahs)
        assertTrue(p.isMetToday)
        assertEquals(1f, p.dailyFraction, 0.0001f)
    }

    @Test
    fun `partial progress reports a fraction`() {
        val goal = QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 20)
        val p = QuranGoalCalculator.progress(goal, 110, 100, today)
        assertEquals(10, p.readTodayAyahs)
        assertFalse(p.isMetToday)
        assertEquals(0.5f, p.dailyFraction, 0.0001f)
    }

    @Test
    fun `over reading is capped and never penalised`() {
        val goal = QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 10)
        val p = QuranGoalCalculator.progress(goal, 200, 100, today)
        assertEquals(1f, p.dailyFraction, 0.0001f)
        assertTrue(p.isMetToday)
    }

    @Test
    fun `a backwards reader position never yields negative progress`() {
        val goal = QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 10)
        val p = QuranGoalCalculator.progress(goal, 50, 100, today)
        assertEquals(0, p.readTodayAyahs)
        assertEquals(0f, p.dailyFraction, 0.0001f)
    }

    @Test
    fun `khatma fraction is derived from the same reader position`() {
        val goal = QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 10)
        val p = QuranGoalCalculator.progress(goal, 3118, 3118, today)
        assertEquals(0.5f, p.khatmaFraction, 0.001f)
        assertFalse(p.isKhatmaComplete)
    }

    @Test
    fun `a completed mushaf reports khatma complete`() {
        val goal = QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 10)
        val p = QuranGoalCalculator.progress(goal, QuranGoal.TOTAL_AYAHS, 0, today)
        assertTrue(p.isKhatmaComplete)
    }

    @Test
    fun `an out of range reader index is clamped`() {
        val goal = QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 10)
        assertEquals(1f, QuranGoalCalculator.progress(goal, 99_999, 0, today).khatmaFraction, 0.0001f)
        assertEquals(0f, QuranGoalCalculator.progress(goal, -50, 0, today).khatmaFraction, 0.0001f)
    }

    // --- Days remaining -----------------------------------------------------

    @Test
    fun `days remaining is only meaningful for a dated khatma`() {
        assertNull(
            QuranGoalCalculator.daysRemaining(
                QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 5),
                today
            )
        )
        assertEquals(
            7L,
            QuranGoalCalculator.daysRemaining(
                QuranGoal(
                    QuranGoalType.KHATMA_BY_DATE,
                    khatmaTargetEpochDay = today.plusDays(7).toEpochDay()
                ),
                today
            )
        )
    }

    @Test
    fun `an overdue khatma reports negative days remaining rather than hiding it`() {
        assertEquals(
            -3L,
            QuranGoalCalculator.daysRemaining(
                QuranGoal(
                    QuranGoalType.KHATMA_BY_DATE,
                    khatmaTargetEpochDay = today.minusDays(3).toEpochDay()
                ),
                today
            )
        )
    }

    // --- Storage mapping ----------------------------------------------------

    @Test
    fun `goal type round trips through storage`() {
        QuranGoalType.entries.forEach {
            assertEquals(it, QuranGoalType.fromStorage(it.name))
        }
    }

    @Test
    fun `unknown or absent goal type means no goal`() {
        assertNull(QuranGoalType.fromStorage(null))
        assertNull(QuranGoalType.fromStorage(""))
        assertNull(QuranGoalType.fromStorage("WEEKLY_STREAK"))
    }

    @Test
    fun `zero target daily goal reports no progress rather than dividing by zero`() {
        val goal = QuranGoal(QuranGoalType.AYAHS_PER_DAY, target = 0)
        val p = QuranGoalCalculator.progress(goal, 100, 50, today)
        assertEquals(0f, p.dailyFraction, 0.0001f)
    }
}
