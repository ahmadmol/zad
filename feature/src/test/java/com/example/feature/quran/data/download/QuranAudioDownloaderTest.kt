package com.example.feature.quran.data.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class QuranAudioDownloaderTest {

    @Test
    fun `transient and permanent http failures are classified separately`() {
        assertThrows(TransientQuranDownloadException::class.java) {
            validateQuranHttpStatus(503)
        }
        assertThrows(PermanentQuranDownloadException::class.java) {
            validateQuranHttpStatus(404)
        }
    }

    @Test
    fun `successful temporary file replaces target and removes partial file`() {
        val target = File.createTempFile("quran_target", ".mp3")
        val temporary = File(target.parentFile, "${target.name}.part")
        target.writeBytes(byteArrayOf(1))
        temporary.writeBytes(byteArrayOf(2, 3, 4))

        finalizeQuranAudioDownload(temporary, target)

        assertEquals(byteArrayOf(2, 3, 4).toList(), target.readBytes().toList())
        assertFalse(temporary.exists())
        target.delete()
    }
}
