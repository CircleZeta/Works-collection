package com.renalytics.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renalytics.data.repository.UserRepository
import com.renalytics.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    fun getAllUsers(): Flow<List<User>> {
        return userRepository.getAllUsers()
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            userRepository.insertUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
        }
    }

    fun setDefaultUser(userId: Long) {
        viewModelScope.launch {
            userRepository.setDefaultUser(userId)
        }
    }

    suspend fun getDefaultUser(): User? {
        return userRepository.getDefaultUser()
    }

    suspend fun getUserById(id: Long): User? {
        return userRepository.getUserById(id)
    }
}
