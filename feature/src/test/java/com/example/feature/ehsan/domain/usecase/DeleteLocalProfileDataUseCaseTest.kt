package com.example.feature.ehsan.domain.usecase

import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.data.local.dao.DonationDao
import com.example.feature.ehsan.data.local.dao.UserDao
import com.example.feature.ehsan.data.local.entity.DonationEntity
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.prayer.domain.repository.ManualPrayerLogRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase 8C — local delete scope + PII cleanup policy. */
class DeleteLocalProfileDataUseCaseTest {

    private class FakeUserDao(initial: UserEntity?) : UserDao {
        val user = MutableStateFlow(initial)
        override suspend fun insertUser(user: UserEntity) { this.user.value = user }
        override fun getUser(): Flow<UserEntity?> = user
        override suspend fun updateName(firstName: String, lastName: String) = Unit
        override suspend fun updateProfile(
            firstName: String,
            lastName: String,
            city: String,
            address: String
        ) = Unit
        override suspend fun login(phoneNumber: String): UserEntity? = null
        override suspend fun clearUser() { user.value = null }
    }

    private class FakeDonationDao(initial: List<DonationEntity>) : DonationDao {
        val donations = MutableStateFlow(initial)
        override fun getAllDonations(): Flow<List<DonationEntity>> = donations
        override suspend fun insertDonation(donation: DonationEntity) {
            donations.value = donations.value + donation
        }
        override suspend fun deleteDonation(donation: DonationEntity) {
            donations.value = donations.value.filterNot { it.id == donation.id }
        }
        override suspend fun getDonationById(id: Long): DonationEntity? =
            donations.value.firstOrNull { it.id == id }
        override suspend fun updateDonationStatus(id: Long, status: String) = Unit
        override fun getDonationsByDonor(name: String): Flow<List<DonationEntity>> =
            MutableStateFlow(donations.value.filter { it.donorName == name })
    }

    private fun donation(id: Long, donor: String, image: String? = null) = DonationEntity(
        id = id,
        title = "t" + id,
        description = "d",
        category = "طعام",
        location = "حلب",
        type = "OFFER",
        status = "ACTIVE",
        donorName = donor,
        phoneNumber = "0900000000",
        imageUrl = image,
        createdAt = 0L
    )

    private val me = UserEntity(
        id = 1,
        firstName = "أحمد",
        lastName = "المولى",
        phoneNumber = "0911111111",
        city = "حلب",
        address = "-",
        role = "USER"
    )

    private val ownerName = "أحمد المولى"

    private fun useCase(
        userDao: UserDao,
        donationDao: DonationDao,
        imageStore: EhsanImageStore = mockk(relaxed = true),
        prayerLog: ManualPrayerLogRepository = mockk(relaxed = true)
    ) = DeleteLocalProfileDataUseCase(userDao, donationDao, imageStore, prayerLog)

    @Test
    fun `mine only deletes the profile and the users own listings`() = runTest {
        val userDao = FakeUserDao(me)
        val donationDao = FakeDonationDao(
            listOf(
                donation(1, ownerName),
                donation(2, "جار آخر"),
                donation(3, ownerName)
            )
        )

        val result = useCase(userDao, donationDao).invoke()

        assertTrue(result.profileDeleted)
        assertEquals(2, result.listingsDeleted)
        assertEquals(listOf(2L), donationDao.donations.value.map { it.id })
    }

    @Test
    fun `mine only never deletes another persons listing`() = runTest {
        val donationDao = FakeDonationDao(listOf(donation(9, "جار آخر")))

        useCase(FakeUserDao(me), donationDao).invoke()

        assertEquals(1, donationDao.donations.value.size)
    }

    @Test
    fun `everything on device deletes all listings`() = runTest {
        val donationDao = FakeDonationDao(
            listOf(donation(1, ownerName), donation(2, "جار آخر"))
        )

        val result = useCase(FakeUserDao(me), donationDao)
            .invoke(DeleteLocalProfileDataUseCase.DeleteScope.EVERYTHING_ON_DEVICE)

        assertEquals(2, result.listingsDeleted)
        assertTrue(donationDao.donations.value.isEmpty())
    }

    @Test
    fun `the profile row is always cleared`() = runTest {
        val userDao = FakeUserDao(me)
        useCase(userDao, FakeDonationDao(emptyList())).invoke()
        assertEquals(null, userDao.user.value)
    }

    @Test
    fun `attached images are deleted with their listing`() = runTest {
        val imageStore: EhsanImageStore = mockk(relaxed = true)
        coEvery { imageStore.cleanupOrphans(any()) } returns 0
        val donationDao = FakeDonationDao(
            listOf(donation(1, ownerName, image = "ihsan-image:a.jpg"))
        )

        useCase(FakeUserDao(me), donationDao, imageStore).invoke()

        coVerify(exactly = 1) { imageStore.delete("ihsan-image:a.jpg") }
    }

    @Test
    fun `orphan images are cleaned up afterwards`() = runTest {
        val imageStore: EhsanImageStore = mockk(relaxed = true)
        coEvery { imageStore.cleanupOrphans(any()) } returns 3
        val donationDao = FakeDonationDao(listOf(donation(1, ownerName, "ihsan-image:a.jpg")))

        val result = useCase(FakeUserDao(me), donationDao, imageStore).invoke()

        coVerify { imageStore.cleanupOrphans(any()) }
        assertEquals(4, result.imagesDeleted)
    }

    @Test
    fun `the private prayer log is always cleared`() = runTest {
        val prayerLog: ManualPrayerLogRepository = mockk(relaxed = true)

        val result = useCase(
            FakeUserDao(me),
            FakeDonationDao(emptyList()),
            prayerLog = prayerLog
        ).invoke()

        coVerify(exactly = 1) { prayerLog.clearAll() }
        assertTrue(result.prayerLogCleared)
    }

    @Test
    fun `deleting with no profile present is a safe no-op for listings`() = runTest {
        val donationDao = FakeDonationDao(listOf(donation(1, "جار آخر")))

        val result = useCase(FakeUserDao(null), donationDao).invoke()

        assertFalse(result.profileDeleted)
        assertEquals(0, result.listingsDeleted)
        assertEquals(1, donationDao.donations.value.size)
    }
}
