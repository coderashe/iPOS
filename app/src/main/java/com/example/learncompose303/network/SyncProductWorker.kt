package com.example.learncompose303.network

import android.annotation.SuppressLint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.ProductDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@SuppressLint("RestrictedApi")
@HiltWorker
class SyncProductWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val productDao: ProductDao,
    private val productApiService: ProductApiService
) : CoroutineWorker(appContext, params) {

    interface Factory : WorkerAssistedFactory<SyncProductWorker>

    companion object {
        private const val TAG = "SyncProductWorker"
        const val KEY_PRODUCT_ID = "productId"
        const val KEY_RETRY = "retry"
        const val KEY_BATCH_SYNC = "batchSync"
    }

    init {
        Log.d(TAG, "Worker injected successfully")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val isBatchSync = inputData.getBoolean(KEY_BATCH_SYNC, false)
            val shouldRetry = inputData.getBoolean(KEY_RETRY, true)

            if (isBatchSync) {
                return@withContext performBatchSync(shouldRetry)
            } else {
                val productId = inputData.getLong(KEY_PRODUCT_ID, -1L)
                if (productId == -1L) {
                    Log.e(TAG, "Invalid product ID provided for sync")
                    return@withContext Result.failure()
                }
                return@withContext syncSingleProduct(productId, shouldRetry)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in sync worker", e)
            return@withContext if (inputData.getBoolean(KEY_RETRY, true)) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun syncSingleProduct(productId: Long, shouldRetry: Boolean): Result {
        val product = productDao.getProductById(productId) ?: run {
            Log.e(TAG, "Product with ID $productId not found in local database")
            return Result.failure()
        }

        // Validate required fields
        if (product.productDescription.isBlank() || product.productCode.isBlank()) {
            Log.e(TAG, "Product $productId has invalid required fields, skipping sync")
            return Result.failure()
        }

        val productDto = product.toDto()
        Log.d(TAG, "Syncing product $productDto")

        return try {
            val response = productApiService.saveProduct(productDto)
            if (response.isSuccessful) {
                response.body()?.let { serverDto ->
                    val updatedProduct = product.copy(
                        lastUpdated = System.currentTimeMillis()
                    )
                    productDao.updateProduct(updatedProduct)
                    Log.d(TAG, "Successfully synced product $productId")
                }
                Result.success()
            } else {
                Log.e(TAG, "Failed to sync product: ${response.code()}")
                Log.e(TAG, "Error body: ${response.errorBody()?.string() ?: "No error details"}")
                if (shouldRetry) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing product: ${e.message}", e)
            if (shouldRetry) Result.retry() else Result.failure()
        }
    }

    private suspend fun performBatchSync(shouldRetry: Boolean): Result {
        try {
            val unsyncedProducts = productDao.getUnsyncedProducts(50)
            if (unsyncedProducts.isEmpty()) {
                Log.d(TAG, "No products to sync in batch")
                return Result.success()
            }

            Log.d(TAG, "Starting batch sync of ${unsyncedProducts.size} products")
            var successCount = 0
            var skippedCount = 0

            for (product in unsyncedProducts) {
                if (product.productDescription.isBlank() || product.productCode.isBlank()) {
                    Log.w(TAG, "Skipping product ${product.productId} with invalid required fields")
                    skippedCount++
                    continue
                }

                Log.d(TAG, "Syncing product: ${product.productId}")
                val result = syncSingleProduct(product.productId, false)
                if (result is Result.Success) {
                    successCount++
                }
            }

            Log.d(
                TAG,
                "Batch sync completed. Success: $successCount, Failed: ${unsyncedProducts.size - successCount - skippedCount}, Skipped: $skippedCount"
            )

            return when {
                successCount == unsyncedProducts.size -> Result.success()
                successCount > 0 && !shouldRetry -> Result.success()
                shouldRetry -> Result.retry()
                else -> Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in batch sync", e)
            return if (shouldRetry) Result.retry() else Result.failure()
        }
    }
}
