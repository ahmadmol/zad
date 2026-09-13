package com.example.feature.quran.domain.usecase

import kotlinx.coroutines.flow.Flow

enum class QuranDownloadNetworkPolicy {
    Connected,
    Unmetered
}

enum class QuranDownloadStatus {
    Enqueued,
    Running,
    Succeeded,
    Failed,
    Cancelled,
    Unknown
}

data class QuranDownloadProgress(
    val tag: String,
    val status: QuranDownloadStatus,
    val percent: Int,
    val errorMessage: String? = null
)

interface QuranDownloadScheduler {
    fun identity(surahId: Int, readerId: String): String

    fun enqueue(
        surahId: Int,
        readerId: String,
        networkPolicy: QuranDownloadNetworkPolicy = QuranDownloadNetworkPolicy.Connected
    ): String

    fun observe(tag: String): Flow<QuranDownloadProgress>
}
