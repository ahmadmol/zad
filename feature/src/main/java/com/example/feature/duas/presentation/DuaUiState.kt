package com.example.feature.duas.presentation

import com.example.feature.duas.domain.model.Dua

data class DuaUiState(
    val duas: List<Dua> = emptyList(),
    val allDuas: List<Dua> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val showFavoritesOnly: Boolean = false
)

sealed interface DuaAction {
    data class OnCategorySelected(val category: String?) : DuaAction
    data class OnSearchQueryChanged(val query: String) : DuaAction
    data class OnToggleFavorite(val id: Long, val isFavorite: Boolean) : DuaAction
    data class OnToggleFavoritesOnly(val show: Boolean) : DuaAction
    object Refresh : DuaAction
}
