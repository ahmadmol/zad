package com.example.feature.goals

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class DailyGoal(val id: String, val title: String, val target: Int = 1,
    val weekdays: Set<Int> = (1..7).toSet(), val createdOn: String, val archived: Boolean = false) {
    fun scheduledOn(date: LocalDate): Boolean = !archived && date >= LocalDate.parse(createdOn) && date.dayOfWeek.value in weekdays
}

@Serializable
data class DailyGoalEntry(val goalId: String, val date: String, val count: Int, val target: Int)

@Serializable
data class DailyGoalLedger(val version: Int = 1, val goals: List<DailyGoal> = emptyList(),
    val entries: List<DailyGoalEntry> = emptyList()) {
    fun count(id: String, date: LocalDate): Int = entries.firstOrNull { it.goalId == id && it.date == date.toString() }?.count ?: 0
    /** Validate before any read/write. Unknown versions or corruption must never be overwritten with defaults. */
    fun validated(): DailyGoalLedger = apply {
        require(version == 1)
        require(goals.map { it.id }.distinct().size == goals.size)
        require(entries.map { it.goalId to it.date }.distinct().size == entries.size)
        goals.forEach {
            require(it.id.isNotBlank() && it.title.isNotBlank() && it.title.length <= 100)
            require(it.target in 1..10_000 && it.weekdays.isNotEmpty() && it.weekdays.all { day -> day in 1..7 })
            LocalDate.parse(it.createdOn)
        }
        entries.forEach {
            require(it.target in 1..10_000 && it.count in 0..it.target && goals.any { g -> g.id == it.goalId })
            LocalDate.parse(it.date)
        }
    }
}
