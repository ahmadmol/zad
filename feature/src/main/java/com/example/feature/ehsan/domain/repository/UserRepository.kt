package com.example.feature.ehsan.domain.repository

import com.example.feature.ehsan.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUser(user: UserEntity)
    fun getUser(): Flow<UserEntity?>
    suspend fun login(phoneNumber: String): UserEntity?
    suspend fun logout()
    suspend fun updateName(firstName: String, lastName: String)
    suspend fun updateProfile(firstName: String, lastName: String, city: String, address: String)
}
