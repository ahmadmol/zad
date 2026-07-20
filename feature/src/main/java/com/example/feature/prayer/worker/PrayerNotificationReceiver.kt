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
        val pendingResult = goAsync()

        val settingsManager = SettingsManager(context)
        val userPreferences = UserPreferences(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val soundType = settingsManager.notificationSoundTypeFlow.first()
                val customAdhanUri = userPreferences.adhanSoundUri.first()
                AdhanNotificationChannelFactory.showNotification(
                    context = context,
                    title = title,
                    message = message,
                    soundType = soundType,
                    customAdhanUri = customAdhanUri
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
