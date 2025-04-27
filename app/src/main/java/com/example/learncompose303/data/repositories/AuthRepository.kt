package com.example.learncompose303.data.repositories

import android.util.Log
import androidx.work.WorkManager
import com.example.learncompose303.data.data_source.UserDao
import com.example.learncompose303.data.entities.User
import com.example.learncompose303.network.NetworkResult
import com.example.learncompose303.network.UserApiService
import com.example.learncompose303.network.UserDto
import javax.inject.Inject

// Define AuthRepository interface
interface AuthRepository {
    suspend fun signUpUser(user: User): Long
    suspend fun loginUser(contact: String, password: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun getAllUsersOnce(): List<User>
    suspend fun fetchAllUsers(): NetworkResult<List<UserDto>>
    suspend fun saveUserRemote(userDto: UserDto): NetworkResult<UserDto>
}

// Implement AuthRepository
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApiService: UserApiService
) : AuthRepository {

    //    save new user
    override suspend fun saveUserRemote(userDto: UserDto): NetworkResult<UserDto> {
        return try {
            // Log the user data before sending
            Log.d(
                "UsersData",
                "Attempting to save user: ${userDto.name}, ${userDto.email}, ${userDto.role}"
            )

            val response = userApiService.saveUser(userDto)

            if (response.isSuccessful) {
                Log.d("UsersData", "Successfully saved user to remote database")
                Log.d("UsersData", "Response code: ${response.code()}")
                NetworkResult.Success(response.body()!!)
            } else {
                Log.e("UsersData", "Failed to save user remotely: ${response.code()}")
                Log.e(
                    "UsersData",
                    "Error body: ${response.errorBody()?.string() ?: "No error details"}"
                )
                NetworkResult.Error("Failed to save user: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("UsersData", "Network error saving user remotely: ${e.message}")
            Log.e("UsersData", "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            NetworkResult.Error("Network error: ${e.message}")
        }
    }

    // Function to fetch all users from the remote API
    override suspend fun fetchAllUsers(): NetworkResult<List<UserDto>> {
        return try {
            val response = userApiService.getAllUsers()

            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                NetworkResult.Success(users)
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to fetch users",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = e.message ?: "Network error occurred")
        }
    }

    // Optional: Fetch and sync to local database
//    suspend fun fetchAndSyncAllUsers() {
//        when (val result = fetchAllUsers()) {
//            is NetworkResult.Success -> {
//                // Convert DTOs to local entities and save to database
//                val localUsers = result.data.map { dto ->
//                    User(
//                        userId = dto.id ?: 0,  // Handle null ID if needed
//                        userFullName = dto.name,
//                        userContact = dto.contact,
//                        userRole = dto.role,
//                        userPassword = dto.password,
//                        userEmail = dto.email,
//                        storeID = dto.store ?: "iSTART001",
//                        userAddress = dto.address
//                    )
//                }
//
//                // Insert all users to local database
//                authRepository.insertAllUsers(localUsers)
//            }
//
//            is NetworkResult.Error -> {
//                // Handle error - log, show message, etc.
//                Log.e("AuthRepository", "Failed to fetch users: ${result.message}")
//            }
//        }
//    }

    // Fetch all users once (for initial check)
    override suspend fun getAllUsersOnce(): List<User> {
        return userDao.getAllUsersOnce()
    }

    override suspend fun signUpUser(user: User): Long {
        return userDao.insertUser(user)
    }

    override suspend fun loginUser(contact: String, password: String): User? {
        return userDao.login(contact, password)
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email) // Querying the user from the local DB
    }
}

