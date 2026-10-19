package com.example.feature.ehsan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.feature.ehsan.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE isActive = 1 LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("UPDATE users SET firstName = :firstName, lastName = :lastName WHERE id = (SELECT id FROM users WHERE isActive = 1 LIMIT 1)")
    suspend fun updateName(firstName: String, lastName: String)

    @Query("UPDATE users SET firstName = :firstName, lastName = :lastName, city = :city, address = :address WHERE id = (SELECT id FROM users WHERE isActive = 1 LIMIT 1)")
    suspend fun updateProfile(firstName: String, lastName: String, city: String, address: String)

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun login(phoneNumber: String): UserEntity?

    @Query("UPDATE users SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE users SET isActive = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun activate(id: Long)

    @Query("DELETE FROM users WHERE isActive = 1")
    suspend fun clearCurrentUser()

    @androidx.room.Transaction
    suspend fun saveActiveUser(user: UserEntity) {
        val existing = if (user.id == 0L) login(user.phoneNumber) else null
        deactivateAll()
        insertUser(user.copy(id = existing?.id ?: user.id, isActive = true))
    }

    @androidx.room.Transaction
    suspend fun openLocalProfile(phoneNumber: String): UserEntity? {
        val user = login(phoneNumber) ?: return null
        activate(user.id)
        return user.copy(isActive = true)
    }

    @Query("DELETE FROM users")
    suspend fun clearUser()
}
