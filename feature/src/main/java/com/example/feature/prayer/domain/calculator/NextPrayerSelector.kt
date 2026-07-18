package com.example.feature.prayer.domain.calculator

import com.example.feature.prayer.domain.model.NextPrayer
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerInstant
import com.example.feature.prayer.domain.model.PrayerName

object NextPrayerSelector {
    /**
     * Selects the next upcoming prayer from [today] or, after Isha, the first prayer of [tomorrow].
     * Matches historical Home/Prayer ViewModel behavior.
     */
    fun select(
        today: PrayerDay,
        tomorrow: PrayerDay,
        nowEpochMillis: Long
    ): NextPrayer? {
        val todayUpcoming = today.instants.firstOrNull { it.epochMillis > nowEpochMillis }
        val chosen: PrayerInstant = todayUpcoming
            ?: tomorrow.instants.firstOrNull()
            ?: return null

        val all = (today.instants + tomorrow.instants).sortedBy { it.epochMillis }
        val index = all.indexOfFirst { it.epochMillis == chosen.epochMillis }
        val previous = if (index > 0) all[index - 1].epochMillis else null

        return NextPrayer(
            name = chosen.name,
            epochMillis = chosen.epochMillis,
            remainingMillis = (chosen.epochMillis - nowEpochMillis).coerceAtLeast(0L),
            previousEpochMillis = previous
        )
    }

    fun progressFraction(next: NextPrayer, nowEpochMillis: Long): Float {
        val previous = next.previousEpochMillis ?: return 0f
        val total = next.epochMillis - previous
        if (total <= 0L) return 0f
        val elapsed = nowEpochMillis - previous
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }
}
