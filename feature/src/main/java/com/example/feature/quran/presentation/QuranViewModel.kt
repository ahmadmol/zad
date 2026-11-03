package com.example.feature.quran.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.quran.domain.model.QuranTargetResolver
import com.example.feature.quran.domain.model.Reader
import com.example.feature.quran.domain.repository.QuranRepository
import com.example.feature.quran.domain.usecase.QuranDownloadScheduler
import com.example.feature.quran.util.AudioPlayerHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuranViewModel(
    private val repository: QuranRepository,
    private val audioHandler: AudioPlayerHandler,
    private val downloadScheduler: QuranDownloadScheduler,
    private val userPreferences: UserPreferences,
    private val savedState: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private val initialReadingMode: QuranReadingMode = savedState.get<String>("reader_mode")?.let {
        runCatching { QuranReadingMode.valueOf(it) }.getOrNull()
    } ?: QuranReadingMode.Mushaf

    private val _uiState = MutableStateFlow(
        QuranUiState(
            searchQuery = savedState.get<String>("quran_query").orEmpty(),
            readingMode = initialReadingMode,
            currentReadingSurahId = savedState.get<Int>("reader_active_surah"),
            currentReadingAyah = savedState.get<Int>("reader_active_ayah"),
            isTargetLocked = savedState.get<Boolean>("reader_target_locked") ?: false,
            activeRequestKey = savedState.get<String>("reader_request_key").orEmpty()
        )
    )
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    private var surahsJob: Job? = null
    private var bookmarksJob: Job? = null
    private var lastReadJob: Job? = null
    private var searchJob: Job? = null
    private var lastSavedPosition: Pair<Int, Int>? = null
    private var surahRequest: Pair<Int, Int?>? = null
    private var surahLoadJob: Job? = null
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
        userPreferences.quranFontSize.catch { error ->
            if (error is kotlinx.coroutines.CancellationException) throw error
            _uiState.update { it.copy(errorMessage = "تعذر قراءة إعدادات القرآن") }
        }.take(1).onEach { size -> _uiState.update { it.copy(fontSize = size) } }.launchIn(viewModelScope)
        userPreferences.quranReaderId.catch { error ->
            if (error is kotlinx.coroutines.CancellationException) throw error
            _uiState.update { it.copy(errorMessage = "تعذر قراءة إعدادات القرآن") }
        }.take(1).onEach { id ->
            defaultReaders.firstOrNull { it.id == id }?.let { reader -> _uiState.update { it.copy(selectedReader = reader) } }
        }.launchIn(viewModelScope)
        observeLastRead()
        observeAudioState()
        if (_uiState.value.searchQuery.isNotBlank()) search(_uiState.value.searchQuery)
    }

    private fun observeAudioState() {
        audioHandler.isBuffering.onEach { value -> _uiState.update { it.copy(isBuffering = value) } }.launchIn(viewModelScope)
        audioHandler.currentRecitation.onEach { position ->
            if (position != null) _uiState.update { it.copy(currentPlayingSurahId = position.surahId, currentPlayingAyah = position.verseNumber) }
        }.launchIn(viewModelScope)

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

    fun onAction(action: QuranAction) {
        when (action) {
            is QuranAction.LoadSurahs -> loadSurahs()
            is QuranAction.LoadBookmarks -> loadBookmarks()
            is QuranAction.LoadKhatmaProgress -> loadKhatmaProgress()
            is QuranAction.InitializeReaderTarget -> initializeReaderTarget(action.explicitSurahId, action.explicitAyahNumber)
            is QuranAction.SelectSurah -> loadSurahDetails(action.surahId, action.ayahNumber)
            is QuranAction.UpdateActivePosition -> updateActivePosition(action.surahId, action.ayahNumber)
            is QuranAction.SetReadingMode -> setReadingMode(action.mode)
            is QuranAction.SaveLastRead -> saveLastRead(action.surahId, action.ayahNumber)
            is QuranAction.ToggleBookmark -> toggleBookmark(action.surahId, action.verseNumber)
            is QuranAction.Search -> search(action.query)
            is QuranAction.ClearSearch -> {
                savedState["quran_query"] = ""
                searchJob?.cancel()
                _uiState.update {
                    it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false,
                        isLoading = false, errorMessage = null)
                }
            }
            is QuranAction.TogglePlay -> togglePlay()
            is QuranAction.PlayAyah -> playAyah(action.ayahNumber)
            is QuranAction.PlayNext -> playNext()
            is QuranAction.PlayPrevious -> playPrevious()
            is QuranAction.SeekTo -> audioHandler.seekTo(action.position)
            is QuranAction.SelectReader -> {
                val resume = _uiState.value.isPlaying || _uiState.value.isBuffering
                val position = audioHandler.currentRecitation.value
                audioHandler.pause()
                _uiState.update { it.copy(selectedReader = action.reader, playbackPosition = 0L, playbackDuration = 0L) }
                viewModelScope.launch {
                    try { userPreferences.setQuranReaderId(action.reader.id) }
                    catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                    catch (error: Exception) { _uiState.update { it.copy(errorMessage = "تعذر حفظ القارئ المختار") } }
                    if (resume && position != null) audioHandler.playPosition(position.copy(readerId = action.reader.id))
                }
                _uiState.value.selectedSurah?.id?.let { observeDownloads(it); observeDownloadWork(it) }
            }
            is QuranAction.UpdateFontSize -> {
                val clamped = action.size.coerceIn(20f, 48f)
                _uiState.update { it.copy(fontSize = clamped) }
                viewModelScope.launch {
                    try { userPreferences.setQuranFontSize(clamped) }
                    catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                    catch (error: Exception) { _uiState.update { it.copy(errorMessage = "تعذر حفظ حجم الخط") } }
                }
            }
            QuranAction.DownloadSurah -> downloadSurah()
            is QuranAction.Retry -> {
                loadBookmarks()
                observeLastRead()
                if (surahRequest != null) {
                    val request = surahRequest!!
                    loadSurahDetails(request.first, request.second)
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

    /**
     * Initializes reader target strictly adhering to the priority model:
     * 1. Explicit navigation target (surahId > 0, ayahNumber)
     * 2. Bookmark / plan target
     * 3. Last saved reading position
     * 4. Default fallback (Surah 1, Ayah 1)
     *
     * Keyed by [QuranTargetResolver.requestIdentity]:
     * - Re-emissions / configuration recreation of the SAME navigation request preserve the user's
     *   active reading position without re-driving a jump.
     * - A NEW navigation request (e.g., jumping to another surah or another ayah of the same surah)
     *   always re-initializes and updates [activeRequestKey] so the screen lands on the new target.
     */
    private fun initializeReaderTarget(explicitSurahId: Int?, explicitAyahNumber: Int?) {
        val requestKey = QuranTargetResolver.requestIdentity(explicitSurahId, explicitAyahNumber)
        val alreadyLocked = savedState.get<Boolean>("reader_target_locked") ?: false
        val savedRequestKey = savedState.get<String>("reader_request_key")
        val savedSurah = savedState.get<Int>("reader_active_surah")
        val savedAyah = savedState.get<Int>("reader_active_ayah")

        val isSameRequest = savedRequestKey == requestKey ||
            (savedRequestKey.isNullOrBlank() && alreadyLocked && savedSurah != null &&
                (explicitSurahId == null || explicitSurahId <= 0 ||
                    (explicitSurahId == savedSurah && (explicitAyahNumber ?: 1) == (savedAyah ?: 1))))

        if (alreadyLocked && savedSurah != null && isSameRequest) {
            val ayah = savedAyah ?: 1
            savedState["reader_request_key"] = requestKey
            if (_uiState.value.selectedSurah?.id != savedSurah) {
                loadSurahDetailsInternal(savedSurah, ayah, isInitialLock = false, requestKey = requestKey)
            } else {
                _uiState.update {
                    it.copy(
                        currentReadingSurahId = savedSurah,
                        currentReadingAyah = ayah,
                        isTargetLocked = true,
                        activeRequestKey = requestKey
                    )
                }
            }
            return
        }

        surahLoadJob?.cancel()
        surahLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // If explicit target is not provided, query last read from repository
                val lastReadPair = if (explicitSurahId == null || explicitSurahId <= 0) {
                    _uiState.value.lastRead?.let { it.first.id to it.second }
                        ?: repository.observeLastRead().firstOrNull()
                } else null

                val resolvedTarget = QuranTargetResolver.resolve(
                    explicitSurahId = explicitSurahId?.takeIf { it > 0 },
                    explicitAyahNumber = explicitAyahNumber?.takeIf { it > 0 },
                    lastReadTarget = lastReadPair
                )

                surahRequest = resolvedTarget.surahId to resolvedTarget.ayahNumber
                savedState["reader_target_locked"] = true
                savedState["reader_request_key"] = requestKey
                savedState["reader_active_surah"] = resolvedTarget.surahId
                savedState["reader_active_ayah"] = resolvedTarget.ayahNumber

                val surah = repository.getSurahById(resolvedTarget.surahId)
                if (surah != null) {
                    val ayahs = repository.getAyahsBySurah(resolvedTarget.surahId)
                    val safeAyah = resolvedTarget.ayahNumber.coerceIn(1, surah.totalVerses.coerceAtLeast(1))
                    savedState["reader_active_ayah"] = safeAyah
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedSurah = surah,
                            ayahs = ayahs,
                            initialAyahScroll = safeAyah,
                            currentReadingSurahId = resolvedTarget.surahId,
                            currentReadingAyah = safeAyah,
                            isTargetLocked = true,
                            activeRequestKey = requestKey
                        )
                    }
                    observeDownloads(resolvedTarget.surahId)
                    observeDownloadWork(resolvedTarget.surahId)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "السورة غير موجودة") }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, errorMessage = "تعذر قراءة القرآن؛ أعد المحاولة") }
            }
        }
    }

    private fun updateActivePosition(surahId: Int, ayahNumber: Int) {
        val currentState = _uiState.value
        val previousSurahId = currentState.selectedSurah?.id
        if (currentState.currentReadingSurahId == surahId &&
            currentState.currentReadingAyah == ayahNumber &&
            previousSurahId == surahId
        ) {
            return
        }

        savedState["reader_active_surah"] = surahId
        savedState["reader_active_ayah"] = ayahNumber
        _uiState.update {
            it.copy(
                currentReadingSurahId = surahId,
                currentReadingAyah = ayahNumber
            )
        }

        // Background update of selected surah if user turned pages into a new surah,
        // without setting isLoading or wiping ayahs / initialAyahScroll.
        if (surahId != previousSurahId) {
            viewModelScope.launch {
                try {
                    val surah = repository.getSurahById(surahId)
                    if (surah != null) {
                        val ayahs = repository.getAyahsBySurah(surahId)
                        _uiState.update { current ->
                            if (current.currentReadingSurahId == surahId) {
                                current.copy(selectedSurah = surah, ayahs = ayahs)
                            } else current
                        }
                        observeDownloads(surahId)
                        observeDownloadWork(surahId)
                    }
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (_: Exception) {}
            }
        }
    }

    private fun setReadingMode(mode: QuranReadingMode) {
        savedState["reader_mode"] = mode.name
        _uiState.update { it.copy(readingMode = mode) }
    }

    private fun loadSurahs() {
        surahsJob?.cancel()
        surahsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.observeAllSurahs()
                .catch { e ->
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    _uiState.update { it.copy(isLoading = false, errorMessage = "تعذر قراءة القرآن؛ أعد المحاولة") }
                }
                .collect { surahs ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = state.isSearching && state.searchQuery.isNotBlank(),
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
        bookmarksJob?.cancel()
        bookmarksJob = kotlinx.coroutines.flow.flow { emitAll(repository.observeAllBookmarks()) }
            .onEach { bookmarks ->
                _uiState.update { it.copy(bookmarks = bookmarks) }
            }
            .catch { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                _uiState.update { it.copy(errorMessage = "تعذر قراءة مفضلات القرآن؛ أعد المحاولة") }
            }
            .launchIn(viewModelScope)
    }

    private fun loadKhatmaProgress() {
        viewModelScope.launch {
            try {
                val progress = repository.getKhatmaProgress()
                _uiState.update { it.copy(khatmaProgress = progress) }
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) { _uiState.update { it.copy(errorMessage = "تعذر قراءة موضع القرآن المحفوظ") } }
        }
    }

    private fun observeLastRead() {
        lastReadJob?.cancel()
        lastReadJob = kotlinx.coroutines.flow.flow { emitAll(repository.observeLastRead()) }
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
            .catch { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                _uiState.update { it.copy(errorMessage = "تعذر قراءة آخر موضع؛ أعد المحاولة") }
            }
            .launchIn(viewModelScope)
    }

    private fun saveLastRead(surahId: Int, ayahNumber: Int) {
        val targetPair = surahId to ayahNumber
        val current = _uiState.value
        val alreadyPersisted = lastSavedPosition == targetPair ||
            (current.lastRead?.first?.id == surahId && current.lastRead.second == ayahNumber)

        savedState["reader_active_surah"] = surahId
        savedState["reader_active_ayah"] = ayahNumber
        _uiState.update { it.copy(currentReadingSurahId = surahId, currentReadingAyah = ayahNumber) }

        if (alreadyPersisted) return
        lastSavedPosition = targetPair

        viewModelScope.launch {
            try { repository.saveLastRead(surahId, ayahNumber) }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) {
                lastSavedPosition = null
                _uiState.update { it.copy(errorMessage = "تعذر حفظ موضع القراءة") }
            }
        }
    }

    private fun toggleBookmark(surahId: Int, verseNumber: Int) {
        viewModelScope.launch {
            try { repository.toggleBookmark(surahId, verseNumber) }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (error: Exception) { _uiState.update { it.copy(errorMessage = "تعذر حفظ المفضلة") } }
        }
    }

    private fun search(query: String) {
        savedState["quran_query"] = query
        _uiState.update { it.copy(searchQuery = query, isSearching = true, isLoading = query.isNotBlank(),
            searchResults = emptyList(), errorMessage = null) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update {
                it.copy(searchResults = emptyList(), isSearching = false, isLoading = false)
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = repository.searchAyahs(query)
                _uiState.update {
                    it.copy(isLoading = false, isSearching = false, searchResults = results)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, isSearching = false,
                    errorMessage = "تعذر البحث في القرآن؛ أعد المحاولة") }
            }
        }
    }

    private fun togglePlay() {
        viewModelScope.launch {
            if (_uiState.value.isPlaying || _uiState.value.isBuffering) { audioHandler.pause(); return@launch }
            val current = audioHandler.currentRecitation.value
            val reader = _uiState.value.selectedReader?.id ?: return@launch
            if (current != null && audioHandler.hasActiveMedia() && current.readerId == reader) audioHandler.togglePlay()
            else if (current != null) audioHandler.playPosition(current.copy(readerId = reader))
            else playAyah(_uiState.value.currentReadingAyah ?: _uiState.value.initialAyahScroll ?: 1)
        }
    }

    private fun readingAudioPosition(): com.example.feature.quran.domain.model.RecitationPosition? {
        val state = _uiState.value
        return audioHandler.currentRecitation.value ?: state.selectedSurah?.let { surah ->
            state.selectedReader?.let { reader ->
                com.example.feature.quran.domain.model.RecitationPosition(
                    surah.id,
                    state.currentReadingAyah ?: state.initialAyahScroll ?: 1,
                    reader.id
                )
            }
        }
    }

    private fun playNext() { viewModelScope.launch { audioHandler.playNext(readingAudioPosition()) } }
    private fun playPrevious() { viewModelScope.launch { audioHandler.playPrevious(readingAudioPosition()) } }

    private fun downloadSurah() {
        if (!com.example.feature.quran.util.RecitationAvailability.RECORDINGS_APPROVED) {
            _uiState.update { it.copy(errorMessage = com.example.feature.quran.util.RecitationAvailability.MESSAGE) }
            return
        }
        val surahId = _uiState.value.selectedSurah?.id ?: return
        val readerId = _uiState.value.selectedReader?.id ?: "Alafasy_128kbps"
        val tag = downloadScheduler.enqueue(surahId, readerId)
        observeDownloadWork(tag)
    }

    private fun loadSurahDetails(surahId: Int, ayahNumber: Int? = null) {
        val requestKey = QuranTargetResolver.requestIdentity(surahId, ayahNumber)
        loadSurahDetailsInternal(surahId, ayahNumber, isInitialLock = true, requestKey = requestKey)
    }

    private fun loadSurahDetailsInternal(
        surahId: Int,
        ayahNumber: Int?,
        isInitialLock: Boolean,
        requestKey: String
    ) {
        surahRequest = surahId to ayahNumber
        savedState["reader_active_surah"] = surahId
        savedState["reader_active_ayah"] = ayahNumber ?: 1
        savedState["reader_request_key"] = requestKey
        if (isInitialLock) {
            savedState["reader_target_locked"] = true
        }
        surahLoadJob?.cancel()
        surahLoadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    initialAyahScroll = ayahNumber ?: 1,
                    currentReadingSurahId = surahId,
                    currentReadingAyah = ayahNumber ?: 1,
                    selectedSurah = null,
                    ayahs = emptyList(),
                    isTargetLocked = true,
                    activeRequestKey = requestKey
                )
            }
            try {
                val surah = repository.getSurahById(surahId)
                if (surah != null) {
                    val ayahs = repository.getAyahsBySurah(surahId)
                    val safeAyah = (ayahNumber ?: 1).coerceIn(1, surah.totalVerses.coerceAtLeast(1))
                    savedState["reader_active_ayah"] = safeAyah
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedSurah = surah,
                            ayahs = ayahs,
                            initialAyahScroll = safeAyah,
                            currentReadingAyah = safeAyah
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
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, errorMessage = "تعذر قراءة القرآن؛ أعد المحاولة") }
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
            val state = _uiState.value
            val surahId = state.selectedSurah?.id ?: return@launch
            val readerId = state.selectedReader?.id ?: return@launch
            _uiState.update { it.copy(errorMessage = null) }
            audioHandler.playPosition(com.example.feature.quran.domain.model.RecitationPosition(surahId, ayahNumber, readerId))
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
