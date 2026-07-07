package com.example.feature.quran.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.quran.domain.model.Reader
import com.example.feature.quran.domain.repository.QuranRepository
import com.example.feature.quran.util.AudioPlayerHandler
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.feature.quran.worker.QuranDownloadWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuranViewModel(
    private val repository: QuranRepository,
    private val audioHandler: AudioPlayerHandler,
    private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    private val defaultReaders = listOf(
        Reader("1", "مشاري العفاسي"),
        Reader("2", "عبد الباسط عبد الصمد"),
        Reader("3", "ماهر المعيقلي")
    )

    init {
        _uiState.update { it.copy(availableReaders = defaultReaders, selectedReader = defaultReaders.first()) }
        onAction(QuranAction.LoadSurahs)
        onAction(QuranAction.LoadBookmarks)
        onAction(QuranAction.LoadKhatmaProgress)
        observeLastRead()
        observeAudioState()
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
                _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false) }
            }
            is QuranAction.TogglePlay -> audioHandler.togglePlay()
            is QuranAction.PlayNext -> playNext()
            is QuranAction.PlayPrevious -> playPrevious()
            is QuranAction.SeekTo -> audioHandler.seekTo(action.position)
            is QuranAction.SelectReader -> _uiState.update { it.copy(selectedReader = action.reader) }
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
        audioHandler.release()
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.observeAllSurahs()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { surahs ->
                    _uiState.update { it.copy(isLoading = false, surahs = surahs) }
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
            } catch (e: Exception) {
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
                        _uiState.update { it.copy(lastRead = surah to ayahNumber) }
                        loadKhatmaProgress()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun saveLastRead(surahId: Int, ayahNumber: Int) {
        viewModelScope.launch {
            repository.saveLastRead(surahId, ayahNumber)
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
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = repository.searchAyahs(query)
                _uiState.update { it.copy(isLoading = false, searchResults = results) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun playNext() {
        val currentAyah = _uiState.value.currentPlayingAyah ?: 1
        val maxAyah = _uiState.value.ayahs.size
        if (currentAyah < maxAyah) {
            val nextAyah = currentAyah + 1
            _uiState.update { it.copy(currentPlayingAyah = nextAyah) }
            startAyahPlayback(nextAyah)
        }
    }

    private fun playPrevious() {
        val currentAyah = _uiState.value.currentPlayingAyah ?: 1
        if (currentAyah > 1) {
            val prevAyah = currentAyah - 1
            _uiState.update { it.copy(currentPlayingAyah = prevAyah) }
            startAyahPlayback(prevAyah)
        }
    }

    private fun downloadSurah() {
        val surahId = _uiState.value.selectedSurah?.id ?: return
        val readerId = _uiState.value.selectedReader?.id ?: "Alafasy_128kbps"
        
        val downloadRequest = OneTimeWorkRequestBuilder<QuranDownloadWorker>()
            .setInputData(workDataOf(
                "surah_id" to surahId,
                "reader_id" to readerId
            ))
            .addTag("download_surah_$surahId")
            .build()
        
        WorkManager.getInstance(context).enqueue(downloadRequest)
    }

    private fun loadSurahDetails(surahId: Int, ayahNumber: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, initialAyahScroll = ayahNumber) }
            try {
                val surah = repository.getSurahById(surahId)
                if (surah != null) {
                    val ayahs = repository.getAyahsBySurah(surahId)
                    _uiState.update { it.copy(isLoading = false, selectedSurah = surah, ayahs = ayahs) }
                    observeDownloads(surahId)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Surah not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private var downloadJob: Job? = null
    private fun observeDownloads(surahId: Int) {
        downloadJob?.cancel()
        val readerId = _uiState.value.selectedReader?.id ?: "Alafasy_128kbps"
        downloadJob = repository.observeDownloadedAyahs(surahId, readerId)
            .onEach { downloaded ->
                _uiState.update { it.copy(downloadedAyahs = downloaded) }
            }
            .launchIn(viewModelScope)
    }

    private fun startAyahPlayback(ayahNumber: Int) {
        viewModelScope.launch {
            val surahId = _uiState.value.selectedSurah?.id ?: return@launch
            val readerId = _uiState.value.selectedReader?.id ?: "Alafasy_128kbps"
            
            val localPath = repository.getLocalAyahPath(surahId, ayahNumber, readerId)
            if (localPath != null && java.io.File(localPath).exists()) {
                audioHandler.playAyah(localPath)
            } else {
                // Format: https://everyayah.com/data/Alafasy_128kbps/001001.mp3
                val surahStr = surahId.toString().padStart(3, '0')
                val ayahStr = ayahNumber.toString().padStart(3, '0')
                val url = "https://everyayah.com/data/$readerId/$surahStr$ayahStr.mp3"
                audioHandler.playAyah(url)
            }
        }
    }
}
