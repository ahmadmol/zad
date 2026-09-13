package com.example.feature.quran.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.preferences.DailyActivityIds
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.quran.domain.model.Reader
import com.example.feature.quran.domain.repository.QuranRepository
import com.example.feature.quran.domain.usecase.QuranDownloadScheduler
import com.example.feature.quran.util.AudioPlayerHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuranViewModel(
    private val repository: QuranRepository,
    private val audioHandler: AudioPlayerHandler,
    private val downloadScheduler: QuranDownloadScheduler,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var downloadedAyahsJob: Job? = null
    private var downloadStatusJob: Job? = null

    private val defaultReaders = listOf(
        Reader("Alafasy_128kbps", "مشاري العفاسي"),
        Reader("Abdul_Basit_Murattal_192kbps", "عبد الباسط عبد الصمد"),
        Reader("MaherAlMuaiqly128kbps", "ماهر المعيقلي")
    )

    init {
        _uiState.update {
            it.copy(availableReaders = defaultReaders, selectedReader = defaultReaders.first())
        }
        onAction(QuranAction.LoadSurahs)
        onAction(QuranAction.LoadBookmarks)
        onAction(QuranAction.LoadKhatmaProgress)
        observeLastRead()
        observeAudioState()
        observePlaybackCompletion()
    }

    private fun observeAudioState() {
        audioHandler.isPlaying.onEach { isPlaying ->
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }.launchIn(viewModelScope)

        audioHandler.currentPosition.onEach { pos ->
            _uiState.update { it.copy(playbackPosition = pos) }
        }.launchIn(viewModelScope)

        audioHandler.duration.onEach { dur ->
            _uiState.update { it.copy(playbackDuration = dur) }
        }.launchIn(viewModelScope)

        audioHandler.playbackError.onEach { message ->
            _uiState.update { it.copy(errorMessage = message, isPlaying = false) }
        }.launchIn(viewModelScope)
    }

    private fun observePlaybackCompletion() {
        audioHandler.ayahCompleted.onEach {
            playNext()
        }.launchIn(viewModelScope)
    }

    fun onAction(action: QuranAction) {
        when (action) {
            is QuranAction.LoadSurahs -> loadSurahs()
            is QuranAction.LoadBookmarks -> loadBookmarks()
            is QuranAction.LoadKhatmaProgress -> loadKhatmaProgress()
            is QuranAction.SelectSurah -> loadSurahDetails(action.surahId, action.ayahNumber)
            is QuranAction.SaveLastRead -> saveLastRead(action.surahId, action.ayahNumber)
            is QuranAction.ToggleBookmark -> toggleBookmark(action.surahId, action.verseNumber)
            is QuranAction.Search -> search(action.query)
            is QuranAction.ClearSearch -> {
                searchJob?.cancel()
                _uiState.update {
                    it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false)
                }
            }
            is QuranAction.TogglePlay -> togglePlay()
            is QuranAction.PlayAyah -> playAyah(action.ayahNumber)
            is QuranAction.PlayNext -> playNext()
            is QuranAction.PlayPrevious -> playPrevious()
            is QuranAction.SeekTo -> audioHandler.seekTo(action.position)
            is QuranAction.SelectReader -> {
                _uiState.update { it.copy(selectedReader = action.reader) }
                _uiState.value.selectedSurah?.id?.let {
                    observeDownloads(it)
                    observeDownloadWork(it)
                }
                val current = _uiState.value.currentPlayingAyah
                if (_uiState.value.isPlaying && current != null) {
                    playAyah(current)
                }
            }
            is QuranAction.UpdateFontSize -> _uiState.update { it.copy(fontSize = action.size) }
            QuranAction.DownloadSurah -> downloadSurah()
            is QuranAction.Retry -> {
                if (_uiState.value.selectedSurah != null) {
                    loadSurahDetails(_uiState.value.selectedSurah!!.id)
                } else {
                    loadSurahs()
                }
            }
            is QuranAction.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Keep the app-scoped audio handler alive for background playback.
        audioHandler.pause()
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.observeAllSurahs()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { surahs ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            surahs = surahs,
                            surahReadingProgress = buildSurahProgressMap(
                                surahs = surahs,
                                lastRead = state.lastRead
                            )
                        )
                    }
                }
        }
    }

    private fun loadBookmarks() {
        repository.observeAllBookmarks()
            .onEach { bookmarks ->
                _uiState.update { it.copy(bookmarks = bookmarks) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadKhatmaProgress() {
        viewModelScope.launch {
            try {
                val progress = repository.getKhatmaProgress()
                _uiState.update { it.copy(khatmaProgress = progress) }
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    private fun observeLastRead() {
        repository.observeLastRead()
            .onEach { lastReadPair ->
                if (lastReadPair != null) {
                    val (surahId, ayahNumber) = lastReadPair
                    val surah = repository.getSurahById(surahId)
                    if (surah != null) {
                        _uiState.update { state ->
                            val lastRead = surah to ayahNumber
                            state.copy(
                                lastRead = lastRead,
                                surahReadingProgress = buildSurahProgressMap(state.surahs, lastRead)
                            )
                        }
                        loadKhatmaProgress()
                    }
                } else {
                    _uiState.update {
                        it.copy(lastRead = null, surahReadingProgress = emptyMap())
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun saveLastRead(surahId: Int, ayahNumber: Int) {
        viewModelScope.launch {
            repository.saveLastRead(surahId, ayahNumber)
            userPreferences.markDailyActivityComplete(DailyActivityIds.QURAN_READING)
        }
    }

    private fun toggleBookmark(surahId: Int, verseNumber: Int) {
        viewModelScope.launch {
            repository.toggleBookmark(surahId, verseNumber)
        }
    }

    private fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = true) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update {
                it.copy(searchResults = emptyList(), isSearching = false)
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = repository.searchAyahs(query)
                _uiState.update {
                    it.copy(isLoading = false, searchResults = results)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun togglePlay() {
        viewModelScope.launch {
            if (_uiState.value.isPlaying) {
                audioHandler.togglePlay()
                return@launch
            }

            if (audioHandler.hasActiveMedia() && _uiState.value.currentPlayingAyah != null) {
                audioHandler.togglePlay()
                return@launch
            }

            val startAyah = _uiState.value.currentPlayingAyah
                ?: _uiState.value.initialAyahScroll
                ?: 1
            playAyah(startAyah)
        }
    }

    private fun playNext() {
        val currentAyah = _uiState.value.currentPlayingAyah ?: return
        val maxAyah = _uiState.value.ayahs.size
        if (currentAyah < maxAyah) {
            playAyah(currentAyah + 1)
        } else {
            _uiState.update { it.copy(isPlaying = false) }
        }
    }

    private fun playPrevious() {
        val currentAyah = _uiState.value.currentPlayingAyah ?: 1
        if (currentAyah > 1) {
            playAyah(currentAyah - 1)
        } else {
            playAyah(1)
        }
    }

    private fun downloadSurah() {
        val surahId = _uiState.value.selectedSurah?.id ?: return
        val readerId = _uiState.value.selectedReader?.id ?: "Alafasy_128kbps"
        val tag = downloadScheduler.enqueue(surahId, readerId)
        observeDownloadWork(tag)
    }

    private fun loadSurahDetails(surahId: Int, ayahNumber: Int? = null) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    initialAyahScroll = ayahNumber,
                    currentPlayingAyah = ayahNumber
                )
            }
            try {
                val surah = repository.getSurahById(surahId)
                if (surah != null) {
                    val ayahs = repository.getAyahsBySurah(surahId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedSurah = surah,
                            ayahs = ayahs
                        )
                    }
                    observeDownloads(surahId)
                    observeDownloadWork(surahId)
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "السورة غير موجودة")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun observeDownloads(surahId: Int) {
        downloadedAyahsJob?.cancel()
        val readerId = _uiState.value.selectedReader?.id ?: "Alafasy_128kbps"
        downloadedAyahsJob = repository.observeDownloadedAyahs(surahId, readerId)
            .onEach { downloaded ->
                _uiState.update { it.copy(downloadedAyahs = downloaded) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeDownloadWork(surahId: Int) {
        val readerId = _uiState.value.selectedReader?.id ?: "Alafasy_128kbps"
        observeDownloadWork(downloadScheduler.identity(surahId, readerId))
    }

    private fun observeDownloadWork(tag: String) {
        downloadStatusJob?.cancel()
        downloadStatusJob = downloadScheduler.observe(tag)
            .onEach { progress ->
                _uiState.update {
                    it.copy(
                        downloadStatus = progress.status,
                        downloadProgress = progress.percent,
                        downloadErrorMessage = progress.errorMessage
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun playAyah(ayahNumber: Int) {
        viewModelScope.launch {
            val surahId = _uiState.value.selectedSurah?.id ?: return@launch
            val readerId = _uiState.value.selectedReader?.id ?: "Alafasy_128kbps"
            val maxAyah = _uiState.value.ayahs.size
            if (ayahNumber !in 1..maxAyah && maxAyah > 0) return@launch

            _uiState.update {
                it.copy(
                    currentPlayingAyah = ayahNumber,
                    errorMessage = null,
                    playbackPosition = 0L
                )
            }
            // Track surah reading from audio position as well as scroll
            saveLastRead(surahId, ayahNumber)

            val localPath = repository.getLocalAyahPath(surahId, ayahNumber, readerId)
            val source = if (localPath != null && java.io.File(localPath).exists()) {
                localPath
            } else {
                val surahStr = surahId.toString().padStart(3, '0')
                val ayahStr = ayahNumber.toString().padStart(3, '0')
                "https://everyayah.com/data/$readerId/$surahStr$ayahStr.mp3"
            }

            audioHandler.playAyah(source)
        }
    }

    private fun buildSurahProgressMap(
        surahs: List<com.example.feature.quran.domain.model.Surah>,
        lastRead: Pair<com.example.feature.quran.domain.model.Surah, Int>?
    ): Map<Int, Float> {
        val (lastSurah, lastAyah) = lastRead ?: return emptyMap()
        if (lastSurah.totalVerses <= 0) return emptyMap()
        val progress = (lastAyah.toFloat() / lastSurah.totalVerses.toFloat()).coerceIn(0f, 1f)
        return mapOf(lastSurah.id to progress)
    }
}
