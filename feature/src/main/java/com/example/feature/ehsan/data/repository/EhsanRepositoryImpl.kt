package com.example.feature.ehsan.data.repository

import com.example.feature.core.observability.AppLogger
import com.example.feature.core.observability.NoOpAppLogger
import com.example.feature.ehsan.data.demo.CommunityDemoManager
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.data.local.dao.DonationDao
import com.example.feature.ehsan.data.local.dao.UserDao
import com.example.feature.ehsan.data.local.entity.DonationEntity
import com.example.feature.ehsan.data.local.entity.toDomain
import com.example.feature.ehsan.data.local.entity.toEntity
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.repository.EhsanRepository
import kotlinx.coroutines.flow.*

class EhsanRepositoryImpl(
    private val dao: DonationDao,
    private val appLogger: AppLogger = NoOpAppLogger,
    private val users: UserDao? = null,
    private val demo: CommunityDemoManager? = null,
    private val imageStore: EhsanImageStore? = null
) : EhsanRepository {
    override fun getAllDonations(): Flow<List<Donation>> = dao.getAllDonations()
        .onStart { demo?.ensureSeeded() }
        .map { rows -> rows.map { it.toDomain() } }
        .catch { error -> appLogger.error("EhsanRepository", "get_all_donations_failed", error); throw error }

    override suspend fun getDonationById(id: Long): Donation? = dao.getDonationById(id)?.toDomain()

    private suspend fun currentOwner(): String = users?.getUser()?.first()?.id?.takeIf { it > 0 }
        ?.let { "local-profile:$it" } ?: throw SecurityException("افتح ملفك الشخصي المحلي أولًا")

    private suspend fun owned(id: Long): DonationEntity {
        val owner = currentOwner()
        val stored = dao.getDonationById(id) ?: throw IllegalArgumentException("السجل غير موجود")
        if (stored.origin != "USER" || stored.ownerId != owner) throw SecurityException("لا تملك هذا السجل المحلي")
        return stored
    }

    override suspend fun addDonation(donation: Donation) {
        require(donation.id == 0L) { "استخدم تعديل السجل الموجود" }
        require(donation.title.isNotBlank() && donation.description.isNotBlank())
        val owner = currentOwner()
        dao.insertDonation(donation.copy(ownerId = owner, origin = "USER").toEntity())
    }

    override suspend fun updateDonation(donation: Donation) {
        val old = owned(donation.id)
        require(donation.title.isNotBlank() && donation.description.isNotBlank())
        require(donation.status in setOf("AVAILABLE", "PENDING", "COMPLETED"))
        // Editing cannot rewrite author, origin, creation time or image references.
        dao.updateDonation(old.copy(title = donation.title.trim(), description = donation.description.trim(),
            category = donation.category, location = donation.location, status = donation.status))
    }

    override suspend fun deleteDonation(donation: Donation) {
        val old = owned(donation.id)
        dao.deleteDonation(old)
        imageStore?.deleteUnreferenced(old.imageUrl, dao.getAllDonations().first().map { it.imageUrl })
    }

    override suspend fun updateDonation(donation: Donation, imageSources: List<String>?) {
        if (imageSources == null) return updateDonation(donation)
        val old = owned(donation.id)
        require(donation.title.isNotBlank() && donation.description.isNotBlank())
        require(donation.status in setOf("AVAILABLE", "PENDING", "COMPLETED"))
        require(imageSources.size <= 3)
        val store = requireNotNull(imageStore) { "تعديل الصور غير متاح" }
        val newImage = if (imageSources.isEmpty()) null else {
            val sources = imageSources.distinct().map(android.net.Uri::parse)
            require(sources.all { it.scheme in setOf("content", "file") }) { "اختر الصور من الجهاز" }
            requireNotNull(store.persistAlbum(sources)) { "تعذر حفظ الصور المختارة؛ بقيت الصور السابقة" }
        }
        try {
            owned(donation.id) // Recheck the active local owner after copying the selected files.
            dao.updateDonation(old.copy(title = donation.title.trim(), description = donation.description.trim(),
                category = donation.category, location = donation.location, status = donation.status, imageUrl = newImage))
        } catch (error: Exception) {
            store.deleteUnreferenced(newImage, dao.getAllDonations().first().map { it.imageUrl })
            throw error
        }
        try { store.deleteUnreferenced(old.imageUrl, dao.getAllDonations().first().map { it.imageUrl }) }
        catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
        catch (error: Exception) { appLogger.error("EhsanRepository", "old_listing_image_cleanup_failed", error) }
    }

    override suspend fun updateDonationStatus(id: Long, status: String) {
        owned(id)
        require(status in setOf("AVAILABLE", "PENDING", "COMPLETED"))
        dao.updateDonationStatus(id, status)
    }

    override fun getMyDonations(ownerId: String): Flow<List<Donation>> = combine(
        users?.getUser() ?: flowOf(null), dao.getDonationsByOwner(ownerId)
    ) { user, rows ->
        if (user == null || ownerId != "local-profile:${user.id}") emptyList()
        else rows.filter { it.origin == "USER" && it.ownerId == ownerId }.map { it.toDomain() }
    }
}
