package com.example.feature.asma.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.asma.domain.repository.AsmaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AsmaViewModel(
    private val repository: AsmaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AsmaUiState())
    val uiState: StateFlow<AsmaUiState> = _uiState.asStateFlow()

    init {
        loadAsma()
        observeAsmaData()
    }

    private fun loadAsma() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.loadAsma()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun observeAsmaData() {
        viewModelScope.launch {
            repository.getAllAsma()
                .catch { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
                .collect { asmaList ->
                    val visibleAsmaList = if (_uiState.value.showFavoritesOnly) {
                        asmaList.filter { it.isFavorite }
                    } else {
                        asmaList
                    }

                    _uiState.update {
                        it.copy(
                            asmaList = asmaList,
                            visibleAsmaList = visibleAsmaList,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun onAction(action: AsmaAction) {
        when (action) {
            is AsmaAction.OnRetry -> loadAsma()
            is AsmaAction.OnNameClick -> {
                _uiState.update { it.copy(selectedName = action.name) }
            }
            is AsmaAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(action.id)
                }
            }
            is AsmaAction.OnToggleFavoritesOnly -> {
                val visibleAsmaList = if (action.show) {
                    _uiState.value.asmaList.filter { it.isFavorite }
                } else {
                    _uiState.value.asmaList
                }
                _uiState.update { it.copy(showFavoritesOnly = action.show, visibleAsmaList = visibleAsmaList) }
            }
            AsmaAction.OnDismissDetails -> {
                _uiState.update { it.copy(selectedName = null) }
            }
        }
    }
}