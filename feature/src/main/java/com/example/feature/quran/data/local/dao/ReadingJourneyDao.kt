package com.example.feature.quran.data.local.dao

import androidx.room.*
import com.example.feature.quran.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReadingJourneyDao {
    @Query("SELECT * FROM reading_plans ORDER BY startEpochDay DESC, id")
    abstract fun observePlans(): Flow<List<ReadingPlanEntity>>
    @Query("SELECT * FROM reading_page_completions")
    abstract fun observeCompletions(): Flow<List<ReadingPageCompletionEntity>>
    @Query("SELECT * FROM reading_plans WHERE id = :id")
    abstract suspend fun plan(id: String): ReadingPlanEntity?
    @Query("UPDATE reading_plans SET active = 0 WHERE active = 1")
    abstract suspend fun archiveActive()
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertPlan(plan: ReadingPlanEntity)
    @Transaction
    open suspend fun replaceActive(plan: ReadingPlanEntity) {
        archiveActive()
        insertPlan(plan)
    }
    @Query("UPDATE reading_plans SET dueEpochDay = :due WHERE id = :id AND active = 1")
    abstract suspend fun extend(id: String, due: Long)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertCompletions(rows: List<ReadingPageCompletionEntity>)
    @Query("DELETE FROM reading_page_completions WHERE planId = :id AND pageNumber = :page")
    abstract suspend fun clearPage(id: String, page: Int)
    @Query("SELECT * FROM private_ayah_notes ORDER BY updatedAt DESC, id")
    abstract fun observeNotes(): Flow<List<PrivateAyahNoteEntity>>
    @Query("SELECT * FROM private_ayah_notes WHERE id = :id")
    abstract suspend fun note(id: String): PrivateAyahNoteEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveNote(note: PrivateAyahNoteEntity)
    @Query("DELETE FROM private_ayah_notes WHERE id = :id")
    abstract suspend fun deleteNote(id: String)
}
