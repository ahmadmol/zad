package com.example.feature.duas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.feature.duas.domain.model.Dua
import kotlinx.serialization.Serializable

@Entity(tableName = "duas")
@Serializable
data class DuaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val text: String,
    val category: String,
    val source: String,
    val reference: String,
    val isFavorite: Boolean = false
)

fun DuaEntity.toDomain() = Dua(
    id = id,
    title = title,
    text = text,
    category = category,
    source = source,
    reference = reference,
    isFavorite = isFavorite
)

fun Dua.toEntity() = DuaEntity(
    id = id,
    title = title,
    text = text,
    category = category,
    source = source,
    reference = reference,
    isFavorite = isFavorite
)
