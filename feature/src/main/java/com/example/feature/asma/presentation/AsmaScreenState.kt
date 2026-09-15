package com.example.feature.asma.presentation

import com.example.feature.asma.domain.model.AllahName

data class AsmaUiState(
    val isLoading: Boolean = true,
    val asmaList: List<AllahName> = emptyList(),
    val visibleAsmaList: List<AllahName> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<AllahName> = emptyList(),
    val error: String? = null,
    val selectedName: AllahName? = null,
    val isDetailsOpen: Boolean = false,
    val showFavoritesOnly: Boolean = false
)

sealed interface AsmaAction {
    object OnRetry : AsmaAction
    data class OnNameClick(val name: AllahName) : AsmaAction
    data class OnSearchQueryChange(val query: String) : AsmaAction
    object OnSelectNext : AsmaAction
    object OnSelectPrevious : AsmaAction
    data class OnToggleFavorite(val id: Int) : AsmaAction
    data class OnToggleFavoritesOnly(val show: Boolean) : AsmaAction
    object OnOpenDetails : AsmaAction
    object OnDismissDetails : AsmaAction
}
