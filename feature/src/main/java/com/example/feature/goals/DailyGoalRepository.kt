package com.example.feature.goals

import com.example.feature.core.preferences.UserPreferences
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

/** Explicit personal goals are independent of the prayer, fasting and Quran journals. */
class DailyGoalRepository(private val preferences: UserPreferences, private val clock: Clock? = null) {
    val ledger = preferences.dailyGoalLedger
    fun today(): LocalDate = LocalDate.now(clock ?: Clock.systemDefaultZone())

    suspend fun save(id: String?, title: String, target: Int, weekdays: Set<Int>) {
        require(title.trim().isNotEmpty() && title.trim().length <= 100)
        require(target in 1..10_000 && weekdays.isNotEmpty() && weekdays.all { it in 1..7 })
        preferences.updateDailyGoalLedger { current ->
            val existing = id?.let { key -> current.goals.single { it.id == key && !it.archived } }
            require(existing != null || current.goals.count { !it.archived } < 50)
            val goal = DailyGoal(existing?.id ?: UUID.randomUUID().toString(), title.trim(), target, weekdays,
                existing?.createdOn ?: today().toString())
            current.copy(goals = if (existing == null) current.goals + goal else current.goals.map { if (it.id == goal.id) goal else it })
        }
    }

    /** Removal archives the definition, retaining the user's explicit historical entries. */
    suspend fun archive(id: String) = preferences.updateDailyGoalLedger { current ->
        require(current.goals.any { it.id == id && !it.archived })
        current.copy(goals = current.goals.map { if (it.id == id) it.copy(archived = true) else it })
    }

    suspend fun increment(id: String, expectedDate: LocalDate, delta: Int) = record(id, expectedDate) { count, target ->
        (count + delta.coerceIn(-10_000, 10_000)).coerceIn(0, target)
    }

    suspend fun complete(id: String, expectedDate: LocalDate, completed: Boolean) = record(id, expectedDate) { _, target ->
        if (completed) target else 0
    }

    private suspend fun record(id: String, expectedDate: LocalDate, transform: (Int, Int) -> Int) {
        preferences.updateDailyGoalLedger { current ->
            val date = today()
            require(expectedDate == date) { "تغير اليوم؛ أعد فتح أهداف اليوم" }
            val goal = current.goals.single { it.id == id && it.scheduledOn(date) }
            val count = transform(current.count(id, date), goal.target)
            val record = DailyGoalEntry(id, date.toString(), count, goal.target)
            current.copy(entries = current.entries.filterNot { it.goalId == id && it.date == record.date } + record)
        }
    }
}
