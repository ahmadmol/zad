package com.example.feature.asma.presentation

import com.example.feature.asma.domain.model.AllahName


data class AsmaUiState(
    val isLoading: Boolean = true,
    val asmaList: List<AllahName> = emptyList(),
    val error: String? = null,
    val selectedName: AllahName? = null
)

sealed interface AsmaAction {
    object OnRetry : AsmaAction
    data class OnNameClick(val name: AllahName) : AsmaAction
}