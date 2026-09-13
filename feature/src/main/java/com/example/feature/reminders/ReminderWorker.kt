package com.example.feature.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.feature.azkar.data.local.SettingsManager

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val kind = inputData.getString(KEY_KIND) ?: return Result.failure()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }
        val morning = kind == AzkarReminderKind.MORNING.name
        val title = if (morning) "أذكار الصباح" else "أذكار المساء"
        val message = if (morning) "ابدأ صباحك بذكر الله" else "اختم مساءك بذكر الله"
        return try {
            showNotification(title, message)
            SettingsManager(applicationContext).recordReminderRun(kind, System.currentTimeMillis())
            Result.success()
        } catch (_: SecurityException) {
            Result.failure()
        }
    }

    private fun showNotification(title: String, message: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "تذكيرات الأذكار", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        manager.notify(
            inputData.getString(KEY_KIND).hashCode(),
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setAutoCancel(true)
                .build()
        )
    }

    companion object {
        const val KEY_KIND = "reminder_kind"
        private const val CHANNEL_ID = "azkar_reminders"
    }
}
