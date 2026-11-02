package com.example.feature.nabiihsan.presentation

import com.example.feature.nabiihsan.domain.model.NabiIhsanEpisode
import com.example.feature.nabiihsan.domain.model.NabiIhsanFilterState
import com.example.feature.nabiihsan.domain.model.NabiSearchResult
import com.example.feature.nabiihsan.domain.model.PropheticRecipe

/**
 * UI State for Nabi Ihsan Main Screen.
 */
data class NabiIhsanMainUiState(
    val episodes: List<NabiIhsanEpisode> = emptyList(),
    val filteredEpisodes: List<NabiIhsanEpisode> = emptyList(),
    val recipes: List<PropheticRecipe> = emptyList(),
    val searchQuery: String = "",
    val filterState: NabiIhsanFilterState = NabiIhsanFilterState(),
    val isFilterSheetVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI State for Nabi Ihsan Episode Details Screen.
 */
data class NabiEpisodeDetailUiState(
    val episode: NabiIhsanEpisode? = null,
    val linkedRecipes: List<PropheticRecipe> = emptyList(),
    val previousEpisodeId: String? = null,
    val nextEpisodeId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI State for Nabi Ihsan Recipe Details Screen.
 */
data class NabiRecipeDetailUiState(
    val recipe: PropheticRecipe? = null,
    val relatedEpisodes: List<NabiIhsanEpisode> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI State for Nabi Ihsan Favorites Screen.
 */
data class NabiFavoritesUiState(
    val selectedTab: Int = 0, // 0 = Episodes, 1 = Recipes
    val favoriteEpisodes: List<NabiIhsanEpisode> = emptyList(),
    val favoriteRecipes: List<PropheticRecipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI State for Nabi Ihsan Watch Later Screen.
 */
data class NabiWatchLaterUiState(
    val watchLaterEpisodes: List<NabiIhsanEpisode> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI State for Nabi Ihsan Search Screen.
 */
data class NabiSearchUiState(
    val query: String = "",
    val searchResult: NabiSearchResult = NabiSearchResult(),
    val filterState: NabiIhsanFilterState = NabiIhsanFilterState(),
    val isFilterSheetVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * UI State for Nabi Ihsan Settings Screen.
 */
data class NabiSettingsUiState(
    val downloadWifiOnly: Boolean = true,
    val newEpisodesNotification: Boolean = true,
    val videoQuality: String = "متوسطة"
)
