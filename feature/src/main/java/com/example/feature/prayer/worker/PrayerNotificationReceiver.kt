package com.example.feature.prayer.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.designsystem.R
import com.example.feature.azkar.data.local.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "تنبيه الصلاة"
        val message = intent.getStringExtra("message") ?: ""
        
        // We need to fetch sound settings from DataStore
        // Since onReceive is synchronous, we use a CoroutineScope
        val settingsManager = SettingsManager(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            val soundType = settingsManager.notificationSoundTypeFlow.first()
            showNotification(context, title, message, soundType)
        }
    }

    private fun showNotification(context: Context, title: String, message: String, soundType: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "prayer_notifications_$soundType"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "مواقيت الصلاة"
            val importance = if (soundType == "SILENT") {
                NotificationManager.IMPORTANCE_LOW
            } else {
                NotificationManager.IMPORTANCE_HIGH
            }
            
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                if (soundType != "SILENT") {
                    enableVibration(true)
                    // TODO: Set actual Athan/Tone URI when files are added to raw resources
                    // val soundUri = Uri.parse("android.resource://${context.packageName}/raw/athan")
                    // setSound(soundUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
                } else {
                    setSound(null, null)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (soundType == "SILENT") NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (soundType == "SILENT") {
            builder.setSilent(true)
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            // TODO: builder.setSound(soundUri)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
