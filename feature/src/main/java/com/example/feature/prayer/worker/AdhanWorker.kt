package com.example.feature.prayer.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.usecase.ReconcilePrayerScheduleUseCase
import com.example.feature.prayer.util.AdhanNotificationChannelFactory
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Periodic worker that reconciles prayer alarms through the shared use case.
 * Notification display for named prayers remains for backward-compatible work requests.
 */
class AdhanWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val userPreferences: UserPreferences by inject()
    private val reconcileUseCase: ReconcilePrayerScheduleUseCase by inject()

    override suspend fun doWork(): Result {
        val prayerName = inputData.getString("prayer_name")

        return if (prayerName != null) {
            val soundUri = userPreferences.adhanSoundUri.first()
            AdhanNotificationChannelFactory.showNotification(
                context = applicationContext,
                title = "حان الآن موعد أذان $prayerName",
                message = "أقم صلاتك تنعم بحياتك",
                soundType = "DEFAULT_ATHAN",
                customAdhanUri = soundUri,
                notificationId = prayerName.hashCode()
            )
            Result.success()
        } else {
            reconcileUseCase(PrayerReconciliationReason.ApplicationStart)
            Result.success()
        }
    }
}
