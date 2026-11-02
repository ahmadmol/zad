package com.example.feature.fasting.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class FastingStatus(val label: String) { FASTED("صمت"), NOT_FASTED("لم أصم") }
data class FastingRecord(val date: LocalDate, val status: FastingStatus?, val typeLabel: String) {
    val label: String get() = status?.label ?: "تسجيل بحالة غير معروفة"
}
data class FastingStats(val fasted: Int, val notFasted: Int, val unknown: Int, val unrecorded: Int) {
    val recorded: Int get() = fasted + notFasted + unknown
    companion object {
        /** Absent rows never mean either fasting or breaking the fast; future days are excluded. */
        fun calculate(rows: List<FastingRecord>, first: LocalDate, last: LocalDate, today: LocalDate): FastingStats {
            val end = minOf(last, today)
            val days = if(end < first) 0 else ChronoUnit.DAYS.between(first, end).toInt()+1
            val saved = rows.filter { it.date in first..end }.distinctBy { it.date }
            return FastingStats(saved.count { it.status == FastingStatus.FASTED },
                saved.count { it.status == FastingStatus.NOT_FASTED }, saved.count { it.status == null }, days-saved.size)
        }
    }
}
