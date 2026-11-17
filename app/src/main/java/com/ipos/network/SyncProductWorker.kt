package com.ipos.network

import android.annotation.SuppressLint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ipos.data.data_source.ProductDao
import kotlinx.coroutines.tasks.await
import com.ipos.data.entities.Products
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
    private val deviceManager: com.ipos.app.core.DeviceManager,
    private val connectivityHelper: ConnectivityHelper,
    private val priceListDao: com.ipos.data.data_source.PriceListDao,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore
) : CoroutineWorker(appContext, params) {

    interface Factory : WorkerAssistedFactory<SyncProductWorker>

    companion object {
        private const val TAG = "SyncProductWorker"
        const val KEY_PRODUCT_ID = "productId"
        const val KEY_RETRY = "retry"
        const val KEY_BATCH_SYNC = "batchSync"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!connectivityHelper.isNetworkAvailable()) {
                return@withContext Result.retry()
            }

            val isBatchSync = inputData.getBoolean(KEY_BATCH_SYNC, false)
            val shouldRetry = inputData.getBoolean(KEY_RETRY, true)

            if (isBatchSync) {
                return@withContext performBatchSync(shouldRetry)
            } else {
                val productId = inputData.getLong(KEY_PRODUCT_ID, -1L)
                if (productId == -1L) {
                    Log.e(TAG, "SyncProductWorker — invalid product ID")
                    return@withContext Result.failure()
                }
                return@withContext syncSingleProduct(productId, shouldRetry)
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "SyncProductWorker — cannot reach server", e)
            return@withContext Result.retry()
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "SyncProductWorker — timeout", e)
            return@withContext Result.retry()
        } catch (e: java.io.IOException) {
            Log.e(TAG, "SyncProductWorker — IOException", e)
            return@withContext Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "SyncProductWorker — unexpected error", e)
            return@withContext if (inputData.getBoolean(KEY_RETRY, true)) Result.retry() else Result.failure()
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

        var firestoreSynced = false
        try {
            syncProductToFirestore(product, sellingPrice, unitCost, markupPercentage)
            firestoreSynced = true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore product sync failed for $productId: ${e.message}", e)
        }

        var apiSynced = false
        try {
            val productDto = product.toDto(
                deviceId = deviceManager.getDeviceId(),
                deviceName = deviceManager.getDeviceName(),
                sellingPrice = sellingPrice,
                unitCost = unitCost,
                markupPercentage = markupPercentage
            )
            val response = productApiService.saveProduct(productDto)
            if (response.isSuccessful) {
                apiSynced = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "REST API product sync skipped/unreachable: ${e.message}")
        }

        if (firestoreSynced || apiSynced) {
            productDao.markSynced(productId)
            Log.d(TAG, "✔ Product $productId marked synced (Firestore: $firestoreSynced, API: $apiSynced)")
            return Result.success()
        }

        return if (shouldRetry) Result.retry() else Result.failure()
    }

    private suspend fun performBatchSync(shouldRetry: Boolean): Result {
        try {
            var totalSynced = 0
            while (true) {
                val unsyncedProducts = productDao.getUnsyncedProducts(100)
                if (unsyncedProducts.isEmpty()) {
                    Log.d(TAG, "SyncProductWorker — done (synced: $totalSynced)")
                    return Result.success()
                }

                val dtos = unsyncedProducts.mapNotNull { product ->
                    if (product.productDescription.isBlank() || product.productCode.isBlank()) {
                        productDao.markSynced(product.productId)
                        null
                    } else {
                        val priceList = priceListDao.getPriceListByProductId(product.productId)
                        product.toDto(
                            deviceId = deviceManager.getDeviceId(),
                            deviceName = deviceManager.getDeviceName(),
                            sellingPrice = priceList?.sellingPrice ?: 0.0,
                            unitCost = priceList?.unitCost ?: 0.0,
                            markupPercentage = priceList?.markupPercentage ?: 0.0
                        )
                    }
                }

                if (dtos.isEmpty()) continue

                // 1. Batch sync to Cloud Firestore
                var firestoreSynced = false
                try {
                    val batch = firestore.batch()
                    unsyncedProducts.forEach { p ->
                        val priceList = priceListDao.getPriceListByProductId(p.productId)
                        val storeId = "ISTART"
                        val docRef = firestore.collection("stores").document(storeId)
                            .collection("products").document(p.productId.toString())
                        val map = hashMapOf(
                            "productId" to p.productId,
                            "productCode" to p.productCode,
                            "productName" to p.productDescription,
                            "currentStock" to p.productQty,
                            "sellingPrice" to (priceList?.sellingPrice ?: 0.0),
                            "unitCost" to (priceList?.unitCost ?: 0.0),
                            "markupPercentage" to (priceList?.markupPercentage ?: 0.0),
                            "storeId" to storeId,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                        batch.set(docRef, map)
                    }
                    batch.commit().await()
                    firestoreSynced = true
                    Log.d(TAG, "✔ Batch synced ${unsyncedProducts.size} products to Cloud Firestore")
                } catch (e: Exception) {
                    Log.e(TAG, "Batch Firestore product sync error: ${e.message}", e)
                }

                // 2. Try REST API in parallel/fallback
                var apiSynced = false
                try {
                    val response = productApiService.saveProducts(dtos)
                    if (response.isSuccessful) {
                        apiSynced = true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "REST API product batch sync skipped: ${e.message}")
                }

                if (firestoreSynced || apiSynced) {
                    unsyncedProducts.forEach { p ->
                        productDao.markSynced(p.productId)
                    }
                    totalSynced += unsyncedProducts.size
                } else if (shouldRetry) {
                    return Result.retry()
                } else {
                    return Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SyncProductWorker — unexpected batch error: ${e.message}", e)
            return if (shouldRetry) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncProductToFirestore(
        product: Products,
        sellingPrice: Double,
        unitCost: Double,
        markupPercentage: Double
    ) {
        val storeId = "ISTART"
        val map = hashMapOf(
            "productId" to product.productId,
            "productCode" to product.productCode,
            "productName" to product.productDescription,
            "currentStock" to product.productQty,
            "sellingPrice" to sellingPrice,
            "unitCost" to unitCost,
            "markupPercentage" to markupPercentage,
            "storeId" to storeId,
            "lastUpdated" to System.currentTimeMillis()
        )

        firestore.collection("stores").document(storeId)
            .collection("products").document(product.productId.toString())
            .set(map)
            .await()

        Log.d(TAG, "✔ Synced product ${product.productDescription} (Stock: ${product.productQty}) to Firestore")
    }
}
