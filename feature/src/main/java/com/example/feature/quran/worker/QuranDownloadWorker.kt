package com.example.feature.quran.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.feature.quran.data.download.PermanentQuranDownloadException
import com.example.feature.quran.data.download.QuranAudioDownloader
import com.example.feature.quran.data.download.TransientQuranDownloadException
import com.example.feature.quran.domain.repository.QuranRepository
import java.io.File

class QuranDownloadWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val repository: QuranRepository,
    private val downloader: QuranAudioDownloader
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val surahId = inputData.getInt("surah_id", -1)
        val readerId = inputData.getString("reader_id") ?: "Alafasy_128kbps"
        
        if (surahId == -1) {
            return Result.failure(workDataOf("error" to "Missing surah id"))
        }

        val ayahs = repository.getAyahsBySurah(surahId)
        val total = ayahs.size
        if (total == 0) {
            return Result.failure(workDataOf("error" to "Surah has no ayahs"))
        }

        var downloadedCount = 0

        ayahs.forEach { verse ->
            val surahStr = surahId.toString().padStart(3, '0')
            val ayahStr = verse.verseNumber.toString().padStart(3, '0')
            val urlString = "https://everyayah.com/data/$readerId/$surahStr$ayahStr.mp3"
            
            try {
                val fileName = "${readerId}_${surahStr}_${ayahStr}.mp3"
                val file = File(applicationContext.filesDir, "quran_audio/$fileName")
                
                downloader.download(urlString, file)
                repository.saveDownloadedAyah(surahId, verse.verseNumber, readerId, file.absolutePath)
                downloadedCount++
                setProgress(workDataOf("progress" to (downloadedCount * 100 / total)))
            } catch (error: TransientQuranDownloadException) {
                return Result.retry()
            } catch (error: PermanentQuranDownloadException) {
                return Result.failure(workDataOf("error" to (error.message ?: "Download failed")))
            }
        }

        return Result.success(workDataOf("progress" to 100))
    }
}
