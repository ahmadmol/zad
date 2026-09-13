package com.example.feature.ehsan.domain.usecase

import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.data.local.dao.DonationDao
import com.example.feature.ehsan.data.local.dao.UserDao
import com.example.feature.prayer.domain.repository.ManualPrayerLogRepository
import kotlinx.coroutines.flow.first

/**
 * Phase 8C — "Delete my local profile and data".
 *
 * ## Ownership / PII policy
 *
 * Ihsan Release 1 is **local-only**: there is no account server, so "delete" means
 * "erase from this device", and it is final — nothing can be restored afterwards.
 *
 * What is deleted, and why:
 *
 * | Data | Deleted | Reason |
 * |------|---------|--------|
 * | User profile row (name, phone, city, address) | always | direct PII |
 * | Listings created by this user | always | carry the user's name + phone |
 * | Images attached to those listings | always | may show the user's home/goods |
 * | Orphaned images in app storage | always | no longer referenced by anything |
 * | Manual prayer log | always | private worship record tied to the person |
 * | Listings created by *other* people on this device | [DeleteScope] decides | not this user's data to erase by default |
 *
 * Ownership on a local board is established by the donor name recorded on the
 * listing, since that is the only identity the app has.
 */
class DeleteLocalProfileDataUseCase(
    private val userDao: UserDao,
    private val donationDao: DonationDao,
    private val imageStore: EhsanImageStore,
    private val manualPrayerLogRepository: ManualPrayerLogRepository
) {

    enum class DeleteScope {
        /** Profile + this user's own listings + their private records. */
        MINE_ONLY,

        /** Everything local, including listings authored by other people on the device. */
        EVERYTHING_ON_DEVICE
    }

    data class Result(
        val profileDeleted: Boolean,
        val listingsDeleted: Int,
        val imagesDeleted: Int,
        val prayerLogCleared: Boolean
    )

    suspend operator fun invoke(scope: DeleteScope = DeleteScope.MINE_ONLY): Result {
        val user = userDao.getUser().first()
        val ownerName = user?.let { (it.firstName + " " + it.lastName).trim() }

        val all = donationDao.getAllDonations().first()
        val toDelete = when (scope) {
            DeleteScope.EVERYTHING_ON_DEVICE -> all
            DeleteScope.MINE_ONLY ->
                if (ownerName.isNullOrBlank()) emptyList()
                else all.filter { it.donorName.trim() == ownerName }
        }

        toDelete.forEach { donation ->
            // Delete the image before the row, so a crash mid-way leaves an orphan
            // file (cleaned below) rather than an unreachable row pointing at PII.
            imageStore.delete(donation.imageUrl)
            donationDao.deleteDonation(donation)
        }

        // Any file no surviving listing references is removed too.
        val surviving = donationDao.getAllDonations().first().map { it.imageUrl }
        val orphansRemoved = imageStore.cleanupOrphans(surviving)

        manualPrayerLogRepository.clearAll()
        userDao.clearUser()

        return Result(
            profileDeleted = user != null,
            listingsDeleted = toDelete.size,
            imagesDeleted = toDelete.count { it.imageUrl != null } + orphansRemoved,
            prayerLogCleared = true
        )
    }
}
