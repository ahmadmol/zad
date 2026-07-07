package com.example.feature.ehsan.data.repository

import com.example.feature.ehsan.data.local.dao.DonationDao
import com.example.feature.ehsan.data.local.entity.toDomain
import com.example.feature.ehsan.data.local.entity.toEntity
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EhsanRepositoryImpl(
    private val dao: DonationDao
) : EhsanRepository {
    override fun getAllDonations(): Flow<List<Donation>> {
        return dao.getAllDonations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDonationById(id: Long): Donation? {
        return dao.getDonationById(id)?.toDomain()
    }

    override suspend fun addDonation(donation: Donation) {
        dao.insertDonation(donation.toEntity())
    }

    override suspend fun deleteDonation(donation: Donation) {
        dao.deleteDonation(donation.toEntity())
    }

    override suspend fun updateDonationStatus(id: Long, status: String) {
        dao.updateDonationStatus(id, status)
    }

    override fun getMyDonations(name: String): Flow<List<Donation>> {
        return dao.getDonationsByDonor(name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
