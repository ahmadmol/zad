package com.example.feature.fahmanallah.domain.repository

import com.example.feature.fahmanallah.domain.model.FahmEpisode
import com.example.feature.fahmanallah.domain.model.FahmEpisodeProgressState
import com.example.feature.fahmanallah.domain.model.FahmJourneyProgress
import com.example.feature.fahmanallah.domain.model.FahmStation
import kotlinx.coroutines.flow.Flow

interface FahmRepository {
    fun getEpisodes(): Flow<List<FahmEpisode>>
    fun getEpisodeById(id: String): Flow<FahmEpisode?>
    fun getStations(): Flow<List<FahmStation>>
    fun getJourneyProgress(): Flow<FahmJourneyProgress>
    fun getFavoriteEpisodes(): Flow<List<FahmEpisode>>
    fun getWatchLaterEpisodes(): Flow<List<FahmEpisode>>
    fun searchEpisodes(query: String): Flow<List<FahmEpisode>>
    
    suspend fun toggleFavorite(episodeId: String)
    suspend fun toggleWatchLater(episodeId: String)
    suspend fun markCompleted(episodeId: String)
    suspend fun updateEpisodeProgress(episodeId: String, state: FahmEpisodeProgressState, lastPlayedSeconds: Int = 0)
    suspend fun resetFeatureData()
}
