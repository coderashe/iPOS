package com.ipos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApiService {
    @POST("/api/users")
    suspend fun saveUser(@Body user: UserDto): Response<UserDto>

    @GET("/api/users")
    suspend fun getAllUsers(): Response<List<UserDto>>
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

sealed class NetworkResult<T> {
    class Loading<T> : NetworkResult<T>()
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : NetworkResult<T>()
}

// And update the repository method:
suspend fun signUpUserRemote(
    userDto: UserDto,
    userApiService: UserApiService
): NetworkResult<UserDto> {
    return try {
        val response = userApiService.saveUser(userDto)
        if (response.isSuccessful) {
            NetworkResult.Success(response.body()!!)
        } else {
            NetworkResult.Error(
                response.errorBody()?.string() ?: "Unknown error",
                response.code()
            )
        }
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "Network error occurred")
    }
}