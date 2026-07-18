package com.example.feature.prayer.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.feature.prayer.domain.model.PrayerAlarmPermissionState
import com.example.feature.prayer.domain.model.PrayerSchedule
import com.example.feature.prayer.domain.model.PrayerScheduleResult
import com.example.feature.prayer.domain.repository.PrayerAlarmGateway
import com.example.feature.prayer.worker.PrayerNotificationReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AndroidPrayerAlarmGateway(
    private val context: Context
) : PrayerAlarmGateway {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val mutex = Mutex()
    private val permissionState = MutableStateFlow(readPermissionState())
    private var lastFingerprint: String? = null
    private var lastAlarmIds: Set<Int> = emptySet()

    override fun observePermissionState(): Flow<PrayerAlarmPermissionState> = permissionState.asStateFlow()

    fun refreshPermissionState() {
        permissionState.value = readPermissionState()
    }

    override suspend fun replaceSchedule(schedule: PrayerSchedule): PrayerScheduleResult = mutex.withLock {
        refreshPermissionState()
        val permission = permissionState.value
        if (permission is PrayerAlarmPermissionState.NotificationsDenied) {
            cancelTrackedAlarms()
            lastFingerprint = null
            return PrayerScheduleResult.Skipped("notifications_denied")
        }

        val fingerprint = schedule.settingsFingerprint + "|" + schedule.dateEpochDay + "|" + schedule.alarms.size
        if (fingerprint == lastFingerprint && schedule.alarms.map { it.stableId }.toSet() == lastAlarmIds) {
            return PrayerScheduleResult.Scheduled(
                scheduledCount = schedule.alarms.size,
                skippedPastCount = 0,
                usedInexactFallback = permission is PrayerAlarmPermissionState.InexactOnly
            )
        }

        cancelTrackedAlarms()

        var scheduled = 0
        var failed = 0
        var usedInexact = false
        val newIds = mutableSetOf<Int>()

        schedule.alarms.forEach { alarm ->
            val ok = scheduleAlarm(alarm.stableId, alarm.triggerEpochMillis, alarm.title, alarm.message)
            if (ok.scheduled) {
                scheduled++
                newIds += alarm.stableId
                if (ok.inexact) usedInexact = true
            } else {
                failed++
            }
        }

        lastFingerprint = fingerprint
        lastAlarmIds = newIds

        return if (failed > 0 && scheduled == 0) {
            PrayerScheduleResult.Failed("all_alarms_failed")
        } else if (failed > 0) {
            PrayerScheduleResult.Failed("partial_failure:$failed")
        } else {
            PrayerScheduleResult.Scheduled(
                scheduledCount = scheduled,
                skippedPastCount = 0,
                usedInexactFallback = usedInexact || permission is PrayerAlarmPermissionState.InexactOnly
            )
        }
    }

    override suspend fun cancelAll(): Result<Unit> = mutex.withLock {
        runCatching {
            cancelTrackedAlarms()
            lastFingerprint = null
        }
    }

    private fun cancelTrackedAlarms() {
        lastAlarmIds.forEach { id ->
            val intent = Intent(context, PrayerNotificationReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pending)
            pending.cancel()
        }
        lastAlarmIds = emptySet()
    }

    private data class ScheduleOutcome(val scheduled: Boolean, val inexact: Boolean)

    private fun scheduleAlarm(id: Int, triggerTime: Long, title: String, message: String): ScheduleOutcome {
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("alarm_id", id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
            if (canExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                ScheduleOutcome(scheduled = true, inexact = false)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                ScheduleOutcome(scheduled = true, inexact = true)
            }
        } catch (_: SecurityException) {
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                ScheduleOutcome(scheduled = true, inexact = true)
            } catch (_: Exception) {
                ScheduleOutcome(scheduled = false, inexact = true)
            }
        } catch (_: Exception) {
            ScheduleOutcome(scheduled = false, inexact = false)
        }
    }

    private fun readPermissionState(): PrayerAlarmPermissionState {
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!notificationsEnabled) return PrayerAlarmPermissionState.NotificationsDenied
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                PrayerAlarmPermissionState.GrantedExact
            } else {
                PrayerAlarmPermissionState.InexactOnly
            }
        } else {
            PrayerAlarmPermissionState.GrantedExact
        }
    }
}
