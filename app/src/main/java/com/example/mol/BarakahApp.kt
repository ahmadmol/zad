package com.example.mol

import android.app.Application
import com.example.mol.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.feature.prayer.worker.AdhanWorker
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import com.example.feature.quran.worker.QuranWorkerFactory
import com.example.feature.reminders.AzkarReminderCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class IhsanApp : Application(), Configuration.Provider, KoinComponent {
    private val reminderCoordinator: AzkarReminderCoordinator by inject()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(QuranWorkerFactory())
            .build()

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

        applicationScope.launch { reminderCoordinator.reconcileAll() }
    }
}
