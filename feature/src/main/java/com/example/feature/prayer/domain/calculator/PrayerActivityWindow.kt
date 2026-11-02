package com.example.feature.prayer.domain.calculator

/**
 * Defines the compact dashboard's active-prayer presentation window.
 * The full prayer schedule remains the source of truth for exact prayer times;
 * this window only determines which card receives the active visual treatment.
 */
object PrayerActivityWindow {
    const val ACTIVE_WINDOW_MILLIS: Long = 45 * 60 * 1000L

    fun isActive(prayerEpochMillis: Long, nowEpochMillis: Long): Boolean =
        nowEpochMillis >= prayerEpochMillis &&
            nowEpochMillis < prayerEpochMillis + ACTIVE_WINDOW_MILLIS
}
