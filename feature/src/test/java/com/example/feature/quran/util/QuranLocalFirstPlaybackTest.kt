package com.example.feature.quran.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Phase 4 — local-first playback regression tests.
 *
 * The product rule (Section 1) is that Quran audio opens from the device whenever a
 * usable local file exists, and that being offline never silently produces a remote
 * request. These tests pin that behaviour, including the corrupt-file cases.
 */
class QuranLocalFirstPlaybackTest {

    @get:Rule
    val temp = TemporaryFolder()

    private val remote = QuranAudioSourceResolver.remoteUrl("Alafasy_128kbps", 2, 255)

    private fun localFile(bytes: Int): String =
        temp.newFile().apply { writeBytes(ByteArray(bytes) { 7 }) }.absolutePath

    // --- Local preferred ---------------------------------------------------

    @Test
    fun `a usable local file wins over the remote url even when online`() {
        val local = localFile(128)
        assertEquals(
            local,
            QuranAudioSourceResolver.selectInitialSourceOrNull(local, remote, isOnline = true)
        )
    }

    @Test
    fun `offline with a local file still plays`() {
        val local = localFile(128)
        assertEquals(
            local,
            QuranAudioSourceResolver.selectInitialSourceOrNull(local, remote, isOnline = false)
        )
    }

    // --- Offline without local --------------------------------------------

    @Test
    fun `offline without a local file yields no source instead of a remote request`() {
        assertNull(
            QuranAudioSourceResolver.selectInitialSourceOrNull(null, remote, isOnline = false)
        )
    }

    @Test
    fun `online without a local file falls back to remote`() {
        assertEquals(
            remote,
            QuranAudioSourceResolver.selectInitialSourceOrNull(null, remote, isOnline = true)
        )
        assertTrue(QuranAudioSourceResolver.isRemoteUrl(remote))
    }

    // --- Corrupt / partial local files -------------------------------------

    @Test
    fun `a zero byte local file is rejected as corrupt`() {
        val empty = temp.newFile().absolutePath
        assertFalse(QuranAudioSourceResolver.isUsableLocalFile(empty))
        assertEquals(
            remote,
            QuranAudioSourceResolver.selectInitialSourceOrNull(empty, remote, isOnline = true)
        )
    }

    @Test
    fun `a corrupt local file offline yields no source rather than a broken play`() {
        val empty = temp.newFile().absolutePath
        assertNull(
            QuranAudioSourceResolver.selectInitialSourceOrNull(empty, remote, isOnline = false)
        )
    }

    @Test
    fun `a deleted local file falls back to remote when online`() {
        val path = temp.newFile().apply { writeBytes(byteArrayOf(1, 2)) }
        assertTrue(QuranAudioSourceResolver.isUsableLocalFile(path.absolutePath))
        path.delete()
        assertFalse(QuranAudioSourceResolver.isUsableLocalFile(path.absolutePath))
        assertEquals(
            remote,
            QuranAudioSourceResolver.selectInitialSourceOrNull(
                path.absolutePath,
                remote,
                isOnline = true
            )
        )
    }

    @Test
    fun `a directory is never a usable audio source`() {
        assertFalse(QuranAudioSourceResolver.isUsableLocalFile(temp.newFolder().absolutePath))
    }

    @Test
    fun `blank and null paths are rejected`() {
        assertFalse(QuranAudioSourceResolver.isUsableLocalFile(null))
        assertFalse(QuranAudioSourceResolver.isUsableLocalFile(""))
        assertFalse(QuranAudioSourceResolver.isUsableLocalFile("   "))
    }

    @Test
    fun `an unfinalized part file is not a usable source for its target`() {
        val dir = temp.newFolder()
        val target = File(dir, "002255.mp3")
        File(dir, "002255.mp3.part").writeBytes(ByteArray(64))

        // The .part file exists but the target does not — playback must go remote.
        assertFalse(QuranAudioSourceResolver.isUsableLocalFile(target.absolutePath))
        assertEquals(
            remote,
            QuranAudioSourceResolver.selectInitialSourceOrNull(
                target.absolutePath,
                remote,
                isOnline = true
            )
        )
    }

    // --- Remote URL shape ---------------------------------------------------

    @Test
    fun `remote url zero pads surah and ayah to three digits`() {
        assertTrue(
            QuranAudioSourceResolver.remoteUrl("R", 2, 255).endsWith("/002255.mp3")
        )
        assertTrue(QuranAudioSourceResolver.remoteUrl("R", 1, 1).endsWith("/001001.mp3"))
        assertTrue(QuranAudioSourceResolver.remoteUrl("R", 114, 6).endsWith("/114006.mp3"))
    }

    @Test
    fun `a local path is never mistaken for a remote url`() {
        assertFalse(QuranAudioSourceResolver.isRemoteUrl(localFile(16)))
    }
}
