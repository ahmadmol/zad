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

data class LearningSearchState(val hits: List<LearningSearchHit> = emptyList(),
    val failedSources: List<String> = emptyList(), val isLoading: Boolean = true, val query: String = "")
private data class SearchSource<T>(val rows: List<T> = emptyList(), val error: String? = null, val loading: Boolean = false)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GlobalLearningSearchViewModel(
    private val sanhya: SanhyaRepository, private val nabi: NabiIhsanRepository, private val fahm: FahmRepository,
    private val handle: androidx.lifecycle.SavedStateHandle = androidx.lifecycle.SavedStateHandle()
) : ViewModel() {
    private val query = MutableStateFlow(handle.get<String>("learning_search_query") ?: "")
    private val refresh = MutableStateFlow(0)
    private fun <T> source(name: String, factory: () -> Flow<List<T>>): Flow<SearchSource<T>> = flow {
        emit(SearchSource(loading = true))
        emitAll(factory().map { SearchSource(rows = it) })
    }.catch { error ->
        if (error is kotlinx.coroutines.CancellationException) throw error
        emit(SearchSource(error = name))
    }
    val uiState = refresh.flatMapLatest {
        combine(source("سنحيا بالقرآن", sanhya::observeStories), source("نبي الإحسان", nabi::observeEpisodes),
            source("الفهم عن الله", fahm::getEpisodes), query) { stories, episodes, lessons, text ->
            val hits = if (text.isBlank()) emptyList() else buildList {
                stories.rows.filter { ArabicSearch.matches(it.title + " " + it.episodes.joinToString(" ") { ep -> ep.title }, text) }
                    .forEach { add(LearningSearchHit(SearchResultKind.SANHYA, it.id, it.title, "سنحيا بالقرآن", text)) }
                episodes.rows.filter { ArabicSearch.matches(it.title, text) }.forEach { add(LearningSearchHit(SearchResultKind.NABI, it.id, it.title, "نبي الإحسان", text)) }
                lessons.rows.filter { ArabicSearch.matches(it.title, text) }.forEach { add(LearningSearchHit(SearchResultKind.FAHM, it.id, it.title, "الفهم عن الله", text)) }
            }
            LearningSearchState(hits, listOfNotNull(stories.error, episodes.error, lessons.error),
                stories.loading || episodes.loading || lessons.loading, text)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearningSearchState())
    val results = uiState.map { it.hits }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun retry() { refresh.value++ }
    fun search(value: String) { query.value = value.trim(); handle["learning_search_query"] = query.value }
}
