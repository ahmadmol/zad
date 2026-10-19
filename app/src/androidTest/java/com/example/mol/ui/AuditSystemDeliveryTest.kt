package com.example.mol.ui

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.prayer.data.scheduler.AndroidPrayerAlarmGateway
import com.example.feature.prayer.domain.model.*
import com.example.feature.reminders.ReminderWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Runs only in the isolated QA package, with notification/exact-alarm grants. */
class AuditSystemDeliveryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun configuredReminderActuallyWaitsForSelectedMinuteThenPosts() = runBlocking {
        val settings = SettingsManager(context)
        val enabled = settings.morningReminderEnabledFlow.first()
        val hour = settings.morningReminderHourFlow.first()
        val minute = settings.morningReminderMinuteFlow.first()
        val coordinator = com.example.feature.reminders.AzkarReminderCoordinator(context, settings)
        val kind = com.example.feature.reminders.AzkarReminderKind.MORNING
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = kind.name.hashCode()
        val target = java.util.Calendar.getInstance().apply {
            // Leave at least 15 seconds for persisted configuration and actual JobScheduler registration.
            add(java.util.Calendar.MINUTE, if (get(java.util.Calendar.SECOND) > 44) 2 else 1)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }
        try {
            manager.cancel(notificationId)
            coordinator.update(kind, true, target.get(java.util.Calendar.HOUR_OF_DAY), target.get(java.util.Calendar.MINUTE))
            val work = WorkManager.getInstance(context)
            withTimeout(10_000) {
                work.getWorkInfosForUniqueWorkFlow(com.example.feature.reminders.AzkarReminderCoordinator.canonicalName(kind))
                    .first { infos -> infos.any { it.state == WorkInfo.State.ENQUEUED } }
            }
            assertFalse("Reminder must not execute immediately instead of respecting the selected time", manager.activeNotifications.any { it.id == notificationId })
            val delivered = withTimeout(150_000) {
                while (true) {
                    val lastRun = settings.morningReminderLastRunFlow.first()
                    if (lastRun != null && lastRun >= target.timeInMillis - 1000 && manager.activeNotifications.any { it.id == notificationId }) break
                    delay(250)
                }
                settings.morningReminderLastRunFlow.first()!!
            }
            assertTrue(delivered >= target.timeInMillis - 1000)
            File(context.getExternalFilesDir(null), "audit-reminder-scheduled-delivery.txt").writeText(
                "PASS actual production coordinator -> persisted selected time -> unique periodic WorkManager/JobScheduler -> notification. target=${target.timeInMillis}; delivered=$delivered; latenessMs=${delivered-target.timeInMillis}\n")
        } finally {
            coordinator.update(kind, enabled, hour, minute)
            manager.cancel(notificationId)
        }
    }
    @Test fun scheduledAlarmActuallyReachesReceiverAndPostsNotification() = runBlocking {
        val settings = SettingsManager(context)
        val priorSound = settings.notificationSoundTypeFlow.first()
        val gatewayContext = object : android.content.ContextWrapper(context) {
            override fun getSharedPreferences(name: String, mode: Int) = super.getSharedPreferences("$name-audit-delivery", mode)
        }
        val gateway = AndroidPrayerAlarmGateway(gatewayContext)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = "اختبار موعد محلي ${System.nanoTime()}"
        try {
            settings.setNotificationSoundType("SILENT")
            val request = PrayerAlarmRequest(945817, PrayerName.DHUHR, PrayerAlarmKind.EXACT,
                System.currentTimeMillis() + 5000, title, "اختبار إشعار آلي؛ ليس موعد صلاة حقيقيًا")
            val result = gateway.replaceSchedule(PrayerSchedule(java.time.LocalDate.now().toEpochDay(),
                "Asia/Damascus", PrayerLocation(36.2, 37.1, "حلب"), title, PrayerSchedulePolicy(), listOf(request)))
            assertTrue(result.toString(), result is PrayerScheduleResult.Scheduled)
            assertFalse("This scenario requires an exact-alarm grant", (result as PrayerScheduleResult.Scheduled).usedInexactFallback)
            withTimeout(30_000) {
                while (manager.activeNotifications.none { it.notification.extras.getString(Notification.EXTRA_TITLE) == title }) delay(250)
            }
            File(context.getExternalFilesDir(null), "audit-alarm-delivery.txt").writeText("PASS: AlarmManager → PrayerNotificationReceiver → active notification; API34 emulator; 5-second synthetic schedule\n")
        } finally {
            gateway.cancelAll()
            manager.activeNotifications.filter { it.notification.extras.getString(Notification.EXTRA_TITLE) == title }.forEach { manager.cancel(it.id) }
            settings.setNotificationSoundType(priorSound)
        }
    }
    @Test fun realWorkManagerReminderPostsAndDisabledReminderDoesNotPost() = runBlocking {
        val settings = SettingsManager(context)
        val enabled = settings.morningReminderEnabledFlow.first()
        val hour = settings.morningReminderHourFlow.first()
        val minute = settings.morningReminderMinuteFlow.first()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val work = WorkManager.getInstance(context)
        val id = "MORNING".hashCode()
        suspend fun runWorker() {
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(workDataOf(ReminderWorker.KEY_KIND to "MORNING")).build()
            work.enqueue(request)
            val finished = withTimeout(30_000) {
                work.getWorkInfoByIdFlow(request.id).first { it?.state?.isFinished == true }
            }
            assertEquals(WorkInfo.State.SUCCEEDED, finished!!.state)
        }
        try {
            settings.setMorningReminder(true, hour, minute)
            manager.cancel(id)
            runWorker()
            assertTrue(manager.activeNotifications.any { it.id == id })
            assertNotNull(settings.morningReminderLastRunFlow.first())
            manager.cancel(id)
            settings.setMorningReminder(false, hour, minute)
            runWorker()
            assertFalse(manager.activeNotifications.any { it.id == id })
            File(context.getExternalFilesDir(null), "audit-reminder-delivery.txt").writeText("PASS: actual WorkManager worker delivery and disabled-setting suppression; API34 emulator\n")
        } finally { settings.setMorningReminder(enabled, hour, minute); manager.cancel(id) }
    }
}
