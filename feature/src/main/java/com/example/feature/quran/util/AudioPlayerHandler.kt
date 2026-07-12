package com.example.feature.quran.util

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.feature.quran.service.QuranAudioService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AudioPlayerHandler(context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var listenerAttached = false

    private val controllerFuture: ListenableFuture<MediaController> = MediaController.Builder(
        context,
        SessionToken(context, ComponentName(context, QuranAudioService::class.java))
    ).buildAsync()

    @Volatile
    private var controller: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _ayahCompleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val ayahCompleted: SharedFlow<Unit> = _ayahCompleted.asSharedFlow()

    private val _playbackError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val playbackError: SharedFlow<String> = _playbackError.asSharedFlow()

    init {
        controllerFuture.addListener({
            runCatching {
                val mediaController = controllerFuture.get()
                controller = mediaController
                setupController(mediaController)
            }.onFailure {
                _playbackError.tryEmit("تعذر الاتصال بمُشغّل التلاوة")
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupController(player: MediaController) {
        if (listenerAttached) return
        listenerAttached = true
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) startProgressUpdate() else stopProgressUpdate()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> _duration.value = player.duration.coerceAtLeast(0L)
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                        _ayahCompleted.tryEmit(Unit)
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _isPlaying.value = false
                _playbackError.tryEmit("تعذر تشغيل التلاوة. تحقق من الاتصال بالإنترنت.")
            }
        })
    }

    suspend fun ensureReady(timeoutMs: Long = 8_000): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val mediaController = controllerFuture.get(timeoutMs, TimeUnit.MILLISECONDS)
            withContext(Dispatchers.Main) {
                controller = mediaController
                setupController(mediaController)
            }
            true
        }.getOrElse {
            _playbackError.tryEmit("مُشغّل التلاوة غير جاهز")
            false
        }
    }

    fun hasActiveMedia(): Boolean {
        val player = controller ?: return false
        return player.mediaItemCount > 0 &&
            player.playbackState != Player.STATE_IDLE &&
            player.playbackState != Player.STATE_ENDED
    }

    suspend fun playAyah(url: String): Boolean {
        if (!ensureReady()) return false
        return withContext(Dispatchers.Main) {
            val player = controller ?: return@withContext false
            runCatching {
                player.setMediaItem(MediaItem.fromUri(url))
                player.prepare()
                player.playWhenReady = true
                player.play()
                true
            }.getOrElse {
                _playbackError.tryEmit("تعذر تشغيل الآية")
                false
            }
        }
    }

    fun togglePlay() {
        val player = controller ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    fun pause() {
        controller?.pause()
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                controller?.let {
                    _currentPosition.value = it.currentPosition
                    if (it.duration > 0) _duration.value = it.duration
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }
}
