package com.example.feature.quran.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranAudioPhaseTest {

    @Test
    fun `verse playing requires player isPlaying not only current ayah`() {
        val connecting = QuranUiState(
            currentPlayingAyah = 1,
            isPlaying = false,
            audioPhase = QuranAudioPhase.Connecting
        )
        assertFalse(connecting.isPlaying && connecting.currentPlayingAyah == 1)

        val playing = connecting.copy(
            isPlaying = true,
            audioPhase = QuranAudioPhase.Playing
        )
        assertTrue(playing.isPlaying && playing.currentPlayingAyah == 1)
    }

    @Test
    fun `error phase clears playing claim`() {
        val error = QuranUiState(
            currentPlayingAyah = null,
            isPlaying = false,
            audioPhase = QuranAudioPhase.Error,
            errorMessage = "تعذر تشغيل التلاوة. تحقق من الاتصال وحاول مرة أخرى."
        )
        assertEquals(QuranAudioPhase.Error, error.audioPhase)
        assertFalse(error.isPlaying)
    }
}
