package com.example.feature.quran.service

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.feature.core.observability.LogcatAppLogger

class QuranAudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val logger = LogcatAppLogger()

    override fun onCreate() {
        super.onCreate()
        try {
            val player = ExoPlayer.Builder(this).build()
            mediaSession = MediaSession.Builder(this, player).build()
        } catch (t: Throwable) {
            logger.error("QuranAudioService", "session_init_failed", t)
            throw t
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
