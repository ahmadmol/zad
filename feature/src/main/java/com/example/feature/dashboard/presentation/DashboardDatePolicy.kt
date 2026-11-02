package com.example.feature.dashboard.presentation

/** Keeps the one-second clock tick independent from the once-per-calendar-day Hijri refresh. */
object DashboardDatePolicy {
    fun shouldRefreshHijriDate(previousDateKey: String?, currentDateKey: String): Boolean =
        previousDateKey != currentDateKey
}
