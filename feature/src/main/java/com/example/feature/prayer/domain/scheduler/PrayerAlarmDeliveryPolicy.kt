package com.example.feature.prayer.domain.scheduler

/**
 * Distinguishes a prayer instant that is still worth announcing from a broadcast
 * Android delivered long after the calculated time (Doze, inexact alarms, or a stale task).
 */
object PrayerAlarmDeliveryPolicy {
    const val LATE_GRACE_MILLIS: Long = 15L * 60L * 1000L

    fun shouldDeliver(triggerEpochMillis: Long, nowEpochMillis: Long, graceMillis: Long = LATE_GRACE_MILLIS): Boolean {
        if (triggerEpochMillis <= 0L) return false
        return nowEpochMillis in triggerEpochMillis..(triggerEpochMillis + graceMillis)
    }
}
