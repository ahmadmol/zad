package com.example.feature.prayer.domain.scheduler

/**
 * A scheduled adhan is delivered only while the device clock is still inside the
 * calculated instant plus [LATE_GRACE_MILLIS].
 *
 * The notification is dropped, and no sound is played, when:
 * - the trigger time is missing or not positive (an old or malformed broadcast);
 * - the broadcast arrives before its trigger (a stale task from a previous clock);
 * - the broadcast arrives more than 15 minutes after the trigger, which covers Doze,
 *   inexact alarms, and a reboot that replays an already passed prayer.
 *
 * Dropping the alert does not change the calculated prayer time shown in the app.
 */
object PrayerAlarmDeliveryPolicy {
    const val LATE_GRACE_MILLIS: Long = 15L * 60L * 1000L

    fun shouldDeliver(triggerEpochMillis: Long, nowEpochMillis: Long, graceMillis: Long = LATE_GRACE_MILLIS): Boolean {
        if (triggerEpochMillis <= 0L) return false
        return nowEpochMillis in triggerEpochMillis..(triggerEpochMillis + graceMillis)
    }
}
