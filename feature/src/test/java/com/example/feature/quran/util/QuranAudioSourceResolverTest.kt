package com.example.feature.quran.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuranAudioSourceResolverTest {

    @Test
    fun `remote url pads surah and ayah`() {
        assertEquals(
            "https://everyayah.com/data/Alafasy_128kbps/112001.mp3",
            QuranAudioSourceResolver.remoteUrl("Alafasy_128kbps", 112, 1)
        )
        assertEquals(
            "https://everyayah.com/data/Alafasy_128kbps/001001.mp3",
            QuranAudioSourceResolver.remoteUrl("Alafasy_128kbps", 1, 1)
        )
    }

    @Test
    fun `usable local requires non empty readable file`() {
        val empty = File.createTempFile("ayah", ".mp3")
        empty.writeBytes(ByteArray(0))
        assertFalse(QuranAudioSourceResolver.isUsableLocalFile(empty.absolutePath))

        val valid = File.createTempFile("ayah_ok", ".mp3")
        valid.writeBytes(byteArrayOf(1, 2, 3))
        assertTrue(QuranAudioSourceResolver.isUsableLocalFile(valid.absolutePath))

        assertFalse(QuranAudioSourceResolver.isUsableLocalFile(null))
        assertFalse(QuranAudioSourceResolver.isUsableLocalFile("C:/missing/ayah.mp3"))
        empty.delete()
        valid.delete()
    }

    @Test
    fun `selectInitialSource prefers valid local then remote`() {
        val remote = "https://everyayah.com/data/Alafasy_128kbps/112001.mp3"
        val valid = File.createTempFile("ayah_ok", ".mp3")
        valid.writeBytes(byteArrayOf(9))
        assertEquals(
            valid.absolutePath,
            QuranAudioSourceResolver.selectInitialSource(valid.absolutePath, remote)
        )
        assertEquals(
            remote,
            QuranAudioSourceResolver.selectInitialSource(null, remote)
        )
        valid.delete()
    }

    @Test
    fun `offline playback uses local audio but refuses remote fallback`() {
        val valid = File.createTempFile("ayah_offline", ".mp3")
        valid.writeBytes(byteArrayOf(9))
        val remote = "https://everyayah.com/data/Alafasy_128kbps/112001.mp3"

        assertEquals(
            valid.absolutePath,
            QuranAudioSourceResolver.selectInitialSourceOrNull(valid.absolutePath, remote, false)
        )
        assertEquals(
            null,
            QuranAudioSourceResolver.selectInitialSourceOrNull(null, remote, false)
        )

        valid.delete()
    }

    @Test
    fun `isRemoteUrl detects http sources`() {
        assertTrue(QuranAudioSourceResolver.isRemoteUrl("https://everyayah.com/x.mp3"))
        assertFalse(QuranAudioSourceResolver.isRemoteUrl("/data/user/0/x.mp3"))
    }
}
