package com.example.feature.prayer.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.feature.prayer.domain.model.PrayerReconciliationReason
import com.example.feature.prayer.domain.usecase.ReconcilePrayerScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Thin receiver that routes system events into the shared reconciliation use case.
 */
class PrayerSystemReconciliationReceiver : BroadcastReceiver(), KoinComponent {

    private val reconcileUseCase: ReconcilePrayerScheduleUseCase by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        val reason = when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> PrayerReconciliationReason.DeviceBooted
            Intent.ACTION_MY_PACKAGE_REPLACED -> PrayerReconciliationReason.AppUpdated
            Intent.ACTION_TIME_CHANGED -> PrayerReconciliationReason.TimeChanged
            Intent.ACTION_TIMEZONE_CHANGED -> PrayerReconciliationReason.TimezoneChanged
            Intent.ACTION_DATE_CHANGED -> PrayerReconciliationReason.TimeChanged
            else -> PrayerReconciliationReason.ManualRetry
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reconcileUseCase(reason)
            } finally {
                pending.finish()
            }
        }
    }
}
