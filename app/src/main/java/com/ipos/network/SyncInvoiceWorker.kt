package com.ipos.network

import android.annotation.SuppressLint
import com.ipos.data.data_source.InvoiceDao
import com.ipos.data.data_source.InvoiceLineDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@SuppressLint("RestrictedApi")
@HiltWorker
class SyncInvoiceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao,
    private val invoiceApiService: InvoiceApiService,
    private val deviceManager: com.ipos.app.core.DeviceManager,
    private val connectivityHelper: ConnectivityHelper,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore
) : CoroutineWorker(appContext, params) {

    // Required companion interface
    interface Factory : WorkerAssistedFactory<SyncInvoiceWorker>

    companion object {
        private const val TAG = "SyncInvoiceWorker"
        const val KEY_INVOICE_ID = "invoiceId"
        const val KEY_RETRY = "retry"
        const val KEY_BATCH_SYNC = "batchSync"
    }

    init {
        Log.d("SyncInvoiceWorker", "Worker injected successfully")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check network connectivity first
            if (!connectivityHelper.isNetworkAvailable()) {
                Log.w(TAG, "No network connection available, will retry later")
                return@withContext Result.retry()
            }

            // Check if this is a batch sync or single invoice sync
            val isBatchSync = inputData.getBoolean(KEY_BATCH_SYNC, false)
            val shouldRetry = inputData.getBoolean(KEY_RETRY, true)

            if (isBatchSync) {
                return@withContext performBatchSync(shouldRetry)
            } else {
                val invoiceId = inputData.getLong(KEY_INVOICE_ID, -1L)
                if (invoiceId == -1L) {
                    Log.e(TAG, "Invalid invoice ID provided for sync")
                    return@withContext Result.failure()
                }
                return@withContext syncSingleInvoice(invoiceId, shouldRetry)
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

    private suspend fun syncSingleInvoice(invoiceId: Long, shouldRetry: Boolean): Result {
        // Get invoice from local database
        val invoice = invoiceDao.getInvoiceById(invoiceId) ?: run {
            Log.e(TAG, "Invoice with ID $invoiceId not found in local database")
            return Result.failure()
        }

        // Validate required fields
        if (invoice.status.isEmpty()) {
            Log.e(TAG, "Invoice $invoiceId has null/empty status, skipping sync")
            return Result.failure()
        }

        var firestoreSynced = false
        // 1. Sync directly to Cloud Firestore (awaits completion)
        try {
            syncInvoiceToFirestore(invoice)
            firestoreSynced = true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync failed for invoice $invoiceId: ${e.message}", e)
        }

        // 2. Also try REST API if available
        var apiSynced = false
        try {
            val invoiceDto = invoice.toDto(
                deviceId = deviceManager.getDeviceId(),
                deviceName = deviceManager.getDeviceName()
            )
            val response = invoiceApiService.saveInvoice(invoiceDto)
            if (response.isSuccessful) {
                apiSynced = true
                Log.d(TAG, "Successfully synced invoice $invoiceId via REST API")
            } else {
                Log.w(TAG, "REST API sync returned code ${response.code()}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "REST API sync skipped/unreachable: ${e.message}")
        }

        // If either Firestore or REST API succeeded, mark invoice as synced
        if (firestoreSynced || apiSynced) {
            invoiceDao.markSynced(listOf(invoice.invoiceId))
            Log.d(TAG, "✔ Invoice $invoiceId marked synced (Firestore: $firestoreSynced, API: $apiSynced)")
            return Result.success()
        }

        return if (shouldRetry) Result.retry() else Result.failure()
    }

    @SuppressLint("RestrictedApi")
    private suspend fun performBatchSync(shouldRetry: Boolean): Result {
        try {
            val unsyncedInvoices = invoiceDao.getUnsyncedInvoices(50)
            if (unsyncedInvoices.isEmpty()) {
                Log.d(TAG, "No invoices to sync in batch")
                return Result.success()
            }

            Log.d(TAG, "Starting batch sync of ${unsyncedInvoices.size} invoices")

            val validInvoices = unsyncedInvoices.filter { it.status.isNotEmpty() }
            val skipped = unsyncedInvoices.size - validInvoices.size
            if (skipped > 0) Log.w(TAG, "Skipping $skipped invoices with empty status")

            var firestoreSyncedCount = 0
            // 1. Batch sync to Cloud Firestore
            try {
                val batch = firestore.batch()
                for (inv in validInvoices) {
                    val storeId = if (inv.storeId.isNotEmpty()) inv.storeId else "ISTART"
                    val docRef = firestore.collection("stores").document(storeId)
                        .collection("invoices").document(inv.invoiceId.toString())
                    
                    val map = com.ipos.data.entities.mapInvoiceToFirestore(inv).toMutableMap()
                    map["storeId"] = storeId

                    // Include line items
                    val lines = invoiceLineDao.getInvoiceLinesByInvoiceId(inv.invoiceId)
                    if (lines.isNotEmpty()) {
                        map["items"] = lines.map { line ->
                            mapOf(
                                "productName" to line.productName,
                                "productPrice" to line.productPrice,
                                "productQtySold" to line.productQtySold,
                                "totalAmount" to line.totalAmount
                            )
                        }
                    }
                    batch.set(docRef, map)
                }
                batch.commit().await()
                firestoreSyncedCount = validInvoices.size
                Log.d(TAG, "✔ Batch synced $firestoreSyncedCount invoices to Cloud Firestore!")
            } catch (e: Exception) {
                Log.e(TAG, "Batch Firestore sync error: ${e.message}", e)
            }

            // 2. Try REST API in parallel/fallback
            var apiSynced = false
            try {
                val dtos = validInvoices.map { it.toDto(
                    deviceId = deviceManager.getDeviceId(),
                    deviceName = deviceManager.getDeviceName()
                )}
                val response = invoiceApiService.syncInvoices(dtos)
                if (response.isSuccessful) {
                    apiSynced = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "REST API batch sync skipped/unreachable: ${e.message}")
            }

            // If Firestore batch or REST API succeeded, mark all valid invoices as synced
            if (firestoreSyncedCount > 0 || apiSynced) {
                invoiceDao.markSynced(validInvoices.map { it.invoiceId })
                Log.d(TAG, "✔ Marked ${validInvoices.size} invoices as synced in local DB")
                return Result.success()
            }

            return if (shouldRetry) Result.retry() else Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync unexpected error: ${e.message}", e)
            return if (shouldRetry) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncInvoiceToFirestore(invoice: com.ipos.data.entities.Invoice) {
        val storeId = if (invoice.storeId.isNotEmpty()) invoice.storeId else "ISTART"
        val map = com.ipos.data.entities.mapInvoiceToFirestore(invoice).toMutableMap()
        map["storeId"] = storeId

        // Attach line items so web dashboard shows top products and details
        val lines = invoiceLineDao.getInvoiceLinesByInvoiceId(invoice.invoiceId)
        if (lines.isNotEmpty()) {
            map["items"] = lines.map { line ->
                mapOf(
                    "productName" to line.productName,
                    "productPrice" to line.productPrice,
                    "productQtySold" to line.productQtySold,
                    "totalAmount" to line.totalAmount
                )
            }
        }

        firestore.collection("stores").document(storeId)
            .collection("invoices").document(invoice.invoiceId.toString())
            .set(map)
            .await()

        Log.d(TAG, "✔ Synced invoice ${invoice.invoiceId} with ${lines.size} items to Firestore (stores/$storeId/invoices)")
    }
}
