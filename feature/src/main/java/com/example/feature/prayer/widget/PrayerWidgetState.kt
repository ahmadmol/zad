package com.example.feature.prayer.widget

import com.example.feature.prayer.domain.calculator.NextPrayerSelector
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerName
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 * Phase 7 — what the Prayer home widget renders.
 *
 * Pure data: no Android types, no clock of its own, no calculation. The whole point is
 * that the widget shows the *same* numbers as the Prayer screen, so its only inputs are
 * the canonical [PrayerDay]s produced by the shared `PrayerCalculator` plus the current
 * time. It never computes prayer times itself.
 */
sealed interface PrayerWidgetState {

    /** Location or settings are not available yet — the widget says so rather than lying. */
    data class Unavailable(val reason: Reason) : PrayerWidgetState {
        enum class Reason { NO_LOCATION, NOT_READY }
    }

    data class Ready(
        val nextPrayerName: PrayerName,
        val nextPrayerTimeLabel: String,
        /** Whole minutes until the next prayer; never negative. */
        val minutesUntilNext: Long,
        val locationLabel: String?,
        /** Today's prayers in order, for the expanded widget size. */
        val today: List<Entry>
    ) : PrayerWidgetState {
        data class Entry(
            val name: PrayerName,
            val timeLabel: String,
            val isNext: Boolean,
            val isPast: Boolean
        )
    }
}

/**
 * Builds [PrayerWidgetState] from canonical prayer data.
 *
 * Reuses [NextPrayerSelector] — the exact selector the Prayer screen and Home summary
 * use — so the widget cannot drift from the app.
 */
object PrayerWidgetStateFactory {

    fun create(
        today: PrayerDay?,
        tomorrow: PrayerDay?,
        nowEpochMillis: Long,
        locationLabel: String?,
        zoneId: ZoneId,
        locale: Locale = Locale.getDefault()
    ): PrayerWidgetState {
        if (today == null || tomorrow == null) {
            return PrayerWidgetState.Unavailable(
                if (locationLabel == null) PrayerWidgetState.Unavailable.Reason.NO_LOCATION
                else PrayerWidgetState.Unavailable.Reason.NOT_READY
            )
        }

        val next = NextPrayerSelector.select(today, tomorrow, nowEpochMillis)
            ?: return PrayerWidgetState.Unavailable(
                PrayerWidgetState.Unavailable.Reason.NOT_READY
            )

        val entries = today.instants.map { instant ->
            PrayerWidgetState.Ready.Entry(
                name = instant.name,
                timeLabel = formatTime(instant.epochMillis, zoneId, locale),
                // Identity by instant, not by name: the "next" prayer can legitimately
                // be tomorrow's Fajr, which is not in today's list at all.
                isNext = instant.epochMillis == next.epochMillis,
                isPast = instant.epochMillis <= nowEpochMillis
            )
        }

        return PrayerWidgetState.Ready(
            nextPrayerName = next.name,
            nextPrayerTimeLabel = formatTime(next.epochMillis, zoneId, locale),
            minutesUntilNext = (next.remainingMillis / 60_000L).coerceAtLeast(0L),
            locationLabel = locationLabel,
            today = entries
        )
    }

    /**
     * When the widget should next redraw: the upcoming prayer instant, so the countdown
     * is refreshed exactly when it becomes stale rather than by polling.
     */
    fun nextRefreshEpochMillis(
        today: PrayerDay?,
        tomorrow: PrayerDay?,
        nowEpochMillis: Long
    ): Long? {
        if (today == null || tomorrow == null) return null
        return NextPrayerSelector.select(today, tomorrow, nowEpochMillis)?.epochMillis
    }

    private fun formatTime(epochMillis: Long, zoneId: ZoneId, locale: Locale): String {
        val time = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalTime()
        return java.time.format.DateTimeFormatter
            .ofPattern("hh:mm a", locale)
            .format(time)
    }
}
