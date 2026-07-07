package com.example.feature.hadith.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.feature.hadith.domain.model.Hadith

@Entity(tableName = "hadiths")
data class HadithEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val narrator: String,
    val source: String,
    val category: String,
    val isFavorite: Boolean = false,
    val explanation: String? = null
) {
    fun toDomain() = Hadith(id, text, narrator, source, category, isFavorite, explanation)
    
    companion object {
        fun fromDomain(hadith: Hadith) = HadithEntity(
            hadith.id,
            hadith.text,
            hadith.narrator,
            hadith.source,
            hadith.category,
            hadith.isFavorite,
            hadith.explanation
        )
    }
}
