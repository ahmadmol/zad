package com.example.feature.fahmanallah.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.network.ConnectivityMonitor
import com.example.feature.fahmanallah.domain.model.*
import com.example.feature.fahmanallah.domain.repository.FahmRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class FahmViewModel(private val repository: FahmRepository, private val connectivityMonitor: ConnectivityMonitor,
    private val handle: SavedStateHandle = SavedStateHandle()) : ViewModel() {
    private var detailJob: Job? = null
    private var searchJob: Job? = null
    private var observation: Job? = null
    private val _mainState = MutableStateFlow(FahmMainUiState())
    val mainState = _mainState.asStateFlow()
    private val _episodesState = MutableStateFlow(FahmEpisodesUiState(selectedTab=(handle.get<Int>("fahm_episode_tab") ?: 0).coerceIn(0,2)))
    val episodesState = _episodesState.asStateFlow()
    private val _detailState = MutableStateFlow(FahmEpisodeDetailUiState())
    val detailState = _detailState.asStateFlow()
    private val _searchState = MutableStateFlow(FahmSearchUiState(query=handle.get<String>("fahm_search_query").orEmpty()))
    val searchState = _searchState.asStateFlow()
    private val _savedState = MutableStateFlow(FahmSavedUiState(selectedTab=(handle.get<Int>("fahm_saved_tab") ?: 0).coerceIn(0,1)))
    val savedStateFlow = _savedState.asStateFlow()
    val savedState: StateFlow<FahmSavedUiState> get() = savedStateFlow
    private val _settingsState = MutableStateFlow(FahmSettingsUiState())
    val settingsState = _settingsState.asStateFlow()

    init { reload(); if(_searchState.value.query.isNotBlank()) search(_searchState.value.query) }
    fun reload() {
        observation?.cancel()
        _mainState.update {it.copy(isLoading=true,error=null)}
        _episodesState.update {it.copy(isLoading=true,error=null)}
        _savedState.update {it.copy(isLoading=true,error=null)}
        observation = combine(repository.getEpisodes(),repository.getStations(),repository.getJourneyProgress(),connectivityMonitor.isOnline) {
            episodes,stations,progress,online ->
            _mainState.update {it.copy(isLoading=false,error=null,episodes=episodes,stations=stations,journeyProgress=progress,continueEpisode=progress.continueEpisode)}
            _episodesState.update {it.copy(isLoading=false,error=null,episodes=episodes,stations=stations,savedEpisodes=episodes.filter {ep->ep.isFavorite || ep.isWatchLater})}
            _savedState.update {it.copy(isLoading=false,error=null,favoriteEpisodes=episodes.filter {ep->ep.isFavorite},watchLaterEpisodes=episodes.filter {ep->ep.isWatchLater})}
            _detailState.update {it.copy(isOnline=online)}
        }.catch { e ->
            if(e is CancellationException) throw e
            val message="تعذر قراءة بيانات التعلم المحلية. محفوظاتك السابقة لم تُحذف."
            _mainState.update {it.copy(isLoading=false,error=message)}
            _episodesState.update {it.copy(isLoading=false,error=message)}
            _savedState.update {it.copy(isLoading=false,error=message)}
        }.launchIn(viewModelScope)
    }
    fun loadEpisodeDetail(episodeId: String) {
        detailJob?.cancel()
        _detailState.update {it.copy(isLoading=true,episode=null,error=null,actionError=null,previousEpisodeId=null,nextEpisodeId=null)}
        detailJob=viewModelScope.launch {
            repository.getEpisodes().catch {e->
                if(e is CancellationException) throw e
                _detailState.update {it.copy(isLoading=false,error="تعذر قراءة الدرس. أعد المحاولة.")}
            }.collect {list->
                val ep=list.firstOrNull {it.id==episodeId || it.number.toString()==episodeId}
                val index=list.indexOf(ep)
                _detailState.update {it.copy(isLoading=false,episode=ep,
                    previousEpisodeId=if(index>0) list[index-1].id else null,
                    nextEpisodeId=if(index>=0 && index<list.lastIndex) list[index+1].id else null,
                    error=if(ep==null) "الدرس غير موجود في المحتوى المتاح" else null)}
            }
        }
    }
    fun setEpisodesTab(tabIndex: Int) {
        val tab=tabIndex.coerceIn(0,2);handle["fahm_episode_tab"]=tab;_episodesState.update {it.copy(selectedTab=tab)}
    }
    fun initialiseSavedTab(tabIndex: Int) {if(!handle.contains("fahm_saved_tab")) setSavedTab(tabIndex)}
    fun setSavedTab(tabIndex: Int) {
        val tab=tabIndex.coerceIn(0,1);handle["fahm_saved_tab"]=tab;_savedState.update {it.copy(selectedTab=tab)}
    }
    fun search(query: String) {
        searchJob?.cancel();handle["fahm_search_query"]=query
        _searchState.update {it.copy(query=query,isLoading=query.isNotBlank(),error=null,results=emptyList())}
        if(query.isBlank()) return
        searchJob=viewModelScope.launch {
            repository.searchEpisodes(query).catch {e->
                if(e is CancellationException) throw e
                _searchState.update {it.copy(isLoading=false,error="تعذر البحث في الدروس. حاول مجددًا.")}
            }.collect {results->_searchState.update {it.copy(isLoading=false,results=results,error=null)}}
        }
    }
    private fun write(action: suspend () -> Unit, onSaved: () -> Unit = {}) {
        if(_detailState.value.busy) return
        _detailState.update {it.copy(busy=true,actionError=null)}
        _mainState.update {it.copy(busy=true,actionError=null)}
        viewModelScope.launch {
            try {action();onSaved()}
            catch(e: Exception) {
                if(e is CancellationException) throw e
                val message="تعذر حفظ تغيير التعلم. لم يُعلن اكتمال الدرس؛ أعد المحاولة."
                _detailState.update {it.copy(actionError=message)}
                _mainState.update {it.copy(actionError=message)}
                _savedState.update {it.copy(error=message)}
            } finally {
                _detailState.update {it.copy(busy=false)}
                _mainState.update {it.copy(busy=false)}
            }
        }
    }
    fun toggleFavorite(id: String)=write({repository.toggleFavorite(id)})
    fun toggleWatchLater(id: String)=write({repository.toggleWatchLater(id)})
    fun markCompleted(id: String,onSaved: () -> Unit = {})=write({repository.markCompleted(id)},onSaved)
    fun updateEpisodeProgress(id: String,state: FahmEpisodeProgressState,lastPlayedSeconds: Int=0)=write({repository.updateEpisodeProgress(id,state,lastPlayedSeconds)})
    fun moveEpisode(id: String,direction: Int)=write({repository.moveEpisode(id,direction)})
    fun resetFeatureData(onSaved: () -> Unit = {})=write({repository.resetFeatureData()},onSaved)
}
