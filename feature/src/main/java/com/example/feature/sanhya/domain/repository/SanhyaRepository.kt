package com.example.feature.sanhya.domain.repository

import com.example.feature.sanhya.domain.model.QuranStory
import com.example.feature.sanhya.domain.model.StoryFilterState
import kotlinx.coroutines.flow.Flow

interface SanhyaRepository {
    fun observeStories(): Flow<List<QuranStory>>
    fun observeFavorites(): Flow<List<QuranStory>>
    fun observeWatchLater(): Flow<List<QuranStory>>
    suspend fun getStoryById(id: String): QuranStory?
    suspend fun toggleFavorite(storyId: String)
    suspend fun toggleWatchLater(storyId: String)
    suspend fun filterStories(query: String, filterState: StoryFilterState): List<QuranStory>
}
