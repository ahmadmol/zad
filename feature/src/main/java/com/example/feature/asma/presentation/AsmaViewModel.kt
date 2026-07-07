package com.example.feature.asma.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.asma.domain.usecase.GetAsmaUseCase
import com.example.feature.asma.domain.usecase.LoadAsmaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AsmaViewModel (
    private val getAsmaUseCase: GetAsmaUseCase,
    private val loadAsmaUseCase: LoadAsmaUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(AsmaUiState())
    val uiState: StateFlow<AsmaUiState> = _uiState.asStateFlow()
    init{
        loadAsma()
        observeAsmaData()
    }

    private fun loadAsma() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                loadAsmaUseCase()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    private fun observeAsmaData() {
        viewModelScope.launch {
            getAsmaUseCase()
                .catch { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
                .collect { asmaList ->
                    _uiState.update {
                        it.copy(
                            asmaList = asmaList,
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
                // سنضيف منطق عرض التفاصيل لاحقاً
            }
        }
    }
}