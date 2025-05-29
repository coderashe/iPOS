package com.example.learncompose303.network

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.UserDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncUserWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userApiService: UserApiService,
    private val userDao: UserDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = inputData.getLong("userId", -1L)
        val shouldRetry = inputData.getBoolean("retry", false)

        if (userId == -1L) {
            return Result.failure()
        }

        // Get user from local database
        val user = userDao.getUserById(userId) ?: return Result.failure()

        // Convert to DTO for API
        val userDto = UserDto(
            name = user.userFullName,
            contact = user.userContact,
            role = user.userRole,
            password = user.userPassword,
            email = user.userEmail,
            store = user.storeID,
            address = user.userAddress
        )

        return try {
            // Attempt to sync with remote server
            val response = userApiService.saveUser(userDto)

            if (response.isSuccessful) {
                Result.success()
            } else {
                if (shouldRetry) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e("SyncUserWorker", "Error syncing user", e)
            if (shouldRetry) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
