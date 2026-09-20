package com.example.feature.quran.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Characterizes the current Quran audio state, which is represented directly by player fields. */
class QuranAudioPhaseTest {

    @Test
    fun `current ayah alone does not claim active playback`() {
        val paused = QuranUiState(currentPlayingAyah = 1, isPlaying = false)

        assertFalse(paused.isPlaying)
        assertEquals(1, paused.currentPlayingAyah)

        val playing = paused.copy(isPlaying = true)
        assertTrue(playing.isPlaying)
        assertEquals(1, playing.currentPlayingAyah)
    }

    @Test
    fun `playback error can clear active media while retaining its message`() {
        val error = QuranUiState(
            currentPlayingAyah = null,
            isPlaying = false,
            errorMessage = "تعذر تشغيل التلاوة. تحقق من الاتصال وحاول مرة أخرى."
        )

        assertNull(error.currentPlayingAyah)
        assertFalse(error.isPlaying)
        assertTrue(error.errorMessage.orEmpty().contains("تعذر تشغيل التلاوة"))
    }
}
