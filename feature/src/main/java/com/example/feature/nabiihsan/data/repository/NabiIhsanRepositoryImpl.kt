package com.example.feature.nabiihsan.data.repository

import com.example.feature.nabiihsan.data.datasource.NabiIhsanFixtureData
import com.example.feature.nabiihsan.domain.model.NabiIhsanEpisode
import com.example.feature.nabiihsan.domain.model.NabiIhsanFilterState
import com.example.feature.nabiihsan.domain.model.NabiSearchResult
import com.example.feature.nabiihsan.domain.model.NabiSortOption
import com.example.feature.nabiihsan.domain.model.PropheticRecipe
import com.example.feature.nabiihsan.domain.repository.NabiIhsanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import com.example.feature.learning.data.LearningBookmarkStore
import com.example.feature.learning.data.LearningCollection

class NabiIhsanRepositoryImpl(private val bookmarks: LearningBookmarkStore? = null) : NabiIhsanRepository {

    private val _episodesFlow = MutableStateFlow(NabiIhsanFixtureData.defaultEpisodes)
    private val _recipesFlow = MutableStateFlow(NabiIhsanFixtureData.defaultRecipes)

    override fun observeEpisodes(): Flow<List<NabiIhsanEpisode>> = bookmarks?.let { saved ->
        combine(_episodesFlow, saved.observe(LearningCollection.NABI_FAVORITES), saved.observe(LearningCollection.NABI_LATER)) {
            episodes, favorites, later -> episodes.map { it.copy(isFavorite = it.id in favorites, isWatchLater = it.id in later) }
        }
    } ?: _episodesFlow.asStateFlow()

    override fun observeRecipes(): Flow<List<PropheticRecipe>> = bookmarks?.let { saved ->
        combine(_recipesFlow, saved.observe(LearningCollection.NABI_RECIPE_FAVORITES)) { recipes, favorites ->
            recipes.map { it.copy(isFavorite = it.id in favorites) }
        }
    } ?: _recipesFlow.asStateFlow()

    override fun observeFavoriteEpisodes(): Flow<List<NabiIhsanEpisode>> {
        return observeEpisodes().map { list -> list.filter { it.isFavorite } }
    }

    override fun observeFavoriteRecipes(): Flow<List<PropheticRecipe>> {
        return observeRecipes().map { list -> list.filter { it.isFavorite } }
    }

    override fun observeWatchLaterEpisodes(): Flow<List<NabiIhsanEpisode>> {
        return observeEpisodes().map { list -> list.filter { it.isWatchLater } }
    }

    override suspend fun getEpisodeById(id: String): NabiIhsanEpisode? {
        return observeEpisodes().first().find { it.id == id }
    }

    override suspend fun getRecipeById(id: String): PropheticRecipe? {
        return observeRecipes().first().find { it.id == id }
    }

    override suspend fun toggleEpisodeFavorite(episodeId: String) {
        if (_episodesFlow.value.none { it.id == episodeId }) return
        if (bookmarks != null) { bookmarks.toggle(LearningCollection.NABI_FAVORITES, episodeId); return }
        _episodesFlow.value = _episodesFlow.value.map { ep ->
            if (ep.id == episodeId) ep.copy(isFavorite = !ep.isFavorite) else ep
        }
    }

    override suspend fun toggleRecipeFavorite(recipeId: String) {
        if (_recipesFlow.value.none { it.id == recipeId }) return
        if (bookmarks != null) { bookmarks.toggle(LearningCollection.NABI_RECIPE_FAVORITES, recipeId); return }
        _recipesFlow.value = _recipesFlow.value.map { recipe ->
            if (recipe.id == recipeId) recipe.copy(isFavorite = !recipe.isFavorite) else recipe
        }
    }

    override suspend fun toggleEpisodeWatchLater(episodeId: String) {
        if (_episodesFlow.value.none { it.id == episodeId }) return
        if (bookmarks != null) { bookmarks.toggle(LearningCollection.NABI_LATER, episodeId); return }
        _episodesFlow.value = _episodesFlow.value.map { ep ->
            if (ep.id == episodeId) ep.copy(isWatchLater = !ep.isWatchLater) else ep
        }
    }

    override suspend fun getEpisodesForRecipe(recipeId: String): List<NabiIhsanEpisode> {
        val recipe = getRecipeById(recipeId) ?: return emptyList()
        val allEpisodes = observeEpisodes().first()
        return allEpisodes.filter { ep ->
            ep.recipeIds.contains(recipeId) || recipe.relatedEpisodeIds.contains(ep.id)
        }
    }

    override suspend fun getRecipesForEpisode(episodeId: String): List<PropheticRecipe> {
        val episode = getEpisodeById(episodeId) ?: return emptyList()
        val allRecipes = observeRecipes().first()
        return allRecipes.filter { recipe ->
            recipe.relatedEpisodeIds.contains(episodeId) || episode.recipeIds.contains(recipe.id)
        }
    }

    override suspend fun search(
        query: String,
        filterState: NabiIhsanFilterState
    ): NabiSearchResult {
        val q = query.trim().lowercase()
        val allEpisodes = observeEpisodes().first()
        val allRecipes = observeRecipes().first()

        val filteredEpisodes = allEpisodes.filter { ep ->
            val matchesQuery = q.isEmpty() ||
                    com.example.feature.core.search.ArabicSearch.matches(ep.title, q) ||
                    com.example.feature.core.search.ArabicSearch.matches(ep.subtitle, q) ||
                    com.example.feature.core.search.ArabicSearch.matches(ep.aboutText, q) ||
                    com.example.feature.core.search.ArabicSearch.matches(ep.seerahContext, q) ||
                    ep.topics.any { com.example.feature.core.search.ArabicSearch.matches(it, q) } ||
                    ep.lessons.any { com.example.feature.core.search.ArabicSearch.matches(it, q) }

            val matchesCategory = filterState.selectedCategory == "الكل" ||
                    ep.topics.contains(filterState.selectedCategory)

            val matchesEpisodeFilter = when (filterState.selectedEpisodeFilter) {
                "حلقة واحدة" -> ep.episodeNumber != null && ep.episodeNumber == 1
                "أكثر من حلقة" -> (ep.episodeNumber ?: 0) > 1
                else -> true
            }

            val matchesDuration = com.example.feature.learning.data.LearningDuration.matches(ep.duration, filterState.selectedDurationFilter)

            matchesQuery && matchesCategory && matchesEpisodeFilter && matchesDuration
        }.let { list ->
            when (filterState.sortOption) {
                NabiSortOption.NEWEST_FIRST -> list.sortedByDescending { it.publishedAt.orEmpty() }
                NabiSortOption.OLDEST_FIRST -> list.sortedBy { it.publishedAt ?: "9999" }
                NabiSortOption.SERIES_ORDER -> list.sortedBy { it.episodeNumber ?: Int.MAX_VALUE }
            }
        }

        val filteredRecipes = allRecipes.filter { recipe ->
            val matchesQuery = q.isEmpty() ||
                    com.example.feature.core.search.ArabicSearch.matches(recipe.title, q) ||
                    com.example.feature.core.search.ArabicSearch.matches(recipe.meaning, q) ||
                    com.example.feature.core.search.ArabicSearch.matches(recipe.seerahContext, q) ||
                    recipe.whatWeLearn.any { com.example.feature.core.search.ArabicSearch.matches(it, q) }

            val matchesCategory = filterState.selectedCategory == "الكل" ||
                    recipe.category == filterState.selectedCategory

            matchesQuery && matchesCategory
        }

        return NabiSearchResult(
            episodes = filteredEpisodes,
            recipes = filteredRecipes
        )
    }

}


