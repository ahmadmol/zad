package com.example.feature.quran.data.local.dao

import androidx.room.*
import com.example.feature.quran.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemorizationDao {
    @Query("SELECT * FROM memorization_plans ORDER BY createdDay DESC, id")
    fun plans(): Flow<List<MemorizationPlanEntity>>
    @Query("SELECT * FROM memorization_reviews ORDER BY reviewedDay DESC, id")
    fun reviews(): Flow<List<MemorizationReviewEntity>>
    @Query("SELECT * FROM memorization_plans WHERE id = :id")
    suspend fun plan(id: String): MemorizationPlanEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(plan: MemorizationPlanEntity)
    @Update suspend fun update(plan: MemorizationPlanEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReview(review: MemorizationReviewEntity)
    @Transaction
    suspend fun record(plan: MemorizationPlanEntity, review: MemorizationReviewEntity) {
        insertReview(review)
        update(plan)
    }
    @Transaction
    suspend fun replaceRange(old: MemorizationPlanEntity, replacement: MemorizationPlanEntity) {
        update(old.copy(archived = true))
        insert(replacement)
    }
}
