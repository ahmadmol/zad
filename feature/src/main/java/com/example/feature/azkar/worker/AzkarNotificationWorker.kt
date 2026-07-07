package com.example.feature.azkar.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent

class AzkarNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    override suspend fun doWork(): Result {
        val type = inputData.getString("azkar_type") ?: return Result.failure()
        showAzkarNotification(type)
        return Result.success()
    }

    private fun showAzkarNotification(type: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "azkar_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تنبيهات الأذكار",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val title = if (type == "MORNING") "أذكار الصباح" else "أذكار المساء"
        val message = if (type == "MORNING") "نور صباحك بذكر الله" else "نور مسائك بذكر الله"

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(type.hashCode(), notification)
    }
}
