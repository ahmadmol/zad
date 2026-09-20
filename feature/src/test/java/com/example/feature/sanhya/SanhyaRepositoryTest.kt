package com.example.feature.sanhya

import com.example.feature.sanhya.data.datasource.SanhyaFixtureData
import com.example.feature.sanhya.data.repository.SanhyaRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SanhyaRepositoryTest {
    @Test
    fun officialCatalog_has23TopicsAnd29Episodes() = runTest {
        val stories = SanhyaRepositoryImpl().observeStories().first()
        val episodes = stories.flatMap { it.episodes }

        assertEquals(23, stories.size)
        assertEquals(29, episodes.size)
        assertEquals(stories.size, stories.map { it.id }.distinct().size)
        assertEquals(episodes.size, episodes.map { it.id }.distinct().size)
        assertEquals((1..29).toList(), episodes.mapNotNull { it.episodeNumber }.sorted())
        assertEquals(28, episodes.mapNotNull { it.youtubeVideoId }.size)
        assertEquals(28, episodes.mapNotNull { it.youtubeVideoId }.distinct().size)
        assertTrue(episodes.filter { it.youtubeVideoId != null }.all {
            it.sourceChannelId == SanhyaFixtureData.OFFICIAL_CHANNEL_ID &&
                it.thumbnailUrl?.startsWith("https://i.ytimg.com/vi/") == true &&
                it.durationLabel != null
        })
        assertTrue(stories.all { !it.coverUrl.contains("unsplash.com") })
        assertTrue(episodes.none { it.youtubeVideoId == "dQw4w9WgXcQ" })
    }

    @Test
    fun multipartTopics_haveVerifiedOrderingAndSingleOwnership() = runTest {
        val stories = SanhyaRepositoryImpl().observeStories().first()
        val allEpisodeIds = stories.flatMap { it.episodes }.map { it.id }

        assertEquals(allEpisodeIds.size, allEpisodeIds.distinct().size)
        assertEquals(listOf(2, 3), stories.single { it.id == "story_adam_sons" }.episodes.map { it.episodeNumber })
        assertEquals(listOf(5, 6), stories.single { it.id == "story_boy_monk_magician" }.episodes.map { it.episodeNumber })
        assertEquals(listOf(9, 10), stories.single { it.id == "story_cave_companions" }.episodes.map { it.episodeNumber })
        assertEquals(listOf(17, 18), stories.single { it.id == "story_moses_khidr" }.episodes.map { it.episodeNumber })
        assertEquals(listOf(27, 28, 29), stories.single { it.id == "topic_steadfast_after_ramadan" }.episodes.map { it.episodeNumber })
    }

    @Test
    fun rejectedProgramMismatch_atSlot27HasNoVideoMetadata() = runTest {
        val episode = SanhyaRepositoryImpl().observeStories().first()
            .flatMap { it.episodes }
            .single { it.episodeNumber == 27 }

        assertNull(episode.youtubeVideoId)
        assertNull(episode.durationLabel)
        assertNull(episode.thumbnailUrl)
        assertTrue(!episode.isEmbeddable)
        assertTrue(!episode.isAvailable)
    }
}
