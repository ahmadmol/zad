package com.example.feature.azkar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.feature.azkar.domain.model.Zikr

@Entity(tableName = "azkar_table")
data class ZikrEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val text: String,
    val currentCount: Int = 0,
    val targetCount: Int,
    val category: String,
    val isFavorite: Boolean = false,
    val source: String = "",
    val dailyProgress: Int = 0,
    val lastUpdatedDate: String = ""
)

fun ZikrEntity.toDomain(): Zikr {
    return Zikr(
        id = id,
        title = title,
        text = text,
        currentCount = currentCount,
        targetCount = targetCount,
        category = category,
        isFavorite = isFavorite,
        source = source,
        dailyProgress = dailyProgress
    )
}

fun Zikr.toEntity(lastUpdatedDate: String): ZikrEntity {
    return ZikrEntity(
        id = id,
        title = title,
        text = text,
        currentCount = currentCount,
        targetCount = targetCount,
        category = category,
        isFavorite = isFavorite,
        source = source,
        dailyProgress = dailyProgress,
        lastUpdatedDate = lastUpdatedDate
    )
}
