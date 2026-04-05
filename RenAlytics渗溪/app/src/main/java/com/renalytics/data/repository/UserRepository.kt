package com.renalytics.data.repository

import com.renalytics.data.db.AppDatabase
import com.renalytics.data.db.UserDao
import com.renalytics.data.models.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    suspend fun getUserById(id: Long): User? {
        return userDao.getUserById(id)
    }

    suspend fun getDefaultUser(): User? {
        return userDao.getDefaultUser()
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun setDefaultUser(userId: Long) {
        userDao.clearDefaultUser()
        val user = userDao.getUserById(userId)
        user?.let {
            it.isDefault = true
            userDao.update(it)
        }
    }
}
