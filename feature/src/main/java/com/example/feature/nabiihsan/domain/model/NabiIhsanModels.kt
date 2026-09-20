package com.example.feature.nabiihsan.domain.model

/**
 * Domain entity representing an Episode in the Nabi Ihsan series.
 * Nabi Ihsan is a standalone program focused on the Prophet's Seerah and Prophetic Recipes.
 */
data class NabiIhsanEpisode(
    val id: String,
    val episodeNumber: Int? = null,
    val title: String,
    val subtitle: String,
    val aboutText: String,
    val seerahContext: String,
    val practicalReflection: String,
    val source: String,
    val youtubeVideoId: String? = null,
    val coverUrl: String,
    val duration: String? = null,
    val publishedAt: String? = null,
    val sourceUrl: String? = null,
    val playlistId: String? = null,
    val playlistPosition: Int? = null,
    val sourceChannelId: String? = null,
    val isEmbeddable: Boolean = false,
    val topics: List<String> = emptyList(),
    val lessons: List<String> = emptyList(),
    val recipeIds: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isWatchLater: Boolean = false,
    val watched: Boolean = false
)

/**
 * Domain entity representing a Prophetic Recipe (الوصفات النبوية).
 * Represents moral, spiritual, or behavioral virtues with Seerah context and practical applications.
 */
data class PropheticRecipe(
    val id: String,
    val title: String,
    val meaning: String,
    val seerahContext: String,
    val practicalReflection: String,
    val whatWeLearn: List<String> = emptyList(),
    val source: String,
    val iconName: String? = null,
    val coverUrl: String? = null,
    val quranOrHadithQuote: String? = null,
    val quoteSource: String? = null,
    val relatedEpisodeIds: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val category: String = "الأخلاق"
)

/**
 * Sorting options for Nabi Ihsan content.
 */
enum class NabiSortOption(val label: String) {
    NEWEST_FIRST("الأحدث أولاً"),
    OLDEST_FIRST("الأقدم أولاً"),
    SERIES_ORDER("ترتيب السلسلة")
}

/**
 * Filter configuration state for Nabi Ihsan episodes and search.
 */
data class NabiIhsanFilterState(
    val selectedCategory: String = "الكل",
    val selectedEpisodeFilter: String = "الكل",
    val selectedDurationFilter: String = "الكل",
    val sortOption: NabiSortOption = NabiSortOption.NEWEST_FIRST
)

/**
 * Grouped search results for episodes and recipes.
 */
data class NabiSearchResult(
    val episodes: List<NabiIhsanEpisode> = emptyList(),
    val recipes: List<PropheticRecipe> = emptyList()
) {
    val isEmpty: Boolean get() = episodes.isEmpty() && recipes.isEmpty()
    val totalCount: Int get() = episodes.size + recipes.size
}
