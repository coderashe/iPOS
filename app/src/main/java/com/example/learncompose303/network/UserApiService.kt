package com.example.learncompose303.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApiService {
    @POST("/user")
    suspend fun saveUser(@Body user: UserDto): Response<UserDto>
}

// Data transfer object for the User - matching the Spring Boot entity structure
data class UserDto(
    val id: Long? = null,
    val name: String,
    val contact: String,
    val role: String,
    val password: String,
    val email: String,
    val store: String?,
    val address: String?
)
