package com.example.feature.nabiihsan.domain.repository

import com.example.feature.nabiihsan.domain.model.NabiIhsanEpisode
import com.example.feature.nabiihsan.domain.model.NabiIhsanFilterState
import com.example.feature.nabiihsan.domain.model.NabiSearchResult
import com.example.feature.nabiihsan.domain.model.PropheticRecipe
import kotlinx.coroutines.flow.Flow

interface NabiIhsanRepository {
    fun observeEpisodes(): Flow<List<NabiIhsanEpisode>>
    fun observeRecipes(): Flow<List<PropheticRecipe>>
    fun observeFavoriteEpisodes(): Flow<List<NabiIhsanEpisode>>
    fun observeFavoriteRecipes(): Flow<List<PropheticRecipe>>
    fun observeWatchLaterEpisodes(): Flow<List<NabiIhsanEpisode>>

    suspend fun getEpisodeById(id: String): NabiIhsanEpisode?
    suspend fun getRecipeById(id: String): PropheticRecipe?

    suspend fun toggleEpisodeFavorite(episodeId: String)
    suspend fun toggleRecipeFavorite(recipeId: String)
    suspend fun toggleEpisodeWatchLater(episodeId: String)

    suspend fun getEpisodesForRecipe(recipeId: String): List<NabiIhsanEpisode>
    suspend fun getRecipesForEpisode(episodeId: String): List<PropheticRecipe>

    suspend fun search(query: String, filterState: NabiIhsanFilterState): NabiSearchResult
}
