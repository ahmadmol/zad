package com.example.feature.ehsan.data.repository

import com.example.feature.ehsan.data.local.dao.UserDao
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {
    override suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override fun getUser(): Flow<UserEntity?> {
        return userDao.getUser()
    }

    override suspend fun login(phoneNumber: String): UserEntity? {
        return userDao.login(phoneNumber)
    }

    override suspend fun logout() {
        userDao.clearUser()
    }

    override suspend fun updateName(firstName: String, lastName: String) {
        userDao.updateName(firstName, lastName)
    }

    override suspend fun updateProfile(firstName: String, lastName: String, city: String, address: String) {
        userDao.updateProfile(firstName, lastName, city, address)
    }
}
