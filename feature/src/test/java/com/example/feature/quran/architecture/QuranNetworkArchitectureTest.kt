package com.example.feature.quran.architecture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuranNetworkArchitectureTest {

    private val repoRoot: File = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }
    }

    @Test
    fun `worker uses injected downloader and explicit retry outcomes`() {
        val worker = read("feature/src/main/java/com/example/feature/quran/worker/QuranDownloadWorker.kt")

        assertFalse(worker.contains("KoinComponent"))
        assertFalse(worker.contains("openStream"))
        assertTrue(worker.contains("Result.retry()"))
        assertTrue(worker.contains("Result.failure("))
    }

    @Test
    fun `scheduler encodes connected and unmetered policies`() {
        val scheduler = read(
            "feature/src/main/java/com/example/feature/quran/data/download/WorkManagerQuranDownloadScheduler.kt"
        )

        assertTrue(scheduler.contains("NetworkType.CONNECTED"))
        assertTrue(scheduler.contains("NetworkType.UNMETERED"))
        assertTrue(scheduler.contains("BackoffPolicy.EXPONENTIAL"))
    }

    @Test
    fun `viewmodel does not own android context or workmanager`() {
        val viewModel = read("feature/src/main/java/com/example/feature/quran/presentation/QuranViewModel.kt")

        assertFalse(viewModel.contains("android.content.Context"))
        assertFalse(viewModel.contains("WorkManager"))
        assertFalse(viewModel.contains("OneTimeWorkRequest"))
    }

    private fun read(relativePath: String): String = File(repoRoot, relativePath).readText()
}
