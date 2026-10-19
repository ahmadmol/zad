package com.example.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.search.ArabicSearch
import com.example.feature.sanhya.domain.repository.SanhyaRepository
import com.example.feature.nabiihsan.domain.repository.NabiIhsanRepository
import com.example.feature.fahmanallah.domain.repository.FahmRepository
import kotlinx.coroutines.flow.*

enum class SearchResultKind { HADITH, ASMA, ZIKR, COMMUNITY, SANHYA, NABI, FAHM, TASBIH }
data class LearningSearchHit(val kind: SearchResultKind, val id: String, val title: String, val source: String, val query: String = "")

class GlobalLearningSearchViewModel(sanhya: SanhyaRepository, nabi: NabiIhsanRepository, fahm: FahmRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    val results = combine(sanhya.observeStories(), nabi.observeEpisodes(), fahm.getEpisodes(), query) { stories, episodes, lessons, text ->
        if (text.isBlank()) emptyList() else buildList {
            stories.filter { ArabicSearch.matches(it.title + " " + it.episodes.joinToString(" ") { episode -> episode.title }, text) }
                .forEach { add(LearningSearchHit(SearchResultKind.SANHYA, it.id, it.title, "سنحيا بالقرآن", text)) }
            episodes.filter { ArabicSearch.matches(it.title, text) }.forEach { add(LearningSearchHit(SearchResultKind.NABI, it.id, it.title, "نبي الإحسان", text)) }
            lessons.filter { ArabicSearch.matches(it.title, text) }.forEach { add(LearningSearchHit(SearchResultKind.FAHM, it.id, it.title, "الفهم عن الله", text)) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun search(value: String) { query.value = value.trim() }
}
