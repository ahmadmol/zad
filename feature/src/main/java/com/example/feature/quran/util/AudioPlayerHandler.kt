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
import com.example.feature.quran.domain.model.RecitationPosition
import com.example.feature.quran.domain.repository.QuranRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AudioPlayerHandler(context: Context, private val repository: QuranRepository? = null) {

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
    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()
    private val _currentRecitation = MutableStateFlow<RecitationPosition?>(null)
    val currentRecitation = _currentRecitation.asStateFlow()
    private val transition = Mutex()

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
                _isBuffering.value = playbackState == Player.STATE_BUFFERING && player.playWhenReady
                when (playbackState) {
                    Player.STATE_READY -> _duration.value = player.duration.coerceAtLeast(0L)
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                        _ayahCompleted.tryEmit(Unit)
                        val completed = _currentRecitation.value
                        scope.launch { if (completed != null && _currentRecitation.value == completed) playNext(completed) }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _isPlaying.value = false
                _isBuffering.value = false
                _playbackError.tryEmit("تعذر تشغيل التلاوة. تحقق من الاتصال بالإنترنت.")
            }
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                _isBuffering.value = playWhenReady && player.playbackState == Player.STATE_BUFFERING
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

    suspend fun playAyah(url: String, position: RecitationPosition? = null): Boolean {
        if (!ensureReady()) return false
        return withContext(Dispatchers.Main) {
            val player = controller ?: return@withContext false
            runCatching {
                _currentPosition.value = 0L
                _duration.value = 0L
                player.setMediaItem(MediaItem.fromUri(url))
                _currentRecitation.value = position
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

    suspend fun playPosition(position: RecitationPosition, expected: RecitationPosition? = null): Boolean = transition.withLock {
        if (!RecitationAvailability.RECORDINGS_APPROVED) {
            _playbackError.emit(RecitationAvailability.MESSAGE)
            return@withLock false
        }
        val repo = repository ?: return@withLock false
        val surah = repo.getSurahById(position.surahId) ?: return@withLock false
        if (position.verseNumber !in 1..surah.totalVerses) return@withLock false
        val local = repo.getLocalAyahPath(position.surahId, position.verseNumber, position.readerId)
        if (expected != null && _currentRecitation.value != expected) return@withLock false
        val url = QuranAudioSourceResolver.selectInitialSource(local,
            QuranAudioSourceResolver.remoteUrl(position.readerId, position.surahId, position.verseNumber))
        val accepted = playAyah(url, position)
        if (accepted) repo.saveLastRead(position.surahId, position.verseNumber)
        accepted
    }

    suspend fun playNext(from: RecitationPosition? = _currentRecitation.value): Boolean {
        val current = from ?: return false
        val startingIdentity = _currentRecitation.value
        if (startingIdentity != null && startingIdentity != current) return false
        val total = repository?.getSurahById(current.surahId)?.totalVerses ?: return false
        val next = when {
            current.verseNumber < total -> current.copy(verseNumber = current.verseNumber + 1)
            current.surahId < 114 -> current.copy(surahId = current.surahId + 1, verseNumber = 1)
            else -> { pause(); return false }
        }
        return playPosition(next, expected = startingIdentity)
    }

    suspend fun playPrevious(from: RecitationPosition? = _currentRecitation.value): Boolean {
        val current = from ?: return false
        val previous = when {
            current.verseNumber > 1 -> current.copy(verseNumber = current.verseNumber - 1)
            current.surahId > 1 -> current.copy(surahId = current.surahId - 1,
                verseNumber = repository?.getSurahById(current.surahId - 1)?.totalVerses ?: return false)
            else -> return false
        }
        return playPosition(previous)
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
