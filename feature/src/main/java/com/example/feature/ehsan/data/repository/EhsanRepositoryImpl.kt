package com.example.feature.ehsan.data.repository

import com.example.feature.core.observability.AppLogger
import com.example.feature.core.observability.NoOpAppLogger
import com.example.feature.ehsan.data.local.dao.DonationDao
import com.example.feature.ehsan.data.local.entity.toDomain
import com.example.feature.ehsan.data.local.entity.toEntity
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class EhsanRepositoryImpl(
    private val dao: DonationDao,
    private val appLogger: AppLogger = NoOpAppLogger
) : EhsanRepository {
    override fun getAllDonations(): Flow<List<Donation>> {
        return dao.getAllDonations()
            .map { entities -> entities.map { it.toDomain() } }
            .catch { error ->
                appLogger.error("EhsanRepository", "get_all_donations_failed", error)
                throw error
            }
    }

    override suspend fun getDonationById(id: Long): Donation? {
        return try {
            dao.getDonationById(id)?.toDomain()
        } catch (t: Throwable) {
            appLogger.error("EhsanRepository", "get_donation_by_id_failed", t)
            throw t
        }
    }

    override suspend fun addDonation(donation: Donation) {
        try {
            dao.insertDonation(donation.toEntity())
        } catch (t: Throwable) {
            appLogger.error("EhsanRepository", "add_donation_failed", t)
            throw t
        }
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
