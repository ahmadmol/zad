package com.example.feature.fahmanallah

import com.example.feature.fahmanallah.data.datasource.FahmFixtureData
import com.example.feature.fahmanallah.data.repository.FahmRepositoryImpl
import com.example.feature.fahmanallah.domain.model.FahmEpisodeProgressState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FahmRepositoryTest {

    private lateinit var repository: FahmRepositoryImpl

    @Before
    fun setUp() {
        repository = FahmRepositoryImpl()
    }

    @Test
    fun repository_containsExactly29CatalogEpisodes() = runTest {
        val episodes = repository.getEpisodes().first()
        assertEquals(29, episodes.size)
        assertTrue(episodes.all { it.isEmbeddable })

        episodes.forEachIndexed { index, episode ->
            val expectedNumber = index + 1
            val formattedNum = if (expectedNumber < 10) "0$expectedNumber" else "$expectedNumber"
            assertEquals(expectedNumber, episode.number)
            assertEquals("ep_$formattedNum", episode.id)
            assertEquals(FahmFixtureData.OFFICIAL_TITLES[index], episode.title)
            assertNotNull("videoId is verified against the official channel", episode.videoId)
            assertNotNull("durationText is verified from YouTube metadata", episode.durationText)
            assertTrue(episode.coverUrl.startsWith("https://i.ytimg.com/vi/"))
            assertEquals(FahmFixtureData.OFFICIAL_CHANNEL_ID, episode.sourceChannelId)
            assertEquals(FahmFixtureData.PLAYLIST_ID, episode.playlistId)
            assertNull("stationId must be null until officially verified mapping", episode.stationId)
            assertEquals(FahmEpisodeProgressState.NOT_STARTED, episode.progressState)
        }
    }

    @Test
    fun repository_hasNoEpisode30AndNoDuplicateVideoIds() = runTest {
        val episodes = repository.getEpisodes().first()
        assertTrue(episodes.none { it.number == 30 || it.id == "ep_30" })
        assertEquals(29, episodes.mapNotNull { it.videoId }.distinct().size)
        assertTrue(episodes.none { it.videoId == "dQw4w9WgXcQ" })
        assertTrue(episodes.all { it.stationId == null })
    }

    @Test
    fun repository_initialJourneyProgress_defaultsToZeroAndNoCurrentEpisode() = runTest {
        val progress = repository.getJourneyProgress().first()
        assertEquals(0, progress.completedCount)
        assertEquals(29, progress.totalEpisodes)
        assertEquals(0, progress.percentage)
        assertNull("continueEpisode must be null for new user", progress.continueEpisode)
    }

    @Test
    fun repository_contains7SpiritualStationsWithEmptyEpisodeMappings() = runTest {
        val stations = repository.getStations().first()
        assertEquals(7, stations.size)
        assertTrue(stations.all { it.relatedEpisodeIds.isEmpty() })
        assertTrue(stations.all { it.description.isBlank() })
        stations.forEach { station ->
            assertTrue(station.relatedEpisodeIds.isEmpty())
            assertTrue(station.description.isBlank())
        }
    }

    @Test
    fun toggleFavorite_updatesEpisodeFavoriteStatus() = runTest {
        val initialEp = repository.getEpisodes().first().first()
        val initialFavorite = initialEp.isFavorite

        repository.toggleFavorite(initialEp.id)
        val updatedEp = repository.getEpisodeById(initialEp.id).first()

        assertNotNull(updatedEp)
        assertEquals(!initialFavorite, updatedEp!!.isFavorite)
    }

    @Test
    fun markCompleted_updatesProgressAndCalculatesPercentage() = runTest {
        val ep01 = "ep_01"
        repository.markCompleted(ep01)

        val progress = repository.getJourneyProgress().first()
        assertEquals(1, progress.completedCount)
        assertTrue(progress.percentage > 0)

        val ep01Updated = repository.getEpisodeById(ep01).first()
        assertNotNull(ep01Updated)
        assertEquals(FahmEpisodeProgressState.COMPLETED, ep01Updated!!.progressState)
    }

    @Test
    fun searchEpisodes_returnsMatchingEpisodes() = runTest {
        val results = repository.searchEpisodes("التوكل").first()
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.title.contains("التوكل") })
    }

    @Test
    fun resetFeatureData_resetsDataToInitialState() = runTest {
        val ep01 = "ep_01"
        repository.markCompleted(ep01)
        repository.toggleFavorite(ep01)

        repository.resetFeatureData()

        val resetEp = repository.getEpisodeById(ep01).first()
        assertNotNull(resetEp)
        assertEquals(FahmEpisodeProgressState.NOT_STARTED, resetEp!!.progressState)
        assertEquals(false, resetEp.isFavorite)

        val progress = repository.getJourneyProgress().first()
        assertEquals(0, progress.completedCount)
        assertNull(progress.continueEpisode)
    }
}
