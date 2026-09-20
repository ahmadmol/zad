package com.example.feature.ehsan.data.repository

import com.example.feature.core.observability.AppLogger
import com.example.feature.core.observability.NoOpAppLogger
import com.example.feature.ehsan.data.local.dao.DonationDao
import com.example.feature.ehsan.data.local.entity.DonationEntity
import com.example.feature.ehsan.data.local.entity.toDomain
import com.example.feature.ehsan.data.local.entity.toEntity
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class EhsanRepositoryImpl(
    private val dao: DonationDao,
    private val appLogger: AppLogger = NoOpAppLogger
) : EhsanRepository {

    private var hasSeeded = false

    override fun getAllDonations(): Flow<List<Donation>> {
        return dao.getAllDonations()
            .onEach { entities ->
                if (!hasSeeded) {
                    hasSeeded = true
                    val dummyEntities = entities.filter { it.title == "تت" || it.description == "عع" }
                    dummyEntities.forEach { dao.deleteDonation(it) }

                    val validEntities = entities.filterNot { it.title == "تت" || it.description == "عع" }
                    if (validEntities.isEmpty()) {
                        seedSampleDonations()
                    }
                }
            }
            .map { entities ->
                entities
                    .filterNot { it.title == "تت" || it.description == "عع" }
                    .map { it.toDomain() }
            }
            .catch { error ->
                appLogger.error("EhsanRepository", "get_all_donations_failed", error)
                throw error
            }
    }

    private suspend fun seedSampleDonations() {
        val now = System.currentTimeMillis()
        val sampleList = listOf(
            DonationEntity(
                title = "حقائب مدرسية لأطفال حلب",
                description = "توفير مجموعة من الحقائب المدرسية الجديدة للأطفال المحتاجين مع المستلزمات الأساسية.",
                category = "أخرى",
                location = "حلب",
                type = "OFFER",
                status = "AVAILABLE",
                donorName = "جمعية الإحسان",
                phoneNumber = "+963912345678",
                imageUrl = "drawable/ihsan_ehsan_school_backpack",
                createdAt = now - 3 * 3600 * 1000L
            ),
            DonationEntity(
                title = "دعم أدوية لمرضى مزمن",
                description = "أحتاج إلى دعم لتأمين أدوية شهرية لوالدي المصاب بمرض مزمن.",
                category = "أخرى",
                location = "حلب",
                type = "REQUEST",
                status = "AVAILABLE",
                donorName = "أبو أحمد",
                phoneNumber = "+963912345679",
                imageUrl = "drawable/ihsan_ehsan_care_support",
                createdAt = now - 24 * 3600 * 1000L
            ),
            DonationEntity(
                title = "ملابس شتوية للعائلات المحتاجة",
                description = "تتوفر لدينا ملابس شتوية جديدة ومستوردة بمقاسات مختلفة للعائلات المحتاجة.",
                category = "ملابس",
                location = "حلب",
                type = "OFFER",
                status = "AVAILABLE",
                donorName = "فاعل خير",
                phoneNumber = "+963912345680",
                imageUrl = "drawable/ihsan_ehsan_winter_clothes",
                createdAt = now - 48 * 3600 * 1000L
            )
        )
        sampleList.forEach { dao.insertDonation(it) }
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
