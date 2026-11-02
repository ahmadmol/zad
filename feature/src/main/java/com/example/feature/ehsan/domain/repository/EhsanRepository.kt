package com.example.feature.ehsan.domain.repository

import com.example.feature.ehsan.domain.model.Donation
import kotlinx.coroutines.flow.Flow

interface EhsanRepository {
    fun getAllDonations(): Flow<List<Donation>>
    suspend fun getDonationById(id: Long): Donation?
    suspend fun addDonation(donation: Donation)
    suspend fun updateDonation(donation: Donation)
    suspend fun updateDonation(donation: Donation, imageSources: List<String>?) {
        require(imageSources == null) { "تعديل الصور غير متاح في هذا المصدر" }
        updateDonation(donation)
    }
    suspend fun deleteDonation(donation: Donation)
    suspend fun updateDonationStatus(id: Long, status: String)
    fun getMyDonations(ownerId: String): Flow<List<Donation>>
}
