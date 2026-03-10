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
    private val userDao: UserDao,
    private val connectivityHelper: ConnectivityHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SyncUserWorker"
    }

    override suspend fun doWork(): Result {
        // Check network connectivity first
        if (!connectivityHelper.isNetworkAvailable()) {
            Log.w(TAG, "No network connection available, will retry later")
            return Result.retry()
        }

        val userId = inputData.getLong("userId", -1L)
        val shouldRetry = inputData.getBoolean("retry", false)

        if (userId == -1L) {
            Log.e(TAG, "Invalid user ID provided")
            return Result.failure()
        }

        // Get user from local database
        val user = userDao.getUserById(userId) ?: run {
            Log.e(TAG, "User with ID $userId not found in local database")
            return Result.failure()
        }

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
                Log.d(TAG, "Successfully synced user $userId")
                Result.success()
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string() ?: "No error details"
                Log.e(TAG, "Failed to sync user: $errorCode - $errorBody")

                // Don't retry on client errors (4xx)
                if (errorCode in 400..499) {
                    Log.e(TAG, "Client error ($errorCode), not retrying")
                    return Result.failure()
                }

                if (shouldRetry) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Cannot reach server: ${e.message}", e)
            if (shouldRetry) Result.retry() else Result.failure()
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Server timeout: ${e.message}", e)
            if (shouldRetry) Result.retry() else Result.failure()
        } catch (e: java.io.IOException) {
            Log.e(TAG, "Network I/O error: ${e.message}", e)
            if (shouldRetry) Result.retry() else Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user: ${e.message}", e)
            if (shouldRetry) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
