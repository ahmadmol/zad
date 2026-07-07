package com.example.feature.ehsan.domain.repository

import com.example.feature.ehsan.domain.model.Donation
import kotlinx.coroutines.flow.Flow

interface EhsanRepository {
    fun getAllDonations(): Flow<List<Donation>>
    suspend fun getDonationById(id: Long): Donation?
    suspend fun addDonation(donation: Donation)
    suspend fun deleteDonation(donation: Donation)
    suspend fun updateDonationStatus(id: Long, status: String)
    fun getMyDonations(name: String): Flow<List<Donation>>
}
