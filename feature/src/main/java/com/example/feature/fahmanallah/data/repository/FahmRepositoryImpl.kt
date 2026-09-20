package com.example.feature.fahmanallah.data.repository

import com.example.feature.fahmanallah.data.datasource.FahmFixtureData
import com.example.feature.fahmanallah.domain.model.FahmEpisode
import com.example.feature.fahmanallah.domain.model.FahmEpisodeProgressState
import com.example.feature.fahmanallah.domain.model.FahmJourneyProgress
import com.example.feature.fahmanallah.domain.model.FahmStation
import com.example.feature.fahmanallah.domain.repository.FahmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FahmRepositoryImpl : FahmRepository {

    private val episodesState = MutableStateFlow(FahmFixtureData.INITIAL_EPISODES)
    private val stationsState = MutableStateFlow(FahmFixtureData.STATIONS)

    override fun getEpisodes(): Flow<List<FahmEpisode>> = episodesState.asStateFlow()

    override fun getEpisodeById(id: String): Flow<FahmEpisode?> =
        episodesState.map { list -> list.find { it.id == id || it.number.toString() == id } }

    override fun getStations(): Flow<List<FahmStation>> = stationsState.asStateFlow()

    override fun getJourneyProgress(): Flow<FahmJourneyProgress> = episodesState.map { list ->
        val completedCount = list.count { it.progressState == FahmEpisodeProgressState.COMPLETED }
        val total = 29
        val percentage = ((completedCount.toFloat() / total) * 100).toInt()
        
        // Pick active in-progress episode if user started one
        val continueEp = list.find { it.progressState == FahmEpisodeProgressState.IN_PROGRESS }

        FahmJourneyProgress(
            completedCount = completedCount,
            totalEpisodes = total,
            percentage = percentage,
            continueEpisode = continueEp
        )
    }

    override fun getFavoriteEpisodes(): Flow<List<FahmEpisode>> =
        episodesState.map { list -> list.filter { it.isFavorite } }

    override fun getWatchLaterEpisodes(): Flow<List<FahmEpisode>> =
        episodesState.map { list -> list.filter { it.isWatchLater } }

    override fun searchEpisodes(query: String): Flow<List<FahmEpisode>> = episodesState.map { list ->
        val q = query.trim()
        if (q.isEmpty()) emptyList()
        else {
            list.filter { ep ->
                ep.title.contains(q, ignoreCase = true) ||
                (ep.subtitle?.contains(q, ignoreCase = true) == true) ||
                (ep.ruleText?.contains(q, ignoreCase = true) == true) ||
                (ep.understandText?.contains(q, ignoreCase = true) == true) ||
                ep.takeaways.any { it.contains(q, ignoreCase = true) }
            }
        }
    }

    override suspend fun toggleFavorite(episodeId: String) {
        val currentList = episodesState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == episodeId || it.number.toString() == episodeId }
        if (index != -1) {
            val item = currentList[index]
            currentList[index] = item.copy(isFavorite = !item.isFavorite)
            episodesState.value = currentList
        }
    }

    override suspend fun toggleWatchLater(episodeId: String) {
        val currentList = episodesState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == episodeId || it.number.toString() == episodeId }
        if (index != -1) {
            val item = currentList[index]
            currentList[index] = item.copy(isWatchLater = !item.isWatchLater)
            episodesState.value = currentList
        }
    }

    override suspend fun markCompleted(episodeId: String) {
        val currentList = episodesState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == episodeId || it.number.toString() == episodeId }
        if (index != -1) {
            val item = currentList[index]
            currentList[index] = item.copy(progressState = FahmEpisodeProgressState.COMPLETED)
            
            // Set next episode to IN_PROGRESS if NOT_STARTED
            val nextIndex = index + 1
            if (nextIndex < currentList.size && currentList[nextIndex].progressState == FahmEpisodeProgressState.NOT_STARTED) {
                currentList[nextIndex] = currentList[nextIndex].copy(progressState = FahmEpisodeProgressState.IN_PROGRESS)
            }
            episodesState.value = currentList
        }
    }

    override suspend fun updateEpisodeProgress(
        episodeId: String,
        state: FahmEpisodeProgressState,
        lastPlayedSeconds: Int
    ) {
        val currentList = episodesState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == episodeId || it.number.toString() == episodeId }
        if (index != -1) {
            val item = currentList[index]
            currentList[index] = item.copy(
                progressState = state,
                lastPlayedSeconds = lastPlayedSeconds
            )
            episodesState.value = currentList
        }
    }

    override suspend fun resetFeatureData() {
        episodesState.value = FahmFixtureData.INITIAL_EPISODES
    }
}
