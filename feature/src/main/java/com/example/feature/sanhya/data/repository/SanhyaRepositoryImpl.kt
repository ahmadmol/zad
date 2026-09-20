package com.example.feature.sanhya.data.repository

import com.example.feature.sanhya.data.datasource.SanhyaFixtureData
import com.example.feature.sanhya.domain.model.QuranStory
import com.example.feature.sanhya.domain.model.StoryFilterState
import com.example.feature.sanhya.domain.model.StorySortOption
import com.example.feature.sanhya.domain.repository.SanhyaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class SanhyaRepositoryImpl : SanhyaRepository {

    private val _storiesFlow = MutableStateFlow(SanhyaFixtureData.defaultStories)

    override fun observeStories(): Flow<List<QuranStory>> = _storiesFlow.asStateFlow()

    override fun observeFavorites(): Flow<List<QuranStory>> {
        return _storiesFlow.map { list -> list.filter { it.isFavorite } }
    }

    override fun observeWatchLater(): Flow<List<QuranStory>> {
        return _storiesFlow.map { list -> list.filter { it.isInWatchLater } }
    }

    override suspend fun getStoryById(id: String): QuranStory? {
        return _storiesFlow.value.find { it.id == id }
    }

    override suspend fun toggleFavorite(storyId: String) {
        _storiesFlow.value = _storiesFlow.value.map { story ->
            if (story.id == storyId) {
                story.copy(isFavorite = !story.isFavorite)
            } else story
        }
    }

    override suspend fun toggleWatchLater(storyId: String) {
        _storiesFlow.value = _storiesFlow.value.map { story ->
            if (story.id == storyId) {
                story.copy(isInWatchLater = !story.isInWatchLater)
            } else story
        }
    }

    override suspend fun filterStories(query: String, filterState: StoryFilterState): List<QuranStory> {
        val all = _storiesFlow.value
        val q = query.trim().lowercase()

        return all.filter { story ->
            // Search match
            val matchesQuery = q.isEmpty() ||
                    story.title.lowercase().contains(q) ||
                    story.subtitle.lowercase().contains(q) ||
                    story.summary.lowercase().contains(q) ||
                    story.surahs.any { it.lowercase().contains(q) } ||
                    story.keywords.any { it.lowercase().contains(q) }

            // Category match
            val matchesCategory = filterState.selectedCategory == "الكل" ||
                    story.category == filterState.selectedCategory

            // Episode count filter
            val matchesEpisodes = when (filterState.selectedEpisodeFilter) {
                "أكثر من حلقة" -> story.episodes.size > 1
                "أكثر من 5 حلقات" -> story.episodes.size > 5
                else -> true
            }

            matchesQuery && matchesCategory && matchesEpisodes
        }.let { list ->
            when (filterState.sortOption) {
                StorySortOption.NEWEST_FIRST -> list
                StorySortOption.OLDEST_FIRST -> list.reversed()
                StorySortOption.SERIES_ORDER -> list.sortedBy { it.id }
            }
        }
    }
}
