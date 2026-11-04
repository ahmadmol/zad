package com.example.feature.prayer.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.prayer.util.AdhanNotificationChannelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "تنبيه الصلاة"
        val message = intent.getStringExtra("message") ?: ""
        val triggerEpoch = intent.getLongExtra("trigger_epoch", 0L)
        val alarmId = intent.getIntExtra("alarm_id", 0)
        when (val decision = com.example.feature.prayer.domain.scheduler.PrayerAlarmDeliveryPolicy.decide(
            triggerEpoch, System.currentTimeMillis()
        )) {
            is com.example.feature.prayer.domain.scheduler.PrayerAlarmDeliveryPolicy.DeliveryDecision.Rearm -> {
                com.example.feature.prayer.data.scheduler.AndroidPrayerAlarmGateway(context)
                    .rearmFutureAlarm(alarmId, decision.triggerEpochMillis, title, message)
                return
            }
            com.example.feature.prayer.domain.scheduler.PrayerAlarmDeliveryPolicy.DeliveryDecision.Drop -> return
            com.example.feature.prayer.domain.scheduler.PrayerAlarmDeliveryPolicy.DeliveryDecision.Deliver -> Unit
        }
        val pendingResult = goAsync()

        val settingsManager = SettingsManager(context)
        val userPreferences = UserPreferences(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val enabled = org.koin.core.context.GlobalContext.get()
                    .get<com.example.feature.prayer.domain.repository.PrayerSettingsRepository>()
                    .observeSettings().first().notificationsEnabled
                // A broadcast queued before cancellation must respect the latest setting.
                if (!enabled) return@launch
                val soundType = settingsManager.notificationSoundTypeFlow.first()
                val customAdhanUri = userPreferences.adhanSoundUri.first()
                AdhanNotificationChannelFactory.showNotification(
                    context = context,
                    title = title,
                    message = message,
                    soundType = soundType,
                    customAdhanUri = customAdhanUri
                )
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                android.util.Log.e("PrayerNotificationReceiver", "Unable to deliver prayer notification", failure)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
