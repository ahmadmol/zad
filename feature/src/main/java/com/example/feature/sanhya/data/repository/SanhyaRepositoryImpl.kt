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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import com.example.feature.learning.data.LearningBookmarkStore
import com.example.feature.learning.data.LearningCollection

class SanhyaRepositoryImpl(private val bookmarks: LearningBookmarkStore? = null) : SanhyaRepository {

    private val _storiesFlow = MutableStateFlow(SanhyaFixtureData.defaultStories)

    override fun observeStories(): Flow<List<QuranStory>> = bookmarks?.let { saved ->
        combine(_storiesFlow, saved.observe(LearningCollection.SANHYA_FAVORITES), saved.observe(LearningCollection.SANHYA_LATER)) {
            stories, favorites, later -> stories.map { it.copy(isFavorite = it.id in favorites, isInWatchLater = it.id in later) }
        }
    } ?: _storiesFlow.asStateFlow()

    override fun observeFavorites(): Flow<List<QuranStory>> {
        return observeStories().map { list -> list.filter { it.isFavorite } }
    }

    override fun observeWatchLater(): Flow<List<QuranStory>> {
        return observeStories().map { list -> list.filter { it.isInWatchLater } }
    }

    override suspend fun getStoryById(id: String): QuranStory? {
        return observeStories().first().find { it.id == id }
    }

    override suspend fun toggleFavorite(storyId: String) {
        if (_storiesFlow.value.none { it.id == storyId }) return
        if (bookmarks != null) { bookmarks.toggle(LearningCollection.SANHYA_FAVORITES, storyId); return }
        _storiesFlow.value = _storiesFlow.value.map { story ->
            if (story.id == storyId) {
                story.copy(isFavorite = !story.isFavorite)
            } else story
        }
    }

    override suspend fun toggleWatchLater(storyId: String) {
        if (_storiesFlow.value.none { it.id == storyId }) return
        if (bookmarks != null) { bookmarks.toggle(LearningCollection.SANHYA_LATER, storyId); return }
        _storiesFlow.value = _storiesFlow.value.map { story ->
            if (story.id == storyId) {
                story.copy(isInWatchLater = !story.isInWatchLater)
            } else story
        }
    }

    override suspend fun filterStories(query: String, filterState: StoryFilterState): List<QuranStory> {
        val all = observeStories().first()
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
                StorySortOption.NEWEST_FIRST -> list.sortedByDescending { story -> story.episodes.mapNotNull { it.publishedAt }.maxOrNull().orEmpty() }
                StorySortOption.OLDEST_FIRST -> list.sortedBy { story -> story.episodes.mapNotNull { it.publishedAt }.minOrNull() ?: "9999" }
                StorySortOption.SERIES_ORDER -> list.sortedBy { it.id }
            }
        }
    }
}
