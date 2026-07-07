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

    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("UPDATE users SET firstName = :firstName, lastName = :lastName WHERE id = (SELECT id FROM users LIMIT 1)")
    suspend fun updateName(firstName: String, lastName: String)

    @Query("UPDATE users SET firstName = :firstName, lastName = :lastName, city = :city, address = :address WHERE id = (SELECT id FROM users LIMIT 1)")
    suspend fun updateProfile(firstName: String, lastName: String, city: String, address: String)

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun login(phoneNumber: String): UserEntity?

    @Query("DELETE FROM users")
    suspend fun clearUser()
}
