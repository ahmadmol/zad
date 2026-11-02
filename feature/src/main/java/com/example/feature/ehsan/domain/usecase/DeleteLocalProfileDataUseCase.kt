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
 * | Listings and attached images | EVERYTHING_ON_DEVICE only | no trusted author identity exists |
 * | Orphaned images in app storage | always | no longer referenced by anything |
 * | Manual prayer log | always | private worship record tied to the person |
 * | Listings created by *other* people on this device | [DeleteScope] decides | not this user's data to erase by default |
 *
 * A donor name or phone does not establish ownership. MINE_ONLY preserves all
 * listings until the storage model provides a trusted author identity.
 */
class DeleteLocalProfileDataUseCase(
    private val userDao: UserDao,
    private val donationDao: DonationDao,
    private val imageStore: EhsanImageStore,
    private val manualPrayerLogRepository: ManualPrayerLogRepository
) {

    enum class DeleteScope {
        /** Profile and private prayer records; listings have no trusted owner identity. */
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

        val all = donationDao.getAllDonations().first()
        val toDelete = when (scope) {
            DeleteScope.EVERYTHING_ON_DEVICE -> all
            DeleteScope.MINE_ONLY -> all.filter { user != null && it.origin == "USER" && it.ownerId == "local-profile:${user.id}" }
        }

        // Rows first: an interrupted file cleanup leaves recoverable orphan files,
        // never surviving rows pointing to images that were already removed.
        toDelete.forEach { donationDao.deleteDonation(it) }

        // Any file no surviving listing references is removed too.
        val surviving = donationDao.getAllDonations().first().map { it.imageUrl }
        var referencedFilesRemoved = 0
        toDelete.map { it.imageUrl }.distinct().forEach { reference ->
            referencedFilesRemoved += imageStore.deleteUnreferenced(reference, surviving)
        }
        val orphansRemoved = imageStore.cleanupOrphans(surviving)

        manualPrayerLogRepository.clearAll()
        if (scope == DeleteScope.EVERYTHING_ON_DEVICE) userDao.clearUser() else userDao.clearCurrentUser()

        return Result(
            profileDeleted = user != null,
            listingsDeleted = toDelete.size,
            imagesDeleted = referencedFilesRemoved + orphansRemoved,
            prayerLogCleared = true
        )
    }
}
