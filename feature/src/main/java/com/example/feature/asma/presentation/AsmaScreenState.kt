package com.example.feature.asma.presentation

import com.example.feature.asma.domain.model.AllahName


data class AsmaUiState(
    val isLoading: Boolean = true,
    val asmaList: List<AllahName> = emptyList(),
    val visibleAsmaList: List<AllahName> = emptyList(),
    val error: String? = null,
    val selectedName: AllahName? = null,
    val showFavoritesOnly: Boolean = false
)

sealed interface AsmaAction {
    object OnRetry : AsmaAction
    data class OnNameClick(val name: AllahName) : AsmaAction
    data class OnToggleFavorite(val id: Int) : AsmaAction
    data class OnToggleFavoritesOnly(val show: Boolean) : AsmaAction
    object OnDismissDetails : AsmaAction
}