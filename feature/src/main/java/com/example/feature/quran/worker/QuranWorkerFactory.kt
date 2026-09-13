package com.example.feature.quran.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.feature.quran.data.download.QuranAudioDownloader
import com.example.feature.quran.domain.repository.QuranRepository
import org.koin.core.context.GlobalContext

class QuranWorkerFactory : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return if (workerClassName == QuranDownloadWorker::class.java.name) {
            val koin = GlobalContext.get()
            QuranDownloadWorker(
                appContext,
                workerParameters,
                repository = koin.get<QuranRepository>(),
                downloader = koin.get<QuranAudioDownloader>()
            )
        } else {
            null
        }
    }
}
