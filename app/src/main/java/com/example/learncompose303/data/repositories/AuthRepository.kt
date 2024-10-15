package com.example.learncompose303.data.repositories

import com.example.learncompose303.data.data_source.UserDao
import com.example.learncompose303.data.entities.User
import javax.inject.Inject

// Define AuthRepository interface
interface AuthRepository {
    suspend fun signUpUser(user: User): Long
    suspend fun loginUser(contact: String, password: String): User?
    suspend fun getUserByEmail(email: String): User?
}

// Implement AuthRepository
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun signUpUser(user: User): Long {
        val userId = userDao.insertUser(user) // Save to local DB
        return userDao.insertUser(user)
    }

    override suspend fun loginUser(contact: String, password: String): User? {
        return userDao.login(contact, password)
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email) // Querying the user from the local DB
    }
}

