package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.UserDao
import com.example.learncompose303.data.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Define AuthRepository interface
interface AuthRepository {
    suspend fun signUpUser(user: User): Long
    suspend fun loginUser(contact: String, password: String): User?
}

// Implement AuthRepository
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun signUpUser(user: User): Long {
        return userDao.insertUser(user)
    }

    override suspend fun loginUser(contact: String, password: String): User? {
        return userDao.login(contact, password)
    }
}
