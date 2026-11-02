package com.example.feature.quran.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "memorization_plans")
data class MemorizationPlanEntity(
    @PrimaryKey val id: String,
    val surahId: Int,
    val firstAyah: Int,
    val lastAyah: Int,
    val createdDay: Long,
    val archived: Boolean = false,
    val confirmedDay: Long? = null,
    val dueDay: Long? = null,
    val stage: Int = 0,
    val lastReviewDay: Long? = null
)

@Entity(tableName = "memorization_reviews", foreignKeys = [ForeignKey(
    entity = MemorizationPlanEntity::class, parentColumns = ["id"], childColumns = ["planId"],
    onDelete = ForeignKey.NO_ACTION)], indices = [Index("planId")])
data class MemorizationReviewEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val reviewedDay: Long,
    val result: String,
    val nextDueDay: Long
)
