package com.example.feature.nabiihsan

import com.example.feature.nabiihsan.data.datasource.NabiIhsanFixtureData
import com.example.feature.nabiihsan.data.repository.NabiIhsanRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NabiIhsanRepositoryTest {
    @Test
    fun officialCatalog_has29SeparateVerifiedEpisodes() = runTest {
        val repository = NabiIhsanRepositoryImpl()
        val episodes = repository.observeEpisodes().first()

        assertEquals(29, episodes.size)
        assertEquals((1..29).toList(), episodes.map { it.episodeNumber })
        assertEquals(episodes.size, episodes.map { it.id }.distinct().size)
        assertEquals(episodes.size, episodes.mapNotNull { it.youtubeVideoId }.distinct().size)
        assertTrue(episodes.all { it.youtubeVideoId?.length == 11 })
        assertTrue(episodes.all { it.sourceChannelId == NabiIhsanFixtureData.OFFICIAL_CHANNEL_ID })
        assertTrue(episodes.all { it.playlistId == NabiIhsanFixtureData.PLAYLIST_ID })
        assertTrue(episodes.all { it.duration != null && it.publishedAt != null })
        assertTrue(episodes.all { it.coverUrl.startsWith("https://i.ytimg.com/vi/") })
        assertTrue(episodes.none { it.youtubeVideoId == "dQw4w9WgXcQ" })
    }

    @Test
    fun recipes_remainSeparateAndUnsupportedMappingsAreEmpty() = runTest {
        val repository = NabiIhsanRepositoryImpl()
        val episodes = repository.observeEpisodes().first()
        val recipes = repository.observeRecipes().first()

        assertTrue(recipes.isEmpty())
        assertTrue(episodes.all { it.recipeIds.isEmpty() })
        assertTrue(repository.getRecipesForEpisode("ep_01").isEmpty())
        assertTrue(repository.getEpisodesForRecipe("unverified_recipe").isEmpty())
    }

    @Test
    fun episodeStateOperationsStillWork() = runTest {
        val repository = NabiIhsanRepositoryImpl()
        repository.toggleEpisodeFavorite("ep_01")
        repository.toggleEpisodeWatchLater("ep_01")

        val episode = repository.getEpisodeById("ep_01")!!
        assertTrue(episode.isFavorite)
        assertTrue(episode.isWatchLater)
    }
}
