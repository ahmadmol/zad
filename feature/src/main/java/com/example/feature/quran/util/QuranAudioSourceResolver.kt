package com.example.feature.quran.util

import java.io.File

/**
 * Resolves playable Quran ayah audio sources without network I/O.
 * Local files must exist, be readable, and have non-zero size.
 */
object QuranAudioSourceResolver {

    fun remoteUrl(readerId: String, surahId: Int, ayahNumber: Int): String {
        val surahStr = surahId.toString().padStart(3, '0')
        val ayahStr = ayahNumber.toString().padStart(3, '0')
        return "https://everyayah.com/data/$readerId/$surahStr$ayahStr.mp3"
    }

    fun isUsableLocalFile(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        val file = File(path)
        return file.exists() && file.canRead() && file.length() > 0L
    }

    fun selectInitialSource(localPath: String?, remoteUrl: String): String {
        return if (isUsableLocalFile(localPath)) requireNotNull(localPath) else remoteUrl
    }

    fun selectInitialSourceOrNull(
        localPath: String?,
        remoteUrl: String,
        isOnline: Boolean
    ): String? {
        if (isUsableLocalFile(localPath)) return localPath
        return remoteUrl.takeIf { isOnline }
    }

    fun isRemoteUrl(source: String): Boolean = source.startsWith("http", ignoreCase = true)
}
