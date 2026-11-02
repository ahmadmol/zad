package com.example.feature.quran.domain.repository

import com.example.feature.quran.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ReadingJourneyRepository {
    val plans: Flow<List<ReadingPlan>>
    val completedPages: Flow<Map<String, Set<Int>>>
    val notes: Flow<List<PrivateAyahNote>>
    suspend fun createPlan(firstPage: Int, lastPage: Int, days: Int)
    suspend fun extendPlan(id: String, daysFromToday: Int)
    suspend fun confirmPages(id: String, pages: Set<Int>)
    suspend fun clearPage(id: String, page: Int)
    suspend fun saveNote(id: String?, surahId: Int, ayahNumber: Int, text: String)
    suspend fun deleteNote(id: String)
}
