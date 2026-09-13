package com.example.feature.quran.data.download

import androidx.work.WorkInfo
import com.example.feature.quran.domain.usecase.QuranDownloadStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class QuranDownloadProgressMappingTest {

    @Test
    fun `running work progress is mapped and clamped`() {
        val progress = mapQuranDownloadProgress(
            tag = "download",
            state = WorkInfo.State.RUNNING,
            percent = 142,
            errorMessage = null
        )

        assertEquals(QuranDownloadStatus.Running, progress.status)
        assertEquals(100, progress.percent)
    }

    @Test
    fun `failed work exposes output error`() {
        val progress = mapQuranDownloadProgress(
            tag = "download",
            state = WorkInfo.State.FAILED,
            percent = 80,
            errorMessage = "HTTP 404"
        )

        assertEquals(QuranDownloadStatus.Failed, progress.status)
        assertEquals("HTTP 404", progress.errorMessage)
    }
}
