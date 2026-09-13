package com.example.feature.quran.domain.model

import java.time.LocalDate
import kotlin.math.ceil

/**
 * Phase 4 — Quran Goal.
 *
 * Deliberately built **on top of** the existing khatma progress rather than beside it:
 * the only progress input is the reader's real position (the running ayah index that
 * already backs `QuranRepository.getKhatmaProgress()`). There is no second progress
 * store, no streak, and no leaderboard.
 */
enum class QuranGoalType {
    /** Read N ayahs per day. */
    AYAHS_PER_DAY,

    /** Read N pages per day (a page is [AYAHS_PER_PAGE] ayahs on average). */
    PAGES_PER_DAY,

    /** Finish the whole mushaf by a chosen date; the daily amount is derived. */
    KHATMA_BY_DATE;

    companion object {
        fun fromStorage(raw: String?): QuranGoalType? =
            entries.firstOrNull { it.name == raw?.trim()?.uppercase() }
    }
}

/**
 * A locally stored reading goal. [target] means ayahs/day or pages/day depending on
 * [type]; for [QuranGoalType.KHATMA_BY_DATE] it is ignored and [khatmaTargetEpochDay]
 * is used instead.
 */
data class QuranGoal(
    val type: QuranGoalType,
    val target: Int = 0,
    val khatmaTargetEpochDay: Long? = null
) {
    companion object {
        /** Total ayahs in the mushaf — the same constant the khatma progress uses. */
        const val TOTAL_AYAHS = 6236

        /** Standard mushaf is 604 pages; ~10.3 ayahs per page. */
        const val AYAHS_PER_PAGE = 10.3

        val NONE: QuranGoal? = null
    }
}

/**
 * Today's picture of a goal. All values are derived — nothing here is persisted, so
 * the goal can never drift out of sync with the reader's real position.
 */
data class QuranGoalProgress(
    val goal: QuranGoal,
    /** Ayahs the user should read today to stay on plan. Always >= 1 for an active goal. */
    val dailyTargetAyahs: Int,
    /** Ayahs actually read today, measured from the reader position. */
    val readTodayAyahs: Int,
    /** Overall mushaf completion, 0f..1f — the existing khatma progress. */
    val khatmaFraction: Float,
    /** Days left until a KHATMA_BY_DATE goal is due; null for daily goals. */
    val daysRemaining: Long?
) {
    /** Today's completion, 0f..1f. Capped — over-reading is fine, never a penalty. */
    val dailyFraction: Float
        get() = if (dailyTargetAyahs <= 0) 0f
        else (readTodayAyahs.toFloat() / dailyTargetAyahs.toFloat()).coerceIn(0f, 1f)

    val isMetToday: Boolean get() = readTodayAyahs >= dailyTargetAyahs

    /** True once the whole mushaf is finished. */
    val isKhatmaComplete: Boolean get() = khatmaFraction >= 1f
}

/**
 * Pure derivation of goal progress. No clock, no storage, no side effects — the caller
 * supplies today's date and the reader position.
 */
object QuranGoalCalculator {

    /** Converts a page count to whole ayahs, rounding up so a goal is never trivial. */
    fun pagesToAyahs(pages: Int): Int =
        if (pages <= 0) 0 else ceil(pages * QuranGoal.AYAHS_PER_PAGE).toInt()

    /**
     * Ayahs the user must read today.
     *
     * For [QuranGoalType.KHATMA_BY_DATE] this is the *remaining* ayahs spread over the
     * *remaining* days, so falling behind adjusts the plan instead of accusing the user.
     * A due-today or overdue goal asks for everything that is left.
     */
    fun dailyTargetAyahs(
        goal: QuranGoal,
        currentAyahIndex: Int,
        today: LocalDate
    ): Int = when (goal.type) {
        QuranGoalType.AYAHS_PER_DAY -> goal.target.coerceAtLeast(0)

        QuranGoalType.PAGES_PER_DAY -> pagesToAyahs(goal.target)

        QuranGoalType.KHATMA_BY_DATE -> {
            val remainingAyahs =
                (QuranGoal.TOTAL_AYAHS - currentAyahIndex.coerceIn(0, QuranGoal.TOTAL_AYAHS))
            if (remainingAyahs <= 0) {
                0
            } else {
                val due = goal.khatmaTargetEpochDay
                val daysLeft =
                    if (due == null) 1L else (due - today.toEpochDay()).coerceAtLeast(1L)
                ceil(remainingAyahs.toDouble() / daysLeft.toDouble()).toInt()
            }
        }
    }

    fun daysRemaining(goal: QuranGoal, today: LocalDate): Long? =
        if (goal.type != QuranGoalType.KHATMA_BY_DATE) null
        else goal.khatmaTargetEpochDay?.let { it - today.toEpochDay() }

    /**
     * @param currentAyahIndex the reader's running ayah index (1-based) — the same
     *   value that produces the existing khatma percentage.
     * @param startOfDayAyahIndex the index recorded when today began; the difference
     *   is what was read today.
     */
    fun progress(
        goal: QuranGoal,
        currentAyahIndex: Int,
        startOfDayAyahIndex: Int,
        today: LocalDate
    ): QuranGoalProgress {
        val bounded = currentAyahIndex.coerceIn(0, QuranGoal.TOTAL_AYAHS)
        val readToday = (bounded - startOfDayAyahIndex).coerceAtLeast(0)
        return QuranGoalProgress(
            goal = goal,
            dailyTargetAyahs = dailyTargetAyahs(goal, bounded, today),
            readTodayAyahs = readToday,
            khatmaFraction = bounded.toFloat() / QuranGoal.TOTAL_AYAHS.toFloat(),
            daysRemaining = daysRemaining(goal, today)
        )
    }
}
