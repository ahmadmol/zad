package com.example.feature.ehsan.data.image

import java.io.InputStream
import java.io.OutputStream
import java.io.IOException

/** Bounds untrusted providers even when their declared size is missing or incorrect. */
internal object ImageImportPolicy {
    const val MAX_BYTES = 8L * 1024 * 1024
    const val MAX_PIXELS = 40_000_000L
    fun extension(mime: String?): String? = when (mime?.lowercase()) {
        "image/jpeg" -> ".jpg"
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        "image/gif" -> ".gif"
        else -> null
    }
    fun copy(input: InputStream, output: OutputStream, limit: Long = MAX_BYTES): Long {
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (count == 0) continue
            total += count
            if (total > limit) throw IOException("Image exceeds local size limit")
            output.write(buffer, 0, count)
        }
        if (total == 0L) throw IOException("Empty image")
        return total
    }
    fun validDimensions(width: Int, height: Int) =
        width > 0 && height > 0 && width.toLong() * height <= MAX_PIXELS
}
