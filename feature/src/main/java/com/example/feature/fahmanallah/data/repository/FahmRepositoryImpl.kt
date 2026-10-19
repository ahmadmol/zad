package com.example.feature.fahmanallah.data.repository

import com.example.feature.core.preferences.UserPreferences
import com.example.feature.core.search.ArabicSearch
import com.example.feature.fahmanallah.data.datasource.FahmFixtureData
import com.example.feature.fahmanallah.domain.model.*
import com.example.feature.fahmanallah.domain.repository.FahmRepository
import kotlinx.coroutines.flow.*

class FahmRepositoryImpl(private val preferences: UserPreferences? = null) : FahmRepository {
    private val episodesState = MutableStateFlow(FahmFixtureData.INITIAL_EPISODES)
    private val episodes: Flow<List<FahmEpisode>> = preferences?.learningIds(STATE_KEY)
        ?.map(::restore) ?: episodesState.asStateFlow()

    override fun getEpisodes() = episodes
    override fun getEpisodeById(id: String) = episodes.map { list -> list.find { it.id == id || it.number.toString() == id } }
    override fun getStations(): Flow<List<FahmStation>> = flowOf(FahmFixtureData.STATIONS)
    override fun getJourneyProgress() = episodes.map { list ->
        val completed = list.count { it.progressState == FahmEpisodeProgressState.COMPLETED }
        FahmJourneyProgress(completedCount = completed, totalEpisodes = list.size,
            percentage = if (list.isEmpty()) 0 else completed * 100 / list.size,
            continueEpisode = list.find { it.progressState == FahmEpisodeProgressState.IN_PROGRESS })
    }
    override fun getFavoriteEpisodes() = episodes.map { list -> list.filter { it.isFavorite } }
    override fun getWatchLaterEpisodes() = episodes.map { list -> list.filter { it.isWatchLater } }
    override fun searchEpisodes(query: String) = episodes.map { list ->
        if (query.isBlank()) emptyList() else list.filter { ep ->
            ArabicSearch.matches(listOfNotNull(ep.title, ep.subtitle, ep.ruleText, ep.understandText)
                .plus(ep.takeaways).joinToString(" "), query)
        }
    }

    private suspend fun update(transform: (List<FahmEpisode>) -> List<FahmEpisode>) {
        if (preferences == null) episodesState.update(transform)
        else preferences.updateLearningIds(STATE_KEY) { records -> transform(restore(records)).map(::encode).toSet() }
    }
    override suspend fun toggleFavorite(episodeId: String) = update { list ->
        list.map { if (it.matches(episodeId)) it.copy(isFavorite = !it.isFavorite) else it }
    }
    override suspend fun toggleWatchLater(episodeId: String) = update { list ->
        list.map { if (it.matches(episodeId)) it.copy(isWatchLater = !it.isWatchLater) else it }
    }
    override suspend fun markCompleted(episodeId: String) = update { list ->
        val index = list.indexOfFirst { it.matches(episodeId) }
        list.mapIndexed { n, ep -> when {
            n == index -> ep.copy(progressState = FahmEpisodeProgressState.COMPLETED)
            index >= 0 && n == index + 1 && ep.progressState == FahmEpisodeProgressState.NOT_STARTED ->
                ep.copy(progressState = FahmEpisodeProgressState.IN_PROGRESS)
            else -> ep
        } }
    }
    override suspend fun updateEpisodeProgress(episodeId: String, state: FahmEpisodeProgressState, lastPlayedSeconds: Int) = update { list ->
        list.map { if (it.matches(episodeId)) it.copy(progressState = state, lastPlayedSeconds = lastPlayedSeconds.coerceAtLeast(0)) else it }
    }
    override suspend fun resetFeatureData() = update { FahmFixtureData.INITIAL_EPISODES }

    private fun FahmEpisode.matches(id: String) = this.id == id || number.toString() == id
    private fun encode(ep: FahmEpisode) = "${ep.id}|${ep.progressState.name}|${ep.lastPlayedSeconds}|${ep.isFavorite}|${ep.isWatchLater}"
    private fun restore(records: Set<String>): List<FahmEpisode> {
        val byId = records.map { it.split('|') }.filter { it.size == 5 }.associateBy { it[0] }
        return FahmFixtureData.INITIAL_EPISODES.map { ep ->
            val values = byId[ep.id] ?: return@map ep
            ep.copy(progressState = runCatching { FahmEpisodeProgressState.valueOf(values[1]) }.getOrDefault(FahmEpisodeProgressState.NOT_STARTED),
                lastPlayedSeconds = values[2].toIntOrNull()?.coerceAtLeast(0) ?: 0,
                isFavorite = values[3] == "true", isWatchLater = values[4] == "true")
        }
    }
    companion object { const val STATE_KEY = "fahm_episode_state_v1" }
}
