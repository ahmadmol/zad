package com.example.feature.quran.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.feature.quran.domain.repository.QuranRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.net.URL

class QuranDownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val repository: QuranRepository by inject()

    override suspend fun doWork(): Result {
        val surahId = inputData.getInt("surah_id", -1)
        val readerId = inputData.getString("reader_id") ?: "Alafasy_128kbps"
        
        if (surahId == -1) return Result.failure()

        val ayahs = repository.getAyahsBySurah(surahId)
        val total = ayahs.size
        
        var downloadedCount = 0

        ayahs.forEachIndexed { index, verse ->
            val surahStr = surahId.toString().padStart(3, '0')
            val ayahStr = verse.verseNumber.toString().padStart(3, '0')
            val urlString = "https://everyayah.com/data/$readerId/$surahStr$ayahStr.mp3"
            
            try {
                val fileName = "${readerId}_${surahStr}_${ayahStr}.mp3"
                val file = File(applicationContext.filesDir, "quran_audio/$fileName")
                
                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                
                if (!file.exists()) {
                    URL(urlString).openStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                
                repository.saveDownloadedAyah(surahId, verse.verseNumber, readerId, file.absolutePath)
                downloadedCount++
                setProgress(workDataOf("progress" to (downloadedCount * 100 / total)))
            } catch (e: Exception) {
                // Log error but continue with other ayahs
            }
        }

        return Result.success()
    }
}
