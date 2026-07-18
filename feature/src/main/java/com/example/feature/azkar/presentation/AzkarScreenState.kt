package com.example.feature.azkar.presentation

import com.example.feature.azkar.domain.model.Zikr

data class AzkarUiState(
    val azkarList: List<Zikr> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val showFavoritesOnly: Boolean = false,
    val searchQuery: String = "",
    val fontSize: Float = 24f,
    val isVibrationEnabled: Boolean = true
)

sealed interface AzkarAction {
    data class OnIncrement(val zikrId: Long) : AzkarAction
    data class OnReset(val zikrId: Long) : AzkarAction
    data class OnResetCategory(val category: String) : AzkarAction
    data object OnRetry : AzkarAction
    data class OnCategorySelected(val category: String?) : AzkarAction
    data class OnToggleFavorite(val zikrId: Long) : AzkarAction
    data class OnToggleShowFavorites(val show: Boolean) : AzkarAction
    data class OnSearchQueryChanged(val query: String) : AzkarAction
    data class OnAddCustomZikr(val text: String, val targetCount: Int) : AzkarAction
}
