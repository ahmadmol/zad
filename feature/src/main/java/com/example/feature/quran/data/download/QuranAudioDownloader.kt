package com.example.feature.quran.data.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

class TransientQuranDownloadException(message: String, cause: Throwable? = null) : IOException(message, cause)

class PermanentQuranDownloadException(message: String) : IOException(message)

class QuranAudioDownloader(
    private val connectTimeoutMs: Int = 15_000,
    private val readTimeoutMs: Int = 30_000
) {
    suspend fun download(url: String, target: File) = withContext(Dispatchers.IO) {
        if (target.exists() && target.isFile && target.length() > 0L) return@withContext

        val parent = target.parentFile ?: throw PermanentQuranDownloadException("Missing audio directory")
        if (!parent.exists() && !parent.mkdirs()) {
            throw PermanentQuranDownloadException("Unable to create audio directory")
        }

        val temporary = File(parent, "${target.name}.part")
        temporary.delete()
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                requestMethod = "GET"
                instanceFollowRedirects = true
                useCaches = false
                setRequestProperty("Accept", "audio/mpeg")
            }

            val responseCode = connection.responseCode
            validateQuranHttpStatus(responseCode)

            connection.inputStream.use { input ->
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                    }
                }
            }

            if (!temporary.exists() || temporary.length() == 0L) {
                throw TransientQuranDownloadException("Empty audio response")
            }
            finalizeQuranAudioDownload(temporary, target)
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            temporary.delete()
            throw cancelled
        } catch (error: TransientQuranDownloadException) {
            temporary.delete()
            throw error
        } catch (error: PermanentQuranDownloadException) {
            temporary.delete()
            throw error
        } catch (error: IOException) {
            temporary.delete()
            throw TransientQuranDownloadException("Network I/O failed", error)
        } finally {
            connection?.disconnect()
        }
    }
}

internal fun validateQuranHttpStatus(responseCode: Int) {
    when {
        responseCode in 200..299 -> Unit
        responseCode == 408 || responseCode == 429 || responseCode >= 500 -> {
            throw TransientQuranDownloadException("HTTP $responseCode")
        }
        else -> throw PermanentQuranDownloadException("HTTP $responseCode")
    }
}

internal fun finalizeQuranAudioDownload(temporary: File, target: File) {
    if (target.exists() && !target.delete()) {
        throw IOException("Unable to replace existing audio file")
    }
    if (temporary.renameTo(target)) return

    try {
        temporary.copyTo(target, overwrite = true)
        if (!temporary.delete()) temporary.deleteOnExit()
    } catch (error: IOException) {
        target.delete()
        throw error
    }
}
