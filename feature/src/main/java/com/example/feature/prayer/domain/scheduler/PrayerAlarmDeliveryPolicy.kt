package com.example.feature.prayer.domain.scheduler

/**
 * A scheduled adhan is delivered only while the device clock is still inside the
 * calculated instant plus [LATE_GRACE_MILLIS].
 *
 * The notification is dropped, and no sound is played, when:
 * - the trigger time is missing or not positive (an old or malformed broadcast);
 * - the broadcast arrives more than 15 minutes after the trigger, which covers Doze,
 *   inexact alarms, and a reboot that replays an already passed prayer.
 *
 * A broadcast that arrives before its trigger is not played. The same alarm id is armed
 * again for that future trigger, so the adhan is not lost and a second alarm is not added.
 *
 * Dropping the alert does not change the calculated prayer time shown in the app.
 */
object PrayerAlarmDeliveryPolicy {
    const val LATE_GRACE_MILLIS: Long = 15L * 60L * 1000L

    fun decide(triggerEpochMillis: Long, nowEpochMillis: Long, graceMillis: Long = LATE_GRACE_MILLIS): DeliveryDecision {
        if (triggerEpochMillis <= 0L) return DeliveryDecision.Drop
        if (nowEpochMillis < triggerEpochMillis) return DeliveryDecision.Rearm(triggerEpochMillis)
        if (nowEpochMillis <= triggerEpochMillis + graceMillis) return DeliveryDecision.Deliver
        return DeliveryDecision.Drop
    }

    fun shouldDeliver(triggerEpochMillis: Long, nowEpochMillis: Long, graceMillis: Long = LATE_GRACE_MILLIS): Boolean =
        decide(triggerEpochMillis, nowEpochMillis, graceMillis) is DeliveryDecision.Deliver

    sealed interface DeliveryDecision {
        data object Deliver : DeliveryDecision
        data object Drop : DeliveryDecision
        data class Rearm(val triggerEpochMillis: Long) : DeliveryDecision
    }
}
