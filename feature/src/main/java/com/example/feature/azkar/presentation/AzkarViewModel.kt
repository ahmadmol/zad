package com.example.feature.azkar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.azkar.domain.usecase.GetAzkarUseCase
import com.example.feature.azkar.domain.usecase.IncrementCounterUseCase
import com.example.feature.azkar.domain.usecase.ResetCounterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AzkarViewModel(
    private val getAzkarUseCase: GetAzkarUseCase,
    private val incrementCounterUseCase: IncrementCounterUseCase,
    private val resetCounterUseCase: ResetCounterUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AzkarUiState())
    val uiState: StateFlow<AzkarUiState> = _uiState.asStateFlow()
    init{
        observeAzkaarData()
    }
    private fun observeAzkaarData() {
        viewModelScope.launch{
            getAzkarUseCase()
                .catch { exepction ->
                    _uiState.update { it.copy(isLoading = false, error = exepction.message) }
                }
                .collect { zikerList ->
                    if(zikerList.isNotEmpty()) {
                        val firstziker = zikerList.first()
                        _uiState.update {
                            it.copy(
                                zikerId = firstziker.id,
                                zikertext = firstziker.text,
                                currentCount = firstziker.currentCount,
                                targetCount = firstziker.targetCount
                            )
                        }
                    }else
                    {
                            _uiState.update { it.copy(isLoading = false) }
                    }

                }
        }
    }
    fun onAction(action: AzkarAction) {
        when (action) {
            is AzkarAction.OnIncrement -> handleIncrement()
            is AzkarAction.OnReset -> handleReset()
        }
    }
    private fun handleIncrement() {
        viewModelScope.launch {
            val currentId = _uiState.value.zikerId
            if (currentId != 0L) {
                incrementCounterUseCase(currentId)
            }
        }
    }

    private fun handleReset() {
        viewModelScope.launch {
            val currentId = _uiState.value.zikerId
            if (currentId != 0L) {
                resetCounterUseCase(currentId)
            }
        }
    }
}
