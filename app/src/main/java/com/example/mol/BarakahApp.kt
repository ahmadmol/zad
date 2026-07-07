package com.example.mol

import android.app.Application
import com.example.mol.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.feature.prayer.worker.AdhanWorker
import com.example.feature.azkar.worker.AzkarNotificationWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import java.util.Calendar

class IhsanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@IhsanApp)
            modules(appModule)
        }
        
        setupWorkers()
    }

    private fun setupWorkers() {
        val workManager = WorkManager.getInstance(this)
        
        // Adhan Scheduler
        val adhanSchedulerRequest = PeriodicWorkRequestBuilder<AdhanWorker>(24, TimeUnit.HOURS)
            .addTag("adhan_scheduler")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "adhan_scheduler",
            ExistingPeriodicWorkPolicy.KEEP,
            adhanSchedulerRequest
        )

        // Azkar Notifications (Morning)
        scheduleAzkarWork("MORNING", 6)
        
        // Azkar Notifications (Evening)
        scheduleAzkarWork("EVENING", 18)
    }

    private fun scheduleAzkarWork(type: String, hour: Int) {
        val workManager = WorkManager.getInstance(this)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val azkarRequest = PeriodicWorkRequestBuilder<AzkarNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("azkar_type" to type))
            .build()

        workManager.enqueueUniquePeriodicWork(
            "azkar_$type",
            ExistingPeriodicWorkPolicy.KEEP,
            azkarRequest
        )
    }
}
