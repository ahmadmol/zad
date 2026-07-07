package com.example.feature.prayer.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.prayer.util.PrayerCalculator
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.TimeUnit

class AdhanWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val userPreferences: UserPreferences by inject()

    override suspend fun doWork(): Result {
        val prayerName = inputData.getString("prayer_name")
        
        if (prayerName != null) {
            val soundUri = userPreferences.adhanSoundUri.first()
            showAdhanNotification(prayerName, soundUri)
        } else {
            scheduleDailyAdhans(applicationContext)
        }
        
        return Result.success()
    }

    private fun showAdhanNotification(prayerName: String, soundUri: String?) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val baseChannelId = "adhan_notifications"
        val channelId = if (soundUri != null) "${baseChannelId}_${soundUri.hashCode()}" else baseChannelId

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "مواقيت الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات بمواعيد الأذان والصلوات الخمس"
                if (soundUri != null) {
                    val uri = Uri.parse(soundUri)
                    val attributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(uri, attributes)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("حان الآن موعد أذان $prayerName")
            .setContentText("أقم صلاتك تنعم بحياتك")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (soundUri != null) {
            notificationBuilder.setSound(Uri.parse(soundUri))
        }

        notificationManager.notify(prayerName.hashCode(), notificationBuilder.build())
    }

    private suspend fun scheduleDailyAdhans(context: Context) {
        val lat = userPreferences.userLatitude.first() ?: 24.7136 // Default Riyadh
        val lng = userPreferences.userLongitude.first() ?: 46.6753
        
        val prayers = PrayerCalculator.calculate(lat, lng)
        val now = System.currentTimeMillis()
        val workManager = WorkManager.getInstance(context)

        // Cancel previous pending adhans to avoid duplicates
        workManager.cancelAllWorkByTag("adhan_tag")

        prayers.forEach { prayer ->
            if (prayer.timestamp > now) {
                val delay = prayer.timestamp - now
                val adhanRequest = OneTimeWorkRequestBuilder<AdhanWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf("prayer_name" to prayer.nameAr))
                    .addTag("adhan_tag")
                    .build()
                
                workManager.enqueue(adhanRequest)
            }
        }
    }
}
