package com.example.feature.quran.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import com.example.feature.quran.domain.model.ReadingPlan
import com.example.feature.quran.domain.model.PrivateAyahNote

@Entity(tableName = "reading_plans", primaryKeys = ["id"])
data class ReadingPlanEntity(val id: String, val startEpochDay: Long, val dueEpochDay: Long,
    val firstPage: Int, val lastPage: Int, val active: Boolean) {
    fun toDomain() = ReadingPlan(id, startEpochDay, dueEpochDay, firstPage, lastPage, active)
}

@Entity(tableName = "reading_page_completions", primaryKeys = ["planId", "pageNumber"])
data class ReadingPageCompletionEntity(val planId: String, val pageNumber: Int, val confirmedAt: Long)

@Entity(tableName = "private_ayah_notes", primaryKeys = ["id"], indices = [Index(value = ["surahId", "ayahNumber"])])
data class PrivateAyahNoteEntity(val id: String, val surahId: Int, val ayahNumber: Int,
    val text: String, val createdAt: Long, val updatedAt: Long) {
    fun toDomain() = PrivateAyahNote(id, surahId, ayahNumber, text, createdAt, updatedAt)
}
