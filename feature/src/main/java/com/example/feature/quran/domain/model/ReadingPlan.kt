package com.example.feature.quran.domain.model

import java.time.LocalDate

/** Explicit page completion is independent of the reader's saved position. */
data class ReadingPlan(
    val id: String,
    val startEpochDay: Long,
    val dueEpochDay: Long,
    val firstPage: Int,
    val lastPage: Int,
    val active: Boolean = true
) {
    init {
        require(firstPage in 1..604 && lastPage in firstPage..604)
        require(dueEpochDay >= startEpochDay)
        require(dueEpochDay - startEpochDay < 3650)
    }
    val pageCount: Int get() = lastPage - firstPage + 1

    fun remaining(completedPages: Set<Int>): List<Int> = (firstPage..lastPage).filterNot { it in completedPages }

    /** Inclusive start/end dates; overdue plans stay editable without fabricating missed days. */
    fun suggestedPages(today: LocalDate, completedPages: Set<Int>): List<Int> {
        return QuranGoalCalculator.suggestedPages(this, today, completedPages)
    }
}

data class PrivateAyahNote(val id: String, val surahId: Int, val ayahNumber: Int,
    val text: String, val createdAt: Long, val updatedAt: Long)
