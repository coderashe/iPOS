package com.ipos.network

import android.annotation.SuppressLint
import com.ipos.data.data_source.InvoiceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    private val invoiceApiService: InvoiceApiService,
    private val deviceManager: com.ipos.app.core.DeviceManager,
    private val connectivityHelper: ConnectivityHelper
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

        // Convert to DTO for API
        val invoiceDto = invoice.toDto(
            deviceId = deviceManager.getDeviceId(),
            deviceName = deviceManager.getDeviceName()
        )
        Log.e(TAG, "Trying to sync invoice $invoiceDto")

        return try {
            val response = invoiceApiService.saveInvoice(invoiceDto)
            if (response.isSuccessful) {
                // Mark as synced unconditionally on any 2xx — don't depend on response body
                invoiceDao.markSynced(listOf(invoice.invoiceId))
                Log.d(TAG, "Successfully synced invoice $invoiceId")
                Result.success()
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string() ?: "No error details"
                Log.e(TAG, "Failed to sync invoice: $errorCode - $errorBody")

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
            Log.e(TAG, "Error syncing invoice: ${e.message}", e)
            if (shouldRetry) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
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

            val dtos = validInvoices.map { it.toDto(
                deviceId = deviceManager.getDeviceId(),
                deviceName = deviceManager.getDeviceName()
            )}

            val response = invoiceApiService.syncInvoices(dtos)

            return if (response.isSuccessful) {
                invoiceDao.markSynced(validInvoices.map { it.invoiceId })
                val synced = response.body()?.get("synced") ?: validInvoices.size
                Log.d(TAG, "✔ Batch sync complete — synced: $synced")
                Result.success()
            } else {
                val code = response.code()
                val error = response.errorBody()?.string() ?: ""
                Log.e(TAG, "✘ Batch sync failed — HTTP $code: $error")
                if (code in 400..499) Result.failure()
                else if (shouldRetry) Result.retry() else Result.failure()
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Batch sync timeout: ${e.message}")
            return if (shouldRetry) Result.retry() else Result.failure()
        } catch (e: java.io.IOException) {
            Log.e(TAG, "Batch sync IO error: ${e.message}")
            return if (shouldRetry) Result.retry() else Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync unexpected error: ${e.message}", e)
            return if (shouldRetry) Result.retry() else Result.failure()
        }
    }

}
