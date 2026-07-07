package com.example.feature.ehsan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.feature.ehsan.domain.model.Donation

@Entity(tableName = "donations")
data class DonationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val type: String,
    val status: String,
    val donorName: String,
    val phoneNumber: String,
    val imageUrl: String? = null,
    val createdAt: Long
)

fun DonationEntity.toDomain() = Donation(
    id = id,
    title = title,
    description = description,
    category = category,
    location = location,
    type = type,
    status = status,
    donorName = donorName,
    phoneNumber = phoneNumber,
    imageUrl = imageUrl,
    createdAt = createdAt
)

fun Donation.toEntity() = DonationEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    location = location,
    type = type,
    status = status,
    donorName = donorName,
    phoneNumber = phoneNumber,
    imageUrl = imageUrl,
    createdAt = createdAt
)
