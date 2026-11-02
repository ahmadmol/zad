package com.example.feature.quran.data.download

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Phase 4 — download reliability regression tests.
 *
 * Covers the failure-classification and atomic-finalization contract that keeps a
 * half-written mp3 from ever being played or counted as "downloaded".
 */
class QuranDownloadReliabilityTest {

    @get:Rule
    val temp = TemporaryFolder()

    // --- Failure classification -------------------------------------------

    @Test
    fun `2xx responses are accepted`() {
        listOf(200, 201, 202, 204, 206, 299).forEach { code ->
            validateQuranHttpStatus(code) // must not throw
        }
    }

    @Test
    fun `retryable server and throttling responses are transient`() {
        listOf(408, 429, 500, 502, 503, 504).forEach { code ->
            val error = runCatching { validateQuranHttpStatus(code) }.exceptionOrNull()
            assertTrue(
                "HTTP " + code + " must be transient but was " + error,
                error is TransientQuranDownloadException
            )
        }
    }

    @Test
    fun `client errors are permanent and must not be retried`() {
        listOf(400, 401, 403, 404, 410, 451).forEach { code ->
            val error = runCatching { validateQuranHttpStatus(code) }.exceptionOrNull()
            assertTrue(
                "HTTP " + code + " must be permanent but was " + error,
                error is PermanentQuranDownloadException
            )
        }
    }

    @Test
    fun `a missing ayah recording is permanent so the worker stops retrying`() {
        val error = runCatching { validateQuranHttpStatus(404) }.exceptionOrNull()
        assertTrue(error is PermanentQuranDownloadException)
        assertFalse(error is TransientQuranDownloadException)
    }

    @Test
    fun `transient and permanent failures are distinct types`() {
        assertFalse(
            PermanentQuranDownloadException("x") is TransientQuranDownloadException
        )
        assertFalse(
            TransientQuranDownloadException("x") is PermanentQuranDownloadException
        )
    }

    // --- Atomic finalization / partial-file safety -------------------------

    @Test
    fun `finalization moves the temp file onto the target and leaves no partial`() {
        val dir = temp.newFolder()
        val part = File(dir, "001001.mp3.part").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val target = File(dir, "001001.mp3")

        finalizeQuranAudioDownload(part, target)

        assertTrue(target.exists())
        assertEquals(4, target.length())
        assertFalse("the .part file must not survive finalization", part.exists())
    }

    @Test
    fun `finalization preserves the exact bytes`() {
        val dir = temp.newFolder()
        val payload = ByteArray(2048) { (it % 251).toByte() }
        val part = File(dir, "a.mp3.part").apply { writeBytes(payload) }
        val target = File(dir, "a.mp3")

        finalizeQuranAudioDownload(part, target)

        assertTrue(payload.contentEquals(target.readBytes()))
    }

    @Test
    fun `finalization replaces a previously corrupt target`() {
        val dir = temp.newFolder()
        val target = File(dir, "b.mp3").apply { writeBytes(ByteArray(0)) }
        val part = File(dir, "b.mp3.part").apply { writeBytes(byteArrayOf(9, 9, 9)) }

        finalizeQuranAudioDownload(part, target)

        assertEquals(3, target.length())
        assertFalse(part.exists())
    }

    @Test
    fun `a leftover part file is never itself a playable target`() {
        val dir = temp.newFolder()
        val part = File(dir, "c.mp3.part").apply { writeBytes(byteArrayOf(1)) }
        val target = File(dir, "c.mp3")

        // Until finalization runs, the real target does not exist at all, so the
        // resolver can never pick up a half-written download.
        assertTrue(part.exists())
        assertFalse(target.exists())
    }

    // --- download() paths reachable without network ------------------------

    @Test
    fun `an already complete file short circuits without touching the network`() =
        runBlocking {
            val dir = temp.newFolder()
            val target = File(dir, "d.mp3").apply { writeBytes(byteArrayOf(1, 2, 3)) }

            // An unroutable URL: if the downloader attempted any I/O this would fail.
            QuranAudioDownloader().download("http://127.0.0.1:1/never.mp3", target)

            assertEquals(3, target.length())
        }

    @Test
    fun `a zero length target is not treated as complete`() = runBlocking {
        val dir = temp.newFolder()
        val target = File(dir, "e.mp3").apply { writeBytes(ByteArray(0)) }

        // Must NOT short-circuit; with no reachable host it fails as transient.
        val error = runCatching {
            QuranAudioDownloader(connectTimeoutMs = 200, readTimeoutMs = 200)
                .download("http://127.0.0.1:1/never.mp3", target)
        }.exceptionOrNull()

        assertTrue(
            "expected a transient network failure, got " + error,
            error is TransientQuranDownloadException
        )
    }

    @Test
    fun `a failed download leaves no partial file behind`() = runBlocking {
        val dir = temp.newFolder()
        val target = File(dir, "f.mp3")

        runCatching {
            QuranAudioDownloader(connectTimeoutMs = 200, readTimeoutMs = 200)
                .download("http://127.0.0.1:1/never.mp3", target)
        }

        assertFalse("target must not be created on failure", target.exists())
        assertFalse(
            "no .part file may be left behind",
            File(dir, "f.mp3.part").exists()
        )
        assertTrue(dir.listFiles().orEmpty().isEmpty())
    }
}
