package com.example.mol.ui

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import androidx.test.platform.app.InstrumentationRegistry
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.prayer.data.scheduler.AndroidPrayerAlarmGateway
import com.example.feature.prayer.data.settings.PrayerSettingsRepositoryImpl
import com.example.feature.prayer.domain.model.*
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import com.example.feature.prayer.worker.PrayerNotificationReceiver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.koin.core.context.GlobalContext

class AuditAlarmRegressionTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val repo get() = GlobalContext.get().get<PrayerSettingsRepository>()
    private val manager get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val nonce = System.nanoTime().toString()
    private val gateways = mutableListOf<AndroidPrayerAlarmGateway>()
    private var priorEnabled = true
    private var priorSound = "SILENT"
    private val title = "اختبار جدولة $nonce"
    private val alarmId = 945818
    private val sandbox by lazy { object : ContextWrapper(context) {
        override fun getSharedPreferences(name: String, mode: Int) = super.getSharedPreferences("$name-$nonce", mode)
    } }
    @Before fun prepare() = runBlocking {
        priorEnabled = repo.observeSettings().first().notificationsEnabled
        priorSound = SettingsManager(context).notificationSoundTypeFlow.first()
        repo.updateNotificationsEnabled(true)
        SettingsManager(context).setNotificationSoundType("SILENT")
    }
    @After fun restore() = runBlocking {
        gateways.forEach { it.cancelAll() }
        manager.activeNotifications.filter { it.notification.extras.getString(Notification.EXTRA_TITLE) == title }.forEach { manager.cancel(it.id) }
        repo.updateNotificationsEnabled(priorEnabled)
        SettingsManager(context).setNotificationSoundType(priorSound)
    }
    private fun gateway() = AndroidPrayerAlarmGateway(sandbox).also { gateways += it }
    private fun schedule(delay: Long) = PrayerSchedule(java.time.LocalDate.now().toEpochDay(), "Asia/Damascus",
        PrayerLocation(36.2, 37.1, "حلب"), "same-settings", PrayerSchedulePolicy(), listOf(
            PrayerAlarmRequest(alarmId, PrayerName.DHUHR, PrayerAlarmKind.EXACT,
                System.currentTimeMillis() + delay, title, "اختبار آلي؛ ليس موعدًا حقيقيًا")))
    private fun pending() = PendingIntent.getBroadcast(sandbox, alarmId, Intent(sandbox, PrayerNotificationReceiver::class.java),
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
    @Test fun recreatedGatewayStillCancelsPersistedAlarmIds() = runBlocking {
        assertTrue(gateway().replaceSchedule(schedule(60_000)) is PrayerScheduleResult.Scheduled)
        assertNotNull(pending())
        gateway().cancelAll().getOrThrow()
        assertNull("A new gateway must cancel alarms created before recreation", pending())
    }
    @Test fun changedTriggerTimeReplacesSameIdAndSettingsRatherThanBeingDeduplicated() = runBlocking {
        val gateway = gateway()
        gateway.replaceSchedule(schedule(60_000))
        gateway.replaceSchedule(schedule(3_000))
        withTimeout(12_000) {
            while (manager.activeNotifications.none { it.notification.extras.getString(Notification.EXTRA_TITLE) == title }) delay(200)
        }
    }
    @Test fun disabledNotificationSettingSuppressesAnAlreadyDeliveredStaleBroadcast() = runBlocking {
        repo.updateNotificationsEnabled(false)
        context.sendBroadcast(Intent(context, PrayerNotificationReceiver::class.java).putExtra("title", title).putExtra("message", "اختبار تنبيه ملغى"))
        delay(2500)
        assertFalse(manager.activeNotifications.any { it.notification.extras.getString(Notification.EXTRA_TITLE) == title })
    }
    @Test fun calculationOffsetsPersistAcrossRepositoryRecreation() = runBlocking {
        val before = repo.observeSettings().first().offsets
        try {
            val expected = PrayerOffsets(fajrMinutes = 2, sunriseMinutes = -1, dhuhrMinutes = 3,
                asrMinutes = 4, maghribMinutes = 1, ishaMinutes = -2)
            repo.updateOffsets(expected)
            assertEquals(expected, PrayerSettingsRepositoryImpl(SettingsManager(context)).observeSettings().first().offsets)
        } finally { repo.updateOffsets(before) }
    }
}
