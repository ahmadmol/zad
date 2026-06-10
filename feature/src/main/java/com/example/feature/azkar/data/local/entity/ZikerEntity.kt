package com.example.feature.azkar.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.feature.azkar.domain.model.Ziker

@Entity(tableName = "azkar_table")
data class ZikerEntity(
    @PrimaryKey(autoGenerate=true)
    val id : Long,
    val text: String,
    val currentCount: Int,
    val targetCount: Int
)

fun ZikerEntity.toDomain() : Ziker {
    return Ziker(
        id =id ,
        text = text,
        currentCount = currentCount,
        targetCount = targetCount
    )
}
fun Ziker.toEntity() : ZikerEntity {
    return ZikerEntity(
        id =id ,
        text = text,
        currentCount = currentCount,
        targetCount = targetCount
    )
}