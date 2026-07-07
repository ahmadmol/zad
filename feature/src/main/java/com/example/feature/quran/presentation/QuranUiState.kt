package com.example.feature.quran.presentation

import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.model.Verse
import com.example.feature.quran.domain.model.Bookmark
import com.example.feature.quran.domain.model.Reader

data class QuranUiState(
    val surahs: List<Surah> = emptyList(),
    val ayahs: List<Verse> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val searchResults: List<Verse> = emptyList(),
    val selectedSurah: Surah? = null,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val lastRead: Pair<Surah, Int>? = null,
    val initialAyahScroll: Int? = null,
    val searchQuery: String = "",
    val khatmaProgress: Float = 0f,
    val fontSize: Float = 28f,
    
    // Audio State
    val isPlaying: Boolean = false,
    val currentPlayingAyah: Int? = null,
    val playbackPosition: Long = 0L,
    val playbackDuration: Long = 0L,
    val selectedReader: Reader? = null,
    val availableReaders: List<Reader> = emptyList(),
    val downloadedAyahs: List<Int> = emptyList(),
    val isDownloading: Boolean = false
)

sealed interface QuranAction {
    object LoadSurahs : QuranAction
    data class SelectSurah(val surahId: Int, val ayahNumber: Int? = null) : QuranAction
    data class SaveLastRead(val surahId: Int, val ayahNumber: Int) : QuranAction
    data class ToggleBookmark(val surahId: Int, val verseNumber: Int) : QuranAction
    object LoadBookmarks : QuranAction
    data class Search(val query: String) : QuranAction
    object ClearSearch : QuranAction
    object LoadKhatmaProgress : QuranAction
    
    // Audio Actions
    object TogglePlay : QuranAction
    object PlayNext : QuranAction
    object PlayPrevious : QuranAction
    data class SeekTo(val position: Long) : QuranAction
    data class SelectReader(val reader: Reader) : QuranAction
    data class UpdateFontSize(val size: Float) : QuranAction
    object DownloadSurah : QuranAction

    object Retry : QuranAction
    object ClearError : QuranAction
}
