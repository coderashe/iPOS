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
    private val productApiService: ProductApiService,
    private val deviceManager: com.example.learncompose303.app.core.DeviceManager,
    private val connectivityHelper: ConnectivityHelper,
    private val priceListDao: com.example.learncompose303.data.data_source.PriceListDao
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
        Log.d("ProductSync", "doWork() started — runAttemptCount=${runAttemptCount}")
        try {
            if (!connectivityHelper.isNetworkAvailable()) {
                Log.w("ProductSync", "No network — retrying")
                return@withContext Result.retry()
            }

            val isBatchSync = inputData.getBoolean(KEY_BATCH_SYNC, false)
            val shouldRetry = inputData.getBoolean(KEY_RETRY, true)
            Log.d("ProductSync", "isBatchSync=$isBatchSync shouldRetry=$shouldRetry")

            if (isBatchSync) {
                return@withContext performBatchSync(shouldRetry)
            } else {
                val productId = inputData.getLong(KEY_PRODUCT_ID, -1L)
                Log.d("ProductSync", "single productId=$productId")
                if (productId == -1L) {
                    Log.e("ProductSync", "Invalid product ID")
                    return@withContext Result.failure()
                }
                return@withContext syncSingleProduct(productId, shouldRetry)
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error: Cannot reach server", e)
            return@withContext Result.retry()
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout: Server not responding", e)
            return@withContext Result.retry()
        } catch (e: java.io.IOException) {
            Log.e(TAG, "Network I/O error", e)
            return@withContext Result.retry()
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

        // Fetch price from PriceList
        val priceList = priceListDao.getPriceListByProductId(productId)
        val sellingPrice = priceList?.sellingPrice ?: 0.0
        val unitCost = priceList?.unitCost ?: 0.0
        val markupPercentage = priceList?.markupPercentage ?: 0.0

        if (sellingPrice <= 0.0) {
            Log.w(TAG, "Product $productId has no valid price — skipping, marking as synced")
            productDao.markSynced(productId)
            return Result.success()
        }

        val productDto = product.toDto(
            deviceId = deviceManager.getDeviceId(),
            deviceName = deviceManager.getDeviceName(),
            sellingPrice = sellingPrice,
            unitCost = unitCost,
            markupPercentage = markupPercentage
        )
        Log.d(TAG, "Syncing product $productDto")

        return try {
            val response = productApiService.saveProduct(productDto)
            if (response.isSuccessful) {
                response.body()?.let {
                    val updatedProduct = product.copy(lastUpdated = System.currentTimeMillis())
                    productDao.updateProduct(updatedProduct)
                    productDao.markSynced(productId)
                    Log.d(TAG, "Successfully synced product $productId")
                }
                Result.success()
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string() ?: "No error details"
                Log.e(TAG, "Failed to sync product: $errorCode - $errorBody")

                // Don't retry on client errors (4xx)
                if (errorCode in 400..499) {
                    Log.e(TAG, "Client error ($errorCode), not retrying")
                    return Result.failure()
                }

                if (shouldRetry) Result.retry() else Result.failure()
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
            Log.e(TAG, "Error syncing product: ${e.message}", e)
            if (shouldRetry) Result.retry() else Result.failure()
        }
    }

    private suspend fun performBatchSync(shouldRetry: Boolean): Result {
        try {
            var totalSynced = 0
            while (true) {
                val unsyncedProducts = productDao.getUnsyncedProducts(100)
                Log.d("ProductSync", "fetchUnsynced returned ${unsyncedProducts.size} products")
                if (unsyncedProducts.isEmpty()) {
                    Log.d("ProductSync", "all done, total synced: $totalSynced")
                    android.util.Log.d("backIntCheck", "SyncProductWorker — all done, total synced: $totalSynced")
                    return Result.success()
                }

                android.util.Log.d("backIntCheck", "SyncProductWorker — syncing batch of ${unsyncedProducts.size} (total so far: $totalSynced)")

                val dtos = unsyncedProducts.mapNotNull { product ->
                    if (product.productDescription.isBlank() || product.productCode.isBlank()) {
                        Log.w("ProductSync", "Skipping product ${product.productId} — blank fields")
                        productDao.markSynced(product.productId)
                        null
                    } else {
                        val priceList = priceListDao.getPriceListByProductId(product.productId)
                        Log.d("ProductSync", "product ${product.productId} priceList=${priceList?.sellingPrice}")
                        product.toDto(
                            deviceId = deviceManager.getDeviceId(),
                            deviceName = deviceManager.getDeviceName(),
                            sellingPrice = priceList?.sellingPrice ?: 0.0,
                            unitCost = priceList?.unitCost ?: 0.0,
                            markupPercentage = priceList?.markupPercentage ?: 0.0
                        )
                    }
                }

                Log.d("ProductSync", "DTOs to send: ${dtos.size}")
                if (dtos.isEmpty()) continue

                try {
                    val response = productApiService.saveProducts(dtos)
                    Log.d("ProductSync", "API response: ${response.code()} isSuccessful=${response.isSuccessful}")
                    when {
                        response.isSuccessful -> {
                            val synced = response.body()?.synced ?: dtos.size
                            unsyncedProducts.forEach { p ->
                                productDao.markSynced(p.productId)
                                productDao.updateProduct(p.copy(lastUpdated = System.currentTimeMillis()))
                            }
                            totalSynced += synced
                            Log.d("ProductSync", "Batch synced $synced products")
                        }
                        response.code() in 400..499 -> {
                            Log.e("ProductSync", "Client error ${response.code()} — body: ${response.errorBody()?.string()}")
                            unsyncedProducts.forEach { productDao.markSynced(it.productId) }
                        }
                        else -> {
                            Log.e("ProductSync", "Server error ${response.code()} — retrying")
                            return if (shouldRetry) Result.retry() else Result.failure()
                        }
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    Log.e("ProductSync", "Timeout hitting ${e.message}")
                    return if (shouldRetry) Result.retry() else Result.failure()
                } catch (e: java.io.IOException) {
                    Log.e("ProductSync", "IOException: ${e.message}")
                    return if (shouldRetry) Result.retry() else Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e("ProductSync", "Unexpected error in batch: ${e.message}", e)
            return if (shouldRetry) Result.retry() else Result.failure()
        }
    }
}
