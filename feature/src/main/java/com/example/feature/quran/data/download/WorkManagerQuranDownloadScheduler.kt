package com.example.feature.quran.data.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import androidx.lifecycle.Observer
import com.example.feature.quran.domain.usecase.QuranDownloadNetworkPolicy
import com.example.feature.quran.domain.usecase.QuranDownloadProgress
import com.example.feature.quran.domain.usecase.QuranDownloadScheduler
import com.example.feature.quran.domain.usecase.QuranDownloadStatus
import com.example.feature.quran.worker.QuranDownloadWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class WorkManagerQuranDownloadScheduler(context: Context) : QuranDownloadScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun identity(surahId: Int, readerId: String): String = tagFor(surahId, readerId)

    override fun enqueue(
        surahId: Int,
        readerId: String,
        networkPolicy: QuranDownloadNetworkPolicy
    ): String {
        val tag = identity(surahId, readerId)
        val networkType = when (networkPolicy) {
            QuranDownloadNetworkPolicy.Connected -> NetworkType.CONNECTED
            QuranDownloadNetworkPolicy.Unmetered -> NetworkType.UNMETERED
        }
        val request = OneTimeWorkRequestBuilder<QuranDownloadWorker>()
            .setInputData(workDataOf("surah_id" to surahId, "reader_id" to readerId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(networkType).build())
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                30_000L,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .addTag(tag)
            .build()

        workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.KEEP, request)
        return tag
    }

    override fun observe(tag: String): Flow<QuranDownloadProgress> = callbackFlow {
        val liveData = workManager.getWorkInfosForUniqueWorkLiveData(tag)
        val observer = Observer<List<WorkInfo>> { infos ->
            val info = infos.firstOrNull()
            trySend(
                if (info == null) {
                    QuranDownloadProgress(tag, QuranDownloadStatus.Unknown, 0)
                } else {
                    info.toQuranDownloadProgress(tag)
                }
            )
        }
        liveData.observeForever(observer)
        awaitClose { liveData.removeObserver(observer) }
    }

    companion object {
        fun tagFor(surahId: Int, readerId: String): String =
            "download_surah_${surahId}_$readerId"
    }
}

internal fun WorkInfo.toQuranDownloadProgress(tag: String): QuranDownloadProgress =
    mapQuranDownloadProgress(
        tag = tag,
        state = state,
        percent = progress.getInt("progress", 0),
        errorMessage = outputData.getString("error")
    )

internal fun mapQuranDownloadProgress(
    tag: String,
    state: WorkInfo.State,
    percent: Int,
    errorMessage: String?
): QuranDownloadProgress = QuranDownloadProgress(
    tag = tag,
    status = state.toDomainStatus(),
    percent = percent.coerceIn(0, 100),
    errorMessage = errorMessage
)

private fun WorkInfo.State.toDomainStatus(): QuranDownloadStatus = when (this) {
    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> QuranDownloadStatus.Enqueued
    WorkInfo.State.RUNNING -> QuranDownloadStatus.Running
    WorkInfo.State.SUCCEEDED -> QuranDownloadStatus.Succeeded
    WorkInfo.State.FAILED -> QuranDownloadStatus.Failed
    WorkInfo.State.CANCELLED -> QuranDownloadStatus.Cancelled
}
