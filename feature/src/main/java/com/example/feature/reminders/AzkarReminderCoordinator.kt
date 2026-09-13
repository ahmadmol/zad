package com.example.feature.reminders

import android.content.Context
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.feature.azkar.data.local.SettingsManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

enum class AzkarReminderKind { MORNING, EVENING }

data class AzkarReminderConfig(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val lastRunAt: Long?
)

data class ReminderRegistration(
    val uniqueName: String,
    val workId: String? = null,
    val state: WorkInfo.State? = null
)

/** Single owner for persisted configuration and WorkManager identities. */
class AzkarReminderCoordinator(
    context: Context,
    private val settings: SettingsManager
) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun config(kind: AzkarReminderKind): Flow<AzkarReminderConfig> = when (kind) {
        AzkarReminderKind.MORNING -> combine(
            settings.morningReminderEnabledFlow,
            settings.morningReminderHourFlow,
            settings.morningReminderMinuteFlow,
            settings.morningReminderLastRunFlow,
            ::AzkarReminderConfig
        )
        AzkarReminderKind.EVENING -> combine(
            settings.eveningReminderEnabledFlow,
            settings.eveningReminderHourFlow,
            settings.eveningReminderMinuteFlow,
            settings.eveningReminderLastRunFlow,
            ::AzkarReminderConfig
        )
    }.distinctUntilChanged()

    fun registration(kind: AzkarReminderKind): Flow<ReminderRegistration> = callbackFlow {
        val uniqueName = canonicalName(kind)
        val liveData = workManager.getWorkInfosForUniqueWorkLiveData(uniqueName)
        val observer = Observer<List<WorkInfo>> { infos ->
            val current = infos.maxByOrNull { it.generation }
            trySend(ReminderRegistration(uniqueName, current?.id?.toString(), current?.state))
        }
        liveData.observeForever(observer)
        awaitClose { liveData.removeObserver(observer) }
    }.distinctUntilChanged()

    suspend fun update(kind: AzkarReminderKind, enabled: Boolean, hour: Int, minute: Int) {
        require(hour in 0..23 && minute in 0..59)
        when (kind) {
            AzkarReminderKind.MORNING -> settings.setMorningReminder(enabled, hour, minute)
            AzkarReminderKind.EVENING -> settings.setEveningReminder(enabled, hour, minute)
        }
        reconcile(kind)
    }

    suspend fun reconcileAll() {
        LEGACY_UNIQUE_NAMES.forEach(workManager::cancelUniqueWork)
        AzkarReminderKind.entries.forEach { reconcile(it) }
    }

    suspend fun reconcile(kind: AzkarReminderKind) {
        val configuration = config(kind).first()
        val uniqueName = canonicalName(kind)
        if (!configuration.enabled) {
            workManager.cancelUniqueWork(uniqueName)
            legacyNames(kind).forEach(workManager::cancelUniqueWork)
            return
        }

        legacyNames(kind).forEach(workManager::cancelUniqueWork)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(nextDelay(configuration.hour, configuration.minute), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_KIND to kind.name))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .addTag(CANONICAL_TAG)
            .build()
        workManager.enqueueUniquePeriodicWork(uniqueName, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun nextDelay(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return (next.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }

    companion object {
        const val CANONICAL_TAG = "ihsan.azkar.reminder"
        fun canonicalName(kind: AzkarReminderKind) = "ihsan.azkar.reminder.${kind.name.lowercase()}"

        private val LEGACY_UNIQUE_NAMES = listOf(
            "azkar_MORNING", "azkar_EVENING", "أذكار الصباح", "أذكار المساء"
        )

        private fun legacyNames(kind: AzkarReminderKind) = when (kind) {
            AzkarReminderKind.MORNING -> listOf("azkar_MORNING", "أذكار الصباح")
            AzkarReminderKind.EVENING -> listOf("azkar_EVENING", "أذكار المساء")
        }
    }
}
