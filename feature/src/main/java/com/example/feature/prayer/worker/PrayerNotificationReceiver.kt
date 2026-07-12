package com.example.feature.prayer.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.preferences.UserPreferences
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
                showNotification(context, title, message, soundType, customAdhanUri)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        soundType: String,
        customAdhanUri: String?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = resolveSoundUri(context, soundType, customAdhanUri)
        val channelId = buildChannelId(soundType, soundUri)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (soundType == "SILENT") {
                NotificationManager.IMPORTANCE_LOW
            } else {
                NotificationManager.IMPORTANCE_HIGH
            }

            val channel = NotificationChannel(channelId, "مواقيت الصلاة", importance).apply {
                description = "تنبيهات الصلاة والأذان"
                enableVibration(soundType != "SILENT")
                if (soundType == "SILENT" || soundUri == null) {
                    setSound(null, null)
                } else {
                    val attributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(soundUri, attributes)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                if (soundType == "SILENT") NotificationCompat.PRIORITY_LOW
                else NotificationCompat.PRIORITY_HIGH
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)

        when {
            soundType == "SILENT" -> builder.setSilent(true)
            soundUri != null -> {
                builder.setSound(soundUri)
                builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS)
            }
            else -> builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun resolveSoundUri(
        context: Context,
        soundType: String,
        customAdhanUri: String?
    ): Uri? {
        return when (soundType) {
            "SILENT" -> null
            "SHORT_TONE" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "DEFAULT_ATHAN" -> {
                customAdhanUri
                    ?.takeIf { it.isNotBlank() }
                    ?.let { Uri.parse(it) }
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    private fun buildChannelId(soundType: String, soundUri: Uri?): String {
        val uriKey = soundUri?.toString()?.hashCode() ?: 0
        return "prayer_notifications_${soundType}_$uriKey"
    }
}
