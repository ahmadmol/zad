package com.example.feature.ehsan.data.local.dao

import androidx.room.*
import com.example.feature.ehsan.data.local.entity.DonationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Query("SELECT * FROM donations ORDER BY createdAt DESC")
    fun getAllDonations(): Flow<List<DonationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: DonationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDemoDonations(donations: List<DonationEntity>)

    @Update
    suspend fun updateDonation(donation: DonationEntity)

    @Query("DELETE FROM donations WHERE origin = 'DEMO'")
    suspend fun deleteDemoDonations()

    @Query("SELECT * FROM donations WHERE ownerId = :ownerId AND origin = 'USER' ORDER BY createdAt DESC")
    fun getDonationsByOwner(ownerId: String): Flow<List<DonationEntity>>

    @Delete
    suspend fun deleteDonation(donation: DonationEntity)

    @Query("SELECT * FROM donations WHERE id = :id")
    suspend fun getDonationById(id: Long): DonationEntity?

    @Query("UPDATE donations SET status = :status WHERE id = :id")
    suspend fun updateDonationStatus(id: Long, status: String)

    @Query("SELECT * FROM donations WHERE donorName = :name ORDER BY createdAt DESC")
    fun getDonationsByDonor(name: String): Flow<List<DonationEntity>>
}
